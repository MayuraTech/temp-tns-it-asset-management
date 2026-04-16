# Shared UI Components - Module 2 Assets

This directory contains reusable UI components for the Module 2 Asset Management feature. All components follow the Editorial Geometry design system standards and are implemented as Angular 17+ standalone components.

## Components Overview

### 1. Asset Status Badge (`asset-status-badge.component.ts`)
- **Purpose**: Displays lifecycle status with color-coded badges
- **Features**: 7 lifecycle statuses with proper color coding, accessibility support
- **Usage**: `<app-asset-status-badge [status]="asset.status"></app-asset-status-badge>`

### 2. Asset Icon (`asset-icon.component.ts`)
- **Purpose**: Displays Material Design icons for 15 asset types
- **Features**: Configurable sizes (small, medium, large, xlarge), consistent iconography
- **Usage**: `<app-asset-icon [assetType]="asset.assetType" size="medium"></app-asset-icon>`

### 3. Asset Filters (`asset-filters.component.ts`)
- **Purpose**: Advanced filter bar with glassmorphism effects
- **Features**: Multi-select dropdowns, active filter chips, reset functionality
- **Usage**: `<app-asset-filters [selectedAssetTypes]="types" (assetTypesChange)="onTypesChange($event)"></app-asset-filters>`

### 4. Assignment Card (`assignment-card.component.ts`)
- **Purpose**: User assignment display with avatar and contact info
- **Features**: Avatar generation, contact details, reassignment actions, unassigned state
- **Usage**: `<app-assignment-card [assignedUser]="asset.assignedUser" (reassignClick)="onReassign()"></app-assignment-card>`

### 5. Lifecycle Timeline (`lifecycle-timeline.component.ts`)
- **Purpose**: Chronological history of asset lifecycle events
- **Features**: Vertical timeline, status icons, user attribution, date formatting
- **Usage**: `<app-lifecycle-timeline [events]="lifecycleEvents"></app-lifecycle-timeline>`

### 6. Quick Actions (`quick-actions.component.ts`)
- **Purpose**: Action buttons for common asset operations
- **Features**: Primary/secondary actions, status transitions, conditional states
- **Usage**: `<app-quick-actions [currentStatus]="asset.status" (editClick)="onEdit()"></app-quick-actions>`

### 7. Technical Specs Grid (`technical-specs-grid.component.ts`)
- **Purpose**: Technical specifications display in responsive grid
- **Features**: Category grouping, asset type defaults, icon support, units display
- **Usage**: `<app-technical-specs-grid [specifications]="specs" [groupedSpecs]="true"></app-technical-specs-grid>`

### 8. Asset Table (`asset-table.component.ts`)
- **Purpose**: Reusable data table with sorting, pagination, and actions
- **Features**: Selection, bulk actions, sorting, pagination, responsive design
- **Usage**: `<app-asset-table [assets]="assets" (actionClick)="onAction($event)"></app-asset-table>`

## Design System Compliance

All components implement Editorial Geometry design principles:

- **No 1px Borders**: Use surface hierarchy and tonal transitions
- **Glassmorphism**: Backdrop blur effects for floating elements
- **Typography**: Manrope for headings, Inter for body text
- **Color Palette**: Primary (#143b7d), Secondary (#a9371d), Surface hierarchy
- **Spacing**: Editorial spacing scale with proper breathing room
- **Accessibility**: WCAG AA compliance with proper ARIA labels

## Testing

Each component includes comprehensive unit tests:
- Component creation and initialization
- Input/output behavior
- Event emissions
- Accessibility features
- Helper methods
- Edge cases and error handling

## Usage Examples

### Basic Asset Display
```typescript
// In your component template
<app-asset-icon [assetType]="asset.assetType"></app-asset-icon>
<app-asset-status-badge [status]="asset.status"></app-asset-status-badge>
```

### Asset Detail View
```typescript
// Full asset detail layout
<app-assignment-card 
  [assignedUser]="asset.assignedUser"
  [assignedUserEmail]="asset.assignedUserEmail"
  (reassignClick)="onReassign()">
</app-assignment-card>

<app-lifecycle-timeline [events]="lifecycleHistory"></app-lifecycle-timeline>

<app-quick-actions 
  [currentStatus]="asset.status"
  [isAssigned]="!!asset.assignedUser"
  (editClick)="onEdit()"
  (statusChange)="onStatusChange($event)">
</app-quick-actions>
```

### Asset List View
```typescript
// Asset inventory with filters and table
<app-asset-filters 
  [selectedAssetTypes]="filters.assetTypes"
  [selectedStatuses]="filters.statuses"
  (filtersReset)="onResetFilters()">
</app-asset-filters>

<app-asset-table 
  [assets]="assets"
  [loading]="loading"
  [showSelection]="true"
  (actionClick)="onAssetAction($event)"
  (bulkActionClick)="onBulkAction($event)">
</app-asset-table>
```

## File Structure

```
shared/
├── asset-status-badge/
│   ├── asset-status-badge.component.ts
│   └── asset-status-badge.component.spec.ts
├── asset-icon/
│   ├── asset-icon.component.ts
│   └── asset-icon.component.spec.ts
├── asset-filters/
│   ├── asset-filters.component.ts
│   └── asset-filters.component.spec.ts
├── assignment-card/
│   ├── assignment-card.component.ts
│   └── assignment-card.component.spec.ts
├── lifecycle-timeline/
│   ├── lifecycle-timeline.component.ts
│   └── lifecycle-timeline.component.spec.ts
├── quick-actions/
│   ├── quick-actions.component.ts
│   └── quick-actions.component.spec.ts
├── technical-specs-grid/
│   ├── technical-specs-grid.component.ts
│   └── technical-specs-grid.component.spec.ts
├── asset-table/
│   ├── asset-table.component.ts
│   └── asset-table.component.spec.ts
├── index.ts
└── README.md
```

## Dependencies

All components use Angular Material components and follow standalone component patterns:
- `@angular/material` - UI components
- `@angular/cdk` - Component development kit
- `@angular/common` - Common Angular directives
- `@angular/forms` - Reactive forms (where needed)

## Performance Considerations

- OnPush change detection strategy for optimal performance
- Lazy loading support through standalone components
- Minimal bundle impact with tree-shaking
- Efficient event handling with proper unsubscription
- Responsive design with CSS Grid and Flexbox