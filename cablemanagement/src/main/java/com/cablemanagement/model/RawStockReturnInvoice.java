package com.cablemanagement.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RawStockReturnInvoice {
    private final IntegerProperty invoiceId = new SimpleIntegerProperty();
    private final StringProperty returnInvoiceNumber = new SimpleStringProperty();
    private final IntegerProperty originalInvoiceId = new SimpleIntegerProperty();
    private final StringProperty originalInvoiceNumber = new SimpleStringProperty();
    private final IntegerProperty supplierId = new SimpleIntegerProperty();
    private final StringProperty supplierName = new SimpleStringProperty();
    private final StringProperty returnDate = new SimpleStringProperty();
    private final DoubleProperty totalReturnAmount = new SimpleDoubleProperty();
    private final ObservableList<RawStockPurchaseItem> returnItems = FXCollections.observableArrayList();

    // Default constructor
    public RawStockReturnInvoice() {
    }

    // Constructor with basic details
    public RawStockReturnInvoice(String returnInvoiceNumber, int originalInvoiceId, 
                                String originalInvoiceNumber, int supplierId, String supplierName,
                                String returnDate, double totalReturnAmount) {
        this.returnInvoiceNumber.set(returnInvoiceNumber);
        this.originalInvoiceId.set(originalInvoiceId);
        this.originalInvoiceNumber.set(originalInvoiceNumber);
        this.supplierId.set(supplierId);
        this.supplierName.set(supplierName);
        this.returnDate.set(returnDate);
        this.totalReturnAmount.set(totalReturnAmount);
    }

    // Full constructor
    public RawStockReturnInvoice(int invoiceId, String returnInvoiceNumber, int originalInvoiceId,
                                String originalInvoiceNumber, int supplierId, String supplierName,
                                String returnDate, double totalReturnAmount) {
        this.invoiceId.set(invoiceId);
        this.returnInvoiceNumber.set(returnInvoiceNumber);
        this.originalInvoiceId.set(originalInvoiceId);
        this.originalInvoiceNumber.set(originalInvoiceNumber);
        this.supplierId.set(supplierId);
        this.supplierName.set(supplierName);
        this.returnDate.set(returnDate);
        this.totalReturnAmount.set(totalReturnAmount);
    }

    // Property getters
    public IntegerProperty invoiceIdProperty() { return invoiceId; }
    public StringProperty returnInvoiceNumberProperty() { return returnInvoiceNumber; }
    public IntegerProperty originalInvoiceIdProperty() { return originalInvoiceId; }
    public StringProperty originalInvoiceNumberProperty() { return originalInvoiceNumber; }
    public IntegerProperty supplierIdProperty() { return supplierId; }
    public StringProperty supplierNameProperty() { return supplierName; }
    public StringProperty returnDateProperty() { return returnDate; }
    public DoubleProperty totalReturnAmountProperty() { return totalReturnAmount; }

    // Value getters
    public int getInvoiceId() { return invoiceId.get(); }
    public String getReturnInvoiceNumber() { return returnInvoiceNumber.get(); }
    public int getOriginalInvoiceId() { return originalInvoiceId.get(); }
    public String getOriginalInvoiceNumber() { return originalInvoiceNumber.get(); }
    public int getSupplierId() { return supplierId.get(); }
    public String getSupplierName() { return supplierName.get(); }
    public String getReturnDate() { return returnDate.get(); }
    public double getTotalReturnAmount() { return totalReturnAmount.get(); }
    public ObservableList<RawStockPurchaseItem> getReturnItems() { return returnItems; }

    // Value setters
    public void setInvoiceId(int invoiceId) { this.invoiceId.set(invoiceId); }
    public void setReturnInvoiceNumber(String returnInvoiceNumber) { this.returnInvoiceNumber.set(returnInvoiceNumber); }
    public void setOriginalInvoiceId(int originalInvoiceId) { this.originalInvoiceId.set(originalInvoiceId); }
    public void setOriginalInvoiceNumber(String originalInvoiceNumber) { this.originalInvoiceNumber.set(originalInvoiceNumber); }
    public void setSupplierId(int supplierId) { this.supplierId.set(supplierId); }
    public void setSupplierName(String supplierName) { this.supplierName.set(supplierName); }
    public void setReturnDate(String returnDate) { this.returnDate.set(returnDate); }
    public void setTotalReturnAmount(double totalReturnAmount) { this.totalReturnAmount.set(totalReturnAmount); }

    // Return items management
    public void addReturnItem(RawStockPurchaseItem item) {
        returnItems.add(item);
        updateTotalAmount();
    }

    public void removeReturnItem(RawStockPurchaseItem item) {
        returnItems.remove(item);
        updateTotalAmount();
    }

    public void clearReturnItems() {
        returnItems.clear();
        updateTotalAmount();
    }

    private void updateTotalAmount() {
        double total = returnItems.stream()
                .mapToDouble(RawStockPurchaseItem::getTotalPrice)
                .sum();
        setTotalReturnAmount(total);
    }

    @Override
    public String toString() {
        return String.format("%s - %s (Original: %s) - %.2f", 
                returnInvoiceNumber.get(), supplierName.get(), 
                originalInvoiceNumber.get(), totalReturnAmount.get());
    }
}
