package com.company.assetmanagement.exception;

import com.company.assetmanagement.model.LifecycleStatus;

import java.util.UUID;

/**
 * Exception thrown when attempting to assign an asset that is not in an assignable status.
 * 
 * Assets can only be assigned when their status is IN_USE, DEPLOYED, or STORAGE.
 * Assets with status ORDERED, RECEIVED, MAINTENANCE, or RETIRED cannot be assigned.
 */
public class AssetNotAssignableException extends RuntimeException {
    
    private final UUID assetId;
    private final LifecycleStatus status;
    
    public AssetNotAssignableException(UUID assetId, LifecycleStatus status) {
        super("Asset " + assetId + " cannot be assigned with status " + status);
        this.assetId = assetId;
        this.status = status;
    }
    
    public AssetNotAssignableException(UUID assetId, LifecycleStatus status, String message) {
        super(message);
        this.assetId = assetId;
        this.status = status;
    }
    
    public UUID getAssetId() {
        return assetId;
    }
    
    public LifecycleStatus getStatus() {
        return status;
    }
}
