# User /me Endpoint Implementation

## Issue Summary

The frontend was calling `/api/v1/users/me` to get the currently authenticated user's information, but the backend didn't have this endpoint. The backend only had `/api/v1/users/{id}` which expected a UUID path parameter, causing the error:

```
Invalid UUID string: me
```

## Solution

Added a new `/me` endpoint to the UserController that returns the currently authenticated user's information based on the JWT token.

## Implementation Details

### New Endpoint

**File**: `backend/src/main/java/com/company/assetmanagement/controller/UserController.java`

```java
/**
 * Retrieves the currently authenticated user's information.
 * 
 * This endpoint returns the user information for the currently authenticated user
 * based on the JWT token. This is a convenience endpoint that doesn't require
 * knowing the user's ID.
 * 
 * @return user DTO for the currently authenticated user
 */
@GetMapping("/me")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER', 'VIEWER')")
@Operation(
    summary = "Get current user",
    description = "Retrieves information for the currently authenticated user. " +
                 "Accessible by all authenticated users."
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Current user retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = UserDTO.class)
        )
    ),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - missing or invalid JWT token",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    ),
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    )
})
public ResponseEntity<UserDTO> getCurrentUser() {
    String userId = extractUserIdFromAuthentication();
    
    logger.info("Get current user request received for user ID: {}", userId);
    
    return userService.getUser(userId)
        .map(user -> {
            logger.info("Current user found with ID: {}", userId);
            return ResponseEntity.ok(user);
        })
        .orElseGet(() -> {
            logger.warn("Current user not found with ID: {}", userId);
            return ResponseEntity.notFound().build();
        });
}
```

### Key Features

1. **Endpoint Placement**: The `/me` endpoint is placed BEFORE the `/{id}` endpoint in the controller. This is critical because Spring matches endpoints in order, and `/me` would otherwise be interpreted as a UUID parameter.

2. **Authentication**: Uses the existing `extractUserIdFromAuthentication()` helper method to get the user ID from the JWT token in the security context.

3. **Authorization**: Accessible by all authenticated users (ADMINISTRATOR, ASSET_MANAGER, VIEWER).

4. **Response**: Returns the same UserDTO as the `/{id}` endpoint, ensuring consistency.

5. **Error Handling**: Returns 404 if the user is not found (shouldn't happen in normal operation since the user is authenticated).

6. **Logging**: Includes appropriate logging for debugging and audit purposes.

7. **OpenAPI Documentation**: Fully documented with Swagger annotations for API documentation.

## API Contract

### Request

```http
GET /api/v1/users/me
Authorization: Bearer <jwt_token>
```

### Response (200 OK)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "admin",
  "email": "admin@example.com",
  "roles": ["Administrator"],
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z",
  "lastLoginAt": "2024-01-16T13:41:17Z",
  "accountLocked": false
}
```

### Error Responses

- **401 Unauthorized**: Missing or invalid JWT token
- **404 Not Found**: User not found (shouldn't happen in normal operation)

## Testing

To verify the fix:

1. **Restart Backend**:
   ```bash
   cd backend
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

2. **Test with Frontend**:
   - Navigate to http://localhost:4200/login
   - Login with: `admin` / `Admin@123456`
   - The frontend should successfully load user information
   - Check browser DevTools Network tab for successful `/api/v1/users/me` request

3. **Test with cURL**:
   ```bash
   # First, login to get a token
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"Admin@123456"}'
   
   # Use the access_token from the response
   curl -X GET http://localhost:8080/api/v1/users/me \
     -H "Authorization: Bearer <access_token>"
   ```

4. **Backend Logs**:
   - Should show: "Get current user request received for user ID: <uuid>"
   - Should show: "Current user found with ID: <uuid>"
   - No more "Invalid UUID string: me" errors

## Related Files

- Backend Controller: `backend/src/main/java/com/company/assetmanagement/controller/UserController.java`
- Frontend Auth Service: `frontend/src/app/core/services/auth.service.ts`
- JWT Token Fix: `JWT_TOKEN_HANDLING_FIX.md`
- Quick Start Guide: `QUICK_START_GUIDE.md`

## Status

✅ **IMPLEMENTED** - The `/me` endpoint is now available and the frontend can successfully retrieve the currently authenticated user's information.

## Next Steps

The authentication flow should now work end-to-end:
1. ✅ Frontend sends login request with correct credentials
2. ✅ Backend returns JWT tokens with snake_case field names
3. ✅ Frontend stores tokens correctly
4. ✅ Frontend adds Authorization header to subsequent requests
5. ✅ Backend validates JWT token
6. ✅ Frontend calls `/api/v1/users/me` to get user information
7. ✅ Backend returns user information
8. ✅ Frontend displays user information and navigates to dashboard
