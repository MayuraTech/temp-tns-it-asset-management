package com.company.assetmanagement.exception;

import java.time.LocalDateTime;

/**
 * Exception thrown when attempting to authenticate with a locked user account.
 * Contains the lock expiration time to inform when the account will be unlocked.
 */
public class AccountLockedException extends RuntimeException {
    
    private final LocalDateTime lockUntil;
    
    /**
     * Creates a new AccountLockedException with the lock expiration time.
     * 
     * @param lockUntil the date and time when the account lock will expire
     */
    public AccountLockedException(LocalDateTime lockUntil) {
        super("Account is locked until " + lockUntil);
        this.lockUntil = lockUntil;
    }
    
    /**
     * Creates a new AccountLockedException with a custom message and lock expiration time.
     * 
     * @param message custom error message
     * @param lockUntil the date and time when the account lock will expire
     */
    public AccountLockedException(String message, LocalDateTime lockUntil) {
        super(message);
        this.lockUntil = lockUntil;
    }
    
    /**
     * Gets the date and time when the account lock will expire.
     * 
     * @return the lock expiration time
     */
    public LocalDateTime getLockUntil() {
        return lockUntil;
    }
}