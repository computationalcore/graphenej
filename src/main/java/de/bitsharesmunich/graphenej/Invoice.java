package de.bitsharesmunich.graphenej;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.bitcoinj.core.Base58;

import de.bitsharesmunich.graphenej.interfaces.JsonSerializable;

/**
 * Class used to handle invoice generation, compression and QR-Code data derivation,
 * as detailed in <a href="http://docs.bitshares.eu/integration/merchants/merchant-protocol.html">this</> link.
 * @author Nelson R. Pérez
 */
public class Invoice implements JsonSerializable {
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

    public String getToLabel() {
        return to_label;
    }

    public void setToLabel(String to_label) {
        this.to_label = to_label;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LineItem[] getLineItems() {
        return line_items;
    }

    public void setLineItems(LineItem[] line_items) {
        this.line_items = line_items;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
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
