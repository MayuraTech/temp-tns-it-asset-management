package com.company.assetmanagement.exception;

import java.util.UUID;

/**
 * Exception thrown when attempting to deallocate or reassign an asset that is not currently assigned.
 * 
 * This exception indicates that the asset has no active assignment and therefore
 * cannot be deallocated or reassigned.
 */
public class AssetNotAssignedException extends RuntimeException {
    
    private final UUID assetId;
    
    public AssetNotAssignedException(UUID assetId) {
        super("Asset " + assetId + " is not currently assigned");
        this.assetId = assetId;
    }
    
    public AssetNotAssignedException(UUID assetId, String message) {
        super(message);
        this.assetId = assetId;
    }
    
    public UUID getAssetId() {
        return assetId;
    }
}
