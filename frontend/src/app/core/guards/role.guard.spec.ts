import { TestBed } from '@angular/core/testing';
import { Router, RouterStateSnapshot } from '@angular/router';
import { roleGuard } from './role.guard';
import { AuthService } from '../services/auth.service';
import { Role, User } from '../models/auth.model';
import { ActivatedRouteSnapshot } from '@angular/router';

describe('RoleGuard', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;
  let mockRoute: ActivatedRouteSnapshot;
  let mockState: RouterStateSnapshot;

  const mockUser: User = {
    id: '123',
    username: 'testuser',
    email: 'test@example.com',
    roles: [Role.ASSET_MANAGER],
    createdAt: new Date(),
    updatedAt: new Date(),
    accountLocked: false
  };

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
    
    mockRoute = {
      data: {}
    } as ActivatedRouteSnapshot;
    
    mockState = {} as RouterStateSnapshot;
  });

  it('should redirect to login when user is not authenticated', () => {
    Object.defineProperty(authService, 'currentUserValue', { value: null });

    const result = TestBed.runInInjectionContext(() => 
      roleGuard(mockRoute, mockState)
    );

    expect(result).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should allow access when no roles are required', () => {
    Object.defineProperty(authService, 'currentUserValue', { value: mockUser });
    mockRoute.data = { roles: [] };

    const result = TestBed.runInInjectionContext(() => 
      roleGuard(mockRoute, mockState)
    );

    expect(result).toBe(true);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should allow access when user has required role', () => {
    Object.defineProperty(authService, 'currentUserValue', { value: mockUser });
    mockRoute.data = { roles: [Role.ASSET_MANAGER] };

    const result = TestBed.runInInjectionContext(() => 
      roleGuard(mockRoute, mockState)
    );

    expect(result).toBe(true);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should allow access when user has one of multiple required roles', () => {
    Object.defineProperty(authService, 'currentUserValue', { value: mockUser });
    mockRoute.data = { roles: [Role.ADMINISTRATOR, Role.ASSET_MANAGER] };

    const result = TestBed.runInInjectionContext(() => 
      roleGuard(mockRoute, mockState)
    );

    expect(result).toBe(true);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should redirect to unauthorized when user lacks required role', () => {
    Object.defineProperty(authService, 'currentUserValue', { value: mockUser });
    mockRoute.data = { roles: [Role.ADMINISTRATOR] };

    const result = TestBed.runInInjectionContext(() => 
      roleGuard(mockRoute, mockState)
    );

    expect(result).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/unauthorized']);
  });

  it('should redirect to unauthorized when user has no matching roles', () => {
    const viewerUser = { ...mockUser, roles: [Role.VIEWER] };
    Object.defineProperty(authService, 'currentUserValue', { value: viewerUser });
    mockRoute.data = { roles: [Role.ADMINISTRATOR, Role.ASSET_MANAGER] };

    const result = TestBed.runInInjectionContext(() => 
      roleGuard(mockRoute, mockState)
    );

    expect(result).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/unauthorized']);
  });
});
