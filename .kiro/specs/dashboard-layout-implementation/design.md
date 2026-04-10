# Design Document: Dashboard Layout Implementation

## Overview

This design document outlines the technical architecture and implementation approach for the AssetIntel IT Asset Management Dashboard Layout based on the **Editorial Geometry** design system. The dashboard provides a comprehensive application shell with fixed sidebar navigation, top navigation bar, and main content area that serves as the foundation for all feature modules in the application.

The implementation follows the **"Corporate Curator"** philosophy, treating digital space like a premium printed journal. It establishes a consistent visual identity through intentional asymmetry, geometric triangle accents, and tonal layering while maintaining navigation structure and responsive layout patterns using Angular 17+ with component-based CSS architecture.

## Design Philosophy: Editorial Geometry

### Creative North Star: "The Corporate Curator"
This design system moves away from rigid, boxy constraints of traditional corporate dashboards toward a **High-End Editorial** experience. The Creative North Star treats digital space like a premium printed journal, blending professional authority with creative soul.

Key principles:
- **Intentional Asymmetry**: Large-scale geometric triangles break the grid and act as visual anchors
- **Depth Through Layering**: Deep blue text blocks partially cover soft background shapes for physical depth
- **Architectural Intent**: Bespoke rather than templated feeling through overlapping elements
- **Premium Editorial Feel**: No harsh lines, using background color shifts for boundaries

## Architecture

### High-Level Architecture

The dashboard layout follows a hierarchical component architecture with Editorial Geometry principles:

```
AppShellComponent (Root Layout Container)
├── SidebarComponent (Fixed Left Navigation with Geometric Accents)
│   ├── BrandHeaderComponent (Logo with Triangle Background)
│   ├── NavigationMenuComponent (Tonal Layering)
│   └── ActionButtonComponent (Glassmorphism Effect)
├── TopNavigationComponent (Glassmorphism Navigation Bar)
│   ├── SearchBarComponent (Ghost Border Styling)
│   ├── SecondaryNavigationComponent (Editorial Typography)
│   └── UserControlsComponent (Geometric Hover Effects)
└── MainContentComponent (Dynamic Content with Triangle Accents)
    └── <router-outlet> (Feature Module Content)
```

### Layout Strategy

The layout uses CSS Grid for the main application shell structure with Editorial Geometry enhancements:
- **Fixed sidebar** (256px width) with geometric triangle accents
- **Glassmorphism top navigation** (64px height) with backdrop blur
- **Tonal layered main content** area with surface hierarchy
- **No-line rule**: Boundaries defined through background color shifts
- **Responsive behavior** maintaining editorial feel at minimum 1024px viewport width

### Surface Hierarchy & Nesting

Following Editorial Geometry principles, treat the UI as stacked materials:
- **Base Layer**: `surface` (#faf9ff) - Light purple background
- **Content Blocks**: `surface-container` (#eeedf4) - Elevated content areas  
- **Elevated Cards**: `surface-container-lowest` (#ffffff) - Pop effect against darker containers
- **Glass Elements**: `surface` at 70% opacity with `backdrop-filter: blur(12px)`

### State Management

Navigation state is managed through:
- Angular Router for route-based active states with geometric accent indicators
- RxJS observables for reactive navigation updates
- Component-level state for UI interactions (search, user controls)
- Geometric triangle positioning based on active states

## Components and Interfaces

### AppShellComponent

**Purpose**: Root layout container that orchestrates the overall dashboard structure

**Interface**:
```typescript
interface AppShellComponent {
  // Properties
  currentRoute$: Observable<string>;
  isAuthenticated$: Observable<boolean>;
  
  // Methods
  ngOnInit(): void;
  ngOnDestroy(): void;
}
```

**Responsibilities**:
- Render fixed layout structure using CSS Grid
- Manage router outlet for feature modules
- Coordinate navigation state between child components
- Handle responsive behavior and viewport changes

### SidebarComponent

**Purpose**: Fixed left navigation panel with brand identity and primary navigation

**Interface**:
```typescript
interface SidebarComponent {
  // Properties
  navigationItems: NavigationItem[];
  activeRoute$: Observable<string>;
  
  // Methods
  onNavigationClick(item: NavigationItem): void;
  onActionButtonClick(): void;
  isActiveRoute(route: string): boolean;
}

interface NavigationItem {
  id: string;
  label: string;
  route: string;
  icon: string;
  type: 'primary' | 'secondary';
  order: number;
}
```

**Responsibilities**:
- Display brand header with logo and application name
- Render primary navigation items (Assets, Software, Licenses, Network, Users)
- Show "Add New Asset" action button
- Display secondary navigation items (Audit Logs, Archived)
- Manage active state visualization with red accent border

### TopNavigationComponent

**Purpose**: Horizontal navigation bar with search functionality and user controls

**Interface**:
```typescript
interface TopNavigationComponent {
  // Properties
  searchQuery$: BehaviorSubject<string>;
  secondaryNavItems: SecondaryNavItem[];
  userInfo$: Observable<UserInfo>;
  
  // Methods
  onSearchInput(query: string): void;
  onSecondaryNavClick(item: SecondaryNavItem): void;
  onUserControlClick(action: UserAction): void;
}

interface SecondaryNavItem {
  id: string;
  label: string;
  route: string;
  active: boolean;
}

interface UserAction {
  type: 'notification' | 'settings' | 'profile';
}
```

**Responsibilities**:
- Display brand name and search functionality
- Render secondary navigation links (Dashboard, Inventory, Reports, Settings)
- Show user controls (notifications, settings, avatar)
- Apply backdrop blur effect and subtle shadow
- Handle search input with debouncing

### MainContentComponent

**Purpose**: Flexible content area for feature-specific modules with content projection

**Interface**:
```typescript
interface MainContentComponent {
  // Properties
  isEmpty$: Observable<boolean>;
  
  // Methods
  ngOnInit(): void;
  showEmptyState(): void;
}
```

**Responsibilities**:
- Provide content projection slot for feature modules
- Display empty state when no content is loaded
- Apply background styling with decorative elements
- Handle vertical scrolling for overflow content

## Data Models

### Design Token System

```typescript
// design-tokens.ts
export const DesignTokens = {
  colors: {
    primary: {
      blue900: '#1e3a8a',
      blue800: '#143b7d',
      blue700: '#315396'
    },
    accent: {
      red800: '#991b1b',
      secondary: '#a9371d',        // Editorial accent color
      tertiary: '#80002b'          // Sophisticated softness
    },
    neutral: {
      slate50: '#f8fafc',
      slate100: '#f1f5f9',
      slate500: '#64748b',
      slate600: '#475569',
      gray400: '#6b7280'
    },
    background: {
      surface: '#faf9ff',                    // Base layer
      surfaceContainer: '#eeedf4',           // Content blocks
      surfaceContainerLow: '#f4f3f9',        // Subtle elevation
      surfaceContainerLowest: '#ffffff',     // Pop effect cards
      surfaceContainerHigh: '#e8e7ee',       // Higher elevation
      surfaceContainerHighest: '#e3e2e8',    // Highest elevation
      surfaceTint: '#3b5da0'                 // Image overlay tint
    },
    editorial: {
      onSurface: '#1a1b20',                  // Never pure black
      onSurfaceVariant: '#434750',           // Subtle text
      outline: '#747782',                    // Standard outline
      outlineVariant: '#c4c6d2'              // Ghost borders
    },
    white: '#ffffff'
  },
  
  spacing: {
    xs: '4px',
    sm: '8px',
    md: '12px',
    lg: '16px',
    xl: '24px',
    xxl: '32px',
    xxxl: '40px',
    editorial: '80px'                        // Geometric accent breathing room
  },
  
  borderRadius: {
    sm: '4px',
    md: '8px',                               // Editorial standard
    lg: '12px'
  },
  
  typography: {
    fontFamilies: {
      heading: 'Manrope, sans-serif',         // Geometric precision
      body: 'Inter, sans-serif'               // Readability workhorse
    },
    fontSizes: {
      xs: '12px',
      sm: '14px',
      md: '16px',
      lg: '18px',
      xl: '20px',
      xxl: '30px',
      displayLg: '48px'                      // Editorial hero statements
    },
    fontWeights: {
      regular: 400,
      medium: 500,
      bold: 700,
      extraBold: 800                         // Editorial headlines
    },
    lineHeights: {
      tight: '16px',
      normal: '20px',
      relaxed: '22.5px',
      loose: '24px',
      extraLoose: '28px',
      spacious: '36px'
    },
    letterSpacing: {
      tight: '-0.75px',                      // Editorial headlines
      normal: '-0.5px',
      wide: '0.3px',
      editorial: '-2%'                       // Hero statements
    }
  },
  
  shadows: {
    subtle: '0px 1px 2px 0px rgba(30,58,138,0.05)',
    button: '0px 10px 15px -3px rgba(20,59,125,0.2), 0px 4px 6px -4px rgba(20,59,125,0.2)',
    editorial: '0 20px 40px rgba(20, 59, 125, 0.06)',  // Blue-tinted ambient shadows
    glass: 'backdrop-filter: blur(12px)'                // Glassmorphism effect
  },
  
  layout: {
    sidebarWidth: '256px',
    topNavHeight: '64px',
    contentPadding: '32px'
  },
  
  editorial: {
    triangleBreathingRoom: '80px',           // Minimum space around geometric accents
    glassOpacity: '70%',                     // Glassmorphism transparency
    ghostBorderOpacity: '15%',               // Subtle border suggestions
    surfaceTintOpacity: '5%'                 // Image overlay tint
  }
} as const;
```

### Navigation Configuration

```typescript
// navigation.config.ts
export interface NavigationConfig {
  primary: NavigationItem[];
  secondary: NavigationItem[];
  topNav: SecondaryNavItem[];
}

export const NAVIGATION_CONFIG: NavigationConfig = {
  primary: [
    { id: 'assets', label: 'Assets', route: '/assets', icon: 'assets-icon', type: 'primary', order: 1 },
    { id: 'software', label: 'Software', route: '/software', icon: 'software-icon', type: 'primary', order: 2 },
    { id: 'licenses', label: 'Licenses', route: '/licenses', icon: 'licenses-icon', type: 'primary', order: 3 },
    { id: 'network', label: 'Network', route: '/network', icon: 'network-icon', type: 'primary', order: 4 },
    { id: 'users', label: 'Users', route: '/users', icon: 'users-icon', type: 'primary', order: 5 }
  ],
  secondary: [
    { id: 'audit-logs', label: 'Audit Logs', route: '/audit-logs', icon: 'audit-icon', type: 'secondary', order: 1 },
    { id: 'archived', label: 'Archived', route: '/archived', icon: 'archive-icon', type: 'secondary', order: 2 }
  ],
  topNav: [
    { id: 'dashboard', label: 'Dashboard', route: '/dashboard', active: false },
    { id: 'inventory', label: 'Inventory', route: '/inventory', active: false },
    { id: 'reports', label: 'Reports', route: '/reports', active: false },
    { id: 'settings', label: 'Settings', route: '/settings', active: false }
  ]
};
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

Based on the prework analysis, the following properties have been identified as suitable for property-based testing:

### Property 1: Layout Structure Persistence

*For any* application route navigation sequence, the layout structure SHALL remain consistent with sidebar, top navigation, and main content areas maintaining their positions and dimensions.

**Validates: Requirements 1.4**

### Property 2: Responsive Content Area Filling

*For any* viewport height that exceeds content height, the main content area SHALL fill the available vertical space without gaps or overflow issues.

**Validates: Requirements 1.5**

### Property 3: Navigation Active State Consistency

*For any* current route, exactly one navigation item in the sidebar SHALL display the active state with red border and bold text styling.

**Validates: Requirements 2.8**

### Property 4: Navigation Item Active State Styling

*For any* navigation item in active state, it SHALL display a 4px red right border (#991b1b) and bold text with color #991b1b.

**Validates: Requirements 3.1, 3.2**

### Property 5: Navigation Item Inactive State Styling

*For any* navigation item in inactive state, it SHALL display medium weight text with color #475569 and no border accent.

**Validates: Requirements 3.3**

### Property 6: Navigation Item Icon Consistency

*For any* navigation item, it SHALL display an icon with dimensions 20x20px positioned to the left of the text with 12px gap.

**Validates: Requirements 3.4, 3.6**

### Property 7: Navigation Item Padding Consistency

*For any* navigation item, it SHALL apply 12px vertical padding and 16px horizontal padding consistently.

**Validates: Requirements 3.5**

### Property 8: Navigation Item Font Consistency

*For any* inactive navigation item, it SHALL use Inter Medium font at 14px size, and for any active navigation item, it SHALL use Inter Bold font at 14px size.

**Validates: Requirements 3.7, 3.8**

### Property 9: Top Navigation Active State Styling

*For any* active top navigation link, it SHALL display a 2px red bottom border (#991b1b) and bold text with color #1e3a8a.

**Validates: Requirements 6.2, 6.3**

### Property 10: Top Navigation Inactive State Styling

*For any* inactive top navigation link, it SHALL display regular weight text with color #64748b and no bottom border.

**Validates: Requirements 6.4**

### Property 11: Top Navigation Font Consistency

*For any* inactive top navigation link, it SHALL use Inter Regular font at 14px size, and for any active link, it SHALL use Inter Bold font at 14px size.

**Validates: Requirements 6.5, 6.6**

### Property 12: Top Navigation Padding Consistency

*For any* top navigation link, it SHALL apply 6px bottom padding to accommodate the active border.

**Validates: Requirements 6.7**

### Property 13: Search Bar Input Functionality

*For any* text input in the search bar, the search bar SHALL accept and display the input correctly while maintaining placeholder behavior when empty.

**Validates: Requirements 5.6**

### Property 14: Content Area Scrolling Behavior

*For any* content height that exceeds the viewport height, the main content area SHALL enable vertical scrolling without affecting the fixed sidebar and top navigation positioning.

**Validates: Requirements 8.5**

### Property 15: Keyboard Navigation Order

*For any* current focus position on an interactive element, pressing Tab SHALL move focus to the next logical interactive element in the defined tab order.

**Validates: Requirements 14.1**

### Property 16: Reverse Keyboard Navigation

*For any* current focus position on an interactive element, pressing Shift+Tab SHALL move focus to the previous interactive element in the defined tab order.

**Validates: Requirements 14.2**

### Property 17: Focus Indicator Visibility

*For any* focused navigation item, a visible focus indicator SHALL be displayed that meets WCAG 2.1 Level AA contrast requirements.

**Validates: Requirements 14.3, 14.7**

### Property 18: Keyboard Activation

*For any* focused navigation item or button, pressing Enter or Space key SHALL trigger the appropriate navigation or action.

**Validates: Requirements 14.4, 14.6**

### Property 19: Screen Reader Accessibility

*For any* navigation item, screen reader technology SHALL announce the item correctly with appropriate role and state information.

**Validates: Requirements 15.9**

<function_calls>
<invoke name="prework">
<parameter name="featureName">dashboard-layout-implementation

## Error Handling

### Component Error Boundaries

The dashboard layout implements comprehensive error handling to ensure graceful degradation:

**Navigation Errors**:
- Invalid route navigation attempts are caught and redirected to a default route
- Missing navigation icons fall back to a generic icon placeholder
- Route guard failures display appropriate error messages

**Search Functionality Errors**:
- Search input validation prevents malformed queries
- Search service failures display user-friendly error messages
- Debounced search prevents excessive API calls

**Layout Rendering Errors**:
- CSS Grid fallback to Flexbox for older browser support
- Missing design tokens fall back to default values
- Component initialization failures are logged and reported

### Error Recovery Strategies

```typescript
// Error handling service
@Injectable({
  providedIn: 'root'
})
export class LayoutErrorService {
  handleNavigationError(error: NavigationError): void {
    console.error('Navigation error:', error);
    this.router.navigate(['/dashboard']);
    this.notificationService.showError('Navigation failed. Redirected to dashboard.');
  }
  
  handleSearchError(error: SearchError): void {
    console.error('Search error:', error);
    this.notificationService.showError('Search temporarily unavailable.');
  }
  
  handleLayoutError(error: LayoutError): void {
    console.error('Layout error:', error);
    // Attempt to re-initialize layout components
    this.layoutService.reinitialize();
  }
}
```

### Accessibility Error Prevention

- Focus trap implementation prevents focus from escaping modal dialogs
- ARIA attributes are validated at build time
- Color contrast ratios are verified programmatically
- Keyboard navigation paths are tested automatically

## Testing Strategy

### Dual Testing Approach

The dashboard layout implementation uses a comprehensive testing strategy combining:

**Unit Tests**: Verify specific component behavior, styling, and interactions
- Component rendering and initialization
- Event handling and user interactions  
- CSS class application and computed styles
- Angular service integration

**Property-Based Tests**: Verify universal properties across randomized inputs
- Navigation state consistency across route changes
- Layout responsiveness across viewport sizes
- Keyboard navigation behavior across focus states
- Accessibility compliance across component states

### Property-Based Testing Configuration

**Framework**: fast-check for TypeScript/Angular
**Minimum Iterations**: 100 runs per property test
**Test Tagging**: Each property test references its design document property

Example property test structure:
```typescript
describe('Feature: dashboard-layout-implementation', () => {
  describe('Property 1: Layout Structure Persistence', () => {
    it('should maintain layout structure across route navigation', () => {
      fc.assert(
        fc.property(
          routeSequenceGenerator(),
          (routes: string[]) => {
            // Test implementation
            routes.forEach(route => {
              router.navigate([route]);
              fixture.detectChanges();
              
              // Verify layout structure remains intact
              expect(getSidebarWidth()).toBe('256px');
              expect(getTopNavHeight()).toBe('64px');
              expect(getMainContentPosition()).toEqual({
                left: '256px',
                top: '64px'
              });
            });
          }
        ),
        { numRuns: 100 }
      );
    });
  });
});
```

### Unit Testing Strategy

**Component Testing**:
- Isolated component rendering with TestBed
- Mock dependencies and services
- Verify DOM structure and styling
- Test user interactions and event handling

**Integration Testing**:
- Router integration with navigation components
- Service integration with layout state management
- Cross-component communication testing

**Visual Regression Testing**:
- Snapshot testing for layout consistency
- Cross-browser rendering verification
- Responsive design validation

### Accessibility Testing

**Automated Testing**:
- axe-core integration for WCAG compliance
- Keyboard navigation path verification
- Screen reader compatibility testing
- Color contrast ratio validation

**Manual Testing**:
- Screen reader testing with NVDA/JAWS
- Keyboard-only navigation testing
- High contrast mode verification
- Zoom level testing (up to 200%)

### Performance Testing

**Metrics Tracking**:
- Initial render time < 2 seconds
- Navigation transition time < 300ms
- Memory usage monitoring
- Bundle size optimization

**Load Testing**:
- Component initialization under load
- Route navigation performance
- Search functionality responsiveness

### Browser Compatibility Testing

**Supported Browsers**:
- Chrome 90+ (primary target)
- Firefox 88+ (secondary target)
- Safari 14+ (secondary target)
- Edge 90+ (secondary target)

**Feature Detection**:
- CSS Grid support detection with Flexbox fallback
- Backdrop filter support with opacity fallback
- Custom properties support with static fallbacks

### Test Coverage Requirements

- **Unit Test Coverage**: Minimum 85% line coverage for layout components
- **Property Test Coverage**: All 19 correctness properties must have tests
- **Integration Test Coverage**: All navigation and routing scenarios
- **Accessibility Test Coverage**: All interactive elements and navigation paths
- **Visual Regression Coverage**: All component states and responsive breakpoints

The testing strategy ensures the dashboard layout meets all functional requirements while maintaining high quality, accessibility, and performance standards across supported browsers and devices.