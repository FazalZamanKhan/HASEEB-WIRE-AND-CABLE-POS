package com.cablemanagement.model;

import javafx.beans.property.*;

public class SalesInvoiceItem {
    private final IntegerProperty id;
    private final IntegerProperty salesInvoiceId;
    private final IntegerProperty productionStockId;
    private final StringProperty productName;
    private final DoubleProperty quantity;
    private final DoubleProperty unitPrice;
    private final DoubleProperty discountPercentage;
    private final DoubleProperty discountAmount;
    private final DoubleProperty totalPrice;

    public SalesInvoiceItem(int id, int salesInvoiceId, int productionStockId, String productName,
                           double quantity, double unitPrice, double discountPercentage, double discountAmount) {
        this.id = new SimpleIntegerProperty(id);
        this.salesInvoiceId = new SimpleIntegerProperty(salesInvoiceId);
        this.productionStockId = new SimpleIntegerProperty(productionStockId);
        this.productName = new SimpleStringProperty(productName);
        this.quantity = new SimpleDoubleProperty(quantity);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
        this.discountPercentage = new SimpleDoubleProperty(discountPercentage);
        this.discountAmount = new SimpleDoubleProperty(discountAmount);
        this.totalPrice = new SimpleDoubleProperty();
        
        // Calculate initial total price
        calculateTotalPrice();
        
        // Add listeners to recalculate when values change
        this.quantity.addListener((obs, oldVal, newVal) -> calculateTotalPrice());
        this.unitPrice.addListener((obs, oldVal, newVal) -> calculateTotalPrice());
        this.discountPercentage.addListener((obs, oldVal, newVal) -> calculateTotalPrice());
        this.discountAmount.addListener((obs, oldVal, newVal) -> calculateTotalPrice());
    }

    // Constructor without ID for new items
    public SalesInvoiceItem(int salesInvoiceId, int productionStockId, String productName,
                           double quantity, double unitPrice, double discountPercentage, double discountAmount) {
        this(0, salesInvoiceId, productionStockId, productName, quantity, unitPrice, discountPercentage, discountAmount);
    }
    
    // Constructor with backward compatibility (no discount)
    public SalesInvoiceItem(int id, int salesInvoiceId, int productionStockId, String productName,
                           double quantity, double unitPrice) {
        this(id, salesInvoiceId, productionStockId, productName, quantity, unitPrice, 0.0, 0.0);
    }

    // Constructor without ID for new items (backward compatibility)
    public SalesInvoiceItem(int salesInvoiceId, int productionStockId, String productName,
                           double quantity, double unitPrice) {
        this(0, salesInvoiceId, productionStockId, productName, quantity, unitPrice, 0.0, 0.0);
    }
    
    private void calculateTotalPrice() {
        double basePrice = quantity.get() * unitPrice.get();
        // discountAmount now contains the total discount for all quantity
        double finalPrice = basePrice - discountAmount.get();
        this.totalPrice.set(Math.max(0, finalPrice)); // Ensure price is not negative
    }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty salesInvoiceIdProperty() { return salesInvoiceId; }
    public IntegerProperty productionStockIdProperty() { return productionStockId; }
    public StringProperty productNameProperty() { return productName; }
    public DoubleProperty quantityProperty() { return quantity; }
    public DoubleProperty unitPriceProperty() { return unitPrice; }
    public DoubleProperty discountPercentageProperty() { return discountPercentage; }
    public DoubleProperty discountAmountProperty() { return discountAmount; }
    public DoubleProperty totalPriceProperty() { return totalPrice; }

    // Value getters
    public int getId() { return id.get(); }
    public int getSalesInvoiceId() { return salesInvoiceId.get(); }
    public int getProductionStockId() { return productionStockId.get(); }
    public String getProductName() { return productName.get(); }
    public double getQuantity() { return quantity.get(); }
    public double getUnitPrice() { return unitPrice.get(); }
    public double getDiscountPercentage() { return discountPercentage.get(); }
    public double getDiscountAmount() { return discountAmount.get(); }
    public double getTotalPrice() { return totalPrice.get(); }

    // Value setters
    public void setId(int id) { this.id.set(id); }
    public void setSalesInvoiceId(int salesInvoiceId) { this.salesInvoiceId.set(salesInvoiceId); }
    public void setProductionStockId(int productionStockId) { this.productionStockId.set(productionStockId); }
    public void setProductName(String productName) { this.productName.set(productName); }
    public void setQuantity(double quantity) { 
        this.quantity.set(quantity);
    }
    public void setUnitPrice(double unitPrice) { 
        this.unitPrice.set(unitPrice);
    }
    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage.set(discountPercentage);
    }
    public void setDiscountAmount(double discountAmount) {
        this.discountAmount.set(discountAmount);
    }
}
