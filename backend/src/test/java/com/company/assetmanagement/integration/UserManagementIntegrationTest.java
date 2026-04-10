package com.company.assetmanagement.integration;

import com.company.assetmanagement.dto.*;
import com.company.assetmanagement.model.Role;
import com.company.assetmanagement.model.User;
import com.company.assetmanagement.model.UserRole;
import com.company.assetmanagement.repository.UserRepository;
import com.company.assetmanagement.repository.UserRoleRepository;
import com.company.assetmanagement.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for User Management module.
 * Tests all API endpoints with actual database connections and security configurations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("User Management Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private String adminToken;
    private String assetManagerToken;
    private String viewerToken;
    private User adminUser;
    private User assetManagerUser;
    private User viewerUser;

    @BeforeEach
    void setUp() {
        // Create test users with different roles
        adminUser = createTestUser("admin", "admin@test.com", "Admin@123456", Role.ADMINISTRATOR);
        assetManagerUser = createTestUser("assetmanager", "manager@test.com", "Manager@123456", Role.ASSET_MANAGER);
        viewerUser = createTestUser("viewer", "viewer@test.com", "Viewer@123456", Role.VIEWER);

        // Generate tokens for each user
        adminToken = generateToken(adminUser.getUsername(), Role.ADMINISTRATOR);
        assetManagerToken = generateToken(assetManagerUser.getUsername(), Role.ASSET_MANAGER);
        viewerToken = generateToken(viewerUser.getUsername(), Role.VIEWER);
    }

    // ==================== Authentication Tests ====================

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/auth/login - Should authenticate user with valid credentials")
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Admin@123456");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").exists());
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/auth/login - Should reject invalid credentials")
    void testLoginFailure() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("WrongPassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.type").value("AUTHENTICATION_FAILED"));
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/v1/auth/login - Should lock account after 5 failed attempts")
    void testAccountLockingAfterFailedAttempts() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("WrongPassword");

        // Attempt login 5 times with wrong password
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        // 6th attempt should return account locked
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.type").value("ACCOUNT_LOCKED"));
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/v1/auth/logout - Should invalidate session")
    void testLogout() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/v1/auth/refresh - Should refresh access token")
    void testTokenRefresh() throws Exception {
        // First login to get refresh token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("Admin@123456");

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TokenResponse tokenResponse = objectMapper.readValue(response, TokenResponse.class);

        // Use refresh token to get new access token
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(tokenResponse.getRefreshToken());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    // ==================== User CRUD Tests ====================

    @Test
    @Order(10)
    @DisplayName("POST /api/v1/users - Admin should create new user")
    void testCreateUserAsAdmin() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@test.com");
        request.setPassword("NewUser@123456");
        request.setRoles(Set.of(Role.VIEWER));

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("VIEWER"));
    }

    @Test
    @Order(11)
    @DisplayName("POST /api/v1/users - Should reject duplicate username")
    void testCreateUserWithDuplicateUsername() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsername("admin"); // Already exists
        request.setEmail("another@test.com");
        request.setPassword("Password@123456");
        request.setRoles(Set.of(Role.VIEWER));

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.type").value("DUPLICATE_USERNAME"));
    }

    @Test
    @Order(12)
    @DisplayName("POST /api/v1/users - Should reject duplicate email")
    void testCreateUserWithDuplicateEmail() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsername("uniqueuser");
        request.setEmail("admin@test.com"); // Already exists
        request.setPassword("Password@123456");
        request.setRoles(Set.of(Role.VIEWER));

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.type").value("DUPLICATE_EMAIL"));
    }

    @Test
    @Order(13)
    @DisplayName("POST /api/v1/users - Should reject invalid password")
    void testCreateUserWithInvalidPassword() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsername("testuser");
        request.setEmail("test@test.com");
        request.setPassword("weak"); // Doesn't meet complexity requirements
        request.setRoles(Set.of(Role.VIEWER));

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.type").value("VALIDATION_ERROR"));
    }

    @Test
    @Order(14)
    @DisplayName("POST /api/v1/users - Viewer should not be able to create user")
    void testCreateUserAsViewer() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsername("newuser2");
        request.setEmail("newuser2@test.com");
        request.setPassword("NewUser@123456");
        request.setRoles(Set.of(Role.VIEWER));

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(15)
    @DisplayName("GET /api/v1/users - Should list all users with pagination")
    void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.number").value(0));
    }

    @Test
    @Order(16)
    @DisplayName("GET /api/v1/users/{id} - Should get user by ID")
    void testGetUserById() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + adminUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(adminUser.getId().toString()))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }

    @Test
    @Order(17)
    @DisplayName("GET /api/v1/users/{id} - Should return 404 for non-existent user")
    void testGetNonExistentUser() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/users/" + nonExistentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.type").value("USER_NOT_FOUND"));
    }

    @Test
    @Order(18)
    @DisplayName("PUT /api/v1/users/{id} - Admin should update user")
    void testUpdateUser() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("updated@test.com");

        mockMvc.perform(put("/api/v1/users/" + viewerUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@test.com"));
    }

    @Test
    @Order(19)
    @DisplayName("DELETE /api/v1/users/{id} - Admin should delete user")
    void testDeleteUser() throws Exception {
        // Create a user to delete
        User userToDelete = createTestUser("deleteme", "delete@test.com", "Delete@123456", Role.VIEWER);

        mockMvc.perform(delete("/api/v1/users/" + userToDelete.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify user is deleted
        mockMvc.perform(get("/api/v1/users/" + userToDelete.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(20)
    @DisplayName("DELETE /api/v1/users/{id} - Should prevent self-deletion")
    void testPreventSelfDeletion() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + adminUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.type").value("VALIDATION_ERROR"));
    }

    // ==================== Account Status Tests ====================

    @Test
    @Order(25)
    @DisplayName("PATCH /api/v1/users/{id}/disable - Admin should disable user account")
    void testDisableUser() throws Exception {
        mockMvc.perform(patch("/api/v1/users/" + viewerUser.getId() + "/disable")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    @Order(26)
    @DisplayName("PATCH /api/v1/users/{id}/enable - Admin should enable user account")
    void testEnableUser() throws Exception {
        // First disable
        mockMvc.perform(patch("/api/v1/users/" + viewerUser.getId() + "/disable")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Then enable
        mockMvc.perform(patch("/api/v1/users/" + viewerUser.getId() + "/enable")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @Order(27)
    @DisplayName("PATCH /api/v1/users/{id}/disable - Should prevent self-disable")
    void testPreventSelfDisable() throws Exception {
        mockMvc.perform(patch("/api/v1/users/" + adminUser.getId() + "/disable")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.type").value("VALIDATION_ERROR"));
    }

    // ==================== Role Management Tests ====================

    @Test
    @Order(30)
    @DisplayName("POST /api/v1/users/{id}/roles - Admin should assign role")
    void testAssignRole() throws Exception {
        RoleAssignmentRequest request = new RoleAssignmentRequest();
        request.setRole(Role.ASSET_MANAGER);

        mockMvc.perform(post("/api/v1/users/" + viewerUser.getId() + "/roles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasSize(2)))
                .andExpect(jsonPath("$.roles", hasItem("VIEWER")))
                .andExpect(jsonPath("$.roles", hasItem("ASSET_MANAGER")));
    }

    @Test
    @Order(31)
    @DisplayName("POST /api/v1/users/{id}/roles - Should reject duplicate role assignment")
    void testAssignDuplicateRole() throws Exception {
        RoleAssignmentRequest request = new RoleAssignmentRequest();
        request.setRole(Role.VIEWER); // Already has this role

        mockMvc.perform(post("/api/v1/users/" + viewerUser.getId() + "/roles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.type").value("VALIDATION_ERROR"));
    }

    @Test
    @Order(32)
    @DisplayName("DELETE /api/v1/users/{id}/roles/{role} - Admin should revoke role")
    void testRevokeRole() throws Exception {
        // First assign a second role
        RoleAssignmentRequest assignRequest = new RoleAssignmentRequest();
        assignRequest.setRole(Role.ASSET_MANAGER);
        mockMvc.perform(post("/api/v1/users/" + viewerUser.getId() + "/roles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk());

        // Then revoke the original role
        mockMvc.perform(delete("/api/v1/users/" + viewerUser.getId() + "/roles/VIEWER")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.roles[0]").value("ASSET_MANAGER"));
    }

    @Test
    @Order(33)
    @DisplayName("DELETE /api/v1/users/{id}/roles/{role} - Should prevent revoking last role")
    void testPreventRevokingLastRole() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + viewerUser.getId() + "/roles/VIEWER")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.type").value("VALIDATION_ERROR"));
    }

    // ==================== Profile Management Tests ====================

    @Test
    @Order(40)
    @DisplayName("GET /api/v1/profile - User should get own profile")
    void testGetProfile() throws Exception {
        mockMvc.perform(get("/api/v1/profile")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }

    @Test
    @Order(41)
    @DisplayName("PUT /api/v1/profile - User should update own profile")
    void testUpdateProfile() throws Exception {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setEmail("newemail@test.com");

        mockMvc.perform(put("/api/v1/profile")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newemail@test.com"));
    }

    @Test
    @Order(42)
    @DisplayName("POST /api/v1/profile/change-password - User should change password")
    void testChangePassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("Viewer@123456");
        request.setNewPassword("NewPassword@123456");

        mockMvc.perform(post("/api/v1/profile/change-password")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify can login with new password
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("viewer");
        loginRequest.setPassword("NewPassword@123456");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(43)
    @DisplayName("POST /api/v1/profile/change-password - Should reject incorrect current password")
    void testChangePasswordWithWrongCurrentPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("WrongPassword");
        request.setNewPassword("NewPassword@123456");

        mockMvc.perform(post("/api/v1/profile/change-password")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.type").value("VALIDATION_ERROR"));
    }

    // ==================== Authorization Tests ====================

    @Test
    @Order(50)
    @DisplayName("Authorization - Viewer should not access admin endpoints")
    void testViewerCannotAccessAdminEndpoints() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsername("test");
        request.setEmail("test@test.com");
        request.setPassword("Test@123456");
        request.setRoles(Set.of(Role.VIEWER));

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(51)
    @DisplayName("Authorization - Asset Manager should not access admin endpoints")
    void testAssetManagerCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + viewerUser.getId())
                        .header("Authorization", "Bearer " + assetManagerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(52)
    @DisplayName("Authorization - All roles should access profile endpoints")
    void testAllRolesCanAccessProfile() throws Exception {
        // Admin
        mockMvc.perform(get("/api/v1/profile")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Asset Manager
        mockMvc.perform(get("/api/v1/profile")
                        .header("Authorization", "Bearer " + assetManagerToken))
                .andExpect(status().isOk());

        // Viewer
        mockMvc.perform(get("/api/v1/profile")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk());
    }

    // ==================== Helper Methods ====================

    private User createTestUser(String username, String email, String password, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setIsActive(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedBy(user);
        userRole.setAssignedAt(LocalDateTime.now());
        userRoleRepository.save(userRole);

        return user;
    }

    private String generateToken(String username, Role role) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                Arrays.asList(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
        return tokenProvider.generateToken(authentication);
    }
}
