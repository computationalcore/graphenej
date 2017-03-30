package de.bitsharesmunich.graphenej.operations;

import com.google.common.primitives.Bytes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import de.bitsharesmunich.graphenej.AssetAmount;
import de.bitsharesmunich.graphenej.BaseOperation;
import de.bitsharesmunich.graphenej.OperationType;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.Util;

/**
 * Operation used to denote the creation of a limit order on the blockchain.
 *
 * The blockchain will atempt to sell amount_to_sell.asset_id for as much min_to_receive.asset_id as possible.
 * The fee will be paid by the seller's account. Market fees will apply as specified by the issuer of both the
 * selling asset and the receiving asset as a percentage of the amount exchanged.
 *
 * If either the selling asset or the receiving asset is white list restricted, the order will only be created
 * if the seller is on the white list of the restricted asset type.
 *
 * Market orders are matched in the order they are included in the block chain.
 */
public class LimitOrderCreateOperation extends BaseOperation {
    // Number of bytes used for the expiration field.
    private final int EXPIRATION_BYTE_LENGTH = 4;

    // Constants used in the JSON representation
    public static final String KEY_SELLER = "seller";
    public static final String KEY_AMOUNT_TO_SELL = "amount_to_sell";
    public static final String KEY_MIN_TO_RECEIVE = "min_to_receive";
    public static final String KEY_EXPIRATION = "expiration";
    public static final String KEY_FILL_OR_KILL = "fill_or_kill";

    // Inner fields of a limit order
    private AssetAmount fee;
    private UserAccount seller;
    private AssetAmount amountToSell;
    private AssetAmount minToReceive;
    private int expiration;
    private boolean fillOrKill;

    /**
     * @param seller: Id of the seller
     * @param toSell: Id of the asset to sell
     * @param minToReceive: The minimum amount of the asset to receive
     * @param expiration: Expiration in seconds
     * @param fillOrKill: If this flag is set the entire order must be filled or the operation is rejected.
     */
    public LimitOrderCreateOperation(UserAccount seller, AssetAmount toSell, AssetAmount minToReceive, int expiration, boolean fillOrKill){
        super(OperationType.LIMIT_ORDER_CREATE_OPERATION);
        this.seller = seller;
        this.amountToSell = toSell;
        this.minToReceive = minToReceive;
        this.expiration = expiration;
        this.fillOrKill = fillOrKill;
    }

    @Override
    public String toJsonString() {
        return null;
    }

    @Override
    public JsonElement toJsonObject() {
        JsonArray array = (JsonArray) super.toJsonObject();
        JsonObject jsonObject = new JsonObject();
        if(fee != null)
            jsonObject.add(KEY_FEE, fee.toJsonObject());
        jsonObject.addProperty(KEY_SELLER, seller.toJsonString());
        jsonObject.add(KEY_AMOUNT_TO_SELL, amountToSell.toJsonObject());
        jsonObject.add(KEY_MIN_TO_RECEIVE, minToReceive.toJsonObject());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Util.TIME_DATE_FORMAT);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        jsonObject.addProperty(KEY_EXPIRATION, simpleDateFormat.format(new Date(((long) expiration) * 1000)));
        jsonObject.addProperty(KEY_FILL_OR_KILL, this.fillOrKill ? "true" : "false");
        jsonObject.add(KEY_EXTENSIONS, new JsonArray());
        array.add(jsonObject);
        return array;
    }

    @Override
    public void setFee(AssetAmount assetAmount) {
        this.fee = assetAmount;
    }

    public AssetAmount getFee(){ return this.fee; }

    public UserAccount getSeller() {
        return seller;
    }

    public void setSeller(UserAccount seller) {
        this.seller = seller;
    }

    public AssetAmount getAmountToSell() {
        return amountToSell;
    }

    public void setAmountToSell(AssetAmount amountToSell) {
        this.amountToSell = amountToSell;
    }

    public AssetAmount getMinToReceive() {
        return minToReceive;
    }

    public void setMinToReceive(AssetAmount minToReceive) {
        this.minToReceive = minToReceive;
    }

    public int getExpiration() {
        return expiration;
    }

    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    public boolean isFillOrKill() {
        return fillOrKill;
    }

    public void setFillOrKill(boolean fillOrKill) {
        this.fillOrKill = fillOrKill;
    }

    @Override
    public byte[] toBytes() {
        byte[] feeBytes = this.fee.toBytes();
        byte[] sellerBytes = this.seller.toBytes();
        byte[] amountBytes = this.amountToSell.toBytes();
        byte[] minAmountBytes = this.minToReceive.toBytes();

        ByteBuffer buffer = ByteBuffer.allocate(EXPIRATION_BYTE_LENGTH);
        buffer.putInt(this.expiration);
        byte[] expirationBytes = Util.revertBytes(buffer.array());

        byte[] fillOrKill = this.fillOrKill ? new byte[]{ 0x1 } : new byte[]{ 0x0 };
        byte[] extensions = this.extensions.toBytes();

        return Bytes.concat(feeBytes, sellerBytes, amountBytes, minAmountBytes, expirationBytes, fillOrKill, extensions);
    }
}
