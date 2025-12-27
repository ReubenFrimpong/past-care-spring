export enum CrisisSeverity {
  CRITICAL = 'CRITICAL',
  HIGH = 'HIGH',
  MODERATE = 'MODERATE',
  LOW = 'LOW'
}

export const CrisisSeverityLabels: Record<CrisisSeverity, string> = {
  [CrisisSeverity.CRITICAL]: 'Critical',
  [CrisisSeverity.HIGH]: 'High',
  [CrisisSeverity.MODERATE]: 'Moderate',
  [CrisisSeverity.LOW]: 'Low'
};
