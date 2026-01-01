# Tier Enforcement Security - Preventing Pricing Bypass

## Overview

**Critical Security Feature**: This document explains how PastCare prevents churches from bypassing pricing tier limitations through bulk member uploads or any other means.

---

## The Security Problem

### Without Tier Enforcement:
```
Church on "Small Church" tier (1-200 members max) at $5.99/month
↓
Church has 180 members
↓
Church attempts bulk upload of 500 members from CSV
↓
🚨 WITHOUT ENFORCEMENT: Upload succeeds → Church now has 680 members
↓
💰 Church pays $5.99/month for 680 members (should be Enterprise tier at $22.99/month)
↓
❌ REVENUE LOSS: $17/month per church = $204/year
```

### With Tier Enforcement:
```
Church on "Small Church" tier (1-200 members max) at $5.99/month
↓
Church has 180 members
↓
Church attempts bulk upload of 500 members from CSV
↓
✅ WITH ENFORCEMENT: System checks tier limit BEFORE import
↓
⛔ Upload BLOCKED: "Tier limit exceeded. Current: 180, Max: 200, Attempting: 500"
↓
💡 Church must upgrade to appropriate tier first
↓
✅ REVENUE PROTECTED: Church upgrades to Enterprise tier ($22.99/month)
```

---

## Implementation

### 1. TierEnforcementService.java

**Location**: `/src/main/java/com/reuben/pastcare_spring/services/TierEnforcementService.java`

**Key Methods**:

```java
/**
 * Check if church can add N members without exceeding tier limit.
 *
 * @param churchId Church attempting to add members
 * @param membersToAdd Number of members to add (1 for individual, N for bulk)
 * @return TierCheckResult with allowed flag and metrics
 */
public TierCheckResult canAddMembers(Long churchId, int membersToAdd)

/**
 * Enforce tier limit - throws exception if would exceed.
 * USE THIS in all member creation endpoints.
 *
 * @throws TierLimitExceededException if limit would be exceeded
 */
public void enforceTierLimit(Long churchId, int membersToAdd)
```

**How It Works**:

1. Fetches church's current pricing tier
2. Gets actual member count from database (not cached)
3. Calculates: `newTotal = currentCount + membersToAdd`
4. Checks: `newTotal <= tierMax + buffer` (buffer = 1% to handle edge cases)
5. Returns `TierCheckResult` with:
   - `allowed`: true/false
   - `currentMemberCount`: current member count
   - `tierMaxMembers`: tier's max (null = unlimited)
   - `membersToAdd`: number attempting to add
   - `newTotalMembers`: what total would be after addition
   - `percentageUsed`: percentage of tier limit (for warnings)
   - `message`: user-friendly error/success message

---

### 2. Integration Points

**All member creation operations MUST call `enforceTierLimit()` BEFORE creating members:**

#### A. Individual Member Creation

```java
// MemberService.java - createMember()
public MemberResponse createMember(MemberRequest request, Long churchId) {
    // ✅ ENFORCE TIER LIMIT FIRST
    tierEnforcementService.enforceTierLimit(churchId, 1);

    // Then create member
    Member member = new Member();
    // ... member creation logic
}
```

#### B. Bulk Member Import

```java
// MemberService.java - bulkImportMembers()
public MemberBulkImportResponse bulkImportMembers(MemberBulkImportRequest request, Long churchId) {
    // ✅ ENFORCE TIER LIMIT FIRST - CHECK ENTIRE BATCH
    int membersToAdd = request.members().size();
    tierEnforcementService.enforceTierLimit(churchId, membersToAdd);

    // Then process bulk import
    for (Map<String, String> rowData : request.members()) {
        // ... import logic
    }
}
```

#### C. Member CSV/Excel Upload

```java
// MembersController.java - uploadMembers()
@PostMapping("/upload")
public ResponseEntity<MemberBulkImportResponse> uploadMembers(
    @RequestParam("file") MultipartFile file,
    @RequestParam Long churchId) {

    // Parse CSV/Excel first to get count
    List<Map<String, String>> members = csvParser.parse(file);

    // ✅ ENFORCE TIER LIMIT BEFORE PROCESSING
    tierEnforcementService.enforceTierLimit(churchId, members.size());

    // Then process upload
    return memberService.bulkImportMembers(members, churchId);
}
```

---

### 3. Exception Handling

**Exception**: `TierLimitExceededException`

**HTTP Status**: `403 Forbidden`

**Error Response**:
```json
{
  "status": 403,
  "error": "TIER_LIMIT_EXCEEDED",
  "title": "Tier Member Limit Exceeded",
  "message": "Tier limit exceeded: Cannot add 500 member(s). Current: 180 members, After addition: 680 members, Tier max: 200 members (340.0% of limit). Please upgrade your subscription tier to add more members.",
  "path": "/api/members/bulk-import",
  "details": {
    "currentMemberCount": 180,
    "tierMaxMembers": 200,
    "membersToAdd": 500,
    "newTotalMembers": 680,
    "percentageUsed": 340.0,
    "upgradeRecommendation": "Consider upgrading to Enterprise tier (unlimited members).",
    "suggestedAction": "Upgrade your pricing tier to add more members"
  }
}
```

**Frontend Handling**:
```typescript
// Handle tier limit error
if (error.error === 'TIER_LIMIT_EXCEEDED') {
  const details = error.details;

  showDialog({
    title: 'Tier Limit Exceeded',
    message: `You cannot add ${details.membersToAdd} members. ` +
             `Your current tier allows up to ${details.tierMaxMembers} members. ` +
             `You currently have ${details.currentMemberCount} members.`,
    actions: [
      { label: 'View Pricing Tiers', action: () => router.navigate('/billing') },
      { label: 'Cancel' }
    ]
  });
}
```

---

### 4. Tier Limits by Plan

| Tier | Display Name | Min Members | Max Members | Monthly Price |
|------|--------------|-------------|-------------|---------------|
| TIER_1 | Small Church | 1 | 200 | $5.99 (GHS 75) |
| TIER_2 | Growing Church | 201 | 500 | $9.99 (GHS 120) |
| TIER_3 | Medium Church | 501 | 1000 | $13.99 (GHS 167.88) |
| TIER_4 | Large Church | 1001 | 2000 | $17.99 (GHS 216) |
| TIER_5 | Enterprise | 2001 | ∞ (NULL) | $22.99 (GHS 276) |

---

### 5. Edge Cases Handled

#### A. Concurrent Requests
**Scenario**: Two admins simultaneously try to add members
```
Admin A: Add 10 members (current: 195, max: 200)
Admin B: Add 10 members (current: 195, max: 200)

Without atomicity: Both succeed → 215 members (exceeds limit)
With enforcement: One succeeds, one fails (tier limit exceeded)
```

**Solution**: Use database-level member count check (`countByChurchId`), not cached count

#### B. Partial Bulk Imports
**Scenario**: Upload 500 members, 400 succeed then error occurs

**Current Behavior**:
```java
// ✅ Check limit BEFORE processing
tierEnforcementService.enforceTierLimit(churchId, 500);

// Then process all 500
// If error at row 401, first 400 are already saved
```

**Options**:
1. **Transactional** (all-or-nothing): Wrap in `@Transactional` - if any row fails, rollback all
2. **Partial** (current): Allow partial imports, show which rows failed

**Recommendation**: Keep partial imports, but add tier check BEFORE import starts

#### C. Tier Boundary Members (Exactly at Limit)
**Scenario**: Church has exactly 200 members (tier max)

**Question**: Can they add 1 more?

**Answer**: NO - limit is INCLUSIVE
- Tier 1: 1-200 (max = 200)
- Church with 200 members is at 100% capacity
- Adding 1 more would require Tier 2 upgrade

**Buffer**: 1% buffer allows slight overage (205 members) to handle edge cases

#### D. Unlimited Tier (Enterprise)
**Scenario**: Church on Enterprise tier (max = NULL)

**Behavior**:
```java
if (tierMaxMembers == null) {
    // Unlimited - always allow
    return new TierCheckResult(true, ...);
}
```

#### E. Deleted/Inactive Members
**Question**: Do deleted members count toward limit?

**Answer**: NO - only ACTIVE members count
```java
long count = memberRepository.countByChurchId(churchId);
// This counts only non-deleted members
```

**Soft Delete**: If you implement soft delete, update query:
```java
@Query("SELECT COUNT(m) FROM Member m WHERE m.church.id = :churchId AND m.deletedAt IS NULL")
long countActiveByChurchId(@Param("churchId") Long churchId);
```

---

### 6. Warning System

**Purpose**: Notify churches BEFORE they hit limit

#### At 80% of Limit:
```
Warning: You've used 80% of your tier's member limit (160/200 members).
Consider upgrading soon.
```

#### At 95% of Limit:
```
CRITICAL: You've used 95% of your tier's member limit (190/200 members).
Upgrade to avoid service interruption.
```

**Implementation**:
```java
// TierEnforcementService.java
public boolean isApproachingTierLimit(Long churchId) {
    TierCheckResult result = canAddMembers(churchId, 0);
    return result.getPercentageUsed() >= 80.0;
}

public String getTierLimitWarning(Long churchId) {
    TierCheckResult result = canAddMembers(churchId, 0);

    if (result.getPercentageUsed() >= 95.0) {
        return "CRITICAL: You've used 95% of your tier's member limit...";
    } else if (result.getPercentageUsed() >= 80.0) {
        return "Warning: You've used 80% of your tier's member limit...";
    }

    return null; // No warning needed
}
```

**Display**: Show warning banner on dashboard and member list pages

---

### 7. Frontend Validation

**Pre-Upload Check**:
```typescript
// Before allowing CSV upload
async validateUpload(file: File, churchId: number) {
  const members = await this.parseCSV(file);
  const count = members.length;

  // Call backend to check tier limit
  const canAdd = await this.tierService.canAddMembers(churchId, count);

  if (!canAdd.allowed) {
    // Show error BEFORE upload
    this.showError({
      title: 'Tier Limit Exceeded',
      message: `Cannot upload ${count} members. ${canAdd.message}`,
      upgradeLink: '/billing/upgrade'
    });
    return false;
  }

  // Show warning if approaching limit
  if (canAdd.isApproachingLimit) {
    this.showWarning({
      message: `After upload, you'll be at ${canAdd.percentageUsed}% of your tier limit`,
      showUpgradeLink: true
    });
  }

  return true;
}
```

---

### 8. Testing Strategy

#### Unit Tests:
```java
@Test
void enforceTierLimit_withinLimit_shouldAllow() {
    // Church with 150 members, tier max 200
    // Attempt to add 30 members
    // Should succeed
}

@Test
void enforceTierLimit_exceedsLimit_shouldThrow() {
    // Church with 190 members, tier max 200
    // Attempt to add 50 members
    // Should throw TierLimitExceededException
}

@Test
void enforceTierLimit_exactlyAtLimit_shouldThrow() {
    // Church with 200 members, tier max 200
    // Attempt to add 1 member
    // Should throw (no room left)
}

@Test
void enforceTierLimit_unlimitedTier_shouldAllow() {
    // Church on Enterprise tier (max = null)
    // Attempt to add 10,000 members
    // Should succeed
}

@Test
void enforceTierLimit_bulkUpload500_exceedsLimit_shouldThrow() {
    // Church with 180 members, tier max 200
    // Attempt bulk upload of 500 members
    // Should throw immediately (before processing any rows)
}
```

#### Integration Tests:
```java
@Test
void bulkImportMembers_exceedsTierLimit_shouldFail() {
    // Setup church with 180 members on Tier 1 (max 200)
    // Prepare CSV with 500 members
    // POST /api/members/bulk-import
    // Expect 403 FORBIDDEN
    // Verify 0 members were imported
}
```

#### E2E Tests:
```typescript
test('bulk upload exceeding tier limit shows error', async ({ page }) => {
  // Login as church admin
  // Navigate to member import page
  // Upload CSV with 500 members
  // Expect error dialog
  // Expect upgrade suggestion
  // Verify no members were added
});
```

---

### 9. Performance Considerations

**Query Optimization**:
```java
// ✅ FAST: Direct count query
long count = memberRepository.countByChurchId(churchId);

// ❌ SLOW: Fetch all members then count
List<Member> members = memberRepository.findByChurchId(churchId);
int count = members.size();
```

**Caching Strategy**:
```java
// Option 1: No caching (safest, always accurate)
// Always query database for real-time count

// Option 2: Cache with short TTL (5 minutes)
@Cacheable(value = "memberCount", key = "#churchId")
public long getCachedMemberCount(Long churchId) {
    return memberRepository.countByChurchId(churchId);
}

// Option 3: Update cache on member add/delete
@CacheEvict(value = "memberCount", key = "#churchId")
public void evictMemberCountCache(Long churchId) {
    // Called after member add/delete
}
```

**Recommendation**: Start with Option 1 (no caching) for accuracy, optimize later if needed

---

### 10. Audit Trail

**Log all tier limit violations**:
```java
logger.warn("Tier limit exceeded for church {}: " +
    "Current: {}, Tier max: {}, Attempting: {}, New total: {} ({:.1f}% of limit)",
    churchId,
    currentCount,
    tierMax,
    membersToAdd,
    newTotal,
    percentage);
```

**Track in database** (optional):
```sql
CREATE TABLE tier_limit_violations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    church_id BIGINT NOT NULL,
    current_member_count INT NOT NULL,
    tier_max_members INT NOT NULL,
    attempted_to_add INT NOT NULL,
    violation_type ENUM('INDIVIDUAL', 'BULK_IMPORT'),
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_church (church_id),
    INDEX idx_attempted_at (attempted_at)
);
```

**Use cases**:
- Identify churches repeatedly hitting limits (upsell opportunity)
- Detect abuse attempts
- Analytics on tier usage patterns

---

## Summary

### Critical Security Rules:

1. ✅ **ALWAYS** call `tierEnforcementService.enforceTierLimit()` BEFORE creating members
2. ✅ **ALWAYS** check ENTIRE batch size for bulk imports (not per-row)
3. ✅ **ALWAYS** use real-time database count (not cached)
4. ✅ **ALWAYS** throw exception if limit exceeded (never silently fail)
5. ✅ **ALWAYS** provide clear error messages with upgrade path

### Integration Checklist:

- [ ] Individual member creation enforces tier limit
- [ ] Bulk member import enforces tier limit
- [ ] CSV/Excel upload enforces tier limit
- [ ] Exception handler returns proper error format
- [ ] Frontend displays tier limit errors
- [ ] Frontend pre-validates uploads
- [ ] Warning system notifies at 80%/95%
- [ ] Unit tests cover all edge cases
- [ ] Integration tests verify enforcement
- [ ] E2E tests verify user experience
- [ ] Audit logging tracks violations

---

**Status**: ✅ TIER ENFORCEMENT SERVICE CREATED
**Next Step**: Integrate into MemberService bulk import
**Date**: 2026-01-01
