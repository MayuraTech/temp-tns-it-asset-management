# Server Status and UI Testing Guide

**Date:** April 10, 2026  
**Status:** ✅ SERVERS RUNNING SUCCESSFULLY

## Server Status

### ✅ Backend Server
- **Status:** Running
- **Port:** 8080
- **Profile:** dev
- **Database:** SQL Server (TNS-IT-DESKTOP\SQLEXPRESS)
- **Database Name:** IT_Asset
- **Startup Time:** 41.522 seconds
- **URL:** http://localhost:8080

**Key Configuration:**
- Flyway: Disabled (using Hibernate DDL auto-generation)
- JPA DDL Auto: update
- SQL Logging: Enabled (DEBUG level)
- CORS: Enabled for http://localhost:4200 and http://localhost:55047

**Startup Log Excerpt:**
```
2026-04-10 16:24:47 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat started on port 8080 (http) with context path ''
2026-04-10 16:24:48 [main] INFO  c.c.a.AssetManagementApplication - Started AssetManagementApplication in 41.522 seconds
```

### ✅ Frontend Server
- **Status:** Running
- **Port:** 51547
- **Framework:** Angular 17
- **Build:** Successful
- **URL:** http://localhost:51547

**Build Status:**
```
√ Compiled successfully.
** Angular Live Development Server is listening on localhost:51547 **
```

## Manual UI Testing Guide

### Prerequisites
- Both servers are running (confirmed above)
- Browser: Chrome, Firefox, or Edge
- Test user credentials (if database is seeded)

### Test Scenarios

#### 1. Login Page Load Test

**Steps:**
1. Open browser
2. Navigate to: http://localhost:51547
3. Verify login page loads

**Expected Results:**
- ✅ Login form displays
- ✅ Username field visible
- ✅ Password field visible
- ✅ "Remember me" checkbox visible
- ✅ Submit button visible
- ✅ "Forgot password?" link visible
- ✅ Geometric triangle accents visible (Editorial Geometry design)
- ✅ Proper color scheme (Primary: #143b7d, Surface: #faf9ff)

#### 2. Form Validation Test

**Steps:**
1. Click in username field
2. Click out (blur) without entering text
3. Observe validation error
4. Click in password field
5. Click out (blur) without entering text
6. Observe validation error

**Expected Results:**
- ✅ "Username is required" error displays below username field
- ✅ "Password is required" error displays below password field
- ✅ Submit button is disabled
- ✅ Error messages styled with secondary color (#a9371d)

#### 3. Validation Clearing Test

**Steps:**
1. Trigger validation errors (as above)
2. Enter text in username field
3. Observe error clears
4. Enter text in password field
5. Observe error clears

**Expected Results:**
- ✅ Username error clears when valid input entered
- ✅ Password error clears when valid input entered
- ✅ Submit button becomes enabled when both fields valid

#### 4. Password Visibility Toggle Test

**Steps:**
1. Enter password in password field
2. Click show/hide password icon
3. Verify password becomes visible
4. Click icon again
5. Verify password becomes hidden

**Expected Results:**
- ✅ Password initially hidden (dots/asterisks)
- ✅ Password visible as plain text when toggled
- ✅ Password hidden again when toggled back
- ✅ Icon changes to indicate state

#### 5. Successful Login Test

**Steps:**
1. Enter valid username (e.g., "admin")
2. Enter valid password (e.g., "Admin@123456")
3. Optionally check "Remember me"
4. Click Submit button

**Expected Results:**
- ✅ Loading indicator displays on submit button
- ✅ Form inputs disabled during authentication
- ✅ Submit button disabled during authentication
- ✅ On success: Redirect to dashboard (/dashboard)
- ✅ If "Remember me" checked: Token stored in localStorage/sessionStorage
- ✅ Password field cleared from memory

**API Call:**
```
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "Admin@123456",
  "rememberMe": true
}
```

#### 6. Failed Login Test

**Steps:**
1. Enter invalid username
2. Enter invalid password
3. Click Submit button

**Expected Results:**
- ✅ Loading indicator displays
- ✅ Error message displays after response
- ✅ Error message styled with secondary color
- ✅ Password field cleared
- ✅ Form re-enabled
- ✅ Username field retains value
- ✅ Error is dismissible (X button)

**Possible Error Messages:**
- "Invalid username or password"
- "Account locked due to too many failed attempts"
- "Network error - please try again"

#### 7. Keyboard Navigation Test

**Steps:**
1. Tab to username field
2. Enter username
3. Press Tab to move to password field
4. Enter password
5. Press Enter key

**Expected Results:**
- ✅ Tab key moves focus between fields
- ✅ Enter key in password field submits form
- ✅ Form submits only if valid
- ✅ Focus indicators visible (accessibility)

#### 8. Remember Me Test

**Steps:**
1. Check "Remember me" checkbox
2. Login successfully
3. Close browser completely
4. Reopen browser
5. Navigate to http://localhost:51547

**Expected Results:**
- ✅ Checkbox can be checked/unchecked
- ✅ On successful login with remember me: Token stored persistently
- ✅ On page reload: Auto-authentication attempted
- ✅ If token valid: Automatic redirect to dashboard
- ✅ If token expired: User stays on login page

#### 9. Forgot Password Navigation Test

**Steps:**
1. Click "Forgot password?" link

**Expected Results:**
- ✅ Link is clickable
- ✅ Navigation to /password-reset route
- ✅ Password reset page loads

#### 10. Account Lockout Test (Security)

**Steps:**
1. Attempt login with wrong password 5 times
2. Observe response on 5th attempt

**Expected Results:**
- ✅ After 5 failed attempts: Account locked message
- ✅ Error type: "ACCOUNT_LOCKED"
- ✅ Message indicates 15-minute lockout period
- ✅ Subsequent login attempts blocked

#### 11. Responsive Design Test

**Steps:**
1. Resize browser window to mobile size (375px width)
2. Verify layout adapts
3. Resize to tablet size (768px width)
4. Verify layout adapts
5. Resize to desktop size (1920px width)
6. Verify layout optimal

**Expected Results:**
- ✅ Mobile: Single column layout, stacked elements
- ✅ Tablet: Optimized spacing and sizing
- ✅ Desktop: Full Editorial Geometry design with triangles
- ✅ All breakpoints: Form remains functional
- ✅ Geometric accents scale appropriately

#### 12. Accessibility Test

**Steps:**
1. Use keyboard only (no mouse)
2. Navigate through entire form
3. Use screen reader (NVDA/JAWS)
4. Verify announcements

**Expected Results:**
- ✅ All elements keyboard accessible
- ✅ Focus indicators visible
- ✅ ARIA labels present
- ✅ Error messages announced
- ✅ Loading state announced
- ✅ Form labels associated with inputs

## API Endpoints to Test

### Authentication Endpoints

#### Login
```
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "Admin@123456",
  "rememberMe": true
}

Response 200:
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 1800
}

Response 401:
{
  "error": {
    "type": "INVALID_CREDENTIALS",
    "message": "Invalid username or password",
    "timestamp": "2026-04-10T10:45:00Z",
    "requestId": "req-123456"
  }
}
```

#### Token Refresh
```
POST http://localhost:8080/api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGc..."
}
```

#### Logout
```
POST http://localhost:8080/api/v1/auth/logout
Authorization: Bearer eyJhbGc...
```

## Browser DevTools Checks

### Network Tab
1. Open DevTools (F12)
2. Go to Network tab
3. Perform login
4. Verify:
   - ✅ POST request to /api/v1/auth/login
   - ✅ Request payload contains username, password, rememberMe
   - ✅ Response contains accessToken and refreshToken
   - ✅ No sensitive data in URL
   - ✅ CORS headers present

### Console Tab
1. Check for errors
2. Verify:
   - ✅ No JavaScript errors
   - ✅ No CORS errors
   - ✅ No 404 errors for assets
   - ✅ Clean console output

### Application Tab
1. Go to Application > Storage
2. After successful login with "Remember me":
   - ✅ Token stored in localStorage or sessionStorage
   - ✅ Token format: JWT (three base64 segments)
   - ✅ No password stored

## Performance Checks

### Page Load Performance
- ✅ Initial page load < 3 seconds
- ✅ Time to Interactive < 5 seconds
- ✅ First Contentful Paint < 1.5 seconds

### Authentication Performance
- ✅ Login request completes < 2 seconds
- ✅ Loading indicator visible during request
- ✅ No UI freezing

## Security Checks

### Password Security
- ✅ Password field type="password"
- ✅ Password not visible in DevTools Network tab
- ✅ Password cleared from memory after submission
- ✅ Password cleared after failed login

### Token Security
- ✅ Token transmitted over HTTPS (in production)
- ✅ Token stored securely (HttpOnly cookie preferred)
- ✅ Token includes expiration
- ✅ Refresh token mechanism available

### CORS Security
- ✅ CORS configured for specific origins
- ✅ No wildcard (*) CORS in production
- ✅ Credentials allowed for authenticated requests

## Design System Compliance

### Editorial Geometry Standards

#### Colors
- ✅ Primary: #143b7d (Blue 800)
- ✅ Secondary: #a9371d (Red-Orange accent)
- ✅ Surface: #faf9ff (Light purple base)
- ✅ On-surface: #1a1b20 (Never pure black)

#### Typography
- ✅ Headings: Manrope font
- ✅ Body: Inter font
- ✅ Display text: 48px with -2% letter-spacing
- ✅ Headline: 30px with -0.75px tracking

#### Components
- ✅ No 1px solid borders for sectioning
- ✅ Glassmorphism on navigation elements
- ✅ Geometric triangle accents
- ✅ 80px breathing room around triangles
- ✅ 8px border radius (no sharp corners)
- ✅ Blue-tinted shadows

#### Spacing
- ✅ Consistent spacing scale used
- ✅ Adequate white space
- ✅ Proper padding and margins

## Test Data

### Valid Test Users
(Assuming database is seeded)

```
Username: admin
Password: Admin@123456
Role: Administrator

Username: manager
Password: Manager@123456
Role: Asset_Manager

Username: viewer
Password: Viewer@123456
Role: Viewer
```

### Invalid Test Cases
```
Username: invalid_user
Password: wrong_password
Expected: 401 Unauthorized

Username: (empty)
Password: (empty)
Expected: Validation errors

Username: admin
Password: (5 wrong attempts)
Expected: Account locked
```

## Automated Testing Script

### Using cURL

```bash
# Test successful login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123456","rememberMe":true}'

# Test failed login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"invalid","password":"wrong","rememberMe":false}'
```

### Using Postman

1. Import collection from API documentation
2. Set environment variables:
   - BASE_URL: http://localhost:8080
   - FRONTEND_URL: http://localhost:51547
3. Run login tests
4. Verify responses

## Troubleshooting

### Backend Not Responding
```bash
# Check if backend is running
curl http://localhost:8080/actuator/health

# Expected response:
{"status":"UP"}
```

### Frontend Not Loading
```bash
# Check if frontend is running
curl http://localhost:51547

# Should return HTML
```

### CORS Errors
- Verify backend CORS configuration includes frontend URL
- Check browser console for specific CORS error
- Verify request includes proper headers

### Database Connection Issues
- Verify SQL Server is running
- Check connection string in application-dev.properties
- Verify database credentials
- Check if database "IT_Asset" exists

## Next Steps

1. ✅ Servers are running successfully
2. ⏳ Perform manual testing using this guide
3. ⏳ Document any issues found
4. ⏳ Create automated Playwright tests
5. ⏳ Add unit tests for login component
6. ⏳ Add integration tests for auth service
7. ⏳ Verify accessibility compliance
8. ⏳ Verify design system compliance

## Summary

Both backend and frontend servers are now **running successfully**. The login flow is ready for comprehensive testing. Use this guide to perform manual testing, then proceed with automated test implementation.

**Test URL:** http://localhost:51547  
**API URL:** http://localhost:8080  
**Status:** ✅ Ready for Testing

---

**Report Generated:** April 10, 2026  
**Servers Started:** 16:24:48 (Backend), 10:40:34 (Frontend)
