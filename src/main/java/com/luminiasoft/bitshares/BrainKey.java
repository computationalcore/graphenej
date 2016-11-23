package com.luminiasoft.bitshares;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by nelson on 11/19/16.
 */
public class BrainKey {

    private ECKey mPrivateKey;

    public BrainKey(String words, int sequence) {
        String encoded = String.format("%s %d", words, sequence);
        try {
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            mPrivateKey = ECKey.fromPrivate(sha256.digest(sha512.digest(encoded.getBytes("UTF-8"))));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgotithmException. Msg: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.out.println("UnsupportedEncodingException. Msg: " + e.getMessage());
        }
    }

    public ECKey getPrivateKey(){
        return this.mPrivateKey;
    }
}
