package com.company.assetmanagement.security;

import com.company.assetmanagement.model.Role;
import com.company.assetmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom UserDetailsService implementation for loading user-specific data.
 * 
 * This service:
 * - Loads user details from the database by username
 * - Converts user roles to Spring Security GrantedAuthority objects
 * - Provides user authentication information to Spring Security
 * - Supports role-based access control (RBAC)
 * 
 * The loaded UserDetails object contains:
 * - Username for identification
 * - Password hash for authentication
 * - Authorities (roles) for authorization
 * - Account status flags (enabled, locked, etc.)
 * 
 * Requirements: 1.3, 12.1, 12.2
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Loads user details by username for authentication.
     * 
     * This method:
     * 1. Queries the database for the user by username
     * 2. Loads the user's roles eagerly to avoid lazy loading issues
     * 3. Converts roles to Spring Security authorities
     * 4. Creates a UserDetails object with user information
     * 
     * Note: Account status validation (active, locked) is performed
     * separately in JwtAuthenticationFilter to provide specific error messages.
     * 
     * @param username the username to load
     * @return UserDetails object containing user authentication information
     * @throws UsernameNotFoundException if user is not found in the database
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Load user with roles from database
        com.company.assetmanagement.model.User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        // Convert user roles to Spring Security authorities
        Collection<? extends GrantedAuthority> authorities = getAuthorities(user.getRoleNames());
        
        // Create Spring Security UserDetails object
        // Note: We use 'true' for all account status flags here because
        // detailed account status validation is performed in JwtAuthenticationFilter
        // This allows us to provide specific error messages for locked/disabled accounts
        return User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)  // Validated in filter
                .credentialsExpired(false)
                .disabled(false)  // Validated in filter
                .build();
    }
    
    /**
     * Converts user roles to Spring Security GrantedAuthority objects.
     * 
     * Roles are prefixed with "ROLE_" as per Spring Security convention.
     * For example: Role.ADMINISTRATOR becomes "ROLE_ADMINISTRATOR"
     * 
     * @param roles the set of user roles
     * @return collection of GrantedAuthority objects
     */
    private Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
    }
}
