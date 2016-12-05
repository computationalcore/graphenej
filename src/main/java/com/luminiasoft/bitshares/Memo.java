package com.luminiasoft.bitshares;

import com.luminiasoft.bitshares.crypto.SecureRandomStrengthener;
import com.luminiasoft.bitshares.interfaces.ByteSerializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.bitcoinj.core.ECKey;

/**
 * Created by nelson on 11/9/16.
 */
public class Memo implements ByteSerializable {
    //TODO: Give this class a proper implementation
    private byte[] from;
    private byte[] to;
    private byte[] nonce = new byte[8];
    private byte[] message;
        
    @Override
    public byte[] toBytes() {
        if ((this.from == null) || (this.to == null) || (this.nonce == null) ||(this.message == null)){
            return new byte[1];
        } 
        
        byte[] result = new byte[this.from.length+this.to.length+this.nonce.length+this.message.length];
        System.arraycopy(this.from, 0, result, 0, this.from.length);
        System.arraycopy(this.to, 0, result, this.from.length, this.to.length);
        System.arraycopy(this.nonce, 0, result, this.from.length+this.to.length, this.nonce.length);
        System.arraycopy(this.message, 0, result, this.from.length+this.to.length+this.nonce.length, this.message.length);
        
        return result;
    }
    
    public Memo(){
        this.from = null;
        this.nonce = null;
        this.to = null;
        this.message = null;
    }
    
    public Memo(byte[] private_key, byte[] public_key, byte[] msg){
        this(private_key,public_key,msg,0);
    }
    
    public Memo(byte[] private_key, byte[] public_key, byte[] msg, long custom_nonce){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            ECKey privateECKey = ECKey.fromPrivate(md.digest(private_key));

            this.from = privateECKey.getPubKey();
            this.to = public_key;

            if (custom_nonce == 0){
                SecureRandomStrengthener randomStrengthener = SecureRandomStrengthener.getInstance();
                //randomStrengthener.addEntropySource(new AndroidRandomSource());
                SecureRandom secureRandom = randomStrengthener.generateAndSeedRandomNumberGenerator();
                secureRandom.nextBytes(nonce);

                long time = System.currentTimeMillis();

                for (int i = 7;i >=1; i--){
                    this.nonce[i] = (byte)(time&0xff);
                    time = time/0x100;
                }
            } else {
                for (int i = 7;i >=0; i--){
                    this.nonce[i] = (byte)(custom_nonce&0xff);
                    custom_nonce = custom_nonce/0x100;
                }
            }


            byte[] secret = privateECKey.getPubKeyPoint().multiply(ECKey.fromPublicOnly(md.digest(public_key)).getPrivKey()).normalize().getXCoord().getEncoded();
            byte[] finalKey = new byte[secret.length + this.nonce.length];

            byte[] sha256Msg = md.digest(msg);
            byte[] serialChecksum = new byte[4];
            System.arraycopy(sha256Msg, 0, serialChecksum, 0, 4);
            byte[] msgFinal = new byte[serialChecksum.length + msg.length];
            System.arraycopy(serialChecksum, 0, msgFinal, 0, serialChecksum.length);
            System.arraycopy(msg, 0, msgFinal, serialChecksum.length, msg.length);

            this.message = Util.encryptAES(msgFinal, finalKey);
        } catch (NoSuchAlgorithmException ex){
            
        }
    }
}
