# Allocation Management User Guide

## Table of Contents

1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
3. [Assigning Assets](#assigning-assets)
4. [Deallocating Assets](#deallocating-assets)
5. [Viewing Assignment History](#viewing-assignment-history)
6. [Querying Assets](#querying-assets)
7. [Assignment Statistics](#assignment-statistics)
8. [Exporting Data](#exporting-data)
9. [Bulk Operations](#bulk-operations)
10. [Troubleshooting](#troubleshooting)

---

## Introduction

The Allocation Management module allows you to track which assets are assigned to users or located in specific places. This helps maintain accountability and enables efficient asset tracking across your organization.

### Key Features

- **Assign assets to users**: Track who is responsible for each asset
- **Assign assets to locations**: Track where assets are physically located
- **View assignment history**: See complete history of all assignments
- **Query assignments**: Find all assets assigned to a specific user or location
- **Generate statistics**: Get insights into asset utilization
- **Export data**: Download assignment data for reporting
- **Bulk operations**: Deallocate multiple assets at once

### User Roles

- **Administrator**: Full access to all allocation features
- **Asset Manager**: Can create, update, and view assignments
- **Viewer**: Can only view assignment data (read-only access)

---

## Getting Started

### Prerequisites

1. You must have an active user account
2. You must have appropriate role permissions (Administrator or Asset Manager for write operations)
3. Assets must be in an assignable status (IN_USE, DEPLOYED, or STORAGE)

### Accessing the Allocation Module

1. Log in to the IT Asset Management system
2. Navigate to **Assets** from the main menu
3. Select an asset to view its details
4. Click the **Assign** button to create a new assignment

---

## Assigning Assets

### Assigning an Asset to a User

**When to use**: When you want to track which employee is responsible for an asset (e.g., laptop, phone, monitor).

**Steps**:

1. Navigate to the asset detail page
2. Click the **Assign to User** button
3. Fill in the assignment form:
   - **User Name**: Enter the full name of the user (e.g., "John Doe")
   - **User Email**: Enter the user's email address (e.g., "john.doe@company.com")
4. Click **Submit**

**Example**:
```
Asset: Dell Laptop XPS 15
Serial Number: LAPTOP-001
Assign to: John Doe
Email: john.doe@company.com
```

**Result**: The asset is now assigned to John Doe, and this assignment is recorded in the system with a timestamp.

### Assigning an Asset to a Location

**When to use**: When you want to track where an asset is physically located (e.g., server in data center, equipment in storage).

**Steps**:

1. Navigate to the asset detail page
2. Click the **Assign to Location** button
3. Fill in the assignment form:
   - **Location Name**: Enter the location (e.g., "Data Center A", "Storage Room B")
4. Click **Submit**

**Example**:
```
Asset: HP Server ProLiant
Serial Number: SERVER-001
Assign to: Data Center A - Rack 5
```

**Result**: The asset is now assigned to the specified location, and this assignment is recorded in the system.

### Assignment Rules

- An asset can only have **one active assignment** at a time
- You cannot assign an asset that is already assigned (you must deallocate it first, or use reassignment)
- Only assets with status **IN_USE**, **DEPLOYED**, or **STORAGE** can be assigned
- Assets with status **ORDERED**, **RECEIVED**, **MAINTENANCE**, or **RETIRED** cannot be assigned

### Common Assignment Scenarios

#### Scenario 1: New Employee Onboarding
```
1. Employee "Jane Smith" joins the company
2. Find an available laptop (status: IN_USE, not assigned)
3. Assign laptop to Jane Smith with her email
4. Jane is now responsible for the laptop
```

#### Scenario 2: Equipment Relocation
```
1. Server needs to be moved from "Data Center A" to "Data Center B"
2. Deallocate server from "Data Center A"
3. Assign server to "Data Center B"
4. Server location is now updated
```

#### Scenario 3: Asset Return
```
1. Employee returns laptop when leaving company
2. Deallocate laptop from employee
3. Laptop is now available for reassignment
```

---

## Deallocating Assets

**When to use**: When you want to remove an asset's current assignment and make it available for reassignment.

### Steps

1. Navigate to the asset detail page
2. Verify the asset has an active assignment
3. Click the **Deallocate** button
4. Confirm the deallocation in the dialog
5. Click **Confirm**

**Result**: 
- The current assignment is closed (unassigned date is set)
- All asset assignment fields are cleared
- The asset is now available for reassignment

### Deallocation Rules

- You can only deallocate assets that have an active assignment
- Deallocation is permanent and cannot be undone (but you can create a new assignment)
- The assignment history is preserved for audit purposes

---

## Viewing Assignment History

**When to use**: When you need to see the complete history of all assignments for an asset.

### Steps

1. Navigate to the asset detail page
2. Click the **Assignment History** tab
3. View the list of all assignments (past and present)

### What You'll See

The assignment history shows:
- **Assignment Type**: USER or LOCATION
- **Assigned To**: User name or location name
- **Assigned By**: Who created the assignment
- **Assigned At**: When the assignment was created
- **Unassigned At**: When the assignment was closed (null for active assignments)
- **Status**: Active or Historical

### Sorting and Filtering

- **Default Sort**: Most recent assignments first
- **Pagination**: 20 records per page (configurable)
- **Search**: Filter by user name or location

### Example History View

```
Assignment History for: Dell Laptop XPS 15 (LAPTOP-001)

1. USER: John Doe
   Assigned: 2024-01-15 10:30 AM by admin
   Unassigned: (Active)
   
2. USER: Jane Smith
   Assigned: 2024-01-01 09:00 AM by admin
   Unassigned: 2024-01-15 10:30 AM
   
3. LOCATION: Storage Room A
   Assigned: 2023-12-15 02:00 PM by admin
   Unassigned: 2024-01-01 09:00 AM
```

---

## Querying Assets

### Query Assets by User

**When to use**: When you need to see all assets assigned to a specific user.

**Steps**:

1. Navigate to **Assignments** > **By User**
2. Enter the user name in the search box
3. Click **Search**
4. View the list of assets assigned to that user

**Use Cases**:
- Employee offboarding: See all assets that need to be returned
- Asset audit: Verify what equipment an employee has
- Inventory check: Confirm asset assignments

### Query Assets by Location

**When to use**: When you need to see all assets in a specific location.

**Steps**:

1. Navigate to **Assignments** > **By Location**
2. Enter the location name in the search box
3. Click **Search**
4. View the list of assets at that location

**Use Cases**:
- Location audit: Verify all equipment in a data center or office
- Relocation planning: See what needs to be moved
- Inventory management: Track assets by physical location

---

## Assignment Statistics

**When to use**: When you need insights into asset utilization and allocation patterns.

### Accessing Statistics

1. Navigate to **Assignments** > **Statistics**
2. View the dashboard with comprehensive metrics

### Available Metrics

1. **Total Assigned Assets**: Count of all currently assigned assets
2. **User vs Location Assignments**: Breakdown of assignment types
3. **Available Assets by Status**: Count of unassigned assets by status
4. **Top 10 Users by Asset Count**: Users with the most assigned assets
5. **Top 10 Locations by Asset Count**: Locations with the most assets

### Example Statistics View

```
Assignment Statistics

Total Assigned Assets: 150
├─ User Assignments: 120 (80%)
└─ Location Assignments: 30 (20%)

Available Assets:
├─ IN_USE: 50
├─ DEPLOYED: 30
└─ STORAGE: 20

Top Users:
1. John Doe - 15 assets
2. Jane Smith - 12 assets
3. Bob Johnson - 10 assets

Top Locations:
1. Data Center A - 25 assets
2. Office Building B - 18 assets
3. Storage Room C - 12 assets
```

---

## Exporting Data

**When to use**: When you need to download assignment data for reporting, analysis, or compliance.

### Steps

1. Navigate to **Assignments** > **Export**
2. (Optional) Apply filters:
   - **Assignment Type**: USER or LOCATION
   - **Date Range**: From and To dates
   - **Assigned By**: Specific user
3. Click **Export to CSV**
4. Save the downloaded file

### Export Format

The exported CSV file contains:
- Asset ID
- Asset Name
- Serial Number
- Asset Type
- Assignment Type
- Assigned To
- Assigned By
- Assigned At

### Export Limits

- Maximum 10,000 records per export
- If your export exceeds this limit, apply filters to reduce the size

### Example Export

```csv
Asset ID,Asset Name,Serial Number,Asset Type,Assignment Type,Assigned To,Assigned By,Assigned At
123e4567-e89b-12d3-a456-426614174000,Dell Laptop XPS 15,LAPTOP-001,WORKSTATION,USER,John Doe,admin,2024-01-15T10:30:00Z
234e5678-e89b-12d3-a456-426614174001,HP Server ProLiant,SERVER-001,SERVER,LOCATION,Data Center A,admin,2024-01-15T11:00:00Z
```

---

## Bulk Operations

### Bulk Deallocation

**When to use**: When you need to deallocate multiple assets at once (e.g., employee offboarding, location closure).

**Steps**:

1. Navigate to **Assignments** > **Bulk Deallocate**
2. Select assets from the list (or upload a CSV with asset IDs)
3. Review the selected assets
4. Click **Deallocate Selected**
5. Review the results

### Bulk Operation Limits

- Maximum 50 assets per bulk operation
- Each deallocation is processed independently
- Some may succeed while others fail

### Results

The bulk operation result shows:
- **Total Requested**: Number of assets in the request
- **Success Count**: Number of successful deallocations
- **Failure Count**: Number of failed deallocations
- **Successful Deallocations**: List of successfully deallocated assets
- **Failed Deallocations**: List of failed assets with error messages

### Example Bulk Result

```
Bulk Deallocation Results

Total Requested: 3
Success: 2
Failed: 1

Successful:
✓ Dell Laptop XPS 15 (LAPTOP-001)
✓ HP Monitor 24" (MONITOR-001)

Failed:
✗ HP Server ProLiant (SERVER-001)
  Error: Asset is not currently assigned
```

---

## Troubleshooting

### Common Issues and Solutions

#### Issue: "Asset already assigned" error

**Cause**: The asset already has an active assignment.

**Solution**: 
1. Deallocate the asset first
2. Then create a new assignment
3. Or use the reassignment feature to change the assignment directly

#### Issue: "Asset not in assignable status" error

**Cause**: The asset status does not allow assignment.

**Solution**: 
1. Check the asset status
2. Only assets with status IN_USE, DEPLOYED, or STORAGE can be assigned
3. Update the asset status if needed

#### Issue: "Asset not currently assigned" error when deallocating

**Cause**: The asset does not have an active assignment.

**Solution**: 
1. Check the assignment history to verify
2. The asset may have already been deallocated

#### Issue: Cannot see assignment buttons

**Cause**: Insufficient permissions.

**Solution**: 
1. Verify you have Administrator or Asset Manager role
2. Contact your system administrator to request appropriate permissions

#### Issue: Export is too large

**Cause**: Export exceeds 10,000 records.

**Solution**: 
1. Apply filters to reduce the export size
2. Filter by date range, assignment type, or assigned by user
3. Export data in multiple batches

---

## Best Practices

1. **Always verify asset status** before attempting to assign
2. **Use descriptive location names** for easy identification
3. **Keep user emails up to date** for accurate records
4. **Review assignment history** before making changes
5. **Use bulk operations** for efficiency when dealing with multiple assets
6. **Export data regularly** for backup and reporting purposes
7. **Document special assignments** in asset notes
8. **Conduct regular audits** to verify assignments are accurate

---

## Keyboard Shortcuts

- **Ctrl + A**: Assign asset (on asset detail page)
- **Ctrl + D**: Deallocate asset (on asset detail page)
- **Ctrl + H**: View assignment history (on asset detail page)
- **Ctrl + E**: Export assignments (on assignments page)

---

## Support

If you encounter issues or have questions:

1. **Check this user guide** for common solutions
2. **Contact IT Support**: support@company.com
3. **Submit a ticket**: https://support.company.com
4. **Call Help Desk**: +1-555-0100

---

## Glossary

- **Assignment**: The act of associating an asset with a user or location
- **Deallocation**: Removing an asset's current assignment
- **Assignment History**: Complete record of all assignments for an asset
- **Active Assignment**: Current assignment (unassigned date is null)
- **Historical Assignment**: Past assignment (unassigned date is set)
- **Bulk Operation**: Processing multiple assets in a single operation
- **Assignment Type**: Either USER (assigned to a person) or LOCATION (assigned to a place)

---

## Appendix: Assignment Workflow Diagrams

### User Assignment Workflow

```
┌─────────────────┐
│ Select Asset    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Check Status    │
│ (IN_USE, etc.)  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Check if        │
│ Already Assigned│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Enter User Info │
│ (Name & Email)  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Submit          │
│ Assignment      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Assignment      │
│ Created         │
└─────────────────┘
```

### Deallocation Workflow

```
┌─────────────────┐
│ Select Asset    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Verify Active   │
│ Assignment      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Click           │
│ Deallocate      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Confirm Action  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Assignment      │
│ Closed          │
└─────────────────┘
```

---

**Document Version**: 1.0.0  
**Last Updated**: January 15, 2024  
**Author**: IT Asset Management Team
