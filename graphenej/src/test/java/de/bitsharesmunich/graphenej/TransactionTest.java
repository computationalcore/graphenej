package de.bitsharesmunich.graphenej;

import com.google.common.primitives.UnsignedLong;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import de.bitsharesmunich.graphenej.api.TransactionBroadcastSequence;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.WitnessResponse;
import de.bitsharesmunich.graphenej.objects.Memo;
import de.bitsharesmunich.graphenej.operations.LimitOrderCreateOperation;
import de.bitsharesmunich.graphenej.operations.LimitOrderCreateOperationTest;
import de.bitsharesmunich.graphenej.operations.TransferOperation;
import de.bitsharesmunich.graphenej.operations.TransferOperationBuilder;
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

    WitnessResponseListener listener = new WitnessResponseListener() {
        @Override
        public void onSuccess(WitnessResponse response) {
            System.out.println("onSuccess");
            WitnessResponse<String> witnessResponse = response;
            Assert.assertNull(witnessResponse.result);
            synchronized (this){
                notifyAll();
            }
        }

        @Override
        public void onError(BaseResponse.Error error) {
            System.out.println("onError");
            System.out.println(error.data.message);
            Assert.assertNull(error);
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
     */
    private void broadcastTransaction(ECKey privateKey, List<BaseOperation> operationList, WitnessResponseListener responseListener) {
        try{
            Transaction transaction = new Transaction(privateKey, null, operationList);

            SSLContext context = null;
            context = NaiveSSLContext.getInstance("TLS");
            WebSocketFactory factory = new WebSocketFactory();

            // Set the custom SSL context.
            factory.setSSLContext(context);

            WebSocket mWebSocket = factory.createSocket(BLOCK_PAY_DE);

            mWebSocket.addListener(new TransactionBroadcastSequence(transaction, CORE_ASSET, listener));
            mWebSocket.connect();
            synchronized (responseListener){
                responseListener.wait();
                System.out.println("Wait released");
            }
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
        broadcastTransaction(sourcePrivateKey, operationList, listener);
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
        broadcastTransaction(privateKey, operationList, listener);
    }
}