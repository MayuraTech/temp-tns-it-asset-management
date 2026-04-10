import { TestBed } from '@angular/core/testing';
import { ValidationService } from './validation.service';

describe('ValidationService', () => {
  let service: ValidationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ValidationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('isRequired', () => {
    it('should return false for null value', () => {
      expect(service.isRequired(null)).toBe(false);
    });

    it('should return false for undefined value', () => {
      expect(service.isRequired(undefined)).toBe(false);
    });

    it('should return false for empty string', () => {
      expect(service.isRequired('')).toBe(false);
    });

    it('should return false for whitespace-only string', () => {
      expect(service.isRequired('   ')).toBe(false);
    });

    it('should return true for non-empty string', () => {
      expect(service.isRequired('test')).toBe(true);
    });
  });

  describe('validateUsername', () => {
    it('should return error message for null username', () => {
      expect(service.validateUsername(null)).toBe('Username is required');
    });

    it('should return error message for undefined username', () => {
      expect(service.validateUsername(undefined)).toBe('Username is required');
    });

    it('should return error message for empty username', () => {
      expect(service.validateUsername('')).toBe('Username is required');
    });

    it('should return error message for whitespace-only username', () => {
      expect(service.validateUsername('   ')).toBe('Username is required');
    });

    it('should return null for valid username', () => {
      expect(service.validateUsername('testuser')).toBeNull();
    });
  });

  describe('validatePassword', () => {
    it('should return error message for null password', () => {
      expect(service.validatePassword(null)).toBe('Password is required');
    });

    it('should return error message for undefined password', () => {
      expect(service.validatePassword(undefined)).toBe('Password is required');
    });

    it('should return error message for empty password', () => {
      expect(service.validatePassword('')).toBe('Password is required');
    });

    it('should return error message for whitespace-only password', () => {
      expect(service.validatePassword('   ')).toBe('Password is required');
    });

    it('should return null for valid password', () => {
      expect(service.validatePassword('password123')).toBeNull();
    });
  });

  describe('validateLoginForm', () => {
    it('should return errors for both fields when empty', () => {
      const errors = service.validateLoginForm('', '');
      expect(errors.username).toBe('Username is required');
      expect(errors.password).toBe('Password is required');
    });

    it('should return error only for username when password is valid', () => {
      const errors = service.validateLoginForm('', 'password123');
      expect(errors.username).toBe('Username is required');
      expect(errors.password).toBeNull();
    });

    it('should return error only for password when username is valid', () => {
      const errors = service.validateLoginForm('testuser', '');
      expect(errors.username).toBeNull();
      expect(errors.password).toBe('Password is required');
    });

    it('should return no errors when both fields are valid', () => {
      const errors = service.validateLoginForm('testuser', 'password123');
      expect(errors.username).toBeNull();
      expect(errors.password).toBeNull();
    });
  });

  describe('isFormValid', () => {
    it('should return false when both fields are empty', () => {
      expect(service.isFormValid('', '')).toBe(false);
    });

    it('should return false when username is empty', () => {
      expect(service.isFormValid('', 'password123')).toBe(false);
    });

    it('should return false when password is empty', () => {
      expect(service.isFormValid('testuser', '')).toBe(false);
    });

    it('should return true when both fields are valid', () => {
      expect(service.isFormValid('testuser', 'password123')).toBe(true);
    });
  });

  describe('getErrorMessage', () => {
    it('should return correct error message for username required', () => {
      expect(service.getErrorMessage('username', 'required')).toBe('Username is required');
    });

    it('should return correct error message for password required', () => {
      expect(service.getErrorMessage('password', 'required')).toBe('Password is required');
    });

    it('should return default message for unknown field', () => {
      expect(service.getErrorMessage('unknown', 'required')).toBe('Invalid input');
    });

    it('should return default message for unknown error type', () => {
      expect(service.getErrorMessage('username', 'unknown')).toBe('Invalid input');
    });
  });
});
