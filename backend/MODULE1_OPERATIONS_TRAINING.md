# Module 1 - User Management: Operations Team Training Guide

## Overview

This training guide provides comprehensive instruction for operations team members responsible for managing and supporting the User Management module in production. The guide includes hands-on exercises, troubleshooting scenarios, and best practices.

---

## Table of Contents

1. [Training Prerequisites](#1-training-prerequisites)
2. [Module Architecture Overview](#2-module-architecture-overview)
3. [Core Concepts](#3-core-concepts)
4. [Hands-On Exercises](#4-hands-on-exercises)
5. [Troubleshooting Scenarios](#5-troubleshooting-scenarios)
6. [Best Practices](#6-best-practices)
7. [Knowledge Check](#7-knowledge-check)
8. [Additional Resources](#8-additional-resources)

---

## 1. Training Prerequisites

### Required Knowledge

- [ ] Basic understanding of REST APIs
- [ ] Familiarity with SQL Server
- [ ] Command-line proficiency (bash/PowerShell)
- [ ] Understanding of HTTP status codes
- [ ] Basic security concepts (authentication, authorization)

### Required Access

- [ ] Production read-only database access
- [ ] Staging environment full access
- [ ] Monitoring dashboards (Grafana)
- [ ] Log aggregation system (Kibana)
- [ ] API documentation (Swagger)

### Training Environment

All hands-on exercises should be performed in the **staging environment** unless explicitly stated otherwise.

**Staging API Base URL**: `https://staging-api.example.com`  
**Staging Database**: `ITAssetManagement_Staging`

---

## 2. Module Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Frontend (Angular)                       │
│  - Login page                                                │
│  - User management interface                                 │
│  - Profile management                                        │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼ HTTPS/REST API
┌─────────────────────────────────────────────────────────────┐
│                  Backend (Spring Boot)                       │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Controllers                                         │   │
│  │  - AuthController                                    │   │
│  │  - UserController                                    │   │
│  │  - ProfileController                                 │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Security Layer                                      │   │
│  │  - JWT Authentication Filter                         │   │
│  │  - Spring Security Configuration                     │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Services                                            │   │
│  │  - AuthenticationService                             │   │
│  │  - UserService                                       │   │
│  │  - ProfileService                                    │   │
│  │  - AuthorizationService                              │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Repositories (JPA)                                  │   │
│  │  - UserRepository                                    │   │
│  │  - UserRoleRepository                                │   │
│  │  - SessionRepository                                 │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼ JDBC
┌─────────────────────────────────────────────────────────────┐
│              Database (SQL Server)                           │
│  - Users table                                               │
│  - UserRoles table                                           │
│  - Sessions table                                            │
│  - AuditLogs table                                           │
└─────────────────────────────────────────────────────────────┘
```

### Key Components

**1. Authentication Service**
- Handles user login/logout
- Generates JWT tokens
- Manages account lockout
- Validates credentials

**2. User Service**
- CRUD operations for user accounts
- Role assignment/revocation
- Account enable/disable
- User search and filtering

**3. Profile Service**
- User profile management
- Password change functionality
- Self-service operations

**4. Authorization Service**
- Permission checking
- Role-based access control
- Account status validation

---

## 3. Core Concepts

### 3.1 Authentication vs Authorization

**Authentication**: Verifying who you are (identity)
- Login with username/password
- JWT token generation
- Session management

**Authorization**: Verifying what you can do (permissions)
- Role-based access control
- Permission checks
- Resource access control

### 3.2 JWT Tokens

**Access Token**:
- Short-lived (30 minutes)
- Used for API authentication
- Contains user ID and roles
- Stateless (no server-side storage)

**Refresh Token**:
- Long-lived (24 hours)
- Used to obtain new access tokens
- Stored in database (Sessions table)
- Can be revoked

**Token Flow**:
```
1. User logs in with credentials
2. Server validates credentials
3. Server generates access token (30 min) and refresh token (24 hours)
4. Client stores tokens
5. Client includes access token in API requests
6. When access token expires, client uses refresh token to get new access token
7. When refresh token expires, user must log in again
```

### 3.3 User Roles

**ADMINISTRATOR**:
- Full system access
- Can create/update/delete users
- Can assign/revoke roles
- Can view all data

**ASSET_MANAGER**:
- Can create/update/delete assets
- Can view all users
- Cannot modify users
- Can manage tickets

**VIEWER**:
- Read-only access
- Can view assets
- Can view own profile
- Cannot modify data

### 3.4 Account Lockout

**Trigger**: 5 consecutive failed login attempts

**Duration**: 30 minutes

**Behavior**:
- Account automatically locked after 5th failed attempt
- User cannot log in until lockout expires
- Lockout can be manually cleared by administrator
- Failed attempt counter resets on successful login

**Security Purpose**: Prevent brute force attacks

### 3.5 Session Management

**Session Lifecycle**:
1. **Created**: When user logs in successfully
2. **Active**: While tokens are valid
3. **Expired**: When tokens expire
4. **Terminated**: When user logs out or admin terminates

**Session Invalidation Triggers**:
- User logout
- Password change
- Account disable
- Role change
- Manual termination by admin

---

## 4. Hands-On Exercises

### Exercise 1: User Authentication Flow

**Objective**: Understand the complete authentication process

**Steps**:

1. **Login with valid credentials**:
```bash
curl -X POST "https://staging-api.example.com/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "training.user",
    "password": "Training@123"
  }'
```

**Expected Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 1800
}
```

2. **Use access token to call protected endpoint**:
```bash
curl -X GET "https://staging-api.example.com/api/v1/profile" \
  -H "Authorization: Bearer {access_token}"
```

3. **Attempt to use expired token** (wait 30 minutes or use old token):
```bash
curl -X GET "https://staging-api.example.com/api/v1/profile" \
  -H "Authorization: Bearer {expired_token}"
```

**Expected Response**: HTTP 401 Unauthorized

4. **Refresh access token**:
```bash
curl -X POST "https://staging-api.example.com/api/v1/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "{refresh_token}"
  }'
```

5. **Logout**:
```bash
curl -X POST "https://staging-api.example.com/api/v1/auth/logout" \
  -H "Authorization: Bearer {access_token}"
```

**Questions**:
- What happens to the session when you logout?
- Can you use the refresh token after logout?
- What information is contained in the JWT token?

---

### Exercise 2: Account Lockout Simulation

**Objective**: Understand account lockout mechanism

**Steps**:

1. **Attempt login with wrong password 5 times**:
```bash
for i in {1..5}; do
  curl -X POST "https://staging-api.example.com/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
      "username": "training.user",
      "password": "WrongPassword"
    }'
  echo "\nAttempt $i"
done
```

2. **Check account status in database**:
```sql
SELECT 
    Username,
    AccountLocked,
    LockUntil,
    FailedLoginAttempts
FROM Users
WHERE Username = 'training.user';
```

**Expected Result**:
- AccountLocked = 1
- LockUntil = 30 minutes from now
- FailedLoginAttempts = 5

3. **Attempt login with correct password**:
```bash
curl -X POST "https://staging-api.example.com/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "training.user",
    "password": "Training@123"
  }'
```

**Expected Response**: HTTP 401 Unauthorized with "Account locked" message

4. **Manually unlock account**:
```sql
UPDATE Users
SET 
    AccountLocked = 0,
    LockUntil = NULL,
    FailedLoginAttempts = 0
WHERE Username = 'training.user';
```

5. **Verify login works**:
```bash
curl -X POST "https://staging-api.example.com/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "training.user",
    "password": "Training@123"
  }'
```

**Questions**:
- Why is account lockout important for security?
- When should you manually unlock an account vs waiting for automatic unlock?
- How would you detect a brute force attack?

---

### Exercise 3: User Management Operations

**Objective**: Perform common user management tasks

**Prerequisites**: Login as administrator

```bash
# Login as admin
ADMIN_TOKEN=$(curl -X POST "https://staging-api.example.com/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin@123456"
  }' | jq -r '.accessToken')
```

**Task 1: Create new user**

```bash
curl -X POST "https://staging-api.example.com/api/v1/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "email": "john.doe@example.com",
    "password": "TempPassword@123",
    "roles": ["VIEWER"]
  }'
```

**Task 2: Assign additional role**

```bash
# Get user ID from previous response
USER_ID="..."

curl -X POST "https://staging-api.example.com/api/v1/users/$USER_ID/roles" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "role": "ASSET_MANAGER"
  }'
```

**Task 3: Disable user account**

```bash
curl -X PATCH "https://staging-api.example.com/api/v1/users/$USER_ID/disable" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Task 4: Verify user cannot login**

```bash
curl -X POST "https://staging-api.example.com/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "password": "TempPassword@123"
  }'
```

**Expected Response**: HTTP 401 Unauthorized with "Account disabled" message

**Task 5: Re-enable user account**

```bash
curl -X PATCH "https://staging-api.example.com/api/v1/users/$USER_ID/enable" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Task 6: Revoke role**

```bash
curl -X DELETE "https://staging-api.example.com/api/v1/users/$USER_ID/roles/ASSET_MANAGER" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Task 7: Delete user**

```bash
curl -X DELETE "https://staging-api.example.com/api/v1/users/$USER_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Questions**:
- What happens to user sessions when you disable an account?
- Can you revoke a user's last role?
- What's the difference between disabling and deleting a user?

---

### Exercise 4: Database Queries

**Objective**: Learn essential database queries for operations

**Query 1: Find all active users**

```sql
SELECT 
    Username,
    Email,
    LastLoginAt,
    CreatedAt
FROM Users
WHERE IsActive = 1
ORDER BY LastLoginAt DESC;
```

**Query 2: Find users with specific role**

```sql
SELECT 
    u.Username,
    u.Email,
    ur.Role,
    ur.AssignedAt
FROM Users u
INNER JOIN UserRoles ur ON u.Id = ur.UserId
WHERE ur.Role = 'ADMINISTRATOR'
ORDER BY u.Username;
```

**Query 3: Find locked accounts**

```sql
SELECT 
    Username,
    Email,
    LockUntil,
    FailedLoginAttempts,
    DATEDIFF(MINUTE, GETUTCDATE(), LockUntil) AS MinutesUntilUnlock
FROM Users
WHERE AccountLocked = 1
AND LockUntil > GETUTCDATE()
ORDER BY LockUntil;
```

**Query 4: Find active sessions**

```sql
SELECT 
    u.Username,
    s.LoginAt,
    s.TokenExpiration,
    DATEDIFF(MINUTE, GETUTCDATE(), s.TokenExpiration) AS MinutesRemaining
FROM Sessions s
INNER JOIN Users u ON s.UserId = u.Id
WHERE s.IsActive = 1
AND s.TokenExpiration > GETUTCDATE()
ORDER BY s.LoginAt DESC;
```

**Query 5: Find users who haven't logged in recently**

```sql
SELECT 
    Username,
    Email,
    LastLoginAt,
    DATEDIFF(DAY, LastLoginAt, GETUTCDATE()) AS DaysSinceLastLogin
FROM Users
WHERE IsActive = 1
AND (LastLoginAt IS NULL OR LastLoginAt < DATEADD(DAY, -30, GETUTCDATE()))
ORDER BY LastLoginAt;
```

**Query 6: Audit trail for specific user**

```sql
SELECT 
    ActionType,
    Details,
    Timestamp,
    (SELECT Username FROM Users WHERE Id = al.PerformedBy) AS PerformedBy
FROM AuditLogs al
WHERE UserId = '{user_id}'
ORDER BY Timestamp DESC;
```

**Practice**: Run each query and understand the results

---

### Exercise 5: Monitoring and Alerting

**Objective**: Learn to use monitoring tools

**Task 1: Access Grafana Dashboard**

1. Navigate to: `https://monitoring.example.com`
2. Login with monitoring credentials
3. Open "User Management - Application Performance" dashboard

**Metrics to Review**:
- Login success rate (should be > 95%)
- Login response time (p95 should be < 500ms)
- Active sessions count
- Failed login rate

**Task 2: Review Logs in Kibana**

1. Navigate to: `https://logs.example.com`
2. Login with monitoring credentials
3. Search for recent authentication events:
   - Query: `application:"it-asset-management" AND logger:"AuthenticationService"`
   - Time range: Last 1 hour

**Task 3: Check Application Health**

```bash
curl -X GET "https://staging-api.example.com/actuator/health"
```

**Expected Response**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "SQL Server",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

**Task 4: Review Prometheus Metrics**

```bash
# Check login success rate
curl -X GET "https://prometheus.example.com/api/v1/query?query=rate(user_login_total{status=\"success\"}[5m])"

# Check active sessions
curl -X GET "https://prometheus.example.com/api/v1/query?query=session_active_count"
```

---

## 5. Troubleshooting Scenarios

### Scenario 1: User Cannot Login

**Reported Issue**: "User Jane Smith cannot log in. She gets 'Invalid credentials' error."

**Your Investigation**:

1. **Verify user exists**:
```sql
SELECT * FROM Users WHERE Username = 'jane.smith';
```

2. **Check account status**:
```sql
SELECT 
    Username,
    IsActive,
    AccountLocked,
    LockUntil,
    FailedLoginAttempts
FROM Users
WHERE Username = 'jane.smith';
```

**Possible Findings**:

**Finding A**: Account is locked
- **Solution**: Unlock account or wait for automatic unlock
- **Action**: 
```sql
UPDATE Users SET AccountLocked = 0, LockUntil = NULL, FailedLoginAttempts = 0
WHERE Username = 'jane.smith';
```

**Finding B**: Account is disabled
- **Solution**: Enable account
- **Action**:
```bash
curl -X PATCH "https://api.example.com/api/v1/users/{user_id}/enable" \
  -H "Authorization: Bearer {admin_token}"
```

**Finding C**: User is using wrong password
- **Solution**: Reset password
- **Action**: Follow password reset procedure

**Finding D**: User doesn't exist
- **Solution**: Create user account
- **Action**: Follow user creation procedure

---

### Scenario 2: Slow Login Performance

**Reported Issue**: "Login is taking 5-10 seconds. Users are complaining."

**Your Investigation**:

1. **Check application metrics**:
```bash
curl -X GET "https://api.example.com/actuator/metrics/http.server.requests?tag=uri:/api/v1/auth/login"
```

2. **Check database performance**:
```sql
-- Find slow queries
SELECT 
    qs.execution_count,
    qs.total_elapsed_time / qs.execution_count / 1000000.0 AS avg_elapsed_time_sec,
    SUBSTRING(qt.text, 1, 100) AS query_text
FROM sys.dm_exec_query_stats qs
CROSS APPLY sys.dm_exec_sql_text(qs.sql_handle) qt
WHERE qt.text LIKE '%Users%'
ORDER BY avg_elapsed_time_sec DESC;
```

3. **Check index fragmentation**:
```sql
SELECT 
    OBJECT_NAME(ips.object_id) AS TableName,
    i.name AS IndexName,
    ips.avg_fragmentation_in_percent
FROM sys.dm_db_index_physical_stats(DB_ID(), OBJECT_ID('Users'), NULL, NULL, 'LIMITED') ips
INNER JOIN sys.indexes i ON ips.object_id = i.object_id AND ips.index_id = i.index_id
WHERE i.name IS NOT NULL;
```

**Possible Solutions**:

**Solution A**: Rebuild fragmented indexes
```sql
ALTER INDEX IX_Users_Username ON Users REBUILD;
```

**Solution B**: Update statistics
```sql
UPDATE STATISTICS Users WITH FULLSCAN;
```

**Solution C**: Check connection pool
```bash
curl -X GET "https://api.example.com/actuator/metrics/hikaricp.connections.active"
```

**Solution D**: Review application logs for errors
```bash
tail -f /var/log/it-asset-management/application.log | grep ERROR
```

---

### Scenario 3: Multiple Account Lockouts

**Reported Issue**: "Multiple users are getting locked out simultaneously."

**Your Investigation**:

1. **Check for brute force attack**:
```sql
SELECT 
    Details->>'$.ipAddress' AS IpAddress,
    COUNT(*) AS FailedAttempts,
    MIN(Timestamp) AS FirstAttempt,
    MAX(Timestamp) AS LastAttempt
FROM AuditLogs
WHERE ActionType = 'LOGIN_FAILED'
AND Timestamp > DATEADD(MINUTE, -15, GETUTCDATE())
GROUP BY Details->>'$.ipAddress'
HAVING COUNT(*) > 20
ORDER BY FailedAttempts DESC;
```

2. **Check locked accounts**:
```sql
SELECT 
    Username,
    LockUntil,
    FailedLoginAttempts
FROM Users
WHERE AccountLocked = 1
ORDER BY LockUntil DESC;
```

3. **Review audit logs**:
```sql
SELECT 
    ActionType,
    Details,
    Timestamp
FROM AuditLogs
WHERE ActionType IN ('LOGIN_FAILED', 'ACCOUNT_LOCKED')
AND Timestamp > DATEADD(HOUR, -1, GETUTCDATE())
ORDER BY Timestamp DESC;
```

**Possible Actions**:

**Action A**: Block malicious IP
```bash
sudo iptables -A INPUT -s {malicious_ip} -j DROP
```

**Action B**: Unlock legitimate users
```sql
UPDATE Users
SET AccountLocked = 0, LockUntil = NULL, FailedLoginAttempts = 0
WHERE Username IN ('user1', 'user2', 'user3');
```

**Action C**: Notify security team

**Action D**: Increase rate limiting temporarily

---

### Scenario 4: Session Issues

**Reported Issue**: "User keeps getting logged out unexpectedly."

**Your Investigation**:

1. **Check user's sessions**:
```sql
SELECT 
    LoginAt,
    LogoutAt,
    TokenExpiration,
    IsActive
FROM Sessions
WHERE UserId = '{user_id}'
ORDER BY LoginAt DESC;
```

2. **Check for session termination events**:
```sql
SELECT 
    ActionType,
    Details,
    Timestamp
FROM AuditLogs
WHERE UserId = '{user_id}'
AND ActionType IN ('LOGOUT', 'SESSION_TERMINATED', 'PASSWORD_CHANGED', 'ROLE_CHANGED')
ORDER BY Timestamp DESC;
```

3. **Check token expiration settings**:
```bash
grep "jwt.expiration" /app/config/application-prod.properties
```

**Possible Causes**:

**Cause A**: Token expiration (normal behavior)
- **Solution**: Educate user about token refresh

**Cause B**: Password was changed
- **Solution**: Explain that password change invalidates all sessions

**Cause C**: Role was changed
- **Solution**: Explain that role change invalidates sessions for security

**Cause D**: Admin terminated sessions
- **Solution**: Check with admin team

---

## 6. Best Practices

### Security Best Practices

1. **Never share admin credentials**: Each admin should have their own account
2. **Use principle of least privilege**: Assign minimum required roles
3. **Monitor failed login attempts**: Watch for brute force attacks
4. **Regular password changes**: Encourage users to change passwords periodically
5. **Audit log review**: Regularly review audit logs for suspicious activity
6. **Secure communication**: Always use HTTPS, never HTTP
7. **Token security**: Never log or expose JWT tokens
8. **Session management**: Terminate sessions when no longer needed

### Operational Best Practices

1. **Document everything**: Keep detailed notes of all actions
2. **Test in staging first**: Never test procedures in production
3. **Verify before action**: Always verify user identity before account changes
4. **Communicate changes**: Notify users of account changes
5. **Monitor after changes**: Watch metrics after making changes
6. **Follow runbooks**: Use documented procedures for common tasks
7. **Escalate when needed**: Don't hesitate to escalate complex issues
8. **Learn from incidents**: Document lessons learned

### Database Best Practices

1. **Read-only in production**: Use read-only access when possible
2. **Backup before changes**: Always backup before manual database changes
3. **Use transactions**: Wrap multiple changes in transactions
4. **Verify queries**: Test queries in staging before production
5. **Index maintenance**: Regularly rebuild fragmented indexes
6. **Statistics updates**: Keep statistics up to date
7. **Monitor performance**: Watch for slow queries
8. **Clean up old data**: Archive old sessions and audit logs

---

## 7. Knowledge Check

### Quiz Questions

**Question 1**: What happens when a user's account is disabled?
- A) User can still log in but has limited access
- B) All active sessions are terminated and user cannot log in
- C) User's password is reset
- D) User's roles are revoked

**Answer**: B

---

**Question 2**: How long is an access token valid?
- A) 15 minutes
- B) 30 minutes
- C) 1 hour
- D) 24 hours

**Answer**: B

---

**Question 3**: After how many failed login attempts is an account locked?
- A) 3
- B) 5
- C) 10
- D) Never automatically locked

**Answer**: B

---

**Question 4**: What triggers session invalidation? (Select all that apply)
- A) User logout
- B) Password change
- C) Role change
- D) Account disable
- E) All of the above

**Answer**: E

---

**Question 5**: Which role has permission to create new users?
- A) VIEWER
- B) ASSET_MANAGER
- C) ADMINISTRATOR
- D) All roles

**Answer**: C

---

**Question 6**: What should you do if you suspect a brute force attack?
- A) Ignore it, the system will handle it
- B) Block the malicious IP and notify security team
- C) Disable all user accounts
- D) Restart the application

**Answer**: B

---

**Question 7**: How long does an account lockout last?
- A) 15 minutes
- B) 30 minutes
- C) 1 hour
- D) Until manually unlocked

**Answer**: B (but can also be manually unlocked)

---

**Question 8**: What is the minimum number of roles a user must have?
- A) 0
- B) 1
- C) 2
- D) 3

**Answer**: B

---

### Practical Assessment

**Scenario**: A user reports they cannot log in. Walk through your troubleshooting steps.

**Your Answer Should Include**:
1. Verify user exists in database
2. Check account status (active/inactive)
3. Check for account lockout
4. Review failed login attempts
5. Check audit logs
6. Determine root cause
7. Apply appropriate solution
8. Verify resolution
9. Document actions taken

---

## 8. Additional Resources

### Documentation

- **API Documentation**: https://api.example.com/swagger-ui.html
- **Operational Runbooks**: `/backend/MODULE1_OPERATIONAL_RUNBOOKS.md`
- **Deployment Checklist**: `/backend/PRODUCTION_DEPLOYMENT_CHECKLIST.md`
- **Monitoring Setup**: `/backend/MONITORING_AND_ALERTING_SETUP.md`

### Tools

- **Grafana Dashboards**: https://monitoring.example.com
- **Kibana Logs**: https://logs.example.com
- **Prometheus Metrics**: https://prometheus.example.com
- **API Testing**: Postman collection available

### Contacts

- **On-Call Engineer**: TBD
- **Senior Engineer**: TBD
- **Database Administrator**: TBD
- **Security Team**: TBD

### Training Schedule

- **Initial Training**: 4 hours (this guide)
- **Hands-On Practice**: 2 hours (staging environment)
- **Shadow Production Support**: 1 week
- **Independent Support**: After certification
- **Refresher Training**: Quarterly

---

## Training Completion Checklist

- [ ] Completed all hands-on exercises
- [ ] Passed knowledge check quiz (minimum 80%)
- [ ] Successfully resolved all troubleshooting scenarios
- [ ] Shadowed production support for 1 week
- [ ] Reviewed all documentation
- [ ] Familiar with monitoring tools
- [ ] Comfortable with database queries
- [ ] Understands escalation procedures

**Trainee Name**: ___________________________

**Trainer Name**: ___________________________

**Completion Date**: ___________________________

**Certification**: ___________________________

---

**Document Version**: 1.0  
**Last Updated**: 2024-01-15  
**Owner**: Training Team  
**Review Schedule**: Quarterly

