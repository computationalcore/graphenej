package cy.agorise.graphenej.api.calls;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.models.ApiCall;

/**
 * Wrapper around the "get_objects" API call.
 */
public class GetObjects implements ApiCallable {
    public static final int REQUIRED_API = ApiAccess.API_DATABASE;
    private List<String> ids;

    public GetObjects(List<String> ids){
        this.ids = ids;
    }

    @Override
    public ApiCall toApiCall(int apiId, long sequenceId) {
        ArrayList<Serializable> params = new ArrayList<>();
        ArrayList<String> subParams = new ArrayList<>(ids);
        params.add(subParams);
        return new ApiCall(apiId, RPC.CALL_GET_OBJECTS, params, RPC.VERSION, sequenceId);
    }
}
