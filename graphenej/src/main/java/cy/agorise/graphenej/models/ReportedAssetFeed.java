package cy.agorise.graphenej.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.Util;

/**
 * Witness-provided asset price feed
 */

public class ReportedAssetFeed {
    private UserAccount reporter;
    private AssetFeed assetFeed;
    private Date reportedDate;

    public ReportedAssetFeed(UserAccount userAccount, Date date, AssetFeed assetFeed){
        this.reporter = userAccount;
        this.reportedDate = date;
        this.assetFeed = assetFeed;
    }

    public UserAccount getReporter() {
        return reporter;
    }

    public void setReporter(UserAccount reporter) {
        this.reporter = reporter;
    }

    public AssetFeed getAssetFeed() {
        return assetFeed;
    }

    public void setAssetFeed(AssetFeed assetFeed) {
        this.assetFeed = assetFeed;
    }

    public Date getReportedDate() {
        return reportedDate;
    }

    public void setReportedDate(Date reportedDate) {
        this.reportedDate = reportedDate;
    }

    public static class ReportedAssetFeedDeserializer implements JsonDeserializer<ReportedAssetFeed> {

        @Override
        public ReportedAssetFeed deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonArray array = json.getAsJsonArray();
            String userId = array.get(0).getAsString();
            JsonArray subArray = (JsonArray) array.get(1);
            String dateString = subArray.get(0).getAsString();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Util.TIME_DATE_FORMAT);
            Date reportDate = null;
            try {
                reportDate = simpleDateFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            AssetFeed assetFeed =  context.deserialize(subArray.get(1), AssetFeed.class);
            UserAccount userAccount = new UserAccount(userId);
            return new ReportedAssetFeed(userAccount, reportDate, assetFeed);
        }
    }
}
