package de.bitsharesmunich.graphenej.api.android;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import com.google.common.primitives.UnsignedLong;

import de.bitsharesmunich.graphenej.Asset;
import de.bitsharesmunich.graphenej.OperationType;
import de.bitsharesmunich.graphenej.api.GetAccounts;
import de.bitsharesmunich.graphenej.api.GetAccountBalances;
import de.bitsharesmunich.graphenej.api.GetAccountByName;
import de.bitsharesmunich.graphenej.api.GetAllAssetHolders;
import de.bitsharesmunich.graphenej.api.GetBlockHeader;
import de.bitsharesmunich.graphenej.api.GetKeyReferences;
import de.bitsharesmunich.graphenej.api.GetLimitOrders;
import de.bitsharesmunich.graphenej.api.GetRequiredFees;
import de.bitsharesmunich.graphenej.errors.RepeatedRequestIdException;
import de.bitsharesmunich.graphenej.errors.MalformedAddressException;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.WitnessResponse;
import de.bitsharesmunich.graphenej.AssetAmount;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.BaseOperation;
import de.bitsharesmunich.graphenej.operations.TransferOperation;

/**
 * Created by nelson on 6/26/17.
 */
public class NodeConnectionTest {
    private String NODE_URL_1 = System.getenv("NODE_URL_1");
    private String NODE_URL_2 = System.getenv("NODE_URL_2");
    private String NODE_URL_3 = System.getenv("NODE_URL_3");
    private String NODE_URL_4 = System.getenv("NODE_URL_4");
    private String ACCOUNT_ID_1 = System.getenv("ACCOUNT_ID_1");
    private String ACCOUNT_ID_2 = System.getenv("ACCOUNT_ID_2");
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
        nodeConnection.addNodeUrl(NODE_URL_1);
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
    /**
     * Test for NodeConnection's addNodeUrl and addNodeUrls working together.
     *
     * Need to setup the NODE_URL_(1 to 4) env to work. Some of the nodes may have invalid nodes
     * websockets URL just to test the hop.
     *
     */
    public void testNodeHopFeature(){
        nodeConnection = NodeConnection.getInstance();
        //nodeConnection.addNodeUrl(NODE_URL_4);
        //Test adding a "sublist"
        ArrayList<String> urlList = new ArrayList<String>(){{
            add(NODE_URL_3);
            add(NODE_URL_3);
        }};
        //nodeConnection.addNodeUrls(urlList);
        nodeConnection.addNodeUrl(NODE_URL_1);

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

    /**
     * Test for GetAccountBalances Handler.
     *
     * Request balances for a valid account (Need to setup the ACCOUNT_ID_1 env with desired account id)
     *
     */
    @Test
    public void testGetAccountBalancesRequest(){
        nodeConnection = NodeConnection.getInstance();
        nodeConnection.addNodeUrl(NODE_URL_1);
        nodeConnection.connect("", "", false, mErrorListener);

        System.out.println("Adding GetAccountBalances here");
        try{
            UserAccount userAccount = new UserAccount(ACCOUNT_ID_1);
            ArrayList<Asset> assetList = new ArrayList<>();
            assetList.add(BTS);
            assetList.add(BITDOLAR);
            assetList.add(BITEURO);
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
     * Test for GetMarketHistory Handler.
     *
     * Request for a valid account block header (Need to setup the BlOCK_TEST_NUMBER env with desired
     * block height)
     */
    @Test
    public void testGetMarketHistoryRequest(){
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

        TransferOperation transferOperation = new TransferOperation(userAccount_from, userAccount_to, assetAmountTest, assetAmountTest);

        ArrayList<BaseOperation> operations = new ArrayList<>();
        operations.add(transferOperation);

        System.out.println("Adding GetRequiredFees request");
        try{
            nodeConnection.addRequestHandler(new GetRequiredFees(operations, testAsset, false, new WitnessResponseListener(){
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