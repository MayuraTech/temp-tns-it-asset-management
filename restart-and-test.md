# Quick Restart and Test Guide

## Step 1: Restart Backend
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Wait for the message: "Started AssetManagementApplication"

## Step 2: Clear Browser Storage

### Option A: Via Browser Console (Fastest)
1. Open browser at http://localhost:4200
2. Press F12 to open DevTools
3. Go to Console tab
4. Paste this command and press Enter:
```javascript
localStorage.clear(); sessionStorage.clear(); location.reload();
```

### Option B: Via Application Tab
1. Open browser at http://localhost:4200
2. Press F12 to open DevTools
3. Go to Application tab
4. Under Storage → Local Storage → http://localhost:4200
5. Right-click and select "Clear"
6. Refresh the page (F5)

## Step 3: Login
1. Navigate to http://localhost:4200/login
2. Enter credentials:
   - Username: `admin`
   - Password: `Admin@123456`
3. Click "Sign In"

## Step 4: Test Users Page
1. Click "Users" in the sidebar
2. You should see the user list without any 403 errors
3. Check browser console (F12) - should be no errors

## Expected Results
✅ Users page loads successfully
✅ User list displays (admin, manager, viewer)
✅ No 403 Forbidden errors
✅ No console errors

## If Still Getting 403 Error
1. Check backend logs for any errors
2. Verify backend is running on port 8080
3. Verify you cleared browser storage completely
4. Try logging out and logging in again
5. Check the JWT token at https://jwt.io - roles should have "ROLE_" prefix

## Quick Test Commands

### Check Backend is Running
```bash
curl http://localhost:8080/actuator/health
```
Should return: `{"status":"UP"}`

### Check Frontend is Running
Open browser to: http://localhost:4200
Should show login page

### Test API with Token (After Login)
1. Get token from browser localStorage (F12 → Application → Local Storage → access_token)
2. Test API:
```bash
curl -H "Authorization: Bearer YOUR_TOKEN_HERE" http://localhost:8080/api/v1/users?page=0&size=20
```
Should return user list JSON (not 403 error)
