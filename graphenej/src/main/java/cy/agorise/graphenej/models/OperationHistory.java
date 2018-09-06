package cy.agorise.graphenej.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.Serializable;
import java.lang.reflect.Type;

import cy.agorise.graphenej.BaseOperation;
import cy.agorise.graphenej.GrapheneObject;


/**
 * This class offers support to deserialization of transfer operations received by the API
 * method get_relative_account_history.
 *
 * More operations types might be listed in the response of that method, but by using this class
 * those will be filtered out of the parsed result.
 */
public class OperationHistory extends GrapheneObject implements Serializable {
    public static final String KEY_OP = "op";
    public static final String KEY_BLOCK_NUM = "block_num";
    public static final String KEY_TRX_IN_BLOCK = "trx_in_block";
    public static final String KEY_OP_IN_TRX = "op_in_trx";
    public static final String KEY_VIRTUAL_OP = "virtual_op";

    private BaseOperation op;
    public Object[] result;
    private long block_num;
    private long trx_in_block;
    private long op_in_trx;
    private long virtual_op;

    public OperationHistory(String id) {
        super(id);
    }

    public BaseOperation getOperation() {
        return op;
    }

    public void setOperation(BaseOperation op) {
        this.op = op;
    }

    public long getBlockNum() {
        return block_num;
    }

    public void setBlockNum(long block_num) {
        this.block_num = block_num;
    }

    public long getTransactionsInBlock() {
        return trx_in_block;
    }

    public void setTransactionsInBlock(long trx_in_block) {
        this.trx_in_block = trx_in_block;
    }

    public long getOperationsInTrx() {
        return op_in_trx;
    }

    public void setOperationsInTrx(long op_in_trx) {
        this.op_in_trx = op_in_trx;
    }

    public long getVirtualOp() {
        return virtual_op;
    }

    public void setVirtualOp(long virtual_op) {
        this.virtual_op = virtual_op;
    }

    /**
     * Deserializer used to transform a an operation history object from its serialized form to an
     * OperationHistory instance.
     *
     * The serialized form of this object is the following:
     *
     * {
            "id": "1.11.178205535",
            "op": [
                14,
                {
                    "fee": {
                        "amount": 10425,
                        "asset_id": "1.3.0"
                    },
                     "issuer": "1.2.374566",
                     "asset_to_issue": {
                         "amount": 8387660,
                         "asset_id": "1.3.3271"
                    },
                     "issue_to_account": "1.2.797835",
                     "extensions": []
                 }
             ],
             "result": [
                 0,
                 {}
             ],
             "block_num": 26473240,
             "trx_in_block": 11,
             "op_in_trx": 0,
             "virtual_op": 660
     }
     * //TODO: Expand this deserializer for operation history objects that have an operation other than the transfer operation
     */
    public static class OperationHistoryDeserializer implements JsonDeserializer<OperationHistory> {

        @Override
        public OperationHistory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String id = jsonObject.get(KEY_ID).getAsString();
            long blockNum = jsonObject.get(KEY_BLOCK_NUM).getAsLong();
            long trxInBlock = jsonObject.get(KEY_TRX_IN_BLOCK).getAsLong();
            long opInTrx = jsonObject.get(KEY_OP_IN_TRX).getAsLong();
            BaseOperation operation = context.deserialize(jsonObject.get(KEY_OP), BaseOperation.class);
            long virtualOp = jsonObject.get(KEY_VIRTUAL_OP).getAsLong();
            OperationHistory operationHistory = new OperationHistory(id);
            operationHistory.setBlockNum(blockNum);
            operationHistory.setTransactionsInBlock(trxInBlock);
            operationHistory.setOperationsInTrx(opInTrx);
            operationHistory.setOperation(operation);
            operationHistory.setVirtualOp(virtualOp);
            return operationHistory;
        }
    }
}