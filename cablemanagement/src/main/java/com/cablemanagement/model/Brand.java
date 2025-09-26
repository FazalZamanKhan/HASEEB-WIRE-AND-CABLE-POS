package com.cablemanagement.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Brand {
    private final StringProperty name;
    private final StringProperty province;
    private final StringProperty district;
    private final StringProperty tehsil;

    public Brand(String name, String province, String district, String tehsil) {
        this.name = new SimpleStringProperty(name);
        this.province = new SimpleStringProperty(province);
        this.district = new SimpleStringProperty(district);
        this.tehsil = new SimpleStringProperty(tehsil);
    }

    public StringProperty nameProperty() { return name; }
    public StringProperty provinceProperty() { return province; }
    public StringProperty districtProperty() { return district; }
    public StringProperty tehsilProperty() { return tehsil; }
}
