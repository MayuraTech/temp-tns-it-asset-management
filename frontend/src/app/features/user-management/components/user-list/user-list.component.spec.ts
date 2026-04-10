import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';

import { UserListComponent } from './user-list.component';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../../../core/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { Role } from '../../../../core/models/auth.model';
import { UserDTO } from '../../models/user.model';
import { PageResponse } from '../../../../shared/models/page-response.model';

describe('UserListComponent', () => {
  let component: UserListComponent;
  let fixture: ComponentFixture<UserListComponent>;
  let userService: jasmine.SpyObj<UserService>;
  let authService: jasmine.SpyObj<AuthService>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;

  const mockUsers: UserDTO[] = [
    {
      id: '1',
      username: 'admin',
      email: 'admin@example.com',
      isActive: true,
      accountLocked: false,
      roles: [Role.ADMINISTRATOR],
      createdAt: new Date(),
      updatedAt: new Date()
    },
    {
      id: '2',
      username: 'manager',
      email: 'manager@example.com',
      isActive: true,
      accountLocked: false,
      roles: [Role.ASSET_MANAGER],
      createdAt: new Date(),
      updatedAt: new Date()
    }
  ];

  const mockPageResponse: PageResponse<UserDTO> = {
    content: mockUsers,
    page: {
      size: 20,
      number: 0,
      totalElements: 2,
      totalPages: 1
    }
  };

  beforeEach(async () => {
    const userServiceSpy = jasmine.createSpyObj('UserService', [
      'getUsers',
      'deleteUser',
      'enableUser',
      'disableUser'
    ]);
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentUser']);
    const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [
        UserListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ],
      providers: [
        { provide: UserService, useValue: userServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: MatSnackBar, useValue: snackBarSpy },
        { provide: MatDialog, useValue: {} }
      ]
    }).compileComponents();

    userService = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    snackBar = TestBed.inject(MatSnackBar) as jasmine.SpyObj<MatSnackBar>;

    // Setup default mock returns
    userService.getUsers.and.returnValue(of(mockPageResponse));
    authService.getCurrentUser.and.returnValue({
      id: '1',
      username: 'admin',
      email: 'admin@example.com',
      roles: [Role.ADMINISTRATOR],
      createdAt: new Date(),
      updatedAt: new Date(),
      accountLocked: false
    });

    fixture = TestBed.createComponent(UserListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load users on init', () => {
    fixture.detectChanges();
    
    expect(userService.getUsers).toHaveBeenCalledWith(0, 20, {});
    expect(component.users().length).toBe(2);
    expect(component.totalElements()).toBe(2);
  });

  it('should set canManageUsers to true for administrators', () => {
    fixture.detectChanges();
    
    expect(component.canManageUsers()).toBe(true);
  });

  it('should set canManageUsers to false for non-administrators', () => {
    authService.getCurrentUser.and.returnValue({
      id: '2',
      username: 'viewer',
      email: 'viewer@example.com',
      roles: [Role.VIEWER],
      createdAt: new Date(),
      updatedAt: new Date(),
      accountLocked: false
    });

    fixture = TestBed.createComponent(UserListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    
    expect(component.canManageUsers()).toBe(false);
  });

  it('should filter users by search text', (done) => {
    fixture.detectChanges();
    
    component.searchControl.setValue('admin');
    
    setTimeout(() => {
      expect(userService.getUsers).toHaveBeenCalledWith(0, 20, { searchText: 'admin' });
      done();
    }, 350); // Wait for debounce
  });

  it('should filter users by role', () => {
    fixture.detectChanges();
    
    component.roleFilterControl.setValue(Role.ADMINISTRATOR);
    
    expect(userService.getUsers).toHaveBeenCalledWith(0, 20, { role: Role.ADMINISTRATOR });
  });

  it('should filter users by status', () => {
    fixture.detectChanges();
    
    component.statusFilterControl.setValue('ACTIVE');
    
    expect(userService.getUsers).toHaveBeenCalledWith(0, 20, { isActive: true });
  });

  it('should handle page change', () => {
    fixture.detectChanges();
    
    component.onPageChange({ pageIndex: 1, pageSize: 20 });
    
    expect(component.currentPage()).toBe(1);
    expect(userService.getUsers).toHaveBeenCalledWith(1, 20, {});
  });

  it('should enable user account', () => {
    userService.enableUser.and.returnValue(of(void 0));
    fixture.detectChanges();
    
    const inactiveUser = { ...mockUsers[0], isActive: false };
    component.onToggleStatus(inactiveUser, new Event('click'));
    
    expect(userService.enableUser).toHaveBeenCalledWith(inactiveUser.id);
    expect(snackBar.open).toHaveBeenCalledWith(
      'User enabled successfully',
      'Close',
      jasmine.any(Object)
    );
  });

  it('should disable user account', () => {
    userService.disableUser.and.returnValue(of(void 0));
    fixture.detectChanges();
    
    component.onToggleStatus(mockUsers[0], new Event('click'));
    
    expect(userService.disableUser).toHaveBeenCalledWith(mockUsers[0].id);
    expect(snackBar.open).toHaveBeenCalledWith(
      'User disabled successfully',
      'Close',
      jasmine.any(Object)
    );
  });

  it('should handle error when loading users', () => {
    userService.getUsers.and.returnValue(throwError(() => new Error('Failed to load')));
    
    fixture.detectChanges();
    
    expect(snackBar.open).toHaveBeenCalledWith(
      'Failed to load users',
      'Close',
      jasmine.any(Object)
    );
  });

  it('should clear all filters', () => {
    fixture.detectChanges();
    
    component.searchControl.setValue('test');
    component.roleFilterControl.setValue(Role.ADMINISTRATOR);
    component.statusFilterControl.setValue('ACTIVE');
    component.currentPage.set(2);
    
    component.clearFilters();
    
    expect(component.searchControl.value).toBe('');
    expect(component.roleFilterControl.value).toBe('ALL');
    expect(component.statusFilterControl.value).toBe('ALL');
    expect(component.currentPage()).toBe(0);
  });

  it('should format last login correctly', () => {
    const now = new Date();
    const twoMinsAgo = new Date(now.getTime() - 2 * 60 * 1000);
    const twoHoursAgo = new Date(now.getTime() - 2 * 60 * 60 * 1000);
    const twoDaysAgo = new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000);
    
    expect(component.formatLastLogin(undefined)).toBe('Never');
    expect(component.formatLastLogin(twoMinsAgo)).toBe('2 mins ago');
    expect(component.formatLastLogin(twoHoursAgo)).toBe('2 hours ago');
    expect(component.formatLastLogin(twoDaysAgo)).toBe('2 days ago');
  });

  it('should get correct role badge class', () => {
    expect(component.getRoleBadgeClass(Role.ADMINISTRATOR)).toBe('role-badge-admin');
    expect(component.getRoleBadgeClass(Role.ASSET_MANAGER)).toBe('role-badge-manager');
    expect(component.getRoleBadgeClass(Role.VIEWER)).toBe('role-badge-viewer');
  });

  it('should get correct role display name', () => {
    expect(component.getRoleDisplayName(Role.ADMINISTRATOR)).toBe('Administrator');
    expect(component.getRoleDisplayName(Role.ASSET_MANAGER)).toBe('Asset Manager');
    expect(component.getRoleDisplayName(Role.VIEWER)).toBe('Viewer');
  });
});
