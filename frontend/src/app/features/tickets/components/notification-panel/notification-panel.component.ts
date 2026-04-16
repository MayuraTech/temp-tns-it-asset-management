import { Component, OnInit, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { BehaviorSubject, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Notification } from '../../../../shared/models/ticket.model';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-notification-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-panel.component.html',
  styleUrls: ['./notification-panel.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NotificationPanelComponent implements OnInit, OnDestroy {
  notifications$ = new BehaviorSubject<Notification[]>([]);
  unreadCount$ = this.notificationService.unreadCount$;
  loading$ = new BehaviorSubject<boolean>(false);
  
  private destroy$ = new Subject<void>();

  constructor(
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
    this.startPolling();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadNotifications(): void {
    this.loading$.next(true);
    this.notificationService.getNotifications(0, 20)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (notifications) => {
          this.notifications$.next(notifications);
          this.loading$.next(false);
        },
        error: (error) => {
          console.error('Error loading notifications:', error);
          this.loading$.next(false);
        }
      });
  }

  startPolling(): void {
    this.notificationService.startPolling()
      .pipe(takeUntil(this.destroy$))
      .subscribe();
  }

  markAsRead(notification: Notification): void {
    if (notification.isRead) return;
    
    this.notificationService.markAsRead(notification.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          const notifications = this.notifications$.value.map(n =>
            n.id === notification.id ? { ...n, isRead: true } : n
          );
          this.notifications$.next(notifications);
        },
        error: (error) => console.error('Error marking notification as read:', error)
      });
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          const notifications = this.notifications$.value.map(n => ({ ...n, isRead: true }));
          this.notifications$.next(notifications);
        },
        error: (error) => console.error('Error marking all as read:', error)
      });
  }

  navigateToTicket(notification: Notification): void {
    this.markAsRead(notification);
    this.router.navigate(['/tickets', notification.ticketId]);
  }

  refresh(): void {
    this.loadNotifications();
  }
}
