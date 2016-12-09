package com.luminiasoft.bitshares;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.luminiasoft.bitshares.crypto.AndroidRandomSource;
import com.luminiasoft.bitshares.crypto.SecureRandomStrengthener;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import org.bitcoinj.core.ECKey;
import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

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
            byte[] publicKey = new byte[33];
            byte[] rawDataEncripted = new byte[input.length - 33];

            System.arraycopy(input, 0, publicKey, 0, publicKey.length);
            System.arraycopy(input, 33, rawDataEncripted, 0, rawDataEncripted.length);

            MessageDigest md = MessageDigest.getInstance("SHA-256");

            ECKey randomECKey = ECKey.fromPublicOnly(publicKey);
            byte[] finalKey = randomECKey.getPubKeyPoint().multiply(ECKey.fromPrivate(md.digest(password.getBytes("UTF-8"))).getPrivKey()).normalize().getXCoord().getEncoded();
            MessageDigest md1 = MessageDigest.getInstance("SHA-512");
            finalKey = md1.digest(finalKey);
            byte[] rawData = Util.decryptAES(rawDataEncripted, Util.byteToString(finalKey).getBytes());
            
            byte[] checksum = new byte[4];
            System.arraycopy(rawData, 0, checksum, 0, 4);
            byte[] compressedData = new byte[rawData.length - 4];
            System.arraycopy(rawData, 4, compressedData, 0, compressedData.length);
            
            byte[] wallet_object_bytes = Util.decompress(compressedData, Util.XZ);
            String wallet_string = new String(wallet_object_bytes, "UTF-8");
            JsonObject wallet = new JsonParser().parse(wallet_string).getAsJsonObject();
            if (wallet.get("wallet").isJsonArray()) {
                wallet = wallet.get("wallet").getAsJsonArray().get(0).getAsJsonObject();
            } else {
                wallet = wallet.get("wallet").getAsJsonObject();
            }

            byte[] encKey_enc = new BigInteger(wallet.get("encryption_key").getAsString(), 16).toByteArray();
            byte[] temp = new byte[encKey_enc.length - (encKey_enc[0] == 0 ? 1 : 0)];
            System.arraycopy(encKey_enc, (encKey_enc[0] == 0 ? 1 : 0), temp, 0, temp.length);
            byte[] encKey = Util.decryptAES(temp, password.getBytes("UTF-8"));
            temp = new byte[encKey.length];
            System.arraycopy(encKey, 0, temp, 0, temp.length);

            byte[] encBrain = new BigInteger(wallet.get("encrypted_brainkey").getAsString(), 16).toByteArray();
            while (encBrain[0] == 0) {
                byte[] temp2 = new byte[encBrain.length - 1];
                System.arraycopy(encBrain, 1, temp2, 0, temp2.length);
                encBrain = temp2;
            }
            String BrainKey = new String((Util.decryptAES(encBrain, temp)), "UTF-8");

            return BrainKey;

        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {

        }
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
            //randomStrengthener.addEntropySource(new AndroidRandomSource());
            SecureRandom secureRandom = randomStrengthener.generateAndSeedRandomNumberGenerator();
            secureRandom.nextBytes(encKey);
            byte[] encKey_enc = Util.encryptAES(encKey, password.getBytes("UTF-8"));
            byte[] encBrain = Util.encryptAES(BrainKey.getBytes("ASCII"), encKey);

            /**
             * Data to Store
             */
            JsonObject wallet = new JsonObject();
            wallet.add("encryption_key", new JsonParser().parse(Util.byteToString(encKey_enc)));
            wallet.add("encrypted_brainkey", new JsonParser().parse(Util.byteToString(encBrain)));
            JsonObject wallet_object = new JsonObject();
            wallet_object.add("wallet", wallet);
            JsonArray accountNames = new JsonArray();
            JsonObject jsonAccountName = new JsonObject();
            jsonAccountName.add("name", new JsonParser().parse(accountName));
            accountNames.add(jsonAccountName);
            wallet_object.add("linked_accounts", accountNames);
            byte[] compressedData = Util.compress(wallet_object.toString().getBytes("UTF-8"), Util.XZ);
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
            rawData = Util.encryptAES(rawData, Util.byteToString(finalKey).getBytes());
            
            byte[] result = new byte[rawData.length + randPubKey.length];
            System.arraycopy(randPubKey, 0, result, 0, randPubKey.length);
            System.arraycopy(rawData, 0, result, randPubKey.length, rawData.length);

            return result;

        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {

        }
        return null;
    }

    
}
