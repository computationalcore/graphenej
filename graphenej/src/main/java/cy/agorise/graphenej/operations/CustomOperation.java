package cy.agorise.graphenej.operations;

import com.google.common.primitives.Bytes;
import com.google.gson.*;
import cy.agorise.graphenej.*;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

public class CustomOperation extends BaseOperation {
    private static final String KEY_FEE = "fee";
    private static final String KEY_PAYER = "payer";
    private static final String KEY_REQUIRED_AUTHS = "required_auths";
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
        jsonObject.add(KEY_REQUIRED_AUTHS, requiredAuthArray);
        jsonObject.addProperty(KEY_ID, operationId);
        jsonObject.addProperty(KEY_DATA, Util.bytesToHex(Util.hexlify(data)));

        array.add(jsonObject);

        return array;
    }

    /**
     * Deserializer used to convert the JSON-formatted representation of a custom_operation
     * into its java object version.
     *
     * The following is an example of the serialized form of this operation:
     *
     *    [
     *        35,
     *        {
     *            "fee": {
     *                "amount": 100000,
     *                "asset_id": "1.3.0"
     *            },
     *            "payer": "1.2.20",
     *            "required_auths": [
     *                "1.2.20"
     *            ],
     *            "id": 61166,
     *            "data": "736f6d652064617461"
     *        }
     *    ]
     */
    public static class CustomOperationDeserializer implements JsonDeserializer<CustomOperation> {

        @Override
        public CustomOperation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonArray()){
                // This block is used just to check if we are in the first step of the deserialization
                // when we are dealing with an array.
                JsonArray serializedCustomOperation = json.getAsJsonArray();
                if (serializedCustomOperation.get(0).getAsInt() != OperationType.CUSTOM_OPERATION.ordinal()){
                    // If the operation type does not correspond to a custom operation, we return null
                    return null;
                } else {
                    // Calling itself recursively, this is only done once, so there will be no problems.
                    return context.deserialize(serializedCustomOperation.get(1), CustomOperation.class);
                }
            }else{
                // This block is called in the second recursion and takes care of deserializing the
                // limit order data itself.
                JsonObject jsonObject = json.getAsJsonObject();

                AssetAmount fee = context.deserialize(jsonObject.get(KEY_FEE), AssetAmount.class);
                String payerId = jsonObject.get(KEY_PAYER) .getAsString();
                UserAccount payer = new UserAccount(payerId);
                List<UserAccount> requiredAuths = new LinkedList<>();
                JsonElement requiredAuthsElement = jsonObject.get(KEY_REQUIRED_AUTHS);
                if ((requiredAuthsElement != null) && (requiredAuthsElement.isJsonArray())) {
                    JsonArray requiredAuthsArray = requiredAuthsElement.getAsJsonArray();
                    for (JsonElement jsonElement : requiredAuthsArray) {
                        String userAccountId = jsonElement.getAsString();
                        requiredAuths.add(new UserAccount(userAccountId));
                    }
                }
                int operationId = jsonObject.get(KEY_ID).getAsInt();
                String data = new String(Util.hexToBytes(jsonObject.get(KEY_DATA).getAsString()));

                return new CustomOperation(fee, payer, operationId, requiredAuths, data);
            }
        }
    }
}
