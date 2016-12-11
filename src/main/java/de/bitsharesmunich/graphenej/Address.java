package de.bitsharesmunich.graphenej;

import com.google.common.primitives.Bytes;
import de.bitsharesmunich.graphenej.errors.MalformedAddressException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.util.Arrays;

/**
 * Class used to encapsulate address-related operations.
 */
public class Address {

    public final static String BITSHARES_PREFIX = "BTS";

    private PublicKey publicKey;
    private String prefix;

    public Address(ECKey key) {
        this.publicKey = new PublicKey(key);
        this.prefix = BITSHARES_PREFIX;
    }

    public Address(ECKey key, String prefix) {
        this.publicKey = new PublicKey(key);
        this.prefix = prefix;
    }

    public Address(String address) throws MalformedAddressException {
        this.prefix = address.substring(0, 3);
        byte[] decoded = Base58.decode(address.substring(3, address.length()));
        byte[] pubKey = Arrays.copyOfRange(decoded, 0, decoded.length - 4);
        byte[] checksum = Arrays.copyOfRange(decoded, decoded.length - 4, decoded.length);
        publicKey = new PublicKey(ECKey.fromPublicOnly(pubKey));
        byte[] calculatedChecksum = calculateChecksum(pubKey);
        for(int i = 0; i < calculatedChecksum.length; i++){
            if(checksum[i] != calculatedChecksum[i]){
                throw new MalformedAddressException("Checksum error");
            }
        }
    }

    public PublicKey getPublicKey(){
        return this.publicKey;
    }

    @Override
    public String toString() {
        byte[] pubKey = this.publicKey.toBytes();
        byte[] checksum = calculateChecksum(pubKey);
        byte[] pubKeyChecksummed = Bytes.concat(pubKey, checksum);
        return this.prefix + Base58.encode(pubKeyChecksummed);
    }

    private byte[] calculateChecksum(byte[] data){
        byte[] checksum = new byte[160 / 8];
        RIPEMD160Digest ripemd160Digest = new RIPEMD160Digest();
        ripemd160Digest.update(data, 0, data.length);
        ripemd160Digest.doFinal(checksum, 0);
        return Arrays.copyOfRange(checksum, 0, 4);
    }
}
