import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AssetsModule } from './assets.module';
import { AssetService } from './services/asset.service';

/**
 * Assets Module Unit Tests
 * 
 * Tests the module configuration and initialization.
 */
describe('AssetsModule', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        AssetsModule,
        RouterTestingModule,
        HttpClientTestingModule
      ]
    });
  });

  it('should create the module', () => {
    const module = TestBed.inject(AssetsModule);
    expect(module).toBeTruthy();
  });

  it('should provide AssetService', () => {
    const service = TestBed.inject(AssetService);
    expect(service).toBeTruthy();
  });

  it('should have correct module configuration', () => {
    const module = TestBed.inject(AssetsModule);
    expect(module).toBeInstanceOf(AssetsModule);
  });
});
