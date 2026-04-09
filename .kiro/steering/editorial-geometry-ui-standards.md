# Editorial Geometry UI Standards

This steering document defines the UI standards and design principles for implementing the Editorial Geometry design system across the IT Infrastructure Asset Management application.

## Design Philosophy: "The Corporate Curator"

The Editorial Geometry design system moves away from rigid, boxy constraints of traditional corporate dashboards toward a **High-End Editorial** experience. The Creative North Star is **"The Corporate Curator"**—a philosophy that treats digital space like a premium printed journal.

### Core Principles

1. **Intentional Asymmetry**: Use large-scale geometric triangles to break the grid and act as visual anchors
2. **Depth Through Layering**: Create physical depth through overlapping elements rather than structural lines
3. **Premium Editorial Feel**: Treat the interface like a high-end magazine with sophisticated typography and spacing
4. **Architectural Intent**: Make designs feel bespoke rather than templated through thoughtful element placement

## Color Strategy & Surface Logic

### Primary Color Palette

```css
/* Primary Colors */
--primary: #143b7d;           /* Blue 800 - Primary buttons, active states */
--primary-container: #315396;  /* Blue 700 - Logo background, gradients */
--secondary: #a9371d;         /* Red-Orange - Editorial accent color */
--tertiary: #80002b;          /* Deep Pink - Sophisticated softness */

/* Surface Hierarchy */
--surface: #faf9ff;                    /* Base layer - Light purple */
--surface-container: #eeedf4;          /* Content blocks - Light purple tint */
--surface-container-low: #f4f3f9;      /* Subtle elevation */
--surface-container-lowest: #ffffff;   /* Pop effect cards */
--surface-container-high: #e8e7ee;     /* Higher elevation */
--surface-container-highest: #e3e2e8;  /* Highest elevation */

/* Editorial Colors */
--on-surface: #1a1b20;               /* Never pure black - softer contrast */
--on-surface-variant: #434750;       /* Subtle text */
--outline: #747782;                  /* Standard outline */
--outline-variant: #c4c6d2;          /* Ghost borders */
--surface-tint: #3b5da0;             /* Image overlay tint */
```

### The "No-Line" Rule

**1px solid borders are prohibited for sectioning.** Boundaries must be defined through background color shifts or subtle tonal transitions.

**Examples:**
- ✅ Use `surface-container-low` section against `surface` background
- ❌ Use `border: 1px solid #ccc` for sectioning
- ✅ Use `outline-variant` at 15% opacity for accessibility-required edges
- ❌ Use solid borders for visual separation

### Surface Hierarchy & Nesting

Treat the UI as a series of stacked materials:

1. **Base Layer**: `surface` (#faf9ff) - Foundation background
2. **Content Blocks**: `surface-container` (#eeedf4) - Main content areas
3. **Elevated Cards**: `surface-container-lowest` (#ffffff) - Pop effect against darker containers
4. **Glass Elements**: `surface` at 70% opacity with `backdrop-filter: blur(12px)`

## Typography: Authority Meets Elegance

### Font System

```css
/* Font Families */
--font-heading: 'Manrope', sans-serif;  /* Geometric precision for headlines */
--font-body: 'Inter', sans-serif;       /* Readability workhorse for content */

/* Editorial Typography Scale */
--display-lg: 48px;      /* Hero statements with -2% letter-spacing */
--headline-lg: 30px;     /* Section headings with -0.75px tracking */
--headline-md: 20px;     /* Subsection headings */
--body-lg: 16px;         /* Standard body text */
--body-md: 14px;         /* Secondary body text */
--label-sm: 12px;        /* Button text, uppercase, 0.3px tracking */
```

### Typography Rules

1. **Display & Headlines**: Use Manrope for geometric precision and modern professional tone
2. **Body & Labels**: Use Inter for readability and functional clarity
3. **Hierarchy**: Always lead with high contrast - `headline-lg` in `secondary` (#a9371d) against `surface` background
4. **Letter Spacing**: Use tight letter-spacing (-2%) for hero statements to mimic editorial layouts
5. **Never Pure Black**: Always use `on-surface` (#1a1b20) instead of #000000 for softer contrast

## Geometric Accents & Visual Elements

### Triangle Accent System

```css
/* Geometric Triangle Standards */
.triangle-accent {
  fill: var(--primary);
  opacity: 0.1;
  position: absolute;
  z-index: -1;
}

.triangle-breathing-room {
  margin: 80px; /* Minimum space around geometric accents */
}
```

### Implementation Rules

1. **Breathing Room**: Maintain at least 80px of space around geometric triangles
2. **Visual Anchors**: Use triangles to break the grid and guide the eye
3. **Depth Markers**: Place triangles partially behind elevated content cards
4. **Color**: Use `primary` color (#143b7d) for triangle accents
5. **Responsiveness**: Scale triangles proportionally across viewport sizes

### Positioning Guidelines

- **Brand Header**: Triangle accent behind logo area
- **Content Cards**: Triangles partially overlapping elevated cards for 3D effect
- **Background Elements**: Subtle triangles in main content area with blur effects
- **Asymmetrical Layout**: Text blocks align left, geometric shapes anchor right

## Glassmorphism & Depth Effects

### Glass Effect Implementation

```css
.glassmorphism {
  background: rgba(250, 249, 255, 0.7); /* surface at 70% opacity */
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.glass-fallback {
  background: rgba(250, 249, 255, 0.95); /* Fallback for unsupported browsers */
}
```

### Ambient Shadows

```css
.editorial-shadow {
  box-shadow: 0 20px 40px rgba(20, 59, 125, 0.06); /* Blue-tinted shadows */
}

.button-shadow {
  box-shadow: 
    0px 10px 15px -3px rgba(20, 59, 125, 0.2),
    0px 4px 6px -4px rgba(20, 59, 125, 0.2);
}
```

### Usage Guidelines

1. **Top Navigation**: Apply glassmorphism with backdrop blur
2. **Floating Elements**: Use glass effects for modals and overlays
3. **Shadow Tinting**: Use blue-tinted shadows to match primary color
4. **Graceful Degradation**: Provide fallbacks for unsupported browsers

## Component Guidelines

### Buttons: The Intentional Action

```css
/* Primary Button */
.btn-primary {
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-container) 100%);
  color: var(--on-primary);
  border-radius: 8px; /* md roundness */
  font-family: var(--font-body);
  font-weight: 700;
  font-size: 12px;
  letter-spacing: 0.3px;
  text-transform: uppercase;
  box-shadow: var(--button-shadow);
}

/* Secondary Button */
.btn-secondary {
  background: var(--secondary-container);
  color: var(--on-secondary-container);
  /* High-contrast pairing for creative CTAs */
}

/* Ghost Button */
.btn-ghost {
  background: transparent;
  color: var(--primary);
  border: none;
}

.btn-ghost::after {
  content: "▲"; /* Triangle icon suffix */
  margin-left: 4px;
  transition: transform 0.2s ease;
}

.btn-ghost:hover::after {
  transform: translateX(4px);
}
```

### Input Fields: Minimalist Approach

```css
.input-field {
  background: transparent;
  border: none;
  border-bottom: 2px solid var(--outline-variant);
  font-family: var(--font-body);
  font-size: 14px;
  padding: 8px 0;
  transition: all 0.3s ease;
}

.input-field:focus {
  border-bottom-color: var(--primary);
  box-shadow: 0 4px 8px rgba(20, 59, 125, 0.1); /* Soft glow */
  outline: none;
}

.input-field::placeholder {
  color: var(--on-surface-variant);
  opacity: 0.7;
}
```

### Cards & Lists: The White Space Rule

```css
.card {
  background: var(--surface-container-lowest);
  border-radius: 8px;
  padding: 24px;
  box-shadow: var(--editorial-shadow);
  /* No divider lines - use spacing and typography hierarchy */
}

.list-item {
  padding: 16px 0;
  border-bottom: none; /* Prohibited */
}

.list-item:nth-child(even) {
  background: var(--surface-container-low); /* Background alternation */
}
```

## Layout & Spacing Standards

### Grid System

```css
.layout-grid {
  display: grid;
  grid-template-columns: 256px 1fr; /* Fixed sidebar, flexible content */
  grid-template-rows: 64px 1fr;     /* Fixed top nav, flexible content */
  min-height: 100vh;
}

.sidebar {
  grid-column: 1;
  grid-row: 1 / -1;
  background: var(--surface-container-low);
  padding: 24px;
}

.top-nav {
  grid-column: 2;
  grid-row: 1;
  background: var(--glassmorphism);
  backdrop-filter: blur(12px);
}

.main-content {
  grid-column: 2;
  grid-row: 2;
  background: var(--surface);
  padding: 32px;
  overflow-y: auto;
}
```

### Spacing Scale

```css
/* Editorial Spacing System */
--space-xs: 4px;
--space-sm: 8px;
--space-md: 12px;
--space-lg: 16px;
--space-xl: 24px;
--space-xxl: 32px;
--space-xxxl: 40px;
--space-editorial: 80px; /* Geometric accent breathing room */
```

## Accessibility Standards

### Focus Indicators

```css
.focus-indicator {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
  border-radius: 4px;
}

/* Ensure 3:1 contrast ratio for focus indicators */
@media (prefers-contrast: high) {
  .focus-indicator {
    outline-width: 3px;
  }
}
```

### Screen Reader Support

```html
<!-- Semantic HTML with ARIA landmarks -->
<nav aria-label="Primary navigation" class="sidebar">
  <ul role="list">
    <li role="listitem">
      <a href="/assets" aria-current="page">Assets</a>
    </li>
  </ul>
</nav>

<main role="main" class="main-content">
  <!-- Content projection area -->
</main>
```

### Color Contrast Requirements

- **Text on surface**: Minimum 4.5:1 contrast ratio (WCAG AA)
- **Focus indicators**: Minimum 3:1 contrast ratio
- **Ghost borders**: 15% opacity ensures subtle visibility while maintaining editorial feel

## Implementation Checklist

### ✅ Do's

- **Do** use asymmetrical layouts with text left, geometric shapes right
- **Do** use Manrope for all numbers and statistics for geometric corporate look
- **Do** apply surface-tint at 5% opacity over images for brand unification
- **Do** maintain 80px breathing room around geometric accents
- **Do** use tonal layering for depth instead of structural lines
- **Do** implement glassmorphism for floating navigation elements
- **Do** use ghost borders (15% opacity) for accessibility-required edges

### ❌ Don'ts

- **Don't** use pure black (#000000) - always use on-surface (#1a1b20)
- **Don't** use 1px solid borders for sectioning
- **Don't** use sharp 90-degree corners - stick to 8px border radius
- **Don't** crowd geometric accents - maintain breathing room
- **Don't** use harsh shadows - prefer blue-tinted ambient shadows
- **Don't** ignore glassmorphism fallbacks for older browsers

## Browser Support & Fallbacks

### Glassmorphism Fallbacks

```css
.glassmorphism {
  background: rgba(250, 249, 255, 0.7);
  backdrop-filter: blur(12px);
}

/* Fallback for browsers without backdrop-filter support */
@supports not (backdrop-filter: blur(12px)) {
  .glassmorphism {
    background: rgba(250, 249, 255, 0.95);
  }
}
```

### CSS Grid Fallbacks

```css
.layout-grid {
  display: grid;
  grid-template-columns: 256px 1fr;
}

/* Flexbox fallback */
@supports not (display: grid) {
  .layout-grid {
    display: flex;
    flex-direction: column;
  }
  
  .sidebar {
    width: 256px;
    position: fixed;
    height: 100vh;
  }
  
  .main-content {
    margin-left: 256px;
  }
}
```

## Performance Considerations

### Font Loading Optimization

```css
/* Preload critical fonts */
@font-face {
  font-family: 'Manrope';
  font-display: swap; /* Prevent FOUT */
  src: url('fonts/manrope.woff2') format('woff2');
}

@font-face {
  font-family: 'Inter';
  font-display: swap;
  src: url('fonts/inter.woff2') format('woff2');
}
```

### CSS Custom Properties for Performance

```css
:root {
  /* Define all design tokens as custom properties */
  --primary: #143b7d;
  --surface: #faf9ff;
  /* ... other tokens */
}

/* Use custom properties for dynamic theming */
.component {
  background: var(--surface);
  color: var(--on-surface);
}
```

This steering document ensures consistent implementation of Editorial Geometry principles across all components and features in the IT Infrastructure Asset Management application.