import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AssetService } from './asset.service';
import { 
  Asset, 
  AssetRequest, 
  AssetSearchQuery, 
  AssetHistoryEvent, 
  AssetHistoryEventType,
  AssetHistoryQuery,
  AssignmentHistoryEntry,
  AssignmentHistoryQuery,
  AssignmentHistorySummary,
  AssignmentStatus,
  Page 
} from '../models';
import { AssetType } from '../models/asset-type.enum';
import { LifecycleStatus } from '../models/lifecycle-status.enum';
import { environment } from '../../../../environments/environment';

describe('AssetService', () => {
  let service: AssetService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/assets`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AssetService]
    });

    service = TestBed.inject(AssetService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('Asset History Services', () => {
    const mockAssetId = '550e8400-e29b-41d4-a716-446655440000';

    describe('getAssetHistory', () => {
      it('should retrieve asset history without query parameters', () => {
        const mockHistoryEvents: AssetHistoryEvent[] = [
          {
            id: 'event-1',
            timestamp: '2024-01-15T10:30:00Z',
            eventType: AssetHistoryEventType.CREATED,
            userId: 'user-123',
            userName: 'John Doe',
            description: 'Asset created',
            fieldChanges: []
          },
          {
            id: 'event-2',
            timestamp: '2024-01-16T14:20:00Z',
            eventType: AssetHistoryEventType.STATUS_CHANGED,
            userId: 'user-456',
            userName: 'Jane Smith',
            description: 'Status changed from ORDERED to RECEIVED',
            fieldChanges: [
              {
                fieldName: 'status',
                oldValue: 'ORDERED',
                newValue: 'RECEIVED',
                displayName: 'Status'
              }
            ]
          }
        ];

        const mockPage: Page<AssetHistoryEvent> = {
          content: mockHistoryEvents,
          page: {
            size: 20,
            number: 0,
            totalElements: 2,
            totalPages: 1
          }
        };

        service.getAssetHistory(mockAssetId).subscribe(result => {
          expect(result).toEqual(mockPage);
          expect(result.content.length).toBe(2);
          expect(result.content[0].eventType).toBe(AssetHistoryEventType.CREATED);
          expect(result.content[1].eventType).toBe(AssetHistoryEventType.STATUS_CHANGED);
        });

        const req = httpMock.expectOne(`${apiUrl}/${mockAssetId}/history`);
        expect(req.request.method).toBe('GET');
        expect(req.request.params.keys().length).toBe(0);
        req.flush(mockPage);
      });

      it('should retrieve asset history with query parameters', () => {
        const query: AssetHistoryQuery = {
          eventType: AssetHistoryEventType.STATUS_CHANGED,
          dateFrom: '2024-01-01',
          dateTo: '2024-01-31',
          page: 1,
          size: 10
        };

        const mockPage: Page<AssetHistoryEvent> = {
          content: [],
          page: {
            size: 10,
            number: 1,
            totalElements: 0,
            totalPages: 0
          }
        };

        service.getAssetHistory(mockAssetId, query).subscribe(result => {
          expect(result).toEqual(mockPage);
        });

        const req = httpMock.expectOne(request => 
          request.url === `${apiUrl}/${mockAssetId}/history` &&
          request.params.get('eventType') === AssetHistoryEventType.STATUS_CHANGED &&
          request.params.get('dateFrom') === '2024-01-01' &&
          request.params.get('dateTo') === '2024-01-31' &&
          request.params.get('page') === '1' &&
          request.params.get('size') === '10'
        );
        expect(req.request.method).toBe('GET');
        req.flush(mockPage);
      });

      it('should handle error when asset history retrieval fails', () => {
        service.getAssetHistory(mockAssetId).subscribe({
          next: () => fail('should have failed'),
          error: (error) => {
            expect(error.message).toContain('The requested resource was not found');
          }
        });

        const req = httpMock.expectOne(`${apiUrl}/${mockAssetId}/history`);
        req.flush('Asset not found', { status: 404, statusText: 'Not Found' });
      });
    });

    describe('getAssignmentHistory', () => {
      it('should retrieve assignment history without query parameters', () => {
        const mockAssignmentHistory: AssignmentHistoryEntry[] = [
          {
            id: 'assignment-1',
            assignedUser: 'John Doe',
            assignedUserEmail: 'john.doe@example.com',
            assignmentDate: '2024-01-15T10:30:00Z',
            unassignmentDate: '2024-01-20T16:45:00Z',
            duration: '5 days',
            durationInDays: 5,
            status: AssignmentStatus.PAST,
            location: 'Office A'
          },
          {
            id: 'assignment-2',
            assignedUser: 'Jane Smith',
            assignedUserEmail: 'jane.smith@example.com',
            assignmentDate: '2024-01-21T09:00:00Z',
            duration: '10 days',
            durationInDays: 10,
            status: AssignmentStatus.CURRENT,
            location: 'Office B'
          }
        ];

        const mockPage: Page<AssignmentHistoryEntry> = {
          content: mockAssignmentHistory,
          page: {
            size: 20,
            number: 0,
            totalElements: 2,
            totalPages: 1
          }
        };

        service.getAssignmentHistory(mockAssetId).subscribe(result => {
          expect(result).toEqual(mockPage);
          expect(result.content.length).toBe(2);
          expect(result.content[0].status).toBe(AssignmentStatus.PAST);
          expect(result.content[1].status).toBe(AssignmentStatus.CURRENT);
        });

        const req = httpMock.expectOne(`${apiUrl}/${mockAssetId}/assignments`);
        expect(req.request.method).toBe('GET');
        expect(req.request.params.keys().length).toBe(0);
        req.flush(mockPage);
      });

      it('should retrieve assignment history with query parameters', () => {
        const query: AssignmentHistoryQuery = {
          status: AssignmentStatus.CURRENT,
          dateFrom: '2024-01-01',
          dateTo: '2024-01-31',
          assignedUser: 'Jane Smith',
          page: 0,
          size: 5
        };

        const mockPage: Page<AssignmentHistoryEntry> = {
          content: [],
          page: {
            size: 5,
            number: 0,
            totalElements: 0,
            totalPages: 0
          }
        };

        service.getAssignmentHistory(mockAssetId, query).subscribe(result => {
          expect(result).toEqual(mockPage);
        });

        const req = httpMock.expectOne(request => 
          request.url === `${apiUrl}/${mockAssetId}/assignments` &&
          request.params.get('status') === AssignmentStatus.CURRENT &&
          request.params.get('dateFrom') === '2024-01-01' &&
          request.params.get('dateTo') === '2024-01-31' &&
          request.params.get('assignedUser') === 'Jane Smith' &&
          request.params.get('page') === '0' &&
          request.params.get('size') === '5'
        );
        expect(req.request.method).toBe('GET');
        req.flush(mockPage);
      });

      it('should handle error when assignment history retrieval fails', () => {
        service.getAssignmentHistory(mockAssetId).subscribe({
          next: () => fail('should have failed'),
          error: (error) => {
            expect(error.message).toContain('You do not have permission');
          }
        });

        const req = httpMock.expectOne(`${apiUrl}/${mockAssetId}/assignments`);
        req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });
      });
    });

    describe('getAssignmentHistorySummary', () => {
      it('should retrieve assignment history summary', () => {
        const mockSummary: AssignmentHistorySummary = {
          totalAssignments: 3,
          currentAssignment: {
            id: 'assignment-current',
            assignedUser: 'Jane Smith',
            assignedUserEmail: 'jane.smith@example.com',
            assignmentDate: '2024-01-21T09:00:00Z',
            duration: '10 days',
            durationInDays: 10,
            status: AssignmentStatus.CURRENT,
            location: 'Office B'
          },
          averageDurationDays: 7.5,
          longestDurationDays: 15,
          totalAssignedDays: 30,
          mostFrequentAssignee: 'John Doe'
        };

        service.getAssignmentHistorySummary(mockAssetId).subscribe(result => {
          expect(result).toEqual(mockSummary);
          expect(result.totalAssignments).toBe(3);
          expect(result.currentAssignment?.status).toBe(AssignmentStatus.CURRENT);
          expect(result.mostFrequentAssignee).toBe('John Doe');
        });

        const req = httpMock.expectOne(`${apiUrl}/${mockAssetId}/assignments/summary`);
        expect(req.request.method).toBe('GET');
        req.flush(mockSummary);
      });

      it('should handle error when assignment summary retrieval fails', () => {
        service.getAssignmentHistorySummary(mockAssetId).subscribe({
          next: () => fail('should have failed'),
          error: (error) => {
            expect(error.message).toContain('Internal server error');
          }
        });

        const req = httpMock.expectOne(`${apiUrl}/${mockAssetId}/assignments/summary`);
        req.flush('Server Error', { status: 500, statusText: 'Internal Server Error' });
      });
    });
  });

  describe('Existing Asset Services', () => {
    describe('getAssets', () => {
      it('should retrieve assets from API', () => {
        const mockAssets: Asset[] = [
          {
            id: '1',
            name: 'Asset 1',
            assetType: AssetType.SERVER,
            serialNumber: 'SRV-001',
            acquisitionDate: '2024-01-15',
            status: LifecycleStatus.IN_USE,
            createdAt: '2024-01-15T10:30:00Z',
            createdBy: 'user-123',
            updatedAt: '2024-01-15T10:30:00Z',
            updatedBy: 'user-123',
            readOnly: false
          },
          {
            id: '2',
            name: 'Asset 2',
            assetType: AssetType.WORKSTATION,
            serialNumber: 'WS-001',
            acquisitionDate: '2024-01-16',
            status: LifecycleStatus.DEPLOYED,
            createdAt: '2024-01-16T11:00:00Z',
            createdBy: 'user-456',
            updatedAt: '2024-01-16T11:00:00Z',
            updatedBy: 'user-456',
            readOnly: false
          }
        ];

        const mockPage: Page<Asset> = {
          content: mockAssets,
          page: {
            size: 20,
            number: 0,
            totalElements: 2,
            totalPages: 1
          }
        };

        service.getAssets().subscribe(result => {
          expect(result).toEqual(mockPage);
          expect(result.content.length).toBe(2);
        });

        const req = httpMock.expectOne(request => 
          request.url === apiUrl &&
          request.params.get('page') === '0' &&
          request.params.get('size') === '20'
        );
        expect(req.request.method).toBe('GET');
        req.flush(mockPage);
      });

      it('should handle error responses', () => {
        service.getAssets().subscribe({
          next: () => fail('should have failed'),
          error: (error) => {
            expect(error.message).toContain('Internal server error');
          }
        });

        const req = httpMock.expectOne(request => request.url === apiUrl);
        req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });
      });
    });

    describe('createAsset', () => {
      it('should create asset via POST request', () => {
        const request: AssetRequest = {
          assetType: AssetType.SERVER,
          name: 'New Server',
          serialNumber: 'SRV-001',
          acquisitionDate: '2024-01-15',
          status: LifecycleStatus.ORDERED
        };

        const mockResponse: Asset = {
          id: '123',
          ...request,
          createdAt: '2024-01-15T10:30:00Z',
          createdBy: 'user-123',
          updatedAt: '2024-01-15T10:30:00Z',
          updatedBy: 'user-123',
          readOnly: false
        };

        service.createAsset(request).subscribe(asset => {
          expect(asset.id).toBe('123');
          expect(asset.name).toBe('New Server');
        });

        const req = httpMock.expectOne(apiUrl);
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual(request);
        req.flush(mockResponse);
      });
    });
  });

  describe('Query Parameter Building', () => {
    it('should build history query parameters correctly', () => {
      const query: AssetHistoryQuery = {
        eventType: AssetHistoryEventType.UPDATED,
        dateFrom: '2024-01-01',
        dateTo: '2024-01-31',
        page: 2,
        size: 15
      };

      service.getAssetHistory('test-id', query).subscribe();

      const req = httpMock.expectOne(request => {
        const params = request.params;
        return params.get('eventType') === AssetHistoryEventType.UPDATED &&
               params.get('dateFrom') === '2024-01-01' &&
               params.get('dateTo') === '2024-01-31' &&
               params.get('page') === '2' &&
               params.get('size') === '15';
      });
      req.flush({ content: [], page: { size: 15, number: 2, totalElements: 0, totalPages: 0 } });
    });

    it('should build assignment history query parameters correctly', () => {
      const query: AssignmentHistoryQuery = {
        status: AssignmentStatus.PAST,
        dateFrom: '2024-01-01',
        assignedUser: 'John Doe',
        page: 1,
        size: 10
      };

      service.getAssignmentHistory('test-id', query).subscribe();

      const req = httpMock.expectOne(request => {
        const params = request.params;
        return params.get('status') === AssignmentStatus.PAST &&
               params.get('dateFrom') === '2024-01-01' &&
               params.get('assignedUser') === 'John Doe' &&
               params.get('page') === '1' &&
               params.get('size') === '10';
      });
      req.flush({ content: [], page: { size: 10, number: 1, totalElements: 0, totalPages: 0 } });
    });
  });
});