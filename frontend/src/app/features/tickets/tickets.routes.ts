import { Routes } from '@angular/router';
import { TicketListComponent } from './components/ticket-list/ticket-list.component';
import { TicketDetailComponent } from './components/ticket-detail/ticket-detail.component';
import { TicketCreateComponent } from './components/ticket-create/ticket-create.component';
import { TicketMetricsDashboardComponent } from './components/ticket-metrics-dashboard/ticket-metrics-dashboard.component';

/**
 * Ticket module routes
 */
export const TICKET_ROUTES: Routes = [
  {
    path: '',
    component: TicketListComponent
  },
  {
    path: 'create',
    component: TicketCreateComponent
  },
  {
    path: 'my-requests',
    component: TicketListComponent
  },
  {
    path: 'pending-approvals',
    component: TicketListComponent
  },
  {
    path: 'metrics',
    component: TicketMetricsDashboardComponent
  },
  {
    path: ':id',
    component: TicketDetailComponent
  }
];
