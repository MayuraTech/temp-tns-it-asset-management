import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { AssetStatusBadgeComponent } from './asset-status-badge.component';
import { LifecycleStatus } from '../../../models';

describe('AssetStatusBadgeComponent', () => {
  let component: AssetStatusBadgeComponent;
  let fixture: ComponentFixture<AssetStatusBadgeComponent>;
  let debugElement: DebugElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AssetStatusBadgeComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(AssetStatusBadgeComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Status Display', () => {
    it('should display correct label for ORDERED status', () => {
      component.status = LifecycleStatus.ORDERED;
      fixture.detectChanges();

      const badge = debugElement.query(By.css('.status-badge'));
      expect(badge.nativeElement.textContent.trim()).toBe('Ordered');
    });

    it('should display correct label for IN_USE status', () => {
      component.status = LifecycleStatus.IN_USE;
      fixture.detectChanges();

      const badge = debugElement.query(By.css('.status-badge'));
      expect(badge.nativeElement.textContent.trim()).toBe('In Use');
    });

    it('should display correct label for RETIRED status', () => {
      component.status = LifecycleStatus.RETIRED;
      fixture.detectChanges();

      const badge = debugElement.query(By.css('.status-badge'));
      expect(badge.nativeElement.textContent.trim()).toBe('Retired');
    });
  });

  describe('CSS Classes', () => {
    it('should apply correct CSS class for ORDERED status', () => {
      component.status = LifecycleStatus.ORDERED;
      fixture.detectChanges();

      const badge = debugElement.query(By.css('.status-badge'));
      expect(badge.nativeElement).toHaveClass('status-ordered');
    });

    it('should apply correct CSS class for IN_USE status', () => {
      component.status = LifecycleStatus.IN_USE;
      fixture.detectChanges();

      const badge = debugElement.query(By.css('.status-badge'));
      expect(badge.nativeElement).toHaveClass('status-in-use');
    });

    it('should apply correct CSS class for MAINTENANCE status', () => {
      component.status = LifecycleStatus.MAINTENANCE;
      fixture.detectChanges();

      const badge = debugElement.query(By.css('.status-badge'));
      expect(badge.nativeElement).toHaveClass('status-maintenance');
    });

    it('should apply correct CSS class for RETIRED status', () => {
      component.status = LifecycleStatus.RETIRED;
      fixture.detectChanges();

      const badge = debugElement.query(By.css('.status-badge'));
      expect(badge.nativeElement).toHaveClass('status-retired');
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA label', () => {
      component.status = LifecycleStatus.DEPLOYED;
      fixture.detectChanges();

      const badge = debugElement.query(By.css('.status-badge'));
      expect(badge.nativeElement.getAttribute('aria-label')).toBe('Deployed');
    });

    it('should have role="status"', () => {
      component.status = LifecycleStatus.STORAGE;
      fixture.detectChanges();

      const badge = debugElement.query(By.css('.status-badge'));
      expect(badge.nativeElement.getAttribute('role')).toBe('status');
    });
  });

  describe('Helper Methods', () => {
    it('should return correct status class', () => {
      expect(component.getStatusClass(LifecycleStatus.ORDERED)).toBe('status-ordered');
      expect(component.getStatusClass(LifecycleStatus.RECEIVED)).toBe('status-received');
      expect(component.getStatusClass(LifecycleStatus.DEPLOYED)).toBe('status-deployed');
      expect(component.getStatusClass(LifecycleStatus.IN_USE)).toBe('status-in-use');
      expect(component.getStatusClass(LifecycleStatus.MAINTENANCE)).toBe('status-maintenance');
      expect(component.getStatusClass(LifecycleStatus.STORAGE)).toBe('status-storage');
      expect(component.getStatusClass(LifecycleStatus.RETIRED)).toBe('status-retired');
    });

    it('should return correct status label', () => {
      expect(component.getStatusLabel(LifecycleStatus.ORDERED)).toBe('Ordered');
      expect(component.getStatusLabel(LifecycleStatus.RECEIVED)).toBe('Received');
      expect(component.getStatusLabel(LifecycleStatus.DEPLOYED)).toBe('Deployed');
      expect(component.getStatusLabel(LifecycleStatus.IN_USE)).toBe('In Use');
      expect(component.getStatusLabel(LifecycleStatus.MAINTENANCE)).toBe('Maintenance');
      expect(component.getStatusLabel(LifecycleStatus.STORAGE)).toBe('Storage');
      expect(component.getStatusLabel(LifecycleStatus.RETIRED)).toBe('Retired');
    });

    it('should handle unknown status gracefully', () => {
      const unknownStatus = 'UNKNOWN' as LifecycleStatus;
      expect(component.getStatusClass(unknownStatus)).toBe('status-ordered');
      expect(component.getStatusLabel(unknownStatus)).toBe('Unknown');
    });
  });

  describe('Component Structure', () => {
    it('should render status badge element', () => {
      component.status = LifecycleStatus.ORDERED;
      fixture.detectChanges();

      const badge = debugElement.query(By.css('.status-badge'));
      expect(badge).toBeTruthy();
    });

    it('should have base status-badge class', () => {
      component.status = LifecycleStatus.ORDERED;
      fixture.detectChanges();

      const badge = debugElement.query(By.css('.status-badge'));
      expect(badge.nativeElement).toHaveClass('status-badge');
    });
  });
});