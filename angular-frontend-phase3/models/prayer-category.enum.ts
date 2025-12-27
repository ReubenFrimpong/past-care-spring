export enum PrayerCategory {
  HEALING = 'HEALING',
  GUIDANCE = 'GUIDANCE',
  PROVISION = 'PROVISION',
  PROTECTION = 'PROTECTION',
  SALVATION = 'SALVATION',
  RELATIONSHIPS = 'RELATIONSHIPS',
  GRIEF = 'GRIEF',
  ADDICTION = 'ADDICTION',
  MENTAL_HEALTH = 'MENTAL_HEALTH',
  EMPLOYMENT = 'EMPLOYMENT',
  MINISTRY = 'MINISTRY',
  TRAVEL = 'TRAVEL',
  EXAMS = 'EXAMS',
  PREGNANCY = 'PREGNANCY',
  BREAKTHROUGH = 'BREAKTHROUGH',
  THANKSGIVING = 'THANKSGIVING',
  OTHER = 'OTHER'
}

export const PrayerCategoryLabels: Record<PrayerCategory, string> = {
  [PrayerCategory.HEALING]: 'Healing',
  [PrayerCategory.GUIDANCE]: 'Guidance',
  [PrayerCategory.PROVISION]: 'Provision',
  [PrayerCategory.PROTECTION]: 'Protection',
  [PrayerCategory.SALVATION]: 'Salvation',
  [PrayerCategory.RELATIONSHIPS]: 'Relationships',
  [PrayerCategory.GRIEF]: 'Grief',
  [PrayerCategory.ADDICTION]: 'Addiction',
  [PrayerCategory.MENTAL_HEALTH]: 'Mental Health',
  [PrayerCategory.EMPLOYMENT]: 'Employment',
  [PrayerCategory.MINISTRY]: 'Ministry',
  [PrayerCategory.TRAVEL]: 'Travel',
  [PrayerCategory.EXAMS]: 'Exams',
  [PrayerCategory.PREGNANCY]: 'Pregnancy',
  [PrayerCategory.BREAKTHROUGH]: 'Breakthrough',
  [PrayerCategory.THANKSGIVING]: 'Thanksgiving',
  [PrayerCategory.OTHER]: 'Other'
};
