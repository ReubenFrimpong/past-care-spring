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
import { TableModule } from 'primeng/table';
import { MessageService, ConfirmationService } from 'primeng/api';

import { CrisisService } from '../../services/crisis.service';
import {
  CrisisResponse,
  CrisisStatsResponse,
  CrisisAffectedMemberRequest,
  CrisisAffectedMemberResponse
} from '../../models/crisis.interface';
import { CrisisType, CrisisTypeLabels } from '../../models/crisis-type.enum';
import { CrisisStatus, CrisisStatusLabels } from '../../models/crisis-status.enum';
import { CrisisSeverity, CrisisSeverityLabels } from '../../models/crisis-severity.enum';

@Component({
  selector: 'app-crises-page',
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
    ConfirmDialogModule,
    TableModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './crises-page.html',
  styleUrls: ['./crises-page.css']
})
export class CrisesPageComponent implements OnInit {
  // Signals for reactive state
  crises = signal<CrisisResponse[]>([]);
  stats = signal<CrisisStatsResponse | null>(null);
  loading = signal(false);

  // Dialog visibility
  showAddDialog = signal(false);
  showEditDialog = signal(false);
  showViewDialog = signal(false);
  showMobilizeDialog = signal(false);
  showResolveDialog = signal(false);
  showAddMemberDialog = signal(false);
  showManageMembersDialog = signal(false);
  showUpdateStatusDialog = signal(false);

  // Selected crisis
  selectedCrisis = signal<CrisisResponse | null>(null);

  // Forms
  crisisForm!: FormGroup;
  mobilizeForm!: FormGroup;
  resolveForm!: FormGroup;
  addMemberForm!: FormGroup;
  updateStatusForm!: FormGroup;

  // Filters
  searchTerm = signal('');
  selectedStatus = signal<CrisisStatus | null>(null);
  selectedSeverity = signal<CrisisSeverity | null>(null);
  selectedType = signal<CrisisType | null>(null);
  showOnlyActive = signal(false);
  showOnlyCritical = signal(false);

  // Dropdowns
  typeOptions = Object.values(CrisisType).map(type => ({
    label: CrisisTypeLabels[type],
    value: type
  }));

  statusOptions = Object.values(CrisisStatus).map(status => ({
    label: CrisisStatusLabels[status],
    value: status
  }));

  severityOptions = Object.values(CrisisSeverity).map(severity => ({
    label: CrisisSeverityLabels[severity],
    value: severity
  }));

  // Computed filtered crises
  filteredCrises = computed(() => {
    let filtered = this.crises();

    const search = this.searchTerm().toLowerCase();
    if (search) {
      filtered = filtered.filter(crisis =>
        crisis.title.toLowerCase().includes(search) ||
        (crisis.description && crisis.description.toLowerCase().includes(search)) ||
        (crisis.location && crisis.location.toLowerCase().includes(search)) ||
        crisis.reportedByName.toLowerCase().includes(search)
      );
    }

    const status = this.selectedStatus();
    if (status) {
      filtered = filtered.filter(crisis => crisis.status === status);
    }

    const severity = this.selectedSeverity();
    if (severity) {
      filtered = filtered.filter(crisis => crisis.severity === severity);
    }

    const type = this.selectedType();
    if (type) {
      filtered = filtered.filter(crisis => crisis.crisisType === type);
    }

    if (this.showOnlyActive()) {
      filtered = filtered.filter(crisis => crisis.isActive);
    }

    if (this.showOnlyCritical()) {
      filtered = filtered.filter(crisis => crisis.isCritical);
    }

    return filtered;
  });

  // Enums for template
  CrisisType = CrisisType;
  CrisisStatus = CrisisStatus;
  CrisisSeverity = CrisisSeverity;
  CrisisTypeLabels = CrisisTypeLabels;
  CrisisStatusLabels = CrisisStatusLabels;
  CrisisSeverityLabels = CrisisSeverityLabels;

  constructor(
    private crisisService: CrisisService,
    private fb: FormBuilder,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) {
    this.initializeForms();
  }

  ngOnInit(): void {
    this.loadCrises();
    this.loadStats();
  }

  initializeForms(): void {
    this.crisisForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      crisisType: [null, Validators.required],
      severity: [null, Validators.required],
      status: [CrisisStatus.ACTIVE, Validators.required],
      incidentDate: [null],
      location: [''],
      affectedMembersCount: [0],
      responseTeamNotes: [''],
      resolutionNotes: [''],
      followUpRequired: [false],
      followUpDate: [null],
      resourcesMobilized: [''],
      communicationSent: [false],
      emergencyContactNotified: [false]
    });

    this.mobilizeForm = this.fb.group({
      resources: ['', Validators.required]
    });

    this.resolveForm = this.fb.group({
      resolutionNotes: ['', Validators.required]
    });

    this.addMemberForm = this.fb.group({
      memberId: [null, Validators.required],
      notes: [''],
      isPrimaryContact: [false]
    });

    this.updateStatusForm = this.fb.group({
      status: [null, Validators.required]
    });
  }

  loadCrises(): void {
    this.loading.set(true);
    this.crisisService.getAllCrises().subscribe({
      next: (response) => {
        this.crises.set(response.content || response);
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

  openAddDialog(): void {
    this.crisisForm.reset({
      status: CrisisStatus.ACTIVE,
      followUpRequired: false,
      communicationSent: false,
      emergencyContactNotified: false,
      affectedMembersCount: 0
    });
    this.showAddDialog.set(true);
  }

  openEditDialog(crisis: CrisisResponse): void {
    this.selectedCrisis.set(crisis);
    this.crisisForm.patchValue({
      ...crisis,
      incidentDate: crisis.incidentDate ? new Date(crisis.incidentDate) : null,
      followUpDate: crisis.followUpDate ? new Date(crisis.followUpDate) : null
    });
    this.showEditDialog.set(true);
  }

  openViewDialog(crisis: CrisisResponse): void {
    this.selectedCrisis.set(crisis);
    this.showViewDialog.set(true);
  }

  openMobilizeDialog(crisis: CrisisResponse): void {
    this.selectedCrisis.set(crisis);
    this.mobilizeForm.reset({
      resources: crisis.resourcesMobilized || ''
    });
    this.showMobilizeDialog.set(true);
  }

  openResolveDialog(crisis: CrisisResponse): void {
    this.selectedCrisis.set(crisis);
    this.resolveForm.reset();
    this.showResolveDialog.set(true);
  }

  openAddMemberDialog(crisis: CrisisResponse): void {
    this.selectedCrisis.set(crisis);
    this.addMemberForm.reset({
      isPrimaryContact: false
    });
    this.showAddMemberDialog.set(true);
  }

  openManageMembersDialog(crisis: CrisisResponse): void {
    this.selectedCrisis.set(crisis);
    this.showManageMembersDialog.set(true);
  }

  openUpdateStatusDialog(crisis: CrisisResponse): void {
    this.selectedCrisis.set(crisis);
    this.updateStatusForm.patchValue({
      status: crisis.status
    });
    this.showUpdateStatusDialog.set(true);
  }

  reportCrisis(): void {
    if (this.crisisForm.invalid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Validation Error',
        detail: 'Please fill all required fields'
      });
      return;
    }

    const formValue = this.crisisForm.value;
    const request = {
      ...formValue,
      incidentDate: formValue.incidentDate ? this.formatDateTime(formValue.incidentDate) : null,
      followUpDate: formValue.followUpDate ? this.formatDate(formValue.followUpDate) : null
    };

    this.crisisService.reportCrisis(request).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Crisis reported successfully'
        });
        this.showAddDialog.set(false);
        this.loadCrises();
        this.loadStats();
      },
      error: (error) => {
        console.error('Error reporting crisis:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to report crisis'
        });
      }
    });
  }

  updateCrisis(): void {
    if (this.crisisForm.invalid || !this.selectedCrisis()) {
      return;
    }

    const formValue = this.crisisForm.value;
    const request = {
      ...formValue,
      incidentDate: formValue.incidentDate ? this.formatDateTime(formValue.incidentDate) : null,
      followUpDate: formValue.followUpDate ? this.formatDate(formValue.followUpDate) : null
    };

    this.crisisService.updateCrisis(this.selectedCrisis()!.id, request).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Crisis updated successfully'
        });
        this.showEditDialog.set(false);
        this.loadCrises();
        this.loadStats();
      },
      error: (error) => {
        console.error('Error updating crisis:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to update crisis'
        });
      }
    });
  }

  deleteCrisis(crisis: CrisisResponse): void {
    this.confirmationService.confirm({
      message: `Are you sure you want to delete crisis "${crisis.title}"?`,
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

  mobilizeResources(): void {
    if (this.mobilizeForm.invalid || !this.selectedCrisis()) {
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

  sendNotifications(crisis: CrisisResponse): void {
    this.confirmationService.confirm({
      message: `Send emergency notifications for crisis "${crisis.title}"?`,
      header: 'Confirm Notification',
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
            this.loadStats();
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

  resolveCrisis(): void {
    if (this.resolveForm.invalid || !this.selectedCrisis()) {
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

  updateStatus(): void {
    if (this.updateStatusForm.invalid || !this.selectedCrisis()) {
      return;
    }

    const status = this.updateStatusForm.value.status;
    this.crisisService.updateStatus(this.selectedCrisis()!.id, status).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Status updated successfully'
        });
        this.showUpdateStatusDialog.set(false);
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

  addAffectedMember(): void {
    if (this.addMemberForm.invalid || !this.selectedCrisis()) {
      return;
    }

    const request: CrisisAffectedMemberRequest = {
      memberId: this.addMemberForm.value.memberId,
      notes: this.addMemberForm.value.notes,
      isPrimaryContact: this.addMemberForm.value.isPrimaryContact
    };

    this.crisisService.addAffectedMember(this.selectedCrisis()!.id, request).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Affected member added successfully'
        });
        this.showAddMemberDialog.set(false);
        this.loadCrises();
        this.loadStats();
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
    if (!this.selectedCrisis()) {
      return;
    }

    this.confirmationService.confirm({
      message: 'Are you sure you want to remove this affected member?',
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
            this.loadStats();
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

  clearFilters(): void {
    this.searchTerm.set('');
    this.selectedStatus.set(null);
    this.selectedSeverity.set(null);
    this.selectedType.set(null);
    this.showOnlyActive.set(false);
    this.showOnlyCritical.set(false);
  }

  getSeverityClass(severity: CrisisSeverity): string {
    const classes: Record<CrisisSeverity, string> = {
      [CrisisSeverity.CRITICAL]: 'severity-critical',
      [CrisisSeverity.HIGH]: 'severity-high',
      [CrisisSeverity.MODERATE]: 'severity-moderate',
      [CrisisSeverity.LOW]: 'severity-low'
    };
    return classes[severity] || '';
  }

  getStatusClass(status: CrisisStatus): string {
    const classes: Record<CrisisStatus, string> = {
      [CrisisStatus.ACTIVE]: 'status-active',
      [CrisisStatus.IN_RESPONSE]: 'status-in-response',
      [CrisisStatus.RESOLVED]: 'status-resolved',
      [CrisisStatus.CLOSED]: 'status-closed'
    };
    return classes[status] || '';
  }

  private formatDate(date: Date | string | null): string {
    if (!date) return '';
    const d = new Date(date);
    return d.toISOString().split('T')[0];
  }

  private formatDateTime(date: Date | string | null): string {
    if (!date) return '';
    const d = new Date(date);
    return d.toISOString();
  }
}
