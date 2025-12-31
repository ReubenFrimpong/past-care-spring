# Billing System Edge Cases - Implementation Progress

**Date:** 2025-12-31
**Status:** In Progress (Phases 1-3 Complete, Phase 4 Partially Complete)

## Completed Work

### ✅ PHASE 1: Database Schema (COMPLETE)

All three database migrations created and ready for deployment:

1. **V87__create_church_storage_addons_junction_table.sql**
   - Junction table tracking addon purchases
   - Supports prorated billing, renewal synchronization, status management
   - Includes purchase_price locking, proration fields, audit trail
   - Proper indexes for performance

2. **V88__enhance_payments_for_storage_addons.sql**
   - Added storage_addon_id foreign key to payments table
   - Added proration tracking fields (is_prorated, prorated_days, original_amount)
   - Constraints ensure data integrity
   - Supports new payment types: STORAGE_ADDON, STORAGE_ADDON_RENEWAL, STORAGE_ADDON_PRORATED

3. **V89__add_storage_limit_cache_to_churches.sql**
   - Added total_storage_limit_mb (denormalized cache)
   - Added storage_limit_updated_at timestamp
   - Initialized all existing churches to 2048 MB base limit

### ✅ PHASE 2: Domain Models & Repositories (COMPLETE)

1. **ChurchStorageAddon.java** - Full entity with:
   - ManyToOne relationship to StorageAddon (EAGER fetch)
   - Business methods: isActive(), markAsCanceled(), markAsSuspended(), reactivate()
   - Renewal date management
   - Audit timestamps with @PrePersist/@PreUpdate
   - Helper methods: getStorageCapacityMb(), calculateRenewalAmount()

2. **ChurchStorageAddonRepository.java** - Complete repository with:
   - findByChurchIdAndStatus() - get addons by status
   - existsByChurchIdAndStorageAddonIdAndStatus() - prevent duplicates
   - sumActiveStorageGbByChurchId() - calculate total addon storage
   - sumActiveStorageMbByChurchId() - MB version for limit checks
   - findActiveDueForRenewal() - for renewal processing
   - findSuspendedByChurchId() - for reactivation
   - Custom queries optimized for performance

3. **Church.java** - Modified with:
   - totalStorageLimitMb field (Long, default 2048)
   - storageLimitUpdatedAt field (LocalDateTime)
   - Denormalized cache for fast upload validation

### ✅ PHASE 3: Core Service - Storage Addon Billing (COMPLETE)

**StorageAddonBillingService.java** - Full implementation with all critical methods:

1. **purchaseStorageAddon()** - Initiate prorated addon purchase
   - Validates subscription is active or in grace period
   - Prevents duplicate purchases
   - Calculates prorated charge (daily rate × days remaining)
   - Creates Payment record with ADDON-{UUID} reference
   - Initializes Paystack payment
   - Returns PaymentInitializationResponse with authorization_url

2. **verifyAndActivateAddon()** - Webhook callback handler
   - Verifies payment with Paystack
   - Marks payment successful
   - Creates ChurchStorageAddon record (status=ACTIVE)
   - Syncs next_renewal_date with base subscription
   - Updates church total_storage_limit_mb cache

3. **calculateProration()** - Proration logic
   - Handles edge case: < 3 days remaining = charge full month
   - Daily rate = monthlyPrice / 30 days
   - Prorated amount = dailyRate × daysRemaining
   - Returns ProrationResult with amount, days, prorated flag

4. **updateChurchStorageLimit()** - Storage limit cache update
   - Formula: 2048 MB (base) + SUM(active addon storage)
   - Updates Church.totalStorageLimitMb
   - Sets storageLimitUpdatedAt timestamp

5. **cancelAddon()** - Cancel addon (no refund)
   - Marks status = CANCELED
   - Addon remains active until period end
   - Storage limit NOT updated until period expires

6. **suspendAddon()** / **reactivateAddon()** - Status management
   - For subscription suspension/activation

### ✅ PHASE 4: Custom Exceptions (COMPLETE)

1. **StorageLimitExceededException.java**
   - HTTP 413 Payload Too Large
   - Carries detailed metrics: currentUsageMb, limitMb, fileSizeMb, newTotalMb, percentageUsed
   - User-friendly message with suggested actions

2. **AddonAlreadyPurchasedException.java**
   - HTTP 409 Conflict
   - Prevents duplicate addon purchases

3. **SubscriptionRequiredForAddonException.java**
   - HTTP 402 Payment Required
   - Prevents addon purchases without active subscription

### ✅ Backend Compilation: SUCCESS

All code compiles successfully with no errors (only existing warnings unrelated to new code).

---

## Pending Work

### 🚧 PHASE 4: Storage Enforcement Service (IN PROGRESS)

**Files to create:**
- `services/StorageEnforcementService.java` - Pre-upload validation service
  - canUploadFile() method with 10MB technical buffer
  - Returns StorageCheckResult{allowed, currentUsageMb, limitMb, percentageUsed, errorMessage}

**Files to modify:**
- Apply enforcement in upload services (ImageService, MemberService, EventImageService)

### ⏳ PHASE 5: Enhanced Renewal Logic

**File to modify:** `services/BillingService.java`

1. **processSubscriptionRenewals()** - Add addon charging:
   - Calculate total: base + sum(active addons × billing period months)
   - Charge single payment for base + all addons
   - Update ALL addon renewal dates (keep synchronized)

2. **processRenewalWithPromoCredits()** - Cover addons:
   - When using promotional credit, extend addon dates too
   - Free month covers base + ALL active addons

### ⏳ PHASE 6: Webhook Routing

**File to modify:** `controllers/PaystackWebhookController.java`

Add routing for addon payments:
```java
if (reference.startsWith("ADDON-")) {
    return handleAddonPayment(reference, data);
}
```

### ⏳ PHASE 7: Edge Case Handlers

**File to modify:** `services/BillingService.java`

1. **suspendPastDueSubscriptions()** - Suspend addons too
2. **manuallyActivateSubscription()** - Reactivate suspended addons

### ⏳ PHASE 8: API Endpoints

**File to create:** `controllers/StorageAddonPurchaseController.java`

Endpoints:
- POST `/api/storage-addons/purchase` - Purchase addon
- GET `/api/storage-addons/my-addons` - List church's addons
- POST `/api/storage-addons/{addonId}/cancel` - Cancel addon

### ⏳ PHASE 9: Frontend Implementation

**File to modify:** `past-care-spring-frontend/src/app/billing-page/billing-page.ts`

Implement `purchaseAddon()` method (currently stubbed at line 531-534)

### ⏳ PHASE 10: Global Exception Handler

**File to modify:** `advice/GlobalExceptionHandler.java`

Add handlers for:
- StorageLimitExceededException → HTTP 413
- AddonAlreadyPurchasedException → HTTP 409
- SubscriptionRequiredForAddonException → HTTP 402

### ⏳ PHASE 11: Testing

**Unit Tests:**
- StorageAddonBillingServiceTest
- StorageEnforcementServiceTest
- BillingServiceRenewalTest

**Integration Tests:**
- AddonPurchaseIntegrationTest

**E2E Tests:**
- addon-purchase-flow.spec.ts
- storage-limit-enforcement.spec.ts
- subscription-renewal-with-addons.spec.ts

---

## Critical Path Summary

**What's Working:**
- ✅ Database schema ready
- ✅ All domain models and repositories
- ✅ Core addon billing logic (purchase, activation, proration)
- ✅ Storage limit cache system
- ✅ Custom exceptions
- ✅ Backend compiles successfully

**What's Needed for MVP:**
1. StorageEnforcementService (hard block on uploads)
2. Apply enforcement in upload services
3. Renewal logic modifications (charge base + addons)
4. Webhook routing for addon payments
5. API controller for addon purchase
6. Frontend implementation
7. Exception handler updates

**Estimated Remaining Effort:**
- Phase 4 (Storage Enforcement): 2-3 hours
- Phase 5 (Renewal Logic): 2-3 hours
- Phase 6 (Webhook Routing): 1 hour
- Phase 7 (Edge Cases): 1-2 hours
- Phase 8 (API Controller): 2 hours
- Phase 9 (Frontend): 3-4 hours
- Phase 10 (Exception Handlers): 1 hour
- Phase 11 (Testing): 6-8 hours
- **Total: 18-26 hours remaining**

---

## Next Steps

**Immediate priorities:**
1. Complete StorageEnforcementService
2. Apply storage enforcement in upload services
3. Modify BillingService renewal logic
4. Add webhook routing
5. Create API controller
6. Test end-to-end flow

**Deployment Strategy:**
- Deploy database migrations first (V87, V88, V89)
- Deploy backend core (Phase 2-3)
- Deploy enforcement (Phase 4) after testing
- Deploy frontend last

---

## Files Created/Modified

### New Files (11):
1. `db/migration/V87__create_church_storage_addons_junction_table.sql`
2. `db/migration/V88__enhance_payments_for_storage_addons.sql`
3. `db/migration/V89__add_storage_limit_cache_to_churches.sql`
4. `models/ChurchStorageAddon.java`
5. `repositories/ChurchStorageAddonRepository.java`
6. `services/StorageAddonBillingService.java`
7. `exceptions/StorageLimitExceededException.java`
8. `exceptions/AddonAlreadyPurchasedException.java`
9. `exceptions/SubscriptionRequiredForAddonException.java`
10. Legal pages (Privacy Policy, Terms, Cookie Policy) - COMPLETED IN PREVIOUS SESSION
11. This progress document

### Modified Files (1):
1. `models/Church.java` - Added storage limit cache fields

### Files to Create (5):
1. `services/StorageEnforcementService.java`
2. `controllers/StorageAddonPurchaseController.java`
3. `StorageAddonBillingServiceTest.java`
4. `StorageEnforcementServiceTest.java`
5. `e2e/addon-purchase-flow.spec.ts`

### Files to Modify (6):
1. `services/ImageService.java` - Add enforcement
2. `services/MemberService.java` - Add enforcement
3. `services/BillingService.java` - Renewal + promo + suspension + activation
4. `controllers/PaystackWebhookController.java` - Addon routing
5. `advice/GlobalExceptionHandler.java` - Exception handlers
6. `past-care-spring-frontend/src/app/billing-page/billing-page.ts` - Frontend impl

---

## Success Criteria (From Plan)

- [ ] Church can purchase storage addon with prorated first-month charge
- [ ] Addon activates immediately and increases storage limit
- [ ] File upload blocked when storage limit exceeded (hard block)
- [ ] Subscription renewal charges base + all active addons in single payment
- [ ] All addon renewal dates stay synchronized with base subscription
- [ ] Promotional credits cover base plan + all addons (free month)
- [ ] Subscription suspension also suspends all addons
- [ ] Manual activation by SUPERADMIN reactivates addons
- [ ] No revenue loss from missing addon charges
- [ ] Complete audit trail of all billing operations
