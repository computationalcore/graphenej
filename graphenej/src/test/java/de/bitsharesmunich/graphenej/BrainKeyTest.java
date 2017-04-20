package de.bitsharesmunich.graphenej;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by nelson on 4/18/17.
 */
public class BrainKeyTest {
//    public final String TEST_BRAINKEY = "SOAPILY GASSING FIFIE OZONATE WHYO TOPLINE PRISMY ZEUGMA GLOTTIC DAVEN CORODY PFUI";
    public final String TEST_BRAINKEY = "BARIC BICKERN LITZ TIPFUL JINGLED POOL TUMBAK PURIST APOPYLE DURAIN SATLIJK FAUCAL";
    private BrainKey mBrainKey;

    @Before
    public void setup(){
        mBrainKey = new BrainKey(TEST_BRAINKEY, BrainKey.DEFAULT_SEQUENCE_NUMBER);
    }

    @Test
    public void testAddress(){
        Address address = mBrainKey.getPublicAddress(Address.BITSHARES_PREFIX);
//        Assert.assertEquals("Assert that the address created is the expected one",
//                "BTS7yT2vnjGAxPocqsnDfoJv3DHUtCNGWwqvc7mWRikkqwuhKtT5s",
//                address.toString());
        Assert.assertEquals("Assert that the address created is the expected one",
                "BTS61UqqgE3ARuTGcckzARsdQm4EMFdBEwYyi1pbwyHrZZWrCDhT2",
                address.toString());
    }
}