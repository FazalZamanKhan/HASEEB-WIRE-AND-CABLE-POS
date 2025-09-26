package com.cablemanagement.model;

import javafx.beans.property.*;

/**
 * Model class for Raw Stock Use Invoice Items
 * Represents individual items used in a Raw Stock Use Invoice
 */
public class RawStockUseItem {
    
    private final IntegerProperty useItemId;
    private final IntegerProperty rawStockId;
    private final StringProperty rawStockName;
    private final StringProperty categoryName;
    private final StringProperty brandName;
    private final StringProperty unitName;
    private final DoubleProperty quantityUsed;
    private final DoubleProperty unitCost;
    private final DoubleProperty totalCost;
    private final DoubleProperty availableQuantity; // Available quantity in stock
    
    // Default constructor
    public RawStockUseItem() {
        this.useItemId = new SimpleIntegerProperty(0);
        this.rawStockId = new SimpleIntegerProperty(0);
        this.rawStockName = new SimpleStringProperty("");
        this.categoryName = new SimpleStringProperty("");
        this.brandName = new SimpleStringProperty("");
        this.unitName = new SimpleStringProperty("");
        this.quantityUsed = new SimpleDoubleProperty(0.0);
        this.unitCost = new SimpleDoubleProperty(0.0);
        this.totalCost = new SimpleDoubleProperty(0.0);
        this.availableQuantity = new SimpleDoubleProperty(0.0);
        
        // Auto-calculate total cost when quantity or unit cost changes
        quantityUsed.addListener((obs, oldVal, newVal) -> recalculateTotalCost());
        unitCost.addListener((obs, oldVal, newVal) -> recalculateTotalCost());
    }
    
    // Constructor with parameters
    public RawStockUseItem(int rawStockId, String rawStockName, String categoryName, String brandName, 
                          String unitName, double quantityUsed, double unitCost, double availableQuantity) {
        this();
        this.rawStockId.set(rawStockId);
        this.rawStockName.set(rawStockName);
        this.categoryName.set(categoryName);
        this.brandName.set(brandName);
        this.unitName.set(unitName);
        this.quantityUsed.set(quantityUsed);
        this.unitCost.set(unitCost);
        this.availableQuantity.set(availableQuantity);
        recalculateTotalCost();
    }
    
    // Getters and Setters for JavaFX Properties
    public IntegerProperty useItemIdProperty() { return useItemId; }
    public int getUseItemId() { return useItemId.get(); }
    public void setUseItemId(int useItemId) { this.useItemId.set(useItemId); }
    
    public IntegerProperty rawStockIdProperty() { return rawStockId; }
    public int getRawStockId() { return rawStockId.get(); }
    public void setRawStockId(int rawStockId) { this.rawStockId.set(rawStockId); }
    
    public StringProperty rawStockNameProperty() { return rawStockName; }
    public String getRawStockName() { return rawStockName.get(); }
    public void setRawStockName(String rawStockName) { this.rawStockName.set(rawStockName); }
    
    public StringProperty categoryNameProperty() { return categoryName; }
    public String getCategoryName() { return categoryName.get(); }
    public void setCategoryName(String categoryName) { this.categoryName.set(categoryName); }
    
    public StringProperty brandNameProperty() { return brandName; }
    public String getBrandName() { return brandName.get(); }
    public void setBrandName(String brandName) { this.brandName.set(brandName); }
    
    public StringProperty unitNameProperty() { return unitName; }
    public String getUnitName() { return unitName.get(); }
    public void setUnitName(String unitName) { this.unitName.set(unitName); }
    
    public DoubleProperty quantityUsedProperty() { return quantityUsed; }
    public double getQuantityUsed() { return quantityUsed.get(); }
    public void setQuantityUsed(double quantityUsed) { this.quantityUsed.set(quantityUsed); }
    
    public DoubleProperty unitCostProperty() { return unitCost; }
    public double getUnitCost() { return unitCost.get(); }
    public void setUnitCost(double unitCost) { this.unitCost.set(unitCost); }
    
    public DoubleProperty totalCostProperty() { return totalCost; }
    public double getTotalCost() { return totalCost.get(); }
    public void setTotalCost(double totalCost) { this.totalCost.set(totalCost); }
    
    public DoubleProperty availableQuantityProperty() { return availableQuantity; }
    public double getAvailableQuantity() { return availableQuantity.get(); }
    public void setAvailableQuantity(double availableQuantity) { this.availableQuantity.set(availableQuantity); }
    
    /**
     * Recalculate the total cost (quantity used * unit cost)
     */
    public void recalculateTotalCost() {
        double total = getQuantityUsed() * getUnitCost();
        setTotalCost(total);
    }
    
    /**
     * Check if the requested quantity is available in stock
     * @param requestedQuantity The quantity requested to use
     * @return true if the quantity is available, false otherwise
     */
    public boolean isQuantityAvailable(double requestedQuantity) {
        return requestedQuantity <= getAvailableQuantity();
    }
    
    /**
     * Get formatted display string for dropdown selection
     * @return Formatted string with stock name, brand, and available quantity
     */
    public String getDisplayString() {
        return String.format("%s - %s (Available: %.2f %s)", 
                getRawStockName(), getBrandName(), getAvailableQuantity(), getUnitName());
    }
    
    /**
     * Create a copy of this item with a specific quantity used
     * @param quantityUsed The quantity to use
     * @return New RawStockUseItem with the specified quantity
     */
    public RawStockUseItem createUseItem(double quantityUsed) {
        return new RawStockUseItem(getRawStockId(), getRawStockName(), getCategoryName(), 
                getBrandName(), getUnitName(), quantityUsed, getUnitCost(), getAvailableQuantity());
    }
    
    @Override
    public String toString() {
        return String.format("RawStockUseItem{id=%d, name='%s', brand='%s', qty=%.2f %s, cost=%.2f, total=%.2f}", 
                getRawStockId(), getRawStockName(), getBrandName(), getQuantityUsed(), 
                getUnitName(), getUnitCost(), getTotalCost());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RawStockUseItem that = (RawStockUseItem) obj;
        return getRawStockId() == that.getRawStockId();
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(getRawStockId());
    }
}
