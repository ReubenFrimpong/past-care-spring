export enum CounselingStatus {
  SCHEDULED = 'SCHEDULED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  NO_SHOW = 'NO_SHOW',
  RESCHEDULED = 'RESCHEDULED'
}

export const CounselingStatusLabels: Record<CounselingStatus, string> = {
  [CounselingStatus.SCHEDULED]: 'Scheduled',
  [CounselingStatus.IN_PROGRESS]: 'In Progress',
  [CounselingStatus.COMPLETED]: 'Completed',
  [CounselingStatus.CANCELLED]: 'Cancelled',
  [CounselingStatus.NO_SHOW]: 'No Show',
  [CounselingStatus.RESCHEDULED]: 'Rescheduled'
};
