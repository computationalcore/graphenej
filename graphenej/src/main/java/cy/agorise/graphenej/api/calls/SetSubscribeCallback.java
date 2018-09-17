package cy.agorise.graphenej.api.calls;

import java.io.Serializable;
import java.util.ArrayList;

import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.models.ApiCall;

public class SetSubscribeCallback implements ApiCallable {
    public static final int REQUIRED_API = ApiAccess.API_DATABASE;

    private boolean clearFilter;

    public SetSubscribeCallback(boolean clearFilter){
        this.clearFilter = clearFilter;
    }

    @Override
    public ApiCall toApiCall(int apiId, long sequenceId) {
        ArrayList<Serializable> subscriptionParams = new ArrayList<>();
        subscriptionParams.add(new Long(sequenceId));
        subscriptionParams.add(clearFilter);
        return new ApiCall(apiId, RPC.CALL_SET_SUBSCRIBE_CALLBACK, subscriptionParams, RPC.VERSION, sequenceId);
    }
}
