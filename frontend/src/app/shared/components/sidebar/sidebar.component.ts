import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/services/auth.service';
import { Observable, map } from 'rxjs';

interface MenuItem {
  label: string;
  icon: string;
  route: string;
  roles: string[];
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
      roles: ['Administrator', 'Asset_Manager', 'Viewer']
    },
    {
      label: 'Assets',
      icon: 'inventory_2',
      route: '/assets',
      roles: ['Administrator', 'Asset_Manager', 'Viewer']
    },
    {
      label: 'My Requests',
      icon: 'assignment',
      route: '/tickets',
      roles: ['Administrator', 'Asset_Manager', 'Viewer']
    },
    {
      label: 'Ticket Management',
      icon: 'approval',
      route: '/ticket-management',
      roles: ['Administrator', 'Asset_Manager']
    },
    {
      label: 'Reports',
      icon: 'assessment',
      route: '/reports',
      roles: ['Administrator', 'Asset_Manager']
    },
    {
      label: 'Users',
      icon: 'people',
      route: '/users',
      roles: ['Administrator']
    },
    {
      label: 'Audit Logs',
      icon: 'history',
      route: '/audit-logs',
      roles: ['Administrator']
    },
    {
      label: 'Settings',
      icon: 'settings',
      route: '/settings',
      roles: ['Administrator']
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
