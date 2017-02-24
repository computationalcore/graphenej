package de.bitsharesmunich.graphenej;

import com.google.common.primitives.UnsignedLong;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by nelson on 2/23/17.
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
}