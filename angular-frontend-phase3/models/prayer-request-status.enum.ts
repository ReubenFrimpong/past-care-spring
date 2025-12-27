export enum PrayerRequestStatus {
  PENDING = 'PENDING',
  ACTIVE = 'ACTIVE',
  ANSWERED = 'ANSWERED',
  ARCHIVED = 'ARCHIVED'
}

export const PrayerRequestStatusLabels: Record<PrayerRequestStatus, string> = {
  [PrayerRequestStatus.PENDING]: 'Pending',
  [PrayerRequestStatus.ACTIVE]: 'Active',
  [PrayerRequestStatus.ANSWERED]: 'Answered',
  [PrayerRequestStatus.ARCHIVED]: 'Archived'
};
