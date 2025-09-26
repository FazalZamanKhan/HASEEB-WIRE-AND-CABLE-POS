package com.cablemanagement.invoice;

public class Item {
    private String name;
    private int quantity;
    private double unitPrice;
    private double discountPercent;

    public Item(String name, int quantity, double unitPrice, double discountPercent) {
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountPercent = discountPercent;
    }

    // Getters and optional setters
    
    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }
}
