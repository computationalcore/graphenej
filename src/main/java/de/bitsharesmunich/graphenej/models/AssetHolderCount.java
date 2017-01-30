package de.bitsharesmunich.graphenej.models;

import com.google.gson.*;
import de.bitsharesmunich.graphenej.Asset;

import java.lang.reflect.Type;

/**
 * Created by nelson on 1/25/17.
 */
public class AssetHolderCount {
    public static final String KEY_ASSET_ID = "asset_id";
    public static final String KEY_COUNT = "count";

    public Asset asset;
    public long count;

    public static class HoldersCountDeserializer implements JsonDeserializer<AssetHolderCount> {

        @Override
        public AssetHolderCount deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            AssetHolderCount holdersCount = new AssetHolderCount();
            holdersCount.asset = new Asset(jsonObject.get(KEY_ASSET_ID).getAsString());
            holdersCount.count = jsonObject.get(KEY_COUNT).getAsLong();
            return holdersCount;
        }
    }
}
