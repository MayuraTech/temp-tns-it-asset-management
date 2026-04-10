package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.AuditEventDTO;
import com.company.assetmanagement.dto.FieldChangeDTO;
import com.company.assetmanagement.dto.UserDTO;
import com.company.assetmanagement.dto.UserRequest;
import com.company.assetmanagement.dto.UserUpdateRequest;
import com.company.assetmanagement.exception.*;
import com.company.assetmanagement.model.Action;
import com.company.assetmanagement.model.Role;
import com.company.assetmanagement.model.User;
import com.company.assetmanagement.model.UserRole;
import com.company.assetmanagement.repository.SessionRepository;
import com.company.assetmanagement.repository.UserRepository;
import com.company.assetmanagement.repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of UserService interface for user management operations.
 * 
 * This service provides comprehensive user account lifecycle management with:
 * - Authorization enforcement for all operations
 * - Input validation and uniqueness checks
 * - Password hashing with BCrypt
 * - Audit logging for all state changes
 * - Session invalidation on security-sensitive operations
 * - Business rule enforcement
 * 
 * Security Features:
 * - BCrypt password hashing with strength 10
 * - Authorization checks before all operations
 * - Session invalidation on role changes, password changes, and account disable
 * - Self-operation prevention (cannot delete/disable own account)
 * - Password hash exclusion from all responses
 * 
 * Transaction Management:
 * - All state-changing operations are transactional
 * - Rollback on any exception
 * - Consistent state maintained across related entities
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private static final int BCRYPT_STRENGTH = 10;

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final SessionRepository sessionRepository;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Constructor with dependency injection.
     *
     * @param userRepository repository for user data access
     * @param userRoleRepository repository for user role data access
     * @param sessionRepository repository for session data access
     * @param authorizationService service for authorization checks
     * @param auditService service for audit logging
     */
    public UserServiceImpl(UserRepository userRepository,
                          UserRoleRepository userRoleRepository,
                          SessionRepository sessionRepository,
                          AuthorizationService authorizationService,
                          AuditService auditService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.sessionRepository = sessionRepository;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
        this.passwordEncoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }

    @Override
    public UserDTO createUser(String creatorId, UserRequest request) {
        logger.debug("Creating user with username: {}", request.getUsername());
        
        // Validate inputs
        validateNotNull(creatorId, "Creator ID");
        validateNotNull(request, "User request");
        
        // 1. Authorization check
        if (!authorizationService.hasPermission(creatorId, Action.CREATE_USER)) {
            logger.warn("User {} attempted to create user without permission", creatorId);
            throw new InsufficientPermissionsException(creatorId, Action.CREATE_USER.name());
        }
        
        // 2. Validate request data (Bean Validation already applied via @Valid in controller)
        // Additional business validation
        validateUserRequest(request);
        
        // 3. Check username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            logger.warn("Attempt to create user with duplicate username: {}", request.getUsername());
            throw new DuplicateUsernameException(request.getUsername());
        }
        
        // 4. Check email uniqueness
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            logger.warn("Attempt to create user with duplicate email: {}", request.getEmail());
            throw new DuplicateEmailException(request.getEmail());
        }
        
        // 5. Hash password with BCrypt
        String passwordHash = passwordEncoder.encode(request.getPassword());
        
        // 6. Get creator user
        User creator = userRepository.findById(UUID.fromString(creatorId))
            .orElseThrow(() -> new UserNotFoundException(creatorId));
        
        // 7. Create user entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordHash);
        user.setIsActive(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setCreatedBy(creator);
        user.setUpdatedBy(creator);
        
        // 8. Persist user
        User savedUser = userRepository.save(user);
        
        // 9. Assign roles
        for (Role role : request.getRoles()) {
            UserRole userRole = new UserRole(savedUser, role, creator);
            userRoleRepository.save(userRole);
            savedUser.addRole(userRole);
        }
        
        // 10. Log audit event
        auditService.logEvent(AuditEventDTO.builder()
            .userId(UUID.fromString(creatorId))
            .actionType(Action.CREATE_USER)
            .resourceType("USER")
            .resourceId(savedUser.getId().toString())
            .metadata(Map.of(
                "username", savedUser.getUsername(),
                "email", savedUser.getEmail(),
                "roles", request.getRoles().stream()
                    .map(Role::getValue)
                    .collect(Collectors.joining(", "))
            ))
            .build());
        
        logger.info("User created successfully: {} by creator: {}", savedUser.getUsername(), creatorId);
        
        // 11. Return DTO
        return mapToDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUser(String userId) {
        logger.debug("Retrieving user with ID: {}", userId);
        
        validateNotNull(userId, "User ID");
        
        return userRepository.findById(UUID.fromString(userId))
            .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        logger.debug("Retrieving all users with pagination: {}", pageable);
        
        validateNotNull(pageable, "Pageable");
        
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getUsersByRole(Role role, Pageable pageable) {
        logger.debug("Retrieving users by role: {} with pagination: {}", role, pageable);
        
        validateNotNull(role, "Role");
        validateNotNull(pageable, "Pageable");
        
        Page<User> users = userRepository.findByRole(role, pageable);
        return users.map(this::mapToDTO);
    }

    @Override
    public UserDTO updateUser(String updaterId, String userId, UserUpdateRequest request) {
        logger.debug("Updating user {} by updater {}", userId, updaterId);
        
        // Validate inputs
        validateNotNull(updaterId, "Updater ID");
        validateNotNull(userId, "User ID");
        validateNotNull(request, "Update request");
        
        // 1. Authorization check
        if (!authorizationService.hasPermission(updaterId, Action.UPDATE_USER)) {
            logger.warn("User {} attempted to update user without permission", updaterId);
            throw new InsufficientPermissionsException(updaterId, Action.UPDATE_USER.name());
        }
        
        // 2. Verify user exists
        User user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        // 3. Get updater user
        User updater = userRepository.findById(UUID.fromString(updaterId))
            .orElseThrow(() -> new UserNotFoundException(updaterId));
        
        // Track changed fields for audit log
        Map<String, String> changedFields = new HashMap<>();
        
        // 4. Update username if provided
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            // Check uniqueness
            if (userRepository.existsByUsernameAndIdNot(request.getUsername(), user.getId())) {
                logger.warn("Attempt to update user with duplicate username: {}", request.getUsername());
                throw new DuplicateUsernameException(request.getUsername());
            }
            changedFields.put("username", user.getUsername() + " -> " + request.getUsername());
            user.setUsername(request.getUsername());
        }
        
        // 5. Update email if provided
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            // Check uniqueness
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(request.getEmail(), user.getId())) {
                logger.warn("Attempt to update user with duplicate email: {}", request.getEmail());
                throw new DuplicateEmailException(request.getEmail());
            }
            changedFields.put("email", user.getEmail() + " -> " + request.getEmail());
            user.setEmail(request.getEmail());
        }
        
        // 6. Set updatedBy
        user.setUpdatedBy(updater);
        
        // 7. Invalidate all active sessions if any fields changed (to refresh user data in active sessions)
        if (!changedFields.isEmpty()) {
            invalidateUserSessions(UUID.fromString(userId));
        }
        
        // 8. Persist changes
        User updatedUser = userRepository.save(user);
        
        // 9. Log audit event if any fields changed
        if (!changedFields.isEmpty()) {
            Map<String, FieldChangeDTO> changes = new HashMap<>();
            for (Map.Entry<String, String> entry : changedFields.entrySet()) {
                String[] parts = entry.getValue().split(" -> ");
                changes.put(entry.getKey(), new FieldChangeDTO(
                    entry.getKey(),
                    parts.length > 0 ? parts[0] : null,
                    parts.length > 1 ? parts[1] : null
                ));
            }
            
            auditService.logEvent(AuditEventDTO.builder()
                .userId(UUID.fromString(updaterId))
                .actionType(Action.UPDATE_USER)
                .resourceType("USER")
                .resourceId(userId)
                .changes(changes)
                .build());
            
            logger.info("User {} updated by {}: {}", userId, updaterId, changedFields);
        }
        
        // 10. Return DTO
        return mapToDTO(updatedUser);
    }

    @Override
    public void deleteUser(String deleterId, String userId) {
        logger.debug("Deleting user {} by deleter {}", userId, deleterId);
        
        // Validate inputs
        validateNotNull(deleterId, "Deleter ID");
        validateNotNull(userId, "User ID");
        
        // 1. Authorization check
        if (!authorizationService.hasPermission(deleterId, Action.DELETE_USER)) {
            logger.warn("User {} attempted to delete user without permission", deleterId);
            throw new InsufficientPermissionsException(deleterId, Action.DELETE_USER.name());
        }
        
        // 2. Verify user exists
        User user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        // 3. Validate not deleting own account
        if (deleterId.equals(userId)) {
            logger.warn("User {} attempted to delete their own account", deleterId);
            throw new ValidationException("userId", "You cannot delete your own account");
        }
        
        // 4. Invalidate all active sessions
        invalidateUserSessions(UUID.fromString(userId));
        
        // 5. Delete user (cascade deletes roles and sessions)
        userRepository.delete(user);
        
        // 6. Log audit event
        auditService.logEvent(AuditEventDTO.builder()
            .userId(UUID.fromString(deleterId))
            .actionType(Action.DELETE_USER)
            .resourceType("USER")
            .resourceId(userId)
            .metadata(Map.of(
                "username", user.getUsername(),
                "email", user.getEmail()
            ))
            .build());
        
        logger.info("User {} deleted by {}", userId, deleterId);
    }

    @Override
    public void enableUser(String adminId, String userId) {
        logger.debug("Enabling user {} by admin {}", userId, adminId);
        
        // Validate inputs
        validateNotNull(adminId, "Admin ID");
        validateNotNull(userId, "User ID");
        
        // 1. Authorization check
        if (!authorizationService.hasPermission(adminId, Action.MANAGE_USER_STATUS)) {
            logger.warn("User {} attempted to enable user without permission", adminId);
            throw new InsufficientPermissionsException(adminId, Action.MANAGE_USER_STATUS.name());
        }
        
        // 2. Verify user exists
        User user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        // 3. Get admin user
        User admin = userRepository.findById(UUID.fromString(adminId))
            .orElseThrow(() -> new UserNotFoundException(adminId));
        
        // 4. Set active status
        user.setIsActive(true);
        user.setUpdatedBy(admin);
        
        // 5. Persist changes
        userRepository.save(user);
        
        // 6. Log audit event
        auditService.logEvent(AuditEventDTO.builder()
            .userId(UUID.fromString(adminId))
            .actionType(Action.ENABLE_USER)
            .resourceType("USER")
            .resourceId(userId)
            .metadata(Map.of("username", user.getUsername()))
            .build());
        
        logger.info("User {} enabled by admin {}", userId, adminId);
    }

    @Override
    public void disableUser(String adminId, String userId) {
        logger.debug("Disabling user {} by admin {}", userId, adminId);
        
        // Validate inputs
        validateNotNull(adminId, "Admin ID");
        validateNotNull(userId, "User ID");
        
        // 1. Authorization check
        if (!authorizationService.hasPermission(adminId, Action.MANAGE_USER_STATUS)) {
            logger.warn("User {} attempted to disable user without permission", adminId);
            throw new InsufficientPermissionsException(adminId, Action.MANAGE_USER_STATUS.name());
        }
        
        // 2. Verify user exists
        User user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        // 3. Validate not disabling own account
        if (adminId.equals(userId)) {
            logger.warn("User {} attempted to disable their own account", adminId);
            throw new ValidationException("userId", "You cannot disable your own account");
        }
        
        // 4. Get admin user
        User admin = userRepository.findById(UUID.fromString(adminId))
            .orElseThrow(() -> new UserNotFoundException(adminId));
        
        // 5. Set inactive status
        user.setIsActive(false);
        user.setUpdatedBy(admin);
        
        // 6. Invalidate all active sessions
        invalidateUserSessions(UUID.fromString(userId));
        
        // 7. Persist changes
        userRepository.save(user);
        
        // 8. Log audit event
        auditService.logEvent(AuditEventDTO.builder()
            .userId(UUID.fromString(adminId))
            .actionType(Action.DISABLE_USER)
            .resourceType("USER")
            .resourceId(userId)
            .metadata(Map.of("username", user.getUsername()))
            .build());
        
        logger.info("User {} disabled by admin {}", userId, adminId);
    }

    @Override
    public void assignRole(String adminId, String userId, Role role) {
        logger.debug("Assigning role {} to user {} by admin {}", role, userId, adminId);
        
        // Validate inputs
        validateNotNull(adminId, "Admin ID");
        validateNotNull(userId, "User ID");
        validateNotNull(role, "Role");
        
        // 1. Authorization check
        if (!authorizationService.hasPermission(adminId, Action.ASSIGN_ROLE)) {
            logger.warn("User {} attempted to assign role without permission", adminId);
            throw new InsufficientPermissionsException(adminId, Action.ASSIGN_ROLE.name());
        }
        
        // 2. Verify user exists
        User user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        // 3. Get admin user
        User admin = userRepository.findById(UUID.fromString(adminId))
            .orElseThrow(() -> new UserNotFoundException(adminId));
        
        // 4. Check user does not already have the role
        if (user.hasRole(role)) {
            logger.warn("Attempt to assign role {} to user {} who already has it", role, userId);
            throw new ValidationException("role", "User already has role: " + role.getValue());
        }
        
        // 5. Create and persist UserRole
        UserRole userRole = new UserRole(user, role, admin);
        userRoleRepository.save(userRole);
        user.addRole(userRole);
        
        // 6. Invalidate all active sessions to refresh permissions
        invalidateUserSessions(UUID.fromString(userId));
        
        // 7. Log audit event
        auditService.logEvent(AuditEventDTO.builder()
            .userId(UUID.fromString(adminId))
            .actionType(Action.ASSIGN_ROLE)
            .resourceType("USER")
            .resourceId(userId)
            .metadata(Map.of(
                "username", user.getUsername(),
                "role", role.getValue()
            ))
            .build());
        
        logger.info("Role {} assigned to user {} by admin {}", role, userId, adminId);
    }

    @Override
    public void revokeRole(String adminId, String userId, Role role) {
        logger.debug("Revoking role {} from user {} by admin {}", role, userId, adminId);
        
        // Validate inputs
        validateNotNull(adminId, "Admin ID");
        validateNotNull(userId, "User ID");
        validateNotNull(role, "Role");
        
        // 1. Authorization check
        if (!authorizationService.hasPermission(adminId, Action.REVOKE_ROLE)) {
            logger.warn("User {} attempted to revoke role without permission", adminId);
            throw new InsufficientPermissionsException(adminId, Action.REVOKE_ROLE.name());
        }
        
        // 2. Verify user exists
        User user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        // 3. Validate user has the role
        if (!user.hasRole(role)) {
            logger.warn("Attempt to revoke role {} from user {} who doesn't have it", role, userId);
            throw new ValidationException("role", "User does not have role: " + role.getValue());
        }
        
        // 4. Validate user will have at least one role remaining
        if (user.getRoles().size() <= 1) {
            logger.warn("Attempt to revoke last role from user {}", userId);
            throw new ValidationException("role", "Cannot revoke last role from user. Users must have at least one role.");
        }
        
        // 5. Validate admin is not revoking their own ADMINISTRATOR role
        if (adminId.equals(userId) && role == Role.ADMINISTRATOR) {
            logger.warn("Admin {} attempted to revoke their own ADMINISTRATOR role", adminId);
            throw new ValidationException("role", "You cannot revoke your own ADMINISTRATOR role");
        }
        
        // 6. Find and remove UserRole
        UserRole userRoleToRemove = user.getRoles().stream()
            .filter(ur -> ur.getRole() == role)
            .findFirst()
            .orElseThrow(() -> new ValidationException("role", "User role not found"));
        
        user.removeRole(userRoleToRemove);
        userRoleRepository.delete(userRoleToRemove);
        
        // 7. Invalidate all active sessions to refresh permissions
        invalidateUserSessions(UUID.fromString(userId));
        
        // 8. Log audit event
        auditService.logEvent(AuditEventDTO.builder()
            .userId(UUID.fromString(adminId))
            .actionType(Action.REVOKE_ROLE)
            .resourceType("USER")
            .resourceId(userId)
            .metadata(Map.of(
                "username", user.getUsername(),
                "role", role.getValue()
            ))
            .build());
        
        logger.info("Role {} revoked from user {} by admin {}", role, userId, adminId);
    }

    // ========================================================================
    // Private Helper Methods
    // ========================================================================

    /**
     * Validates that an object is not null.
     *
     * @param obj the object to validate
     * @param fieldName the field name for error message
     * @throws IllegalArgumentException if object is null
     */
    private void validateNotNull(Object obj, String fieldName) {
        if (obj == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }

    /**
     * Validates user request data.
     * Additional business validation beyond Bean Validation.
     *
     * @param request the user request to validate
     * @throws ValidationException if validation fails
     */
    private void validateUserRequest(UserRequest request) {
        List<com.company.assetmanagement.dto.ValidationError> errors = new ArrayList<>();
        
        // Validate roles are not empty
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            errors.add(new com.company.assetmanagement.dto.ValidationError(
                "roles", "At least one role is required"));
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    /**
     * Invalidates all active sessions for a user.
     * Used when user account is disabled, deleted, or roles are changed.
     *
     * @param userId the user ID
     */
    private void invalidateUserSessions(UUID userId) {
        int invalidatedCount = sessionRepository.invalidateUserSessions(userId);
        logger.debug("Invalidated {} sessions for user {}", invalidatedCount, userId);
    }

    /**
     * Maps User entity to UserDTO.
     * Excludes password hash for security.
     *
     * @param user the user entity
     * @return user DTO
     */
    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setIsActive(user.getIsActive());
        dto.setAccountLocked(user.getAccountLocked());
        dto.setLockUntil(user.getLockUntil());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setRoles(user.getRoleNames());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setCreatedBy(user.getCreatedBy() != null ? user.getCreatedBy().getUsername() : null);
        dto.setUpdatedBy(user.getUpdatedBy() != null ? user.getUpdatedBy().getUsername() : null);
        return dto;
    }
}
