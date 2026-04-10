# Dashboard Stats Component

A standalone Angular component that displays real-time asset statistics for dashboard use.

## Features

- **Real-time Updates**: Automatically refreshes every 30 seconds
- **Quick Statistics**: Shows Total Assets, Assets In Use, and Assets Available
- **Loading States**: Displays spinner during data loading
- **Error Handling**: Shows error messages with retry functionality
- **Editorial Geometry Design**: Follows design system standards with glassmorphism and geometric accents
- **Responsive Design**: Adapts to different screen sizes
- **Accessibility**: WCAG compliant with proper ARIA labels

## Usage

### Basic Usage

```typescript
import { DashboardStatsComponent } from './dashboard-stats/dashboard-stats.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DashboardStatsComponent],
  template: `
    <div class="dashboard-container">
      <app-dashboard-stats></app-dashboard-stats>
    </div>
  `
})
export class DashboardComponent {}
```

### In Asset Inventory

The component is designed to be used in the asset inventory screen as a quick stats widget:

```html
<!-- Asset Inventory Template -->
<div class="inventory-layout">
  <!-- Header with stats widget -->
  <div class="inventory-header">
    <h1>Asset Inventory</h1>
    <app-dashboard-stats></app-dashboard-stats>
  </div>
  
  <!-- Asset table and other content -->
  <div class="inventory-content">
    <!-- ... -->
  </div>
</div>
```

## API Integration

The component automatically calls the `AssetService.getAssetStats()` method which makes a GET request to `/api/v1/assets/stats`.

### Expected API Response

```json
{
  "totalAssets": 150,
  "assetsInUse": 120,
  "lastUpdated": "2024-01-15T10:30:00"
}
```

### Calculated Fields

The component automatically calculates:
- **Assets Available**: `totalAssets - assetsInUse`
- **Usage Percentage**: `(assetsInUse / totalAssets) * 100`

## Styling

The component follows Editorial Geometry design system principles:

- **Surface Hierarchy**: Uses `surface-container-low` background with `surface-container-lowest` cards
- **Typography**: Manrope for numbers/statistics, Inter for labels
- **Colors**: Primary blue for icons, secondary red-orange for title
- **Geometric Accents**: Subtle triangle accent in top-right corner
- **Glassmorphism**: Applied to last updated info section

## Accessibility

- Proper semantic HTML structure
- ARIA labels for interactive elements
- Screen reader friendly content
- High contrast mode support
- Keyboard navigation support

## Performance

- **Real-time Updates**: 30-second refresh interval
- **Efficient Queries**: Backend uses COUNT queries for performance
- **Change Detection**: OnPush strategy for optimal performance
- **Memory Management**: Proper subscription cleanup on destroy

## Testing

The component includes comprehensive unit tests covering:

- Component initialization and lifecycle
- Stats calculations (available assets, usage percentage)
- Loading and error states
- Real-time update intervals
- Template integration
- Accessibility features

Run tests with:
```bash
npm test -- --testNamePattern="DashboardStatsComponent"
```

## Requirements Fulfilled

- **Requirement 22**: Dashboard and Quick Stats
  - ✅ Displays Total Assets, Assets In Use, Assets Available
  - ✅ Real-time updates every 30 seconds
  - ✅ Efficient database aggregation queries
  - ✅ Performance target < 500ms response time

## Browser Support

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

Includes fallbacks for:
- CSS backdrop-filter (glassmorphism)
- CSS Grid (flexbox fallback)
- Reduced motion preferences