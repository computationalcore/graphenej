package com.luminiasoft.bitshares;

import java.util.Arrays;
import java.util.Base64;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.spongycastle.crypto.digests.RIPEMD160Digest;
import org.spongycastle.crypto.digests.SHA512Digest;

/**
 *
 * @author hvarona
 */
public class BIP39 {

    private final ECKey mPrivateKey;

    public BIP39(String words, String passphrase) {

        byte[] seed = MnemonicCode.toSeed(Arrays.asList(words.split(" ")), passphrase);
        mPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);

    }

    public String getUncompressedAddress() {
        RIPEMD160Digest ripemd160Digest = new RIPEMD160Digest();
        SHA512Digest sha512Digest = new SHA512Digest();
        sha512Digest.update(mPrivateKey.decompress().getPubKey(), 0, mPrivateKey.decompress().getPubKey().length);
        byte[] intermediate = new byte[512 / 8];
        sha512Digest.doFinal(intermediate, 0);
        ripemd160Digest.update(intermediate, 0, intermediate.length);
        byte[] output = new byte[160 / 8];
        ripemd160Digest.doFinal(output, 0);
        String encoded = Base58.encode(output);
        byte[] checksum = new byte[(160 / 8) + 4];
        System.arraycopy(calculateChecksum(output), 0, checksum, checksum.length - 4, 4);
        System.arraycopy(output, 0, checksum, 0, output.length);

        return ("BTS" + Base58.encode(checksum));
    }

    public String getAddress() {
        RIPEMD160Digest ripemd160Digest = new RIPEMD160Digest();
        SHA512Digest sha512Digest = new SHA512Digest();
        sha512Digest.update(mPrivateKey.getPubKey(), 0, mPrivateKey.getPubKey().length);
        byte[] intermediate = new byte[512 / 8];
        sha512Digest.doFinal(intermediate, 0);
        ripemd160Digest.update(intermediate, 0, intermediate.length);
        byte[] output = new byte[160 / 8];
        ripemd160Digest.doFinal(output, 0);
        String encoded = Base58.encode(output);
        byte[] checksum = new byte[(160 / 8) + 4];
        System.arraycopy(calculateChecksum(output), 0, checksum, checksum.length - 4, 4);
        System.arraycopy(output, 0, checksum, 0, output.length);

        return ("BTS" + Base58.encode(checksum));
    }

    public byte[] calculateChecksum(byte[] input) {
        byte[] answer = new byte[4];
        RIPEMD160Digest ripemd160Digest = new RIPEMD160Digest();
        ripemd160Digest.update(input, 0, input.length);
        byte[] output = new byte[160 / 8];
        ripemd160Digest.doFinal(output, 0);
        System.arraycopy(output, 0, answer, 0, 4);
        return answer;
    }

    public byte[] getPublicKey() {
        return mPrivateKey.getPubKey();
    }

    public ECKey getPrivateKey() {
        return mPrivateKey;
    }

}
