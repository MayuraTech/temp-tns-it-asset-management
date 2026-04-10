import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { MatIconModule } from '@angular/material/icon';
import { AssetIconComponent } from './asset-icon.component';
import { AssetType } from '../../../models';

describe('AssetIconComponent', () => {
  let component: AssetIconComponent;
  let fixture: ComponentFixture<AssetIconComponent>;
  let debugElement: DebugElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AssetIconComponent, MatIconModule]
    }).compileComponents();

    fixture = TestBed.createComponent(AssetIconComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Icon Display', () => {
    it('should display correct icon for SERVER asset type', () => {
      component.assetType = AssetType.SERVER;
      fixture.detectChanges();

      const icon = debugElement.query(By.css('mat-icon'));
      expect(icon.nativeElement.textContent.trim()).toBe('dns');
    });

    it('should display correct icon for LAPTOP asset type', () => {
      component.assetType = AssetType.LAPTOP;
      fixture.detectChanges();

      const icon = debugElement.query(By.css('mat-icon'));
      expect(icon.nativeElement.textContent.trim()).toBe('laptop');
    });

    it('should display correct icon for NETWORK_DEVICE asset type', () => {
      component.assetType = AssetType.NETWORK_DEVICE;
      fixture.detectChanges();

      const icon = debugElement.query(By.css('mat-icon'));
      expect(icon.nativeElement.textContent.trim()).toBe('router');
    });

    it('should display default icon for unknown asset type', () => {
      const unknownType = 'UNKNOWN' as AssetType;
      component.assetType = unknownType;
      fixture.detectChanges();

      const icon = debugElement.query(By.css('mat-icon'));
      expect(icon.nativeElement.textContent.trim()).toBe('inventory_2');
    });
  });

  describe('Size Configuration', () => {
    it('should apply small size class', () => {
      component.assetType = AssetType.SERVER;
      component.size = 'small';
      fixture.detectChanges();

      const icon = debugElement.query(By.css('mat-icon'));
      expect(icon.nativeElement).toHaveClass('size-small');
    });

    it('should apply medium size class by default', () => {
      component.assetType = AssetType.SERVER;
      fixture.detectChanges();

      const icon = debugElement.query(By.css('mat-icon'));
      expect(icon.nativeElement).toHaveClass('size-medium');
    });

    it('should apply large size class', () => {
      component.assetType = AssetType.SERVER;
      component.size = 'large';
      fixture.detectChanges();

      const icon = debugElement.query(By.css('mat-icon'));
      expect(icon.nativeElement).toHaveClass('size-large');
    });

    it('should apply xlarge size class', () => {
      component.assetType = AssetType.SERVER;
      component.size = 'xlarge';
      fixture.detectChanges();

      const icon = debugElement.query(By.css('mat-icon'));
      expect(icon.nativeElement).toHaveClass('size-xlarge');
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA label for SERVER', () => {
      component.assetType = AssetType.SERVER;
      fixture.detectChanges();

      const icon = debugElement.query(By.css('mat-icon'));
      expect(icon.nativeElement.getAttribute('aria-label')).toBe('Server icon');
    });

    it('should have proper ARIA label for LAPTOP', () => {
      component.assetType = AssetType.LAPTOP;
      fixture.detectChanges();

      const icon = debugElement.query(By.css('mat-icon'));
      expect(icon.nativeElement.getAttribute('aria-label')).toBe('Laptop icon');
    });

    it('should have role="img"', () => {
      component.assetType = AssetType.SERVER;
      fixture.detectChanges();

      const icon = debugElement.query(By.css('mat-icon'));
      expect(icon.nativeElement.getAttribute('role')).toBe('img');
    });
  });

  describe('Helper Methods', () => {
    it('should return correct icon names for all asset types', () => {
      expect(component.getIconName(AssetType.SERVER)).toBe('dns');
      expect(component.getIconName(AssetType.WORKSTATION)).toBe('computer');
      expect(component.getIconName(AssetType.NETWORK_DEVICE)).toBe('router');
      expect(component.getIconName(AssetType.STORAGE_DEVICE)).toBe('storage');
      expect(component.getIconName(AssetType.SOFTWARE_LICENSE)).toBe('key');
      expect(component.getIconName(AssetType.PERIPHERAL)).toBe('devices_other');
      expect(component.getIconName(AssetType.KEYBOARD)).toBe('keyboard');
      expect(component.getIconName(AssetType.MOUSE)).toBe('mouse');
      expect(component.getIconName(AssetType.LAPTOP)).toBe('laptop');
      expect(component.getIconName(AssetType.MONITOR)).toBe('monitor');
      expect(component.getIconName(AssetType.HEADSET)).toBe('headset');
      expect(component.getIconName(AssetType.LAPTOP_CHARGER)).toBe('power');
      expect(component.getIconName(AssetType.HDMI_CABLE)).toBe('cable');
      expect(component.getIconName(AssetType.NETWORK_CABLE)).toBe('settings_ethernet');
      expect(component.getIconName(AssetType.ACCESS_CARD)).toBe('badge');
    });

    it('should return correct size classes', () => {
      expect(component.getSizeClass('small')).toBe('size-small');
      expect(component.getSizeClass('medium')).toBe('size-medium');
      expect(component.getSizeClass('large')).toBe('size-large');
      expect(component.getSizeClass('xlarge')).toBe('size-xlarge');
    });

    it('should return correct ARIA labels for all asset types', () => {
      expect(component.getAriaLabel(AssetType.SERVER)).toBe('Server icon');
      expect(component.getAriaLabel(AssetType.WORKSTATION)).toBe('Workstation icon');
      expect(component.getAriaLabel(AssetType.NETWORK_DEVICE)).toBe('Network device icon');
      expect(component.getAriaLabel(AssetType.STORAGE_DEVICE)).toBe('Storage device icon');
      expect(component.getAriaLabel(AssetType.SOFTWARE_LICENSE)).toBe('Software license icon');
      expect(component.getAriaLabel(AssetType.PERIPHERAL)).toBe('Peripheral device icon');
      expect(component.getAriaLabel(AssetType.KEYBOARD)).toBe('Keyboard icon');
      expect(component.getAriaLabel(AssetType.MOUSE)).toBe('Mouse icon');
      expect(component.getAriaLabel(AssetType.LAPTOP)).toBe('Laptop icon');
      expect(component.getAriaLabel(AssetType.MONITOR)).toBe('Monitor icon');
      expect(component.getAriaLabel(AssetType.HEADSET)).toBe('Headset icon');
      expect(component.getAriaLabel(AssetType.LAPTOP_CHARGER)).toBe('Laptop charger icon');
      expect(component.getAriaLabel(AssetType.HDMI_CABLE)).toBe('HDMI cable icon');
      expect(component.getAriaLabel(AssetType.NETWORK_CABLE)).toBe('Network cable icon');
      expect(component.getAriaLabel(AssetType.ACCESS_CARD)).toBe('Access card icon');
    });

    it('should handle unknown asset type gracefully', () => {
      const unknownType = 'UNKNOWN' as AssetType;
      expect(component.getIconName(unknownType)).toBe('inventory_2');
      expect(component.getAriaLabel(unknownType)).toBe('Asset icon');
    });
  });

  describe('Component Structure', () => {
    it('should render mat-icon element', () => {
      component.assetType = AssetType.SERVER;
      fixture.detectChanges();

      const icon = debugElement.query(By.css('mat-icon'));
      expect(icon).toBeTruthy();
    });

    it('should have base asset-icon class', () => {
      component.assetType = AssetType.SERVER;
      fixture.detectChanges();

      const icon = debugElement.query(By.css('mat-icon'));
      expect(icon.nativeElement).toHaveClass('asset-icon');
    });
  });
});