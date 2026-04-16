import 'zone.js';
import 'zone.js/testing';
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AllocationService } from './allocation.service';
import {
  Assignment,
  AssignmentRequest,
  AssignmentHistoryDTO,
  AssignmentStatistics,
  BulkDeallocationResult,
  ExportFilters,
  Asset,
  PageResponse,
  AssignmentType
} from '../../shared/models';
import { environment } from '../../../environments/environment';

describe('AllocationService', () => {
  let service: AllocationService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/assets`;
  const assignmentsUrl = `${environment.apiUrl}/assignments`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AllocationService]
    });

    service = TestBed.inject(AllocationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('assignToUser', () => {
    it('should assign asset to user via POST request', () => {
      const assetId = '123e4567-e89b-12d3-a456-426614174000';
      const request: AssignmentRequest = {
        assignmentType: AssignmentType.USER,
        assignedTo: 'John Doe',
        assignedUserEmail: 'john.doe@example.com'
      };

      const mockResponse: Assignment = {
        id: '550e8400-e29b-41d4-a716-446655440000',
        assetId: assetId,
        assignmentType: AssignmentType.USER,
        assignedTo: 'John Doe',
        assignedBy: 'admin',
        assignedAt: '2024-01-15T10:30:00Z'
      };

      service.assignToUser(assetId, request).subscribe(assignment => {
        expect(assignment).toEqual(mockResponse);
        expect(assignment.assignmentType).toBe(AssignmentType.USER);
      });

      const req = httpMock.expectOne(`${apiUrl}/${assetId}/assignments`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body.assignmentType).toBe('USER');
      expect(req.request.body.assignedTo).toBe('John Doe');
      expect(req.request.body.assignedUserEmail).toBe('john.doe@example.com');
      req.flush(mockResponse);
    });

    it('should handle error when assigning to user fails', () => {
      const assetId = '123e4567-e89b-12d3-a456-426614174000';
      const request: AssignmentRequest = {
        assignmentType: AssignmentType.USER,
        assignedTo: 'John Doe',
        assignedUserEmail: 'john.doe@example.com'
      };

      service.assignToUser(assetId, request).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.message).toContain('Asset already assigned');
        }
      });

      const req = httpMock.expectOne(`${apiUrl}/${assetId}/assignments`);
      req.flush(
        { error: { message: 'Asset already assigned' } },
        { status: 409, statusText: 'Conflict' }
      );
    });
  });

  describe('assignToLocation', () => {
    it('should assign asset to location via POST request', () => {
      const assetId = '123e4567-e89b-12d3-a456-426614174000';
      const request: AssignmentRequest = {
        assignmentType: AssignmentType.LOCATION,
        assignedTo: 'Data Center A'
      };

      const mockResponse: Assignment = {
        id: '550e8400-e29b-41d4-a716-446655440000',
        assetId: assetId,
        assignmentType: AssignmentType.LOCATION,
        assignedTo: 'Data Center A',
        assignedBy: 'admin',
        assignedAt: '2024-01-15T10:30:00Z'
      };

      service.assignToLocation(assetId, request).subscribe(assignment => {
        expect(assignment).toEqual(mockResponse);
        expect(assignment.assignmentType).toBe(AssignmentType.LOCATION);
      });

      const req = httpMock.expectOne(`${apiUrl}/${assetId}/assignments`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body.assignmentType).toBe('LOCATION');
      expect(req.request.body.assignedTo).toBe('Data Center A');
      req.flush(mockResponse);
    });
  });

  describe('deallocate', () => {
    it('should deallocate asset via DELETE request', () => {
      const assetId = '123e4567-e89b-12d3-a456-426614174000';

      service.deallocate(assetId).subscribe(() => {
        expect(true).toBe(true); // Success
      });

      const req = httpMock.expectOne(`${apiUrl}/${assetId}/assignments`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should handle error when asset is not assigned', () => {
      const assetId = '123e4567-e89b-12d3-a456-426614174000';

      service.deallocate(assetId).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.message).toContain('not currently assigned');
        }
      });

      const req = httpMock.expectOne(`${apiUrl}/${assetId}/assignments`);
      req.flush(
        { error: { message: 'Asset not currently assigned' } },
        { status: 404, statusText: 'Not Found' }
      );
    });
  });

  describe('getAssignmentHistory', () => {
    it('should retrieve assignment history with pagination', () => {
      const assetId = '123e4567-e89b-12d3-a456-426614174000';
      const mockHistory: PageResponse<AssignmentHistoryDTO> = {
        content: [
          {
            id: '550e8400-e29b-41d4-a716-446655440000',
            assetId: assetId,
            assignmentType: AssignmentType.USER,
            assignedTo: 'John Doe',
            assignedBy: 'admin-id',
            assignedByUsername: 'admin',
            assignedAt: '2024-01-15T10:30:00Z',
            unassignedAt: '2024-01-20T14:00:00Z'
          }
        ],
        page: {
          size: 20,
          number: 0,
          totalElements: 1,
          totalPages: 1
        }
      };

      service.getAssignmentHistory(assetId, 0, 20).subscribe(history => {
        expect(history.content.length).toBe(1);
        expect(history.content[0].assetId).toBe(assetId);
        expect(history.page.totalElements).toBe(1);
      });

      const req = httpMock.expectOne(
        `${apiUrl}/${assetId}/assignment-history?page=0&size=20`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockHistory);
    });

    it('should use default pagination parameters', () => {
      const assetId = '123e4567-e89b-12d3-a456-426614174000';
      const mockHistory: PageResponse<AssignmentHistoryDTO> = {
        content: [],
        page: {
          size: 20,
          number: 0,
          totalElements: 0,
          totalPages: 0
        }
      };

      service.getAssignmentHistory(assetId).subscribe();

      const req = httpMock.expectOne(
        `${apiUrl}/${assetId}/assignment-history?page=0&size=20`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockHistory);
    });
  });

  describe('getAssetsByUser', () => {
    it('should retrieve assets assigned to user', () => {
      const userName = 'John Doe';
      const mockAssets: PageResponse<Asset> = {
        content: [
          {
            id: '123e4567-e89b-12d3-a456-426614174000',
            assetType: 'SERVER' as any,
            name: 'Server 01',
            serialNumber: 'SRV-001',
            acquisitionDate: '2024-01-15',
            status: 'IN_USE' as any,
            assignedUser: 'John Doe',
            assignedUserEmail: 'john.doe@example.com',
            createdAt: '2024-01-15T10:30:00Z',
            createdBy: 'admin',
            updatedAt: '2024-01-15T10:30:00Z',
            updatedBy: 'admin',
            readOnly: false
          }
        ],
        page: {
          size: 20,
          number: 0,
          totalElements: 1,
          totalPages: 1
        }
      };

      service.getAssetsByUser(userName, 0, 20).subscribe(assets => {
        expect(assets.content.length).toBe(1);
        expect(assets.content[0].assignedUser).toBe('John Doe');
      });

      const req = httpMock.expectOne(
        `${assignmentsUrl}/user/${encodeURIComponent(userName)}?page=0&size=20`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockAssets);
    });
  });

  describe('getAssetsByLocation', () => {
    it('should retrieve assets assigned to location', () => {
      const location = 'Data Center A';
      const mockAssets: PageResponse<Asset> = {
        content: [
          {
            id: '123e4567-e89b-12d3-a456-426614174000',
            assetType: 'SERVER' as any,
            name: 'Server 01',
            serialNumber: 'SRV-001',
            acquisitionDate: '2024-01-15',
            status: 'IN_USE' as any,
            location: 'Data Center A',
            createdAt: '2024-01-15T10:30:00Z',
            createdBy: 'admin',
            updatedAt: '2024-01-15T10:30:00Z',
            updatedBy: 'admin',
            readOnly: false
          }
        ],
        page: {
          size: 20,
          number: 0,
          totalElements: 1,
          totalPages: 1
        }
      };

      service.getAssetsByLocation(location, 0, 20).subscribe(assets => {
        expect(assets.content.length).toBe(1);
        expect(assets.content[0].location).toBe('Data Center A');
      });

      const req = httpMock.expectOne(
        `${assignmentsUrl}/location/${encodeURIComponent(location)}?page=0&size=20`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockAssets);
    });
  });

  describe('getStatistics', () => {
    it('should retrieve assignment statistics', () => {
      const mockStats: AssignmentStatistics = {
        totalAssigned: 150,
        userAssignments: 100,
        locationAssignments: 50,
        availableAssets: {
          inUse: 120,
          deployed: 80,
          storage: 30
        },
        topUsers: [
          { name: 'John Doe', count: 15 },
          { name: 'Jane Smith', count: 12 }
        ],
        topLocations: [
          { name: 'Data Center A', count: 25 },
          { name: 'Office Building 1', count: 20 }
        ]
      };

      service.getStatistics().subscribe(stats => {
        expect(stats.totalAssigned).toBe(150);
        expect(stats.userAssignments).toBe(100);
        expect(stats.topUsers.length).toBe(2);
        expect(stats.topLocations.length).toBe(2);
      });

      const req = httpMock.expectOne(`${assignmentsUrl}/statistics`);
      expect(req.request.method).toBe('GET');
      req.flush(mockStats);
    });
  });

  describe('exportAssignments', () => {
    it('should export assignments without filters', () => {
      const mockBlob = new Blob(['csv data'], { type: 'text/csv' });

      service.exportAssignments().subscribe(blob => {
        expect(blob).toBeTruthy();
        expect(blob.type).toBe('text/csv');
      });

      const req = httpMock.expectOne(`${assignmentsUrl}/export`);
      expect(req.request.method).toBe('GET');
      expect(req.request.responseType).toBe('blob');
      req.flush(mockBlob);
    });

    it('should export assignments with filters', () => {
      const filters: ExportFilters = {
        assignmentType: AssignmentType.USER,
        dateFrom: '2024-01-01',
        dateTo: '2024-12-31',
        assignedBy: 'admin'
      };
      const mockBlob = new Blob(['csv data'], { type: 'text/csv' });

      service.exportAssignments(filters).subscribe(blob => {
        expect(blob).toBeTruthy();
      });

      const req = httpMock.expectOne(
        `${assignmentsUrl}/export?assignmentType=USER&dateFrom=2024-01-01&dateTo=2024-12-31&assignedBy=admin`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockBlob);
    });
  });

  describe('bulkDeallocate', () => {
    it('should bulk deallocate multiple assets', () => {
      const assetIds = [
        '123e4567-e89b-12d3-a456-426614174000',
        '223e4567-e89b-12d3-a456-426614174001'
      ];

      const mockResult: BulkDeallocationResult = {
        successful: [
          {
            id: '550e8400-e29b-41d4-a716-446655440000',
            assetId: assetIds[0],
            assignmentType: AssignmentType.USER,
            assignedTo: 'John Doe',
            assignedBy: 'admin',
            assignedAt: '2024-01-15T10:30:00Z',
            unassignedAt: '2024-01-20T14:00:00Z'
          }
        ],
        failed: [
          {
            assetId: assetIds[1],
            error: 'Asset not currently assigned'
          }
        ],
        successCount: 1,
        failureCount: 1
      };

      service.bulkDeallocate(assetIds).subscribe(result => {
        expect(result.successCount).toBe(1);
        expect(result.failureCount).toBe(1);
        expect(result.successful.length).toBe(1);
        expect(result.failed.length).toBe(1);
      });

      const req = httpMock.expectOne(`${assignmentsUrl}/bulk-deallocate`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(assetIds);
      req.flush(mockResult);
    });

    it('should reject bulk deallocation exceeding 50 assets', (done) => {
      const assetIds = Array(51).fill('123e4567-e89b-12d3-a456-426614174000');

      service.bulkDeallocate(assetIds).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.message).toContain('limited to 50 assets');
          done();
        }
      });
    });
  });

  describe('reassign', () => {
    it('should reassign asset to user', () => {
      const assetId = '123e4567-e89b-12d3-a456-426614174000';
      const request: AssignmentRequest = {
        assignmentType: AssignmentType.USER,
        assignedTo: 'Jane Smith',
        assignedUserEmail: 'jane.smith@example.com'
      };

      const mockResponse: Assignment = {
        id: '550e8400-e29b-41d4-a716-446655440000',
        assetId: assetId,
        assignmentType: AssignmentType.USER,
        assignedTo: 'Jane Smith',
        assignedBy: 'admin',
        assignedAt: '2024-01-20T10:30:00Z'
      };

      service.reassign(assetId, request).subscribe(assignment => {
        expect(assignment.assignedTo).toBe('Jane Smith');
      });

      const req = httpMock.expectOne(`${apiUrl}/${assetId}/assignments`);
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });

    it('should reassign asset to location', () => {
      const assetId = '123e4567-e89b-12d3-a456-426614174000';
      const request: AssignmentRequest = {
        assignmentType: AssignmentType.LOCATION,
        assignedTo: 'Data Center B'
      };

      const mockResponse: Assignment = {
        id: '550e8400-e29b-41d4-a716-446655440000',
        assetId: assetId,
        assignmentType: AssignmentType.LOCATION,
        assignedTo: 'Data Center B',
        assignedBy: 'admin',
        assignedAt: '2024-01-20T10:30:00Z'
      };

      service.reassign(assetId, request).subscribe(assignment => {
        expect(assignment.assignedTo).toBe('Data Center B');
      });

      const req = httpMock.expectOne(`${apiUrl}/${assetId}/assignments`);
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });
  });

  describe('Error Handling', () => {
    it('should handle 400 Bad Request errors', () => {
      const assetId = '123e4567-e89b-12d3-a456-426614174000';
      const request: AssignmentRequest = {
        assignmentType: AssignmentType.USER,
        assignedTo: '',
        assignedUserEmail: 'invalid-email'
      };

      service.assignToUser(assetId, request).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.message).toBeTruthy();
        }
      });

      const req = httpMock.expectOne(`${apiUrl}/${assetId}/assignments`);
      req.flush(
        { error: { message: 'Validation failed' } },
        { status: 400, statusText: 'Bad Request' }
      );
    });

    it('should handle 403 Forbidden errors', () => {
      const assetId = '123e4567-e89b-12d3-a456-426614174000';
      const request: AssignmentRequest = {
        assignmentType: AssignmentType.USER,
        assignedTo: 'John Doe',
        assignedUserEmail: 'john.doe@example.com'
      };

      service.assignToUser(assetId, request).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.message).toBeTruthy();
        }
      });

      const req = httpMock.expectOne(`${apiUrl}/${assetId}/assignments`);
      req.flush(
        { error: { message: 'Insufficient permissions' } },
        { status: 403, statusText: 'Forbidden' }
      );
    });

    it('should handle 404 Not Found errors', () => {
      const assetId = 'non-existent-id';

      service.deallocate(assetId).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.message).toBeTruthy();
        }
      });

      const req = httpMock.expectOne(`${apiUrl}/${assetId}/assignments`);
      req.flush(
        { error: { message: 'Asset not found' } },
        { status: 404, statusText: 'Not Found' }
      );
    });

    it('should handle 422 Unprocessable Entity errors', () => {
      const assetId = '123e4567-e89b-12d3-a456-426614174000';
      const request: AssignmentRequest = {
        assignmentType: AssignmentType.USER,
        assignedTo: 'John Doe',
        assignedUserEmail: 'john.doe@example.com'
      };

      service.assignToUser(assetId, request).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.message).toContain('not assignable');
        }
      });

      const req = httpMock.expectOne(`${apiUrl}/${assetId}/assignments`);
      req.flush(
        { error: { message: 'Asset not assignable - status is RETIRED' } },
        { status: 422, statusText: 'Unprocessable Entity' }
      );
    });

    it('should handle 500 Internal Server Error', () => {
      const assetId = '123e4567-e89b-12d3-a456-426614174000';

      service.getAssignmentHistory(assetId).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.message).toBeTruthy();
        }
      });

      const req = httpMock.expectOne(`${apiUrl}/${assetId}/assignment-history?page=0&size=20`);
      req.flush(
        { error: { message: 'Internal server error' } },
        { status: 500, statusText: 'Internal Server Error' }
      );
    });

    it('should handle network errors', () => {
      const assetId = '123e4567-e89b-12d3-a456-426614174000';

      service.getStatistics().subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.message).toBeTruthy();
        }
      });

      const req = httpMock.expectOne(`${assignmentsUrl}/statistics`);
      req.error(new ProgressEvent('Network error'));
    });
  });

  describe('Request Parameters and Headers', () => {
    it('should properly encode URL parameters for user queries', () => {
      const userName = 'John Doe Jr.';
      const mockAssets: PageResponse<Asset> = {
        content: [],
        page: {
          size: 20,
          number: 0,
          totalElements: 0,
          totalPages: 0
        }
      };

      service.getAssetsByUser(userName, 0, 20).subscribe();

      const req = httpMock.expectOne(
        `${assignmentsUrl}/user/${encodeURIComponent(userName)}?page=0&size=20`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockAssets);
    });

    it('should properly encode URL parameters for location queries', () => {
      const location = 'Data Center A/B';
      const mockAssets: PageResponse<Asset> = {
        content: [],
        page: {
          size: 20,
          number: 0,
          totalElements: 0,
          totalPages: 0
        }
      };

      service.getAssetsByLocation(location, 0, 20).subscribe();

      const req = httpMock.expectOne(
        `${assignmentsUrl}/location/${encodeURIComponent(location)}?page=0&size=20`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockAssets);
    });

    it('should send correct Content-Type header for POST requests', () => {
      const assetId = '123e4567-e89b-12d3-a456-426614174000';
      const request: AssignmentRequest = {
        assignmentType: AssignmentType.USER,
        assignedTo: 'John Doe',
        assignedUserEmail: 'john.doe@example.com'
      };

      const mockResponse: Assignment = {
        id: '550e8400-e29b-41d4-a716-446655440000',
        assetId: assetId,
        assignmentType: AssignmentType.USER,
        assignedTo: 'John Doe',
        assignedBy: 'admin',
        assignedAt: '2024-01-15T10:30:00Z'
      };

      service.assignToUser(assetId, request).subscribe();

      const req = httpMock.expectOne(`${apiUrl}/${assetId}/assignments`);
      expect(req.request.headers.has('Content-Type')).toBe(false); // HttpClient sets this automatically
      req.flush(mockResponse);
    });

    it('should request blob response type for export', () => {
      const mockBlob = new Blob(['csv data'], { type: 'text/csv' });

      service.exportAssignments().subscribe();

      const req = httpMock.expectOne(`${assignmentsUrl}/export`);
      expect(req.request.responseType).toBe('blob');
      req.flush(mockBlob);
    });
  });

  describe('Edge Cases', () => {
    it('should handle empty assignment history', () => {
      const assetId = '123e4567-e89b-12d3-a456-426614174000';
      const mockHistory: PageResponse<AssignmentHistoryDTO> = {
        content: [],
        page: {
          size: 20,
          number: 0,
          totalElements: 0,
          totalPages: 0
        }
      };

      service.getAssignmentHistory(assetId).subscribe(history => {
        expect(history.content.length).toBe(0);
        expect(history.page.totalElements).toBe(0);
      });

      const req = httpMock.expectOne(`${apiUrl}/${assetId}/assignment-history?page=0&size=20`);
      req.flush(mockHistory);
    });

    it('should handle empty user assets query', () => {
      const userName = 'NonExistentUser';
      const mockAssets: PageResponse<Asset> = {
        content: [],
        page: {
          size: 20,
          number: 0,
          totalElements: 0,
          totalPages: 0
        }
      };

      service.getAssetsByUser(userName).subscribe(assets => {
        expect(assets.content.length).toBe(0);
      });

      const req = httpMock.expectOne(
        `${assignmentsUrl}/user/${encodeURIComponent(userName)}?page=0&size=20`
      );
      req.flush(mockAssets);
    });

    it('should handle empty location assets query', () => {
      const location = 'NonExistentLocation';
      const mockAssets: PageResponse<Asset> = {
        content: [],
        page: {
          size: 20,
          number: 0,
          totalElements: 0,
          totalPages: 0
        }
      };

      service.getAssetsByLocation(location).subscribe(assets => {
        expect(assets.content.length).toBe(0);
      });

      const req = httpMock.expectOne(
        `${assignmentsUrl}/location/${encodeURIComponent(location)}?page=0&size=20`
      );
      req.flush(mockAssets);
    });

    it('should handle statistics with zero assignments', () => {
      const mockStats: AssignmentStatistics = {
        totalAssigned: 0,
        userAssignments: 0,
        locationAssignments: 0,
        availableAssets: {
          inUse: 0,
          deployed: 0,
          storage: 0
        },
        topUsers: [],
        topLocations: []
      };

      service.getStatistics().subscribe(stats => {
        expect(stats.totalAssigned).toBe(0);
        expect(stats.topUsers.length).toBe(0);
        expect(stats.topLocations.length).toBe(0);
      });

      const req = httpMock.expectOne(`${assignmentsUrl}/statistics`);
      req.flush(mockStats);
    });

    it('should handle bulk deallocation with empty array', () => {
      const assetIds: string[] = [];
      const mockResult: BulkDeallocationResult = {
        successful: [],
        failed: [],
        successCount: 0,
        failureCount: 0
      };

      service.bulkDeallocate(assetIds).subscribe(result => {
        expect(result.successCount).toBe(0);
        expect(result.failureCount).toBe(0);
      });

      const req = httpMock.expectOne(`${assignmentsUrl}/bulk-deallocate`);
      req.flush(mockResult);
    });

    it('should handle export with partial filters', () => {
      const filters: ExportFilters = {
        assignmentType: AssignmentType.USER
      };
      const mockBlob = new Blob(['csv data'], { type: 'text/csv' });

      service.exportAssignments(filters).subscribe();

      const req = httpMock.expectOne(`${assignmentsUrl}/export?assignmentType=USER`);
      expect(req.request.method).toBe('GET');
      req.flush(mockBlob);
    });
  });
});
