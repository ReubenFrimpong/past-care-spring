import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { CalendarModule } from 'primeng/calendar';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { CheckboxModule } from 'primeng/checkbox';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { MessageService, ConfirmationService } from 'primeng/api';

import { CounselingSessionService } from '../../services/counseling-session.service';
import {
  CounselingSessionResponse,
  CounselingSessionStatsResponse,
  CompleteSessionRequest,
  ScheduleFollowUpRequest,
  CreateReferralRequest
} from '../../models/counseling-session.interface';
import { CounselingType, CounselingTypeLabels } from '../../models/counseling-type.enum';
import { CounselingStatus, CounselingStatusLabels } from '../../models/counseling-status.enum';
import { SessionOutcome, SessionOutcomeLabels } from '../../models/session-outcome.enum';

@Component({
  selector: 'app-counseling-sessions-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    CardModule,
    ButtonModule,
    DialogModule,
    DropdownModule,
    CalendarModule,
    InputTextModule,
    InputTextareaModule,
    CheckboxModule,
    ToastModule,
    ConfirmDialogModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './counseling-sessions-page.html',
  styleUrls: ['./counseling-sessions-page.css']
})
export class CounselingSessionsPageComponent implements OnInit {
  // Signals for reactive state
  sessions = signal<CounselingSessionResponse[]>([]);
  stats = signal<CounselingSessionStatsResponse | null>(null);
  loading = signal(false);

  // Dialog visibility
  showAddDialog = signal(false);
  showEditDialog = signal(false);
  showViewDialog = signal(false);
  showCompleteDialog = signal(false);
  showFollowUpDialog = signal(false);
  showReferralDialog = signal(false);

  // Selected session
  selectedSession = signal<CounselingSessionResponse | null>(null);

  // Forms
  sessionForm!: FormGroup;
  completeForm!: FormGroup;
  followUpForm!: FormGroup;
  referralForm!: FormGroup;

  // Filters
  searchTerm = signal('');
  selectedStatus = signal<CounselingStatus | null>(null);
  selectedType = signal<CounselingType | null>(null);

  // Dropdowns
  typeOptions = Object.values(CounselingType).map(type => ({
    label: CounselingTypeLabels[type],
    value: type
  }));

  statusOptions = Object.values(CounselingStatus).map(status => ({
    label: CounselingStatusLabels[status],
    value: status
  }));

  outcomeOptions = Object.values(SessionOutcome).map(outcome => ({
    label: SessionOutcomeLabels[outcome],
    value: outcome
  }));

  // Computed filtered sessions
  filteredSessions = computed(() => {
    let filtered = this.sessions();

    const search = this.searchTerm().toLowerCase();
    if (search) {
      filtered = filtered.filter(session =>
        session.memberName.toLowerCase().includes(search) ||
        session.counselorName.toLowerCase().includes(search) ||
        (session.purpose && session.purpose.toLowerCase().includes(search))
      );
    }

    const status = this.selectedStatus();
    if (status) {
      filtered = filtered.filter(session => session.status === status);
    }

    const type = this.selectedType();
    if (type) {
      filtered = filtered.filter(session => session.type === type);
    }

    return filtered;
  });

  // Enums for template
  CounselingType = CounselingType;
  CounselingStatus = CounselingStatus;
  SessionOutcome = SessionOutcome;
  CounselingTypeLabels = CounselingTypeLabels;
  CounselingStatusLabels = CounselingStatusLabels;
  SessionOutcomeLabels = SessionOutcomeLabels;

  constructor(
    private counselingService: CounselingSessionService,
    private fb: FormBuilder,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) {
    this.initializeForms();
  }

  ngOnInit(): void {
    this.loadSessions();
    this.loadStats();
  }

  initializeForms(): void {
    this.sessionForm = this.fb.group({
      memberId: [null, Validators.required],
      counselorId: [null, Validators.required],
      careNeedId: [null],
      sessionDate: [null, Validators.required],
      startTime: [null],
      endTime: [null],
      type: [null, Validators.required],
      status: [CounselingStatus.SCHEDULED, Validators.required],
      purpose: [''],
      notes: [''],
      outcomes: [''],
      outcome: [null],
      isConfidential: [false],
      requiresFollowUp: [false],
      followUpDate: [null],
      referredToProfessional: [false],
      referralDetails: ['']
    });

    this.completeForm = this.fb.group({
      outcome: [null, Validators.required],
      outcomes: [''],
      requiresFollowUp: [false],
      followUpDate: [null]
    });

    this.followUpForm = this.fb.group({
      followUpDate: [null, Validators.required],
      notes: ['']
    });

    this.referralForm = this.fb.group({
      referralDetails: ['', Validators.required]
    });
  }

  loadSessions(): void {
    this.loading.set(true);
    this.counselingService.getAllSessions().subscribe({
      next: (response) => {
        this.sessions.set(response.content || response);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading sessions:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load counseling sessions'
        });
        this.loading.set(false);
      }
    });
  }

  loadStats(): void {
    this.counselingService.getSessionStats().subscribe({
      next: (stats) => {
        this.stats.set(stats);
      },
      error: (error) => {
        console.error('Error loading stats:', error);
      }
    });
  }

  openAddDialog(): void {
    this.sessionForm.reset({
      status: CounselingStatus.SCHEDULED,
      isConfidential: false,
      requiresFollowUp: false,
      referredToProfessional: false
    });
    this.showAddDialog.set(true);
  }

  openEditDialog(session: CounselingSessionResponse): void {
    this.selectedSession.set(session);
    this.sessionForm.patchValue({
      ...session,
      sessionDate: new Date(session.sessionDate)
    });
    this.showEditDialog.set(true);
  }

  openViewDialog(session: CounselingSessionResponse): void {
    this.selectedSession.set(session);
    this.showViewDialog.set(true);
  }

  openCompleteDialog(session: CounselingSessionResponse): void {
    this.selectedSession.set(session);
    this.completeForm.reset();
    this.showCompleteDialog.set(true);
  }

  openFollowUpDialog(session: CounselingSessionResponse): void {
    this.selectedSession.set(session);
    this.followUpForm.reset();
    this.showFollowUpDialog.set(true);
  }

  openReferralDialog(session: CounselingSessionResponse): void {
    this.selectedSession.set(session);
    this.referralForm.reset();
    this.showReferralDialog.set(true);
  }

  saveSession(): void {
    if (this.sessionForm.invalid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Validation Error',
        detail: 'Please fill all required fields'
      });
      return;
    }

    const formValue = this.sessionForm.value;
    const request = {
      ...formValue,
      sessionDate: this.formatDate(formValue.sessionDate),
      followUpDate: formValue.followUpDate ? this.formatDate(formValue.followUpDate) : null
    };

    this.counselingService.createSession(request).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Counseling session created successfully'
        });
        this.showAddDialog.set(false);
        this.loadSessions();
        this.loadStats();
      },
      error: (error) => {
        console.error('Error creating session:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to create counseling session'
        });
      }
    });
  }

  updateSession(): void {
    if (this.sessionForm.invalid || !this.selectedSession()) {
      return;
    }

    const formValue = this.sessionForm.value;
    const request = {
      ...formValue,
      sessionDate: this.formatDate(formValue.sessionDate),
      followUpDate: formValue.followUpDate ? this.formatDate(formValue.followUpDate) : null
    };

    this.counselingService.updateSession(this.selectedSession()!.id, request).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Counseling session updated successfully'
        });
        this.showEditDialog.set(false);
        this.loadSessions();
        this.loadStats();
      },
      error: (error) => {
        console.error('Error updating session:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to update counseling session'
        });
      }
    });
  }

  completeSession(): void {
    if (this.completeForm.invalid || !this.selectedSession()) {
      return;
    }

    const formValue = this.completeForm.value;
    const request: CompleteSessionRequest = {
      outcome: formValue.outcome,
      outcomes: formValue.outcomes,
      requiresFollowUp: formValue.requiresFollowUp,
      followUpDate: formValue.followUpDate ? this.formatDate(formValue.followUpDate) : undefined
    };

    this.counselingService.completeSession(this.selectedSession()!.id, request).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Session completed successfully'
        });
        this.showCompleteDialog.set(false);
        this.loadSessions();
        this.loadStats();
      },
      error: (error) => {
        console.error('Error completing session:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to complete session'
        });
      }
    });
  }

  scheduleFollowUp(): void {
    if (this.followUpForm.invalid || !this.selectedSession()) {
      return;
    }

    const formValue = this.followUpForm.value;
    const request: ScheduleFollowUpRequest = {
      followUpDate: this.formatDate(formValue.followUpDate),
      notes: formValue.notes
    };

    this.counselingService.scheduleFollowUp(this.selectedSession()!.id, request).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Follow-up scheduled successfully'
        });
        this.showFollowUpDialog.set(false);
        this.loadSessions();
        this.loadStats();
      },
      error: (error) => {
        console.error('Error scheduling follow-up:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to schedule follow-up'
        });
      }
    });
  }

  createReferral(): void {
    if (this.referralForm.invalid || !this.selectedSession()) {
      return;
    }

    const request: CreateReferralRequest = {
      referralDetails: this.referralForm.value.referralDetails
    };

    this.counselingService.createReferral(this.selectedSession()!.id, request).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Referral created successfully'
        });
        this.showReferralDialog.set(false);
        this.loadSessions();
        this.loadStats();
      },
      error: (error) => {
        console.error('Error creating referral:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to create referral'
        });
      }
    });
  }

  deleteSession(session: CounselingSessionResponse): void {
    this.confirmationService.confirm({
      message: `Are you sure you want to delete this counseling session with ${session.memberName}?`,
      header: 'Confirm Delete',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.counselingService.deleteSession(session.id).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Success',
              detail: 'Counseling session deleted successfully'
            });
            this.loadSessions();
            this.loadStats();
          },
          error: (error) => {
            console.error('Error deleting session:', error);
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to delete counseling session'
            });
          }
        });
      }
    });
  }

  clearFilters(): void {
    this.searchTerm.set('');
    this.selectedStatus.set(null);
    this.selectedType.set(null);
  }

  getStatusClass(status: CounselingStatus): string {
    const classes: Record<CounselingStatus, string> = {
      [CounselingStatus.SCHEDULED]: 'status-scheduled',
      [CounselingStatus.IN_PROGRESS]: 'status-in-progress',
      [CounselingStatus.COMPLETED]: 'status-completed',
      [CounselingStatus.CANCELLED]: 'status-cancelled',
      [CounselingStatus.NO_SHOW]: 'status-no-show',
      [CounselingStatus.RESCHEDULED]: 'status-rescheduled'
    };
    return classes[status] || '';
  }

  getOutcomeClass(outcome: SessionOutcome): string {
    const classes: Record<SessionOutcome, string> = {
      [SessionOutcome.POSITIVE]: 'outcome-positive',
      [SessionOutcome.NEUTRAL]: 'outcome-neutral',
      [SessionOutcome.CHALLENGING]: 'outcome-challenging',
      [SessionOutcome.NEEDS_FOLLOWUP]: 'outcome-needs-followup',
      [SessionOutcome.NEEDS_REFERRAL]: 'outcome-needs-referral',
      [SessionOutcome.RESOLVED]: 'outcome-resolved',
      [SessionOutcome.ONGOING]: 'outcome-ongoing'
    };
    return classes[outcome] || '';
  }

  private formatDate(date: Date | string | null): string {
    if (!date) return '';
    const d = new Date(date);
    return d.toISOString().split('T')[0];
  }
}
