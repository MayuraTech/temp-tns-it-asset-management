package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.AuditEventDTO;
import com.company.assetmanagement.dto.ChangePasswordRequest;
import com.company.assetmanagement.dto.FieldChangeDTO;
import com.company.assetmanagement.dto.ProfileUpdateRequest;
import com.company.assetmanagement.dto.UserDTO;
import com.company.assetmanagement.exception.DuplicateEmailException;
import com.company.assetmanagement.exception.UserNotFoundException;
import com.company.assetmanagement.exception.ValidationException;
import com.company.assetmanagement.model.Action;
import com.company.assetmanagement.model.Role;
import com.company.assetmanagement.model.Session;
import com.company.assetmanagement.model.User;
import com.company.assetmanagement.repository.SessionRepository;
import com.company.assetmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of ProfileService for user profile self-service operations.
 * 
 * This service allows users to view and update their own profile information
 * without administrator privileges. It enforces security constraints and
 * validates all inputs before making changes.
 * 
 * Key Features:
 * - Profile retrieval with password hash exclusion
 * - Email updates with uniqueness validation
 * - Password changes with current password verification
 * - Session invalidation after password changes
 * - Comprehensive audit logging
 * 
 * Security Measures:
 * - Password hashes never exposed in responses
 * - Current password verification before changes
 * - BCrypt password hashing with strength 10
 * - Session invalidation on password change
 * - Email uniqueness enforcement (case-insensitive)
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
@Service
@Transactional
public class ProfileServiceImpl implements ProfileService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfileServiceImpl.class);
    
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    
    /**
     * Constructor with dependency injection.
     *
     * @param userRepository the user repository
     * @param sessionRepository the session repository
     * @param passwordEncoder the password encoder for BCrypt hashing
     * @param auditService the audit service for logging events
     */
    public ProfileServiceImpl(UserRepository userRepository,
                            SessionRepository sessionRepository,
                            PasswordEncoder passwordEncoder,
                            AuditService auditService) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UserDTO getProfile(String userId) {
        logger.debug("Retrieving profile for user: {}", userId);
        
        // 1. Verify user exists
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        // 2. Map user entity to UserDTO (password hash is excluded)
        UserDTO userDTO = mapToDTO(user);
        
        logger.info("Profile retrieved successfully for user: {}", userId);
        return userDTO;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public UserDTO updateProfile(String userId, ProfileUpdateRequest request) {
        logger.debug("Updating profile for user: {}", userId);
        
        // 1. Verify user exists
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        Map<String, FieldChangeDTO> changes = new HashMap<>();
        
        // 2. Validate and update email if provided
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim();
            
            // Check if email is actually changing
            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
                // 3. Check email uniqueness (case-insensitive)
                if (userRepository.existsByEmailIgnoreCaseAndIdNot(newEmail, user.getId())) {
                    logger.warn("Attempted to update profile with duplicate email: {}", newEmail);
                    throw new DuplicateEmailException(newEmail);
                }
                
                // Record the change for audit logging
                changes.put("email", new FieldChangeDTO("email", user.getEmail(), newEmail));
                
                // Update email
                user.setEmail(newEmail);
                logger.debug("Email updated for user: {}", userId);
            }
        }
        
        // 4. Record update timestamp (handled by @LastModifiedDate)
        // 5. Save user
        User updatedUser = userRepository.save(user);
        
        // 6. Log the profile update event via AuditService
        if (!changes.isEmpty()) {
            AuditEventDTO auditEvent = new AuditEventDTO();
            auditEvent.setUserId(UUID.fromString(userId));
            auditEvent.setActionType(Action.UPDATE);
            auditEvent.setResourceType("USER_PROFILE");
            auditEvent.setResourceId(userId);
            auditEvent.setChanges(changes);
            auditEvent.setTimestamp(LocalDateTime.now());
            
            auditService.logEvent(auditEvent);
            logger.info("Profile updated successfully for user: {}", userId);
        } else {
            logger.debug("No changes detected in profile update for user: {}", userId);
        }
        
        // 7. Return updated UserDTO with password hash excluded
        return mapToDTO(updatedUser);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void changePassword(String userId, ChangePasswordRequest request) {
        logger.debug("Changing password for user: {}", userId);
        
        // 1. Verify user exists
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        // 2. Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            logger.warn("Failed password change attempt for user: {} - incorrect current password", userId);
            throw new ValidationException("Current password is incorrect");
        }
        
        // 3. Validate new password is different from current
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            logger.warn("Failed password change attempt for user: {} - new password same as current", userId);
            throw new ValidationException("New password must be different from current password");
        }
        
        // 4. Hash new password with BCrypt (strength 10)
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);
        
        // 5. Save user with new password
        userRepository.save(user);
        
        // 6. Invalidate all active sessions for the user
        List<Session> activeSessions = sessionRepository.findActiveSessionsByUserId(user.getId());
        for (Session session : activeSessions) {
            session.setIsActive(false);
            session.setLogoutAt(LocalDateTime.now());
        }
        sessionRepository.saveAll(activeSessions);
        
        logger.info("Invalidated {} active sessions for user: {}", activeSessions.size(), userId);
        
        // 7. Log the password change event via AuditService (without password values)
        AuditEventDTO auditEvent = new AuditEventDTO();
        auditEvent.setUserId(UUID.fromString(userId));
        auditEvent.setActionType(Action.UPDATE);
        auditEvent.setResourceType("USER_PASSWORD");
        auditEvent.setResourceId(userId);
        auditEvent.setTimestamp(LocalDateTime.now());
        
        auditService.logEvent(auditEvent);
        
        logger.info("Password changed successfully for user: {}", userId);
    }
    
    /**
     * Maps a User entity to a UserDTO.
     * Ensures password hash is excluded from the response.
     *
     * @param user the user entity
     * @return the user DTO with password hash excluded
     */
    private UserDTO mapToDTO(User user) {
        Set<Role> roles = user.getRoles().stream()
                .map(userRole -> userRole.getRole())
                .collect(Collectors.toSet());
        
        String createdByUsername = user.getCreatedBy() != null ? user.getCreatedBy().getUsername() : null;
        String updatedByUsername = user.getUpdatedBy() != null ? user.getUpdatedBy().getUsername() : null;
        
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getIsActive(),
                user.getAccountLocked(),
                user.getLockUntil(),
                user.getLastLoginAt(),
                roles,
                user.getCreatedAt(),
                user.getUpdatedAt(),
                createdByUsername,
                updatedByUsername
        );
    }
}
