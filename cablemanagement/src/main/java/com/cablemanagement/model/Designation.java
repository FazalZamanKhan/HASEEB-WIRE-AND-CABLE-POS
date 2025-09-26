package com.cablemanagement.model;

public class Designation {
    private int designationId;
    private String designationTitle;
    
    // Constructor
    public Designation() {}
    
    public Designation(int designationId, String designationTitle) {
        this.designationId = designationId;
        this.designationTitle = designationTitle;
    }
    
    public Designation(String designationTitle) {
        this.designationTitle = designationTitle;
    }
    
    // Getters and Setters
    public int getDesignationId() {
        return designationId;
    }
    
    public void setDesignationId(int designationId) {
        this.designationId = designationId;
    }
    
    public String getDesignationTitle() {
        return designationTitle;
    }
    
    public void setDesignationTitle(String designationTitle) {
        this.designationTitle = designationTitle;
    }
    
    @Override
    public String toString() {
        return designationTitle;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Designation that = (Designation) obj;
        return designationId == that.designationId;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(designationId);
    }
}
