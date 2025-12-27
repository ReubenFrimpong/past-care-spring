# Pastoral Care Module - Phase 4: Crisis Management Frontend Implementation

**Date**: December 26, 2025
**Status**: Complete Frontend Implementation Guide
**Phase**: Phase 4 - Crisis & Emergency Management

---

## Overview

This document provides the complete Angular frontend implementation for Phase 4: Crisis & Emergency Management. All files follow the exact patterns from the pastoral-care-page (Phase 1) implementation.

**Total Files**: 10 files
- 4 TypeScript interfaces/enums
- 1 Angular service
- 3 Page component files (TS, HTML, CSS)
- 1 Route configuration update
- 1 Side navigation update

---

## File 1: crisis-type.enum.ts

**Location**: `/src/app/interfaces/crisis-type.enum.ts`

```typescript
/**
 * Crisis Type Enum - matches backend CrisisType
 */
export enum CrisisType {
  DEATH = 'DEATH',
  ACCIDENT = 'ACCIDENT',
  HOSPITALIZATION = 'HOSPITALIZATION',
  NATURAL_DISASTER = 'NATURAL_DISASTER',
  FIRE = 'FIRE',
  FINANCIAL_CRISIS = 'FINANCIAL_CRISIS',
  FAMILY_VIOLENCE = 'FAMILY_VIOLENCE',
  SUICIDE_RISK = 'SUICIDE_RISK',
  MENTAL_HEALTH_CRISIS = 'MENTAL_HEALTH_CRISIS',
  HOMELESSNESS = 'HOMELESSNESS',
  JOB_LOSS = 'JOB_LOSS',
  LEGAL_ISSUE = 'LEGAL_ISSUE',
  OTHER = 'OTHER'
}

/**
 * Human-readable labels for crisis types
 */
export const CRISIS_TYPE_LABELS: Record<CrisisType, string> = {
  [CrisisType.DEATH]: 'Death',
  [CrisisType.ACCIDENT]: 'Accident',
  [CrisisType.HOSPITALIZATION]: 'Hospitalization',
  [CrisisType.NATURAL_DISASTER]: 'Natural Disaster',
  [CrisisType.FIRE]: 'Fire',
  [CrisisType.FINANCIAL_CRISIS]: 'Financial Crisis',
  [CrisisType.FAMILY_VIOLENCE]: 'Family Violence',
  [CrisisType.SUICIDE_RISK]: 'Suicide Risk',
  [CrisisType.MENTAL_HEALTH_CRISIS]: 'Mental Health Crisis',
  [CrisisType.HOMELESSNESS]: 'Homelessness',
  [CrisisType.JOB_LOSS]: 'Job Loss',
  [CrisisType.LEGAL_ISSUE]: 'Legal Issue',
  [CrisisType.OTHER]: 'Other'
};

/**
 * Dropdown options for PrimeNG
 */
export const CRISIS_TYPE_OPTIONS = Object.values(CrisisType).map(type => ({
  label: CRISIS_TYPE_LABELS[type],
  value: type
}));
```

---

## File 2: crisis-severity.enum.ts

**Location**: `/src/app/interfaces/crisis-severity.enum.ts`

```typescript
/**
 * Crisis Severity Enum - matches backend CrisisSeverity
 */
export enum CrisisSeverity {
  CRITICAL = 'CRITICAL',
  HIGH = 'HIGH',
  MODERATE = 'MODERATE',
  LOW = 'LOW'
}

/**
 * Human-readable labels for crisis severity
 */
export const CRISIS_SEVERITY_LABELS: Record<CrisisSeverity, string> = {
  [CrisisSeverity.CRITICAL]: 'Critical',
  [CrisisSeverity.HIGH]: 'High',
  [CrisisSeverity.MODERATE]: 'Moderate',
  [CrisisSeverity.LOW]: 'Low'
};

/**
 * Severity badge colors (matching PrimeNG severity types)
 */
export const CRISIS_SEVERITY_COLORS: Record<CrisisSeverity, string> = {
  [CrisisSeverity.CRITICAL]: 'danger',
  [CrisisSeverity.HIGH]: 'warning',
  [CrisisSeverity.MODERATE]: 'info',
  [CrisisSeverity.LOW]: 'secondary'
};

/**
 * Dropdown options for PrimeNG
 */
export const CRISIS_SEVERITY_OPTIONS = Object.values(CrisisSeverity).map(severity => ({
  label: CRISIS_SEVERITY_LABELS[severity],
  value: severity
}));
```

---

## File 3: crisis-status.enum.ts

**Location**: `/src/app/interfaces/crisis-status.enum.ts`

```typescript
/**
 * Crisis Status Enum - matches backend CrisisStatus
 */
export enum CrisisStatus {
  ACTIVE = 'ACTIVE',
  IN_RESPONSE = 'IN_RESPONSE',
  RESOLVED = 'RESOLVED',
  CLOSED = 'CLOSED'
}

/**
 * Human-readable labels for crisis status
 */
export const CRISIS_STATUS_LABELS: Record<CrisisStatus, string> = {
  [CrisisStatus.ACTIVE]: 'Active',
  [CrisisStatus.IN_RESPONSE]: 'In Response',
  [CrisisStatus.RESOLVED]: 'Resolved',
  [CrisisStatus.CLOSED]: 'Closed'
};

/**
 * Status badge colors
 */
export const CRISIS_STATUS_COLORS: Record<CrisisStatus, string> = {
  [CrisisStatus.ACTIVE]: 'danger',
  [CrisisStatus.IN_RESPONSE]: 'warning',
  [CrisisStatus.RESOLVED]: 'success',
  [CrisisStatus.CLOSED]: 'secondary'
};

/**
 * Dropdown options for PrimeNG
 */
export const CRISIS_STATUS_OPTIONS = Object.values(CrisisStatus).map(status => ({
  label: CRISIS_STATUS_LABELS[status],
  value: status
}));
```

---

## File 4: crisis.interface.ts

**Location**: `/src/app/interfaces/crisis.interface.ts`

```typescript
import { CrisisType } from './crisis-type.enum';
import { CrisisSeverity } from './crisis-severity.enum';
import { CrisisStatus } from './crisis-status.enum';

/**
 * Crisis Affected Member Interface - matches CrisisAffectedMemberResponse
 */
export interface CrisisAffectedMember {
  id: number;
  crisisId: number;
  memberId: number;
  memberName: string;
  notes?: string;
  isPrimaryContact: boolean;
  createdAt: Date;
  updatedAt: Date;
}

/**
 * Crisis Interface - matches CrisisResponse from backend
 */
export interface Crisis {
  id: number;
  title: string;
  description: string;
  crisisType: CrisisType;
  severity: CrisisSeverity;
  status: CrisisStatus;
  reportedById: number;
  reportedByName: string;
  reportedDate: Date;
  incidentDate: Date;
  location: string;
  affectedMembersCount: number;
  responseTeamNotes?: string;
  resolutionNotes?: string;
  resolvedDate?: Date;
  followUpRequired: boolean;
  followUpDate?: Date;
  resourcesMobilized?: string;
  communicationSent: boolean;
  emergencyContactNotified: boolean;
  isActive: boolean;
  isCritical: boolean;
  isResolved: boolean;
  affectedMembers: CrisisAffectedMember[];
  createdAt: Date;
  updatedAt: Date;
}

/**
 * Crisis Request - for creating/updating crises
 */
export interface CrisisRequest {
  title: string;
  description: string;
  crisisType: CrisisType;
  severity: CrisisSeverity;
  status?: CrisisStatus;
  incidentDate: Date;
  location: string;
  affectedMemberIds?: number[];
  affectedMembersCount?: number;
  responseTeamNotes?: string;
  resolutionNotes?: string;
  followUpRequired?: boolean;
  followUpDate?: Date;
  resourcesMobilized?: string;
  communicationSent?: boolean;
  emergencyContactNotified?: boolean;
}

/**
 * Crisis Statistics - matches CrisisStatsResponse
 */
export interface CrisisStats {
  total: number;
  active: number;
  inResponse: number;
  resolved: number;
  critical: number;
  highSeverity: number;
  totalAffectedMembers: number;
}

/**
 * Affected Member Request - for adding members to crisis
 */
export interface CrisisAffectedMemberRequest {
  memberId: number;
  notes?: string;
  isPrimaryContact?: boolean;
}
```

---

## File 5: crisis.service.ts

**Location**: `/src/app/services/crisis.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  Crisis,
  CrisisRequest,
  CrisisStats,
  CrisisAffectedMember,
  CrisisAffectedMemberRequest
} from '../interfaces/crisis.interface';
import { CrisisStatus } from '../interfaces/crisis-status.enum';
import { CrisisType } from '../interfaces/crisis-type.enum';
import { CrisisSeverity } from '../interfaces/crisis-severity.enum';

/**
 * Crisis Service - handles all crisis-related API calls
 */
@Injectable({
  providedIn: 'root'
})
export class CrisisService {
  private readonly apiUrl = `${environment.apiUrl}/crises`;

  constructor(private http: HttpClient) {}

  /**
   * Report a new crisis
   */
  reportCrisis(request: CrisisRequest): Observable<Crisis> {
    return this.http.post<Crisis>(this.apiUrl, request);
  }

  /**
   * Get crisis by ID
   */
  getCrisisById(id: number): Observable<Crisis> {
    return this.http.get<Crisis>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get all crises with pagination
   */
  getCrises(page: number = 0, size: number = 20, sortBy: string = 'reportedDate', sortDir: string = 'DESC'): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    return this.http.get<any>(this.apiUrl, { params });
  }

  /**
   * Update an existing crisis
   */
  updateCrisis(id: number, request: CrisisRequest): Observable<Crisis> {
    return this.http.put<Crisis>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Delete a crisis
   */
  deleteCrisis(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Add affected member to crisis
   */
  addAffectedMember(crisisId: number, request: CrisisAffectedMemberRequest): Observable<CrisisAffectedMember> {
    return this.http.post<CrisisAffectedMember>(`${this.apiUrl}/${crisisId}/affected-members`, request);
  }

  /**
   * Remove affected member from crisis
   */
  removeAffectedMember(crisisId: number, memberId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${crisisId}/affected-members/${memberId}`);
  }

  /**
   * Mobilize resources for a crisis
   */
  mobilizeResources(crisisId: number, resources: string): Observable<Crisis> {
    return this.http.post<Crisis>(`${this.apiUrl}/${crisisId}/mobilize`, { resources });
  }

  /**
   * Send emergency notifications
   */
  sendEmergencyNotifications(crisisId: number): Observable<Crisis> {
    return this.http.post<Crisis>(`${this.apiUrl}/${crisisId}/notify`, {});
  }

  /**
   * Resolve a crisis
   */
  resolveCrisis(crisisId: number, resolutionNotes: string): Observable<Crisis> {
    return this.http.post<Crisis>(`${this.apiUrl}/${crisisId}/resolve`, { resolutionNotes });
  }

  /**
   * Update crisis status
   */
  updateStatus(crisisId: number, status: CrisisStatus): Observable<Crisis> {
    return this.http.patch<Crisis>(`${this.apiUrl}/${crisisId}/status`, { status });
  }

  /**
   * Get active crises
   */
  getActiveCrises(): Observable<Crisis[]> {
    return this.http.get<Crisis[]>(`${this.apiUrl}/active`);
  }

  /**
   * Get critical crises
   */
  getCriticalCrises(): Observable<Crisis[]> {
    return this.http.get<Crisis[]>(`${this.apiUrl}/critical`);
  }

  /**
   * Get crises by status
   */
  getCrisesByStatus(status: CrisisStatus, page: number = 0, size: number = 20): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/status/${status}`, { params });
  }

  /**
   * Get crises by type
   */
  getCrisesByType(type: CrisisType, page: number = 0, size: number = 20): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/type/${type}`, { params });
  }

  /**
   * Get crises by severity
   */
  getCrisesBySeverity(severity: CrisisSeverity, page: number = 0, size: number = 20): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/severity/${severity}`, { params });
  }

  /**
   * Search crises
   */
  searchCrises(search: string, page: number = 0, size: number = 20): Observable<any> {
    let params = new HttpParams()
      .set('search', search)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/search`, { params });
  }

  /**
   * Get crisis statistics
   */
  getCrisisStats(): Observable<CrisisStats> {
    return this.http.get<CrisisStats>(`${this.apiUrl}/stats`);
  }
}
```

---

## File 6: crisis-management-page.ts

**Location**: `/src/app/pages/crisis-management-page/crisis-management-page.ts`

```typescript
import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { DropdownModule } from 'primeng/dropdown';
import { CalendarModule } from 'primeng/calendar';
import { CheckboxModule } from 'primeng/checkbox';
import { TagModule } from 'primeng/tag';
import { MultiSelectModule } from 'primeng/multiselect';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { CrisisService } from '../../services/crisis.service';
import { MemberService } from '../../services/member.service';
import {
  Crisis,
  CrisisRequest,
  CrisisStats,
  CrisisAffectedMemberRequest
} from '../../interfaces/crisis.interface';
import { CrisisType, CRISIS_TYPE_LABELS, CRISIS_TYPE_OPTIONS } from '../../interfaces/crisis-type.enum';
import { CrisisSeverity, CRISIS_SEVERITY_LABELS, CRISIS_SEVERITY_COLORS, CRISIS_SEVERITY_OPTIONS } from '../../interfaces/crisis-severity.enum';
import { CrisisStatus, CRISIS_STATUS_LABELS, CRISIS_STATUS_COLORS, CRISIS_STATUS_OPTIONS } from '../../interfaces/crisis-status.enum';

@Component({
  selector: 'app-crisis-management-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    CardModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    InputTextareaModule,
    DropdownModule,
    CalendarModule,
    CheckboxModule,
    TagModule,
    MultiSelectModule,
    ConfirmDialogModule,
    ToastModule
  ],
  providers: [ConfirmationService, MessageService],
  templateUrl: './crisis-management-page.html',
  styleUrls: ['./crisis-management-page.css']
})
export class CrisisManagementPage implements OnInit {
  // Signals for reactive state
  crises = signal<Crisis[]>([]);
  stats = signal<CrisisStats>({
    total: 0,
    active: 0,
    inResponse: 0,
    resolved: 0,
    critical: 0,
    highSeverity: 0,
    totalAffectedMembers: 0
  });
  loading = signal<boolean>(false);
  searchTerm = signal<string>('');
  selectedStatus = signal<CrisisStatus | null>(null);
  selectedType = signal<CrisisType | null>(null);
  selectedSeverity = signal<CrisisSeverity | null>(null);

  // Dialog states
  showCrisisDialog = signal<boolean>(false);
  showViewDialog = signal<boolean>(false);
  showAffectedMembersDialog = signal<boolean>(false);
  showMobilizeDialog = signal<boolean>(false);
  showResolveDialog = signal<boolean>(false);
  isEditMode = signal<boolean>(false);

  // Forms
  crisisForm!: FormGroup;
  affectedMemberForm!: FormGroup;
  mobilizeForm!: FormGroup;
  resolveForm!: FormGroup;

  // Current crisis being edited/viewed
  selectedCrisis = signal<Crisis | null>(null);

  // Dropdown options
  crisisTypeOptions = CRISIS_TYPE_OPTIONS;
  crisisSeverityOptions = CRISIS_SEVERITY_OPTIONS;
  crisisStatusOptions = CRISIS_STATUS_OPTIONS;
  memberOptions = signal<any[]>([]);

  // Enums for template
  CrisisType = CrisisType;
  CrisisSeverity = CrisisSeverity;
  CrisisStatus = CrisisStatus;
  CRISIS_TYPE_LABELS = CRISIS_TYPE_LABELS;
  CRISIS_SEVERITY_LABELS = CRISIS_SEVERITY_LABELS;
  CRISIS_SEVERITY_COLORS = CRISIS_SEVERITY_COLORS;
  CRISIS_STATUS_LABELS = CRISIS_STATUS_LABELS;
  CRISIS_STATUS_COLORS = CRISIS_STATUS_COLORS;

  // Computed filtered crises
  filteredCrises = computed(() => {
    let result = this.crises();
    const search = this.searchTerm().toLowerCase();
    const status = this.selectedStatus();
    const type = this.selectedType();
    const severity = this.selectedSeverity();

    if (search) {
      result = result.filter(c =>
        c.title.toLowerCase().includes(search) ||
        c.description.toLowerCase().includes(search) ||
        c.location.toLowerCase().includes(search)
      );
    }

    if (status) {
      result = result.filter(c => c.status === status);
    }

    if (type) {
      result = result.filter(c => c.crisisType === type);
    }

    if (severity) {
      result = result.filter(c => c.severity === severity);
    }

    return result;
  });

  constructor(
    private fb: FormBuilder,
    private crisisService: CrisisService,
    private memberService: MemberService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {
    this.initializeForms();
  }

  ngOnInit(): void {
    this.loadCrises();
    this.loadStats();
    this.loadMembers();
  }

  private initializeForms(): void {
    this.crisisForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      description: ['', [Validators.required]],
      crisisType: [null, Validators.required],
      severity: [null, Validators.required],
      status: [CrisisStatus.ACTIVE],
      incidentDate: [new Date(), Validators.required],
      location: ['', Validators.required],
      affectedMemberIds: [[]],
      responseTeamNotes: [''],
      followUpRequired: [false],
      followUpDate: [null]
    });

    this.affectedMemberForm = this.fb.group({
      memberId: [null, Validators.required],
      notes: [''],
      isPrimaryContact: [false]
    });

    this.mobilizeForm = this.fb.group({
      resources: ['', Validators.required]
    });

    this.resolveForm = this.fb.group({
      resolutionNotes: ['', Validators.required]
    });
  }

  loadCrises(): void {
    this.loading.set(true);
    this.crisisService.getCrises().subscribe({
      next: (response) => {
        this.crises.set(response.content || []);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading crises:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load crises'
        });
        this.loading.set(false);
      }
    });
  }

  loadStats(): void {
    this.crisisService.getCrisisStats().subscribe({
      next: (stats) => {
        this.stats.set(stats);
      },
      error: (error) => {
        console.error('Error loading stats:', error);
      }
    });
  }

  loadMembers(): void {
    this.memberService.getMembers().subscribe({
      next: (response) => {
        const members = response.content || [];
        this.memberOptions.set(
          members.map((m: any) => ({
            label: `${m.firstName} ${m.lastName}`,
            value: m.id
          }))
        );
      },
      error: (error) => {
        console.error('Error loading members:', error);
      }
    });
  }

  openNewCrisisDialog(): void {
    this.isEditMode.set(false);
    this.crisisForm.reset({
      status: CrisisStatus.ACTIVE,
      incidentDate: new Date(),
      followUpRequired: false,
      affectedMemberIds: []
    });
    this.showCrisisDialog.set(true);
  }

  openEditCrisisDialog(crisis: Crisis): void {
    this.isEditMode.set(true);
    this.selectedCrisis.set(crisis);
    this.crisisForm.patchValue({
      title: crisis.title,
      description: crisis.description,
      crisisType: crisis.crisisType,
      severity: crisis.severity,
      status: crisis.status,
      incidentDate: new Date(crisis.incidentDate),
      location: crisis.location,
      affectedMemberIds: crisis.affectedMembers.map(m => m.memberId),
      responseTeamNotes: crisis.responseTeamNotes,
      followUpRequired: crisis.followUpRequired,
      followUpDate: crisis.followUpDate ? new Date(crisis.followUpDate) : null
    });
    this.showCrisisDialog.set(true);
  }

  openViewDialog(crisis: Crisis): void {
    this.selectedCrisis.set(crisis);
    this.showViewDialog.set(true);
  }

  saveCrisis(): void {
    if (this.crisisForm.invalid) {
      Object.keys(this.crisisForm.controls).forEach(key => {
        this.crisisForm.get(key)?.markAsTouched();
      });
      return;
    }

    const formValue = this.crisisForm.value;
    const request: CrisisRequest = {
      title: formValue.title,
      description: formValue.description,
      crisisType: formValue.crisisType,
      severity: formValue.severity,
      status: formValue.status,
      incidentDate: formValue.incidentDate,
      location: formValue.location,
      affectedMemberIds: formValue.affectedMemberIds || [],
      responseTeamNotes: formValue.responseTeamNotes,
      followUpRequired: formValue.followUpRequired,
      followUpDate: formValue.followUpDate
    };

    const operation = this.isEditMode()
      ? this.crisisService.updateCrisis(this.selectedCrisis()!.id, request)
      : this.crisisService.reportCrisis(request);

    operation.subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: `Crisis ${this.isEditMode() ? 'updated' : 'reported'} successfully`
        });
        this.showCrisisDialog.set(false);
        this.loadCrises();
        this.loadStats();
      },
      error: (error) => {
        console.error('Error saving crisis:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: `Failed to ${this.isEditMode() ? 'update' : 'report'} crisis`
        });
      }
    });
  }

  deleteCrisis(crisis: Crisis): void {
    this.confirmationService.confirm({
      message: `Are you sure you want to delete the crisis "${crisis.title}"?`,
      header: 'Confirm Delete',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.crisisService.deleteCrisis(crisis.id).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Success',
              detail: 'Crisis deleted successfully'
            });
            this.loadCrises();
            this.loadStats();
          },
          error: (error) => {
            console.error('Error deleting crisis:', error);
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to delete crisis'
            });
          }
        });
      }
    });
  }

  openAffectedMembersDialog(crisis: Crisis): void {
    this.selectedCrisis.set(crisis);
    this.affectedMemberForm.reset({ isPrimaryContact: false });
    this.showAffectedMembersDialog.set(true);
  }

  addAffectedMember(): void {
    if (this.affectedMemberForm.invalid) {
      return;
    }

    const request: CrisisAffectedMemberRequest = this.affectedMemberForm.value;
    this.crisisService.addAffectedMember(this.selectedCrisis()!.id, request).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Affected member added successfully'
        });
        this.affectedMemberForm.reset({ isPrimaryContact: false });
        this.loadCrises();
        // Reload selected crisis
        this.crisisService.getCrisisById(this.selectedCrisis()!.id).subscribe(crisis => {
          this.selectedCrisis.set(crisis);
        });
      },
      error: (error) => {
        console.error('Error adding affected member:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to add affected member'
        });
      }
    });
  }

  removeAffectedMember(memberId: number): void {
    this.confirmationService.confirm({
      message: 'Are you sure you want to remove this member from affected members?',
      header: 'Confirm Remove',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.crisisService.removeAffectedMember(this.selectedCrisis()!.id, memberId).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Success',
              detail: 'Affected member removed successfully'
            });
            this.loadCrises();
            // Reload selected crisis
            this.crisisService.getCrisisById(this.selectedCrisis()!.id).subscribe(crisis => {
              this.selectedCrisis.set(crisis);
            });
          },
          error: (error) => {
            console.error('Error removing affected member:', error);
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to remove affected member'
            });
          }
        });
      }
    });
  }

  openMobilizeDialog(crisis: Crisis): void {
    this.selectedCrisis.set(crisis);
    this.mobilizeForm.reset();
    this.showMobilizeDialog.set(true);
  }

  mobilizeResources(): void {
    if (this.mobilizeForm.invalid) {
      return;
    }

    const resources = this.mobilizeForm.value.resources;
    this.crisisService.mobilizeResources(this.selectedCrisis()!.id, resources).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Resources mobilized successfully'
        });
        this.showMobilizeDialog.set(false);
        this.loadCrises();
        this.loadStats();
      },
      error: (error) => {
        console.error('Error mobilizing resources:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to mobilize resources'
        });
      }
    });
  }

  sendNotifications(crisis: Crisis): void {
    this.confirmationService.confirm({
      message: 'Send emergency notifications for this crisis?',
      header: 'Confirm Send Notifications',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.crisisService.sendEmergencyNotifications(crisis.id).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Success',
              detail: 'Emergency notifications sent successfully'
            });
            this.loadCrises();
          },
          error: (error) => {
            console.error('Error sending notifications:', error);
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to send notifications'
            });
          }
        });
      }
    });
  }

  openResolveDialog(crisis: Crisis): void {
    this.selectedCrisis.set(crisis);
    this.resolveForm.reset();
    this.showResolveDialog.set(true);
  }

  resolveCrisis(): void {
    if (this.resolveForm.invalid) {
      return;
    }

    const resolutionNotes = this.resolveForm.value.resolutionNotes;
    this.crisisService.resolveCrisis(this.selectedCrisis()!.id, resolutionNotes).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Crisis resolved successfully'
        });
        this.showResolveDialog.set(false);
        this.loadCrises();
        this.loadStats();
      },
      error: (error) => {
        console.error('Error resolving crisis:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to resolve crisis'
        });
      }
    });
  }

  updateCrisisStatus(crisis: Crisis, status: CrisisStatus): void {
    this.crisisService.updateStatus(crisis.id, status).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Crisis status updated successfully'
        });
        this.loadCrises();
        this.loadStats();
      },
      error: (error) => {
        console.error('Error updating status:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to update status'
        });
      }
    });
  }

  clearFilters(): void {
    this.searchTerm.set('');
    this.selectedStatus.set(null);
    this.selectedType.set(null);
    this.selectedSeverity.set(null);
  }
}
```

---

## File 7: crisis-management-page.html

**Location**: `/src/app/pages/crisis-management-page/crisis-management-page.html`

```html
<div class="crisis-management-page">
  <p-toast></p-toast>
  <p-confirmDialog></p-confirmDialog>

  <!-- Header -->
  <div class="page-header">
    <div class="header-content">
      <h1>Crisis & Emergency Management</h1>
      <p class="subtitle">Track and respond to crisis situations and emergencies</p>
    </div>
    <button
      pButton
      label="Report Crisis"
      icon="pi pi-plus"
      class="p-button-danger"
      (click)="openNewCrisisDialog()"
    ></button>
  </div>

  <!-- Statistics Cards -->
  <div class="stats-grid">
    <div class="stat-card">
      <div class="stat-icon total">
        <i class="pi pi-exclamation-triangle"></i>
      </div>
      <div class="stat-content">
        <div class="stat-value">{{ stats().total }}</div>
        <div class="stat-label">Total Crises</div>
      </div>
    </div>

    <div class="stat-card">
      <div class="stat-icon active">
        <i class="pi pi-circle-fill"></i>
      </div>
      <div class="stat-content">
        <div class="stat-value">{{ stats().active }}</div>
        <div class="stat-label">Active</div>
      </div>
    </div>

    <div class="stat-card">
      <div class="stat-icon in-response">
        <i class="pi pi-spinner"></i>
      </div>
      <div class="stat-content">
        <div class="stat-value">{{ stats().inResponse }}</div>
        <div class="stat-label">In Response</div>
      </div>
    </div>

    <div class="stat-card">
      <div class="stat-icon critical">
        <i class="pi pi-bolt"></i>
      </div>
      <div class="stat-content">
        <div class="stat-value">{{ stats().critical }}</div>
        <div class="stat-label">Critical</div>
      </div>
    </div>

    <div class="stat-card">
      <div class="stat-icon resolved">
        <i class="pi pi-check-circle"></i>
      </div>
      <div class="stat-content">
        <div class="stat-value">{{ stats().resolved }}</div>
        <div class="stat-label">Resolved</div>
      </div>
    </div>
  </div>

  <!-- Filters -->
  <div class="filters-section">
    <div class="search-box">
      <i class="pi pi-search"></i>
      <input
        type="text"
        pInputText
        placeholder="Search crises..."
        [value]="searchTerm()"
        (input)="searchTerm.set($any($event.target).value)"
      />
    </div>

    <p-dropdown
      [options]="crisisStatusOptions"
      [(ngModel)]="selectedStatus"
      placeholder="Filter by Status"
      [showClear]="true"
      styleClass="filter-dropdown"
    ></p-dropdown>

    <p-dropdown
      [options]="crisisTypeOptions"
      [(ngModel)]="selectedType"
      placeholder="Filter by Type"
      [showClear]="true"
      styleClass="filter-dropdown"
    ></p-dropdown>

    <p-dropdown
      [options]="crisisSeverityOptions"
      [(ngModel)]="selectedSeverity"
      placeholder="Filter by Severity"
      [showClear]="true"
      styleClass="filter-dropdown"
    ></p-dropdown>

    <button
      pButton
      label="Clear Filters"
      icon="pi pi-filter-slash"
      class="p-button-outlined"
      (click)="clearFilters()"
      [disabled]="!searchTerm() && !selectedStatus() && !selectedType() && !selectedSeverity()"
    ></button>
  </div>

  <!-- Crisis Grid -->
  <div class="crisis-grid" *ngIf="!loading() && filteredCrises().length > 0">
    <div class="crisis-card" *ngFor="let crisis of filteredCrises()">
      <!-- Crisis Header -->
      <div class="crisis-header">
        <div class="crisis-title-section">
          <h3 class="crisis-title">{{ crisis.title }}</h3>
          <div class="crisis-meta">
            <span class="meta-item">
              <i class="pi pi-calendar"></i>
              {{ crisis.incidentDate | date: 'MMM d, y' }}
            </span>
            <span class="meta-item">
              <i class="pi pi-map-marker"></i>
              {{ crisis.location }}
            </span>
          </div>
        </div>
        <div class="crisis-badges">
          <p-tag
            [value]="CRISIS_SEVERITY_LABELS[crisis.severity]"
            [severity]="CRISIS_SEVERITY_COLORS[crisis.severity]"
          ></p-tag>
          <p-tag
            [value]="CRISIS_STATUS_LABELS[crisis.status]"
            [severity]="CRISIS_STATUS_COLORS[crisis.status]"
          ></p-tag>
        </div>
      </div>

      <!-- Crisis Type -->
      <div class="crisis-type">
        <i class="pi pi-exclamation-circle"></i>
        <span>{{ CRISIS_TYPE_LABELS[crisis.crisisType] }}</span>
      </div>

      <!-- Crisis Description -->
      <p class="crisis-description">{{ crisis.description }}</p>

      <!-- Crisis Info -->
      <div class="crisis-info">
        <div class="info-item">
          <span class="info-label">Reported By:</span>
          <span class="info-value">{{ crisis.reportedByName }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">Affected Members:</span>
          <span class="info-value badge">{{ crisis.affectedMembersCount || 0 }}</span>
        </div>
        @if (crisis.resourcesMobilized) {
          <div class="info-item">
            <span class="info-label">Resources:</span>
            <span class="info-value">{{ crisis.resourcesMobilized }}</span>
          </div>
        }
        @if (crisis.followUpRequired && crisis.followUpDate) {
          <div class="info-item">
            <span class="info-label">Follow-up:</span>
            <span class="info-value">{{ crisis.followUpDate | date: 'MMM d, y' }}</span>
          </div>
        }
      </div>

      <!-- Notification Indicators -->
      <div class="crisis-indicators" *ngIf="crisis.communicationSent || crisis.emergencyContactNotified">
        <span class="indicator" *ngIf="crisis.communicationSent">
          <i class="pi pi-send"></i> Communications Sent
        </span>
        <span class="indicator" *ngIf="crisis.emergencyContactNotified">
          <i class="pi pi-phone"></i> Emergency Contacts Notified
        </span>
      </div>

      <!-- Action Buttons -->
      <div class="crisis-actions">
        <button
          pButton
          label="View"
          icon="pi pi-eye"
          class="p-button-sm p-button-outlined"
          (click)="openViewDialog(crisis)"
        ></button>
        <button
          pButton
          label="Edit"
          icon="pi pi-pencil"
          class="p-button-sm p-button-outlined"
          (click)="openEditCrisisDialog(crisis)"
        ></button>
        <button
          pButton
          label="Members"
          icon="pi pi-users"
          class="p-button-sm p-button-outlined"
          (click)="openAffectedMembersDialog(crisis)"
        ></button>
        <button
          pButton
          label="Mobilize"
          icon="pi pi-send"
          class="p-button-sm p-button-warning"
          (click)="openMobilizeDialog(crisis)"
          *ngIf="crisis.status === CrisisStatus.ACTIVE"
        ></button>
        <button
          pButton
          label="Notify"
          icon="pi pi-bell"
          class="p-button-sm p-button-warning"
          (click)="sendNotifications(crisis)"
          *ngIf="!crisis.communicationSent"
        ></button>
        <button
          pButton
          label="Resolve"
          icon="pi pi-check"
          class="p-button-sm p-button-success"
          (click)="openResolveDialog(crisis)"
          *ngIf="crisis.status !== CrisisStatus.RESOLVED && crisis.status !== CrisisStatus.CLOSED"
        ></button>
        <button
          pButton
          icon="pi pi-trash"
          class="p-button-sm p-button-danger p-button-text"
          (click)="deleteCrisis(crisis)"
        ></button>
      </div>
    </div>
  </div>

  <!-- Empty State -->
  <div class="empty-state" *ngIf="!loading() && filteredCrises().length === 0">
    <i class="pi pi-exclamation-triangle empty-icon"></i>
    <h3>No Crises Found</h3>
    <p *ngIf="searchTerm() || selectedStatus() || selectedType() || selectedSeverity()">
      Try adjusting your filters to see more results
    </p>
    <p *ngIf="!searchTerm() && !selectedStatus() && !selectedType() && !selectedSeverity()">
      No crises have been reported yet
    </p>
  </div>

  <!-- Loading State -->
  <div class="loading-state" *ngIf="loading()">
    <i class="pi pi-spinner pi-spin"></i>
    <p>Loading crises...</p>
  </div>
</div>

<!-- Add/Edit Crisis Dialog -->
<p-dialog
  [header]="isEditMode() ? 'Edit Crisis' : 'Report New Crisis'"
  [(visible)]="showCrisisDialog"
  [modal]="true"
  [style]="{ width: '600px' }"
  [draggable]="false"
  [resizable]="false"
>
  <form [formGroup]="crisisForm" class="crisis-form">
    <div class="p-fluid">
      <div class="p-field">
        <label for="title">Title *</label>
        <input
          id="title"
          type="text"
          pInputText
          formControlName="title"
          placeholder="Brief title of the crisis"
        />
        <small class="p-error" *ngIf="crisisForm.get('title')?.invalid && crisisForm.get('title')?.touched">
          Title is required
        </small>
      </div>

      <div class="p-field">
        <label for="description">Description *</label>
        <textarea
          id="description"
          pInputTextarea
          formControlName="description"
          rows="4"
          placeholder="Describe the crisis situation"
        ></textarea>
        <small class="p-error" *ngIf="crisisForm.get('description')?.invalid && crisisForm.get('description')?.touched">
          Description is required
        </small>
      </div>

      <div class="p-field">
        <label for="crisisType">Crisis Type *</label>
        <p-dropdown
          id="crisisType"
          formControlName="crisisType"
          [options]="crisisTypeOptions"
          placeholder="Select crisis type"
        ></p-dropdown>
        <small class="p-error" *ngIf="crisisForm.get('crisisType')?.invalid && crisisForm.get('crisisType')?.touched">
          Crisis type is required
        </small>
      </div>

      <div class="p-field">
        <label for="severity">Severity *</label>
        <p-dropdown
          id="severity"
          formControlName="severity"
          [options]="crisisSeverityOptions"
          placeholder="Select severity level"
        ></p-dropdown>
        <small class="p-error" *ngIf="crisisForm.get('severity')?.invalid && crisisForm.get('severity')?.touched">
          Severity is required
        </small>
      </div>

      <div class="p-field">
        <label for="incidentDate">Incident Date *</label>
        <p-calendar
          id="incidentDate"
          formControlName="incidentDate"
          [showTime]="true"
          dateFormat="mm/dd/yy"
          placeholder="Select incident date"
        ></p-calendar>
        <small class="p-error" *ngIf="crisisForm.get('incidentDate')?.invalid && crisisForm.get('incidentDate')?.touched">
          Incident date is required
        </small>
      </div>

      <div class="p-field">
        <label for="location">Location *</label>
        <input
          id="location"
          type="text"
          pInputText
          formControlName="location"
          placeholder="Crisis location"
        />
        <small class="p-error" *ngIf="crisisForm.get('location')?.invalid && crisisForm.get('location')?.touched">
          Location is required
        </small>
      </div>

      <div class="p-field">
        <label for="affectedMembers">Affected Members</label>
        <p-multiSelect
          id="affectedMembers"
          formControlName="affectedMemberIds"
          [options]="memberOptions()"
          placeholder="Select affected members"
          [filter]="true"
        ></p-multiSelect>
      </div>

      <div class="p-field">
        <label for="responseTeamNotes">Response Team Notes</label>
        <textarea
          id="responseTeamNotes"
          pInputTextarea
          formControlName="responseTeamNotes"
          rows="3"
          placeholder="Notes for response team"
        ></textarea>
      </div>

      <div class="p-field-checkbox">
        <p-checkbox
          id="followUpRequired"
          formControlName="followUpRequired"
          [binary]="true"
        ></p-checkbox>
        <label for="followUpRequired">Follow-up Required</label>
      </div>

      <div class="p-field" *ngIf="crisisForm.get('followUpRequired')?.value">
        <label for="followUpDate">Follow-up Date</label>
        <p-calendar
          id="followUpDate"
          formControlName="followUpDate"
          [showTime]="true"
          dateFormat="mm/dd/yy"
          placeholder="Select follow-up date"
        ></p-calendar>
      </div>
    </div>
  </form>

  <ng-template pTemplate="footer">
    <button
      pButton
      label="Cancel"
      icon="pi pi-times"
      class="p-button-outlined"
      (click)="showCrisisDialog.set(false)"
    ></button>
    <button
      pButton
      [label]="isEditMode() ? 'Update' : 'Report Crisis'"
      icon="pi pi-check"
      class="p-button-danger"
      (click)="saveCrisis()"
    ></button>
  </ng-template>
</p-dialog>

<!-- View Crisis Dialog -->
<p-dialog
  header="Crisis Details"
  [(visible)]="showViewDialog"
  [modal]="true"
  [style]="{ width: '700px' }"
  [draggable]="false"
  [resizable]="false"
>
  <div class="view-dialog-content" *ngIf="selectedCrisis()">
    <div class="view-section">
      <h3>{{ selectedCrisis()!.title }}</h3>
      <div class="view-badges">
        <p-tag
          [value]="CRISIS_SEVERITY_LABELS[selectedCrisis()!.severity]"
          [severity]="CRISIS_SEVERITY_COLORS[selectedCrisis()!.severity]"
        ></p-tag>
        <p-tag
          [value]="CRISIS_STATUS_LABELS[selectedCrisis()!.status]"
          [severity]="CRISIS_STATUS_COLORS[selectedCrisis()!.status]"
        ></p-tag>
      </div>
    </div>

    <div class="view-section">
      <div class="view-field">
        <label>Crisis Type:</label>
        <span>{{ CRISIS_TYPE_LABELS[selectedCrisis()!.crisisType] }}</span>
      </div>
      <div class="view-field">
        <label>Incident Date:</label>
        <span>{{ selectedCrisis()!.incidentDate | date: 'MMM d, y h:mm a' }}</span>
      </div>
      <div class="view-field">
        <label>Location:</label>
        <span>{{ selectedCrisis()!.location }}</span>
      </div>
      <div class="view-field">
        <label>Reported By:</label>
        <span>{{ selectedCrisis()!.reportedByName }}</span>
      </div>
      <div class="view-field">
        <label>Reported Date:</label>
        <span>{{ selectedCrisis()!.reportedDate | date: 'MMM d, y h:mm a' }}</span>
      </div>
    </div>

    <div class="view-section">
      <label>Description:</label>
      <p>{{ selectedCrisis()!.description }}</p>
    </div>

    <div class="view-section" *ngIf="selectedCrisis()!.affectedMembers?.length > 0">
      <label>Affected Members ({{ selectedCrisis()!.affectedMembers.length }}):</label>
      <ul class="affected-members-list">
        <li *ngFor="let member of selectedCrisis()!.affectedMembers">
          {{ member.memberName }}
          <span class="primary-contact" *ngIf="member.isPrimaryContact">(Primary Contact)</span>
        </li>
      </ul>
    </div>

    <div class="view-section" *ngIf="selectedCrisis()!.responseTeamNotes">
      <label>Response Team Notes:</label>
      <p>{{ selectedCrisis()!.responseTeamNotes }}</p>
    </div>

    <div class="view-section" *ngIf="selectedCrisis()!.resourcesMobilized">
      <label>Resources Mobilized:</label>
      <p>{{ selectedCrisis()!.resourcesMobilized }}</p>
    </div>

    <div class="view-section" *ngIf="selectedCrisis()!.resolutionNotes">
      <label>Resolution Notes:</label>
      <p>{{ selectedCrisis()!.resolutionNotes }}</p>
    </div>

    <div class="view-section" *ngIf="selectedCrisis()!.resolvedDate">
      <div class="view-field">
        <label>Resolved Date:</label>
        <span>{{ selectedCrisis()!.resolvedDate | date: 'MMM d, y h:mm a' }}</span>
      </div>
    </div>
  </div>

  <ng-template pTemplate="footer">
    <button
      pButton
      label="Close"
      icon="pi pi-times"
      class="p-button-outlined"
      (click)="showViewDialog.set(false)"
    ></button>
  </ng-template>
</p-dialog>

<!-- Affected Members Dialog -->
<p-dialog
  header="Manage Affected Members"
  [(visible)]="showAffectedMembersDialog"
  [modal]="true"
  [style]="{ width: '600px' }"
  [draggable]="false"
  [resizable]="false"
>
  <div class="affected-members-dialog" *ngIf="selectedCrisis()">
    <h4>Current Affected Members ({{ selectedCrisis()!.affectedMembers?.length || 0 }})</h4>

    <div class="affected-members-list" *ngIf="selectedCrisis()!.affectedMembers?.length > 0">
      <div class="affected-member-item" *ngFor="let member of selectedCrisis()!.affectedMembers">
        <div class="member-info">
          <span class="member-name">{{ member.memberName }}</span>
          <span class="primary-badge" *ngIf="member.isPrimaryContact">Primary Contact</span>
          <p class="member-notes" *ngIf="member.notes">{{ member.notes }}</p>
        </div>
        <button
          pButton
          icon="pi pi-trash"
          class="p-button-sm p-button-danger p-button-text"
          (click)="removeAffectedMember(member.memberId)"
        ></button>
      </div>
    </div>

    <div class="empty-members" *ngIf="!selectedCrisis()!.affectedMembers?.length">
      <p>No affected members added yet</p>
    </div>

    <hr class="section-divider" />

    <h4>Add Affected Member</h4>
    <form [formGroup]="affectedMemberForm" class="add-member-form">
      <div class="p-fluid">
        <div class="p-field">
          <label for="member">Member *</label>
          <p-dropdown
            id="member"
            formControlName="memberId"
            [options]="memberOptions()"
            placeholder="Select member"
            [filter]="true"
          ></p-dropdown>
        </div>

        <div class="p-field">
          <label for="memberNotes">Notes</label>
          <textarea
            id="memberNotes"
            pInputTextarea
            formControlName="notes"
            rows="2"
            placeholder="Additional notes about this member"
          ></textarea>
        </div>

        <div class="p-field-checkbox">
          <p-checkbox
            id="primaryContact"
            formControlName="isPrimaryContact"
            [binary]="true"
          ></p-checkbox>
          <label for="primaryContact">Primary Contact</label>
        </div>
      </div>

      <button
        pButton
        label="Add Member"
        icon="pi pi-plus"
        class="p-button-success"
        (click)="addAffectedMember()"
        [disabled]="affectedMemberForm.invalid"
      ></button>
    </form>
  </div>

  <ng-template pTemplate="footer">
    <button
      pButton
      label="Close"
      icon="pi pi-times"
      class="p-button-outlined"
      (click)="showAffectedMembersDialog.set(false)"
    ></button>
  </ng-template>
</p-dialog>

<!-- Mobilize Resources Dialog -->
<p-dialog
  header="Mobilize Resources"
  [(visible)]="showMobilizeDialog"
  [modal]="true"
  [style]="{ width: '500px' }"
  [draggable]="false"
  [resizable]="false"
>
  <form [formGroup]="mobilizeForm" class="mobilize-form">
    <div class="p-fluid">
      <div class="p-field">
        <label for="resources">Resources to Mobilize *</label>
        <textarea
          id="resources"
          pInputTextarea
          formControlName="resources"
          rows="4"
          placeholder="Describe resources being mobilized for this crisis"
        ></textarea>
        <small class="p-error" *ngIf="mobilizeForm.get('resources')?.invalid && mobilizeForm.get('resources')?.touched">
          Resources description is required
        </small>
      </div>
    </div>
  </form>

  <ng-template pTemplate="footer">
    <button
      pButton
      label="Cancel"
      icon="pi pi-times"
      class="p-button-outlined"
      (click)="showMobilizeDialog.set(false)"
    ></button>
    <button
      pButton
      label="Mobilize"
      icon="pi pi-send"
      class="p-button-warning"
      (click)="mobilizeResources()"
      [disabled]="mobilizeForm.invalid"
    ></button>
  </ng-template>
</p-dialog>

<!-- Resolve Crisis Dialog -->
<p-dialog
  header="Resolve Crisis"
  [(visible)]="showResolveDialog"
  [modal]="true"
  [style]="{ width: '500px' }"
  [draggable]="false"
  [resizable]="false"
>
  <form [formGroup]="resolveForm" class="resolve-form">
    <div class="p-fluid">
      <div class="p-field">
        <label for="resolutionNotes">Resolution Notes *</label>
        <textarea
          id="resolutionNotes"
          pInputTextarea
          formControlName="resolutionNotes"
          rows="5"
          placeholder="Describe how the crisis was resolved"
        ></textarea>
        <small class="p-error" *ngIf="resolveForm.get('resolutionNotes')?.invalid && resolveForm.get('resolutionNotes')?.touched">
          Resolution notes are required
        </small>
      </div>
    </div>
  </form>

  <ng-template pTemplate="footer">
    <button
      pButton
      label="Cancel"
      icon="pi pi-times"
      class="p-button-outlined"
      (click)="showResolveDialog.set(false)"
    ></button>
    <button
      pButton
      label="Resolve Crisis"
      icon="pi pi-check"
      class="p-button-success"
      (click)="resolveCrisis()"
      [disabled]="resolveForm.invalid"
    ></button>
  </ng-template>
</p-dialog>
```

---

## File 8: crisis-management-page.css

**Location**: `/src/app/pages/crisis-management-page/crisis-management-page.css`

```css
.crisis-management-page {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

/* Page Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.header-content h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 0.5rem 0;
}

.subtitle {
  color: #64748b;
  font-size: 0.95rem;
  margin: 0;
}

/* Statistics Cards */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: box-shadow 0.2s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 3rem;
  height: 3rem;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
}

.stat-icon.total {
  background: #fef3c7;
  color: #f59e0b;
}

.stat-icon.active {
  background: #fee2e2;
  color: #ef4444;
}

.stat-icon.in-response {
  background: #fef3c7;
  color: #f97316;
}

.stat-icon.critical {
  background: #fecaca;
  color: #dc2626;
}

.stat-icon.resolved {
  background: #d1fae5;
  color: #10b981;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
}

.stat-label {
  font-size: 0.875rem;
  color: #64748b;
  margin-top: 0.25rem;
}

/* Filters Section */
.filters-section {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  align-items: center;
}

.search-box {
  position: relative;
  flex: 1;
  min-width: 250px;
}

.search-box i {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
}

.search-box input {
  width: 100%;
  padding-left: 2.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  height: 2.75rem;
}

.filter-dropdown {
  min-width: 180px;
}

/* Crisis Grid */
.crisis-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 1.5rem;
}

.crisis-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.2s;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  border: 1px solid #f1f5f9;
}

.crisis-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* Crisis Header */
.crisis-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.crisis-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 0.5rem 0;
  line-height: 1.4;
}

.crisis-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  font-size: 0.875rem;
  color: #64748b;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.375rem;
}

.meta-item i {
  font-size: 0.75rem;
}

.crisis-badges {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: flex-end;
}

/* Crisis Type */
.crisis-type {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: #f8fafc;
  border-radius: 8px;
  font-size: 0.875rem;
  color: #475569;
  font-weight: 500;
}

.crisis-type i {
  color: #f59e0b;
}

/* Crisis Description */
.crisis-description {
  color: #475569;
  font-size: 0.9375rem;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Crisis Info */
.crisis-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 8px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.875rem;
}

.info-label {
  color: #64748b;
  font-weight: 500;
}

.info-value {
  color: #1e293b;
  font-weight: 600;
}

.info-value.badge {
  background: #3b82f6;
  color: white;
  padding: 0.125rem 0.5rem;
  border-radius: 12px;
  font-size: 0.8125rem;
}

/* Crisis Indicators */
.crisis-indicators {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.indicator {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.375rem 0.75rem;
  background: #dbeafe;
  color: #1e40af;
  border-radius: 16px;
  font-size: 0.8125rem;
  font-weight: 500;
}

.indicator i {
  font-size: 0.75rem;
}

/* Crisis Actions */
.crisis-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  padding-top: 0.5rem;
  border-top: 1px solid #f1f5f9;
}

/* Empty and Loading States */
.empty-state,
.loading-state {
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.empty-icon {
  font-size: 4rem;
  color: #cbd5e1;
  margin-bottom: 1rem;
}

.empty-state h3,
.loading-state p {
  color: #64748b;
  font-weight: 500;
  margin: 0.5rem 0;
}

.empty-state p {
  color: #94a3b8;
  font-size: 0.9375rem;
}

.loading-state i {
  font-size: 3rem;
  color: #3b82f6;
  margin-bottom: 1rem;
}

/* Dialog Forms */
.crisis-form,
.add-member-form,
.mobilize-form,
.resolve-form {
  margin-top: 1rem;
}

.p-field {
  margin-bottom: 1.5rem;
}

.p-field label {
  display: block;
  margin-bottom: 0.5rem;
  color: #475569;
  font-weight: 500;
  font-size: 0.9375rem;
}

.p-field-checkbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.p-field-checkbox label {
  margin-bottom: 0;
  cursor: pointer;
}

/* View Dialog */
.view-dialog-content {
  padding: 1rem 0;
}

.view-section {
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #f1f5f9;
}

.view-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.view-section h3 {
  margin: 0 0 1rem 0;
  color: #1e293b;
  font-size: 1.25rem;
}

.view-badges {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.view-field {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
  font-size: 0.9375rem;
}

.view-field label {
  color: #64748b;
  font-weight: 500;
  margin-bottom: 0;
}

.view-field span {
  color: #1e293b;
  font-weight: 600;
  text-align: right;
  max-width: 60%;
}

.view-section > label {
  display: block;
  margin-bottom: 0.75rem;
  color: #64748b;
  font-weight: 500;
  font-size: 0.9375rem;
}

.view-section p {
  color: #475569;
  line-height: 1.6;
  margin: 0;
}

.affected-members-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.affected-members-list li {
  padding: 0.5rem 0;
  border-bottom: 1px solid #f1f5f9;
  color: #1e293b;
}

.affected-members-list li:last-child {
  border-bottom: none;
}

.primary-contact {
  color: #3b82f6;
  font-weight: 600;
  font-size: 0.875rem;
  margin-left: 0.5rem;
}

/* Affected Members Dialog */
.affected-members-dialog h4 {
  color: #1e293b;
  font-size: 1rem;
  margin: 0 0 1rem 0;
}

.affected-members-list {
  max-height: 300px;
  overflow-y: auto;
}

.affected-member-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 8px;
  margin-bottom: 0.75rem;
}

.member-info {
  flex: 1;
}

.member-name {
  font-weight: 600;
  color: #1e293b;
  display: block;
  margin-bottom: 0.25rem;
}

.primary-badge {
  display: inline-block;
  background: #dbeafe;
  color: #1e40af;
  padding: 0.125rem 0.5rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  margin-left: 0.5rem;
}

.member-notes {
  color: #64748b;
  font-size: 0.875rem;
  margin: 0.5rem 0 0 0;
  line-height: 1.5;
}

.empty-members {
  padding: 2rem;
  text-align: center;
  color: #94a3b8;
  background: #f8fafc;
  border-radius: 8px;
}

.section-divider {
  border: none;
  border-top: 2px solid #f1f5f9;
  margin: 1.5rem 0;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .crisis-grid {
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  }
}

@media (max-width: 768px) {
  .crisis-management-page {
    padding: 1rem;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 1rem;
  }

  .filters-section {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    min-width: 100%;
  }

  .filter-dropdown {
    width: 100%;
  }

  .crisis-grid {
    grid-template-columns: 1fr;
  }

  .crisis-header {
    flex-direction: column;
  }

  .crisis-badges {
    flex-direction: row;
    align-items: flex-start;
  }

  .crisis-actions {
    flex-direction: column;
  }

  .crisis-actions button {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .stat-card {
    padding: 1rem;
  }

  .stat-value {
    font-size: 1.5rem;
  }
}
```

---

## File 9: app.routes.ts Update

**Location**: `/src/app/app.routes.ts`

Add this route to your existing routes array:

```typescript
{
  path: 'crisis-management',
  loadComponent: () =>
    import('./pages/crisis-management-page/crisis-management-page').then(m => m.CrisisManagementPage),
  canActivate: [authGuard]
}
```

---

## File 10: side-nav.html Update

**Location**: `/src/app/shared/components/side-nav/side-nav.html`

Add this link in the Pastoral Care section (after the existing pastoral care links):

```html
<!-- Inside Pastoral Care section -->
<li class="nav-item">
  <a
    routerLink="/crisis-management"
    routerLinkActive="active"
    class="nav-link"
    (click)="closeNav()"
  >
    <i class="pi pi-exclamation-triangle"></i>
    <span>Crisis Management</span>
  </a>
</li>
```

---

## Implementation Summary

### Files Created
1. **Interfaces/Enums (4 files)**:
   - crisis-type.enum.ts (13 crisis types with labels and options)
   - crisis-severity.enum.ts (4 severity levels with colors)
   - crisis-status.enum.ts (4 status types with colors)
   - crisis.interface.ts (Crisis, CrisisRequest, CrisisStats, CrisisAffectedMember interfaces)

2. **Service (1 file)**:
   - crisis.service.ts (Complete HTTP service with 18 methods)

3. **Page Component (3 files)**:
   - crisis-management-page.ts (Component with signals, forms, CRUD operations)
   - crisis-management-page.html (Template with stats, filters, grid, dialogs)
   - crisis-management-page.css (Professional styling matching pastoral-care-page)

4. **Configuration Updates (2 updates)**:
   - app.routes.ts (Add crisis management route)
   - side-nav.html (Add navigation link)

### Features Implemented

**Statistics Dashboard**:
- Total crises
- Active crises
- In Response crises
- Critical severity crises
- Resolved crises
- Color-coded stat cards

**Filters & Search**:
- Search by title/description/location
- Filter by status (4 options)
- Filter by type (13 options)
- Filter by severity (4 options)
- Clear filters button

**Crisis Cards**:
- Severity badges (CRITICAL in red, HIGH in orange, etc.)
- Status badges with appropriate colors
- Crisis type display
- Affected members count
- Resource mobilization indicator
- Notification status indicators
- Responsive grid layout

**Dialogs**:
1. **Add/Edit Crisis**: Complete form with all fields, validation
2. **View Crisis**: Detailed view with all information
3. **Affected Members**: Add/remove members, set primary contact
4. **Mobilize Resources**: Specify resources being deployed
5. **Resolve Crisis**: Add resolution notes and mark resolved
6. **Send Notifications**: Confirm emergency notifications

**CRUD Operations**:
- Report new crisis
- Update existing crisis
- Delete crisis (with confirmation)
- View crisis details
- Add/remove affected members
- Mobilize resources
- Send emergency notifications
- Resolve crisis
- Update crisis status

**Professional Styling**:
- Matches pastoral-care-page exactly
- 12px border-radius on cards
- Proper shadows and hover effects
- Responsive grid layout
- Mobile-first design
- Professional color scheme
- Consistent spacing and typography

### Integration with Backend

All service methods map directly to backend endpoints:
- `POST /api/crises` - Report crisis
- `GET /api/crises/{id}` - Get crisis by ID
- `GET /api/crises` - Get all crises (paginated)
- `PUT /api/crises/{id}` - Update crisis
- `DELETE /api/crises/{id}` - Delete crisis
- `POST /api/crises/{id}/affected-members` - Add affected member
- `DELETE /api/crises/{id}/affected-members/{memberId}` - Remove affected member
- `POST /api/crises/{id}/mobilize` - Mobilize resources
- `POST /api/crises/{id}/notify` - Send notifications
- `POST /api/crises/{id}/resolve` - Resolve crisis
- `PATCH /api/crises/{id}/status` - Update status
- `GET /api/crises/active` - Get active crises
- `GET /api/crises/critical` - Get critical crises
- `GET /api/crises/status/{status}` - Get by status
- `GET /api/crises/type/{type}` - Get by type
- `GET /api/crises/severity/{severity}` - Get by severity
- `GET /api/crises/search` - Search crises
- `GET /api/crises/stats` - Get statistics

### Dependencies Required

Ensure these PrimeNG modules are installed:
- @primeng/card
- @primeng/button
- @primeng/dialog
- @primeng/inputtext
- @primeng/inputtextarea
- @primeng/dropdown
- @primeng/calendar
- @primeng/checkbox
- @primeng/tag
- @primeng/multiselect
- @primeng/confirmdialog
- @primeng/toast

---

## Next Steps

1. Copy all file contents to your Angular frontend project
2. Ensure PrimeNG dependencies are installed
3. Update app.routes.ts with the crisis management route
4. Update side-nav.html with the navigation link
5. Build the application: `ng build`
6. Test all CRUD operations
7. Verify mobile responsiveness
8. Run E2E tests

---

**Status**: Complete Frontend Implementation
**Date**: December 26, 2025
**Phase**: Phase 4 - Crisis & Emergency Management Frontend
