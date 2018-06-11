package cy.agorise.graphenej.api.calls;

import java.io.Serializable;
import java.util.ArrayList;

import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.models.ApiCall;

/**
 * Wrapper around the "get_block" API call.
 */

public class GetBlock implements ApiCallable {

    public static final int REQUIRED_API = ApiAccess.API_DATABASE;

    private long blockNumber;

    public GetBlock(long blockNum){
        this.blockNumber = blockNum;
    }

    public ApiCall toApiCall(int apiId, long sequenceId){
        ArrayList<Serializable> params = new ArrayList<>();
        String blockNum = String.format("%d", this.blockNumber);
        params.add(blockNum);
        return new ApiCall(apiId, RPC.CALL_GET_BLOCK, params, RPC.VERSION, sequenceId);
    }
}
