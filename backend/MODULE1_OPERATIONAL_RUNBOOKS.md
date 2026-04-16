# Module 1 - User Management: Operational Runbooks

## Overview

This document provides step-by-step operational procedures for managing and troubleshooting the User Management module in production. These runbooks are designed for operations teams to quickly resolve common issues and perform routine maintenance tasks.

---

## Table of Contents

1. [User Account Management](#1-user-account-management)
2. [Authentication Issues](#2-authentication-issues)
3. [Account Lockout Management](#3-account-lockout-management)
4. [Session Management](#4-session-management)
5. [Password Reset Procedures](#5-password-reset-procedures)
6. [Role Management](#6-role-management)
7. [Performance Issues](#7-performance-issues)
8. [Security Incidents](#8-security-incidents)
9. [Database Maintenance](#9-database-maintenance)
10. [Monitoring and Alerts](#10-monitoring-and-alerts)

---

## 1. User Account Management

### 1.1 Create New User Account

**When to Use**: New employee onboarding, contractor access

**Prerequisites**:
- Administrator credentials
- User details (username, email, role)
- Approval from manager

**Procedure**:

```bash
# Step 1: Verify user doesn't already exist
curl -X GET "https://api.example.com/api/v1/users?text={username}" \
  -H "Authorization: Bearer {admin_token}"

# Step 2: Create user account
curl -X POST "https://api.example.com/api/v1/users" \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "email": "john.doe@example.com",
    "password": "TempPassword@123",
    "roles": ["VIEWER"]
  }'

# Step 3: Verify creation
curl -X GET "https://api.example.com/api/v1/users/{user_id}" \
  -H "Authorization: Bearer {admin_token}"
```

**Expected Result**: HTTP 201 Created with user details

**Rollback**: Delete user account if creation was erroneous

**Audit**: Check audit logs for user creation event

---

### 1.2 Disable User Account

**When to Use**: Employee termination, security incident, extended leave

**Prerequisites**:
- Administrator credentials
- User ID or username
- Approval from manager

**Procedure**:

```bash
# Step 1: Identify user
curl -X GET "https://api.example.com/api/v1/users?text={username}" \
  -H "Authorization: Bearer {admin_token}"

# Step 2: Disable account
curl -X PATCH "https://api.example.com/api/v1/users/{user_id}/disable" \
  -H "Authorization: Bearer {admin_token}"

# Step 3: Verify all sessions terminated
curl -X GET "https://api.example.com/api/v1/admin/sessions?userId={user_id}" \
  -H "Authorization: Bearer {admin_token}"
```

**Expected Result**: 
- HTTP 200 OK
- User account status = inactive
- All active sessions terminated

**Verification**:
- User cannot log in
- Existing sessions invalidated
- Audit log entry created

---

### 1.3 Re-enable User Account

**When to Use**: User returns from leave, false positive security incident

**Prerequisites**:
- Administrator credentials
- User ID
- Approval from manager

**Procedure**:

```bash
# Step 1: Verify account is disabled
curl -X GET "https://api.example.com/api/v1/users/{user_id}" \
  -H "Authorization: Bearer {admin_token}"

# Step 2: Enable account
curl -X PATCH "https://api.example.com/api/v1/users/{user_id}/enable" \
  -H "Authorization: Bearer {admin_token}"

# Step 3: Notify user to log in
# Send email with login instructions
```

**Expected Result**: HTTP 200 OK, account status = active

**Post-Action**: Verify user can successfully log in

---

### 1.4 Delete User Account

**When to Use**: Permanent removal (use with caution)

**Prerequisites**:
- Administrator credentials
- User ID
- Manager approval
- Data retention policy compliance

**Procedure**:

```bash
# Step 1: Export user data for compliance
curl -X GET "https://api.example.com/api/v1/users/{user_id}" \
  -H "Authorization: Bearer {admin_token}" > user_backup.json

# Step 2: Verify user has no active assignments
curl -X GET "https://api.example.com/api/v1/assets?assignedUserId={user_id}" \
  -H "Authorization: Bearer {admin_token}"

# Step 3: Delete user
curl -X DELETE "https://api.example.com/api/v1/users/{user_id}" \
  -H "Authorization: Bearer {admin_token}"

# Step 4: Verify deletion
curl -X GET "https://api.example.com/api/v1/users/{user_id}" \
  -H "Authorization: Bearer {admin_token}"
```

**Expected Result**: HTTP 204 No Content, user not found on verification

**Warning**: This action is irreversible. Prefer disabling accounts over deletion.

---

## 2. Authentication Issues

### 2.1 User Cannot Log In - Invalid Credentials

**Symptoms**:
- User reports "Invalid username or password" error
- No account lockout

**Diagnosis**:

```bash
# Check if user exists
curl -X GET "https://api.example.com/api/v1/users?text={username}" \
  -H "Authorization: Bearer {admin_token}"

# Check failed login attempts
SELECT Username, FailedLoginAttempts, LastLoginAt, AccountLocked
FROM Users
WHERE Username = '{username}';

# Check audit logs
SELECT * FROM AuditLogs
WHERE UserId = '{user_id}'
AND ActionType = 'LOGIN_FAILED'
ORDER BY Timestamp DESC;
```

**Resolution**:

1. **Verify username**: Confirm user is using correct username (case-sensitive)
2. **Reset password**: If user forgot password, initiate password reset
3. **Check account status**: Ensure account is active
4. **Clear failed attempts**: If needed, reset failed login counter

```sql
-- Reset failed login attempts
UPDATE Users
SET FailedLoginAttempts = 0
WHERE Username = '{username}';
```

---

### 2.2 JWT Token Expired

**Symptoms**:
- User receives 401 Unauthorized after period of inactivity
- Error message: "Token expired"

**Diagnosis**:

```bash
# Check token expiration settings
grep "jwt.expiration" /app/config/application-prod.properties

# Expected: jwt.expiration=1800000 (30 minutes)
```

**Resolution**:

1. **User action**: User should use refresh token to get new access token
2. **If refresh token expired**: User must log in again

```bash
# Refresh token
curl -X POST "https://api.example.com/api/v1/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "{refresh_token}"
  }'
```

**Prevention**: Educate users about token expiration and refresh mechanism

---

### 2.3 Authentication Service Down

**Symptoms**:
- All users unable to log in
- Health check failing
- Alert: "ApplicationDown"

**Diagnosis**:

```bash
# Check application health
curl -X GET "https://api.example.com/actuator/health"

# Check application logs
tail -f /var/log/it-asset-management/application.log | grep ERROR

# Check database connectivity
curl -X GET "https://api.example.com/actuator/health/db"
```

**Resolution**:

1. **Check application status**:
```bash
systemctl status it-asset-management
```

2. **Restart application if needed**:
```bash
systemctl restart it-asset-management
```

3. **Check database connection**:
```sql
SELECT @@VERSION;
SELECT DB_NAME();
```

4. **Review recent deployments**: Check if issue started after deployment

5. **Escalate**: If unresolved in 15 minutes, escalate to senior engineer

---

## 3. Account Lockout Management

### 3.1 Unlock Locked Account

**Symptoms**:
- User reports "Account locked" error
- User exceeded 5 failed login attempts

**Diagnosis**:

```sql
-- Check account lock status
SELECT 
    Username,
    AccountLocked,
    LockUntil,
    FailedLoginAttempts,
    DATEDIFF(MINUTE, GETUTCDATE(), LockUntil) AS MinutesUntilUnlock
FROM Users
WHERE Username = '{username}';
```

**Resolution**:

**Option 1: Wait for automatic unlock (30 minutes)**
- Inform user to wait until LockUntil time
- No manual intervention needed

**Option 2: Manual unlock (if urgent)**

```sql
-- Manually unlock account
UPDATE Users
SET 
    AccountLocked = 0,
    LockUntil = NULL,
    FailedLoginAttempts = 0
WHERE Username = '{username}';
```

**Verification**:

```bash
# Verify user can log in
curl -X POST "https://api.example.com/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "{username}",
    "password": "{password}"
  }'
```

**Post-Action**:
- Document reason for manual unlock
- Investigate if lockout was due to brute force attack

---

### 3.2 Investigate Suspicious Lockouts

**Symptoms**:
- Multiple accounts locked simultaneously
- Alert: "FailedLoginSpike"
- Unusual pattern of failed logins

**Diagnosis**:

```sql
-- Check recent lockouts
SELECT 
    Username,
    FailedLoginAttempts,
    LockUntil,
    LastLoginAt
FROM Users
WHERE AccountLocked = 1
AND LockUntil > GETUTCDATE()
ORDER BY LockUntil DESC;

-- Check failed login patterns
SELECT 
    Username,
    COUNT(*) AS FailedAttempts,
    MIN(Timestamp) AS FirstAttempt,
    MAX(Timestamp) AS LastAttempt
FROM AuditLogs
WHERE ActionType = 'LOGIN_FAILED'
AND Timestamp > DATEADD(HOUR, -1, GETUTCDATE())
GROUP BY Username
HAVING COUNT(*) > 5
ORDER BY FailedAttempts DESC;
```

**Resolution**:

1. **Identify attack source**: Check IP addresses in audit logs
2. **Block malicious IPs**: Add to firewall blacklist
3. **Notify security team**: Report potential brute force attack
4. **Monitor**: Continue monitoring for additional attempts
5. **Consider**: Temporary increase in rate limiting

**Prevention**:
- Ensure rate limiting is properly configured
- Review firewall rules
- Consider implementing CAPTCHA for repeated failures

---

## 4. Session Management

### 4.1 View Active Sessions

**When to Use**: Security audit, capacity planning, troubleshooting

**Procedure**:

```sql
-- Count active sessions
SELECT COUNT(*) AS ActiveSessions
FROM Sessions
WHERE IsActive = 1
AND TokenExpiration > GETUTCDATE();

-- View active sessions by user
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

-- View sessions by time range
SELECT 
    DATEPART(HOUR, LoginAt) AS Hour,
    COUNT(*) AS SessionCount
FROM Sessions
WHERE LoginAt > DATEADD(DAY, -1, GETUTCDATE())
GROUP BY DATEPART(HOUR, LoginAt)
ORDER BY Hour;
```

**Metrics to Monitor**:
- Total active sessions
- Sessions per user
- Peak session times
- Average session duration

---

### 4.2 Terminate User Sessions

**When to Use**: Security incident, user account compromise, forced logout

**Procedure**:

```sql
-- Terminate all sessions for a user
UPDATE Sessions
SET 
    IsActive = 0,
    LogoutAt = GETUTCDATE()
WHERE UserId = '{user_id}'
AND IsActive = 1;

-- Verify termination
SELECT * FROM Sessions
WHERE UserId = '{user_id}'
AND IsActive = 1;
```

**Expected Result**: No active sessions for user

**Verification**: User must log in again to access system

---

### 4.3 Clean Up Expired Sessions

**When to Use**: Routine maintenance, database cleanup

**Procedure**:

```sql
-- Identify expired sessions
SELECT COUNT(*) AS ExpiredSessions
FROM Sessions
WHERE IsActive = 1
AND TokenExpiration < GETUTCDATE();

-- Mark expired sessions as inactive
UPDATE Sessions
SET IsActive = 0
WHERE IsActive = 1
AND TokenExpiration < GETUTCDATE();

-- Archive old sessions (older than 90 days)
DELETE FROM Sessions
WHERE LoginAt < DATEADD(DAY, -90, GETUTCDATE());
```

**Schedule**: Run daily via automated job

---

## 5. Password Reset Procedures

### 5.1 Administrative Password Reset

**When to Use**: User forgot password, account recovery

**Prerequisites**:
- Administrator credentials
- User verification (confirm identity)
- Approval from manager

**Procedure**:

```bash
# Step 1: Generate temporary password
TEMP_PASSWORD=$(openssl rand -base64 12)

# Step 2: Update user password (requires direct database access)
# Note: In production, use dedicated admin endpoint
```

```sql
-- Hash password using BCrypt (strength 10)
-- This should be done through application API, not directly in database
-- For emergency use only:

DECLARE @Username NVARCHAR(100) = '{username}';
DECLARE @TempPassword NVARCHAR(255) = '{bcrypt_hashed_password}';

UPDATE Users
SET 
    PasswordHash = @TempPassword,
    UpdatedAt = GETUTCDATE()
WHERE Username = @Username;

-- Invalidate all existing sessions
UPDATE Sessions
SET IsActive = 0, LogoutAt = GETUTCDATE()
WHERE UserId = (SELECT Id FROM Users WHERE Username = @Username);
```

**Step 3: Notify user**
- Send temporary password via secure channel (not email)
- Instruct user to change password immediately upon login

**Security Note**: Never send passwords via email. Use secure communication channel.

---

### 5.2 User Self-Service Password Change

**When to Use**: User wants to change password

**Procedure**:

```bash
# User must be authenticated
curl -X POST "https://api.example.com/api/v1/profile/change-password" \
  -H "Authorization: Bearer {user_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "{current_password}",
    "newPassword": "{new_password}"
  }'
```

**Expected Result**: 
- HTTP 200 OK
- All existing sessions invalidated
- User must log in with new password

**Validation**:
- Current password must be correct
- New password must meet complexity requirements:
  - Minimum 8 characters
  - At least 1 uppercase letter
  - At least 1 lowercase letter
  - At least 1 digit
  - At least 1 special character

---

## 6. Role Management

### 6.1 Assign Role to User

**When to Use**: Promotion, role change, additional permissions needed

**Prerequisites**:
- Administrator credentials
- User ID
- Role to assign (ADMINISTRATOR, ASSET_MANAGER, VIEWER)
- Manager approval

**Procedure**:

```bash
# Step 1: Verify current roles
curl -X GET "https://api.example.com/api/v1/users/{user_id}" \
  -H "Authorization: Bearer {admin_token}"

# Step 2: Assign new role
curl -X POST "https://api.example.com/api/v1/users/{user_id}/roles" \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "role": "ASSET_MANAGER"
  }'

# Step 3: Verify role assignment
curl -X GET "https://api.example.com/api/v1/users/{user_id}" \
  -H "Authorization: Bearer {admin_token}"
```

**Expected Result**:
- HTTP 200 OK
- User has new role
- All existing sessions invalidated (user must log in again)

**Post-Action**: Notify user of role change and new permissions

---

### 6.2 Revoke Role from User

**When to Use**: Role change, demotion, security incident

**Prerequisites**:
- Administrator credentials
- User ID
- Role to revoke
- Manager approval

**Procedure**:

```bash
# Step 1: Verify user has multiple roles
curl -X GET "https://api.example.com/api/v1/users/{user_id}" \
  -H "Authorization: Bearer {admin_token}"

# Step 2: Revoke role
curl -X DELETE "https://api.example.com/api/v1/users/{user_id}/roles/ASSET_MANAGER" \
  -H "Authorization: Bearer {admin_token}"

# Step 3: Verify role revocation
curl -X GET "https://api.example.com/api/v1/users/{user_id}" \
  -H "Authorization: Bearer {admin_token}"
```

**Expected Result**:
- HTTP 204 No Content
- User no longer has revoked role
- All existing sessions invalidated

**Warning**: Cannot revoke user's last role. User must have at least one role.

---

## 7. Performance Issues

### 7.1 Slow Login Response Times

**Symptoms**:
- Login taking > 500ms
- Alert: "SlowResponseTime"
- User complaints about slow authentication

**Diagnosis**:

```sql
-- Check database query performance
SELECT 
    qs.execution_count,
    qs.total_elapsed_time / 1000000.0 AS total_elapsed_time_sec,
    qs.total_elapsed_time / qs.execution_count / 1000000.0 AS avg_elapsed_time_sec,
    SUBSTRING(qt.text, (qs.statement_start_offset/2)+1,
        ((CASE qs.statement_end_offset
            WHEN -1 THEN DATALENGTH(qt.text)
            ELSE qs.statement_end_offset
        END - qs.statement_start_offset)/2)+1) AS query_text
FROM sys.dm_exec_query_stats qs
CROSS APPLY sys.dm_exec_sql_text(qs.sql_handle) qt
WHERE qt.text LIKE '%Users%'
ORDER BY qs.total_elapsed_time DESC;

-- Check index fragmentation
SELECT 
    OBJECT_NAME(ips.object_id) AS TableName,
    i.name AS IndexName,
    ips.avg_fragmentation_in_percent
FROM sys.dm_db_index_physical_stats(DB_ID(), OBJECT_ID('Users'), NULL, NULL, 'LIMITED') ips
INNER JOIN sys.indexes i ON ips.object_id = i.object_id AND ips.index_id = i.index_id
WHERE i.name IS NOT NULL;
```

**Resolution**:

1. **Rebuild fragmented indexes**:
```sql
ALTER INDEX IX_Users_Username ON Users REBUILD;
ALTER INDEX IX_Users_Email ON Users REBUILD;
```

2. **Update statistics**:
```sql
UPDATE STATISTICS Users WITH FULLSCAN;
```

3. **Check connection pool**:
```bash
# View HikariCP metrics
curl -X GET "https://api.example.com/actuator/metrics/hikaricp.connections.active"
```

4. **Review application logs**:
```bash
tail -f /var/log/it-asset-management/application.log | grep "AuthenticationService"
```

---

### 7.2 High Database Connection Usage

**Symptoms**:
- Alert: "DatabaseConnectionPoolExhaustion"
- Connection timeout errors
- Slow response times

**Diagnosis**:

```sql
-- Check active connections
SELECT 
    DB_NAME(dbid) as DatabaseName,
    COUNT(dbid) as NumberOfConnections,
    loginame as LoginName
FROM sys.sysprocesses
WHERE dbid > 0
GROUP BY dbid, loginame;

-- Check long-running queries
SELECT 
    r.session_id,
    r.start_time,
    r.status,
    r.command,
    r.wait_type,
    r.total_elapsed_time
FROM sys.dm_exec_requests r
WHERE r.total_elapsed_time > 5000
ORDER BY r.total_elapsed_time DESC;
```

**Resolution**:

1. **Kill long-running queries** (if safe):
```sql
KILL {session_id};
```

2. **Increase connection pool size** (temporary):
```properties
spring.datasource.hikari.maximum-pool-size=30
```

3. **Restart application** (if needed):
```bash
systemctl restart it-asset-management
```

4. **Investigate root cause**: Review slow queries and optimize

---

## 8. Security Incidents

### 8.1 Suspected Brute Force Attack

**Symptoms**:
- Alert: "FailedLoginSpike"
- Multiple failed login attempts from same IP
- Multiple accounts locked

**Immediate Actions**:

1. **Identify attack source**:
```sql
SELECT 
    Details->>'$.ipAddress' AS IpAddress,
    COUNT(*) AS FailedAttempts
FROM AuditLogs
WHERE ActionType = 'LOGIN_FAILED'
AND Timestamp > DATEADD(MINUTE, -15, GETUTCDATE())
GROUP BY Details->>'$.ipAddress'
HAVING COUNT(*) > 20
ORDER BY FailedAttempts DESC;
```

2. **Block malicious IPs**:
```bash
# Add to firewall blacklist
sudo iptables -A INPUT -s {malicious_ip} -j DROP
```

3. **Notify security team**: Send incident report

4. **Monitor**: Continue monitoring for additional attempts

5. **Review**: Check if any accounts were compromised

**Post-Incident**:
- Review and strengthen rate limiting
- Consider implementing CAPTCHA
- Update security policies

---

### 8.2 Suspected Account Compromise

**Symptoms**:
- Unusual login location
- Login at unusual time
- User reports unauthorized access

**Immediate Actions**:

1. **Disable account**:
```bash
curl -X PATCH "https://api.example.com/api/v1/users/{user_id}/disable" \
  -H "Authorization: Bearer {admin_token}"
```

2. **Terminate all sessions**:
```sql
UPDATE Sessions
SET IsActive = 0, LogoutAt = GETUTCDATE()
WHERE UserId = '{user_id}';
```

3. **Review audit logs**:
```sql
SELECT * FROM AuditLogs
WHERE UserId = '{user_id}'
AND Timestamp > DATEADD(DAY, -7, GETUTCDATE())
ORDER BY Timestamp DESC;
```

4. **Contact user**: Verify recent activity

5. **Reset password**: Force password change

6. **Investigate**: Determine how account was compromised

**Post-Incident**:
- Document findings
- Update security procedures
- Consider additional security measures (MFA)

---

## 9. Database Maintenance

### 9.1 User Table Maintenance

**When to Use**: Monthly maintenance window

**Procedure**:

```sql
-- 1. Update statistics
UPDATE STATISTICS Users WITH FULLSCAN;
UPDATE STATISTICS UserRoles WITH FULLSCAN;
UPDATE STATISTICS Sessions WITH FULLSCAN;

-- 2. Rebuild fragmented indexes
ALTER INDEX IX_Users_Username ON Users REBUILD;
ALTER INDEX IX_Users_Email ON Users REBUILD;
ALTER INDEX IX_Users_AccountLocked ON Users REBUILD;
ALTER INDEX IX_UserRoles_UserId ON UserRoles REBUILD;
ALTER INDEX IX_Sessions_UserId ON Sessions REBUILD;

-- 3. Clean up old sessions (> 90 days)
DELETE FROM Sessions
WHERE LoginAt < DATEADD(DAY, -90, GETUTCDATE());

-- 4. Verify data integrity
SELECT 
    u.Username,
    COUNT(ur.Id) AS RoleCount
FROM Users u
LEFT JOIN UserRoles ur ON u.Id = ur.UserId
GROUP BY u.Username
HAVING COUNT(ur.Id) = 0;
-- Should return no results (all users must have at least one role)
```

**Schedule**: Run during maintenance window (low traffic period)

---

### 9.2 Backup Verification

**When to Use**: Daily, after backup completion

**Procedure**:

```bash
# 1. Verify backup file exists
ls -lh /backups/ITAssetManagement_Full_$(date +%Y%m%d).bak

# 2. Check backup size (should be consistent)
du -h /backups/ITAssetManagement_Full_$(date +%Y%m%d).bak

# 3. Verify backup integrity (test restore to separate database)
sqlcmd -S localhost -Q "RESTORE VERIFYONLY FROM DISK = '/backups/ITAssetManagement_Full_$(date +%Y%m%d).bak'"

# 4. Document verification
echo "$(date): Backup verified successfully" >> /var/log/backup-verification.log
```

**Alert**: If verification fails, notify DBA immediately

---

## 10. Monitoring and Alerts

### 10.1 Key Metrics to Monitor

**Authentication Metrics**:
- Login success rate: Should be > 95%
- Login response time: Should be < 500ms (p95)
- Failed login rate: Should be < 5%
- Account lockout rate: Monitor for spikes

**Session Metrics**:
- Active sessions: Monitor capacity
- Session creation rate: Track user activity
- Session duration: Average should be reasonable

**Performance Metrics**:
- Database query time: Should be < 200ms (p95)
- Connection pool usage: Should be < 80%
- JVM memory usage: Should be < 80%
- CPU usage: Should be < 70%

---

### 10.2 Alert Response Times

| Severity | Response Time | Escalation |
|----------|---------------|------------|
| Critical | 15 minutes | Immediate |
| High | 1 hour | If unresolved in 2 hours |
| Medium | 4 hours | If unresolved in 8 hours |
| Low | Next business day | N/A |

---

### 10.3 Common Alerts and Responses

**Alert: HighErrorRate**
- **Action**: Check application logs, review recent deployments
- **Escalate**: If error rate > 10% for 10 minutes

**Alert: SlowResponseTime**
- **Action**: Check database performance, review slow queries
- **Escalate**: If p95 > 2s for 15 minutes

**Alert: DatabaseConnectionPoolExhaustion**
- **Action**: Check for connection leaks, kill long-running queries
- **Escalate**: Immediately if pool fully exhausted

**Alert: ApplicationDown**
- **Action**: Restart application, check database connectivity
- **Escalate**: Immediately

**Alert: FailedLoginSpike**
- **Action**: Investigate for brute force attack, block malicious IPs
- **Escalate**: If attack continues for 10 minutes

---

## Appendix: Quick Reference Commands

### User Management
```bash
# List users
curl -X GET "https://api.example.com/api/v1/users" -H "Authorization: Bearer {token}"

# Get user details
curl -X GET "https://api.example.com/api/v1/users/{id}" -H "Authorization: Bearer {token}"

# Disable user
curl -X PATCH "https://api.example.com/api/v1/users/{id}/disable" -H "Authorization: Bearer {token}"

# Enable user
curl -X PATCH "https://api.example.com/api/v1/users/{id}/enable" -H "Authorization: Bearer {token}"
```

### Database Queries
```sql
-- Active users
SELECT COUNT(*) FROM Users WHERE IsActive = 1;

-- Locked accounts
SELECT Username, LockUntil FROM Users WHERE AccountLocked = 1;

-- Active sessions
SELECT COUNT(*) FROM Sessions WHERE IsActive = 1 AND TokenExpiration > GETUTCDATE();

-- Recent failed logins
SELECT TOP 10 * FROM AuditLogs WHERE ActionType = 'LOGIN_FAILED' ORDER BY Timestamp DESC;
```

### Application Management
```bash
# Check application status
systemctl status it-asset-management

# Restart application
systemctl restart it-asset-management

# View logs
tail -f /var/log/it-asset-management/application.log

# Check health
curl -X GET "https://api.example.com/actuator/health"
```

---

## Emergency Contacts

| Role | Name | Phone | Email | Availability |
|------|------|-------|-------|--------------|
| On-Call Engineer | TBD | TBD | TBD | 24/7 |
| Senior Engineer | TBD | TBD | TBD | Business hours |
| Database Administrator | TBD | TBD | TBD | 24/7 |
| Security Team | TBD | TBD | TBD | 24/7 |
| IT Manager | TBD | TBD | TBD | Business hours |

---

**Document Version**: 1.0  
**Last Updated**: 2024-01-15  
**Owner**: Operations Team  
**Review Schedule**: Quarterly

