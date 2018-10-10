package cy.agorise.graphenej.api.calls;

import java.io.Serializable;
import java.util.ArrayList;

import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.models.ApiCall;

/**
 * Wrapper around the "get_relative_account_history" API call
 */
public class GetRelativeAccountHistory implements ApiCallable {

    public static final int REQUIRED_API = ApiAccess.API_HISTORY;

    // API call parameters
    private UserAccount mUserAccount;
    private int stop;
    private int limit;
    private int start;

    /**
     * Constructor
     * @param userAccount
     * @param stop
     * @param limit
     * @param start
     */
    public GetRelativeAccountHistory(UserAccount userAccount, int stop, int limit, int start){
        this.mUserAccount = userAccount;
        this.stop = stop;
        this.limit = limit;
        this.start = start;
    }

    @Override
    public ApiCall toApiCall(int apiId, long sequenceId) {
        ArrayList<Serializable> params = new ArrayList<>();
        params.add(mUserAccount.getObjectId());
        params.add(this.stop);
        params.add(this.limit);
        params.add(this.start);
        return new ApiCall(apiId, RPC.CALL_GET_RELATIVE_ACCOUNT_HISTORY, params, RPC.VERSION, sequenceId);
    }
}
