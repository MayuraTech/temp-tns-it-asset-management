import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TechnicalSpecsGridComponent, TechnicalSpec } from './technical-specs-grid.component';
import { AssetType } from '../../../models';

describe('TechnicalSpecsGridComponent', () => {
  let component: TechnicalSpecsGridComponent;
  let fixture: ComponentFixture<TechnicalSpecsGridComponent>;
  let debugElement: DebugElement;

  const mockSpecs: TechnicalSpec[] = [
    { label: 'CPU', value: 'Intel Core i7-12700K', icon: 'memory', category: 'Hardware' },
    { label: 'RAM', value: '32', unit: 'GB', icon: 'storage', category: 'Hardware' },
    { label: 'Storage', value: '1', unit: 'TB', icon: 'hard_drive', category: 'Storage' },
    { label: 'OS', value: 'Windows 11 Pro', icon: 'computer', category: 'Software' }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        TechnicalSpecsGridComponent,
        MatIconModule,
        MatTooltipModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TechnicalSpecsGridComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Specifications Display', () => {
    beforeEach(() => {
      component.specifications = mockSpecs;
      fixture.detectChanges();
    });

    it('should display specs container when specifications exist', () => {
      const container = debugElement.query(By.css('.specs-container'));
      expect(container).toBeTruthy();
    });

    it('should display specs title', () => {
      const title = debugElement.query(By.css('.specs-title'));
      expect(title.nativeElement.textContent.trim()).toBe('Technical Specifications');
    });

    it('should render all specification items', () => {
      const specItems = debugElement.queryAll(By.css('.spec-item'));
      expect(specItems.length).toBe(4);
    });

    it('should display specification labels', () => {
      const labels = debugElement.queryAll(By.css('.label-text'));
      expect(labels[0].nativeElement.textContent.trim()).toBe('CPU');
      expect(labels[1].nativeElement.textContent.trim()).toBe('RAM');
      expect(labels[2].nativeElement.textContent.trim()).toBe('Storage');
      expect(labels[3].nativeElement.textContent.trim()).toBe('OS');
    });

    it('should display specification values', () => {
      const values = debugElement.queryAll(By.css('.value-text'));
      expect(values[0].nativeElement.textContent.trim()).toBe('Intel Core i7-12700K');
      expect(values[1].nativeElement.textContent.trim()).toBe('32');
      expect(values[2].nativeElement.textContent.trim()).toBe('1');
      expect(values[3].nativeElement.textContent.trim()).toBe('Windows 11 Pro');
    });

    it('should display units when provided', () => {
      const units = debugElement.queryAll(By.css('.value-unit'));
      expect(units.length).toBe(2); // Only RAM and Storage have units
      expect(units[0].nativeElement.textContent.trim()).toBe('GB');
      expect(units[1].nativeElement.textContent.trim()).toBe('TB');
    });

    it('should display icons when provided', () => {
      const icons = debugElement.queryAll(By.css('.spec-icon'));
      expect(icons.length).toBe(4);
      expect(icons[0].nativeElement.textContent.trim()).toBe('memory');
      expect(icons[1].nativeElement.textContent.trim()).toBe('storage');
      expect(icons[2].nativeElement.textContent.trim()).toBe('hard_drive');
      expect(icons[3].nativeElement.textContent.trim()).toBe('computer');
    });
  });

  describe('Grouped Specifications', () => {
    beforeEach(() => {
      component.specifications = mockSpecs;
      component.groupedSpecs = true;
      fixture.detectChanges();
    });

    it('should display categories when groupedSpecs is true', () => {
      const categories = debugElement.queryAll(By.css('.category-title'));
      expect(categories.length).toBeGreaterThan(0);
    });

    it('should group specifications by category', () => {
      const categories = component.getCategories();
      expect(categories).toContain('Hardware');
      expect(categories).toContain('Storage');
      expect(categories).toContain('Software');
    });

    it('should return specifications for specific category', () => {
      const hardwareSpecs = component.getSpecsByCategory('Hardware');
      expect(hardwareSpecs.length).toBe(2); // CPU and RAM
      expect(hardwareSpecs[0].label).toBe('CPU');
      expect(hardwareSpecs[1].label).toBe('RAM');
    });

    it('should generate proper category IDs', () => {
      expect(component.getCategoryId('Hardware')).toBe('hardware');
      expect(component.getCategoryId('Storage Device')).toBe('storage-device');
    });
  });

  describe('Default Specifications', () => {
    it('should display default specs for SERVER asset type', () => {
      component.assetType = AssetType.SERVER;
      fixture.detectChanges();
      
      const specItems = debugElement.queryAll(By.css('.spec-item'));
      expect(specItems.length).toBeGreaterThan(0);
      
      const labels = debugElement.queryAll(By.css('.label-text'));
      const labelTexts = labels.map(label => label.nativeElement.textContent.trim());
      expect(labelTexts).toContain('CPU');
      expect(labelTexts).toContain('RAM');
      expect(labelTexts).toContain('Storage');
    });

    it('should display default specs for LAPTOP asset type', () => {
      component.assetType = AssetType.LAPTOP;
      fixture.detectChanges();
      
      const labels = debugElement.queryAll(By.css('.label-text'));
      const labelTexts = labels.map(label => label.nativeElement.textContent.trim());
      expect(labelTexts).toContain('CPU');
      expect(labelTexts).toContain('RAM');
      expect(labelTexts).toContain('Screen Size');
      expect(labelTexts).toContain('Battery');
    });

    it('should display default specs for MONITOR asset type', () => {
      component.assetType = AssetType.MONITOR;
      fixture.detectChanges();
      
      const labels = debugElement.queryAll(By.css('.label-text'));
      const labelTexts = labels.map(label => label.nativeElement.textContent.trim());
      expect(labelTexts).toContain('Size');
      expect(labelTexts).toContain('Resolution');
      expect(labelTexts).toContain('Panel Type');
      expect(labelTexts).toContain('Refresh Rate');
    });

    it('should display default specs for SOFTWARE_LICENSE asset type', () => {
      component.assetType = AssetType.SOFTWARE_LICENSE;
      fixture.detectChanges();
      
      const labels = debugElement.queryAll(By.css('.label-text'));
      const labelTexts = labels.map(label => label.nativeElement.textContent.trim());
      expect(labelTexts).toContain('Version');
      expect(labelTexts).toContain('License Type');
      expect(labelTexts).toContain('Users');
      expect(labelTexts).toContain('Expiry');
    });
  });

  describe('Empty State', () => {
    beforeEach(() => {
      component.specifications = [];
      component.assetType = undefined;
      fixture.detectChanges();
    });

    it('should display empty state when no specifications', () => {
      const emptyState = debugElement.query(By.css('.empty-specs'));
      expect(emptyState).toBeTruthy();
    });

    it('should display empty state title', () => {
      const title = debugElement.query(By.css('.empty-title'));
      expect(title.nativeElement.textContent.trim()).toBe('No Specifications Available');
    });

    it('should display empty state description', () => {
      const description = debugElement.query(By.css('.empty-description'));
      expect(description.nativeElement.textContent.trim()).toBe('Technical specifications will be displayed here when available.');
    });

    it('should display empty state icon', () => {
      const icon = debugElement.query(By.css('.empty-icon mat-icon'));
      expect(icon.nativeElement.textContent.trim()).toBe('info');
    });
  });

  describe('Display Specs Getter', () => {
    it('should return custom specifications when provided', () => {
      component.specifications = mockSpecs;
      component.assetType = AssetType.SERVER;
      
      expect(component.displaySpecs).toEqual(mockSpecs);
    });

    it('should return default specifications when no custom specs and asset type provided', () => {
      component.specifications = [];
      component.assetType = AssetType.SERVER;
      
      const displaySpecs = component.displaySpecs;
      expect(displaySpecs.length).toBeGreaterThan(0);
      expect(displaySpecs[0].label).toBe('CPU');
    });

    it('should return empty array when no specifications and no asset type', () => {
      component.specifications = [];
      component.assetType = undefined;
      
      expect(component.displaySpecs).toEqual([]);
    });
  });

  describe('Helper Methods', () => {
    beforeEach(() => {
      component.specifications = mockSpecs;
      component.groupedSpecs = true;
    });

    it('should return unique categories', () => {
      const categories = component.getCategories();
      expect(categories).toEqual(['Hardware', 'Storage', 'Software']);
    });

    it('should return empty array when groupedSpecs is false', () => {
      component.groupedSpecs = false;
      const categories = component.getCategories();
      expect(categories).toEqual([]);
    });

    it('should filter specifications by category', () => {
      const hardwareSpecs = component.getSpecsByCategory('Hardware');
      expect(hardwareSpecs.length).toBe(2);
      expect(hardwareSpecs.every(spec => spec.category === 'Hardware')).toBeTrue();
    });

    it('should generate kebab-case category IDs', () => {
      expect(component.getCategoryId('Hardware Specifications')).toBe('hardware-specifications');
      expect(component.getCategoryId('Network Config')).toBe('network-config');
    });
  });

  describe('Accessibility', () => {
    beforeEach(() => {
      component.specifications = mockSpecs;
      component.groupedSpecs = true;
      fixture.detectChanges();
    });

    it('should have proper ARIA labels on categories', () => {
      const categories = debugElement.queryAll(By.css('.spec-category'));
      categories.forEach(category => {
        expect(category.nativeElement.getAttribute('aria-labelledby')).toContain('category-');
      });
    });

    it('should have proper IDs on category titles', () => {
      const categoryTitles = debugElement.queryAll(By.css('.category-title'));
      categoryTitles.forEach(title => {
        expect(title.nativeElement.id).toContain('category-');
      });
    });

    it('should have proper ARIA labels on spec items', () => {
      const specLabels = debugElement.queryAll(By.css('.spec-label'));
      specLabels.forEach(label => {
        expect(label.nativeElement.getAttribute('aria-label')).toBeTruthy();
      });
    });

    it('should have role="definition" on spec items', () => {
      const specItems = debugElement.queryAll(By.css('.spec-item'));
      specItems.forEach(item => {
        expect(item.nativeElement.getAttribute('role')).toBe('definition');
      });
    });
  });

  describe('Component Structure', () => {
    it('should render specs container', () => {
      component.specifications = mockSpecs;
      fixture.detectChanges();
      
      const container = debugElement.query(By.css('.specs-container'));
      expect(container).toBeTruthy();
    });

    it('should have proper styling classes', () => {
      component.specifications = mockSpecs;
      fixture.detectChanges();
      
      const grid = debugElement.query(By.css('.specs-grid'));
      expect(grid).toBeTruthy();
    });
  });
});