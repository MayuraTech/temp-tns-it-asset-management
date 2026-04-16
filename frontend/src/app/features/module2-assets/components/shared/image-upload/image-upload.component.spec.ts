import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ImageUploadComponent } from './image-upload.component';

describe('ImageUploadComponent', () => {
  let component: ImageUploadComponent;
  let fixture: ComponentFixture<ImageUploadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImageUploadComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(ImageUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit validation error for invalid file type', () => {
    spyOn(component.validationError, 'emit');
    
    const invalidFile = new File(['test'], 'test.txt', { type: 'text/plain' });
    component['handleFile'](invalidFile);
    
    expect(component.validationError.emit).toHaveBeenCalledWith(
      'Invalid file format. Please select a JPG, PNG, or WebP image.'
    );
  });

  it('should emit validation error for oversized file', () => {
    spyOn(component.validationError, 'emit');
    
    // Create a mock file that exceeds the size limit
    const oversizedFile = new File(['x'.repeat(6 * 1024 * 1024)], 'large.jpg', { 
      type: 'image/jpeg' 
    });
    
    component['handleFile'](oversizedFile);
    
    expect(component.validationError.emit).toHaveBeenCalledWith(
      'File size exceeds 5MB limit.'
    );
  });

  it('should emit file selected for valid file', () => {
    spyOn(component.fileSelected, 'emit');
    
    const validFile = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
    Object.defineProperty(validFile, 'size', { value: 1024 * 1024 }); // 1MB
    
    component['handleFile'](validFile);
    
    expect(component.fileSelected.emit).toHaveBeenCalledWith(validFile);
    expect(component.selectedFile).toBe(validFile);
  });

  it('should format file size correctly', () => {
    expect(component.getFormattedFileSize(0)).toBe('0 Bytes');
    expect(component.getFormattedFileSize(1024)).toBe('1 KB');
    expect(component.getFormattedFileSize(1024 * 1024)).toBe('1 MB');
    expect(component.getFormattedFileSize(1536)).toBe('1.5 KB');
  });

  it('should return accepted extensions string', () => {
    component.acceptedFormats = ['image/jpeg', 'image/png', 'image/webp'];
    expect(component.acceptedExtensions).toBe('JPEG, PNG, WEBP');
  });

  it('should remove file and emit event', () => {
    spyOn(component.fileRemoved, 'emit');
    
    component.selectedFile = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
    component.previewUrl = 'data:image/jpeg;base64,test';
    
    component.removeFile();
    
    expect(component.selectedFile).toBeUndefined();
    expect(component.previewUrl).toBeUndefined();
    expect(component.fileRemoved.emit).toHaveBeenCalled();
  });

  it('should handle drag over event', () => {
    const event = new DragEvent('dragover');
    spyOn(event, 'preventDefault');
    spyOn(event, 'stopPropagation');
    
    component.onDragOver(event);
    
    expect(event.preventDefault).toHaveBeenCalled();
    expect(event.stopPropagation).toHaveBeenCalled();
    expect(component.isDragOver).toBe(true);
  });

  it('should not set drag over when disabled', () => {
    component.disabled = true;
    const event = new DragEvent('dragover');
    
    component.onDragOver(event);
    
    expect(component.isDragOver).toBe(false);
  });

  it('should handle drag leave event', () => {
    component.isDragOver = true;
    const event = new DragEvent('dragleave');
    
    component.onDragLeave(event);
    
    expect(component.isDragOver).toBe(false);
  });

  it('should emit useDefaultPlaceholder when useDefault is called', () => {
    spyOn(component.useDefaultPlaceholder, 'emit');
    spyOn(component.fileRemoved, 'emit');
    
    component.selectedFile = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
    component.useDefault();
    
    expect(component.selectedFile).toBeUndefined();
    expect(component.fileRemoved.emit).toHaveBeenCalled();
    expect(component.useDefaultPlaceholder.emit).toHaveBeenCalled();
  });

  it('should accept JPG format', () => {
    spyOn(component.fileSelected, 'emit');
    
    const jpgFile = new File(['test'], 'test.jpg', { type: 'image/jpg' });
    Object.defineProperty(jpgFile, 'size', { value: 1024 * 1024 });
    
    component['handleFile'](jpgFile);
    
    expect(component.fileSelected.emit).toHaveBeenCalledWith(jpgFile);
  });

  it('should accept PNG format', () => {
    spyOn(component.fileSelected, 'emit');
    
    const pngFile = new File(['test'], 'test.png', { type: 'image/png' });
    Object.defineProperty(pngFile, 'size', { value: 1024 * 1024 });
    
    component['handleFile'](pngFile);
    
    expect(component.fileSelected.emit).toHaveBeenCalledWith(pngFile);
  });

  it('should accept WebP format', () => {
    spyOn(component.fileSelected, 'emit');
    
    const webpFile = new File(['test'], 'test.webp', { type: 'image/webp' });
    Object.defineProperty(webpFile, 'size', { value: 1024 * 1024 });
    
    component['handleFile'](webpFile);
    
    expect(component.fileSelected.emit).toHaveBeenCalledWith(webpFile);
  });

  it('should validate maximum file size of 5MB', () => {
    spyOn(component.validationError, 'emit');
    
    const maxSizeFile = new File(['x'.repeat(5 * 1024 * 1024 + 1)], 'large.jpg', { 
      type: 'image/jpeg' 
    });
    
    component['handleFile'](maxSizeFile);
    
    expect(component.validationError.emit).toHaveBeenCalledWith(
      'File size exceeds 5MB limit.'
    );
  });

  it('should not open file dialog when disabled', () => {
    component.disabled = true;
    spyOn(component.fileInput.nativeElement, 'click');
    
    component.openFileDialog();
    
    expect(component.fileInput.nativeElement.click).not.toHaveBeenCalled();
  });

  it('should open file dialog when not disabled', () => {
    component.disabled = false;
    spyOn(component.fileInput.nativeElement, 'click');
    
    component.openFileDialog();
    
    expect(component.fileInput.nativeElement.click).toHaveBeenCalled();
  });
});