package cy.agorise.graphenej.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import cy.agorise.graphenej.Price;

/**
 * Price feed of a given asset.
 */
public class AssetFeed {
    public static final String KEY_SETTLEMENT_PRICE = "settlement_price";
    public static final String KEY_MAINTENANCE_COLLATERAL_RATIO = "maintenance_collateral_ratio";
    public static final String KEY_MAXIMUM_SHORT_SQUEEZE_RATIO = "maximum_short_squeeze_ratio";
    public static final String KEY_CORE_EXCHANGE_RATE = "core_exchange_rate";

    private Price settlement_price;
    private long maintenance_collateral_ratio;
    private long maximum_short_squeeze_ratio;
    private Price core_exchange_rate;

    public AssetFeed(Price settlementPrice, long maintenanceCollateralRatio, long maximumShortSqueezeRatio, Price coreExchangeRate){
        this.settlement_price = settlementPrice;
        this.maintenance_collateral_ratio = maintenanceCollateralRatio;
        this.maximum_short_squeeze_ratio = maximumShortSqueezeRatio;
        this.core_exchange_rate = coreExchangeRate;
    }

    public Price getSettlementPrice() {
        return settlement_price;
    }

    public void setSettlementPrice(Price settlement_price) {
        this.settlement_price = settlement_price;
    }

    public long getMaintenanceCollateralRatio() {
        return maintenance_collateral_ratio;
    }

    public void setMaintenanceCollateralRatio(long maintenance_collateral_ratio) {
        this.maintenance_collateral_ratio = maintenance_collateral_ratio;
    }

    public long getMaximumShortSqueezeRatio() {
        return maximum_short_squeeze_ratio;
    }

    public void setMaximumShortSqueezeRatio(long maximum_short_squeeze_ratio) {
        this.maximum_short_squeeze_ratio = maximum_short_squeeze_ratio;
    }

    public Price getCoreExchangeRate() {
        return core_exchange_rate;
    }

    public void setCoreExchangeRate(Price core_exchange_rate) {
        this.core_exchange_rate = core_exchange_rate;
    }

    public static class AssetFeedDeserializer implements JsonDeserializer<AssetFeed> {

        @Override
        public AssetFeed deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            Price settlementPrice = context.deserialize(jsonObject.get(KEY_SETTLEMENT_PRICE).getAsJsonObject(), Price.class);
            long collateralRatio = jsonObject.get(KEY_MAINTENANCE_COLLATERAL_RATIO).getAsLong();
            long maximumShortSqueezeRatio = jsonObject.get(KEY_MAXIMUM_SHORT_SQUEEZE_RATIO).getAsLong();
            Price coreExchangeRate = context.deserialize(jsonObject.get(KEY_CORE_EXCHANGE_RATE), Price.class);
            AssetFeed assetFeed = new AssetFeed(settlementPrice, collateralRatio, maximumShortSqueezeRatio, coreExchangeRate);
            return assetFeed;
        }
    }

}
