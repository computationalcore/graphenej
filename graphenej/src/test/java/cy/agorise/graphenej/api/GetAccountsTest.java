package cy.agorise.graphenej.api;

import com.neovisionaries.ws.client.WebSocketException;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.AccountProperties;
import cy.agorise.graphenej.models.BaseResponse;
import cy.agorise.graphenej.models.WitnessResponse;

public class GetAccountsTest extends BaseApiTest {
    private UserAccount ltmAccount = new UserAccount("1.2.99700");
    private UserAccount nonLtmAccount = new UserAccount("1.2.140994");

    @Test
    public void testGetAccount(){
        ArrayList<UserAccount> userAccounts = new ArrayList<>();
        userAccounts.add(ltmAccount);
        userAccounts.add(nonLtmAccount);
        mWebSocket.addListener(new GetAccounts(userAccounts, true, new WitnessResponseListener(){

            @Override
            public void onSuccess(WitnessResponse response) {
                System.out.println("onSuccess.");
                List<AccountProperties> accounts = (List<AccountProperties>) response.result;
                System.out.println(String.format("Got %d accounts", accounts.size()));
                for(AccountProperties accountProperties : accounts){
                    System.out.println("account name....: "+accountProperties.name);
                    System.out.println("expiration date.: "+accountProperties.membership_expiration_date);
                }
                AccountProperties ltmAccountProperties = accounts.get(0);
                AccountProperties nonLtmAccountProperties = accounts.get(1);
                Assert.assertEquals(ltmAccountProperties.membership_expiration_date, UserAccount.LIFETIME_EXPIRATION_DATE);
                Assert.assertFalse(nonLtmAccountProperties.membership_expiration_date.equals(UserAccount.LIFETIME_EXPIRATION_DATE));
                synchronized (GetAccountsTest.this){
                    GetAccountsTest.this.notifyAll();
                }
            }

            @Override
            public void onError(BaseResponse.Error error) {
                System.out.println("onError. Msg: "+error.message);
                synchronized (GetAccountsTest.this){
                    GetAccountsTest.this.notifyAll();
                }
            }
        }));

        try{
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
