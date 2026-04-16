import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';

import { AllocationFormComponent } from './allocation-form.component';
import { AllocationService } from '../../../../core/services/allocation.service';
import { AssignmentType } from '../../../../shared/models/allocation.model';

describe('AllocationFormComponent', () => {
  let component: AllocationFormComponent;
  let fixture: ComponentFixture<AllocationFormComponent>;
  let mockAllocationService: jasmine.SpyObj<AllocationService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;
  let mockActivatedRoute: any;

  beforeEach(async () => {
    mockAllocationService = jasmine.createSpyObj('AllocationService', [
      'assignToUser',
      'assignToLocation'
    ]);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
    mockActivatedRoute = {
      snapshot: {
        paramMap: {
          get: jasmine.createSpy('get').and.returnValue('test-asset-id')
        }
      }
    };

    await TestBed.configureTestingModule({
      imports: [
        AllocationFormComponent,
        ReactiveFormsModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: AllocationService, useValue: mockAllocationService },
        { provide: Router, useValue: mockRouter },
        { provide: MatSnackBar, useValue: mockSnackBar },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AllocationFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with default values', () => {
    expect(component.allocationForm).toBeDefined();
    expect(component.allocationForm.get('assignmentType')?.value).toBe(AssignmentType.USER);
    expect(component.allocationForm.get('assignedTo')?.value).toBe('');
    expect(component.allocationForm.get('assignedUserEmail')?.value).toBe('');
  });

  it('should get asset ID from route parameters', () => {
    expect(component.assetId).toBe('test-asset-id');
  });

  describe('Email Validation', () => {
    it('should require email for USER assignment type', () => {
      component.allocationForm.patchValue({
        assignmentType: AssignmentType.USER,
        assignedTo: 'John Doe',
        assignedUserEmail: ''
      });

      const emailControl = component.allocationForm.get('assignedUserEmail');
      expect(emailControl?.hasError('required')).toBe(true);
    });

    it('should not require email for LOCATION assignment type', () => {
      component.allocationForm.patchValue({
        assignmentType: AssignmentType.LOCATION,
        assignedTo: 'Building A',
        assignedUserEmail: ''
      });

      const emailControl = component.allocationForm.get('assignedUserEmail');
      expect(emailControl?.hasError('required')).toBe(false);
    });

    it('should validate email format for USER assignment', () => {
      component.allocationForm.patchValue({
        assignmentType: AssignmentType.USER,
        assignedTo: 'John Doe',
        assignedUserEmail: 'invalid-email'
      });

      const emailControl = component.allocationForm.get('assignedUserEmail');
      expect(emailControl?.hasError('email')).toBe(true);
    });

    it('should update email validation when assignment type changes', () => {
      // Start with USER type
      component.allocationForm.patchValue({
        assignmentType: AssignmentType.USER
      });
      
      let emailControl = component.allocationForm.get('assignedUserEmail');
      expect(emailControl?.hasError('required')).toBe(true);

      // Change to LOCATION type
      component.allocationForm.patchValue({
        assignmentType: AssignmentType.LOCATION
      });

      emailControl = component.allocationForm.get('assignedUserEmail');
      expect(emailControl?.hasError('required')).toBe(false);
    });
  });

  describe('Form Submission', () => {
    it('should call assignToUser when assignment type is USER', () => {
      const mockAssignment = {
        id: 'assignment-1',
        assetId: 'test-asset-id',
        assignmentType: AssignmentType.USER,
        assignedTo: 'John Doe',
        assignedBy: 'admin',
        assignedAt: new Date()
      };

      mockAllocationService.assignToUser.and.returnValue(of(mockAssignment));

      component.allocationForm.patchValue({
        assignmentType: AssignmentType.USER,
        assignedTo: 'John Doe',
        assignedUserEmail: 'john@example.com'
      });

      component.onSubmit();

      expect(mockAllocationService.assignToUser).toHaveBeenCalledWith(
        'test-asset-id',
        jasmine.objectContaining({
          assignmentType: AssignmentType.USER,
          assignedTo: 'John Doe',
          assignedUserEmail: 'john@example.com'
        })
      );
    });

    it('should call assignToLocation when assignment type is LOCATION', () => {
      const mockAssignment = {
        id: 'assignment-1',
        assetId: 'test-asset-id',
        assignmentType: AssignmentType.LOCATION,
        assignedTo: 'Building A',
        assignedBy: 'admin',
        assignedAt: new Date()
      };

      mockAllocationService.assignToLocation.and.returnValue(of(mockAssignment));

      component.allocationForm.patchValue({
        assignmentType: AssignmentType.LOCATION,
        assignedTo: 'Building A'
      });

      component.onSubmit();

      expect(mockAllocationService.assignToLocation).toHaveBeenCalledWith(
        'test-asset-id',
        jasmine.objectContaining({
          assignmentType: AssignmentType.LOCATION,
          assignedTo: 'Building A'
        })
      );
    });

    it('should show success message and navigate on successful assignment', () => {
      const mockAssignment = {
        id: 'assignment-1',
        assetId: 'test-asset-id',
        assignmentType: AssignmentType.USER,
        assignedTo: 'John Doe',
        assignedBy: 'admin',
        assignedAt: new Date()
      };

      mockAllocationService.assignToUser.and.returnValue(of(mockAssignment));

      component.allocationForm.patchValue({
        assignmentType: AssignmentType.USER,
        assignedTo: 'John Doe',
        assignedUserEmail: 'john@example.com'
      });

      component.onSubmit();

      expect(mockSnackBar.open).toHaveBeenCalledWith(
        'Asset assigned successfully',
        'Close',
        { duration: 3000 }
      );
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/assets', 'test-asset-id']);
    });

    it('should show error message on failed assignment', () => {
      const errorMessage = 'Asset already assigned';
      mockAllocationService.assignToUser.and.returnValue(
        throwError(() => new Error(errorMessage))
      );

      component.allocationForm.patchValue({
        assignmentType: AssignmentType.USER,
        assignedTo: 'John Doe',
        assignedUserEmail: 'john@example.com'
      });

      component.onSubmit();

      expect(mockSnackBar.open).toHaveBeenCalledWith(
        errorMessage,
        'Close',
        { duration: 5000 }
      );
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should not submit if form is invalid', () => {
      component.allocationForm.patchValue({
        assignmentType: AssignmentType.USER,
        assignedTo: '',
        assignedUserEmail: ''
      });

      component.onSubmit();

      expect(mockAllocationService.assignToUser).not.toHaveBeenCalled();
      expect(mockAllocationService.assignToLocation).not.toHaveBeenCalled();
    });

    it('should mark all fields as touched when submitting invalid form', () => {
      component.allocationForm.patchValue({
        assignmentType: AssignmentType.USER,
        assignedTo: '',
        assignedUserEmail: ''
      });

      component.onSubmit();

      expect(component.allocationForm.get('assignedTo')?.touched).toBe(true);
      expect(component.allocationForm.get('assignedUserEmail')?.touched).toBe(true);
    });
  });

  describe('Cancel Action', () => {
    it('should navigate to asset detail page on cancel', () => {
      component.onCancel();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/assets', 'test-asset-id']);
    });

    it('should navigate to assets list if no asset ID', () => {
      component.assetId = null;
      component.onCancel();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/assets']);
    });
  });

  describe('Error Messages', () => {
    it('should return correct error message for required field', () => {
      const control = component.allocationForm.get('assignedTo');
      control?.markAsTouched();
      control?.setValue('');

      const errorMessage = component.getErrorMessage('assignedTo');
      expect(errorMessage).toBe('Assigned to is required');
    });

    it('should return correct error message for invalid email', () => {
      component.allocationForm.patchValue({
        assignmentType: AssignmentType.USER
      });
      
      const control = component.allocationForm.get('assignedUserEmail');
      control?.markAsTouched();
      control?.setValue('invalid-email');

      const errorMessage = component.getErrorMessage('assignedUserEmail');
      expect(errorMessage).toBe('Please enter a valid email address');
    });

    it('should return correct error message for max length violation', () => {
      const control = component.allocationForm.get('assignedTo');
      control?.markAsTouched();
      control?.setValue('a'.repeat(256));

      const errorMessage = component.getErrorMessage('assignedTo');
      expect(errorMessage).toContain('must not exceed 255 characters');
    });

    it('should return empty string if field has no errors', () => {
      const control = component.allocationForm.get('assignedTo');
      control?.setValue('Valid Name');

      const errorMessage = component.getErrorMessage('assignedTo');
      expect(errorMessage).toBe('');
    });
  });

  describe('hasError', () => {
    it('should return true if field is invalid and touched', () => {
      const control = component.allocationForm.get('assignedTo');
      control?.markAsTouched();
      control?.setValue('');

      expect(component.hasError('assignedTo')).toBe(true);
    });

    it('should return false if field is valid', () => {
      const control = component.allocationForm.get('assignedTo');
      control?.markAsTouched();
      control?.setValue('Valid Name');

      expect(component.hasError('assignedTo')).toBe(false);
    });

    it('should return false if field is invalid but not touched', () => {
      const control = component.allocationForm.get('assignedTo');
      control?.setValue('');

      expect(component.hasError('assignedTo')).toBe(false);
    });
  });
});
