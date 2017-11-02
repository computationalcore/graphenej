package cy.agorise.graphenej;

import junit.framework.Assert;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.junit.Test;

/**
 * Created by nelson on 11/2/17.
 */

public class KeyTest {

    /**
     * Testing key to address derivation
     */
    @Test
    public void testKeyToAddress(){
        String wif = "5J96pne45qWM1WpektoeazN6k9Mt93jQ7LyueRxFfEMTiy6yxjM";
        ECKey sourcePrivate = DumpedPrivateKey.fromBase58(null, wif).getKey();
        Address address = new Address(ECKey.fromPublicOnly(sourcePrivate.getPubKey()));
        Assert.assertEquals("Generated address matches expected one", "BTS8RiFgs8HkcVPVobHLKEv6yL3iXcC9SWjbPVS15dDAXLG9GYhnY", address.toString());
    }
}
