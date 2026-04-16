import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Component, Input, ChangeDetectorRef } from '@angular/core';
import { By } from '@angular/platform-browser';
import { SidebarComponent } from './sidebar.component';
import { NavigationService } from '../../services/navigation.service';
import { PRIMARY_NAVIGATION, SECONDARY_NAVIGATION, NavigationItem } from '../../../shared/constants/navigation.config';

// Stub child components
@Component({ selector: 'app-geometric-triangle', template: '', standalone: true })
class GeometricTriangleStubComponent {
  @Input() size: string = '';
  @Input() position: string = '';
  @Input() color: string = '';
  @Input() opacity: number = 0;
}

@Component({ selector: 'app-primary-action-button', template: '', standalone: true })
class PrimaryActionButtonStubComponent {
  @Input() label: string = '';
  @Input() icon: string = '';
  @Input() fullWidth: boolean = false;
}

@Component({ selector: 'app-icon', template: '', standalone: true })
class IconStubComponent {
  @Input() name: string = '';
  @Input() size: number = 20;
}

describe('SidebarComponent', () => {
  let component: SidebarComponent;
  let fixture: ComponentFixture<SidebarComponent>;
  let navigationServiceSpy: jasmine.SpyObj<NavigationService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('NavigationService', ['navigateTo', 'isActiveRoute'], {
      primaryNavigation: PRIMARY_NAVIGATION,
      secondaryNavigation: SECONDARY_NAVIGATION,
      currentRoute$: jasmine.createSpyObj('Observable', ['pipe'])
    });
    spy.isActiveRoute.and.returnValue(false);

    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        SidebarComponent,
        GeometricTriangleStubComponent,
        PrimaryActionButtonStubComponent,
        IconStubComponent
      ],
      providers: [
        { provide: NavigationService, useValue: spy }
      ]
    }).compileComponents();

    navigationServiceSpy = TestBed.inject(NavigationService) as jasmine.SpyObj<NavigationService>;
    fixture = TestBed.createComponent(SidebarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should render primary navigation items', () => {
    const navLinks = fixture.nativeElement.querySelectorAll('.sidebar__primary-nav .nav-link');
    expect(navLinks.length).toBe(PRIMARY_NAVIGATION.length);
  });

  it('should render all primary navigation labels', () => {
    const navTexts = fixture.nativeElement.querySelectorAll('.sidebar__primary-nav .nav-text');
    const labels = Array.from(navTexts).map((el: any) => el.textContent.trim());
    PRIMARY_NAVIGATION.forEach(item => {
      expect(labels).toContain(item.label);
    });
  });

  it('should render secondary navigation items', () => {
    const secondaryLinks = fixture.nativeElement.querySelectorAll('.sidebar__secondary-nav .nav-link');
    expect(secondaryLinks.length).toBe(SECONDARY_NAVIGATION.length);
  });

  it('should render all secondary navigation labels', () => {
    const navTexts = fixture.nativeElement.querySelectorAll('.sidebar__secondary-nav .nav-text');
    const labels = Array.from(navTexts).map((el: any) => el.textContent.trim());
    SECONDARY_NAVIGATION.forEach(item => {
      expect(labels).toContain(item.label);
    });
  });

  it('should call navigateTo when a navigation item is clicked', () => {
    const firstNavLink = fixture.nativeElement.querySelector('.sidebar__primary-nav .nav-link');
    firstNavLink.click();
    expect(navigationServiceSpy.navigateTo).toHaveBeenCalledWith(PRIMARY_NAVIGATION[0].route);
  });

  it('should call isActiveRoute to determine active state', () => {
    expect(navigationServiceSpy.isActiveRoute).toHaveBeenCalled();
  });

  it('should apply aria-current="page" to active navigation item', () => {
    navigationServiceSpy.isActiveRoute.and.callFake((route: string) => route === '/assets');
    // With OnPush, we need to get the component's ChangeDetectorRef and mark it for check
    const cdRef = fixture.debugElement.injector.get(ChangeDetectorRef);
    cdRef.markForCheck();
    fixture.detectChanges();

    const activeLink = fixture.nativeElement.querySelector('[aria-current="page"]');
    expect(activeLink).not.toBeNull();
  });

  it('should not apply aria-current to inactive navigation items', () => {
    navigationServiceSpy.isActiveRoute.and.returnValue(false);
    fixture.detectChanges();

    const activeLinks = fixture.nativeElement.querySelectorAll('[aria-current="page"]');
    expect(activeLinks.length).toBe(0);
  });

  it('should have a nav element with aria-label for primary navigation', () => {
    const nav = fixture.nativeElement.querySelector('nav[aria-label="Primary navigation"]');
    expect(nav).not.toBeNull();
  });

  it('should call navigateTo with asset creation route on action button click', () => {
    component.onActionButtonClick();
    expect(navigationServiceSpy.navigateTo).toHaveBeenCalledWith('/assets/new');
  });
});
