package com.cablemanagement.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Customer {
    private final IntegerProperty customerId = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty contact = new SimpleStringProperty();
    private final StringProperty tehsil = new SimpleStringProperty();
    private final DoubleProperty balance = new SimpleDoubleProperty();

    public Customer(String name, String contact) {
        this.customerId.set(-1); // -1 indicates new customer without ID
        this.name.set(name);
        this.contact.set(contact);
        this.tehsil.set("");
        this.balance.set(0.0);
    }

    public Customer(String name, String contact, String tehsil) {
        this.customerId.set(-1); // -1 indicates new customer without ID
        this.name.set(name);
        this.contact.set(contact);
        this.tehsil.set(tehsil);
        this.balance.set(0.0);
    }

    public Customer(String name, String contact, String tehsil, double balance) {
        this.customerId.set(-1); // -1 indicates new customer without ID
        this.name.set(name);
        this.contact.set(contact);
        this.tehsil.set(tehsil);
        this.balance.set(balance);
    }

    public Customer(int customerId, String name, String contact, String tehsil, double balance) {
        this.customerId.set(customerId);
        this.name.set(name);
        this.contact.set(contact);
        this.tehsil.set(tehsil);
        this.balance.set(balance);
    }

    public IntegerProperty customerIdProperty() {
        return customerId;
    }

    public int getCustomerId() {
        return customerId.get();
    }

    public void setCustomerId(int customerId) {
        this.customerId.set(customerId);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty contactProperty() {
        return contact;
    }

    public String getContact() {
        return contact.get();
    }

    public void setContact(String contact) {
        this.contact.set(contact);
    }

    public StringProperty tehsilProperty() {
        return tehsil;
    }

    public String getTehsil() {
        return tehsil.get();
    }

    public void setTehsil(String tehsil) {
        this.tehsil.set(tehsil);
    }

    public DoubleProperty balanceProperty() {
        return balance;
    }

    public double getBalance() {
        return balance.get();
    }

    public void setBalance(double balance) {
        this.balance.set(balance);
    }
}
