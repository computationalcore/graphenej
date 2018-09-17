package cy.agorise.graphenej.api.calls;

import java.io.Serializable;
import java.util.ArrayList;

import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.models.ApiCall;

public class CancelAllSubscriptions implements ApiCallable {
    public static final int REQUIRED_API = ApiAccess.API_DATABASE;

    @Override
    public ApiCall toApiCall(int apiId, long sequenceId) {
        return new ApiCall(apiId, RPC.CALL_CANCEL_ALL_SUBSCRIPTIONS, new ArrayList<Serializable>(), RPC.VERSION, sequenceId);
    }
}
