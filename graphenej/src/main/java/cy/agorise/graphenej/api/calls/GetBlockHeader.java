package cy.agorise.graphenej.api.calls;

import java.io.Serializable;
import java.util.ArrayList;

import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.models.ApiCall;

/**
 * Wrapper around the "get_block_header" API call. To be used in the single-connection mode.
 */
public class GetBlockHeader implements ApiCallable {
    public static final int REQUIRED_API = ApiAccess.API_DATABASE;

    private long blockNumber;

    public GetBlockHeader(long number){
        this.blockNumber = number;
    }

    @Override
    public ApiCall toApiCall(int apiId, long sequenceId) {
        ArrayList<Serializable> params = new ArrayList<>();
        String blockNum = String.format("%d", this.blockNumber);
        params.add(blockNum);

        return new ApiCall(apiId, RPC.CALL_GET_BLOCK_HEADER, params, RPC.VERSION, sequenceId);
    }
}
