package de.bitsharesmunich.graphenej;

/**
 * Created by nelson on 11/9/16.
 */
public class Asset extends GrapheneObject {
    private String id;
    private String symbol;
    private int precision;
    private String issuer;
    private String dynamic_asset_data_id;
    private AssetOptions options;

    public Asset(String id) {
        super(id);
        this.id = id;
    }

    public String getSymbol(){
        return this.symbol;
    }

    public String getId(){
        return this.id;
    }
}
