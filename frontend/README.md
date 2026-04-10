# IT Infrastructure Asset Management - Frontend

Angular frontend application for the IT Infrastructure Asset Management system.

## Overview

This is a modern, responsive single-page application (SPA) built with Angular 17+ that provides a comprehensive user interface for managing IT infrastructure assets. The application features role-based access control, real-time updates, and an intuitive design optimized for desktop, tablet, and mobile devices.

## Technology Stack

- **Framework**: Angular 17+
- **Language**: TypeScript 5.2+
- **UI Components**: Angular Material
- **State Management**: RxJS
- **HTTP Client**: Angular HttpClient
- **Testing**: Jasmine, Karma, fast-check (property-based testing)
- **Build Tool**: Angular CLI
- **Styling**: SCSS

## Prerequisites

- Node.js 18+ and npm 9+
- Angular CLI 17+
- Git

## Project Structure

```
src/
├── app/
│   ├── core/                    # Singleton services, guards, interceptors
│   │   ├── auth/
│   │   ├── guards/
│   │   ├── interceptors/
│   │   └── services/
│   ├── shared/                  # Shared components, directives, pipes
│   │   ├── components/
│   │   ├── directives/
│   │   ├── pipes/
│   │   └── models/
│   ├── features/                # Feature modules
│   │   ├── assets/
│   │   ├── tickets/
│   │   ├── reports/
│   │   └── users/
│   ├── app.component.ts
│   ├── app.config.ts
│   └── app.routes.ts
├── assets/                      # Static assets
├── environments/                # Environment configurations
├── styles.scss                  # Global styles
└── index.html
```

## Getting Started

### 1. Install Dependencies

```bash
npm install
```

### 2. Configure Environment

Create `src/environments/environment.development.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1',
  apiTimeout: 30000
};
```

### 3. Start Development Server

```bash
npm start
```

The application will be available at `http://localhost:4200`

The app will automatically reload when you make changes to source files.

### 4. Build for Production

```bash
npm run build:prod
```

Production build artifacts will be stored in the `dist/` directory.

## Development

### Generate Components

```bash
# Generate a new component
ng generate component features/assets/components/asset-list

# Generate a new service
ng generate service core/services/asset

# Generate a new guard
ng generate guard core/guards/auth

# Generate a new interceptor
ng generate interceptor core/interceptors/jwt

# Generate a new pipe
ng generate pipe shared/pipes/date-format
```

### Code Scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Testing

### Run Unit Tests

```bash
npm test
```

This executes unit tests via Karma in headless Chrome.

### Run Tests with Coverage

```bash
npm run test:coverage
```

Coverage report will be available in the `coverage/` directory.

### Run Tests in Watch Mode

```bash
npm run test:watch
```

### Run Property-Based Tests

Property-based tests using fast-check are included in the test suite and run automatically with `npm test`.

## Linting

### Run Linter

```bash
npm run lint
```

### Fix Linting Issues

```bash
npm run lint -- --fix
```

## Code Style Guidelines

- Follow Angular style guide: https://angular.io/guide/styleguide
- Use TypeScript strict mode
- Implement OnPush change detection strategy for components
- Use reactive forms over template-driven forms
- Unsubscribe from observables in ngOnDestroy
- Use async pipe in templates when possible
- Follow the coding standards in `.kiro/steering/it-asset-management-coding-standards.md`

## Features

### Authentication

- JWT-based authentication
- Secure token storage
- Automatic token refresh
- Session timeout handling

### Asset Management

- Create, view, update, and delete assets
- Advanced search and filtering
- Asset lifecycle tracking
- Assignment management
- Bulk operations

### Ticketing System

- Create allocation/de-allocation requests
- Ticket approval workflow
- Status tracking
- Priority management

### Reporting

- Asset distribution reports
- Lifecycle status reports
- End-of-life analysis
- Export capabilities

### User Management

- User account management
- Role assignment
- Permission control
- Profile management

## Environment Configuration

### Development

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1',
  apiTimeout: 30000
};
```

### Production

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://api.example.com/api/v1',
  apiTimeout: 30000
};
```

## API Integration

The application communicates with the backend REST API. All API calls are made through service classes in the `core/services` directory.

Example service:

```typescript
@Injectable({
  providedIn: 'root'
})
export class AssetService {
  private readonly apiUrl = `${environment.apiUrl}/assets`;

  constructor(private http: HttpClient) {}

  getAssets(): Observable<Asset[]> {
    return this.http.get<Asset[]>(this.apiUrl);
  }

  createAsset(asset: AssetRequest): Observable<Asset> {
    return this.http.post<Asset>(this.apiUrl, asset);
  }
}
```

## Routing

The application uses Angular Router for navigation. Routes are defined in `app.routes.ts`:

```typescript
export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'assets',
    loadChildren: () => import('./features/assets/assets.routes')
      .then(m => m.ASSETS_ROUTES),
    canActivate: [AuthGuard]
  }
];
```

## State Management

The application uses RxJS for state management with BehaviorSubjects and Observables. Services maintain state and components subscribe to state changes.

## Error Handling

Global error handling is implemented via HTTP interceptors:

- Authentication errors (401) redirect to login
- Authorization errors (403) show permission denied message
- Validation errors (400) display field-specific errors
- Server errors (5xx) show generic error message

## Performance Optimization

- Lazy loading of feature modules
- OnPush change detection strategy
- Virtual scrolling for large lists
- Image optimization
- Bundle size optimization
- Tree shaking

## Accessibility

The application follows WCAG 2.1 Level AA guidelines:

- Semantic HTML
- ARIA labels and roles
- Keyboard navigation
- Screen reader support
- Color contrast compliance

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Troubleshooting

### Port Already in Use

If port 4200 is already in use:

```bash
ng serve --port 4201
```

### Module Not Found

Clear node_modules and reinstall:

```bash
rm -rf node_modules package-lock.json
npm install
```

### Build Errors

Clear Angular cache:

```bash
rm -rf .angular/cache
ng build
```

## Contributing

1. Create a feature branch from `develop`
2. Make your changes following coding standards
3. Write tests for new functionality
4. Ensure all tests pass
5. Run linter and fix any issues
6. Submit a pull request

## Further Help

To get more help on the Angular CLI use `ng help` or check out the [Angular CLI Overview and Command Reference](https://angular.io/cli) page.

## License

Copyright © 2024 Company Name. All rights reserved.
