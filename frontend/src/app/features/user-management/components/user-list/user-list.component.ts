import { Component, ChangeDetectionStrategy, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil, finalize } from 'rxjs';

// Material imports
import { MaterialModule } from '../../../../shared/material.module';
import { MatTableDataSource } from '@angular/material/table';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { MatSlideToggleChange } from '@angular/material/slide-toggle';

// Services and models
import { UserService } from '../../services/user.service';
import { AuthService } from '../../../../core/services/auth.service';
import { UserDTO, UserFilterOptions } from '../../models/user.model';
import { Role } from '../../../../core/models/auth.model';
import { PageResponse } from '../../../../shared/models/page-response.model';

/**
 * User List Component
 * 
 * Displays paginated list of users with filtering, search, and management capabilities.
 * Implements Editorial Geometry design system with table display, role filtering,
 * and account status filtering.
 * 
 * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6
 */
@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MaterialModule
  ],
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserListComponent implements OnInit, OnDestroy {
  // Signals for reactive state management
  users = signal<UserDTO[]>([]);
  loading = signal<boolean>(false);
  totalElements = signal<number>(0);
  currentPage = signal<number>(0);
  pageSize = signal<number>(20);
  
  // Computed signals
  totalPages = computed(() => Math.ceil(this.totalElements() / this.pageSize()));
  hasUsers = computed(() => this.users().length > 0);
  
  // Form controls for filtering
  searchControl = new FormControl('');
  roleFilterControl = new FormControl<Role | 'ALL'>('ALL');
  statusFilterControl = new FormControl<'ACTIVE' | 'INACTIVE' | 'ALL'>('ALL');
  
  // Table configuration
  displayedColumns: string[] = ['username', 'email', 'roles', 'status', 'lastLogin', 'actions'];
  dataSource = new MatTableDataSource<UserDTO>([]);
  
  // Role enum for template
  Role = Role;
  
  // Available roles for filtering
  availableRoles = [
    { value: 'ALL', label: 'All Roles' },
    { value: Role.ADMINISTRATOR, label: 'Administrator' },
    { value: Role.ASSET_MANAGER, label: 'Asset Manager' },
    { value: Role.VIEWER, label: 'Viewer' }
  ];
  
  // Status options for filtering
  statusOptions = [
    { value: 'ALL', label: 'All' },
    { value: 'ACTIVE', label: 'Active Only' },
    { value: 'INACTIVE', label: 'Inactive Only' }
  ];
  
  // Current user permissions
  canManageUsers = signal<boolean>(false);
  
  private destroy$ = new Subject<void>();
  
  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}
  
  ngOnInit(): void {
    this.checkPermissions();
    this.setupSearchListener();
    this.setupFilterListeners();
    this.loadUsers();
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  /**
   * Check if current user has permission to manage users
   */
  private checkPermissions(): void {
    const currentUser = this.authService.getCurrentUser();
    const hasAdminRole = currentUser?.roles.includes(Role.ADMINISTRATOR);
    this.canManageUsers.set(hasAdminRole || false);
  }
  
  /**
   * Setup search input listener with debounce
   */
  private setupSearchListener(): void {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.currentPage.set(0);
        this.loadUsers();
      });
  }
  
  /**
   * Setup filter listeners
   */
  private setupFilterListeners(): void {
    this.roleFilterControl.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.currentPage.set(0);
        this.loadUsers();
      });
    
    this.statusFilterControl.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.currentPage.set(0);
        this.loadUsers();
      });
  }
  
  /**
   * Load users with current filters and pagination
   */
  loadUsers(): void {
    this.loading.set(true);
    
    const filters: UserFilterOptions = {};
    
    // Apply search filter
    const searchText = this.searchControl.value?.trim();
    if (searchText) {
      filters.searchText = searchText;
    }
    
    // Apply role filter
    const roleFilter = this.roleFilterControl.value;
    if (roleFilter && roleFilter !== 'ALL') {
      filters.role = roleFilter as Role;
    }
    
    // Apply status filter
    const statusFilter = this.statusFilterControl.value;
    if (statusFilter && statusFilter !== 'ALL') {
      filters.isActive = statusFilter === 'ACTIVE';
    }
    
    this.userService.getUsers(this.currentPage(), this.pageSize(), filters)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (response: PageResponse<UserDTO>) => {
          this.users.set(response.content);
          this.dataSource.data = response.content;
          this.totalElements.set(response.page.totalElements);
        },
        error: (error) => {
          console.error('Error loading users:', error);
          this.snackBar.open('Failed to load users', 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      });
  }
  
  /**
   * Handle page change event
   */
  onPageChange(event: any): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadUsers();
  }
  
  /**
   * Navigate to create user page
   */
  onCreateUser(): void {
    this.router.navigate(['/users/create']);
  }
  
  /**
   * Navigate to user detail page
   */
  onViewUser(user: UserDTO): void {
    this.router.navigate(['/users', user.id]);
  }
  
  /**
   * Navigate to edit user page
   */
  onEditUser(user: UserDTO, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/users', user.id, 'edit']);
  }
  
  /**
   * Toggle user account status
   */
  onToggleStatus(user: UserDTO, event: MatSlideToggleChange): void {
    // Remove focus after toggle
    (event.source as any)._elementRef.nativeElement.blur();
    
    const action = user.isActive ? 'disable' : 'enable';
    const actionText = user.isActive ? 'disabled' : 'enabled';
    
    this.loading.set(true);
    
    const operation = user.isActive 
      ? this.userService.disableUser(user.id)
      : this.userService.enableUser(user.id);
    
    operation
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: () => {
          this.snackBar.open(`User ${actionText} successfully`, 'Close', {
            duration: 3000,
            panelClass: ['success-snackbar']
          });
          this.loadUsers();
        },
        error: (error) => {
          console.error(`Error ${action}ing user:`, error);
          this.snackBar.open(`Failed to ${action} user`, 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      });
  }
  
  /**
   * Delete user with confirmation
   */
  onDeleteUser(user: UserDTO, event: Event): void {
    event.stopPropagation();
    
    const confirmed = confirm(`Are you sure you want to delete user "${user.username}"? This action cannot be undone.`);
    
    if (!confirmed) {
      return;
    }
    
    this.loading.set(true);
    
    this.userService.deleteUser(user.id)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: () => {
          this.snackBar.open('User deleted successfully', 'Close', {
            duration: 3000,
            panelClass: ['success-snackbar']
          });
          this.loadUsers();
        },
        error: (error) => {
          console.error('Error deleting user:', error);
          this.snackBar.open('Failed to delete user', 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      });
  }
  
  /**
   * Get role badge class for styling
   */
  getRoleBadgeClass(role: Role): string {
    switch (role) {
      case Role.ADMINISTRATOR:
        return 'role-badge-admin';
      case Role.ASSET_MANAGER:
        return 'role-badge-manager';
      case Role.VIEWER:
        return 'role-badge-viewer';
      default:
        return 'role-badge-default';
    }
  }
  
  /**
   * Get role display name
   */
  getRoleDisplayName(role: Role): string {
    switch (role) {
      case Role.ADMINISTRATOR:
        return 'Administrator';
      case Role.ASSET_MANAGER:
        return 'Asset Manager';
      case Role.VIEWER:
        return 'Viewer';
      default:
        return role;
    }
  }
  
  /**
   * Format last login date
   */
  formatLastLogin(date: Date | undefined): string {
    if (!date) {
      return 'Never';
    }
    
    const now = new Date();
    const loginDate = new Date(date);
    const diffMs = now.getTime() - loginDate.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);
    
    if (diffMins < 1) {
      return 'Just now';
    } else if (diffMins < 60) {
      return `${diffMins} min${diffMins > 1 ? 's' : ''} ago`;
    } else if (diffHours < 24) {
      return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
    } else if (diffDays < 30) {
      return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
    } else {
      return loginDate.toLocaleDateString();
    }
  }
  
  /**
   * Clear all filters
   */
  clearFilters(): void {
    this.searchControl.setValue('');
    this.roleFilterControl.setValue('ALL');
    this.statusFilterControl.setValue('ALL');
    this.currentPage.set(0);
    this.loadUsers();
  }
}
