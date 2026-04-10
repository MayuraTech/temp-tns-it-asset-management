import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { AssetInventoryComponent } from './asset-inventory.component';
import { AssetService } from '../../services/asset.service';
import { Asset, AssetType, LifecycleStatus, Page } from '../../models';

describe('AssetInventoryComponent', () => {
  let component: AssetInventoryComponent;
  let fixture: ComponentFixture<AssetInventoryComponent>;
  let assetService: jasmine.SpyObj<AssetService>;
  let router: jasmine.SpyObj<Router>;

  const mockAssets: Asset[] = [
    {
      id: '1',
      assetType: AssetType.SERVER,
      name: 'Test Server 1',
      serialNumber: 'SRV-001',
      acquisitionDate: '2024-01-15',
      status: LifecycleStatus.IN_USE,
      location: 'Data Center A',
      assignedUser: 'John Doe',
      assignedUserEmail: 'john@example.com',
      createdAt: '2024-01-15T10:00:00Z',
      createdBy: 'admin',
      updatedAt: '2024-01-15T10:00:00Z',
      updatedBy: 'admin',
      readOnly: false
    },
    {
      id: '2',
      assetType: AssetType.LAPTOP,
      name: 'Test Laptop 1',
      serialNumber: 'LAP-001',
      acquisitionDate: '2024-01-16',
      status: LifecycleStatus.DEPLOYED,
      location: 'Office B',
      assignedUser: undefined,
      assignedUserEmail: undefined,
      createdAt: '2024-01-16T10:00:00Z',
      createdBy: 'admin',
      updatedAt: '2024-01-16T10:00:00Z',
      updatedBy: 'admin',
      readOnly: false
    }
  ];

  const mockPage: Page<Asset> = {
    content: mockAssets,
    page: {
      size: 20,
      number: 0,
      totalElements: 2,
      totalPages: 1
    }
  };

  beforeEach(async () => {
    const assetServiceSpy = jasmine.createSpyObj('AssetService', [
      'getAssets',
      'deleteAsset',
      'exportAssets'
    ]);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [AssetInventoryComponent],
      imports: [
        HttpClientTestingModule,
        BrowserAnimationsModule
      ],
      providers: [
        { provide: AssetService, useValue: assetServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    assetService = TestBed.inject(AssetService) as jasmine.SpyObj<AssetService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    
    assetService.getAssets.and.returnValue(of(mockPage));
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssetInventoryComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Component Initialization', () => {
    it('should load assets on init', () => {
      fixture.detectChanges();
      
      expect(assetService.getAssets).toHaveBeenCalled();
      expect(component.assets$.value).toEqual(mockAssets);
      expect(component.totalElements).toBe(2);
      expect(component.totalPages).toBe(1);
    });

    it('should setup search debounce on init', fakeAsync(() => {
      fixture.detectChanges();
      
      component.onSearchChange('test');
      expect(assetService.getAssets).toHaveBeenCalledTimes(1); // Only initial load
      
      tick(300); // Wait for debounce
      expect(assetService.getAssets).toHaveBeenCalledTimes(2); // Now search triggered
    }));

    it('should calculate quick stats on init', () => {
      fixture.detectChanges();
      
      expect(component.totalAssets).toBe(2);
      expect(component.assetsInUse).toBe(1);
      expect(component.assetsAvailable).toBe(1);
    });
  });

  describe('loadAssets', () => {
    it('should load assets with default parameters', () => {
      component.loadAssets();
      
      expect(assetService.getAssets).toHaveBeenCalledWith(
        jasmine.objectContaining({}),
        0,
        20,
        'createdAt,desc'
      );
    });

    it('should load assets with search text', () => {
      component.searchText = 'server';
      component.loadAssets();
      
      expect(assetService.getAssets).toHaveBeenCalledWith(
        jasmine.objectContaining({ text: 'server' }),
        0,
        20,
        'createdAt,desc'
      );
    });

    it('should load assets with filters', () => {
      component.selectedAssetTypes = [AssetType.SERVER];
      component.selectedStatuses = [LifecycleStatus.IN_USE];
      component.selectedLocation = 'Data Center A';
      component.loadAssets();
      
      expect(assetService.getAssets).toHaveBeenCalledWith(
        jasmine.objectContaining({
          assetTypes: [AssetType.SERVER],
          statuses: [LifecycleStatus.IN_USE],
          location: 'Data Center A'
        }),
        0,
        20,
        'createdAt,desc'
      );
    });

    it('should set loading state during load', fakeAsync(() => {
      component.loadAssets();
      
      expect(component.loading$.value).toBe(true);
      
      tick();
      
      expect(component.loading$.value).toBe(false);
    }));

    it('should handle errors', fakeAsync(() => {
      const error = new Error('Failed to load assets');
      assetService.getAssets.and.returnValue(throwError(() => error));
      
      component.loadAssets();
      tick();
      
      expect(component.error$.value).toBe('Failed to load assets');
      expect(component.loading$.value).toBe(false);
    }));

    it('should extract unique locations from assets', () => {
      fixture.detectChanges();
      
      expect(component.locations).toContain('Data Center A');
      expect(component.locations).toContain('Office B');
      expect(component.locations.length).toBe(2);
    });
  });

  describe('Search Functionality', () => {
    it('should debounce search input', fakeAsync(() => {
      fixture.detectChanges();
      assetService.getAssets.calls.reset();
      
      component.onSearchChange('t');
      component.onSearchChange('te');
      component.onSearchChange('tes');
      component.onSearchChange('test');
      
      expect(assetService.getAssets).not.toHaveBeenCalled();
      
      tick(300);
      
      expect(assetService.getAssets).toHaveBeenCalledTimes(1);
      expect(component.searchText).toBe('test');
    }));

    it('should reset to first page on search', fakeAsync(() => {
      fixture.detectChanges();
      component.currentPage = 2;
      
      component.onSearchChange('test');
      tick(300);
      
      expect(component.currentPage).toBe(0);
    }));
  });

  describe('Filter Functionality', () => {
    beforeEach(() => {
      fixture.detectChanges();
      assetService.getAssets.calls.reset();
    });

    it('should filter by asset type', () => {
      component.onAssetTypeFilterChange([AssetType.SERVER]);
      
      expect(component.selectedAssetTypes).toEqual([AssetType.SERVER]);
      expect(component.currentPage).toBe(0);
      expect(assetService.getAssets).toHaveBeenCalled();
    });

    it('should filter by status', () => {
      component.onStatusFilterChange([LifecycleStatus.IN_USE]);
      
      expect(component.selectedStatuses).toEqual([LifecycleStatus.IN_USE]);
      expect(component.currentPage).toBe(0);
      expect(assetService.getAssets).toHaveBeenCalled();
    });

    it('should filter by location', () => {
      component.onLocationFilterChange('Data Center A');
      
      expect(component.selectedLocation).toBe('Data Center A');
      expect(component.currentPage).toBe(0);
      expect(assetService.getAssets).toHaveBeenCalled();
    });

    it('should reset all filters', () => {
      component.searchText = 'test';
      component.selectedAssetTypes = [AssetType.SERVER];
      component.selectedStatuses = [LifecycleStatus.IN_USE];
      component.selectedLocation = 'Data Center A';
      component.currentPage = 2;
      
      component.resetFilters();
      
      expect(component.searchText).toBe('');
      expect(component.selectedAssetTypes).toEqual([]);
      expect(component.selectedStatuses).toEqual([]);
      expect(component.selectedLocation).toBe('');
      expect(component.currentPage).toBe(0);
      expect(assetService.getAssets).toHaveBeenCalled();
    });
  });

  describe('Sorting Functionality', () => {
    beforeEach(() => {
      fixture.detectChanges();
      assetService.getAssets.calls.reset();
    });

    it('should sort by field in ascending order', () => {
      component.onSort('name');
      
      expect(component.sortField).toBe('name');
      expect(component.sortDirection).toBe('asc');
      expect(assetService.getAssets).toHaveBeenCalled();
    });

    it('should toggle sort direction on same field', () => {
      component.sortField = 'name';
      component.sortDirection = 'asc';
      
      component.onSort('name');
      
      expect(component.sortDirection).toBe('desc');
    });

    it('should reset to ascending when sorting by new field', () => {
      component.sortField = 'name';
      component.sortDirection = 'desc';
      
      component.onSort('assetType');
      
      expect(component.sortField).toBe('assetType');
      expect(component.sortDirection).toBe('asc');
    });
  });

  describe('Pagination Functionality', () => {
    beforeEach(() => {
      fixture.detectChanges();
      assetService.getAssets.calls.reset();
    });

    it('should change page', () => {
      component.onPageChange(2);
      
      expect(component.currentPage).toBe(2);
      expect(assetService.getAssets).toHaveBeenCalled();
    });

    it('should change page size and reset to first page', () => {
      component.currentPage = 2;
      
      component.onPageSizeChange(50);
      
      expect(component.pageSize).toBe(50);
      expect(component.currentPage).toBe(0);
      expect(assetService.getAssets).toHaveBeenCalled();
    });
  });

  describe('Navigation Actions', () => {
    it('should navigate to asset detail view', () => {
      const asset = mockAssets[0];
      
      component.onViewAsset(asset);
      
      expect(router.navigate).toHaveBeenCalledWith(['/assets', asset.id]);
    });

    it('should navigate to asset edit form', () => {
      const asset = mockAssets[0];
      
      component.onEditAsset(asset);
      
      expect(router.navigate).toHaveBeenCalledWith(['/assets', asset.id, 'edit']);
    });

    it('should navigate to add new asset form', () => {
      component.onAddNewAsset();
      
      expect(router.navigate).toHaveBeenCalledWith(['/assets/new']);
    });
  });

  describe('Delete Functionality', () => {
    beforeEach(() => {
      fixture.detectChanges();
      assetService.deleteAsset.and.returnValue(of(void 0));
      spyOn(window, 'confirm').and.returnValue(true);
    });

    it('should delete asset after confirmation', fakeAsync(() => {
      const asset = mockAssets[0];
      
      component.onDeleteAsset(asset);
      tick();
      
      expect(window.confirm).toHaveBeenCalled();
      expect(assetService.deleteAsset).toHaveBeenCalledWith(asset.id);
    }));

    it('should not delete asset if not confirmed', () => {
      (window.confirm as jasmine.Spy).and.returnValue(false);
      const asset = mockAssets[0];
      
      component.onDeleteAsset(asset);
      
      expect(assetService.deleteAsset).not.toHaveBeenCalled();
    });

    it('should reload assets after successful deletion', fakeAsync(() => {
      assetService.getAssets.calls.reset();
      const asset = mockAssets[0];
      
      component.onDeleteAsset(asset);
      tick();
      
      expect(assetService.getAssets).toHaveBeenCalled();
    }));

    it('should handle deletion errors', fakeAsync(() => {
      const error = new Error('Failed to delete asset');
      assetService.deleteAsset.and.returnValue(throwError(() => error));
      const asset = mockAssets[0];
      
      component.onDeleteAsset(asset);
      tick();
      
      expect(component.error$.value).toBe('Failed to delete asset');
    }));
  });

  describe('Export Functionality', () => {
    beforeEach(() => {
      const blob = new Blob(['test'], { type: 'text/csv' });
      assetService.exportAssets.and.returnValue(of(blob));
      spyOn(window.URL, 'createObjectURL').and.returnValue('blob:test');
      spyOn(window.URL, 'revokeObjectURL');
    });

    it('should export assets to CSV', fakeAsync(() => {
      component.onExport('CSV');
      tick();
      
      expect(assetService.exportAssets).toHaveBeenCalledWith(
        'CSV',
        jasmine.any(Object)
      );
      expect(window.URL.createObjectURL).toHaveBeenCalled();
    }));

    it('should export assets to JSON', fakeAsync(() => {
      component.onExport('JSON');
      tick();
      
      expect(assetService.exportAssets).toHaveBeenCalledWith(
        'JSON',
        jasmine.any(Object)
      );
    }));

    it('should handle export errors', fakeAsync(() => {
      const error = new Error('Failed to export assets');
      assetService.exportAssets.and.returnValue(throwError(() => error));
      
      component.onExport('CSV');
      tick();
      
      expect(component.error$.value).toBe('Failed to export assets');
    }));
  });

  describe('Helper Methods', () => {
    it('should get correct icon for asset type', () => {
      expect(component.getAssetTypeIcon(AssetType.SERVER)).toBe('dns');
      expect(component.getAssetTypeIcon(AssetType.LAPTOP)).toBe('laptop');
      expect(component.getAssetTypeIcon(AssetType.WORKSTATION)).toBe('computer');
    });

    it('should format asset type label', () => {
      expect(component.getAssetTypeLabel(AssetType.SERVER)).toBe('Server');
      expect(component.getAssetTypeLabel(AssetType.NETWORK_DEVICE)).toBe('Network Device');
    });

    it('should format status label', () => {
      expect(component.getStatusLabel(LifecycleStatus.IN_USE)).toBe('In Use');
      expect(component.getStatusLabel(LifecycleStatus.ORDERED)).toBe('Ordered');
    });
  });

  describe('Component Cleanup', () => {
    it('should unsubscribe on destroy', () => {
      fixture.detectChanges();
      spyOn(component['destroy$'], 'next');
      spyOn(component['destroy$'], 'complete');
      
      component.ngOnDestroy();
      
      expect(component['destroy$'].next).toHaveBeenCalled();
      expect(component['destroy$'].complete).toHaveBeenCalled();
    });
  });
});
