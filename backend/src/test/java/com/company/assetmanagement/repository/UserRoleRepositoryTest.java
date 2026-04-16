package com.company.assetmanagement.repository;

import com.company.assetmanagement.model.Role;
import com.company.assetmanagement.model.User;
import com.company.assetmanagement.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserRoleRepository.
 * 
 * Tests all repository methods including:
 * - Role assignment and revocation operations
 * - Finder methods for roles by user and users by role
 * - Cascade delete handling
 * - Business rule validation queries
 * - Statistics and reporting queries
 * 
 * Uses @DataJpaTest for focused repository testing with in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRoleRepository Tests")
class UserRoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserRepository userRepository;

    private User adminUser;
    private User managerUser;
    private User viewerUser;
    private User assignerUser;

    @BeforeEach
    void setUp() {
        // Create test users
        adminUser = new User("admin", "$2a$10$hashedPassword", "admin@example.com");
        managerUser = new User("manager", "$2a$10$hashedPassword", "manager@example.com");
        viewerUser = new User("viewer", "$2a$10$hashedPassword", "viewer@example.com");
        assignerUser = new User("assigner", "$2a$10$hashedPassword", "assigner@example.com");

        // Persist users
        adminUser = entityManager.persistAndFlush(adminUser);
        managerUser = entityManager.persistAndFlush(managerUser);
        viewerUser = entityManager.persistAndFlush(viewerUser);
        assignerUser = entityManager.persistAndFlush(assignerUser);

        // Create role assignments
        UserRole adminRole = new UserRole(adminUser, Role.ADMINISTRATOR, assignerUser);
        UserRole managerRole = new UserRole(managerUser, Role.ASSET_MANAGER, assignerUser);
        UserRole viewerRole = new UserRole(viewerUser, Role.VIEWER, assignerUser);

        entityManager.persistAndFlush(adminRole);
        entityManager.persistAndFlush(managerRole);
        entityManager.persistAndFlush(viewerRole);

        entityManager.clear();
    }

    // ========================================================================
    // Role assignment and revocation operations tests
    // ========================================================================

    @Test
    @DisplayName("Should find role assignment by user and role")
    void shouldFindRoleAssignmentByUserAndRole() {
        // When
        Optional<UserRole> result = userRoleRepository.findByUserAndRole(adminUser, Role.ADMINISTRATOR);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUser()).isEqualTo(adminUser);
        assertThat(result.get().getRole()).isEqualTo(Role.ADMINISTRATOR);
        assertThat(result.get().getAssignedBy()).isEqualTo(assignerUser);
    }

    @Test
    @DisplayName("Should return empty when role assignment not found")
    void shouldReturnEmptyWhenRoleAssignmentNotFound() {
        // When
        Optional<UserRole> result = userRoleRepository.findByUserAndRole(adminUser, Role.VIEWER);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should check if user has specific role")
    void shouldCheckIfUserHasSpecificRole() {
        // When & Then
        assertThat(userRoleRepository.existsByUserAndRole(adminUser, Role.ADMINISTRATOR)).isTrue();
        assertThat(userRoleRepository.existsByUserAndRole(adminUser, Role.VIEWER)).isFalse();
        assertThat(userRoleRepository.existsByUserAndRole(managerUser, Role.ASSET_MANAGER)).isTrue();
        assertThat(userRoleRepository.existsByUserAndRole(viewerUser, Role.VIEWER)).isTrue();
    }

    @Test
    @DisplayName("Should delete role assignment by user and role")
    void shouldDeleteRoleAssignmentByUserAndRole() {
        // Given
        assertThat(userRoleRepository.existsByUserAndRole(adminUser, Role.ADMINISTRATOR)).isTrue();

        // When
        int deletedCount = userRoleRepository.deleteByUserAndRole(adminUser, Role.ADMINISTRATOR);

        // Then
        assertThat(deletedCount).isEqualTo(1);
        assertThat(userRoleRepository.existsByUserAndRole(adminUser, Role.ADMINISTRATOR)).isFalse();
    }

    @Test
    @DisplayName("Should return zero when deleting non-existent role assignment")
    void shouldReturnZeroWhenDeletingNonExistentRoleAssignment() {
        // When
        int deletedCount = userRoleRepository.deleteByUserAndRole(adminUser, Role.VIEWER);

        // Then
        assertThat(deletedCount).isEqualTo(0);
    }

    // ========================================================================
    // Finder methods for roles by user tests
    // ========================================================================

    @Test
    @DisplayName("Should find all role assignments for user")
    void shouldFindAllRoleAssignmentsForUser() {
        // Given - Add additional role to admin user
        UserRole additionalRole = new UserRole(adminUser, Role.ASSET_MANAGER, assignerUser);
        entityManager.persistAndFlush(additionalRole);

        // When
        List<UserRole> roles = userRoleRepository.findByUser(adminUser);

        // Then
        assertThat(roles).hasSize(2);
        assertThat(roles).extracting(UserRole::getRole)
                .containsExactlyInAnyOrder(Role.ADMINISTRATOR, Role.ASSET_MANAGER);
    }

    @Test
    @DisplayName("Should find role assignments for user with pagination")
    void shouldFindRoleAssignmentsForUserWithPagination() {
        // Given - Add additional roles to admin user
        UserRole role1 = new UserRole(adminUser, Role.ASSET_MANAGER, assignerUser);
        UserRole role2 = new UserRole(adminUser, Role.VIEWER, assignerUser);
        entityManager.persistAndFlush(role1);
        entityManager.persistAndFlush(role2);

        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<UserRole> result = userRoleRepository.findByUser(adminUser, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find roles by user (enum values only)")
    void shouldFindRolesByUser() {
        // Given - Add additional role to admin user
        UserRole additionalRole = new UserRole(adminUser, Role.ASSET_MANAGER, assignerUser);
        entityManager.persistAndFlush(additionalRole);

        // When
        Set<Role> roles = userRoleRepository.findRolesByUser(adminUser);

        // Then
        assertThat(roles).hasSize(2);
        assertThat(roles).containsExactlyInAnyOrder(Role.ADMINISTRATOR, Role.ASSET_MANAGER);
    }

    @Test
    @DisplayName("Should find role assignments by user ID")
    void shouldFindRoleAssignmentsByUserId() {
        // When
        List<UserRole> roles = userRoleRepository.findByUserId(adminUser.getId());

        // Then
        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getRole()).isEqualTo(Role.ADMINISTRATOR);
        assertThat(roles.get(0).getUser()).isEqualTo(adminUser);
    }

    @Test
    @DisplayName("Should count roles by user")
    void shouldCountRolesByUser() {
        // Given - Add additional role to admin user
        UserRole additionalRole = new UserRole(adminUser, Role.ASSET_MANAGER, assignerUser);
        entityManager.persistAndFlush(additionalRole);

        // When
        long count = userRoleRepository.countByUser(adminUser);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should count roles by user ID")
    void shouldCountRolesByUserId() {
        // When
        long count = userRoleRepository.countByUserId(adminUser.getId());

        // Then
        assertThat(count).isEqualTo(1);
    }

    // ========================================================================
    // Finder methods for users by role tests
    // ========================================================================

    @Test
    @DisplayName("Should find all users with specific role")
    void shouldFindAllUsersWithSpecificRole() {
        // Given - Add another administrator
        User anotherAdmin = new User("admin2", "$2a$10$hashedPassword", "admin2@example.com");
        anotherAdmin = entityManager.persistAndFlush(anotherAdmin);
        UserRole anotherAdminRole = new UserRole(anotherAdmin, Role.ADMINISTRATOR, assignerUser);
        entityManager.persistAndFlush(anotherAdminRole);

        // When
        List<UserRole> adminRoles = userRoleRepository.findByRole(Role.ADMINISTRATOR);

        // Then
        assertThat(adminRoles).hasSize(2);
        assertThat(adminRoles).extracting(ur -> ur.getUser().getUsername())
                .containsExactlyInAnyOrder("admin", "admin2");
    }

    @Test
    @DisplayName("Should find users by role with pagination")
    void shouldFindUsersByRoleWithPagination() {
        // Given - Add more administrators
        for (int i = 1; i <= 3; i++) {
            User admin = new User("admin" + i, "$2a$10$hashedPassword", "admin" + i + "@example.com");
            admin = entityManager.persistAndFlush(admin);
            UserRole adminRole = new UserRole(admin, Role.ADMINISTRATOR, assignerUser);
            entityManager.persistAndFlush(adminRole);
        }

        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<UserRole> result = userRoleRepository.findByRole(Role.ADMINISTRATOR, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(4); // Original admin + 3 new ones
    }

    @Test
    @DisplayName("Should find users by role (User entities only)")
    void shouldFindUsersByRole() {
        // Given - Add another administrator
        User anotherAdmin = new User("admin2", "$2a$10$hashedPassword", "admin2@example.com");
        anotherAdmin = entityManager.persistAndFlush(anotherAdmin);
        UserRole anotherAdminRole = new UserRole(anotherAdmin, Role.ADMINISTRATOR, assignerUser);
        entityManager.persistAndFlush(anotherAdminRole);

        // When
        List<User> admins = userRoleRepository.findUsersByRole(Role.ADMINISTRATOR);

        // Then
        assertThat(admins).hasSize(2);
        assertThat(admins).extracting(User::getUsername)
                .containsExactlyInAnyOrder("admin", "admin2");
    }

    @Test
    @DisplayName("Should find active users by role")
    void shouldFindActiveUsersByRole() {
        // Given - Add inactive administrator
        User inactiveAdmin = new User("inactive_admin", "$2a$10$hashedPassword", "inactive@example.com");
        inactiveAdmin.setIsActive(false);
        inactiveAdmin = entityManager.persistAndFlush(inactiveAdmin);
        UserRole inactiveAdminRole = new UserRole(inactiveAdmin, Role.ADMINISTRATOR, assignerUser);
        entityManager.persistAndFlush(inactiveAdminRole);

        // When
        List<User> activeAdmins = userRoleRepository.findActiveUsersByRole(Role.ADMINISTRATOR);

        // Then
        assertThat(activeAdmins).hasSize(1);
        assertThat(activeAdmins.get(0).getUsername()).isEqualTo("admin");
        assertThat(activeAdmins.get(0).getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should find active users by role with pagination")
    void shouldFindActiveUsersByRoleWithPagination() {
        // Given - Add active and inactive administrators
        User activeAdmin = new User("active_admin", "$2a$10$hashedPassword", "active@example.com");
        activeAdmin = entityManager.persistAndFlush(activeAdmin);
        UserRole activeAdminRole = new UserRole(activeAdmin, Role.ADMINISTRATOR, assignerUser);
        entityManager.persistAndFlush(activeAdminRole);

        User inactiveAdmin = new User("inactive_admin", "$2a$10$hashedPassword", "inactive@example.com");
        inactiveAdmin.setIsActive(false);
        inactiveAdmin = entityManager.persistAndFlush(inactiveAdmin);
        UserRole inactiveAdminRole = new UserRole(inactiveAdmin, Role.ADMINISTRATOR, assignerUser);
        entityManager.persistAndFlush(inactiveAdminRole);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRoleRepository.findActiveUsersByRole(Role.ADMINISTRATOR, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2); // Original admin + active_admin
        assertThat(result.getContent()).extracting(User::getUsername)
                .containsExactlyInAnyOrder("admin", "active_admin");
        assertThat(result.getContent()).allMatch(User::getIsActive);
    }

    @Test
    @DisplayName("Should count users by role")
    void shouldCountUsersByRole() {
        // Given - Add another administrator
        User anotherAdmin = new User("admin2", "$2a$10$hashedPassword", "admin2@example.com");
        anotherAdmin = entityManager.persistAndFlush(anotherAdmin);
        UserRole anotherAdminRole = new UserRole(anotherAdmin, Role.ADMINISTRATOR, assignerUser);
        entityManager.persistAndFlush(anotherAdminRole);

        // When
        long adminCount = userRoleRepository.countByRole(Role.ADMINISTRATOR);
        long managerCount = userRoleRepository.countByRole(Role.ASSET_MANAGER);
        long viewerCount = userRoleRepository.countByRole(Role.VIEWER);

        // Then
        assertThat(adminCount).isEqualTo(2);
        assertThat(managerCount).isEqualTo(1);
        assertThat(viewerCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should count active users by role")
    void shouldCountActiveUsersByRole() {
        // Given - Add inactive administrator
        User inactiveAdmin = new User("inactive_admin", "$2a$10$hashedPassword", "inactive@example.com");
        inactiveAdmin.setIsActive(false);
        inactiveAdmin = entityManager.persistAndFlush(inactiveAdmin);
        UserRole inactiveAdminRole = new UserRole(inactiveAdmin, Role.ADMINISTRATOR, assignerUser);
        entityManager.persistAndFlush(inactiveAdminRole);

        // When
        long activeAdminCount = userRoleRepository.countActiveUsersByRole(Role.ADMINISTRATOR);

        // Then
        assertThat(activeAdminCount).isEqualTo(1); // Only the original active admin
    }

    // ========================================================================
    // Multiple roles tests
    // ========================================================================

    @Test
    @DisplayName("Should find users by multiple roles")
    void shouldFindUsersByMultipleRoles() {
        // When
        List<User> users = userRoleRepository.findUsersByRoleIn(
                List.of(Role.ADMINISTRATOR, Role.ASSET_MANAGER));

        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getUsername)
                .containsExactlyInAnyOrder("admin", "manager");
    }

    @Test
    @DisplayName("Should find users by multiple roles with pagination")
    void shouldFindUsersByMultipleRolesWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<User> result = userRoleRepository.findUsersByRoleIn(
                List.of(Role.ADMINISTRATOR, Role.ASSET_MANAGER), pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find users with all specified roles")
    void shouldFindUsersWithAllSpecifiedRoles() {
        // Given - Add multiple roles to admin user
        UserRole managerRole = new UserRole(adminUser, Role.ASSET_MANAGER, assignerUser);
        entityManager.persistAndFlush(managerRole);

        List<Role> requiredRoles = List.of(Role.ADMINISTRATOR, Role.ASSET_MANAGER);

        // When
        List<User> users = userRoleRepository.findUsersByAllRoles(requiredRoles, requiredRoles.size());

        // Then
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getUsername()).isEqualTo("admin");
    }

    // ========================================================================
    // Assignment tracking and audit tests
    // ========================================================================

    @Test
    @DisplayName("Should find role assignments by assigner")
    void shouldFindRoleAssignmentsByAssigner() {
        // When
        List<UserRole> assignments = userRoleRepository.findByAssignedBy(assignerUser);

        // Then
        assertThat(assignments).hasSize(3); // admin, manager, viewer roles
        assertThat(assignments).allMatch(ur -> ur.getAssignedBy().equals(assignerUser));
    }

    @Test
    @DisplayName("Should find role assignments by assigner with pagination")
    void shouldFindRoleAssignmentsByAssignerWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<UserRole> result = userRoleRepository.findByAssignedBy(assignerUser, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should find role assignments by date range")
    void shouldFindRoleAssignmentsByDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // When
        List<UserRole> assignments = userRoleRepository.findByAssignedAtBetween(startDate, endDate);

        // Then
        assertThat(assignments).hasSize(3);
        assertThat(assignments).allMatch(ur -> 
                ur.getAssignedAt().isAfter(startDate) && ur.getAssignedAt().isBefore(endDate));
    }

    @Test
    @DisplayName("Should find recent role assignments")
    void shouldFindRecentRoleAssignments() {
        // Given
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(1);

        // When
        List<UserRole> recentAssignments = userRoleRepository.findRecentAssignments(cutoffDate);

        // Then
        assertThat(recentAssignments).hasSize(3);
        assertThat(recentAssignments).allMatch(ur -> ur.getAssignedAt().isAfter(cutoffDate));
    }

    // ========================================================================
    // Cascade delete tests
    // ========================================================================

    @Test
    @DisplayName("Should delete all role assignments for user")
    void shouldDeleteAllRoleAssignmentsForUser() {
        // Given - Add additional role to admin user
        UserRole additionalRole = new UserRole(adminUser, Role.ASSET_MANAGER, assignerUser);
        entityManager.persistAndFlush(additionalRole);

        assertThat(userRoleRepository.countByUser(adminUser)).isEqualTo(2);

        // When
        int deletedCount = userRoleRepository.deleteByUser(adminUser);

        // Then
        assertThat(deletedCount).isEqualTo(2);
        assertThat(userRoleRepository.countByUser(adminUser)).isEqualTo(0);
    }

    @Test
    @DisplayName("Should delete role assignments by user ID")
    void shouldDeleteRoleAssignmentsByUserId() {
        // Given
        assertThat(userRoleRepository.countByUserId(adminUser.getId())).isEqualTo(1);

        // When
        int deletedCount = userRoleRepository.deleteByUserId(adminUser.getId());

        // Then
        assertThat(deletedCount).isEqualTo(1);
        assertThat(userRoleRepository.countByUserId(adminUser.getId())).isEqualTo(0);
    }

    // ========================================================================
    // Business rule validation tests
    // ========================================================================

    @Test
    @DisplayName("Should check if role is user's last role")
    void shouldCheckIfRoleIsUsersLastRole() {
        // Given - User with only one role
        assertThat(userRoleRepository.countByUser(adminUser)).isEqualTo(1);

        // When & Then
        assertThat(userRoleRepository.isLastRole(adminUser, Role.ADMINISTRATOR)).isTrue();
        assertThat(userRoleRepository.isLastRole(adminUser, Role.VIEWER)).isFalse();

        // Given - Add another role
        UserRole additionalRole = new UserRole(adminUser, Role.ASSET_MANAGER, assignerUser);
        entityManager.persistAndFlush(additionalRole);

        // When & Then
        assertThat(userRoleRepository.isLastRole(adminUser, Role.ADMINISTRATOR)).isFalse();
        assertThat(userRoleRepository.isLastRole(adminUser, Role.ASSET_MANAGER)).isFalse();
    }

    @Test
    @DisplayName("Should find users with only specified role")
    void shouldFindUsersWithOnlySpecifiedRole() {
        // When
        List<User> usersWithOnlyAdminRole = userRoleRepository.findUsersWithOnlyRole(Role.ADMINISTRATOR);
        List<User> usersWithOnlyViewerRole = userRoleRepository.findUsersWithOnlyRole(Role.VIEWER);

        // Then
        assertThat(usersWithOnlyAdminRole).hasSize(1);
        assertThat(usersWithOnlyAdminRole.get(0).getUsername()).isEqualTo("admin");
        
        assertThat(usersWithOnlyViewerRole).hasSize(1);
        assertThat(usersWithOnlyViewerRole.get(0).getUsername()).isEqualTo("viewer");
    }

    @Test
    @DisplayName("Should find users who would have no roles without specified role")
    void shouldFindUsersWhoWouldHaveNoRolesWithoutSpecifiedRole() {
        // When
        List<User> usersWithoutAdmin = userRoleRepository.findUsersWhoWouldHaveNoRolesWithoutRole(Role.ADMINISTRATOR);
        List<User> usersWithoutViewer = userRoleRepository.findUsersWhoWouldHaveNoRolesWithoutRole(Role.VIEWER);

        // Then
        assertThat(usersWithoutAdmin).hasSize(1);
        assertThat(usersWithoutAdmin.get(0).getUsername()).isEqualTo("admin");
        
        assertThat(usersWithoutViewer).hasSize(1);
        assertThat(usersWithoutViewer.get(0).getUsername()).isEqualTo("viewer");
    }

    // ========================================================================
    // Advanced queries tests
    // ========================================================================

    @Test
    @DisplayName("Should find all role assignments with user and assigner")
    void shouldFindAllRoleAssignmentsWithUserAndAssigner() {
        // When
        List<UserRole> assignments = userRoleRepository.findAllWithUserAndAssigner();

        // Then
        assertThat(assignments).hasSize(3);
        assertThat(assignments).allMatch(ur -> ur.getUser() != null);
        assertThat(assignments).allMatch(ur -> ur.getAssignedBy() != null);
    }

    @Test
    @DisplayName("Should find role assignments by user with assigner")
    void shouldFindRoleAssignmentsByUserWithAssigner() {
        // When
        List<UserRole> assignments = userRoleRepository.findByUserWithAssigner(adminUser);

        // Then
        assertThat(assignments).hasSize(1);
        assertThat(assignments.get(0).getUser()).isEqualTo(adminUser);
        assertThat(assignments.get(0).getAssignedBy()).isEqualTo(assignerUser);
    }

    @Test
    @DisplayName("Should find role assignments by role with user")
    void shouldFindRoleAssignmentsByRoleWithUser() {
        // When
        List<UserRole> assignments = userRoleRepository.findByRoleWithUser(Role.ADMINISTRATOR);

        // Then
        assertThat(assignments).hasSize(1);
        assertThat(assignments.get(0).getRole()).isEqualTo(Role.ADMINISTRATOR);
        assertThat(assignments.get(0).getUser()).isEqualTo(adminUser);
    }

    @Test
    @DisplayName("Should find role assignments with filters")
    void shouldFindRoleAssignmentsWithFilters() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        Pageable pageable = PageRequest.of(0, 10);

        // When - Filter by user
        Page<UserRole> userFilter = userRoleRepository.findWithFilters(
                adminUser, null, null, null, null, pageable);

        // When - Filter by role
        Page<UserRole> roleFilter = userRoleRepository.findWithFilters(
                null, Role.ADMINISTRATOR, null, null, null, pageable);

        // When - Filter by assigner
        Page<UserRole> assignerFilter = userRoleRepository.findWithFilters(
                null, null, assignerUser, null, null, pageable);

        // When - Filter by date range
        Page<UserRole> dateFilter = userRoleRepository.findWithFilters(
                null, null, null, startDate, endDate, pageable);

        // Then
        assertThat(userFilter.getContent()).hasSize(1);
        assertThat(userFilter.getContent().get(0).getUser()).isEqualTo(adminUser);

        assertThat(roleFilter.getContent()).hasSize(1);
        assertThat(roleFilter.getContent().get(0).getRole()).isEqualTo(Role.ADMINISTRATOR);

        assertThat(assignerFilter.getContent()).hasSize(3);
        assertThat(assignerFilter.getContent()).allMatch(ur -> ur.getAssignedBy().equals(assignerUser));

        assertThat(dateFilter.getContent()).hasSize(3);
        assertThat(dateFilter.getContent()).allMatch(ur -> 
                ur.getAssignedAt().isAfter(startDate) && ur.getAssignedAt().isBefore(endDate));
    }

    // ========================================================================
    // Statistics and reporting tests
    // ========================================================================

    @Test
    @DisplayName("Should get role distribution statistics")
    void shouldGetRoleDistributionStatistics() {
        // When
        List<Object[]> distribution = userRoleRepository.getRoleDistribution();

        // Then
        assertThat(distribution).hasSize(3);
        
        // Convert to map for easier assertions
        var distributionMap = distribution.stream()
                .collect(java.util.stream.Collectors.toMap(
                        arr -> (Role) arr[0],
                        arr -> (Long) arr[1]
                ));

        assertThat(distributionMap.get(Role.ADMINISTRATOR)).isEqualTo(1L);
        assertThat(distributionMap.get(Role.ASSET_MANAGER)).isEqualTo(1L);
        assertThat(distributionMap.get(Role.VIEWER)).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should get role assignment activity")
    void shouldGetRoleAssignmentActivity() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // When
        List<Object[]> activity = userRoleRepository.getRoleAssignmentActivity(startDate, endDate);

        // Then
        assertThat(activity).isNotEmpty();
        // All assignments should be from today
        assertThat(activity).hasSize(1);
        assertThat(activity.get(0)[1]).isEqualTo(3L); // 3 assignments today
    }

    @Test
    @DisplayName("Should get most active assigners")
    void shouldGetMostActiveAssigners() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);

        // When
        List<Object[]> activeAssigners = userRoleRepository.getMostActiveAssigners(pageable);

        // Then
        assertThat(activeAssigners).hasSize(1);
        assertThat(activeAssigners.get(0)[0]).isEqualTo(assignerUser);
        assertThat(activeAssigners.get(0)[1]).isEqualTo(3L); // 3 assignments
    }

    @Test
    @DisplayName("Should find orphaned assignments")
    void shouldFindOrphanedAssignments() {
        // When
        List<UserRole> orphaned = userRoleRepository.findOrphanedAssignments();

        // Then
        assertThat(orphaned).isEmpty(); // No orphaned assignments in our test data
    }
}