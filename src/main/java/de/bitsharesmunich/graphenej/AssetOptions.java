package de.bitsharesmunich.graphenej;

import com.google.common.primitives.UnsignedLong;

/**
 * Created by nelson on 12/13/16.
 */
public class AssetOptions {
    private UnsignedLong max_supply;
    private long market_fee_percent;
    private UnsignedLong max_market_fee;
    private long issuer_permissions;
    private int flags;
    private Price core_exchange_rate;
    //TODO: Implement whitelist_authorities, blacklist_authorities, whitelist_markets, blacklist_markets and extensions
    private String description;

    public UnsignedLong getMaxSupply() {
        return max_supply;
    }

    public void setMaxSupply(UnsignedLong max_supply) {
        this.max_supply = max_supply;
    }

    public long getMarketFeePercent() {
        return market_fee_percent;
    }

    public void setMarketFeePercent(long market_fee_percent) {
        this.market_fee_percent = market_fee_percent;
    }

    public UnsignedLong getMax_market_fee() {
        return max_market_fee;
    }

    public void setMaxMarketFee(UnsignedLong max_market_fee) {
        this.max_market_fee = max_market_fee;
    }

    public long getIssuerPermissions() {
        return issuer_permissions;
    }

    public void setIssuerPermissions(long issuer_permissions) {
        this.issuer_permissions = issuer_permissions;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public Price getCoreExchangeRate() {
        return core_exchange_rate;
    }

    public void setCoreExchangeRate(Price core_exchange_rate) {
        this.core_exchange_rate = core_exchange_rate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
