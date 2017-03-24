package de.bitsharesmunich.graphenej;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

import de.bitsharesmunich.graphenej.interfaces.ByteSerializable;

/**
 *
 * @author henry
 */
public class LimitOrder extends GrapheneObject implements ByteSerializable {

    public static final String KEY_EXPIRATION = "expiration";
    public static final String KEY_SELLER = "seller";
    public static final String KEY_FOR_SALE = "for_sale";
    public static final String KEY_DEFERRED_FEE = "deferred_fee";
    public static final String KEY_PRICE = "sell_price";

    private String expiration;
    private UserAccount seller;
    private long forSale;
    private long deferredFee;
    private Price sellPrice;

    public LimitOrder(String id) {
        super(id);
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public UserAccount getSeller() {
        return seller;
    }

    public void setSeller(UserAccount seller) {
        this.seller = seller;
    }

    public long getForSale() {
        return forSale;
    }

    public void setForSale(long forSale) {
        this.forSale = forSale;
    }

    public long getDeferredFee() {
        return deferredFee;
    }

    public void setDeferredFee(long deferredFee) {
        this.deferredFee = deferredFee;
    }

    public Price getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(Price sellPrice) {
        this.sellPrice = sellPrice;
    }

    @Override
    public byte[] toBytes() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutput out = new DataOutputStream(byteArrayOutputStream);
        byte[] serialized = null;
        try {
            Varint.writeUnsignedVarLong(this.instance, out);
            serialized = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serialized;
    }

    /**
     * Custom deserializer for the LimitOrder class, used to deserialize a json-formatted string in
     * the following format:
     *
     *     {
     *        "id": "1.7.2389233",
     *        "expiration": "2017-04-21T15:40:04",
     *        "seller": "1.2.114363",
     *        "forSale": "10564959415",
     *        "sell_price": {
     *            "base": {
     *                "amount": "10565237932",
     *                "asset_id": "1.3.0"
     *                },
     *            "quote": {
     *                "amount": 5803878,
     *                "asset_id": "1.3.121"
     *            }
     *        },
     *        "deferredFee": 0
     *    }
     */
    public static class LimitOrderDeserializer implements JsonDeserializer<LimitOrder> {

        @Override
        public LimitOrder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            String id = object.get(KEY_ID).getAsString();
            String expiration = object.get(KEY_EXPIRATION).getAsString();
            UserAccount seller = context.deserialize(object.get(KEY_SELLER), UserAccount.class);
            String forSale = object.get(KEY_FOR_SALE).getAsString();
            Price price = context.deserialize(object.get(KEY_PRICE), Price.class);
            long deferredFee = object.get(KEY_DEFERRED_FEE).getAsLong();

            LimitOrder limitOrder = new LimitOrder(id);
            limitOrder.setExpiration(expiration);
            limitOrder.setSeller(seller);
            limitOrder.setForSale(Long.parseLong(forSale));
            limitOrder.setSellPrice(price);
            limitOrder.setDeferredFee(deferredFee);
            return limitOrder;
        }
    }
}
