package de.bitsharesmunich.graphenej.objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.PublicKey;
import de.bitsharesmunich.graphenej.Util;
import de.bitsharesmunich.graphenej.errors.ChecksumException;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.junit.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Unit Tests for Memo Related Classes.
 */
public class MemoTest {

    private ECKey sourcePrivate;
    private Address sourceAddress;

    private ECKey destinationPrivate;
    private Address destinationAddress;

    private long nonce;
    private String sourceWIF = System.getenv("SOURCE_WIF");
    private String destinationWIF = System.getenv("DESTINATION_WIF");
    private byte[] memoEncryptedEnvMessage = Util.hexToBytes(System.getenv("MEMO_MESSAGE"));
    //private String sourceWIF = "5J96pne45qWM1WpektoeazN6k9Mt93jQ7LyueRxFfEMTiy6yxjM";
    //private String destinationWIF = "5HuGQT8qwHScBgD4XsGbQUmXQF18MrbzxaQDiGGXFNRrCtqgT5Q";
    private String shortMessage = "test";
    private String longerMessage = "testing now longer string with some special charaters é ç o ú á í Í mMno!!";

    private byte[] shortEncryptedMessage = Util.hexToBytes("4c81c2db6ebc61e3f9e0ead65c0559dd");
    private byte[] longerEncryptedMessage = Util.hexToBytes("1f8a08f1ff53dcefd48eeb052d26fba425f2a917f508ce61fc3d5696b10efa17");

    private String decodedMessage;

    @Before
    public void setUp() throws Exception {
        //Source
        sourcePrivate = DumpedPrivateKey.fromBase58(null, sourceWIF).getKey();
        PublicKey publicKey = new PublicKey(ECKey.fromPublicOnly(sourcePrivate.getPubKey()));
        sourceAddress = new Address(publicKey.getKey());

        //Destination
        destinationPrivate = DumpedPrivateKey.fromBase58(null, destinationWIF).getKey();
        publicKey = new PublicKey(ECKey.fromPublicOnly(destinationPrivate.getPubKey()));
        destinationAddress = new Address(publicKey.getKey());

        //memo.getNonce()
        nonce = 5;
    }

    @Test
    public void shouldMatchPredefinedChiphertext(){
        byte[] encrypted = Memo.encryptMessage(sourcePrivate, destinationAddress, 1, shortMessage);
        assertArrayEquals("Testing with short message and nonce 1", encrypted, shortEncryptedMessage);

        byte[] encryptedLong = Memo.encryptMessage(sourcePrivate, destinationAddress, 1, longerMessage);
        assertArrayEquals("Testing with longer message and nonce 1", encryptedLong, longerEncryptedMessage);
    }

    @Test
    public void shouldDecryptEnvMessage(){
        try {
            String decrypted = Memo.decryptMessage(destinationPrivate, sourceAddress, nonce, memoEncryptedEnvMessage);
            System.out.println("Short Decrypted Message: " + decrypted);
            assertEquals("Decrypted message must be equal to original", decrypted, shortMessage);
        } catch (ChecksumException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldDecryptShortMessage(){
        try {
            String decrypted = Memo.decryptMessage(destinationPrivate, sourceAddress, nonce, shortEncryptedMessage);
            System.out.println("Short Decrypted Message: " + decrypted);
            assertEquals("Decrypted message must be equal to original", decrypted, shortMessage);
        } catch (ChecksumException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldDecryptLongerMessage(){
        try{
            String longDecrypted = Memo.decryptMessage(destinationPrivate, sourceAddress, nonce, longerEncryptedMessage);
            System.out.println("Long Decrypted Message: " + longDecrypted);
            assertEquals("The longer message must be equal to the original", longerMessage, longDecrypted);
        } catch (ChecksumException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldEncryptAndDecryptShortMessage(){
        try {
            byte[] encrypted = Memo.encryptMessage(sourcePrivate, destinationAddress, nonce, shortMessage);
            String decrypted = Memo.decryptMessage(destinationPrivate, sourceAddress, nonce, encrypted);
            System.out.println("Short Decrypted Message: " + decrypted);
            assertEquals("Decrypted message must be equal to original", decrypted, shortMessage);
        } catch (ChecksumException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldEncryptAndDecryptLongerMessage(){
        try{
            byte[] longEncrypted = Memo.encryptMessage(sourcePrivate, destinationAddress, nonce, longerMessage);
            String longDecrypted = Memo.decryptMessage(destinationPrivate, sourceAddress, nonce, longEncrypted);
            System.out.println("Long Decrypted Message: " + longDecrypted);
            assertEquals("The longer message must be equal to the original", longerMessage, longDecrypted);
        } catch (ChecksumException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = ChecksumException.class)
    public void shouldThrowException() throws ChecksumException {
        byte[] corrupted = Memo.encryptMessage(sourcePrivate, destinationAddress, nonce, longerMessage);
        corrupted[0] = 0;
        String longDecrypted = Memo.decryptMessage(destinationPrivate, sourceAddress, nonce, corrupted);
    }

    @Test
    public void shouldBeJsonObjectSerializable(){
        byte[] encrypted = Memo.encryptMessage(sourcePrivate, destinationAddress, 1, shortMessage);
        Memo memo = new Memo(sourceAddress, destinationAddress, 1, encrypted);
        JsonElement jsonObject = memo.toJsonObject();
        JsonObject reference = new JsonObject();
        reference.addProperty("from", "BTS8RiFgs8HkcVPVobHLKEv6yL3iXcC9SWjbPVS15dDAXLG9GYhnY");
        reference.addProperty("to", "BTS8ADjGaswhfFoxMGxqCdBtzhTBJsrGadCLoc9Ey5AGc8eoVZ5bV");
        reference.addProperty("nonce", "1");
        reference.addProperty("message", "4c81c2db6ebc61e3f9e0ead65c0559dd");
        assertEquals("Memo instance should generate a valid JsonObject",jsonObject, reference);
    }

    @Test
    public void shouldBeByteSerializable(){
        String byteReference = "0103d1fb8c7421db64d46fba7e36f428854ca06eff65698b293f37c7ffaa54e2c2b203aece7c31616c02fcc96b50d3397c0e8d33d6384655d477c300d9196c728a5ee20100000000000000104c81c2db6ebc61e3f9e0ead65c0559dd";
        byte[] encrypted = Memo.encryptMessage(sourcePrivate, destinationAddress, 1, shortMessage);
        Memo memo = new Memo(sourceAddress, destinationAddress, 1, encrypted);
        byte[] memoBytes = memo.toBytes();
        assertEquals("Memo instance should generate a valid byte array", byteReference, Util.bytesToHex(memoBytes));
    }
}