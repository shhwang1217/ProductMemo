package com.longher.www.productmemo;

public class ProductRecord {
    private String barcode;
    private String name;
    private double cost;
    private double price;
    private byte[] picture;

    public ProductRecord( String barcode ) {
        this.barcode = barcode;
        this.name = "";
        this.cost = 0.0;
        this.price = 0.0;
        this.picture = null;
    }

    public ProductRecord( String barcode, String name, double cost, double price, byte[] picture) {
        this.barcode = barcode;
        this.name = name;
        this.cost = cost;
        this.price = price;
        this.picture = picture;
    }

    public void setBarcode( String barcode) {
        this.barcode = barcode;
    }

    public String getBarcode() {
        return this.barcode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCost( double cost ) {
        this.cost = cost;
    }

    public double getCost() {
        return cost;
    }

    public void setPrice( double price ) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    public byte[] getPicture() {
        return picture;
    }

}
