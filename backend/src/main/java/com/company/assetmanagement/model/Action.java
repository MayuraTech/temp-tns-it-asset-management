package com.company.assetmanagement.model;

/**
 * Enumeration of actions that can be performed in the system.
 * Used for permission checking and authorization.
 */
public enum Action {
    // Asset Management Actions
    CREATE_ASSET,
    UPDATE_ASSET,
    DELETE_ASSET,
    VIEW_ASSET,
    
    // User Management Actions
    CREATE_USER,
    UPDATE_USER,
    DELETE_USER,
    VIEW_USER,
    MANAGE_USER_STATUS,
    ENABLE_USER,
    DISABLE_USER,
    ASSIGN_ROLE,
    REVOKE_ROLE,
    MANAGE_USERS,
    
    // Authentication Actions
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT,
    TOKEN_REFRESH,
    PASSWORD_CHANGE,
    
    // Audit and System Actions
    VIEW_AUDIT_LOG,
    EXPORT_DATA,
    IMPORT_DATA,
    CONFIGURE_SYSTEM,
    
    // Ticket Management Actions
    CREATE_TICKET,
    APPROVE_TICKET,
    REJECT_TICKET,
    COMPLETE_TICKET,
    VIEW_TICKET,
    
    // Authentication actions
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT,
    TOKEN_REFRESH,
    PASSWORD_CHANGE
}
