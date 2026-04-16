# Quick Test Guide - Logout Functionality

## ⚠️ IMPORTANT: Clear Browser Storage First!

Before testing, you MUST clear browser storage to remove old JWT tokens:

1. Open DevTools (F12)
2. Go to **Application** tab
3. Click **Clear Storage** (left sidebar)
4. Click **Clear site data** button
5. Close and reopen the browser tab

## 🚀 Quick Test Steps

### 1. Restart Backend (if needed)
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Wait for: `Started ItAssetManagementApplication`

### 2. Start Frontend (if needed)
```bash
cd frontend
npm start
```

Wait for: `Compiled successfully`

### 3. Test Logout

1. **Login**:
   - Go to: `http://localhost:4200/login`
   - Username: `admin`
   - Password: `Admin@123456`
   - Click "Sign In"

2. **Verify User Menu**:
   - Look at top-right corner
   - You should see user avatar with initials "AD"
   - Click on the avatar

3. **Check Dropdown Menu**:
   - Menu should appear with:
     - User name: "admin"
     - User email
     - Profile option (person icon)
     - Settings option (settings icon)
     - Logout option (logout icon)

4. **Test Logout**:
   - Click "Logout"
   - Should redirect to login page
   - Try going to: `http://localhost:4200/dashboard`
   - Should redirect back to login (you're logged out!)

## ✅ Success Criteria

- ✅ Avatar shows user initials
- ✅ Clicking avatar opens dropdown menu
- ✅ Menu shows user name and email
- ✅ Logout option is visible
- ✅ Clicking logout redirects to login page
- ✅ Cannot access protected routes after logout
- ✅ No 403 errors when accessing user list

## 🐛 If You See 403 Errors

If you still see "You do not have permission to perform this action":

1. **Clear browser storage again** (most common issue!)
2. **Restart backend** to ensure JWT role prefix fix is active
3. **Login again** to get new JWT token with correct format

## 📝 Test Other Features

After successful logout test, try:

1. **User Management**:
   - Login as admin
   - Go to: `http://localhost:4200/users`
   - Should see user list (no 403 error!)

2. **Different Roles**:
   - Logout
   - Login as `manager` / `Manager@123456`
   - Test access to features

3. **Profile**:
   - Click avatar → Profile
   - Should navigate to user profile page

## 🎯 What Was Fixed

1. ✅ Added logout button to user menu dropdown
2. ✅ Implemented logout handler in top navigation
3. ✅ Connected to AuthService.logout()
4. ✅ Proper redirect to login page
5. ✅ Session cleanup on logout
6. ✅ JWT role prefix fix (ROLE_ADMINISTRATOR, etc.)
7. ✅ User info loaded from AuthService

All TypeScript compilation errors resolved!
