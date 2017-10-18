package cy.agorise.graphenej.objects;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cy.agorise.graphenej.Address;
import cy.agorise.graphenej.PublicKey;
import cy.agorise.graphenej.Util;
import cy.agorise.graphenej.errors.ChecksumException;

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
    private byte[] memoEncryptedEnvMessage;
    //private String sourceWIF = "5J96pne45qWM1WpektoeazN6k9Mt93jQ7LyueRxFfEMTiy6yxjM";
    //private String destinationWIF = "5HuGQT8qwHScBgD4XsGbQUmXQF18MrbzxaQDiGGXFNRrCtqgT5Q";
    private String shortMessage = "test";
    private String longerMessage = "testing now longer string with some special charaters é ç o ú á í Í mMno!!";

    private byte[] shortEncryptedMessage = Util.hexToBytes("4c81c2db6ebc61e3f9e0ead65c0559dd");
    private byte[] longerEncryptedMessage = Util.hexToBytes("1f8a08f1ff53dcefd48eeb052d26fba425f2a917f508ce61fc3d5696b10efa17");

    private String decodedMessage;

    @Before
    public void setUp() throws Exception {
        if(System.getenv("MEMO_MESSAGE") != null){
            memoEncryptedEnvMessage = Util.hexToBytes(System.getenv("MEMO_MESSAGE"));
        }

        if(sourceWIF != null && destinationWIF != null){
            //Source
            sourcePrivate = DumpedPrivateKey.fromBase58(null, sourceWIF).getKey();
            PublicKey publicKey = new PublicKey(ECKey.fromPublicOnly(sourcePrivate.getPubKey()));
            sourceAddress = new Address(publicKey.getKey());

            //Destination
            destinationPrivate = DumpedPrivateKey.fromBase58(null, destinationWIF).getKey();
            publicKey = new PublicKey(ECKey.fromPublicOnly(destinationPrivate.getPubKey()));
            destinationAddress = new Address(publicKey.getKey());
        }

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

    @Test
    public void shouldDeserializeFromString(){
        String jsonMemo = "{\"from\": \"BTS6nB7gw1EawYXRofLvuivLsboVmh2inXroQgSQqYfAc5Bamk4Vq\",\"to\": \"BTS4xAQGg2ePLeDGZvQFpsh9CjMhQvRnVkPp6jPoE6neVPotRfZX9\",\"nonce\": \"15f2d8ee4ec23\",\"message\": \"b9aeb7632f1f4281eedcf28a684828a42d02de71254fb88e13ddcb9a79adf51d9770c58d7e7efcdbb1515f1136c3be3e\"}";
        GsonBuilder gsonBuilder = new GsonBuilder().registerTypeAdapter(Memo.class, new Memo.MemoDeserializer());
        Memo memo = gsonBuilder.create().fromJson(jsonMemo, Memo.class);
        Assert.assertEquals("Source address should match the serialized one", "BTS6nB7gw1EawYXRofLvuivLsboVmh2inXroQgSQqYfAc5Bamk4Vq", memo.getSource().toString());
        Assert.assertEquals("Destination address should match the serialized one", "BTS4xAQGg2ePLeDGZvQFpsh9CjMhQvRnVkPp6jPoE6neVPotRfZX9", memo.getDestination().toString());
        Assert.assertEquals("Nonce should match serialized one", Long.parseLong("15f2d8ee4ec23", 16), memo.getNonce());
        Assert.assertArrayEquals(Util.hexToBytes("b9aeb7632f1f4281eedcf28a684828a42d02de71254fb88e13ddcb9a79adf51d9770c58d7e7efcdbb1515f1136c3be3e"), memo.getByteMessage());
    }
}