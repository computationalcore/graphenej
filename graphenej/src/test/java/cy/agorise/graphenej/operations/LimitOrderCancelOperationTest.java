package cy.agorise.graphenej.operations;

import com.google.common.primitives.UnsignedLong;
import cy.agorise.graphenej.*;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by nelson on 3/21/17.
 */
public class LimitOrderCancelOperationTest {
    private static final Asset CORE_ASSET = new Asset("1.3.0");
    private UserAccount feePayingAccount;
    private LimitOrder limitOrder;

    @Before
    public void setup(){
        feePayingAccount = new UserAccount("1.2.143563");
        limitOrder = new LimitOrder("1.7.2360289");
    }

    @Test
    public void toBytes() throws Exception {
        LimitOrderCancelOperation operation = new LimitOrderCancelOperation(limitOrder, feePayingAccount);
        operation.setFee(new AssetAmount(UnsignedLong.valueOf(2), CORE_ASSET));
        byte[] serialized = operation.toBytes();
        assertArrayEquals("Correct serialization", Util.hexToBytes("020000000000000000cbe108e187900100"), serialized);
    }
}