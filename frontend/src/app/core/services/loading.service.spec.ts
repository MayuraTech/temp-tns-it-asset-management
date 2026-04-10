import { TestBed } from '@angular/core/testing';
import { LoadingService } from './loading.service';

describe('LoadingService', () => {
  let service: LoadingService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LoadingService]
    });
    service = TestBed.inject(LoadingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('show', () => {
    it('should set loading to true on first show', (done) => {
      service.loading$.subscribe(loading => {
        if (loading) {
          expect(loading).toBe(true);
          expect(service.isLoading).toBe(true);
          done();
        }
      });

      service.show();
    });

    it('should increment request count', () => {
      service.show();
      expect(service['requestCount']).toBe(1);
      
      service.show();
      expect(service['requestCount']).toBe(2);
    });

    it('should only emit true once for multiple shows', () => {
      let emissionCount = 0;
      
      service.loading$.subscribe(loading => {
        if (loading) {
          emissionCount++;
        }
      });

      service.show();
      service.show();
      service.show();

      expect(emissionCount).toBe(1);
    });
  });

  describe('hide', () => {
    it('should set loading to false when all requests complete', (done) => {
      service.show();
      service.show();

      service.hide();
      expect(service.isLoading).toBe(true);

      service.loading$.subscribe(loading => {
        if (!loading) {
          expect(loading).toBe(false);
          done();
        }
      });

      service.hide();
    });

    it('should decrement request count', () => {
      service.show();
      service.show();
      expect(service['requestCount']).toBe(2);

      service.hide();
      expect(service['requestCount']).toBe(1);

      service.hide();
      expect(service['requestCount']).toBe(0);
    });

    it('should not go below zero request count', () => {
      service.hide();
      service.hide();
      
      expect(service['requestCount']).toBe(0);
      expect(service.isLoading).toBe(false);
    });
  });

  describe('reset', () => {
    it('should reset loading state and request count', (done) => {
      service.show();
      service.show();
      service.show();

      service.loading$.subscribe(loading => {
        if (!loading && service['requestCount'] === 0) {
          expect(service.isLoading).toBe(false);
          expect(service['requestCount']).toBe(0);
          done();
        }
      });

      service.reset();
    });
  });

  describe('isLoading', () => {
    it('should return current loading state', () => {
      expect(service.isLoading).toBe(false);

      service.show();
      expect(service.isLoading).toBe(true);

      service.hide();
      expect(service.isLoading).toBe(false);
    });
  });
});
