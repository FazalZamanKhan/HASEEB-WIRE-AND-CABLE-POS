package com.cablemanagement.model;

public class RawStockPurchaseItem {
    private Integer rawStockId;
    private String rawStockName;
    private String categoryName;
    private String brandName;
    private String unitName;
    private Double quantity;
    private Double unitPrice;

    public RawStockPurchaseItem() {
        // Default constructor
    }

    public RawStockPurchaseItem(Integer rawStockId, String rawStockName, String brandName, Double quantity, Double unitPrice) {
        this.rawStockId = rawStockId;
        this.rawStockName = rawStockName;
        this.brandName = brandName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.categoryName = ""; // Default value
        this.unitName = "";     // Default value
    }

    // Getters
    public Integer getRawStockId() { return rawStockId; }
    public String getRawStockName() { return rawStockName; }
    public String getCategoryName() { return categoryName; }
    public String getBrandName() { return brandName; }
    public String getUnitName() { return unitName; }
    public Double getQuantity() { return quantity; }
    public Double getUnitPrice() { return unitPrice; }

    // Setters
    public void setRawStockId(Integer rawStockId) { this.rawStockId = rawStockId; }
    public void setRawStockName(String rawStockName) { this.rawStockName = rawStockName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
        public double getTotalPrice() {
        // Replace with actual calculation or field
        if (quantity != null && unitPrice != null) {
            return quantity * unitPrice;
        }
        return 0.0;
    }
}