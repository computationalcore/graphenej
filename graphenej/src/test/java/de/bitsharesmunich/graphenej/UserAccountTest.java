package de.bitsharesmunich.graphenej;

import com.neovisionaries.ws.client.WebSocketException;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import de.bitsharesmunich.graphenej.api.BaseApiTest;
import de.bitsharesmunich.graphenej.api.GetObjects;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.WitnessResponse;

/**
 * Created by nelson on 5/20/17.
 */
public class UserAccountTest extends BaseApiTest {
    private final UserAccount bilthon_25 = new UserAccount("1.2.151069");

    @Test
    public void testToJsonString() {
        try{
            ArrayList<String> ids = new ArrayList<>();
            ids.add(bilthon_25.getObjectId());
            mWebSocket.addListener(new GetObjects(ids, new WitnessResponseListener() {

                @Override
                public void onSuccess(WitnessResponse response) {
                    System.out.println("onSuccess");
                    List<GrapheneObject> result = (List<GrapheneObject>) response.result;
                    UserAccount userAccount = (UserAccount) result.get(0);
                    System.out.println("user account: "+userAccount.toJsonString());

                    synchronized (UserAccountTest.this){
                        UserAccountTest.this.notifyAll();
                    }
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("onError");
                    synchronized (UserAccountTest.this){
                        UserAccountTest.this.notifyAll();
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
}