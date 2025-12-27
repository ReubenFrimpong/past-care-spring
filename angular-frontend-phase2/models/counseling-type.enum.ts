export enum CounselingType {
  INDIVIDUAL = 'INDIVIDUAL',
  COUPLES = 'COUPLES',
  FAMILY = 'FAMILY',
  GROUP = 'GROUP',
  YOUTH = 'YOUTH',
  GRIEF = 'GRIEF',
  ADDICTION = 'ADDICTION',
  FINANCIAL = 'FINANCIAL',
  CAREER = 'CAREER',
  SPIRITUAL = 'SPIRITUAL',
  MENTAL_HEALTH = 'MENTAL_HEALTH',
  CRISIS = 'CRISIS',
  PRE_MARITAL = 'PRE_MARITAL',
  CONFLICT_RESOLUTION = 'CONFLICT_RESOLUTION',
  OTHER = 'OTHER'
}

export const CounselingTypeLabels: Record<CounselingType, string> = {
  [CounselingType.INDIVIDUAL]: 'Individual',
  [CounselingType.COUPLES]: 'Couples',
  [CounselingType.FAMILY]: 'Family',
  [CounselingType.GROUP]: 'Group',
  [CounselingType.YOUTH]: 'Youth',
  [CounselingType.GRIEF]: 'Grief',
  [CounselingType.ADDICTION]: 'Addiction',
  [CounselingType.FINANCIAL]: 'Financial',
  [CounselingType.CAREER]: 'Career',
  [CounselingType.SPIRITUAL]: 'Spiritual',
  [CounselingType.MENTAL_HEALTH]: 'Mental Health',
  [CounselingType.CRISIS]: 'Crisis',
  [CounselingType.PRE_MARITAL]: 'Pre-Marital',
  [CounselingType.CONFLICT_RESOLUTION]: 'Conflict Resolution',
  [CounselingType.OTHER]: 'Other'
};
