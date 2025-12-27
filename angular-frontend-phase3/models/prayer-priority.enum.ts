export enum PrayerPriority {
  LOW = 'LOW',
  NORMAL = 'NORMAL',
  HIGH = 'HIGH',
  URGENT = 'URGENT'
}

export const PrayerPriorityLabels: Record<PrayerPriority, string> = {
  [PrayerPriority.LOW]: 'Low',
  [PrayerPriority.NORMAL]: 'Normal',
  [PrayerPriority.HIGH]: 'High',
  [PrayerPriority.URGENT]: 'Urgent'
};
