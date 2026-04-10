import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Subject, BehaviorSubject, debounceTime, distinctUntilChanged, takeUntil, finalize } from 'rxjs';
import { AssetService } from '../../services/asset.service';
import { Asset, AssetSearchQuery, AssetType, LifecycleStatus, Page } from '../../models';
import { environment } from '../../../../../environments/environment';

/**
 * Asset Inventory Component
 * 
 * Main list view for assets with:
 * - Global search functionality
 * - Advanced filtering (Asset Type, Status, Location)
 * - Sortable table columns
 * - Pagination controls
 * - Quick stats dashboard
 * - Export functionality
 * - Row-level actions (View, Edit, Delete)
 * 
 * Implements Editorial Geometry design system with:
 * - Asymmetrical layout
 * - Geometric triangle accents
 * - Surface hierarchy
 * - Glassmorphism effects
 */
@Component({
  selector: 'app-asset-inventory',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  templateUrl: './asset-inventory.component.html',
  styleUrls: ['./asset-inventory.component.scss']
})
export class AssetInventoryComponent implements OnInit, OnDestroy {
  // Make Math available in template
  Math = Math;
  
  // Observable streams
  assets$ = new BehaviorSubject<Asset[]>([]);
  loading$ = new BehaviorSubject<boolean>(false);
  error$ = new BehaviorSubject<string | null>(null);
  
  // Pagination
  currentPage = 0;
  pageSize = environment.pagination.defaultPageSize;
  totalElements = 0;
  totalPages = 0;
  
  // Search and filters
  searchText = '';
  selectedAssetTypes: AssetType[] = [];
  selectedStatuses: LifecycleStatus[] = [];
  selectedLocation = '';
  
  // Sorting
  sortField = 'createdAt';
  sortDirection: 'asc' | 'desc' = 'desc';
  
  // Quick stats
  totalAssets = 0;
  assetsInUse = 0;
  assetsAvailable = 0;
  
  // Enums for template
  assetTypes = Object.values(AssetType);
  lifecycleStatuses = Object.values(LifecycleStatus);
  
  // Locations (would typically come from API)
  locations: string[] = [];
  
  // Search debounce
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();
  
  constructor(
    private assetService: AssetService,
    private router: Router
  ) {}
  
  ngOnInit(): void {
    this.setupSearchDebounce();
    this.loadAssets();
    this.loadQuickStats();
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  /**
   * Setup search input debouncing to avoid excessive API calls
   */
  private setupSearchDebounce(): void {
    this.searchSubject
      .pipe(
        debounceTime(environment.ui.debounceTime),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(searchText => {
        this.searchText = searchText;
        this.currentPage = 0; // Reset to first page on new search
        this.loadAssets();
      });
  }
  
  /**
   * Load assets with current filters, pagination, and sorting
   */
  loadAssets(): void {
    this.loading$.next(true);
    this.error$.next(null);
    
    const query: AssetSearchQuery = {
      text: this.searchText || undefined,
      assetTypes: this.selectedAssetTypes.length > 0 ? this.selectedAssetTypes : undefined,
      statuses: this.selectedStatuses.length > 0 ? this.selectedStatuses : undefined,
      location: this.selectedLocation || undefined
    };
    
    const sort = `${this.sortField},${this.sortDirection}`;
    
    this.assetService.getAssets(query, this.currentPage, this.pageSize, sort)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading$.next(false))
      )
      .subscribe({
        next: (page: Page<Asset>) => {
          this.assets$.next(page.content);
          this.totalElements = page.page.totalElements;
          this.totalPages = page.page.totalPages;
          this.currentPage = page.page.number;
          
          // Extract unique locations for filter dropdown
          this.extractLocations(page.content);
        },
        error: (error) => {
          this.error$.next(error.message || 'Failed to load assets');
          console.error('Error loading assets:', error);
        }
      });
  }
  
  /**
   * Load quick statistics for dashboard widget
   */
  private loadQuickStats(): void {
    // This would typically be a separate API call
    // For now, we'll calculate from loaded assets
    this.assets$.pipe(takeUntil(this.destroy$)).subscribe(assets => {
      this.totalAssets = this.totalElements;
      this.assetsInUse = assets.filter(a => a.status === LifecycleStatus.IN_USE).length;
      this.assetsAvailable = assets.filter(a => 
        a.status === LifecycleStatus.DEPLOYED || 
        a.status === LifecycleStatus.STORAGE
      ).length;
    });
  }
  
  /**
   * Extract unique locations from assets for filter dropdown
   */
  private extractLocations(assets: Asset[]): void {
    const locationSet = new Set<string>();
    assets.forEach(asset => {
      if (asset.location) {
        locationSet.add(asset.location);
      }
    });
    this.locations = Array.from(locationSet).sort();
  }
  
  /**
   * Handle search input changes
   */
  onSearchChange(searchText: string): void {
    this.searchSubject.next(searchText);
  }
  
  /**
   * Handle asset type filter changes
   */
  onAssetTypeFilterChange(types: AssetType[]): void {
    this.selectedAssetTypes = types;
    this.currentPage = 0;
    this.loadAssets();
  }
  
  /**
   * Handle status filter changes
   */
  onStatusFilterChange(statuses: LifecycleStatus[]): void {
    this.selectedStatuses = statuses;
    this.currentPage = 0;
    this.loadAssets();
  }
  
  /**
   * Handle location filter changes
   */
  onLocationFilterChange(location: string): void {
    this.selectedLocation = location;
    this.currentPage = 0;
    this.loadAssets();
  }
  
  /**
   * Reset all filters
   */
  resetFilters(): void {
    this.searchText = '';
    this.selectedAssetTypes = [];
    this.selectedStatuses = [];
    this.selectedLocation = '';
    this.currentPage = 0;
    this.loadAssets();
  }
  
  /**
   * Handle column sorting
   */
  onSort(field: string): void {
    if (this.sortField === field) {
      // Toggle direction if same field
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      // New field, default to ascending
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.loadAssets();
  }
  
  /**
   * Handle pagination changes
   */
  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadAssets();
  }
  
  /**
   * Handle page size changes
   */
  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 0;
    this.loadAssets();
  }
  
  /**
   * Navigate to asset detail view
   */
  onViewAsset(asset: Asset): void {
    this.router.navigate(['/assets', asset.id]);
  }
  
  /**
   * Navigate to asset edit form
   */
  onEditAsset(asset: Asset): void {
    this.router.navigate(['/assets', asset.id, 'edit']);
  }
  
  /**
   * Handle asset deletion
   */
  onDeleteAsset(asset: Asset): void {
    if (confirm(`Are you sure you want to delete asset "${asset.name}"?`)) {
      this.assetService.deleteAsset(asset.id)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.loadAssets();
            this.loadQuickStats();
          },
          error: (error) => {
            this.error$.next(error.message || 'Failed to delete asset');
            console.error('Error deleting asset:', error);
          }
        });
    }
  }
  
  /**
   * Navigate to add new asset form
   */
  onAddNewAsset(): void {
    this.router.navigate(['/assets/new']);
  }
  
  /**
   * Export assets to CSV or JSON
   */
  onExport(format: 'CSV' | 'JSON'): void {
    const query: AssetSearchQuery = {
      text: this.searchText || undefined,
      assetTypes: this.selectedAssetTypes.length > 0 ? this.selectedAssetTypes : undefined,
      statuses: this.selectedStatuses.length > 0 ? this.selectedStatuses : undefined,
      location: this.selectedLocation || undefined
    };
    
    this.assetService.exportAssets(format, query)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob: Blob) => {
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = `assets-export-${new Date().toISOString()}.${format.toLowerCase()}`;
          link.click();
          window.URL.revokeObjectURL(url);
        },
        error: (error) => {
          this.error$.next(error.message || 'Failed to export assets');
          console.error('Error exporting assets:', error);
        }
      });
  }
  
  /**
   * Get icon name for asset type
   */
  getAssetTypeIcon(type: AssetType): string {
    const iconMap: Record<AssetType, string> = {
      [AssetType.SERVER]: 'dns',
      [AssetType.WORKSTATION]: 'computer',
      [AssetType.NETWORK_DEVICE]: 'router',
      [AssetType.STORAGE_DEVICE]: 'storage',
      [AssetType.SOFTWARE_LICENSE]: 'key',
      [AssetType.PERIPHERAL]: 'devices',
      [AssetType.KEYBOARD]: 'keyboard',
      [AssetType.MOUSE]: 'mouse',
      [AssetType.LAPTOP]: 'laptop',
      [AssetType.MONITOR]: 'monitor',
      [AssetType.HEADSET]: 'headset',
      [AssetType.LAPTOP_CHARGER]: 'power',
      [AssetType.HDMI_CABLE]: 'cable',
      [AssetType.NETWORK_CABLE]: 'cable',
      [AssetType.ACCESS_CARD]: 'badge'
    };
    return iconMap[type] || 'inventory_2';
  }
  
  /**
   * Get display label for asset type
   */
  getAssetTypeLabel(type: AssetType): string {
    return type.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
  }
  
  /**
   * Get display label for lifecycle status
   */
  getStatusLabel(status: LifecycleStatus): string {
    return status.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
  }
}
