import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { TopNavigationComponent } from './top-navigation.component';
import { NavigationService } from '../../services/navigation.service';
import { TOP_NAVIGATION } from '../../../shared/constants/navigation.config';

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

  const currentRoute$ = new BehaviorSubject<string>('/dashboard');

  beforeEach(async () => {
    const topNavWithActive = TOP_NAVIGATION.map(item => ({
      ...item,
      active: item.route === '/dashboard'
    }));

    const spy = jasmine.createSpyObj('NavigationService', ['navigateTo', 'isActiveRoute'], {
      currentRoute$: currentRoute$.asObservable(),
      topNavigation: topNavWithActive
    });
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
    expect(brandName.textContent.trim()).toBe('AssetIntel');
  });

  it('should render the search bar', () => {
    const searchBar = fixture.nativeElement.querySelector('app-search-bar');
    expect(searchBar).not.toBeNull();
  });

  it('should render secondary navigation links', () => {
    const navLinks = fixture.nativeElement.querySelectorAll('.top-nav__secondary-nav .nav-link');
    expect(navLinks.length).toBe(TOP_NAVIGATION.length);
  });

  it('should render all top navigation labels', () => {
    const navLinks = fixture.nativeElement.querySelectorAll('.top-nav__secondary-nav .nav-link');
    const labels = Array.from(navLinks).map((el: any) => el.textContent.trim());
    TOP_NAVIGATION.forEach(item => {
      expect(labels).toContain(item.label);
    });
  });

  it('should render user controls', () => {
    const userControls = fixture.nativeElement.querySelector('app-user-controls');
    expect(userControls).not.toBeNull();
  });

  it('should have a nav element with aria-label for secondary navigation', () => {
    const nav = fixture.nativeElement.querySelector('nav[aria-label="Secondary navigation"]');
    expect(nav).not.toBeNull();
  });

  it('should call navigateTo when a secondary nav item is clicked', () => {
    const firstNavLink = fixture.nativeElement.querySelector('.top-nav__secondary-nav .nav-link');
    firstNavLink.click();
    expect(navigationServiceSpy.navigateTo).toHaveBeenCalled();
  });

  it('should update searchQuery$ when search input changes', () => {
    component.onSearchInput('test query');
    expect(component.searchQuery$.value).toBe('test query');
  });

  it('should navigate to settings when settings user control is clicked', () => {
    component.onUserControlClick('settings');
    expect(navigationServiceSpy.navigateTo).toHaveBeenCalledWith('/settings');
  });

  it('should initialize topNavItems$ with navigation items', (done) => {
    component.topNavItems$.subscribe(items => {
      expect(items.length).toBe(TOP_NAVIGATION.length);
      done();
    });
  });
});
