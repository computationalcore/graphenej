package cy.agorise.graphenej.api.calls;

import java.io.Serializable;
import java.util.ArrayList;

import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.models.ApiCall;

public class ListAssets implements ApiCallable {

    public static final int REQUIRED_API = ApiAccess.API_DATABASE;

    /**
     * Constant that must be used as argument to the constructor of this class to indicate
     * that the user wants to get all existing assets.
     */
    public static final int LIST_ALL = -1;

    /**
     * Internal constant used to represent the maximum limit of assets retrieved in one call.
     */
    public static final int MAX_BATCH_SIZE = 100;

    private String lowerBound;
    private int limit;

    public ListAssets(String lowerBoundSymbol, int limit){
        this.lowerBound = lowerBoundSymbol;
        this.limit = limit;
    }

    @Override
    public ApiCall toApiCall(int apiId, long sequenceId) {
        ArrayList<Serializable> params = new ArrayList<>();
        params.add(this.lowerBound);
        if(limit > MAX_BATCH_SIZE || limit == LIST_ALL){
            params.add(MAX_BATCH_SIZE);
        }else{
            params.add(this.limit);
        }
        return new ApiCall(apiId, RPC.CALL_LIST_ASSETS, params, RPC.VERSION, sequenceId);
    }
}
