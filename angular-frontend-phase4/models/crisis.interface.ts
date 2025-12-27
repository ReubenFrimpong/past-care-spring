import { CrisisType } from './crisis-type.enum';
import { CrisisSeverity } from './crisis-severity.enum';
import { CrisisStatus } from './crisis-status.enum';

export interface CrisisAffectedMemberResponse {
  id: number;
  crisisId: number;
  memberId: number;
  memberName: string;
  notes?: string;
  isPrimaryContact: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CrisisAffectedMemberRequest {
  memberId: number;
  notes?: string;
  isPrimaryContact?: boolean;
}

export interface CrisisRequest {
  title: string;
  description?: string;
  crisisType: CrisisType;
  severity: CrisisSeverity;
  status?: CrisisStatus;
  incidentDate?: string;
  location?: string;
  affectedMembersCount?: number;
  responseTeamNotes?: string;
  resolutionNotes?: string;
  followUpRequired?: boolean;
  followUpDate?: string;
  resourcesMobilized?: string;
  communicationSent?: boolean;
  emergencyContactNotified?: boolean;
  affectedMemberIds?: number[];
}

export interface CrisisResponse {
  id: number;
  title: string;
  description?: string;
  crisisType: CrisisType;
  severity: CrisisSeverity;
  status: CrisisStatus;
  reportedById: number;
  reportedByName: string;
  reportedDate: string;
  incidentDate?: string;
  location?: string;
  affectedMembersCount?: number;
  responseTeamNotes?: string;
  resolutionNotes?: string;
  resolvedDate?: string;
  followUpRequired: boolean;
  followUpDate?: string;
  resourcesMobilized?: string;
  communicationSent: boolean;
  emergencyContactNotified: boolean;
  isActive: boolean;
  isCritical: boolean;
  isResolved: boolean;
  affectedMembers?: CrisisAffectedMemberResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface CrisisStatsResponse {
  totalCrises: number;
  activeCrises: number;
  inResponseCrises: number;
  resolvedCrises: number;
  criticalCrises: number;
  highSeverityCrises: number;
  totalAffectedMembers: number;
}

export interface MobilizeResourcesRequest {
  resources: string;
}

export interface ResolveCrisisRequest {
  resolutionNotes: string;
}

export interface UpdateStatusRequest {
  status: CrisisStatus;
}
