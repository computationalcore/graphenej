package com.luminiasoft.bitshares;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    public static byte[] getBytesFromBrainKey(String BrainKey, String password) {

        // Cypher AES password
        // Get random public key address
        // Cypher random public key with aespassword
        // Cypher key ciphered key and aespassword
        // Cypher brainkey
        // Store cypher brainkey and cyher public + password
        //LZMA compress
        //Generate another public key
        //Cypher public key with password
        // result Cypher compressed message
        return null;
    }

    public static byte[] compressDataLZMA(byte[] inputBytes) {
        LZMAOutputStream out = null;
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(inputBytes);
            ByteArrayOutputStream output = new ByteArrayOutputStream(2048);
            LZMA2Options options = new LZMA2Options();
            out = new LZMAOutputStream(output, options,-1);
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
}
