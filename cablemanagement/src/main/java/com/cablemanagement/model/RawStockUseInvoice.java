package com.cablemanagement.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Model class for Raw Stock Use Invoice
 * Represents an invoice for using raw materials from stock for production
 */
public class RawStockUseInvoice {
    
    private final IntegerProperty useInvoiceId;
    private final StringProperty useInvoiceNumber;
    private final ObjectProperty<LocalDate> usageDate;
    private final DoubleProperty totalUsageAmount;
    private final StringProperty referencePurpose;
    private final ObservableList<RawStockUseItem> useItems;
    
    // Default constructor
    public RawStockUseInvoice() {
        this.useInvoiceId = new SimpleIntegerProperty(0);
        this.useInvoiceNumber = new SimpleStringProperty("");
        this.usageDate = new SimpleObjectProperty<>(LocalDate.now());
        this.totalUsageAmount = new SimpleDoubleProperty(0.0);
        this.referencePurpose = new SimpleStringProperty("");
        this.useItems = FXCollections.observableArrayList();
    }
    
    // Constructor with parameters
    public RawStockUseInvoice(int useInvoiceId, String useInvoiceNumber, LocalDate usageDate, 
                             double totalUsageAmount, String referencePurpose) {
        this.useInvoiceId = new SimpleIntegerProperty(useInvoiceId);
        this.useInvoiceNumber = new SimpleStringProperty(useInvoiceNumber);
        this.usageDate = new SimpleObjectProperty<>(usageDate);
        this.totalUsageAmount = new SimpleDoubleProperty(totalUsageAmount);
        this.referencePurpose = new SimpleStringProperty(referencePurpose);
        this.useItems = FXCollections.observableArrayList();
    }
    
    // Getters and Setters for JavaFX Properties
    public IntegerProperty useInvoiceIdProperty() { return useInvoiceId; }
    public int getUseInvoiceId() { return useInvoiceId.get(); }
    public void setUseInvoiceId(int useInvoiceId) { this.useInvoiceId.set(useInvoiceId); }
    
    public StringProperty useInvoiceNumberProperty() { return useInvoiceNumber; }
    public String getUseInvoiceNumber() { return useInvoiceNumber.get(); }
    public void setUseInvoiceNumber(String useInvoiceNumber) { this.useInvoiceNumber.set(useInvoiceNumber); }
    
    public ObjectProperty<LocalDate> usageDateProperty() { return usageDate; }
    public LocalDate getUsageDate() { return usageDate.get(); }
    public void setUsageDate(LocalDate usageDate) { this.usageDate.set(usageDate); }
    
    public DoubleProperty totalUsageAmountProperty() { return totalUsageAmount; }
    public double getTotalUsageAmount() { return totalUsageAmount.get(); }
    public void setTotalUsageAmount(double totalUsageAmount) { this.totalUsageAmount.set(totalUsageAmount); }
    
    public StringProperty referencePurposeProperty() { return referencePurpose; }
    public String getReferencePurpose() { return referencePurpose.get(); }
    public void setReferencePurpose(String referencePurpose) { this.referencePurpose.set(referencePurpose); }
    
    public ObservableList<RawStockUseItem> getUseItems() { return useItems; }
    
    /**
     * Add an item to the use invoice
     * @param item The RawStockUseItem to add
     */
    public void addUseItem(RawStockUseItem item) {
        useItems.add(item);
        recalculateTotal();
    }
    
    /**
     * Remove an item from the use invoice
     * @param item The RawStockUseItem to remove
     */
    public void removeUseItem(RawStockUseItem item) {
        useItems.remove(item);
        recalculateTotal();
    }
    
    /**
     * Clear all items from the use invoice
     */
    public void clearUseItems() {
        useItems.clear();
        recalculateTotal();
    }
    
    /**
     * Recalculate the total usage amount based on all items
     */
    public void recalculateTotal() {
        double total = useItems.stream()
                .mapToDouble(item -> item.getQuantityUsed() * item.getUnitCost())
                .sum();
        setTotalUsageAmount(total);
    }
    
    /**
     * Get formatted usage date as string
     * @return Formatted date string (yyyy-MM-dd)
     */
    public String getFormattedUsageDate() {
        return usageDate.get().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    
    /**
     * Set usage date from string
     * @param dateString Date in yyyy-MM-dd format
     */
    public void setUsageDateFromString(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            setUsageDate(date);
        } catch (Exception e) {
            // If parsing fails, use current date
            setUsageDate(LocalDate.now());
        }
    }
    
    @Override
    public String toString() {
        return String.format("RawStockUseInvoice{id=%d, number='%s', date=%s, total=%.2f, purpose='%s', items=%d}", 
                getUseInvoiceId(), getUseInvoiceNumber(), getFormattedUsageDate(), 
                getTotalUsageAmount(), getReferencePurpose(), useItems.size());
    }
}
