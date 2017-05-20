package de.bitsharesmunich.graphenej;

import com.google.common.math.DoubleMath;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.UnsignedLong;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.RoundingMode;

import de.bitsharesmunich.graphenej.errors.IncompatibleOperation;
import de.bitsharesmunich.graphenej.interfaces.ByteSerializable;
import de.bitsharesmunich.graphenej.interfaces.JsonSerializable;

/**
 * Created by nelson on 11/7/16.
 */
public class AssetAmount implements ByteSerializable, JsonSerializable {
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

    /**
     * Adds two asset amounts. They must refer to the same Asset type.
     * @param other: The other AssetAmount to add to this.
     * @return: A new instance of the AssetAmount class with the combined amount.
     */
    public AssetAmount add(AssetAmount other){
        if(!this.getAsset().getObjectId().equals(other.getAsset().getObjectId())){
            throw new IncompatibleOperation("Cannot add two AssetAmount instances that refer to different assets");
        }
        UnsignedLong combined = this.amount.plus(other.getAmount());
        return new AssetAmount(combined, asset);
    }

    /**
     * Adds an aditional amount of base units to this AssetAmount.
     * @param additional: The amount to add.
     * @return: A new instance of the AssetAmount class with the added aditional.
     */
    public AssetAmount add(long additional){
        UnsignedLong combined = this.amount.plus(UnsignedLong.valueOf(additional));
        return new AssetAmount(combined, asset);
    }

    /**
     * Subtracts another instance of AssetAmount from this one. This method will always
     * return absolute values.
     * @param other: The other asset amount to subtract from this.
     * @return: The absolute value of the subtraction of the other minus this asset amount.
     */
    public AssetAmount subtract(AssetAmount other){
        if(!this.getAsset().getObjectId().equals(other.getAsset().getObjectId())){
            throw new IncompatibleOperation("Cannot subtract two AssetAmount instances that refer to different assets");
        }
        UnsignedLong result = null;
        if(this.amount.compareTo(other.getAmount()) < 0){
            result = other.getAmount().minus(this.amount);
        }else{
            result = this.amount.minus(other.getAmount());
        }
        return new AssetAmount(result, asset);
    }

    /**
     * Multiplies the current amount by a factor provided as the first parameter. The second parameter
     * specifies the rounding method to be used.
     * @param factor: The multiplying factor
     * @param roundingMode: The rounding mode as an instance of the {@link RoundingMode} class
     * @return The same AssetAmount instance, but with the changed amount value.
     */
    public AssetAmount multiplyBy(double factor, RoundingMode roundingMode){
        this.amount = UnsignedLong.valueOf(DoubleMath.roundToLong(this.amount.longValue() * factor, roundingMode));
        return this;
    }

    /**
     * Multiplies the current amount by a factor, using the {@link RoundingMode#HALF_DOWN} constant.
     * @param factor: The multiplying factor
     * @return The same AssetAmount instance, but with the changed amount value.
     */
    public AssetAmount multiplyBy(double factor){
        return this.multiplyBy(factor, RoundingMode.HALF_DOWN);
    }

    /**
     * Divides the current amount by a divisor provided as the first parameter. The second parameter
     * specifies the rounding method to be used.
     * @param divisor: The divisor
     * @return: The same AssetAMount instance, but with the divided amount value
     */
    public AssetAmount dividedBy(double divisor, RoundingMode roundingMode){
        this.amount = UnsignedLong.valueOf(DoubleMath.roundToLong(this.amount.longValue() / divisor, roundingMode));
        return this;
    }


    /**
     * Divides the current amount by a divisor provided as the first parameter, using
     * the {@link RoundingMode#HALF_DOWN} constant
     * @param divisor: The divisor
     * @return: The same AssetAMount instance, but with the divided amount value
     */
    public AssetAmount dividedBy(double divisor){
        return this.dividedBy(divisor, RoundingMode.HALF_DOWN);
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
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutput out = new DataOutputStream(byteArrayOutputStream);
        try {
            Varint.writeUnsignedVarLong(asset.instance, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Getting asset id
        byte[] assetId = byteArrayOutputStream.toByteArray();
        byte[] value = Util.revertLong(this.amount.longValue());

        // Concatenating and returning value + asset id
        return Bytes.concat(value, assetId);
    }

    @Override
    public String toJsonString() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(AssetAmount.class, new AssetAmountSerializer());
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
    public static class AssetAmountSerializer implements JsonSerializer<AssetAmount> {

        @Override
        public JsonElement serialize(AssetAmount assetAmount, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject obj = new JsonObject();
            obj.addProperty(KEY_AMOUNT, assetAmount.amount);
            obj.addProperty(KEY_ASSET_ID, assetAmount.asset.getObjectId());
            return obj;
        }
    }

    /**
     * Custom deserializer used for this class
     */
    public static class AssetAmountDeserializer implements JsonDeserializer<AssetAmount> {

        @Override
        public AssetAmount deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            Long amount = json.getAsJsonObject().get(KEY_AMOUNT).getAsLong();
            String assetId = json.getAsJsonObject().get(KEY_ASSET_ID).getAsString();
            AssetAmount assetAmount = new AssetAmount(UnsignedLong.valueOf(amount), new Asset(assetId));
            return assetAmount;
        }
    }
}
