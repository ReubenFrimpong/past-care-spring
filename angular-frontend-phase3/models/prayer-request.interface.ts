import { PrayerCategory } from './prayer-category.enum';
import { PrayerPriority } from './prayer-priority.enum';
import { PrayerRequestStatus } from './prayer-request-status.enum';

export interface PrayerRequestRequest {
  memberId: number;
  title: string;
  description?: string;
  category: PrayerCategory;
  priority?: PrayerPriority;
  status?: PrayerRequestStatus;
  isAnonymous?: boolean;
  isUrgent?: boolean;
  expirationDate?: string;
  isPublic?: boolean;
  tags?: string;
}

export interface PrayerRequestResponse {
  id: number;
  memberId: number;
  memberName: string;
  submittedById: number;
  submittedByName: string;
  title: string;
  description?: string;
  category: PrayerCategory;
  priority: PrayerPriority;
  status: PrayerRequestStatus;
  isAnonymous: boolean;
  isUrgent: boolean;
  expirationDate?: string;
  answeredDate?: string;
  testimony?: string;
  prayerCount: number;
  isPublic: boolean;
  tags?: string;
  isExpired: boolean;
  isAnswered: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PrayerRequestStatsResponse {
  totalPrayerRequests: number;
  pendingPrayerRequests: number;
  activePrayerRequests: number;
  answeredPrayerRequests: number;
  urgentPrayerRequests: number;
  publicPrayerRequests: number;
}

export interface MarkAsAnsweredRequest {
  testimony: string;
}
