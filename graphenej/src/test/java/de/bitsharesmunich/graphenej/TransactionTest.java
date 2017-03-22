package de.bitsharesmunich.graphenej;

import com.google.common.primitives.UnsignedLong;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import de.bitsharesmunich.graphenej.api.GetLimitOrders;
import de.bitsharesmunich.graphenej.api.TransactionBroadcastSequence;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.WitnessResponse;
import de.bitsharesmunich.graphenej.objects.Memo;
import de.bitsharesmunich.graphenej.operations.*;
import de.bitsharesmunich.graphenej.test.NaiveSSLContext;
import org.bitcoinj.core.ECKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nelson on 3/6/17.
 */
public class TransactionTest {
    private final String BILTHON_15_BRAIN_KEY = System.getenv("BILTHON_15_BRAINKEY");
    private final String BILTHON_5_BRAIN_KEY = System.getenv("BILTHON_5_BRAINKEY");
    private final String BILTHON_16_BRAIN_KEY = System.getenv("BILTHON_16_BRAINKEY");

    private final String BLOCK_PAY_DE = System.getenv("BLOCKPAY_DE");
    private final String BLOCK_PAY_FR = System.getenv("BLOCKPAY_FR");

    // Transfer operation transaction
    private final Asset CORE_ASSET = new Asset("1.3.0");
    private final UserAccount bilthon_15 = new UserAccount("1.2.143563");
    private final UserAccount bilthon_5 = new UserAccount("1.2.139313");
    private final UserAccount bilthon_16 = new UserAccount("1.2.143569");

    // Limit order create transaction
    private final Asset BIT_USD = new Asset("1.3.121");
    private UserAccount seller = bilthon_15;
    private AssetAmount amountToSell = new AssetAmount(UnsignedLong.valueOf(100000), CORE_ASSET);
    private AssetAmount minToReceive = new AssetAmount(UnsignedLong.valueOf(520), BIT_USD);
    private long expiration;

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

            WebSocket mWebSocket = factory.createSocket(BLOCK_PAY_DE);

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
        ECKey sourcePrivateKey = new BrainKey(BILTHON_15_BRAIN_KEY, 0).getPrivateKey();
        PublicKey to1 = new PublicKey(ECKey.fromPublicOnly(new BrainKey(BILTHON_5_BRAIN_KEY, 0).getPublicKey()));
        PublicKey to2 = new PublicKey(ECKey.fromPublicOnly(new BrainKey(BILTHON_16_BRAIN_KEY, 0).getPublicKey()));

        // Creating memo
        long nonce = 1;
        byte[] encryptedMessage = Memo.encryptMessage(sourcePrivateKey, to1, nonce, "another message");
        Memo memo = new Memo(new Address(ECKey.fromPublicOnly(sourcePrivateKey.getPubKey())), new Address(to1.getKey()), nonce, encryptedMessage);

        // Creating operation 1
        TransferOperation transferOperation1 = new TransferOperationBuilder()
                .setTransferAmount(new AssetAmount(UnsignedLong.valueOf(1), CORE_ASSET))
                .setSource(bilthon_15)
                .setDestination(bilthon_5) // bilthon-5
                .setFee(new AssetAmount(UnsignedLong.valueOf(264174), CORE_ASSET))
                .build();

        // Creating operation 2
        TransferOperation transferOperation2 = new TransferOperationBuilder()
                .setTransferAmount(new AssetAmount(UnsignedLong.valueOf(1), CORE_ASSET))
                .setSource(bilthon_15) // bilthon-15
                .setDestination(bilthon_16) // bilthon-16
                .setFee(new AssetAmount(UnsignedLong.valueOf(264174), CORE_ASSET))
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
        ECKey privateKey = new BrainKey(BILTHON_15_BRAIN_KEY, 0).getPrivateKey();
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
        ECKey privateKey = new BrainKey(BILTHON_15_BRAIN_KEY, 0).getPrivateKey();
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
                    WebSocket mWebSocket = factory.createSocket(BLOCK_PAY_DE);

                    // Requesting limit order to cancel (Task 2)
                    mWebSocket.addListener(new GetLimitOrders(base.getObjectId(), quote.getObjectId(), 100, new WitnessResponseListener() {

                        @Override
                        public void onSuccess(WitnessResponse response) {
                            System.out.println("onSuccess.1");
                            List<LimitOrder> orders = (List<LimitOrder>) response.result;
                            for(LimitOrder order : orders){
                                if(order.getSeller().getObjectId().equals(bilthon_15.getObjectId())){

                                    // Instantiating a private key for bilthon-15
                                    ECKey privateKey = new BrainKey(BILTHON_15_BRAIN_KEY, 0).getPrivateKey();

                                    // Creating limit order cancellation operation
                                    LimitOrderCancelOperation operation = new LimitOrderCancelOperation(order, bilthon_15);
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
}