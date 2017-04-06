package de.bitsharesmunich.graphenej;

import com.google.common.math.DoubleMath;
import com.google.common.primitives.UnsignedLong;

import java.math.RoundingMode;
import java.util.List;

import de.bitsharesmunich.graphenej.operations.LimitOrderCreateOperation;

/**
 * This class will maintain a snapshot of the order book between two assets.
 *
 * It also provides a handy method that should return the appropriate LimitOrderCreateOperation
 * object needed in case the user wants to perform market-priced operations.
 *
 * It is important to keep the order book updated, ideally by listening to blockchain events, and calling the 'update' method.
 *
 * Created by nelson on 3/25/17.
 */
public class OrderBook {
    private List<LimitOrder> limitOrders;

    public OrderBook(List<LimitOrder> limitOrders){
        this.limitOrders = limitOrders;
    }

    /**
     * Replaces the current limit order by the list provided as parameter.
     * @param limitOrders: New list of orders
     */
    public void update(List<LimitOrder> limitOrders){
        this.limitOrders = limitOrders;
    }

    public void update(LimitOrder limitOrder){
        //TODO: Implement the method that will update a single limit order from the order book
    }

    /**
     * High level method used to exchange a specific amount of an asset (The base) for another
     * one (The quote) at market value.
     *
     * It should analyze the order book and figure out the optimal amount of the base asset to give
     * away in order to obtain the desired amount of the quote asset.
     *
     * @param seller: User account of the seller, used to build the limit order create operation
     * @param myBaseAsset: The asset the user is willing to give away
     * @param myQuoteAmount: The amount of a given asset the user wants
     * @param expiration: The expiration time of the limit order
     *
     * @return An instance of the LimitOrderCreateOperation class, which is ready to be broadcasted.
     */
    public LimitOrderCreateOperation exchange(UserAccount seller, Asset myBaseAsset, AssetAmount myQuoteAmount, int expiration){
        AssetAmount toSell = new AssetAmount(UnsignedLong.valueOf(calculateRequiredBase(myQuoteAmount)), myBaseAsset);
        AssetAmount toReceive = myQuoteAmount;
        LimitOrderCreateOperation buyOrder = new LimitOrderCreateOperation(seller, toSell, toReceive, expiration, true);

        return buyOrder;
    }

    /**
     * Given a specific amount of a desired asset, this method will calculate how much of the corresponding
     * asset we need to offer to perform a successful transaction with the current order book.
     * @param quoteAmount: The amount of the desired asset.
     * @return: The minimum amount of the base asset that we need to give away
     */
    public long calculateRequiredBase(AssetAmount quoteAmount){
        long totalBought = 0;
        long totalSold = 0;
        for(int i = 0; i < this.limitOrders.size() && totalBought < quoteAmount.getAmount().longValue(); i++){
            LimitOrder order = this.limitOrders.get(i);

            // If the base asset is the same as our quote asset, we have a match
            if(order.getSellPrice().base.getAsset().getObjectId().equals(quoteAmount.getAsset().getObjectId())){
                // My quote amount, is the order's base amount
                long orderAmount = order.getForSale();

                // The amount of the quote asset we still need
                long stillNeed = quoteAmount.getAmount().longValue() - totalBought;

                // If the offered amount is greater than what we still need, we exchange just what we need
                if(orderAmount >= stillNeed) {
                    totalBought += stillNeed;
                    double additionalRatio = (double) stillNeed / (double) order.getSellPrice().base.getAmount().longValue();
                    double additionalAmount = order.getSellPrice().quote.getAmount().longValue() * additionalRatio;
                    long longAdditional = DoubleMath.roundToLong(additionalAmount, RoundingMode.HALF_UP);
                    totalSold += longAdditional;
                }else{
                    // If the offered amount is less than what we need, we exchange the whole order
                    totalBought += orderAmount;
                    totalSold += order.getSellPrice().quote.getAmount().longValue();
                }
            }
        }
        return totalSold;
    }
}
