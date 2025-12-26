# PastCare SaaS - Critical Architectural Issues & Solutions

**Date**: December 26, 2025
**Status**: CRITICAL - Requires Immediate Attention
**Severity**: HIGH - Production blockers identified

---

## Executive Summary

This document outlines three critical architectural gaps in the PastCare SaaS church management system that must be addressed before production deployment:

1. **Billing & Subscription Management** - No billing system exists for SaaS revenue
2. **Multi-Tenant Data Leakage** - Severe vulnerabilities in tenant isolation
3. **Role-Based Access Control (RBAC)** - Inadequate authorization and permissions

Each issue has been analyzed in depth with concrete implementation recommendations.

---

## Issue #1: SaaS Billing & Subscription Management

### Current State: ❌ NO BILLING SYSTEM EXISTS

**Problem**: The application is designed as a SaaS platform but has:
- No subscription tiers or pricing model
- No payment processing integration
- No trial period management
- No usage limits or quota enforcement
- No billing admin interface
- No invoice generation
- No payment failure handling

**Business Impact**:
- Cannot monetize the platform
- No revenue stream for sustainability
- Cannot scale operations
- No way to manage church subscriptions

---

### Recommended Solution: Complete Billing Module

#### A. Subscription Tiers & Pricing Model

**Proposed Tiers**:

```
┌─────────────┬──────────┬────────────┬─────────────┬────────────┐
│ Feature     │ FREE     │ BASIC      │ PRO         │ ENTERPRISE │
├─────────────┼──────────┼────────────┼─────────────┼────────────┤
│ Members     │ 50       │ 200        │ 1,000       │ Unlimited  │
│ Fellowships │ 3        │ 10         │ Unlimited   │ Unlimited  │
│ SMS/month   │ 0        │ 100        │ 500         │ Unlimited  │
│ Storage     │ 500MB    │ 5GB        │ 50GB        │ Unlimited  │
│ Users       │ 2        │ 5          │ 20          │ Unlimited  │
│ Price/month │ GHS 0    │ GHS 99     │ GHS 499     │ Custom     │
│ Trial       │ N/A      │ 14 days    │ 14 days     │ 30 days    │
└─────────────┴──────────┴────────────┴─────────────┴────────────┘
```

**Additional Features by Tier**:
- **FREE**: Basic member management, limited attendance tracking
- **BASIC**: + Online giving (3% + GHS 0.50 fee), basic reports, email support
- **PRO**: + SMS communications, advanced analytics, custom fields, priority support
- **ENTERPRISE**: + API access, custom integrations, dedicated account manager, SLA

---

#### B. Database Schema for Billing

```sql
-- Subscription plans
CREATE TABLE subscription_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,  -- FREE, BASIC, PRO, ENTERPRISE
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    price_monthly DECIMAL(10, 2) NOT NULL,
    price_yearly DECIMAL(10, 2),  -- Discounted annual price
    currency VARCHAR(3) DEFAULT 'GHS',

    -- Limits
    max_members INT,
    max_fellowships INT,
    max_sms_per_month INT,
    max_storage_mb BIGINT,
    max_users INT,

    -- Features (JSON for flexibility)
    features JSON,  -- {"online_giving": true, "sms": false, "api_access": false}

    trial_days INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    display_order INT DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_plan_active (is_active, display_order)
);

-- Church subscriptions
CREATE TABLE church_subscription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL UNIQUE,  -- One active subscription per church
    plan_id BIGINT NOT NULL,

    -- Subscription status
    status VARCHAR(20) NOT NULL DEFAULT 'TRIAL',  -- TRIAL, ACTIVE, PAST_DUE, CANCELLED, SUSPENDED

    -- Billing cycle
    billing_cycle VARCHAR(20) DEFAULT 'MONTHLY',  -- MONTHLY, YEARLY
    current_period_start DATE NOT NULL,
    current_period_end DATE NOT NULL,

    -- Trial management
    trial_start DATE,
    trial_end DATE,
    is_trial BOOLEAN DEFAULT FALSE,

    -- Payment tracking
    next_billing_date DATE,
    last_payment_date DATE,
    last_payment_amount DECIMAL(10, 2),

    -- Cancellation
    cancel_at_period_end BOOLEAN DEFAULT FALSE,
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT,

    -- Usage tracking
    current_members_count INT DEFAULT 0,
    current_sms_count INT DEFAULT 0,
    current_storage_mb BIGINT DEFAULT 0,

    -- Metadata
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_church_subscription_church FOREIGN KEY (church_id) REFERENCES church(id) ON DELETE CASCADE,
    CONSTRAINT fk_church_subscription_plan FOREIGN KEY (plan_id) REFERENCES subscription_plan(id),

    INDEX idx_subscription_status (status),
    INDEX idx_subscription_billing_date (next_billing_date),
    INDEX idx_subscription_trial (is_trial, trial_end)
);

-- Payment transactions
CREATE TABLE payment_transaction_billing (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_subscription_id BIGINT NOT NULL,

    -- Payment details
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'GHS',
    payment_method VARCHAR(30),  -- MOBILE_MONEY, BANK_TRANSFER, CREDIT_CARD

    -- Payment gateway
    payment_provider VARCHAR(50),  -- PAYSTACK, STRIPE, FLUTTERWAVE
    provider_transaction_id VARCHAR(255),
    provider_reference VARCHAR(255),

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, PROCESSING, SUCCESS, FAILED, REFUNDED

    -- Transaction type
    transaction_type VARCHAR(30) NOT NULL,  -- SUBSCRIPTION, UPGRADE, ADDON, REFUND

    -- Metadata
    description TEXT,
    receipt_url VARCHAR(500),
    invoice_number VARCHAR(100),

    -- Timestamps
    processed_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_billing_subscription FOREIGN KEY (church_subscription_id)
        REFERENCES church_subscription(id) ON DELETE CASCADE,

    INDEX idx_payment_billing_subscription (church_subscription_id),
    INDEX idx_payment_billing_status (status),
    INDEX idx_payment_billing_provider (provider_transaction_id)
);

-- Invoices
CREATE TABLE invoice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_subscription_id BIGINT NOT NULL,
    invoice_number VARCHAR(100) NOT NULL UNIQUE,

    -- Invoice details
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'GHS',
    tax_amount DECIMAL(10, 2) DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL,

    -- Period
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',  -- DRAFT, SENT, PAID, VOID, UNCOLLECTIBLE

    -- Due dates
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    paid_date DATE,

    -- Line items (JSON)
    line_items JSON,  -- [{"description": "Pro Plan - Monthly", "amount": 499.00}]

    -- Files
    pdf_url VARCHAR(500),

    -- Payment
    payment_transaction_id BIGINT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_invoice_subscription FOREIGN KEY (church_subscription_id)
        REFERENCES church_subscription(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoice_payment FOREIGN KEY (payment_transaction_id)
        REFERENCES payment_transaction_billing(id) ON DELETE SET NULL,

    INDEX idx_invoice_subscription (church_subscription_id),
    INDEX idx_invoice_status (status),
    INDEX idx_invoice_due_date (due_date)
);

-- Usage tracking (for metered billing and quota enforcement)
CREATE TABLE usage_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL,

    -- Usage type
    usage_type VARCHAR(50) NOT NULL,  -- MEMBER_ADDED, SMS_SENT, STORAGE_USED, API_CALL

    -- Quantity
    quantity INT DEFAULT 1,

    -- Metadata
    resource_id BIGINT,  -- ID of the resource (member_id, message_id, etc.)
    metadata JSON,

    -- Timestamps
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,

    CONSTRAINT fk_usage_church FOREIGN KEY (church_id) REFERENCES church(id) ON DELETE CASCADE,

    INDEX idx_usage_church_period (church_id, period_start, period_end),
    INDEX idx_usage_type (usage_type)
);
```

---

#### C. Payment Gateway Integration

**Primary Gateway: Paystack** (already integrated for donations)

**Additional Options**:
1. **Stripe** - Global coverage, excellent developer experience
2. **Flutterwave** - African market focus
3. **Mobile Money** - MTN Mobile Money, Vodafone Cash (Ghana specific)

**Implementation**:
```java
@Service
public class SubscriptionPaymentService {

    @Autowired
    private PaystackService paystackService;

    public PaymentInitializationResponse initializeSubscriptionPayment(
        Church church,
        SubscriptionPlan plan,
        BillingCycle cycle
    ) {
        BigDecimal amount = cycle == BillingCycle.YEARLY
            ? plan.getPriceYearly()
            : plan.getPriceMonthly();

        PaymentInitializationRequest request = PaymentInitializationRequest.builder()
            .email(church.getEmail())
            .amount(amount)
            .metadata(Map.of(
                "church_id", church.getId(),
                "plan_id", plan.getId(),
                "billing_cycle", cycle.name(),
                "payment_type", "SUBSCRIPTION"
            ))
            .callbackUrl(env.getProperty("app.billing.callback-url"))
            .build();

        return paystackService.initializePayment(request);
    }

    @Scheduled(cron = "0 0 2 * * *")  // Run at 2 AM daily
    public void processBillingCycle() {
        // Find subscriptions due for renewal
        LocalDate today = LocalDate.now();
        List<ChurchSubscription> dueSubscriptions =
            subscriptionRepository.findByNextBillingDate(today);

        for (ChurchSubscription subscription : dueSubscriptions) {
            try {
                // Charge saved payment method
                processSubscriptionRenewal(subscription);
            } catch (PaymentFailedException e) {
                handlePaymentFailure(subscription, e);
            }
        }
    }
}
```

---

#### D. Quota Enforcement

**Implementation Strategy**:
```java
@Aspect
@Component
public class QuotaEnforcementAspect {

    @Before("@annotation(QuotaCheck)")
    public void checkQuota(JoinPoint joinPoint) throws QuotaExceededException {
        QuotaCheck annotation = getAnnotation(joinPoint);
        Long churchId = extractChurchId(joinPoint);

        ChurchSubscription subscription = subscriptionService.getSubscription(churchId);
        SubscriptionPlan plan = subscription.getPlan();

        switch (annotation.quotaType()) {
            case MEMBERS:
                int currentMembers = memberRepository.countByChurchId(churchId);
                if (currentMembers >= plan.getMaxMembers()) {
                    throw new QuotaExceededException(
                        "Member limit reached. Upgrade to add more members."
                    );
                }
                break;
            case SMS:
                int smsThisMonth = usageRepository.countSmsThisMonth(churchId);
                if (smsThisMonth >= plan.getMaxSmsPerMonth()) {
                    throw new QuotaExceededException(
                        "Monthly SMS limit reached. Upgrade for more messages."
                    );
                }
                break;
            // ... other quota types
        }
    }
}

// Usage in controllers
@PostMapping("/members")
@QuotaCheck(quotaType = QuotaType.MEMBERS)
public ResponseEntity<MemberResponse> createMember(@RequestBody MemberRequest request) {
    // Member creation logic
}
```

---

#### E. Trial Management

**Auto-upgrade Logic**:
```java
@Scheduled(cron = "0 0 3 * * *")  // Run at 3 AM daily
public void checkExpiredTrials() {
    LocalDate today = LocalDate.now();
    List<ChurchSubscription> expiredTrials =
        subscriptionRepository.findExpiredTrials(today);

    for (ChurchSubscription subscription : expiredTrials) {
        if (subscription.hasPaymentMethod()) {
            // Auto-convert to paid subscription
            upgradeFromTrial(subscription);
        } else {
            // Downgrade to FREE plan
            downgradeToFree(subscription);
            emailService.sendTrialExpiredNotification(subscription.getChurch());
        }
    }
}
```

---

#### F. Billing Admin Interface

**Superadmin Features Needed**:
- View all church subscriptions
- Manually upgrade/downgrade churches
- Issue refunds
- View payment transactions
- Generate revenue reports
- Handle failed payments
- Export billing data

**Church Admin Features**:
- View current plan and usage
- Upgrade/downgrade subscription
- Update payment method
- View invoices and receipts
- Cancel subscription
- Download tax documents

---

### Implementation Priority

**Phase 1: Foundation (Week 1-2)**
1. Create subscription_plan, church_subscription tables
2. Seed initial subscription plans (FREE, BASIC, PRO)
3. Assign all existing churches to FREE plan
4. Create SubscriptionService with basic CRUD

**Phase 2: Payment Integration (Week 3-4)**
5. Integrate Paystack for subscription payments
6. Create checkout flow for plan upgrades
7. Implement webhook handling for payment confirmations
8. Build invoice generation

**Phase 3: Quota Enforcement (Week 5-6)**
9. Implement @QuotaCheck aspect
10. Add quota validation to all resource-creating endpoints
11. Build usage tracking system
12. Create quota exceeded UI/messaging

**Phase 4: Trial & Lifecycle (Week 7-8)**
13. Implement trial period logic
14. Build scheduled tasks for billing cycles
15. Create payment failure handling
16. Build subscription cancellation flow

**Phase 5: Admin Interface (Week 9-10)**
17. Build superadmin billing dashboard
18. Create church admin subscription page
19. Implement plan comparison page
20. Build revenue analytics

---

## Issue #2: Multi-Tenant Data Leakage Vulnerabilities

### Current State: ❌ CRITICAL SECURITY FLAWS

**Security Audit Findings** (see full report from exploration agent):

1. **Hibernate Filters Not Enabled** - Filters defined but never activated in request pipeline
2. **TenantContext Never Set** - ThreadLocal exists but not populated
3. **Manual churchId Extraction** - Error-prone, inconsistent across controllers
4. **No JWT Validation** - churchId claims trusted without backend verification
5. **Unfiltered Queries** - Repository methods search across all tenants
6. **No Relationship Guards** - Loading associated entities can leak cross-tenant data

**Risk Level**: **CRITICAL** - Production deployment would expose all church data to any authenticated user

---

### Recommended Solution: Comprehensive Multi-Tenancy Security

#### A. Automatic Tenant Context Filter

**Create Servlet Filter**:
```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)  // After JwtAuthenticationFilter
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
            // Extract JWT from request
            String token = extractToken(request);

            if (token != null && jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.extractUserId(token);
                Long jwtChurchId = jwtUtil.extractChurchId(token);
                String role = jwtUtil.extractRole(token);

                // CRITICAL: Validate JWT churchId matches database
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

                Long dbChurchId = user.getChurch() != null ? user.getChurch().getId() : null;

                // Security check: JWT churchId must match database churchId
                // Exception: SUPERADMIN can have null churchId
                if (!"SUPERADMIN".equals(role)) {
                    if (jwtChurchId == null || !jwtChurchId.equals(dbChurchId)) {
                        throw new TenantViolationException(
                            "JWT churchId mismatch. Possible token tampering."
                        );
                    }
                }

                // Set tenant context for this request
                if (dbChurchId != null) {
                    TenantContext.setCurrentChurchId(dbChurchId);
                }

                // Log for audit
                MDC.put("churchId", String.valueOf(dbChurchId));
                MDC.put("userId", String.valueOf(userId));
                MDC.put("role", role);
            }

            filterChain.doFilter(request, response);

        } finally {
            // CRITICAL: Clear tenant context after request
            TenantContext.clear();
            MDC.clear();
        }
    }
}
```

---

#### B. Enable Hibernate Filters Automatically

**Hibernate Interceptor**:
```java
@Component
public class TenantInterceptor extends EmptyInterceptor {

    @Override
    public String onPrepareStatement(String sql) {
        Long churchId = TenantContext.getCurrentChurchId();

        if (churchId != null) {
            // Log all SQL for audit
            log.debug("Executing SQL for church {}: {}", churchId, sql);

            // Validate that SQL contains church_id filter
            if (sql.contains("FROM") && !sql.contains("church_id")) {
                log.warn("SECURITY WARNING: Unfiltered query detected: {}", sql);
            }
        }

        return super.onPrepareStatement(sql);
    }
}

@Configuration
public class HibernateConfig {

    @Bean
    public FilterRegistrationBean<TenantFilterActivator> tenantFilterActivator() {
        FilterRegistrationBean<TenantFilterActivator> registrationBean =
            new FilterRegistrationBean<>();

        registrationBean.setFilter(new TenantFilterActivator());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 11);

        return registrationBean;
    }
}

public class TenantFilterActivator extends OncePerRequestFilter {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        Long churchId = TenantContext.getCurrentChurchId();

        if (churchId != null) {
            EntityManager em = entityManagerFactory.createEntityManager();
            Session session = em.unwrap(Session.class);

            // Enable Hibernate filter for this request
            Filter filter = session.enableFilter("churchFilter");
            filter.setParameter("churchId", churchId);

            log.debug("Enabled Hibernate churchFilter for churchId: {}", churchId);
        }

        filterChain.doFilter(request, response);
    }
}
```

---

#### C. Repository Security Layer

**Secure Base Repository**:
```java
@NoRepositoryBean
public interface SecureRepository<T, ID> extends JpaRepository<T, ID> {

    /**
     * Find by ID with tenant validation
     */
    default Optional<T> findByIdSecure(ID id) {
        Long churchId = TenantContext.getCurrentChurchId();
        if (churchId == null) {
            throw new TenantContextMissingException();
        }

        Optional<T> result = findById(id);

        // Validate entity belongs to current tenant
        if (result.isPresent() && result.get() instanceof TenantBaseEntity) {
            TenantBaseEntity entity = (TenantBaseEntity) result.get();
            if (!churchId.equals(entity.getChurch().getId())) {
                log.error("SECURITY VIOLATION: Attempted cross-tenant access. " +
                    "User churchId: {}, Entity churchId: {}",
                    churchId, entity.getChurch().getId());
                throw new TenantViolationException("Access denied");
            }
        }

        return result;
    }

    /**
     * Find all for current tenant only
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.church.id = :churchId")
    List<T> findAllForCurrentTenant(@Param("churchId") Long churchId);
}

// Update all repositories
@Repository
public interface MemberRepository extends SecureRepository<Member, Long> {
    // All existing methods remain, plus secure methods from base
}
```

---

#### D. AOP-Based Tenant Validation

**Aspect for Automatic Validation**:
```java
@Aspect
@Component
public class TenantSecurityAspect {

    /**
     * Intercept all service methods that return TenantBaseEntity
     */
    @AfterReturning(
        pointcut = "execution(* com.reuben.pastcare_spring.services.*.*(..))",
        returning = "result"
    )
    public void validateTenantOwnership(Object result) {
        if (result instanceof TenantBaseEntity) {
            validateEntity((TenantBaseEntity) result);
        } else if (result instanceof Collection) {
            ((Collection<?>) result).forEach(item -> {
                if (item instanceof TenantBaseEntity) {
                    validateEntity((TenantBaseEntity) item);
                }
            });
        }
    }

    private void validateEntity(TenantBaseEntity entity) {
        Long currentChurchId = TenantContext.getCurrentChurchId();
        Long entityChurchId = entity.getChurch() != null ? entity.getChurch().getId() : null;

        if (currentChurchId != null && !currentChurchId.equals(entityChurchId)) {
            log.error("SECURITY VIOLATION: Service returned entity from wrong tenant. " +
                "Expected: {}, Got: {}", currentChurchId, entityChurchId);
            throw new TenantViolationException("Cross-tenant data access detected");
        }
    }
}
```

---

#### E. Database-Level Row Security (PostgreSQL Option)

If migrating to PostgreSQL, enable Row-Level Security:

```sql
-- Enable RLS on all tenant tables
ALTER TABLE member ENABLE ROW LEVEL SECURITY;
ALTER TABLE fellowship ENABLE ROW LEVEL SECURITY;
-- ... repeat for all tenant tables

-- Create policy: Users can only see their church's data
CREATE POLICY church_isolation_policy ON member
    USING (church_id = current_setting('app.current_church_id')::bigint);

CREATE POLICY church_isolation_policy ON fellowship
    USING (church_id = current_setting('app.current_church_id')::bigint);
```

Set church_id at connection level:
```java
@Bean
public DataSource dataSource() {
    HikariDataSource ds = new HikariDataSource();
    ds.setConnectionInitSql("SET app.current_church_id = " +
        TenantContext.getCurrentChurchId());
    return ds;
}
```

---

#### F. Cross-Tenant Access Monitoring

**Audit Logging**:
```java
@Component
public class TenantAuditLogger {

    @EventListener
    public void onTenantViolation(TenantViolationEvent event) {
        log.error("SECURITY ALERT: Tenant violation detected. " +
            "User: {}, Attempted Church: {}, User's Church: {}, IP: {}, Endpoint: {}",
            event.getUserId(),
            event.getAttemptedChurchId(),
            event.getUserChurchId(),
            event.getIpAddress(),
            event.getEndpoint()
        );

        // Store in security_audit table
        securityAuditRepository.save(SecurityAudit.builder()
            .eventType("TENANT_VIOLATION")
            .userId(event.getUserId())
            .details(event.toJson())
            .timestamp(Instant.now())
            .severity("CRITICAL")
            .build());

        // Alert admins for repeated violations
        if (violationCount(event.getUserId()) > 3) {
            alertService.sendSecurityAlert(
                "Multiple tenant violations detected for user: " + event.getUserId()
            );
        }
    }
}
```

---

### Implementation Priority

**Phase 1: Critical Fixes (Week 1)**
1. Implement TenantContextFilter
2. Enable Hibernate filters automatically
3. Add JWT churchId validation
4. Deploy to staging and test

**Phase 2: Repository Security (Week 2)**
5. Create SecureRepository base interface
6. Update all repositories to extend SecureRepository
7. Add tenant validation to all findById calls
8. Test cross-tenant access scenarios

**Phase 3: AOP & Monitoring (Week 3)**
9. Implement TenantSecurityAspect
10. Add comprehensive audit logging
11. Create security dashboard for violations
12. Set up alerts for repeated violations

**Phase 4: Testing & Validation (Week 4)**
13. Write E2E tests for tenant isolation
14. Perform penetration testing
15. Load test with multi-tenant scenarios
16. Document security architecture

---

## Issue #3: Role-Based Access Control (RBAC)

### Current State: ❌ AUTHENTICATION ONLY, NO AUTHORIZATION

**Current Roles** (defined but not enforced):
- `SUPERADMIN` - Platform administrator
- `ADMIN` - Church administrator
- `TREASURER` - Financial officer
- `FELLOWSHIP_HEAD` - Fellowship leader

**Problems**:
1. Only `@PreAuthorize("isAuthenticated()")` used - no role checks
2. All authenticated users have full access to their church's data
3. Treasurers can modify member data (should only access finances)
4. Fellowship heads have no special permissions for their fellowships
5. No resource-level permissions (e.g., "can delete this specific member")

---

### Recommended Solution: Comprehensive RBAC System

#### A. Extended Role Hierarchy

**Proposed Roles**:

```
SUPERADMIN (Platform Level)
├── Manage all churches
├── Billing & subscriptions
├── System configuration
└── Global analytics

CHURCH_ADMIN (Church Level)
├── Manage church settings
├── Manage users and roles
├── Full member CRUD
├── Full fellowship CRUD
├── View all financial data
└── Export data

PASTOR (Church Level)
├── View all members
├── Edit member pastoral data
├── View care needs
├── Assign care tasks
├── View attendance reports
└── Create communication campaigns

TREASURER (Church Level)
├── View all financial data
├── Record donations
├── Manage campaigns & pledges
├── Generate financial reports
├── Issue receipts
└── Export financial data

FELLOWSHIP_LEADER (Fellowship Level)
├── View fellowship members
├── Edit own fellowship info
├── Record fellowship attendance
├── Send fellowship messages
├── Create fellowship events
└── View fellowship analytics

MEMBER_MANAGER (Church Level)
├── Full member CRUD
├── Manage households
├── Import/export members
├── Manage member tags
└── View member analytics

COMMUNICATIONS_MANAGER (Church Level)
├── Send SMS/Email campaigns
├── Manage communication templates
├── View communication logs
└── View message analytics

VOLUNTEER (Limited)
├── View assigned members only
├── Record assigned attendance
├── View assigned care needs
└── Update assigned tasks
```

---

#### B. Permission-Based Authorization

**Granular Permissions**:

```java
public enum Permission {
    // Member permissions
    MEMBER_VIEW,
    MEMBER_CREATE,
    MEMBER_EDIT,
    MEMBER_DELETE,
    MEMBER_EXPORT,

    // Fellowship permissions
    FELLOWSHIP_VIEW_ALL,
    FELLOWSHIP_VIEW_OWN,
    FELLOWSHIP_CREATE,
    FELLOWSHIP_EDIT_ALL,
    FELLOWSHIP_EDIT_OWN,
    FELLOWSHIP_DELETE,

    // Financial permissions
    DONATION_VIEW,
    DONATION_CREATE,
    DONATION_EDIT,
    DONATION_DELETE,
    DONATION_EXPORT,
    CAMPAIGN_MANAGE,
    PLEDGE_MANAGE,

    // Communication permissions
    SMS_SEND,
    EMAIL_SEND,
    BULK_MESSAGE_SEND,

    // Admin permissions
    USER_MANAGE,
    ROLE_ASSIGN,
    CHURCH_SETTINGS_EDIT,

    // Reports
    REPORT_FINANCIAL,
    REPORT_MEMBER_ANALYTICS,
    REPORT_ATTENDANCE,

    // Care permissions
    CARE_NEED_VIEW_ALL,
    CARE_NEED_VIEW_ASSIGNED,
    CARE_NEED_CREATE,
    CARE_NEED_ASSIGN,

    // Superadmin
    SUPERADMIN_ACCESS
}
```

**Role-Permission Mapping**:
```java
public enum Role {
    SUPERADMIN(Set.of(Permission.SUPERADMIN_ACCESS)),

    CHURCH_ADMIN(Set.of(
        Permission.MEMBER_VIEW, Permission.MEMBER_CREATE, Permission.MEMBER_EDIT,
        Permission.MEMBER_DELETE, Permission.MEMBER_EXPORT,
        Permission.FELLOWSHIP_VIEW_ALL, Permission.FELLOWSHIP_CREATE,
        Permission.FELLOWSHIP_EDIT_ALL, Permission.FELLOWSHIP_DELETE,
        Permission.USER_MANAGE, Permission.ROLE_ASSIGN,
        Permission.CHURCH_SETTINGS_EDIT,
        Permission.DONATION_VIEW, Permission.REPORT_FINANCIAL,
        Permission.REPORT_MEMBER_ANALYTICS, Permission.REPORT_ATTENDANCE
    )),

    TREASURER(Set.of(
        Permission.DONATION_VIEW, Permission.DONATION_CREATE,
        Permission.DONATION_EDIT, Permission.DONATION_DELETE,
        Permission.DONATION_EXPORT,
        Permission.CAMPAIGN_MANAGE, Permission.PLEDGE_MANAGE,
        Permission.REPORT_FINANCIAL
    )),

    FELLOWSHIP_LEADER(Set.of(
        Permission.FELLOWSHIP_VIEW_OWN, Permission.FELLOWSHIP_EDIT_OWN,
        Permission.MEMBER_VIEW,  // View members in their fellowship
        Permission.SMS_SEND  // Limited to fellowship members
    )),

    MEMBER_MANAGER(Set.of(
        Permission.MEMBER_VIEW, Permission.MEMBER_CREATE,
        Permission.MEMBER_EDIT, Permission.MEMBER_DELETE,
        Permission.MEMBER_EXPORT,
        Permission.REPORT_MEMBER_ANALYTICS
    )),

    COMMUNICATIONS_MANAGER(Set.of(
        Permission.SMS_SEND, Permission.EMAIL_SEND,
        Permission.BULK_MESSAGE_SEND
    )),

    VOLUNTEER(Set.of(
        Permission.CARE_NEED_VIEW_ASSIGNED,
        Permission.MEMBER_VIEW  // Limited scope
    ));

    private final Set<Permission> permissions;

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission) ||
               permissions.contains(Permission.SUPERADMIN_ACCESS);
    }
}
```

---

#### C. Database Schema for RBAC

```sql
-- Roles table (if roles become dynamic)
CREATE TABLE role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    level VARCHAR(20) NOT NULL,  -- PLATFORM, CHURCH, FELLOWSHIP, CUSTOM
    is_system BOOLEAN DEFAULT FALSE,  -- Cannot be deleted

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Permissions table
CREATE TABLE permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50),  -- MEMBER, FELLOWSHIP, FINANCIAL, etc.

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Role-Permission mapping
CREATE TABLE role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,

    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_perm_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_perm_permission FOREIGN KEY (permission_id) REFERENCES permission(id) ON DELETE CASCADE
);

-- User roles (support multiple roles per user)
CREATE TABLE user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,

    -- Scope (for FELLOWSHIP_LEADER role, which fellowship?)
    scope_type VARCHAR(20),  -- CHURCH, FELLOWSHIP, GLOBAL
    scope_id BIGINT,  -- fellowship_id if scope_type = FELLOWSHIP

    -- Metadata
    granted_by_id BIGINT,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,  -- Optional: temporary roles

    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_granted_by FOREIGN KEY (granted_by_id) REFERENCES user(id) ON DELETE SET NULL,

    INDEX idx_user_role_user (user_id),
    INDEX idx_user_role_scope (scope_type, scope_id)
);
```

---

#### D. Authorization Annotations

**Custom Annotations**:
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    Permission[] value();
    LogicalOperation operation() default LogicalOperation.OR;
}

public enum LogicalOperation {
    AND,  // Requires ALL permissions
    OR    // Requires ANY permission
}

// Usage examples:
@PostMapping("/members")
@RequirePermission(Permission.MEMBER_CREATE)
public ResponseEntity<MemberResponse> createMember(@RequestBody MemberRequest request) {
    // ...
}

@DeleteMapping("/members/{id}")
@RequirePermission({Permission.MEMBER_DELETE, Permission.SUPERADMIN_ACCESS})
public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
    // ...
}
```

**AOP Implementation**:
```java
@Aspect
@Component
public class PermissionCheckAspect {

    @Autowired
    private UserService userService;

    @Before("@annotation(requirePermission)")
    public void checkPermissions(JoinPoint joinPoint, RequirePermission requirePermission) {
        User currentUser = SecurityUtils.getCurrentUser();

        Permission[] requiredPermissions = requirePermission.value();
        LogicalOperation operation = requirePermission.operation();

        boolean hasAccess = switch (operation) {
            case AND -> hasAllPermissions(currentUser, requiredPermissions);
            case OR -> hasAnyPermission(currentUser, requiredPermissions);
        };

        if (!hasAccess) {
            log.warn("Access denied for user {} to {}. Required: {}",
                currentUser.getId(),
                joinPoint.getSignature().getName(),
                Arrays.toString(requiredPermissions)
            );
            throw new AccessDeniedException("Insufficient permissions");
        }
    }

    private boolean hasAllPermissions(User user, Permission[] permissions) {
        return Arrays.stream(permissions)
            .allMatch(p -> userService.hasPermission(user, p));
    }

    private boolean hasAnyPermission(User user, Permission[] permissions) {
        return Arrays.stream(permissions)
            .anyMatch(p -> userService.hasPermission(user, p));
    }
}
```

---

#### E. Resource-Level Authorization

**For Fellowship Leaders** (can only edit their own fellowship):

```java
@PutMapping("/fellowships/{id}")
@RequirePermission(Permission.FELLOWSHIP_EDIT_OWN)
public ResponseEntity<FellowshipResponse> updateFellowship(
    @PathVariable Long id,
    @RequestBody FellowshipRequest request
) {
    User currentUser = SecurityUtils.getCurrentUser();
    Fellowship fellowship = fellowshipRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Fellowship not found"));

    // Check if user is the leader of this fellowship
    if (!fellowship.getLeader().getId().equals(currentUser.getId())) {
        throw new AccessDeniedException("You can only edit your own fellowship");
    }

    // Update fellowship...
}
```

**Service-Level Authorization**:
```java
@Service
public class MemberService {

    public List<Member> getAccessibleMembers(User user) {
        if (user.hasPermission(Permission.MEMBER_VIEW)) {
            // Can view all members in church
            return memberRepository.findByChurch(user.getChurch());
        } else if (user.hasRole(Role.FELLOWSHIP_LEADER)) {
            // Can only view members in their fellowships
            List<Fellowship> ledFellowships =
                fellowshipRepository.findByLeader(user);
            return memberRepository.findByFellowshipsIn(ledFellowships);
        } else if (user.hasPermission(Permission.CARE_NEED_VIEW_ASSIGNED)) {
            // Can only view members with assigned care needs
            return memberRepository.findByAssignedCareNeeds(user);
        } else {
            return Collections.emptyList();
        }
    }
}
```

---

#### F. Frontend Permission Checks

**Angular Permission Directive**:
```typescript
@Directive({
  selector: '[hasPermission]'
})
export class HasPermissionDirective implements OnInit {
  @Input() hasPermission: Permission[];

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private authService: AuthService
  ) {}

  ngOnInit() {
    const hasAccess = this.hasPermission.some(p =>
      this.authService.hasPermission(p)
    );

    if (hasAccess) {
      this.viewContainer.createEmbeddedView(this.templateRef);
    } else {
      this.viewContainer.clear();
    }
  }
}

// Usage:
<button *hasPermission="['MEMBER_DELETE']" (click)="deleteMember()">
  Delete
</button>
```

---

#### G. Role Management Interface

**Superadmin Features**:
- Create custom roles
- Assign permissions to roles
- View role usage statistics

**Church Admin Features**:
- Assign roles to users
- View user permissions
- Cannot create custom roles (uses predefined)

**UI for Role Assignment**:
```html
<div class="role-assignment">
  <h3>Assign Roles to {{ user.name }}</h3>

  <div *ngFor="let role of availableRoles">
    <label>
      <input type="checkbox"
             [checked]="userHasRole(role)"
             (change)="toggleRole(role)">
      {{ role.displayName }}
    </label>

    <!-- Fellowship scope selector for FELLOWSHIP_LEADER -->
    <div *ngIf="role.name === 'FELLOWSHIP_LEADER' && userHasRole(role)">
      <select [(ngModel)]="fellowshipScope">
        <option *ngFor="let fellowship of fellowships"
                [value]="fellowship.id">
          {{ fellowship.name }}
        </option>
      </select>
    </div>
  </div>

  <button (click)="saveRoles()">Save Changes</button>
</div>
```

---

### Implementation Priority

**Phase 1: Core RBAC (Week 1-2)**
1. Create permission enum and role-permission mapping
2. Add permission check methods to User entity
3. Implement @RequirePermission annotation and aspect
4. Update critical endpoints with permission checks

**Phase 2: Database Schema (Week 3)**
5. Create role, permission, role_permission tables
6. Create user_role table for multiple roles
7. Migrate existing users to new role system
8. Seed initial permissions

**Phase 3: Service-Level Security (Week 4)**
9. Implement resource-level authorization
10. Add fellowship leader scope checks
11. Implement data filtering by user permissions
12. Add comprehensive audit logging

**Phase 4: Admin Interface (Week 5-6)**
13. Build role management UI (superadmin)
14. Build user role assignment UI (church admin)
15. Create permission matrix documentation
16. Build role usage analytics

---

## Recommended Implementation Timeline

### Immediate (Week 1-2): Multi-Tenancy Security Fixes
- **CRITICAL**: Implement TenantContextFilter
- **CRITICAL**: Enable Hibernate filters
- **CRITICAL**: Add JWT churchId validation
- Deploy and test thoroughly

### Short-Term (Week 3-6): RBAC Foundation
- Implement permission system
- Add @RequirePermission to all endpoints
- Build role management interface
- Update frontend with permission checks

### Medium-Term (Week 7-12): Billing System
- Create subscription schema
- Integrate payment gateway
- Build quota enforcement
- Implement trial management
- Create billing admin interface

### Long-Term (Month 4+): Advanced Features
- Custom roles and permissions
- Resource-level ACLs
- Advanced usage analytics
- Automated billing workflows
- Revenue reporting

---

## Testing Requirements

### Multi-Tenancy Tests
1. Attempt to access another church's data with valid token
2. Attempt JWT tampering (modify churchId claim)
3. Load test with multiple concurrent tenants
4. Test Hibernate filter activation
5. Test relationship loading across tenants

### RBAC Tests
1. Test each permission on each role
2. Test permission inheritance
3. Test fellowship-scoped permissions
4. Test resource ownership validation
5. Test frontend permission hiding

### Billing Tests
1. Test plan upgrade/downgrade flows
2. Test quota enforcement at each limit
3. Test payment failure scenarios
4. Test trial expiration handling
5. Test invoice generation

---

## Monitoring & Alerting

### Security Alerts
- Cross-tenant access attempts
- JWT tampering detection
- Repeated authorization failures
- Unusual permission usage patterns

### Billing Alerts
- Payment failures (retry 3x, then alert)
- Trial expirations approaching
- Quota approaching limits
- Subscription cancellations

### Performance Alerts
- Slow queries (> 1s)
- Tenant filter not applied
- High concurrent tenant load
- Database connection pool exhaustion

---

## Compliance Considerations

### Data Privacy (GDPR/PDPA)
- Multi-tenancy ensures church data isolation
- RBAC controls who can export data
- Audit logs track all data access
- Data deletion on church cancellation

### Financial Compliance
- PCI DSS for payment processing (delegated to Paystack/Stripe)
- Invoice generation with tax compliance
- Audit trail for all financial transactions
- Refund and chargeback handling

### Security Standards
- OWASP Top 10 compliance
- Regular penetration testing
- Security audit logging
- Incident response plan

---

## Summary

These three architectural issues are **production blockers** that must be resolved:

1. **Billing**: Implement complete subscription system with Paystack integration (10 weeks)
2. **Multi-Tenancy**: Fix critical data leakage vulnerabilities (4 weeks)
3. **RBAC**: Implement comprehensive permission system (6 weeks)

**Total Estimated Effort**: 20 weeks (~5 months)

**Recommended Approach**:
- Week 1-2: Multi-tenancy security fixes (CRITICAL)
- Week 3-6: RBAC foundation
- Week 7-16: Billing system
- Week 17-20: Polish, testing, documentation

This represents a significant investment but is **essential** for a secure, scalable, monetizable SaaS platform.

---

**Document Status**: Complete
**Last Updated**: 2025-12-26
**Next Review**: After Phase 1 multi-tenancy fixes deployed
