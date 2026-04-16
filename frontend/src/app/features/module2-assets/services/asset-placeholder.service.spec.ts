import { TestBed } from '@angular/core/testing';
import { AssetPlaceholderService } from './asset-placeholder.service';
import { AssetType } from '../models/asset-type.enum';

describe('AssetPlaceholderService', () => {
  let service: AssetPlaceholderService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AssetPlaceholderService]
    });
    service = TestBed.inject(AssetPlaceholderService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getPlaceholderUrl', () => {
    it('should return server placeholder for SERVER type', () => {
      const url = service.getPlaceholderUrl(AssetType.SERVER);
      expect(url).toBe('/assets/images/placeholders/server.svg');
    });

    it('should return laptop placeholder for LAPTOP type', () => {
      const url = service.getPlaceholderUrl(AssetType.LAPTOP);
      expect(url).toBe('/assets/images/placeholders/laptop.svg');
    });

    it('should return monitor placeholder for MONITOR type', () => {
      const url = service.getPlaceholderUrl(AssetType.MONITOR);
      expect(url).toBe('/assets/images/placeholders/monitor.svg');
    });

    it('should return keyboard placeholder for KEYBOARD type', () => {
      const url = service.getPlaceholderUrl(AssetType.KEYBOARD);
      expect(url).toBe('/assets/images/placeholders/keyboard.svg');
    });

    it('should return workstation placeholder for WORKSTATION type', () => {
      const url = service.getPlaceholderUrl(AssetType.WORKSTATION);
      expect(url).toBe('/assets/images/placeholders/workstation.svg');
    });

    it('should return network device placeholder for NETWORK_DEVICE type', () => {
      const url = service.getPlaceholderUrl(AssetType.NETWORK_DEVICE);
      expect(url).toBe('/assets/images/placeholders/network_device.svg');
    });

    it('should return storage device placeholder for STORAGE_DEVICE type', () => {
      const url = service.getPlaceholderUrl(AssetType.STORAGE_DEVICE);
      expect(url).toBe('/assets/images/placeholders/storage_device.svg');
    });

    it('should return software license placeholder for SOFTWARE_LICENSE type', () => {
      const url = service.getPlaceholderUrl(AssetType.SOFTWARE_LICENSE);
      expect(url).toBe('/assets/images/placeholders/software_license.svg');
    });

    it('should return peripheral placeholder for PERIPHERAL type', () => {
      const url = service.getPlaceholderUrl(AssetType.PERIPHERAL);
      expect(url).toBe('/assets/images/placeholders/peripheral.svg');
    });

    it('should return mouse placeholder for MOUSE type', () => {
      const url = service.getPlaceholderUrl(AssetType.MOUSE);
      expect(url).toBe('/assets/images/placeholders/mouse.svg');
    });

    it('should return headset placeholder for HEADSET type', () => {
      const url = service.getPlaceholderUrl(AssetType.HEADSET);
      expect(url).toBe('/assets/images/placeholders/headset.svg');
    });

    it('should return laptop charger placeholder for LAPTOP_CHARGER type', () => {
      const url = service.getPlaceholderUrl(AssetType.LAPTOP_CHARGER);
      expect(url).toBe('/assets/images/placeholders/laptop_charger.svg');
    });

    it('should return HDMI cable placeholder for HDMI_CABLE type', () => {
      const url = service.getPlaceholderUrl(AssetType.HDMI_CABLE);
      expect(url).toBe('/assets/images/placeholders/hdmi_cable.svg');
    });

    it('should return network cable placeholder for NETWORK_CABLE type', () => {
      const url = service.getPlaceholderUrl(AssetType.NETWORK_CABLE);
      expect(url).toBe('/assets/images/placeholders/network_cable.svg');
    });

    it('should return access card placeholder for ACCESS_CARD type', () => {
      const url = service.getPlaceholderUrl(AssetType.ACCESS_CARD);
      expect(url).toBe('/assets/images/placeholders/access_card.svg');
    });
  });

  describe('getDefaultPlaceholderUrl', () => {
    it('should return default placeholder URL', () => {
      const url = service.getDefaultPlaceholderUrl();
      expect(url).toBe('/assets/images/placeholders/default.svg');
    });
  });

  describe('getAssetImageUrl', () => {
    it('should return custom image URL when provided', () => {
      const customUrl = 'https://example.com/image.jpg';
      const url = service.getAssetImageUrl(customUrl, AssetType.SERVER);
      expect(url).toBe(customUrl);
    });

    it('should return placeholder when image URL is null', () => {
      const url = service.getAssetImageUrl(null, AssetType.LAPTOP);
      expect(url).toBe('/assets/images/placeholders/laptop.svg');
    });

    it('should return placeholder when image URL is undefined', () => {
      const url = service.getAssetImageUrl(undefined, AssetType.MONITOR);
      expect(url).toBe('/assets/images/placeholders/monitor.svg');
    });

    it('should return placeholder when image URL is empty string', () => {
      const url = service.getAssetImageUrl('', AssetType.KEYBOARD);
      expect(url).toBe('/assets/images/placeholders/default.svg');
    });
  });

  describe('isPlaceholder', () => {
    it('should return true for placeholder URLs', () => {
      const placeholderUrl = '/assets/images/placeholders/server.svg';
      expect(service.isPlaceholder(placeholderUrl)).toBe(true);
    });

    it('should return false for custom image URLs', () => {
      const customUrl = 'https://example.com/image.jpg';
      expect(service.isPlaceholder(customUrl)).toBe(false);
    });

    it('should return false for relative custom URLs', () => {
      const customUrl = '/uploads/asset-images/image.jpg';
      expect(service.isPlaceholder(customUrl)).toBe(false);
    });
  });

  describe('placeholder coverage', () => {
    it('should have placeholders for all asset types', () => {
      const assetTypes = Object.values(AssetType);
      
      assetTypes.forEach(type => {
        const url = service.getPlaceholderUrl(type);
        expect(url).toContain('/assets/images/placeholders/');
        expect(url).toContain('.svg');
      });
    });
  });
});
