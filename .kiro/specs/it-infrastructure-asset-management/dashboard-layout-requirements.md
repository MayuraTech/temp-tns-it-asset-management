# Dashboard Layout - Requirements and Design Document

## Document Information

- **Document Type**: Requirements & Design Specification
- **Feature**: IT Asset Management Dashboard Layout
- **Version**: 1.0
- **Date**: 2024-01-15
- **Status**: Draft
- **Figma Source**: https://www.figma.com/design/EOtSDldwzxItHdzBAoImB4/Untitled?node-id=1-2

## Executive Summary

This document defines the requirements and design specifications for the IT Infrastructure Asset Management dashboard layout. The design establishes a comprehensive application shell with a fixed sidebar navigation, top navigation bar, and main content area that serves as the foundation for all feature modules.

## Design Overview

The dashboard implements a standard enterprise application layout pattern with:
- **Fixed left sidebar** (256px) for primary navigation
- **Top navigation bar** (64px) with search, secondary navigation, and user controls
- **Main content area** (flexible) for feature-specific content
- **Empty state placeholder** demonstrating the workspace canvas concept

### Visual Identity

**Brand Name**: AssetIntel  
**Tagline**: Corporate IT - Global Infrastructure  
**Design System**: Prism Logic editorial guidelines  
**Color Palette**:
- Primary: #1e3a8a (Blue 900)
- Accent: #991b1b (Red 800)
- Background: #faf9ff (Light Purple)
- Sidebar: #f8fafc (Slate 50)
- Text Primary: #1e3a8a
- Text Secondary: #64748b

**Typography**:
- Headings: Manrope (Bold, ExtraBold)
- Body: Inter (Regular, Medium, Bold)
- Logo: Manrope Bold, 20px
- Navigation: Inter Medium, 14px

## Functional Requirements

### FR-1: Application Shell Layout

**Priority**: High  
**Description**: The application must provide a consistent layout structure across all pages.

**Acceptance Criteria**:
- Fixed sidebar navigation (256px width) on the left
- Top navigation bar (64px height) spanning the remaining width
- Main content area fills remaining space
- Layout is responsive and maintains structure on different screen sizes
- Sidebar remains visible and accessible at all times

### FR-2: Sidebar Navigation

**Priority**: High  
**Description**: Left sidebar provides primary navigation for the application.

**Components**:
1. **Brand Header**
   - Logo icon (40x40px, blue background #315396)
   - Application name: "AssetIntel"
   - Subtitle: "Corporate IT - Global Infrastructure"

2. **Primary Navigation Menu**
   - Assets (default active state)
   - Software
   - Licenses
   - Network
   - Users

3. **Action Button**
   - "Add New Asset" button (primary action)
   - Blue background (#143b7d)
   - White text, uppercase
   - Full width with shadow

4. **Secondary Navigation**
   - Audit Logs
   - Archived

**Acceptance Criteria**:
- Active navigation item shows red right border (#991b1b) and bold text
- Inactive items show medium weight text in slate color
- Icons accompany each navigation item
- Hover states provide visual feedback
- Navigation items are keyboard accessible

### FR-3: Top Navigation Bar

**Priority**: High  
**Description**: Horizontal navigation bar providing search, secondary navigation, and user controls.

**Components**:
1. **Brand Name** (left section)
   - "AssetIntel" text logo

2. **Search Bar** (left-center)
   - 256px width
   - Placeholder: "Search infrastructure..."
   - Search icon on the left
   - Rounded corners (12px)
   - Light background (#f1f5f9 with 50% opacity)

3. **Secondary Navigation** (center-right)
   - Dashboard (default active)
   - Inventory
   - Reports
   - Settings
   - Active state: red bottom border (#991b1b)

4. **User Controls** (right section)
   - Notification bell icon
   - Settings/gear icon
   - User profile avatar (32x32px, circular)

**Acceptance Criteria**:
- Search bar is functional and accepts text input
- Active navigation item shows red underline
- Icons are clickable and provide visual feedback
- User avatar displays user profile image
- Navigation bar has subtle shadow and backdrop blur effect

### FR-4: Main Content Area

**Priority**: High  
**Description**: Flexible content area for displaying feature-specific modules.

**Components**:
1. **Background Elements**
   - Geometric accent shapes (decorative)
   - Subtle blur effects
   - Light purple background (#faf9ff)

2. **Empty State Display** (placeholder)
   - Icon (27x27px) in rounded background
   - Heading: "Workspace Canvas"
   - Description text explaining the purpose
   - Centered layout

**Acceptance Criteria**:
- Content area fills remaining space after sidebar and top bar
- Background elements are decorative and don't interfere with content
- Empty state is centered both horizontally and vertically
- Content area supports scrolling when content exceeds viewport

### FR-5: Responsive Behavior

**Priority**: Medium  
**Description**: Layout adapts to different screen sizes while maintaining usability.

**Acceptance Criteria**:
- Minimum supported width: 1024px
- Sidebar collapses to icon-only view on smaller screens (future enhancement)
- Top navigation adapts to available space
- Content area maintains readability at all sizes

## Non-Functional Requirements

### NFR-1: Performance

- Initial page load: < 2 seconds
- Navigation transitions: < 300ms
- Smooth scrolling in content area
- No layout shift during page load

### NFR-2: Accessibility

- WCAG 2.1 Level AA compliance
- Keyboard navigation support for all interactive elements
- Screen reader compatible
- Sufficient color contrast ratios (minimum 4.5:1 for text)
- Focus indicators visible on all interactive elements

### NFR-3: Browser Compatibility

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

### NFR-4: Visual Consistency

- Follows Prism Logic editorial guidelines
- Consistent spacing using 4px/8px grid system
- Consistent border radius (4px, 8px, 12px)
- Consistent shadow depths

## Design Specifications

### Layout Dimensions

```
Total Width: 100vw
Total Height: 100vh

Sidebar:
- Width: 256px (fixed)
- Height: 100vh
- Background: #f8fafc
- Padding: 24px vertical

Top Navigation:
- Height: 64px (fixed)
- Width: calc(100vw - 256px)
- Background: rgba(255,255,255,0.7) with backdrop blur
- Padding: 32px horizontal

Main Content:
- Width: calc(100vw - 256px)
- Height: calc(100vh - 64px)
- Background: #faf9ff
- Padding: 32px
```

### Spacing System

- Base unit: 4px
- Common spacing: 4px, 8px, 12px, 16px, 24px, 32px
- Component gaps: 12px (sidebar items), 32px (top nav sections)

### Border Radius

- Small: 4px (logo background)
- Medium: 8px (buttons)
- Large: 12px (search bar, icons, empty state background)

### Shadows

- Top navigation: 0px 1px 2px 0px rgba(30,58,138,0.05)
- Button: 0px 10px 15px -3px rgba(20,59,125,0.2), 0px 4px 6px -4px rgba(20,59,125,0.2)

### Typography Scale

```
Logo: Manrope Bold, 20px, -0.5px tracking, 28px line-height
Page Title: Manrope ExtraBold, 30px, -0.75px tracking, 36px line-height
Section Title: Manrope ExtraBold, 18px, 22.5px line-height
Navigation: Inter Medium/Bold, 14px, 20px line-height
Body: Inter Regular, 16px, 24px line-height
Small: Inter Medium, 10px, 20px line-height, 1px tracking, uppercase
Button: Inter Bold, 12px, 16px line-height, 0.3px tracking, uppercase
```

### Color Palette

```
Primary Colors:
- Blue 900: #1e3a8a (headings, active text)
- Blue 700: #315396 (logo background)
- Blue 800: #143b7d (primary button)

Accent Colors:
- Red 800: #991b1b (active indicators)
- Red 900 (5% opacity): rgba(169,55,29,0.05) (decorative)

Neutral Colors:
- Slate 50: #f8fafc (sidebar background)
- Slate 100: #f1f5f9 (input background)
- Slate 200: #e2e8f0 (borders)
- Slate 400: #94a3b8 (placeholder text)
- Slate 500: #64748b (secondary text)
- Slate 600: #475569 (inactive nav text)
- Gray 400: #6b7280 (search placeholder)
- Gray 700: #434750 (body text)

Background:
- Light Purple: #faf9ff (main background)
- Purple Tint: #f4f3f9 (decorative)
- Light Purple Tint: #eeedf4 (empty state background)

White:
- Pure White: #ffffff (text on dark backgrounds)
- White 70%: rgba(255,255,255,0.7) (top nav background)
```

## Component Specifications

### Sidebar Navigation Item

**States**:
- **Active**: Red right border (4px, #991b1b), bold text (#991b1b)
- **Inactive**: No border, medium weight text (#475569)
- **Hover**: Background highlight (future enhancement)

**Structure**:
```
[Icon 20x20px] [Text 14px Inter Medium/Bold]
Padding: 12px vertical, 16px horizontal
Gap: 12px between icon and text
```

### Top Navigation Link

**States**:
- **Active**: Red bottom border (2px, #991b1b), bold text (#1e3a8a)
- **Inactive**: No border, regular weight text (#64748b)
- **Hover**: Underline (future enhancement)

**Structure**:
```
Text: 14px Inter Regular/Bold
Padding: 6px bottom (for active state border)
```

### Search Input

**Specifications**:
- Width: 256px
- Height: auto (content-based)
- Background: rgba(241,245,249,0.5)
- Border radius: 12px
- Padding: 6px top, 7px bottom, 40px left, 16px right
- Icon: 10.5px width, positioned 13.75px from left
- Placeholder: "Search infrastructure..." (#6b7280)

### Primary Button (Add New Asset)

**Specifications**:
- Background: #143b7d
- Text: White, 12px Inter Bold, uppercase, 0.3px tracking
- Padding: 12px vertical
- Border radius: 8px
- Shadow: Multi-layer (see shadows section)
- Full width within container

### Icon Button

**Specifications**:
- Size: 40x40px
- Border radius: 12px
- Icon size: 16-20px
- Background: Transparent (hover state: light background)

### User Avatar

**Specifications**:
- Size: 32x32px
- Border: 1px solid #e2e8f0
- Border radius: 12px
- Overflow: hidden (for image)

## User Interactions

### Navigation Flow

1. **Primary Navigation** (Sidebar)
   - Click on navigation item → Navigate to corresponding page
   - Active state persists on current page
   - Default active: "Assets"

2. **Secondary Navigation** (Top Bar)
   - Click on navigation link → Navigate to corresponding section
   - Active state shows red underline
   - Default active: "Dashboard"

3. **Search**
   - Click search bar → Focus input
   - Type query → Filter/search infrastructure items
   - Press Enter → Execute search

4. **Action Button**
   - Click "Add New Asset" → Open asset creation modal/page

5. **User Controls**
   - Click notification icon → Show notifications panel
   - Click settings icon → Open settings menu
   - Click user avatar → Show user menu (profile, logout, etc.)

### Keyboard Navigation

- Tab: Move focus through interactive elements
- Shift+Tab: Move focus backward
- Enter/Space: Activate focused element
- Arrow keys: Navigate within menus (future enhancement)
- Escape: Close open menus/modals

## Implementation Notes

### Technology Stack

- **Frontend Framework**: Angular 17+
- **Styling**: Component-based CSS (not Tailwind)
- **Typography**: Manrope and Inter fonts (Google Fonts or self-hosted)
- **Icons**: Custom icon set (provided via Figma assets)
- **State Management**: Angular services
- **Routing**: Angular Router

### Component Structure

```
app/
├── core/
│   ├── layout/
│   │   ├── app-shell/
│   │   │   ├── app-shell.component.ts
│   │   │   ├── app-shell.component.html
│   │   │   └── app-shell.component.scss
│   │   ├── sidebar/
│   │   │   ├── sidebar.component.ts
│   │   │   ├── sidebar.component.html
│   │   │   └── sidebar.component.scss
│   │   ├── top-nav/
│   │   │   ├── top-nav.component.ts
│   │   │   ├── top-nav.component.html
│   │   │   └── top-nav.component.scss
│   │   └── main-content/
│   │       ├── main-content.component.ts
│   │       ├── main-content.component.html
│   │       └── main-content.component.scss
```

### Design Tokens

Create a design tokens file for consistent styling:

```typescript
// design-tokens.ts
export const DesignTokens = {
  colors: {
    primary: {
      blue900: '#1e3a8a',
      blue800: '#143b7d',
      blue700: '#315396',
    },
    accent: {
      red800: '#991b1b',
    },
    neutral: {
      slate50: '#f8fafc',
      slate100: '#f1f5f9',
      slate200: '#e2e8f0',
      slate400: '#94a3b8',
      slate500: '#64748b',
      slate600: '#475569',
    },
    background: {
      main: '#faf9ff',
      sidebar: '#f8fafc',
    }
  },
  spacing: {
    xs: '4px',
    sm: '8px',
    md: '12px',
    lg: '16px',
    xl: '24px',
    xxl: '32px',
  },
  borderRadius: {
    sm: '4px',
    md: '8px',
    lg: '12px',
  },
  typography: {
    fontFamily: {
      heading: 'Manrope, sans-serif',
      body: 'Inter, sans-serif',
    }
  }
};
```

## Testing Requirements

### Unit Tests

- Sidebar navigation component renders correctly
- Top navigation component renders correctly
- Active state logic works for navigation items
- Search input accepts and emits user input
- Button click events trigger correct actions

### Integration Tests

- Navigation between pages updates active states
- Search functionality integrates with backend
- User controls open correct menus/panels
- Layout maintains structure across different routes

### Visual Regression Tests

- Layout matches Figma design pixel-perfectly
- Spacing and alignment are consistent
- Colors match design specifications
- Typography renders correctly

### Accessibility Tests

- Keyboard navigation works for all interactive elements
- Screen reader announces navigation items correctly
- Color contrast meets WCAG AA standards
- Focus indicators are visible

## Future Enhancements

1. **Responsive Sidebar**
   - Collapsible sidebar for smaller screens
   - Icon-only view with tooltips
   - Mobile hamburger menu

2. **Enhanced Interactions**
   - Hover states for all interactive elements
   - Smooth transitions and animations
   - Loading states for navigation

3. **Customization**
   - User-configurable sidebar order
   - Theme switching (light/dark mode)
   - Collapsible sidebar preference

4. **Advanced Search**
   - Search suggestions/autocomplete
   - Recent searches
   - Advanced filters

5. **Notifications**
   - Real-time notification updates
   - Notification badges with counts
   - Notification preferences

## Appendix

### Asset URLs

All icons and images are available via Figma MCP server:
- User profile image
- Navigation icons (Assets, Software, Licenses, Network, Users)
- Top navigation icons (Notifications, Settings)
- Empty state icon
- Logo icon
- Decorative SVG elements

Assets are hosted temporarily and should be downloaded and stored in the project's assets directory.

### Design System Reference

This layout follows the Prism Logic editorial guidelines, which emphasize:
- Clean, professional aesthetic
- Consistent spacing and alignment
- Clear visual hierarchy
- Accessible color combinations
- Geometric accent elements for visual interest

### Figma Node Structure

```
Body (1:2)
├── Main Content Wrapper (1:3)
│   ├── Header - TopNavBar (1:4)
│   └── Main - Body Content (1:32)
└── Aside - SideNavBar (1:47)
```
