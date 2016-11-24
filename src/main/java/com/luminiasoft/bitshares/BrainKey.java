package com.luminiasoft.bitshares;

import com.luminiasoft.bitshares.crypto.AndroidRandomSource;
import com.luminiasoft.bitshares.crypto.SecureRandomStrengthener;
import org.bitcoinj.core.ECKey;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;

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
        StringBuilder stringBuilder = new StringBuilder();
        for(String word : suggestedBrainKey){
            stringBuilder.append(word);
            stringBuilder.append(" ");
        }
        System.out.println("Suggestion: '"+stringBuilder.toString().trim()+"'");
        return stringBuilder.toString().trim();
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
    public byte[] getPublicKey() {
        return mPrivateKey.getPubKey();
    }

    public ECKey getPrivateKey() {
        return mPrivateKey;
    }
}
