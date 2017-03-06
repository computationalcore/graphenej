package de.bitsharesmunich.graphenej.operations;

import com.google.common.primitives.Bytes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.bitsharesmunich.graphenej.*;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Operation used to denote the creation of a limit order on the blockchain.
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
    private AssetAmount toSell;
    private AssetAmount minToReceive;
    private int expiration;
    private boolean fillOrKill;

    public LimitOrderCreateOperation(UserAccount seller, AssetAmount toSell, AssetAmount minToReceive, int expiration, boolean fillOrKill){
        super(OperationType.LIMIT_ORDER_CREATE_OPERATION);
        this.seller = seller;
        this.toSell = toSell;
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
        JsonArray array = new JsonArray();
        array.add(this.getId());
        JsonObject jsonObject = new JsonObject();
        if(fee != null)
            jsonObject.add(KEY_FEE, fee.toJsonObject());
        jsonObject.addProperty(KEY_SELLER, seller.toJsonString());
        jsonObject.add(KEY_AMOUNT_TO_SELL, toSell.toJsonObject());
        jsonObject.add(KEY_MIN_TO_RECEIVE, minToReceive.toJsonObject());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Util.TIME_DATE_FORMAT);
        jsonObject.addProperty(KEY_EXPIRATION, simpleDateFormat.format(new Date(expiration)));
        jsonObject.add(KEY_EXTENSIONS, new JsonArray());
        array.add(jsonObject);
        return array;
    }

    @Override
    public void setFee(AssetAmount assetAmount) {
        this.fee = assetAmount;
    }

    @Override
    public byte[] toBytes() {
        byte[] feeBytes = this.fee.toBytes();
        byte[] sellerBytes = this.seller.toBytes();
        byte[] amountBytes = this.toSell.toBytes();
        byte[] minAmountBytes = this.minToReceive.toBytes();

        ByteBuffer buffer = ByteBuffer.allocate(EXPIRATION_BYTE_LENGTH);
        buffer.putInt(this.expiration);
        byte[] expirationBytes = Util.revertBytes(buffer.array());

        byte[] fillOrKill = this.fillOrKill ? new byte[]{ 0x1 } : new byte[]{ 0x0 };
        return Bytes.concat(feeBytes, sellerBytes, amountBytes, minAmountBytes, expirationBytes, fillOrKill);
    }
}
