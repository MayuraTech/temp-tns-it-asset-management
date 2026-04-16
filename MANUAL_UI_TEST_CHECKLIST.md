# Manual UI Flow Test Checklist

**Application URL:** http://localhost:4200  
**Backend API:** http://localhost:8080  
**Date:** April 10, 2026

---

## Pre-Test Setup

- [x] Backend running on port 8080
- [x] Frontend running on port 4200
- [ ] Browser opened (Chrome/Firefox/Edge recommended)
- [ ] Browser DevTools ready (F12)

---

## Test 1: Initial Page Load

**URL:** http://localhost:4200

### Steps:
1. Open browser
2. Navigate to http://localhost:4200
3. Observe page load

### Checklist:
- [ ] Page loads without errors
- [ ] Login form is visible
- [ ] Username input field present
- [ ] Password input field present
- [ ] "Remember me" checkbox present
- [ ] Submit button present and labeled "Sign In" or "Login"
- [ ] "Forgot password?" link present
- [ ] No console errors in DevTools
- [ ] Geometric triangle accents visible (Editorial Geometry design)
- [ ] Color scheme matches design (Primary: #143b7d, Surface: #faf9ff)

### Expected Visual Elements:
```
┌─────────────────────────────────────┐
│  [Geometric Triangle Accent]        │
│                                     │
│         Login / Sign In             │
│                                     │
│  Username: [________________]       │
│                                     │
│  Password: [________________] 👁    │
│                                     │
│  ☐ Remember me                      │
│                                     │
│  [        Sign In        ]          │
│                                     │
│  Forgot password?                   │
│                                     │
└─────────────────────────────────────┘
```

---

## Test 2: Form Validation - Empty Fields

### Steps:
1. Click in the username field
2. Click out (blur) without entering anything
3. Observe validation message
4. Click in the password field
5. Click out (blur) without entering anything
6. Observe validation message

### Checklist:
- [ ] "Username is required" error appears below username field
- [ ] Error message styled in red/secondary color (#a9371d)
- [ ] "Password is required" error appears below password field
- [ ] Submit button is disabled (grayed out or unclickable)
- [ ] Errors appear only after blur (not immediately on page load)

---

## Test 3: Form Validation - Error Clearing

### Steps:
1. Trigger validation errors (as in Test 2)
2. Type "admin" in username field
3. Observe error state
4. Type "password123" in password field
5. Observe error state

### Checklist:
- [ ] Username error clears immediately when text is entered
- [ ] Password error clears immediately when text is entered
- [ ] Submit button becomes enabled when both fields have values
- [ ] No errors remain visible

---

## Test 4: Password Visibility Toggle

### Steps:
1. Type "MyPassword123" in password field
2. Observe password display (should be dots/asterisks)
3. Click the eye icon (👁) next to password field
4. Observe password display
5. Click the eye icon again

### Checklist:
- [ ] Password initially hidden (shows dots: ••••••••••••)
- [ ] Eye icon is visible and clickable
- [ ] After clicking: Password visible as plain text "MyPassword123"
- [ ] Eye icon changes appearance (e.g., eye with slash)
- [ ] After clicking again: Password hidden again
- [ ] Toggle works multiple times

---

## Test 5: Remember Me Checkbox

### Steps:
1. Click the "Remember me" checkbox
2. Observe checkbox state
3. Click again to uncheck

### Checklist:
- [ ] Checkbox can be checked
- [ ] Checkbox shows checkmark when checked
- [ ] Checkbox can be unchecked
- [ ] Checkbox is properly labeled
- [ ] Checkbox is keyboard accessible (Tab + Space)

---

## Test 6: Keyboard Navigation

### Steps:
1. Refresh page
2. Press Tab key repeatedly
3. Observe focus movement
4. Type in username field
5. Press Tab
6. Type in password field
7. Press Enter

### Checklist:
- [ ] Tab moves focus to username field
- [ ] Tab moves focus to password field
- [ ] Tab moves focus to remember me checkbox
- [ ] Tab moves focus to submit button
- [ ] Tab moves focus to forgot password link
- [ ] Focus indicators are visible (outline/border)
- [ ] Enter key in password field submits form (if valid)
- [ ] Enter key does nothing if form is invalid

---

## Test 7: Successful Login (If Database Has Test User)

### Steps:
1. Enter username: "admin"
2. Enter password: "Admin@123456" (or your test password)
3. Check "Remember me"
4. Click Submit button

### Checklist:
- [ ] Submit button shows loading indicator (spinner or text change)
- [ ] Form inputs become disabled during submission
- [ ] Submit button becomes disabled during submission
- [ ] No console errors in DevTools
- [ ] Network tab shows POST to http://localhost:8080/api/v1/auth/login
- [ ] Response status is 200 OK
- [ ] Response contains accessToken and refreshToken
- [ ] Page redirects to dashboard (/dashboard)
- [ ] Token stored in localStorage or sessionStorage (check Application tab)

### Network Request to Verify:
```
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

Request Body:
{
  "username": "admin",
  "password": "Admin@123456",
  "rememberMe": true
}

Expected Response (200):
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 1800
}
```

---

## Test 8: Failed Login - Invalid Credentials

### Steps:
1. Enter username: "wronguser"
2. Enter password: "wrongpassword"
3. Click Submit button

### Checklist:
- [ ] Submit button shows loading indicator
- [ ] Network request sent to backend
- [ ] Response status is 401 Unauthorized
- [ ] Error message appears on page
- [ ] Error message says "Invalid username or password" or similar
- [ ] Error message styled in red/secondary color
- [ ] Password field is cleared
- [ ] Username field retains the entered value
- [ ] Form is re-enabled (not disabled)
- [ ] Submit button is re-enabled
- [ ] Error has dismiss button (X icon)

### Network Response to Verify:
```
Response (401):
{
  "error": {
    "type": "INVALID_CREDENTIALS",
    "message": "Invalid username or password",
    "timestamp": "2026-04-10T...",
    "requestId": "req-..."
  }
}
```

---

## Test 9: Error Message Dismissal

### Steps:
1. Trigger a login error (as in Test 8)
2. Observe error message
3. Click the X or dismiss button on error message

### Checklist:
- [ ] Error message has dismiss button (X icon)
- [ ] Clicking dismiss button removes error message
- [ ] Error message fades out or disappears smoothly
- [ ] Form remains functional after dismissal

---

## Test 10: Forgot Password Link

### Steps:
1. Click "Forgot password?" link

### Checklist:
- [ ] Link is clickable
- [ ] Link has proper styling (underline on hover, color change)
- [ ] Navigation occurs to /password-reset route
- [ ] Password reset page loads (or shows "Coming soon" if not implemented)
- [ ] No console errors

---

## Test 11: CORS and API Communication

### Steps:
1. Open DevTools Network tab
2. Attempt login
3. Observe network request

### Checklist:
- [ ] Request sent to http://localhost:8080/api/v1/auth/login
- [ ] Request method is POST
- [ ] Request has Content-Type: application/json header
- [ ] Request body contains username, password, rememberMe
- [ ] Response has CORS headers (Access-Control-Allow-Origin)
- [ ] No CORS errors in console
- [ ] Response is received successfully

---

## Test 12: Responsive Design (Optional)

### Steps:
1. Resize browser window to 375px width (mobile)
2. Observe layout
3. Resize to 768px width (tablet)
4. Observe layout
5. Resize to 1920px width (desktop)
6. Observe layout

### Checklist:
- [ ] Mobile: Form stacks vertically, readable
- [ ] Mobile: All elements accessible
- [ ] Tablet: Optimized spacing
- [ ] Desktop: Full design with geometric accents
- [ ] All sizes: Form remains functional
- [ ] No horizontal scrolling at any size

---

## Test 13: Accessibility (Optional)

### Steps:
1. Use keyboard only (no mouse)
2. Navigate entire form
3. Submit form using keyboard

### Checklist:
- [ ] All elements reachable via keyboard
- [ ] Focus indicators clearly visible
- [ ] Tab order is logical (top to bottom)
- [ ] Enter key submits form
- [ ] Space key toggles checkbox
- [ ] No keyboard traps

---

## Test 14: Browser Console Check

### Steps:
1. Open DevTools Console tab
2. Perform login flow
3. Check for errors

### Checklist:
- [ ] No JavaScript errors
- [ ] No 404 errors for assets
- [ ] No CORS errors
- [ ] No TypeScript compilation errors
- [ ] Only expected log messages (if any)

---

## Test 15: Local Storage / Session Storage

### Steps:
1. Login successfully with "Remember me" checked
2. Open DevTools > Application tab
3. Check Storage section

### Checklist:
- [ ] Token stored in localStorage or sessionStorage
- [ ] Token key is identifiable (e.g., "authToken", "accessToken")
- [ ] Token value is a JWT (three base64 segments separated by dots)
- [ ] No password stored anywhere
- [ ] Refresh token also stored (if applicable)

---

## Test 16: Auto-Authentication (If Remember Me Works)

### Steps:
1. Login with "Remember me" checked
2. Close browser completely
3. Reopen browser
4. Navigate to http://localhost:4200

### Checklist:
- [ ] Page attempts auto-authentication
- [ ] If token valid: Automatic redirect to dashboard
- [ ] If token expired: User stays on login page
- [ ] No errors during auto-auth attempt

---

## Test 17: Account Lockout (Security Feature)

### Steps:
1. Attempt login with wrong password 5 times
2. Observe response on 5th attempt

### Checklist:
- [ ] After 5 failed attempts: Special error message
- [ ] Error indicates account is locked
- [ ] Error mentions lockout duration (15 minutes)
- [ ] Subsequent login attempts are blocked
- [ ] Error type is "ACCOUNT_LOCKED"

---

## Design System Verification

### Editorial Geometry Standards

#### Colors:
- [ ] Primary buttons use #143b7d (Blue 800)
- [ ] Error messages use #a9371d (Red-Orange)
- [ ] Background is #faf9ff (Light purple surface)
- [ ] Text is #1a1b20 (NOT pure black #000000)

#### Typography:
- [ ] Headings use Manrope font
- [ ] Body text uses Inter font
- [ ] Font sizes follow design scale
- [ ] Letter spacing is appropriate

#### Components:
- [ ] No 1px solid borders for sectioning
- [ ] Geometric triangle accents present
- [ ] 8px border radius on buttons/inputs
- [ ] Glassmorphism effects (if applicable)
- [ ] Proper spacing and white space

---

## Summary Checklist

### Critical Path (Must Pass):
- [ ] Page loads successfully
- [ ] Form validation works
- [ ] Login request sent to backend
- [ ] Success: Redirects to dashboard
- [ ] Failure: Shows error message
- [ ] Password field cleared after failed login

### Important Features (Should Pass):
- [ ] Password visibility toggle works
- [ ] Remember me checkbox functional
- [ ] Keyboard navigation works
- [ ] Forgot password link navigates
- [ ] Error messages dismissible

### Nice to Have (Optional):
- [ ] Responsive design works
- [ ] Accessibility compliant
- [ ] Design system followed
- [ ] Auto-authentication works
- [ ] Account lockout enforced

---

## Issues Found

**Document any issues here:**

| Test # | Issue Description | Severity | Screenshot/Details |
|--------|------------------|----------|-------------------|
|        |                  |          |                   |
|        |                  |          |                   |
|        |                  |          |                   |

---

## Test Results Summary

**Date Tested:** _______________  
**Tester:** _______________  
**Browser:** _______________  
**OS:** _______________

**Overall Status:** 
- [ ] ✅ All tests passed
- [ ] ⚠️ Minor issues found
- [ ] ❌ Critical issues found

**Notes:**
_______________________________________
_______________________________________
_______________________________________

---

## Next Steps

After completing manual testing:
1. [ ] Document all issues found
2. [ ] Create bug tickets for critical issues
3. [ ] Implement automated Playwright tests
4. [ ] Add unit tests for login component
5. [ ] Verify accessibility with screen reader
6. [ ] Performance testing
7. [ ] Security testing

---

**Test Checklist Version:** 1.0  
**Last Updated:** April 10, 2026
