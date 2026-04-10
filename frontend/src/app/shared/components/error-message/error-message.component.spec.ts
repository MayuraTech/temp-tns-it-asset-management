import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ErrorMessageComponent } from './error-message.component';
import { AuthError } from '../../../core/models/error.model';

describe('ErrorMessageComponent - Error Type Handling', () => {
  let component: ErrorMessageComponent;
  let fixture: ComponentFixture<ErrorMessageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ErrorMessageComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(ErrorMessageComponent);
    component = fixture.componentInstance;
  });

  describe('displayMessage getter', () => {
    it('should return invalid credentials message for invalid_credentials error type', () => {
      // Requirement 4.1
      const error: AuthError = {
        type: 'invalid_credentials',
        message: 'Auth failed',
        timestamp: new Date()
      };
      component.error = error;

      expect(component.displayMessage).toBe('Invalid username or password. Please try again.');
    });

    it('should return account locked message for account_locked error type', () => {
      // Requirement 4.2
      const error: AuthError = {
        type: 'account_locked',
        message: 'Account locked',
        timestamp: new Date()
      };
      component.error = error;

      expect(component.displayMessage).toBe('Account locked due to multiple failed attempts. Please try again in 15 minutes.');
    });

    it('should return network error message for network_error error type', () => {
      // Requirement 4.3
      const error: AuthError = {
        type: 'network_error',
        message: 'Network failed',
        timestamp: new Date()
      };
      component.error = error;

      expect(component.displayMessage).toBe('Unable to connect to server. Please check your connection and try again.');
    });

    it('should return generic message for unknown error type', () => {
      const error: AuthError = {
        type: 'unknown',
        message: 'Something went wrong',
        timestamp: new Date()
      };
      component.error = error;

      expect(component.displayMessage).toBe('Something went wrong');
    });

    it('should return generic fallback message when error message is empty', () => {
      const error: AuthError = {
        type: 'unknown',
        message: '',
        timestamp: new Date()
      };
      component.error = error;

      expect(component.displayMessage).toBe('An unexpected error occurred. Please try again.');
    });

    it('should return empty string when error is null', () => {
      component.error = null;

      expect(component.displayMessage).toBe('');
    });
  });

  describe('error message display in template', () => {
    it('should display the mapped error message in the template', () => {
      const error: AuthError = {
        type: 'invalid_credentials',
        message: 'Auth failed',
        timestamp: new Date()
      };
      component.error = error;
      fixture.detectChanges();

      const errorText = fixture.nativeElement.querySelector('.error-text');
      expect(errorText.textContent).toBe('Invalid username or password. Please try again.');
    });

    it('should not display error message when error is null', () => {
      component.error = null;
      fixture.detectChanges();

      const errorMessage = fixture.nativeElement.querySelector('.error-message');
      expect(errorMessage).toBeNull();
    });
  });
});
