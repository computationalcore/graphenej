package com.luminiasoft.bitshares;

/**
 * Class to manage the Bin Files
 * @author Henry Varona
 */
public abstract class FileBin {
    
    /**
     * Method to get the brainkey fron an input of bytes
     * @param input Array of bytes of the file to be processed
     * @param password the pin code
     * @return the brainkey file, or null if the file or the password are incorrect
     */
    public static String getBrainkeyFromByte(byte[] input, String password){
        
        
        
        return null;
    }
    
    /**
     * Method to generate the file form a brainkey
     * @param BrainKey The input brainkey
     * @param password The pin code
     * @return The array byte of the file, or null if an error ocurred
     */
    public static byte[] getBytesFromBrainKey(String BrainKey,String password){
        
        
        return null;
    }
    
    
}
