package de.bitsharesmunich.graphenej.api.android;

import org.junit.Test;

import java.util.Timer;
import java.util.TimerTask;

import de.bitsharesmunich.graphenej.api.GetAccounts;
import de.bitsharesmunich.graphenej.errors.RepeatedRequestIdException;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.WitnessResponse;

/**
 * Created by nelson on 6/26/17.
 */
public class NodeConnectionTest {
    private String BLOCK_PAY_DE = System.getenv("OPENLEDGER_EU");
    private NodeConnection nodeConnection;

    private TimerTask subscribeTask = new TimerTask() {
        @Override
        public void run() {
            System.out.println("Adding request here");
            try{
                nodeConnection.addRequestHandler(new GetAccounts("1.2.100", false, new WitnessResponseListener(){

                    @Override
                    public void onSuccess(WitnessResponse response) {
                        System.out.println("getAccounts.onSuccess");
                    }

                    @Override
                    public void onError(BaseResponse.Error error) {
                        System.out.println("getAccounts.onError. Msg: "+ error.message);
                    }
                }));
            }catch(RepeatedRequestIdException e){
                System.out.println("RepeatedRequestIdException. Msg: "+e.getMessage());
            }
        }
    };

    private TimerTask releaseTask = new TimerTask() {
        @Override
        public void run() {
            System.out.println("Releasing lock!");
            synchronized (NodeConnectionTest.this){
                NodeConnectionTest.this.notifyAll();
            }
        }
    };

    @Test
    public void testNodeConnection(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(BLOCK_PAY_DE);
        nodeConnection.connect("", "", true, mErrorListener);

        Timer timer = new Timer();
        timer.schedule(subscribeTask, 5000);
        timer.schedule(releaseTask, 30000);

        try{
            // Holding this thread while we get update notifications
            synchronized (this){
                wait();
            }
        }catch(InterruptedException e){
            System.out.println("InterruptedException. Msg: "+e.getMessage());
        }
    }

    private WitnessResponseListener mErrorListener = new WitnessResponseListener() {

        @Override
        public void onSuccess(WitnessResponse response) {
            System.out.println("onSuccess");
        }

        @Override
        public void onError(BaseResponse.Error error) {
            System.out.println("onError");
        }
    };
}