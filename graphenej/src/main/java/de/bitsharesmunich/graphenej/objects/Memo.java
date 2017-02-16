package de.bitsharesmunich.graphenej.objects;

import com.google.common.primitives.Bytes;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.PublicKey;
import de.bitsharesmunich.graphenej.Util;
import de.bitsharesmunich.graphenej.errors.ChecksumException;
import de.bitsharesmunich.graphenej.interfaces.ByteSerializable;
import de.bitsharesmunich.graphenej.interfaces.JsonSerializable;
import org.bitcoinj.core.ECKey;
import org.spongycastle.math.ec.ECPoint;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by nelson on 11/9/16.
 */
public class Memo implements ByteSerializable, JsonSerializable {
    public final static String TAG = "Memo";
    public static final String KEY_FROM = "from";
    public static final String KEY_TO = "to";
    public static final String KEY_NONCE = "nonce";
    public static final String KEY_MESSAGE = "message";

    private Address from;
    private Address to;
    private long nonce;
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
    public Memo(Address from, Address to, long nonce, byte[] message){
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

    public long getNonce(){
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
    public static byte[] encryptMessage(ECKey privateKey, PublicKey publicKey, long nonce, String message){
        byte[] encrypted = null;
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");

            // Getting nonce bytes
            String stringNonce = String.format("%d", nonce);
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
    public static byte[] encryptMessage(ECKey privateKey, Address destinationAddress, long nonce, String message){
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
    public static String decryptMessage(ECKey privateKey, PublicKey publicKey, long nonce, byte[] message) throws ChecksumException {
        String plaintext = "";
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");

            // Getting nonce bytes
            String stringNonce = String.format("%d", nonce);
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
    public static String decryptMessage(ECKey privateKey, Address sourceAddress, long nonce, byte[] message) throws ChecksumException {
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
            byte[] nonceBytes = Util.revertLong(nonce);

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

    @Override
    public String toJsonString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JsonElement toJsonObject() {
        JsonObject memoObject = new JsonObject();
        if ((this.from == null) && (this.to == null)) {
            // Public memo
            // TODO: Add public memo support
//            memoObject.addProperty(KEY_FROM, "");
//            memoObject.addProperty(KEY_TO, "");
//            memoObject.addProperty(KEY_NONCE, "");
//            memoObject.addProperty(KEY_MESSAGE, Util.bytesToHex(this.message));
            return null;
        }else{
            memoObject.addProperty(KEY_FROM, this.from.toString());
            memoObject.addProperty(KEY_TO, this.to.toString());
            memoObject.addProperty(KEY_NONCE, String.format("%d", this.nonce));
            memoObject.addProperty(KEY_MESSAGE, Util.bytesToHex(this.message));
        }
        return memoObject;
    }
}