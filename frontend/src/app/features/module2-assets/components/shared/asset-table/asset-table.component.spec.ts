import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule } from '@angular/material/sort';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatMenuModule } from '@angular/material/menu';
import { AssetTableComponent, AssetTableAction, BulkAction } from './asset-table.component';
import { AssetStatusBadgeComponent } from '../asset-status-badge/asset-status-badge.component';
import { AssetIconComponent } from '../asset-icon/asset-icon.component';
import { Asset, AssetType, LifecycleStatus } from '../../../models';

describe('AssetTableComponent', () => {
  let component: AssetTableComponent;
  let fixture: ComponentFixture<AssetTableComponent>;
  let debugElement: DebugElement;

  const mockAssets: Asset[] = [
    {
      id: '1',
      assetType: AssetType.SERVER,
      name: 'Production Server 01',
      serialNumber: 'SRV-001',
      acquisitionDate: '2024-01-15',
      status: LifecycleStatus.IN_USE,
      location: 'Data Center A',
      assignedUser: 'John Doe',
      assignedUserEmail: 'john.doe@example.com',
      createdAt: '2024-01-15T10:30:00Z',
      createdBy: 'admin',
      updatedAt: '2024-01-15T10:30:00Z',
      updatedBy: 'admin',
      readOnly: false
    },
    {
      id: '2',
      assetType: AssetType.LAPTOP,
      name: 'MacBook Pro 16"',
      serialNumber: 'MBP-002',
      acquisitionDate: '2024-01-10',
      status: LifecycleStatus.DEPLOYED,
      location: 'Office Floor 2',
      assignedUser: 'Jane Smith',
      assignedUserEmail: 'jane.smith@example.com',
      createdAt: '2024-01-10T09:15:00Z',
      createdBy: 'admin',
      updatedAt: '2024-01-10T09:15:00Z',
      updatedBy: 'admin',
      readOnly: false
    }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AssetTableComponent,
        AssetStatusBadgeComponent,
        AssetIconComponent,
        NoopAnimationsModule,
        MatTableModule,
        MatSortModule,
        MatPaginatorModule,
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        MatCheckboxModule,
        MatMenuModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AssetTableComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Component Initialization', () => {
    it('should initialize with default values', () => {
      expect(component.assets).toEqual([]);
      expect(component.loading).toBeFalse();
      expect(component.showSelection).toBeFalse();
      expect(component.showActions).toBeTrue();
      expect(component.showBulkActions).toBeTrue();
      expect(component.showPagination).toBeTrue();
      expect(component.stickyHeader).toBeFalse();
    });

    it('should initialize data source', () => {
      expect(component.dataSource).toBeTruthy();
      expect(component.selection).toBeTruthy();
    });
  });

  describe('Asset Display', () => {
    beforeEach(() => {
      component.assets = mockAssets;
      component.ngOnChanges();
      fixture.detectChanges();
    });

    it('should display asset table', () => {
      const table = debugElement.query(By.css('.asset-table'));
      expect(table).toBeTruthy();
    });

    it('should render asset rows', () => {
      const rows = debugElement.queryAll(By.css('.data-row'));
      expect(rows.length).toBe(2);
    });

    it('should display asset names', () => {
      const nameElements = debugElement.queryAll(By.css('.asset-name'));
      expect(nameElements[0].nativeElement.textContent.trim()).toBe('Production Server 01');
      expect(nameElements[1].nativeElement.textContent.trim()).toBe('MacBook Pro 16"');
    });

    it('should display serial numbers', () => {
      const serialElements = debugElement.queryAll(By.css('.serial-number'));
      expect(serialElements[0].nativeElement.textContent.trim()).toBe('SRV-001');
      expect(serialElements[1].nativeElement.textContent.trim()).toBe('MBP-002');
    });

    it('should display asset icons', () => {
      const icons = debugElement.queryAll(By.css('app-asset-icon'));
      expect(icons.length).toBe(2);
    });

    it('should display status badges', () => {
      const badges = debugElement.queryAll(By.css('app-asset-status-badge'));
      expect(badges.length).toBe(2);
    });
  });

  describe('Column Configuration', () => {
    beforeEach(() => {
      component.assets = mockAssets;
      fixture.detectChanges();
    });

    it('should display default columns', () => {
      const headers = debugElement.queryAll(By.css('th'));
      const headerTexts = headers.map(h => h.nativeElement.textContent.trim());
      
      expect(headerTexts).toContain('Name');
      expect(headerTexts).toContain('Type');
      expect(headerTexts).toContain('Serial Number');
      expect(headerTexts).toContain('Status');
      expect(headerTexts).toContain('Actions');
    });

    it('should show selection column when enabled', () => {
      component.showSelection = true;
      fixture.detectChanges();
      
      const selectColumn = debugElement.query(By.css('.select-column'));
      expect(selectColumn).toBeTruthy();
    });

    it('should hide actions column when disabled', () => {
      component.showActions = false;
      fixture.detectChanges();
      
      const actionsColumn = debugElement.query(By.css('.actions-column'));
      expect(actionsColumn).toBeFalsy();
    });

    it('should use custom columns when provided', () => {
      component.columns = ['name', 'serialNumber', 'status'];
      fixture.detectChanges();
      
      const displayedColumns = component.displayedColumns;
      expect(displayedColumns).toContain('name');
      expect(displayedColumns).toContain('serialNumber');
      expect(displayedColumns).toContain('status');
      expect(displayedColumns).not.toContain('type');
    });
  });

  describe('Selection Functionality', () => {
    beforeEach(() => {
      component.assets = mockAssets;
      component.showSelection = true;
      component.ngOnChanges();
      fixture.detectChanges();
    });

    it('should show selection checkboxes', () => {
      const checkboxes = debugElement.queryAll(By.css('mat-checkbox'));
      expect(checkboxes.length).toBe(3); // Header + 2 rows
    });

    it('should select individual assets', () => {
      component.selection.select(mockAssets[0]);
      expect(component.selection.isSelected(mockAssets[0])).toBeTrue();
    });

    it('should toggle all rows selection', () => {
      component.toggleAllRows();
      expect(component.selection.selected.length).toBe(2);
      
      component.toggleAllRows();
      expect(component.selection.selected.length).toBe(0);
    });

    it('should detect when all rows are selected', () => {
      component.selection.select(...mockAssets);
      expect(component.isAllSelected()).toBeTrue();
    });

    it('should clear selection', () => {
      component.selection.select(...mockAssets);
      component.clearSelection();
      expect(component.selection.selected.length).toBe(0);
    });

    it('should generate correct checkbox labels', () => {
      expect(component.checkboxLabel()).toBe('select all');
      expect(component.checkboxLabel(mockAssets[0])).toBe('select asset Production Server 01');
      
      component.selection.select(mockAssets[0]);
      expect(component.checkboxLabel(mockAssets[0])).toBe('deselect asset Production Server 01');
    });
  });

  describe('Bulk Actions', () => {
    beforeEach(() => {
      component.assets = mockAssets;
      component.showSelection = true;
      component.showBulkActions = true;
      component.selection.select(mockAssets[0]);
      fixture.detectChanges();
    });

    it('should show bulk actions bar when assets are selected', () => {
      const bulkActionsBar = debugElement.query(By.css('.bulk-actions-bar'));
      expect(bulkActionsBar).toBeTruthy();
    });

    it('should display selection count', () => {
      const selectionCount = debugElement.query(By.css('.selection-count'));
      expect(selectionCount.nativeElement.textContent.trim()).toBe('1 selected');
    });

    it('should show bulk action buttons', () => {
      const bulkButtons = debugElement.queryAll(By.css('.bulk-action-btn'));
      expect(bulkButtons.length).toBeGreaterThan(0);
    });

    it('should emit bulk action events', () => {
      spyOn(component.bulkActionClick, 'emit');
      
      component.onBulkAction('export');
      
      expect(component.bulkActionClick.emit).toHaveBeenCalledWith({
        action: 'export',
        assets: [mockAssets[0]],
        data: undefined
      });
    });

    it('should hide bulk actions bar when showBulkActions is false', () => {
      component.showBulkActions = false;
      fixture.detectChanges();
      
      const bulkActionsBar = debugElement.query(By.css('.bulk-actions-bar'));
      expect(bulkActionsBar).toBeFalsy();
    });
  });

  describe('Row Actions', () => {
    beforeEach(() => {
      component.assets = mockAssets;
      component.showActions = true;
      fixture.detectChanges();
    });

    it('should show action buttons', () => {
      const actionButtons = debugElement.queryAll(By.css('.action-btn'));
      expect(actionButtons.length).toBeGreaterThan(0);
    });

    it('should emit action events when buttons are clicked', () => {
      spyOn(component.actionClick, 'emit');
      
      component.onAction('view', mockAssets[0]);
      
      expect(component.actionClick.emit).toHaveBeenCalledWith({
        action: 'view',
        asset: mockAssets[0]
      });
    });

    it('should disable edit and delete buttons for read-only assets', () => {
      const readOnlyAsset = { ...mockAssets[0], readOnly: true };
      component.assets = [readOnlyAsset];
      component.ngOnChanges();
      fixture.detectChanges();
      
      const editButton = debugElement.query(By.css('.edit-btn'));
      const deleteButton = debugElement.query(By.css('.delete-btn'));
      
      expect(editButton.nativeElement.disabled).toBeTrue();
      expect(deleteButton.nativeElement.disabled).toBeTrue();
    });
  });

  describe('Row Click Events', () => {
    beforeEach(() => {
      component.assets = mockAssets;
      fixture.detectChanges();
    });

    it('should emit row click events', () => {
      spyOn(component.rowClick, 'emit');
      
      component.onRowClick(mockAssets[0]);
      
      expect(component.rowClick.emit).toHaveBeenCalledWith(mockAssets[0]);
    });

    it('should handle row clicks in template', () => {
      spyOn(component, 'onRowClick');
      
      const row = debugElement.query(By.css('.data-row'));
      row.nativeElement.click();
      
      expect(component.onRowClick).toHaveBeenCalled();
    });
  });

  describe('Pagination', () => {
    beforeEach(() => {
      component.assets = mockAssets;
      component.showPagination = true;
      component.totalElements = 100;
      fixture.detectChanges();
    });

    it('should show paginator when enabled', () => {
      const paginator = debugElement.query(By.css('mat-paginator'));
      expect(paginator).toBeTruthy();
    });

    it('should hide paginator when disabled', () => {
      component.showPagination = false;
      fixture.detectChanges();
      
      const paginator = debugElement.query(By.css('mat-paginator'));
      expect(paginator).toBeFalsy();
    });

    it('should emit page change events', () => {
      spyOn(component.pageChange, 'emit');
      
      const pageEvent = { pageIndex: 1, pageSize: 20, length: 100 };
      component.onPageChange(pageEvent);
      
      expect(component.pageChange.emit).toHaveBeenCalledWith(pageEvent);
    });
  });

  describe('Loading State', () => {
    it('should show loading state', () => {
      component.loading = true;
      fixture.detectChanges();
      
      const table = debugElement.query(By.css('.asset-table'));
      expect(table.nativeElement).toHaveClass('loading');
    });

    it('should display loading message in no data row', () => {
      component.loading = true;
      component.assets = [];
      component.ngOnChanges();
      fixture.detectChanges();
      
      const noDataTitle = debugElement.query(By.css('.no-data-title'));
      expect(noDataTitle.nativeElement.textContent.trim()).toBe('Loading assets...');
    });
  });

  describe('Empty State', () => {
    beforeEach(() => {
      component.assets = [];
      component.loading = false;
      component.ngOnChanges();
      fixture.detectChanges();
    });

    it('should show empty state when no assets', () => {
      const noDataRow = debugElement.query(By.css('.no-data-row'));
      expect(noDataRow).toBeTruthy();
    });

    it('should display empty state message', () => {
      const noDataTitle = debugElement.query(By.css('.no-data-title'));
      expect(noDataTitle.nativeElement.textContent.trim()).toBe('No assets found');
    });

    it('should display custom empty message when provided', () => {
      component.emptyMessage = 'Custom empty message';
      fixture.detectChanges();
      
      const noDataDescription = debugElement.query(By.css('.no-data-description'));
      expect(noDataDescription.nativeElement.textContent.trim()).toBe('Custom empty message');
    });
  });

  describe('Helper Methods', () => {
    it('should format asset type correctly', () => {
      expect(component.formatAssetType('NETWORK_DEVICE')).toBe('Network Device');
      expect(component.formatAssetType('SOFTWARE_LICENSE')).toBe('Software License');
    });

    it('should format date correctly', () => {
      const formatted = component.formatDate('2024-01-15');
      expect(formatted).toContain('2024');
      expect(formatted).toContain('Jan');
      expect(formatted).toContain('15');
    });
  });

  describe('Accessibility', () => {
    beforeEach(() => {
      component.assets = mockAssets;
      fixture.detectChanges();
    });

    it('should have proper table role', () => {
      const table = debugElement.query(By.css('.asset-table'));
      expect(table.nativeElement.getAttribute('role')).toBe('table');
    });

    it('should have proper ARIA label on table', () => {
      const table = debugElement.query(By.css('.asset-table'));
      expect(table.nativeElement.getAttribute('aria-label')).toBe('Assets table');
    });

    it('should have proper ARIA labels on action buttons', () => {
      const viewButton = debugElement.query(By.css('.view-btn'));
      expect(viewButton.nativeElement.getAttribute('aria-label')).toBe('View asset');
    });

    it('should have proper ARIA labels on rows', () => {
      const rows = debugElement.queryAll(By.css('.data-row'));
      expect(rows[0].nativeElement.getAttribute('aria-label')).toBe('Asset: Production Server 01');
    });
  });

  describe('Component Structure', () => {
    it('should render table container', () => {
      fixture.detectChanges();
      
      const container = debugElement.query(By.css('.table-container'));
      expect(container).toBeTruthy();
    });

    it('should have proper styling classes', () => {
      component.assets = mockAssets;
      fixture.detectChanges();
      
      const table = debugElement.query(By.css('.asset-table'));
      expect(table).toBeTruthy();
    });
  });
});