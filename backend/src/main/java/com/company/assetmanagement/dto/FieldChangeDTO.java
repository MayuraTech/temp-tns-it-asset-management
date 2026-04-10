package com.company.assetmanagement.dto;

/**
 * Data Transfer Object representing a field change in an audit event.
 * Captures the before and after values of a field modification.
 */
public class FieldChangeDTO {
    
    private String field;
    private Object oldValue;
    private Object newValue;
    
    // Constructors
    public FieldChangeDTO() {
    }
    
    public FieldChangeDTO(String field, Object oldValue, Object newValue) {
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    // Getters and Setters
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }
    
    public Object getOldValue() {
        return oldValue;
    }
    
    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }
    
    public Object getNewValue() {
        return newValue;
    }
    
    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }
}
