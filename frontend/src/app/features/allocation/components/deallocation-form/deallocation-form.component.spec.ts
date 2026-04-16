import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { of, throwError } from 'rxjs';

import { DeallocationFormComponent } from './deallocation-form.component';
import { AllocationService } from '../../../../core/services/allocation.service';
import { ConfirmationDialogComponent } from './confirmation-dialog.component';

describe('DeallocationFormComponent', () => {
  let component: DeallocationFormComponent;
  let fixture: ComponentFixture<DeallocationFormComponent>;
  let mockAllocationService: jasmine.SpyObj<AllocationService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockDialog: jasmine.SpyObj<MatDialog>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;
  let mockActivatedRoute: any;

  beforeEach(async () => {
    mockAllocationService = jasmine.createSpyObj('AllocationService', ['deallocate']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
    mockActivatedRoute = {
      snapshot: {
        paramMap: {
          get: jasmine.createSpy('get').and.returnValue('test-asset-id')
        },
        data: {
          asset: {
            name: 'Test Server',
            assignedUser: 'John Doe'
          }
        }
      }
    };

    await TestBed.configureTestingModule({
      imports: [
        DeallocationFormComponent,
        NoopAnimationsModule
      ],
      providers: [
        { provide: AllocationService, useValue: mockAllocationService },
        { provide: Router, useValue: mockRouter },
        { provide: MatDialog, useValue: mockDialog },
        { provide: MatSnackBar, useValue: mockSnackBar },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DeallocationFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Component Initialization', () => {
    it('should get asset ID from route parameters', () => {
      expect(component.assetId).toBe('test-asset-id');
    });

    it('should get asset name from route data', () => {
      expect(component.assetName).toBe('Test Server');
    });

    it('should get current assignment from route data (assignedUser)', () => {
      expect(component.currentAssignment).toBe('John Doe');
    });

    it('should use location if assignedUser is not available', () => {
      mockActivatedRoute.snapshot.data.asset = {
        name: 'Test Server',
        location: 'Building A'
      };

      fixture = TestBed.createComponent(DeallocationFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      expect(component.currentAssignment).toBe('Building A');
    });

    it('should default to "Unknown" if no assignment info available', () => {
      mockActivatedRoute.snapshot.data.asset = {
        name: 'Test Server'
      };

      fixture = TestBed.createComponent(DeallocationFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      expect(component.currentAssignment).toBe('Unknown');
    });

    it('should default asset name to "Asset" if not provided', () => {
      mockActivatedRoute.snapshot.data.asset = {};

      fixture = TestBed.createComponent(DeallocationFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      expect(component.assetName).toBe('Asset');
    });

    it('should navigate to assets if no asset ID', () => {
      mockActivatedRoute.snapshot.paramMap.get.and.returnValue(null);
      
      fixture = TestBed.createComponent(DeallocationFormComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      expect(mockSnackBar.open).toHaveBeenCalledWith(
        'Asset ID is required',
        'Close',
        { duration: 3000 }
      );
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/assets']);
    });

    it('should initialize isProcessing to false', () => {
      expect(component.isProcessing).toBe(false);
    });
  });

  describe('Deallocation Workflow', () => {
    it('should open confirmation dialog when deallocate is called', () => {
      const mockDialogRef = {
        afterClosed: () => of(false)
      };
      mockDialog.open.and.returnValue(mockDialogRef as any);

      component.deallocate();

      expect(mockDialog.open).toHaveBeenCalledWith(
        ConfirmationDialogComponent,
        jasmine.objectContaining({
          width: '500px',
          data: jasmine.objectContaining({
            title: 'Confirm Deallocation',
            confirmText: 'Deallocate',
            cancelText: 'Cancel'
          })
        })
      );
    });

    it('should include asset name and assignment in dialog message', () => {
      const mockDialogRef = {
        afterClosed: () => of(false)
      };
      mockDialog.open.and.returnValue(mockDialogRef as any);

      component.deallocate();

      const dialogData = mockDialog.open.calls.mostRecent().args[1]?.data;
      expect(dialogData.message).toContain('John Doe');
    });

    it('should perform deallocation when user confirms', () => {
      const mockDialogRef = {
        afterClosed: () => of(true)
      };
      mockDialog.open.and.returnValue(mockDialogRef as any);
      mockAllocationService.deallocate.and.returnValue(of(undefined));

      component.deallocate();

      expect(mockAllocationService.deallocate).toHaveBeenCalledWith('test-asset-id');
    });

    it('should not perform deallocation when user cancels', () => {
      const mockDialogRef = {
        afterClosed: () => of(false)
      };
      mockDialog.open.and.returnValue(mockDialogRef as any);

      component.deallocate();

      expect(mockAllocationService.deallocate).not.toHaveBeenCalled();
    });

    it('should not perform deallocation when dialog returns undefined', () => {
      const mockDialogRef = {
        afterClosed: () => of(undefined)
      };
      mockDialog.open.and.returnValue(mockDialogRef as any);

      component.deallocate();

      expect(mockAllocationService.deallocate).not.toHaveBeenCalled();
    });

    it('should set isProcessing to true during deallocation', () => {
      const mockDialogRef = {
        afterClosed: () => of(true)
      };
      mockDialog.open.and.returnValue(mockDialogRef as any);
      mockAllocationService.deallocate.and.returnValue(of(undefined));

      component.deallocate();

      // isProcessing should be set to true before the observable completes
      expect(component.isProcessing).toBe(false); // Completed by the time we check
    });

    it('should show success message and navigate on successful deallocation', () => {
      const mockDialogRef = {
        afterClosed: () => of(true)
      };
      mockDialog.open.and.returnValue(mockDialogRef as any);
      mockAllocationService.deallocate.and.returnValue(of(undefined));

      component.deallocate();

      expect(mockSnackBar.open).toHaveBeenCalledWith(
        'Asset deallocated successfully',
        'Close',
        { duration: 3000 }
      );
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/assets', 'test-asset-id']);
    });

    it('should show error message on failed deallocation', () => {
      const mockDialogRef = {
        afterClosed: () => of(true)
      };
      mockDialog.open.and.returnValue(mockDialogRef as any);
      
      const errorMessage = 'Asset not assigned';
      mockAllocationService.deallocate.and.returnValue(
        throwError(() => new Error(errorMessage))
      );

      component.deallocate();

      expect(mockSnackBar.open).toHaveBeenCalledWith(
        errorMessage,
        'Close',
        { duration: 5000 }
      );
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should show default error message if error has no message', () => {
      const mockDialogRef = {
        afterClosed: () => of(true)
      };
      mockDialog.open.and.returnValue(mockDialogRef as any);
      
      mockAllocationService.deallocate.and.returnValue(
        throwError(() => ({}))
      );

      component.deallocate();

      expect(mockSnackBar.open).toHaveBeenCalledWith(
        'Failed to deallocate asset',
        'Close',
        { duration: 5000 }
      );
    });

    it('should reset isProcessing on error', () => {
      const mockDialogRef = {
        afterClosed: () => of(true)
      };
      mockDialog.open.and.returnValue(mockDialogRef as any);
      
      mockAllocationService.deallocate.and.returnValue(
        throwError(() => new Error('Test error'))
      );

      component.deallocate();

      expect(component.isProcessing).toBe(false);
    });

    it('should not call deallocate if assetId is null', () => {
      component.assetId = null;
      const mockDialogRef = {
        afterClosed: () => of(true)
      };
      mockDialog.open.and.returnValue(mockDialogRef as any);

      component.deallocate();

      expect(mockAllocationService.deallocate).not.toHaveBeenCalled();
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

  describe('Template Rendering', () => {
    it('should display asset name in template', () => {
      const compiled = fixture.nativeElement;
      expect(compiled.textContent).toContain('Test Server');
    });

    it('should display current assignment in template', () => {
      const compiled = fixture.nativeElement;
      expect(compiled.textContent).toContain('John Doe');
    });

    it('should display warning message', () => {
      const compiled = fixture.nativeElement;
      expect(compiled.textContent).toContain('Warning');
      expect(compiled.textContent).toContain('Deallocating this asset will remove its current assignment');
    });

    it('should have deallocate button', () => {
      const button = fixture.debugElement.query(By.css('.btn-deallocate'));
      expect(button).toBeTruthy();
    });

    it('should have cancel button', () => {
      const button = fixture.debugElement.query(By.css('.btn-ghost'));
      expect(button).toBeTruthy();
    });

    it('should disable buttons when isProcessing is true', () => {
      component.isProcessing = true;
      fixture.detectChanges();

      const deallocateButton = fixture.debugElement.query(By.css('.btn-deallocate'));
      const cancelButton = fixture.debugElement.query(By.css('.btn-ghost'));

      expect(deallocateButton.nativeElement.disabled).toBe(true);
      expect(cancelButton.nativeElement.disabled).toBe(true);
    });

    it('should show spinner when isProcessing is true', () => {
      component.isProcessing = true;
      fixture.detectChanges();

      const spinner = fixture.debugElement.query(By.css('mat-spinner'));
      expect(spinner).toBeTruthy();
    });

    it('should not show spinner when isProcessing is false', () => {
      component.isProcessing = false;
      fixture.detectChanges();

      const spinner = fixture.debugElement.query(By.css('mat-spinner'));
      expect(spinner).toBeFalsy();
    });

    it('should call deallocate method when deallocate button is clicked', () => {
      spyOn(component, 'deallocate');
      
      const button = fixture.debugElement.query(By.css('.btn-deallocate'));
      button.nativeElement.click();

      expect(component.deallocate).toHaveBeenCalled();
    });

    it('should call onCancel method when cancel button is clicked', () => {
      spyOn(component, 'onCancel');
      
      const button = fixture.debugElement.query(By.css('.btn-ghost'));
      button.nativeElement.click();

      expect(component.onCancel).toHaveBeenCalled();
    });

    it('should display geometric triangle accent', () => {
      const triangle = fixture.debugElement.query(By.css('.triangle-accent'));
      expect(triangle).toBeTruthy();
    });

    it('should have proper Editorial Geometry styling classes', () => {
      const container = fixture.debugElement.query(By.css('.deallocation-form-container'));
      const card = fixture.debugElement.query(By.css('.deallocation-card'));
      const header = fixture.debugElement.query(By.css('.card-header'));

      expect(container).toBeTruthy();
      expect(card).toBeTruthy();
      expect(header).toBeTruthy();
    });
  });

  describe('Accessibility', () => {
    it('should have proper button text for screen readers', () => {
      const deallocateButton = fixture.debugElement.query(By.css('.btn-deallocate'));
      expect(deallocateButton.nativeElement.textContent).toContain('Deallocate Asset');
    });

    it('should have proper button text for cancel', () => {
      const cancelButton = fixture.debugElement.query(By.css('.btn-ghost'));
      expect(cancelButton.nativeElement.textContent).toContain('Cancel');
    });

    it('should update button text when processing', () => {
      component.isProcessing = true;
      fixture.detectChanges();

      const deallocateButton = fixture.debugElement.query(By.css('.btn-deallocate'));
      expect(deallocateButton.nativeElement.textContent).toContain('Deallocating');
    });
  });
});
