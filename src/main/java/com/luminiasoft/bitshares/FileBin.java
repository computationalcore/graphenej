package com.luminiasoft.bitshares;

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
import org.bitcoinj.core.ECKey;
import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.InvalidCipherTextException;
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
        try {
            byte[] publicKey = new byte[34];
            byte[] rawData = new byte[input.length-34];

            System.arraycopy(input, 0, publicKey, 0, publicKey.length);
            System.arraycopy(input, 34, rawData, 0, rawData.length);

            MessageDigest md = MessageDigest.getInstance("SHA-256");

            ECKey randomECKey = ECKey.fromPublicOnly(publicKey);
            byte[] finalKey = randomECKey.getPubKeyPoint().multiply(ECKey.fromPrivate(md.digest(password.getBytes("UTF-8"))).getPrivKey()).normalize().getXCoord().getEncoded();
            MessageDigest md1 = MessageDigest.getInstance("SHA-512");
            finalKey = md1.digest(finalKey);
            rawData = decryptAES(rawData, byteToString(finalKey).getBytes());
            
            byte[] checksum = new byte[4];
            System.arraycopy(rawData, 0, checksum, 0, 4);
            byte[] compressedData = new byte[rawData.length-4];
            System.arraycopy(rawData, 4, compressedData, 0, compressedData.length);
            byte[] wallet_object_bytes = Util.decompress(compressedData);
            String wallet_string = byteToString(wallet_object_bytes);
            
            return wallet_string;
            //JsonObject wallet = new JsonParser().parse(wallet_string).getAsJsonObject();           
            
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex){
            
        }
        //Creates cypher AES with password
        //Uncrypt 
        return null;
    }

    /**
     * Method to generate the file form a brainkey
     *
     * @param BrainKey The input brainkey
     * @param password The pin code
     * @param accountName The Account Name
     * @return The array byte of the file, or null if an error happens
     */
    public static byte[] getBytesFromBrainKey(String BrainKey, String password, String accountName) {

        try {
            byte[] encKey = new byte[32];
            SecureRandomStrengthener randomStrengthener = SecureRandomStrengthener.getInstance();
            randomStrengthener.addEntropySource(new AndroidRandomSource());
            SecureRandom secureRandom = randomStrengthener.generateAndSeedRandomNumberGenerator();
            secureRandom.nextBytes(encKey);
            byte[] encKey_enc = encryptAES(encKey, password.getBytes("UTF-8"));
            byte[] encBrain = encryptAES(BrainKey.getBytes("ASCII"), encKey);

            /**
             * Data to Store
             */
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
            byte[] compressedData = Util.compress(wallet_object.toString().getBytes("UTF-8"));
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] checksum = md.digest(compressedData);
            byte[] rawData = new byte[compressedData.length + 4];
            System.arraycopy(checksum, 0, rawData, 0, 4);
            System.arraycopy(compressedData, 0, rawData, 4, compressedData.length);
            byte[] randomKey = new byte[32];
            secureRandom.nextBytes(randomKey);
            ECKey randomECKey = ECKey.fromPrivate(md.digest(randomKey));
            byte[] randPubKey = randomECKey.getPubKey();
            byte[] finalKey = randomECKey.getPubKeyPoint().multiply(ECKey.fromPrivate(md.digest(password.getBytes("UTF-8"))).getPrivKey()).normalize().getXCoord().getEncoded();
            MessageDigest md1 = MessageDigest.getInstance("SHA-512");
            finalKey = md1.digest(finalKey);
            rawData = encryptAES(rawData, byteToString(finalKey).getBytes());
            byte[] result = new byte[rawData.length + randPubKey.length];
            System.arraycopy(randPubKey, 0, result, 0, randPubKey.length);
            System.arraycopy(rawData, 0, result, randPubKey.length, rawData.length);

            return result;

        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {

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
            byte[] out = new byte[cipher.getOutputSize(temp.length)];
            int proc = cipher.processBytes(temp, 0, temp.length, out, 0);
            cipher.doFinal(out, proc);
            temp = new byte[out.length - 16];
            System.arraycopy(out, 0, temp, 0, temp.length);
            return temp;
        } catch (NoSuchAlgorithmException | DataLengthException | IllegalStateException | InvalidCipherTextException ex) {
        }
        return null;
    }
    
    private static byte[] decryptAES(byte[] input, byte[] key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] result = md.digest(key);
            byte[] ivBytes = new byte[16];
            System.arraycopy(result, 32, ivBytes, 0, 16);
            byte[] sksBytes = new byte[32];
            System.arraycopy(result, 0, sksBytes, 0, 32);
            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
            cipher.init(false, new ParametersWithIV(new KeyParameter(sksBytes), ivBytes));
            byte[] out = new byte[cipher.getOutputSize(input.length)];
            int proc = cipher.processBytes(input, 0, input.length, out, 0);
            cipher.doFinal(out, proc);
            
            //Unpadding
            int count = out[out.length-1];
            byte[] temp = new byte[count];
            System.arraycopy(out, out.length-count, temp, 0, temp.length);
            byte[] temp2 = new byte[count];
            Arrays.fill(temp2, (byte)count);
            if (Arrays.equals(temp, temp2)){
                temp = new byte[out.length-count];
                System.arraycopy(out, 0, temp, 0, out.length-count);
                return temp;
            } else {
                return out;
            }			
        } catch (NoSuchAlgorithmException | DataLengthException | IllegalStateException | InvalidCipherTextException ex) {
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
