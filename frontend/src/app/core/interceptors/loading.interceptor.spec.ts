import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { loadingInterceptor } from './loading.interceptor';
import { LoadingService } from '../services/loading.service';

describe('LoadingInterceptor', () => {
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;
  let loadingService: LoadingService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([loadingInterceptor])),
        provideHttpClientTesting(),
        LoadingService
      ]
    });

    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
    loadingService = TestBed.inject(LoadingService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should show loading on request start and hide on complete', (done) => {
    let showCalled = false;
    let hideCalled = false;

    spyOn(loadingService, 'show').and.callFake(() => {
      showCalled = true;
    });

    spyOn(loadingService, 'hide').and.callFake(() => {
      hideCalled = true;
      expect(showCalled).toBe(true);
      done();
    });

    httpClient.get('/api/v1/assets').subscribe();

    const req = httpMock.expectOne('/api/v1/assets');
    req.flush({});
  });

  it('should hide loading on request error', (done) => {
    spyOn(loadingService, 'show');
    spyOn(loadingService, 'hide').and.callFake(() => {
      done();
    });

    httpClient.get('/api/v1/assets').subscribe({
      error: () => {}
    });

    const req = httpMock.expectOne('/api/v1/assets');
    req.flush({}, { status: 500, statusText: 'Server Error' });
  });

  it('should skip loading when X-Skip-Loading header is present', () => {
    spyOn(loadingService, 'show');
    spyOn(loadingService, 'hide');

    httpClient.get('/api/v1/assets', {
      headers: { 'X-Skip-Loading': 'true' }
    }).subscribe();

    const req = httpMock.expectOne('/api/v1/assets');
    req.flush({});

    expect(loadingService.show).not.toHaveBeenCalled();
    expect(loadingService.hide).not.toHaveBeenCalled();
  });
});
