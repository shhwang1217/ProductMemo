package com.longher.www.productmemo;

public class SearchRecord {

    private String barcode;
    private String name;
    byte [] image; // Thumbnail. Small picture
    double price;

    public SearchRecord() {
        super();
    }

    public SearchRecord( String barcode, String name, double price, byte [] image ) {
        super();
        this.barcode = barcode;
        this.name = name;
        this.price = price;
        this.image = image;
    }

    public byte []  getImage() {
        return image;
    }

    public void setImage( byte [] image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode( String barcode ) {
        this.barcode = barcode;
    }

    public double getPrice() {
        return this.price;
    }

    public void setPrice( double price ) {
        this.price = price;
    }
}
