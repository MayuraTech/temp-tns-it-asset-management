package com.company.assetmanagement.model;

/**
 * Enumeration of actions that can be performed in the system.
 * Used for permission checking and authorization.
 */
public enum Action {
    CREATE_ASSET,
    UPDATE_ASSET,
    DELETE_ASSET,
    VIEW_ASSET,
    MANAGE_USERS,
    VIEW_AUDIT_LOG,
    EXPORT_DATA,
    IMPORT_DATA,
    CONFIGURE_SYSTEM,
    CREATE_TICKET,
    APPROVE_TICKET,
    REJECT_TICKET,
    COMPLETE_TICKET,
    VIEW_TICKET
}
