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
import cy.agorise.graphenej.operations.CustomOperation;
import cy.agorise.graphenej.operations.TransferOperation;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.instanceOf;

public class GetBlockTest extends BaseApiTest {
    // data for the test with transfer operation
    private final static long TRANSFER_BLOCK_NUMBER = 15776988L;

    private final static String TRANSFER_EXPECTED_PREVIOUS = "00f0bcdbe3d9dc66d8597e7b013a16d8dea5f778";
    private final static String TRANSFER_EXPECTED_TIMESTAMP = "2017-04-18T04:14:51";
    private final static String TRANSFER_EXPECTED_WITNESS = "1.6.17";
    private final static String TRANSFER_EXPECTED_TRANSACTION_MERKLE_ROOT = "fb87a3f8af907a2450c327d181b2833b8270a504";
    private final static int TRANSFER_EXPECTED_EXTENSIONS_SIZE = 0;
    private final static String TRANSFER_EXPECTED_WITNESS_SIGNATURE = "20160921c8ba0d312dee14bdc780c3d05b27e406e2d014b8a2415e9843bf7075cb7abbc45f5173ffefac69cecf4dd2afa5dce6076bdf24cc577ff49427babe75e1";
    private final static int TRANSFER_EXPECTED_TRANSACTIONS_SIZE = 1;
    private final static int TRANSFER_EXPECTED_OPERATIONS_SIZE = 1;

    private final static UnsignedLong TRANSFER_EXPECTED_FEE_AMOUNT = UnsignedLong.valueOf(2048);
    private final static String TRANSFER_EXPECTED_FEE_ASSET_ID = "1.3.113";
    private final static String TRANSFER_EXPECTED_FROM = "1.2.151069";
    private final static String TRANSFER_EXPECTED_TO = "1.2.116354";
    private final static UnsignedLong TRANSFER_EXPECTED_AMOUNT = UnsignedLong.valueOf(13700);
    private final static String TRANSFER_EXPECTED_ASSET_ID = "1.3.113";

    // data for the test with custom operation
    private final static long CUSTOM_BLOCK_NUMBER = 22754473L;
    private final static int CUSTOM_TRANSACTION_INDEX = 8;

    private final static UnsignedLong CUSTOM_EXPECTED_FEE_AMOUNT = UnsignedLong.valueOf(13798);
    private final static String CUSTOM_EXPECTED_FEE_ASSET_ID = "1.3.0";
    private final static String CUSTOM_EXPECTED_PAYER = "1.2.140994";
    private final static int CUSTOM_EXPECTED_REQUIRED_AUTHS_SIZE = 1;
    private final static String CUSTOM_EXPECTED_REQUIRED_AUTH = "1.2.140994";
    private final static int CUSTOM_EXPECTED_ID = 61166;
    private final static String CUSTOM_EXPECTED_DATA = "some data";

    @Test
    public void testGetBlockWithTransferOperation() {
        try {
            final Exchanger<Object> responseExchanger = new Exchanger<>();
            mWebSocket.addListener(new GetBlock(TRANSFER_BLOCK_NUMBER, new WitnessResponseListener() {
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
            Assert.assertEquals(TRANSFER_EXPECTED_PREVIOUS, block.getPrevious());
            Assert.assertEquals(TRANSFER_EXPECTED_TIMESTAMP, block.getTimestamp());
            Assert.assertEquals(TRANSFER_EXPECTED_WITNESS, block.getWitness());
            Assert.assertEquals(TRANSFER_EXPECTED_TRANSACTION_MERKLE_ROOT, block.getTransaction_merkle_root());
            Assert.assertEquals(TRANSFER_EXPECTED_EXTENSIONS_SIZE, block.getExtensions().length);
            Assert.assertEquals(TRANSFER_EXPECTED_WITNESS_SIGNATURE, block.getWitness_signature());

            List<Transaction> transactions = block.getTransactions();
            Assert.assertEquals(TRANSFER_EXPECTED_TRANSACTIONS_SIZE, transactions.size());

            List<BaseOperation> operations = transactions.get(0).getOperations();
            Assert.assertEquals(TRANSFER_EXPECTED_OPERATIONS_SIZE, operations.size());

            BaseOperation operation = operations.get(0);
            Assert.assertThat(operation, instanceOf(TransferOperation.class));

            TransferOperation transferOperation = (TransferOperation) operation;
            AssetAmount fee = transferOperation.getFee();
            Assert.assertEquals(TRANSFER_EXPECTED_FEE_AMOUNT, fee.getAmount());
            Assert.assertEquals(TRANSFER_EXPECTED_FEE_ASSET_ID, fee.getAsset().getObjectId());
            Assert.assertEquals(TRANSFER_EXPECTED_FROM, transferOperation.getFrom().getObjectId());
            Assert.assertEquals(TRANSFER_EXPECTED_TO, transferOperation.getTo().getObjectId());
            AssetAmount assetAmount = transferOperation.getAssetAmount();
            Assert.assertEquals(TRANSFER_EXPECTED_AMOUNT, assetAmount.getAmount());
            Assert.assertEquals(TRANSFER_EXPECTED_ASSET_ID, assetAmount.getAsset().getObjectId());
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

    @Test
    public void testGetBlockWithCustomOperation() {
        try {
            final Exchanger<Object> responseExchanger = new Exchanger<>();
            mWebSocket.addListener(new GetBlock(CUSTOM_BLOCK_NUMBER, new WitnessResponseListener() {
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
            List<Transaction> transactions = block.getTransactions();
            List<BaseOperation> operations = transactions.get(CUSTOM_TRANSACTION_INDEX).getOperations();
            BaseOperation operation = operations.get(0);
            Assert.assertThat(operation, instanceOf(CustomOperation.class));

            CustomOperation customOperation = (CustomOperation) operation;
            AssetAmount fee = customOperation.getFee();
            Assert.assertEquals(CUSTOM_EXPECTED_FEE_AMOUNT, fee.getAmount());
            Assert.assertEquals(CUSTOM_EXPECTED_FEE_ASSET_ID, fee.getAsset().getObjectId());
            Assert.assertEquals(CUSTOM_EXPECTED_PAYER, customOperation.getPayer().getObjectId());
            Assert.assertEquals(CUSTOM_EXPECTED_REQUIRED_AUTHS_SIZE, customOperation.getRequiredAuths().size());
            Assert.assertEquals(CUSTOM_EXPECTED_REQUIRED_AUTH, customOperation.getRequiredAuths().get(0).getObjectId());
            Assert.assertEquals(CUSTOM_EXPECTED_ID, customOperation.getOperationId());
            Assert.assertEquals(CUSTOM_EXPECTED_DATA, customOperation.getData());
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
