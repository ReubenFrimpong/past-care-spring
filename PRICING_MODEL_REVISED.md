# PastCare SaaS - Congregation-Based Pricing Model

**Date**: 2026-01-01
**Status**: FINALIZED
**Based on**: PastCare 2025 Pricing spreadsheet

---

## Final Pricing Decision

### Key Pricing Principles

1. **No Free Plan** - Churches must subscribe from day 1
2. **Congregation-Based Pricing** - Pricing based on member count, not storage
3. **Dual Currency Display** - GHS (primary) and USD (secondary)
4. **Fixed Dollar Discounts** - Same discount amounts across all tiers
5. **Four Tiers** - Small, Standard, Professional, Enterprise

**IMPORTANT: All tiers have IDENTICAL features. The ONLY difference is the member limit.**

---

## Pricing Structure

```
┌──────────────────────────────────────────────────────────────────────┐
│              PASTCARE SAAS - CONGREGATION PRICING                    │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│ TIER 1: SMALL CHURCH (1-200 Members)                                │
│   Monthly:    GHS 70   ($5.99)                                      │
│   Quarterly:  GHS 200  ($16.47)  - Save GHS 18  (save $1.50)       │
│   Biannual:   GHS 380  ($31.44)  - Save GHS 54  (save $4.50)       │
│   Annual:     GHS 720  ($59.88)  - Save GHS 144 (save $12.00)      │
│                                                                      │
│ TIER 2: STANDARD CHURCH (201-500 Members)                           │
│   Monthly:    GHS 120  ($9.99)                                      │
│   Quarterly:  GHS 340  ($28.47)  - Save GHS 18  (save $1.50)       │
│   Biannual:   GHS 665  ($55.44)  - Save GHS 54  (save $4.50)       │
│   Annual:     GHS 1,295 ($107.88) - Save GHS 144 (save $12.00)     │
│                                                                      │
│ TIER 3: PROFESSIONAL CHURCH (501-1000 Members)                      │
│   Monthly:    GHS 170  ($13.99)                                     │
│   Quarterly:  GHS 485  ($40.47)  - Save GHS 18  (save $1.50)       │
│   Biannual:   GHS 955  ($79.44)  - Save GHS 54  (save $4.50)       │
│   Annual:     GHS 1,870 ($155.88) - Save GHS 144 (save $12.00)     │
│                                                                      │
│ TIER 4: ENTERPRISE CHURCH (1001+ Members)                           │
│   Monthly:    GHS 215  ($17.99)                                     │
│   Quarterly:  GHS 630  ($52.47)  - Save GHS 18  (save $1.50)       │
│   Biannual:   GHS 1,245 ($103.44) - Save GHS 54  (save $4.50)      │
│   Annual:     GHS 2,450 ($203.88) - Save GHS 144 (save $12.00)     │
│                                                                      │
│ Exchange Rate: GHS 12.00 = $1.00 USD (configurable by SUPERADMIN)  │
│ Trial: NONE - Subscription required from day 1                      │
│ Grace Period: Admin-controlled on case-by-case basis                │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Tier Details

**IMPORTANT: All tiers have IDENTICAL features. The ONLY difference is the member limit.**

### Small Church (1-200 Members)
- **Target**: Small and new churches
- **Monthly Rate**: GHS 70/month ($5.99/month)
- **Member Limit**: 1-200 members
- **Features**: All features included (same as all tiers)

### Standard Church (201-500 Members)
- **Target**: Growing churches with established congregations
- **Monthly Rate**: GHS 120/month ($9.99/month)
- **Member Limit**: 201-500 members
- **Features**: All features included (same as all tiers)

### Professional Church (501-1000 Members)
- **Target**: Established churches with active congregations
- **Monthly Rate**: GHS 170/month ($13.99/month)
- **Member Limit**: 501-1000 members
- **Features**: All features included (same as all tiers)

### Enterprise Church (1001+ Members)
- **Target**: Large churches and multi-campus organizations
- **Monthly Rate**: GHS 215/month ($17.99/month)
- **Member Limit**: 1001+ members (unlimited)
- **Features**: All features included (same as all tiers)

---

## Billing Intervals

### Monthly Billing
- **Payment**: Every month
- **Discount**: None
- **Cancel**: Anytime

### Quarterly Billing (3 Months)
- **Payment**: Every 3 months
- **Discount**: Save $1.50 (GHS 18)
- **Cancel**: Anytime

### Biannual Billing (6 Months)
- **Payment**: Every 6 months
- **Discount**: Save $4.50 (GHS 54)
- **Cancel**: Anytime

### Annual Billing (12 Months)
- **Payment**: Once per year
- **Discount**: Save $12.00 (GHS 144)
- **Cancel**: Anytime
- **Recommended**: Best value for churches

---

## What's Included in ALL Tiers

### Member Management
- Unlimited member profiles
- Photo uploads
- Custom fields
- Import/export capabilities
- Member search and filtering
- Tags and categories

### User Management
- Unlimited users (staff/volunteers)
- Role-based access control (7 roles)
- Permission management
- Activity tracking

### Event Management
- Unlimited events
- Recurring events
- Event registration
- Attendance tracking
- Event images and documents

### Fellowship Management
- Unlimited fellowships/groups
- Fellowship leaders
- Member assignments
- Fellowship reports

### Pastoral Care
- Visit tracking
- Prayer requests
- Follow-up reminders
- Care notes

### Donations & Pledges
- Donation tracking
- Pledge management
- Financial reports
- Receipt generation

### Communication
- SMS notifications
- Email notifications (coming soon)
- Bulk messaging

### Analytics & Reports
- Member reports
- Attendance reports
- Donation reports
- Custom exports

### Support
- Email support
- Knowledge base
- Mobile access
- Cloud backup

---

## Grace Period System

### How Grace Periods Work

**Default Behavior**:
- No automatic trial or grace period
- Churches must have an active subscription to use the system
- Payment is required before activation

**Admin-Controlled Grace Periods**:
Platform administrators can grant grace periods for:

1. **Partner Churches** - 1-3 months grace period
2. **Special Cases** - Financial hardship, natural disasters
3. **Promotional Campaigns** - Launch promotions, referral rewards
4. **Partnership Codes** - Coded promotional access
5. **Early Adopters** - Initial launch incentives

---

## Payment Processing

### Paystack Integration

**Currency**: GHS (Ghana Cedis)
**Gateway**: Paystack
**Methods**: Cards, Mobile Money

**Payment Flow**:
1. Church selects tier and billing interval
2. System calculates GHS amount from USD base price
3. Payment initialized with Paystack
4. User redirects to Paystack checkout
5. Payment processed
6. Webhook confirms payment
7. Subscription activated

**Exchange Rate**:
- Base rate: GHS 12.00 = $1.00 USD
- Configurable by SUPERADMIN
- Applied to all pricing calculations
- Updated centrally affects all displays

---

## Pricing Philosophy

### Why Congregation-Based Pricing?

1. **Fair and Predictable**: Churches know costs based on size
2. **Scales with Growth**: Natural progression as church grows
3. **Simple to Understand**: Clear tiers, no complex calculations
4. **Removes Storage Concerns**: Focus on ministry, not data limits
5. **Encourages Growth**: No penalty for adding members within tier

### Why Fixed Dollar Discounts?

1. **Consistent Savings**: Same savings amount across all tiers
2. **Easy to Communicate**: "$1.50 off quarterly" is clear
3. **Spreadsheet-Accurate**: Matches provided pricing exactly
4. **No Confusion**: Everyone saves the same absolute amount

### Why GHS Primary Display?

1. **Target Market**: Ghana-based churches
2. **Payment Currency**: Paystack processes GHS
3. **User Familiarity**: Churches think in local currency
4. **USD Secondary**: Still visible for reference

---

## Support Information

### For Support Team

**Key Points**:
- Pricing based on congregation size, not storage
- 4 tiers: Small (1-200), Standard (201-500), Professional (501-1000), Enterprise (1001+)
- All tiers have IDENTICAL features - only member limit differs
- Discount: $1.50 quarterly, $4.50 biannual, $12 annual
- Exchange rate: GHS 12 = $1 USD
- Display format: "GHS 70 ($5.99)"

### For Sales Team

**Talking Points**:
- New/small churches (1-200): Small tier at GHS 70/month
- Growing churches (201-500): Standard tier at GHS 120/month
- Established churches (501-1000): Professional tier at GHS 170/month
- Large churches (1001+): Enterprise tier at GHS 215/month
- Annual billing: Save GHS 144 per year
- All features available at every tier level

---

## Appendix: Complete Pricing Table

| Tier | Members | Monthly USD | Monthly GHS | Quarterly USD | Quarterly GHS | Biannual USD | Biannual GHS | Annual USD | Annual GHS |
|------|---------|-------------|-------------|---------------|---------------|--------------|--------------|------------|------------|
| Small | 1-200 | $5.99 | 70 | $16.47 | 200 | $31.44 | 380 | $59.88 | 720 |
| Standard | 201-500 | $9.99 | 120 | $28.47 | 340 | $55.44 | 665 | $107.88 | 1,295 |
| Professional | 501-1000 | $13.99 | 170 | $40.47 | 485 | $79.44 | 955 | $155.88 | 1,870 |
| Enterprise | 1001+ | $17.99 | 215 | $52.47 | 630 | $103.44 | 1,245 | $203.88 | 2,450 |

### Discount Calculations

**Quarterly (3 months)**:
- Full price: Monthly × 3
- Discount: -$1.50
- Final: (Monthly × 3) - $1.50

**Biannual (6 months)**:
- Full price: Monthly × 6
- Discount: -$4.50
- Final: (Monthly × 6) - $4.50

**Annual (12 months)**:
- Full price: Monthly × 12
- Discount: -$12.00
- Final: (Monthly × 12) - $12.00

---

**Document Version**: 3.0
**Last Updated**: 2026-01-01
**Source**: PastCare 2025 Pricing spreadsheet
