package com.company.assetmanagement.repository;

import com.company.assetmanagement.model.Asset;
import com.company.assetmanagement.model.LifecycleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Asset entities.
 * 
 * Placeholder repository for allocation management implementation.
 * Full implementation will be provided by Asset Management module.
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID> {
    
    /**
     * Find assets by assigned user name (case-insensitive).
     *
     * @param userName the user name to search for
     * @param pageable pagination information
     * @return page of assets assigned to the user
     */
    Page<Asset> findByAssignedUserContainingIgnoreCase(String userName, Pageable pageable);
    
    /**
     * Find assets by location (case-insensitive).
     *
     * @param location the location to search for
     * @param pageable pagination information
     * @return page of assets at the location
     */
    Page<Asset> findByLocationContainingIgnoreCase(String location, Pageable pageable);
    
    /**
     * Count assets by status where assigned user and location are null.
     *
     * @param status the lifecycle status
     * @return count of unassigned assets with the specified status
     */
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.status = :status AND a.assignedUser IS NULL AND a.location IS NULL")
    long countByStatusAndUnassigned(@Param("status") LifecycleStatus status);
    
    /**
     * Find all assets by status.
     *
     * @param statuses list of lifecycle statuses
     * @return list of assets with the specified statuses
     */
    List<Asset> findByStatusIn(List<LifecycleStatus> statuses);
    
    /**
     * Count assets that are currently assigned (have assignedUser or location set).
     * An asset is considered assigned if assignedUser is not null OR location is not null.
     *
     * @return count of currently assigned assets
     */
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.assignedUser IS NOT NULL OR a.location IS NOT NULL")
    long countAssignedAssets();
}
