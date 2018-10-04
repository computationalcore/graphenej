package cy.agorise.graphenej.api.calls;

import java.io.Serializable;
import java.util.ArrayList;

import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.models.ApiCall;

public class GetDynamicGlobalProperties implements ApiCallable {
    public static final int REQUIRED_API = ApiAccess.API_NONE;

    @Override
    public ApiCall toApiCall(int apiId, long sequenceId) {
        ArrayList<Serializable> params = new ArrayList<>();
        return new ApiCall(apiId, RPC.CALL_GET_DYNAMIC_GLOBAL_PROPERTIES, params, RPC.VERSION, sequenceId);
    }
}
