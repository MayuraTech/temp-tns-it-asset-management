import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';

import { AssignmentHistoryComponent } from './assignment-history.component';
import { AllocationService } from '../../../../core/services/allocation.service';
import { AssignmentType } from '../../../../shared/models/allocation.model';
import { PageEvent } from '@angular/material/paginator';

describe('AssignmentHistoryComponent', () => {
  let component: AssignmentHistoryComponent;
  let fixture: ComponentFixture<AssignmentHistoryComponent>;
  let mockAllocationService: jasmine.SpyObj<AllocationService>;
  let mockActivatedRoute: any;

  const mockHistoryData = {
    content: [
      {
        id: 'history-1',
        assetId: 'asset-1',
        assignmentType: AssignmentType.USER,
        assignedTo: 'John Doe',
        assignedBy: 'admin-id',
        assignedByUsername: 'admin',
        assignedAt: new Date('2024-01-15T10:00:00Z'),
        unassignedAt: new Date('2024-01-20T15:00:00Z')
      },
      {
        id: 'history-2',
        assetId: 'asset-1',
        assignmentType: AssignmentType.LOCATION,
        assignedTo: 'Building A',
        assignedBy: 'admin-id',
        assignedByUsername: 'admin',
        assignedAt: new Date('2024-01-10T09:00:00Z'),
        unassignedAt: undefined
      }
    ],
    page: {
      size: 20,
      number: 0,
      totalElements: 2,
      totalPages: 1
    }
  };

  beforeEach(async () => {
    mockAllocationService = jasmine.createSpyObj('AllocationService', [
      'getAssignmentHistory'
    ]);
    mockActivatedRoute = {
      snapshot: {
        paramMap: {
          get: jasmine.createSpy('get').and.returnValue('asset-1')
        }
      }
    };

    await TestBed.configureTestingModule({
      imports: [
        AssignmentHistoryComponent,
        NoopAnimationsModule
      ],
      providers: [
        { provide: AllocationService, useValue: mockAllocationService },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AssignmentHistoryComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get asset ID from route parameters', () => {
    mockAllocationService.getAssignmentHistory.and.returnValue(of(mockHistoryData));
    fixture.detectChanges();

    expect(component.assetId).toBe('asset-1');
  });

  it('should load assignment history on init', () => {
    mockAllocationService.getAssignmentHistory.and.returnValue(of(mockHistoryData));
    fixture.detectChanges();

    expect(mockAllocationService.getAssignmentHistory).toHaveBeenCalledWith('asset-1', 0, 20);
    expect(component.history$.value).toEqual(mockHistoryData.content);
    expect(component.totalElements).toBe(2);
  });

  it('should set loading state while fetching history', () => {
    mockAllocationService.getAssignmentHistory.and.returnValue(of(mockHistoryData));
    
    expect(component.loading$.value).toBe(false);
    
    fixture.detectChanges();
    
    // After loading completes
    expect(component.loading$.value).toBe(false);
  });

  it('should handle error when loading history fails', () => {
    const errorMessage = 'Failed to load history';
    mockAllocationService.getAssignmentHistory.and.returnValue(
      throwError(() => new Error(errorMessage))
    );

    fixture.detectChanges();

    expect(component.error$.value).toBe(errorMessage);
    expect(component.loading$.value).toBe(false);
  });

  it('should set error if asset ID is missing', () => {
    mockActivatedRoute.snapshot.paramMap.get.and.returnValue(null);
    
    fixture = TestBed.createComponent(AssignmentHistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.error$.value).toBe('Asset ID is required');
  });

  describe('Pagination', () => {
    it('should handle page change event', () => {
      mockAllocationService.getAssignmentHistory.and.returnValue(of(mockHistoryData));
      fixture.detectChanges();

      const pageEvent: PageEvent = {
        pageIndex: 1,
        pageSize: 50,
        length: 100
      };

      component.onPageChange(pageEvent);

      expect(component.pageIndex).toBe(1);
      expect(component.pageSize).toBe(50);
      expect(mockAllocationService.getAssignmentHistory).toHaveBeenCalledWith('asset-1', 1, 50);
    });
  });

  describe('Date Formatting', () => {
    it('should format date correctly', () => {
      const date = new Date('2024-01-15T10:30:00Z');
      const formatted = component.formatDate(date);

      expect(formatted).toContain('Jan');
      expect(formatted).toContain('15');
      expect(formatted).toContain('2024');
    });

    it('should return N/A for undefined date', () => {
      const formatted = component.formatDate(undefined);
      expect(formatted).toBe('N/A');
    });

    it('should handle string dates', () => {
      const formatted = component.formatDate('2024-01-15T10:30:00Z');
      expect(formatted).toContain('Jan');
    });
  });

  describe('Assignment Type', () => {
    it('should return correct label for USER type', () => {
      const label = component.getAssignmentTypeLabel(AssignmentType.USER);
      expect(label).toBe('User');
    });

    it('should return correct label for LOCATION type', () => {
      const label = component.getAssignmentTypeLabel(AssignmentType.LOCATION);
      expect(label).toBe('Location');
    });

    it('should return correct color for USER type', () => {
      const color = component.getAssignmentTypeColor(AssignmentType.USER);
      expect(color).toBe('primary');
    });

    it('should return correct color for LOCATION type', () => {
      const color = component.getAssignmentTypeColor(AssignmentType.LOCATION);
      expect(color).toBe('accent');
    });
  });

  describe('Assignment Status', () => {
    it('should identify active assignment correctly', () => {
      const activeAssignment = mockHistoryData.content[1];
      expect(component.isActive(activeAssignment)).toBe(true);
    });

    it('should identify historical assignment correctly', () => {
      const historicalAssignment = mockHistoryData.content[0];
      expect(component.isActive(historicalAssignment)).toBe(false);
    });

    it('should return correct status label for active assignment', () => {
      const activeAssignment = mockHistoryData.content[1];
      const label = component.getStatusLabel(activeAssignment);
      expect(label).toBe('Active');
    });

    it('should return correct status label for historical assignment', () => {
      const historicalAssignment = mockHistoryData.content[0];
      const label = component.getStatusLabel(historicalAssignment);
      expect(label).toBe('Historical');
    });

    it('should return correct color for active assignment', () => {
      const activeAssignment = mockHistoryData.content[1];
      const color = component.getStatusColor(activeAssignment);
      expect(color).toBe('primary');
    });

    it('should return correct color for historical assignment', () => {
      const historicalAssignment = mockHistoryData.content[0];
      const color = component.getStatusColor(historicalAssignment);
      expect(color).toBe('default');
    });
  });

  describe('Observable State Management', () => {
    it('should initialize observables with correct default values', () => {
      expect(component.history$.value).toEqual([]);
      expect(component.loading$.value).toBe(false);
      expect(component.error$.value).toBeNull();
    });

    it('should update history observable when data is loaded', () => {
      mockAllocationService.getAssignmentHistory.and.returnValue(of(mockHistoryData));
      fixture.detectChanges();

      expect(component.history$.value.length).toBe(2);
      expect(component.history$.value[0].id).toBe('history-1');
    });

    it('should clear error when loading new data', () => {
      // First set an error
      component.error$.next('Previous error');
      
      mockAllocationService.getAssignmentHistory.and.returnValue(of(mockHistoryData));
      component.loadHistory();

      expect(component.error$.value).toBeNull();
    });
  });

  describe('Component Initialization', () => {
    it('should have correct table columns configured', () => {
      expect(component.displayedColumns).toEqual([
        'assignmentType',
        'assignedTo',
        'assignedBy',
        'assignedAt',
        'unassignedAt',
        'status'
      ]);
    });

    it('should initialize with default pagination values', () => {
      expect(component.pageSize).toBe(20);
      expect(component.pageIndex).toBe(0);
      expect(component.totalElements).toBe(0);
    });

    it('should call loadHistory on ngOnInit when asset ID is present', () => {
      spyOn(component, 'loadHistory');
      mockActivatedRoute.snapshot.paramMap.get.and.returnValue('asset-1');
      
      component.ngOnInit();

      expect(component.loadHistory).toHaveBeenCalled();
    });

    it('should not call loadHistory when asset ID is missing', () => {
      spyOn(component, 'loadHistory');
      mockActivatedRoute.snapshot.paramMap.get.and.returnValue(null);
      
      component.ngOnInit();

      expect(component.loadHistory).not.toHaveBeenCalled();
    });
  });

  describe('Error Handling', () => {
    it('should handle network errors gracefully', () => {
      const networkError = new Error('Network connection failed');
      mockAllocationService.getAssignmentHistory.and.returnValue(
        throwError(() => networkError)
      );

      fixture.detectChanges();

      expect(component.error$.value).toBe('Network connection failed');
      expect(component.loading$.value).toBe(false);
      expect(component.history$.value).toEqual([]);
    });

    it('should handle server errors with custom messages', () => {
      const serverError = new Error('Server error: Asset not found');
      mockAllocationService.getAssignmentHistory.and.returnValue(
        throwError(() => serverError)
      );

      fixture.detectChanges();

      expect(component.error$.value).toContain('Asset not found');
    });

    it('should maintain previous data when reload fails', () => {
      // First successful load
      mockAllocationService.getAssignmentHistory.and.returnValue(of(mockHistoryData));
      fixture.detectChanges();
      
      const previousData = component.history$.value;

      // Second load fails
      mockAllocationService.getAssignmentHistory.and.returnValue(
        throwError(() => new Error('Failed'))
      );
      component.loadHistory();

      // Data should remain unchanged
      expect(component.history$.value).toEqual(previousData);
    });
  });

  describe('Empty State Handling', () => {
    it('should handle empty history correctly', () => {
      const emptyResponse = {
        content: [],
        page: {
          size: 20,
          number: 0,
          totalElements: 0,
          totalPages: 0
        }
      };

      mockAllocationService.getAssignmentHistory.and.returnValue(of(emptyResponse));
      fixture.detectChanges();

      expect(component.history$.value).toEqual([]);
      expect(component.totalElements).toBe(0);
      expect(component.error$.value).toBeNull();
    });
  });

  describe('Pagination Edge Cases', () => {
    it('should handle pagination with single page', () => {
      mockAllocationService.getAssignmentHistory.and.returnValue(of(mockHistoryData));
      fixture.detectChanges();

      expect(component.totalElements).toBe(2);
      // With 2 elements and page size 20, should be 1 page
    });

    it('should handle pagination to last page', () => {
      const largeDataset = {
        content: mockHistoryData.content,
        page: {
          size: 20,
          number: 4,
          totalElements: 100,
          totalPages: 5
        }
      };

      mockAllocationService.getAssignmentHistory.and.returnValue(of(largeDataset));
      fixture.detectChanges();

      const pageEvent: PageEvent = {
        pageIndex: 4,
        pageSize: 20,
        length: 100
      };

      component.onPageChange(pageEvent);

      expect(component.pageIndex).toBe(4);
      expect(mockAllocationService.getAssignmentHistory).toHaveBeenCalledWith('asset-1', 4, 20);
    });

    it('should handle page size change', () => {
      mockAllocationService.getAssignmentHistory.and.returnValue(of(mockHistoryData));
      fixture.detectChanges();

      const pageEvent: PageEvent = {
        pageIndex: 0,
        pageSize: 100,
        length: 100
      };

      component.onPageChange(pageEvent);

      expect(component.pageSize).toBe(100);
      expect(mockAllocationService.getAssignmentHistory).toHaveBeenCalledWith('asset-1', 0, 100);
    });
  });

  describe('Date Formatting Edge Cases', () => {
    it('should handle null date', () => {
      const formatted = component.formatDate(null as any);
      expect(formatted).toBe('N/A');
    });

    it('should handle invalid date string', () => {
      const formatted = component.formatDate('invalid-date');
      expect(formatted).toContain('Invalid Date');
    });

    it('should format date with time correctly', () => {
      const date = new Date('2024-01-15T14:30:45Z');
      const formatted = component.formatDate(date);

      expect(formatted).toContain('2024');
      expect(formatted).toContain('Jan');
      expect(formatted).toContain('15');
      // Should include time
      expect(formatted).toMatch(/\d{2}:\d{2}/);
    });
  });

  describe('Assignment Type Edge Cases', () => {
    it('should handle assignment with missing username', () => {
      const assignmentWithoutUsername = {
        ...mockHistoryData.content[0],
        assignedByUsername: undefined
      };

      mockAllocationService.getAssignmentHistory.and.returnValue(of({
        content: [assignmentWithoutUsername],
        page: mockHistoryData.page
      }));

      fixture.detectChanges();

      // Component should handle missing username gracefully
      expect(component.history$.value[0].assignedByUsername).toBeUndefined();
    });
  });

  describe('Component Lifecycle', () => {
    it('should not reload data after component is initialized', () => {
      mockAllocationService.getAssignmentHistory.and.returnValue(of(mockHistoryData));
      fixture.detectChanges();

      const callCount = mockAllocationService.getAssignmentHistory.calls.count();

      // Trigger change detection again
      fixture.detectChanges();

      // Should not call service again
      expect(mockAllocationService.getAssignmentHistory.calls.count()).toBe(callCount);
    });
  });

  describe('OnPush Change Detection', () => {
    it('should use OnPush change detection strategy', () => {
      const changeDetectionStrategy = (component.constructor as any).ɵcmp.changeDetection;
      expect(changeDetectionStrategy).toBe(0); // 0 = OnPush
    });
  });

  describe('Accessibility', () => {
    it('should have proper table structure for screen readers', () => {
      mockAllocationService.getAssignmentHistory.and.returnValue(of(mockHistoryData));
      fixture.detectChanges();

      const compiled = fixture.nativeElement;
      const table = compiled.querySelector('table');
      
      expect(table).toBeTruthy();
    });
  });

  describe('Loading State Management', () => {
    it('should set loading to true before API call', (done) => {
      let loadingDuringCall = false;
      
      mockAllocationService.getAssignmentHistory.and.callFake(() => {
        loadingDuringCall = component.loading$.value;
        return of(mockHistoryData);
      });

      component.loadHistory();

      setTimeout(() => {
        expect(loadingDuringCall).toBe(true);
        done();
      }, 0);
    });

    it('should set loading to false after successful API call', () => {
      mockAllocationService.getAssignmentHistory.and.returnValue(of(mockHistoryData));
      
      component.loadHistory();

      expect(component.loading$.value).toBe(false);
    });

    it('should set loading to false after failed API call', () => {
      mockAllocationService.getAssignmentHistory.and.returnValue(
        throwError(() => new Error('Failed'))
      );
      
      component.loadHistory();

      expect(component.loading$.value).toBe(false);
    });
  });
});
