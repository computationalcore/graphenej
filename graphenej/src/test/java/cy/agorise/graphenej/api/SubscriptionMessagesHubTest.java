package cy.agorise.graphenej.api;

import com.neovisionaries.ws.client.WebSocketException;

import org.junit.Test;

import java.io.Serializable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cy.agorise.graphenej.ObjectType;
import cy.agorise.graphenej.interfaces.NodeErrorListener;
import cy.agorise.graphenej.interfaces.SubscriptionListener;
import cy.agorise.graphenej.models.BaseResponse;
import cy.agorise.graphenej.models.BroadcastedTransaction;
import cy.agorise.graphenej.models.DynamicGlobalProperties;
import cy.agorise.graphenej.models.OperationHistory;
import cy.agorise.graphenej.models.SubscriptionResponse;
import cy.agorise.graphenej.Transaction;

/**
 * Class used to encapsulate all tests that relate to the {@see SubscriptionMessagesHub} class.
 * This test requires setting up the NODE_URL environment variable
 */
public class SubscriptionMessagesHubTest extends BaseApiTest {

    private SubscriptionMessagesHub mMessagesHub;

    /**
     * Error listener
     */
    private NodeErrorListener mErrorListener = new NodeErrorListener() {
        @Override
        public void onError(BaseResponse.Error error) {
            System.out.println("onError");
        }
    };

    /**
     * Testing the subscription and unsubscription features.
     *
     * The test is deemed successful if no exception is thown and the messages indeed
     * are cancelled.
     */
    @Test
    public void testSubscribeUnsubscribe(){
        /**
         * Task that will send a 'cancel_all_subscriptions' API message.
         */
        TimerTask unsubscribeTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Cancelling all subscriptions");
                mMessagesHub.cancelSubscriptions();
            }
        };

        TimerTask resubscribeTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Resubscribing..");
                mMessagesHub.resubscribe();
            }
        };

        /**
         * Task that will just finish the test.
         */
        TimerTask shutdownTask = new TimerTask() {

            @Override
            public void run() {
                System.out.println("Finish test");
                synchronized (SubscriptionMessagesHubTest.this){
                    SubscriptionMessagesHubTest.this.notifyAll();
                }
            }
        };

        try{
            mMessagesHub = new SubscriptionMessagesHub("", "", true, mErrorListener);
            mWebSocket.addListener(mMessagesHub);
            mWebSocket.connect();

            Timer timer = new Timer();
            timer.schedule(unsubscribeTask, 5000);
            timer.schedule(resubscribeTask, 10000);
            timer.schedule(shutdownTask, 20000);

            // Holding this thread while we get update notifications
            synchronized (this){
                wait();
            }
        } catch (InterruptedException e) {
            System.out.println("InterruptedException. Msg: "+e.getMessage());
        } catch (WebSocketException e) {
            System.out.println("WebSocketException. Msg: " + e.getMessage());
        }
    }

    /**
     * This test will register a {@see SubscriptionListener} and wait for an amount equal to MAX_MESSAGES
     * of {@see DynamicGlobalProperties} objects to be returned.
     *
     * The test will be deemed successfull if no errors arise in the meantime.
     */
    @Test
    public void testGlobalPropertiesDeserializer(){
        try{
            mMessagesHub = new SubscriptionMessagesHub("", "", true, mErrorListener);
            mMessagesHub.addSubscriptionListener(new SubscriptionListener() {
                private int MAX_MESSAGES = 10;
                private int messageCounter = 0;

                @Override
                public ObjectType getInterestObjectType() {
                    return ObjectType.DYNAMIC_GLOBAL_PROPERTY_OBJECT;
                }

                @Override
                public void onSubscriptionUpdate(SubscriptionResponse response) {
                    System.out.println("On block");
                    if(response.params.size() == 2){
                        try{
                            List<Object> payload = (List) response.params.get(1);
                            if(payload.size() > 0 && payload.get(0) instanceof DynamicGlobalProperties){
                                DynamicGlobalProperties globalProperties = (DynamicGlobalProperties) payload.get(0);
//                                System.out.println("time.....................: "+globalProperties.time);
//                                System.out.println("next_maintenance_time....: "+globalProperties.next_maintenance_time);
//                                System.out.println("recent_slots_filled......: "+globalProperties.recent_slots_filled);
                            }
                        }catch(Exception e){
                            System.out.println("Exception");
                            System.out.println("Type: "+e.getClass());
                            System.out.println("Msg: "+e.getMessage());
                        }
                    }
                    // Waiting for MAX_MESSAGES messages before releasing the wait lock
                    messageCounter++;
                    if(messageCounter > MAX_MESSAGES){
                        synchronized (SubscriptionMessagesHubTest.this){
                            SubscriptionMessagesHubTest.this.notifyAll();
                        }
                    }
                }
            });

            mMessagesHub.addSubscriptionListener(new SubscriptionListener() {
                @Override
                public ObjectType getInterestObjectType() {
                    return ObjectType.TRANSACTION_OBJECT;
                }

                @Override
                public void onSubscriptionUpdate(SubscriptionResponse response) {
                    System.out.println("onTx");
                }
            });
            mWebSocket.addListener(mMessagesHub);
            mWebSocket.connect();

            // Holding this thread while we get update notifications
            synchronized (this){
                wait();
            }
        } catch (WebSocketException e) {
            System.out.println("WebSocketException. Msg: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("InterruptedException. Msg: "+e.getMessage());
        }
    }

    /**
     * This is a basic test that will only display a count of operations per received broadcasted transactions.
     *
     * The test will be deemed successfull if we get to receive MAX_MESSAGES transaction objects without errors.
     */
    @Test
    public void testBroadcastedTransactionDeserializer(){
        try{
            mMessagesHub = new SubscriptionMessagesHub("", "", true, mErrorListener);
            mMessagesHub.addSubscriptionListener(new SubscriptionListener() {
                private int MAX_MESSAGES = 15;
                private int messageCounter = 0;

                @Override
                public ObjectType getInterestObjectType() {
                    return ObjectType.TRANSACTION_OBJECT;
                }

                @Override
                public void onSubscriptionUpdate(SubscriptionResponse response) {
                    if(response.params.size() == 2){
                        List<Serializable> payload = (List) response.params.get(1);
                        if(payload.size() > 0){
                            for(Serializable item : payload){
                                if(item instanceof BroadcastedTransaction){
                                    BroadcastedTransaction broadcastedTransaction = (BroadcastedTransaction) item;
                                    Transaction tx = broadcastedTransaction.getTransaction();
//                                    System.out.println(String.format("Got %d operations", tx.getOperations().size()));
                                }
                            }
                        }
                    }

                    // Waiting for MAX_MESSAGES messages before releasing the wait lock
                    messageCounter++;
                    if(messageCounter > MAX_MESSAGES){
                        synchronized (SubscriptionMessagesHubTest.this){
                            SubscriptionMessagesHubTest.this.notifyAll();
                        }
                    }
                }
            });

            mMessagesHub.addSubscriptionListener(new SubscriptionListener() {

                @Override
                public ObjectType getInterestObjectType() {
                    return ObjectType.OPERATION_HISTORY_OBJECT;
                }

                @Override
                public void onSubscriptionUpdate(SubscriptionResponse response) {
                    System.out.println("onSubscriptionUpdate. response.params.size: "+response.params.size());
                    if(response.params.size() == 2){
                        List<Serializable> payload = (List) response.params.get(1);
                        if(payload.size() > 0){
                            for(Serializable item : payload){
                                if(item instanceof OperationHistory){
                                    OperationHistory operationHistory = (OperationHistory) item;
                                    System.out.println("Operation history: <id:"+operationHistory.getObjectId()+", op: "+operationHistory.getOperation().toJsonString()+">");
                                }
                            }
                        }
                    }
                }
            });

            mWebSocket.addListener(mMessagesHub);
            mWebSocket.connect();

            // Holding this thread while we get update notifications
            synchronized (this){
                wait();
            }
        } catch (WebSocketException e) {
            System.out.println("WebSocketException. Msg: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("InterruptedException. Msg: "+e.getMessage());
        }
    }
}