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

import { PrayerRequestService } from '../../services/prayer-request.service';
import {
  PrayerRequestResponse,
  PrayerRequestStatsResponse,
  MarkAsAnsweredRequest
} from '../../models/prayer-request.interface';
import { PrayerCategory, PrayerCategoryLabels } from '../../models/prayer-category.enum';
import { PrayerPriority, PrayerPriorityLabels } from '../../models/prayer-priority.enum';
import { PrayerRequestStatus, PrayerRequestStatusLabels } from '../../models/prayer-request-status.enum';

@Component({
  selector: 'app-prayer-requests-page',
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
  templateUrl: './prayer-requests-page.html',
  styleUrls: ['./prayer-requests-page.css']
})
export class PrayerRequestsPageComponent implements OnInit {
  // Signals for reactive state
  prayerRequests = signal<PrayerRequestResponse[]>([]);
  stats = signal<PrayerRequestStatsResponse | null>(null);
  loading = signal(false);

  // Dialog visibility
  showAddDialog = signal(false);
  showEditDialog = signal(false);
  showViewDialog = signal(false);
  showAnswerDialog = signal(false);

  // Selected prayer request
  selectedPrayerRequest = signal<PrayerRequestResponse | null>(null);

  // Forms
  prayerRequestForm!: FormGroup;
  answerForm!: FormGroup;

  // Filters
  searchTerm = signal('');
  selectedStatus = signal<PrayerRequestStatus | null>(null);
  selectedCategory = signal<PrayerCategory | null>(null);
  selectedPriority = signal<PrayerPriority | null>(null);
  showOnlyUrgent = signal(false);
  showOnlyPublic = signal(false);

  // Dropdowns
  categoryOptions = Object.values(PrayerCategory).map(category => ({
    label: PrayerCategoryLabels[category],
    value: category
  }));

  priorityOptions = Object.values(PrayerPriority).map(priority => ({
    label: PrayerPriorityLabels[priority],
    value: priority
  }));

  statusOptions = Object.values(PrayerRequestStatus).map(status => ({
    label: PrayerRequestStatusLabels[status],
    value: status
  }));

  // Computed filtered prayer requests
  filteredPrayerRequests = computed(() => {
    let filtered = this.prayerRequests();

    const search = this.searchTerm().toLowerCase();
    if (search) {
      filtered = filtered.filter(request =>
        request.title.toLowerCase().includes(search) ||
        (request.description && request.description.toLowerCase().includes(search)) ||
        request.memberName.toLowerCase().includes(search) ||
        (request.tags && request.tags.toLowerCase().includes(search))
      );
    }

    const status = this.selectedStatus();
    if (status) {
      filtered = filtered.filter(request => request.status === status);
    }

    const category = this.selectedCategory();
    if (category) {
      filtered = filtered.filter(request => request.category === category);
    }

    const priority = this.selectedPriority();
    if (priority) {
      filtered = filtered.filter(request => request.priority === priority);
    }

    if (this.showOnlyUrgent()) {
      filtered = filtered.filter(request => request.isUrgent);
    }

    if (this.showOnlyPublic()) {
      filtered = filtered.filter(request => request.isPublic);
    }

    return filtered;
  });

  // Enums for template
  PrayerCategory = PrayerCategory;
  PrayerPriority = PrayerPriority;
  PrayerRequestStatus = PrayerRequestStatus;
  PrayerCategoryLabels = PrayerCategoryLabels;
  PrayerPriorityLabels = PrayerPriorityLabels;
  PrayerRequestStatusLabels = PrayerRequestStatusLabels;

  constructor(
    private prayerRequestService: PrayerRequestService,
    private fb: FormBuilder,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) {
    this.initializeForms();
  }

  ngOnInit(): void {
    this.loadPrayerRequests();
    this.loadStats();
  }

  initializeForms(): void {
    this.prayerRequestForm = this.fb.group({
      memberId: [null, Validators.required],
      title: ['', Validators.required],
      description: [''],
      category: [null, Validators.required],
      priority: [PrayerPriority.NORMAL, Validators.required],
      status: [PrayerRequestStatus.PENDING, Validators.required],
      isAnonymous: [false],
      isUrgent: [false],
      expirationDate: [null],
      isPublic: [true],
      tags: ['']
    });

    this.answerForm = this.fb.group({
      testimony: ['', Validators.required]
    });
  }

  loadPrayerRequests(): void {
    this.loading.set(true);
    this.prayerRequestService.getAllPrayerRequests().subscribe({
      next: (response) => {
        this.prayerRequests.set(response.content || response);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading prayer requests:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load prayer requests'
        });
        this.loading.set(false);
      }
    });
  }

  loadStats(): void {
    this.prayerRequestService.getPrayerRequestStats().subscribe({
      next: (stats) => {
        this.stats.set(stats);
      },
      error: (error) => {
        console.error('Error loading stats:', error);
      }
    });
  }

  openAddDialog(): void {
    this.prayerRequestForm.reset({
      priority: PrayerPriority.NORMAL,
      status: PrayerRequestStatus.PENDING,
      isAnonymous: false,
      isUrgent: false,
      isPublic: true
    });
    this.showAddDialog.set(true);
  }

  openEditDialog(prayerRequest: PrayerRequestResponse): void {
    this.selectedPrayerRequest.set(prayerRequest);
    this.prayerRequestForm.patchValue({
      ...prayerRequest,
      expirationDate: prayerRequest.expirationDate ? new Date(prayerRequest.expirationDate) : null
    });
    this.showEditDialog.set(true);
  }

  openViewDialog(prayerRequest: PrayerRequestResponse): void {
    this.selectedPrayerRequest.set(prayerRequest);
    this.showViewDialog.set(true);
  }

  openAnswerDialog(prayerRequest: PrayerRequestResponse): void {
    this.selectedPrayerRequest.set(prayerRequest);
    this.answerForm.reset();
    this.showAnswerDialog.set(true);
  }

  savePrayerRequest(): void {
    if (this.prayerRequestForm.invalid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Validation Error',
        detail: 'Please fill all required fields'
      });
      return;
    }

    const formValue = this.prayerRequestForm.value;
    const request = {
      ...formValue,
      expirationDate: formValue.expirationDate ? this.formatDate(formValue.expirationDate) : null
    };

    this.prayerRequestService.createPrayerRequest(request).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Prayer request created successfully'
        });
        this.showAddDialog.set(false);
        this.loadPrayerRequests();
        this.loadStats();
      },
      error: (error) => {
        console.error('Error creating prayer request:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to create prayer request'
        });
      }
    });
  }

  updatePrayerRequest(): void {
    if (this.prayerRequestForm.invalid || !this.selectedPrayerRequest()) {
      return;
    }

    const formValue = this.prayerRequestForm.value;
    const request = {
      ...formValue,
      expirationDate: formValue.expirationDate ? this.formatDate(formValue.expirationDate) : null
    };

    this.prayerRequestService.updatePrayerRequest(this.selectedPrayerRequest()!.id, request).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Prayer request updated successfully'
        });
        this.showEditDialog.set(false);
        this.loadPrayerRequests();
        this.loadStats();
      },
      error: (error) => {
        console.error('Error updating prayer request:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to update prayer request'
        });
      }
    });
  }

  deletePrayerRequest(prayerRequest: PrayerRequestResponse): void {
    this.confirmationService.confirm({
      message: `Are you sure you want to delete the prayer request "${prayerRequest.title}"?`,
      header: 'Confirm Delete',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.prayerRequestService.deletePrayerRequest(prayerRequest.id).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Success',
              detail: 'Prayer request deleted successfully'
            });
            this.loadPrayerRequests();
            this.loadStats();
          },
          error: (error) => {
            console.error('Error deleting prayer request:', error);
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to delete prayer request'
            });
          }
        });
      }
    });
  }

  incrementPrayerCount(prayerRequest: PrayerRequestResponse): void {
    this.prayerRequestService.incrementPrayerCount(prayerRequest.id).subscribe({
      next: (updated) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Prayer Recorded',
          detail: 'Thank you for praying!'
        });
        // Update the prayer request in the list
        const requests = this.prayerRequests();
        const index = requests.findIndex(r => r.id === updated.id);
        if (index !== -1) {
          requests[index] = updated;
          this.prayerRequests.set([...requests]);
        }
        this.loadStats();
      },
      error: (error) => {
        console.error('Error incrementing prayer count:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to record prayer'
        });
      }
    });
  }

  markAsAnswered(): void {
    if (this.answerForm.invalid || !this.selectedPrayerRequest()) {
      return;
    }

    const testimony = this.answerForm.value.testimony;

    this.prayerRequestService.markAsAnswered(this.selectedPrayerRequest()!.id, testimony).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Prayer request marked as answered'
        });
        this.showAnswerDialog.set(false);
        this.loadPrayerRequests();
        this.loadStats();
      },
      error: (error) => {
        console.error('Error marking as answered:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to mark prayer request as answered'
        });
      }
    });
  }

  archivePrayerRequest(prayerRequest: PrayerRequestResponse): void {
    this.confirmationService.confirm({
      message: `Are you sure you want to archive the prayer request "${prayerRequest.title}"?`,
      header: 'Confirm Archive',
      icon: 'pi pi-info-circle',
      accept: () => {
        this.prayerRequestService.archivePrayerRequest(prayerRequest.id).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Success',
              detail: 'Prayer request archived successfully'
            });
            this.loadPrayerRequests();
            this.loadStats();
          },
          error: (error) => {
            console.error('Error archiving prayer request:', error);
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to archive prayer request'
            });
          }
        });
      }
    });
  }

  clearFilters(): void {
    this.searchTerm.set('');
    this.selectedStatus.set(null);
    this.selectedCategory.set(null);
    this.selectedPriority.set(null);
    this.showOnlyUrgent.set(false);
    this.showOnlyPublic.set(false);
  }

  getStatusClass(status: PrayerRequestStatus): string {
    const classes: Record<PrayerRequestStatus, string> = {
      [PrayerRequestStatus.PENDING]: 'status-pending',
      [PrayerRequestStatus.ACTIVE]: 'status-active',
      [PrayerRequestStatus.ANSWERED]: 'status-answered',
      [PrayerRequestStatus.ARCHIVED]: 'status-archived'
    };
    return classes[status] || '';
  }

  getPriorityClass(priority: PrayerPriority): string {
    const classes: Record<PrayerPriority, string> = {
      [PrayerPriority.LOW]: 'priority-low',
      [PrayerPriority.NORMAL]: 'priority-normal',
      [PrayerPriority.HIGH]: 'priority-high',
      [PrayerPriority.URGENT]: 'priority-urgent'
    };
    return classes[priority] || '';
  }

  getCategoryClass(category: PrayerCategory): string {
    return 'category-badge';
  }

  getDaysUntilExpiration(expirationDate: string | undefined): number {
    if (!expirationDate) return -1;
    const today = new Date();
    const expiration = new Date(expirationDate);
    const diffTime = expiration.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  }

  isExpiringSoon(prayerRequest: PrayerRequestResponse): boolean {
    if (!prayerRequest.expirationDate) return false;
    const daysLeft = this.getDaysUntilExpiration(prayerRequest.expirationDate);
    return daysLeft >= 0 && daysLeft <= 7;
  }

  getExpirationText(prayerRequest: PrayerRequestResponse): string {
    if (!prayerRequest.expirationDate) return '';
    const daysLeft = this.getDaysUntilExpiration(prayerRequest.expirationDate);
    if (daysLeft < 0) return 'Expired';
    if (daysLeft === 0) return 'Expires today';
    if (daysLeft === 1) return 'Expires tomorrow';
    return `Expires in ${daysLeft} days`;
  }

  private formatDate(date: Date | string | null): string {
    if (!date) return '';
    const d = new Date(date);
    return d.toISOString().split('T')[0];
  }
}
