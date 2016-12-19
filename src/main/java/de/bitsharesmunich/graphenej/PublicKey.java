package de.bitsharesmunich.graphenej;

import de.bitsharesmunich.graphenej.interfaces.ByteSerializable;
import org.bitcoinj.core.ECKey;
import org.spongycastle.math.ec.ECPoint;

/**
 * Created by nelson on 11/30/16.
 */
public class PublicKey implements ByteSerializable {
    private ECKey publicKey;

    public PublicKey(ECKey key) {
        if(key.hasPrivKey()){
            throw new IllegalStateException("Passing a private key to PublicKey constructor");
        }
        this.publicKey = key;
    }

    public ECKey getKey(){
        return publicKey;
    }

    @Override
    public byte[] toBytes() {
        if(publicKey.isCompressed()) {
            return publicKey.getPubKey();
        }else{
            publicKey = ECKey.fromPublicOnly(ECKey.compressPoint(publicKey.getPubKeyPoint()));
            return publicKey.getPubKey();
        }
    }

    public String getAddress(){
        ECKey pk = ECKey.fromPublicOnly(publicKey.getPubKey());
        if(!pk.isCompressed()){
            ECPoint point = ECKey.compressPoint(pk.getPubKeyPoint());
            pk = ECKey.fromPublicOnly(point);
        }
        return new Address(pk).toString();
    }

    @Override
    public int hashCode() {
        return publicKey.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        PublicKey other = (PublicKey) obj;
        return this.publicKey.equals(other.getKey());
    }
}