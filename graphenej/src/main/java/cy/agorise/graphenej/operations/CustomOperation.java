package cy.agorise.graphenej.operations;

import com.google.common.primitives.Bytes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cy.agorise.graphenej.*;

import java.util.LinkedList;
import java.util.List;

public class CustomOperation extends BaseOperation {
    private static final String KEY_FEE = "fee";
    private static final String KEY_PAYER = "payer";
    private static final String KEY_REQUIRED_AUTH = "required_auths";
    private static final String KEY_ID = "id";
    private static final String KEY_DATA = "data";

    private AssetAmount fee;
    private UserAccount payer;
    private List<UserAccount> requiredAuths;
    private int operationId;
    private String data;

    public CustomOperation(AssetAmount fee, UserAccount payer, int operationId, List<UserAccount> requiredAuths, String data) {
        super(OperationType.CUSTOM_OPERATION);
        this.fee = fee;
        this.payer = payer;
        this.operationId = operationId;
        this.requiredAuths = new LinkedList<>();
        if (requiredAuths != null) {
            this.requiredAuths.addAll(requiredAuths);
        }
        this.data = data;
    }

    public AssetAmount getFee() {
        return fee;
    }

    @Override
    public void setFee(AssetAmount fee) {
        this.fee = fee;
    }

    public UserAccount getPayer() {
        return payer;
    }

    public void setPayer(UserAccount payer) {
        this.payer = payer;
    }

    public List<UserAccount> getRequiredAuths() {
        return requiredAuths;
    }

    public void setRequiredAuths(List<UserAccount> requiredAuths) {
        this.requiredAuths = requiredAuths;
    }

    public int getOperationId() {
        return operationId;
    }

    public void setOperationId(int operationId) {
        this.operationId = operationId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public byte[] toBytes() {
        byte[] feeBytes = fee.toBytes();
        byte[] payerBytes = payer.toBytes();
        List<Byte> requiredAuthsSerialized = new LinkedList<>();
        if (this.requiredAuths != null) {
            for (UserAccount userAccount : this.requiredAuths) {
                requiredAuthsSerialized.addAll(Bytes.asList(userAccount.toBytes()));
            }
        }
        byte[] requiredAuthsBytes = Bytes.toArray(requiredAuthsSerialized);
        byte[] requiredAuthsLength = {(byte)this.requiredAuths.size()};
        byte[] operationIdBytes = Util.revertShort((short)operationId);
        byte[] dataLength = Util.serializeLongToBytes(data.length());
        byte[] dataBytes = Util.hexlify(data);

        return Bytes.concat(feeBytes, payerBytes, requiredAuthsLength, requiredAuthsBytes, operationIdBytes, dataLength, dataBytes);
    }

    @Override
    public String toJsonString() {
        return toJsonObject().toString();
    }

    @Override
    public JsonElement toJsonObject() {
        JsonArray array = new JsonArray();
        array.add(this.getId());

        JsonObject jsonObject = new JsonObject();
        jsonObject.add(KEY_FEE, fee.toJsonObject());
        jsonObject.addProperty(KEY_PAYER, payer.getObjectId());
        JsonArray requiredAuthArray = new JsonArray();
        if (requiredAuths != null) {
            for (UserAccount userAccount : requiredAuths) {
                requiredAuthArray.add(userAccount.getObjectId());
            }
        }
        jsonObject.add(KEY_REQUIRED_AUTH, requiredAuthArray);
        jsonObject.addProperty(KEY_ID, operationId);
        jsonObject.addProperty(KEY_DATA, Util.bytesToHex(Util.hexlify(data)));

        array.add(jsonObject);

        return array;
    }
}
