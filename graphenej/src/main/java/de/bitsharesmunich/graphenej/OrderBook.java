package de.bitsharesmunich.graphenej;

import com.google.common.primitives.UnsignedLong;

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
     * Method used to exchange a specific amount of an asset (The base) for another one (The quote) at market value.
     *
     * It should analyze the order book and figure out the optimal amount of the base asset to give
     * away in order to obtain the desired amount of the quote asset.
     *
     * @param seller: User account of the seller, used to build the limit order create operation
     * @param myBaseAsset: The asset the user is willing to give away
     * @param myQuoteAmount: The amount of a given asset the user wants
     *
     * @return An instance of the LimitOrderCreateOperation class, which is ready to be broadcasted.
     */
    public LimitOrderCreateOperation exchange(UserAccount seller, Asset myBaseAsset, AssetAmount myQuoteAmount){
        long totalBought = 0;
        long totalSold = 0;
        for(int i = 0; i < this.limitOrders.size() && totalBought < myQuoteAmount.getAmount().longValue(); i++){
            LimitOrder order = this.limitOrders.get(i);

            // If the base asset is the same as our quote asset, we have a match
            if(order.getSellPrice().base.getAsset().getObjectId().equals(myQuoteAmount.getAsset().getObjectId())){
                // My quote amount, is the order's base amount
                long orderAmount = order.getSellPrice().base.getAmount().longValue();

                // The amount of the quote asset we still need
                long stillNeed = myQuoteAmount.getAmount().longValue() - totalBought;

                // If the offered amount is greater than what we still need, we exchange just what we need
                if(orderAmount >= stillNeed) {
                    totalBought += stillNeed;
                    totalSold += (order.getSellPrice().quote.getAmount().longValue() * stillNeed) / order.getSellPrice().base.getAmount().longValue();;
                }else{
                    // If the offered amount is less than what we need, we exchange the whole order
                    totalBought += orderAmount;
                    totalSold += order.getSellPrice().quote.getAmount().longValue();
                }
            }
        }
        AssetAmount toSell = new AssetAmount(UnsignedLong.valueOf(totalSold), myBaseAsset);
        AssetAmount toReceive = myQuoteAmount;
        LimitOrderCreateOperation buyOrder = new LimitOrderCreateOperation(seller, toSell, toReceive, true);

        return buyOrder;
    }
}
