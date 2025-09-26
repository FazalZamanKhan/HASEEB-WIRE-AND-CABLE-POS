package com.cablemanagement.invoice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceData {
    public static final String TYPE_PURCHASE = "purchase";
    public static final String TYPE_PURCHASE_RETURN = "purchase_return";
    public static final String TYPE_RAW_STOCK = "raw_stock";
    public static final String TYPE_PRODUCTION = "production";
    public static final String TYPE_PRODUCTION_RETURN = "production_return";
    public static final String TYPE_SALE = "sale";
    public static final String TYPE_SALE_RETURN = "sale_return";

    private String invoiceID;
    private String date;
    private String type;
    private String entityName;  // Customer or Supplier name
    private String entityAddress;  // Customer or Supplier address
    private String originalInvoiceNumber;  // For return invoices
    private String operator;
    private List<Item> items;
    private double previousBalance;
    private double totalBalance;  // previousBalance + current invoice total
    private double netBalance;    // totalBalance - paid amount
    private double discountAmount; // Item-level discounts only
    private double otherDiscountAmount; // Payment information discount
    private double paidAmount;
    
    // Added for more flexibility - stores additional fields without changing core structure
    private Map<String, Object> metadata;

    // Original constructor for backward compatibility
    public InvoiceData(String invoiceID, String customerName, String customerAddress, String date, List<Item> items, double previousBalance) {
        this(TYPE_SALE, invoiceID, date, customerName, customerAddress, items, previousBalance);
    }

    // Original constructor overload for backward compatibility
    public InvoiceData(String invoiceNumber, String date, String customerName, String customerAddress, 
                      double previousBalance, List<Item> items) {
        this(TYPE_SALE, invoiceNumber, date, customerName, customerAddress, items, previousBalance);
    }

    // New constructor with type specification
    public InvoiceData(String type, String invoiceID, String date, String entityName, 
                      String entityAddress, List<Item> items, double previousBalance) {
        this.type = type;
        this.invoiceID = invoiceID;
        this.date = date;
        this.entityName = entityName;
        this.entityAddress = entityAddress;
        this.items = items != null ? items : new ArrayList<>();
        this.previousBalance = previousBalance;
        this.operator = "admin";  // Default value
        this.metadata = new HashMap<>();  // Initialize empty metadata map
    }
    
    // Methods for working with metadata
    public void setMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }
    
    public Object getMetadata(String key, Object defaultValue) {
        if (metadata != null && metadata.containsKey(key)) {
            return metadata.get(key);
        }
        return defaultValue;
    }
    
    public boolean hasMetadata(String key) {
        return metadata != null && metadata.containsKey(key);
    }
    
    public Map<String, Object> getAllMetadata() {
        return metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }

    public String getType() {
        return type;
    }

    public String getInvoiceNumber() {
        return invoiceID;
    }

    public String getDate() {
        return date;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getEntityAddress() {
        return entityAddress;
    }

    public String getOriginalInvoiceNumber() {
        return originalInvoiceNumber;
    }

    public void setOriginalInvoiceNumber(String originalInvoiceNumber) {
        this.originalInvoiceNumber = originalInvoiceNumber;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public List<Item> getItems() {
        return items;
    }

    public double getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(double previousBalance) {
        this.previousBalance = previousBalance;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
    }

    public double getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(double netBalance) {
        this.netBalance = netBalance;
    }

    /**
     * Set all balance values at once
     * @param previousBalance Balance before this invoice
     * @param totalBalance Previous balance + current invoice total
     * @param netBalance Total balance - paid amount
     */
    public void setBalanceDetails(double previousBalance, double totalBalance, double netBalance) {
        this.previousBalance = previousBalance;
        this.totalBalance = totalBalance;
        this.netBalance = netBalance;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setOtherDiscountAmount(double otherDiscountAmount) {
        this.otherDiscountAmount = otherDiscountAmount;
    }

    public double getOtherDiscountAmount() {
        return otherDiscountAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    // Helper methods for type checking
    public boolean isSalesInvoice() {
        return TYPE_SALE.equals(type);
    }

    public boolean isPurchaseInvoice() {
        return TYPE_PURCHASE.equals(type);
    }

    public boolean isReturnInvoice() {
        return type != null && type.contains("return");
    }

    public boolean isProductionInvoice() {
        return TYPE_PRODUCTION.equals(type);
    }

    public boolean isRawStockInvoice() {
        return TYPE_RAW_STOCK.equals(type);
    }

    // Backward compatibility methods
    public String getCustomerName() {
        return type.toLowerCase().contains("sale") ? entityName : null;
    }

    public String getCustomerAddress() {
        return type.toLowerCase().contains("sale") ? entityAddress : null;
    }

    // Factory methods for different invoice types
    public static InvoiceData createSalesInvoice(String invoiceNumber, String date, String customerName, 
                                               String customerAddress, List<Item> items, double previousBalance) {
        return new InvoiceData(TYPE_SALE, invoiceNumber, date, customerName, customerAddress, items, previousBalance);
    }

    public static InvoiceData createPurchaseInvoice(String invoiceNumber, String date, String supplierName, 
                                                  String supplierAddress, List<Item> items, double previousBalance) {
        return new InvoiceData(TYPE_PURCHASE, invoiceNumber, date, supplierName, supplierAddress, items, previousBalance);
    }

    public static InvoiceData createReturnSalesInvoice(String invoiceNumber, String date, String customerName, 
                                                     String customerAddress, List<Item> items, double previousBalance) {
        InvoiceData data = new InvoiceData(TYPE_SALE_RETURN, invoiceNumber, date, customerName, customerAddress, items, previousBalance);
        data.setOriginalInvoiceNumber(invoiceNumber); // Can be updated later if different
        return data;
    }

    public static InvoiceData createReturnPurchaseInvoice(String invoiceNumber, String date, String supplierName, 
                                                        String supplierAddress, List<Item> items, double previousBalance) {
        InvoiceData data = new InvoiceData(TYPE_PURCHASE_RETURN, invoiceNumber, date, supplierName, supplierAddress, items, previousBalance);
        data.setOriginalInvoiceNumber(invoiceNumber); // Can be updated later if different
        return data;
    }
}
