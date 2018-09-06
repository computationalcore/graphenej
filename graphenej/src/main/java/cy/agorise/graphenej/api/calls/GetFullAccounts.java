package cy.agorise.graphenej.api.calls;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.models.ApiCall;

/**
 * Wrapper around the 'get_full_accounts' API call.
 */
public class GetFullAccounts implements ApiCallable {

    public static final int REQUIRED_API = ApiAccess.API_NONE;

    private List<String> mUserAccounts;
    private boolean mSubscribe;

    public GetFullAccounts(List<String> accounts, boolean subscribe){
        this.mUserAccounts = accounts;
        this.mSubscribe = subscribe;
    }

    @Override
    public ApiCall toApiCall(int apiId, long sequenceId) {
        ArrayList<Serializable> params = new ArrayList<>();
        ArrayList<Serializable> accounts = new ArrayList<Serializable>(mUserAccounts);
        params.add(accounts);
        params.add(mSubscribe);
        return new ApiCall(apiId, RPC.CALL_GET_FULL_ACCOUNTS, params, RPC.VERSION, sequenceId);
    }
}
