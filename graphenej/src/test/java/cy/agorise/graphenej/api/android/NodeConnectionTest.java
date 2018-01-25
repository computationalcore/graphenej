package cy.agorise.graphenej.api.android;

import com.google.common.primitives.UnsignedLong;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import junit.framework.Assert;

import org.bitcoinj.core.ECKey;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLContext;

import cy.agorise.graphenej.Address;
import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.AssetAmount;
import cy.agorise.graphenej.BaseOperation;
import cy.agorise.graphenej.BrainKey;
import cy.agorise.graphenej.Transaction;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.api.GetAccountBalances;
import cy.agorise.graphenej.api.GetAccountByName;
import cy.agorise.graphenej.api.GetAccounts;
import cy.agorise.graphenej.api.GetAllAssetHolders;
import cy.agorise.graphenej.api.GetBlockHeader;
import cy.agorise.graphenej.api.GetKeyReferences;
import cy.agorise.graphenej.api.GetLimitOrders;
import cy.agorise.graphenej.api.GetMarketHistory;
import cy.agorise.graphenej.api.GetObjects;
import cy.agorise.graphenej.api.GetRelativeAccountHistory;
import cy.agorise.graphenej.api.GetRequiredFees;
import cy.agorise.graphenej.api.GetTradeHistory;
import cy.agorise.graphenej.api.ListAssets;
import cy.agorise.graphenej.api.LookupAccounts;
import cy.agorise.graphenej.api.LookupAssetSymbols;
import cy.agorise.graphenej.api.TransactionBroadcastSequence;
import cy.agorise.graphenej.errors.MalformedAddressException;
import cy.agorise.graphenej.errors.RepeatedRequestIdException;
import cy.agorise.graphenej.interfaces.NodeErrorListener;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.BaseResponse;
import cy.agorise.graphenej.models.WitnessResponse;
import cy.agorise.graphenej.operations.TransferOperation;
import cy.agorise.graphenej.operations.TransferOperationBuilder;
import cy.agorise.graphenej.test.NaiveSSLContext;

/**
 * Created by nelson on 6/26/17.
 */
public class NodeConnectionTest {
    private String NODE_URL_1 = System.getenv("NODE_URL_1");
    private String NODE_URL_2 = System.getenv("NODE_URL_2");
    private String NODE_URL_3 = System.getenv("NODE_URL_3");
    private String NODE_URL_4 = System.getenv("NODE_URL_4");
    private String TEST_ACCOUNT_BRAIN_KEY = System.getenv("TEST_ACCOUNT_BRAIN_KEY");
    private String ACCOUNT_ID_1 = "1.2.140994";
    private String ACCOUNT_ID_2 = "1.2.138632";
    private String ACCOUNT_NAME = "bilthon-7";
    private long BlOCK_TEST_NUMBER = 11000000;
    private Asset BTS = new Asset("1.3.0");
    private Asset BLOCKPAY = new Asset("1.3.1072");
    private Asset BITUSD = new Asset("1.3.121"); //USD Smartcoin
    private Asset BITEUR = new Asset("1.3.120"); //EUR Smartcoin
    private NodeConnection nodeConnection;

    /**
     * Sample task to be scheduled
     */
    private TimerTask getAccountsTask = new TimerTask() {
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

    /**
     * Task that will release the worker thread, effectively terminating this test
     */
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
        System.out.println("** Testing simple node connection **");
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(NODE_URL_1);
        nodeConnection.connect("", "", true, mErrorListener);

        Timer timer = new Timer();
        timer.schedule(getAccountsTask, 5000);
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
    public void testWrongUrl(){
        System.out.println("** Testing simple node connection with wrong URL **");
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl("wss://non-existing-host");
        nodeConnection.connect("", "", true, new NodeErrorListener() {
            @Override
            public void onError(BaseResponse.Error error) {
                Assert.assertEquals("Can't connect, ran out of URLs!", error.message);
            }
        });

        Timer timer = new Timer();
        timer.schedule(releaseTask, 5000);

        try{
            // Holding this thread while we get update notifications
            synchronized (this){
                wait();
            }
        }catch(InterruptedException e){
            System.out.println("InterruptedException. Msg: "+e.getMessage());
        }
    }

    /**
     * Test for NodeConnection's addNodeUrl and addNodeUrls working together.
     *
     * Need to setup the NODE_URL_2 env to work. The first hard-coded URL is wrong and will
     * fail. The NodeConnection instance should recover and try the next one in the list.
     *
     */
    @Test
    public void testNodeHopFeature(){
        System.out.println("** Testing node hopping **");
        nodeConnection = NodeConnection.getInstance();

        //Test adding a "sublist"
        ArrayList<String> urlList = new ArrayList<String>(){{
            add("wss://eu.openledger.info/wrong");
            add(NODE_URL_2);
        }};
        nodeConnection.addNodeUrls(urlList);

        nodeConnection.connect("", "", true, mErrorListener);

        Timer timer = new Timer();
        timer.schedule(releaseTask, 15000);

        try{
            // Holding this thread while we get update notifications
            synchronized (this){
                wait();
            }
        }catch(InterruptedException e){
            System.out.println("InterruptedException. Msg: "+e.getMessage());
        }
    }

    /**
     * Test for GetAccountBalances Handler.
     *
     * Request balances for a valid account (Need to setup the ACCOUNT_ID_1 env with desired account id)
     *
     */
    @Test
    public void testGetAccountBalancesRequest(){
        System.out.println("** Testing GetAccountBalances request **");
        int callbackCount = 0;
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(NODE_URL_1);
        nodeConnection.connect("", "", false, mErrorListener);

        System.out.println("Adding GetAccountBalances here");
        // Trying to get the balances of ACCOUNT_ID_1 for BTS, bitUSD & bitEUR
        try{
            UserAccount userAccount = new UserAccount(ACCOUNT_ID_1);
            ArrayList<Asset> assetList = new ArrayList<>();
            assetList.add(BTS);
            assetList.add(BITUSD);
            assetList.add(BITEUR);
            System.out.println("Test: Request to discrete asset list");
            nodeConnection.addRequestHandler(new GetAccountBalances(userAccount, assetList, false, new WitnessResponseListener(){
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

        // Trying to get the balances of ACCOUNT_ID_1 for all assets
        try{
            UserAccount userAccount = new UserAccount(ACCOUNT_ID_1);
            System.out.println("Test: Request to all account' assets balance");
            nodeConnection.addRequestHandler(new GetAccountBalances(userAccount, null, false, new WitnessResponseListener(){
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

        Timer timer = new Timer();
        timer.schedule(releaseTask, 10000);
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
    /**
     * Test for GetAccountByName Handler.
     *
     * Request for a valid account name by name (Need to setup the ACCOUNT_NAME env with desired
     * account name)
     *
     */
    public void testGetAccountByNameRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(NODE_URL_1);
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

    /**
     * Test for GetAccounts Handler.
     *
     * Request for a valid account name by name (Need to setup the ACCOUNT_NAME env with desired
     * account name)
     *
     */
    public void testGetAccountsRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(NODE_URL_1);

        ArrayList<UserAccount> accountList = new ArrayList<UserAccount>(){{
            add(new UserAccount(ACCOUNT_ID_1));
            add(new UserAccount(ACCOUNT_ID_2));
        }};

        nodeConnection.connect("", "", false, mErrorListener);

        System.out.println("Adding GetAccounts for one Account ID.");
        try{
            nodeConnection.addRequestHandler(new GetAccounts(ACCOUNT_ID_1, false, new WitnessResponseListener(){
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("GetAccounts.onSuccess");
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("GetAccounts.onError. Msg: "+ error.message);
                }
            }));
        }catch(RepeatedRequestIdException e){
            System.out.println("RepeatedRequestIdException. Msg: "+e.getMessage());
        }

        System.out.println("Adding GetAccounts for a list of Account IDs.");
        try{
            nodeConnection.addRequestHandler(new GetAccounts(accountList, false, new WitnessResponseListener(){
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("GetAccounts.onSuccess");
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("GetAccounts.onError. Msg: "+ error.message);
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

    /**
     * Test for GetAllAssetHolders Handler.
     *
     */
    @Test
    public void testGetAllAssetHoldersRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(NODE_URL_1);
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

    /**
     * Test for GetBlockHeader Handler.
     *
     * Request for a valid account block header (Need to setup the BlOCK_TEST_NUMBER env with desired
     * block height)
     */
    @Test
    public void testGetBlockHeaderRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(NODE_URL_1);
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

    /**
     * Test for GetKeyReferences Handler.
     *
     */
    @Test
    public void testGetKeyReferencesRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(NODE_URL_1);
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

    /**
     * Test for GetLimitOrders Handler.
     *
     * Request for a limit orders between two assets
     */
    @Test
    public void testGetLimitOrdersRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(NODE_URL_1);
        nodeConnection.connect("", "", false, mErrorListener);

        String asset_sold_id = BLOCKPAY.getBitassetId();
        String asset_purchased_id = BTS.getBitassetId();
        int limit = 10;

        System.out.println("Adding GetLimitOrders request");
        try{
            nodeConnection.addRequestHandler(new GetLimitOrders(asset_sold_id, asset_purchased_id, limit, true, new WitnessResponseListener(){
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("GetLimitOrders.onSuccess");
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("GetLimitOrders.onError. Msg: "+ error.message);
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


    /**
     * Test for GetMarketHistory Handler.
     *
     * Request for market history of a base asset compared to a quote asset.
     */
    @Test
    public void testGetMarketHistoryRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(NODE_URL_1);
        nodeConnection.connect("", "", false, mErrorListener);

        Asset asset_base = BLOCKPAY;
        Asset asset_quote = BTS;
        //the time interval of the bucket in seconds
        long bucket = 3600;

        //datetime of of the most recent operation to retrieve
        Date start = new Date();
        //datetime of the the earliest operation to retrieve
        //07/16/2017 1:33pm
        Date end = new Date(1500211991);

        System.out.println("Adding GetMarketHistory request");
        try{
            nodeConnection.addRequestHandler(new GetMarketHistory(asset_base, asset_quote, bucket, start, end, true, new WitnessResponseListener(){
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("GetMarketHistory.onSuccess");
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("GetMarketHistory.onError. Msg: "+ error.message);
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

    /**
     * Test for GetObjects Handler.
     *
     * Request for a limit orders between two assets
     */
    @Test
    public void testGetObjectsRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(NODE_URL_1);
        nodeConnection.connect("", "", false, mErrorListener);

        String asset_sold_id = BLOCKPAY.getBitassetId();
        String asset_purchased_id = BTS.getBitassetId();
        int limit = 10;

        ArrayList<String> objectList = new ArrayList<String>(){{
            add(BLOCKPAY.getBitassetId());
            add(BTS.getBitassetId());
            add(BITEUR.getBitassetId());
        }};

        System.out.println("Adding GetObjects request");
        try{
            nodeConnection.addRequestHandler(new GetObjects(objectList, true, new WitnessResponseListener(){
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("GetObjects.onSuccess");
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("GetObjects.onError. Msg: "+ error.message);
                }
            }));
        }catch(RepeatedRequestIdException e){
            System.out.println("RepeatedRequestIdException. Msg: "+e.getMessage());
        }

        Timer timer = new Timer();
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

    /**
     * Test for GetRelativeAccount Handler.
     *
     * Request for the transaction history of a user account.
     */
    @Test
    public void testGetRelativeAccountHistoryRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(NODE_URL_1);
        nodeConnection.connect("", "", false, mErrorListener);

        UserAccount userAccount = new UserAccount(ACCOUNT_ID_1);

        //Sequence number of earliest operation
        int stop = 10;
        int limit = 50;
        //Sequence number of the most recent operation to retrieve
        int start = 50;

        System.out.println("Adding GetRelativeAccountHistory request");
        try{
            nodeConnection.addRequestHandler(new GetRelativeAccountHistory(userAccount, stop, limit, start, true, new WitnessResponseListener(){
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("GetRelativeAccountHistory.onSuccess");
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("GetRelativeAccountHistory.onError. Msg: "+ error.message);
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

    /**
     * Test for GetRequiredFees Handler.
     *
     */
    @Test
    public void testGetRequiredFeesRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(NODE_URL_1);
        nodeConnection.connect("", "", false, mErrorListener);

        UserAccount userAccount_from = new UserAccount(ACCOUNT_ID_1);
        UserAccount userAccount_to = new UserAccount(ACCOUNT_ID_2);

        //Test with 2 BTS
        Asset testAsset = new Asset("1.3.0");
        AssetAmount assetAmountTest = new AssetAmount(UnsignedLong.valueOf(200000), testAsset);
        AssetAmount feeAmountTest = new AssetAmount(UnsignedLong.valueOf(100000), testAsset);

        TransferOperation transferOperation = new TransferOperation(userAccount_from, userAccount_to, assetAmountTest, feeAmountTest);

        ArrayList<BaseOperation> operations = new ArrayList<>();
        operations.add(transferOperation);

        System.out.println("Adding GetBlockHeader request");
        try{
            nodeConnection.addRequestHandler(new GetRequiredFees(operations, testAsset, true, new WitnessResponseListener(){
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("GetRequiredFees.onSuccess");
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("GetRequiredFees.onError. Msg: "+ error.message);
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

    /**
     * Test for GetTradeHistory Handler.
     *
     */
    @Test
    public void testGetTradeHistoryRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(NODE_URL_1);
        nodeConnection.connect("", "", false, mErrorListener);

        UserAccount userAccount_from = new UserAccount(ACCOUNT_ID_1);
        UserAccount userAccount_to = new UserAccount(ACCOUNT_ID_2);


        String asset_sold_id = BLOCKPAY.getBitassetId();
        String asset_purchased_id = BTS.getBitassetId();
        int limit = 10;

        System.out.println("Adding GetTradeHistory request");
        try{
            nodeConnection.addRequestHandler(new GetTradeHistory(asset_sold_id, asset_purchased_id, "", "", limit, true, new WitnessResponseListener(){
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("GetTradeHistory.onSuccess");
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("GetTradeHistory.onError. Msg: "+ error.message);
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

    /**
     * Test for ListAsset Handler.
     *'
     * Request the  'list_assets' API call to the witness node
     */
    @Test
    public void testListAssetRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(NODE_URL_1);
        nodeConnection.connect("", "", false, mErrorListener);

        UserAccount userAccount_from = new UserAccount(ACCOUNT_ID_1);
        UserAccount userAccount_to = new UserAccount(ACCOUNT_ID_2);


        String asset_symbol = BLOCKPAY.getSymbol();
        int limit = 10;

        System.out.println("Adding ListAssets request");
        try{
            nodeConnection.addRequestHandler(new ListAssets(asset_symbol, limit, true, new WitnessResponseListener(){
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("ListAssets.onSuccess");
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("ListAssets.onError. Msg: "+ error.message);
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

    /**
     * Test for GetRelativeAccount Handler.
     *
     * Request for the transaction history of a user account.
     */
    @Test
    public void testGetLookupAccountsRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(NODE_URL_1);
        nodeConnection.connect("", "", false, mErrorListener);

        UserAccount userAccount = new UserAccount(ACCOUNT_ID_1);
        UserAccount userAccount_2 = new UserAccount(ACCOUNT_ID_2);

        int maxAccounts = 10;

        System.out.println("Adding LookupAccounts request");
        try{
            nodeConnection.addRequestHandler(new LookupAccounts(userAccount.getName(), true, new WitnessResponseListener(){
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("LookupAccounts.onSuccess");
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("LookupAccounts.onError. Msg: "+ error.message);
                }
            }));
        }catch(RepeatedRequestIdException e){
            System.out.println("RepeatedRequestIdException. Msg: "+e.getMessage());
        }

        System.out.println("Adding LookupAccounts request . maxAccounts = "+maxAccounts);
        try{
            nodeConnection.addRequestHandler(new LookupAccounts(userAccount_2.getName(), maxAccounts, true, new WitnessResponseListener(){
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("LookupAccounts.onSuccess");
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("LookupAccounts.onError. Msg: "+ error.message);
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

    /**
     * Test for LookupAssetSymbols Handler.
     *
     * Request for the assets corresponding to the provided symbols or IDs.
     */
    @Test
    public void testLookupAssetSymbolsRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(NODE_URL_1);
        nodeConnection.connect("", "", false, mErrorListener);


        ArrayList<Asset> assetList = new ArrayList<Asset>(){{
            add(BLOCKPAY);
            add(BTS);
            add(BITEUR);
        }};

        System.out.println("Adding LookupAssetSymbols request");
        try{
            nodeConnection.addRequestHandler(new LookupAssetSymbols(assetList, true, new WitnessResponseListener(){
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("LookupAssetSymbols.onSuccess");
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("LookupAssetSymbols.onError. Msg: "+ error.message);
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

    /**
     * Test for TransactionBroadcastSequence Handler.
     *
     */
    @Test
    public void testTransactionBroadcastSequenceRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(NODE_URL_1);
        nodeConnection.connect("", "", false, mErrorListener);


        ArrayList<Asset> assetList = new ArrayList<Asset>(){{
            add(BLOCKPAY);
            add(BTS);
            add(BITEUR);
        }};

        ECKey privateKey = new BrainKey(TEST_ACCOUNT_BRAIN_KEY, 0).getPrivateKey();

        UserAccount userAccount = new UserAccount(ACCOUNT_ID_1);
        UserAccount userAccount_2 = new UserAccount(ACCOUNT_ID_2);

        TransferOperation transferOperation1 = new TransferOperationBuilder()
                .setTransferAmount(new AssetAmount(UnsignedLong.valueOf(1), BTS))
                .setSource(userAccount)
                .setDestination(userAccount_2)
                .setFee(new AssetAmount(UnsignedLong.valueOf(264174), BTS))
                .build();


        ArrayList<BaseOperation> operationList = new ArrayList<>();
        operationList.add(transferOperation1);

        try{
            Transaction transaction = new Transaction(privateKey, null, operationList);

            SSLContext context = null;
            context = NaiveSSLContext.getInstance("TLS");
            WebSocketFactory factory = new WebSocketFactory();

            // Set the custom SSL context.
            factory.setSSLContext(context);

            WebSocket mWebSocket = factory.createSocket(NODE_URL_1);

            mWebSocket.addListener(new TransactionBroadcastSequence(transaction, BTS, new WitnessResponseListener(){
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("TransactionBroadcastSequence.onSuccess");
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("TransactionBroadcastSequence.onError. Msg: "+ error.message);
                }
            }));
            mWebSocket.connect();

            try{
                // Holding this thread while we get update notifications
                synchronized (this){
                    wait();
                }
            }catch(InterruptedException e){
                System.out.println("InterruptedException. Msg: "+e.getMessage());
            }

        }catch(NoSuchAlgorithmException e){
            System.out.println("NoSuchAlgoritmException. Msg: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException. Msg: " + e.getMessage());
        } catch (WebSocketException e) {
            System.out.println("WebSocketException. Msg: " + e.getMessage());
        }
    }


    private NodeErrorListener mErrorListener = new NodeErrorListener() {
        @Override
        public void onError(BaseResponse.Error error) {
            System.out.println("onError");
        }
    };
}