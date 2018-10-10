package cy.agorise.graphenej.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cy.agorise.graphenej.GrapheneObject;
import cy.agorise.graphenej.Price;
import cy.agorise.graphenej.Util;

/**
 * This is the representation of the response from the 'get_objects' call with
 * a 2.4.x id, which will retrieve a 'impl_asset_bitasset_data_type'.
 *
 */
public class BitAssetData extends GrapheneObject {
    public static final String KEY_FEEDS = "feeds";
    public static final String KEY_CURRENT_FEED = "current_feed";
    public static final String KEY_CURRENT_FEED_PUBLICATION_TIME = "current_feed_publication_time";
    public static final String KEY_OPERATIONS = "operations";
    public static final String KEY_FORCE_SETTLED_VOLUME = "force_settled_volume";
    public static final String KEY_IS_PREDICTION_MARKET = "is_prediction_market";
    public static final String KEY_SETTLEMENT_PRICE = "settlement_price";
    public static final String KEY_SETTLEMENT_FUND = "settlement_fund";

    private ReportedAssetFeed[] feeds;
    private AssetFeed current_feed;
    private Date current_feed_publication_time;
    private Options options;
    private long force_settled_volume;
    private boolean is_prediction_market;
    private Price settlement_price;
    private long settlement_fund;

    public BitAssetData(String id) {
        super(id);
    }

    public ReportedAssetFeed[] getFeeds() {
        return feeds;
    }

    public void setFeeds(ReportedAssetFeed[] feeds) {
        this.feeds = feeds;
    }

    public AssetFeed getCurrentFeed() {
        return current_feed;
    }

    public void setCurrentFeed(AssetFeed current_feed) {
        this.current_feed = current_feed;
    }

    public Date getCurrentFeedPublicationTime() {
        return current_feed_publication_time;
    }

    public void setCurrentFeedPublicationTime(String currentFeedPublicationTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Util.TIME_DATE_FORMAT);
        try {
            this.current_feed_publication_time = simpleDateFormat.parse(currentFeedPublicationTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public long getForceSettledVolume() {
        return force_settled_volume;
    }

    public void setForceSettledVolume(long force_settled_volume) {
        this.force_settled_volume = force_settled_volume;
    }

    public boolean isPredictionMarket() {
        return is_prediction_market;
    }

    public void setIsPredictionMarket(boolean is_prediction_market) {
        this.is_prediction_market = is_prediction_market;
    }

    public Price getSettlementPrice() {
        return settlement_price;
    }

    public void setSettlementPrice(Price settlementPrice) {
        this.settlement_price = settlementPrice;
    }

    public long getSettlementFund() {
        return settlement_fund;
    }

    public void setSettlementFund(long settlementFund) {
        this.settlement_fund = settlementFund;
    }

    /**
     * Custom deserializer used to instantiate the BitAssetData class from the response of the
     * 'get_objects' API call.
     */
    public static class BitAssetDataDeserializer implements JsonDeserializer<BitAssetData> {

        @Override
        public BitAssetData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String id = jsonObject.get(GrapheneObject.KEY_ID).getAsString();
            BitAssetData bitAssetData = new BitAssetData(id);
            ArrayList<ReportedAssetFeed> reportedAssetFeeds = new ArrayList<>();

            JsonArray jsonAssetFeeds = jsonObject.get(KEY_FEEDS).getAsJsonArray();
            for(JsonElement jsonFeed : jsonAssetFeeds){
                ReportedAssetFeed reportedAssetFeed = context.deserialize(jsonFeed, ReportedAssetFeed.class);
                reportedAssetFeeds.add(reportedAssetFeed);
            }

            // Deserializing attributes
            JsonElement jsonCurrentFeed = jsonObject.get(KEY_CURRENT_FEED).getAsJsonObject();
            AssetFeed assetFeed = context.deserialize(jsonCurrentFeed, AssetFeed.class);
            String publicationTime = jsonObject.get(KEY_CURRENT_FEED_PUBLICATION_TIME).getAsString();
            Options options = context.deserialize(jsonObject.get(KEY_OPERATIONS), Options.class);
            long forceSettledVolume = jsonObject.get(KEY_FORCE_SETTLED_VOLUME).getAsLong();
            boolean isPredictionMarket = jsonObject.get(KEY_IS_PREDICTION_MARKET).getAsBoolean();
            Price settlementPrice = context.deserialize(jsonObject.get(KEY_SETTLEMENT_PRICE), Price.class);
            long settlementFund = jsonObject.get(KEY_SETTLEMENT_FUND).getAsLong();

            // Setting attributes
            bitAssetData.setFeeds(reportedAssetFeeds.toArray(new ReportedAssetFeed[reportedAssetFeeds.size()]));
            bitAssetData.setCurrentFeed(assetFeed);
            bitAssetData.setCurrentFeedPublicationTime(publicationTime);
            bitAssetData.setOptions(options);
            bitAssetData.setForceSettledVolume(forceSettledVolume);
            bitAssetData.setIsPredictionMarket(isPredictionMarket);
            bitAssetData.setSettlementPrice(settlementPrice);
            bitAssetData.setSettlementFund(settlementFund);
            return bitAssetData;
        }
    }
}
