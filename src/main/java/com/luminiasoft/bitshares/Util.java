package com.luminiasoft.bitshares;

import org.tukaani.xz.FinishableOutputStream;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAInputStream;
import org.tukaani.xz.LZMAOutputStream;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

/**
 * Class used to encapsulate common utility methods
 */
public class Util {
    public static final String TAG = "Util";
    private static final char[] hexArray = "0123456789abcdef".toCharArray();
    public static final int LZMA = 0;
    public static final int XZ = 1;

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Utility function that compresses data using the LZMA algorithm.
     * @param inputBytes Input bytes of the data to be compressed.
     * @param which Which subclass of the FinishableOutputStream to use.
     * @return Compressed data
     * @author Henry Varona
     */
    public static byte[] compress(byte[] inputBytes, int which) {
        FinishableOutputStream out = null;
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(inputBytes);
            ByteArrayOutputStream output = new ByteArrayOutputStream(2048);
            LZMA2Options options = new LZMA2Options();
            if(which == Util.LZMA) {
                out = new LZMAOutputStream(output, options, -1);
            }else if(which == Util.XZ){
                out = new XZOutputStream(output, options);
            }
            byte[] buf = new byte[inputBytes.length];
            int size;
            while ((size = input.read(buf)) != -1) {
                out.write(buf, 0, size);
            }
            out.finish();
            return output.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    /**
     * Utility function that decompresses data that has been compressed using the LZMA algorithm
     * by the {@link Util#compress(byte[], int)} method.
     * @param inputBytes Compressed data.
     * @param which Which subclass if InputStream to use.
     * @return Uncompressed data
     * @author Henry Varona
     */
    public static byte[] decompress(byte[] inputBytes, int which) {
        InputStream in = null;
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(inputBytes);
            ByteArrayOutputStream output = new ByteArrayOutputStream(2048);
            if(which == XZ) {
                in = new XZInputStream(input);
            }else if(which == LZMA){
                in = new LZMAInputStream(input);
            }
            int size;
            while ((size = in.read()) != -1) {
                output.write(size);
            }
            in.close();
            return output.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);            }
        }
        return null;
    }

    /**
     * Returns an array of bytes with the underlying data used to represent an integer in the reverse form.
     * This is useful for endianess switches, meaning that if you give this function a big-endian integer
     * it will return it's little-endian bytes.
     * @param input An Integer value.
     * @return The array of bytes that represent this value in the reverse format.
     */
    public static byte[] revertInteger(Integer input){
        return ByteBuffer.allocate(Integer.SIZE / 8).putInt(Integer.reverseBytes(input)).array();
    }

    /**
     * Same operation as in the revertInteger function, but in this case for a short (2 bytes) value.
     * @param input A Short value
     * @return The array of bytes that represent this value in the reverse format.
     */
    public static byte[] revertShort(Short input){
        return ByteBuffer.allocate(Short.SIZE / 8).putShort(Short.reverseBytes(input)).array();
    }

    /**
     * Same operation as in the revertInteger function, but in this case for a long (8 bytes) value.
     * @param input A Long value
     * @return The array of bytes that represent this value in the reverse format.
     */
    public static byte[] revertLong(Long input) {
        return ByteBuffer.allocate(Long.SIZE / 8).putLong(Long.reverseBytes(input)).array();
    }

    /**
     * Function to encrypt a message with AES
     * @param input data to encrypt
     * @param key key for encryption
     * @return AES Encription of input 
     */
    public static byte[] encryptAES(byte[] input, byte[] key) {
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

    /**
     * Function to decrypt a message with AES encryption
     * @param input data to decrypt
     * @param key key for decryption
     * @return input decrypted with AES. Null if the decrypt failed (Bad Key)
     */
    public static byte[] decryptAES(byte[] input, byte[] key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] result = md.digest(key);
            byte[] ivBytes = new byte[16];
            System.arraycopy(result, 32, ivBytes, 0, 16);
            byte[] sksBytes = new byte[32];
            System.arraycopy(result, 0, sksBytes, 0, 32);
            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
            cipher.init(false, new ParametersWithIV(new KeyParameter(sksBytes), ivBytes));

            byte[] pre_out = new byte[cipher.getOutputSize(input.length)];
            int proc = cipher.processBytes(input, 0, input.length, pre_out, 0);
            int proc2 = cipher.doFinal(pre_out, proc);
            byte[] out = new byte[proc+proc2]; 
            System.arraycopy(pre_out, 0, out, 0, proc+proc2);
            
            //Unpadding
            byte countByte = (byte)((byte)out[out.length-1] % 16);
            int count = countByte & 0xFF;
                       
            if ((count > 15) || (count <= 0)){
                return out;
            }
            
            byte[] temp = new byte[count];
            System.arraycopy(out, out.length - count, temp, 0, temp.length);
            byte[] temp2 = new byte[count];
            Arrays.fill(temp2, (byte) count);
            if (Arrays.equals(temp, temp2)) {
                temp = new byte[out.length - count];
                System.arraycopy(out, 0, temp, 0, out.length - count);
                return temp;
            } else {
                return out;
            }            
        } catch (NoSuchAlgorithmException | DataLengthException | IllegalStateException | InvalidCipherTextException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Transform an array of bytes to an hex String representation
     * @param input array of bytes to transform as a string
     * @return Input as a String
     */
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
