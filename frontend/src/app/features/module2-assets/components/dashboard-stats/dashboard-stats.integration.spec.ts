import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { AssetInventoryComponent } from '../asset-inventory/asset-inventory.component';
import { AssetService, AssetStats } from '../../services/asset.service';

describe('Dashboard Stats Widget Integration', () => {
  let component: AssetInventoryComponent;
  let fixture: ComponentFixture<AssetInventoryComponent>;
  let mockAssetService: jasmine.SpyObj<AssetService>;

  const mockStats: AssetStats = {
    totalAssets: 150,
    assetsInUse: 120,
    lastUpdated: '2024-01-15T10:30:00Z'
  };

  beforeEach(async () => {
    const assetServiceSpy = jasmine.createSpyObj('AssetService', [
      'getAssets',
      'getAssetStats',
      'searchAssets'
    ]);

    await TestBed.configureTestingModule({
      imports: [
        AssetInventoryComponent,
        NoopAnimationsModule
      ],
      providers: [
        { provide: AssetService, useValue: assetServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AssetInventoryComponent);
    component = fixture.componentInstance;
    mockAssetService = TestBed.inject(AssetService) as jasmine.SpyObj<AssetService>;

    // Setup default mocks
    mockAssetService.getAssets.and.returnValue(of({
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 20,
      number: 0,
      page: {
        size: 20,
        number: 0,
        totalElements: 0,
        totalPages: 0
      }
    }));
    mockAssetService.getAssetStats.and.returnValue(of(mockStats));
    mockAssetService.searchAssets.and.returnValue(of({
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 20,
      number: 0,
      page: {
        size: 20,
        number: 0,
        totalElements: 0,
        totalPages: 0
      }
    }));
  });

  it('should create asset inventory with dashboard stats widget', () => {
    expect(component).toBeTruthy();
  });

  it('should render dashboard stats widget in template', () => {
    fixture.detectChanges();
    
    const dashboardStatsElement = fixture.nativeElement.querySelector('app-dashboard-stats');
    expect(dashboardStatsElement).toBeTruthy();
  });

  it('should have dashboard stats section with proper styling', () => {
    fixture.detectChanges();
    
    const dashboardStatsSection = fixture.nativeElement.querySelector('.dashboard-stats-section');
    expect(dashboardStatsSection).toBeTruthy();
  });
});