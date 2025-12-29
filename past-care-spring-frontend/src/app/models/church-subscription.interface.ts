import { SubscriptionPlan } from './subscription-plan.interface';

/**
 * Church subscription with status and billing information.
 */
export interface ChurchSubscription {
  id: number;
  churchId: number;
  plan: SubscriptionPlan;
  status: SubscriptionStatus;
  trialEndDate: string | null; // ISO date string
  nextBillingDate: string | null;
  currentPeriodStart: string | null;
  currentPeriodEnd: string | null;
  canceledAt: string | null;
  endsAt: string | null;
  paystackCustomerCode: string | null;
  paystackSubscriptionCode: string | null;
  paystackEmailToken: string | null;
  paystackAuthorizationCode: string | null;
  paymentMethodType: string | null; // CARD, BANK_TRANSFER, etc.
  cardLast4: string | null;
  cardBrand: string | null; // VISA, MASTERCARD, etc.
  autoRenew: boolean;
  gracePeriodDays: number;
  failedPaymentAttempts: number;
  createdAt: string;
  updatedAt: string;
}

/**
 * Subscription status enum.
 */
export type SubscriptionStatus =
  | 'TRIALING'
  | 'ACTIVE'
  | 'PAST_DUE'
  | 'CANCELED'
  | 'SUSPENDED';

/**
 * Detailed subscription status response.
 */
export interface SubscriptionStatusResponse {
  isActive: boolean;
  isTrialing: boolean;
  isPastDue: boolean;
  isCanceled: boolean;
  isSuspended: boolean;
  isInGracePeriod: boolean;
  planName: string;
  planDisplayName: string;
  status: SubscriptionStatus;
  trialEndDate: string | null;
  nextBillingDate: string | null;
  currentPeriodEnd: string | null;
}

/**
 * Helper to get status badge class.
 */
export function getStatusBadgeClass(status: SubscriptionStatus): string {
  switch (status) {
    case 'ACTIVE': return 'badge-success';
    case 'TRIALING': return 'badge-info';
    case 'PAST_DUE': return 'badge-warning';
    case 'CANCELED': return 'badge-secondary';
    case 'SUSPENDED': return 'badge-danger';
    default: return 'badge-secondary';
  }
}

/**
 * Helper to get status display text.
 */
export function getStatusDisplayText(status: SubscriptionStatus): string {
  switch (status) {
    case 'ACTIVE': return 'Active';
    case 'TRIALING': return 'Trial';
    case 'PAST_DUE': return 'Past Due';
    case 'CANCELED': return 'Canceled';
    case 'SUSPENDED': return 'Suspended';
    default: return status;
  }
}
