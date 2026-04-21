package com.company.assetmanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for the User Management API.
 * 
 * This configuration provides comprehensive API documentation accessible at:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * - OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
 * 
 * Features:
 * - Complete API endpoint documentation
 * - Request/response schema definitions
 * - Authentication and authorization requirements
 * - Example requests and responses
 * - Error code documentation
 * - Interactive API testing via Swagger UI
 * 
 * Security:
 * - JWT Bearer token authentication configured
 * - All protected endpoints require Authorization header
 * - Token format: "Bearer {access_token}"
 * 
 * @author IT Asset Management System
 * @version 1.0
 * @since 2024-01-15
 */
@Configuration
public class OpenApiConfig {
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    @Value("${spring.application.name:IT Asset Management}")
    private String applicationName;
    
    @Value("${server.servlet.context-path:/}")
    private String contextPath;
    
    @Value("${swagger.server.url:}")
    private String swaggerServerUrl;
    
    /**
     * Configures OpenAPI documentation for the User Management API.
     * 
     * Defines:
     * - API metadata (title, version, description, contact, license)
     * - Server URLs for different environments
     * - Security schemes (JWT Bearer authentication)
     * - Global security requirements
     * 
     * @return configured OpenAPI instance
     */
    @Bean
    public OpenAPI userManagementOpenAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .servers(apiServers())
            .components(securityComponents())
            .addSecurityItem(securityRequirement());
    }
    
    /**
     * Defines API metadata including title, version, description, and contact information.
     * 
     * @return API info object
     */
    private Info apiInfo() {
        return new Info()
            .title("User Management API")
            .version("1.0.0")
            .description("""
                # User Management API Documentation
                
                ## Overview
                
                The User Management API provides comprehensive user account lifecycle management for the 
                IT Infrastructure Asset Management System. This API enables secure authentication, 
                authorization, user CRUD operations, role management, and profile self-service capabilities.
                
                ## Key Features
                
                - **Authentication**: JWT-based stateless authentication with access and refresh tokens
                - **Authorization**: Role-based access control (RBAC) with three roles
                - **User Management**: Complete CRUD operations for user accounts
                - **Role Management**: Assign and revoke roles with permission enforcement
                - **Profile Management**: Self-service profile updates and password changes
                - **Security**: Account locking, password complexity, session tracking, and audit logging
                
                ## Authentication Flow
                
                1. **Login**: POST /api/v1/auth/login with username and password
                2. **Receive Tokens**: Get access token (30 min) and refresh token (24 hours)
                3. **Use Access Token**: Include in Authorization header: `Bearer {access_token}`
                4. **Refresh Token**: POST /api/v1/auth/refresh when access token expires
                5. **Logout**: POST /api/v1/auth/logout to invalidate session
                
                ## Roles and Permissions
                
                ### Administrator
                - Full access to all user management operations
                - Create, update, delete users
                - Assign and revoke roles
                - Enable and disable accounts
                - View all users
                
                ### Asset Manager
                - View all users
                - View user details
                - Cannot modify users or roles
                
                ### Viewer
                - View all users
                - View user details
                - Cannot modify users or roles
                
                ### All Authenticated Users
                - View own profile
                - Update own profile (email only)
                - Change own password
                
                ## Error Handling
                
                All error responses follow a consistent structure:
                
                ```json
                {
                  "error": {
                    "type": "ERROR_TYPE",
                    "message": "Human-readable error message",
                    "details": {},
                    "timestamp": "2024-01-15T10:30:00Z",
                    "requestId": "req-123456"
                  }
                }
                ```
                
                ### Common Error Types
                
                - `VALIDATION_ERROR`: Request validation failed
                - `AUTHENTICATION_ERROR`: Invalid credentials or token
                - `ACCOUNT_LOCKED`: Account locked due to failed login attempts
                - `ACCOUNT_DISABLED`: Account is inactive
                - `INSUFFICIENT_PERMISSIONS`: User lacks required permissions
                - `DUPLICATE_USERNAME`: Username already exists
                - `DUPLICATE_EMAIL`: Email already exists
                - `USER_NOT_FOUND`: User ID not found
                - `INVALID_STATUS_TRANSITION`: Invalid account status change
                
                ## Rate Limiting
                
                - **Authenticated users**: 1000 requests per hour
                - **Unauthenticated users**: 100 requests per hour
                - Rate limit headers included in responses
                
                ## Pagination
                
                List endpoints support pagination with query parameters:
                - `page`: Page number (0-indexed, default: 0)
                - `size`: Items per page (default: 20, max: 100)
                - `sort`: Sort field and direction (e.g., `createdAt,desc`)
                
                ## Security Best Practices
                
                1. **Always use HTTPS** in production environments
                2. **Store tokens securely** (HttpOnly cookies recommended)
                3. **Implement token refresh** before access token expires
                4. **Handle 401 responses** by redirecting to login
                5. **Never log or expose** password values or hashes
                6. **Validate all inputs** on both client and server
                7. **Follow password complexity** requirements
                
                ## Support
                
                For API support, contact the development team or refer to the comprehensive 
                API documentation and usage guide included with this system.
                """)
            .contact(new Contact()
                .name("IT Asset Management Team")
                .email("support@example.com")
                .url("https://example.com/support"))
            .license(new License()
                .name("Proprietary")
                .url("https://example.com/license"));
    }
    
    /**
     * Defines server URLs for different environments.
     * Uses environment variable SWAGGER_SERVER_URL if provided,
     * otherwise falls back to localhost for local development.
     * 
     * @return list of server configurations
     */
    private List<Server> apiServers() {
        // If SWAGGER_SERVER_URL is provided (e.g., from ECS), use it as the primary server
        if (swaggerServerUrl != null && !swaggerServerUrl.isEmpty()) {
            Server deployedServer = new Server()
                .url(swaggerServerUrl)
                .description("Deployed environment");
            
            // Also include localhost for local testing
            Server localServer = new Server()
                .url("http://localhost:" + serverPort + contextPath)
                .description("Local development server");
            
            return List.of(deployedServer, localServer);
        }
        
        // Default: local development configuration
        Server localServer = new Server()
            .url("http://localhost:" + serverPort + contextPath)
            .description("Local development server");
        
        return List.of(localServer);
    }
    
    /**
     * Configures security components including JWT Bearer authentication scheme.
     * 
     * @return security components configuration
     */
    private Components securityComponents() {
        return new Components()
            .addSecuritySchemes("bearerAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("""
                    JWT Bearer token authentication.
                    
                    To authenticate:
                    1. Obtain access token via POST /api/v1/auth/login
                    2. Include token in Authorization header: `Bearer {access_token}`
                    3. Access token expires after 30 minutes
                    4. Use refresh token to obtain new access token
                    
                    Example:
                    ```
                    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                    ```
                    """));
    }
    
    /**
     * Defines global security requirement for JWT authentication.
     * 
     * @return security requirement configuration
     */
    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList("bearerAuth");
    }
}
