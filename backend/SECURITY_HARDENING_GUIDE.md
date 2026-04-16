# Security Hardening Implementation Guide
## Module 1: User Management

**Purpose**: This guide provides step-by-step instructions for implementing the security recommendations from the security review report.

---

## Priority 1: Critical Security Enhancements

### 1.1 Secure JWT Secret Storage

**Current Issue**: JWT secret is stored in application.properties with a default value.

**Solution**: Use environment variables and secure vault for production.

#### Step 1: Update Application Configuration

```properties
# application-prod.properties
jwt.secret=${JWT_SECRET}
# Remove default value in production
```

#### Step 2: Set Environment Variable (Development)

```bash
# Linux/Mac
export JWT_SECRET="your-very-long-and-secure-secret-key-minimum-32-characters-required-for-hs256-algorithm"

# Windows
set JWT_SECRET=your-very-long-and-secure-secret-key-minimum-32-characters-required-for-hs256-algorithm
```

#### Step 3: AWS Secrets Manager Integration (Production)

```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>com.amazonaws.secretsmanager</groupId>
    <artifactId>aws-secretsmanager-caching-java</artifactId>
    <version>1.0.2</version>
</dependency>
```

```java
// Create SecretManagerConfig.java
@Configuration
public class SecretManagerConfig {
    
    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }
    
    @Bean
    public String jwtSecret(SecretsManagerClient client) {
        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId("it-asset-management/jwt-secret")
                .build();
        
        GetSecretValueResponse response = client.getSecretValue(request);
        return response.secretString();
    }
}
```

#### Step 4: Azure Key Vault Integration (Alternative)

```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-secrets</artifactId>
    <version>4.6.0</version>
</dependency>
```

```java
// Create KeyVaultConfig.java
@Configuration
public class KeyVaultConfig {
    
    @Value("${azure.keyvault.uri}")
    private String keyVaultUri;
    
    @Bean
    public SecretClient secretClient() {
        return new SecretClientBuilder()
                .vaultUrl(keyVaultUri)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
    }
    
    @Bean
    public String jwtSecret(SecretClient client) {
        KeyVaultSecret secret = client.getSecret("jwt-secret");
        return secret.getValue();
    }
}
```

---

### 1.2 Implement Rate Limiting

**Current Issue**: No rate limiting implemented, vulnerable to brute force attacks.

**Solution**: Implement Bucket4j for rate limiting.

#### Step 1: Add Dependencies

```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.7.0</version>
</dependency>
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-redis</artifactId>
    <version>8.7.0</version>
</dependency>
```

#### Step 2: Create Rate Limit Configuration

```java
// Create RateLimitConfig.java
package com.company.assetmanagement.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting configuration using Bucket4j.
 * 
 * Implements token bucket algorithm for rate limiting:
 * - Login endpoint: 5 requests per minute per IP
 * - Token refresh: 10 requests per minute per IP
 * - Password change: 3 requests per minute per user
 */
@Configuration
public class RateLimitConfig {
    
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    
    /**
     * Creates a bucket for login rate limiting.
     * Allows 5 requests per minute with refill every 12 seconds.
     */
    @Bean
    public Bucket loginBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
    
    /**
     * Creates a bucket for token refresh rate limiting.
     * Allows 10 requests per minute with refill every 6 seconds.
     */
    @Bean
    public Bucket refreshBucket() {
        Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
    
    /**
     * Creates a bucket for password change rate limiting.
     * Allows 3 requests per minute with refill every 20 seconds.
     */
    @Bean
    public Bucket passwordChangeBucket() {
        Bandwidth limit = Bandwidth.classic(3, Refill.intervally(3, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
    
    /**
     * Resolves a bucket for a given key (IP address or user ID).
     * Creates new bucket if not exists.
     */
    public Bucket resolveBucket(String key, Bandwidth limit) {
        return cache.computeIfAbsent(key, k -> 
            Bucket.builder()
                .addLimit(limit)
                .build()
        );
    }
}
```

#### Step 3: Create Rate Limit Interceptor

```java
// Create RateLimitInterceptor.java
package com.company.assetmanagement.interceptor;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor for rate limiting HTTP requests.
 * 
 * Checks rate limit before processing request and returns 429 if exceeded.
 * Adds rate limit headers to response for client information.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);
    
    private final Bucket bucket;
    
    public RateLimitInterceptor(Bucket bucket) {
        this.bucket = bucket;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) throws Exception {
        
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            // Request allowed
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            // Rate limit exceeded
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.sendError(HttpServletResponse.SC_TOO_MANY_REQUESTS, 
                "Rate limit exceeded. Please try again later.");
            
            logger.warn("Rate limit exceeded for IP: {}", request.getRemoteAddr());
            return false;
        }
    }
}
```

#### Step 4: Register Interceptor

```java
// Update WebMvcConfig.java or create new
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/v1/auth/login", 
                               "/api/v1/auth/refresh",
                               "/api/v1/profile/change-password");
    }
}
```

---

### 1.3 Dependency Vulnerability Scanning

**Current Issue**: No automated dependency vulnerability scanning.

**Solution**: Implement OWASP Dependency-Check and Snyk.

#### Step 1: Add OWASP Dependency-Check Plugin

```xml
<!-- Add to pom.xml -->
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>8.4.0</version>
    <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>
        <suppressionFiles>
            <suppressionFile>dependency-check-suppressions.xml</suppressionFile>
        </suppressionFiles>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

#### Step 2: Create Suppression File

```xml
<!-- Create dependency-check-suppressions.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<suppressions xmlns="https://jeremylong.github.io/DependencyCheck/dependency-suppression.1.3.xsd">
    <!-- Add suppressions for false positives here -->
    <!-- Example:
    <suppress>
        <notes>False positive - not applicable to our usage</notes>
        <cve>CVE-2021-12345</cve>
    </suppress>
    -->
</suppressions>
```

#### Step 3: Run Dependency Check

```bash
# Run dependency check
mvn dependency-check:check

# Generate report
mvn dependency-check:aggregate

# View report at: target/dependency-check-report.html
```

#### Step 4: Set Up GitHub Actions for Automated Scanning

```yaml
# Create .github/workflows/security-scan.yml
name: Security Scan

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  schedule:
    - cron: '0 0 * * 0'  # Weekly on Sunday

jobs:
  dependency-check:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Run OWASP Dependency Check
      run: mvn dependency-check:check
    
    - name: Upload Dependency Check Report
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: dependency-check-report
        path: target/dependency-check-report.html
```

---

## Priority 2: High Priority Enhancements

### 2.1 Enhanced CSP Configuration

**Current Issue**: CSP allows 'unsafe-inline' which weakens XSS protection.

**Solution**: Implement nonce-based CSP.

#### Step 1: Create CSP Nonce Generator

```java
// Create CspNonceGenerator.java
package com.company.assetmanagement.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates cryptographically secure nonces for Content Security Policy.
 */
@Component
public class CspNonceGenerator {
    
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int NONCE_LENGTH = 16;
    
    /**
     * Generates a new CSP nonce and stores it in request attribute.
     */
    public String generateNonce(HttpServletRequest request) {
        byte[] nonceBytes = new byte[NONCE_LENGTH];
        RANDOM.nextBytes(nonceBytes);
        String nonce = Base64.getEncoder().encodeToString(nonceBytes);
        request.setAttribute("cspNonce", nonce);
        return nonce;
    }
    
    /**
     * Retrieves the CSP nonce from request attribute.
     */
    public String getNonce(HttpServletRequest request) {
        return (String) request.getAttribute("cspNonce");
    }
}
```

#### Step 2: Create CSP Filter

```java
// Create CspFilter.java
package com.company.assetmanagement.filter;

import com.company.assetmanagement.security.CspNonceGenerator;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter that adds Content Security Policy header with nonce.
 */
@Component
public class CspFilter implements Filter {
    
    private final CspNonceGenerator nonceGenerator;
    
    public CspFilter(CspNonceGenerator nonceGenerator) {
        this.nonceGenerator = nonceGenerator;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Generate nonce
        String nonce = nonceGenerator.generateNonce(httpRequest);
        
        // Build CSP header with nonce
        String csp = String.format(
            "default-src 'self'; " +
            "script-src 'self' 'nonce-%s'; " +
            "style-src 'self' 'nonce-%s'; " +
            "img-src 'self' data: https:; " +
            "font-src 'self' data:; " +
            "connect-src 'self'; " +
            "frame-ancestors 'none'; " +
            "base-uri 'self'; " +
            "form-action 'self'",
            nonce, nonce
        );
        
        httpResponse.setHeader("Content-Security-Policy", csp);
        
        chain.doFilter(request, response);
    }
}
```

#### Step 3: Update SecurityConfig

```java
// Remove CSP from SecurityConfig.java (now handled by CspFilter)
.headers(headers -> headers
    // Remove: .contentSecurityPolicy(...)
    .xssProtection(xss -> xss.headerValue(HeaderValue.ENABLED_MODE_BLOCK))
    .frameOptions(frame -> frame.deny())
    // ... rest of headers
)
```

---

### 2.2 Token Blacklisting

**Current Issue**: Tokens remain valid until expiration even after logout.

**Solution**: Implement Redis-based token blacklist.

#### Step 1: Add Redis Dependencies

```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

#### Step 2: Configure Redis

```properties
# Add to application.properties
spring.redis.host=${REDIS_HOST:localhost}
spring.redis.port=${REDIS_PORT:6379}
spring.redis.password=${REDIS_PASSWORD:}
spring.redis.timeout=2000ms
spring.cache.type=redis
spring.cache.redis.time-to-live=1800000
```

#### Step 3: Create Token Blacklist Service

```java
// Create TokenBlacklistService.java
package com.company.assetmanagement.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing blacklisted JWT tokens.
 * Uses Redis for distributed token blacklist storage.
 */
@Service
public class TokenBlacklistService {
    
    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    private static final long TOKEN_EXPIRATION_HOURS = 24;
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Adds a token to the blacklist.
     * Token will be automatically removed after expiration.
     */
    public void blacklistToken(String token) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", 
            Duration.ofHours(TOKEN_EXPIRATION_HOURS));
    }
    
    /**
     * Checks if a token is blacklisted.
     */
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * Removes a token from the blacklist (for testing purposes).
     */
    public void removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.delete(key);
    }
}
```

#### Step 4: Update JwtAuthenticationFilter

```java
// Update JwtAuthenticationFilter.java
@Autowired
private TokenBlacklistService tokenBlacklistService;

@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) throws ServletException, IOException {
    try {
        String jwt = getJwtFromRequest(request);
        
        if (StringUtils.hasText(jwt)) {
            // Check if token is blacklisted
            if (tokenBlacklistService.isBlacklisted(jwt)) {
                logger.warn("Blacklisted token attempted authentication");
                filterChain.doFilter(request, response);
                return;
            }
            
            // ... rest of validation
        }
    } catch (Exception ex) {
        // ... error handling
    }
    
    filterChain.doFilter(request, response);
}
```

#### Step 5: Update Logout to Blacklist Token

```java
// Update AuthenticationServiceImpl.java
@Override
public void logout(String userId) {
    // ... existing logout logic
    
    // Get current token from request
    HttpServletRequest request = 
        ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
            .getRequest();
    String token = extractTokenFromRequest(request);
    
    if (token != null) {
        tokenBlacklistService.blacklistToken(token);
    }
    
    // ... rest of logout logic
}
```

---

### 2.3 Security Monitoring and Alerting

**Current Issue**: No real-time security monitoring or alerting.

**Solution**: Implement security event monitoring with alerts.

#### Step 1: Create Security Event Service

```java
// Create SecurityEventService.java
package com.company.assetmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for monitoring and alerting on security events.
 */
@Service
public class SecurityEventService {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityEventService.class);
    
    private static final int FAILED_LOGIN_THRESHOLD = 10;
    private static final int TIME_WINDOW_MINUTES = 5;
    
    private final Map<String, AtomicInteger> failedLoginAttempts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastFailedAttempt = new ConcurrentHashMap<>();
    
    /**
     * Records a failed login attempt and triggers alert if threshold exceeded.
     */
    public void recordFailedLogin(String ipAddress, String username) {
        String key = ipAddress + ":" + username;
        
        // Clean up old entries
        cleanupOldEntries();
        
        // Increment counter
        AtomicInteger counter = failedLoginAttempts.computeIfAbsent(key, k -> new AtomicInteger(0));
        int attempts = counter.incrementAndGet();
        lastFailedAttempt.put(key, LocalDateTime.now());
        
        // Check threshold
        if (attempts >= FAILED_LOGIN_THRESHOLD) {
            triggerSecurityAlert("Multiple failed login attempts", 
                Map.of(
                    "ipAddress", ipAddress,
                    "username", username,
                    "attempts", String.valueOf(attempts)
                ));
        }
    }
    
    /**
     * Records a successful login after failed attempts.
     */
    public void recordSuccessfulLogin(String ipAddress, String username) {
        String key = ipAddress + ":" + username;
        failedLoginAttempts.remove(key);
        lastFailedAttempt.remove(key);
    }
    
    /**
     * Triggers a security alert (log, email, Slack, etc.).
     */
    private void triggerSecurityAlert(String message, Map<String, String> details) {
        logger.error("SECURITY ALERT: {} - Details: {}", message, details);
        
        // TODO: Implement email notification
        // TODO: Implement Slack notification
        // TODO: Implement PagerDuty integration
    }
    
    /**
     * Cleans up entries older than time window.
     */
    private void cleanupOldEntries() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(TIME_WINDOW_MINUTES);
        lastFailedAttempt.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
    }
}
```

---

## Priority 3: Medium Priority Enhancements

### 3.1 Password Policy Enhancements

#### Password History Implementation

```java
// Create PasswordHistory entity
@Entity
@Table(name = "PasswordHistory")
public class PasswordHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 255)
    private String passwordHash;
    
    @Column(nullable = false)
    private LocalDateTime changedAt;
    
    // Getters, setters
}

// Create PasswordHistoryRepository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, UUID> {
    List<PasswordHistory> findTop5ByUserOrderByChangedAtDesc(User user);
}

// Update ProfileServiceImpl
@Override
public void changePassword(String userId, ChangePasswordRequest request) {
    // ... existing validation
    
    // Check password history
    List<PasswordHistory> history = passwordHistoryRepository
        .findTop5ByUserOrderByChangedAtDesc(user);
    
    for (PasswordHistory entry : history) {
        if (passwordEncoder.matches(request.getNewPassword(), entry.getPasswordHash())) {
            throw new ValidationException("Cannot reuse recent passwords");
        }
    }
    
    // Save to history
    PasswordHistory historyEntry = new PasswordHistory();
    historyEntry.setUser(user);
    historyEntry.setPasswordHash(user.getPasswordHash());
    historyEntry.setChangedAt(LocalDateTime.now());
    passwordHistoryRepository.save(historyEntry);
    
    // ... rest of password change logic
}
```

---

### 3.2 Additional Security Headers

```java
// Update SecurityConfig.java
.headers(headers -> headers
    // ... existing headers
    .referrerPolicy(referrer -> 
        referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
    .permissionsPolicy(permissions -> 
        permissions.policy("geolocation=(), microphone=(), camera=()"))
)
```

---

## Testing Security Enhancements

### Test Rate Limiting

```java
@Test
public void testRateLimiting() {
    // Make 6 requests (limit is 5)
    for (int i = 0; i < 6; i++) {
        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
            "/api/v1/auth/login",
            new LoginRequest("testuser", "password"),
            TokenResponse.class
        );
        
        if (i < 5) {
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        } else {
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        }
    }
}
```

### Test Token Blacklisting

```java
@Test
public void testTokenBlacklisting() {
    // Login
    TokenResponse tokens = authService.login(new LoginRequest("testuser", "password"));
    
    // Logout (blacklists token)
    authService.logout(userId);
    
    // Try to use blacklisted token
    assertThatThrownBy(() -> 
        makeAuthenticatedRequest(tokens.getAccessToken())
    ).isInstanceOf(UnauthorizedException.class);
}
```

---

## Deployment Checklist

### Pre-Production
- [ ] Update JWT secret to strong random value
- [ ] Configure secure vault (AWS Secrets Manager or Azure Key Vault)
- [ ] Set up Redis for token blacklisting
- [ ] Configure rate limiting thresholds
- [ ] Set up security monitoring and alerting
- [ ] Run OWASP Dependency Check
- [ ] Run security penetration tests
- [ ] Review and update CORS origins
- [ ] Enable HTTPS only
- [ ] Configure TLS 1.3 minimum

### Production
- [ ] Verify JWT secret is loaded from vault
- [ ] Verify rate limiting is active
- [ ] Verify token blacklisting is working
- [ ] Verify security headers are present
- [ ] Verify HTTPS is enforced
- [ ] Verify audit logging is working
- [ ] Set up log aggregation
- [ ] Configure security alerts
- [ ] Document incident response procedures
- [ ] Schedule quarterly security reviews

---

## Monitoring and Maintenance

### Daily
- Monitor failed login attempts
- Review security alerts
- Check rate limiting metrics

### Weekly
- Review audit logs
- Check for suspicious activities
- Verify backup integrity

### Monthly
- Run dependency vulnerability scan
- Review and update security policies
- Test disaster recovery procedures

### Quarterly
- Conduct security penetration testing
- Review and update security documentation
- Rotate JWT secrets
- Conduct security training

---

## Support and Resources

### Documentation
- OWASP Top 10: https://owasp.org/www-project-top-ten/
- Spring Security: https://spring.io/projects/spring-security
- JWT Best Practices: https://tools.ietf.org/html/rfc8725

### Tools
- OWASP Dependency-Check: https://owasp.org/www-project-dependency-check/
- OWASP ZAP: https://www.zaproxy.org/
- Snyk: https://snyk.io/
- Bucket4j: https://github.com/vladimir-bukhtoyarov/bucket4j

### Contact
- Security Team: security@company.com
- Emergency: security-emergency@company.com
