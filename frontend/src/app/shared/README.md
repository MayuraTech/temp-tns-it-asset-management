# Shared Components

This directory contains reusable Angular components used throughout the IT Asset Management application.

## Components

### HeaderComponent
**Location:** `components/header/`

Navigation header with user menu and notifications.

**Features:**
- Application branding and title
- Sidebar toggle button
- Notification badge with unread count
- User menu with profile and logout options
- Responsive design for mobile devices

**Usage:**
```html
<app-header (sidebarToggle)="onSidebarToggle()"></app-header>
```

**Outputs:**
- `sidebarToggle`: Emitted when the sidebar toggle button is clicked

---

### SidebarComponent
**Location:** `components/sidebar/`

Navigation sidebar with role-based menu items.

**Features:**
- Role-based menu filtering
- Active route highlighting
- Material Design icons
- Responsive navigation

**Usage:**
```html
<app-sidebar></app-sidebar>
```

**Menu Items:**
- Dashboard (All roles)
- Assets (All roles)
- My Requests (All roles)
- Ticket Management (Administrator, Asset_Manager)
- Reports (Administrator, Asset_Manager)
- Users (Administrator only)
- Audit Logs (Administrator only)
- Settings (Administrator only)

---

### FooterComponent
**Location:** `components/footer/`

Application footer with copyright and version information.

**Features:**
- Copyright notice
- Version display
- Responsive layout

**Usage:**
```html
<app-footer></app-footer>
```

---

### LoadingSpinnerComponent
**Location:** `components/loading-spinner/`

Loading indicator with optional overlay and message.

**Features:**
- Configurable spinner size
- Optional loading message
- Full-screen overlay mode
- Material Design spinner

**Usage:**
```html
<!-- Basic spinner -->
<app-loading-spinner></app-loading-spinner>

<!-- Custom size and message -->
<app-loading-spinner 
  [diameter]="60" 
  message="Loading assets...">
</app-loading-spinner>

<!-- Full-screen overlay -->
<app-loading-spinner 
  [overlay]="true" 
  message="Processing...">
</app-loading-spinner>
```

**Inputs:**
- `diameter`: Spinner diameter in pixels (default: 50)
- `message`: Loading message text (default: 'Loading...')
- `overlay`: Enable full-screen overlay (default: false)

---

### ConfirmationDialogComponent
**Location:** `components/confirmation-dialog/`

Modal dialog for user confirmations.

**Features:**
- Customizable title and message
- Configurable button text
- Type-based styling (info, warning, danger)
- Material Design dialog

**Usage:**
```typescript
import { MatDialog } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from '@shared/components';

constructor(private dialog: MatDialog) {}

openConfirmDialog(): void {
  const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
    width: '400px',
    data: {
      title: 'Delete Asset',
      message: 'Are you sure you want to delete this asset? This action cannot be undone.',
      confirmText: 'Delete',
      cancelText: 'Cancel',
      type: 'danger'
    }
  });

  dialogRef.afterClosed().subscribe(result => {
    if (result) {
      // User confirmed
    }
  });
}
```

**Data Interface:**
```typescript
interface ConfirmationDialogData {
  title: string;
  message: string;
  confirmText?: string;  // default: 'Confirm'
  cancelText?: string;   // default: 'Cancel'
  type?: 'info' | 'warning' | 'danger';  // default: 'info'
}
```

---

### StatusBadgeComponent
**Location:** `components/status-badge/`

Colored badge for displaying asset and ticket statuses.

**Features:**
- Asset status display (ordered, received, deployed, in_use, maintenance, storage, retired)
- Ticket status display (pending, approved, rejected, in_progress, completed, cancelled)
- Color-coded badges
- Automatic text formatting

**Usage:**
```html
<!-- Asset status -->
<app-status-badge 
  [status]="asset.status" 
  type="asset">
</app-status-badge>

<!-- Ticket status -->
<app-status-badge 
  [status]="ticket.status" 
  type="ticket">
</app-status-badge>
```

**Inputs:**
- `status`: Status value (required)
- `type`: 'asset' or 'ticket' (default: 'asset')

**Status Colors:**

Asset Statuses:
- Ordered: Blue
- Received: Purple
- Deployed/In Use: Green
- Maintenance: Orange
- Storage: Pink
- Retired: Gray

Ticket Statuses:
- Pending: Yellow
- Approved: Green
- Rejected: Red
- In Progress: Blue
- Completed: Green
- Cancelled: Gray

---

### NotificationBadgeComponent
**Location:** `components/notification-badge/`

Badge component for displaying notification counts.

**Features:**
- Unread count display
- Maximum count with overflow indicator (99+)
- Auto-hide when count is zero
- Customizable icon and color

**Usage:**
```html
<!-- Basic notification badge -->
<app-notification-badge [count]="unreadCount"></app-notification-badge>

<!-- Custom icon and color -->
<app-notification-badge 
  [count]="messageCount"
  icon="mail"
  color="primary"
  [max]="999">
</app-notification-badge>
```

**Inputs:**
- `count`: Number of notifications (default: 0)
- `max`: Maximum count before showing overflow (default: 99)
- `icon`: Material icon name (default: 'notifications')
- `color`: Badge color - 'primary', 'accent', or 'warn' (default: 'warn')

---

## Styling Guidelines

All shared components follow these styling principles:

1. **Material Design**: Components use Angular Material for consistent UI
2. **Responsive**: All components adapt to mobile, tablet, and desktop screens
3. **Theming**: Components respect the application's Material theme
4. **Accessibility**: Components follow WCAG guidelines for accessibility

## Responsive Breakpoints

- **Mobile**: < 768px
- **Tablet**: 768px - 1024px
- **Desktop**: > 1024px

## Dependencies

All shared components are standalone and import their required Angular Material modules directly. No shared module is needed.

Required Angular Material modules:
- `MatToolbarModule`
- `MatButtonModule`
- `MatIconModule`
- `MatMenuModule`
- `MatBadgeModule`
- `MatSidenavModule`
- `MatListModule`
- `MatProgressSpinnerModule`
- `MatDialogModule`
- `MatChipsModule`

## Integration with Core Services

Shared components integrate with core services:

- **HeaderComponent**: Uses `AuthService` for authentication state
- **SidebarComponent**: Uses `AuthService` for role-based menu filtering
- **LoadingSpinnerComponent**: Can be controlled by `LoadingService`

## Future Enhancements

Potential improvements for shared components:

1. Add animation transitions
2. Implement dark mode support
3. Add more customization options
4. Create additional reusable components (breadcrumbs, pagination, etc.)
5. Add internationalization (i18n) support
