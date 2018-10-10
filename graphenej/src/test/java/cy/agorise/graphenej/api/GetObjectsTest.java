package cy.agorise.graphenej.api;

import com.neovisionaries.ws.client.WebSocketException;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.GrapheneObject;
import cy.agorise.graphenej.Price;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.BaseResponse;
import cy.agorise.graphenej.models.BitAssetData;
import cy.agorise.graphenej.models.WitnessResponse;

/**
 * Testing the {@link GetObjects} API wrapper and its deserialization
 *
 * Created by nelson on 5/10/17.
 */
public class GetObjectsTest extends BaseApiTest{
    private final Asset asset = new Asset("1.3.0", "BTS", 5);
    private final UserAccount account = new UserAccount("1.2.116354");
    private final UserAccount bilthon_25 = new UserAccount("1.2.151069");
    private UserAccount ltmAccount = new UserAccount("1.2.99700");
    private final String[] bitAssetIds = new String[]{"2.4.21", "2.4.83"};

    @Test
    public void testGetAsset(){
        try{
            ArrayList<String> ids = new ArrayList<>();
            ids.add(asset.getObjectId());
            mWebSocket.addListener(new GetObjects(ids, new WitnessResponseListener() {
                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("onSuccess");
                    List<GrapheneObject> result = (List<GrapheneObject>) response.result;
                    System.out.println("Got " + result.size() + " result");
                    Assert.assertEquals("Making sure we only get one address back", 1, result.size());
                    synchronized (GetObjectsTest.this){
                        GetObjectsTest.this.notifyAll();
                    }
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("onError");
                    synchronized (GetObjectsTest.this){
                        GetObjectsTest.this.notifyAll();
                    }
                }
            }));

            mWebSocket.connect();
            synchronized (this){
                wait();
            }
        }catch (WebSocketException e) {
            System.out.println("WebSocketException. Msg: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("InterruptedException. Msg: "+e.getMessage());
        }
    }

    @Test
    public void testGetAccount(){
        try{
            ArrayList<String> ids = new ArrayList<>();
            ids.add(bilthon_25.getObjectId());
            mWebSocket.addListener(new GetObjects(ids, new WitnessResponseListener() {

                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("onSuccess");
                    List<GrapheneObject> result = (List<GrapheneObject>) response.result;
                    UserAccount userAccount = (UserAccount) result.get(0);
                    System.out.println("Account name.....: "+userAccount.getName());
                    System.out.println("json string......: "+userAccount.toJsonString());
                    System.out.println("owner............: "+userAccount.getOwner().getKeyAuthList().get(0).getAddress());
                    System.out.println("active key.......: "+userAccount.getActive().getKeyAuthList().get(0).getAddress());
                    System.out.println("active account...: "+userAccount.getActive().getAccountAuthList().get(0).getObjectId());
                    System.out.println("memo: "+userAccount.getOptions().getMemoKey().getAddress());
                    synchronized (GetObjectsTest.this){
                        GetObjectsTest.this.notifyAll();
                    }
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("onError");
                    synchronized (GetObjectsTest.this){
                        GetObjectsTest.this.notifyAll();
                    }
                }
            }));

            mWebSocket.connect();
            synchronized (this){
                wait();
            }
        }catch (WebSocketException e) {
            System.out.println("WebSocketException. Msg: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("InterruptedException. Msg: "+e.getMessage());
        }
    }

    @Test
    public void testGetLtmAccount(){
        ArrayList<String> ids = new ArrayList<>();
        ids.add(ltmAccount.getObjectId());
        mWebSocket.addListener(new GetObjects(ids, new WitnessResponseListener() {

            @Override
            public void onSuccess(WitnessResponse response) {
                System.out.println("onSuccess");
                List<GrapheneObject> result = (List<GrapheneObject>) response.result;
                UserAccount userAccount = (UserAccount) result.get(0);
                System.out.println("Account name.....: "+userAccount.getName());
                System.out.println("Is LTM...........: "+userAccount.isLifeTime());
                System.out.println("json string......: "+userAccount.toJsonString());
                System.out.println("owner............: "+userAccount.getOwner().getKeyAuthList().get(0).getAddress());
                System.out.println("active key.......: "+userAccount.getActive().getKeyAuthList().get(0).getAddress());
                System.out.println("memo: "+userAccount.getOptions().getMemoKey().getAddress());
                Assert.assertEquals("We expect this account to be LTM",true, userAccount.isLifeTime());
                synchronized (GetObjectsTest.this){
                    GetObjectsTest.this.notifyAll();
                }
            }

            @Override
            public void onError(BaseResponse.Error error) {
                System.out.println("onError");
                synchronized (GetObjectsTest.this){
                    GetObjectsTest.this.notifyAll();
                }
            }
        }));

        try {
            mWebSocket.connect();
            synchronized (this){
                wait();
            }
        }catch (WebSocketException e) {
            System.out.println("WebSocketException. Msg: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("InterruptedException. Msg: "+e.getMessage());
        }
    }

    @Test
    public void testBitAssetData(){
        try{
            ArrayList<String> ids = new ArrayList<>();
            for(String bitAssetId : bitAssetIds){
                ids.add(bitAssetId);
            }
            mWebSocket.addListener(new GetObjects(ids, new WitnessResponseListener() {

                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("onSuccess");
                    List<BitAssetData> list = (List<BitAssetData>) response.result;
                    System.out.println("Response array length: "+list.size());
                    BitAssetData bitAssetData1 = list.get(0);
                    BitAssetData bitAssetData2 = list.get(1);

                    Price price1 = bitAssetData1.getCurrentFeed().getSettlementPrice();
                    Price price2 = bitAssetData2.getCurrentFeed().getSettlementPrice();

                    System.out.println("Bitasset data 1");
                    System.out.println("Price 1: "+price1.toString());

                    System.out.println("Bitasset data 2");
                    System.out.println("Price 1: "+price2.toString());

                    synchronized (GetObjectsTest.this){
                        GetObjectsTest.this.notifyAll();
                    }
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("onError");
                    synchronized (GetObjectsTest.this){
                        GetObjectsTest.this.notifyAll();
                    }
                }
            }));

            mWebSocket.connect();
            synchronized (this){
                wait();
            }
        }catch (WebSocketException e) {
            System.out.println("WebSocketException. Msg: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("InterruptedException. Msg: "+e.getMessage());
        }    }
}