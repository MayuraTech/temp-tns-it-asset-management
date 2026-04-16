# Allocation Management Feature Module

This module implements the frontend components for the Allocation Management feature of the IT Infrastructure Asset Management System.

## Overview

The Allocation Management module provides a complete user interface for:
- Assigning assets to users and locations
- Viewing assignment history
- Deallocating assets
- Viewing assignment statistics and analytics

## Components

### 1. Allocation Form Component (`allocation-form/`)

**Purpose**: Provides a reactive form for assigning assets to users or locations.

**Features**:
- Reactive form with FormBuilder
- Conditional email validation (required for USER assignments, optional for LOCATION)
- Dynamic form validation based on assignment type
- Material Design UI components
- Editorial Geometry styling with geometric triangle accents
- Success/error messaging with MatSnackBar
- Navigation on successful assignment

**Routes**: `/allocation/assign/:id`

**Key Methods**:
- `onSubmit()`: Validates and submits assignment request
- `updateEmailValidation()`: Dynamically updates email field validators
- `getErrorMessage()`: Returns user-friendly error messages
- `hasError()`: Checks if field has validation errors

### 2. Assignment History Component (`assignment-history/`)

**Purpose**: Displays paginated assignment history for an asset.

**Features**:
- Material table with pagination
- Shows both active and historical assignments
- Assignment type badges (USER/LOCATION)
- Status indicators (Active/Historical)
- Date formatting
- Loading and empty states
- Editorial Geometry card layout

**Routes**: `/allocation/history/:id`

**Key Methods**:
- `loadHistory()`: Fetches assignment history from service
- `onPageChange()`: Handles pagination events
- `formatDate()`: Formats dates for display
- `isActive()`: Determines if assignment is currently active

### 3. Deallocation Form Component (`deallocation-form/`)

**Purpose**: Provides interface for deallocating assets with confirmation.

**Features**:
- Confirmation dialog before deallocation
- Current assignment information display
- Warning message about deallocation impact
- Success/error messaging
- Editorial Geometry styling with warning indicators

**Routes**: `/allocation/deallocate/:id`

**Key Methods**:
- `deallocate()`: Opens confirmation dialog
- `performDeallocation()`: Executes deallocation operation
- `onCancel()`: Navigates back to asset detail

**Sub-Components**:
- `ConfirmationDialogComponent`: Reusable confirmation dialog

### 4. Assignment Statistics Component (`assignment-statistics/`)

**Purpose**: Displays comprehensive assignment statistics and analytics.

**Features**:
- Asymmetrical layout with geometric accents
- Large display numbers for key metrics
- Total assigned assets counter
- User vs location breakdown
- Available assets by status
- Top 10 users by assignment count (with bar charts)
- Top 10 locations by assignment count (with bar charts)
- Editorial Geometry styling with multiple triangle accents

**Routes**: `/allocation/statistics`

**Key Methods**:
- `loadStatistics()`: Fetches statistics from service
- `calculatePercentage()`: Calculates percentage for visual representation
- `getMaxCount()`: Gets maximum count for bar chart scaling
- `getBarWidth()`: Calculates bar width percentage

## Styling

All components follow the **Editorial Geometry UI Standards**:

### Design Principles
- **Intentional Asymmetry**: Geometric triangle accents break the grid
- **Depth Through Layering**: Overlapping elements create physical depth
- **Premium Editorial Feel**: High-end magazine aesthetic
- **No 1px Borders**: Use background color shifts for sectioning

### Color Palette
- Primary: `#143b7d` (Blue 800)
- Secondary: `#a9371d` (Red-Orange)
- Surface: `#faf9ff` (Light purple)
- Surface Container Lowest: `#ffffff` (Pop effect cards)

### Typography
- Headings: Manrope (geometric precision)
- Body: Inter (readability)
- Display numbers: Manrope for corporate look

### Key Styling Features
- Geometric triangle accents with 80px breathing room
- Editorial shadows: `0 20px 40px rgba(20, 59, 125, 0.06)`
- Gradient buttons: Primary gradient for CTAs
- Ghost buttons: Transparent with triangle icon suffix
- OnPush change detection for performance
- Responsive design with mobile breakpoints
- Accessibility: Focus indicators, ARIA labels, high contrast mode support

## Testing

Each component has comprehensive unit tests:

### Allocation Form Tests (`allocation-form.component.spec.ts`)
- Form initialization
- Conditional email validation
- Form submission (USER and LOCATION types)
- Success/error handling
- Cancel action
- Error message generation

### Assignment History Tests (`assignment-history.component.spec.ts`)
- History loading
- Pagination
- Date formatting
- Assignment type labels and colors
- Active vs historical status
- Error handling

### Deallocation Form Tests (`deallocation-form.component.spec.ts`)
- Confirmation dialog
- Deallocation operation
- Success/error handling
- Cancel action

### Assignment Statistics Tests (`assignment-statistics.component.spec.ts`)
- Statistics loading
- Percentage calculations
- Bar chart calculations
- Error handling
- Reload functionality

## Dependencies

### Angular Material Components
- `MatFormFieldModule`
- `MatInputModule`
- `MatSelectModule`
- `MatButtonModule`
- `MatSnackBarModule`
- `MatProgressSpinnerModule`
- `MatTableModule`
- `MatPaginatorModule`
- `MatCardModule`
- `MatChipsModule`
- `MatDialogModule`

### Services
- `AllocationService`: Core service for allocation operations
- `Router`: Navigation
- `ActivatedRoute`: Route parameter access
- `MatSnackBar`: User notifications
- `MatDialog`: Confirmation dialogs

### Models
- `Assignment`
- `AssignmentRequest`
- `AssignmentHistoryDTO`
- `AssignmentStatistics`
- `AssignmentType` (enum)
- `PageResponse<T>`

## Module Configuration

The module uses standalone components with lazy loading for optimal performance:

```typescript
const routes: Routes = [
  {
    path: 'assign/:id',
    loadComponent: () => import('./components/allocation-form/allocation-form.component')
      .then(m => m.AllocationFormComponent)
  },
  // ... other routes
];
```

## Usage Examples

### Assigning an Asset
```typescript
// Navigate to allocation form
this.router.navigate(['/allocation/assign', assetId]);
```

### Viewing Assignment History
```typescript
// Navigate to history view
this.router.navigate(['/allocation/history', assetId]);
```

### Deallocating an Asset
```typescript
// Navigate to deallocation form
this.router.navigate(['/allocation/deallocate', assetId]);
```

### Viewing Statistics
```typescript
// Navigate to statistics dashboard
this.router.navigate(['/allocation/statistics']);
```

## Performance Optimizations

1. **OnPush Change Detection**: All components use `ChangeDetectionStrategy.OnPush`
2. **Lazy Loading**: Components are lazy-loaded via route configuration
3. **Standalone Components**: Reduced bundle size with standalone architecture
4. **Observable State Management**: BehaviorSubjects for reactive state
5. **Efficient Rendering**: TrackBy functions for lists (where applicable)

## Accessibility

All components follow WCAG 2.1 AA standards:
- Semantic HTML with ARIA landmarks
- Focus indicators (2px solid outline)
- Keyboard navigation support
- Screen reader support
- High contrast mode support
- Color contrast ratios: 4.5:1 for text, 3:1 for focus indicators

## Future Enhancements

Potential improvements for future iterations:
- Advanced filtering in assignment history
- Export assignment history to CSV
- Bulk assignment operations
- Assignment approval workflow integration
- Real-time statistics updates with WebSocket
- Chart library integration (Chart.js or D3.js) for enhanced visualizations
- Assignment notifications
- Assignment templates

## Related Documentation

- [Allocation Management Requirements](/.kiro/specs/allocation-management/requirements.md)
- [Allocation Management Design](/.kiro/specs/allocation-management/design.md)
- [Allocation Management Tasks](/.kiro/specs/allocation-management/tasks.md)
- [Editorial Geometry UI Standards](/.kiro/steering/editorial-geometry-ui-standards.md)
- [IT Asset Management Coding Standards](/.kiro/steering/it-asset-management-coding-standards.md)
