import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError, BehaviorSubject } from 'rxjs';
import { AssetDetailComponent } from './asset-detail.component';
import { AssetService } from '../../services/asset.service';
import { Asset, AssetType, LifecycleStatus } from '../../models';

describe('AssetDetailComponent', () => {
  let component: AssetDetailComponent;
  let fixture: ComponentFixture<AssetDetailComponent>;
  let mockAssetService: jasmine.SpyObj<AssetService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockActivatedRoute: any;
  let paramMapSubject: BehaviorSubject<any>;

  const mockAsset: Asset = {
    id: '550e8400-e29b-41d4-a716-446655440000',
    assetType: AssetType.SERVER,
    name: 'Test Server',
    serialNumber: 'SRV-001',
    acquisitionDate: '2024-01-15',
    status: LifecycleStatus.IN_USE,
    location: 'Data Center A',
    assignedUser: 'John Doe',
    assignedUserEmail: 'john.doe@example.com',
    assignmentDate: '2024-01-20T10:00:00Z',
    locationUpdateDate: '2024-01-20T10:00:00Z',
    notes: 'Test notes',
    customFields: '{}',
    createdAt: '2024-01-15T10:00:00Z',
    createdBy: 'admin',
    updatedAt: '2024-01-20T10:00:00Z',
    updatedBy: 'admin',
    readOnly: false
  };

  beforeEach(async () => {
    // Create spy objects
    mockAssetService = jasmine.createSpyObj('AssetService', [
      'getAsset',
      'updateStatus',
      'deleteAsset'
    ]);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    
    // Create paramMap subject for route params
    paramMapSubject = new BehaviorSubject({
      get: (key: string) => key === 'id' ? mockAsset.id : null
    });
    
    mockActivatedRoute = {
      paramMap: paramMapSubject.asObservable()
    };

    await TestBed.configureTestingModule({
      declarations: [AssetDetailComponent],
      providers: [
        { provide: AssetService, useValue: mockAssetService },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AssetDetailComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    fixture.destroy();
  });

  describe('Component Initialization', () => {
    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should load asset on init when route param is present', () => {
      mockAssetService.getAsset.and.returnValue(of(mockAsset));

      fixture.detectChanges(); // Triggers ngOnInit

      expect(mockAssetService.getAsset).toHaveBeenCalledWith(mockAsset.id);
      expect(component.asset).toEqual(mockAsset);
      expect(component.loading).toBe(false);
      expect(component.error).toBeNull();
    });

    it('should handle error when loading asset fails', () => {
      const errorMessage = 'Failed to load asset';
      mockAssetService.getAsset.and.returnValue(
        throwError(() => new Error(errorMessage))
      );

      fixture.detectChanges();

      expect(component.asset).toBeNull();
      expect(component.loading).toBe(false);
      expect(component.error).toBe(errorMessage);
    });

    it('should not load asset if no route param', () => {
      paramMapSubject.next({
        get: () => null
      });

      fixture.detectChanges();

      expect(mockAssetService.getAsset).not.toHaveBeenCalled();
    });
  });

  describe('Navigation', () => {
    it('should navigate back to assets list', () => {
      component.goBack();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/assets']);
    });

    it('should navigate to edit page', () => {
      component.assetId = mockAsset.id;

      component.editAsset();

      expect(mockRouter.navigate).toHaveBeenCalledWith([
        '/assets',
        mockAsset.id,
        'edit'
      ]);
    });

    it('should not navigate to edit if no asset ID', () => {
      component.assetId = null;

      component.editAsset();

      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });
  });

  describe('Status Change Dialog', () => {
    beforeEach(() => {
      component.asset = mockAsset;
    });

    it('should open status dialog', () => {
      component.openStatusDialog();

      expect(component.showStatusDialog).toBe(true);
      expect(component.selectedStatus).toBe(mockAsset.status);
    });

    it('should close status dialog', () => {
      component.showStatusDialog = true;
      component.selectedStatus = LifecycleStatus.MAINTENANCE;

      component.closeStatusDialog();

      expect(component.showStatusDialog).toBe(false);
      expect(component.selectedStatus).toBeNull();
    });

    it('should confirm status change successfully', () => {
      const updatedAsset = { ...mockAsset, status: LifecycleStatus.MAINTENANCE };
      mockAssetService.updateStatus.and.returnValue(of(updatedAsset));
      
      component.assetId = mockAsset.id;
      component.selectedStatus = LifecycleStatus.MAINTENANCE;

      component.confirmStatusChange();

      expect(mockAssetService.updateStatus).toHaveBeenCalledWith(
        mockAsset.id,
        LifecycleStatus.MAINTENANCE
      );
      expect(component.asset).toEqual(updatedAsset);
      expect(component.showStatusDialog).toBe(false);
    });

    it('should handle error when status change fails', () => {
      const errorMessage = 'Failed to update status';
      mockAssetService.updateStatus.and.returnValue(
        throwError(() => new Error(errorMessage))
      );
      
      component.assetId = mockAsset.id;
      component.selectedStatus = LifecycleStatus.MAINTENANCE;

      component.confirmStatusChange();

      expect(component.error).toBe(errorMessage);
    });

    it('should not confirm status change without asset ID', () => {
      component.assetId = null;
      component.selectedStatus = LifecycleStatus.MAINTENANCE;

      component.confirmStatusChange();

      expect(mockAssetService.updateStatus).not.toHaveBeenCalled();
    });

    it('should not confirm status change without selected status', () => {
      component.assetId = mockAsset.id;
      component.selectedStatus = null;

      component.confirmStatusChange();

      expect(mockAssetService.updateStatus).not.toHaveBeenCalled();
    });
  });

  describe('Delete Confirmation Dialog', () => {
    it('should open delete dialog', () => {
      component.openDeleteDialog();

      expect(component.showDeleteDialog).toBe(true);
    });

    it('should close delete dialog', () => {
      component.showDeleteDialog = true;

      component.closeDeleteDialog();

      expect(component.showDeleteDialog).toBe(false);
    });

    it('should confirm delete successfully', () => {
      mockAssetService.deleteAsset.and.returnValue(of(void 0));
      component.assetId = mockAsset.id;

      component.confirmDelete();

      expect(mockAssetService.deleteAsset).toHaveBeenCalledWith(mockAsset.id);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/assets']);
    });

    it('should handle error when delete fails', () => {
      const errorMessage = 'Failed to delete asset';
      mockAssetService.deleteAsset.and.returnValue(
        throwError(() => new Error(errorMessage))
      );
      component.assetId = mockAsset.id;
      component.showDeleteDialog = true;

      component.confirmDelete();

      expect(component.error).toBe(errorMessage);
      expect(component.showDeleteDialog).toBe(false);
    });

    it('should not confirm delete without asset ID', () => {
      component.assetId = null;

      component.confirmDelete();

      expect(mockAssetService.deleteAsset).not.toHaveBeenCalled();
    });
  });

  describe('Helper Methods', () => {
    it('should get asset type label', () => {
      expect(component.getAssetTypeLabel(AssetType.SERVER)).toBe('SERVER');
      expect(component.getAssetTypeLabel(AssetType.NETWORK_DEVICE)).toBe('NETWORK DEVICE');
    });

    it('should get status label', () => {
      expect(component.getStatusLabel(LifecycleStatus.IN_USE)).toBe('IN USE');
      expect(component.getStatusLabel(LifecycleStatus.ORDERED)).toBe('ORDERED');
    });

    it('should get status class', () => {
      expect(component.getStatusClass(LifecycleStatus.ORDERED)).toBe('status-ordered');
      expect(component.getStatusClass(LifecycleStatus.IN_USE)).toBe('status-in-use');
      expect(component.getStatusClass(LifecycleStatus.RETIRED)).toBe('status-retired');
    });

    it('should get asset icon', () => {
      expect(component.getAssetIcon(AssetType.SERVER)).toBe('server');
      expect(component.getAssetIcon(AssetType.LAPTOP)).toBe('laptop');
      expect(component.getAssetIcon(AssetType.MONITOR)).toBe('monitor');
    });

    it('should format date correctly', () => {
      const result = component.formatDate('2024-01-15');
      expect(result).toContain('Jan');
      expect(result).toContain('15');
      expect(result).toContain('2024');
    });

    it('should return N/A for undefined date', () => {
      expect(component.formatDate(undefined)).toBe('N/A');
    });

    it('should format datetime correctly', () => {
      const result = component.formatDateTime('2024-01-15T10:30:00Z');
      expect(result).toContain('Jan');
      expect(result).toContain('15');
      expect(result).toContain('2024');
    });

    it('should return N/A for undefined datetime', () => {
      expect(component.formatDateTime(undefined)).toBe('N/A');
    });
  });

  describe('Lifecycle History', () => {
    it('should generate lifecycle history from asset', () => {
      mockAssetService.getAsset.and.returnValue(of(mockAsset));

      fixture.detectChanges();

      expect(component.lifecycleHistory.length).toBeGreaterThan(0);
      expect(component.lifecycleHistory[0].status).toBe(mockAsset.status);
    });
  });

  describe('Assignment History', () => {
    it('should generate assignment history when asset is assigned', () => {
      mockAssetService.getAsset.and.returnValue(of(mockAsset));

      fixture.detectChanges();

      expect(component.assignmentHistory.length).toBe(1);
      expect(component.assignmentHistory[0].userName).toBe(mockAsset.assignedUser!);
      expect(component.assignmentHistory[0].isCurrent).toBe(true);
    });

    it('should have empty assignment history when asset is not assigned', () => {
      const unassignedAsset = { ...mockAsset, assignedUser: undefined };
      mockAssetService.getAsset.and.returnValue(of(unassignedAsset));

      fixture.detectChanges();

      expect(component.assignmentHistory.length).toBe(0);
    });
  });

  describe('Placeholder Methods', () => {
    it('should log generate report action', () => {
      spyOn(console, 'log');
      component.assetId = mockAsset.id;

      component.generateReport();

      expect(console.log).toHaveBeenCalledWith('Generate report for asset:', mockAsset.id);
    });

    it('should log reassign asset action', () => {
      spyOn(console, 'log');
      component.assetId = mockAsset.id;

      component.reassignAsset();

      expect(console.log).toHaveBeenCalledWith('Reassign asset:', mockAsset.id);
    });

    it('should log view full history action', () => {
      spyOn(console, 'log');
      component.assetId = mockAsset.id;

      component.viewFullHistory();

      expect(console.log).toHaveBeenCalledWith('View full history for asset:', mockAsset.id);
    });
  });

  describe('Component Cleanup', () => {
    it('should unsubscribe on destroy', () => {
      const destroySpy = spyOn(component['destroy$'], 'next');
      const completeSpy = spyOn(component['destroy$'], 'complete');

      component.ngOnDestroy();

      expect(destroySpy).toHaveBeenCalled();
      expect(completeSpy).toHaveBeenCalled();
    });
  });

  describe('Available Statuses', () => {
    it('should have all lifecycle statuses available', () => {
      expect(component.availableStatuses).toEqual(Object.values(LifecycleStatus));
      expect(component.availableStatuses.length).toBe(7);
    });
  });
});
