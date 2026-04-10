import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { 
  UserDTO, 
  UserRequest, 
  UserUpdateRequest, 
  RoleAssignmentRequest,
  UserFilterOptions 
} from '../models';
import { Role } from '../../../core/models/auth.model';
import { PageResponse } from '../../../shared/models/page-response.model';

/**
 * User Service
 * 
 * Handles all user management API operations including:
 * - User CRUD operations
 * - Role management
 * - Account status management
 * - User filtering and pagination
 */
@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  /**
   * Retrieves paginated list of users with optional filtering
   */
  getUsers(
    page: number = 0, 
    size: number = 20, 
    filters?: UserFilterOptions
  ): Observable<PageResponse<UserDTO>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (filters?.role) {
      params = params.set('role', filters.role);
    }

    if (filters?.isActive !== undefined) {
      params = params.set('isActive', filters.isActive.toString());
    }

    if (filters?.searchText) {
      params = params.set('search', filters.searchText);
    }

    return this.http.get<PageResponse<UserDTO>>(this.apiUrl, { params });
  }

  /**
   * Retrieves a single user by ID
   */
  getUser(id: string): Observable<UserDTO> {
    return this.http.get<UserDTO>(`${this.apiUrl}/${id}`);
  }

  /**
   * Creates a new user account
   */
  createUser(request: UserRequest): Observable<UserDTO> {
    return this.http.post<UserDTO>(this.apiUrl, request);
  }

  /**
   * Updates an existing user account
   */
  updateUser(id: string, request: UserUpdateRequest): Observable<UserDTO> {
    return this.http.put<UserDTO>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Deletes a user account
   */
  deleteUser(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Enables a user account
   */
  enableUser(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/enable`, {});
  }

  /**
   * Disables a user account
   */
  disableUser(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/disable`, {});
  }

  /**
   * Assigns a role to a user
   */
  assignRole(id: string, role: Role): Observable<void> {
    const request: RoleAssignmentRequest = { role };
    return this.http.post<void>(`${this.apiUrl}/${id}/roles`, request);
  }

  /**
   * Revokes a role from a user
   */
  revokeRole(id: string, role: Role): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}/roles/${role}`);
  }

  /**
   * Retrieves users by role with pagination
   */
  getUsersByRole(role: Role, page: number = 0, size: number = 20): Observable<PageResponse<UserDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('role', role);

    return this.http.get<PageResponse<UserDTO>>(this.apiUrl, { params });
  }
}
