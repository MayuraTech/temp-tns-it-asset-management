import { FormControl } from '@angular/forms';
import { emailValidator, passwordComplexityValidator, dateNotInFutureValidator } from './custom-validators';

describe('Custom Validators', () => {
  
  describe('emailValidator', () => {
    it('should return null for valid email addresses', () => {
      const validator = emailValidator();
      const control = new FormControl('test@example.com');
      expect(validator(control)).toBeNull();
    });

    it('should validate email with subdomain', () => {
      const validator = emailValidator();
      const control = new FormControl('user@mail.example.com');
      expect(validator(control)).toBeNull();
    });

    it('should validate email with plus sign', () => {
      const validator = emailValidator();
      const control = new FormControl('user+tag@example.com');
      expect(validator(control)).toBeNull();
    });

    it('should validate email with numbers', () => {
      const validator = emailValidator();
      const control = new FormControl('user123@example.com');
      expect(validator(control)).toBeNull();
    });

    it('should return error for invalid email (missing @)', () => {
      const validator = emailValidator();
      const control = new FormControl('testexample.com');
      const result = validator(control);
      expect(result).not.toBeNull();
      expect(result?.['email']).toBeDefined();
    });

    it('should return error for invalid email (missing domain)', () => {
      const validator = emailValidator();
      const control = new FormControl('test@');
      const result = validator(control);
      expect(result).not.toBeNull();
      expect(result?.['email']).toBeDefined();
    });

    it('should return error for invalid email (missing TLD)', () => {
      const validator = emailValidator();
      const control = new FormControl('test@example');
      const result = validator(control);
      expect(result).not.toBeNull();
      expect(result?.['email']).toBeDefined();
    });

    it('should return error for invalid email (spaces)', () => {
      const validator = emailValidator();
      const control = new FormControl('test @example.com');
      const result = validator(control);
      expect(result).not.toBeNull();
      expect(result?.['email']).toBeDefined();
    });

    it('should return null for empty value', () => {
      const validator = emailValidator();
      const control = new FormControl('');
      expect(validator(control)).toBeNull();
    });

    it('should return null for null value', () => {
      const validator = emailValidator();
      const control = new FormControl(null);
      expect(validator(control)).toBeNull();
    });
  });

  describe('passwordComplexityValidator', () => {
    it('should return null for valid password', () => {
      const validator = passwordComplexityValidator();
      const control = new FormControl('Test@1234');
      expect(validator(control)).toBeNull();
    });

    it('should return null for complex password', () => {
      const validator = passwordComplexityValidator();
      const control = new FormControl('MyP@ssw0rd!');
      expect(validator(control)).toBeNull();
    });

    it('should return error for password too short', () => {
      const validator = passwordComplexityValidator();
      const control = new FormControl('Test@1');
      const result = validator(control);
      expect(result).not.toBeNull();
      expect(result?.['passwordComplexity']?.['minLength']).toBeDefined();
    });

    it('should return error for password without uppercase', () => {
      const validator = passwordComplexityValidator();
      const control = new FormControl('test@1234');
      const result = validator(control);
      expect(result).not.toBeNull();
      expect(result?.['passwordComplexity']?.['uppercase']).toBeDefined();
    });

    it('should return error for password without lowercase', () => {
      const validator = passwordComplexityValidator();
      const control = new FormControl('TEST@1234');
      const result = validator(control);
      expect(result).not.toBeNull();
      expect(result?.['passwordComplexity']?.['lowercase']).toBeDefined();
    });

    it('should return error for password without number', () => {
      const validator = passwordComplexityValidator();
      const control = new FormControl('Test@Password');
      const result = validator(control);
      expect(result).not.toBeNull();
      expect(result?.['passwordComplexity']?.['number']).toBeDefined();
    });

    it('should return error for password without special character', () => {
      const validator = passwordComplexityValidator();
      const control = new FormControl('Test1234');
      const result = validator(control);
      expect(result).not.toBeNull();
      expect(result?.['passwordComplexity']?.['specialChar']).toBeDefined();
    });

    it('should return multiple errors for weak password', () => {
      const validator = passwordComplexityValidator();
      const control = new FormControl('test');
      const result = validator(control);
      expect(result).not.toBeNull();
      expect(result?.['passwordComplexity']?.['minLength']).toBeDefined();
      expect(result?.['passwordComplexity']?.['uppercase']).toBeDefined();
      expect(result?.['passwordComplexity']?.['number']).toBeDefined();
      expect(result?.['passwordComplexity']?.['specialChar']).toBeDefined();
    });

    it('should return null for empty value', () => {
      const validator = passwordComplexityValidator();
      const control = new FormControl('');
      expect(validator(control)).toBeNull();
    });

    it('should return null for null value', () => {
      const validator = passwordComplexityValidator();
      const control = new FormControl(null);
      expect(validator(control)).toBeNull();
    });

    it('should accept various special characters', () => {
      const validator = passwordComplexityValidator();
      const specialChars = ['!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '+', '='];
      
      specialChars.forEach(char => {
        const control = new FormControl(`Test1234${char}`);
        expect(validator(control)).toBeNull();
      });
    });
  });

  describe('dateNotInFutureValidator', () => {
    it('should return null for today\'s date', () => {
      const validator = dateNotInFutureValidator();
      const today = new Date();
      const control = new FormControl(today.toISOString().split('T')[0]);
      expect(validator(control)).toBeNull();
    });

    it('should return null for past date', () => {
      const validator = dateNotInFutureValidator();
      const pastDate = new Date('2020-01-15');
      const control = new FormControl(pastDate.toISOString().split('T')[0]);
      expect(validator(control)).toBeNull();
    });

    it('should return null for date object in the past', () => {
      const validator = dateNotInFutureValidator();
      const pastDate = new Date('2020-01-15');
      const control = new FormControl(pastDate);
      expect(validator(control)).toBeNull();
    });

    it('should return error for future date', () => {
      const validator = dateNotInFutureValidator();
      const futureDate = new Date();
      futureDate.setDate(futureDate.getDate() + 1);
      const control = new FormControl(futureDate.toISOString().split('T')[0]);
      const result = validator(control);
      expect(result).not.toBeNull();
      expect(result?.['futureDate']).toBeDefined();
    });

    it('should return error for date far in the future', () => {
      const validator = dateNotInFutureValidator();
      const futureDate = new Date('2030-12-31');
      const control = new FormControl(futureDate.toISOString().split('T')[0]);
      const result = validator(control);
      expect(result).not.toBeNull();
      expect(result?.['futureDate']).toBeDefined();
    });

    it('should return error for invalid date string', () => {
      const validator = dateNotInFutureValidator();
      const control = new FormControl('invalid-date');
      const result = validator(control);
      expect(result).not.toBeNull();
      expect(result?.['invalidDate']).toBeDefined();
    });

    it('should return null for empty value', () => {
      const validator = dateNotInFutureValidator();
      const control = new FormControl('');
      expect(validator(control)).toBeNull();
    });

    it('should return null for null value', () => {
      const validator = dateNotInFutureValidator();
      const control = new FormControl(null);
      expect(validator(control)).toBeNull();
    });

    it('should handle ISO date strings', () => {
      const validator = dateNotInFutureValidator();
      const pastDate = '2020-01-15T10:30:00Z';
      const control = new FormControl(pastDate);
      expect(validator(control)).toBeNull();
    });

    it('should ignore time component when comparing dates', () => {
      const validator = dateNotInFutureValidator();
      const today = new Date();
      today.setHours(23, 59, 59, 999); // End of today
      const control = new FormControl(today);
      expect(validator(control)).toBeNull();
    });
  });
});
