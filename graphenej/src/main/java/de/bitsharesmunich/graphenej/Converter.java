package de.bitsharesmunich.graphenej;

import java.math.BigDecimal;
import java.math.MathContext;

import de.bitsharesmunich.graphenej.errors.IncompleteAssetError;
import de.bitsharesmunich.graphenej.models.BucketObject;

/**
 * Generic converter class used to translate the market information contained in a BucketObject and/or Price instances.
 *
 * Created by nelson on 12/23/16.
 */
public class Converter {
    private final String TAG = this.getClass().getName();
    public static final int OPEN_VALUE = 0;
    public static final int CLOSE_VALUE = 1;
    public static final int HIGH_VALUE = 2;
    public static final int LOW_VALUE = 3;

    public static final int BASE_TO_QUOTE = 100;
    public static final int QUOTE_TO_BASE = 101;

    private Asset base;
    private Asset quote;
    private BucketObject bucket;

    /**
     * Constructor meant to be used trying to perform a conversion and in possession of a Price object.
     */
    public Converter(){}

    /**
     * Constructor meant to be used when trying to perform a conversion and in possession of
     * a BucketObject, typically resulting from a 'get_market_history' API call.
     * @param base
     * @param quote
     * @param bucket
     */
    public Converter(Asset base, Asset quote, BucketObject bucket){
        this.base = base;
        this.quote = quote;
        this.bucket = bucket;
    }

    /**
     * Method used to obtain the equivalence between two assets considering their precisions
     * and given the specific time bucket passed in the constructor.
     *
     * The resulting double value will tell us how much of a given asset, a unit of
     * its pair is worth.
     *
     * The second argument is used to specify which of the assets should
     * be taken as a unit reference.
     *
     * For instance if used with the BASE_TO_QUOTE constant, this method will tell us how
     * many of the quote asset will make up for a unit of the base asset. And the opposite
     * is true for the QUOTE_TO_BASE contant.
     *
     * @param bucketAttribute: The desired bucket attribute to take in consideration. Can
     *                       be any of the following: OPEN_VALUE, CLOSE_VALUE, HIGH_VALUE or
     *                       LOW_VALUE.
     * @param direction: One of two constants 'BASE_TO_QUOTE' or 'QUOTE_TO_BASE' used to specify
     *                 which of the two assets is the one used as a unitary reference.
     * @return: double value representing how much of one asset, a unit of the paired asset
     * was worth at the point in time specified by the time bucket and the bucket parameter.
     */
    public double getConversionRate(int bucketAttribute, int direction){
        if(this.base.getPrecision() == -1 || this.quote.getPrecision() == -1){
            throw new IncompleteAssetError();
        }
        BigDecimal baseValue;
        BigDecimal quoteValue;
        switch (bucketAttribute){
            case OPEN_VALUE:
                baseValue = bucket.open_base;
                quoteValue = bucket.open_quote;
                break;
            case CLOSE_VALUE:
                baseValue = bucket.close_base;
                quoteValue = bucket.close_quote;
                break;
            case HIGH_VALUE:
                baseValue = bucket.high_base;
                quoteValue = bucket.high_quote;
                break;
            case LOW_VALUE:
                baseValue = bucket.low_base;
                quoteValue = bucket.low_quote;
                break;
            default:
                baseValue = bucket.close_base;
                quoteValue = bucket.close_quote;
        }
        double basePrecisionAdjusted = baseValue.divide(BigDecimal.valueOf((long) Math.pow(10, base.getPrecision()))).doubleValue();
        double quotePrecisionAdjusted = quoteValue.divide(BigDecimal.valueOf((long) Math.pow(10, quote.getPrecision()))).doubleValue();
        if(direction == QUOTE_TO_BASE){
            return basePrecisionAdjusted / quotePrecisionAdjusted;
        }else{
            return quotePrecisionAdjusted / basePrecisionAdjusted;
        }
    }

    /**
     * Converts a given asset amount to the corresponding pair used when creating this class.
     * @param assetAmount: The asset to convert from.
     * @param bucketAttribute: The bucket attribute to use as a reference. Possible values are OPEN_VALUE,
     *                       CLOSE_VALUE, HIGH_VALUE or LOW_VALUE.
     * @return: The converted value in base units, that is the number of a unit x 10^precision
     */
    public long convert(AssetAmount assetAmount, int bucketAttribute) {
        double conversionRate = 0;
        double precisionFactor = 0.0;
        if(assetAmount.getAsset().equals(this.base)){
            conversionRate = this.getConversionRate(bucketAttribute, BASE_TO_QUOTE);
            precisionFactor = Math.pow(10, this.quote.getPrecision()) / Math.pow(10, this.base.getPrecision());
        }else if(assetAmount.getAsset().equals(this.quote)){
            conversionRate = this.getConversionRate(bucketAttribute, QUOTE_TO_BASE);
            precisionFactor = Math.pow(10, this.base.getPrecision()) / Math.pow(10, this.quote.getPrecision());
        }
        long assetAmountValue = assetAmount.getAmount().longValue();
        long convertedBaseValue = (long) (assetAmountValue * conversionRate * precisionFactor);
        return convertedBaseValue;
    }

    /**
     * Method used to obtain the conversion rate between two assets given in a Price instance as recovered by the
     * 'get_limit_orders' API call.
     *
     * The same rules that apply for the {@link #getConversionRate(int bucketAttribute, int direction) getConversionRate}
     * are valid for the 'direction' argument.
     *
     * @param price: The Price object instance
     * @param direction: The direction from which to perform the conversion, can be only one of BASE_TO_QUOTE or
     *                 QUOTE_TO_BASE.
     * @return: A double representing the exchange rate.
     */
    public double getConversionRate(Price price, int direction){
        Asset base = price.base.getAsset();
        Asset quote = price.quote.getAsset();
        if(base.getPrecision() == -1 || quote.getPrecision() == -1){
            throw new IncompleteAssetError("The given asset instance must provide precision information");
        }
        double conversionRate = 0;
        double precisionFactor = 0.0;
        MathContext mathContext = new MathContext(Math.max(base.getPrecision(), quote.getPrecision()));
        BigDecimal baseValue = BigDecimal.valueOf(price.base.getAmount().longValue());
        BigDecimal quoteValue = BigDecimal.valueOf(price.quote.getAmount().doubleValue());
//        System.out.println(String.format("base: %d, quote: %d", baseValue.longValue(), quoteValue.longValue()));
        if(direction == BASE_TO_QUOTE){
            conversionRate = quoteValue.divide(baseValue, mathContext).doubleValue();
            precisionFactor = Math.pow(10, base.getPrecision()) / Math.pow(10, quote.getPrecision());
        }else{
            conversionRate = baseValue.divide(quoteValue, mathContext).doubleValue();
            precisionFactor = Math.pow(10, quote.getPrecision()) / Math.pow(10, base.getPrecision());
        }
//        System.out.println(String.format("conversion rate: %.4f, precision factor: %.2f", conversionRate, precisionFactor));
        return conversionRate * precisionFactor;
    }
}