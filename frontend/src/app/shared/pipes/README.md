# Shared Pipes

This directory contains reusable Angular pipes for the IT Asset Management application.

## Available Pipes

### DateFormatPipe

Provides consistent date formatting across the application.

**Usage:**
```typescript
import { DateFormatPipe } from '@app/shared';

// In template
{{ asset.acquisitionDate | dateFormat }}
{{ asset.acquisitionDate | dateFormat:'short' }}
{{ asset.acquisitionDate | dateFormat:'long' }}
```

**Formats:**
- `short`: M/d/yy (e.g., 1/15/24)
- `medium`: MMM d, y (e.g., Jan 15, 2024) - default
- `long`: MMMM d, y (e.g., January 15, 2024)
- `full`: EEEE, MMMM d, y (e.g., Monday, January 15, 2024)

**Features:**
- Handles both Date objects and ISO string dates
- Returns empty string for null/undefined/invalid dates
- Consistent formatting across the application

### StatusColorPipe

Maps status values to CSS color classes or hex values for consistent status badge styling.

**Usage:**
```typescript
import { StatusColorPipe } from '@app/shared';

// In template - CSS class
<span [class]="asset.status | statusColor">{{ asset.status }}</span>

// In template - Hex color
<span [style.color]="asset.status | statusColor:'hex'">{{ asset.status }}</span>
```

**Supported Status Types:**
- LifecycleStatus (ORDERED, RECEIVED, IN_USE, IN_MAINTENANCE, IN_STORAGE, RETIRED, DISPOSED)
- TicketStatus (PENDING, APPROVED, REJECTED, IN_PROGRESS, COMPLETED, CANCELLED)

**Color Mappings:**
- Success (green): IN_USE, DEPLOYED, APPROVED, COMPLETED
- Warning (yellow): PENDING, IN_MAINTENANCE
- Danger (red): REJECTED
- Info (blue): ORDERED, IN_PROGRESS
- Secondary (gray): IN_STORAGE, CANCELLED
- Dark (dark gray): RETIRED, DISPOSED

## Testing

All pipes include comprehensive unit tests. Run tests with:

```bash
npm test
```

## Best Practices

1. Use pipes for presentation logic only
2. Keep pipes pure (no side effects)
3. Use standalone pipes for better tree-shaking
4. Test edge cases (null, undefined, invalid values)
