export enum CrisisStatus {
  ACTIVE = 'ACTIVE',
  IN_RESPONSE = 'IN_RESPONSE',
  RESOLVED = 'RESOLVED',
  CLOSED = 'CLOSED'
}

export const CrisisStatusLabels: Record<CrisisStatus, string> = {
  [CrisisStatus.ACTIVE]: 'Active',
  [CrisisStatus.IN_RESPONSE]: 'In Response',
  [CrisisStatus.RESOLVED]: 'Resolved',
  [CrisisStatus.CLOSED]: 'Closed'
};
