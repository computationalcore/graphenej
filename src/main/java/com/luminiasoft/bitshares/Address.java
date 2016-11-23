package com.luminiasoft.bitshares;

import com.google.common.primitives.Bytes;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.util.Arrays;

/**
 * Class used to encapsulate address-related operations.
 */
public class Address {
    public final static String DEFAULT_PREFIX = "BTS";

    private ECKey key;
    private String prefix;

    public Address(ECKey key){
        this.key = key;
        this.prefix = DEFAULT_PREFIX;
    }

    public Address(ECKey key, String prefix){
        this.key = key;
        this.prefix = prefix;
    }

    @Override
    public String toString(){
        byte[] pubKey = key.getPubKey();
        byte[] checksum = new byte[160 / 8];
        RIPEMD160Digest ripemd160Digest = new RIPEMD160Digest();
        ripemd160Digest.update(pubKey, 0, pubKey.length);
        ripemd160Digest.doFinal(checksum, 0);
        byte[] pubKeyChecksummed = Bytes.concat(pubKey, Arrays.copyOfRange(checksum, 0, 4));
        return this.prefix + Base58.encode(pubKeyChecksummed);
    }
}
