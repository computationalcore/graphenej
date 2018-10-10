package cy.agorise.graphenej.api.calls;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cy.agorise.graphenej.OperationType;
import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.models.ApiCall;

public class GetAccountHistoryByOperations implements ApiCallable {

    public static final int REQUIRED_API = ApiAccess.API_HISTORY;

    private UserAccount mUserAccount;
    private List<OperationType> mOperationTypes;
    private long mStart;
    private long mLimit;

    /**
     * @param userAccount       The user account that should be queried
     * @param operationsTypes   The IDs of the operation we want to get operations in the account( 0 = transfer , 1 = limit order create, ...)
     * @param start             The sequence number where to start listing operations
     * @param limit             The max number of entries to return (from start number)
     */
    public GetAccountHistoryByOperations(UserAccount userAccount, List<OperationType> operationsTypes, long start, long limit){
        this.mUserAccount = userAccount;
        this.mOperationTypes = operationsTypes;
        this.mStart = start;
        this.mLimit = limit;
    }

    /**
     * @param userAccount       The user account that should be queried
     * @param operationsTypes   The IDs of the operation we want to get operations in the account( 0 = transfer , 1 = limit order create, ...)
     * @param start             The sequence number where to start listing operations
     * @param limit             The max number of entries to return (from start number)
     */
    public GetAccountHistoryByOperations(String userAccount, List<OperationType> operationsTypes, long start, long limit){
        if(userAccount.matches("^1\\.2\\.\\d*$")){
            this.mUserAccount = new UserAccount(userAccount);
        }else{
            this.mUserAccount = new UserAccount("", userAccount);
        }
        this.mOperationTypes = operationsTypes;
        this.mStart = start;
        this.mLimit = limit;
    }

    @Override
    public ApiCall toApiCall(int apiId, long sequenceId) {
        ArrayList<Serializable> params = new ArrayList<>();
        if(mUserAccount.getName() != null){
            params.add(mUserAccount.getName());
        }else{
            params.add(mUserAccount.getObjectId());
        }
        ArrayList<Integer> operationTypes = new ArrayList<>();
        for(OperationType operationType : mOperationTypes){
            operationTypes.add(operationType.ordinal());
        }
        params.add(operationTypes);
        params.add(mStart);
        params.add(mLimit);
        return new ApiCall(apiId, RPC.CALL_GET_ACCOUNT_HISTORY_BY_OPERATIONS, params, RPC.VERSION, sequenceId);
    }
}
