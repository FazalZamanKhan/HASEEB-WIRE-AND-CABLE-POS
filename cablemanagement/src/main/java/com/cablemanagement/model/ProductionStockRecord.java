package com.cablemanagement.model;

public class ProductionStockRecord {
    private final int productionId;
    private final String name;
    private final String description;
    private final String brand;
    private final String brandDescription;
    private final String unit;
    private final int quantity;
    private final double unitCost;
    private final double salePrice;
    
    // Original constructor for backward compatibility
    public ProductionStockRecord(String name, String brand, int quantity, double unitCost, double totalCost) {
        this.productionId = 0;
        this.name = name;
        this.description = "";
        this.brand = brand;
        this.brandDescription = "";
        this.unit = "N/A";
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.salePrice = totalCost; // Use totalCost as salePrice for backward compatibility
    }
    
    // New comprehensive constructor
    public ProductionStockRecord(int productionId, String name, String description, String brand, 
                                String brandDescription, String unit, int quantity, double unitCost, double salePrice) {
        this.productionId = productionId;
        this.name = name;
        this.description = description;
        this.brand = brand;
        this.brandDescription = brandDescription;
        this.unit = unit;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.salePrice = salePrice;
    }
    
    // Getters
    public int getProductionId() { return productionId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getBrand() { return brand; }
    public String getBrandDescription() { return brandDescription; }
    public String getUnit() { return unit; }
    public int getQuantity() { return quantity; }
    public double getUnitCost() { return unitCost; }
    public double getSalePrice() { return salePrice; }
    
    // Backward compatibility methods
    public double getTotalCost() { return quantity * unitCost; }
}
