package cy.agorise.graphenej.api;

import com.google.common.primitives.UnsignedLong;
import com.neovisionaries.ws.client.WebSocketException;
import cy.agorise.graphenej.AssetAmount;
import cy.agorise.graphenej.BaseOperation;
import cy.agorise.graphenej.Transaction;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.BaseResponse;
import cy.agorise.graphenej.models.Block;
import cy.agorise.graphenej.models.WitnessResponse;
import cy.agorise.graphenej.operations.TransferOperation;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.instanceOf;

public class GetBlockTest extends BaseApiTest {
    private final static long BLOCK_NUMBER = 15776988L;

    private final static String EXPECTED_PREVIOUS = "00f0bcdbe3d9dc66d8597e7b013a16d8dea5f778";
    private final static String EXPECTED_TIMESTAMP = "2017-04-18T04:14:51";
    private final static String EXPECTED_WITNESS = "1.6.17";
    private final static String EXPECTED_TRANSACTION_MERKLE_ROOT = "fb87a3f8af907a2450c327d181b2833b8270a504";
    private final static int EXPECTED_EXTENSIONS_SIZE = 0;
    private final static String EXPECTED_WITNESS_SIGNATURE = "20160921c8ba0d312dee14bdc780c3d05b27e406e2d014b8a2415e9843bf7075cb7abbc45f5173ffefac69cecf4dd2afa5dce6076bdf24cc577ff49427babe75e1";
    private final static int EXPECTED_TRANSACTIONS_SIZE = 1;
    private final static int EXPECTED_OPERATIONS_SIZE = 1;

    private final static UnsignedLong EXPECTED_FEE_AMOUNT = UnsignedLong.valueOf(2048);
    private final static String EXPECTED_FEE_ASSET_ID = "1.3.113";
    private final static String EXPECTED_FROM = "1.2.151069";
    private final static String EXPECTED_TO = "1.2.116354";
    private final static UnsignedLong EXPECTED_AMOUNT = UnsignedLong.valueOf(13700);
    private final static String EXPECTED_ASSET_ID = "1.3.113";

    @Test
    public void testGetBlock() {
        try {
            final Exchanger<Object> responseExchanger = new Exchanger<>();
            mWebSocket.addListener(new GetBlock(BLOCK_NUMBER, new WitnessResponseListener() {
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("onSuccess");
                    try {
                        responseExchanger.exchange(response.result);
                    } catch (InterruptedException e) {
                        System.out.println("InterruptedException in success handler. Msg: " + e.getMessage());
                    }
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("onError");
                    try {
                        responseExchanger.exchange(null);
                    } catch (InterruptedException e) {
                        System.out.println("InterruptedException in error handler. Msg: " + e.getMessage());
                    }
                }
            }));

            mWebSocket.connect();

            Object responseResult = responseExchanger.exchange(null, 5, TimeUnit.SECONDS);

            Block block = (Block) responseResult;
            Assert.assertNotNull(block);
            Assert.assertEquals(EXPECTED_PREVIOUS, block.getPrevious());
            Assert.assertEquals(EXPECTED_TIMESTAMP, block.getTimestamp());
            Assert.assertEquals(EXPECTED_WITNESS, block.getWitness());
            Assert.assertEquals(EXPECTED_TRANSACTION_MERKLE_ROOT, block.getTransaction_merkle_root());
            Assert.assertEquals(EXPECTED_EXTENSIONS_SIZE, block.getExtensions().length);
            Assert.assertEquals(EXPECTED_WITNESS_SIGNATURE, block.getWitness_signature());

            List<Transaction> transactions = block.getTransactions();
            Assert.assertEquals(EXPECTED_TRANSACTIONS_SIZE, transactions.size());

            List<BaseOperation> operations = transactions.get(0).getOperations();
            Assert.assertEquals(EXPECTED_OPERATIONS_SIZE, operations.size());

            BaseOperation operation = operations.get(0);
            Assert.assertThat(operation, instanceOf(TransferOperation.class));

            TransferOperation transferOperation = (TransferOperation) operation;
            AssetAmount fee = transferOperation.getFee();
            Assert.assertEquals(EXPECTED_FEE_AMOUNT, fee.getAmount());
            Assert.assertEquals(EXPECTED_FEE_ASSET_ID, fee.getAsset().getObjectId());
            Assert.assertEquals(EXPECTED_FROM, transferOperation.getFrom().getObjectId());
            Assert.assertEquals(EXPECTED_TO, transferOperation.getTo().getObjectId());
            AssetAmount assetAmount = transferOperation.getAssetAmount();
            Assert.assertEquals(EXPECTED_AMOUNT, assetAmount.getAmount());
            Assert.assertEquals(EXPECTED_ASSET_ID, assetAmount.getAsset().getObjectId());
        } catch (WebSocketException e) {
            System.out.println("WebSocketException. Msg: " + e.getMessage());
            Assert.fail("Fail because of WebSocketException");
        } catch (InterruptedException e) {
            System.out.println("InterruptedException. Msg: " + e.getMessage());
            Assert.fail("Fail because of InterruptedException");
        } catch (TimeoutException e) {
            System.out.println("TimeoutException. Msg: " + e.getMessage());
            Assert.fail("Fail because of TimeoutException");
        }
    }
}
