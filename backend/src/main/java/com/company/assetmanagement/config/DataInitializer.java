package com.company.assetmanagement.config;

import com.company.assetmanagement.model.Role;
import com.company.assetmanagement.model.User;
import com.company.assetmanagement.model.UserRole;
import com.company.assetmanagement.repository.UserRepository;
import com.company.assetmanagement.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Initializer for Development Environment
 * 
 * Seeds the database with initial users for testing and development.
 * Only runs when the 'dev' profile is active.
 * 
 * Default Users:
 * - admin / Admin@123456 (Administrator)
 * - manager / Manager@123456 (Asset Manager)
 * - viewer / Viewer@123456 (Viewer)
 */
@Configuration
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            log.info("=".repeat(80));
            log.info("Initializing development data...");
            log.info("=".repeat(80));
            
            // Check if data already exists
            if (userRepository.count() > 0) {
                log.info("Database already contains data. Skipping initialization.");
                return;
            }
            
            // Create admin user
            User admin = createUser(
                "admin",
                "admin@example.com",
                "Admin@123456",
                "System Administrator"
            );
            assignRole(admin, Role.ADMINISTRATOR, admin);
            log.info("✓ Created admin user: username='admin', password='Admin@123456'");
            
            // Create asset manager user
            User manager = createUser(
                "manager",
                "manager@example.com",
                "Manager@123456",
                "Asset Manager"
            );
            assignRole(manager, Role.ASSET_MANAGER, admin);
            log.info("✓ Created manager user: username='manager', password='Manager@123456'");
            
            // Create viewer user
            User viewer = createUser(
                "viewer",
                "viewer@example.com",
                "Viewer@123456",
                "Read-Only Viewer"
            );
            assignRole(viewer, Role.VIEWER, admin);
            log.info("✓ Created viewer user: username='viewer', password='Viewer@123456'");
            
            log.info("=".repeat(80));
            log.info("Development data initialization complete!");
            log.info("=".repeat(80));
            log.info("");
            log.info("Default Login Credentials:");
            log.info("  Administrator: admin / Admin@123456");
            log.info("  Asset Manager: manager / Manager@123456");
            log.info("  Viewer:        viewer / Viewer@123456");
            log.info("");
            log.info("=".repeat(80));
        };
    }
    
    /**
     * Create a user with the given details
     */
    private User createUser(String username, String email, String password, String description) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setIsActive(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Assign a role to a user
     */
    private void assignRole(User user, Role role, User assignedBy) {
        UserRole userRole = new UserRole();
        userRole.setId(UUID.randomUUID());
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedBy(assignedBy);
        userRole.setAssignedAt(LocalDateTime.now());
        
        userRoleRepository.save(userRole);
    }
}
