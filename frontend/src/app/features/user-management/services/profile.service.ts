import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { UserDTO, ProfileUpdateRequest, ChangePasswordRequest } from '../models';

/**
 * Profile Service
 * 
 * Handles user profile management operations including:
 * - Viewing current user profile
 * - Updating profile information
 * - Changing password
 */
@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private readonly apiUrl = `${environment.apiUrl}/profile`;

  constructor(private http: HttpClient) {}

  /**
   * Retrieves the current user's profile
   */
  getProfile(): Observable<UserDTO> {
    return this.http.get<UserDTO>(this.apiUrl);
  }

  /**
   * Updates the current user's profile
   */
  updateProfile(request: ProfileUpdateRequest): Observable<UserDTO> {
    return this.http.put<UserDTO>(this.apiUrl, request);
  }

  /**
   * Changes the current user's password
   */
  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/change-password`, request);
  }
}
