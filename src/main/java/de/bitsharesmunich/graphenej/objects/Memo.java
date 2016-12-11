package de.bitsharesmunich.graphenej.objects;

import com.google.common.primitives.Bytes;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.bitsharesmunich.graphenej.PublicKey;
import de.bitsharesmunich.graphenej.Util;
import de.bitsharesmunich.graphenej.crypto.SecureRandomStrengthener;
import de.bitsharesmunich.graphenej.interfaces.ByteSerializable;
import de.bitsharesmunich.graphenej.interfaces.JsonSerializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by nelson on 11/9/16.
 */
public class Memo implements ByteSerializable, JsonSerializable {

    public static final String KEY_FROM = "from";
    public static final String KEY_TO = "to";
    public static final String KEY_NONCE = "nonce";
    public static final String KEY_MESSAGE = "message";

    private PublicKey from;
    private PublicKey to;
    private final byte[] nonce = new byte[8];
    private byte[] message;

    /**
     * Empty Constructor
     */
    public Memo() {
        this.from = null;
        this.to = null;
        this.message = null;
    }
    
    /**
     * Implement metod, serialized this Object
     * @return the byte array of this object serialized
     */
    @Override
    public byte[] toBytes() {
        if ((this.from == null) || (this.to == null) || (this.message == null)) {
            return new byte[]{(byte) 0};
        } else {
            byte[] nonceformat = new byte[nonce.length];
            for (int i = 0; i < nonceformat.length; i++) {
                nonceformat[i] = nonce[nonce.length - i - 1];
            }
            return Bytes.concat(new byte[]{1}, this.from.toBytes(), this.to.toBytes(), nonceformat, new byte[]{(byte) this.message.length}, this.message);
        }
    }

    /**
     * Encode a memo message using the input source key and destination key
     * @param fromKey The source destination, need to have a private key accesss
     * @param toKey The destination, needs only the public key access
     * @param msg The message in bytes
     * @return a Memo corresponding with the message encoding
     */
    public static Memo encodeMessage(PublicKey fromKey, PublicKey toKey, byte[] msg) {
        return encodeMessage(fromKey, toKey, msg, 0);
    }

    /**
     * Encode a message a return a memo with that message encoded
     * 
     * @param fromKey The source destination, need to have a private key accesss
     * @param toKey The destination, needs only the public key access
     * @param msg The message in bytes
     * @param custom_nonce the custom nonce to be use or 0 to create a new one
     * @return a Memo corresponding with the message encoding
     */
    public static Memo encodeMessage(PublicKey fromKey, PublicKey toKey, byte[] msg, long custom_nonce) {
        Memo memo = new Memo();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            memo.from = fromKey;
            memo.to = toKey;

            if (custom_nonce == 0) {
                SecureRandomStrengthener randomStrengthener = SecureRandomStrengthener.getInstance();
                //randomStrengthener.addEntropySource(new AndroidRandomSource());
                SecureRandom secureRandom = randomStrengthener.generateAndSeedRandomNumberGenerator();
                secureRandom.nextBytes(memo.nonce);

                long time = System.currentTimeMillis();

                for (int i = 7; i >= 1; i--) {
                    memo.nonce[i] = (byte) (time & 0xff);
                    time = time / 0x100;
                }
            } else {
                for (int i = 7; i >= 0; i--) {
                    memo.nonce[i] = (byte) (custom_nonce & 0xff);
                    custom_nonce = custom_nonce / 0x100;
                }
            }

            byte[] secret = toKey.getKey().getPubKeyPoint().multiply(fromKey.getKey().getPrivKey()).normalize().getXCoord().getEncoded();
            byte[] finalKey = new byte[secret.length + memo.nonce.length];
            System.arraycopy(secret, 0, finalKey, 0, secret.length);
            System.arraycopy(memo.nonce, 0, finalKey, secret.length, memo.nonce.length);

            byte[] sha256Msg = md.digest(msg);
            byte[] serialChecksum = new byte[4];
            System.arraycopy(sha256Msg, 0, serialChecksum, 0, 4);
            byte[] msgFinal = new byte[serialChecksum.length + msg.length];
            System.arraycopy(serialChecksum, 0, msgFinal, 0, serialChecksum.length);
            System.arraycopy(msg, 0, msgFinal, serialChecksum.length, msg.length);
            memo.message = Util.encryptAES(msgFinal, finalKey);
        } catch (NoSuchAlgorithmException ex) {
        }
        return memo;
    }

    /**
     * returns the string coreesponding a encode memo
     * 
     * @param fromKey The soruce key, need to have public key only
     * @param toKey The destination key, need to have private key access
     * @param msg The message to be decoded
     * @param nonce The nonce used in the decoded message
     * @return The message
     */
    public static String decodeMessage(PublicKey fromKey, PublicKey toKey, byte[] msg, byte[] nonce) {

        byte[] secret = fromKey.getKey().getPubKeyPoint().multiply(toKey.getKey().getPrivKey()).normalize().getXCoord().getEncoded();
        byte[] finalKey = new byte[secret.length + nonce.length];
        System.arraycopy(secret, 0, finalKey, 0, secret.length);
        System.arraycopy(nonce, 0, finalKey, secret.length, nonce.length);

        byte[] msgFinal = Util.decryptAES(msg, finalKey);
        byte[] decodedMsg = new byte[msgFinal.length - 4];
        //TODO verify checksum for integrity
        System.arraycopy(msgFinal, 4, decodedMsg, 0, decodedMsg.length);
        return new String(decodedMsg);
    }

    /**
     * returns the string coreesponding a encode memo
     * 
     * @param fromKey The soruce key, need to have public key only
     * @param toKey The destination key, need to have private key access
     * @param message The message to be decoded
     * @param nonce The nonce used in the decoded message
     * @return The message
     */
    public static String decodeMessage(PublicKey fromKey, PublicKey toKey, String message, String nonce) {
        byte[] msg = new BigInteger(message, 16).toByteArray();
        if (msg[0] == 0) {
            byte[] temp = new byte[msg.length - 1];
            System.arraycopy(msg, 1, temp, 0, temp.length);
            msg = temp;
        }
        byte[] firstNonce = new BigInteger(nonce, 10).toByteArray();
        byte[] nonceByte = new byte[8];
        System.arraycopy(firstNonce, firstNonce.length - 8, nonceByte, 0, nonceByte.length);
        return decodeMessage(fromKey, toKey, msg, nonceByte);
    }

    @Override
    public String toJsonString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JsonElement toJsonObject() {
        if ((this.from == null) || (this.to == null) || (this.nonce == null) || (this.message == null)) {
            return null;
        }
        JsonObject memoObject = new JsonObject();
        memoObject.addProperty(KEY_FROM, this.from.getAddress());
        memoObject.addProperty(KEY_TO, this.to.getAddress());
        memoObject.addProperty(KEY_NONCE, new BigInteger(1, this.nonce).toString(10));
        memoObject.addProperty(KEY_MESSAGE, new BigInteger(1, this.message).toString(16));
        return memoObject;
    }

}
