package com.company.assetmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * UserRole entity representing the assignment of a role to a user.
 * 
 * This entity implements the many-to-many relationship between Users and Roles
 * with additional metadata about the assignment:
 * - Who assigned the role (assignedBy)
 * - When the role was assigned (assignedAt)
 * - Audit trail for role management
 * 
 * Business Rules:
 * - Each user must have at least one role
 * - A user cannot have duplicate role assignments
 * - Role assignments are tracked for audit purposes
 * - Only administrators can assign/revoke roles
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
@Entity
@Table(name = "UserRoles", 
       indexes = {
           @Index(name = "IX_UserRoles_UserId", columnList = "userId"),
           @Index(name = "IX_UserRoles_Role", columnList = "role"),
           @Index(name = "IX_UserRoles_Role_UserId", columnList = "role, userId"),
           @Index(name = "IX_UserRoles_AssignedBy", columnList = "assignedBy"),
           @Index(name = "IX_UserRoles_AssignedAt", columnList = "assignedAt")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "UQ_UserRoles_UserId_Role", columnNames = {"userId", "role"})
       })
@EntityListeners(AuditingEntityListener.class)
public class UserRole {

    /**
     * Unique identifier for the user role assignment.
     * Generated automatically using UUID strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Reference to the user who has this role.
     * Cannot be null - every role assignment must be associated with a user.
     * Lazy loading to avoid unnecessary queries.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false, referencedColumnName = "Id")
    @NotNull(message = "User is required for role assignment")
    private User user;

    /**
     * The role assigned to the user.
     * Must be one of the predefined roles: ADMINISTRATOR, ASSET_MANAGER, VIEWER.
     * Stored as string in database for flexibility.
     */
    @Column(name = "Role", nullable = false, length = 50)
    @Convert(converter = RoleJpaConverter.class)
    @NotNull(message = "Role is required")
    private Role role;

    /**
     * Reference to the user who assigned this role.
     * Used for audit trail and accountability.
     * Cannot be null - all role assignments must be traceable.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AssignedBy", nullable = false, referencedColumnName = "Id")
    @NotNull(message = "Assigner is required for role assignment")
    private User assignedBy;

    /**
     * Timestamp when the role was assigned.
     * Automatically set by Spring Data JPA auditing.
     * Immutable after creation.
     */
    @CreatedDate
    @Column(name = "AssignedAt", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * Default constructor for JPA.
     */
    public UserRole() {
    }

    /**
     * Constructor for creating a new role assignment.
     * 
     * @param user the user to assign the role to
     * @param role the role to assign
     * @param assignedBy the user performing the assignment
     */
    public UserRole(User user, Role role, User assignedBy) {
        this.user = user;
        this.role = role;
        this.assignedBy = assignedBy;
    }

    // ========================================================================
    // Getters and Setters
    // ========================================================================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public User getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(User assignedBy) {
        this.assignedBy = assignedBy;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    // ========================================================================
    // Business Logic Methods
    // ========================================================================

    /**
     * Checks if this role assignment is for an administrator role.
     * 
     * @return true if the role is ADMINISTRATOR
     */
    public boolean isAdministratorRole() {
        return role == Role.ADMINISTRATOR;
    }

    /**
     * Checks if this role assignment is for an asset manager role.
     * 
     * @return true if the role is ASSET_MANAGER
     */
    public boolean isAssetManagerRole() {
        return role == Role.ASSET_MANAGER;
    }

    /**
     * Checks if this role assignment is for a viewer role.
     * 
     * @return true if the role is VIEWER
     */
    public boolean isViewerRole() {
        return role == Role.VIEWER;
    }

    /**
     * Gets the display name of the role.
     * 
     * @return the role's display value
     */
    public String getRoleDisplayName() {
        return role != null ? role.getValue() : null;
    }

    /**
     * Checks if the role was assigned by the same user who has the role.
     * This would indicate a self-assignment, which may be restricted.
     * 
     * @return true if the user assigned the role to themselves
     */
    public boolean isSelfAssigned() {
        return user != null && assignedBy != null && 
               Objects.equals(user.getId(), assignedBy.getId());
    }

    /**
     * Gets the username of the user who has this role.
     * 
     * @return the username or null if user is not set
     */
    public String getUserUsername() {
        return user != null ? user.getUsername() : null;
    }

    /**
     * Gets the username of the user who assigned this role.
     * 
     * @return the assigner's username or null if assignedBy is not set
     */
    public String getAssignedByUsername() {
        return assignedBy != null ? assignedBy.getUsername() : null;
    }

    // ========================================================================
    // Object Methods (equals, hashCode, toString)
    // ========================================================================

    /**
     * Equals method based on user and role for business equality.
     * Two UserRole entities are considered equal if they have the same user and role.
     * This enforces the business rule that a user cannot have duplicate role assignments.
     * 
     * @param obj the object to compare
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        UserRole userRole = (UserRole) obj;
        return Objects.equals(user, userRole.user) && 
               Objects.equals(role, userRole.role);
    }

    /**
     * Hash code based on user and role for consistency with equals.
     * 
     * @return hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(user, role);
    }

    /**
     * String representation of the user role assignment.
     * Includes key information about the assignment for debugging and logging.
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return "UserRole{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : null) +
                ", role=" + role +
                ", assignedBy=" + (assignedBy != null ? assignedBy.getUsername() : null) +
                ", assignedAt=" + assignedAt +
                '}';
    }
}