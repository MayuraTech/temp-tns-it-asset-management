# Security Configuration

This package contains the Spring Security configuration with JWT authentication for the IT Infrastructure Asset Management application.

## Components

### SecurityConfig
Main security configuration class that sets up:
- **JWT Authentication**: Stateless authentication using JWT tokens
- **CORS Configuration**: Cross-Origin Resource Sharing settings for frontend integration
- **Security Headers**: 
  - Content Security Policy (CSP)
  - XSS Protection
  - Frame Options (deny)
  - HTTP Strict Transport Security (HSTS)
  - X-Content-Type-Options
- **Password Encoding**: BCrypt with strength 10
- **Authorization Rules**: Public and protected endpoint configuration

### JwtTokenProvider
Handles JWT token operations:
- **Token Generation**: Creates access tokens with 30-minute expiration
- **Refresh Tokens**: Generates refresh tokens with 24-hour expiration
- **Token Validation**: Validates token signature, expiration, and format
- **Username Extraction**: Extracts username from valid tokens
- **Role Claims**: Includes user roles in token claims

### JwtAuthenticationFilter
Spring Security filter that:
- Extracts JWT tokens from Authorization header (Bearer scheme)
- Validates tokens using JwtTokenProvider
- Loads user details and sets authentication in SecurityContext
- Runs once per request

### CustomUserDetailsService
UserDetailsService implementation for loading user details.
- Currently a placeholder that will be replaced with database-backed implementation
- Will be integrated with User entity and UserRepository in future tasks

## Configuration Properties

Add these properties to `application.properties`:

```properties
# JWT Configuration
jwt.secret=${JWT_SECRET:defaultSecretKeyForDevelopmentOnlyMinimum256BitsRequired}
jwt.expiration=1800000
jwt.refresh-expiration=86400000

# CORS Configuration
cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:4200}
```

### Environment Variables

For production, set these environment variables:
- `JWT_SECRET`: Strong secret key (minimum 256 bits / 32 characters)
- `CORS_ALLOWED_ORIGINS`: Comma-separated list of allowed origins

Example:
```bash
export JWT_SECRET="your-very-long-and-secure-secret-key-here-minimum-32-characters"
export CORS_ALLOWED_ORIGINS="https://app.example.com,https://admin.example.com"
```

## Security Features

### Authentication
- **Stateless**: No server-side session storage
- **JWT Tokens**: Self-contained tokens with user information
- **Token Expiration**: Access tokens expire after 30 minutes
- **Refresh Tokens**: Long-lived tokens for obtaining new access tokens

### Authorization
- **Method Security**: Use `@PreAuthorize` annotations on controller methods
- **Role-Based**: Supports ADMINISTRATOR, ASSET_MANAGER, and VIEWER roles
- **Endpoint Protection**: All endpoints except public ones require authentication

### Security Headers
- **CSP**: Prevents XSS attacks by restricting resource loading
- **HSTS**: Forces HTTPS connections for 1 year
- **Frame Options**: Prevents clickjacking attacks
- **XSS Protection**: Browser-level XSS protection
- **Content Type Options**: Prevents MIME-sniffing attacks

### CORS
- **Allowed Origins**: Configurable list of allowed frontend origins
- **Allowed Methods**: GET, POST, PUT, PATCH, DELETE, OPTIONS
- **Credentials**: Supports cookies and authorization headers
- **Preflight Caching**: 1-hour cache for OPTIONS requests

## Usage Examples

### Generating Tokens

```java
@Autowired
private JwtTokenProvider tokenProvider;

@Autowired
private AuthenticationManager authenticationManager;

public String login(String username, String password) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(username, password)
    );
    
    return tokenProvider.generateToken(authentication);
}
```

### Protecting Endpoints

```java
@RestController
@RequestMapping("/api/v1/assets")
public class AssetController {
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER', 'VIEWER')")
    public ResponseEntity<List<Asset>> getAssets() {
        // Implementation
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    public ResponseEntity<Asset> createAsset(@RequestBody AssetRequest request) {
        // Implementation
    }
}
```

### Making Authenticated Requests

Frontend should include JWT token in Authorization header:

```typescript
const headers = {
  'Authorization': `Bearer ${accessToken}`,
  'Content-Type': 'application/json'
};

fetch('/api/v1/assets', { headers })
  .then(response => response.json())
  .then(data => console.log(data));
```

## Testing

Unit tests are provided for:
- JWT token generation and validation
- Password encoding
- Security configuration
- CORS settings
- Security headers

Run tests:
```bash
mvn test -Dtest=JwtTokenProviderTest,SecurityConfigTest
```

## Security Best Practices

1. **Never commit JWT_SECRET**: Use environment variables in production
2. **Use HTTPS**: Always use HTTPS in production for secure token transmission
3. **Rotate Secrets**: Periodically rotate JWT secret keys
4. **Short Expiration**: Keep access token expiration short (30 minutes)
5. **Secure Storage**: Store tokens securely on client (HttpOnly cookies preferred)
6. **Validate Input**: Always validate and sanitize user inputs
7. **Rate Limiting**: Implement rate limiting for authentication endpoints
8. **Audit Logging**: Log all authentication and authorization events

## Future Enhancements

- [ ] Integrate with User entity and database
- [ ] Implement refresh token rotation
- [ ] Add token blacklisting for logout
- [ ] Implement account lockout after failed attempts
- [ ] Add session timeout tracking
- [ ] Implement password complexity validation
- [ ] Add multi-factor authentication support
- [ ] Implement rate limiting
