package com.company.assetmanagement.exception;

import java.time.LocalDateTime;

/**
 * Exception thrown when a user account is locked due to multiple failed login attempts.
 */
public class AccountLockedException extends RuntimeException {
    
    private final String username;
    private final LocalDateTime lockUntil;
    
    public AccountLockedException(String username, LocalDateTime lockUntil) {
        super("Account locked due to multiple failed attempts. Please try again later.");
        this.username = username;
        this.lockUntil = lockUntil;
    }
    
    public String getUsername() {
        return username;
    }
    
    public LocalDateTime getLockUntil() {
        return lockUntil;
    }
}
