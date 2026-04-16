# Disaster Recovery Testing - Module 1: User Management

## Overview
This document outlines the disaster recovery (DR) testing procedures for the User Management module, including backup strategies, restore procedures, and recovery validation.

---

## 1. Disaster Recovery Objectives

### 1.1 Recovery Time Objective (RTO)
**Target**: 4 hours

The maximum acceptable time to restore the system to operational status after a disaster.

### 1.2 Recovery Point Objective (RPO)
**Target**: 15 minutes

The maximum acceptable amount of data loss measured in time. With transaction log backups every 15 minutes, we can restore to within 15 minutes of the disaster.

### 1.3 Service Level Agreement (SLA)
- **Uptime**: 99.9% during business hours (8 AM - 6 PM, Monday-Friday)
- **Downtime Allowance**: ~8.76 hours per year
- **Planned Maintenance**: Excluded from SLA calculations

---

## 2. Backup Strategy

### 2.1 Backup Types

#### Full Backup
- **Frequency**: Daily at 2:00 AM
- **Retention**: 30 days
- **Storage**: On-site and off-site
- **Compression**: Enabled
- **Verification**: Automated integrity check

#### Differential Backup
- **Frequency**: Every 6 hours (8 AM, 2 PM, 8 PM)
- **Retention**: 7 days
- **Storage**: On-site
- **Compression**: Enabled

#### Transaction Log Backup
- **Frequency**: Every 15 minutes
- **Retention**: 7 days
- **Storage**: On-site and off-site
- **Compression**: Enabled

### 2.2 Backup Scripts

#### Full Backup Script
```sql
-- Full Database Backup
DECLARE @BackupPath NVARCHAR(500);
DECLARE @BackupFile NVARCHAR(500);
DECLARE @DatabaseName NVARCHAR(100) = 'ITAssetManagement';

-- Generate backup file name with timestamp
SET @BackupFile = @DatabaseName + '_Full_' + 
                  CONVERT(VARCHAR(8), GETDATE(), 112) + '_' +
                  REPLACE(CONVERT(VARCHAR(8), GETDATE(), 108), ':', '') + '.bak';
SET @BackupPath = 'C:\Backups\' + @BackupFile;

-- Perform full backup
BACKUP DATABASE @DatabaseName
TO DISK = @BackupPath
WITH 
    FORMAT,
    COMPRESSION,
    STATS = 10,
    CHECKSUM,
    DESCRIPTION = 'Full backup of IT Asset Management database';

-- Verify backup
RESTORE VERIFYONLY 
FROM DISK = @BackupPath;

PRINT 'Full backup completed successfully: ' + @BackupPath;
```

#### Differential Backup Script
```sql
-- Differential Database Backup
DECLARE @BackupPath NVARCHAR(500);
DECLARE @BackupFile NVARCHAR(500);
DECLARE @DatabaseName NVARCHAR(100) = 'ITAssetManagement';

-- Generate backup file name with timestamp
SET @BackupFile = @DatabaseName + '_Diff_' + 
                  CONVERT(VARCHAR(8), GETDATE(), 112) + '_' +
                  REPLACE(CONVERT(VARCHAR(8), GETDATE(), 108), ':', '') + '.bak';
SET @BackupPath = 'C:\Backups\' + @BackupFile;

-- Perform differential backup
BACKUP DATABASE @DatabaseName
TO DISK = @BackupPath
WITH 
    DIFFERENTIAL,
    COMPRESSION,
    STATS = 10,
    CHECKSUM,
    DESCRIPTION = 'Differential backup of IT Asset Management database';

-- Verify backup
RESTORE VERIFYONLY 
FROM DISK = @BackupPath;

PRINT 'Differential backup completed successfully: ' + @BackupPath;
```

#### Transaction Log Backup Script
```sql
-- Transaction Log Backup
DECLARE @BackupPath NVARCHAR(500);
DECLARE @BackupFile NVARCHAR(500);
DECLARE @DatabaseName NVARCHAR(100) = 'ITAssetManagement';

-- Generate backup file name with timestamp
SET @BackupFile = @DatabaseName + '_Log_' + 
                  CONVERT(VARCHAR(8), GETDATE(), 112) + '_' +
                  REPLACE(CONVERT(VARCHAR(8), GETDATE(), 108), ':', '') + '.trn';
SET @BackupPath = 'C:\Backups\' + @BackupFile;

-- Perform transaction log backup
BACKUP LOG @DatabaseName
TO DISK = @BackupPath
WITH 
    COMPRESSION,
    STATS = 10,
    CHECKSUM,
    DESCRIPTION = 'Transaction log backup of IT Asset Management database';

-- Verify backup
RESTORE VERIFYONLY 
FROM DISK = @BackupPath;

PRINT 'Transaction log backup completed successfully: ' + @BackupPath;
```

### 2.3 Automated Backup Jobs

#### SQL Server Agent Job Configuration
```sql
USE msdb;
GO

-- Create Full Backup Job
EXEC dbo.sp_add_job
    @job_name = N'ITAssetManagement_FullBackup',
    @enabled = 1,
    @description = N'Daily full backup of IT Asset Management database',
    @category_name = N'Database Maintenance';

EXEC dbo.sp_add_jobstep
    @job_name = N'ITAssetManagement_FullBackup',
    @step_name = N'Perform Full Backup',
    @subsystem = N'TSQL',
    @command = N'
        DECLARE @BackupPath NVARCHAR(500);
        DECLARE @BackupFile NVARCHAR(500);
        SET @BackupFile = ''ITAssetManagement_Full_'' + 
                         CONVERT(VARCHAR(8), GETDATE(), 112) + ''.bak'';
        SET @BackupPath = ''C:\Backups\'' + @BackupFile;
        
        BACKUP DATABASE ITAssetManagement
        TO DISK = @BackupPath
        WITH FORMAT, COMPRESSION, STATS = 10, CHECKSUM;
        
        RESTORE VERIFYONLY FROM DISK = @BackupPath;
    ',
    @on_success_action = 1,
    @on_fail_action = 2;

EXEC dbo.sp_add_jobschedule
    @job_name = N'ITAssetManagement_FullBackup',
    @name = N'Daily at 2 AM',
    @freq_type = 4,  -- Daily
    @freq_interval = 1,
    @active_start_time = 020000;  -- 2:00 AM

-- Create Differential Backup Job
EXEC dbo.sp_add_job
    @job_name = N'ITAssetManagement_DiffBackup',
    @enabled = 1,
    @description = N'Differential backup every 6 hours',
    @category_name = N'Database Maintenance';

EXEC dbo.sp_add_jobstep
    @job_name = N'ITAssetManagement_DiffBackup',
    @step_name = N'Perform Differential Backup',
    @subsystem = N'TSQL',
    @command = N'
        DECLARE @BackupPath NVARCHAR(500);
        DECLARE @BackupFile NVARCHAR(500);
        SET @BackupFile = ''ITAssetManagement_Diff_'' + 
                         CONVERT(VARCHAR(8), GETDATE(), 112) + ''_'' +
                         REPLACE(CONVERT(VARCHAR(8), GETDATE(), 108), '':'', '''') + ''.bak'';
        SET @BackupPath = ''C:\Backups\'' + @BackupFile;
        
        BACKUP DATABASE ITAssetManagement
        TO DISK = @BackupPath
        WITH DIFFERENTIAL, COMPRESSION, STATS = 10, CHECKSUM;
        
        RESTORE VERIFYONLY FROM DISK = @BackupPath;
    ',
    @on_success_action = 1,
    @on_fail_action = 2;

EXEC dbo.sp_add_jobschedule
    @job_name = N'ITAssetManagement_DiffBackup',
    @name = N'Every 6 Hours',
    @freq_type = 4,  -- Daily
    @freq_interval = 1,
    @freq_subday_type = 8,  -- Hours
    @freq_subday_interval = 6,
    @active_start_time = 080000;  -- 8:00 AM

-- Create Transaction Log Backup Job
EXEC dbo.sp_add_job
    @job_name = N'ITAssetManagement_LogBackup',
    @enabled = 1,
    @description = N'Transaction log backup every 15 minutes',
    @category_name = N'Database Maintenance';

EXEC dbo.sp_add_jobstep
    @job_name = N'ITAssetManagement_LogBackup',
    @step_name = N'Perform Log Backup',
    @subsystem = N'TSQL',
    @command = N'
        DECLARE @BackupPath NVARCHAR(500);
        DECLARE @BackupFile NVARCHAR(500);
        SET @BackupFile = ''ITAssetManagement_Log_'' + 
                         CONVERT(VARCHAR(8), GETDATE(), 112) + ''_'' +
                         REPLACE(CONVERT(VARCHAR(8), GETDATE(), 108), '':'', '''') + ''.trn'';
        SET @BackupPath = ''C:\Backups\'' + @BackupFile;
        
        BACKUP LOG ITAssetManagement
        TO DISK = @BackupPath
        WITH COMPRESSION, STATS = 10, CHECKSUM;
        
        RESTORE VERIFYONLY FROM DISK = @BackupPath;
    ',
    @on_success_action = 1,
    @on_fail_action = 2;

EXEC dbo.sp_add_jobschedule
    @job_name = N'ITAssetManagement_LogBackup',
    @name = N'Every 15 Minutes',
    @freq_type = 4,  -- Daily
    @freq_interval = 1,
    @freq_subday_type = 4,  -- Minutes
    @freq_subday_interval = 15,
    @active_start_time = 000000;  -- 12:00 AM
```

---

## 3. Restore Procedures

### 3.1 Full Database Restore

#### Scenario: Complete database loss

```sql
-- Step 1: Verify backup file exists
RESTORE FILELISTONLY 
FROM DISK = 'C:\Backups\ITAssetManagement_Full_20260410.bak';

-- Step 2: Restore database (with REPLACE to overwrite existing)
RESTORE DATABASE ITAssetManagement
FROM DISK = 'C:\Backups\ITAssetManagement_Full_20260410.bak'
WITH 
    REPLACE,
    RECOVERY,
    STATS = 10;

-- Step 3: Verify database integrity
DBCC CHECKDB(ITAssetManagement) WITH NO_INFOMSGS;

-- Step 4: Verify data
SELECT COUNT(*) AS UserCount FROM Users;
SELECT COUNT(*) AS RoleCount FROM UserRoles;
SELECT COUNT(*) AS SessionCount FROM Sessions;
SELECT COUNT(*) AS AuditCount FROM AuditLogs;

PRINT 'Full database restore completed successfully';
```

### 3.2 Point-in-Time Recovery

#### Scenario: Recover to specific point in time (e.g., before data corruption)

```sql
-- Step 1: Restore full backup (NORECOVERY to allow log application)
RESTORE DATABASE ITAssetManagement
FROM DISK = 'C:\Backups\ITAssetManagement_Full_20260410.bak'
WITH 
    REPLACE,
    NORECOVERY,
    STATS = 10;

-- Step 2: Restore differential backup (NORECOVERY)
RESTORE DATABASE ITAssetManagement
FROM DISK = 'C:\Backups\ITAssetManagement_Diff_20260410_1400.bak'
WITH 
    NORECOVERY,
    STATS = 10;

-- Step 3: Restore transaction logs up to specific time
-- Restore all log backups in sequence
RESTORE LOG ITAssetManagement
FROM DISK = 'C:\Backups\ITAssetManagement_Log_20260410_1415.trn'
WITH NORECOVERY, STATS = 10;

RESTORE LOG ITAssetManagement
FROM DISK = 'C:\Backups\ITAssetManagement_Log_20260410_1430.trn'
WITH NORECOVERY, STATS = 10;

-- Step 4: Final log restore with STOPAT (point-in-time)
RESTORE LOG ITAssetManagement
FROM DISK = 'C:\Backups\ITAssetManagement_Log_20260410_1445.trn'
WITH 
    RECOVERY,
    STOPAT = '2026-04-10 14:40:00',  -- Restore to this specific time
    STATS = 10;

-- Step 5: Verify database integrity
DBCC CHECKDB(ITAssetManagement) WITH NO_INFOMSGS;

-- Step 6: Verify data at restored point in time
SELECT TOP 10 * FROM Users ORDER BY UpdatedAt DESC;
SELECT TOP 10 * FROM AuditLogs ORDER BY Timestamp DESC;

PRINT 'Point-in-time recovery completed successfully';
```

### 3.3 Table-Level Recovery

#### Scenario: Recover specific table (e.g., accidentally deleted users)

```sql
-- Step 1: Restore database to temporary database
RESTORE DATABASE ITAssetManagement_Temp
FROM DISK = 'C:\Backups\ITAssetManagement_Full_20260410.bak'
WITH 
    MOVE 'ITAssetManagement' TO 'C:\TempRestore\ITAssetManagement_Temp.mdf',
    MOVE 'ITAssetManagement_log' TO 'C:\TempRestore\ITAssetManagement_Temp_log.ldf',
    RECOVERY,
    STATS = 10;

-- Step 2: Copy specific data from temp database to production
USE ITAssetManagement;

-- Restore deleted users (example: users deleted after specific time)
INSERT INTO Users (Id, Username, PasswordHash, Email, IsActive, AccountLocked, 
                   FailedLoginAttempts, CreatedAt, UpdatedAt, CreatedBy, UpdatedBy)
SELECT Id, Username, PasswordHash, Email, IsActive, AccountLocked,
       FailedLoginAttempts, CreatedAt, UpdatedAt, CreatedBy, UpdatedBy
FROM ITAssetManagement_Temp.dbo.Users
WHERE Id NOT IN (SELECT Id FROM Users);

-- Restore associated roles
INSERT INTO UserRoles (Id, UserId, Role, AssignedBy, AssignedAt)
SELECT Id, UserId, Role, AssignedBy, AssignedAt
FROM ITAssetManagement_Temp.dbo.UserRoles
WHERE Id NOT IN (SELECT Id FROM UserRoles);

-- Step 3: Drop temporary database
DROP DATABASE ITAssetManagement_Temp;

-- Step 4: Verify restored data
SELECT COUNT(*) AS RestoredUsers FROM Users 
WHERE CreatedAt > '2026-04-10 00:00:00';

PRINT 'Table-level recovery completed successfully';
```

---

## 4. Disaster Recovery Test Scenarios

### 4.1 Test Scenario 1: Complete Database Loss

**Objective**: Validate full database restore procedure

**Pre-Test Setup**:
1. Create full backup of production database
2. Document current database state (record counts, checksums)
3. Prepare restore environment

**Test Steps**:
1. Simulate database loss (drop database or detach)
2. Execute full restore procedure
3. Verify database integrity (DBCC CHECKDB)
4. Verify data completeness (record counts)
5. Verify application connectivity
6. Test user authentication
7. Test user management operations
8. Verify audit log continuity

**Success Criteria**:
- [ ] Database restored successfully
- [ ] All data intact (matching pre-test counts)
- [ ] Database integrity check passes
- [ ] Application connects successfully
- [ ] User authentication works
- [ ] All CRUD operations work
- [ ] Audit logs are complete
- [ ] Restore completed within RTO (4 hours)

**Rollback Plan**:
- Restore from original backup if test fails

### 4.2 Test Scenario 2: Point-in-Time Recovery

**Objective**: Validate point-in-time restore to recover from data corruption

**Pre-Test Setup**:
1. Create full backup
2. Create differential backup
3. Create multiple transaction log backups
4. Document specific timestamp for recovery

**Test Steps**:
1. Simulate data corruption (update/delete records)
2. Note exact time of corruption
3. Execute point-in-time restore to time before corruption
4. Verify database integrity
5. Verify corrupted data is not present
6. Verify data before corruption is intact
7. Test application functionality

**Success Criteria**:
- [ ] Database restored to exact point in time
- [ ] Corrupted data not present
- [ ] Pre-corruption data intact
- [ ] Database integrity check passes
- [ ] Application works correctly
- [ ] Restore completed within RTO (4 hours)
- [ ] Data loss within RPO (15 minutes)

### 4.3 Test Scenario 3: Table-Level Recovery

**Objective**: Validate selective table restore

**Pre-Test Setup**:
1. Create full backup
2. Document specific table state (Users table)
3. Prepare temporary restore location

**Test Steps**:
1. Simulate accidental deletion (delete users)
2. Restore database to temporary location
3. Copy deleted records from temp to production
4. Verify restored records
5. Verify no duplicate records
6. Test application functionality
7. Clean up temporary database

**Success Criteria**:
- [ ] Deleted records restored successfully
- [ ] No duplicate records created
- [ ] Related data (roles, sessions) intact
- [ ] Application works correctly
- [ ] Restore completed within RTO (4 hours)

### 4.4 Test Scenario 4: Backup Verification

**Objective**: Validate backup integrity and restorability

**Test Steps**:
1. Select random backup file
2. Execute RESTORE VERIFYONLY
3. Restore to test environment
4. Verify database integrity
5. Verify data completeness
6. Document results

**Success Criteria**:
- [ ] Backup verification passes
- [ ] Restore completes successfully
- [ ] Database integrity check passes
- [ ] Data is complete and accessible

**Frequency**: Weekly automated test

### 4.5 Test Scenario 5: Disaster Recovery Drill

**Objective**: Full disaster recovery simulation

**Pre-Test Setup**:
1. Schedule maintenance window
2. Notify stakeholders
3. Prepare DR environment
4. Document current production state

**Test Steps**:
1. **Simulate Disaster** (2:00 PM)
   - Take production database offline
   - Simulate complete server failure

2. **Initiate DR Procedures** (2:05 PM)
   - Activate DR team
   - Begin restore procedures
   - Communicate status to stakeholders

3. **Restore Database** (2:10 PM - 4:00 PM)
   - Restore full backup
   - Apply differential backup
   - Apply transaction logs
   - Verify database integrity

4. **Restore Application** (4:00 PM - 5:00 PM)
   - Deploy application to DR environment
   - Configure environment variables
   - Start application services
   - Verify health checks

5. **Validation** (5:00 PM - 5:30 PM)
   - Test user authentication
   - Test user management operations
   - Verify data integrity
   - Test all critical workflows

6. **Cutover** (5:30 PM - 6:00 PM)
   - Update DNS/load balancer
   - Redirect traffic to DR environment
   - Monitor application performance
   - Verify user access

**Success Criteria**:
- [ ] RTO met (< 4 hours from disaster to operational)
- [ ] RPO met (< 15 minutes data loss)
- [ ] All critical functions operational
- [ ] User authentication works
- [ ] Data integrity verified
- [ ] Application performance acceptable
- [ ] Stakeholders notified of status

**Post-Test Activities**:
1. Document lessons learned
2. Update DR procedures
3. Update runbooks
4. Train operations team
5. Schedule next DR drill

---

## 5. Backup Monitoring and Alerting

### 5.1 Backup Monitoring Queries

```sql
-- Check last successful backup
SELECT 
    database_name,
    type,
    MAX(backup_finish_date) AS last_backup_date,
    DATEDIFF(HOUR, MAX(backup_finish_date), GETDATE()) AS hours_since_backup
FROM msdb.dbo.backupset
WHERE database_name = 'ITAssetManagement'
GROUP BY database_name, type
ORDER BY type;

-- Check backup history (last 7 days)
SELECT 
    database_name,
    type,
    backup_start_date,
    backup_finish_date,
    DATEDIFF(SECOND, backup_start_date, backup_finish_date) AS duration_seconds,
    backup_size / 1024 / 1024 AS backup_size_mb,
    compressed_backup_size / 1024 / 1024 AS compressed_size_mb
FROM msdb.dbo.backupset
WHERE database_name = 'ITAssetManagement'
    AND backup_start_date >= DATEADD(DAY, -7, GETDATE())
ORDER BY backup_start_date DESC;

-- Check failed backups
SELECT 
    session_id,
    database_name,
    error_number,
    error_message,
    start_time,
    end_time
FROM msdb.dbo.backuphistory bh
INNER JOIN msdb.dbo.backupmediafamily bmf ON bh.media_set_id = bmf.media_set_id
WHERE database_name = 'ITAssetManagement'
    AND error_number <> 0
    AND start_time >= DATEADD(DAY, -7, GETDATE())
ORDER BY start_time DESC;
```

### 5.2 Alerting Rules

**Critical Alerts**:
- Full backup not completed in last 25 hours
- Transaction log backup not completed in last 20 minutes
- Backup failure detected
- Backup verification failed
- Disk space low on backup location

**Warning Alerts**:
- Differential backup not completed in last 7 hours
- Backup duration exceeds baseline by 50%
- Backup size increased significantly
- Backup retention policy violation

---

## 6. Disaster Recovery Checklist

### 6.1 Pre-Disaster Preparation
- [ ] Backup strategy documented and approved
- [ ] Backup jobs configured and tested
- [ ] Backup verification automated
- [ ] Off-site backup storage configured
- [ ] DR procedures documented
- [ ] DR team identified and trained
- [ ] DR environment prepared
- [ ] Monitoring and alerting configured
- [ ] Stakeholder communication plan established

### 6.2 During Disaster
- [ ] Disaster declared and DR team activated
- [ ] Stakeholders notified
- [ ] Cause of disaster identified (if possible)
- [ ] DR procedures initiated
- [ ] Progress communicated regularly
- [ ] Issues documented and escalated

### 6.3 Post-Disaster Recovery
- [ ] Database restored and verified
- [ ] Application restored and tested
- [ ] Data integrity validated
- [ ] User access verified
- [ ] Performance validated
- [ ] Monitoring re-established
- [ ] Stakeholders notified of recovery
- [ ] Post-mortem scheduled

### 6.4 Post-Mortem
- [ ] Root cause analysis completed
- [ ] Timeline documented
- [ ] Lessons learned documented
- [ ] DR procedures updated
- [ ] Training gaps identified
- [ ] Improvements implemented
- [ ] Next DR drill scheduled

---

## 7. DR Test Schedule

### 7.1 Regular Testing Schedule

| Test Type | Frequency | Duration | Next Scheduled |
|-----------|-----------|----------|----------------|
| Backup Verification | Weekly | 30 min | Every Monday |
| Table-Level Recovery | Monthly | 2 hours | First Friday |
| Point-in-Time Recovery | Quarterly | 3 hours | April 15, 2026 |
| Full DR Drill | Annually | 4 hours | June 1, 2026 |

### 7.2 Test Results Tracking

| Date | Test Type | Status | RTO | RPO | Issues | Actions |
|------|-----------|--------|-----|-----|--------|---------|
| [TBD] | [TBD] | [TBD] | [TBD] | [TBD] | [TBD] | [TBD] |

---

## 8. Sign-Off

### 8.1 Disaster Recovery Testing Approval

- [ ] Backup strategy implemented and tested
- [ ] Restore procedures documented and tested
- [ ] DR drill completed successfully
- [ ] RTO and RPO targets met
- [ ] DR documentation updated
- [ ] Operations team trained

**DR Coordinator**: ___________________  
**Date**: ___________________  

**Database Administrator**: ___________________  
**Date**: ___________________  

**Technical Lead**: ___________________  
**Date**: ___________________  

**Project Manager**: ___________________  
**Date**: ___________________  

---

**Document Version**: 1.0  
**Last Updated**: April 10, 2026  
**Next Review**: After DR testing completion
