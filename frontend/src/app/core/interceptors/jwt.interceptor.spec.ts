import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { jwtInterceptor } from './jwt.interceptor';
import { AuthService } from '../services/auth.service';
import { LoginResponse } from '../models/auth.model';

describe('JwtInterceptor', () => {
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'getAccessToken',
      'getRefreshToken',
      'refreshToken',
      'logout'
    ]);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate'], { url: '/assets' });

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([jwtInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });

    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('Token Attachment', () => {
    it('should add Authorization header when token exists', () => {
      authService.getAccessToken.and.returnValue('test-token');

      httpClient.get('/api/v1/assets').subscribe();

      const req = httpMock.expectOne('/api/v1/assets');
      expect(req.request.headers.has('Authorization')).toBe(true);
      expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
      req.flush({});
    });

    it('should not add Authorization header when no token exists', () => {
      authService.getAccessToken.and.returnValue(null);

      httpClient.get('/api/v1/assets').subscribe();

      const req = httpMock.expectOne('/api/v1/assets');
      expect(req.request.headers.has('Authorization')).toBe(false);
      req.flush({});
    });

    it('should not add Authorization header for login endpoint', () => {
      authService.getAccessToken.and.returnValue('test-token');

      httpClient.post('/api/v1/auth/login', {}).subscribe();

      const req = httpMock.expectOne('/api/v1/auth/login');
      expect(req.request.headers.has('Authorization')).toBe(false);
      req.flush({});
    });

    it('should not add Authorization header for refresh endpoint', () => {
      authService.getAccessToken.and.returnValue('test-token');

      httpClient.post('/api/v1/auth/refresh', {}).subscribe();

      const req = httpMock.expectOne('/api/v1/auth/refresh');
      expect(req.request.headers.has('Authorization')).toBe(false);
      req.flush({});
    });
  });

  describe('Token Refresh on 401', () => {
    it('should refresh token and retry request on 401 error', (done) => {
      const mockRefreshResponse: LoginResponse = {
        accessToken: 'new-access-token',
        refreshToken: 'new-refresh-token',
        tokenType: 'Bearer',
        expiresIn: 1800
      };

      authService.getAccessToken.and.returnValue('expired-token');
      authService.getRefreshToken.and.returnValue('valid-refresh-token');
      authService.refreshToken.and.returnValue(of(mockRefreshResponse));

      httpClient.get('/api/v1/assets').subscribe({
        next: (response) => {
          expect(response).toEqual({ data: 'success' });
          expect(authService.refreshToken).toHaveBeenCalled();
          done();
        },
        error: () => fail('Should not error')
      });

      // First request with expired token returns 401
      const req1 = httpMock.expectOne('/api/v1/assets');
      expect(req1.request.headers.get('Authorization')).toBe('Bearer expired-token');
      req1.flush(null, { status: 401, statusText: 'Unauthorized' });

      // After token refresh, request is retried with new token
      const req2 = httpMock.expectOne('/api/v1/assets');
      expect(req2.request.headers.get('Authorization')).toBe('Bearer new-access-token');
      req2.flush({ data: 'success' });
    });

    it('should redirect to login when refresh token is not available', (done) => {
      authService.getAccessToken.and.returnValue('expired-token');
      authService.getRefreshToken.and.returnValue(null);
      authService.logout.and.returnValue(of(void 0));

      httpClient.get('/api/v1/assets').subscribe({
        next: () => fail('Should not succeed'),
        error: (error) => {
          expect(error.message).toBe('No refresh token available');
          expect(authService.logout).toHaveBeenCalled();
          expect(router.navigate).toHaveBeenCalledWith(['/login']);
          done();
        }
      });

      const req = httpMock.expectOne('/api/v1/assets');
      req.flush(null, { status: 401, statusText: 'Unauthorized' });
    });

    it('should redirect to login when token refresh fails', (done) => {
      authService.getAccessToken.and.returnValue('expired-token');
      authService.getRefreshToken.and.returnValue('invalid-refresh-token');
      authService.refreshToken.and.returnValue(
        throwError(() => new Error('Refresh failed'))
      );
      authService.logout.and.returnValue(of(void 0));

      httpClient.get('/api/v1/assets').subscribe({
        next: () => fail('Should not succeed'),
        error: (error) => {
          expect(error.message).toBe('Token refresh failed');
          expect(authService.logout).toHaveBeenCalled();
          expect(router.navigate).toHaveBeenCalledWith(['/login'], {
            queryParams: { returnUrl: '/assets', reason: 'session-expired' }
          });
          done();
        }
      });

      const req = httpMock.expectOne('/api/v1/assets');
      req.flush(null, { status: 401, statusText: 'Unauthorized' });
    });

    it('should queue multiple requests during token refresh', (done) => {
      const mockRefreshResponse: LoginResponse = {
        accessToken: 'new-access-token',
        refreshToken: 'new-refresh-token',
        tokenType: 'Bearer',
        expiresIn: 1800
      };

      authService.getAccessToken.and.returnValue('expired-token');
      authService.getRefreshToken.and.returnValue('valid-refresh-token');
      authService.refreshToken.and.returnValue(of(mockRefreshResponse));

      let completedRequests = 0;
      const checkCompletion = () => {
        completedRequests++;
        if (completedRequests === 2) {
          expect(authService.refreshToken).toHaveBeenCalledTimes(1);
          done();
        }
      };

      // Make two simultaneous requests
      httpClient.get('/api/v1/assets').subscribe({
        next: () => checkCompletion(),
        error: () => fail('Request 1 should not error')
      });

      httpClient.get('/api/v1/users').subscribe({
        next: () => checkCompletion(),
        error: () => fail('Request 2 should not error')
      });

      // Both requests return 401
      const req1 = httpMock.expectOne('/api/v1/assets');
      req1.flush(null, { status: 401, statusText: 'Unauthorized' });

      const req2 = httpMock.expectOne('/api/v1/users');
      req2.flush(null, { status: 401, statusText: 'Unauthorized' });

      // Both requests are retried with new token
      const retryReq1 = httpMock.expectOne('/api/v1/assets');
      expect(retryReq1.request.headers.get('Authorization')).toBe('Bearer new-access-token');
      retryReq1.flush({ data: 'assets' });

      const retryReq2 = httpMock.expectOne('/api/v1/users');
      expect(retryReq2.request.headers.get('Authorization')).toBe('Bearer new-access-token');
      retryReq2.flush({ data: 'users' });
    });

    it('should not attempt token refresh for auth endpoints', (done) => {
      authService.getAccessToken.and.returnValue('expired-token');

      httpClient.post('/api/v1/auth/login', {}).subscribe({
        next: () => fail('Should not succeed'),
        error: (error) => {
          expect(authService.refreshToken).not.toHaveBeenCalled();
          done();
        }
      });

      const req = httpMock.expectOne('/api/v1/auth/login');
      req.flush(null, { status: 401, statusText: 'Unauthorized' });
    });
  });

  describe('Error Handling', () => {
    it('should pass through non-401 errors', (done) => {
      authService.getAccessToken.and.returnValue('valid-token');

      httpClient.get('/api/v1/assets').subscribe({
        next: () => fail('Should not succeed'),
        error: (error) => {
          expect(error.status).toBe(403);
          expect(authService.refreshToken).not.toHaveBeenCalled();
          done();
        }
      });

      const req = httpMock.expectOne('/api/v1/assets');
      req.flush(null, { status: 403, statusText: 'Forbidden' });
    });

    it('should handle network errors gracefully', (done) => {
      authService.getAccessToken.and.returnValue('valid-token');

      httpClient.get('/api/v1/assets').subscribe({
        next: () => fail('Should not succeed'),
        error: (error) => {
          expect(error).toBeDefined();
          expect(authService.refreshToken).not.toHaveBeenCalled();
          done();
        }
      });

      const req = httpMock.expectOne('/api/v1/assets');
      req.error(new ProgressEvent('error'));
    });
  });
});
