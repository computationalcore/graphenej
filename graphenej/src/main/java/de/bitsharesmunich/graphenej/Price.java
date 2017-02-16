package de.bitsharesmunich.graphenej;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * The price struct stores asset prices in the Graphene system.
 *
 * A price is defined as a ratio between two assets, and represents a possible exchange rate
 * between those two assets. prices are generally not stored in any simplified form, i.e. a price
 * of (1000 CORE)/(20 USD) is perfectly normal.

 * The assets within a price are labeled base and quote. Throughout the Graphene code base,
 * the convention used is that the base asset is the asset being sold, and the quote asset is
 * the asset being purchased, where the price is represented as base/quote, so in the example
 * price above the seller is looking to sell CORE asset and get USD in return.
 *
 * Note: Taken from the Graphene doxygen.
 * Created by nelson on 12/16/16.
 */
public class Price {
    public AssetAmount base;
    public AssetAmount quote;
}
