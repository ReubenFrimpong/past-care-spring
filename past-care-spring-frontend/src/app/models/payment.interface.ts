import { SubscriptionPlan } from './subscription-plan.interface';

/**
 * Payment transaction record.
 */
export interface Payment {
  id: number;
  churchId: number;
  plan: SubscriptionPlan;
  amount: number;
  currency: string;
  status: PaymentStatus;
  paystackReference: string;
  paystackTransactionId: string | null;
  paystackAuthorizationCode: string | null;
  paymentMethod: string | null; // CARD, BANK_TRANSFER, MOBILE_MONEY, USSD
  cardLast4: string | null;
  cardBrand: string | null;
  cardExpiry: string | null;
  paymentType: string; // SUBSCRIPTION, ONE_TIME, UPGRADE, DOWNGRADE
  description: string | null;
  metadata: string | null;
  invoiceNumber: string | null;
  paymentDate: string | null; // ISO datetime string
  refundAmount: number | null;
  refundDate: string | null;
  refundReason: string | null;
  failureReason: string | null;
  ipAddress: string | null;
  userAgent: string | null;
  createdAt: string;
  updatedAt: string;
}

/**
 * Payment status type.
 */
export type PaymentStatus =
  | 'PENDING'
  | 'SUCCESS'
  | 'FAILED'
  | 'REFUNDED'
  | 'CHARGEBACK';

/**
 * Payment initialization response from Paystack.
 */
export interface PaymentInitializationResponse {
  authorizationUrl: string;
  accessCode: string;
  reference: string;
}

/**
 * Helper to get payment status badge class.
 */
export function getPaymentStatusBadgeClass(status: PaymentStatus): string {
  switch (status) {
    case 'SUCCESS': return 'badge-success';
    case 'PENDING': return 'badge-warning';
    case 'FAILED': return 'badge-danger';
    case 'REFUNDED': return 'badge-info';
    case 'CHARGEBACK': return 'badge-danger';
    default: return 'badge-secondary';
  }
}

/**
 * Helper to format payment amount.
 */
export function formatPaymentAmount(payment: Payment): string {
  return `${payment.currency} ${payment.amount.toFixed(2)}`;
}
