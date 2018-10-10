package cy.agorise.graphenej.api.calls;

import java.io.Serializable;
import java.util.ArrayList;

import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.models.ApiCall;

public class GetAccountByName implements ApiCallable {
    public static final int REQUIRED_API = ApiAccess.API_NONE;

    private String accountName;

    public GetAccountByName(String name){
        this.accountName = name;
    }

    @Override
    public ApiCall toApiCall(int apiId, long sequenceId) {
        ArrayList<Serializable> accountParams = new ArrayList<>();
        accountParams.add(this.accountName);
        return new ApiCall(apiId, RPC.CALL_GET_ACCOUNT_BY_NAME, accountParams, RPC.VERSION, sequenceId);
    }
}
