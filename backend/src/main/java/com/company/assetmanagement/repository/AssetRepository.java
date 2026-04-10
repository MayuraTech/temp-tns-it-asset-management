package com.company.assetmanagement.repository;

import com.company.assetmanagement.model.Asset;
import com.company.assetmanagement.model.AssetType;
import com.company.assetmanagement.model.LifecycleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Asset entities.
 * Provides custom query methods for asset retrieval, search, and aggregation.
 * 
 * This repository supports:
 * - Serial number uniqueness checks
 * - Multi-criteria search with text, type, status, location, and date filters
 * - User and location-based queries
 * - Aggregation queries for reporting
 * 
 * @see Asset
 * @see AssetType
 * @see LifecycleStatus
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID> {
    
    /**
     * Check if an asset with the given serial number exists.
     * Used for enforcing serial number uniqueness before asset creation.
     * 
     * @param serialNumber the serial number to check
     * @return true if an asset with this serial number exists, false otherwise
     */
    boolean existsBySerialNumber(String serialNumber);
    
    /**
     * Find an asset by its serial number.
     * Serial numbers are unique identifiers for assets.
     * 
     * @param serialNumber the serial number to search for
     * @return Optional containing the asset if found, empty otherwise
     */
    Optional<Asset> findBySerialNumber(String serialNumber);
    
    /**
     * Search assets with multiple filter criteria.
     * Supports text search across name, serial number, and location fields.
     * All parameters are optional - null values are ignored in the query.
     * 
     * Text search is case-insensitive and matches partial strings using LIKE.
     * Multiple asset types and statuses can be provided as lists.
     * Date range filtering supports open-ended ranges (either dateFrom or dateTo can be null).
     * 
     * @param text optional text to search in name, serialNumber, and location (case-insensitive)
     * @param assetTypes optional list of asset types to filter by
     * @param statuses optional list of lifecycle statuses to filter by
     * @param location optional exact location match (case-insensitive)
     * @param dateFrom optional start date for acquisition date range (inclusive)
     * @param dateTo optional end date for acquisition date range (inclusive)
     * @param pageable pagination and sorting parameters
     * @return page of assets matching the search criteria
     */
    @Query("SELECT a FROM Asset a WHERE " +
           "(:text IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
           "OR LOWER(a.serialNumber) LIKE LOWER(CONCAT('%', :text, '%')) " +
           "OR LOWER(a.location) LIKE LOWER(CONCAT('%', :text, '%'))) " +
           "AND (:assetTypes IS NULL OR a.assetType IN :assetTypes) " +
           "AND (:statuses IS NULL OR a.status IN :statuses) " +
           "AND (:location IS NULL OR LOWER(a.location) = LOWER(:location)) " +
           "AND (:dateFrom IS NULL OR a.acquisitionDate >= :dateFrom) " +
           "AND (:dateTo IS NULL OR a.acquisitionDate <= :dateTo)")
    Page<Asset> searchAssets(
        @Param("text") String text,
        @Param("assetTypes") List<AssetType> assetTypes,
        @Param("statuses") List<LifecycleStatus> statuses,
        @Param("location") String location,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
        Pageable pageable
    );
    
    /**
     * Find all assets assigned to a specific user.
     * Returns assets where the assignedUser field matches the provided username.
     * 
     * @param assignedUser the username of the assigned user
     * @return list of assets assigned to the user
     */
    List<Asset> findByAssignedUser(String assignedUser);
    
    /**
     * Find all assets at a specific location.
     * Returns assets where the location field matches exactly (case-sensitive).
     * 
     * @param location the location to search for
     * @return list of assets at the specified location
     */
    List<Asset> findByLocation(String location);
    
    /**
     * Count assets grouped by asset type.
     * Returns aggregated counts for reporting and dashboard statistics.
     * 
     * The result is a list of Object arrays where:
     * - Object[0] is the AssetType enum value
     * - Object[1] is the Long count
     * 
     * @return list of [AssetType, count] pairs
     */
    @Query("SELECT a.assetType, COUNT(a) FROM Asset a GROUP BY a.assetType")
    List<Object[]> countByAssetType();
    
    /**
     * Count assets grouped by lifecycle status.
     * Returns aggregated counts for reporting and dashboard statistics.
     * 
     * The result is a list of Object arrays where:
     * - Object[0] is the LifecycleStatus enum value
     * - Object[1] is the Long count
     * 
     * @return list of [LifecycleStatus, count] pairs
     */
    @Query("SELECT a.status, COUNT(a) FROM Asset a GROUP BY a.status")
    List<Object[]> countByStatus();
    
    /**
     * Count assets with a specific lifecycle status.
     * Used for dashboard statistics to get counts for specific statuses.
     * 
     * @param status the lifecycle status to count
     * @return number of assets with the specified status
     */
    Long countByStatus(LifecycleStatus status);
}
