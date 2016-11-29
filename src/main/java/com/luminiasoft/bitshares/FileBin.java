package com.luminiasoft.bitshares;

import com.google.common.primitives.Bytes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.luminiasoft.bitshares.crypto.AndroidRandomSource;
import com.luminiasoft.bitshares.crypto.SecureRandomStrengthener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.digests.RIPEMD160Digest;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAInputStream;
import org.tukaani.xz.LZMAOutputStream;

/**
 * Class to manage the Bin Files
 *
 * @author Henry Varona
 */
public abstract class FileBin {

    /**
     * Method to get the brainkey fron an input of bytes
     *
     * @param input Array of bytes of the file to be processed
     * @param password the pin code
     * @return the brainkey file, or null if the file or the password are
     * incorrect
     */
    public static String getBrainkeyFromByte(byte[] input, String password) {

        //Creates cypher AES with password
        //Uncrypt 
        return null;
    }

    /**
     * Method to generate the file form a brainkey
     *
     * @param BrainKey The input brainkey
     * @param password The pin code
     * @return The array byte of the file, or null if an error ocurred
     */
    public static byte[] getBytesFromBrainKey(String BrainKey, String password, String accountName) {

        try {
            byte[] encKey = new byte[32];
            SecureRandomStrengthener randomStrengthener = SecureRandomStrengthener.getInstance();
            randomStrengthener.addEntropySource(new AndroidRandomSource());
            SecureRandom secureRandom = randomStrengthener.generateAndSeedRandomNumberGenerator();
            secureRandom.nextBytes(encKey);

            //byte[] encKey = new byte[]{(byte) 23, (byte) 216, (byte) 129, (byte) 104, (byte) 115, (byte) 250, (byte) 179, (byte) 214, (byte) 64, (byte) 173, (byte) 173, (byte) 145, (byte) 251, (byte) 234, (byte) 25, (byte) 189, (byte) 20, (byte) 227, (byte) 239, (byte) 103, (byte) 226, (byte) 39, (byte) 145, (byte) 234, (byte) 12, (byte) 104, (byte) 91, (byte) 73, (byte) 76, (byte) 151, (byte) 47, (byte) 210};
            byte[] encKey_enc = encryptAES(encKey, password.getBytes("UTF-8"));
            byte[] encBrain = encryptAES(BrainKey.getBytes("ASCII"), encKey);

            JsonObject wallet = new JsonObject();
            wallet.add("encryption_key", new JsonParser().parse(byteToString(encKey_enc)));
            wallet.add("encrypted_brainkey", new JsonParser().parse(byteToString(encBrain)));

            JsonObject wallet_object = new JsonObject();
            wallet_object.add("wallet", wallet);
            JsonArray accountNames = new JsonArray();
            JsonObject jsonAccountName = new JsonObject();
            jsonAccountName.add("name", new JsonParser().parse(accountName));
            accountNames.add(jsonAccountName);

            wallet_object.add("linked_accounts", accountNames);
            System.out.println(wallet_object.toString());
            byte[] compressedData = compressDataLZMA(wallet_object.toString().getBytes("UTF-8"));
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] checksum = md.digest(compressedData);
            byte[] rawData = new byte[compressedData.length + 4];
            System.arraycopy(checksum, 0, rawData, 0, 4);
            System.arraycopy(compressedData, 0, rawData, 4, compressedData.length);

            byte[] passPrivKey = ECKey.fromPrivate(md.digest(password.getBytes("UTF-8"))).getPrivKeyBytes();

            byte[] randomKey = new byte[32];
            secureRandom.nextBytes(randomKey);
            byte[] randPubKey = ECKey.fromPrivate(md.digest(randomKey)).getPubKey();

            //System.out.println(byteToString(cipher.doFinal(encKey)));
            // Cypher random public key with aespassword
            // Cypher key ciphered key and aespassword
            // Cypher brainkey
            // Store cypher brainkey and cyher public + password
            //LZMA compress
            //Generate another public key
            //Cypher public key with password
            // result Cypher compressed message
            return null;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(FileBin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(FileBin.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static byte[] compressDataLZMA(byte[] inputBytes) {
        LZMAOutputStream out = null;
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(inputBytes);
            ByteArrayOutputStream output = new ByteArrayOutputStream(2048);
            LZMA2Options options = new LZMA2Options();
            out = new LZMAOutputStream(output, options, -1);
            byte[] buf = new byte[inputBytes.length];
            int size;
            while ((size = input.read(buf)) != -1) {
                out.write(buf, 0, size);
            }
            out.finish();
            return output.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(FileBin.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(FileBin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    private static byte[] encryptAES(byte[] input, byte[] key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] result = md.digest(key);
            byte[] ivBytes = new byte[16];
            System.arraycopy(result, 32, ivBytes, 0, 16);
            byte[] sksBytes = new byte[32];
            System.arraycopy(result, 0, sksBytes, 0, 32);

            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
            cipher.init(true, new ParametersWithIV(new KeyParameter(sksBytes), ivBytes));
            byte[] temp = new byte[input.length + (16 - (input.length % 16))];
            System.arraycopy(input, 0, temp, 0, input.length);
            Arrays.fill(temp, input.length, temp.length, (byte) (16 - (input.length % 16)));
            System.out.println(byteToString(temp));
            byte[] out = new byte[cipher.getOutputSize(temp.length)];
            int proc = cipher.processBytes(temp, 0, temp.length, out, 0);
            cipher.doFinal(out, proc);
            temp = new byte[out.length - 16];
            System.arraycopy(out, 0, temp, 0, temp.length);
            return temp;
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (DataLengthException ex) {
            ex.printStackTrace();
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        } catch (InvalidCipherTextException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static byte[] decompressDataLZMA(byte[] inputBytes) {
        LZMAInputStream in = null;
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(inputBytes);
            ByteArrayOutputStream output = new ByteArrayOutputStream(2048);
            in = new LZMAInputStream(input);
            int size;
            while ((size = in.read()) != -1) {
                output.write(size);
            }
            in.close();
            return output.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(FileBin.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(FileBin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public static String byteToString(byte[] input) {
        StringBuilder result = new StringBuilder();
        for (byte in : input) {
            if ((in & 0xff) < 0x10) {
                result.append("0");
            }
            result.append(Integer.toHexString(in & 0xff));
        }
        return result.toString();
    }
}
