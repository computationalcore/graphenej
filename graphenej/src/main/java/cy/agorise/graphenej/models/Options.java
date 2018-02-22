package cy.agorise.graphenej.models;

/**
 * Class used to represent the 'options' object returned inside the response obtained after
 * querying for an object of type 'asset_bitasset_data' (2.4.x)
 */

public class Options {
    private long feed_lifetime_sec;
    private long minimum_feeds;
    private long force_settlement_delay_sec;
    private long force_settlement_offset_percent;
    private long maximum_force_settlement_volume;
    private String short_backing_asset;
}
