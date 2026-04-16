import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  Assignment,
  AssignmentRequest,
  AssignmentHistoryDTO,
  AssignmentStatistics,
  BulkDeallocationResult,
  ExportFilters,
  Asset,
  PageResponse
} from '../../shared/models';

/**
 * Allocation Service
 * 
 * Handles all allocation management operations including:
 * - Asset assignment to users and locations
 * - Asset deallocation
 * - Assignment history retrieval
 * - Querying assets by user or location
 * - Assignment statistics
 * - Export and bulk operations
 * 
 * @Injectable providedIn: 'root' - Singleton service
 */
@Injectable({
  providedIn: 'root'
})
export class AllocationService {
  private readonly apiUrl = `${environment.apiUrl}/assets`;
  private readonly assignmentsUrl = `${environment.apiUrl}/assignments`;

  constructor(private http: HttpClient) {}

  /**
   * Assigns an asset to a user
   * 
   * @param assetId - The unique identifier of the asset
   * @param request - The assignment request containing user details
   * @returns Observable that emits the created assignment
   * @throws Error if assignment fails or user lacks permission
   */
  assignToUser(assetId: string, request: AssignmentRequest): Observable<Assignment> {
    const userRequest: AssignmentRequest = {
      ...request,
      assignmentType: 'USER' as any
    };
    
    return this.http.post<Assignment>(
      `${this.apiUrl}/${assetId}/assignments`,
      userRequest
    ).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Assigns an asset to a location
   * 
   * @param assetId - The unique identifier of the asset
   * @param request - The assignment request containing location details
   * @returns Observable that emits the created assignment
   * @throws Error if assignment fails or user lacks permission
   */
  assignToLocation(assetId: string, request: AssignmentRequest): Observable<Assignment> {
    const locationRequest: AssignmentRequest = {
      ...request,
      assignmentType: 'LOCATION' as any
    };
    
    return this.http.post<Assignment>(
      `${this.apiUrl}/${assetId}/assignments`,
      locationRequest
    ).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Deallocates an asset by removing its current assignment
   * 
   * @param assetId - The unique identifier of the asset
   * @returns Observable that completes when deallocation is successful
   * @throws Error if deallocation fails or asset is not assigned
   */
  deallocate(assetId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${assetId}/assignments`
    ).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Reassigns an asset from one user/location to another
   * This is a convenience method that combines deallocate and assign operations
   * 
   * @param assetId - The unique identifier of the asset
   * @param request - The new assignment request
   * @returns Observable that emits the new assignment
   * @throws Error if reassignment fails
   */
  reassign(assetId: string, request: AssignmentRequest): Observable<Assignment> {
    // Note: The backend handles reassignment as a single atomic operation
    // For now, we'll use the standard assignment endpoint
    // In the future, a dedicated reassignment endpoint could be added
    if (request.assignmentType === 'USER') {
      return this.assignToUser(assetId, request);
    } else {
      return this.assignToLocation(assetId, request);
    }
  }

  /**
   * Retrieves the assignment history for an asset
   * 
   * @param assetId - The unique identifier of the asset
   * @param page - Page number (zero-based, default: 0)
   * @param size - Number of items per page (default: 20)
   * @returns Observable that emits a paginated list of assignment history records
   * @throws Error if retrieval fails or asset not found
   */
  getAssignmentHistory(
    assetId: string,
    page: number = 0,
    size: number = 20
  ): Observable<PageResponse<AssignmentHistoryDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PageResponse<AssignmentHistoryDTO>>(
      `${this.apiUrl}/${assetId}/assignment-history`,
      { params }
    ).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Queries all assets assigned to a specific user
   * 
   * @param userName - The user name to search for (case-insensitive)
   * @param page - Page number (zero-based, default: 0)
   * @param size - Number of items per page (default: 20)
   * @returns Observable that emits a paginated list of assets
   * @throws Error if query fails
   */
  getAssetsByUser(
    userName: string,
    page: number = 0,
    size: number = 20
  ): Observable<PageResponse<Asset>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PageResponse<Asset>>(
      `${this.assignmentsUrl}/user/${encodeURIComponent(userName)}`,
      { params }
    ).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Queries all assets assigned to a specific location
   * 
   * @param location - The location name to search for (case-insensitive)
   * @param page - Page number (zero-based, default: 0)
   * @param size - Number of items per page (default: 20)
   * @returns Observable that emits a paginated list of assets
   * @throws Error if query fails
   */
  getAssetsByLocation(
    location: string,
    page: number = 0,
    size: number = 20
  ): Observable<PageResponse<Asset>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PageResponse<Asset>>(
      `${this.assignmentsUrl}/location/${encodeURIComponent(location)}`,
      { params }
    ).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Retrieves comprehensive assignment statistics
   * 
   * @returns Observable that emits assignment statistics including:
   *   - Total assigned assets
   *   - User vs location assignment counts
   *   - Available assets by status
   *   - Top 10 users and locations by assignment count
   * @throws Error if retrieval fails or user lacks permission
   */
  getStatistics(): Observable<AssignmentStatistics> {
    return this.http.get<AssignmentStatistics>(
      `${this.assignmentsUrl}/statistics`
    ).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Exports assignment data to CSV format
   * 
   * @param filters - Optional filters for the export (type, date range, assigned by)
   * @returns Observable that emits the CSV file as a Blob
   * @throws Error if export fails or exceeds maximum size (10,000 records)
   */
  exportAssignments(filters?: ExportFilters): Observable<Blob> {
    let params = new HttpParams();

    if (filters) {
      if (filters.assignmentType) {
        params = params.set('assignmentType', filters.assignmentType);
      }
      if (filters.dateFrom) {
        params = params.set('dateFrom', filters.dateFrom);
      }
      if (filters.dateTo) {
        params = params.set('dateTo', filters.dateTo);
      }
      if (filters.assignedBy) {
        params = params.set('assignedBy', filters.assignedBy);
      }
    }

    return this.http.get(
      `${this.assignmentsUrl}/export`,
      {
        params,
        responseType: 'blob'
      }
    ).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Bulk deallocates multiple assets in a single operation
   * 
   * @param assetIds - Array of asset IDs to deallocate (maximum 50)
   * @returns Observable that emits the bulk operation result with success/failure details
   * @throws Error if request exceeds maximum bulk size or user lacks permission
   */
  bulkDeallocate(assetIds: string[]): Observable<BulkDeallocationResult> {
    if (assetIds.length > 50) {
      return throwError(() => new Error('Bulk deallocation limited to 50 assets per request'));
    }

    return this.http.post<BulkDeallocationResult>(
      `${this.assignmentsUrl}/bulk-deallocate`,
      assetIds
    ).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Handles HTTP errors and transforms them into user-friendly error messages
   * 
   * @param error - The HTTP error response
   * @returns Observable that throws a formatted error
   * @private
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An error occurred';

    if (error.error instanceof ErrorEvent) {
      // Client-side or network error
      errorMessage = error.error.message;
    } else {
      // Server-side error
      if (error.error?.error?.message) {
        errorMessage = error.error.error.message;
      } else if (error.error?.message) {
        errorMessage = error.error.message;
      } else if (error.message) {
        errorMessage = error.message;
      } else {
        errorMessage = `Error Code: ${error.status}`;
      }
    }

    return throwError(() => new Error(errorMessage));
  }
}
