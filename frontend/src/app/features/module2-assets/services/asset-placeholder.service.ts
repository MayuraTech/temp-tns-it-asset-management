import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { AssetType } from '../models/asset-type.enum';

/**
 * Service for managing asset placeholder images.
 * 
 * Provides default placeholder images for each asset type when no custom image is uploaded.
 * Placeholder images follow the Editorial Geometry design system.
 */
@Injectable({
  providedIn: 'root'
})
export class AssetPlaceholderService {
  private readonly placeholderBasePath = '/assets/images/placeholders';
  
  /**
   * Mapping of asset types to their placeholder image filenames.
   */
  private readonly placeholderMap: Map<AssetType, string> = new Map([
    [AssetType.SERVER, 'server.svg'],
    [AssetType.WORKSTATION, 'workstation.svg'],
    [AssetType.NETWORK_DEVICE, 'network_device.svg'],
    [AssetType.STORAGE_DEVICE, 'storage_device.svg'],
    [AssetType.SOFTWARE_LICENSE, 'software_license.svg'],
    [AssetType.PERIPHERAL, 'peripheral.svg'],
    [AssetType.KEYBOARD, 'keyboard.svg'],
    [AssetType.MOUSE, 'mouse.svg'],
    [AssetType.LAPTOP, 'laptop.svg'],
    [AssetType.MONITOR, 'monitor.svg'],
    [AssetType.HEADSET, 'headset.svg'],
    [AssetType.LAPTOP_CHARGER, 'laptop_charger.svg'],
    [AssetType.HDMI_CABLE, 'hdmi_cable.svg'],
    [AssetType.NETWORK_CABLE, 'network_cable.svg'],
    [AssetType.ACCESS_CARD, 'access_card.svg']
  ]);
  
  /**
   * Get the placeholder image URL for a given asset type.
   * 
   * @param assetType - The type of asset
   * @returns The URL path to the placeholder image
   */
  getPlaceholderUrl(assetType: AssetType): string {
    const filename = this.placeholderMap.get(assetType) || 'default.svg';
    return `${this.placeholderBasePath}/${filename}`;
  }
  
  /**
   * Get the default placeholder image URL (used when asset type is unknown).
   * 
   * @returns The URL path to the default placeholder image
   */
  getDefaultPlaceholderUrl(): string {
    return `${this.placeholderBasePath}/default.svg`;
  }
  
  /**
   * Get the image URL for an asset, using placeholder if no custom image exists.
   * 
   * @param imageUrl - The custom image URL (if any)
   * @param assetType - The type of asset
   * @returns The URL to display (custom image or placeholder)
   */
  getAssetImageUrl(imageUrl: string | undefined | null, assetType: AssetType): string {
    if (imageUrl) {
      return this.resolveImageUrl(imageUrl);
    }
    return this.getPlaceholderUrl(assetType);
  }

  /**
   * Turn backend-relative paths (e.g. /api/v1/assets/{id}/image) into absolute URLs
   * so <img src> loads from the API host instead of the dev server.
   */
  private resolveImageUrl(url: string): string {
    const trimmed = url.trim();
    if (!trimmed.startsWith('/') || trimmed.startsWith('//')) {
      return trimmed;
    }
    const origin = environment.apiUrl.replace(/\/api\/v1\/?$/, '');
    return `${origin}${trimmed}`;
  }
  
  /**
   * Check if an image URL is a placeholder.
   * 
   * @param imageUrl - The image URL to check
   * @returns True if the URL is a placeholder image
   */
  isPlaceholder(imageUrl: string): boolean {
    return imageUrl.startsWith(this.placeholderBasePath);
  }
}
