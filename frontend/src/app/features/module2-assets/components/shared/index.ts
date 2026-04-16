/**
 * Shared UI Components for Module 2 Assets
 * 
 * This barrel file exports all reusable UI components used across asset screens.
 * Components follow Editorial Geometry design system standards.
 */

// Status and Icon Components
export { AssetStatusBadgeComponent } from './asset-status-badge/asset-status-badge.component';
export { AssetIconComponent } from './asset-icon/asset-icon.component';

// Filter and Search Components
export { AssetFiltersComponent } from './asset-filters/asset-filters.component';

// Assignment and User Components
export { AssignmentCardComponent } from './assignment-card/assignment-card.component';

// Timeline and History Components
export { LifecycleTimelineComponent, LifecycleEvent } from './lifecycle-timeline/lifecycle-timeline.component';

// Action Components
export { QuickActionsComponent } from './quick-actions/quick-actions.component';

// Technical Information Components
export { TechnicalSpecsGridComponent, TechnicalSpec } from './technical-specs-grid/technical-specs-grid.component';

// Table Components
export { AssetTableComponent, AssetTableAction, BulkAction } from './asset-table/asset-table.component';

// Dashboard Components
export { DashboardStatsComponent } from '../dashboard-stats/dashboard-stats.component';