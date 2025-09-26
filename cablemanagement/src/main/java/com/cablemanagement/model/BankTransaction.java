package com.cablemanagement.model;

public class BankTransaction {
    private int transactionId;
    private int bankId; // 0 for pure cash transactions
    private String transactionDate;
    private String transactionType;
    private double amount;
    private String description;
    private int relatedBankId; // 0 if not applicable

    // Constructor
    public BankTransaction(int transactionId, int bankId, String transactionDate, 
                         String transactionType, double amount, String description, 
                         int relatedBankId) {
        this.transactionId = transactionId;
        this.bankId = bankId;
        this.transactionDate = transactionDate;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.relatedBankId = relatedBankId;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getBankId() {
        return bankId;
    }

    public void setBankId(int bankId) {
        this.bankId = bankId;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRelatedBankId() {
        return relatedBankId;
    }

    public void setRelatedBankId(int relatedBankId) {
        this.relatedBankId = relatedBankId;
    }
}