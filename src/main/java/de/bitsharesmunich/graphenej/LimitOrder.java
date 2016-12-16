package de.bitsharesmunich.graphenej;

/**
 *
 * @author henry
 */
public class LimitOrder {
    public String id;
    public String expiration;
    public UserAccount seller;
    public long for_sale;
    public long deferred_fee;
    public Price sell_price;
}
