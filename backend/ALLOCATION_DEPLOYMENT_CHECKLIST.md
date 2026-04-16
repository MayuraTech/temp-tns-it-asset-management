# Allocation Management - Deployment and Verification Checklist

## Overview

This checklist ensures that the Allocation Management module is properly deployed, configured, and functioning correctly in production.

---

## Pre-Deployment Checklist

### Database Preparation

- [ ] **Database Migration V3 Verified**
  - [ ] Migration file `V3__create_assignment_history_table.sql` exists
  - [ ] Migration creates `AssignmentHistory` table with all required columns
  - [ ] Foreign key constraints to `Assets` and `Users` tables are defined
  - [ ] Indexes on `AssetId`, `AssignedTo`, and `AssignedAt` are created
  - [ ] Check constraint for `AssignmentType` enum values is defined
  - [ ] Migration tested on development database

- [ ] **Database Backup Created**
  - [ ] Full backup of production database completed
  - [ ] Backup verified and stored securely
  - [ ] Rollback plan documented

### Code Review

- [ ] **Backend Code Review**
  - [ ] All service methods implemented and tested
  - [ ] All controller endpoints implemented with proper annotations
  - [ ] Exception handling implemented for all error scenarios
  - [ ] Authorization checks in place (@PreAuthorize annotations)
  - [ ] Audit logging implemented for all state changes
  - [ ] Transaction management configured correctly

- [ ] **Test Coverage**
  - [ ] Unit tests passing (>80% coverage)
  - [ ] Integration tests passing
  - [ ] Property-based tests passing (Properties 18, 19, 20)
  - [ ] All test scenarios documented

### Configuration

- [ ] **Application Properties**
  - [ ] Database connection settings configured
  - [ ] Connection pool settings optimized (min: 5, max: 20)
  - [ ] JWT secret configured securely
  - [ ] Logging levels configured appropriately
  - [ ] CORS settings configured for frontend

- [ ] **Security Configuration**
  - [ ] JWT token expiration set (30 minutes)
  - [ ] Refresh token expiration set (24 hours)
  - [ ] Rate limiting configured (1000 requests/hour)
  - [ ] HTTPS enforced in production

### Documentation

- [ ] **API Documentation**
  - [ ] OpenAPI/Swagger annotations complete
  - [ ] All endpoints documented with examples
  - [ ] Error responses documented
  - [ ] Authentication requirements documented

- [ ] **User Documentation**
  - [ ] User guide created and reviewed
  - [ ] Assignment workflow documented
  - [ ] Troubleshooting guide included
  - [ ] Screenshots and examples provided

---

## Deployment Steps

### Step 1: Database Migration

- [ ] **Run Migration**
  ```bash
  # Verify migration status
  SELECT * FROM flyway_schema_history;
  
  # Run migration (automatic on application startup)
  # Or manually: flyway migrate
  ```

- [ ] **Verify Migration Success**
  ```sql
  -- Check table exists
  SELECT * FROM INFORMATION_SCHEMA.TABLES 
  WHERE TABLE_NAME = 'AssignmentHistory';
  
  -- Check indexes
  SELECT * FROM sys.indexes 
  WHERE object_id = OBJECT_ID('AssignmentHistory');
  
  -- Check constraints
  SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
  WHERE TABLE_NAME = 'AssignmentHistory';
  ```

### Step 2: Application Deployment

- [ ] **Build Application**
  ```bash
  mvn clean package -DskipTests
  ```

- [ ] **Deploy to Server**
  - [ ] Stop existing application
  - [ ] Deploy new JAR file
  - [ ] Update configuration files
  - [ ] Start application

- [ ] **Verify Application Startup**
  - [ ] Check application logs for errors
  - [ ] Verify database connection established
  - [ ] Verify all beans initialized correctly
  - [ ] Check actuator health endpoint: `/actuator/health`

### Step 3: Smoke Tests

- [ ] **Health Check**
  ```bash
  curl https://api.example.com/actuator/health
  # Expected: {"status":"UP"}
  ```

- [ ] **Authentication Test**
  ```bash
  curl -X POST https://api.example.com/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"Admin@123456"}'
  # Expected: JWT token returned
  ```

- [ ] **Create Assignment Test**
  ```bash
  curl -X POST https://api.example.com/api/v1/assets/{id}/assignments \
    -H "Authorization: Bearer TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"assignmentType":"USER","assignedTo":"Test User","assignedUserEmail":"test@example.com"}'
  # Expected: 201 Created with assignment DTO
  ```

- [ ] **Get Assignment History Test**
  ```bash
  curl https://api.example.com/api/v1/assets/{id}/assignment-history \
    -H "Authorization: Bearer TOKEN"
  # Expected: 200 OK with paginated history
  ```

---

## Post-Deployment Verification

### Functional Testing

- [ ] **Assignment Creation**
  - [ ] Create user assignment successfully
  - [ ] Create location assignment successfully
  - [ ] Verify assignment record created in database
  - [ ] Verify asset fields updated correctly
  - [ ] Verify audit log entry created

- [ ] **Deallocation**
  - [ ] Deallocate assigned asset successfully
  - [ ] Verify assignment record closed (unassignedAt set)
  - [ ] Verify asset fields cleared
  - [ ] Verify audit log entry created

- [ ] **Assignment History**
  - [ ] View assignment history for asset
  - [ ] Verify chronological order (most recent first)
  - [ ] Verify pagination works correctly
  - [ ] Verify all assignment details displayed

- [ ] **Query Operations**
  - [ ] Query assets by user name
  - [ ] Query assets by location
  - [ ] Verify case-insensitive search works
  - [ ] Verify pagination works correctly

- [ ] **Statistics**
  - [ ] View assignment statistics
  - [ ] Verify counts are accurate
  - [ ] Verify top users/locations displayed
  - [ ] Verify available assets count correct

- [ ] **Export**
  - [ ] Export assignment data to CSV
  - [ ] Verify CSV format correct
  - [ ] Verify filters work correctly
  - [ ] Verify export limit enforced (10,000 records)

- [ ] **Bulk Operations**
  - [ ] Bulk deallocate multiple assets
  - [ ] Verify success/failure results accurate
  - [ ] Verify bulk limit enforced (50 assets)

### Authorization Testing

- [ ] **Administrator Role**
  - [ ] Can create assignments
  - [ ] Can deallocate assets
  - [ ] Can view assignment history
  - [ ] Can access statistics
  - [ ] Can export data
  - [ ] Can perform bulk operations

- [ ] **Asset Manager Role**
  - [ ] Can create assignments
  - [ ] Can deallocate assets
  - [ ] Can view assignment history
  - [ ] Can access statistics
  - [ ] Can export data
  - [ ] Can perform bulk operations

- [ ] **Viewer Role**
  - [ ] Cannot create assignments (403 Forbidden)
  - [ ] Cannot deallocate assets (403 Forbidden)
  - [ ] Can view assignment history
  - [ ] Cannot access statistics (403 Forbidden)
  - [ ] Cannot export data (403 Forbidden)
  - [ ] Cannot perform bulk operations (403 Forbidden)

### Error Handling Testing

- [ ] **Validation Errors**
  - [ ] Missing required fields returns 400 Bad Request
  - [ ] Invalid email format returns 400 Bad Request
  - [ ] Field length violations return 400 Bad Request
  - [ ] Error response includes all validation errors

- [ ] **Not Found Errors**
  - [ ] Non-existent asset returns 404 Not Found
  - [ ] Non-existent assignment returns 404 Not Found

- [ ] **Conflict Errors**
  - [ ] Assigning already assigned asset returns 409 Conflict
  - [ ] Deallocating unassigned asset returns 404 Not Found

- [ ] **Authorization Errors**
  - [ ] Missing token returns 401 Unauthorized
  - [ ] Invalid token returns 401 Unauthorized
  - [ ] Insufficient permissions returns 403 Forbidden

### Performance Testing

- [ ] **Assignment Creation Performance**
  - [ ] Single assignment completes in < 500ms
  - [ ] Verify database query performance
  - [ ] Check for N+1 query issues

- [ ] **Assignment History Performance**
  - [ ] History retrieval completes in < 1 second (1,000 records)
  - [ ] Verify index usage in query plan
  - [ ] Check pagination performance

- [ ] **Query Performance**
  - [ ] Query by user completes in < 2 seconds (10,000 assets)
  - [ ] Query by location completes in < 2 seconds (10,000 assets)
  - [ ] Verify index usage in query plan

- [ ] **Bulk Operation Performance**
  - [ ] Bulk deallocate (50 assets) completes in < 10 seconds
  - [ ] Verify transaction handling
  - [ ] Check for deadlocks or timeouts

### Database Verification

- [ ] **Data Integrity**
  - [ ] Foreign key constraints enforced
  - [ ] Check constraints enforced (AssignmentType enum)
  - [ ] Unique constraints enforced where applicable
  - [ ] Cascade deletes working correctly

- [ ] **Index Performance**
  - [ ] Verify indexes are being used in queries
  - [ ] Check index fragmentation
  - [ ] Verify query execution plans

- [ ] **Connection Pooling**
  - [ ] Verify connection pool settings (min: 5, max: 20)
  - [ ] Monitor active connections
  - [ ] Check for connection leaks

### Audit Logging Verification

- [ ] **Assignment Creation Logged**
  - [ ] Action type: CREATE
  - [ ] Resource type: ASSIGNMENT
  - [ ] Resource ID: assignment ID
  - [ ] User ID: user who created assignment
  - [ ] Metadata includes asset ID, assignment type, assigned to

- [ ] **Deallocation Logged**
  - [ ] Action type: DELETE
  - [ ] Resource type: ASSIGNMENT
  - [ ] Resource ID: assignment ID
  - [ ] User ID: user who deallocated
  - [ ] Metadata includes asset ID

- [ ] **Export Logged**
  - [ ] Action type: EXPORT
  - [ ] Resource type: ASSIGNMENT
  - [ ] User ID: user who exported
  - [ ] Metadata includes filter parameters

### Monitoring Setup

- [ ] **Application Metrics**
  - [ ] Assignment creation count metric
  - [ ] Deallocation count metric
  - [ ] Assignment operation duration metric
  - [ ] Error rate metric

- [ ] **Alerts Configured**
  - [ ] High error rate alert
  - [ ] Slow operation alert (> 2 seconds)
  - [ ] Failed audit logging alert
  - [ ] Database connection pool exhaustion alert

- [ ] **Logging**
  - [ ] All allocation operations logged
  - [ ] Errors logged with context
  - [ ] Slow operations logged
  - [ ] Log aggregation configured (ELK, Splunk, etc.)

---

## Rollback Plan

### If Deployment Fails

1. **Stop Application**
   ```bash
   systemctl stop it-asset-management
   ```

2. **Restore Database**
   ```sql
   -- Restore from backup
   RESTORE DATABASE ITAssetManagement 
   FROM DISK = 'C:\Backups\ITAssetManagement_PreDeployment.bak'
   WITH REPLACE;
   ```

3. **Deploy Previous Version**
   ```bash
   # Deploy previous JAR file
   cp /backups/it-asset-management-previous.jar /opt/app/
   ```

4. **Start Application**
   ```bash
   systemctl start it-asset-management
   ```

5. **Verify Rollback**
   - [ ] Application starts successfully
   - [ ] Health check passes
   - [ ] Basic functionality works
   - [ ] No errors in logs

### If Partial Failure

1. **Identify Issue**
   - Check application logs
   - Check database logs
   - Review error messages

2. **Apply Hotfix**
   - Fix specific issue
   - Test fix in staging
   - Deploy hotfix to production

3. **Verify Fix**
   - Run smoke tests
   - Verify issue resolved
   - Monitor for additional issues

---

## Production Monitoring

### First 24 Hours

- [ ] **Monitor Application Logs**
  - Check for errors every hour
  - Review warning messages
  - Verify no unexpected behavior

- [ ] **Monitor Database**
  - Check query performance
  - Monitor connection pool usage
  - Verify no deadlocks or timeouts

- [ ] **Monitor Metrics**
  - Track assignment creation rate
  - Track error rate
  - Track response times

- [ ] **User Feedback**
  - Monitor support tickets
  - Check for user-reported issues
  - Gather feedback on new features

### First Week

- [ ] **Performance Review**
  - Analyze slow queries
  - Review database indexes
  - Optimize if needed

- [ ] **Error Analysis**
  - Review all errors
  - Identify patterns
  - Fix recurring issues

- [ ] **Usage Analysis**
  - Track feature adoption
  - Identify most-used features
  - Identify unused features

---

## Sign-Off

### Deployment Team

- [ ] **Database Administrator**
  - Name: ___________________
  - Date: ___________________
  - Signature: ___________________

- [ ] **Backend Developer**
  - Name: ___________________
  - Date: ___________________
  - Signature: ___________________

- [ ] **QA Engineer**
  - Name: ___________________
  - Date: ___________________
  - Signature: ___________________

- [ ] **DevOps Engineer**
  - Name: ___________________
  - Date: ___________________
  - Signature: ___________________

### Approval

- [ ] **Technical Lead**
  - Name: ___________________
  - Date: ___________________
  - Signature: ___________________

- [ ] **Product Owner**
  - Name: ___________________
  - Date: ___________________
  - Signature: ___________________

---

## Notes

Use this section to document any issues, deviations from the plan, or additional steps taken during deployment:

```
[Add deployment notes here]
```

---

**Document Version**: 1.0.0  
**Last Updated**: January 15, 2024  
**Prepared By**: IT Asset Management Team
