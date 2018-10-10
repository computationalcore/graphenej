package cy.agorise.graphenej;

import com.google.common.primitives.UnsignedLong;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.bitcoinj.core.ECKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLContext;

import cy.agorise.graphenej.api.GetLimitOrders;
import cy.agorise.graphenej.api.TransactionBroadcastSequence;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.BaseResponse;
import cy.agorise.graphenej.models.WitnessResponse;
import cy.agorise.graphenej.operations.CustomOperation;
import cy.agorise.graphenej.operations.LimitOrderCancelOperation;
import cy.agorise.graphenej.operations.LimitOrderCreateOperation;
import cy.agorise.graphenej.operations.TransferOperation;
import cy.agorise.graphenej.operations.TransferOperationBuilder;
import cy.agorise.graphenej.test.NaiveSSLContext;

/**
 * Created by nelson on 3/6/17.
 */
public class TransactionTest {
    private final String BILTHON_7_BRAIN_KEY = TestAccounts.Bilthon7.BRAINKEY;
    private final String BILTHON_5_BRAIN_KEY = TestAccounts.Bilthon5.BRAINKEY;
    private final String BILTHON_16_BRAIN_KEY = TestAccounts.Bilthon16.BRAINKEY;

    private final String NODE_URL = "wss://eu.openledger.info/ws";

    // Transfer operation transaction
    private final Asset CORE_ASSET = new Asset("1.3.0");
    private final UserAccount bilthon_7 = new UserAccount("1.2.140994");
    private final UserAccount bilthon_5 = new UserAccount("1.2.139313");
    private final UserAccount bilthon_16 = new UserAccount("1.2.143569");

    // Limit order create transaction
    private final Asset BIT_USD = new Asset("1.3.121");
    private UserAccount seller = bilthon_7;
    private AssetAmount amountToSell = new AssetAmount(UnsignedLong.valueOf(100000), CORE_ASSET);
    private AssetAmount minToReceive = new AssetAmount(UnsignedLong.valueOf(520), BIT_USD);
    private long expiration;

    // Custom operation transaction
    private final AssetAmount fee = new AssetAmount(UnsignedLong.valueOf(100000), CORE_ASSET);
    private final UserAccount payer = bilthon_7;
    private final Integer operationId = 61166;
    private final List<UserAccount> requiredAuths = Collections.singletonList(payer);
    private final String data = "some data";

    private final long FEE_AMOUNT = 21851;

    // Lock object
    private static final class Lock { }
    private final Object lockObject = new Lock();

    // Response
    private BaseResponse baseResponse;

    /**
     * Generic witness response listener that will just release the lock created in
     * main thread.
     */
    WitnessResponseListener listener = new WitnessResponseListener() {

        @Override
        public void onSuccess(WitnessResponse response) {
            System.out.println("onSuccess");
            baseResponse = response;
            synchronized (this){
                this.notifyAll();
            }
        }

        @Override
        public void onError(BaseResponse.Error error) {
            System.out.println("onError. Msg: "+error.data.message);
            synchronized (this){
                notifyAll();
            }
        }
    };

    @Before
    public void setup(){
    }

    /**
     * Receives the elements required for building a transaction, puts them together and broadcasts it.
     * @param privateKey: The private key used to sign the transaction.
     * @param operationList: The list of operations to include
     * @param responseListener: The response listener.
     * @param lockObject: Optional object to use as a lock
     */
    private void broadcastTransaction(ECKey privateKey, List<BaseOperation> operationList, WitnessResponseListener responseListener, Object lockObject) {
        try{
            Transaction transaction = new Transaction(privateKey, null, operationList);

            SSLContext context = null;
            context = NaiveSSLContext.getInstance("TLS");
            WebSocketFactory factory = new WebSocketFactory();

            // Set the custom SSL context.
            factory.setSSLContext(context);

            WebSocket mWebSocket = factory.createSocket(NODE_URL);

            mWebSocket.addListener(new TransactionBroadcastSequence(transaction, CORE_ASSET, responseListener));
            mWebSocket.connect();

            // If a lock object is specified, we use it
            if(lockObject != null){
                synchronized (lockObject){
                    lockObject.wait();
                }
            }else{
                // Otherwise we just use this listener as the lock
                synchronized (responseListener){
                    responseListener.wait();
                }
            }
            Assert.assertNotNull(baseResponse);
            Assert.assertNull(baseResponse.error);
        }catch(NoSuchAlgorithmException e){
            System.out.println("NoSuchAlgoritmException. Msg: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("InterruptedException. Msg: "+e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException. Msg: " + e.getMessage());
        } catch (WebSocketException e) {
            System.out.println("WebSocketException. Msg: " + e.getMessage());
        }
    }

    @Test
    public void testTransferTransaction(){
        ECKey sourcePrivateKey = new BrainKey(BILTHON_7_BRAIN_KEY, 0).getPrivateKey();
        PublicKey to1 = new PublicKey(ECKey.fromPublicOnly(new BrainKey(BILTHON_5_BRAIN_KEY, 0).getPublicKey()));
        PublicKey to2 = new PublicKey(ECKey.fromPublicOnly(new BrainKey(BILTHON_16_BRAIN_KEY, 0).getPublicKey()));

        // Creating memo
        BigInteger nonce = BigInteger.ONE;
        byte[] encryptedMessage = Memo.encryptMessage(sourcePrivateKey, to1, nonce, "another message");
        Memo memo = new Memo(new Address(ECKey.fromPublicOnly(sourcePrivateKey.getPubKey())), new Address(to1.getKey()), nonce, encryptedMessage);

        // Creating operation 1
        TransferOperation transferOperation1 = new TransferOperationBuilder()
                .setTransferAmount(new AssetAmount(UnsignedLong.valueOf(1), CORE_ASSET))
                .setSource(bilthon_7)
                .setDestination(bilthon_5) // bilthon-5
                .setFee(new AssetAmount(UnsignedLong.valueOf(FEE_AMOUNT), CORE_ASSET))
                .setMemo(memo)
                .build();

        // Creating operation 2
        TransferOperation transferOperation2 = new TransferOperationBuilder()
                .setTransferAmount(new AssetAmount(UnsignedLong.valueOf(1), CORE_ASSET))
                .setSource(bilthon_7) // bilthon-15
                .setDestination(bilthon_16) // bilthon-16
                .setFee(new AssetAmount(UnsignedLong.valueOf(FEE_AMOUNT), CORE_ASSET))
                .setMemo(memo)
                .build();


        // Adding operations to the operation list
        ArrayList<BaseOperation> operationList = new ArrayList<>();
        operationList.add(transferOperation1);
        operationList.add(transferOperation2);

        // Broadcasting transaction
        broadcastTransaction(sourcePrivateKey, operationList, listener, null);
    }

    @Test
    public void testLimitOrderCreateTransaction(){
        ECKey privateKey = new BrainKey(BILTHON_7_BRAIN_KEY, 0).getPrivateKey();
        expiration = (System.currentTimeMillis() / 1000) + 60 * 60;

        // Creating limit order creation operation
        LimitOrderCreateOperation operation = new LimitOrderCreateOperation(seller, amountToSell, minToReceive, (int) expiration, false);
        operation.setFee(new AssetAmount(UnsignedLong.valueOf(2), CORE_ASSET));

        ArrayList<BaseOperation> operationList = new ArrayList<>();
        operationList.add(operation);

        // Broadcasting transaction
        broadcastTransaction(privateKey, operationList, listener, null);
    }

    /**
     * Since tests should be independent of each other, in order to be able to test the cancellation of an
     * existing order we must first proceed to create one. And after creating one, we must also retrieve
     * its id in a separate call.
     *
     * All of this just makes this test a bit more complex, since we have 3 clearly defined tasks that require
     * network communication
     *
     * 1- Create order
     * 2- Retrieve order id
     * 3- Send order cancellation tx
     *
     * Only the last one is what we actually want to test
     *
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws WebSocketException
     */
    @Test
    public void testLimitOrderCancelTransaction() throws NoSuchAlgorithmException, IOException, WebSocketException {

        // We first must create a limit order for this test
        ECKey privateKey = new BrainKey(BILTHON_7_BRAIN_KEY, 0).getPrivateKey();
        expiration = (System.currentTimeMillis() / 1000) + 60 * 5;

        // Creating limit order creation operation
        LimitOrderCreateOperation operation = new LimitOrderCreateOperation(seller, amountToSell, minToReceive, (int) expiration, false);
        operation.setFee(new AssetAmount(UnsignedLong.valueOf(2), CORE_ASSET));

        ArrayList<BaseOperation> operationList = new ArrayList<>();
        operationList.add(operation);

        // Broadcasting transaction (Task 1)
        broadcastTransaction(privateKey, operationList, new WitnessResponseListener() {

            @Override
            public void onSuccess(WitnessResponse response) {

                System.out.println("onSuccess.0");
                try{
                    // Setting up the assets
                    Asset base = amountToSell.getAsset();
                    Asset quote = minToReceive.getAsset();

                    SSLContext context = NaiveSSLContext.getInstance("TLS");
                    WebSocketFactory factory = new WebSocketFactory();

                    // Set the custom SSL context.
                    factory.setSSLContext(context);
                    WebSocket mWebSocket = factory.createSocket(NODE_URL);

                    // Requesting limit order to cancel (Task 2)
                    mWebSocket.addListener(new GetLimitOrders(base.getObjectId(), quote.getObjectId(), 100, new WitnessResponseListener() {

                        @Override
                        public void onSuccess(WitnessResponse response) {
                            System.out.println("onSuccess.1");
                            List<LimitOrder> orders = (List<LimitOrder>) response.result;
                            for(LimitOrder order : orders){
                                if(order.getSeller().getObjectId().equals(bilthon_7.getObjectId())){

                                    // Instantiating a private key for bilthon-15
                                    ECKey privateKey = new BrainKey(BILTHON_7_BRAIN_KEY, 0).getPrivateKey();

                                    // Creating limit order cancellation operation
                                    LimitOrderCancelOperation operation = new LimitOrderCancelOperation(order, bilthon_7);
                                    ArrayList<BaseOperation> operationList = new ArrayList<>();
                                    operationList.add(operation);

                                    // Broadcasting order cancellation tx (Task 3)
                                    broadcastTransaction(privateKey, operationList, new WitnessResponseListener() {

                                        @Override
                                        public void onSuccess(WitnessResponse response) {
                                            System.out.println("onSuccess.2");
                                            baseResponse = response;
                                            synchronized (this){
                                                notifyAll();
                                            }
                                            synchronized (lockObject){
                                                lockObject.notifyAll();
                                            }
                                        }

                                        @Override
                                        public void onError(BaseResponse.Error error) {
                                            System.out.println("onError.2");
                                            synchronized (this){
                                                notifyAll();
                                            }
                                            synchronized (lockObject){
                                                lockObject.notifyAll();
                                            }
                                        }
                                    }, null);
                                }
                            }
                        }

                        @Override
                        public void onError(BaseResponse.Error error) {
                            System.out.println("onError.1");
                            System.out.println(error.data.message);
                            Assert.assertNull(error);
                            synchronized (lockObject){
                                lockObject.notifyAll();
                            }
                        }
                    }));

                    mWebSocket.connect();

                }catch(NoSuchAlgorithmException e){
                    System.out.println("NoSuchAlgorithmException. Msg: "+e.getMessage());
                } catch (WebSocketException e) {
                    System.out.println("WebSocketException. Msg: "+e.getMessage());
                } catch (IOException e) {
                    System.out.println("IOException. Msg: "+e.getMessage());
                }
            }

            @Override
            public void onError(BaseResponse.Error error) {
                System.out.println("OnError. Msg: "+error.message);
                synchronized (this){
                    notifyAll();
                }
            }
        }, lockObject);
    }

    @Test
    public void testCustomOperationTransaction(){
        ECKey sourcePrivateKey = new BrainKey(BILTHON_7_BRAIN_KEY, 0).getPrivateKey();

        // Creating custom operation
        CustomOperation customOperation = new CustomOperation(fee, payer, operationId, requiredAuths, data);

        // Adding operation to the operation list
        ArrayList<BaseOperation> operationList = new ArrayList<>();
        operationList.add(customOperation);

        // Broadcasting transaction
        broadcastTransaction(sourcePrivateKey, operationList, listener, null);
    }
}