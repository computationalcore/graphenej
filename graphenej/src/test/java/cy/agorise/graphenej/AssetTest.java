package cy.agorise.graphenej;

import static org.junit.Assert.*;

/**
 * Created by nelson on 12/24/16.
 */
public class AssetTest {

    @org.junit.Test
    public void equals() throws Exception {
        Asset bts = new Asset("1.3.0");
        Asset bitUSD = new Asset("1.3.121");
        assertNotEquals("Different assets should not be equal", bts, bitUSD);
    }

}