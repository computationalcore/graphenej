package de.bitsharesmunich.graphenej.models;

import de.bitsharesmunich.graphenej.Price;

/**
 * Created by nelson on 1/9/17.
 */
public class AssetFeed {
    public Price settlement_price;
    public long maintenance_collateral_ratio;
    public long maximum_short_squeeze_ratio;
    public Price core_exchange_rate;
}
