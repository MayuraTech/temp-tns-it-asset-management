package com.company.assetmanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation.
 * Configures API metadata, security schemes, and servers.
 */
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development server"),
                        new Server()
                                .url("https://api.example.com")
                                .description("Production server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication", createSecurityScheme()));
    }
    
    private Info apiInfo() {
        return new Info()
                .title("IT Infrastructure Asset Management API")
                .description("""
                        Comprehensive REST API for managing IT infrastructure assets.
                        
                        ## Features
                        - **Asset Management**: Complete CRUD operations for IT assets
                        - **Lifecycle Tracking**: Track assets through 7 lifecycle statuses (ORDERED, RECEIVED, DEPLOYED, IN_USE, MAINTENANCE, STORAGE, RETIRED)
                        - **Search & Filtering**: Advanced search with multiple filter criteria
                        - **Import/Export**: Bulk operations with CSV and JSON support
                        - **Audit Trail**: Complete audit logging of all operations
                        - **Role-Based Access**: Three roles (ADMINISTRATOR, ASSET_MANAGER, VIEWER)
                        
                        ## Authentication
                        All endpoints require JWT authentication. Include the JWT token in the Authorization header:
                        ```
                        Authorization: Bearer <your-jwt-token>
                        ```
                        
                        ## Asset Types
                        - SERVER
                        - WORKSTATION
                        - NETWORK_DEVICE
                        - STORAGE_DEVICE
                        - SOFTWARE_LICENSE
                        - PERIPHERAL
                        - KEYBOARD
                        - MOUSE
                        - LAPTOP
                        - MONITOR
                        - HEADSET
                        - LAPTOP_CHARGER
                        - HDMI_CABLE
                        - NETWORK_CABLE
                        - ACCESS_CARD
                        
                        ## Lifecycle Statuses
                        - **ORDERED**: Asset has been ordered but not yet received
                        - **RECEIVED**: Asset has been received and is in inventory
                        - **DEPLOYED**: Asset has been deployed but not yet in active use
                        - **IN_USE**: Asset is actively being used
                        - **MAINTENANCE**: Asset is undergoing maintenance
                        - **STORAGE**: Asset is in storage (not in active use)
                        - **RETIRED**: Asset has been retired (read-only, no further transitions)
                        
                        ## Status Transitions
                        Valid status transitions are enforced by business rules:
                        - From ORDERED: Can transition to RECEIVED
                        - From RECEIVED: Can transition to DEPLOYED
                        - From DEPLOYED: Can transition to IN_USE or STORAGE
                        - From IN_USE: Can transition to STORAGE or RETIRED
                        - From STORAGE: Can transition to DEPLOYED or RETIRED
                        - From MAINTENANCE: Can transition to any status (except RETIRED)
                        - From RETIRED: No transitions allowed (terminal state)
                        - Any status (except RETIRED): Can transition to MAINTENANCE
                        
                        ## Error Handling
                        All errors follow a consistent structure with:
                        - **type**: Error type identifier
                        - **message**: Human-readable error message
                        - **details**: Additional error details (e.g., validation errors)
                        - **timestamp**: When the error occurred
                        - **requestId**: Unique request identifier for tracking
                        
                        ## Pagination
                        List endpoints support pagination with query parameters:
                        - **page**: Page number (0-indexed, default: 0)
                        - **size**: Page size (default: 20, max: 100)
                        - **sort**: Sort field and direction (e.g., name,asc)
                        
                        ## Rate Limiting
                        - Authenticated users: 1000 requests per hour
                        - Unauthenticated users: 100 requests per hour
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("IT Asset Management Team")
                        .email("support@example.com")
                        .url("https://example.com/support"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://example.com/license"));
    }
    
    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT authentication token. Obtain by calling POST /api/v1/auth/login with valid credentials.");
    }
}
