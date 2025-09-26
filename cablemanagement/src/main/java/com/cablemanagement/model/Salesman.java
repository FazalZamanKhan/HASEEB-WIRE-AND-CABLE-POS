package com.cablemanagement.model;

public class Salesman {
    private int id;
    private String name;
    private String contact;
    private String address;
    private double commissionRate;

    public Salesman(int id, String name, String contact, String address, double commissionRate) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.address = address;
        this.commissionRate = commissionRate;
    }

    // Constructor without ID for new salesmen
    public Salesman(String name, String contact, String address, double commissionRate) {
        this(0, name, contact, address, commissionRate);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getContact() { return contact; }
    public String getAddress() { return address; }
    public double getCommissionRate() { return commissionRate; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setContact(String contact) { this.contact = contact; }
    public void setAddress(String address) { this.address = address; }
    public void setCommissionRate(double commissionRate) { this.commissionRate = commissionRate; }
}
