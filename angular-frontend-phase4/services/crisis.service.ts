import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CrisisRequest,
  CrisisResponse,
  CrisisStatsResponse,
  CrisisAffectedMemberRequest,
  CrisisAffectedMemberResponse
} from '../models/crisis.interface';
import { CrisisStatus } from '../models/crisis-status.enum';
import { CrisisType } from '../models/crisis-type.enum';
import { CrisisSeverity } from '../models/crisis-severity.enum';

@Injectable({
  providedIn: 'root'
})
export class CrisisService {
  private apiUrl = `${environment.apiUrl}/crises`;

  constructor(private http: HttpClient) {}

  // CRUD Operations
  reportCrisis(request: CrisisRequest): Observable<CrisisResponse> {
    return this.http.post<CrisisResponse>(this.apiUrl, request);
  }

  getCrisisById(id: number): Observable<CrisisResponse> {
    return this.http.get<CrisisResponse>(`${this.apiUrl}/${id}`);
  }

  getAllCrises(page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(this.apiUrl, { params });
  }

  updateCrisis(id: number, request: CrisisRequest): Observable<CrisisResponse> {
    return this.http.put<CrisisResponse>(`${this.apiUrl}/${id}`, request);
  }

  deleteCrisis(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Affected Members Management
  addAffectedMember(crisisId: number, request: CrisisAffectedMemberRequest): Observable<CrisisAffectedMemberResponse> {
    return this.http.post<CrisisAffectedMemberResponse>(`${this.apiUrl}/${crisisId}/affected-members`, request);
  }

  removeAffectedMember(crisisId: number, memberId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${crisisId}/affected-members/${memberId}`);
  }

  // Crisis Actions
  mobilizeResources(id: number, resources: string): Observable<CrisisResponse> {
    return this.http.post<CrisisResponse>(`${this.apiUrl}/${id}/mobilize`, { resources });
  }

  sendEmergencyNotifications(id: number): Observable<CrisisResponse> {
    return this.http.post<CrisisResponse>(`${this.apiUrl}/${id}/notify`, {});
  }

  resolveCrisis(id: number, resolutionNotes: string): Observable<CrisisResponse> {
    return this.http.post<CrisisResponse>(`${this.apiUrl}/${id}/resolve`, { resolutionNotes });
  }

  updateStatus(id: number, status: CrisisStatus): Observable<CrisisResponse> {
    return this.http.patch<CrisisResponse>(`${this.apiUrl}/${id}/status`, { status });
  }

  // Query Methods
  getActiveCrises(page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/active`, { params });
  }

  getCriticalCrises(page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/critical`, { params });
  }

  getCrisesByStatus(status: CrisisStatus, page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/status/${status}`, { params });
  }

  getCrisesByType(type: CrisisType, page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/type/${type}`, { params });
  }

  getCrisesBySeverity(severity: CrisisSeverity, page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/severity/${severity}`, { params });
  }

  searchCrises(searchTerm: string, page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('searchTerm', searchTerm)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/search`, { params });
  }

  getCrisisStats(): Observable<CrisisStatsResponse> {
    return this.http.get<CrisisStatsResponse>(`${this.apiUrl}/stats`);
  }
}
