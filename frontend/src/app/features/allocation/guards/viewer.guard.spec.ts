import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { Role } from '../../../core/models/auth.model';
import { viewerGuard } from './viewer.guard';

describe('viewerGuard', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;
  let route: ActivatedRouteSnapshot;
  let state: RouterStateSnapshot;

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
      currentUserValue: null
    });
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });

    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    
    route = {} as ActivatedRouteSnapshot;
    state = { url: '/allocation/history/123' } as RouterStateSnapshot;
  });

  it('should allow access for ADMINISTRATOR role', () => {
    const mockUser = {
      id: '1',
      username: 'admin',
      email: 'admin@example.com',
      roles: [Role.ADMINISTRATOR],
      createdAt: new Date(),
      updatedAt: new Date(),
      accountLocked: false
    };
    
    Object.defineProperty(authService, 'currentUserValue', {
      get: () => mockUser
    });

    const result = TestBed.runInInjectionContext(() => 
      viewerGuard(route, state)
    );

    expect(result).toBe(true);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should allow access for ASSET_MANAGER role', () => {
    const mockUser = {
      id: '2',
      username: 'manager',
      email: 'manager@example.com',
      roles: [Role.ASSET_MANAGER],
      createdAt: new Date(),
      updatedAt: new Date(),
      accountLocked: false
    };
    
    Object.defineProperty(authService, 'currentUserValue', {
      get: () => mockUser
    });

    const result = TestBed.runInInjectionContext(() => 
      viewerGuard(route, state)
    );

    expect(result).toBe(true);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should allow access for VIEWER role', () => {
    const mockUser = {
      id: '3',
      username: 'viewer',
      email: 'viewer@example.com',
      roles: [Role.VIEWER],
      createdAt: new Date(),
      updatedAt: new Date(),
      accountLocked: false
    };
    
    Object.defineProperty(authService, 'currentUserValue', {
      get: () => mockUser
    });

    const result = TestBed.runInInjectionContext(() => 
      viewerGuard(route, state)
    );

    expect(result).toBe(true);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should redirect to login when user is not authenticated', () => {
    Object.defineProperty(authService, 'currentUserValue', {
      get: () => null
    });

    const result = TestBed.runInInjectionContext(() => 
      viewerGuard(route, state)
    );

    expect(result).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(
      ['/login'],
      { queryParams: { returnUrl: '/allocation/history/123' } }
    );
  });

  it('should allow access for user with multiple roles', () => {
    const mockUser = {
      id: '4',
      username: 'superuser',
      email: 'superuser@example.com',
      roles: [Role.ADMINISTRATOR, Role.ASSET_MANAGER, Role.VIEWER],
      createdAt: new Date(),
      updatedAt: new Date(),
      accountLocked: false
    };
    
    Object.defineProperty(authService, 'currentUserValue', {
      get: () => mockUser
    });

    const result = TestBed.runInInjectionContext(() => 
      viewerGuard(route, state)
    );

    expect(result).toBe(true);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should deny access for user with no roles', () => {
    const mockUser = {
      id: '5',
      username: 'norole',
      email: 'norole@example.com',
      roles: [],
      createdAt: new Date(),
      updatedAt: new Date(),
      accountLocked: false
    };
    
    Object.defineProperty(authService, 'currentUserValue', {
      get: () => mockUser
    });

    const result = TestBed.runInInjectionContext(() => 
      viewerGuard(route, state)
    );

    expect(result).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/unauthorized']);
  });
});
