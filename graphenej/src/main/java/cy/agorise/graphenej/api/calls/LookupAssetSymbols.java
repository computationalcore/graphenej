package cy.agorise.graphenej.api.calls;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.models.ApiCall;

public class LookupAssetSymbols implements ApiCallable {
    public static final int REQUIRED_API = ApiAccess.API_NONE;

    private List<Asset> mAssetList;

    public LookupAssetSymbols(List<Asset> assetList){
        this.mAssetList = assetList;
    }

    public LookupAssetSymbols(Asset asset){
        mAssetList = new ArrayList<Asset>();
        mAssetList.add(asset);
    }

    @Override
    public ApiCall toApiCall(int apiId, long sequenceId) {
        ArrayList<Serializable> params = new ArrayList<>();
        ArrayList<String> subArray = new ArrayList<>();
        for(int i = 0; i < mAssetList.size(); i++){
            Asset asset = mAssetList.get(i);
            subArray.add(asset.getObjectId());
            params.add(subArray);
        }
        return new ApiCall(apiId, RPC.CALL_LOOKUP_ASSET_SYMBOLS, params, RPC.VERSION, sequenceId);
    }
}
