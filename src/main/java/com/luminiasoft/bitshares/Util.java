package com.luminiasoft.bitshares;

import com.google.common.primitives.Bytes;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAInputStream;
import org.tukaani.xz.LZMAOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tukaani.xz.XZOutputStream;

/**
 * Class used to encapsulate common utility methods
 */
public class Util {
    final private static char[] hexArray = "0123456789abcdef".toCharArray();

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
     * @return Compressed data
     * @author Henry Varona
     */
    public static byte[] compress(byte[] inputBytes) {
        XZOutputStream out = null;
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(inputBytes);
            ByteArrayOutputStream output = new ByteArrayOutputStream(2048);
            LZMA2Options options = new LZMA2Options();
            out = new XZOutputStream(output, options);
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
     * by the {@link Util#compress(byte[])} method.
     * @param inputBytes Compressed data
     * @return Uncompressed data
     * @author Henry Varona
     */
    public static byte[] decompress(byte[] inputBytes) {
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
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
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
        return ByteBuffer.allocate(Integer.BYTES).putInt(Integer.reverseBytes(input)).array();
    }

    /**
     * Same operation as in the revertInteger function, but in this case for a short (2 bytes) value.
     * @param input A Short value
     * @return The array of bytes that represent this value in the reverse format.
     */
    public static byte[] revertShort(Short input){
        return ByteBuffer.allocate(Short.BYTES).putShort(Short.reverseBytes(input)).array();
    }

    /**
     * Same operation as in the revertInteger function, but in this case for a long (8 bytes) value.
     * @param input A Long value
     * @return The array of bytes that represent this value in the reverse format.
     */
    public static byte[] revertLong(Long input){
        return ByteBuffer.allocate(Long.BYTES).putLong(Long.reverseBytes(input)).array();
    }
}
