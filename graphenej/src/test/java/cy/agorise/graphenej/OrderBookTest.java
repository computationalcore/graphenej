package cy.agorise.graphenej;

import com.google.common.primitives.UnsignedLong;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;

import cy.agorise.graphenej.models.WitnessResponse;

/**
 * Testing the {@link OrderBook} class
 */

public class OrderBookTest {

    @Test
    public void testRequiredBase(){
        String serializedOrderBook = "{\"id\": 1,\"result\": [{\"id\": \"1.7.37284933\",\"expiration\": \"2018-11-17T00:03:49\",\"seller\": \"1.2.132823\",\"for_sale\": 10,\"sell_price\": {\"base\": {\"amount\": 1,\"asset_id\": \"1.3.121\"},\"quote\": {\"amount\": 10,\"asset_id\": \"1.3.0\"}},\"deferred_fee\": 0},{\"id\": \"1.7.37284933\",\"expiration\": \"2018-11-17T00:03:49\",\"seller\": \"1.2.132823\",\"for_sale\": 20,\"sell_price\": {\"base\": {\"amount\": 1,\"asset_id\": \"1.3.121\"},\"quote\": {\"amount\": 20,\"asset_id\": \"1.3.0\"}},\"deferred_fee\": 0}]}";
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer());
        builder.registerTypeAdapter(UserAccount.class, new UserAccount.UserAccountSimpleDeserializer());
        builder.registerTypeAdapter(LimitOrder.class, new LimitOrder.LimitOrderDeserializer());
        Type GetLimitOrdersResponse = new TypeToken<WitnessResponse<List<LimitOrder>>>() {}.getType();
        WitnessResponse<List<LimitOrder>> witnessResponse = builder.create().fromJson(serializedOrderBook, GetLimitOrdersResponse);
        List<LimitOrder> orders = witnessResponse.result;
        OrderBook orderBook = new OrderBook(orders);


        final Asset _quote = new Asset("1.3.121", "USD", 4);
        final Asset _base = new Asset("1.3.0", "BTS", 5);
        long _totalQuote = 14;
        UnsignedLong _totalBase = orderBook.calculateRequiredBase(new AssetAmount(UnsignedLong.valueOf(_totalQuote), _quote));
        Assert.assertEquals("Should buy 10 at 10 and 4 at 20, which sums up to 180",180, _totalBase.longValue());
        System.out.println(String.format("Base: %s, Quote: %s", _base.getObjectId(), _quote.getObjectId()));
        System.out.println(String.format("_totalQuote: %d, _totalBase: %d", _totalQuote, _totalBase.longValue()));
    }

    @Test
    public void testRequiredQuote(){
        String serializedOrderBook = "{\"id\":1,\"result\":[{\"id\":\"1.7.37284933\",\"expiration\":\"2018-11-17T00:03:49\",\"seller\":\"1.2.132823\",\"for_sale\":20,\"sell_price\":{\"base\":{\"amount\":20,\"asset_id\":\"1.3.0\"},\"quote\":{\"amount\":1,\"asset_id\":\"1.3.121\"}},\"deferred_fee\":0},{\"id\":\"1.7.37284933\",\"expiration\":\"2018-11-17T00:03:49\",\"seller\":\"1.2.132823\",\"for_sale\":100,\"sell_price\":{\"base\":{\"amount\":10,\"asset_id\":\"1.3.0\"},\"quote\":{\"amount\":1,\"asset_id\":\"1.3.121\"}},\"deferred_fee\":0}]}";
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer());
        builder.registerTypeAdapter(UserAccount.class, new UserAccount.UserAccountSimpleDeserializer());
        builder.registerTypeAdapter(LimitOrder.class, new LimitOrder.LimitOrderDeserializer());
        Type GetLimitOrdersResponse = new TypeToken<WitnessResponse<List<LimitOrder>>>() {}.getType();
        WitnessResponse<List<LimitOrder>> witnessResponse = builder.create().fromJson(serializedOrderBook, GetLimitOrdersResponse);
        List<LimitOrder> orders = witnessResponse.result;
        OrderBook orderBook = new OrderBook(orders);


        final Asset _base = new Asset("1.3.121", "USD", 4);
        final Asset _quote = new Asset("1.3.0", "BTS", 5);
        long _totalBase = 2;
        UnsignedLong _totalQuote = orderBook.calculateObtainedQuote(new AssetAmount(UnsignedLong.valueOf(_totalBase), _base));
        Assert.assertEquals("Should be able to buy 20 on the first order and 10 on the second, totalling 30",30, _totalQuote.longValue());
        System.out.println(String.format("Base: %s, Quote: %s", _base.getObjectId(), _quote.getObjectId()));
        System.out.println(String.format("_totalQuote: %d, _totalBase: %d", _totalQuote.longValue(), _totalQuote.longValue()));
    }
}
