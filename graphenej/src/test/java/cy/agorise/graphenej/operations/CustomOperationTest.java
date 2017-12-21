package cy.agorise.graphenej.operations;

import com.google.common.primitives.UnsignedLong;
import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.AssetAmount;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.Util;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class CustomOperationTest {
    private final Asset CORE_ASSET = new Asset("1.3.0");
    private final AssetAmount fee = new AssetAmount(UnsignedLong.valueOf(100000L), CORE_ASSET);
    private final UserAccount payer = new UserAccount("1.2.20");
    private final Integer operationId = 61166;
    private final List<UserAccount> requiredAuths = Collections.singletonList(payer);
    private final String shortData = "some data";
    private final String longData = "very long data, very long data, very long data, very long data, very long data, very long data,  very long data, very long data,  very long data, very long data,  very long data, very long data,  very long data, very long data...";

    private static final byte[] EXPECTED_SERIALIZED_BYTES_1 = Util.hexToBytes("a08601000000000000140114eeee09736f6d652064617461");
    private static final byte[] EXPECTED_SERIALIZED_BYTES_2 = Util.hexToBytes("a08601000000000000140114eeeee50176657279206c6f6e6720646174612c2076657279206c6f6e6720646174612c2076657279206c6f6e6720646174612c2076657279206c6f6e6720646174612c2076657279206c6f6e6720646174612c2076657279206c6f6e6720646174612c202076657279206c6f6e6720646174612c2076657279206c6f6e6720646174612c202076657279206c6f6e6720646174612c2076657279206c6f6e6720646174612c202076657279206c6f6e6720646174612c2076657279206c6f6e6720646174612c202076657279206c6f6e6720646174612c2076657279206c6f6e6720646174612e2e2e");

    @Test
    public void testToBytes() throws Exception {
        CustomOperation customOperation1 = new CustomOperation(fee, payer, operationId, requiredAuths, shortData);
        byte[] serialized1 = customOperation1.toBytes();
        assertArrayEquals(EXPECTED_SERIALIZED_BYTES_1, serialized1);

        // test with some long data string to check if data length is serialized correctly
        CustomOperation customOperation2 = new CustomOperation(fee, payer, operationId, requiredAuths, longData);
        byte[] serialized2 = customOperation2.toBytes();
        assertArrayEquals(EXPECTED_SERIALIZED_BYTES_2, serialized2);
    }
}

