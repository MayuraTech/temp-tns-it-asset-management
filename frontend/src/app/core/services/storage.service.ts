import { Injectable } from '@angular/core';

/**
 * Storage Service for secure token management
 * 
 * Handles persistent and session token storage with expiration management.
 * 
 * Requirements: 11.2, 11.3, 11.4
 */
@Injectable({
  providedIn: 'root'
})
export class StorageService {
  private readonly TOKEN_EXPIRATION_DAYS = 30; // Requirement 11.4

  /**
   * Store token with optional persistence
   * 
   * Requirements:
   * - 11.2: Store persistent Session_Token when Remember Me is checked
   * - 11.3: Auto-authenticate and redirect if token valid
   * 
   * @param key Storage key
   * @param value Value to store
   * @param persistent Whether to use localStorage (true) or sessionStorage (false)
   */
  setItem(key: string, value: string, persistent: boolean = false): void {
    const storage = persistent ? localStorage : sessionStorage;
    
    if (persistent) {
      // Store expiration timestamp for persistent tokens (Requirement 11.4)
      const expirationDate = new Date();
      expirationDate.setDate(expirationDate.getDate() + this.TOKEN_EXPIRATION_DAYS);
      storage.setItem(`${key}_expiration`, expirationDate.toISOString());
    }
    
    storage.setItem(key, value);
  }

  /**
   * Retrieve token from storage
   * 
   * Checks both localStorage and sessionStorage.
   * For persistent tokens, validates expiration.
   * 
   * Requirements:
   * - 11.3: Check for valid persistent token on load
   * - 11.4: Handle token expiration (30 days)
   * 
   * @param key Storage key
   * @returns Value if found and valid, null otherwise
   */
  getItem(key: string): string | null {
    // Check localStorage first (persistent tokens)
    const persistentValue = localStorage.getItem(key);
    if (persistentValue) {
      // Check if token has expired (Requirement 11.4)
      const expirationStr = localStorage.getItem(`${key}_expiration`);
      if (expirationStr) {
        const expirationDate = new Date(expirationStr);
        const now = new Date();
        
        if (now > expirationDate) {
          // Token expired, remove it
          this.removeItem(key);
          return null;
        }
      }
      return persistentValue;
    }
    
    // Check sessionStorage (non-persistent tokens)
    return sessionStorage.getItem(key);
  }

  /**
   * Remove token from storage
   * 
   * Removes from both localStorage and sessionStorage.
   * 
   * @param key Storage key
   */
  removeItem(key: string): void {
    localStorage.removeItem(key);
    localStorage.removeItem(`${key}_expiration`);
    sessionStorage.removeItem(key);
  }

  /**
   * Check if a token exists and is valid
   * 
   * Requirements:
   * - 11.3: Check for valid persistent token on load
   * - 11.4: Require re-authentication if token expired
   * 
   * @param key Storage key
   * @returns True if token exists and is valid, false otherwise
   */
  hasValidToken(key: string): boolean {
    return this.getItem(key) !== null;
  }

  /**
   * Check if token is stored persistently
   * 
   * @param key Storage key
   * @returns True if token is in localStorage, false otherwise
   */
  isPersistent(key: string): boolean {
    return localStorage.getItem(key) !== null;
  }

  /**
   * Clear all stored tokens
   * 
   * Removes all authentication-related data from both storages.
   */
  clear(): void {
    const keysToRemove = [
      'access_token',
      'refresh_token',
      'token_type',
      'current_user',
      'session_persistent'
    ];
    
    keysToRemove.forEach(key => {
      this.removeItem(key);
    });
  }

  /**
   * Get token expiration date
   * 
   * @param key Storage key
   * @returns Expiration date if token is persistent, null otherwise
   */
  getTokenExpiration(key: string): Date | null {
    const expirationStr = localStorage.getItem(`${key}_expiration`);
    return expirationStr ? new Date(expirationStr) : null;
  }

  /**
   * Check if token will expire soon (within 24 hours)
   * 
   * @param key Storage key
   * @returns True if token expires within 24 hours, false otherwise
   */
  isTokenExpiringSoon(key: string): boolean {
    const expiration = this.getTokenExpiration(key);
    if (!expiration) {
      return false;
    }
    
    const now = new Date();
    const hoursUntilExpiration = (expiration.getTime() - now.getTime()) / (1000 * 60 * 60);
    
    return hoursUntilExpiration <= 24;
  }
}
