package com.luminiasoft.bitshares;

import com.google.common.primitives.UnsignedLong;
import com.google.gson.*;
import com.luminiasoft.bitshares.interfaces.ByteSerializable;
import com.luminiasoft.bitshares.interfaces.JsonSerializable;

import java.lang.reflect.Type;

/**
 * Created by nelson on 11/7/16.
 */
public class AssetAmount implements ByteSerializable, JsonSerializable{
    /**
     * Constants used in the JSON serialization procedure.
     */
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_ASSET_ID = "asset_id";

    private UnsignedLong amount;
    private Asset asset;

    public AssetAmount(UnsignedLong amount, Asset asset){
        this.amount = amount;
        this.asset = asset;
    }

    public void setAmount(UnsignedLong amount){
        this.amount = amount;
    }

    public UnsignedLong getAmount(){
        return this.amount;
    }

    public Asset getAsset(){ return this.asset; }
    @Override
    public byte[] toBytes() {
        byte[] serialized = new byte[8 + 1];
        byte[] amountBytes = this.amount.bigIntegerValue().toByteArray();
        serialized[serialized.length - 1] = (byte) asset.instance;

        for(int i = 0; i < amountBytes.length; i++)
            serialized[i] = amountBytes[amountBytes.length - 1 - i];

        return serialized;
    }

    @Override
    public String toJsonString() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(AssetAmount.class, new AssetSerializer());
        return gsonBuilder.create().toJson(this);
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonAmount = new JsonObject();
        jsonAmount.addProperty(KEY_AMOUNT, amount);
        jsonAmount.addProperty(KEY_ASSET_ID, asset.getObjectId());
        return jsonAmount;
    }

    /**
     * Custom serializer used to translate this object into the JSON-formatted entry we need for a transaction.
     */
    public static class AssetSerializer implements JsonSerializer<AssetAmount> {

        @Override
        public JsonElement serialize(AssetAmount assetAmount, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject obj = new JsonObject();
            obj.addProperty(KEY_AMOUNT, assetAmount.amount);
            obj.addProperty(KEY_ASSET_ID, assetAmount.asset.getObjectId());
            return obj;
        }
    }

    public static class AssetDeserializer implements JsonDeserializer<AssetAmount> {

        @Override
        public AssetAmount deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            Long amount = json.getAsJsonObject().get(KEY_AMOUNT).getAsLong();
            String assetId = json.getAsJsonObject().get(KEY_ASSET_ID).getAsString();
            AssetAmount assetAmount = new AssetAmount(UnsignedLong.valueOf(amount), new Asset(assetId));
            return assetAmount;
        }
    }
}
