# Custom Validators

This directory contains reusable form validators for the IT Asset Management application.

## Available Validators

### emailValidator

Validates email format with comprehensive regex pattern.

**Usage:**
```typescript
import { emailValidator } from '@app/shared';
import { FormBuilder, Validators } from '@angular/forms';

this.form = this.fb.group({
  email: ['', [Validators.required, emailValidator()]]
});
```

**Validation Rules:**
- Standard email format: `user@domain.tld`
- Supports subdomains: `user@mail.example.com`
- Supports plus addressing: `user+tag@example.com`
- Supports numbers and special characters in local part

**Error Object:**
```typescript
{ email: { value: 'invalid-email' } }
```

### passwordComplexityValidator

Validates password meets security complexity requirements.

**Usage:**
```typescript
import { passwordComplexityValidator } from '@app/shared';

this.form = this.fb.group({
  password: ['', [Validators.required, passwordComplexityValidator()]]
});
```

**Validation Rules:**
- Minimum 8 characters
- At least one uppercase letter (A-Z)
- At least one lowercase letter (a-z)
- At least one number (0-9)
- At least one special character (!@#$%^&*()_+-=[]{};\:'"|,.<>/?)

**Error Object:**
```typescript
{
  passwordComplexity: {
    minLength?: { requiredLength: 8, actualLength: number },
    uppercase?: { message: string },
    lowercase?: { message: string },
    number?: { message: string },
    specialChar?: { message: string }
  }
}
```

**Display Errors:**
```typescript
get passwordErrors() {
  const errors = this.form.get('password')?.errors?.['passwordComplexity'];
  if (!errors) return [];
  
  const messages = [];
  if (errors.minLength) messages.push('At least 8 characters');
  if (errors.uppercase) messages.push('One uppercase letter');
  if (errors.lowercase) messages.push('One lowercase letter');
  if (errors.number) messages.push('One number');
  if (errors.specialChar) messages.push('One special character');
  
  return messages;
}
```

### dateNotInFutureValidator

Ensures date is not in the future (today or earlier).

**Usage:**
```typescript
import { dateNotInFutureValidator } from '@app/shared';

this.form = this.fb.group({
  acquisitionDate: ['', [Validators.required, dateNotInFutureValidator()]]
});
```

**Validation Rules:**
- Date must be today or in the past
- Ignores time component (compares dates only)
- Validates date is valid before checking

**Error Objects:**
```typescript
// For future dates
{ futureDate: { value: string, message: 'Date cannot be in the future' } }

// For invalid dates
{ invalidDate: { value: string } }
```

## Common Patterns

### Combining Validators

```typescript
this.form = this.fb.group({
  email: ['', [
    Validators.required,
    emailValidator()
  ]],
  password: ['', [
    Validators.required,
    passwordComplexityValidator()
  ]],
  acquisitionDate: ['', [
    Validators.required,
    dateNotInFutureValidator()
  ]]
});
```

### Displaying Validation Errors

```typescript
// In component
hasError(field: string, errorType: string): boolean {
  const control = this.form.get(field);
  return control?.hasError(errorType) && (control?.dirty || control?.touched);
}

// In template
<mat-error *ngIf="hasError('email', 'required')">
  Email is required
</mat-error>
<mat-error *ngIf="hasError('email', 'email')">
  Please enter a valid email address
</mat-error>
```

### Custom Error Messages

```typescript
getErrorMessage(field: string): string {
  const control = this.form.get(field);
  
  if (control?.hasError('required')) {
    return 'This field is required';
  }
  
  if (control?.hasError('email')) {
    return 'Please enter a valid email address';
  }
  
  if (control?.hasError('futureDate')) {
    return 'Date cannot be in the future';
  }
  
  if (control?.hasError('passwordComplexity')) {
    const errors = control.errors['passwordComplexity'];
    return 'Password must meet complexity requirements';
  }
  
  return '';
}
```

## Testing

All validators include comprehensive unit tests covering:
- Valid inputs
- Invalid inputs
- Edge cases (null, undefined, empty)
- Multiple validation errors
- Various input formats

Run tests with:

```bash
npm test
```

## Best Practices

1. **Don't validate empty values** - Use `Validators.required` separately
2. **Return null for valid values** - Angular convention
3. **Provide descriptive error objects** - Include helpful information
4. **Test edge cases** - null, undefined, empty, invalid formats
5. **Combine validators** - Use multiple validators for comprehensive validation
6. **Show errors after interaction** - Check `dirty` or `touched` state
