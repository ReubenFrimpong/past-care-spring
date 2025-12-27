export enum SessionOutcome {
  POSITIVE = 'POSITIVE',
  NEUTRAL = 'NEUTRAL',
  CHALLENGING = 'CHALLENGING',
  NEEDS_FOLLOWUP = 'NEEDS_FOLLOWUP',
  NEEDS_REFERRAL = 'NEEDS_REFERRAL',
  RESOLVED = 'RESOLVED',
  ONGOING = 'ONGOING'
}

export const SessionOutcomeLabels: Record<SessionOutcome, string> = {
  [SessionOutcome.POSITIVE]: 'Positive',
  [SessionOutcome.NEUTRAL]: 'Neutral',
  [SessionOutcome.CHALLENGING]: 'Challenging',
  [SessionOutcome.NEEDS_FOLLOWUP]: 'Needs Follow-up',
  [SessionOutcome.NEEDS_REFERRAL]: 'Needs Referral',
  [SessionOutcome.RESOLVED]: 'Resolved',
  [SessionOutcome.ONGOING]: 'Ongoing'
};
