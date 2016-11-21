package com.luminiasoft.bitshares;

import com.google.common.primitives.Bytes;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Class used to encapsulate the Transfer operation related functionalities.
 */
public class Transfer extends BaseOperation {
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

    public Transfer(UserAccount from, UserAccount to, AssetAmount transferAmount, AssetAmount fee){
        super(OperationType.transfer_operation);
        this.from = from;
        this.to = to;
        this.amount = transferAmount;
        this.fee = fee;
        this.memo = new Memo();
    }

    public Transfer(UserAccount from, UserAccount to, AssetAmount transferAmount){
        super(OperationType.transfer_operation);
        this.from = from;
        this.to = to;
        this.amount = transferAmount;
        this.memo = new Memo();
    }

    public void setFee(AssetAmount newFee){
        this.fee = newFee;
    }

    @Override
    public byte getId() {
        return (byte) this.type.ordinal();
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
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Transfer.class, new TransferSerializer());
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

    class TransferSerializer implements JsonSerializer<Transfer> {

        @Override
        public JsonElement serialize(Transfer transfer, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonArray arrayRep = new JsonArray();
            arrayRep.add(transfer.getId());
            arrayRep.add(toJsonObject());
            return arrayRep;
        }
    }
}
