package com.luminiasoft.bitshares;

import org.bitcoinj.core.ECKey;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Created by nelson on 11/19/16.
 */
public class BrainKey {

    private ECKey mPrivateKey;

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

    public String getPublicKey() {
        return Base64.getEncoder().encodeToString(mPrivateKey.getPubKey());
    }
}