/**
 * Editorial Geometry Design Token System
 * 
 * Implements the "Corporate Curator" philosophy treating digital space
 * like a premium printed journal with intentional asymmetry, geometric
 * triangle accents, and tonal layering.
 */

export const DesignTokens = {
  colors: {
    // Primary Colors - Editorial Geometry Palette
    primary: {
      blue900: '#1e3a8a',      // Primary headings and active text
      blue800: '#143b7d',      // Primary buttons, active states
      blue700: '#315396'       // Logo background, gradients
    },
    
    // Editorial Accent Colors
    accent: {
      red800: '#991b1b',       // Active state indicators
      secondary: '#a9371d',    // Editorial accent color
      tertiary: '#80002b'      // Deep pink - sophisticated softness
    },
    
    // Neutral Colors - Traditional Corporate
    neutral: {
      slate50: '#f8fafc',      // Sidebar background
      slate100: '#f1f5f9',     // Input backgrounds
      slate500: '#64748b',     // Secondary text
      slate600: '#475569',     // Inactive navigation text
      gray400: '#6b7280'       // Placeholder text
    },
    
    // Surface Hierarchy - Editorial Geometry Tonal Layering
    surface: {
      base: '#faf9ff',                    // Base layer - Light purple
      container: '#eeedf4',               // Content blocks - Light purple tint
      containerLow: '#f4f3f9',            // Subtle elevation
      containerLowest: '#ffffff',         // Pop effect cards
      containerHigh: '#e8e7ee',           // Higher elevation
      containerHighest: '#e3e2e8',        // Highest elevation
      tint: '#3b5da0'                     // Image overlay tint
    },
    
    // Editorial Colors - Never Pure Black
    editorial: {
      onSurface: '#1a1b20',              // Never pure black - softer contrast
      onSurfaceVariant: '#434750',       // Subtle text
      outline: '#747782',                // Standard outline
      outlineVariant: '#c4c6d2'          // Ghost borders
    },
    
    // Semantic Colors
    white: '#ffffff',
    transparent: 'transparent'
  },
  
  // Editorial Spacing System - 4px base unit with editorial breathing room
  spacing: {
    xs: '4px',
    sm: '8px',
    md: '12px',
    lg: '16px',
    xl: '24px',
    xxl: '32px',
    xxxl: '40px',
    editorial: '80px'                    // Geometric accent breathing room
  },
  
  // Border Radius - Editorial Geometry Standards
  borderRadius: {
    sm: '4px',
    md: '8px',                           // Editorial standard
    lg: '12px'
  },
  
  // Typography - Authority Meets Elegance
  typography: {
    fontFamilies: {
      heading: 'Manrope, sans-serif',     // Geometric precision for headlines
      body: 'Inter, sans-serif'           // Readability workhorse for content
    },
    
    // Editorial Typography Scale
    fontSizes: {
      xs: '12px',                        // Button text, labels
      sm: '14px',                        // Navigation, body text
      md: '16px',                        // Standard body text
      lg: '18px',                        // Section headings
      xl: '20px',                        // Logo text
      xxl: '30px',                       // Page titles
      displayLg: '48px'                  // Hero statements
    },
    
    fontWeights: {
      regular: 400,
      medium: 500,
      bold: 700,
      extraBold: 800                     // Editorial headlines
    },
    
    lineHeights: {
      tight: '16px',                     // Button text
      normal: '20px',                    // Navigation items
      relaxed: '22.5px',                 // Section headings
      loose: '24px',                     // Body text
      extraLoose: '28px',                // Logo text
      spacious: '36px'                   // Page titles
    },
    
    // Editorial Letter Spacing
    letterSpacing: {
      tight: '-0.75px',                  // Editorial headlines
      normal: '-0.5px',                  // Logo text
      wide: '0.3px',                     // Button text (uppercase)
      editorial: '-2%'                   // Hero statements
    }
  },
  
  // Shadows - Blue-tinted Ambient Effects
  shadows: {
    subtle: '0px 1px 2px 0px rgba(30,58,138,0.05)',
    button: '0px 10px 15px -3px rgba(20,59,125,0.2), 0px 4px 6px -4px rgba(20,59,125,0.2)',
    editorial: '0 20px 40px rgba(20, 59, 125, 0.06)',  // Blue-tinted ambient shadows
    focusGlow: '0 4px 8px rgba(20, 59, 125, 0.1)'      // Soft focus glow
  },
  
  // Layout Dimensions
  layout: {
    sidebarWidth: '256px',
    topNavHeight: '64px',
    contentPadding: '32px'
  },
  
  // Editorial Geometry Specific Values
  editorial: {
    triangleBreathingRoom: '80px',       // Minimum space around geometric accents
    glassOpacity: '70%',                 // Glassmorphism transparency
    ghostBorderOpacity: '15%',           // Subtle border suggestions
    surfaceTintOpacity: '5%',            // Image overlay tint
    backdropBlur: '12px'                 // Glassmorphism blur effect
  }
} as const;

// Type definitions for design tokens
export type ColorToken = typeof DesignTokens.colors;
export type SpacingToken = typeof DesignTokens.spacing;
export type TypographyToken = typeof DesignTokens.typography;
export type ShadowToken = typeof DesignTokens.shadows;
export type LayoutToken = typeof DesignTokens.layout;
export type EditorialToken = typeof DesignTokens.editorial;