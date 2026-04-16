import { Routes } from '@angular/router';

/**
 * Asset Management Routes
 * 
 * Defines routing configuration for the Asset Management module (Module 2).
 * Uses lazy loading with standalone components for optimal performance.
 * 
 * Routes:
 * - /assets - Asset inventory list view
 * - /assets/new - Create new asset form
 * - /assets/:id - Asset detail view
 * - /assets/:id/edit - Edit asset form
 */
export const ASSET_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => 
      import('./components/asset-inventory/asset-inventory.component')
        .then(m => m.AssetInventoryComponent),
    title: 'Asset Inventory'
  },
  {
    path: 'new',
    loadComponent: () => 
      import('./components/asset-form/asset-form.component')
        .then(m => m.AssetFormComponent),
    title: 'Create New Asset'
  },
  {
    path: ':id',
    loadComponent: () => 
      import('./components/asset-detail/asset-detail.component')
        .then(m => m.AssetDetailComponent),
    title: 'Asset Details'
  },
  {
    path: ':id/edit',
    loadComponent: () => 
      import('./components/asset-form/asset-form.component')
        .then(m => m.AssetFormComponent),
    title: 'Edit Asset'
  }
];
