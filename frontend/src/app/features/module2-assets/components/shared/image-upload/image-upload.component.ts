import { Component, EventEmitter, Input, Output, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Image upload component with drag-and-drop functionality.
 * Supports JPG, PNG, and WebP formats with 5MB size limit.
 * Provides image preview before upload.
 */
@Component({
  selector: 'app-image-upload',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './image-upload.component.html',
  styleUrls: ['./image-upload.component.scss']
})
export class ImageUploadComponent {
  @Input() currentImageUrl?: string;
  @Input() assetType?: string; // Asset type for placeholder selection
  @Input() disabled = false;
  @Input() maxSizeBytes = 5 * 1024 * 1024; // 5MB
  @Input() acceptedFormats = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
  
  @Output() fileSelected = new EventEmitter<File>();
  @Output() fileRemoved = new EventEmitter<void>();
  @Output() validationError = new EventEmitter<string>();
  @Output() useDefaultPlaceholder = new EventEmitter<void>();
  
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;
  
  isDragOver = false;
  previewUrl?: string;
  selectedFile?: File;
  
  /**
   * Handle file selection from input.
   */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.handleFile(input.files[0]);
    }
  }
  
  /**
   * Handle drag over event.
   */
  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    if (!this.disabled) {
      this.isDragOver = true;
    }
  }
  
  /**
   * Handle drag leave event.
   */
  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;
  }
  
  /**
   * Handle file drop event.
   */
  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;
    
    if (this.disabled) {
      return;
    }
    
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.handleFile(files[0]);
    }
  }
  
  /**
   * Open file selection dialog.
   */
  openFileDialog(): void {
    if (!this.disabled) {
      this.fileInput.nativeElement.click();
    }
  }
  
  /**
   * Remove selected file.
   */
  removeFile(): void {
    this.selectedFile = undefined;
    this.previewUrl = undefined;
    this.fileInput.nativeElement.value = '';
    this.fileRemoved.emit();
  }
  
  /**
   * Handle file validation and preview.
   */
  private handleFile(file: File): void {
    // Validate file type
    if (!this.acceptedFormats.includes(file.type)) {
      this.validationError.emit('Invalid file format. Please select a JPG, PNG, or WebP image.');
      return;
    }
    
    // Validate file size
    if (file.size > this.maxSizeBytes) {
      const maxSizeMB = this.maxSizeBytes / (1024 * 1024);
      this.validationError.emit(`File size exceeds ${maxSizeMB}MB limit.`);
      return;
    }
    
    // Set selected file
    this.selectedFile = file;
    
    // Create preview
    const reader = new FileReader();
    reader.onload = (e) => {
      this.previewUrl = e.target?.result as string;
    };
    reader.readAsDataURL(file);
    
    // Emit file selected event
    this.fileSelected.emit(file);
  }
  
  /**
   * Get formatted file size.
   */
  getFormattedFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
  
  /**
   * Use default placeholder image.
   */
  useDefault(): void {
    this.removeFile();
    this.useDefaultPlaceholder.emit();
  }
  
  /**
   * Get accepted file extensions for display.
   */
  get acceptedExtensions(): string {
    return this.acceptedFormats
      .map(format => format.split('/')[1].toUpperCase())
      .join(', ');
  }
}