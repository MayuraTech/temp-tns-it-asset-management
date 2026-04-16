import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/services/auth.service';
import { Role } from '../../../core/models/auth.model';
import { Observable, map } from 'rxjs';

interface MenuItem {
  label: string;
  icon: string;
  route: string;
  roles: Role[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule
  ],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit {
  menuItems: MenuItem[] = [
    {
      label: 'Dashboard',
      icon: 'dashboard',
      route: '/dashboard',
      roles: [Role.ADMINISTRATOR, Role.ASSET_MANAGER, Role.VIEWER]
    },
    {
      label: 'Assets',
      icon: 'inventory_2',
      route: '/assets',
      roles: [Role.ADMINISTRATOR, Role.ASSET_MANAGER, Role.VIEWER]
    },
    {
      label: 'My Requests',
      icon: 'assignment',
      route: '/tickets',
      roles: [Role.ADMINISTRATOR, Role.ASSET_MANAGER, Role.VIEWER]
    },
    {
      label: 'Ticket Management',
      icon: 'approval',
      route: '/ticket-management',
      roles: [Role.ADMINISTRATOR, Role.ASSET_MANAGER]
    },
    {
      label: 'Reports',
      icon: 'assessment',
      route: '/reports',
      roles: [Role.ADMINISTRATOR, Role.ASSET_MANAGER]
    },
    {
      label: 'Users',
      icon: 'people',
      route: '/users',
      roles: [Role.ADMINISTRATOR]
    },
    {
      label: 'Audit Logs',
      icon: 'history',
      route: '/audit-logs',
      roles: [Role.ADMINISTRATOR]
    },
    {
      label: 'Settings',
      icon: 'settings',
      route: '/settings',
      roles: [Role.ADMINISTRATOR]
    }
  ];

  filteredMenuItems$: Observable<MenuItem[]>;

  constructor(private authService: AuthService) {
    this.filteredMenuItems$ = this.authService.currentUser$.pipe(
      map(user => {
        if (!user || !user.roles) {
          return [];
        }
        return this.menuItems.filter(item =>
          item.roles.some(role => user.roles.includes(role))
        );
      })
    );
  }

  ngOnInit(): void {}
}
