import {
  Component,
  ChangeDetectionStrategy,
  OnInit,
  OnDestroy,
  signal,
  inject
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { HttpErrorResponse } from '@angular/common/http';
import { Subject, switchMap, takeUntil, of, throwError } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../../../core/services/auth.service';
import { UserDTO } from '../../models/user.model';
import { Role } from '../../../../core/models/auth.model';

@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './user-detail.component.html',
  styleUrls: ['./user-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserDetailComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly destroy$ = new Subject<void>();

  readonly user = signal<UserDTO | null>(null);
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);
  readonly canEdit = signal<boolean>(false);

  readonly Role = Role;

  ngOnInit(): void {
    const current = this.authService.getCurrentUser();
    this.canEdit.set(!!current?.roles.includes(Role.ADMINISTRATOR));

    this.route.paramMap
      .pipe(
        switchMap((params) => {
          const id = params.get('id');
          if (!id) {
            this.error.set('Missing user id');
            this.loading.set(false);
            return of(null);
          }
          this.loading.set(true);
          this.error.set(null);
          // finalize must be on the HTTP stream — paramMap never completes, so an outer
          // finalize would not run after getUser and loading would stay true forever.
          return this.userService.getUser(id).pipe(
            catchError((err: HttpErrorResponse) => {
              const msg =
                err.error?.error?.message ||
                err.error?.message ||
                (err.status === 404 ? 'User not found.' : err.message) ||
                'Failed to load user';
              const text = typeof msg === 'string' ? msg : 'Failed to load user';
              return throwError(() => new Error(text));
            }),
            finalize(() => this.loading.set(false))
          );
        }),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (u) => {
          if (u) {
            this.user.set(u);
          }
        },
        error: (err: unknown) => {
          const msg =
            err instanceof Error ? err.message : 'Failed to load user';
          this.error.set(msg);
          this.user.set(null);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  goBack(): void {
    void this.router.navigate(['/users']);
  }

  editUser(): void {
    const u = this.user();
    if (u) {
      void this.router.navigate(['/users', u.id, 'edit']);
    }
  }

  getRoleDisplayName(role: Role): string {
    switch (role) {
      case Role.ADMINISTRATOR:
        return 'Administrator';
      case Role.ASSET_MANAGER:
        return 'Asset Manager';
      case Role.VIEWER:
        return 'Viewer';
      default:
        return String(role);
    }
  }

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
}
