import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { LoginResponse, User, Role } from '../models/auth.model';
import { environment } from '../../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const mockLoginResponse: LoginResponse = {
    accessToken: 'mock-access-token',
    refreshToken: 'mock-refresh-token',
    tokenType: 'Bearer',
    expiresIn: 1800
  };

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
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    
    // Clear localStorage before each test
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  describe('login', () => {
    it('should authenticate user and store tokens', (done) => {
      service.login('testuser', 'password').subscribe({
        next: (response) => {
          expect(response).toEqual(mockLoginResponse);
          expect(localStorage.getItem('access_token')).toBe(mockLoginResponse.accessToken);
          expect(localStorage.getItem('refresh_token')).toBe(mockLoginResponse.refreshToken);
          done();
        }
      });

      const loginReq = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
      expect(loginReq.request.method).toBe('POST');
      expect(loginReq.request.body).toEqual({ username: 'testuser', password: 'password' });
      loginReq.flush(mockLoginResponse);

      const userReq = httpMock.expectOne(`${environment.apiUrl}/users/me`);
      userReq.flush(mockUser);
    });

    it('should handle login failure', (done) => {
      service.login('testuser', 'wrongpassword').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
          expect(localStorage.getItem('access_token')).toBeNull();
          done();
        }
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
      req.flush({ error: { message: 'Invalid credentials' } }, { status: 401, statusText: 'Unauthorized' });
    });
  });

  describe('logout', () => {
    it('should clear session on logout', (done) => {
      // Setup: store tokens
      localStorage.setItem('access_token', 'token');
      localStorage.setItem('refresh_token', 'refresh');
      localStorage.setItem('current_user', JSON.stringify(mockUser));

      service.logout().subscribe({
        next: () => {
          expect(localStorage.getItem('access_token')).toBeNull();
          expect(localStorage.getItem('refresh_token')).toBeNull();
          expect(localStorage.getItem('current_user')).toBeNull();
          expect(service.currentUserValue).toBeNull();
          done();
        }
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/logout`);
      expect(req.request.method).toBe('POST');
      req.flush({});
    });

    it('should clear session even if logout request fails', (done) => {
      localStorage.setItem('access_token', 'token');

      service.logout().subscribe({
        error: () => {
          expect(localStorage.getItem('access_token')).toBeNull();
          done();
        }
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/logout`);
      req.flush({}, { status: 500, statusText: 'Server Error' });
    });
  });

  describe('refreshToken', () => {
    it('should refresh access token', (done) => {
      localStorage.setItem('refresh_token', 'old-refresh-token');

      service.refreshToken().subscribe({
        next: (response) => {
          expect(response).toEqual(mockLoginResponse);
          expect(localStorage.getItem('access_token')).toBe(mockLoginResponse.accessToken);
          done();
        }
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/refresh`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ refreshToken: 'old-refresh-token' });
      req.flush(mockLoginResponse);
    });

    it('should clear session if refresh fails', (done) => {
      localStorage.setItem('refresh_token', 'invalid-token');
      localStorage.setItem('access_token', 'token');

      service.refreshToken().subscribe({
        error: () => {
          expect(localStorage.getItem('access_token')).toBeNull();
          expect(localStorage.getItem('refresh_token')).toBeNull();
          done();
        }
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/refresh`);
      req.flush({}, { status: 401, statusText: 'Unauthorized' });
    });

    it('should return error if no refresh token available', (done) => {
      service.refreshToken().subscribe({
        error: (error) => {
          expect(error.message).toContain('No refresh token available');
          done();
        }
      });
    });
  });

  describe('isAuthenticated', () => {
    it('should return true when user and token exist', () => {
      localStorage.setItem('access_token', 'token');
      service['currentUserSubject'].next(mockUser);

      expect(service.isAuthenticated).toBe(true);
    });

    it('should return false when no token exists', () => {
      service['currentUserSubject'].next(mockUser);

      expect(service.isAuthenticated).toBe(false);
    });

    it('should return false when no user exists', () => {
      localStorage.setItem('access_token', 'token');

      expect(service.isAuthenticated).toBe(false);
    });
  });

  describe('getAccessToken', () => {
    it('should return access token from storage', () => {
      localStorage.setItem('access_token', 'test-token');

      expect(service.getAccessToken()).toBe('test-token');
    });

    it('should return null if no token exists', () => {
      expect(service.getAccessToken()).toBeNull();
    });
  });

  describe('changePassword', () => {
    it('should send password change request', (done) => {
      service.changePassword('oldPass', 'newPass').subscribe({
        next: () => {
          done();
        }
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/change-password`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({
        currentPassword: 'oldPass',
        newPassword: 'newPass'
      });
      req.flush({});
    });
  });
});
