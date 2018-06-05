package cy.agorise.graphenej.api.calls;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.models.ApiCall;

/**
 * Wrapper around the "get_accounts" API call.
 */
public class GetAccounts implements ApiCallable {
    public static final int REQUIRED_API = ApiAccess.API_NONE;

    private List<UserAccount> mUserAccounts;

    public GetAccounts(List<UserAccount> accountList){
        mUserAccounts = accountList;
    }

    public GetAccounts(UserAccount userAccount){
        mUserAccounts = new ArrayList<>();
        mUserAccounts.add(userAccount);
    }

    @Override
    public ApiCall toApiCall(int apiId, long sequenceId) {
        ArrayList<Serializable> params = new ArrayList<>();
        ArrayList<Serializable> accountIds = new ArrayList<>();
        for(UserAccount userAccount : mUserAccounts){
            accountIds.add(userAccount.getObjectId());
        }
        params.add(accountIds);
        return new ApiCall(apiId, RPC.CALL_GET_ACCOUNTS, params, RPC.VERSION, sequenceId);
    }
}
