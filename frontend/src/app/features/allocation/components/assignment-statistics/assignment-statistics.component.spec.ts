import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';

import { AssignmentStatisticsComponent } from './assignment-statistics.component';
import { AllocationService } from '../../../../core/services/allocation.service';
import { AssignmentStatistics } from '../../../../shared/models/allocation.model';

describe('AssignmentStatisticsComponent', () => {
  let component: AssignmentStatisticsComponent;
  let fixture: ComponentFixture<AssignmentStatisticsComponent>;
  let mockAllocationService: jasmine.SpyObj<AllocationService>;

  const mockStatistics: AssignmentStatistics = {
    totalAssigned: 150,
    userAssignments: 100,
    locationAssignments: 50,
    availableAssets: {
      inUse: 120,
      deployed: 80,
      storage: 30
    },
    topUsers: [
      { name: 'John Doe', count: 15 },
      { name: 'Jane Smith', count: 12 },
      { name: 'Bob Johnson', count: 10 }
    ],
    topLocations: [
      { name: 'Building A', count: 25 },
      { name: 'Building B', count: 20 },
      { name: 'Data Center', count: 15 }
    ]
  };

  beforeEach(async () => {
    mockAllocationService = jasmine.createSpyObj('AllocationService', ['getStatistics']);

    await TestBed.configureTestingModule({
      imports: [
        AssignmentStatisticsComponent,
        NoopAnimationsModule
      ],
      providers: [
        { provide: AllocationService, useValue: mockAllocationService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AssignmentStatisticsComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load statistics on init', () => {
    mockAllocationService.getStatistics.and.returnValue(of(mockStatistics));
    fixture.detectChanges();

    expect(mockAllocationService.getStatistics).toHaveBeenCalled();
    expect(component.statistics$.value).toEqual(mockStatistics);
  });

  it('should set loading state while fetching statistics', () => {
    mockAllocationService.getStatistics.and.returnValue(of(mockStatistics));
    
    expect(component.loading$.value).toBe(false);
    
    fixture.detectChanges();
    
    // After loading completes
    expect(component.loading$.value).toBe(false);
  });

  it('should handle error when loading statistics fails', () => {
    const errorMessage = 'Failed to load statistics';
    mockAllocationService.getStatistics.and.returnValue(
      throwError(() => new Error(errorMessage))
    );

    fixture.detectChanges();

    expect(component.error$.value).toBe(errorMessage);
    expect(component.loading$.value).toBe(false);
  });

  describe('Percentage Calculation', () => {
    it('should calculate percentage correctly', () => {
      const percentage = component.calculatePercentage(100, 150);
      expect(percentage).toBe(67);
    });

    it('should return 0 when total is 0', () => {
      const percentage = component.calculatePercentage(50, 0);
      expect(percentage).toBe(0);
    });

    it('should round to nearest integer', () => {
      const percentage = component.calculatePercentage(33, 100);
      expect(percentage).toBe(33);
    });
  });

  describe('Bar Chart Calculations', () => {
    it('should get maximum count from assignees', () => {
      const assignees = [
        { name: 'User 1', count: 15 },
        { name: 'User 2', count: 25 },
        { name: 'User 3', count: 10 }
      ];

      const maxCount = component.getMaxCount(assignees);
      expect(maxCount).toBe(25);
    });

    it('should return 1 for empty assignees array', () => {
      const maxCount = component.getMaxCount([]);
      expect(maxCount).toBe(1);
    });

    it('should calculate bar width percentage correctly', () => {
      const width = component.getBarWidth(15, 25);
      expect(width).toBe(60);
    });

    it('should return 0 when max count is 0', () => {
      const width = component.getBarWidth(10, 0);
      expect(width).toBe(0);
    });

    it('should return 100 for maximum value', () => {
      const width = component.getBarWidth(25, 25);
      expect(width).toBe(100);
    });
  });

  describe('Reload Statistics', () => {
    it('should reload statistics when loadStatistics is called', () => {
      mockAllocationService.getStatistics.and.returnValue(of(mockStatistics));
      fixture.detectChanges();

      // Clear previous calls
      mockAllocationService.getStatistics.calls.reset();

      component.loadStatistics();

      expect(mockAllocationService.getStatistics).toHaveBeenCalled();
    });
  });
});
