# Module 2: Asset Management - Design Document

## Developer Assignment

**Developer**: Developer 2  
**Module**: Asset Management (Module 2)  
**Package**: `com.company.assetmanagement.module2`

---

## Module Overview

The Asset Management module is responsible for the complete lifecycle management of IT infrastructure assets. This module handles asset registration, updates, lifecycle tracking, search/filtering, validation, and import/export operations. It serves as the core domain module that other modules depend on for asset data.

### Module Responsibilities

- Asset CRUD operations (Create, Read, Update, Delete)
- Asset lifecycle status management (7 statuses)
- Asset search and filtering across large inventories
- Serial number uniqueness enforcement
- Asset data validation
- Import/Export functionality (CSV/JSON)
- Integration with Audit Service for logging
- Integration with User Management for authorization

---

## Architecture

### Component Structure

```
module2/
├── controller/
│   └── AssetController.java          # REST API endpoints
├── service/
│   ├── AssetService.java             # Service interface
│   ├── AssetServiceImpl.java         # Business logic implementation
│   └── AssetValidationService.java   # Validation logic
├── repository/
│   └── AssetRepository.java          # Data access layer
├── model/
│   ├── Asset.java                    # Asset entity
│   ├── AssetType.java                # Asset type enum
│   └── LifecycleStatus.java          # Lifecycle status enum
└── dto/
    ├── AssetDTO.java                 # Asset data transfer object
    ├── AssetRequest.java             # Create/Update request
    ├── AssetResponse.java            # API response wrapper
    └── AssetSearchQuery.java         # Search query parameters
```

### Dependencies

**Internal Dependencies:**
- `common.exception.*` - Shared exception handling
- `common.dto.*` - Shared DTOs (ErrorResponse, PageResponse)
- `common.util.*` - Utility classes (DateUtil, ValidationUtil, StringUtil)
- `security.*` - Authentication and authorization
- `audit.*` - Audit logging service

**External Dependencies:**
- Spring Data JPA - Database access
- Spring Security - Authorization checks
- MS SQL Server - Database
- Jackson - JSON serialization

---

## Data Model

### Asset Entity

```java
@Entity
@Table(name = "Assets", indexes = {
    @Index(name = "IX_Assets_SerialNumber", columnList = "serialNumber"),
    @Index(name = "IX_Assets_AssetType", columnList = "assetType"),
    @Index(name = "IX_Assets_Status", columnList = "status"),
    @Index(name = "IX_Assets_Location", columnList = "location"),
    @Index(name = "IX_Assets_AssignedUser", columnList = "assignedUser")
})
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AssetType assetType;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(nullable = false, unique = true, length = 100, updatable = false)
    private String serialNumber;
    
    @Column(nullable = false)
    private LocalDate acquisitionDate;
    
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private LifecycleStatus status;
    
    @Column(length = 255)
    private String location;
    
    @Column(length = 255)
    private String assignedUser;
    
    @Column(length = 255)
    private String assignedUserEmail;
    
    private LocalDateTime assignmentDate;
    
    private LocalDateTime locationUpdateDate;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String notes;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String customFields; // JSON string
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false, updatable = false)
    private UUID createdBy;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private UUID updatedBy;
    
    @Column(nullable = false)
    private boolean readOnly = false;
    
    // Getters, setters, equals, hashCode, toString
}
```

### AssetType Enum

```java
public enum AssetType {
    SERVER("server"),
    WORKSTATION("workstation"),
    NETWORK_DEVICE("network_device"),
    STORAGE_DEVICE("storage_device"),
    SOFTWARE_LICENSE("software_license"),
    PERIPHERAL("peripheral"),
    KEYBOARD("keyboard"),
    MOUSE("mouse"),
    LAPTOP("laptop"),
    MONITOR("monitor"),
    HEADSET("headset"),
    LAPTOP_CHARGER("laptop_charger"),
    HDMI_CABLE("hdmi_cable"),
    NETWORK_CABLE("network_cable"),
    ACCESS_CARD("access_card");
    
    private final String value;
    
    AssetType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
```

### LifecycleStatus Enum

```java
public enum LifecycleStatus {
    ORDERED("ordered"),
    RECEIVED("received"),
    DEPLOYED("deployed"),
    IN_USE("in_use"),
    MAINTENANCE("maintenance"),
    STORAGE("storage"),
    RETIRED("retired");
    
    private final String value;
    
    LifecycleStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    // Valid status transitions
    public boolean canTransitionTo(LifecycleStatus newStatus) {
        if (this == RETIRED) {
            return false; // No transitions from RETIRED
        }
        
        if (newStatus == MAINTENANCE) {
            return true; // Can always go to MAINTENANCE
        }
        
        switch (this) {
            case ORDERED:
                return newStatus == RECEIVED;
            case RECEIVED:
                return newStatus == DEPLOYED;
            case DEPLOYED:
                return newStatus == IN_USE || newStatus == STORAGE;
            case IN_USE:
                return newStatus == STORAGE || newStatus == RETIRED;
            case MAINTENANCE:
                return true; // Can return to any status
            case STORAGE:
                return newStatus == DEPLOYED || newStatus == RETIRED;
            default:
                return false;
        }
    }
}
```

---

## API Endpoints

### Asset Controller

```java
@RestController
@RequestMapping("/api/v1/assets")
@Validated
public class AssetController {
    
    private final AssetService assetService;
    
    // GET /api/v1/assets - List all assets with pagination and filtering
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER', 'VIEWER')")
    public ResponseEntity<Page<AssetDTO>> getAssets(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<AssetType> assetTypes,
            @RequestParam(required = false) List<LifecycleStatus> statuses,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate acquisitionDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate acquisitionDateTo,
            Pageable pageable);
    
    // GET /api/v1/assets/{id} - Get single asset by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER', 'VIEWER')")
    public ResponseEntity<AssetDTO> getAsset(@PathVariable UUID id);
    
    // POST /api/v1/assets - Create new asset
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    public ResponseEntity<AssetDTO> createAsset(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AssetRequest request);
    
    // PUT /api/v1/assets/{id} - Update entire asset
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    public ResponseEntity<AssetDTO> updateAsset(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AssetRequest request);
    
    // PATCH /api/v1/assets/{id} - Partial update
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    public ResponseEntity<AssetDTO> patchAsset(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> updates);
    
    // DELETE /api/v1/assets/{id} - Delete asset
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteAsset(@PathVariable UUID id);
    
    // PATCH /api/v1/assets/{id}/status - Update lifecycle status
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    public ResponseEntity<AssetDTO> updateStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody StatusUpdateRequest request);
    
    // GET /api/v1/assets/search - Advanced search
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER', 'VIEWER')")
    public ResponseEntity<Page<AssetDTO>> searchAssets(
            @Valid @ModelAttribute AssetSearchQuery query,
            Pageable pageable);
    
    // GET /api/v1/assets/export - Export assets
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    public ResponseEntity<byte[]> exportAssets(
            @RequestParam(defaultValue = "CSV") ExportFormat format,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<AssetType> assetTypes);
    
    // POST /api/v1/assets/import - Import assets
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    public ResponseEntity<ImportResult> importAssets(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam ImportFormat format,
            @RequestParam("file") MultipartFile file);
}
```

---

## Service Layer

### AssetService Interface

```java
public interface AssetService {
    
    /**
     * Create a new asset
     * @param userId User creating the asset
     * @param request Asset creation request
     * @return Created asset DTO
     * @throws DuplicateSerialNumberException if serial number already exists
     * @throws ValidationException if validation fails
     * @throws InsufficientPermissionsException if user lacks permission
     */
    AssetDTO createAsset(String userId, AssetRequest request);
    
    /**
     * Update an existing asset
     * @param userId User updating the asset
     * @param assetId Asset ID
     * @param request Asset update request
     * @return Updated asset DTO
     * @throws ResourceNotFoundException if asset not found
     * @throws ValidationException if validation fails
     * @throws InsufficientPermissionsException if user lacks permission
     */
    AssetDTO updateAsset(String userId, UUID assetId, AssetRequest request);
    
    /**
     * Get asset by ID
     * @param assetId Asset ID
     * @return Asset DTO
     * @throws ResourceNotFoundException if asset not found
     */
    Optional<AssetDTO> getAsset(UUID assetId);
    
    /**
     * Search assets with pagination
     * @param query Search query parameters
     * @param pageable Pagination parameters
     * @return Page of asset DTOs
     */
    Page<AssetDTO> searchAssets(AssetSearchQuery query, Pageable pageable);
    
    /**
     * Update asset lifecycle status
     * @param userId User updating the status
     * @param assetId Asset ID
     * @param newStatus New lifecycle status
     * @return Updated asset DTO
     * @throws ResourceNotFoundException if asset not found
     * @throws InvalidStatusTransitionException if transition is invalid
     * @throws InsufficientPermissionsException if user lacks permission
     */
    AssetDTO updateStatus(String userId, UUID assetId, LifecycleStatus newStatus);
    
    /**
     * Delete an asset
     * @param userId User deleting the asset
     * @param assetId Asset ID
     * @throws ResourceNotFoundException if asset not found
     * @throws InsufficientPermissionsException if user lacks permission
     */
    void deleteAsset(String userId, UUID assetId);
    
    /**
     * Export assets to specified format
     * @param format Export format (CSV/JSON)
     * @param query Optional search query to filter exports
     * @return Export result with file data
     */
    ExportResult exportAssets(ExportFormat format, AssetSearchQuery query);
    
    /**
     * Import assets from file
     * @param userId User importing assets
     * @param format Import format (CSV/JSON)
     * @param data File data
     * @return Import result with success/failure counts
     */
    ImportResult importAssets(String userId, ImportFormat format, byte[] data);
}
```

### AssetServiceImpl Implementation Pattern

```java
@Service
@Transactional
public class AssetServiceImpl implements AssetService {
    
    private final AssetRepository assetRepository;
    private final AuditService auditService;
    private final AuthorizationService authorizationService;
    private final AssetValidationService validationService;
    
    @Override
    public AssetDTO createAsset(String userId, AssetRequest request) {
        // 1. Authorization check
        if (!authorizationService.hasPermission(userId, Action.CREATE_ASSET)) {
            throw new InsufficientPermissionsException();
        }
        
        // 2. Validation
        validationService.validateAssetRequest(request);
        
        // 3. Business rule: Check serial number uniqueness
        if (assetRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new DuplicateSerialNumberException(request.getSerialNumber());
        }
        
        // 4. Create entity
        Asset asset = mapToEntity(request);
        asset.setCreatedBy(UUID.fromString(userId));
        asset.setUpdatedBy(UUID.fromString(userId));
        
        // 5. Persist
        Asset savedAsset = assetRepository.save(asset);
        
        // 6. Audit logging
        auditService.logEvent(AuditEvent.builder()
            .userId(userId)
            .actionType(ActionType.CREATE)
            .resourceType(ResourceType.ASSET)
            .resourceId(savedAsset.getId().toString())
            .build());
        
        // 7. Return DTO
        return mapToDTO(savedAsset);
    }
    
    // Additional methods follow similar pattern...
}
```

---

## Validation Rules

### AssetValidationService

```java
@Service
public class AssetValidationService {
    
    public void validateAssetRequest(AssetRequest request) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Required field validation
        if (request.getAssetType() == null) {
            errors.add(new ValidationError("assetType", "Asset type is required"));
        }
        
        if (StringUtils.isBlank(request.getName())) {
            errors.add(new ValidationError("name", "Name is required"));
        } else if (request.getName().length() > 255) {
            errors.add(new ValidationError("name", "Name must not exceed 255 characters"));
        }
        
        if (StringUtils.isBlank(request.getSerialNumber())) {
            errors.add(new ValidationError("serialNumber", "Serial number is required"));
        } else if (request.getSerialNumber().length() < 5 || request.getSerialNumber().length() > 100) {
            errors.add(new ValidationError("serialNumber", "Serial number must be between 5 and 100 characters"));
        }
        
        if (request.getAcquisitionDate() == null) {
            errors.add(new ValidationError("acquisitionDate", "Acquisition date is required"));
        } else if (request.getAcquisitionDate().isAfter(LocalDate.now())) {
            errors.add(new ValidationError("acquisitionDate", "Acquisition date cannot be in the future"));
        }
        
        if (request.getStatus() == null) {
            errors.add(new ValidationError("status", "Initial status is required"));
        }
        
        // Email validation
        if (StringUtils.isNotBlank(request.getAssignedUserEmail())) {
            if (!ValidationUtil.isValidEmail(request.getAssignedUserEmail())) {
                errors.add(new ValidationError("assignedUserEmail", "Invalid email format"));
            }
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
    
    public void validateStatusTransition(LifecycleStatus currentStatus, LifecycleStatus newStatus) {
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(currentStatus, newStatus);
        }
    }
}
```

---

## Database Queries

### AssetRepository

```java
@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID> {
    
    // Check serial number uniqueness
    boolean existsBySerialNumber(String serialNumber);
    
    // Find by serial number
    Optional<Asset> findBySerialNumber(String serialNumber);
    
    // Search with filters
    @Query("SELECT a FROM Asset a WHERE " +
           "(:text IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
           "OR LOWER(a.serialNumber) LIKE LOWER(CONCAT('%', :text, '%')) " +
           "OR LOWER(a.location) LIKE LOWER(CONCAT('%', :text, '%'))) " +
           "AND (:assetTypes IS NULL OR a.assetType IN :assetTypes) " +
           "AND (:statuses IS NULL OR a.status IN :statuses) " +
           "AND (:location IS NULL OR LOWER(a.location) = LOWER(:location)) " +
           "AND (:dateFrom IS NULL OR a.acquisitionDate >= :dateFrom) " +
           "AND (:dateTo IS NULL OR a.acquisitionDate <= :dateTo)")
    Page<Asset> searchAssets(
        @Param("text") String text,
        @Param("assetTypes") List<AssetType> assetTypes,
        @Param("statuses") List<LifecycleStatus> statuses,
        @Param("location") String location,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
        Pageable pageable);
    
    // Find by assigned user
    List<Asset> findByAssignedUser(String assignedUser);
    
    // Find by location
    List<Asset> findByLocation(String location);
    
    // Count by asset type
    @Query("SELECT a.assetType, COUNT(a) FROM Asset a GROUP BY a.assetType")
    List<Object[]> countByAssetType();
    
    // Count by status
    @Query("SELECT a.status, COUNT(a) FROM Asset a GROUP BY a.status")
    List<Object[]> countByStatus();
}
```

---

## Integration Points

### With Audit Service

```java
// Log asset creation
auditService.logEvent(AuditEvent.builder()
    .userId(userId)
    .actionType(ActionType.CREATE)
    .resourceType(ResourceType.ASSET)
    .resourceId(asset.getId().toString())
    .build());

// Log asset update with field changes
auditService.logEvent(AuditEvent.builder()
    .userId(userId)
    .actionType(ActionType.UPDATE)
    .resourceType(ResourceType.ASSET)
    .resourceId(asset.getId().toString())
    .changes(List.of(
        new FieldChange("location", oldLocation, newLocation),
        new FieldChange("status", oldStatus, newStatus)
    ))
    .build());
```

### With Authorization Service

```java
// Check permission before operation
if (!authorizationService.hasPermission(userId, Action.CREATE_ASSET)) {
    throw new InsufficientPermissionsException();
}
```

### With Module 3 (Allocation Management)

Module 3 depends on Module 2 for:
- Asset availability checks
- Asset assignment updates
- Assignment history tracking

```java
// Module 3 calls Module 2 to update asset assignment
assetService.updateAsset(userId, assetId, AssetRequest.builder()
    .assignedUser(userName)
    .assignedUserEmail(userEmail)
    .assignmentDate(LocalDateTime.now())
    .build());
```

---

## Performance Considerations

### Database Indexing

```sql
-- Critical indexes for performance
CREATE INDEX IX_Assets_SerialNumber ON Assets(SerialNumber);
CREATE INDEX IX_Assets_AssetType ON Assets(AssetType);
CREATE INDEX IX_Assets_Status ON Assets(Status);
CREATE INDEX IX_Assets_Location ON Assets(Location);
CREATE INDEX IX_Assets_AssignedUser ON Assets(AssignedUser);
CREATE INDEX IX_Assets_AcquisitionDate ON Assets(AcquisitionDate);
```

### Query Optimization

- Use pagination for all list operations (default page size: 20, max: 100)
- Implement database-level filtering to reduce data transfer
- Use projection queries when full entity data is not needed
- Cache frequently accessed configuration data

### Performance Requirements

- Search operations: < 2 seconds for 100,000 assets
- Report generation: < 10 seconds for 100,000 assets
- Export operations: < 30 seconds for 100,000 assets
- Import operations: Support up to 10,000 records per batch

---

## Error Handling

### Custom Exceptions

```java
// DuplicateSerialNumberException
public class DuplicateSerialNumberException extends RuntimeException {
    private final String serialNumber;
    
    public DuplicateSerialNumberException(String serialNumber) {
        super("Asset with serial number already exists: " + serialNumber);
        this.serialNumber = serialNumber;
    }
}

// InvalidStatusTransitionException
public class InvalidStatusTransitionException extends RuntimeException {
    private final LifecycleStatus fromStatus;
    private final LifecycleStatus toStatus;
    
    public InvalidStatusTransitionException(LifecycleStatus from, LifecycleStatus to) {
        super(String.format("Invalid status transition from %s to %s", from, to));
        this.fromStatus = from;
        this.toStatus = to;
    }
}
```

### Error Response Format

```json
{
  "error": {
    "type": "DUPLICATE_SERIAL_NUMBER",
    "message": "Asset with serial number already exists: SRV-001",
    "details": {
      "serialNumber": "SRV-001"
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-123456"
  }
}
```

---

## Testing Strategy

### Unit Tests

- Test all service methods with mocked dependencies
- Test validation logic with valid and invalid inputs
- Test status transition logic
- Test mapping between entities and DTOs
- Target: 80% code coverage minimum

### Integration Tests

- Test repository queries against actual database
- Test complete API endpoints with Spring Boot Test
- Test transaction rollback on errors
- Test concurrent access scenarios

### Property-Based Tests

Module 2 is responsible for testing properties 7-12, 16-17, 28-29, 32-33:

- Property 7: Valid asset creation generates unique identifier
- Property 8: Asset data persistence and retrieval
- Property 9: Serial number uniqueness enforcement
- Property 10: Asset update preserves immutable fields
- Property 11: Lifecycle status transition validation
- Property 12: Retired assets become read-only
- Property 16: Search returns matching assets
- Property 17: Search performance under load
- Property 28: Import validation catches errors
- Property 29: Export completeness
- Property 32: Concurrent updates maintain consistency
- Property 33: Database constraints enforced

---

## Frontend Integration

### UI Screens (Based on Figma Design)

The Module 2 frontend consists of four main screens:

1. **Asset Inventory Screen** - Main list view with search, filters, and data table
2. **Asset Detail View** - Comprehensive view of a single asset with 3-column bento grid layout
3. **Add New Asset Form** - Multi-section form for creating new assets
4. **Edit Asset Form** - Multi-section form for updating existing assets

### Screen 1: Asset Inventory

**Layout Components:**
- Top Navigation Bar with global search
- Side Navigation Bar with application menu
- Hero Header Section with page title and action buttons ("Export" and "Add New Asset")
- Advanced Filter Bar with dropdowns for Asset Type, Status, and Location
- Asset Table with sortable columns and pagination
- Dashboard Snapshot Widget showing Quick Stats

**Table Columns:**
- Name (with icon)
- Type
- Serial Number
- Status (badge)
- Acquisition Date
- Location
- Assigned User
- Actions (View, Edit, Delete buttons)

**Filters:**
- Asset Type dropdown (15 types)
- Status dropdown (7 lifecycle statuses)
- Location dropdown
- Text search across inventory

**Actions:**
- Export button (CSV/JSON)
- Add New Asset button (primary action)
- Row-level actions: View, Edit, Delete

### Screen 2: Asset Detail View

**Layout: 3-Column Bento Grid**

**Left Column (40%):**
- General Details Section with icon
  - Asset Type
  - Serial Number
  - Acquisition Date
  - Status (with colored badge)
  - Location
  - Notes (textarea)
- Asset Image/Visual Identity Card

**Middle Column (30%):**
- Assignment Card
  - User avatar and name
  - Email
  - Phone
  - Department
  - "Reassign Asset" button
- Lifecycle History Timeline
  - Chronological events with dates
  - Status changes
  - Assignment changes

**Right Column (30%):**
- Quick Actions Section
  - Edit Asset
  - Change Status
  - Generate Report
- Assignment History
  - Previous assignments with dates
  - Duration tracking
  - "View Full History" button

**Header:**
- Breadcrumb navigation
- Asset icon and name
- Status badge
- Primary action: "Edit Asset" button

### Screen 3 & 4: Add/Edit Asset Forms

**Layout: Two-Column Form with Side Panel**

**Main Form Area (Left):**

**Section 1: General Details**
- Icon header
- Asset Type (dropdown)
- Manufacturer (text input)
- Model Name (text input)
- Serial Number (text input with lock icon for edit mode)

**Section 2: Lifecycle & Warranty**
- Icon header
- Purchase Date (date picker)
- Warranty Expiry (date picker)
- Cost Center (text input)
- Purchase Value USD (number input)

**Section 3: Asset Tracking**
- Icon header
- Current Status (dropdown)
- Assigned User (text input with @ prefix)
- Office Location (textarea)
- IP Address (text input)

**Actions Container:**
- Cancel button (secondary)
- Save Changes button (primary with checkmark icon)

**Side Panel (Right):**

**Visual Identity Card:**
- Asset image preview
- Asset name
- Brief description
- "Change Image" link

**Recent Activity Bento:**
- Timeline of recent changes
- Icons for different event types
- Timestamps
- "View All Activity" button

**Technical Specs Mini-Grid:**
- Key-value pairs
- CPU, RAM, Storage specifications

### Angular Service

```typescript
@Injectable({
  providedIn: 'root'
})
export class AssetService {
  private readonly apiUrl = '/api/v1/assets';
  
  constructor(private http: HttpClient) {}
  
  getAssets(query?: AssetSearchQuery): Observable<Page<Asset>> {
    const params = this.buildQueryParams(query);
    return this.http.get<Page<Asset>>(this.apiUrl, { params });
  }
  
  getAsset(id: string): Observable<Asset> {
    return this.http.get<Asset>(`${this.apiUrl}/${id}`);
  }
  
  createAsset(asset: AssetRequest): Observable<Asset> {
    return this.http.post<Asset>(this.apiUrl, asset);
  }
  
  updateAsset(id: string, asset: AssetRequest): Observable<Asset> {
    return this.http.put<Asset>(`${this.apiUrl}/${id}`, asset);
  }
  
  updateStatus(id: string, status: LifecycleStatus): Observable<Asset> {
    return this.http.patch<Asset>(`${this.apiUrl}/${id}/status`, { status });
  }
  
  deleteAsset(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
  
  exportAssets(format: ExportFormat, query?: AssetSearchQuery): Observable<Blob> {
    const params = this.buildQueryParams(query);
    params.set('format', format);
    return this.http.get(`${this.apiUrl}/export`, {
      params,
      responseType: 'blob'
    });
  }
  
  getAssetHistory(id: string): Observable<AssetHistoryEvent[]> {
    return this.http.get<AssetHistoryEvent[]>(`${this.apiUrl}/${id}/history`);
  }
  
  getAssignmentHistory(id: string): Observable<AssignmentHistoryEntry[]> {
    return this.http.get<AssignmentHistoryEntry[]>(`${this.apiUrl}/${id}/assignments`);
  }
}
```

### Component Structure

```
features/module2-assets/
├── components/
│   ├── asset-inventory/
│   │   ├── asset-inventory.component.ts
│   │   ├── asset-inventory.component.html
│   │   ├── asset-inventory.component.scss
│   │   └── asset-table/
│   │       ├── asset-table.component.ts
│   │       └── asset-table.component.html
│   ├── asset-detail/
│   │   ├── asset-detail.component.ts
│   │   ├── asset-detail.component.html
│   │   ├── asset-detail.component.scss
│   │   ├── assignment-card/
│   │   ├── lifecycle-timeline/
│   │   └── quick-actions/
│   ├── asset-form/
│   │   ├── asset-form.component.ts
│   │   ├── asset-form.component.html
│   │   ├── asset-form.component.scss
│   │   ├── general-details-section/
│   │   ├── lifecycle-warranty-section/
│   │   └── asset-tracking-section/
│   └── shared/
│       ├── asset-status-badge/
│       ├── asset-icon/
│       └── asset-filters/
├── services/
│   └── asset.service.ts
├── models/
│   ├── asset.model.ts
│   ├── asset-history.model.ts
│   └── assignment-history.model.ts
└── assets.module.ts
```

---

## Security Considerations

### Authorization Checks

- All endpoints require authentication
- CREATE/UPDATE/DELETE operations require ADMINISTRATOR or ASSET_MANAGER role
- READ operations allow VIEWER role
- Validate user permissions at service layer (defense in depth)

### Input Validation

- Validate all inputs at API boundary
- Sanitize user inputs to prevent XSS
- Use parameterized queries to prevent SQL injection
- Validate file uploads (size, type, content)

### Audit Logging

- Log all state-changing operations
- Include user ID, timestamp, and changed fields
- Never log sensitive data (passwords, tokens)
- Make audit logs immutable

---

## Deployment Considerations

### Database Migration

```sql
-- Flyway migration: V2__create_assets_table.sql
CREATE TABLE Assets (
  Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
  AssetType NVARCHAR(50) NOT NULL,
  Name NVARCHAR(255) NOT NULL,
  SerialNumber NVARCHAR(100) NOT NULL UNIQUE,
  -- ... (full schema from design document)
);

CREATE INDEX IX_Assets_SerialNumber ON Assets(SerialNumber);
CREATE INDEX IX_Assets_AssetType ON Assets(AssetType);
-- ... (all indexes)
```

### Configuration

```properties
# application.properties
# Asset module specific configuration
asset.import.max-records=10000
asset.export.timeout-seconds=30
asset.search.max-results=100000
asset.validation.serial-number-pattern=^[A-Z0-9-]{5,100}$
```

---

## Success Criteria

### Module Completion Checklist

- [ ] All API endpoints implemented and documented
- [ ] All service methods implemented with business logic
- [ ] Repository queries optimized with indexes
- [ ] Validation rules implemented and tested
- [ ] Integration with Audit Service complete
- [ ] Integration with Authorization Service complete
- [ ] Unit test coverage > 80%
- [ ] All 12 property-based tests passing
- [ ] Integration tests passing
- [ ] API documentation (Swagger) complete
- [ ] Frontend service integration tested
- [ ] Performance requirements met (search < 2s, reports < 10s)
- [ ] Code reviewed and approved
- [ ] No critical or high-severity bugs

---

## Reference Documents

- [Main Design Document](./design.md)
- [Requirements Document](./requirements.md)
- [Team Structure Document](./team-structure-and-tasks.md)
- [Coding Standards](../../steering/it-asset-management-coding-standards.md)
- [Testing Guide](../../steering/it-asset-management-testing-guide.md)
- [API Design Guide](../../steering/it-asset-management-api-design.md)
