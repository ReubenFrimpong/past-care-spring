# PastCare SaaS - Architecture Fixes Implementation Plan

**Date**: 2025-12-28
**Status**: IN PROGRESS
**Priority**: CRITICAL

---

## Executive Summary

This document outlines the implementation plan for addressing critical architectural issues identified in [ARCHITECTURE_CRITICAL_ISSUES.md](ARCHITECTURE_CRITICAL_ISSUES.md), with specific modifications based on user requirements:

1. **Simplified Pricing Model** - Storage-based pricing at USD 9.99 (modified from complex tier system)
2. **Role-Based Access Control (RBAC)** - Complete permission system for both backend and frontend
3. **Multi-Tenancy Security** - Critical data leakage fixes
4. **Reports Module** - Defer Phase 2+ implementation

---

## Part 1: Simplified Pricing & Subscription Model

### User Requirement
> "I want to only limit by storage starting at USD 9.99"

### Proposed Pricing Model

```
┌──────────────────────────────────────────────────────────┐
│              PASTCARE SAAS PRICING                       │
├──────────────────────────────────────────────────────────┤
│ Base Price: USD 9.99/month                              │
│ Includes: 10 GB storage                                  │
│                                                          │
│ Additional Storage:                                      │
│ - USD 2.00 per additional 10 GB/month                   │
│                                                          │
│ Features: All features included (unlimited)              │
│ - Unlimited members                                      │
│ - Unlimited fellowships                                  │
│ - Unlimited users                                        │
│ - Full feature access                                    │
│ - Email & priority support                              │
│                                                          │
│ Trial: 30 days free (10 GB storage)                     │
└──────────────────────────────────────────────────────────┘
```

### Storage Calculation

**What counts towards storage:**
1. **Event Images** - Photos uploaded to events
2. **Member Profile Photos** - Member avatar images
3. **Church Logo** - Church branding assets
4. **Document Attachments** - PDF receipts, reports, exports
5. **Backup Files** - Automated database backups

**Storage Tracking**:
```java
public class StorageUsageTracker {

    public long calculateChurchStorage(Long churchId) {
        long totalBytes = 0;

        // Event images
        totalBytes += eventImageRepository.sumFileSizeByChurch(churchId);

        // Member photos
        totalBytes += memberRepository.sumPhotoSizeByChurch(churchId);

        // Church assets
        totalBytes += churchRepository.getAssetsSizeById(churchId);

        // Documents and exports
        totalBytes += documentRepository.sumFileSizeByChurch(churchId);

        return totalBytes;
    }

    public long convertToGB(long bytes) {
        return bytes / (1024 * 1024 * 1024);
    }
}
```

---

### Database Schema (Simplified)

```sql
-- Simplified subscription plan (single plan with storage tiers)
CREATE TABLE subscription_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL DEFAULT 'STANDARD',
    display_name VARCHAR(100) NOT NULL DEFAULT 'PastCare Standard',

    -- Pricing
    base_price_usd DECIMAL(10, 2) NOT NULL DEFAULT 9.99,
    base_storage_gb INT NOT NULL DEFAULT 10,
    additional_storage_price_per_10gb DECIMAL(10, 2) NOT NULL DEFAULT 2.00,

    currency VARCHAR(3) DEFAULT 'USD',
    trial_days INT DEFAULT 30,
    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Church subscriptions
CREATE TABLE church_subscription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL UNIQUE,
    plan_id BIGINT NOT NULL,

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'TRIAL',
    -- TRIAL, ACTIVE, PAST_DUE, CANCELLED, SUSPENDED

    -- Billing
    billing_cycle VARCHAR(20) DEFAULT 'MONTHLY',  -- MONTHLY, YEARLY
    current_period_start DATE NOT NULL,
    current_period_end DATE NOT NULL,
    next_billing_date DATE,

    -- Trial
    trial_start DATE,
    trial_end DATE,
    is_trial BOOLEAN DEFAULT FALSE,

    -- Storage
    storage_limit_gb INT NOT NULL DEFAULT 10,
    current_storage_gb DECIMAL(10, 2) DEFAULT 0.00,

    -- Payment
    last_payment_date DATE,
    last_payment_amount DECIMAL(10, 2),

    -- Cancellation
    cancel_at_period_end BOOLEAN DEFAULT FALSE,
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT,

    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_church_subscription_church
        FOREIGN KEY (church_id) REFERENCES church(id) ON DELETE CASCADE,
    CONSTRAINT fk_church_subscription_plan
        FOREIGN KEY (plan_id) REFERENCES subscription_plan(id),

    INDEX idx_subscription_status (status),
    INDEX idx_subscription_billing_date (next_billing_date),
    INDEX idx_subscription_trial (is_trial, trial_end)
);

-- Storage usage tracking
CREATE TABLE storage_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL,

    -- Breakdown by category
    event_images_bytes BIGINT DEFAULT 0,
    member_photos_bytes BIGINT DEFAULT 0,
    church_assets_bytes BIGINT DEFAULT 0,
    documents_bytes BIGINT DEFAULT 0,
    backups_bytes BIGINT DEFAULT 0,
    other_bytes BIGINT DEFAULT 0,

    -- Total
    total_bytes BIGINT GENERATED ALWAYS AS (
        event_images_bytes + member_photos_bytes + church_assets_bytes +
        documents_bytes + backups_bytes + other_bytes
    ) STORED,

    total_gb DECIMAL(10, 2) GENERATED ALWAYS AS (
        total_bytes / 1073741824  -- Convert bytes to GB
    ) STORED,

    -- Last calculation
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_storage_usage_church
        FOREIGN KEY (church_id) REFERENCES church(id) ON DELETE CASCADE,

    INDEX idx_storage_church (church_id)
);

-- Payment transactions
CREATE TABLE payment_transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_subscription_id BIGINT NOT NULL,

    -- Payment details
    amount_usd DECIMAL(10, 2) NOT NULL,
    description TEXT,

    -- Payment gateway (Paystack for MVP)
    payment_provider VARCHAR(50) DEFAULT 'PAYSTACK',
    provider_transaction_id VARCHAR(255),
    provider_reference VARCHAR(255),

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- PENDING, PROCESSING, SUCCESS, FAILED, REFUNDED

    transaction_type VARCHAR(30) NOT NULL,
    -- SUBSCRIPTION, STORAGE_UPGRADE, REFUND

    -- Receipt
    receipt_url VARCHAR(500),
    invoice_number VARCHAR(100),

    -- Timestamps
    processed_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_subscription
        FOREIGN KEY (church_subscription_id)
        REFERENCES church_subscription(id) ON DELETE CASCADE,

    INDEX idx_payment_subscription (church_subscription_id),
    INDEX idx_payment_status (status),
    INDEX idx_payment_provider (provider_transaction_id)
);
```

---

### Pricing Calculation Logic

```java
@Service
public class PricingService {

    public BigDecimal calculateMonthlyPrice(ChurchSubscription subscription) {
        SubscriptionPlan plan = subscription.getPlan();

        // Base price
        BigDecimal basePrice = plan.getBasePriceUsd();  // 9.99

        // Calculate storage overage
        int storageLimit = subscription.getStorageLimitGb();  // 10 GB
        BigDecimal currentStorage = subscription.getCurrentStorageGb();

        if (currentStorage.compareTo(BigDecimal.valueOf(storageLimit)) <= 0) {
            // Within limit, just base price
            return basePrice;
        }

        // Calculate additional storage needed
        BigDecimal overage = currentStorage.subtract(BigDecimal.valueOf(storageLimit));
        int additionalBlocks = (int) Math.ceil(overage.doubleValue() / 10.0);

        // Additional storage price: $2.00 per 10 GB block
        BigDecimal storagePrice = plan.getAdditionalStoragePricePer10Gb()
            .multiply(BigDecimal.valueOf(additionalBlocks));

        return basePrice.add(storagePrice);
    }

    /**
     * Example calculations:
     * - 5 GB used: $9.99
     * - 10 GB used: $9.99
     * - 15 GB used: $11.99 ($9.99 + $2.00 for 1 block)
     * - 25 GB used: $13.99 ($9.99 + $4.00 for 2 blocks)
     * - 100 GB used: $27.99 ($9.99 + $18.00 for 9 blocks)
     */
}
```

---

### Storage Quota Enforcement

```java
@Aspect
@Component
public class StorageQuotaAspect {

    @Autowired
    private StorageUsageService storageUsageService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Before("@annotation(StorageCheck)")
    public void checkStorageQuota(JoinPoint joinPoint)
            throws StorageQuotaExceededException {

        Long churchId = RequestContextUtil.extractChurchId();

        // Get current usage
        StorageUsage usage = storageUsageService.getUsage(churchId);
        ChurchSubscription subscription = subscriptionService.getSubscription(churchId);

        // Check if storage is full
        if (usage.getTotalGb().compareTo(
                BigDecimal.valueOf(subscription.getStorageLimitGb())) >= 0) {

            throw new StorageQuotaExceededException(
                "Storage limit reached. Upgrade your storage plan to continue."
            );
        }
    }
}

// Usage in controllers
@PostMapping("/events/{eventId}/images")
@StorageCheck
public ResponseEntity<EventImageResponse> uploadEventImage(
        @PathVariable Long eventId,
        @RequestParam("file") MultipartFile file) {
    // Image upload logic
}
```

---

## Part 2: Role-Based Access Control (RBAC)

### Defined Roles for PastCare

Based on the document analysis and typical church management needs:

```
┌─────────────────────────────────────────────────────────────────┐
│                    PASTCARE APPLICATION ROLES                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│ 1. SUPERADMIN (Platform Level)                                 │
│    - Full platform access                                      │
│    - Manage all churches                                       │
│    - Billing & subscription management                         │
│    - System configuration                                      │
│    - Global analytics & reporting                              │
│                                                                 │
│ 2. ADMIN (Church Administrator)                                │
│    - Full church access                                        │
│    - Manage church settings                                    │
│    - User & role management                                    │
│    - All member, fellowship, events CRUD                       │
│    - View all financial data                                   │
│    - Export data                                               │
│    - Subscription management                                   │
│                                                                 │
│ 3. PASTOR                                                      │
│    - View all members & households                             │
│    - Edit member pastoral data (care needs, visits)            │
│    - Create & manage care needs                                │
│    - Assign care tasks                                         │
│    - View & create events                                      │
│    - Send communications                                       │
│    - View attendance & pastoral reports                        │
│                                                                 │
│ 4. TREASURER                                                   │
│    - View all financial data                                   │
│    - Record donations                                          │
│    - Manage campaigns & pledges                                │
│    - Generate financial reports                                │
│    - Issue receipts                                            │
│    - Export financial data                                     │
│    - Manage payment methods                                    │
│                                                                 │
│ 5. FELLOWSHIP_LEADER                                           │
│    - View members in own fellowship(s)                         │
│    - Edit own fellowship info                                  │
│    - Record fellowship attendance                              │
│    - Send fellowship messages                                  │
│    - Create fellowship events                                  │
│    - View fellowship analytics                                 │
│                                                                 │
│ 6. MEMBER_MANAGER                                              │
│    - Full member CRUD                                          │
│    - Manage households                                         │
│    - Import/export members                                     │
│    - Manage member tags                                        │
│    - View member analytics                                     │
│    - No financial access                                       │
│                                                                 │
│ 7. MEMBER (Regular church member)                             │
│    - View own profile                                          │
│    - Edit own basic info (limited fields)                      │
│    - View fellowship membership                                │
│    - View church events                                        │
│    - Register for events                                       │
│    - View own giving history                                   │
│    - Submit prayer requests                                    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

### Permission Matrix

```java
public enum Permission {
    // ========== MEMBER PERMISSIONS ==========
    MEMBER_VIEW_ALL,           // View all members in church
    MEMBER_VIEW_OWN,           // View only own profile
    MEMBER_VIEW_FELLOWSHIP,    // View members in own fellowship
    MEMBER_CREATE,
    MEMBER_EDIT_ALL,
    MEMBER_EDIT_OWN,           // Edit own basic info only
    MEMBER_EDIT_PASTORAL,      // Edit pastoral data (care needs, visits)
    MEMBER_DELETE,
    MEMBER_EXPORT,
    MEMBER_IMPORT,

    // ========== HOUSEHOLD PERMISSIONS ==========
    HOUSEHOLD_VIEW,
    HOUSEHOLD_CREATE,
    HOUSEHOLD_EDIT,
    HOUSEHOLD_DELETE,

    // ========== FELLOWSHIP PERMISSIONS ==========
    FELLOWSHIP_VIEW_ALL,
    FELLOWSHIP_VIEW_OWN,       // View own fellowship
    FELLOWSHIP_CREATE,
    FELLOWSHIP_EDIT_ALL,
    FELLOWSHIP_EDIT_OWN,
    FELLOWSHIP_DELETE,
    FELLOWSHIP_MANAGE_MEMBERS, // Add/remove members

    // ========== FINANCIAL PERMISSIONS ==========
    DONATION_VIEW_ALL,
    DONATION_VIEW_OWN,         // View own giving history
    DONATION_CREATE,
    DONATION_EDIT,
    DONATION_DELETE,
    DONATION_EXPORT,
    CAMPAIGN_VIEW,
    CAMPAIGN_MANAGE,
    PLEDGE_VIEW_ALL,
    PLEDGE_VIEW_OWN,
    PLEDGE_MANAGE,
    RECEIPT_ISSUE,

    // ========== EVENTS PERMISSIONS ==========
    EVENT_VIEW_ALL,
    EVENT_VIEW_PUBLIC,         // View public events only
    EVENT_CREATE,
    EVENT_EDIT_ALL,
    EVENT_EDIT_OWN,            // Edit events created by user
    EVENT_DELETE,
    EVENT_REGISTER,            // Register for events
    EVENT_MANAGE_REGISTRATIONS,

    // ========== ATTENDANCE PERMISSIONS ==========
    ATTENDANCE_VIEW_ALL,
    ATTENDANCE_VIEW_FELLOWSHIP,
    ATTENDANCE_RECORD,
    ATTENDANCE_EDIT,

    // ========== PASTORAL CARE PERMISSIONS ==========
    CARE_NEED_VIEW_ALL,
    CARE_NEED_VIEW_ASSIGNED,   // View only assigned care needs
    CARE_NEED_CREATE,
    CARE_NEED_EDIT,
    CARE_NEED_ASSIGN,
    VISIT_VIEW_ALL,
    VISIT_CREATE,
    VISIT_EDIT,
    PRAYER_REQUEST_VIEW_ALL,
    PRAYER_REQUEST_CREATE,
    PRAYER_REQUEST_EDIT,

    // ========== COMMUNICATION PERMISSIONS ==========
    SMS_SEND,
    SMS_SEND_FELLOWSHIP,       // Send only to own fellowship
    EMAIL_SEND,
    BULK_MESSAGE_SEND,

    // ========== REPORTS PERMISSIONS ==========
    REPORT_MEMBER,
    REPORT_FINANCIAL,
    REPORT_ATTENDANCE,
    REPORT_ANALYTICS,
    REPORT_EXPORT,

    // ========== ADMIN PERMISSIONS ==========
    USER_VIEW,
    USER_CREATE,
    USER_EDIT,
    USER_DELETE,
    USER_MANAGE_ROLES,
    CHURCH_SETTINGS_VIEW,
    CHURCH_SETTINGS_EDIT,
    SUBSCRIPTION_VIEW,
    SUBSCRIPTION_MANAGE,

    // ========== SUPERADMIN PERMISSIONS ==========
    PLATFORM_ACCESS,           // Access platform admin panel
    ALL_CHURCHES_VIEW,
    ALL_CHURCHES_MANAGE,
    BILLING_MANAGE,
    SYSTEM_CONFIG
}
```

---

### Role-Permission Mapping

```java
@Getter
public enum Role {
    SUPERADMIN(Set.of(
        Permission.PLATFORM_ACCESS,
        Permission.ALL_CHURCHES_VIEW,
        Permission.ALL_CHURCHES_MANAGE,
        Permission.BILLING_MANAGE,
        Permission.SYSTEM_CONFIG
        // Superadmin has access to everything
    )),

    ADMIN(Set.of(
        // Members
        Permission.MEMBER_VIEW_ALL,
        Permission.MEMBER_CREATE,
        Permission.MEMBER_EDIT_ALL,
        Permission.MEMBER_DELETE,
        Permission.MEMBER_EXPORT,
        Permission.MEMBER_IMPORT,

        // Households
        Permission.HOUSEHOLD_VIEW,
        Permission.HOUSEHOLD_CREATE,
        Permission.HOUSEHOLD_EDIT,
        Permission.HOUSEHOLD_DELETE,

        // Fellowships
        Permission.FELLOWSHIP_VIEW_ALL,
        Permission.FELLOWSHIP_CREATE,
        Permission.FELLOWSHIP_EDIT_ALL,
        Permission.FELLOWSHIP_DELETE,
        Permission.FELLOWSHIP_MANAGE_MEMBERS,

        // Financial (view only)
        Permission.DONATION_VIEW_ALL,
        Permission.CAMPAIGN_VIEW,
        Permission.PLEDGE_VIEW_ALL,
        Permission.REPORT_FINANCIAL,

        // Events
        Permission.EVENT_VIEW_ALL,
        Permission.EVENT_CREATE,
        Permission.EVENT_EDIT_ALL,
        Permission.EVENT_DELETE,
        Permission.EVENT_MANAGE_REGISTRATIONS,

        // Attendance
        Permission.ATTENDANCE_VIEW_ALL,
        Permission.ATTENDANCE_RECORD,
        Permission.ATTENDANCE_EDIT,

        // Pastoral Care
        Permission.CARE_NEED_VIEW_ALL,
        Permission.CARE_NEED_CREATE,
        Permission.CARE_NEED_EDIT,
        Permission.CARE_NEED_ASSIGN,
        Permission.VISIT_VIEW_ALL,
        Permission.VISIT_CREATE,
        Permission.VISIT_EDIT,
        Permission.PRAYER_REQUEST_VIEW_ALL,
        Permission.PRAYER_REQUEST_CREATE,
        Permission.PRAYER_REQUEST_EDIT,

        // Communication
        Permission.SMS_SEND,
        Permission.EMAIL_SEND,
        Permission.BULK_MESSAGE_SEND,

        // Reports
        Permission.REPORT_MEMBER,
        Permission.REPORT_FINANCIAL,
        Permission.REPORT_ATTENDANCE,
        Permission.REPORT_ANALYTICS,
        Permission.REPORT_EXPORT,

        // Admin
        Permission.USER_VIEW,
        Permission.USER_CREATE,
        Permission.USER_EDIT,
        Permission.USER_DELETE,
        Permission.USER_MANAGE_ROLES,
        Permission.CHURCH_SETTINGS_VIEW,
        Permission.CHURCH_SETTINGS_EDIT,
        Permission.SUBSCRIPTION_VIEW,
        Permission.SUBSCRIPTION_MANAGE
    )),

    PASTOR(Set.of(
        // Members
        Permission.MEMBER_VIEW_ALL,
        Permission.MEMBER_EDIT_PASTORAL,

        // Households
        Permission.HOUSEHOLD_VIEW,

        // Fellowships
        Permission.FELLOWSHIP_VIEW_ALL,

        // Events
        Permission.EVENT_VIEW_ALL,
        Permission.EVENT_CREATE,
        Permission.EVENT_EDIT_OWN,
        Permission.EVENT_REGISTER,

        // Attendance
        Permission.ATTENDANCE_VIEW_ALL,
        Permission.ATTENDANCE_RECORD,

        // Pastoral Care
        Permission.CARE_NEED_VIEW_ALL,
        Permission.CARE_NEED_CREATE,
        Permission.CARE_NEED_EDIT,
        Permission.CARE_NEED_ASSIGN,
        Permission.VISIT_VIEW_ALL,
        Permission.VISIT_CREATE,
        Permission.VISIT_EDIT,
        Permission.PRAYER_REQUEST_VIEW_ALL,
        Permission.PRAYER_REQUEST_CREATE,
        Permission.PRAYER_REQUEST_EDIT,

        // Communication
        Permission.SMS_SEND,
        Permission.EMAIL_SEND,

        // Reports
        Permission.REPORT_MEMBER,
        Permission.REPORT_ATTENDANCE,
        Permission.REPORT_ANALYTICS
    )),

    TREASURER(Set.of(
        // Financial
        Permission.DONATION_VIEW_ALL,
        Permission.DONATION_CREATE,
        Permission.DONATION_EDIT,
        Permission.DONATION_DELETE,
        Permission.DONATION_EXPORT,
        Permission.CAMPAIGN_VIEW,
        Permission.CAMPAIGN_MANAGE,
        Permission.PLEDGE_VIEW_ALL,
        Permission.PLEDGE_MANAGE,
        Permission.RECEIPT_ISSUE,

        // Reports
        Permission.REPORT_FINANCIAL,
        Permission.REPORT_EXPORT,

        // Members (view only for donor management)
        Permission.MEMBER_VIEW_ALL
    )),

    FELLOWSHIP_LEADER(Set.of(
        // Members
        Permission.MEMBER_VIEW_FELLOWSHIP,

        // Fellowships
        Permission.FELLOWSHIP_VIEW_OWN,
        Permission.FELLOWSHIP_EDIT_OWN,

        // Events
        Permission.EVENT_VIEW_ALL,
        Permission.EVENT_CREATE,
        Permission.EVENT_EDIT_OWN,

        // Attendance
        Permission.ATTENDANCE_VIEW_FELLOWSHIP,
        Permission.ATTENDANCE_RECORD,

        // Communication
        Permission.SMS_SEND_FELLOWSHIP,
        Permission.EMAIL_SEND
    )),

    MEMBER_MANAGER(Set.of(
        // Members
        Permission.MEMBER_VIEW_ALL,
        Permission.MEMBER_CREATE,
        Permission.MEMBER_EDIT_ALL,
        Permission.MEMBER_DELETE,
        Permission.MEMBER_EXPORT,
        Permission.MEMBER_IMPORT,

        // Households
        Permission.HOUSEHOLD_VIEW,
        Permission.HOUSEHOLD_CREATE,
        Permission.HOUSEHOLD_EDIT,
        Permission.HOUSEHOLD_DELETE,

        // Reports
        Permission.REPORT_MEMBER,
        Permission.REPORT_ANALYTICS,
        Permission.REPORT_EXPORT
    )),

    MEMBER(Set.of(
        // Members
        Permission.MEMBER_VIEW_OWN,
        Permission.MEMBER_EDIT_OWN,

        // Fellowships
        Permission.FELLOWSHIP_VIEW_OWN,

        // Financial
        Permission.DONATION_VIEW_OWN,
        Permission.PLEDGE_VIEW_OWN,

        // Events
        Permission.EVENT_VIEW_PUBLIC,
        Permission.EVENT_REGISTER,

        // Pastoral Care
        Permission.PRAYER_REQUEST_CREATE
    ));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public boolean hasPermission(Permission permission) {
        // SUPERADMIN has all permissions
        if (this == SUPERADMIN) {
            return true;
        }
        return permissions.contains(permission);
    }

    public boolean hasAnyPermission(Permission... permissions) {
        return Arrays.stream(permissions)
            .anyMatch(this::hasPermission);
    }

    public boolean hasAllPermissions(Permission... permissions) {
        return Arrays.stream(permissions)
            .allMatch(this::hasPermission);
    }
}
```

---

## Part 3: Multi-Tenancy Security Implementation

### Critical Security Fixes (Week 1-2)

#### 1. TenantContextFilter

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TenantContextFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = extractToken(request);

            if (token != null && jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.extractUserId(token);
                Long jwtChurchId = jwtUtil.extractChurchId(token);
                String role = jwtUtil.extractRole(token);

                // CRITICAL: Validate JWT churchId matches database
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

                Long dbChurchId = user.getChurch() != null
                    ? user.getChurch().getId()
                    : null;

                // Security check
                if (!"SUPERADMIN".equals(role)) {
                    if (jwtChurchId == null || !jwtChurchId.equals(dbChurchId)) {
                        throw new TenantViolationException(
                            "JWT churchId mismatch. Possible token tampering."
                        );
                    }
                }

                // Set tenant context
                if (dbChurchId != null) {
                    TenantContext.setCurrentChurchId(dbChurchId);
                    TenantContext.setCurrentUserId(userId);
                    TenantContext.setCurrentUserRole(role);
                }

                // Audit logging
                MDC.put("churchId", String.valueOf(dbChurchId));
                MDC.put("userId", String.valueOf(userId));
                MDC.put("role", role);
            }

            filterChain.doFilter(request, response);

        } finally {
            // CRITICAL: Clear context
            TenantContext.clear();
            MDC.clear();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
```

#### 2. Enhanced TenantContext

```java
public class TenantContext {
    private static final ThreadLocal<Long> CHURCH_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ROLE = new ThreadLocal<>();

    public static void setCurrentChurchId(Long churchId) {
        CHURCH_ID.set(churchId);
    }

    public static Long getCurrentChurchId() {
        return CHURCH_ID.get();
    }

    public static void setCurrentUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getCurrentUserId() {
        return USER_ID.get();
    }

    public static void setCurrentUserRole(String role) {
        USER_ROLE.set(role);
    }

    public static String getCurrentUserRole() {
        return USER_ROLE.get();
    }

    public static boolean isSuperadmin() {
        return "SUPERADMIN".equals(USER_ROLE.get());
    }

    public static void clear() {
        CHURCH_ID.remove();
        USER_ID.remove();
        USER_ROLE.remove();
    }
}
```

---

## Implementation Timeline

### Week 1-2: Critical Security (HIGHEST PRIORITY)
- ✅ Implement TenantContextFilter
- ✅ Add JWT churchId validation
- ✅ Enable Hibernate filters
- ✅ Test multi-tenancy isolation
- ✅ Deploy to staging

### Week 3-4: RBAC Backend
- ✅ Create Permission enum
- ✅ Update Role enum with permissions
- ✅ Create @RequirePermission annotation
- ✅ Implement PermissionCheckAspect
- ✅ Update all critical endpoints
- ✅ Add role management endpoints

### Week 5-6: RBAC Frontend
- ✅ Create HasPermissionDirective
- ✅ Update all pages with permission checks
- ✅ Hide unauthorized UI elements
- ✅ Add role management UI (admin)
- ✅ Test permission matrix

### Week 7-8: Simplified Billing
- ✅ Create subscription schema
- ✅ Implement storage tracking
- ✅ Create pricing calculation
- ✅ Integrate Paystack
- ✅ Build subscription management UI
- ✅ Implement trial period

### Week 9-10: Testing & Polish
- ✅ E2E security testing
- ✅ Penetration testing
- ✅ Performance testing
- ✅ Documentation
- ✅ Production deployment

---

## Next Steps

1. **Start with TenantContextFilter** - Highest priority security fix
2. **Implement RBAC Backend** - Permission system
3. **Implement RBAC Frontend** - Permission directives
4. **Add Simplified Billing** - Storage-based pricing

---

**Document Status**: Ready for Implementation
**Created**: 2025-12-28
**Implementation Start**: Immediate
