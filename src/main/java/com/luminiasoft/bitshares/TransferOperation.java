package com.luminiasoft.bitshares;

import com.google.common.primitives.Bytes;
import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Class used to encapsulate the TransferOperation operation related functionalities.
 * TODO: Add extensions support
 */
public class TransferOperation extends BaseOperation {
    public static final String KEY_FEE = "fee";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_EXTENSIONS = "extensions";
    public static final String KEY_FROM = "from";
    public static final String KEY_TO = "to";

    private AssetAmount fee;
    private AssetAmount amount;
    private UserAccount from;
    private UserAccount to;
    private Memo memo;
    private String[] extensions;

    public TransferOperation(UserAccount from, UserAccount to, AssetAmount transferAmount, AssetAmount fee){
        super(OperationType.transfer_operation);
        this.from = from;
        this.to = to;
        this.amount = transferAmount;
        this.fee = fee;
        this.memo = new Memo();
    }

    public TransferOperation(UserAccount from, UserAccount to, AssetAmount transferAmount){
        super(OperationType.transfer_operation);
        this.from = from;
        this.to = to;
        this.amount = transferAmount;
        this.memo = new Memo();
    }

    @Override
    public void setFee(AssetAmount newFee){
        this.fee = newFee;
    }

    public UserAccount getFrom(){
        return this.from;
    }

    public UserAccount getTo(){
        return this.to;
    }

    public AssetAmount getAmount(){
        return this.amount;
    }

    public AssetAmount getFee(){
        return this.fee;
    }

    @Override
    public byte[] toBytes() {
        byte[] feeBytes = fee.toBytes();
        byte[] fromBytes = from.toBytes();
        byte[] toBytes = to.toBytes();
        byte[] amountBytes = amount.toBytes();
        byte[] memoBytes = memo.toBytes();
        return Bytes.concat(feeBytes, fromBytes, toBytes, amountBytes, memoBytes);
    }

    @Override
    public String toJsonString() {
        //TODO: Evaluate using simple Gson class to return a simple string representation and drop the TransferSerializer class
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(TransferOperation.class, new TransferSerializer());
        return gsonBuilder.create().toJson(this);
    }

    @Override
    public JsonElement toJsonObject() {
        JsonArray array = new JsonArray();
        array.add(this.getId());
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(KEY_FEE, fee.toJsonObject());
        jsonObject.add(KEY_AMOUNT, amount.toJsonObject());
        jsonObject.add(KEY_EXTENSIONS, new JsonArray());
        jsonObject.addProperty(KEY_FROM, from.toJsonString());
        jsonObject.addProperty(KEY_TO, to.toJsonString());
        array.add(jsonObject);
        return array;
    }

    public static class TransferSerializer implements JsonSerializer<TransferOperation> {

        @Override
        public JsonElement serialize(TransferOperation transfer, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonArray arrayRep = new JsonArray();
            arrayRep.add(transfer.getId());
            arrayRep.add(transfer.toJsonObject());
            return arrayRep;
        }
    }

    /**
     * This deserializer will work on any transfer operation serialized in the 'array form' used a lot in
     * the Graphene Blockchain API.
     *
     * An example of this serialized form is the following:
     *
     *    [
     *       0,
     *       {
     *           "fee": {
     *               "amount": 264174,
     *               "asset_id": "1.3.0"
     *           },
     *           "from": "1.2.138632",
     *           "to": "1.2.129848",
     *           "amount": {
     *               "amount": 100,
     *               "asset_id": "1.3.0"
     *           },
     *           "extensions": []
     *       }
     *    ]
     *
     * It will convert this data into a nice TransferOperation object.
     */
    public static class TransferDeserializer implements JsonDeserializer<TransferOperation> {

        @Override
        public TransferOperation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if(json.isJsonArray()){
                // This block is used just to check if we are in the first step of the deserialization
                // when we are dealing with an array.
                JsonArray serializedTransfer = json.getAsJsonArray();
                if(serializedTransfer.get(0).getAsInt() != OperationType.transfer_operation.ordinal()){
                    // If the operation type does not correspond to a transfer operation, we return null
                    return null;
                }else{
                    // Calling itself recursively, this is only done once, so there will be no problems.
                    return context.deserialize(serializedTransfer.get(1), TransferOperation.class);
                }
            }else{
                // This block is called in the second recursion and takes care of deserializing the
                // transfer data itself.
                JsonObject jsonObject = json.getAsJsonObject();

                // Deserializing AssetAmount objects
                AssetAmount amount = context.deserialize(jsonObject.get("amount"), AssetAmount.class);
                AssetAmount fee = context.deserialize(jsonObject.get("fee"), AssetAmount.class);

                // Deserializing UserAccount objects
                UserAccount from = new UserAccount(jsonObject.get("from").getAsString());
                UserAccount to = new UserAccount(jsonObject.get("to").getAsString());
                TransferOperation transfer = new TransferOperation(from, to, amount, fee);
                return transfer;
            }
        }
    }
}
