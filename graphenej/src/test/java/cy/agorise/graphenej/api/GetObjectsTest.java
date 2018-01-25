package cy.agorise.graphenej.api;

import com.neovisionaries.ws.client.WebSocketException;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.GrapheneObject;
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
    private final String bitAssetId = "2.4.13";

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
    public void testBitAssetData(){
        try{
            ArrayList<String> ids = new ArrayList<>();
            ids.add(bitAssetId);
            mWebSocket.addListener(new GetObjects(ids, new WitnessResponseListener() {

                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("onSuccess");
                    List<GrapheneObject> list = (List<GrapheneObject>) response.result;
                    BitAssetData bitAssetData = (BitAssetData) list.get(0);
                    System.out.println("feed time: " + bitAssetData.current_feed_publication_time);
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("onError");
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