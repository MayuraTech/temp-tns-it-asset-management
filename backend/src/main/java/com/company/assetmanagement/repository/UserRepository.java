package com.company.assetmanagement.repository;

import com.company.assetmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entities.
 * 
 * Placeholder repository for allocation management implementation.
 * Full implementation will be provided by User Management module.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Find user by username.
     *
     * @param username the username
     * @return optional containing the user if found
     */
    Optional<User> findByUsername(String username);
}
