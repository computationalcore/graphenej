package cy.agorise.graphenej;

import com.google.common.primitives.UnsignedLong;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Testing AssetAmount operations.
 */
public class AssetAmountTest {
    private final int LARGE_VALUE = 1000;
    private final int SMALL_VALUE = 500;
    private AssetAmount large;
    private AssetAmount small;
    private Asset testAsset = new Asset("1.3.0");

    @Before
    public void setUp(){
        large = new AssetAmount(UnsignedLong.valueOf(LARGE_VALUE), testAsset);
        small = new AssetAmount(UnsignedLong.valueOf(SMALL_VALUE), testAsset);
    }

    @Test
    public void testSubtraction(){
        assertEquals(large.subtract(small).getAmount(), new AssetAmount(UnsignedLong.valueOf(LARGE_VALUE - SMALL_VALUE), testAsset).getAmount());
        assertEquals(small.subtract(large).getAmount(), new AssetAmount(UnsignedLong.valueOf(Math.abs(SMALL_VALUE - LARGE_VALUE)), testAsset).getAmount());
    }

    @Test
    public void testMultiplication(){
        // Testing a simple multiplication by a double
        AssetAmount result = large.multiplyBy(0.5);
        assertEquals(500, result.getAmount().longValue());

        // Testing the multiplication of a number that would normally give an overflow
        AssetAmount max = new AssetAmount(UnsignedLong.valueOf(Long.MAX_VALUE), testAsset);
        AssetAmount overMaxLong = max.multiplyBy(1.5);
        assertEquals("13835058055282163712", overMaxLong.getAmount().toString(10));

        assertNotSame("Making sure the result and original references point to different instances",result, large);
    }

    @Test
    public void testDivision(){
        // Testing a simple division by a double
        AssetAmount result = large.divideBy(0.5);
        assertEquals(2000, result.getAmount().longValue());

        // Testing a division of a number that would normally give an overflow
        AssetAmount max = new AssetAmount(UnsignedLong.valueOf(Long.MAX_VALUE), testAsset);
        AssetAmount overMaxLong = max.divideBy(0.8);
        assertEquals("11529215046068469760", overMaxLong.getAmount().toString());

        assertNotSame("Making sure the result and original references point to different instances",result, large);
    }
}