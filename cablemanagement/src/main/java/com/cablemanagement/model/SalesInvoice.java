package com.cablemanagement.model;

import javafx.beans.property.*;

public class SalesInvoice {
    private final IntegerProperty id;
    private final StringProperty invoiceNumber;
    private final IntegerProperty customerId;
    private final StringProperty customerName;
    private final StringProperty salesDate;
    private final DoubleProperty totalAmount;
    private final DoubleProperty discountAmount;
    private final DoubleProperty paidAmount;

    public SalesInvoice(int id, String invoiceNumber, int customerId, String customerName, 
                       String salesDate, double totalAmount, double discountAmount, double paidAmount) {
        this.id = new SimpleIntegerProperty(id);
        this.invoiceNumber = new SimpleStringProperty(invoiceNumber);
        this.customerId = new SimpleIntegerProperty(customerId);
        this.customerName = new SimpleStringProperty(customerName);
        this.salesDate = new SimpleStringProperty(salesDate);
        this.totalAmount = new SimpleDoubleProperty(totalAmount);
        this.discountAmount = new SimpleDoubleProperty(discountAmount);
        this.paidAmount = new SimpleDoubleProperty(paidAmount);
    }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty invoiceNumberProperty() { return invoiceNumber; }
    public IntegerProperty customerIdProperty() { return customerId; }
    public StringProperty customerNameProperty() { return customerName; }
    public StringProperty salesDateProperty() { return salesDate; }
    public DoubleProperty totalAmountProperty() { return totalAmount; }
    public DoubleProperty discountAmountProperty() { return discountAmount; }
    public DoubleProperty paidAmountProperty() { return paidAmount; }

    // Value getters
    public int getId() { return id.get(); }
    public String getInvoiceNumber() { return invoiceNumber.get(); }
    public int getCustomerId() { return customerId.get(); }
    public String getCustomerName() { return customerName.get(); }
    public String getSalesDate() { return salesDate.get(); }
    public double getTotalAmount() { return totalAmount.get(); }
    public double getDiscountAmount() { return discountAmount.get(); }
    public double getPaidAmount() { return paidAmount.get(); }

    // Value setters
    public void setId(int id) { this.id.set(id); }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber.set(invoiceNumber); }
    public void setCustomerId(int customerId) { this.customerId.set(customerId); }
    public void setCustomerName(String customerName) { this.customerName.set(customerName); }
    public void setSalesDate(String salesDate) { this.salesDate.set(salesDate); }
    public void setTotalAmount(double totalAmount) { this.totalAmount.set(totalAmount); }
    public void setDiscountAmount(double discountAmount) { this.discountAmount.set(discountAmount); }
    public void setPaidAmount(double paidAmount) { this.paidAmount.set(paidAmount); }
}
