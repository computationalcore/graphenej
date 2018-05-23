package cy.agorise.graphenej;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by nelson on 4/18/17.
 */
public class BrainKeyTest {
    public final String TEST_BRAINKEY = "BARIC BICKERN LITZ TIPFUL JINGLED POOL TUMBAK PURIST APOPYLE DURAIN SATLIJK FAUCAL";

    public final String TEST_BRAINKEY_OPENLEDGER = "ona refan abscise neebor battik terbia bandit sundra gasser debar phytol frat hauler accede primy garland";

    private BrainKey mBrainKey;

    @Before
    public void setup(){
        mBrainKey = new BrainKey(TEST_BRAINKEY, BrainKey.DEFAULT_SEQUENCE_NUMBER);
    }

    /**
     * Test making sure that a simple brainkey can successfully generate the expected public address
     */
    @Test
    public void testAddress(){
        Address address = mBrainKey.getPublicAddress(Address.BITSHARES_PREFIX);
        Assert.assertEquals("Assert that the address created is the expected one",
                "BTS61UqqgE3ARuTGcckzARsdQm4EMFdBEwYyi1pbwyHrZZWrCDhT2",
                address.toString());
    }

    /**
     * Test making sure that a OpenLedger's brainkey can successfully generate the given
     * 'owner' and 'active' keys.
     */
    @Test
    public void testOpenledgerAddress(){
        BrainKey brainKey1 = new BrainKey(TEST_BRAINKEY_OPENLEDGER, 0);
        BrainKey brainKey2 = new BrainKey(TEST_BRAINKEY_OPENLEDGER, 1);

        Address ownerAddress = brainKey1.getPublicAddress(Address.BITSHARES_PREFIX);
        Address activeAddress = brainKey2.getPublicAddress(Address.BITSHARES_PREFIX);

        Assert.assertEquals("Owner address matches",
                "BTS6dqT3J7tUcZP6xHo2mHkL8tq8zw5TQgGd6ntRMXH1EoNsCWTzm",
                ownerAddress.toString());

        Assert.assertEquals("Active address matches",
                "BTS6DKvgY3yPyN7wKrhBGYhrnghhLSVCYz3ugUdi9pDPkicS6B7N2",
                activeAddress.toString());
    }
}