package cy.agorise.graphenej;

import com.google.common.primitives.UnsignedLong;

import java.util.List;

import cy.agorise.graphenej.operations.LimitOrderCreateOperation;

/**
 * This class will maintain a snapshot of the order book between two assets.
 *
 * It also provides a handy method that should return the appropriate LimitOrderCreateOperation
 * object needed in case the user wants to perform market-priced operations.
 *
 * It is important to keep the order book updated, ideally by listening to blockchain events,
 * and calling the 'update' method.
 *
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
        AssetAmount toSell = new AssetAmount(calculateRequiredBase(myQuoteAmount), myBaseAsset);
        AssetAmount toReceive = myQuoteAmount;
        LimitOrderCreateOperation buyOrder = new LimitOrderCreateOperation(seller, toSell, toReceive, expiration, true);

        return buyOrder;
    }

    public LimitOrderCreateOperation exchange(UserAccount seller, AssetAmount baseAmount, Asset quoteAsset, int expiration){
        AssetAmount minToReceive = new AssetAmount(calculateObtainedQuote(baseAmount), quoteAsset);
        return new LimitOrderCreateOperation(seller, baseAmount, minToReceive, expiration, true);
    }

    /**
     * Method that calculates the amount of an asset that we will obtain (the quote amount) if we trade
     * a known fixed amount of the asset we already have (the base amount).
     *
     * @param baseAmount The fixed amount of the asset we have and want to sell
     * @return The equivalent amount to receive in exchange of the base amount
     */
    public UnsignedLong calculateObtainedQuote(AssetAmount baseAmount){
        UnsignedLong myBase = baseAmount.getAmount();
        UnsignedLong obtainedQuote = UnsignedLong.ZERO;
        for(int i = 0; i < limitOrders.size() && myBase.compareTo(UnsignedLong.ZERO) > 0; i++){
            LimitOrder order = limitOrders.get(i);

            // Checking to make sure the order matches our needs
            if(order.getSellPrice().quote.getAsset().equals(baseAmount.getAsset())){
                UnsignedLong orderBase = order.getSellPrice().base.getAmount();
                UnsignedLong orderQuote = order.getSellPrice().quote.getAmount();
                UnsignedLong availableBase = order.getForSale();

                UnsignedLong myQuote = UnsignedLong.valueOf((long)(myBase.times(orderBase).doubleValue() / (orderQuote.doubleValue())));
                if(myQuote.compareTo(availableBase) > 0){
                    // We consume this order entirely
                    // myBase = myBase - (for_sale) * (order_quote / order_base)
                    myBase = myBase.minus(availableBase.times(orderQuote).dividedBy(orderBase));
                    // We need more than this order can offer us, but have to take in consideration how much there really is.
                    // (order base / order quote) x (available order base / order base)
                    UnsignedLong thisBatch = UnsignedLong.valueOf((long)(orderBase.times(availableBase).doubleValue() / orderQuote.times(orderBase).doubleValue()));
                    obtainedQuote = obtainedQuote.plus(thisBatch);
                }else{
                    // This order consumes all our base asset
                    // obtained_quote = obtained_quote + (my base * order_base / order_quote)
                    obtainedQuote = obtainedQuote.plus(myBase.times(orderBase).dividedBy(orderQuote));
                    myBase = UnsignedLong.ZERO;
                }
            }
        }
        return obtainedQuote;
    }

    /**
     * Method that calculates the amount of an asset that we will consume (the base amount) if we want to obtain
     * a known fixed amount of another asset (the quote amount).
     * @param quoteAmount The fixed amount of an asset that we want to obtain
     * @return The amount of an asset we already have that will be consumed by the trade
     */
    public UnsignedLong calculateRequiredBase(AssetAmount quoteAmount){
        UnsignedLong myQuote = quoteAmount.getAmount();
        UnsignedLong obtainedBase = UnsignedLong.ZERO;
        for(int i = 0; i < limitOrders.size() && myQuote.compareTo(UnsignedLong.ZERO) > 0; i++){
            LimitOrder order = limitOrders.get(i);

            // Checking to make sure the order matches our needs
            if(order.getSellPrice().base.getAsset().equals(quoteAmount.getAsset())){
                UnsignedLong orderBase = order.getSellPrice().base.getAmount();
                UnsignedLong orderQuote = order.getSellPrice().quote.getAmount();
                UnsignedLong forSale = order.getForSale();

                if(forSale.compareTo(myQuote) > 0){
                    // Found an order that fills our requirements
                    obtainedBase = obtainedBase.plus(UnsignedLong.valueOf((long) (myQuote.doubleValue() * orderQuote.doubleValue() / orderBase.doubleValue())));
                    myQuote = UnsignedLong.ZERO;
                }else{
                    // Found an order that partially fills our needs
                    obtainedBase = obtainedBase.plus(UnsignedLong.valueOf((long) (forSale.doubleValue() * orderQuote.doubleValue() / orderBase.doubleValue())));
                    myQuote = myQuote.minus(forSale);
                }
            }
        }
        return obtainedBase;
    }

    public List<LimitOrder> getLimitOrders(){
        return limitOrders;
    }
}
