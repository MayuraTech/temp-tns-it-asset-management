# Design Document: Allocation Management

## Overview

This design document outlines the technical architecture and implementation approach for Module 3: Allocation Management of the IT Infrastructure Asset Management System. The module handles asset assignment to users and locations, tracks assignment history, and maintains a complete audit trail of all allocation operations.

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Frontend (Angular)                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Allocation   │  │ Assignment   │  │ Deallocation │     │
│  │ Form         │  │ History      │  │ Form         │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│         │                  │                  │             │
│         └──────────────────┴──────────────────┘             │
│                            │                                │
│                   ┌────────▼────────┐                       │
│                   │ Allocation      │                       │
│                   │ Service         │                       │
│                   └────────┬────────┘                       │
└────────────────────────────┼──────────────────────────────┘
                             │ HTTP/REST
┌────────────────────────────┼──────────────────────────────┐
│                     Backend (Spring Boot)                   │
│                   ┌────────▼────────┐                       │
│                   │ Allocation      │                       │
│                   │ Controller      │                       │
│                   └────────┬────────┘                       │
│                            │                                │
│                   ┌────────▼────────┐                       │
│                   │ Allocation      │                       │
│                   │ Service         │                       │
│                   └────────┬────────┘                       │
│                            │                                │
│         ┌──────────────────┼──────────────────┐            │
│         │                  │                  │            │
│  ┌──────▼──────┐  ┌────────▼────────┐  ┌─────▼─────┐     │
│  │ Assignment  │  │ Asset           │  │ Audit     │     │
│  │ History     │  │ Repository      │  │ Service   │     │
│  │ Repository  │  │ (Module 2)      │  │ (Common)  │     │
│  └─────────────┘  └─────────────────┘  └───────────┘     │
└─────────────────────────────────────────────────────────────┘
                             │
┌────────────────────────────┼──────────────────────────────┐
│                    Database (MS SQL Server)                 │
│  ┌──────────────────┐  ┌──────────────────┐               │
│  │ AssignmentHistory│  │ Assets           │               │
│  │ Table            │  │ Table            │               │
│  └──────────────────┘  └──────────────────┘               │
└─────────────────────────────────────────────────────────────┘
```

### Component Interaction Flow

1. **Assignment Creation**:
   - User submits assignment request via AllocationFormComponent
   - AllocationService (Frontend) sends POST request to AllocationController
   - AllocationController validates authorization
   - AllocationService (Backend) validates asset availability
   - AssignmentHistoryRepository creates assignment record
   - AssetRepository updates asset assignment fields
   - AuditService logs the operation
   - Response returned to frontend

2. **Assignment History Retrieval**:
   - User requests history via AssignmentHistoryComponent
   - AllocationService (Frontend) sends GET request
   - AllocationController validates authorization
   - AllocationService (Backend) queries AssignmentHistoryRepository
   - Paginated results returned to frontend

## Backend Design

### Package Structure

```
com.company.assetmanagement.module3/
├── controller/
│   ├── AllocationController.java
│   └── AssignmentHistoryController.java
├── service/
│   ├── AllocationService.java
│   └── AllocationServiceImpl.java
├── repository/
│   └── AssignmentHistoryRepository.java
├── model/
│   ├── AssignmentHistory.java
│   └── AssignmentType.java (enum)
└── dto/
    ├── AssignmentDTO.java
    ├── AssignmentRequest.java
    ├── AssignmentHistoryDTO.java
    └── AssignmentStatisticsDTO.java
```

### Data Models

#### AssignmentHistory Entity

```java
@Entity
@Table(name = "AssignmentHistory", indexes = {
    @Index(name = "IX_AssignmentHistory_AssetId", columnList = "assetId"),
    @Index(name = "IX_AssignmentHistory_AssignedTo", columnList = "assignedTo"),
    @Index(name = "IX_AssignmentHistory_AssignedAt", columnList = "assignedAt")
})
public class AssignmentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(nullable = false)
    private UUID assetId;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AssignmentType assignmentType;
    
    @Column(nullable = false, length = 255)
    private String assignedTo;
    
    @Column(nullable = false)
    private UUID assignedBy;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime assignedAt;
    
    @Column(nullable = true)
    private LocalDateTime unassignedAt;
    
    // Getters, setters, equals, hashCode
}
```

#### AssignmentType Enum

```java
public enum AssignmentType {
    USER,
    LOCATION
}
```

### Service Layer

#### AllocationService Interface

```java
public interface AllocationService {
    AssignmentDTO assignToUser(String userId, UUID assetId, AssignmentRequest request);
    AssignmentDTO assignToLocation(String userId, UUID assetId, AssignmentRequest request);
    void deallocate(String userId, UUID assetId);
    AssignmentDTO reassign(String userId, UUID assetId, AssignmentRequest request);
    Page<AssignmentHistoryDTO> getAssignmentHistory(UUID assetId, Pageable pageable);
    Page<AssetDTO> getAssetsByUser(String userName, Pageable pageable);
    Page<AssetDTO> getAssetsByLocation(String location, Pageable pageable);
    AssignmentStatisticsDTO getStatistics();
    List<AssignmentDTO> bulkDeallocate(String userId, List<UUID> assetIds);
}
```

#### AllocationServiceImpl

```java
@Service
@Transactional
public class AllocationServiceImpl implements AllocationService {
    
    private final AssignmentHistoryRepository assignmentHistoryRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final AuthorizationService authorizationService;
    
    @Override
    public AssignmentDTO assignToUser(String userId, UUID assetId, AssignmentRequest request) {
        // 1. Authorization check
        if (!authorizationService.hasPermission(userId, Action.ALLOCATE_ASSET)) {
            throw new InsufficientPermissionsException();
        }
        
        // 2. Validation
        validateAssignmentRequest(request, AssignmentType.USER);
        
        // 3. Check asset availability
        Asset asset = assetRepository.findById(assetId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset", assetId));
        
        validateAssetAssignable(asset);
        
        if (asset.getAssignedUser() != null || asset.getLocation() != null) {
            throw new AssetAlreadyAssignedException(assetId);
        }
        
        // 4. Create assignment record
        AssignmentHistory assignment = new AssignmentHistory();
        assignment.setAssetId(assetId);
        assignment.setAssignmentType(AssignmentType.USER);
        assignment.setAssignedTo(request.getAssignedTo());
        assignment.setAssignedBy(UUID.fromString(userId));
        assignment.setAssignedAt(LocalDateTime.now());
        
        assignment = assignmentHistoryRepository.save(assignment);
        
        // 5. Update asset
        asset.setAssignedUser(request.getAssignedTo());
        asset.setAssignedUserEmail(request.getAssignedUserEmail());
        asset.setAssignmentDate(LocalDateTime.now());
        assetRepository.save(asset);
        
        // 6. Audit logging
        auditService.logEvent(AuditEvent.builder()
            .userId(userId)
            .actionType(ActionType.CREATE)
            .resourceType(ResourceType.ASSIGNMENT)
            .resourceId(assignment.getId().toString())
            .metadata(Map.of(
                "assetId", assetId.toString(),
                "assignmentType", "USER",
                "assignedTo", request.getAssignedTo()
            ))
            .build());
        
        return mapToDTO(assignment);
    }
    
    private void validateAssetAssignable(Asset asset) {
        if (!List.of(LifecycleStatus.IN_USE, LifecycleStatus.DEPLOYED, LifecycleStatus.STORAGE)
                .contains(asset.getStatus())) {
            throw new AssetNotAssignableException(asset.getId(), asset.getStatus());
        }
    }
}
```

### Controller Layer

#### AllocationController

```java
@RestController
@RequestMapping("/api/v1/assets")
@Validated
public class AllocationController {
    
    private final AllocationService allocationService;
    
    @PostMapping("/{id}/assignments")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    public ResponseEntity<AssignmentDTO> createAssignment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody AssignmentRequest request) {
        
        AssignmentDTO assignment;
        if (request.getAssignmentType() == AssignmentType.USER) {
            assignment = allocationService.assignToUser(userDetails.getUsername(), id, request);
        } else {
            assignment = allocationService.assignToLocation(userDetails.getUsername(), id, request);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }
    
    @DeleteMapping("/{id}/assignments")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER')")
    public ResponseEntity<Void> deallocate(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        
        allocationService.deallocate(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/assignment-history")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ASSET_MANAGER', 'VIEWER')")
    public ResponseEntity<Page<AssignmentHistoryDTO>> getAssignmentHistory(
            @PathVariable UUID id,
            Pageable pageable) {
        
        Page<AssignmentHistoryDTO> history = allocationService.getAssignmentHistory(id, pageable);
        return ResponseEntity.ok(history);
    }
}
```

### DTOs

#### AssignmentRequest

```java
public class AssignmentRequest {
    @NotNull(message = "Assignment type is required")
    private AssignmentType assignmentType;
    
    @NotBlank(message = "Assigned to is required")
    @Size(max = 255, message = "Assigned to must not exceed 255 characters")
    private String assignedTo;
    
    @Email(message = "Invalid email format")
    private String assignedUserEmail; // Required for USER assignments
    
    // Getters and setters
}
```

#### AssignmentDTO

```java
public class AssignmentDTO {
    private UUID id;
    private UUID assetId;
    private AssignmentType assignmentType;
    private String assignedTo;
    private String assignedBy;
    private LocalDateTime assignedAt;
    private LocalDateTime unassignedAt;
    
    // Getters and setters
}
```

## Frontend Design

### Component Structure

```
features/module3-allocation/
├── allocation-form/
│   ├── allocation-form.component.ts
│   ├── allocation-form.component.html
│   └── allocation-form.component.scss
├── allocation-history/
│   ├── allocation-history.component.ts
│   ├── allocation-history.component.html
│   └── allocation-history.component.scss
├── deallocation-form/
│   ├── deallocation-form.component.ts
│   ├── deallocation-form.component.html
│   └── deallocation-form.component.scss
├── services/
│   └── allocation.service.ts
├── models/
│   ├── assignment.model.ts
│   └── assignment-history.model.ts
└── allocation.module.ts
```

### Allocation Service

```typescript
@Injectable({
  providedIn: 'root'
})
export class AllocationService {
  private readonly apiUrl = '/api/v1/assets';
  
  constructor(private http: HttpClient) {}
  
  assignToUser(assetId: string, request: AssignmentRequest): Observable<Assignment> {
    return this.http.post<Assignment>(
      `${this.apiUrl}/${assetId}/assignments`,
      { ...request, assignmentType: 'USER' }
    ).pipe(catchError(this.handleError));
  }
  
  assignToLocation(assetId: string, request: AssignmentRequest): Observable<Assignment> {
    return this.http.post<Assignment>(
      `${this.apiUrl}/${assetId}/assignments`,
      { ...request, assignmentType: 'LOCATION' }
    ).pipe(catchError(this.handleError));
  }
  
  deallocate(assetId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${assetId}/assignments`)
      .pipe(catchError(this.handleError));
  }
  
  getAssignmentHistory(assetId: string, page: number = 0, size: number = 20): Observable<Page<AssignmentHistory>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<Page<AssignmentHistory>>(
      `${this.apiUrl}/${assetId}/assignment-history`,
      { params }
    ).pipe(catchError(this.handleError));
  }
  
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An error occurred';
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = error.error.message;
    } else {
      errorMessage = error.error?.message || `Error Code: ${error.status}`;
    }
    
    return throwError(() => new Error(errorMessage));
  }
}
```

### Allocation Form Component

```typescript
@Component({
  selector: 'app-allocation-form',
  templateUrl: './allocation-form.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AllocationFormComponent implements OnInit {
  allocationForm: FormGroup;
  assignmentTypes = ['USER', 'LOCATION'];
  
  constructor(
    private fb: FormBuilder,
    private allocationService: AllocationService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.allocationForm = this.fb.group({
      assignmentType: ['USER', Validators.required],
      assignedTo: ['', [Validators.required, Validators.maxLength(255)]],
      assignedUserEmail: ['', [Validators.email]]
    });
  }
  
  ngOnInit(): void {
    // Watch assignment type changes to conditionally require email
    this.allocationForm.get('assignmentType')?.valueChanges.subscribe(type => {
      const emailControl = this.allocationForm.get('assignedUserEmail');
      if (type === 'USER') {
        emailControl?.setValidators([Validators.required, Validators.email]);
      } else {
        emailControl?.clearValidators();
      }
      emailControl?.updateValueAndValidity();
    });
  }
  
  onSubmit(): void {
    if (this.allocationForm.valid) {
      const assetId = this.route.snapshot.paramMap.get('id')!;
      const request = this.allocationForm.value;
      
      const operation = request.assignmentType === 'USER'
        ? this.allocationService.assignToUser(assetId, request)
        : this.allocationService.assignToLocation(assetId, request);
      
      operation.subscribe({
        next: () => {
          this.snackBar.open('Asset assigned successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/assets', assetId]);
        },
        error: (error) => {
          this.snackBar.open(error.message, 'Close', { duration: 5000 });
        }
      });
    } else {
      this.allocationForm.markAllAsTouched();
    }
  }
}
```

## API Endpoints

### POST /api/v1/assets/{id}/assignments

**Request**:
```json
{
  "assignmentType": "USER",
  "assignedTo": "John Doe",
  "assignedUserEmail": "john.doe@example.com"
}
```

**Response** (201 Created):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "assetId": "123e4567-e89b-12d3-a456-426614174000",
  "assignmentType": "USER",
  "assignedTo": "John Doe",
  "assignedBy": "admin",
  "assignedAt": "2024-01-15T10:30:00Z",
  "unassignedAt": null
}
```

### DELETE /api/v1/assets/{id}/assignments

**Response** (204 No Content)

### GET /api/v1/assets/{id}/assignment-history

**Query Parameters**:
- page: 0 (default)
- size: 20 (default)

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "assetId": "123e4567-e89b-12d3-a456-426614174000",
      "assignmentType": "USER",
      "assignedTo": "John Doe",
      "assignedBy": "admin",
      "assignedAt": "2024-01-15T10:30:00Z",
      "unassignedAt": "2024-01-20T14:00:00Z"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 5,
    "totalPages": 1
  }
}
```

## Error Handling

### Custom Exceptions

```java
public class AssetAlreadyAssignedException extends RuntimeException {
    public AssetAlreadyAssignedException(UUID assetId) {
        super("Asset " + assetId + " is already assigned");
    }
}

public class AssetNotAssignedException extends RuntimeException {
    public AssetNotAssignedException(UUID assetId) {
        super("Asset " + assetId + " is not currently assigned");
    }
}

public class AssetNotAssignableException extends RuntimeException {
    public AssetNotAssignableException(UUID assetId, LifecycleStatus status) {
        super("Asset " + assetId + " cannot be assigned with status " + status);
    }
}
```

### Global Exception Handler

```java
@RestControllerAdvice
public class AllocationExceptionHandler {
    
    @ExceptionHandler(AssetAlreadyAssignedException.class)
    public ResponseEntity<ErrorResponse> handleAssetAlreadyAssigned(
            AssetAlreadyAssignedException ex, HttpServletRequest request) {
        
        ErrorResponse error = ErrorResponse.builder()
            .type("ASSET_ALREADY_ASSIGNED")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .requestId(request.getHeader("X-Request-ID"))
            .build();
            
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(AssetNotAssignableException.class)
    public ResponseEntity<ErrorResponse> handleAssetNotAssignable(
            AssetNotAssignableException ex, HttpServletRequest request) {
        
        ErrorResponse error = ErrorResponse.builder()
            .type("ASSET_NOT_ASSIGNABLE")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .requestId(request.getHeader("X-Request-ID"))
            .build();
            
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }
}
```

## Security Implementation

### Authorization

```java
@Configuration
@EnableMethodSecurity
public class AllocationSecurityConfig {
    
    @Bean
    public AuthorizationService authorizationService() {
        return new AuthorizationServiceImpl();
    }
}
```

### Role-Based Access Control

- **ADMINISTRATOR**: Full access to all allocation operations
- **ASSET_MANAGER**: Can allocate, deallocate, and view history
- **VIEWER**: Can only view assignment history

## Performance Optimization

### Database Indexes

```sql
CREATE INDEX IX_AssignmentHistory_AssetId ON AssignmentHistory(AssetId);
CREATE INDEX IX_AssignmentHistory_AssignedTo ON AssignmentHistory(AssignedTo);
CREATE INDEX IX_AssignmentHistory_AssignedAt ON AssignmentHistory(AssignedAt);
```

### Query Optimization

```java
@Query("SELECT ah FROM AssignmentHistory ah WHERE ah.assetId = :assetId ORDER BY ah.assignedAt DESC")
Page<AssignmentHistory> findByAssetIdOrderByAssignedAtDesc(
    @Param("assetId") UUID assetId, 
    Pageable pageable
);
```

### Connection Pooling

```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

## Testing Strategy

### Property-Based Testing

**Property 18**: Assignment creation generates unique identifier
```java
@Property
void assignmentCreationGeneratesUniqueIdentifier(
        @ForAll("validAssignmentRequests") AssignmentRequest request) {
    
    UUID assetId = createTestAsset();
    AssignmentDTO result = allocationService.assignToUser("admin", assetId, request);
    
    assertThat(result.getId()).isNotNull();
    assertThat(result.getAssetId()).isEqualTo(assetId);
    assertThat(result.getAssignedTo()).isEqualTo(request.getAssignedTo());
}
```

**Property 19**: Assignment history maintains chronological order
```java
@Property
void assignmentHistoryMaintainsChronologicalOrder(
        @ForAll("assignmentSequence") List<AssignmentRequest> requests) {
    
    UUID assetId = createTestAsset();
    
    for (AssignmentRequest request : requests) {
        allocationService.assignToUser("admin", assetId, request);
        allocationService.deallocate("admin", assetId);
    }
    
    Page<AssignmentHistoryDTO> history = allocationService.getAssignmentHistory(
        assetId, PageRequest.of(0, 100)
    );
    
    List<LocalDateTime> timestamps = history.getContent().stream()
        .map(AssignmentHistoryDTO::getAssignedAt)
        .collect(Collectors.toList());
    
    assertThat(timestamps).isSortedAccordingTo(Comparator.reverseOrder());
}
```

### Unit Testing

```java
@ExtendWith(MockitoExtension.class)
class AllocationServiceImplTest {
    
    @Mock
    private AssignmentHistoryRepository assignmentHistoryRepository;
    
    @Mock
    private AssetRepository assetRepository;
    
    @Mock
    private AuditService auditService;
    
    @InjectMocks
    private AllocationServiceImpl allocationService;
    
    @Test
    void shouldAssignAssetToUser() {
        // Given
        UUID assetId = UUID.randomUUID();
        AssignmentRequest request = new AssignmentRequest();
        request.setAssignmentType(AssignmentType.USER);
        request.setAssignedTo("John Doe");
        request.setAssignedUserEmail("john@example.com");
        
        Asset asset = createTestAsset(assetId);
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(assignmentHistoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        // When
        AssignmentDTO result = allocationService.assignToUser("admin", assetId, request);
        
        // Then
        assertThat(result).isNotNull();
        verify(assignmentHistoryRepository).save(any(AssignmentHistory.class));
        verify(assetRepository).save(any(Asset.class));
        verify(auditService).logEvent(any(AuditEvent.class));
    }
}
```

## Integration Points

### Module 2 (Asset Management)
- Read asset status to validate assignability
- Update asset assignment fields
- Query assets by assignment status

### Module 1 (User Management)
- Validate assigned by user exists
- Check user permissions for allocation operations

### Module 4 (Ticket Management)
- Process allocations when tickets are completed
- Validate asset state for ticket-driven allocations

### Audit Service
- Log all allocation operations
- Include metadata for audit trail

## Deployment Considerations

### Database Migration

```sql
-- V3__create_assignment_history_table.sql
CREATE TABLE AssignmentHistory (
    Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    AssetId UNIQUEIDENTIFIER NOT NULL,
    AssignmentType NVARCHAR(20) NOT NULL,
    AssignedTo NVARCHAR(255) NOT NULL,
    AssignedBy UNIQUEIDENTIFIER NOT NULL,
    AssignedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    UnassignedAt DATETIME2 NULL,
    
    CONSTRAINT FK_AssignmentHistory_AssetId 
        FOREIGN KEY (AssetId) REFERENCES Assets(Id) ON DELETE CASCADE,
    CONSTRAINT FK_AssignmentHistory_AssignedBy 
        FOREIGN KEY (AssignedBy) REFERENCES Users(Id),
    CONSTRAINT CHK_AssignmentHistory_Type 
        CHECK (AssignmentType IN ('USER', 'LOCATION'))
);

CREATE INDEX IX_AssignmentHistory_AssetId ON AssignmentHistory(AssetId);
CREATE INDEX IX_AssignmentHistory_AssignedTo ON AssignmentHistory(AssignedTo);
CREATE INDEX IX_AssignmentHistory_AssignedAt ON AssignmentHistory(AssignedAt);
```

### Configuration

```properties
# application.properties
allocation.max-bulk-size=50
allocation.max-export-size=10000
allocation.default-page-size=20
allocation.max-page-size=100
```

## Monitoring and Logging

### Metrics

```java
@Component
public class AllocationMetrics {
    private final MeterRegistry meterRegistry;
    
    public void recordAssignment(AssignmentType type) {
        meterRegistry.counter("allocation.assignments.created",
            "type", type.name()).increment();
    }
    
    public void recordDeallocation() {
        meterRegistry.counter("allocation.deallocations").increment();
    }
}
```

### Logging

```java
@Slf4j
@Service
public class AllocationServiceImpl implements AllocationService {
    
    @Override
    public AssignmentDTO assignToUser(String userId, UUID assetId, AssignmentRequest request) {
        log.info("Assigning asset {} to user {} by user {}", 
            assetId, request.getAssignedTo(), userId);
        
        try {
            // Implementation
            log.info("Successfully assigned asset {} to user {}", 
                assetId, request.getAssignedTo());
        } catch (Exception e) {
            log.error("Failed to assign asset {} to user {}: {}", 
                assetId, request.getAssignedTo(), e.getMessage());
            throw e;
        }
    }
}
```

## Correctness Properties

### Property 18: Assignment Creation
*For any* valid assignment request, the system SHALL generate a unique identifier and persist all assignment fields correctly.

### Property 19: Assignment History Order
*For any* sequence of assignments and deallocations, the assignment history SHALL maintain chronological order with most recent first.

### Property 20: Deallocation Completeness
*For any* deallocation operation, the system SHALL properly close the assignment record and clear all asset assignment fields.

These properties will be validated through property-based testing using jqwik framework with minimum 100 iterations per test.
