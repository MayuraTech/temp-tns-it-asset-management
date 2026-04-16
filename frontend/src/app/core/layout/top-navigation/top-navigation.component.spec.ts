import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { TopNavigationComponent } from './top-navigation.component';
import { NavigationService } from '../../services/navigation.service';
// Stub child components
@Component({ selector: 'app-search-bar', template: '', standalone: true })
class SearchBarStubComponent {
  @Input() placeholder: string = '';
  @Input() value: string = '';
  @Output() searchChange = new EventEmitter<string>();
  @Output() searchSubmit = new EventEmitter<string>();
}

@Component({ selector: 'app-user-controls', template: '', standalone: true })
class UserControlsStubComponent {
  @Input() userInfo: any;
  @Input() notificationCount: number = 0;
  @Output() controlClick = new EventEmitter<string>();
}

describe('TopNavigationComponent', () => {
  let component: TopNavigationComponent;
  let fixture: ComponentFixture<TopNavigationComponent>;
  let navigationServiceSpy: jasmine.SpyObj<NavigationService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('NavigationService', ['navigateTo', 'isActiveRoute']);
    spy.isActiveRoute.and.callFake((route: string) => route === '/dashboard');

    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        TopNavigationComponent,
        SearchBarStubComponent,
        UserControlsStubComponent
      ],
      providers: [
        { provide: NavigationService, useValue: spy }
      ]
    }).compileComponents();

    navigationServiceSpy = TestBed.inject(NavigationService) as jasmine.SpyObj<NavigationService>;
    fixture = TestBed.createComponent(TopNavigationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should render the brand name', () => {
    const brandName = fixture.nativeElement.querySelector('.brand-name');
    expect(brandName).not.toBeNull();
    expect(brandName.textContent.trim()).toBe('TnS Assets');
  });

  it('should render the search bar', () => {
    const searchBar = fixture.nativeElement.querySelector('app-search-bar');
    expect(searchBar).not.toBeNull();
  });

  it('should render user controls', () => {
    const userControls = fixture.nativeElement.querySelector('app-user-controls');
    expect(userControls).not.toBeNull();
  });

  it('should have a nav element with top bar aria-label', () => {
    const nav = fixture.nativeElement.querySelector('nav[aria-label="Top bar"]');
    expect(nav).not.toBeNull();
  });

  it('should update searchQuery$ when search input changes', () => {
    component.onSearchInput('test query');
    expect(component.searchQuery$.value).toBe('test query');
  });

  it('should navigate to settings when settings user control is clicked', () => {
    component.onUserControlClick('settings');
    expect(navigationServiceSpy.navigateTo).toHaveBeenCalledWith('/settings');
  });
});
