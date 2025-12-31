# Unlimited Storage Plan Implementation - 2025-12-31

## Overview

Added a new **PastCare Unlimited** subscription plan at **GHS 499/month** with **unlimited storage** for churches with extensive media needs.

---

## Changes Made

### 1. Database Migration

**File**: [V87__add_unlimited_storage_plan.sql](src/main/resources/db/migration/V87__add_unlimited_storage_plan.sql)

**Created new plan**:
```sql
INSERT INTO subscription_plans (
    name,
    display_name,
    description,
    price,
    billing_interval,
    storage_limit_mb,
    user_limit,
    is_free,
    is_active,
    display_order,
    features,
    created_at
)
VALUES (
    'UNLIMITED',
    'PastCare Unlimited',
    'For large churches with extensive media needs',
    499.00,
    'MONTHLY',
    -1,  -- -1 indicates UNLIMITED storage
    999999,
    0,
    1,
    2,
    '["Unlimited Members","Unlimited Users","Member Management",...,"Unlimited Storage","No Upload Limits","Premium Features"]',
    NOW()
);
```

**Key Details**:
- **Name**: `UNLIMITED`
- **Display Name**: `PastCare Unlimited`
- **Price**: GHS 499.00/month
- **Storage Limit**: `-1` (special value meaning unlimited)
- **Display Order**: `2` (appears second after STANDARD plan)
- **Active**: `true`

---

### 2. Backend Storage Enforcement Logic

**File**: [StorageEnforcementService.java:47-64](src/main/java/com/reuben/pastcare_spring/services/StorageEnforcementService.java#L47-L64)

**Added unlimited storage handling**:

```java
// UNLIMITED STORAGE: If limit is -1, always allow upload
if (limitMb == -1) {
    double currentUsageMb = storageCalculationService.getLatestStorageUsage(churchId).getTotalStorageMb();
    double fileSizeMb = fileSizeBytes / (1024.0 * 1024.0);
    double newTotalMb = currentUsageMb + fileSizeMb;

    log.debug("Unlimited storage plan for church {}: uploading {} MB (current: {} MB, new total: {} MB)",
            churchId, fileSizeMb, currentUsageMb, newTotalMb);

    return new StorageCheckResult(
            true,           // Always allowed
            currentUsageMb,
            -1,             // Unlimited
            fileSizeMb,
            newTotalMb,
            0.0             // 0% usage for unlimited
    );
}
```

**How It Works**:
1. Before checking storage limits, service checks if `limitMb == -1`
2. If unlimited plan, **always returns `allowed = true`**
3. Still tracks current usage for analytics
4. Returns `0.0%` usage percentage (unlimited has no percentage)
5. Normal storage enforcement continues for non-unlimited plans

---

### 3. Frontend Integration

**No changes needed** - Frontend already loads plans dynamically from API:

**File**: [pricing-section.ts:62-92](past-care-spring-frontend/src/app/pricing-section/pricing-section.ts#L62-L92)

```typescript
loadPlans(): void {
  this.http.get<any[]>(`${environment.apiUrl}/billing/plans`).subscribe({
    next: (plans) => {
      const transformedPlans = plans.map((plan) => ({
        ...plan,
        highlighted: !plan.isFree,
        buttonText: plan.isFree ? 'Start Free' : 'Get Started',
        tagline: this.getTagline(plan.name),
        features: this.parseFeatures(plan.features)
      }));
      this.plans.set(transformedPlans);
    }
  });
}
```

**Result**:
- Unlimited plan will automatically appear on pricing page
- Displays as "PastCare Unlimited - GHS 499/month"
- Shows "Unlimited Storage" feature
- Includes all premium features

---

## Plan Comparison

### STANDARD Plan (Existing)
```
Name: PastCare Standard
Price: GHS 150/month
Storage: 2 GB
Features:
- Unlimited Members & Users
- All Core Features
- Member/Event/Donation Management
- SMS Notifications
- Advanced Analytics
- Priority Support
```

### UNLIMITED Plan (NEW)
```
Name: PastCare Unlimited
Price: GHS 499/month
Storage: UNLIMITED ♾️
Features:
- Everything in STANDARD
- UNLIMITED Storage
- No Upload Limits
- Premium Features
- Ideal for: Large churches, extensive media libraries, video content
```

**Price Difference**: GHS 349/month more for unlimited storage

---

## Technical Implementation

### Storage Limit Convention

The system uses **`-1`** as a special sentinel value to indicate unlimited storage:

```
storageLimitMb values:
  2048    = 2 GB limit
  5120    = 5 GB limit
  10240   = 10 GB limit
  -1      = UNLIMITED ♾️
```

### Storage Enforcement Flow

```
User uploads file
       ↓
StorageEnforcementService.canUploadFile()
       ↓
Check: limitMb == -1?
  ├─ YES → Always allow (unlimited plan)
  └─ NO  → Check against limit (standard plan)
       ↓
Return StorageCheckResult
```

### Benefits of -1 Convention

1. **Database Efficiency**: No separate boolean column needed
2. **Backward Compatible**: Existing code works for limited plans
3. **Clear Semantics**: Negative value clearly indicates "no limit"
4. **Easy to Check**: Simple `if (limitMb == -1)` check
5. **Works with Existing Schema**: No migration needed for existing tables

---

## Features Included in Unlimited Plan

From migration (JSON features array):
```json
[
  "Unlimited Members",
  "Unlimited Users",
  "Member Management",
  "Event Management",
  "Attendance Tracking",
  "Donation Tracking",
  "SMS Notifications",
  "Pastoral Care Tools",
  "Advanced Analytics",
  "Priority Support",
  "Unlimited Storage",          ← NEW
  "No Upload Limits",            ← NEW
  "Premium Features"             ← NEW
]
```

---

## Billing Periods for Unlimited Plan

The unlimited plan uses the same billing intervals as STANDARD:

```
Monthly:   GHS 499.00  (GHS 499/month)
3 Months:  GHS 1,497.00 (GHS 499/month)
6 Months:  GHS 2,994.00 (GHS 499/month)
Yearly:    GHS 5,988.00 (GHS 499/month)
```

**No discounts** - consistent with STANDARD plan pricing model.

---

## Use Cases for Unlimited Plan

### Ideal For:
1. **Large Churches**: 500+ members with extensive photo libraries
2. **Multi-Campus Churches**: Multiple locations with shared media
3. **Video Ministry**: Churches recording/storing sermon videos
4. **Historical Archives**: Long-established churches with decades of records
5. **Media-Heavy Events**: Frequent photo/video documentation

### Examples:
- Church with 1,000 members, each with profile photo (3 MB avg) = 3 GB just for photos
- 100 events/year with 50 photos each (5 MB avg) = 25 GB/year
- Sermon video library: 52 sermons × 500 MB = 26 GB
- **Total**: 54+ GB needed → Unlimited plan is cost-effective

---

## Migration Steps

### To Apply This Update:

1. **Run Database Migration**:
   ```bash
   ./mvnw flyway:migrate
   ```
   This will execute `V87__add_unlimited_storage_plan.sql`

2. **Verify Plan Created**:
   ```sql
   SELECT name, display_name, price, storage_limit_mb, is_active, display_order
   FROM subscription_plans
   WHERE name = 'UNLIMITED';
   ```

   Expected result:
   ```
   name      | display_name       | price  | storage_limit_mb | is_active | display_order
   ----------|--------------------| -------|------------------|-----------|---------------
   UNLIMITED | PastCare Unlimited | 499.00 | -1               | 1         | 2
   ```

3. **Restart Backend** (if running):
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Verify Frontend**:
   - Navigate to landing page pricing section
   - Should see two plans: "PastCare Standard" and "PastCare Unlimited"
   - Unlimited plan should show "Unlimited Storage" feature

---

## Testing Unlimited Storage

### Test Scenario 1: Church with Unlimited Plan

```sql
-- 1. Assign church to unlimited plan
UPDATE church_subscriptions
SET plan_id = (SELECT id FROM subscription_plans WHERE name = 'UNLIMITED')
WHERE church_id = 1;

-- 2. Update church's storage limit
UPDATE churches
SET total_storage_limit_mb = -1
WHERE id = 1;

-- 3. Try uploading large file
-- Should succeed regardless of size (within reasonable server limits)
```

### Test Scenario 2: Verify Storage Enforcement

```java
// Church with unlimited plan
StorageEnforcementService.StorageCheckResult result =
    storageEnforcementService.canUploadFile(churchId, 5_000_000_000L); // 5 GB file

// Expected:
// result.isAllowed() = true
// result.getLimitMb() = -1
// result.getPercentageUsed() = 0.0
// result.getCurrentUsageMb() = actual usage
```

---

## Pricing Justification

### STANDARD Plan: GHS 150/month for 2 GB
```
Cost per GB = GHS 150 / 2 = GHS 75/GB
```

### UNLIMITED Plan: GHS 499/month
```
Break-even point = GHS 499 / GHS 75 per GB ≈ 6.65 GB

If church needs >7 GB, unlimited is more cost-effective than:
- Buying multiple storage add-ons
- Managing storage limits
- Deleting old content
```

---

## Revenue Impact

### Scenario: 100 Subscribed Churches

**Current (all on STANDARD)**:
```
100 churches × GHS 150/month = GHS 15,000/month
```

**With 20% upgrading to UNLIMITED**:
```
80 churches × GHS 150 = GHS 12,000
20 churches × GHS 499 = GHS 9,980
Total = GHS 21,980/month (+46% revenue increase)
```

**Annual impact** (20% conversion):
```
Monthly increase: GHS 6,980
Annual increase: GHS 83,760
```

---

## Files Modified

### Backend
1. **src/main/resources/db/migration/V87__add_unlimited_storage_plan.sql** (NEW)
   - Creates UNLIMITED subscription plan
   - Sets storage_limit_mb = -1

2. **src/main/java/com/reuben/pastcare_spring/services/StorageEnforcementService.java**
   - Lines 47-64: Added unlimited storage check
   - Returns always-allowed for limitMb == -1

### Frontend
- **No changes needed** - Plans loaded dynamically from API

---

## Deployment Checklist

- [ ] Run database migration (`./mvnw flyway:migrate`)
- [ ] Verify UNLIMITED plan created in database
- [ ] Fix pre-existing compilation error in PaystackWebhookController
- [ ] Compile backend successfully
- [ ] Restart backend server
- [ ] Test unlimited storage upload on test church
- [ ] Verify pricing page shows both plans
- [ ] Test checkout flow for unlimited plan
- [ ] Update marketing materials with new plan

---

## Next Steps (Optional Enhancements)

### 1. Storage Analytics for Unlimited Plans
Even though unlimited, track usage for insights:
- Monthly growth trends
- Average file sizes
- Most storage-heavy features

### 2. Fair Use Policy
Consider adding:
- Reasonable use limits (e.g., no single file >10 GB)
- Bandwidth throttling for extreme usage
- Terms of service for unlimited storage

### 3. Premium Features for Unlimited
Could add exclusive features:
- Video sermon hosting
- Advanced media library
- Backup/restore functionality
- Multi-site replication

---

## Summary

**Changes**:
1. ✅ Created database migration for UNLIMITED plan (GHS 499/month)
2. ✅ Updated storage enforcement to handle -1 (unlimited)
3. ✅ Frontend automatically displays new plan via API

**Pricing Structure**:
```
STANDARD:  GHS 150/month (2 GB)
UNLIMITED: GHS 499/month (No Limits) ♾️
```

**Technical Approach**:
- `-1` in storage_limit_mb = unlimited storage
- Storage enforcement checks for -1 before applying limits
- Always allows uploads for unlimited plans
- Still tracks usage for analytics

---

**Status**: ✅ IMPLEMENTATION COMPLETE
**Date**: 2025-12-31
**Migration**: V87__add_unlimited_storage_plan.sql
**Backend**: StorageEnforcementService.java updated
**Frontend**: No changes needed (dynamic loading)
**Compilation**: Pending fix for unrelated PaystackWebhookController error
