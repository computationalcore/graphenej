package cy.agorise.graphenej.objects;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import cy.agorise.graphenej.Address;
import cy.agorise.graphenej.PublicKey;
import cy.agorise.graphenej.TestAccounts;
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

    private String sourceWIF = TestAccounts.Bilthon16.WIF;
    private String destinationWIF = TestAccounts.Bilthon7.WIF;
    private String shortMessage = "test";
    private String longerMessage = "testing now longer string with some special charaters é ç o ú á í Í mMno!!";

    private byte[] shortEncryptedMessage = Util.hexToBytes("93c398e05f2a36a535f82880032a062d");
    private BigInteger shortEncryptedMessageNonce = new BigInteger("386471255144360");

    private byte[] longerEncryptedMessage = Util.hexToBytes("8ba8f5ed85ad9f7675bd30408a28d6f6ba138476d1e995dd61c01f0041ab25911e04d93fe4ce30e4f6c9a5134cceb67d653e140aa542da19ce2fc646bcde46e088da06a9327eaac79ffe8bc9d71d586195c04bb023995f18e66c9f9e5c6b0d7c");
    private BigInteger longEncryptedMessageNonce = new BigInteger("386469162162343");

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
    }

    @Test
    public void shouldMatchPredefinedCiphertext(){
        byte[] encrypted = Memo.encryptMessage(sourcePrivate, destinationAddress, shortEncryptedMessageNonce, shortMessage);
        assertArrayEquals("Testing with short message and nonce 1", encrypted, shortEncryptedMessage);

        byte[] encryptedLong = Memo.encryptMessage(sourcePrivate, destinationAddress, longEncryptedMessageNonce, longerMessage);
        assertArrayEquals("Testing with longer message and nonce 1", encryptedLong, longerEncryptedMessage);
    }

    @Test
    public void shouldDecryptShortMessage(){
        try {
            String decrypted = Memo.decryptMessage(destinationPrivate, sourceAddress, shortEncryptedMessageNonce, shortEncryptedMessage);
            System.out.println("Short Decrypted Message: " + decrypted);
            assertEquals("Decrypted message must be equal to original", decrypted, shortMessage);
        } catch (ChecksumException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldDecryptLongerMessage(){
        try{
            System.out.println("Source address: "+sourceAddress.toString());
            System.out.println("Dest address..: "+new Address(ECKey.fromPublicOnly(ECKey.fromPrivate(destinationPrivate.getPrivKeyBytes()).getPubKey())).toString());
            System.out.println("Nonce.........: "+longEncryptedMessageNonce);
            System.out.println("Encrypted msg.: "+Util.bytesToHex(longerEncryptedMessage));
            String longDecrypted = Memo.decryptMessage(destinationPrivate, sourceAddress, longEncryptedMessageNonce, longerEncryptedMessage);
            System.out.println("Long Decrypted Message: " + longDecrypted);
            assertEquals("The longer message must be equal to the original", longerMessage, longDecrypted);
        } catch (ChecksumException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldEncryptAndDecryptShortMessage(){
        try {
            BigInteger nonce = BigInteger.ONE;
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
            BigInteger nonce = BigInteger.ONE;
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
        byte[] corrupted = Memo.encryptMessage(sourcePrivate, destinationAddress, longEncryptedMessageNonce, longerMessage);
        corrupted[0] = 0;
        String longDecrypted = Memo.decryptMessage(destinationPrivate, sourceAddress, longEncryptedMessageNonce, corrupted);
    }

    @Test
    public void shouldBeJsonObjectSerializable(){
        byte[] encrypted = Memo.encryptMessage(sourcePrivate, destinationAddress, shortEncryptedMessageNonce, shortMessage);
        Memo memo = new Memo(sourceAddress, destinationAddress, shortEncryptedMessageNonce, encrypted);
        JsonElement jsonObject = memo.toJsonObject();
        JsonObject expected = new JsonObject();
        expected.addProperty("from", new Address(ECKey.fromPublicOnly(ECKey.fromPrivate(DumpedPrivateKey.fromBase58(null, TestAccounts.Bilthon16.WIF).getKey().getPrivKeyBytes()).getPubKey())).toString());
        expected.addProperty("to", new Address(ECKey.fromPublicOnly(ECKey.fromPrivate(DumpedPrivateKey.fromBase58(null, TestAccounts.Bilthon7.WIF).getKey().getPrivKeyBytes()).getPubKey())).toString());
        expected.addProperty("nonce", String.format("%x", shortEncryptedMessageNonce));
        expected.addProperty("message", "93c398e05f2a36a535f82880032a062d");
        assertEquals("Memo instance should generate a valid JsonObject",expected, jsonObject);
    }

    @Test
    public void shouldBeByteSerializable(){
        String byteReference = "01029392096400eafe5f5ce7e2ab74134c3422fc49e5853bdeb298fb096258e26f6303d1fb8c7421db64d46fba7e36f428854ca06eff65698b293f37c7ffaa54e2c2b20100000000000000104ccbca3750fd2e531441de02b23fe6c7";
        byte[] encrypted = Memo.encryptMessage(sourcePrivate, destinationAddress, BigInteger.ONE, shortMessage);
        Memo memo = new Memo(sourceAddress, destinationAddress, BigInteger.ONE, encrypted);
        byte[] memoBytes = memo.toBytes();
        assertEquals("Memo instance should generate a valid byte array", byteReference, Util.bytesToHex(memoBytes));
    }

    @Test
    public void shouldDeserializeFromString(){
        String jsonMemo = "{\"from\":\"BTS6nB7gw1EawYXRofLvuivLsboVmh2inXroQgSQqYfAc5Bamk4Vq\",\"to\":\"BTS4xAQGg2ePLeDGZvQFpsh9CjMhQvRnVkPp6jPoE6neVPotRfZX9\",\"nonce\":\"8000000000000000\",\"message\":\"b9aeb7632f1f4281eedcf28a684828a42d02de71254fb88e13ddcb9a79adf51d9770c58d7e7efcdbb1515f1136c3be3e\"}";
        GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(Memo.class, new Memo.MemoSerializer())
                .registerTypeAdapter(Memo.class, new Memo.MemoDeserializer());
        Memo memo = gsonBuilder.create().fromJson(jsonMemo, Memo.class);
        Assert.assertEquals("Source address should match the serialized one", "BTS6nB7gw1EawYXRofLvuivLsboVmh2inXroQgSQqYfAc5Bamk4Vq", memo.getSource().toString());
        Assert.assertEquals("Destination address should match the serialized one", "BTS4xAQGg2ePLeDGZvQFpsh9CjMhQvRnVkPp6jPoE6neVPotRfZX9", memo.getDestination().toString());
        Assert.assertEquals("Nonce should match serialized one", new BigInteger("8000000000000000", 10), memo.getNonce());
        Assert.assertArrayEquals(Util.hexToBytes("b9aeb7632f1f4281eedcf28a684828a42d02de71254fb88e13ddcb9a79adf51d9770c58d7e7efcdbb1515f1136c3be3e"), memo.getByteMessage());

        String json = gsonBuilder.create().toJson(memo);
        Assert.assertEquals("Serialized memo matches the original one", jsonMemo, json);
    }
}