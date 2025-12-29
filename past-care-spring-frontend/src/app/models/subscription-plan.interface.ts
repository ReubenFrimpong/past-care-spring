/**
 * Subscription plan with pricing and limits.
 */
export interface SubscriptionPlan {
  id: number;
  name: string; // STARTER, PROFESSIONAL, ENTERPRISE
  displayName: string;
  description: string;
  price: number;
  billingInterval: string; // MONTHLY, YEARLY
  storageLimitMb: number;
  userLimit: number; // -1 = unlimited
  isFree: boolean;
  isActive: boolean;
  paystackPlanCode: string | null;
  features: string[]; // JSON array of feature descriptions
  displayOrder: number;
  createdAt: string;
  updatedAt: string;
}

/**
 * Helper to check if plan has unlimited users.
 */
export function hasUnlimitedUsers(plan: SubscriptionPlan): boolean {
  return plan.userLimit === -1;
}

/**
 * Helper to format storage limit.
 */
export function formatStorageLimit(limitMb: number): string {
  if (limitMb >= 1024) {
    return `${(limitMb / 1024).toFixed(0)} GB`;
  }
  return `${limitMb} MB`;
}
