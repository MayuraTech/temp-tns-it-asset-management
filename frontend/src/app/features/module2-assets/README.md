# Asset Management Module (Module 2)

## Overview

The Asset Management module provides comprehensive lifecycle management for IT infrastructure assets. This module implements the complete asset management workflow from creation to retirement, including search, filtering, assignment tracking, and audit history.

## Module Structure

```
module2-assets/
├── components/
│   ├── asset-inventory/          # Main list view with search and filters
│   ├── asset-form/                # Create/Edit form component
│   └── asset-detail/              # Detail view with 3-column bento layout
├── services/
│   └── asset.service.ts           # API communication service
├── models/
│   ├── asset.model.ts             # Asset entity model
│   ├── asset-type.enum.ts         # 15 asset types
│   ├── lifecycle-status.enum.ts   # 7 lifecycle statuses
│   ├── asset-request.model.ts     # Create/Update request model
│   ├── asset-search-query.model.ts # Search parameters model
│   └── page.model.ts              # Pagination model
├── assets.module.ts               # Module configuration
├── assets.routes.ts               # Routing configuration
└── index.ts                       # Barrel exports
```

## Features

### Asset Inventory (List View)
- **Global Search**: Search across name, serial number, and location
- **Advanced Filtering**: Filter by asset type, status, and location
- **Sortable Columns**: Sort by any column in the table
- **Pagination**: Configurable page size with navigation controls
- **Quick Stats**: Dashboard widget showing total assets, in use, and available
- **Export**: Export filtered results to CSV or JSON
- **Bulk Actions**: Select multiple assets for batch operations

### Asset Form (Create/Edit)
- **Three-Section Layout**:
  1. General Details (Asset Type, Manufacturer, Model, Serial Number)
  2. Lifecycle & Warranty (Purchase Date, Warranty, Cost Center, Value)
  3. Asset Tracking (Status, Assigned User, Location, IP Address)
- **Side Panel**:
  - Visual Identity Card with asset image
  - Recent Activity timeline
  - Technical Specs mini-grid
- **Validation**: Comprehensive client-side validation with error messages
- **Read-Only Fields**: Serial number is read-only in edit mode
- **Auto-Save**: Draft status with last saved timestamp

### Asset Detail View
- **3-Column Bento Grid Layout**:
  - **Left Column (40%)**: General details and asset image
  - **Middle Column (30%)**: Current assignment and lifecycle history
  - **Right Column (30%)**: Quick actions and assignment history
- **Breadcrumb Navigation**: Easy navigation back to inventory
- **Quick Actions**: Edit, Change Status, Generate Report
- **Status Change Dialog**: Update lifecycle status with validation
- **Delete Confirmation**: Secure deletion with confirmation dialog

## Routing

| Route | Component | Description |
|-------|-----------|-------------|
| `/assets` | AssetInventoryComponent | Asset list with search and filters |
| `/assets/new` | AssetFormComponent | Create new asset form |
| `/assets/:id` | AssetDetailComponent | Asset detail view |
| `/assets/:id/edit` | AssetFormComponent | Edit existing asset |

## Services

### AssetService

The `AssetService` provides methods for all asset-related API operations:

```typescript
// Retrieve assets with pagination and filtering
getAssets(query?: AssetSearchQuery, page?: number, size?: number, sort?: string): Observable<Page<Asset>>

// Get single asset by ID
getAsset(id: string): Observable<Asset>

// Create new asset
createAsset(request: AssetRequest): Observable<Asset>

// Update existing asset
updateAsset(id: string, request: AssetRequest): Observable<Asset>

// Update asset status
updateStatus(id: string, status: LifecycleStatus): Observable<Asset>

// Delete asset
deleteAsset(id: string): Observable<void>

// Search assets with advanced filters
searchAssets(query: AssetSearchQuery, page?: number, size?: number, sort?: string): Observable<Page<Asset>>

// Export assets to CSV or JSON
exportAssets(format: 'CSV' | 'JSON', query?: AssetSearchQuery): Observable<Blob>

// Import assets from file
importAssets(format: 'CSV' | 'JSON', file: File): Observable<ImportResult>
```

## Models

### Asset
```typescript
interface Asset {
  id: string;
  assetType: AssetType;
  name: string;
  serialNumber: string;
  acquisitionDate: string;
  status: LifecycleStatus;
  location?: string;
  assignedUser?: string;
  assignedUserEmail?: string;
  assignmentDate?: string;
  locationUpdateDate?: string;
  notes?: string;
  customFields?: string;
  createdAt: string;
  createdBy: string;
  updatedAt: string;
  updatedBy: string;
  readOnly: boolean;
}
```

### AssetType (15 types)
- SERVER
- WORKSTATION
- NETWORK_DEVICE
- STORAGE_DEVICE
- SOFTWARE_LICENSE
- PERIPHERAL
- KEYBOARD
- MOUSE
- LAPTOP
- MONITOR
- HEADSET
- LAPTOP_CHARGER
- HDMI_CABLE
- NETWORK_CABLE
- ACCESS_CARD

### LifecycleStatus (7 statuses)
- ORDERED
- RECEIVED
- DEPLOYED
- IN_USE
- MAINTENANCE
- STORAGE
- RETIRED

## Design System

This module follows the **Editorial Geometry UI Standards**:

### Color Palette
- **Primary**: #143b7d (Blue 800)
- **Secondary**: #a9371d (Red-Orange)
- **Surface**: #faf9ff (Light purple base)
- **Surface Container**: #eeedf4 (Content blocks)

### Typography
- **Headings**: Manrope (geometric precision)
- **Body**: Inter (readability)
- **Display Large**: 48px with -2% letter-spacing
- **Headline Large**: 30px with -0.75px tracking

### Layout Principles
- **Asymmetrical Layouts**: Text left, geometric shapes right
- **Geometric Accents**: Triangle shapes with 80px breathing room
- **Glassmorphism**: Backdrop blur for floating elements
- **No-Line Rule**: Use tonal layering instead of borders
- **Surface Hierarchy**: Depth through background color shifts

## Usage Example

### Importing the Module

```typescript
import { AssetsModule } from './features/module2-assets';

@NgModule({
  imports: [
    AssetsModule,
    // other modules
  ]
})
export class AppModule { }
```

### Using the Service

```typescript
import { AssetService } from './features/module2-assets';

@Component({
  selector: 'app-my-component',
  template: '...'
})
export class MyComponent implements OnInit {
  constructor(private assetService: AssetService) {}
  
  ngOnInit(): void {
    this.assetService.getAssets().subscribe(assets => {
      console.log('Assets:', assets);
    });
  }
}
```

### Navigating to Asset Routes

```typescript
// Navigate to asset inventory
this.router.navigate(['/assets']);

// Navigate to create new asset
this.router.navigate(['/assets/new']);

// Navigate to asset detail
this.router.navigate(['/assets', assetId]);

// Navigate to edit asset
this.router.navigate(['/assets', assetId, 'edit']);
```

## API Integration

The module communicates with the backend API at:
- Base URL: `http://localhost:8080/api/v1/assets`
- Authentication: JWT Bearer token in Authorization header
- Content Type: application/json

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/assets` | List assets with pagination |
| GET | `/api/v1/assets/{id}` | Get single asset |
| POST | `/api/v1/assets` | Create new asset |
| PUT | `/api/v1/assets/{id}` | Update asset |
| PATCH | `/api/v1/assets/{id}/status` | Update status |
| DELETE | `/api/v1/assets/{id}` | Delete asset |
| GET | `/api/v1/assets/search` | Advanced search |
| GET | `/api/v1/assets/export` | Export assets |
| POST | `/api/v1/assets/import` | Import assets |

## Testing

### Unit Tests
- Component tests with TestBed
- Service tests with HttpClientTestingModule
- Mock data fixtures for testing

### Integration Tests
- End-to-end tests with Cypress
- API integration tests
- User workflow tests

### Test Coverage
- Target: 80% code coverage minimum
- All critical paths tested
- Edge cases covered

## Performance Considerations

- **Lazy Loading**: Module is lazy loaded for optimal initial load time
- **Pagination**: Default page size of 20 items, configurable up to 100
- **Debouncing**: Search input debounced at 300ms
- **Caching**: Asset details cached for 5 minutes
- **Virtual Scrolling**: Implemented for large lists
- **OnPush Change Detection**: Used for optimal performance

## Security

- **Authentication**: JWT token required for all operations
- **Authorization**: Role-based access control (RBAC)
  - Administrator: Full access
  - Asset_Manager: Create, read, update
  - Viewer: Read-only access
- **Input Validation**: Client-side and server-side validation
- **XSS Prevention**: Sanitized user inputs
- **CSRF Protection**: Token-based protection

## Future Enhancements

- [ ] Bulk import with progress tracking
- [ ] Advanced reporting with charts
- [ ] Asset QR code generation
- [ ] Mobile app integration
- [ ] Real-time notifications
- [ ] Asset depreciation calculator
- [ ] Maintenance scheduling
- [ ] Asset comparison tool

## Related Modules

- **Module 3**: Allocation Management (depends on Module 2)
- **Module 4**: Ticketing System (references assets)
- **Module 5**: Reporting (aggregates asset data)

## Support

For issues or questions about this module, please contact:
- Developer: Developer 2
- Module: Asset Management (Module 2)
- Package: `com.company.assetmanagement.module2`
