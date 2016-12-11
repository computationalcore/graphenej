package de.bitsharesmunich.graphenej;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import de.bitsharesmunich.graphenej.interfaces.JsonSerializable;
import org.bitcoinj.core.Base58;

/**
 * Class used to handle invoice generation, compression and QR-Code data derivation,
 * as detailed in <a href="http://docs.bitshares.eu/integration/merchants/merchant-protocol.html">this</> link.
 * @author Nelson R. Pérez
 */
public class Invoice implements JsonSerializable {
    public static class LineItem {
        private String label;
        private int quantity;
        private String price;

        public LineItem(String label, int quantity, String price){
            this.label = label;
            this.quantity = quantity;
            this.price = price;
        }
    }
    private String to;
    private String to_label;
    private String memo;
    private String currency;
    private LineItem[] line_items;
    private String note;
    private String callback;

    public Invoice(String to, String to_label, String memo, String currency, LineItem[] items, String note, String callback){
        this.to = to;
        this.to_label = to_label;
        this.memo = memo;
        this.currency = currency;
        this.line_items = items;
        this.note = note;
        this.callback = callback;
    }

    @Override
    public String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public JsonElement toJsonObject() {
        return null;
    }

    public static String toQrCode(Invoice invoice){
        String json = invoice.toJsonString();
        return Base58.encode(Util.compress(json.getBytes(), Util.LZMA));
    }

    public static Invoice fromQrCode(String encoded){
        String json = new String(Util.decompress(Base58.decode(encoded), Util.LZMA));
        Gson gson = new Gson();
        return gson.fromJson(json, Invoice.class);
    }
}
