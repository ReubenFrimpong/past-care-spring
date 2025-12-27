import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  PrayerRequestRequest,
  PrayerRequestResponse,
  PrayerRequestStatsResponse,
  MarkAsAnsweredRequest
} from '../models/prayer-request.interface';
import { PrayerRequestStatus } from '../models/prayer-request-status.enum';
import { PrayerCategory } from '../models/prayer-category.enum';

@Injectable({
  providedIn: 'root'
})
export class PrayerRequestService {
  private apiUrl = `${environment.apiUrl}/prayer-requests`;

  constructor(private http: HttpClient) {}

  // CRUD Operations
  createPrayerRequest(request: PrayerRequestRequest): Observable<PrayerRequestResponse> {
    return this.http.post<PrayerRequestResponse>(this.apiUrl, request);
  }

  getPrayerRequestById(id: number): Observable<PrayerRequestResponse> {
    return this.http.get<PrayerRequestResponse>(`${this.apiUrl}/${id}`);
  }

  getAllPrayerRequests(page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(this.apiUrl, { params });
  }

  updatePrayerRequest(id: number, request: PrayerRequestRequest): Observable<PrayerRequestResponse> {
    return this.http.put<PrayerRequestResponse>(`${this.apiUrl}/${id}`, request);
  }

  deletePrayerRequest(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Prayer Actions
  incrementPrayerCount(id: number): Observable<PrayerRequestResponse> {
    return this.http.post<PrayerRequestResponse>(`${this.apiUrl}/${id}/pray`, {});
  }

  markAsAnswered(id: number, testimony: string): Observable<PrayerRequestResponse> {
    return this.http.post<PrayerRequestResponse>(`${this.apiUrl}/${id}/answer`, { testimony });
  }

  archivePrayerRequest(id: number): Observable<PrayerRequestResponse> {
    return this.http.post<PrayerRequestResponse>(`${this.apiUrl}/${id}/archive`, {});
  }

  // Query Methods
  getActivePrayerRequests(page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/active`, { params });
  }

  getUrgentPrayerRequests(page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/urgent`, { params });
  }

  getMyPrayerRequests(page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/my-requests`, { params });
  }

  getPrayerRequestsByStatus(status: PrayerRequestStatus, page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/status/${status}`, { params });
  }

  getPrayerRequestsByCategory(category: PrayerCategory, page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/category/${category}`, { params });
  }

  getAnsweredPrayerRequests(page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/answered`, { params });
  }

  getPublicPrayerRequests(page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/public`, { params });
  }

  searchPrayerRequests(searchTerm: string, page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('searchTerm', searchTerm)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/search`, { params });
  }

  getPrayerRequestStats(): Observable<PrayerRequestStatsResponse> {
    return this.http.get<PrayerRequestStatsResponse>(`${this.apiUrl}/stats`);
  }

  getExpiringSoon(page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/expiring-soon`, { params });
  }

  autoArchiveExpired(): Observable<number> {
    return this.http.post<number>(`${this.apiUrl}/auto-archive`, {});
  }
}
