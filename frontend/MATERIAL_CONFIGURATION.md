# Angular Material Configuration

This document describes the Angular Material configuration for the IT Infrastructure Asset Management application.

## Overview

Angular Material has been configured with a custom theme, responsive breakpoints, and all required UI components for the application.

**Requirements Addressed:** 17.1, 17.2, 17.3, 17.4, 17.5

## Installation

Angular Material and CDK are already installed:

```json
"@angular/material": "^17.0.0",
"@angular/cdk": "^17.0.0"
```

## Theme Configuration

### Custom Theme (`src/theme.scss`)

A custom Material theme has been created with:

- **Primary Color**: Blue (#1e88e5) - Professional and trustworthy
- **Accent Color**: Pink (#e91e63) - Highlights and call-to-action elements
- **Warn Color**: Red (#f44336) - Errors and warnings

The theme includes:
- Light theme (default)
- Dark theme (`.dark-theme` class)
- Custom status badge colors for asset lifecycle states
- Custom priority badge colors for tickets

### Status Badge Colors

Asset lifecycle statuses:
- **Ordered**: Light blue background
- **Received**: Light purple background
- **Deployed**: Light green background
- **In Use**: Dark green background
- **Maintenance**: Orange background
- **Storage**: Pink background
- **Retired**: Gray background

Ticket statuses:
- **Pending**: Orange background
- **Approved**: Green background
- **Rejected**: Red background
- **In Progress**: Blue background
- **Completed**: Dark green background
- **Cancelled**: Gray background

Priority levels:
- **Low**: Green background
- **Medium**: Orange background
- **High**: Red background
- **Urgent**: Dark red background with bold text

## Responsive Breakpoints (`src/breakpoints.scss`)

### Breakpoint Values

- **Mobile**: 320px - 767px
- **Tablet**: 768px - 1279px
- **Desktop**: 1280px+
- **Desktop Large**: 1920px+

### Responsive Mixins

```scss
@include mobile { /* Mobile styles */ }
@include mobile-only { /* Mobile only */ }
@include tablet { /* Tablet styles */ }
@include tablet-up { /* Tablet and above */ }
@include desktop { /* Desktop styles */ }
@include desktop-large { /* Large desktop styles */ }
@include touch-friendly { /* Touch-friendly controls */ }
```

### Responsive Features

1. **Container System**: Responsive max-widths with appropriate padding
2. **Grid System**: 12-column flexible grid with responsive column classes
3. **Responsive Tables**: Card-based layout on mobile, table layout on larger screens
4. **Touch-Friendly Controls**: Minimum 48x48px touch targets on mobile/tablet
5. **Responsive Forms**: Stacked on mobile, side-by-side on tablet+
6. **Responsive Dialogs**: Full-screen on mobile, centered on larger screens
7. **Utility Classes**: Hide/show elements based on screen size

### Usage Examples

#### Responsive Grid

```html
<div class="row">
  <div class="col-mobile-12 col-tablet-6 col-desktop-4">
    <!-- Content -->
  </div>
</div>
```

#### Responsive Table

```html
<div class="table-responsive">
  <table mat-table [dataSource]="dataSource">
    <!-- Table content -->
  </table>
</div>
```

#### Responsive Form

```html
<form class="form-responsive">
  <mat-form-field>
    <input matInput placeholder="Name">
  </mat-form-field>
  <mat-form-field>
    <input matInput placeholder="Email">
  </mat-form-field>
</form>
```

## Material Module (`src/app/shared/material.module.ts`)

A centralized Material module exports all required components:

### Layout Components
- Toolbar
- Sidenav
- List
- Card
- Divider
- Grid List

### Table Components
- Table
- Paginator
- Sort

### Form Components
- Form Field
- Input
- Select
- Datepicker
- Checkbox
- Radio
- Slide Toggle
- Autocomplete
- Slider
- Button Toggle

### Button & Indicator Components
- Button
- Icon
- Badge
- Chips
- Progress Spinner
- Progress Bar
- Ripple

### Popup & Modal Components
- Dialog
- Snack Bar
- Tooltip
- Menu

### Navigation Components
- Tabs
- Expansion Panel
- Stepper

## Usage in Components

### Importing Material Module

```typescript
import { MaterialModule } from '@shared/material.module';

@Component({
  selector: 'app-example',
  standalone: true,
  imports: [MaterialModule],
  // ...
})
export class ExampleComponent { }
```

### Using Material Components

```typescript
import { Component } from '@angular/core';
import { MaterialModule } from '@shared/material.module';

@Component({
  selector: 'app-asset-list',
  standalone: true,
  imports: [MaterialModule],
  template: `
    <mat-card>
      <mat-card-header>
        <mat-card-title>Assets</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <table mat-table [dataSource]="assets">
          <!-- Table columns -->
        </table>
      </mat-card-content>
    </mat-card>
  `
})
export class AssetListComponent { }
```

### Using Status Badges

```html
<span class="status-badge in-use">In Use</span>
<span class="status-badge pending">Pending</span>
<span class="priority-badge high">High</span>
```

## Global Styles (`src/styles.scss`)

The main stylesheet imports the theme and breakpoints, and provides utility classes:

### Spacing Utilities
- Margin: `.mt-1` to `.mt-4`, `.mb-1` to `.mb-4`, etc.
- Padding: `.p-1` to `.p-4`, `.pt-1` to `.pt-4`, etc.

### Flexbox Utilities
- `.d-flex`, `.flex-column`, `.flex-row`
- `.justify-content-*`, `.align-items-*`
- `.flex-grow-1`, `.flex-shrink-0`

### Display Utilities
- `.d-none`, `.d-block`, `.d-inline`, `.d-inline-block`

### Text Utilities
- `.text-center`, `.text-left`, `.text-right`
- `.text-primary`, `.text-accent`, `.text-warn`, `.text-muted`
- `.font-weight-*`

### Background Utilities
- `.bg-white`, `.bg-light`, `.bg-primary`, `.bg-accent`, `.bg-warn`

### Border & Shadow Utilities
- `.border`, `.border-top`, `.border-bottom`, etc.
- `.rounded`, `.rounded-lg`
- `.shadow-sm`, `.shadow-md`, `.shadow-lg`

## Dark Theme Support

To enable dark theme, add the `.dark-theme` class to a parent element:

```html
<body class="dark-theme">
  <!-- App content -->
</body>
```

Or toggle dynamically:

```typescript
toggleDarkTheme() {
  document.body.classList.toggle('dark-theme');
}
```

## Accessibility

All Material components follow WCAG 2.1 Level AA guidelines:

- Proper ARIA labels and roles
- Keyboard navigation support
- Focus indicators
- Color contrast ratios meet standards
- Screen reader support

## Performance Considerations

1. **Tree Shaking**: Only imported Material components are included in the bundle
2. **Lazy Loading**: Material module can be imported per feature module
3. **OnPush Change Detection**: Use with Material components for better performance
4. **Virtual Scrolling**: Use `cdk-virtual-scroll-viewport` for large lists

## Browser Support

Angular Material 17 supports:

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)
- Mobile browsers (iOS Safari, Chrome Mobile)

## Testing

Material components are fully testable with Jasmine/Karma:

```typescript
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MaterialModule } from '@shared/material.module';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('ExampleComponent', () => {
  let component: ExampleComponent;
  let fixture: ComponentFixture<ExampleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ExampleComponent,
        MaterialModule,
        NoopAnimationsModule // Disable animations in tests
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ExampleComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
```

## Resources

- [Angular Material Documentation](https://material.angular.io/)
- [Material Design Guidelines](https://material.io/design)
- [Angular CDK Documentation](https://material.angular.io/cdk/categories)
- [Material Icons](https://fonts.google.com/icons)

## Next Steps

1. Create feature-specific components using Material components
2. Implement responsive layouts using the grid system
3. Use status badges for asset and ticket displays
4. Implement forms with Material form controls
5. Add dialogs for confirmations and data entry
6. Use snack bars for notifications
7. Implement data tables with sorting and pagination
