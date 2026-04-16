# Module 2 Assets - Services

This directory contains services for API communication with the backend for asset management operations.

## AssetService

The `AssetService` provides a comprehensive API client for managing IT infrastructure assets. It handles all HTTP communication with the backend REST API.

### Features

- **CRUD Operations**: Create, read, update, and delete assets
- **Search & Filtering**: Advanced search with multiple filter criteria
- **Status Management**: Update asset lifecycle status with validation
- **Import/Export**: Bulk operations for CSV and JSON formats
- **Error Handling**: Comprehensive error handling with user-friendly messages
- **Type Safety**: Fully typed with TypeScript interfaces

### Usage Examples

#### Importing the Service

```typescript
import { AssetService } from '@features/module2-assets/services';
```

#### Retrieving Assets

```typescript
// Get paginated list of assets
this.assetService.getAssets().subscribe({
  next: (page) => {
    console.log('Assets:', page.content);
    console.log('Total:', page.page.totalElements);
  },
  error: (error) => console.error('Error:', error.message)
});

// Get assets with custom pagination
this.assetService.getAssets(undefined, 2, 50).subscribe(page => {
  // Page 2 with 50 items per page
});

// Get single asset by ID
this.assetService.getAsset('asset-id').subscribe(asset => {
  console.log('Asset:', asset);
});
```

#### Creating Assets

```typescript
const request: AssetRequest = {
  assetType: AssetType.SERVER,
  name: 'Production Server 01',
  serialNumber: 'SRV-PROD-001',
  acquisitionDate: '2024-01-15',
  status: LifecycleStatus.ORDERED,
  location: 'Data Center A',
  notes: 'Primary application server'
};

this.assetService.createAsset(request).subscribe({
  next: (asset) => {
    console.log('Created asset:', asset);
    this.snackBar.open('Asset created successfully', 'Close', { duration: 3000 });
  },
  error: (error) => {
    console.error('Error:', error.message);
    this.snackBar.open(error.message, 'Close', { duration: 5000 });
  }
});
```

#### Updating Assets

```typescript
// Full update
const request: AssetRequest = {
  assetType: AssetType.SERVER,
  name: 'Updated Server Name',
  serialNumber: 'SRV-PROD-001',
  acquisitionDate: '2024-01-15',
  status: LifecycleStatus.IN_USE,
  location: 'Data Center B'
};

this.assetService.updateAsset('asset-id', request).subscribe(asset => {
  console.log('Updated asset:', asset);
});

// Status update only
this.assetService.updateStatus('asset-id', LifecycleStatus.DEPLOYED).subscribe(asset => {
  console.log('Status updated:', asset.status);
});
```

#### Searching Assets

```typescript
const query: AssetSearchQuery = {
  text: 'server',
  assetTypes: [AssetType.SERVER, AssetType.WORKSTATION],
  statuses: [LifecycleStatus.IN_USE, LifecycleStatus.DEPLOYED],
  location: 'Data Center A',
  acquisitionDateFrom: '2024-01-01',
  acquisitionDateTo: '2024-12-31'
};

this.assetService.searchAssets(query, 0, 20, 'name,asc').subscribe(page => {
  console.log('Search results:', page.content);
});
```

#### Deleting Assets

```typescript
this.assetService.deleteAsset('asset-id').subscribe({
  next: () => {
    console.log('Asset deleted successfully');
    this.snackBar.open('Asset deleted', 'Close', { duration: 3000 });
  },
  error: (error) => {
    console.error('Error:', error.message);
  }
});
```

#### Exporting Assets

```typescript
// Export all assets as CSV
this.assetService.exportAssets('CSV').subscribe(blob => {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = 'assets.csv';
  link.click();
  window.URL.revokeObjectURL(url);
});

// Export filtered assets as JSON
const query: AssetSearchQuery = {
  assetTypes: [AssetType.SERVER]
};

this.assetService.exportAssets('JSON', query).subscribe(blob => {
  // Handle blob download
});
```

#### Importing Assets

```typescript
onFileSelected(event: Event): void {
  const input = event.target as HTMLInputElement;
  if (input.files && input.files.length > 0) {
    const file = input.files[0];
    
    this.assetService.importAssets('CSV', file).subscribe({
      next: (result) => {
        console.log(`Import completed: ${result.successCount} succeeded, ${result.failureCount} failed`);
        
        if (result.errors && result.errors.length > 0) {
          console.log('Import errors:', result.errors);
        }
        
        this.snackBar.open(
          `Imported ${result.successCount} assets successfully`,
          'Close',
          { duration: 5000 }
        );
      },
      error: (error) => {
        console.error('Import failed:', error.message);
        this.snackBar.open(error.message, 'Close', { duration: 5000 });
      }
    });
  }
}
```

### API Methods

#### getAssets(query?, page?, size?, sort?)
Retrieves a paginated list of assets with optional filtering.

**Parameters:**
- `query?: AssetSearchQuery` - Optional search query parameters
- `page?: number` - Page number (zero-based, default: 0)
- `size?: number` - Items per page (default: 20)
- `sort?: string` - Sort field and direction (e.g., 'name,asc')

**Returns:** `Observable<Page<Asset>>`

#### getAsset(id)
Retrieves a single asset by its ID.

**Parameters:**
- `id: string` - Asset ID (UUID format)

**Returns:** `Observable<Asset>`

#### createAsset(request)
Creates a new asset in the system.

**Parameters:**
- `request: AssetRequest` - Asset creation request

**Returns:** `Observable<Asset>`

#### updateAsset(id, request)
Updates an existing asset with new data.

**Parameters:**
- `id: string` - Asset ID
- `request: AssetRequest` - Asset update request

**Returns:** `Observable<Asset>`

#### updateStatus(id, status)
Updates the lifecycle status of an asset.

**Parameters:**
- `id: string` - Asset ID
- `status: LifecycleStatus` - New lifecycle status

**Returns:** `Observable<Asset>`

#### deleteAsset(id)
Deletes an asset from the system.

**Parameters:**
- `id: string` - Asset ID

**Returns:** `Observable<void>`

#### searchAssets(query, page?, size?, sort?)
Searches assets with advanced filtering.

**Parameters:**
- `query: AssetSearchQuery` - Search query parameters
- `page?: number` - Page number (default: 0)
- `size?: number` - Items per page (default: 20)
- `sort?: string` - Sort field and direction

**Returns:** `Observable<Page<Asset>>`

#### exportAssets(format, query?)
Exports assets to the specified format.

**Parameters:**
- `format: 'CSV' | 'JSON'` - Export format
- `query?: AssetSearchQuery` - Optional filter for export

**Returns:** `Observable<Blob>`

#### importAssets(format, file)
Imports assets from a file.

**Parameters:**
- `format: 'CSV' | 'JSON'` - Import format
- `file: File` - File to import

**Returns:** `Observable<ImportResult>`

### Error Handling

The service provides comprehensive error handling with user-friendly messages:

- **400 Bad Request**: "Invalid request. Please check your input."
- **401 Unauthorized**: "Authentication required. Please log in."
- **403 Forbidden**: "You do not have permission to perform this action."
- **404 Not Found**: "The requested resource was not found."
- **409 Conflict**: "A conflict occurred. The resource may already exist."
- **422 Unprocessable Entity**: "The request cannot be processed due to invalid state."
- **429 Too Many Requests**: "Too many requests. Please try again later."
- **500 Internal Server Error**: "Internal server error. Please try again later."
- **503 Service Unavailable**: "Service temporarily unavailable. Please try again later."
- **Network Errors**: "Network error: Unable to connect to the server"

All errors are logged to the console with full error details for debugging.

### Testing

The service includes comprehensive unit tests using Jasmine and HttpClientTestingModule:

- ✅ 25 test cases covering all methods
- ✅ Success scenarios for all operations
- ✅ Error handling for all HTTP status codes
- ✅ Pagination and filtering
- ✅ Query parameter building
- ✅ Network error handling
- ✅ Structured error response handling

Run tests with:
```bash
npm test -- --include='**/asset.service.spec.ts'
```

### Dependencies

- `@angular/common/http` - HttpClient for HTTP requests
- `rxjs` - Observable streams and operators
- `environment` - Environment configuration for API URL

### Related Files

- `../models/asset.model.ts` - Asset interface
- `../models/asset-request.model.ts` - AssetRequest interface
- `../models/asset-search-query.model.ts` - AssetSearchQuery interface
- `../models/page.model.ts` - Page and PageInfo interfaces
- `../models/lifecycle-status.enum.ts` - LifecycleStatus enum
- `../models/asset-type.enum.ts` - AssetType enum

### Best Practices

1. **Always handle errors**: Use error callbacks in subscribe() to handle failures gracefully
2. **Unsubscribe**: Use takeUntil() or async pipe to prevent memory leaks
3. **Loading states**: Show loading indicators during API calls
4. **User feedback**: Display success/error messages using snackbars or toasts
5. **Validation**: Validate user input before calling API methods
6. **Type safety**: Use TypeScript interfaces for type checking

### Example Component Integration

```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { AssetService } from '@features/module2-assets/services';
import { Asset, AssetSearchQuery } from '@features/module2-assets/models';

@Component({
  selector: 'app-asset-list',
  templateUrl: './asset-list.component.html'
})
export class AssetListComponent implements OnInit, OnDestroy {
  assets: Asset[] = [];
  loading = false;
  error: string | null = null;
  
  private destroy$ = new Subject<void>();
  
  constructor(private assetService: AssetService) {}
  
  ngOnInit(): void {
    this.loadAssets();
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  loadAssets(): void {
    this.loading = true;
    this.error = null;
    
    this.assetService.getAssets()
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading = false)
      )
      .subscribe({
        next: (page) => {
          this.assets = page.content;
        },
        error: (error) => {
          this.error = error.message;
        }
      });
  }
}
```

## Future Enhancements

- Add caching for frequently accessed assets
- Implement retry logic for failed requests
- Add request cancellation support
- Implement optimistic updates for better UX
- Add batch operations support
- Implement real-time updates with WebSockets
