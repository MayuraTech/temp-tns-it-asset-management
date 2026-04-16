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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserRepository.
 * 
 * Tests the custom query methods and ensures proper functionality
 * of user data access operations including:
 * - Username and email-based queries
 * - Role-based filtering
 * - Account status queries
 * - Pagination and search functionality
 * - Bulk update operations
 * 
 * Uses @DataJpaTest for focused repository testing with in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;
    private User testUser3;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser1 = new User("testuser1", "$2a$10$hashedpassword1", "test1@example.com");
        testUser1.setIsActive(true);
        testUser1.setAccountLocked(false);
        testUser1.setFailedLoginAttempts(0);

        testUser2 = new User("testuser2", "$2a$10$hashedpassword2", "test2@example.com");
        testUser2.setIsActive(false);
        testUser2.setAccountLocked(false);
        testUser2.setFailedLoginAttempts(2);

        testUser3 = new User("testuser3", "$2a$10$hashedpassword3", "test3@example.com");
        testUser3.setIsActive(true);
        testUser3.setAccountLocked(true);
        testUser3.setLockUntil(LocalDateTime.now().plusMinutes(30));
        testUser3.setFailedLoginAttempts(5);

        adminUser = new User("admin", "$2a$10$hashedpasswordadmin", "admin@example.com");
        adminUser.setIsActive(true);
        adminUser.setAccountLocked(false);
        adminUser.setFailedLoginAttempts(0);

        // Persist users
        testUser1 = entityManager.persistAndFlush(testUser1);
        testUser2 = entityManager.persistAndFlush(testUser2);
        testUser3 = entityManager.persistAndFlush(testUser3);
        adminUser = entityManager.persistAndFlush(adminUser);

        // Create roles
        UserRole adminRole = new UserRole(adminUser, Role.ADMINISTRATOR, adminUser);
        UserRole managerRole = new UserRole(testUser1, Role.ASSET_MANAGER, adminUser);
        UserRole viewerRole = new UserRole(testUser2, Role.VIEWER, adminUser);

        entityManager.persistAndFlush(adminRole);
        entityManager.persistAndFlush(managerRole);
        entityManager.persistAndFlush(viewerRole);

        entityManager.clear();
    }

    // ========================================================================
    // Username-based query tests
    // ========================================================================

    @Test
    @DisplayName("Should find user by username")
    void shouldFindUserByUsername() {
        // When
        Optional<User> found = userRepository.findByUsername("testuser1");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser1");
        assertThat(found.get().getEmail()).isEqualTo("test1@example.com");
    }

    @Test
    @DisplayName("Should return empty when username not found")
    void shouldReturnEmptyWhenUsernameNotFound() {
        // When
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find user by username with roles")
    void shouldFindUserByUsernameWithRoles() {
        // When
        Optional<User> found = userRepository.findByUsernameWithRoles("admin");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("admin");
        assertThat(found.get().getRoles()).isNotEmpty();
        assertThat(found.get().hasRole(Role.ADMINISTRATOR)).isTrue();
    }

    @Test
    @DisplayName("Should check if username exists")
    void shouldCheckIfUsernameExists() {
        // When & Then
        assertThat(userRepository.existsByUsername("testuser1")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Should check username exists for different user")
    void shouldCheckUsernameExistsForDifferentUser() {
        // When & Then
        assertThat(userRepository.existsByUsernameAndIdNot("testuser1", testUser2.getId())).isTrue();
        assertThat(userRepository.existsByUsernameAndIdNot("testuser1", testUser1.getId())).isFalse();
    }

    // ========================================================================
    // Email-based query tests
    // ========================================================================

    @Test
    @DisplayName("Should find user by email ignoring case")
    void shouldFindUserByEmailIgnoreCase() {
        // When
        Optional<User> found = userRepository.findByEmailIgnoreCase("TEST1@EXAMPLE.COM");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test1@example.com");
    }

    @Test
    @DisplayName("Should check if email exists ignoring case")
    void shouldCheckIfEmailExistsIgnoreCase() {
        // When & Then
        assertThat(userRepository.existsByEmailIgnoreCase("TEST1@EXAMPLE.COM")).isTrue();
        assertThat(userRepository.existsByEmailIgnoreCase("nonexistent@example.com")).isFalse();
    }

    @Test
    @DisplayName("Should check email exists for different user ignoring case")
    void shouldCheckEmailExistsForDifferentUserIgnoreCase() {
        // When & Then
        assertThat(userRepository.existsByEmailIgnoreCaseAndIdNot("TEST1@EXAMPLE.COM", testUser2.getId())).isTrue();
        assertThat(userRepository.existsByEmailIgnoreCaseAndIdNot("TEST1@EXAMPLE.COM", testUser1.getId())).isFalse();
    }

    // ========================================================================
    // Account status query tests
    // ========================================================================

    @Test
    @DisplayName("Should find active users with pagination")
    void shouldFindActiveUsersWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> activePage = userRepository.findByIsActiveTrue(pageable);

        // Then
        assertThat(activePage.getContent()).hasSize(3); // testUser1, testUser3, adminUser
        assertThat(activePage.getContent()).allMatch(User::getIsActive);
    }

    @Test
    @DisplayName("Should find inactive users with pagination")
    void shouldFindInactiveUsersWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> inactivePage = userRepository.findByIsActiveFalse(pageable);

        // Then
        assertThat(inactivePage.getContent()).hasSize(1); // testUser2
        assertThat(inactivePage.getContent()).allMatch(user -> !user.getIsActive());
    }

    @Test
    @DisplayName("Should find locked users")
    void shouldFindLockedUsers() {
        // When
        List<User> lockedUsers = userRepository.findByAccountLockedTrue();

        // Then
        assertThat(lockedUsers).hasSize(1);
        assertThat(lockedUsers.get(0).getUsername()).isEqualTo("testuser3");
        assertThat(lockedUsers.get(0).getAccountLocked()).isTrue();
    }

    @Test
    @DisplayName("Should find users with high failed login attempts")
    void shouldFindUsersWithHighFailedLoginAttempts() {
        // When
        List<User> usersWithFailedAttempts = userRepository.findByFailedLoginAttemptsGreaterThanEqual(3);

        // Then
        assertThat(usersWithFailedAttempts).hasSize(1);
        assertThat(usersWithFailedAttempts.get(0).getUsername()).isEqualTo("testuser3");
        assertThat(usersWithFailedAttempts.get(0).getFailedLoginAttempts()).isEqualTo(5);
    }

    // ========================================================================
    // Role-based query tests
    // ========================================================================

    @Test
    @DisplayName("Should find users by role with pagination")
    void shouldFindUsersByRoleWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> adminUsers = userRepository.findByRole(Role.ADMINISTRATOR, pageable);
        Page<User> managerUsers = userRepository.findByRole(Role.ASSET_MANAGER, pageable);

        // Then
        assertThat(adminUsers.getContent()).hasSize(1);
        assertThat(adminUsers.getContent().get(0).getUsername()).isEqualTo("admin");

        assertThat(managerUsers.getContent()).hasSize(1);
        assertThat(managerUsers.getContent().get(0).getUsername()).isEqualTo("testuser1");
    }

    @Test
    @DisplayName("Should find active users by role")
    void shouldFindActiveUsersByRole() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> activeManagers = userRepository.findActiveUsersByRole(Role.ASSET_MANAGER, pageable);

        // Then
        assertThat(activeManagers.getContent()).hasSize(1);
        assertThat(activeManagers.getContent().get(0).getUsername()).isEqualTo("testuser1");
        assertThat(activeManagers.getContent().get(0).getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should count users by role")
    void shouldCountUsersByRole() {
        // When
        long adminCount = userRepository.countByRole(Role.ADMINISTRATOR);
        long managerCount = userRepository.countByRole(Role.ASSET_MANAGER);
        long viewerCount = userRepository.countByRole(Role.VIEWER);

        // Then
        assertThat(adminCount).isEqualTo(1);
        assertThat(managerCount).isEqualTo(1);
        assertThat(viewerCount).isEqualTo(1);
    }

    // ========================================================================
    // Search and filtering tests
    // ========================================================================

    @Test
    @DisplayName("Should search users by text")
    void shouldSearchUsersByText() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> searchResults = userRepository.findBySearchText("testuser", pageable);

        // Then
        assertThat(searchResults.getContent()).hasSize(3);
        assertThat(searchResults.getContent())
            .extracting(User::getUsername)
            .containsExactlyInAnyOrder("testuser1", "testuser2", "testuser3");
    }

    @Test
    @DisplayName("Should search active users by text")
    void shouldSearchActiveUsersByText() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> searchResults = userRepository.findActiveUsersBySearchText("testuser", pageable);

        // Then
        assertThat(searchResults.getContent()).hasSize(2); // testuser1 and testuser3 are active
        assertThat(searchResults.getContent()).allMatch(User::getIsActive);
    }

    @Test
    @DisplayName("Should find users with advanced filters")
    void shouldFindUsersWithAdvancedFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When - search for active users with "test" in username/email
        Page<User> filteredResults = userRepository.findWithFilters("test", true, null, pageable);

        // Then
        assertThat(filteredResults.getContent()).hasSize(2); // testuser1 and testuser3
        assertThat(filteredResults.getContent()).allMatch(User::getIsActive);
    }

    // ========================================================================
    // Statistics and reporting tests
    // ========================================================================

    @Test
    @DisplayName("Should count active and inactive users")
    void shouldCountActiveAndInactiveUsers() {
        // When
        long activeCount = userRepository.countByIsActiveTrue();
        long inactiveCount = userRepository.countByIsActiveFalse();

        // Then
        assertThat(activeCount).isEqualTo(3); // testUser1, testUser3, adminUser
        assertThat(inactiveCount).isEqualTo(1); // testUser2
    }

    @Test
    @DisplayName("Should find users who never logged in")
    void shouldFindUsersWhoNeverLoggedIn() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> neverLoggedIn = userRepository.findByLastLoginAtIsNull(pageable);

        // Then
        assertThat(neverLoggedIn.getContent()).hasSize(4); // All test users have null lastLoginAt
    }

    // ========================================================================
    // Bulk update operation tests
    // ========================================================================

    @Test
    @DisplayName("Should update last login timestamp")
    void shouldUpdateLastLoginTimestamp() {
        // Given
        LocalDateTime loginTime = LocalDateTime.now();

        // When
        int updatedRows = userRepository.updateLastLoginAt(testUser1.getId(), loginTime);

        // Then
        assertThat(updatedRows).isEqualTo(1);
        
        // Verify the update
        entityManager.clear();
        User updatedUser = userRepository.findById(testUser1.getId()).orElseThrow();
        assertThat(updatedUser.getLastLoginAt()).isEqualToIgnoringNanos(loginTime);
    }

    @Test
    @DisplayName("Should reset failed login attempts")
    void shouldResetFailedLoginAttempts() {
        // When
        int updatedRows = userRepository.resetFailedLoginAttempts(testUser3.getId());

        // Then
        assertThat(updatedRows).isEqualTo(1);
        
        // Verify the update
        entityManager.clear();
        User updatedUser = userRepository.findById(testUser3.getId()).orElseThrow();
        assertThat(updatedUser.getFailedLoginAttempts()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should increment failed login attempts")
    void shouldIncrementFailedLoginAttempts() {
        // Given
        int initialAttempts = testUser1.getFailedLoginAttempts();

        // When
        int updatedRows = userRepository.incrementFailedLoginAttempts(testUser1.getId());

        // Then
        assertThat(updatedRows).isEqualTo(1);
        
        // Verify the update
        entityManager.clear();
        User updatedUser = userRepository.findById(testUser1.getId()).orElseThrow();
        assertThat(updatedUser.getFailedLoginAttempts()).isEqualTo(initialAttempts + 1);
    }

    @Test
    @DisplayName("Should lock user account")
    void shouldLockUserAccount() {
        // Given
        LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(30);

        // When
        int updatedRows = userRepository.lockUserAccount(testUser1.getId(), lockUntil);

        // Then
        assertThat(updatedRows).isEqualTo(1);
        
        // Verify the update
        entityManager.clear();
        User updatedUser = userRepository.findById(testUser1.getId()).orElseThrow();
        assertThat(updatedUser.getAccountLocked()).isTrue();
        assertThat(updatedUser.getLockUntil()).isEqualToIgnoringNanos(lockUntil);
    }

    @Test
    @DisplayName("Should update user active status")
    void shouldUpdateUserActiveStatus() {
        // When
        int updatedRows = userRepository.updateUserActiveStatus(testUser1.getId(), false, adminUser);

        // Then
        assertThat(updatedRows).isEqualTo(1);
        
        // Verify the update
        entityManager.clear();
        User updatedUser = userRepository.findById(testUser1.getId()).orElseThrow();
        assertThat(updatedUser.getIsActive()).isFalse();
        assertThat(updatedUser.getUpdatedBy().getId()).isEqualTo(adminUser.getId());
    }
}