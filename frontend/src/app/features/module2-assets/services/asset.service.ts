import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { Asset, AssetRequest, AssetSearchQuery, Page, SpringPagePayload, mapSpringPageToAppPage } from '../models';
import { LifecycleStatus } from '../models/lifecycle-status.enum';
import { AssetHistoryEvent, AssetHistoryQuery } from '../models/asset-history-event.model';
import { AssignmentHistoryEntry, AssignmentHistoryQuery, AssignmentHistorySummary } from '../models/assignment-history-entry.model';

/**
 * Asset Service for API communication with the backend.
 * 
 * Provides methods for:
 * - CRUD operations on assets
 * - Asset search and filtering
 * - Status updates
 * - Import/Export functionality
 * 
 * All methods return RxJS Observables for reactive programming.
 * Error handling is implemented with catchError operator.
 */
@Injectable({
  providedIn: 'root'
})
export class AssetService {
  private readonly apiUrl = `${environment.apiUrl}/assets`;

  constructor(private http: HttpClient) {}

  /**
   * Retrieves a paginated list of assets with optional filtering.
   * 
   * @param query - Optional search query parameters for filtering
   * @param page - Page number (zero-based, default: 0)
   * @param size - Number of items per page (default: 20)
   * @param sort - Sort field and direction (e.g., 'name,asc')
   * @returns Observable that emits a paginated list of assets
   * @throws Error if the request fails
   */
  getAssets(
    query?: AssetSearchQuery,
    page: number = 0,
    size: number = environment.pagination.defaultPageSize,
    sort?: string
  ): Observable<Page<Asset>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (sort) {
      params = params.set('sort', sort);
    }

    if (query) {
      params = this.buildQueryParams(params, query);
    }

    return this.http.get<Page<Asset>>(this.apiUrl, { params })
      .pipe(
        map(body => mapSpringPageToAppPage(body as SpringPagePayload<Asset>)),
        catchError(this.handleError)
      );
  }

  /**
   * Retrieves a single asset by its ID.
   * 
   * @param id - The unique identifier of the asset (UUID format)
   * @returns Observable that emits the asset or throws an error
   * @throws Error if asset is not found or user lacks permission
   */
  getAsset(id: string): Observable<Asset> {
    return this.http.get<Asset>(`${this.apiUrl}/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Creates a new asset in the system.
   * 
   * @param request - Asset creation request containing asset details
   * @returns Observable that emits the created asset
   * @throws Error if validation fails or user lacks permission
   */
  createAsset(request: AssetRequest): Observable<Asset> {
    return this.http.post<Asset>(this.apiUrl, request)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Updates an existing asset with new data (full update).
   * 
   * @param id - The unique identifier of the asset to update
   * @param request - Asset update request containing updated asset details
   * @returns Observable that emits the updated asset
   * @throws Error if asset not found, validation fails, or user lacks permission
   */
  updateAsset(id: string, request: AssetRequest): Observable<Asset> {
    return this.http.put<Asset>(`${this.apiUrl}/${id}`, request)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Updates the lifecycle status of an asset.
   * 
   * @param id - The unique identifier of the asset
   * @param status - The new lifecycle status
   * @returns Observable that emits the updated asset
   * @throws Error if status transition is invalid or user lacks permission
   */
  updateStatus(id: string, status: LifecycleStatus): Observable<Asset> {
    return this.http.patch<Asset>(`${this.apiUrl}/${id}/status`, { status })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Deletes an asset from the system.
   * 
   * @param id - The unique identifier of the asset to delete
   * @returns Observable that completes when deletion is successful
   * @throws Error if asset not found or user lacks permission
   */
  deleteAsset(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Searches assets with advanced filtering and pagination.
   * 
   * @param query - Search query parameters
   * @param page - Page number (zero-based, default: 0)
   * @param size - Number of items per page (default: 20)
   * @param sort - Sort field and direction (e.g., 'name,asc')
   * @returns Observable that emits a paginated list of matching assets
   * @throws Error if the request fails
   */
  searchAssets(
    query: AssetSearchQuery,
    page: number = 0,
    size: number = environment.pagination.defaultPageSize,
    sort?: string
  ): Observable<Page<Asset>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (sort) {
      params = params.set('sort', sort);
    }

    params = this.buildQueryParams(params, query);

    return this.http.get<Page<Asset>>(`${this.apiUrl}/search`, { params })
      .pipe(
        map(body => mapSpringPageToAppPage(body as SpringPagePayload<Asset>)),
        catchError(this.handleError)
      );
  }

  /**
   * Exports assets to the specified format (CSV or JSON).
   * 
   * @param format - Export format ('CSV' or 'JSON')
   * @param query - Optional search query to filter exported assets
   * @returns Observable that emits the exported file as a Blob
   * @throws Error if export fails or user lacks permission
   */
  exportAssets(format: 'CSV' | 'JSON', query?: AssetSearchQuery): Observable<Blob> {
    let params = new HttpParams().set('format', format);

    if (query) {
      params = this.buildQueryParams(params, query);
    }

    return this.http.get(`${this.apiUrl}/export`, {
      params,
      responseType: 'blob'
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Imports assets from a file (CSV or JSON format).
   * 
   * @param format - Import format ('CSV' or 'JSON')
   * @param file - The file to import
   * @returns Observable that emits the import result with success/failure counts
   * @throws Error if import fails, validation errors occur, or user lacks permission
   */
  importAssets(format: 'CSV' | 'JSON', file: File): Observable<ImportResult> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('format', format);

    return this.http.post<ImportResult>(`${this.apiUrl}/import`, formData)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Retrieves the complete history of events for an asset.
   * 
   * @param id - The unique identifier of the asset
   * @param query - Optional query parameters for filtering history
   * @returns Observable that emits an array of asset history events
   * @throws Error if asset is not found or user lacks permission
   */
  getAssetHistory(id: string, query?: AssetHistoryQuery): Observable<Page<AssetHistoryEvent>> {
    let params = new HttpParams();

    if (query) {
      params = this.buildHistoryQueryParams(params, query);
    }

    return this.http.get<Page<AssetHistoryEvent>>(`${this.apiUrl}/${id}/history`, { params })
      .pipe(
        map(body => mapSpringPageToAppPage(body as SpringPagePayload<AssetHistoryEvent>)),
        catchError(this.handleError)
      );
  }

  /**
   * Retrieves the assignment history for an asset.
   * 
   * @param id - The unique identifier of the asset
   * @param query - Optional query parameters for filtering assignment history
   * @returns Observable that emits an array of assignment history entries
   * @throws Error if asset is not found or user lacks permission
   */
  getAssignmentHistory(id: string, query?: AssignmentHistoryQuery): Observable<Page<AssignmentHistoryEntry>> {
    let params = new HttpParams();

    if (query) {
      params = this.buildAssignmentHistoryQueryParams(params, query);
    }

    return this.http.get<Page<AssignmentHistoryEntry>>(`${this.apiUrl}/${id}/assignments`, { params })
      .pipe(
        map(body => mapSpringPageToAppPage(body as SpringPagePayload<AssignmentHistoryEntry>)),
        catchError(this.handleError)
      );
  }

  /**
   * Retrieves assignment history summary statistics for an asset.
   * 
   * @param id - The unique identifier of the asset
   * @returns Observable that emits assignment history summary
   * @throws Error if asset is not found or user lacks permission
   */
  getAssignmentHistorySummary(id: string): Observable<AssignmentHistorySummary> {
    return this.http.get<AssignmentHistorySummary>(`${this.apiUrl}/${id}/assignments/summary`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Retrieves asset statistics for dashboard display.
   * 
   * Returns quick statistics including:
   * - Total number of assets in the system
   * - Number of assets currently in use
   * - Timestamp when statistics were calculated
   * 
   * @returns Observable that emits asset statistics
   * @throws Error if the request fails or user lacks permission
   */
  getAssetStats(): Observable<AssetStats> {
    return this.http.get<AssetStats>(`${this.apiUrl}/stats`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Upload an image for an asset.
   * 
   * @param id Asset ID
   * @param file Image file to upload
   * @returns Observable of updated asset with image information
   */
  uploadAssetImage(id: string, file: File): Observable<Asset> {
    const formData = new FormData();
    formData.append('file', file);
    
    return this.http.post<Asset>(`${this.apiUrl}/${id}/image`, formData)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Builds HTTP query parameters from an AssetSearchQuery object.
   * 
   * @param params - Existing HttpParams object
   * @param query - Search query parameters
   * @returns Updated HttpParams with query parameters
   */
  private buildQueryParams(params: HttpParams, query: AssetSearchQuery): HttpParams {
    if (query.text) {
      params = params.set('text', query.text);
    }

    if (query.assetTypes && query.assetTypes.length > 0) {
      query.assetTypes.forEach(type => {
        params = params.append('assetTypes', type);
      });
    }

    if (query.statuses && query.statuses.length > 0) {
      query.statuses.forEach(status => {
        params = params.append('statuses', status);
      });
    }

    if (query.location) {
      params = params.set('location', query.location);
    }

    if (query.acquisitionDateFrom) {
      params = params.set('acquisitionDateFrom', query.acquisitionDateFrom);
    }

    if (query.acquisitionDateTo) {
      params = params.set('acquisitionDateTo', query.acquisitionDateTo);
    }

    if (query.assignedUser) {
      params = params.set('assignedUser', query.assignedUser);
    }

    return params;
  }

  /**
   * Builds HTTP query parameters from an AssetHistoryQuery object.
   * 
   * @param params - Existing HttpParams object
   * @param query - History query parameters
   * @returns Updated HttpParams with query parameters
   */
  private buildHistoryQueryParams(params: HttpParams, query: AssetHistoryQuery): HttpParams {
    if (query.eventType) {
      params = params.set('eventType', query.eventType);
    }

    if (query.dateFrom) {
      params = params.set('dateFrom', query.dateFrom);
    }

    if (query.dateTo) {
      params = params.set('dateTo', query.dateTo);
    }

    if (query.page !== undefined) {
      params = params.set('page', query.page.toString());
    }

    if (query.size !== undefined) {
      params = params.set('size', query.size.toString());
    }

    return params;
  }

  /**
   * Builds HTTP query parameters from an AssignmentHistoryQuery object.
   * 
   * @param params - Existing HttpParams object
   * @param query - Assignment history query parameters
   * @returns Updated HttpParams with query parameters
   */
  private buildAssignmentHistoryQueryParams(params: HttpParams, query: AssignmentHistoryQuery): HttpParams {
    if (query.status) {
      params = params.set('status', query.status);
    }

    if (query.dateFrom) {
      params = params.set('dateFrom', query.dateFrom);
    }

    if (query.dateTo) {
      params = params.set('dateTo', query.dateTo);
    }

    if (query.assignedUser) {
      params = params.set('assignedUser', query.assignedUser);
    }

    if (query.page !== undefined) {
      params = params.set('page', query.page.toString());
    }

    if (query.size !== undefined) {
      params = params.set('size', query.size.toString());
    }

    return params;
  }

  /**
   * Handles HTTP errors and transforms them into user-friendly error messages.
   * 
   * @param error - The HTTP error response
   * @returns Observable that throws an error with a descriptive message
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unexpected error occurred';

    if (error.error instanceof ErrorEvent) {
      // Client-side or network error
      errorMessage = `Network error: ${error.error.message}`;
    } else if (error.error instanceof ProgressEvent) {
      // Network error (e.g., connection refused)
      errorMessage = 'Network error: Unable to connect to the server';
    } else {
      // Backend error - check for structured error response first
      if (error.error?.error?.message) {
        // Structured error response from backend
        errorMessage = error.error.error.message;
      } else if (error.error?.message) {
        errorMessage = error.error.message;
      } else {
        // Generic error based on status code
        switch (error.status) {
          case 400:
            errorMessage = 'Invalid request. Please check your input.';
            break;
          case 401:
            errorMessage = 'Authentication required. Please log in.';
            break;
          case 403:
            errorMessage = 'You do not have permission to perform this action.';
            break;
          case 404:
            errorMessage = 'The requested resource was not found.';
            break;
          case 409:
            errorMessage = 'A conflict occurred. The resource may already exist.';
            break;
          case 422:
            errorMessage = 'The request cannot be processed due to invalid state.';
            break;
          case 429:
            errorMessage = 'Too many requests. Please try again later.';
            break;
          case 500:
            errorMessage = 'Internal server error. Please try again later.';
            break;
          case 503:
            errorMessage = 'Service temporarily unavailable. Please try again later.';
            break;
          default:
            errorMessage = `Error: ${error.status} - ${error.statusText}`;
        }
      }
    }

    console.error('AssetService Error:', error);
    return throwError(() => new Error(errorMessage));
  }
}

/**
 * Import result interface for asset import operations.
 */
export interface ImportResult {
  /** Number of assets successfully imported */
  successCount: number;
  
  /** Number of assets that failed to import */
  failureCount: number;
  
  /** Total number of assets processed */
  totalCount: number;
  
  /** List of errors encountered during import */
  errors?: ImportError[];
  
  /** Optional message about the import operation */
  message?: string;
}

/**
 * Import error interface for individual asset import failures.
 */
export interface ImportError {
  /** Line number or row index where the error occurred */
  line: number;
  
  /** Error message describing what went wrong */
  message: string;
  
  /** Optional field name that caused the error */
  field?: string;
}

/**
 * Asset statistics interface for dashboard display.
 */
export interface AssetStats {
  /** Total number of assets in the system */
  totalAssets: number;
  
  /** Number of assets currently in use (status = IN_USE) */
  assetsInUse: number;
  
  /** Timestamp when statistics were calculated */
  lastUpdated: string;
}
