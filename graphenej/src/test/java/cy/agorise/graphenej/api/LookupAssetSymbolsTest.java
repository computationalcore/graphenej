package cy.agorise.graphenej.api;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.BaseResponse;
import cy.agorise.graphenej.models.WitnessResponse;

/**
 * Testing the standalone usage of the {@link LookupAssetSymbols} API handler.
 */

public class LookupAssetSymbolsTest extends BaseApiTest {

    @Test
    public void testLookupAssetSymbolsWithString(){
        ArrayList<String> assetSymbols = new ArrayList<>();
        assetSymbols.add("USD");
        mWebSocket.addListener(new LookupAssetSymbols(assetSymbols, true, new WitnessResponseListener() {
            @Override
            public void onSuccess(WitnessResponse response) {
                System.out.println("onSuccess");
                List<Asset> assets = (List<Asset>) response.result;
                Assert.assertEquals(1, assets.size());
                Assert.assertEquals("1.3.121", assets.get(0).getObjectId());
            }

            @Override
            public void onError(BaseResponse.Error error) {
                System.out.println("onError");
            }
        }));
    }

    @Test
    public void testLookupAssetSymbolsWithAsset(){
        ArrayList<Asset> assetSymbols = new ArrayList<>();
        assetSymbols.add(new Asset("1.3.121"));
        mWebSocket.addListener(new LookupAssetSymbols(assetSymbols, true, new WitnessResponseListener() {
            @Override
            public void onSuccess(WitnessResponse response) {
                System.out.println("onSuccess");
                List<Asset> assets = (List<Asset>) response.result;
                Assert.assertEquals(1, assets.size());
                Assert.assertEquals("1.3.121", assets.get(0).getObjectId());
            }

            @Override
            public void onError(BaseResponse.Error error) {
                System.out.println("onError");
            }
        }));
    }
}
