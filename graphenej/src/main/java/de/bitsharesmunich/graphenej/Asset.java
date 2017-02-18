package de.bitsharesmunich.graphenej;

import com.google.common.primitives.UnsignedLong;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by nelson on 11/9/16.
 */
public class Asset extends GrapheneObject {
    public final static String TAG = "Asset";

    public static final String KEY_ID = "id";
    public static final String KEY_SYMBOL = "symbol";
    public static final String KEY_PRECISION = "precision";
    public static final String KEY_ISSUER = "issuer";
    public static final String KEY_OPTIONS = "options";
    public static final String KEY_MAX_SUPPLY = "max_supply";
    public static final String KEY_MARKET_FEE_PERCENT = "market_fee_percent";
    public static final String KEY_MARKET_FEE = "max_market_fee";
    public static final String KEY_ISSUER_PERMISSIONS = "issuer_permissions";
    public static final String KEY_FLAGS = "flags";
    public static final String KEY_CORE_EXCHANGE_RATE = "core_exchange_rate";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_DYNAMIC_ASSET_DATA_ID = "dynamic_asset_data_id";
    public static final String KEY_BITASSET_DATA_ID = "bitasset_data_id";

    /**
     * Enum type used to represent the possible types an asset can be classified into.
     */
    public enum AssetType {
        CORE_ASSET,
        UIA,
        SMART_COIN,
        PREDICTION_MARKET
    }

    private String symbol;
    private int precision = -1;
    private String issuer;
    private String description;
    private String dynamic_asset_data_id;
    private AssetOptions options;
    private String bitasset_data_id;
    private AssetType mAssetType;

    /**
     * Simple constructor
     * @param id
     */
    public Asset(String id) {
        super(id);
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

    /**
     * Constructor
     * @param id: The graphene object id.
     * @param symbol: The asset symbol.
     * @param precision: The asset precision.
     * @param issuer: Graphene object id of the issuer.
     */
    public Asset(String id, String symbol, int precision, String issuer){
        super(id);
        this.symbol = symbol;
        this.precision = precision;
        this.issuer = issuer;
    }

    public String getSymbol(){
        return this.symbol;
    }

    public void setSymbol(String symbol){
        this.symbol = symbol;
    }

    public void setPrecision(int precision){
        this.precision = precision;
    }

    public int getPrecision(){
        return this.precision;
    }

    public void setIssuer(String issuer){ this.issuer = issuer; }

    public String getIssuer() { return this.issuer; }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setAssetOptions(AssetOptions options){
        this.options = options;
    }

    public AssetOptions getAssetOptions(){
        return this.options;
    }

    public String getBitassetId(){
        return this.bitasset_data_id;
    }

    public void setBitassetDataId(String id){
        this.bitasset_data_id = id;
    }

    public AssetType getAssetType() {
        return mAssetType;
    }

    public void setAssetType(AssetType mAssetType) {
        this.mAssetType = mAssetType;
    }

    @Override
    public int hashCode() {
        return this.getObjectId() == null ? 0 : this.getObjectId().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof Asset){
            return this.getObjectId().equals(((Asset)other).getObjectId());
        }else{
            return false;
        }
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
            String issuer = object.get(KEY_ISSUER).getAsString();
            JsonObject optionsJson = object.get(KEY_OPTIONS).getAsJsonObject();
            JsonElement bitassetDataId = object.get(KEY_BITASSET_DATA_ID);

            // Deserializing asset options
            AssetOptions options = new AssetOptions();
            options.setMaxSupply(UnsignedLong.valueOf(optionsJson.get(KEY_MAX_SUPPLY).getAsString()));
            options.setMarketFeePercent(optionsJson.get(KEY_MARKET_FEE_PERCENT).getAsInt());
            options.setMaxMarketFee(UnsignedLong.valueOf(optionsJson.get(KEY_MARKET_FEE).getAsString()));
            options.setIssuerPermissions(optionsJson.get(KEY_ISSUER_PERMISSIONS).getAsLong());
            options.setFlags(optionsJson.get(KEY_FLAGS).getAsInt());
            if(optionsJson.has(KEY_DESCRIPTION))
                options.setDescription(optionsJson.get(KEY_DESCRIPTION).getAsString());
            //TODO: Deserialize core_exchange_rate field

            Asset asset = new Asset(id, symbol, precision, issuer);
            asset.setAssetOptions(options);
            if(bitassetDataId != null){
                asset.setBitassetDataId(bitassetDataId.getAsString());
            }
            return asset;
        }
    }
}