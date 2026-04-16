package com.company.assetmanagement.dto;

import com.company.assetmanagement.model.AssignmentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating asset assignments.
 * 
 * Contains the assignment type and target information (user or location).
 * For USER assignments, assignedUserEmail is required.
 * For LOCATION assignments, only assignedTo (location name) is required.
 */
public class AssignmentRequest {
    
    @NotNull(message = "Assignment type is required")
    private AssignmentType assignmentType;
    
    @NotBlank(message = "Assigned to is required")
    @Size(max = 255, message = "Assigned to must not exceed 255 characters")
    private String assignedTo;
    
    @Email(message = "Invalid email format")
    private String assignedUserEmail;
    
    // Constructors
    
    public AssignmentRequest() {
    }
    
    public AssignmentRequest(AssignmentType assignmentType, String assignedTo, String assignedUserEmail) {
        this.assignmentType = assignmentType;
        this.assignedTo = assignedTo;
        this.assignedUserEmail = assignedUserEmail;
    }
    
    // Getters and Setters
    
    public AssignmentType getAssignmentType() {
        return assignmentType;
    }
    
    public void setAssignmentType(AssignmentType assignmentType) {
        this.assignmentType = assignmentType;
    }
    
    public String getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public String getAssignedUserEmail() {
        return assignedUserEmail;
    }
    
    public void setAssignedUserEmail(String assignedUserEmail) {
        this.assignedUserEmail = assignedUserEmail;
    }
    
    @Override
    public String toString() {
        return "AssignmentRequest{" +
                "assignmentType=" + assignmentType +
                ", assignedTo='" + assignedTo + '\'' +
                ", assignedUserEmail='" + assignedUserEmail + '\'' +
                '}';
    }
}
