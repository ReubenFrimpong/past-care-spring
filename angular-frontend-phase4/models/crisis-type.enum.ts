export enum CrisisType {
  DEATH = 'DEATH',
  ACCIDENT = 'ACCIDENT',
  HOSPITALIZATION = 'HOSPITALIZATION',
  NATURAL_DISASTER = 'NATURAL_DISASTER',
  FIRE = 'FIRE',
  FINANCIAL_CRISIS = 'FINANCIAL_CRISIS',
  FAMILY_VIOLENCE = 'FAMILY_VIOLENCE',
  SUICIDE_RISK = 'SUICIDE_RISK',
  MENTAL_HEALTH_CRISIS = 'MENTAL_HEALTH_CRISIS',
  HOMELESSNESS = 'HOMELESSNESS',
  JOB_LOSS = 'JOB_LOSS',
  LEGAL_ISSUE = 'LEGAL_ISSUE',
  OTHER = 'OTHER'
}

export const CrisisTypeLabels: Record<CrisisType, string> = {
  [CrisisType.DEATH]: 'Death',
  [CrisisType.ACCIDENT]: 'Accident',
  [CrisisType.HOSPITALIZATION]: 'Hospitalization',
  [CrisisType.NATURAL_DISASTER]: 'Natural Disaster',
  [CrisisType.FIRE]: 'Fire',
  [CrisisType.FINANCIAL_CRISIS]: 'Financial Crisis',
  [CrisisType.FAMILY_VIOLENCE]: 'Family Violence',
  [CrisisType.SUICIDE_RISK]: 'Suicide Risk',
  [CrisisType.MENTAL_HEALTH_CRISIS]: 'Mental Health Crisis',
  [CrisisType.HOMELESSNESS]: 'Homelessness',
  [CrisisType.JOB_LOSS]: 'Job Loss',
  [CrisisType.LEGAL_ISSUE]: 'Legal Issue',
  [CrisisType.OTHER]: 'Other'
};
