package de.bitsharesmunich.graphenej;

/**
 * Created by nelson on 1/11/17.
 */
public class LineItem {
    private String label;
    private int quantity;
    private double price;

    public LineItem(String label, int quantity, double price){
        this.label = label;
        this.quantity = quantity;
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getLabel(){
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}