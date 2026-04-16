import { ComponentFixture, TestBed, fakeAsync, tick, discardPeriodicTasks } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { DashboardStatsComponent } from './dashboard-stats.component';
import { AssetService, AssetStats } from '../../services/asset.service';

describe('DashboardStatsComponent', () => {
  let component: DashboardStatsComponent;
  let fixture: ComponentFixture<DashboardStatsComponent>;
  let mockAssetService: jasmine.SpyObj<AssetService>;

  const mockStats: AssetStats = {
    totalAssets: 150,
    assetsInUse: 120,
    lastUpdated: '2024-01-15T10:30:00Z'
  };

  afterEach(() => {
    // Clean up any pending timers
    try {
      discardPeriodicTasks();
    } catch (e) {
      // Ignore if no timers to discard
    }
  });

  beforeEach(async () => {
    const assetServiceSpy = jasmine.createSpyObj('AssetService', ['getAssetStats']);

    await TestBed.configureTestingModule({
      imports: [
        DashboardStatsComponent,
        NoopAnimationsModule
      ],
      providers: [
        { provide: AssetService, useValue: assetServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardStatsComponent);
    component = fixture.componentInstance;
    mockAssetService = TestBed.inject(AssetService) as jasmine.SpyObj<AssetService>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Component Initialization', () => {
    it('should load stats on init', fakeAsync(() => {
      mockAssetService.getAssetStats.and.returnValue(of(mockStats));

      component.ngOnInit();
      tick();

      expect(mockAssetService.getAssetStats).toHaveBeenCalled();
      expect(component.stats$.value).toEqual(mockStats);
      expect(component.loading$.value).toBeFalse();
      
      // Clean up the interval
      component.ngOnDestroy();
      discardPeriodicTasks();
    }));

    it('should handle loading state correctly', fakeAsync(() => {
      mockAssetService.getAssetStats.and.returnValue(of(mockStats));

      // Check initial loading state before ngOnInit
      expect(component.loading$.value).toBeFalse();
      
      component.ngOnInit();
      
      // Loading should be true during the request
      expect(component.loading$.value).toBeTrue();
      
      tick();
      
      // After response, loading should be false
      expect(component.loading$.value).toBeFalse();
      
      // Clean up the interval
      component.ngOnDestroy();
      discardPeriodicTasks();
    }));

    it('should handle error state correctly', fakeAsync(() => {
      const errorMessage = 'Network error';
      mockAssetService.getAssetStats.and.returnValue(throwError(() => new Error(errorMessage)));

      spyOn(console, 'error');
      component.ngOnInit();
      tick();

      expect(component.error$.value).toBe('Failed to load statistics');
      expect(component.loading$.value).toBeFalse();
      expect(console.error).toHaveBeenCalled();
      
      // Clean up the interval
      component.ngOnDestroy();
      discardPeriodicTasks();
    }));
  });

  describe('Stats Calculations', () => {
    it('should calculate available assets correctly', () => {
      const result = component.getAvailableAssets(mockStats);
      expect(result).toBe(30); // 150 - 120
    });

    it('should calculate usage percentage correctly', () => {
      const result = component.getUsagePercentage(mockStats);
      expect(result).toBe(80); // (120 / 150) * 100
    });

    it('should handle zero total assets for percentage calculation', () => {
      const zeroStats: AssetStats = {
        totalAssets: 0,
        assetsInUse: 0,
        lastUpdated: '2024-01-15T10:30:00Z'
      };

      const result = component.getUsagePercentage(zeroStats);
      expect(result).toBe(0);
    });
  });

  describe('Manual Refresh', () => {
    it('should refresh stats when refreshStats is called', fakeAsync(() => {
      mockAssetService.getAssetStats.and.returnValue(of(mockStats));

      component.refreshStats();
      tick();

      expect(mockAssetService.getAssetStats).toHaveBeenCalled();
      expect(component.stats$.value).toEqual(mockStats);
      
      discardPeriodicTasks();
    }));

    it('should handle refresh errors', fakeAsync(() => {
      mockAssetService.getAssetStats.and.returnValue(throwError(() => new Error('Refresh failed')));

      spyOn(console, 'error');
      component.refreshStats();
      tick();

      expect(component.error$.value).toBe('Failed to load statistics');
      expect(console.error).toHaveBeenCalled();
      
      discardPeriodicTasks();
    }));
  });

  describe('Real-time Updates', () => {
    it('should set up interval for automatic updates', fakeAsync(() => {
      mockAssetService.getAssetStats.and.returnValue(of(mockStats));

      component.ngOnInit();
      tick(); // Initial call
      
      expect(mockAssetService.getAssetStats).toHaveBeenCalledTimes(1);

      // Advance time by 30 seconds (refresh interval)
      tick(30000);
      
      expect(mockAssetService.getAssetStats).toHaveBeenCalledTimes(2);
      
      // Clean up the interval
      component.ngOnDestroy();
      discardPeriodicTasks();
    }));
  });

  describe('Component Cleanup', () => {
    it('should clean up subscriptions on destroy', () => {
      spyOn(component['destroy$'], 'next');
      spyOn(component['destroy$'], 'complete');

      component.ngOnDestroy();

      expect(component['destroy$'].next).toHaveBeenCalled();
      expect(component['destroy$'].complete).toHaveBeenCalled();
    });
  });

  describe('Template Integration', () => {
    beforeEach(fakeAsync(() => {
      mockAssetService.getAssetStats.and.returnValue(of(mockStats));
      // Prevent automatic initialization
      spyOn(component, 'ngOnInit');
    }));

    afterEach(fakeAsync(() => {
      discardPeriodicTasks();
    }));

    it('should display loading spinner when loading', fakeAsync(() => {
      component.loading$.next(true);
      fixture.detectChanges();

      const loadingElement = fixture.nativeElement.querySelector('.loading-container');
      expect(loadingElement).toBeTruthy();

      const spinner = fixture.nativeElement.querySelector('mat-spinner');
      expect(spinner).toBeTruthy();
    }));

    it('should display error message when error occurs', fakeAsync(() => {
      component.error$.next('Test error message');
      fixture.detectChanges();

      const errorElement = fixture.nativeElement.querySelector('.error-container');
      expect(errorElement).toBeTruthy();

      const errorText = fixture.nativeElement.querySelector('.error-text');
      expect(errorText.textContent.trim()).toBe('Test error message');
    }));

    it('should display stats when loaded', fakeAsync(() => {
      component.stats$.next(mockStats);
      fixture.detectChanges();

      const statsContent = fixture.nativeElement.querySelector('.stats-content');
      expect(statsContent).toBeTruthy();

      const statCards = fixture.nativeElement.querySelectorAll('.stat-card');
      expect(statCards.length).toBe(3);
    }));

    it('should display correct stat values', fakeAsync(() => {
      component.stats$.next(mockStats);
      fixture.detectChanges();

      const statValues = fixture.nativeElement.querySelectorAll('.stat-value');
      expect(statValues[0].textContent.trim()).toBe('150'); // Total assets
      expect(statValues[1].textContent.trim()).toBe('120'); // Assets in use
      expect(statValues[2].textContent.trim()).toBe('30');  // Available assets
    }));

    it('should call refreshStats when refresh button is clicked', fakeAsync(() => {
      spyOn(component, 'refreshStats');
      component.stats$.next(mockStats);
      fixture.detectChanges();

      const refreshButton = fixture.nativeElement.querySelector('.refresh-button');
      refreshButton.click();

      expect(component.refreshStats).toHaveBeenCalled();
    }));

    it('should disable refresh button when loading', fakeAsync(() => {
      component.loading$.next(true);
      component.stats$.next(mockStats);
      fixture.detectChanges();

      const refreshButton = fixture.nativeElement.querySelector('.refresh-button');
      expect(refreshButton.disabled).toBeTrue();
    }));
  });

  describe('Accessibility', () => {
    beforeEach(fakeAsync(() => {
      mockAssetService.getAssetStats.and.returnValue(of(mockStats));
      // Prevent automatic initialization
      spyOn(component, 'ngOnInit');
    }));

    afterEach(fakeAsync(() => {
      discardPeriodicTasks();
    }));

    it('should have proper ARIA labels', fakeAsync(() => {
      component.stats$.next(mockStats);
      fixture.detectChanges();

      const refreshButton = fixture.nativeElement.querySelector('.refresh-button');
      expect(refreshButton.getAttribute('aria-label')).toBe('Refresh statistics');
    }));

    it('should have proper semantic structure', fakeAsync(() => {
      component.stats$.next(mockStats);
      fixture.detectChanges();

      const title = fixture.nativeElement.querySelector('.stats-title');
      expect(title.tagName.toLowerCase()).toBe('h2');
    }));

    it('should hide decorative elements from screen readers', fakeAsync(() => {
      component.stats$.next(mockStats);
      fixture.detectChanges();

      const geometricAccent = fixture.nativeElement.querySelector('.geometric-accent');
      expect(geometricAccent.getAttribute('aria-hidden')).toBe('true');
    }));
  });
});