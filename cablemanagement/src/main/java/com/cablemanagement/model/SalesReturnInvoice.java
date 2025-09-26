package com.cablemanagement.model;

import javafx.beans.property.*;

public class SalesReturnInvoice {
    private final IntegerProperty id;
    private final StringProperty returnInvoiceNumber;
    private final IntegerProperty originalSalesInvoiceId;
    private final StringProperty originalInvoiceNumber;
    private final IntegerProperty customerId;
    private final StringProperty customerName;
    private final StringProperty returnDate;
    private final DoubleProperty totalReturnAmount;

    public SalesReturnInvoice(int id, String returnInvoiceNumber, int originalSalesInvoiceId, 
                             String originalInvoiceNumber, int customerId, String customerName,
                             String returnDate, double totalReturnAmount) {
        this.id = new SimpleIntegerProperty(id);
        this.returnInvoiceNumber = new SimpleStringProperty(returnInvoiceNumber);
        this.originalSalesInvoiceId = new SimpleIntegerProperty(originalSalesInvoiceId);
        this.originalInvoiceNumber = new SimpleStringProperty(originalInvoiceNumber);
        this.customerId = new SimpleIntegerProperty(customerId);
        this.customerName = new SimpleStringProperty(customerName);
        this.returnDate = new SimpleStringProperty(returnDate);
        this.totalReturnAmount = new SimpleDoubleProperty(totalReturnAmount);
    }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty returnInvoiceNumberProperty() { return returnInvoiceNumber; }
    public IntegerProperty originalSalesInvoiceIdProperty() { return originalSalesInvoiceId; }
    public StringProperty originalInvoiceNumberProperty() { return originalInvoiceNumber; }
    public IntegerProperty customerIdProperty() { return customerId; }
    public StringProperty customerNameProperty() { return customerName; }
    public StringProperty returnDateProperty() { return returnDate; }
    public DoubleProperty totalReturnAmountProperty() { return totalReturnAmount; }

    // Value getters
    public int getId() { return id.get(); }
    public String getReturnInvoiceNumber() { return returnInvoiceNumber.get(); }
    public int getOriginalSalesInvoiceId() { return originalSalesInvoiceId.get(); }
    public String getOriginalInvoiceNumber() { return originalInvoiceNumber.get(); }
    public int getCustomerId() { return customerId.get(); }
    public String getCustomerName() { return customerName.get(); }
    public String getReturnDate() { return returnDate.get(); }
    public double getTotalReturnAmount() { return totalReturnAmount.get(); }

    // Value setters
    public void setId(int id) { this.id.set(id); }
    public void setReturnInvoiceNumber(String returnInvoiceNumber) { this.returnInvoiceNumber.set(returnInvoiceNumber); }
    public void setOriginalSalesInvoiceId(int originalSalesInvoiceId) { this.originalSalesInvoiceId.set(originalSalesInvoiceId); }
    public void setOriginalInvoiceNumber(String originalInvoiceNumber) { this.originalInvoiceNumber.set(originalInvoiceNumber); }
    public void setCustomerId(int customerId) { this.customerId.set(customerId); }
    public void setCustomerName(String customerName) { this.customerName.set(customerName); }
    public void setReturnDate(String returnDate) { this.returnDate.set(returnDate); }
    public void setTotalReturnAmount(double totalReturnAmount) { this.totalReturnAmount.set(totalReturnAmount); }
}
