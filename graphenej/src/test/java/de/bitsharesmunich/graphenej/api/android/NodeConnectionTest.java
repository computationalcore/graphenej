package de.bitsharesmunich.graphenej.api.android;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.bitsharesmunich.graphenej.Asset;
import de.bitsharesmunich.graphenej.api.GetAccounts;
import de.bitsharesmunich.graphenej.api.GetAccountBalances;
import de.bitsharesmunich.graphenej.api.GetAccountByName;
import de.bitsharesmunich.graphenej.api.GetAllAssetHolders;
import de.bitsharesmunich.graphenej.api.GetBlockHeader;
import de.bitsharesmunich.graphenej.api.GetKeyReferences;
import de.bitsharesmunich.graphenej.api.GetLimitOrders;
import de.bitsharesmunich.graphenej.errors.RepeatedRequestIdException;
import de.bitsharesmunich.graphenej.errors.MalformedAddressException;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.WitnessResponse;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.Address;

/**
 * Created by nelson on 6/26/17.
 */
public class NodeConnectionTest {
    private String BLOCK_PAY_DE = System.getenv("OPENLEDGER_EU");
    private String NODE_URL_1 = System.getenv("NODE_URL_1");
    private String NODE_URL_2 = System.getenv("NODE_URL_2");
    private String NODE_URL_3 = System.getenv("NODE_URL_3");
    private String ACCOUNT_ID = System.getenv("ACCOUNT_ID");
    private String ACCOUNT_NAME = System.getenv("ACCOUNT_NAME");
    private long BlOCK_TEST_NUMBER = Long.parseLong(System.getenv("BlOCK_TEST_NUMBER"));
    private Asset BTS = new Asset("1.3.0");
    private Asset BITDOLAR = new Asset("1.3.121"); //USD Smartcoin
    private Asset BITEURO = new Asset("1.3.120"); //EUR Smartcoin
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

    @Test
    public void testNodeHopFeature(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(NODE_URL_1);
        //Test adding a "sublist"
        ArrayList<String> urlList = new ArrayList<String>(){{
            add(NODE_URL_1);
            add(NODE_URL_2);
        }};
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

    @Test
    public void testGetAccountBalancesRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(BLOCK_PAY_DE);
        nodeConnection.connect("", "", false, mErrorListener);

        System.out.println("Adding GetAccountBalances here");
        try{
            UserAccount userAccount = new UserAccount(ACCOUNT_ID);
            ArrayList<Asset> assetList = new ArrayList<>();
            assetList.add(BTS);
            assetList.add(BITDOLAR);
            assetList.add(BITEURO);
            nodeConnection.addRequestHandler(new GetAccountBalances(userAccount, false, assetList, new WitnessResponseListener(){
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("getAccountBalances.onSuccess");
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("getAccountBalances.onError. Msg: "+ error.message);
                }
            }));
        }catch(RepeatedRequestIdException e){
            System.out.println("RepeatedRequestIdException. Msg: "+e.getMessage());
        }

        try{
            // Holding this thread while we get update notifications
            synchronized (this){
                wait();
            }
        }catch(InterruptedException e){
            System.out.println("InterruptedException. Msg: "+e.getMessage());
        }
    }

    @Test
    public void testGetAccountByNameRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(BLOCK_PAY_DE);
        nodeConnection.connect("", "", false, mErrorListener);

        System.out.println("Adding GetAccountByName here");
        try{
            nodeConnection.addRequestHandler(new GetAccountByName(ACCOUNT_NAME, false, new WitnessResponseListener(){
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("GetAccountByName.onSuccess");
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("GetAccountByName.onError. Msg: "+ error.message);
                }
            }));
        }catch(RepeatedRequestIdException e){
            System.out.println("RepeatedRequestIdException. Msg: "+e.getMessage());
        }

        try{
            // Holding this thread while we get update notifications
            synchronized (this){
                wait();
            }
        }catch(InterruptedException e){
            System.out.println("InterruptedException. Msg: "+e.getMessage());
        }
    }

    @Test
    public void testGetAllAssetHoldersRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(BLOCK_PAY_DE);
        nodeConnection.connect("", "", false, mErrorListener);

        System.out.println("Adding GetAllAssetHolders request");
        try{
            nodeConnection.addRequestHandler(new GetAllAssetHolders(false, new WitnessResponseListener(){
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("GetAllAssetHolders.onSuccess");
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("GetAllAssetHolders.onError. Msg: "+ error.message);
                }
            }));
        }catch(RepeatedRequestIdException e){
            System.out.println("RepeatedRequestIdException. Msg: "+e.getMessage());
        }

        try{
            // Holding this thread while we get update notifications
            synchronized (this){
                wait();
            }
        }catch(InterruptedException e){
            System.out.println("InterruptedException. Msg: "+e.getMessage());
        }
    }

    @Test
    public void testGetBlockHeaderRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(BLOCK_PAY_DE);
        nodeConnection.connect("", "", false, mErrorListener);


        System.out.println("Adding GetBlockHeader request");
        try{
            nodeConnection.addRequestHandler(new GetBlockHeader(BlOCK_TEST_NUMBER,false, new WitnessResponseListener(){
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("GetBlockHeader.onSuccess");
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("GetBlockHeader.onError. Msg: "+ error.message);
                }
            }));
        }catch(RepeatedRequestIdException e){
            System.out.println("RepeatedRequestIdException. Msg: "+e.getMessage());
        }


        try{
            // Holding this thread while we get update notifications
            synchronized (this){
                wait();
            }
        }catch(InterruptedException e){
            System.out.println("InterruptedException. Msg: "+e.getMessage());
        }
    }

    @Test
    public void testGetKeyReferencesRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(BLOCK_PAY_DE);
        nodeConnection.connect("", "", false, mErrorListener);

        Address address1 = null;
        Address address2 = null;
        Address address3 = null;
        try {
            address1 = new Address("BTS8RiFgs8HkcVPVobHLKEv6yL3iXcC9SWjbPVS15dDAXLG9GYhnY");
            address2 = new Address("BTS8RiFgs8HkcVPVobHLKEv6yL3iXcC9SWjbPVS15dDAXLG9GYhnY");
            address3 = new Address("BTS8RiFgs8HkcVPVobHLKEv6yL3iXcC9SWjbPVS15dDAXLG9GYp00");
        } catch (MalformedAddressException e) {
            System.out.println("MalformedAddressException. Msg: " + e.getMessage());
        }

        ArrayList<Address> addresses = new ArrayList<>();
        addresses.add(address1);
        addresses.add(address2);
        addresses.add(address3);

        // Test with the one address constructor
        System.out.println("Adding GetKeyReferences one address request (One address)");
        try{
            nodeConnection.addRequestHandler(new GetKeyReferences(address1, false, new WitnessResponseListener(){
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("GetKeyReferences.onSuccess");
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("GetKeyReferences.onError. Msg: "+ error.message);
                }
            }));
        }catch(RepeatedRequestIdException e){
            System.out.println("RepeatedRequestIdException. Msg: "+e.getMessage());
        }

        // Test with the list of addresses constructor
        System.out.println("Adding GetKeyReferences address request (List of Addresses)");
        try{
            nodeConnection.addRequestHandler(new GetKeyReferences(addresses, false, new WitnessResponseListener(){
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("GetKeyReferences.onSuccess");
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("GetKeyReferences.onError. Msg: "+ error.message);
                }
            }));
        }catch(RepeatedRequestIdException e){
            System.out.println("RepeatedRequestIdException. Msg: "+e.getMessage());
        }

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