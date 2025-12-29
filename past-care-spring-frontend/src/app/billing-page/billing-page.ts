import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BillingService } from '../services/billing.service';
import { StorageUsageService } from '../services/storage-usage.service';
import { UsersService } from '../services/users.service';
import {
  ChurchSubscription,
  getStatusBadgeClass,
  getStatusDisplayText,
} from '../models/church-subscription.interface';
import {
  SubscriptionPlan,
  formatStorageLimit,
  hasUnlimitedUsers,
} from '../models/subscription-plan.interface';
import {
  Payment,
  getPaymentStatusBadgeClass,
  formatPaymentAmount,
} from '../models/payment.interface';

/**
 * Billing and subscription management page.
 */
@Component({
  selector: 'app-billing-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './billing-page.html',
  styleUrls: ['./billing-page.css'],
})
export class BillingPage implements OnInit {
  // Current subscription
  subscription = signal<ChurchSubscription | null>(null);

  // Available plans
  availablePlans = signal<SubscriptionPlan[]>([]);

  // Payment history
  payments = signal<Payment[]>([]);

  // Storage and user usage
  storageUsageMb = signal<number>(0);
  userCount = signal<number>(0);

  // Loading states
  isLoadingSubscription = signal<boolean>(true);
  isLoadingPlans = signal<boolean>(true);
  isLoadingPayments = signal<boolean>(true);
  isProcessing = signal<boolean>(false);

  // Dialogs
  showCancelDialog = signal<boolean>(false);
  showDowngradeDialog = signal<boolean>(false);
  selectedPlanForUpgrade = signal<SubscriptionPlan | null>(null);

  // Messages
  successMessage = signal<string | null>(null);
  errorMessage = signal<string | null>(null);

  // Computed properties
  daysRemainingInTrial = computed(() => {
    const sub = this.subscription();
    if (!sub) return null;
    return this.billingService.getDaysRemainingInTrial(sub);
  });

  storageUsagePercent = computed(() => {
    const sub = this.subscription();
    if (!sub) return 0;
    return this.billingService.getStorageUsagePercentage(
      this.storageUsageMb(),
      sub.plan.storageLimitMb
    );
  });

  userUsagePercent = computed(() => {
    const sub = this.subscription();
    if (!sub) return 0;
    return this.billingService.getUserUsagePercentage(
      this.userCount(),
      sub.plan.userLimit
    );
  });

  shouldShowUpgradePrompt = computed(() => {
    const sub = this.subscription();
    if (!sub) return false;
    return this.billingService.shouldUpgrade(
      sub,
      this.storageUsageMb(),
      this.userCount()
    );
  });

  constructor(
    private billingService: BillingService,
    private storageUsageService: StorageUsageService,
    private usersService: UsersService
  ) {}

  ngOnInit(): void {
    this.loadSubscription();
    this.loadPlans();
    this.loadPaymentHistory();
    this.loadUsageStats();
  }

  /**
   * Load current subscription.
   */
  loadSubscription(): void {
    this.isLoadingSubscription.set(true);
    this.billingService.getCurrentSubscription().subscribe({
      next: (sub) => {
        this.subscription.set(sub);
        this.isLoadingSubscription.set(false);
      },
      error: (err) => {
        console.error('Error loading subscription:', err);
        this.showError('Failed to load subscription');
        this.isLoadingSubscription.set(false);
      },
    });
  }

  /**
   * Load available plans.
   */
  loadPlans(): void {
    this.isLoadingPlans.set(true);
    this.billingService.getAvailablePlans().subscribe({
      next: (plans) => {
        this.availablePlans.set(plans);
        this.isLoadingPlans.set(false);
      },
      error: (err) => {
        console.error('Error loading plans:', err);
        this.showError('Failed to load subscription plans');
        this.isLoadingPlans.set(false);
      },
    });
  }

  /**
   * Load payment history.
   */
  loadPaymentHistory(): void {
    this.isLoadingPayments.set(true);
    this.billingService.getPaymentHistory().subscribe({
      next: (payments) => {
        this.payments.set(payments);
        this.isLoadingPayments.set(false);
      },
      error: (err) => {
        console.error('Error loading payments:', err);
        this.isLoadingPayments.set(false);
      },
    });
  }

  /**
   * Load storage and user usage stats.
   */
  loadUsageStats(): void {
    // Load storage usage
    this.storageUsageService.getCurrentUsage().subscribe({
      next: (usage) => {
        this.storageUsageMb.set(usage.totalSizeMb);
      },
      error: (err) => {
        console.error('Error loading storage usage:', err);
      },
    });

    // Load user count
    this.usersService.getUsers().subscribe({
      next: (users) => {
        this.userCount.set(users.length);
      },
      error: (err) => {
        console.error('Error loading user count:', err);
      },
    });
  }

  /**
   * Initiate upgrade to selected plan.
   */
  upgradeToPlan(plan: SubscriptionPlan): void {
    this.selectedPlanForUpgrade.set(plan);
    this.initiatePayment(plan);
  }

  /**
   * Initiate payment with Paystack.
   */
  initiatePayment(plan: SubscriptionPlan): void {
    this.isProcessing.set(true);
    const email = 'admin@church.com'; // TODO: Get from user profile
    const callbackUrl = `${window.location.origin}/billing/verify`;

    this.billingService
      .initializeSubscription(plan.id, email, callbackUrl)
      .subscribe({
        next: (response) => {
          // Redirect to Paystack payment page
          window.location.href = response.authorizationUrl;
        },
        error: (err) => {
          console.error('Error initializing payment:', err);
          this.showError('Failed to initialize payment. Please try again.');
          this.isProcessing.set(false);
        },
      });
  }

  /**
   * Cancel subscription.
   */
  cancelSubscription(): void {
    this.isProcessing.set(true);
    this.billingService.cancelSubscription().subscribe({
      next: (response) => {
        this.showSuccess(response.message);
        this.showCancelDialog.set(false);
        this.isProcessing.set(false);
        this.loadSubscription();
      },
      error: (err) => {
        console.error('Error canceling subscription:', err);
        this.showError('Failed to cancel subscription');
        this.isProcessing.set(false);
      },
    });
  }

  /**
   * Reactivate subscription.
   */
  reactivateSubscription(): void {
    this.isProcessing.set(true);
    this.billingService.reactivateSubscription().subscribe({
      next: (response) => {
        this.showSuccess(response.message);
        this.isProcessing.set(false);
        this.loadSubscription();
      },
      error: (err) => {
        console.error('Error reactivating subscription:', err);
        this.showError('Failed to reactivate subscription');
        this.isProcessing.set(false);
      },
    });
  }

  /**
   * Downgrade to free plan.
   */
  downgradeToFree(): void {
    this.isProcessing.set(true);
    this.billingService.downgradeToFree().subscribe({
      next: (response) => {
        this.showSuccess(response.message);
        this.showDowngradeDialog.set(false);
        this.isProcessing.set(false);
        this.loadSubscription();
      },
      error: (err) => {
        console.error('Error downgrading:', err);
        this.showError('Failed to downgrade to free plan');
        this.isProcessing.set(false);
      },
    });
  }

  /**
   * Check if plan is current plan.
   */
  isCurrentPlan(plan: SubscriptionPlan): boolean {
    const sub = this.subscription();
    return sub?.plan?.id === plan.id;
  }

  /**
   * Check if plan is upgrade from current.
   */
  isUpgrade(plan: SubscriptionPlan): boolean {
    const sub = this.subscription();
    if (!sub) return false;
    return plan.price > sub.plan.price;
  }

  /**
   * Check if plan is downgrade from current.
   */
  isDowngrade(plan: SubscriptionPlan): boolean {
    const sub = this.subscription();
    if (!sub) return false;
    return plan.price < sub.plan.price && !plan.isFree;
  }

  /**
   * Get storage progress bar class.
   */
  getStorageProgressClass(): string {
    const percent = this.storageUsagePercent();
    if (percent >= 90) return 'progress-bar-danger';
    if (percent >= 80) return 'progress-bar-warning';
    return 'progress-bar-success';
  }

  /**
   * Get user progress bar class.
   */
  getUserProgressClass(): string {
    const percent = this.userUsagePercent();
    if (percent >= 90) return 'progress-bar-danger';
    if (percent >= 80) return 'progress-bar-warning';
    return 'progress-bar-success';
  }

  /**
   * Format date for display.
   */
  formatDate(dateString: string | null): string {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString();
  }

  /**
   * Format datetime for display.
   */
  formatDateTime(dateString: string | null): string {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleString();
  }

  /**
   * Show success message.
   */
  showSuccess(message: string): void {
    this.successMessage.set(message);
    this.errorMessage.set(null);
    setTimeout(() => this.successMessage.set(null), 5000);
  }

  /**
   * Show error message.
   */
  showError(message: string): void {
    this.errorMessage.set(message);
    this.successMessage.set(null);
    setTimeout(() => this.errorMessage.set(null), 5000);
  }

  // Helper functions for template
  getStatusBadgeClass = getStatusBadgeClass;
  getStatusDisplayText = getStatusDisplayText;
  formatStorageLimit = formatStorageLimit;
  hasUnlimitedUsers = hasUnlimitedUsers;
  getPaymentStatusBadgeClass = getPaymentStatusBadgeClass;
  formatPaymentAmount = formatPaymentAmount;
}
