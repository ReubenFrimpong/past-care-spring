import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ChurchSubscription,
  SubscriptionStatusResponse,
} from '../models/church-subscription.interface';
import { SubscriptionPlan } from '../models/subscription-plan.interface';
import {
  Payment,
  PaymentInitializationResponse,
} from '../models/payment.interface';

/**
 * Service for managing billing and subscriptions.
 */
@Injectable({
  providedIn: 'root',
})
export class BillingService {
  private apiUrl = `${environment.apiUrl}/api/billing`;

  // Cache for current subscription
  private subscriptionCache$ = new BehaviorSubject<ChurchSubscription | null>(
    null
  );
  public currentSubscription$ = this.subscriptionCache$.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Get current subscription for the church.
   */
  getCurrentSubscription(): Observable<ChurchSubscription> {
    return this.http.get<ChurchSubscription>(`${this.apiUrl}/subscription`).pipe(
      tap((subscription) => this.subscriptionCache$.next(subscription))
    );
  }

  /**
   * Get all available subscription plans.
   */
  getAvailablePlans(): Observable<SubscriptionPlan[]> {
    return this.http.get<SubscriptionPlan[]>(`${this.apiUrl}/plans`);
  }

  /**
   * Get a specific plan by ID.
   */
  getPlanById(planId: number): Observable<SubscriptionPlan> {
    return this.http.get<SubscriptionPlan>(`${this.apiUrl}/plans/${planId}`);
  }

  /**
   * Initialize payment for subscription upgrade.
   */
  initializeSubscription(
    planId: number,
    email: string,
    callbackUrl: string
  ): Observable<PaymentInitializationResponse> {
    return this.http.post<PaymentInitializationResponse>(
      `${this.apiUrl}/subscribe`,
      { planId, email, callbackUrl }
    );
  }

  /**
   * Verify payment and activate subscription.
   */
  verifyAndActivate(reference: string): Observable<Payment> {
    return this.http.post<Payment>(
      `${this.apiUrl}/verify/${reference}`,
      {}
    ).pipe(
      tap(() => {
        // Refresh subscription after activation
        this.getCurrentSubscription().subscribe();
      })
    );
  }

  /**
   * Cancel subscription (remains active until end of period).
   */
  cancelSubscription(): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/cancel`, {}).pipe(
      tap(() => {
        // Refresh subscription after cancellation
        this.getCurrentSubscription().subscribe();
      })
    );
  }

  /**
   * Reactivate a canceled subscription.
   */
  reactivateSubscription(): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/reactivate`, {}).pipe(
      tap(() => {
        // Refresh subscription after reactivation
        this.getCurrentSubscription().subscribe();
      })
    );
  }

  /**
   * Downgrade to free plan.
   */
  downgradeToFree(): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.apiUrl}/downgrade-to-free`,
      {}
    ).pipe(
      tap(() => {
        // Refresh subscription after downgrade
        this.getCurrentSubscription().subscribe();
      })
    );
  }

  /**
   * Get payment history for the church.
   */
  getPaymentHistory(): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.apiUrl}/payments`);
  }

  /**
   * Get successful payments only.
   */
  getSuccessfulPayments(): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.apiUrl}/payments/successful`);
  }

  /**
   * Get detailed subscription status.
   */
  getSubscriptionStatus(): Observable<SubscriptionStatusResponse> {
    return this.http.get<SubscriptionStatusResponse>(`${this.apiUrl}/status`);
  }

  /**
   * Clear subscription cache (call on logout).
   */
  clearCache(): void {
    this.subscriptionCache$.next(null);
  }

  /**
   * Calculate days remaining in trial.
   */
  getDaysRemainingInTrial(subscription: ChurchSubscription): number | null {
    if (!subscription.trialEndDate) return null;
    const trialEnd = new Date(subscription.trialEndDate);
    const now = new Date();
    const diffTime = trialEnd.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays > 0 ? diffDays : 0;
  }

  /**
   * Calculate storage usage percentage.
   */
  getStorageUsagePercentage(usedMb: number, limitMb: number): number {
    if (limitMb === 0) return 0;
    return Math.min(100, (usedMb / limitMb) * 100);
  }

  /**
   * Calculate user usage percentage.
   */
  getUserUsagePercentage(userCount: number, userLimit: number): number {
    if (userLimit === -1) return 0; // Unlimited
    if (userLimit === 0) return 0;
    return Math.min(100, (userCount / userLimit) * 100);
  }

  /**
   * Check if upgrade is needed based on usage.
   */
  shouldUpgrade(
    subscription: ChurchSubscription,
    storageUsedMb: number,
    userCount: number
  ): boolean {
    const storagePercent = this.getStorageUsagePercentage(
      storageUsedMb,
      subscription.plan.storageLimitMb
    );
    const userPercent = this.getUserUsagePercentage(
      userCount,
      subscription.plan.userLimit
    );
    return storagePercent >= 80 || userPercent >= 80;
  }
}
