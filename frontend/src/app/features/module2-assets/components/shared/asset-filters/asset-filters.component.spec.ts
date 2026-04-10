import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { FormsModule } from '@angular/forms';
import { AssetFiltersComponent } from './asset-filters.component';
import { AssetType, LifecycleStatus } from '../../../models';

describe('AssetFiltersComponent', () => {
  let component: AssetFiltersComponent;
  let fixture: ComponentFixture<AssetFiltersComponent>;
  let debugElement: DebugElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AssetFiltersComponent,
        NoopAnimationsModule,
        MatSelectModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatChipsModule,
        FormsModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AssetFiltersComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Component Initialization', () => {
    it('should initialize with empty filters', () => {
      expect(component.selectedAssetTypes).toEqual([]);
      expect(component.selectedStatuses).toEqual([]);
      expect(component.selectedLocation).toBe('');
      expect(component.locations).toEqual([]);
    });

    it('should populate asset types and lifecycle statuses', () => {
      expect(component.assetTypes.length).toBe(15);
      expect(component.lifecycleStatuses.length).toBe(7);
      expect(component.assetTypes).toContain(AssetType.SERVER);
      expect(component.lifecycleStatuses).toContain(LifecycleStatus.IN_USE);
    });
  });

  describe('Filter Changes', () => {
    it('should emit asset type changes', () => {
      spyOn(component.assetTypesChange, 'emit');
      const newTypes = [AssetType.SERVER, AssetType.LAPTOP];
      
      component.onAssetTypeChange(newTypes);
      
      expect(component.assetTypesChange.emit).toHaveBeenCalledWith(newTypes);
    });

    it('should emit status changes', () => {
      spyOn(component.statusesChange, 'emit');
      const newStatuses = [LifecycleStatus.IN_USE, LifecycleStatus.DEPLOYED];
      
      component.onStatusChange(newStatuses);
      
      expect(component.statusesChange.emit).toHaveBeenCalledWith(newStatuses);
    });

    it('should emit location changes', () => {
      spyOn(component.locationChange, 'emit');
      const newLocation = 'Data Center A';
      
      component.onLocationChange(newLocation);
      
      expect(component.locationChange.emit).toHaveBeenCalledWith(newLocation);
    });

    it('should emit filters reset', () => {
      spyOn(component.filtersReset, 'emit');
      
      component.resetFilters();
      
      expect(component.filtersReset.emit).toHaveBeenCalled();
    });
  });

  describe('Filter Removal', () => {
    beforeEach(() => {
      component.selectedAssetTypes = [AssetType.SERVER, AssetType.LAPTOP];
      component.selectedStatuses = [LifecycleStatus.IN_USE, LifecycleStatus.DEPLOYED];
      component.selectedLocation = 'Data Center A';
    });

    it('should remove specific asset type filter', () => {
      spyOn(component.assetTypesChange, 'emit');
      
      component.removeAssetTypeFilter(AssetType.SERVER);
      
      expect(component.assetTypesChange.emit).toHaveBeenCalledWith([AssetType.LAPTOP]);
    });

    it('should remove specific status filter', () => {
      spyOn(component.statusesChange, 'emit');
      
      component.removeStatusFilter(LifecycleStatus.IN_USE);
      
      expect(component.statusesChange.emit).toHaveBeenCalledWith([LifecycleStatus.DEPLOYED]);
    });

    it('should remove location filter', () => {
      spyOn(component.locationChange, 'emit');
      
      component.removeLocationFilter();
      
      expect(component.locationChange.emit).toHaveBeenCalledWith('');
    });
  });

  describe('Active Filters Detection', () => {
    it('should detect no active filters when all are empty', () => {
      component.selectedAssetTypes = [];
      component.selectedStatuses = [];
      component.selectedLocation = '';
      
      expect(component.hasActiveFilters()).toBeFalse();
    });

    it('should detect active filters when asset types are selected', () => {
      component.selectedAssetTypes = [AssetType.SERVER];
      component.selectedStatuses = [];
      component.selectedLocation = '';
      
      expect(component.hasActiveFilters()).toBeTrue();
    });

    it('should detect active filters when statuses are selected', () => {
      component.selectedAssetTypes = [];
      component.selectedStatuses = [LifecycleStatus.IN_USE];
      component.selectedLocation = '';
      
      expect(component.hasActiveFilters()).toBeTrue();
    });

    it('should detect active filters when location is selected', () => {
      component.selectedAssetTypes = [];
      component.selectedStatuses = [];
      component.selectedLocation = 'Data Center A';
      
      expect(component.hasActiveFilters()).toBeTrue();
    });
  });

  describe('Formatting Methods', () => {
    it('should format asset type correctly', () => {
      expect(component.formatAssetType(AssetType.NETWORK_DEVICE)).toBe('Network Device');
      expect(component.formatAssetType(AssetType.SOFTWARE_LICENSE)).toBe('Software License');
      expect(component.formatAssetType(AssetType.LAPTOP_CHARGER)).toBe('Laptop Charger');
    });

    it('should format status correctly', () => {
      expect(component.formatStatus(LifecycleStatus.IN_USE)).toBe('In Use');
      expect(component.formatStatus(LifecycleStatus.ORDERED)).toBe('Ordered');
      expect(component.formatStatus(LifecycleStatus.MAINTENANCE)).toBe('Maintenance');
    });
  });

  describe('UI Rendering', () => {
    it('should render filter controls', () => {
      fixture.detectChanges();
      
      const filterControls = debugElement.query(By.css('.filter-controls'));
      expect(filterControls).toBeTruthy();
    });

    it('should render reset button', () => {
      fixture.detectChanges();
      
      const resetButton = debugElement.query(By.css('.reset-button'));
      expect(resetButton).toBeTruthy();
    });

    it('should disable reset button when no active filters', () => {
      component.selectedAssetTypes = [];
      component.selectedStatuses = [];
      component.selectedLocation = '';
      fixture.detectChanges();
      
      const resetButton = debugElement.query(By.css('.reset-button'));
      expect(resetButton.nativeElement.disabled).toBeTrue();
    });

    it('should enable reset button when filters are active', () => {
      component.selectedAssetTypes = [AssetType.SERVER];
      fixture.detectChanges();
      
      const resetButton = debugElement.query(By.css('.reset-button'));
      expect(resetButton.nativeElement.disabled).toBeFalse();
    });

    it('should show active filters section when filters are active', () => {
      component.selectedAssetTypes = [AssetType.SERVER];
      fixture.detectChanges();
      
      const activeFilters = debugElement.query(By.css('.active-filters'));
      expect(activeFilters).toBeTruthy();
    });

    it('should hide active filters section when no filters are active', () => {
      component.selectedAssetTypes = [];
      component.selectedStatuses = [];
      component.selectedLocation = '';
      fixture.detectChanges();
      
      const activeFilters = debugElement.query(By.css('.active-filters'));
      expect(activeFilters).toBeFalsy();
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA labels on reset button', () => {
      fixture.detectChanges();
      
      const resetButton = debugElement.query(By.css('.reset-button'));
      expect(resetButton.nativeElement.getAttribute('aria-label')).toBe('Reset all filters');
    });

    it('should have proper form field labels', () => {
      fixture.detectChanges();
      
      const assetTypeField = debugElement.query(By.css('mat-form-field mat-label'));
      expect(assetTypeField).toBeTruthy();
    });
  });

  describe('Component Structure', () => {
    it('should have filters container', () => {
      fixture.detectChanges();
      
      const container = debugElement.query(By.css('.filters-container'));
      expect(container).toBeTruthy();
    });

    it('should have glassmorphism styling', () => {
      fixture.detectChanges();
      
      const container = debugElement.query(By.css('.filters-container'));
      const styles = getComputedStyle(container.nativeElement);
      expect(styles.backdropFilter).toContain('blur');
    });
  });
});