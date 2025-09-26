package com.cablemanagement.model;

// Bank.java
public class Bank {
    private int bankId;
    private String bankName;
    private String branchName;
    private String accountNumber;
    private double balance;
    
    // Constructors, getters, setters
    public Bank() {
    }

    public Bank(int bankId, String bankName, String branchName, String accountNumber, double balance) {
        this.bankId = bankId;
        this.bankName = bankName;
        this.branchName = branchName;
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public int getBankId() {
        return bankId;
    }

    public void setBankId(int bankId) {
        this.bankId = bankId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }


    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}

