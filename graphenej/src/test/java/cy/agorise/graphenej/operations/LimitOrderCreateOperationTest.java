package cy.agorise.graphenej.operations;

import com.google.common.primitives.UnsignedLong;

import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.AssetAmount;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.Util;

/**
 * Created by nelson on 3/6/17.
 */
public class LimitOrderCreateOperationTest {
    private static final int AMOUNT_TO_SELL = 25000000;
    private static final int MIN_TO_RECEIVE = 1;
    private static final Asset CORE_ASSET = new Asset("1.3.0");
    private static final Asset BIT_USD = new Asset("1.3.121");
    private static final int DEFAULT_EXPIRATION = 1488831620; // 2017-03-06T20:20:20

    private UserAccount seller;
    private AssetAmount amountToSell;
    private AssetAmount minToReceive;
    private int expiration;
    private boolean fillOrKill;

    @Before
    public void setup(){
        seller = new UserAccount("1.2.143563");
        amountToSell = new AssetAmount(UnsignedLong.valueOf(AMOUNT_TO_SELL), CORE_ASSET);
        minToReceive = new AssetAmount(UnsignedLong.valueOf(MIN_TO_RECEIVE), BIT_USD);
        expiration = DEFAULT_EXPIRATION;
    }

    @Test
    public void toBytes() throws Exception {
        // Testing serialization of operation with fillOrKill parameter == true
        LimitOrderCreateOperation operation = new LimitOrderCreateOperation(seller, amountToSell, minToReceive, expiration, true);
        operation.setFee(new AssetAmount(UnsignedLong.valueOf(2), CORE_ASSET));
        byte[] serialized = operation.toBytes();
        Assert.assertArrayEquals("Correct serialization", serialized, Util.hexToBytes("020000000000000000cbe10840787d01000000000001000000000000007984c4bd580100"));
        Assert.assertThat("Incorrect serialization", serialized, IsNot.not(IsEqual.equalTo(Util.hexToBytes("020000000000000000cbe10840787d01000000000001000000000000007984c4bd580000"))));

        // Testing serialization of operation with fillOrKill parameter == false
        operation = new LimitOrderCreateOperation(seller, amountToSell, minToReceive, expiration, false);
        operation.setFee(new AssetAmount(UnsignedLong.valueOf(2), CORE_ASSET));
        serialized = operation.toBytes();
        Assert.assertArrayEquals("Correct serialization", serialized, Util.hexToBytes("020000000000000000cbe10840787d01000000000001000000000000007984c4bd580000"));
        Assert.assertThat("Incorrect serialization", serialized, IsNot.not(IsEqual.equalTo(Util.hexToBytes("020000000000000000cbe10840787d01000000000001000000000000007984c4bd580100"))));
    }
}