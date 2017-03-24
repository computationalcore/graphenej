package de.bitsharesmunich.graphenej.operations;

import com.google.common.primitives.Bytes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.bitsharesmunich.graphenej.*;

/**
 * Created by nelson on 3/21/17.
 */
public class LimitOrderCancelOperation extends BaseOperation {

    // Constants used in the JSON representation
    public static final String KEY_FEE_PAYING_ACCOUNT = "fee_paying_account";
    public static final String KEY_ORDER_ID = "order";


    public LimitOrderCancelOperation(LimitOrder order, UserAccount feePayingAccount) {
        super(OperationType.LIMIT_ORDER_CANCEL_OPERATION);
        this.order = order;
        this.feePayingAccount = feePayingAccount;
    }

    // Inner fields of a limit order cancel operation
    private AssetAmount fee;
    private UserAccount feePayingAccount;
    private LimitOrder order;

    @Override
    public String toJsonString() {
        return null;
    }

    @Override
    public JsonElement toJsonObject() {
        JsonArray array = (JsonArray) super.toJsonObject();
        JsonObject jsonObject = new JsonObject();
        if(fee != null)
            jsonObject.add(KEY_FEE, fee.toJsonObject());
        jsonObject.addProperty(KEY_FEE_PAYING_ACCOUNT, feePayingAccount.getObjectId());
        jsonObject.addProperty(KEY_ORDER_ID, order.getObjectId());
        jsonObject.add(KEY_EXTENSIONS, new JsonArray());
        array.add(jsonObject);
        return array;
    }

    @Override
    public void setFee(AssetAmount assetAmount) {
        this.fee = assetAmount;
    }

    @Override
    public byte[] toBytes() {
        byte[] feeBytes = this.fee.toBytes();
        byte[] feePayingAccountBytes = this.feePayingAccount.toBytes();
        byte[] orderIdBytes = this.order.toBytes();
        byte[] extensions = this.extensions.toBytes();
        return Bytes.concat(feeBytes, feePayingAccountBytes, orderIdBytes, extensions);
    }
}
