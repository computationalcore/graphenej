package cy.agorise.graphenej.api;

import com.neovisionaries.ws.client.WebSocketException;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import cy.agorise.graphenej.Address;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.errors.MalformedAddressException;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.BaseResponse;
import cy.agorise.graphenej.models.WitnessResponse;

/**
 * Created by nelson on 4/14/17.
 */
public class GetKeyReferencesTest extends BaseApiTest {

    private String[] publicKeys = new String[] {
        "BTS8DuGHXpHYedq7qhT65BEEdQPvLT8nxZ862Hf8NgvSZUMuwUFkn",
        "BTS53ehf9Qoeg9o4E1KuxdZRXCVg3Z9ApbEDHVdQhERDJDEFkPkGs"
    };

    @Test
    public void testGetKeyReferences(){
        ArrayList<Address> addresses = new ArrayList<>();
        for(String addr : publicKeys){
            try {
                Address address = new Address(addr);
                addresses.add(address);
            } catch (MalformedAddressException e) {
                System.out.println("MalformedAddressException. Msg: "+e.getMessage());
            }
        }
        mWebSocket.addListener(new GetKeyReferences(addresses, new WitnessResponseListener() {
            @Override
            public void onSuccess(WitnessResponse response) {
                System.out.println("onSuccess");
                int counter = 0;
                List<List<UserAccount>> accountListList = (List<List<UserAccount>>) response.result;
                for(List<UserAccount> accountList : accountListList){
                    for(UserAccount userAccount : accountList){
                        System.out.println("User account: "+userAccount.getObjectId());
                    }
                    if(accountList.size() > 1){
                        System.out.println("Key with address: "+publicKeys[counter]+" controls more than one role in account: "+accountList.get(0).getObjectId());
                    }else if(accountList.size() == 1){
                        System.out.println("Key with address: "+publicKeys[counter]+" controls just one role in account: "+accountList.get(0).getObjectId());
                    }
                    counter++;
                }
                synchronized (GetKeyReferencesTest.this){
                    GetKeyReferencesTest.this.notifyAll();
                }
            }

            @Override
            public void onError(BaseResponse.Error error) {
                System.out.println("onError. Msg: "+error.message);

                synchronized (GetKeyReferencesTest.this){
                    GetKeyReferencesTest.this.notifyAll();
                }
            }
        }));

        try{
            mWebSocket.connect();
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