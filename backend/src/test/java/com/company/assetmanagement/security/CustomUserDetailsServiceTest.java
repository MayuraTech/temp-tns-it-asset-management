package com.company.assetmanagement.security;

import com.company.assetmanagement.model.Role;
import com.company.assetmanagement.model.User;
import com.company.assetmanagement.model.UserRole;
import com.company.assetmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomUserDetailsService.
 * 
 * Tests cover:
 * - Loading user by username
 * - Role to authority conversion
 * - Multiple roles handling
 * - User not found scenarios
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private CustomUserDetailsService userDetailsService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "hashedPassword123", "test@example.com");
        testUser.setId(UUID.randomUUID());
        testUser.setIsActive(true);
        testUser.setAccountLocked(false);
    }
    
    @Test
    @DisplayName("Should load user by username successfully")
    void shouldLoadUserByUsername() {
        // Given
        UserRole viewerRole = new UserRole();
        viewerRole.setRole(Role.VIEWER);
        viewerRole.setUser(testUser);
        testUser.getRoles().add(viewerRole);
        
        when(userRepository.findByUsernameWithRoles("testuser"))
                .thenReturn(Optional.of(testUser));
        
        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");
        
        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("hashedPassword123");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_VIEWER");
        
        verify(userRepository).findByUsernameWithRoles("testuser");
    }
    
    @Test
    @DisplayName("Should load user with multiple roles")
    void shouldLoadUserWithMultipleRoles() {
        // Given
        UserRole adminRole = new UserRole();
        adminRole.setRole(Role.ADMINISTRATOR);
        adminRole.setUser(testUser);
        
        UserRole managerRole = new UserRole();
        managerRole.setRole(Role.ASSET_MANAGER);
        managerRole.setUser(testUser);
        
        testUser.getRoles().add(adminRole);
        testUser.getRoles().add(managerRole);
        
        when(userRepository.findByUsernameWithRoles("testuser"))
                .thenReturn(Optional.of(testUser));
        
        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");
        
        // Then
        assertThat(userDetails.getAuthorities()).hasSize(2);
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMINISTRATOR", "ROLE_ASSET_MANAGER");
    }
    
    @Test
    @DisplayName("Should convert all role types correctly")
    void shouldConvertAllRoleTypes() {
        // Given
        UserRole adminRole = new UserRole();
        adminRole.setRole(Role.ADMINISTRATOR);
        adminRole.setUser(testUser);
        
        UserRole managerRole = new UserRole();
        managerRole.setRole(Role.ASSET_MANAGER);
        managerRole.setUser(testUser);
        
        UserRole viewerRole = new UserRole();
        viewerRole.setRole(Role.VIEWER);
        viewerRole.setUser(testUser);
        
        testUser.getRoles().add(adminRole);
        testUser.getRoles().add(managerRole);
        testUser.getRoles().add(viewerRole);
        
        when(userRepository.findByUsernameWithRoles("testuser"))
                .thenReturn(Optional.of(testUser));
        
        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");
        
        // Then
        assertThat(userDetails.getAuthorities()).hasSize(3);
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder(
                        "ROLE_ADMINISTRATOR",
                        "ROLE_ASSET_MANAGER",
                        "ROLE_VIEWER"
                );
    }
    
    @Test
    @DisplayName("Should throw UsernameNotFoundException when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findByUsernameWithRoles("nonexistent"))
                .thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: nonexistent");
        
        verify(userRepository).findByUsernameWithRoles("nonexistent");
    }
    
    @Test
    @DisplayName("Should return UserDetails with account flags set correctly")
    void shouldReturnUserDetailsWithCorrectAccountFlags() {
        // Given
        UserRole viewerRole = new UserRole();
        viewerRole.setRole(Role.VIEWER);
        viewerRole.setUser(testUser);
        testUser.getRoles().add(viewerRole);
        
        when(userRepository.findByUsernameWithRoles("testuser"))
                .thenReturn(Optional.of(testUser));
        
        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");
        
        // Then
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();
    }
    
    @Test
    @DisplayName("Should handle user with no roles")
    void shouldHandleUserWithNoRoles() {
        // Given - user with no roles
        when(userRepository.findByUsernameWithRoles("testuser"))
                .thenReturn(Optional.of(testUser));
        
        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");
        
        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getAuthorities()).isEmpty();
    }
    
    @Test
    @DisplayName("Should use findByUsernameWithRoles for eager loading")
    void shouldUseFindByUsernameWithRoles() {
        // Given
        UserRole viewerRole = new UserRole();
        viewerRole.setRole(Role.VIEWER);
        viewerRole.setUser(testUser);
        testUser.getRoles().add(viewerRole);
        
        when(userRepository.findByUsernameWithRoles("testuser"))
                .thenReturn(Optional.of(testUser));
        
        // When
        userDetailsService.loadUserByUsername("testuser");
        
        // Then
        verify(userRepository).findByUsernameWithRoles("testuser");
        verify(userRepository, never()).findByUsername(anyString());
    }
}
