package com.company.assetmanagement.controller;

import com.company.assetmanagement.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Example controller demonstrating exception handling.
 * This controller shows how different exceptions are thrown and handled by GlobalExceptionHandler.
 * 
 * NOTE: This is for demonstration purposes only and should be removed in production.
 */
@RestController
@RequestMapping("/api/v1/examples")
public class ExampleController {
    
    /**
     * Example endpoint that throws ValidationException
     */
    @GetMapping("/validation-error")
    public ResponseEntity<String> validationError() {
        List<ValidationException.ValidationError> errors = List.of(
            new ValidationException.ValidationError("name", "Name is required"),
            new ValidationException.ValidationError("serialNumber", "Serial number must be at least 5 characters", "ABC")
        );
        throw new ValidationException(errors);
    }
    
    /**
     * Example endpoint that throws DuplicateSerialNumberException
     */
    @GetMapping("/duplicate-serial")
    public ResponseEntity<String> duplicateSerial() {
        throw new DuplicateSerialNumberException("SRV-001");
    }
    
    /**
     * Example endpoint that throws InsufficientPermissionsException
     */
    @GetMapping("/insufficient-permissions")
    public ResponseEntity<String> insufficientPermissions() {
        throw new InsufficientPermissionsException("user-123", "CREATE_ASSET");
    }
    
    /**
     * Example endpoint that throws ResourceNotFoundException
     */
    @GetMapping("/not-found")
    public ResponseEntity<String> notFound() {
        throw new ResourceNotFoundException("Asset", "550e8400-e29b-41d4-a716-446655440000");
    }
    
    /**
     * Example endpoint that throws InvalidStatusTransitionException
     */
    @GetMapping("/invalid-transition")
    public ResponseEntity<String> invalidTransition() {
        throw new InvalidStatusTransitionException("retired", "in_use", "Asset");
    }
    
    /**
     * Example endpoint that throws generic Exception
     */
    @GetMapping("/server-error")
    public ResponseEntity<String> serverError() {
        throw new RuntimeException("Unexpected database error");
    }
}
