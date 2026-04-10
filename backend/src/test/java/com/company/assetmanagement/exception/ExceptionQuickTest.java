package com.company.assetmanagement.exception;

import java.time.LocalDateTime;

/**
 * Quick test to verify exception classes work correctly.
 * This is a simple main method test that doesn't require Maven.
 */
public class ExceptionQuickTest {
    
    public static void main(String[] args) {
        System.out.println("Testing User Management Exceptions...");
        
        // Test AccountLockedException
        LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(30);
        AccountLockedException lockedException = new AccountLockedException(lockUntil);
        System.out.println("✓ AccountLockedException: " + lockedException.getMessage());
        System.out.println("  Lock until: " + lockedException.getLockUntil());
        
        // Test AccountDisabledException
        AccountDisabledException disabledException = new AccountDisabledException("user-123");
        System.out.println("✓ AccountDisabledException: " + disabledException.getMessage());
        System.out.println("  User ID: " + disabledException.getUserId());
        
        // Test DuplicateUsernameException
        DuplicateUsernameException usernameException = new DuplicateUsernameException("testuser");
        System.out.println("✓ DuplicateUsernameException: " + usernameException.getMessage());
        System.out.println("  Username: " + usernameException.getUsername());
        
        // Test DuplicateEmailException
        DuplicateEmailException emailException = new DuplicateEmailException("test@example.com");
        System.out.println("✓ DuplicateEmailException: " + emailException.getMessage());
        System.out.println("  Email: " + emailException.getEmail());
        
        // Test UserNotFoundException
        UserNotFoundException userNotFoundException = new UserNotFoundException("user-123");
        System.out.println("✓ UserNotFoundException: " + userNotFoundException.getMessage());
        System.out.println("  User ID: " + userNotFoundException.getUserId());
        
        System.out.println("\nAll exception classes created successfully!");
    }
}