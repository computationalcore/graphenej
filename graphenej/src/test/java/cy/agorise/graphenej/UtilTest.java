package cy.agorise.graphenej;

import com.google.common.primitives.UnsignedLong;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Class used to test Util methods
 */

public class UtilTest {

    @Test
    public void testRevertUnsignedLong(){
        UnsignedLong unsignedLong = UnsignedLong.valueOf("12179241258665439971");
        byte[] reversed = Util.revertUnsignedLong(unsignedLong);
        Assert.assertEquals("e3f28878655b05a9", Util.bytesToHex(reversed));
    }
}
