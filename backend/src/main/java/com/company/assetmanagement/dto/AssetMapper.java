package com.company.assetmanagement.dto;

import com.company.assetmanagement.model.Asset;

/**
 * Mapper utility class for converting between Asset entities and DTOs.
 * Provides bidirectional mapping methods for entity-DTO conversion.
 */
public class AssetMapper {
    
    /**
     * Private constructor to prevent instantiation.
     */
    private AssetMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Convert Asset entity to AssetDTO.
     * 
     * @param asset the asset entity to convert
     * @return the converted AssetDTO
     */
    public static AssetDTO toDTO(Asset asset) {
        if (asset == null) {
            return null;
        }
        
        return AssetDTO.builder()
                .id(asset.getId() != null ? asset.getId().toString() : null)
                .assetType(asset.getAssetType())
                .name(asset.getName())
                .serialNumber(asset.getSerialNumber())
                .acquisitionDate(asset.getAcquisitionDate())
                .status(asset.getStatus())
                .location(asset.getLocation())
                .assignedUser(asset.getAssignedUser())
                .assignedUserEmail(asset.getAssignedUserEmail())
                .assignmentDate(asset.getAssignmentDate())
                .locationUpdateDate(asset.getLocationUpdateDate())
                .notes(asset.getNotes())
                .customFields(asset.getCustomFields())
                .createdAt(asset.getCreatedAt())
                .createdBy(asset.getCreatedBy() != null ? asset.getCreatedBy().toString() : null)
                .updatedAt(asset.getUpdatedAt())
                .updatedBy(asset.getUpdatedBy() != null ? asset.getUpdatedBy().toString() : null)
                .readOnly(asset.isReadOnly())
                .build();
    }
    
    /**
     * Convert AssetRequest to Asset entity.
     * Note: This creates a new entity without ID and audit fields.
     * Those should be set separately by the service layer.
     * 
     * @param request the asset request to convert
     * @return the converted Asset entity
     */
    public static Asset toEntity(AssetRequest request) {
        if (request == null) {
            return null;
        }
        
        Asset asset = new Asset();
        asset.setAssetType(request.getAssetType());
        asset.setName(request.getName());
        asset.setSerialNumber(request.getSerialNumber());
        asset.setAcquisitionDate(request.getAcquisitionDate());
        asset.setStatus(request.getStatus());
        asset.setLocation(request.getLocation());
        asset.setAssignedUser(request.getAssignedUser());
        asset.setAssignedUserEmail(request.getAssignedUserEmail());
        asset.setAssignmentDate(request.getAssignmentDate());
        asset.setLocationUpdateDate(request.getLocationUpdateDate());
        asset.setNotes(request.getNotes());
        asset.setCustomFields(request.getCustomFields());
        
        return asset;
    }
    
    /**
     * Update an existing Asset entity with data from AssetRequest.
     * Only updates mutable fields, preserves immutable fields like ID and serial number.
     * 
     * @param asset the asset entity to update
     * @param request the asset request with new data
     */
    public static void updateEntityFromRequest(Asset asset, AssetRequest request) {
        if (asset == null || request == null) {
            return;
        }
        
        // Update mutable fields only
        if (request.getAssetType() != null) {
            asset.setAssetType(request.getAssetType());
        }
        
        if (request.getName() != null) {
            asset.setName(request.getName());
        }
        
        // Serial number is immutable - do not update
        
        if (request.getAcquisitionDate() != null) {
            asset.setAcquisitionDate(request.getAcquisitionDate());
        }
        
        if (request.getStatus() != null) {
            asset.setStatus(request.getStatus());
        }
        
        // Allow null to clear location
        asset.setLocation(request.getLocation());
        
        // Allow null to clear assignment
        asset.setAssignedUser(request.getAssignedUser());
        asset.setAssignedUserEmail(request.getAssignedUserEmail());
        asset.setAssignmentDate(request.getAssignmentDate());
        
        asset.setLocationUpdateDate(request.getLocationUpdateDate());
        
        // Allow null to clear notes
        asset.setNotes(request.getNotes());
        
        // Allow null to clear custom fields
        asset.setCustomFields(request.getCustomFields());
    }
    
    /**
     * Partially update an existing Asset entity with non-null fields from AssetRequest.
     * This is useful for PATCH operations where only specified fields should be updated.
     * 
     * @param asset the asset entity to update
     * @param request the asset request with partial data
     */
    public static void patchEntityFromRequest(Asset asset, AssetRequest request) {
        if (asset == null || request == null) {
            return;
        }
        
        // Only update fields that are explicitly set (non-null)
        if (request.getAssetType() != null) {
            asset.setAssetType(request.getAssetType());
        }
        
        if (request.getName() != null) {
            asset.setName(request.getName());
        }
        
        // Serial number is immutable - never update
        
        if (request.getAcquisitionDate() != null) {
            asset.setAcquisitionDate(request.getAcquisitionDate());
        }
        
        if (request.getStatus() != null) {
            asset.setStatus(request.getStatus());
        }
        
        if (request.getLocation() != null) {
            asset.setLocation(request.getLocation());
        }
        
        if (request.getAssignedUser() != null) {
            asset.setAssignedUser(request.getAssignedUser());
        }
        
        if (request.getAssignedUserEmail() != null) {
            asset.setAssignedUserEmail(request.getAssignedUserEmail());
        }
        
        if (request.getAssignmentDate() != null) {
            asset.setAssignmentDate(request.getAssignmentDate());
        }
        
        if (request.getLocationUpdateDate() != null) {
            asset.setLocationUpdateDate(request.getLocationUpdateDate());
        }
        
        if (request.getNotes() != null) {
            asset.setNotes(request.getNotes());
        }
        
        if (request.getCustomFields() != null) {
            asset.setCustomFields(request.getCustomFields());
        }
    }
}
