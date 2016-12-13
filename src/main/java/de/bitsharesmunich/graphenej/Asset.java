package de.bitsharesmunich.graphenej;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by nelson on 11/9/16.
 */
public class Asset extends GrapheneObject {
    public static final String KEY_ID = "id";
    public static final String KEY_SYMBOL = "symbol";
    public static final String KEY_PRECISION = "precision";
    public static final String KEY_ISSUER = "issuer";
    public static final String KEY_DYNAMIC_ASSET_DATA_ID = "dynamic_asset_data_id";

    private String id;
    private String symbol;
    private int precision;
    private String issuer;
    private String dynamic_asset_data_id;
    private AssetOptions options;

    /**
     * Simple constructor
     * @param id
     */
    public Asset(String id) {
        super(id);
        this.id = id;
    }

    /**
     * Constructor
     * @param id: The graphene object id.
     * @param symbol: The asset symbol.
     * @param precision: The asset precision.
     */
    public Asset(String id, String symbol, int precision){
        super(id);
        this.symbol = symbol;
        this.precision = precision;
    }

    public String getSymbol(){
        return this.symbol;
    }

    public int getPrecision(){
        return this.precision;
    }

    /**
     * Custom deserializer used to instantiate a simple version of the Asset class from the response of the
     * 'lookup_asset_symbols' API call.
     */
    public static class AssetDeserializer implements JsonDeserializer<Asset> {

        @Override
        public Asset deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            String id = object.get(KEY_ID).getAsString();
            String symbol = object.get(KEY_SYMBOL).getAsString();
            int precision = object.get(KEY_PRECISION).getAsInt();
            return new Asset(id, symbol, precision);
        }
    }
}
