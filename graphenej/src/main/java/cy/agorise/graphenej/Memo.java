package cy.agorise.graphenej;

import com.google.common.primitives.Bytes;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.bitcoinj.core.ECKey;
import org.spongycastle.math.ec.ECPoint;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import cy.agorise.graphenej.errors.ChecksumException;
import cy.agorise.graphenej.errors.MalformedAddressException;
import cy.agorise.graphenej.interfaces.ByteSerializable;
import cy.agorise.graphenej.interfaces.JsonSerializable;

/**
 * Class used to represent a memo data structure
 * {@url https://bitshares.org/doxygen/structgraphene_1_1chain_1_1memo__data.html}
 */
public class Memo implements ByteSerializable, JsonSerializable {
    public static final String KEY_FROM = "from";
    public static final String KEY_TO = "to";
    public static final String KEY_NONCE = "nonce";
    public static final String KEY_MESSAGE = "message";

    private Address from;
    private Address to;
    private BigInteger nonce;
    private byte[] message;
    private String plaintextMessage;

    public String getPlaintextMessage() {
        if(plaintextMessage == null)
            return "";
        else
            return plaintextMessage;
    }

    public void setPlaintextMessage(String plaintextMessage) {
        this.plaintextMessage = plaintextMessage;
    }

    /**
     * Empty Constructor
     */
    public Memo() {
        this.from = null;
        this.to = null;
        this.message = null;
    }

    /**
     * Constructor used for private memos.
     * @param from: Address of sender
     * @param to: Address of recipient.
     * @param nonce: Nonce used in the encryption.
     * @param message: Message in ciphertext.
     */
    public Memo(Address from, Address to, BigInteger nonce, byte[] message){
        this.from = from;
        this.to = to;
        this.nonce = nonce;
        this.message = message;
    }

    /**
     * Constructor intended to be used with public memos
     * @param message: Message in plaintext.
     */
    public Memo(String message){
        this.message = message.getBytes();
    }

    public Address getSource(){
        return this.from;
    }

    public Address getDestination(){
        return this.to;
    }

    public BigInteger getNonce(){
        return this.nonce;
    }

    public byte[] getByteMessage(){
        return this.message;
    }

    public String getStringMessage(){
        if(this.message != null)
            return new String(this.message);
        else
            return "";
    }

    /**
     * Method used to decrypt memo data.
     * @param privateKey: Private key of the sender.
     * @param publicKey: Public key of the recipient.
     * @param nonce: The nonce.
     * @param message: Plaintext message.
     * @return: The encrypted version of the message.
     */
    public static byte[] encryptMessage(ECKey privateKey, PublicKey publicKey, BigInteger nonce, String message){
        byte[] encrypted = null;
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");

            // Getting nonce bytes
            String stringNonce = nonce.toString();
            byte[] nonceBytes = Arrays.copyOfRange(Util.hexlify(stringNonce), 0, stringNonce.length());

            // Getting shared secret
            byte[] secret = publicKey.getKey().getPubKeyPoint().multiply(privateKey.getPrivKey()).normalize().getXCoord().getEncoded();

            // SHA-512 of shared secret
            byte[] ss = sha512.digest(secret);

            byte[] seed = Bytes.concat(nonceBytes, Util.hexlify(Util.bytesToHex(ss)));

            // Calculating checksum
            byte[] sha256Msg = sha256.digest(message.getBytes());
            byte[] checksum = Arrays.copyOfRange(sha256Msg, 0, 4);

            // Concatenating checksum + message bytes
            byte[] msgFinal = Bytes.concat(checksum, message.getBytes());

            // Applying encryption
            encrypted = Util.encryptAES(msgFinal, seed);
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("NoSuchAlgotithmException. Msg:"+ ex.getMessage());
        }
        return encrypted;
    }

    /**
     * Method used to encrypt memo data.
     * @param privateKey: Private key of the sender.
     * @param destinationAddress: Address of the recipient.
     * @param nonce: The nonce.
     * @param message: Plaintext message.
     * @return: The encrypted version of the message.
     */
    public static byte[] encryptMessage(ECKey privateKey, Address destinationAddress, BigInteger nonce, String message){
        return encryptMessage(privateKey, destinationAddress.getPublicKey(), nonce, message);
    }


    /**
     * Method used to decrypt memo data.
     * @param privateKey: The private key of the recipient.
     * @param publicKey: The public key of the sender.
     * @param nonce: The nonce.
     * @param message: The encrypted message.
     * @return: The plaintext version of the enrcrypted message.
     * @throws ChecksumException
     */
    public static String decryptMessage(ECKey privateKey, PublicKey publicKey, BigInteger nonce, byte[] message) throws ChecksumException {
        String plaintext = "";
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");

            // Getting nonce bytes
            String stringNonce = nonce.toString();
            byte[] nonceBytes = Arrays.copyOfRange(Util.hexlify(stringNonce), 0, stringNonce.length());

            // Getting shared secret
            byte[] secret = publicKey.getKey().getPubKeyPoint().multiply(privateKey.getPrivKey()).normalize().getXCoord().getEncoded();

            // SHA-512 of shared secret
            byte[] ss = sha512.digest(secret);

            byte[] seed = Bytes.concat(nonceBytes, Util.hexlify(Util.bytesToHex(ss)));

            // Calculating checksum
            byte[] sha256Msg = sha256.digest(message);


            // Applying decryption
            byte[] temp = Util.decryptAES(message, seed);
            byte[] checksum = Arrays.copyOfRange(temp, 0, 4);
            byte[] decrypted = Arrays.copyOfRange(temp, 4, temp.length);
            plaintext = new String(decrypted);
            byte[] checksumConfirmation = Arrays.copyOfRange(sha256.digest(decrypted), 0, 4);
            boolean checksumVerification = Arrays.equals(checksum, checksumConfirmation);
            if(!checksumVerification){
                throw new ChecksumException("Invalid checksum found while performing decryption");
            }
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgotithmException. Msg:"+ e.getMessage());
        }
        return plaintext;
    }

    /**
     * Method used to decrypt memo data.
     * @param privateKey: The private key of the recipient.
     * @param sourceAddress: The public address key of the sender.
     * @param nonce: The nonce.
     * @param message: The encrypted message.
     * @return: The plaintext version of the enrcrypted message.
     * @throws ChecksumException
     */
    public static String decryptMessage(ECKey privateKey, Address sourceAddress, BigInteger nonce, byte[] message) throws ChecksumException {
        return decryptMessage(privateKey, sourceAddress.getPublicKey(), nonce, message);
    }


    /**
     * Implement metod, serialized this Object
     * @return the byte array of this object serialized
     */
    @Override
    public byte[] toBytes() {
        if ((this.from == null) && (this.to == null) && (this.message == null)) {
            return new byte[]{(byte) 0};
        } else if(this.from == null && this.to == null & this.message != null){
            return Bytes.concat(new byte[]{1},
                    new byte[]{(byte)0},
                    new byte[]{(byte)0},
                    new byte[]{(byte)0},
                    new byte[]{(byte) this.message.length},
                    this.message);
        } else {

            byte[] paddedNonceBytes = new byte[8];
            byte[] originalNonceBytes = nonce.toByteArray();
            System.arraycopy(originalNonceBytes, 0, paddedNonceBytes, 8 - originalNonceBytes.length, originalNonceBytes.length);
            byte[] nonceBytes = Util.revertBytes(paddedNonceBytes);
//            byte[] nonceBytes = Util.revertBytes(nonce.toByteArray());

            ECPoint senderPoint = ECKey.compressPoint(from.getPublicKey().getKey().getPubKeyPoint());
            PublicKey senderPublicKey = new PublicKey(ECKey.fromPublicOnly(senderPoint));

            ECPoint recipientPoint = ECKey.compressPoint(to.getPublicKey().getKey().getPubKeyPoint());
            PublicKey recipientPublicKey = new PublicKey(ECKey.fromPublicOnly(recipientPoint));

            return Bytes.concat(new byte[]{1},
                    senderPublicKey.toBytes(),
                    recipientPublicKey.toBytes(),
                    nonceBytes,
                    new byte[]{(byte) this.message.length},
                    this.message);
        }
    }

    /**
     * Converts a memo instance to a String.
     *
     * The nonce is always encoded as a decimal number in a string field.
     * @return
     */
    @Override
    public String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(toJson(true));
    }

    /**
     * Converts a memo instance into a JsonElement.
     *
     * This method differs from the {@link #toJson(boolean)} in that here the nonce is encoded as an
     * hexadecimal number, while in that one we offer the user to choose either the decimal or hexadecimal
     * representations.
     *
     * @return JsonObject instance representing this memo
     */
    @Override
    public JsonElement toJsonObject() {
        JsonObject memoObject = new JsonObject();
        if ((this.from == null) && (this.to == null)) {
            // TODO: Check if this public memo serialization is accepted
            memoObject.addProperty(KEY_FROM, "");
            memoObject.addProperty(KEY_TO, "");
            memoObject.addProperty(KEY_NONCE, "");
            if(this.message != null)
                memoObject.addProperty(KEY_MESSAGE, Util.bytesToHex(this.message));
            return null;
        }else{
            memoObject.addProperty(KEY_FROM, this.from.toString());
            memoObject.addProperty(KEY_TO, this.to.toString());
            memoObject.addProperty(KEY_NONCE, String.format("%x", this.nonce));
            if(this.message != null)
                memoObject.addProperty(KEY_MESSAGE, Util.bytesToHex(this.message));
        }
        return memoObject;
    }

    /**
     * Method that converts the memo into a JsonObject.
     *
     * @param decimal If true, the nonce is saved as string containing a decimal number representation.
     * @return JsonObject instance representing this memo
     */
    public JsonElement toJson(boolean decimal){
        JsonElement jsonElement = toJsonObject();
        if(decimal && jsonElement != null){
            JsonObject jsonObject = (JsonObject) jsonElement;
            // The nonce is interpreted in base 16, but it is going to be written in base 10
            BigInteger nonce = new BigInteger(jsonObject.get(KEY_NONCE).getAsString(), 16);
            jsonObject.addProperty(KEY_NONCE, nonce.toString());
        }
        return jsonElement;
    }

    /**
     * Class used to deserialize a memo
     */
    public static class MemoDeserializer implements JsonDeserializer<Memo> {

        @Override
        public Memo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String fromAddress = jsonObject.get(KEY_FROM).getAsString();
            String toAddress = jsonObject.get(KEY_TO).getAsString();

            // Apparently the nonce is always coming from the full node as a string containing a
            // decimal number. This is at odds with the result of the #toJsonObject method
            // which encodes this data in hexadecimal.
            BigInteger nonce = new BigInteger(jsonObject.get(KEY_NONCE).getAsString(), 10);

            String msg = jsonObject.get(KEY_MESSAGE).getAsString();
            Memo memo = null;
            try{
                Address from = new Address(fromAddress);
                Address to = new Address(toAddress);
                byte[] message = Util.hexToBytes(msg);

                memo = new Memo(from, to, nonce, message);
            }catch(MalformedAddressException e){
                System.out.println("MalformedAddressException. Msg: "+e.getMessage());
            }
            return memo;
        }
    }

    /**
     * Class used to serialize a memo
     */
    public static class MemoSerializer implements JsonSerializer<Memo> {

        @Override
        public JsonElement serialize(Memo memo, Type typeOfSrc, JsonSerializationContext context) {
            return memo.toJson(true);
        }
    }
 }