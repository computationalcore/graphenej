package cy.agorise.graphenej.models;

import cy.agorise.graphenej.GrapheneObject;
import cy.agorise.graphenej.Price;

/**
 * This is the representation of the response from the 'get_objects' call with
 * a 2.4.x id, which will retrieve a 'impl_asset_bitasset_data_type'.
 *
 */
public class BitAssetData extends GrapheneObject {
    public static final String KEY_CURRENT_FEED = "current_feed";

    public ReportedAssetFeed[] feeds;
    public AssetFeed current_feed;
    public String current_feed_publication_time;
    public Object options;
    public long force_settled_volume;
    public boolean is_prediction_market;
    public Price settlement_price;
    public long settlement_fund;

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

    public String getCurrentFeedPublicationTime() {
        return current_feed_publication_time;
    }

    public void setCurrentFeedPublicationTime(String current_feed_publication_time) {
        this.current_feed_publication_time = current_feed_publication_time;
    }

    public Object getOptions() {
        return options;
    }

    public void setOptions(Object options) {
        this.options = options;
    }

    public long getForceSettledVolume() {
        return force_settled_volume;
    }

    public void setForceSettledVolume(long force_settled_volume) {
        this.force_settled_volume = force_settled_volume;
    }

    public boolean isIsPredictionMarket() {
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
}
