package com.company.assetmanagement.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom UserDetailsService implementation.
 * This is a placeholder implementation that will be replaced with actual
 * database-backed user loading in future tasks.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO: Replace with actual database lookup in future tasks
        // This is a placeholder for testing the security configuration
        
        // For now, throw exception as no users exist yet
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
