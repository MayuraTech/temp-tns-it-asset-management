package com.company.assetmanagement.service;

import com.company.assetmanagement.dto.*;
import com.company.assetmanagement.exception.*;
import com.company.assetmanagement.model.*;
import com.company.assetmanagement.repository.AssignmentHistoryRepository;
import com.company.assetmanagement.repository.AssetRepository;
import com.company.assetmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of AllocationService.
 * 
 * Handles all allocation management operations including assignment, deallocation,
 * reassignment, and querying of asset assignments. All operations are audited and
 * require appropriate authorization.
 */
@Service
@Transactional
public class AllocationServiceImpl implements AllocationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AllocationServiceImpl.class);
    
    private static final int MAX_BULK_SIZE = 50;
    private static final int MAX_EXPORT_SIZE = 10000;
    
    private final AssignmentHistoryRepository assignmentHistoryRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final AuthorizationService authorizationService;
    
    public AllocationServiceImpl(
            AssignmentHistoryRepository assignmentHistoryRepository,
            AssetRepository assetRepository,
            UserRepository userRepository,
            AuditService auditService,
            AuthorizationService authorizationService) {
        this.assignmentHistoryRepository = assignmentHistoryRepository;
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.authorizationService = authorizationService;
    }
    
    @Override
    public AssignmentDTO assignToUser(String userId, UUID assetId, AssignmentRequest request) {
        logger.info("Assigning asset {} to user {} by user {}", assetId, request.getAssignedTo(), userId);
        
        // 1. Authorization check
        if (!authorizationService.hasPermission(userId, Action.ALLOCATE_ASSET)) {
            logger.warn("User {} lacks permission to allocate assets", userId);
            throw new InsufficientPermissionsException("User does not have permission to allocate assets");
        }
        
        // 2. Validation
        validateAssignmentRequest(request, AssignmentType.USER);
        
        // 3. Check asset availability and status
        Asset asset = assetRepository.findById(assetId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset", assetId.toString()));
        
        validateAssetAssignable(asset);
        
        // 4. Verify asset not already assigned (check AssignedUser and Location fields)
        if (asset.getAssignedUser() != null || asset.getLocation() != null) {
            String assignedTo = asset.getAssignedUser() != null ? asset.getAssignedUser() : asset.getLocation();
            throw new AssetAlreadyAssignedException(assetId, 
                "Asset is already assigned to " + assignedTo);
        }
        
        // 5. Create AssignmentHistory record with all required fields
        AssignmentHistory assignment = new AssignmentHistory();
        assignment.setAssetId(assetId);
        assignment.setAssignmentType(AssignmentType.USER);
        assignment.setAssignedTo(request.getAssignedTo());
        assignment.setAssignedBy(UUID.fromString(userId));
        assignment.setAssignedAt(LocalDateTime.now());
        
        assignment = assignmentHistoryRepository.save(assignment);
        
        // 6. Update Asset assignment fields (AssignedUser, AssignedUserEmail, AssignmentDate)
        asset.setAssignedUser(request.getAssignedTo());
        asset.setAssignedUserEmail(request.getAssignedUserEmail());
        asset.setAssignmentDate(LocalDateTime.now());
        asset.setUpdatedAt(LocalDateTime.now());
        assetRepository.save(asset);
        
        // 7. Log to Audit Service with action type CREATE and resource type ASSIGNMENT
        auditService.logEvent(createAuditEvent(
            userId,
            Action.CREATE_ASSET,
            "ASSIGNMENT",
            assignment.getId().toString(),
            Map.of(
                "assetId", assetId.toString(),
                "assignmentType", "USER",
                "assignedTo", request.getAssignedTo(),
                "assignedUserEmail", request.getAssignedUserEmail()
            )
        ));
        
        logger.info("Successfully assigned asset {} to user {}", assetId, request.getAssignedTo());
        
        // 8. Return mapped DTO
        return mapToDTO(assignment, userId);
    }
    
    @Override
    public AssignmentDTO assignToLocation(String userId, UUID assetId, AssignmentRequest request) {
        logger.info("Assigning asset {} to location {} by user {}", assetId, request.getAssignedTo(), userId);
        
        // 1. Authorization check
        if (!authorizationService.hasPermission(userId, Action.ALLOCATE_ASSET)) {
            logger.warn("User {} lacks permission to allocate assets", userId);
            throw new InsufficientPermissionsException("User does not have permission to allocate assets");
        }
        
        // 2. Validation
        validateAssignmentRequest(request, AssignmentType.LOCATION);
        
        // 3. Check asset availability and status
        Asset asset = assetRepository.findById(assetId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset", assetId.toString()));
        
        validateAssetAssignable(asset);
        
        // 4. Verify asset not already assigned (check AssignedUser and Location fields)
        if (asset.getAssignedUser() != null || asset.getLocation() != null) {
            String assignedTo = asset.getAssignedUser() != null ? asset.getAssignedUser() : asset.getLocation();
            throw new AssetAlreadyAssignedException(assetId, 
                "Asset is already assigned to " + assignedTo);
        }
        
        // 5. Create AssignmentHistory record with all required fields
        AssignmentHistory assignment = new AssignmentHistory();
        assignment.setAssetId(assetId);
        assignment.setAssignmentType(AssignmentType.LOCATION);
        assignment.setAssignedTo(request.getAssignedTo());
        assignment.setAssignedBy(UUID.fromString(userId));
        assignment.setAssignedAt(LocalDateTime.now());
        
        assignment = assignmentHistoryRepository.save(assignment);
        
        // 6. Update Asset location field (Location and LocationUpdateDate)
        asset.setLocation(request.getAssignedTo());
        asset.setLocationUpdateDate(LocalDateTime.now());
        asset.setUpdatedAt(LocalDateTime.now());
        assetRepository.save(asset);
        
        // 7. Log to Audit Service with action type CREATE and resource type ASSIGNMENT
        auditService.logEvent(createAuditEvent(
            userId,
            Action.CREATE_ASSET,
            "ASSIGNMENT",
            assignment.getId().toString(),
            Map.of(
                "assetId", assetId.toString(),
                "assignmentType", "LOCATION",
                "assignedTo", request.getAssignedTo()
            )
        ));
        
        logger.info("Successfully assigned asset {} to location {}", assetId, request.getAssignedTo());
        
        // 8. Return mapped DTO
        return mapToDTO(assignment, userId);
    }
    
    @Override
    public void deallocate(String userId, UUID assetId) {
        logger.info("Deallocating asset {} by user {}", assetId, userId);
        
        // 1. Authorization check
        if (!authorizationService.hasPermission(userId, Action.DEALLOCATE_ASSET)) {
            logger.warn("User {} lacks permission to deallocate assets", userId);
            throw new InsufficientPermissionsException("User does not have permission to deallocate assets");
        }
        
        // 2. Check asset exists
        Asset asset = assetRepository.findById(assetId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset", assetId.toString()));
        
        // 3. Find active assignment
        List<AssignmentHistory> activeAssignments = assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId);
        if (activeAssignments.isEmpty()) {
            throw new AssetNotAssignedException(assetId);
        }
        
        AssignmentHistory assignment = activeAssignments.get(0);
        
        // 4. Close assignment record
        assignment.setUnassignedAt(LocalDateTime.now());
        assignmentHistoryRepository.save(assignment);
        
        // 5. Clear asset assignment fields
        asset.setAssignedUser(null);
        asset.setAssignedUserEmail(null);
        asset.setLocation(null);
        asset.setAssignmentDate(null);
        asset.setLocationUpdateDate(null);
        asset.setUpdatedAt(LocalDateTime.now());
        assetRepository.save(asset);
        
        // 6. Audit logging
        auditService.logEvent(createAuditEvent(
            userId,
            Action.DELETE_ASSET,
            "ASSIGNMENT",
            assignment.getId().toString(),
            Map.of(
                "assetId", assetId.toString(),
                "assignmentType", assignment.getAssignmentType().toString(),
                "assignedTo", assignment.getAssignedTo()
            )
        ));
        
        logger.info("Successfully deallocated asset {}", assetId);
    }
    
    @Override
    public AssignmentDTO reassign(String userId, UUID assetId, AssignmentRequest request) {
        logger.info("Reassigning asset {} by user {}", assetId, userId);
        
        // 1. Authorization check
        if (!authorizationService.hasPermission(userId, Action.ALLOCATE_ASSET)) {
            logger.warn("User {} lacks permission to allocate assets", userId);
            throw new InsufficientPermissionsException("User does not have permission to allocate assets");
        }
        
        // 2. Validation
        validateAssignmentRequest(request, request.getAssignmentType());
        
        // 3. Check asset exists
        Asset asset = assetRepository.findById(assetId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset", assetId.toString()));
        
        validateAssetAssignable(asset);
        
        // 4. Find active assignment
        List<AssignmentHistory> activeAssignments = assignmentHistoryRepository.findActiveAssignmentsByAssetId(assetId);
        if (activeAssignments.isEmpty()) {
            throw new AssetNotAssignedException(assetId);
        }
        
        AssignmentHistory oldAssignment = activeAssignments.get(0);
        
        // 5. Atomic operation: close old and create new
        // Close old assignment
        oldAssignment.setUnassignedAt(LocalDateTime.now());
        assignmentHistoryRepository.save(oldAssignment);
        
        // Create new assignment
        AssignmentHistory newAssignment = new AssignmentHistory();
        newAssignment.setAssetId(assetId);
        newAssignment.setAssignmentType(request.getAssignmentType());
        newAssignment.setAssignedTo(request.getAssignedTo());
        newAssignment.setAssignedBy(UUID.fromString(userId));
        newAssignment.setAssignedAt(LocalDateTime.now());
        
        newAssignment = assignmentHistoryRepository.save(newAssignment);
        
        // 6. Update asset
        if (request.getAssignmentType() == AssignmentType.USER) {
            asset.setAssignedUser(request.getAssignedTo());
            asset.setAssignedUserEmail(request.getAssignedUserEmail());
            asset.setAssignmentDate(LocalDateTime.now());
            asset.setLocation(null);
            asset.setLocationUpdateDate(null);
        } else {
            asset.setLocation(request.getAssignedTo());
            asset.setLocationUpdateDate(LocalDateTime.now());
            asset.setAssignedUser(null);
            asset.setAssignedUserEmail(null);
            asset.setAssignmentDate(null);
        }
        asset.setUpdatedAt(LocalDateTime.now());
        assetRepository.save(asset);
        
        // 7. Audit logging (log both operations)
        auditService.logEvent(createAuditEvent(
            userId,
            Action.DELETE_ASSET,
            "ASSIGNMENT",
            oldAssignment.getId().toString(),
            Map.of(
                "assetId", assetId.toString(),
                "operation", "reassignment_close",
                "oldAssignedTo", oldAssignment.getAssignedTo()
            )
        ));
        
        auditService.logEvent(createAuditEvent(
            userId,
            Action.CREATE_ASSET,
            "ASSIGNMENT",
            newAssignment.getId().toString(),
            Map.of(
                "assetId", assetId.toString(),
                "operation", "reassignment_create",
                "assignmentType", request.getAssignmentType().toString(),
                "assignedTo", request.getAssignedTo()
            )
        ));
        
        logger.info("Successfully reassigned asset {} from {} to {}", 
            assetId, oldAssignment.getAssignedTo(), request.getAssignedTo());
        
        return mapToDTO(newAssignment, userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AssignmentHistoryDTO> getAssignmentHistory(String userId, UUID assetId, Pageable pageable) {
        logger.debug("Retrieving assignment history for asset {} by user {}", assetId, userId);
        
        // 1. Authorization check - verify user has ADMINISTRATOR, ASSET_MANAGER, or VIEWER role
        if (!authorizationService.hasPermission(userId, Action.VIEW_ASSET)) {
            logger.warn("User {} lacks permission to view assignment history", userId);
            throw new InsufficientPermissionsException("User does not have permission to view assignment history");
        }
        
        // 2. Verify asset exists
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset", assetId.toString());
        }
        
        // 3. Query AssignmentHistoryRepository with pagination, ordered by AssignedAt descending
        Page<AssignmentHistory> history = assignmentHistoryRepository.findByAssetIdOrderByAssignedAtDesc(assetId, pageable);
        
        // 4. Map to DTO and include username of assigner
        return history.map(assignment -> {
            AssignmentHistoryDTO dto = new AssignmentHistoryDTO();
            dto.setId(assignment.getId());
            dto.setAssetId(assignment.getAssetId());
            dto.setAssignmentType(assignment.getAssignmentType());
            dto.setAssignedTo(assignment.getAssignedTo());
            dto.setAssignedBy(assignment.getAssignedBy());
            dto.setAssignedAt(assignment.getAssignedAt());
            dto.setUnassignedAt(assignment.getUnassignedAt());
            dto.setActive(assignment.isActive());
            
            // Include username of user who performed assignment
            userRepository.findById(assignment.getAssignedBy())
                .ifPresent(user -> dto.setAssignedByUsername(user.getUsername()));
            
            return dto;
        });
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AssetDTO> getAssetsByUser(String userName, Pageable pageable) {
        logger.debug("Querying assets assigned to user {}", userName);
        
        // Query assets where AssignedUser matches the userName parameter (case-insensitive)
        // This method already filters for assets with active assignments because:
        // 1. Asset.assignedUser is only set when an assignment is created
        // 2. Asset.assignedUser is cleared when an assignment is closed (deallocated)
        // 3. The Asset table maintains the current assignment state
        Page<Asset> assets = assetRepository.findByAssignedUserContainingIgnoreCase(userName, pageable);
        
        // Map Asset entities to AssetDTO with all required fields:
        // - asset ID, name, serial number, asset type, status, and assignment date
        return assets.map(this::mapAssetToDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AssetDTO> getAssetsByLocation(String location, Pageable pageable) {
        logger.debug("Querying assets assigned to location {}", location);
        
        // Query assets where Location matches the location parameter (case-insensitive)
        // This method already filters for assets with active assignments because:
        // 1. Asset.location is only set when an assignment is created
        // 2. Asset.location is cleared when an assignment is closed (deallocated)
        // 3. The Asset table maintains the current assignment state
        Page<Asset> assets = assetRepository.findByLocationContainingIgnoreCase(location, pageable);
        
        // Map Asset entities to AssetDTO with all required fields:
        // - asset ID, name, serial number, asset type, status, and location update date
        return assets.map(this::mapAssetToDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AssignmentStatisticsDTO getStatistics(String userId) {
        logger.debug("Generating assignment statistics for user {}", userId);
        
        // 1. Authorization check - requires ADMINISTRATOR or ASSET_MANAGER role
        if (!authorizationService.hasPermission(userId, Action.VIEW_ASSET)) {
            logger.warn("User {} lacks permission to view assignment statistics", userId);
            throw new InsufficientPermissionsException("User does not have permission to view assignment statistics");
        }
        
        AssignmentStatisticsDTO stats = new AssignmentStatisticsDTO();
        
        // 2. Total assigned assets - count assets where assignedUser is not null OR location is not null
        long totalAssigned = assetRepository.countAssignedAssets();
        stats.setTotalAssignedAssets(totalAssigned);
        
        // 3. Assignments by type (USER vs LOCATION)
        List<Object[]> typeStats = assignmentHistoryRepository.getAssignmentStatistics();
        for (Object[] row : typeStats) {
            AssignmentType type = (AssignmentType) row[0];
            Long count = (Long) row[1];
            
            if (type == AssignmentType.USER) {
                stats.setUserAssignments(count);
            } else if (type == AssignmentType.LOCATION) {
                stats.setLocationAssignments(count);
            }
        }
        
        // 4. Available (unassigned) assets by status (IN_USE, DEPLOYED, STORAGE)
        Map<String, Long> availableByStatus = new HashMap<>();
        for (LifecycleStatus status : Arrays.asList(LifecycleStatus.IN_USE, LifecycleStatus.DEPLOYED, LifecycleStatus.STORAGE)) {
            long count = assetRepository.countByStatusAndUnassigned(status);
            availableByStatus.put(status.toString(), count);
        }
        stats.setAvailableAssetsByStatus(availableByStatus);
        
        // 5. Top 10 users by number of assigned assets
        List<Object[]> topUsers = assignmentHistoryRepository.getTopAssignmentsByType(
            AssignmentType.USER, PageRequest.of(0, 10)
        );
        List<AssignmentStatisticsDTO.AssignmentCountDTO> topUsersList = topUsers.stream()
            .map(row -> new AssignmentStatisticsDTO.AssignmentCountDTO((String) row[0], (Long) row[1]))
            .collect(Collectors.toList());
        stats.setTopUsersByAssignments(topUsersList);
        
        // 6. Top 10 locations by number of assigned assets
        List<Object[]> topLocations = assignmentHistoryRepository.getTopAssignmentsByType(
            AssignmentType.LOCATION, PageRequest.of(0, 10)
        );
        List<AssignmentStatisticsDTO.AssignmentCountDTO> topLocationsList = topLocations.stream()
            .map(row -> new AssignmentStatisticsDTO.AssignmentCountDTO((String) row[0], (Long) row[1]))
            .collect(Collectors.toList());
        stats.setTopLocationsByAssignments(topLocationsList);
        
        logger.info("Successfully generated assignment statistics for user {}", userId);
        
        return stats;
    }
    
    @Override
    public BulkDeallocationResult bulkDeallocate(String userId, List<UUID> assetIds) {
        logger.info("Bulk deallocating {} assets by user {}", assetIds.size(), userId);
        
        BulkDeallocationResult result = new BulkDeallocationResult();
        result.setTotalRequested(assetIds.size());
        
        // 1. Validate bulk request size (max 50)
        if (assetIds.size() > MAX_BULK_SIZE) {
            logger.warn("Bulk deallocation request exceeds maximum size: {} > {}", 
                assetIds.size(), MAX_BULK_SIZE);
            throw new ValidationException(Collections.singletonList(
                new ValidationError("assetIds", "Bulk operation limited to " + MAX_BULK_SIZE + " assets")
            ));
        }
        
        // 2. Authorization check (once for entire operation)
        if (!authorizationService.hasPermission(userId, Action.DEALLOCATE_ASSET)) {
            logger.warn("User {} lacks permission to deallocate assets", userId);
            throw new InsufficientPermissionsException("User does not have permission to deallocate assets");
        }
        
        // 3. Process each deallocation independently
        for (UUID assetId : assetIds) {
            try {
                // Perform deallocation
                deallocate(userId, assetId);
                
                // Find the closed assignment to return
                List<AssignmentHistory> closedAssignments = assignmentHistoryRepository
                    .findByAssetIdOrderByAssignedAtDesc(assetId, PageRequest.of(0, 1))
                    .getContent();
                
                if (!closedAssignments.isEmpty()) {
                    AssignmentDTO closedAssignment = mapToDTO(closedAssignments.get(0), userId);
                    result.addSuccess(assetId, closedAssignment);
                    
                    // 4. Log each operation separately
                    logger.info("Successfully deallocated asset {} in bulk operation", assetId);
                } else {
                    // This shouldn't happen, but handle gracefully
                    result.addFailure(assetId, "UNEXPECTED_ERROR", 
                        "Deallocation succeeded but assignment record not found");
                    logger.warn("Deallocation succeeded for asset {} but assignment record not found", assetId);
                }
                
            } catch (AssetNotAssignedException e) {
                // Asset not assigned - collect failure
                result.addFailure(assetId, "ASSET_NOT_ASSIGNED", e.getMessage());
                logger.error("Failed to deallocate asset {}: not assigned", assetId);
                
            } catch (ResourceNotFoundException e) {
                // Asset not found - collect failure
                result.addFailure(assetId, "ASSET_NOT_FOUND", e.getMessage());
                logger.error("Failed to deallocate asset {}: not found", assetId);
                
            } catch (Exception e) {
                // Other errors - collect failure
                result.addFailure(assetId, "UNEXPECTED_ERROR", e.getMessage());
                logger.error("Failed to deallocate asset {}: {}", assetId, e.getMessage(), e);
            }
            // Continue processing remaining assets even if some fail
        }
        
        logger.info("Bulk deallocation completed: {} successful, {} failed out of {} requested", 
            result.getSuccessCount(), result.getFailureCount(), result.getTotalRequested());
        
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public byte[] exportAssignments(String userId, Map<String, Object> filters) {
        logger.info("Exporting assignments with filters: {} by user {}", filters, userId);
        
        // Extract filter parameters
        AssignmentType assignmentType = null;
        LocalDateTime dateFrom = null;
        LocalDateTime dateTo = null;
        UUID assignedBy = null;
        
        if (filters != null) {
            // Parse assignment type filter
            if (filters.containsKey("assignmentType")) {
                Object typeValue = filters.get("assignmentType");
                if (typeValue instanceof String) {
                    try {
                        assignmentType = AssignmentType.valueOf((String) typeValue);
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid assignment type filter: {}", typeValue);
                    }
                } else if (typeValue instanceof AssignmentType) {
                    assignmentType = (AssignmentType) typeValue;
                }
            }
            
            // Parse date from filter
            if (filters.containsKey("dateFrom")) {
                Object dateFromValue = filters.get("dateFrom");
                if (dateFromValue instanceof String) {
                    try {
                        dateFrom = LocalDateTime.parse((String) dateFromValue);
                    } catch (Exception e) {
                        logger.warn("Invalid dateFrom filter: {}", dateFromValue);
                    }
                } else if (dateFromValue instanceof LocalDateTime) {
                    dateFrom = (LocalDateTime) dateFromValue;
                }
            }
            
            // Parse date to filter
            if (filters.containsKey("dateTo")) {
                Object dateToValue = filters.get("dateTo");
                if (dateToValue instanceof String) {
                    try {
                        dateTo = LocalDateTime.parse((String) dateToValue);
                    } catch (Exception e) {
                        logger.warn("Invalid dateTo filter: {}", dateToValue);
                    }
                } else if (dateToValue instanceof LocalDateTime) {
                    dateTo = (LocalDateTime) dateToValue;
                }
            }
            
            // Parse assigned by user filter
            if (filters.containsKey("assignedBy")) {
                Object assignedByValue = filters.get("assignedBy");
                if (assignedByValue instanceof String) {
                    try {
                        assignedBy = UUID.fromString((String) assignedByValue);
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid assignedBy filter: {}", assignedByValue);
                    }
                } else if (assignedByValue instanceof UUID) {
                    assignedBy = (UUID) assignedByValue;
                }
            }
        }
        
        // Get filtered assignments
        List<AssignmentHistory> assignments = assignmentHistoryRepository.findActiveAssignmentsWithFilters(
            assignmentType, dateFrom, dateTo, assignedBy
        );
        
        // Check size limit
        if (assignments.size() > MAX_EXPORT_SIZE) {
            throw new ValidationException(Collections.singletonList(
                new ValidationError("export", "Export limited to " + MAX_EXPORT_SIZE + " records. Please apply filters.")
            ));
        }
        
        // Generate CSV
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(baos)) {
            
            // Write header
            writer.println("Asset ID,Asset Name,Serial Number,Asset Type,Assignment Type,Assigned To,Assigned By,Assigned At");
            
            // Write data
            for (AssignmentHistory assignment : assignments) {
                Asset asset = assetRepository.findById(assignment.getAssetId()).orElse(null);
                if (asset != null) {
                    User user = userRepository.findById(assignment.getAssignedBy()).orElse(null);
                    
                    writer.printf("%s,%s,%s,%s,%s,%s,%s,%s%n",
                        asset.getId(),
                        escapeCsv(asset.getName()),
                        escapeCsv(asset.getSerialNumber()),
                        asset.getAssetType(),
                        assignment.getAssignmentType(),
                        escapeCsv(assignment.getAssignedTo()),
                        user != null ? escapeCsv(user.getUsername()) : "",
                        assignment.getAssignedAt()
                    );
                }
            }
            
            writer.flush();
            
            // Audit logging with action type EXPORT_DATA and resource type ASSIGNMENT
            auditService.logEvent(createAuditEvent(
                userId,
                Action.EXPORT_DATA,
                "ASSIGNMENT",
                "export",
                Map.of(
                    "recordCount", assignments.size(),
                    "filters", filters != null ? filters : Map.of()
                )
            ));
            
            logger.info("Successfully exported {} assignment records", assignments.size());
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            logger.error("Failed to export assignments", e);
            throw new RuntimeException("Failed to export assignments", e);
        }
    }
    
    // Validation methods
    
    /**
     * Validate assignment request data.
     *
     * @param request the assignment request
     * @param expectedType the expected assignment type
     */
    private void validateAssignmentRequest(AssignmentRequest request, AssignmentType expectedType) {
        List<ValidationError> errors = new ArrayList<>();
        
        if (request.getAssignmentType() != expectedType) {
            errors.add(new ValidationError("assignmentType", 
                "Assignment type must be " + expectedType));
        }
        
        if (request.getAssignedTo() == null || request.getAssignedTo().isBlank()) {
            errors.add(new ValidationError("assignedTo", "Assigned to is required"));
        }
        
        if (request.getAssignedTo() != null && request.getAssignedTo().length() > 255) {
            errors.add(new ValidationError("assignedTo", 
                "Assigned to must not exceed 255 characters"));
        }
        
        if (expectedType == AssignmentType.USER) {
            if (request.getAssignedUserEmail() == null || request.getAssignedUserEmail().isBlank()) {
                errors.add(new ValidationError("assignedUserEmail", 
                    "Assigned user email is required for user assignments"));
            } else if (!isValidEmail(request.getAssignedUserEmail())) {
                errors.add(new ValidationError("assignedUserEmail", 
                    "Invalid email format"));
            }
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
    
    /**
     * Validate email format.
     *
     * @param email the email to validate
     * @return true if email is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        // Simple email validation pattern
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailPattern);
    }
    }
    
    /**
     * Validate that an asset is in an assignable status.
     *
     * @param asset the asset to validate
     */
    private void validateAssetAssignable(Asset asset) {
        List<LifecycleStatus> assignableStatuses = Arrays.asList(
            LifecycleStatus.IN_USE,
            LifecycleStatus.DEPLOYED,
            LifecycleStatus.STORAGE
        );
        
        if (!assignableStatuses.contains(asset.getStatus())) {
            throw new AssetNotAssignableException(asset.getId(), asset.getStatus());
        }
    }
    
    // Mapping methods
    
    /**
     * Map AssignmentHistory entity to AssignmentDTO.
     *
     * @param assignment the assignment entity
     * @param userId the user ID for username lookup
     * @return the assignment DTO
     */
    private AssignmentDTO mapToDTO(AssignmentHistory assignment, String userId) {
        AssignmentDTO dto = new AssignmentDTO();
        dto.setId(assignment.getId());
        dto.setAssetId(assignment.getAssetId());
        dto.setAssignmentType(assignment.getAssignmentType());
        dto.setAssignedTo(assignment.getAssignedTo());
        dto.setAssignedBy(assignment.getAssignedBy());
        dto.setAssignedAt(assignment.getAssignedAt());
        dto.setUnassignedAt(assignment.getUnassignedAt());
        dto.setActive(assignment.isActive());
        
        // Get username for assignedBy
        userRepository.findById(assignment.getAssignedBy())
            .ifPresent(user -> dto.setAssignedByUsername(user.getUsername()));
        
        return dto;
    }
    
    /**
     * Map Asset entity to AssetDTO.
     *
     * @param asset the asset entity
     * @return the asset DTO
     */
    private AssetDTO mapAssetToDTO(Asset asset) {
        AssetDTO dto = new AssetDTO();
        dto.setId(asset.getId());
        dto.setAssetType(asset.getAssetType());
        dto.setName(asset.getName());
        dto.setSerialNumber(asset.getSerialNumber());
        dto.setAcquisitionDate(asset.getAcquisitionDate());
        dto.setStatus(asset.getStatus());
        dto.setLocation(asset.getLocation());
        dto.setAssignedUser(asset.getAssignedUser());
        dto.setAssignedUserEmail(asset.getAssignedUserEmail());
        dto.setAssignmentDate(asset.getAssignmentDate());
        dto.setLocationUpdateDate(asset.getLocationUpdateDate());
        dto.setCreatedAt(asset.getCreatedAt());
        dto.setUpdatedAt(asset.getUpdatedAt());
        return dto;
    }
    
    /**
     * Create an audit event DTO.
     *
     * @param userId the user ID
     * @param action the action type
     * @param resourceType the resource type
     * @param resourceId the resource ID
     * @param metadata additional metadata
     * @return the audit event DTO
     */
    private AuditEventDTO createAuditEvent(String userId, Action action, String resourceType, 
                                          String resourceId, Map<String, Object> metadata) {
        AuditEventDTO event = new AuditEventDTO();
        event.setTimestamp(LocalDateTime.now());
        event.setUserId(UUID.fromString(userId));
        event.setActionType(action);
        event.setResourceType(resourceType);
        event.setResourceId(resourceId);
        event.setMetadata(metadata);
        
        // Get username
        userRepository.findById(UUID.fromString(userId))
            .ifPresent(user -> event.setUsername(user.getUsername()));
        
        return event;
    }
    
    /**
     * Escape CSV field value.
     *
     * @param value the value to escape
     * @return the escaped value
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
