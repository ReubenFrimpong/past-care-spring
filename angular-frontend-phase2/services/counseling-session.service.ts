import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CounselingSessionRequest,
  CounselingSessionResponse,
  CounselingSessionStatsResponse,
  CompleteSessionRequest,
  ScheduleFollowUpRequest,
  CreateReferralRequest
} from '../models/counseling-session.interface';
import { CounselingStatus } from '../models/counseling-status.enum';
import { CounselingType } from '../models/counseling-type.enum';

@Injectable({
  providedIn: 'root'
})
export class CounselingSessionService {
  private apiUrl = `${environment.apiUrl}/counseling-sessions`;

  constructor(private http: HttpClient) {}

  // CRUD Operations
  createSession(request: CounselingSessionRequest): Observable<CounselingSessionResponse> {
    return this.http.post<CounselingSessionResponse>(this.apiUrl, request);
  }

  getSessionById(id: number): Observable<CounselingSessionResponse> {
    return this.http.get<CounselingSessionResponse>(`${this.apiUrl}/${id}`);
  }

  getAllSessions(page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(this.apiUrl, { params });
  }

  updateSession(id: number, request: CounselingSessionRequest): Observable<CounselingSessionResponse> {
    return this.http.put<CounselingSessionResponse>(`${this.apiUrl}/${id}`, request);
  }

  deleteSession(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Session Actions
  completeSession(id: number, request: CompleteSessionRequest): Observable<CounselingSessionResponse> {
    return this.http.post<CounselingSessionResponse>(`${this.apiUrl}/${id}/complete`, request);
  }

  scheduleFollowUp(id: number, request: ScheduleFollowUpRequest): Observable<CounselingSessionResponse> {
    return this.http.post<CounselingSessionResponse>(`${this.apiUrl}/${id}/follow-up`, request);
  }

  createReferral(id: number, request: CreateReferralRequest): Observable<CounselingSessionResponse> {
    return this.http.post<CounselingSessionResponse>(`${this.apiUrl}/${id}/referral`, request);
  }

  // Query Methods
  getMySessions(page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/my-sessions`, { params });
  }

  getSessionsByCounselor(counselorId: number, page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/counselor/${counselorId}`, { params });
  }

  getSessionsByStatus(status: CounselingStatus, page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/status/${status}`, { params });
  }

  getSessionsByType(type: CounselingType, page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/type/${type}`, { params });
  }

  getUpcomingSessions(page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/upcoming`, { params });
  }

  getMyUpcomingSessions(page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/my-upcoming`, { params });
  }

  getSessionsRequiringFollowUp(page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/follow-ups`, { params });
  }

  getSessionsByMember(memberId: number, page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/member/${memberId}`, { params });
  }

  getSessionsByCareNeed(careNeedId: number, page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/care-need/${careNeedId}`, { params });
  }

  searchSessions(searchTerm: string, page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('searchTerm', searchTerm)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/search`, { params });
  }

  getSessionStats(): Observable<CounselingSessionStatsResponse> {
    return this.http.get<CounselingSessionStatsResponse>(`${this.apiUrl}/stats`);
  }
}
