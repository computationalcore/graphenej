package cy.agorise.graphenej.api.calls;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.models.ApiCall;

public class GetMarketHistory implements ApiCallable {
    public static final int REQUIRED_API = ApiAccess.API_HISTORY;

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

    // API call parameters
    private Asset base;
    private Asset quote;
    private long bucket;
    private Date start;
    private Date end;

    /**
     * Constructor that receives the start and end time as UNIX timestamp in milliseconds.
     *
     * @param base      Desired asset history
     * @param quote     Asset to which the base price will be compared to
     * @param bucket    The time interval (in seconds) for each point should be (analog to
     *                      candles on a candle stick graph).
     * @param start     Timestamp (POSIX) of of the most recent operation to retrieve
     *                  (Note: The name can be counter intuitive, but it follow the original
     *                  API parameter name)
     * @param end       Timestamp (POSIX) of the the earliest operation to retrieve
     */
    public GetMarketHistory(Asset base, Asset quote, long bucket, long start, long end){
        this(base, quote, bucket, fromTimestamp(start), fromTimestamp(end));
    }

    /**
     * Constructor that receives the start and end time as Date instance objects.
     *
     * @param base      Desired asset history
     * @param quote     Asset to which the base price will be compared to
     * @param bucket    The time interval (in seconds) for each point should be (analog to
     *                      candles on a candle stick graph).
     * @param start     Date and time of of the most recent operation to retrieve
     *                  (Note: The name can be counter intuitive, but it follow the original
     *                  API parameter name)
     * @param end       Date and time of the the earliest operation to retrieve
     */
    public GetMarketHistory(Asset base, Asset quote, long bucket, Date start, Date end){
        this.base = base;
        this.quote = quote;
        this.bucket = bucket;
        this.start = start;
        this.end = end;
    }

    /**
     * Internal method used to convert a timestamp to a Date.
     *
     * @param timestamp POSIX timestamp expressed in milliseconds since 1/1/1970
     * @return          Date instance
     */
    private static Date fromTimestamp(long timestamp){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    @Override
    public ApiCall toApiCall(int apiId, long sequenceId) {
        ArrayList<Serializable> params = new ArrayList<>();
        params.add(this.base.getObjectId());
        params.add(this.quote.getObjectId());
        params.add(this.bucket);
        params.add(DATE_FORMAT.format(this.start));
        params.add(DATE_FORMAT.format(this.end));
        return new ApiCall(apiId, RPC.CALL_GET_MARKET_HISTORY, params, RPC.VERSION, sequenceId);
    }
}
