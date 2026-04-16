package com.company.assetmanagement.exception;

import java.util.UUID;

/**
 * Exception thrown when attempting to assign an asset that is already assigned.
 * 
 * This exception indicates a conflict state where the asset has an active assignment
 * and cannot be assigned again without first deallocating it.
 */
public class AssetAlreadyAssignedException extends RuntimeException {
    
    private final UUID assetId;
    
    public AssetAlreadyAssignedException(UUID assetId) {
        super("Asset " + assetId + " is already assigned");
        this.assetId = assetId;
    }
    
    public AssetAlreadyAssignedException(UUID assetId, String message) {
        super(message);
        this.assetId = assetId;
    }
    
    public UUID getAssetId() {
        return assetId;
    }
}
