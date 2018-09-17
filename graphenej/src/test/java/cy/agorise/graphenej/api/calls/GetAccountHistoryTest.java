package cy.agorise.graphenej.api.calls;

import junit.framework.Assert;

import org.junit.Test;

import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.models.ApiCall;

public class GetAccountHistoryTest {

    @Test
    public void testSerialization(){
        UserAccount userAccount = new UserAccount("1.2.139293");
        String end = "1.11.225030218";
        String start = "1.11.225487973";
        int limit = 20;
        GetAccountHistory getAccountHistory = new GetAccountHistory(userAccount, start, end, limit);
        ApiCall apiCall = getAccountHistory.toApiCall(2, 3);
        String serialized = apiCall.toJsonString();
        System.out.println("> "+serialized);
        String expected = "{\"id\":3,\"method\":\"call\",\"params\":[2,\"get_account_history\",[\"1.2.139293\",\"1.11.225030218\",20,\"1.11.225487973\"]],\"jsonrpc\":\"2.0\"}";
        Assert.assertEquals("Serialized is as expected", expected, serialized);
    }
}
