package com.luminiasoft.bitshares;

import com.google.common.primitives.Bytes;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.luminiasoft.bitshares.crypto.SecureRandomStrengthener;
import com.luminiasoft.bitshares.interfaces.ByteSerializable;
import com.luminiasoft.bitshares.interfaces.JsonSerializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;

/**
 * Created by nelson on 11/9/16.
 */
public class Memo implements ByteSerializable, JsonSerializable {
     public static final String KEY_FROM = "from";
     public static final String KEY_TO = "to";
     public static final String KEY_NONCE = "nonce";
     public static final String KEY_MESSAGE = "message";

    //TODO: Give this class a proper implementation
    private PublicKey from;
    private PublicKey to;
    private byte[] nonce = new byte[8];
    private byte[] message;

    @Override
    public byte[] toBytes() {
        if ((this.from == null) || (this.to == null) || (this.nonce == null) || (this.message == null)) {
            return new byte[]{(byte) 0};
        }
        return Bytes.concat(this.from.toBytes(), this.to.toBytes(), this.nonce, this.message);
    }

    public Memo() {
        this.from = null;
        this.to = null;
        this.message = null;
    }

    public void encodeMessage(ECKey fromKey, ECKey toKey, byte[] msg) {
        this.encodeMessage(fromKey, toKey, msg, 0);
    }

    public void encodeMessage(ECKey fromKey, ECKey toKey, byte[] msg, long custom_nonce) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            this.from = new PublicKey(fromKey);
            this.to = new PublicKey(toKey);

            if (custom_nonce == 0) {
                SecureRandomStrengthener randomStrengthener = SecureRandomStrengthener.getInstance();
                //randomStrengthener.addEntropySource(new AndroidRandomSource());
                SecureRandom secureRandom = randomStrengthener.generateAndSeedRandomNumberGenerator();
                secureRandom.nextBytes(nonce);

                long time = System.currentTimeMillis();

                for (int i = 7; i >= 1; i--) {
                    this.nonce[i] = (byte) (time & 0xff);
                    time = time / 0x100;
                }
            } else {
                for (int i = 7; i >= 0; i--) {
                    this.nonce[i] = (byte) (custom_nonce & 0xff);
                    custom_nonce = custom_nonce / 0x100;
                }
            }

            byte[] secret = fromKey.getPubKeyPoint().multiply(toKey.getPrivKey()).normalize().getXCoord().getEncoded();
            byte[] finalKey = new byte[secret.length + this.nonce.length];
            System.arraycopy(secret, 0, finalKey, 0, secret.length);
            System.arraycopy(this.nonce, 0, finalKey, secret.length, this.nonce.length);

            byte[] sha256Msg = md.digest(msg);
            byte[] serialChecksum = new byte[4];
            System.arraycopy(sha256Msg, 0, serialChecksum, 0, 4);
            byte[] msgFinal = new byte[serialChecksum.length + msg.length];
            System.arraycopy(serialChecksum, 0, msgFinal, 0, serialChecksum.length);
            System.arraycopy(msg, 0, msgFinal, serialChecksum.length, msg.length);

            this.message = Util.encryptAES(msgFinal, finalKey);
        } catch (NoSuchAlgorithmException ex) {

        }
    }

    public void decodeMessage(ECKey toKey, ECKey fromKey, byte[] msg, byte[] nonce) {
        this.to = new PublicKey(toKey);
        this.from = new PublicKey(fromKey);
        this.nonce = nonce;

        byte[] secret = toKey.getPubKeyPoint().multiply(fromKey.getPrivKey()).normalize().getXCoord().getEncoded();
        byte[] finalKey = new byte[secret.length + this.nonce.length];
        System.arraycopy(secret, 0, finalKey, 0, secret.length);
        System.arraycopy(this.nonce, 0, finalKey, secret.length, this.nonce.length);

        byte[] msgFinal = Util.decryptAES(msg, finalKey);
        byte[] decodedMsg = new byte[msgFinal.length - 4];
        //TODO verify checksum for integrity
        System.arraycopy(msgFinal, 4, decodedMsg, 0, decodedMsg.length);
        this.message = decodedMsg;
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
        memoObject.addProperty(KEY_FROM, new Address(this.from.getKey()).toString());
        memoObject.addProperty(KEY_TO, new Address(this.to.getKey()).toString());
        //memoObject.addProperty(KEY_NONCE, new BigInteger(1,this.nonce).toString(10));
        memoObject.addProperty(KEY_NONCE, new BigInteger(1,this.nonce).toString(10));
        memoObject.addProperty(KEY_MESSAGE, new BigInteger(1,this.message).toString(16));
        return memoObject;
    }

}
