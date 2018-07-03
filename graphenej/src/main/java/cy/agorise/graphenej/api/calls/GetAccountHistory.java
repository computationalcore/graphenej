package cy.agorise.graphenej.api.calls;

import java.io.Serializable;
import java.util.ArrayList;

import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.models.ApiCall;

public class GetAccountHistory implements ApiCallable {
    public static final int REQUIRED_API = ApiAccess.API_HISTORY;

    private UserAccount mUserAccount;
    private String startOperation;
    private String endOperation;
    private int limit;

    public GetAccountHistory(UserAccount userAccount, String start, String end, int limit){
        this.mUserAccount = userAccount;
        this.startOperation = start;
        this.endOperation = end;
        this.limit = limit;
    }

    public GetAccountHistory(String userId, String start, String end, int limit){
        this.mUserAccount = new UserAccount(userId);
        this.startOperation = start;
        this.endOperation = end;
        this.limit = limit;
    }


    @Override
    public ApiCall toApiCall(int apiId, long sequenceId) {
        ArrayList<Serializable> params = new ArrayList<>();
        params.add(mUserAccount.getObjectId());
        params.add(endOperation);
        params.add(limit);
        params.add(startOperation);
        return new ApiCall(apiId, RPC.CALL_GET_ACCOUNT_HISTORY, params, RPC.VERSION, sequenceId);
    }
}
