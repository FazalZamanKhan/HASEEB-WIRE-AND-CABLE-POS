package com.cablemanagement.model;

import javafx.beans.property.*;

public class ProductionStockItem {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty category;
    private final StringProperty brand;
    private final StringProperty unit;
    private final DoubleProperty quantity;
    private final DoubleProperty salePrice;

    public ProductionStockItem(int id, String name, String category, String brand, String unit, double quantity, double salePrice) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.category = new SimpleStringProperty(category);
        this.brand = new SimpleStringProperty(brand);
        this.unit = new SimpleStringProperty(unit);
        this.quantity = new SimpleDoubleProperty(quantity);
        this.salePrice = new SimpleDoubleProperty(salePrice);
    }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty categoryProperty() { return category; }
    public StringProperty brandProperty() { return brand; }
    public StringProperty unitProperty() { return unit; }
    public DoubleProperty quantityProperty() { return quantity; }
    public DoubleProperty salePriceProperty() { return salePrice; }

    // Value getters
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getCategory() { return category.get(); }
    public String getBrand() { return brand.get(); }
    public String getUnit() { return unit.get(); }
    public double getQuantity() { return quantity.get(); }
    public double getSalePrice() { return salePrice.get(); }

    // Value setters
    public void setId(int id) { this.id.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setCategory(String category) { this.category.set(category); }
    public void setBrand(String brand) { this.brand.set(brand); }
    public void setUnit(String unit) { this.unit.set(unit); }
    public void setQuantity(double quantity) { this.quantity.set(quantity); }
    public void setSalePrice(double salePrice) { this.salePrice.set(salePrice); }
}
