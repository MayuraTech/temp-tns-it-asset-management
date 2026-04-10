package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.AssetRequest;
import com.company.assetmanagement.dto.ValidationError;
import com.company.assetmanagement.exception.InvalidStatusTransitionException;
import com.company.assetmanagement.exception.ValidationException;
import com.company.assetmanagement.model.LifecycleStatus;
import com.company.assetmanagement.util.AppConstants;
import com.company.assetmanagement.util.ErrorMessages;
import com.company.assetmanagement.util.ValidationUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for validating asset data and business rules.
 * Implements comprehensive validation logic for asset creation and updates.
 * 
 * Requirement 6: Asset Data Validation
 */
@Service
public class AssetValidationService {
    
    /**
     * Validates an asset request for creation or update.
     * Collects all validation errors and throws ValidationException if any errors exist.
     * 
     * @param request the asset request to validate
     * @throws ValidationException if validation fails with all validation errors
     */
    public void validateAssetRequest(AssetRequest request) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Validate required fields
        validateRequiredFields(request, errors);
        
        // Validate field lengths
        validateFieldLengths(request, errors);
        
        // Validate acquisition date
        validateAcquisitionDate(request, errors);
        
        // Validate email format
        validateEmailFormat(request, errors);
        
        // If there are any validation errors, throw ValidationException
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
    
    /**
     * Validates required fields are present and non-empty.
     * Requirement 6.1: Validate all required fields are present and non-empty
     * 
     * @param request the asset request
     * @param errors list to collect validation errors
     */
    private void validateRequiredFields(AssetRequest request, List<ValidationError> errors) {
        // Asset type is required
        if (request.getAssetType() == null) {
            errors.add(new ValidationError("assetType", ErrorMessages.ASSET_TYPE_REQUIRED));
        }
        
        // Name is required
        if (ValidationUtil.isNullOrEmpty(request.getName())) {
            errors.add(new ValidationError("name", ErrorMessages.NAME_REQUIRED));
        }
        
        // Serial number is required
        if (ValidationUtil.isNullOrEmpty(request.getSerialNumber())) {
            errors.add(new ValidationError("serialNumber", ErrorMessages.SERIAL_NUMBER_REQUIRED));
        }
        
        // Acquisition date is required
        if (request.getAcquisitionDate() == null) {
            errors.add(new ValidationError("acquisitionDate", ErrorMessages.ACQUISITION_DATE_REQUIRED));
        }
        
        // Status is required
        if (request.getStatus() == null) {
            errors.add(new ValidationError("status", ErrorMessages.STATUS_REQUIRED));
        }
    }
    
    /**
     * Validates field lengths are within acceptable ranges.
     * Requirement 6.3: Validate name length is between 1 and 255 characters
     * Requirement 6.4: Validate serialNumber length is between 5 and 100 characters
     * Requirement 6.8: Validate location length does not exceed 255 characters
     * 
     * @param request the asset request
     * @param errors list to collect validation errors
     */
    private void validateFieldLengths(AssetRequest request, List<ValidationError> errors) {
        // Validate name length (1-255 characters)
        if (request.getName() != null) {
            int nameLength = request.getName().length();
            if (nameLength < 1) {
                errors.add(new ValidationError("name", 
                    ErrorMessages.fieldTooShort("Name", 1), 
                    request.getName()));
            } else if (nameLength > AppConstants.MAX_ASSET_NAME_LENGTH) {
                errors.add(new ValidationError("name", 
                    ErrorMessages.NAME_TOO_LONG, 
                    request.getName()));
            }
        }
        
        // Validate serial number length (5-100 characters)
        if (request.getSerialNumber() != null) {
            int serialLength = request.getSerialNumber().length();
            if (serialLength < AppConstants.MIN_SERIAL_NUMBER_LENGTH) {
                errors.add(new ValidationError("serialNumber", 
                    ErrorMessages.SERIAL_NUMBER_TOO_SHORT, 
                    request.getSerialNumber()));
            } else if (serialLength > AppConstants.MAX_SERIAL_NUMBER_LENGTH) {
                errors.add(new ValidationError("serialNumber", 
                    ErrorMessages.SERIAL_NUMBER_TOO_LONG, 
                    request.getSerialNumber()));
            }
        }
        
        // Validate location length (max 255 characters)
        if (request.getLocation() != null && 
            request.getLocation().length() > AppConstants.MAX_LOCATION_LENGTH) {
            errors.add(new ValidationError("location", 
                ErrorMessages.LOCATION_TOO_LONG, 
                request.getLocation()));
        }
    }
    
    /**
     * Validates acquisition date is not in the future.
     * Requirement 6.5: Validate acquisitionDate is not in the future
     * 
     * @param request the asset request
     * @param errors list to collect validation errors
     */
    private void validateAcquisitionDate(AssetRequest request, List<ValidationError> errors) {
        if (request.getAcquisitionDate() != null) {
            LocalDate today = LocalDate.now();
            if (request.getAcquisitionDate().isAfter(today)) {
                errors.add(new ValidationError("acquisitionDate", 
                    ErrorMessages.ACQUISITION_DATE_FUTURE, 
                    request.getAcquisitionDate()));
            }
        }
    }
    
    /**
     * Validates email format if provided.
     * Requirement 6.7: Validate assignedUserEmail matches standard email format (if provided)
     * 
     * @param request the asset request
     * @param errors list to collect validation errors
     */
    private void validateEmailFormat(AssetRequest request, List<ValidationError> errors) {
        if (ValidationUtil.isNotNullOrEmpty(request.getAssignedUserEmail())) {
            if (!ValidationUtil.isValidEmail(request.getAssignedUserEmail())) {
                errors.add(new ValidationError("assignedUserEmail", 
                    ErrorMessages.EMAIL_INVALID, 
                    request.getAssignedUserEmail()));
            }
        }
    }
    
    /**
     * Validates a status transition is allowed according to lifecycle rules.
     * Requirement 4.3: Enforce valid status transitions
     * 
     * Valid transitions:
     * - ORDERED → RECEIVED
     * - RECEIVED → DEPLOYED
     * - DEPLOYED → IN_USE or STORAGE
     * - IN_USE → STORAGE or RETIRED
     * - STORAGE → DEPLOYED or RETIRED
     * - Any status → MAINTENANCE
     * - MAINTENANCE → Any status (except RETIRED)
     * - RETIRED → No transitions allowed
     * 
     * @param currentStatus the current lifecycle status
     * @param newStatus the target lifecycle status
     * @throws InvalidStatusTransitionException if the transition is not allowed
     */
    public void validateStatusTransition(LifecycleStatus currentStatus, LifecycleStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            throw new IllegalArgumentException("Current status and new status cannot be null");
        }
        
        // Use the canTransitionTo method from LifecycleStatus enum
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                currentStatus.name(), 
                newStatus.name(), 
                "Asset"
            );
        }
    }
}
