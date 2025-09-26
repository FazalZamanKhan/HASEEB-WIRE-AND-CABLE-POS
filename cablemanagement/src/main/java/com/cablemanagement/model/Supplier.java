package com.cablemanagement.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Supplier {
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty contact = new SimpleStringProperty();
    private final StringProperty tehsil = new SimpleStringProperty();
    private final DoubleProperty balance = new SimpleDoubleProperty();

    public Supplier(String name, String contact) {
        this.name.set(name);
        this.contact.set(contact);
        this.tehsil.set("");
        this.balance.set(0.0);
    }

    public Supplier(String name, String contact, String tehsil) {
        this.name.set(name);
        this.contact.set(contact);
        this.tehsil.set(tehsil);
        this.balance.set(0.0);
    }

    public Supplier(String name, String contact, String tehsil, double balance) {
        this.name.set(name);
        this.contact.set(contact);
        this.tehsil.set(tehsil);
        this.balance.set(balance);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty contactProperty() {
        return contact;
    }

    public StringProperty tehsilProperty() {
        return tehsil;
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
