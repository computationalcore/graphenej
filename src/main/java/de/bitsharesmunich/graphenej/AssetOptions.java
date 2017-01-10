package de.bitsharesmunich.graphenej;

import com.google.common.primitives.UnsignedLong;

/**
 * Created by nelson on 12/13/16.
 */
public class AssetOptions {
    // TODO: Use these constants instead of using cryptic constants like 128 and 511
    public static final int CHARGE_MARKET_FEE = 0x01;
    public static final int WHITE_LIST = 0x02;
    public static final int OVERRIDE_AUTHORITY = 0x04;
    public static final int TRANSFER_RESTRICTED = 0x08;
    public static final int DISABLE_FORCE_SETTLE = 0x10;
    public static final int GLOBAL_SETTLE = 0x20;
    public static final int DISABLE_CONFIDENTIAL = 0x40;
    public static final int WITNESS_FED_ASSET = 0x80;
    public static final int COMITEE_FED_ASSET = 0x100;

    private UnsignedLong max_supply;
    private float market_fee_percent;
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

    public float getMarketFeePercent() {
        return market_fee_percent;
    }

    public void setMarketFeePercent(float market_fee_percent) {
        this.market_fee_percent = market_fee_percent;
    }

    public UnsignedLong getMaxMarketFee() {
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