import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Component } from '@angular/core';
import { AppShellComponent } from './app-shell.component';

// Stub child components to avoid pulling in their full dependency trees
@Component({ selector: 'app-sidebar', template: '', standalone: true })
class SidebarStubComponent {}

@Component({ selector: 'app-top-navigation', template: '', standalone: true })
class TopNavigationStubComponent {}

@Component({ selector: 'app-main-content', template: '<ng-content></ng-content>', standalone: true })
class MainContentStubComponent {}

describe('AppShellComponent', () => {
  let component: AppShellComponent;
  let fixture: ComponentFixture<AppShellComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AppShellComponent],
      imports: [
        RouterTestingModule,
        SidebarStubComponent,
        TopNavigationStubComponent,
        MainContentStubComponent
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AppShellComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should render the sidebar', () => {
    const sidebar = fixture.nativeElement.querySelector('app-sidebar');
    expect(sidebar).not.toBeNull();
  });

  it('should render the top navigation', () => {
    const topNav = fixture.nativeElement.querySelector('app-top-navigation');
    expect(topNav).not.toBeNull();
  });

  it('should render the main content area', () => {
    const main = fixture.nativeElement.querySelector('main');
    expect(main).not.toBeNull();
  });

  it('should contain a router-outlet inside the main content area', () => {
    const routerOutlet = fixture.nativeElement.querySelector('router-outlet');
    expect(routerOutlet).not.toBeNull();
  });

  it('should have the app-shell wrapper element', () => {
    const appShell = fixture.nativeElement.querySelector('.app-shell');
    expect(appShell).not.toBeNull();
  });

  it('should have a skip navigation link', () => {
    const skipLink = fixture.nativeElement.querySelector('a.skip-link');
    expect(skipLink).not.toBeNull();
  });

  it('should have main content with id for skip link target', () => {
    const mainContent = fixture.nativeElement.querySelector('#main-content');
    expect(mainContent).not.toBeNull();
  });

  it('should initialize currentRoute$ observable', () => {
    expect(component.currentRoute$).toBeDefined();
  });

  it('should clean up on destroy', () => {
    expect(() => component.ngOnDestroy()).not.toThrow();
  });
});
