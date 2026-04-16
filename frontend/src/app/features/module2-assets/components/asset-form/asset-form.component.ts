import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { AssetService } from '../../services/asset.service';
import { Asset, AssetRequest, AssetType, LifecycleStatus } from '../../models';
import { ImageUploadComponent } from '../shared/image-upload/image-upload.component';

/**
 * Asset Form Component
 * 
 * Handles both creation and editing of assets with a comprehensive form layout.
 * Implements Editorial Geometry design system with three-section layout and side panel.
 * 
 * Features:
 * - Reactive form with comprehensive validation
 * - Create and edit modes in the same component
 * - Three-section layout: General Details, Lifecycle & Warranty, Asset Tracking
 * - Side panel with Visual Identity Card, Recent Activity, and Technical Specs
 * - Read-only serial number in edit mode
 * - Responsive design for mobile/tablet
 */
@Component({
  selector: 'app-asset-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    ImageUploadComponent
  ],
  templateUrl: './asset-form.component.html',
  styleUrls: ['./asset-form.component.scss']
})
export class AssetFormComponent implements OnInit, OnDestroy {
  assetForm!: FormGroup;
  isEditMode = false;
  assetId: string | null = null;
  loading = false;
  submitting = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  
  // Enum references for template
  assetTypes = Object.values(AssetType);
  lifecycleStatuses = Object.values(LifecycleStatus);
  
  // Current asset data (for edit mode)
  currentAsset: Asset | null = null;
  
  // Image upload
  selectedImageFile: File | null = null;
  imageUploadError: string | null = null;
  uploadingImage = false;
  
  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private assetService: AssetService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.checkEditMode();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Initialize the reactive form with all asset fields and validators
   */
  private initializeForm(): void {
    this.assetForm = this.fb.group({
      // Section 1: General Details
      assetType: ['', Validators.required],
      manufacturer: ['', [Validators.maxLength(255)]],
      modelName: ['', [Validators.maxLength(255)]],
      serialNumber: ['', [
        Validators.required,
        Validators.minLength(5),
        Validators.maxLength(100)
      ]],
      
      // Section 2: Lifecycle & Warranty
      purchaseDate: ['', [Validators.required, this.dateNotInFutureValidator()]],
      warrantyExpiry: [''],
      costCenter: ['', [Validators.maxLength(255)]],
      purchaseValue: ['', [Validators.min(0)]],
      
      // Section 3: Asset Tracking
      status: ['', Validators.required],
      assignedUser: ['', [Validators.maxLength(255)]],
      officeLocation: ['', [Validators.maxLength(255)]],
      ipAddress: ['', [this.ipAddressValidator()]],
      
      // Additional fields
      notes: ['']
    });
  }

  /**
   * Check if we're in edit mode and load asset data if needed
   */
  private checkEditMode(): void {
    this.assetId = this.route.snapshot.paramMap.get('id');
    
    if (this.assetId) {
      this.isEditMode = true;
      this.loadAsset(this.assetId);
    }
  }

  /**
   * Load asset data for editing
   */
  private loadAsset(id: string): void {
    this.loading = true;
    this.errorMessage = null;
    
    this.assetService.getAsset(id)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading = false)
      )
      .subscribe({
        next: (asset) => {
          this.currentAsset = asset;
          this.populateForm(asset);
          
          // Make serial number read-only in edit mode
          if (this.isEditMode) {
            this.assetForm.get('serialNumber')?.disable();
          }
        },
        error: (error) => {
          this.errorMessage = error.message || 'Failed to load asset';
        }
      });
  }

  /**
   * Populate form with existing asset data
   */
  private populateForm(asset: Asset): void {
    this.assetForm.patchValue({
      assetType: asset.assetType,
      manufacturer: '', // Not in current model, would need backend extension
      modelName: asset.name,
      serialNumber: asset.serialNumber,
      purchaseDate: asset.acquisitionDate,
      warrantyExpiry: '',
      costCenter: '',
      purchaseValue: '',
      status: asset.status,
      assignedUser: asset.assignedUser || '',
      officeLocation: asset.location || '',
      ipAddress: '',
      notes: asset.notes || ''
    });
  }

  /**
   * Custom validator: Date cannot be in the future
   */
  private dateNotInFutureValidator() {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) {
        return null;
      }
      
      const inputDate = new Date(control.value);
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      return inputDate > today 
        ? { futureDate: { value: control.value } }
        : null;
    };
  }

  /**
   * Custom validator: IP address format
   */
  private ipAddressValidator() {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) {
        return null;
      }
      
      const ipPattern = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
      
      return ipPattern.test(control.value)
        ? null
        : { invalidIpAddress: { value: control.value } };
    };
  }

  /**
   * Handle form submission
   */
  onSubmit(): void {
    if (this.assetForm.invalid) {
      this.assetForm.markAllAsTouched();
      this.errorMessage = 'Please fix the validation errors before submitting';
      return;
    }
    
    this.submitting = true;
    this.errorMessage = null;
    this.successMessage = null;
    
    const formValue = this.assetForm.getRawValue(); // getRawValue includes disabled fields
    const request: AssetRequest = this.buildAssetRequest(formValue);
    
    const operation = this.isEditMode && this.assetId
      ? this.assetService.updateAsset(this.assetId, request)
      : this.assetService.createAsset(request);
    
    operation
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.submitting = false)
      )
      .subscribe({
        next: (asset) => {
          this.successMessage = this.isEditMode 
            ? 'Asset updated successfully'
            : 'Asset created successfully';
          
          // Upload image if selected
          if (this.selectedImageFile) {
            this.uploadImageIfSelected(asset.id);
          }
          
          // Navigate to asset detail view after short delay
          setTimeout(() => {
            this.router.navigate(['/assets', asset.id]);
          }, 1500);
        },
        error: (error) => {
          this.errorMessage = error.message || 'Failed to save asset';
        }
      });
  }

  /**
   * Build AssetRequest from form values
   */
  private buildAssetRequest(formValue: any): AssetRequest {
    return {
      assetType: formValue.assetType,
      name: formValue.modelName || formValue.manufacturer || 'Unnamed Asset',
      serialNumber: formValue.serialNumber,
      acquisitionDate: formValue.purchaseDate,
      status: formValue.status,
      location: formValue.officeLocation || undefined,
      assignedUser: formValue.assignedUser || undefined,
      assignedUserEmail: undefined, // Would need additional field
      notes: formValue.notes || undefined
    };
  }

  /**
   * Handle cancel button click
   */
  onCancel(): void {
    if (this.isEditMode && this.assetId) {
      this.router.navigate(['/assets', this.assetId]);
    } else {
      this.router.navigate(['/assets']);
    }
  }

  /**
   * Get form control for template access
   */
  getControl(name: string): AbstractControl | null {
    return this.assetForm.get(name);
  }

  /**
   * Check if a field has an error
   */
  hasError(fieldName: string, errorType: string): boolean {
    const control = this.getControl(fieldName);
    return !!(control && control.hasError(errorType) && (control.dirty || control.touched));
  }

  /**
   * Get error message for a field
   */
  getErrorMessage(fieldName: string): string {
    const control = this.getControl(fieldName);
    
    if (!control || !control.errors) {
      return '';
    }
    
    if (control.hasError('required')) {
      return `${this.getFieldLabel(fieldName)} is required`;
    }
    
    if (control.hasError('minlength')) {
      const minLength = control.errors['minlength'].requiredLength;
      return `${this.getFieldLabel(fieldName)} must be at least ${minLength} characters`;
    }
    
    if (control.hasError('maxlength')) {
      const maxLength = control.errors['maxlength'].requiredLength;
      return `${this.getFieldLabel(fieldName)} must not exceed ${maxLength} characters`;
    }
    
    if (control.hasError('futureDate')) {
      return 'Date cannot be in the future';
    }
    
    if (control.hasError('invalidIpAddress')) {
      return 'Invalid IP address format';
    }
    
    if (control.hasError('min')) {
      return 'Value must be greater than or equal to 0';
    }
    
    return 'Invalid value';
  }

  /**
   * Get human-readable field label
   */
  private getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      assetType: 'Asset Type',
      manufacturer: 'Manufacturer',
      modelName: 'Model Name',
      serialNumber: 'Serial Number',
      purchaseDate: 'Purchase Date',
      warrantyExpiry: 'Warranty Expiry',
      costCenter: 'Cost Center',
      purchaseValue: 'Purchase Value',
      status: 'Status',
      assignedUser: 'Assigned User',
      officeLocation: 'Office Location',
      ipAddress: 'IP Address',
      notes: 'Notes'
    };
    
    return labels[fieldName] || fieldName;
  }

  /**
   * Format asset type for display
   */
  formatAssetType(type: AssetType): string {
    return type.replace(/_/g, ' ').toLowerCase()
      .split(' ')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  /**
   * Format lifecycle status for display
   */
  formatLifecycleStatus(status: LifecycleStatus): string {
    return status.replace(/_/g, ' ').toLowerCase()
      .split(' ')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }
  
  /**
   * Handle image file selection
   */
  onImageSelected(file: File): void {
    this.selectedImageFile = file;
    this.imageUploadError = null;
  }
  
  /**
   * Handle image file removal
   */
  onImageRemoved(): void {
    this.selectedImageFile = null;
    this.imageUploadError = null;
  }
  
  /**
   * Handle image validation error
   */
  onImageValidationError(error: string): void {
    this.imageUploadError = error;
    this.selectedImageFile = null;
  }
  
  /**
   * Handle use default placeholder
   */
  onUseDefaultPlaceholder(): void {
    this.selectedImageFile = null;
    this.imageUploadError = null;
    // The placeholder will be shown automatically by the service
  }
  
  /**
   * Upload image after asset is created/updated
   */
  private uploadImageIfSelected(assetId: string): void {
    if (!this.selectedImageFile) {
      return;
    }
    
    this.uploadingImage = true;
    
    this.assetService.uploadAssetImage(assetId, this.selectedImageFile)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.uploadingImage = false)
      )
      .subscribe({
        next: () => {
          // Image uploaded successfully
          this.selectedImageFile = null;
        },
        error: (error) => {
          this.imageUploadError = error.message || 'Failed to upload image';
        }
      });
  }
}
