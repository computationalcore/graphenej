package de.bitsharesmunich.graphenej;

/**
 *
 * @author henry
 */
public class Market {

    public String id;
    public String expiration;
    public String seller;
    public long for_sale;
    public long deferred_fee;
    public Price sell_price;
    
    public class Price {
        public AmountPrice base;
        public AmountPrice quote;
    }

    public class AmountPrice {

        public String asset_id;
        public long amount;
    }
}
