package cy.agorise.graphenej.api.calls;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.BaseOperation;
import cy.agorise.graphenej.BlockData;
import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.Transaction;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.models.ApiCall;

/**
 * Wrapper around the "get_required_fees" API call
 */

public class GetRequiredFees implements ApiCallable {

    public static final int REQUIRED_API = ApiAccess.API_DATABASE;

    private Transaction mTransaction;
    private Asset mFeeAsset;

    public GetRequiredFees(Transaction transaction, Asset feeAsset){
        this.mTransaction = transaction;
        this.mFeeAsset = feeAsset;
    }

    public GetRequiredFees(List<BaseOperation> operations, Asset feeAsset){
        this.mTransaction = new Transaction(new BlockData(0, 0, 0), operations);
        this.mFeeAsset = feeAsset;
    }

    @Override
    public ApiCall toApiCall(int apiId, long sequenceId) {
        // Building a new API call to request fees information
        ArrayList<Serializable> accountParams = new ArrayList<>();
        accountParams.add((Serializable) mTransaction.getOperations());
        accountParams.add(this.mFeeAsset.getObjectId());
        return new ApiCall(apiId, RPC.CALL_GET_REQUIRED_FEES, accountParams, RPC.VERSION, sequenceId);
    }
}
