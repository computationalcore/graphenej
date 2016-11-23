package com.luminiasoft.bitshares;

import com.luminiasoft.bitshares.crypto.AndroidRandomSource;
import com.luminiasoft.bitshares.crypto.SecureRandomStrengthener;
import org.bitcoinj.core.ECKey;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.VerificationException;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.utils.MonetaryFormat;
import org.spongycastle.crypto.digests.RIPEMD160Digest;
import org.spongycastle.crypto.digests.SHA512Digest;

/**
 * Class used to encapsulate all BrainKey-related operations.
 */
public class BrainKey {

    // The size of the word dictionary
    public static final int DICT_WORD_COUNT = 49744;

    // The required number of words
    public static final int BRAINKEY_WORD_COUNT = 12;

    private ECKey mPrivateKey;

    /**
     * Method that will generate a random brain key
     *
     * @param words The list of words from the graphene specification
     * dictionary.
     * @return A random sequence of words
     */
    public static String suggest(String words) {
        String[] wordArray = words.split(",");
        ArrayList<String> suggestedBrainKey = new ArrayList<String>();
        assert (wordArray.length == DICT_WORD_COUNT);
        SecureRandomStrengthener randomStrengthener = SecureRandomStrengthener.getInstance();
        randomStrengthener.addEntropySource(new AndroidRandomSource());
        SecureRandom secureRandom = randomStrengthener.generateAndSeedRandomNumberGenerator();
        int index;
        for (int i = 0; i < BRAINKEY_WORD_COUNT; i++) {
            index = secureRandom.nextInt(DICT_WORD_COUNT - 1);
            suggestedBrainKey.add(wordArray[index].toUpperCase());
        }
        String result = String.join(" ", suggestedBrainKey.toArray(new String[suggestedBrainKey.size()]));
        System.out.println("result: '" + result + "'");
        return result;
    }

    /**
     * BrainKey constructor that takes as argument a specific brain key word
     * sequence and generates the private key and address from that.
     *
     * @param words The brain key specifying the private key
     * @param sequence Sequence number
     */
    public BrainKey(String words, int sequence) {
        String encoded = String.format("%s %d", words, sequence);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = md.digest(encoded.getBytes("UTF-8"));
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] result = sha256.digest(bytes);
            mPrivateKey = ECKey.fromPrivate(result);

        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgotithmException. Msg: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.out.println("UnsupportedEncodingException. Msg: " + e.getMessage());
        }
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
