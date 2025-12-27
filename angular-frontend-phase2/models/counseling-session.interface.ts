import { CounselingType } from './counseling-type.enum';
import { CounselingStatus } from './counseling-status.enum';
import { SessionOutcome } from './session-outcome.enum';

export interface CounselingSessionRequest {
  memberId: number;
  counselorId: number;
  careNeedId?: number;
  sessionDate: string;
  startTime?: string;
  endTime?: string;
  type: CounselingType;
  status: CounselingStatus;
  purpose?: string;
  notes?: string;
  outcomes?: string;
  outcome?: SessionOutcome;
  isConfidential: boolean;
  requiresFollowUp: boolean;
  followUpDate?: string;
  referredToProfessional: boolean;
  referralDetails?: string;
}

export interface CounselingSessionResponse {
  id: number;
  memberId: number;
  memberName: string;
  counselorId: number;
  counselorName: string;
  careNeedId?: number;
  sessionDate: string;
  startTime?: string;
  endTime?: string;
  type: CounselingType;
  status: CounselingStatus;
  purpose?: string;
  notes?: string;
  outcomes?: string;
  outcome?: SessionOutcome;
  isConfidential: boolean;
  requiresFollowUp: boolean;
  followUpDate?: string;
  referredToProfessional: boolean;
  referralDetails?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CounselingSessionStatsResponse {
  totalSessions: number;
  scheduledSessions: number;
  completedSessions: number;
  requiresFollowUp: number;
  byType: Record<CounselingType, number>;
  byStatus: Record<CounselingStatus, number>;
  byOutcome: Record<SessionOutcome, number>;
}

export interface CompleteSessionRequest {
  outcome: SessionOutcome;
  outcomes?: string;
  requiresFollowUp?: boolean;
  followUpDate?: string;
}

export interface ScheduleFollowUpRequest {
  followUpDate: string;
  notes?: string;
}

export interface CreateReferralRequest {
  referralDetails: string;
}
