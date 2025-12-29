# Consolidated Pending Tasks - Single Source of Truth
**Date**: 2025-12-29
**Source**: Comprehensive scan of all MD files

---

## 🎯 HIGH PRIORITY (Revenue & User Management Focus)

### 1. Billing & Payment System (Paystack Integration) ⭐⭐⭐⭐⭐ CRITICAL
**Status**: 95% complete (Backend ✅ Complete, Frontend ✅ Complete, Integration Pending)
**Priority**: 🔴 **ABSOLUTE HIGHEST** - No revenue without this!
**Effort**: 2-3 weeks total (Backend ✅, Frontend ✅, Integration 15-30 min)
**Dependencies**: Storage Backend (✅ Complete), RBAC (✅ Complete)
**Last Updated**: 2025-12-29 16:30

**Why Critical**: **Cannot bill customers without this!** This is the revenue engine. Churches cannot pay for subscriptions, and we cannot monetize the platform without implementing Paystack recurring billing.

**Phase 1: Paystack Recurring Billing Backend** ✅ **COMPLETE** (2025-12-29)
- [x] Subscription Plan Entity
  - [x] Plans: STARTER (Free, 2GB, 5 users), PROFESSIONAL ($50/mo, 10GB, 50 users), ENTERPRISE ($150/mo, 50GB, unlimited)
  - [x] Migration V58__create_subscription_plans_table.sql with seed data
- [x] Church Subscription Entity
  - [x] status (TRIALING, ACTIVE, PAST_DUE, CANCELED, SUSPENDED)
  - [x] trial_end_date (14 days default), next_billing_date, payment_method
  - [x] paystackAuthorizationCode for recurring payments
  - [x] Auto-renewal, grace period (7 days), failed payment tracking
  - [x] Migration V59__create_church_subscriptions_table.sql + V61__add_paystack_authorization_code
  - [x] Auto-created subscriptions for existing churches (all on STARTER trial)
- [x] Payment Entity
  - [x] Track all transactions (amount, status, reference, card details, refunds)
  - [x] Migration V60__create_payments_table.sql
- [x] PaystackService (verified - already exists from donation module)
  - [x] initializePayment() - uses PaymentInitializationRequest DTO
  - [x] verifyPayment() - returns JsonNode
  - [x] chargeAuthorization() - for recurring payments
- [x] BillingService - Complete subscription lifecycle management
  - [x] createInitialSubscription() - 14-day trial on STARTER
  - [x] initializeSubscriptionPayment() - upgrade payment flow
  - [x] verifyAndActivateSubscription() - payment verification & activation
  - [x] cancelSubscription() / reactivateSubscription()
  - [x] downgradeToFreePlan()
  - [x] hasExceededStorageLimit() / hasExceededUserLimit()
  - [x] processSubscriptionRenewals() - scheduled task for recurring billing
  - [x] suspendPastDueSubscriptions() - grace period enforcement
  - [x] getSubscriptionStats() - platform-wide metrics
- [x] BillingController - REST API with RBAC
  - [x] GET /api/billing/subscription - view current subscription (SUBSCRIPTION_VIEW)
  - [x] GET /api/billing/plans - list available plans (public)
  - [x] GET /api/billing/plans/{id} - get plan details
  - [x] POST /api/billing/subscribe - initialize upgrade payment (SUBSCRIPTION_MANAGE)
  - [x] POST /api/billing/verify/{reference} - verify & activate (SUBSCRIPTION_MANAGE)
  - [x] POST /api/billing/cancel - cancel subscription (SUBSCRIPTION_MANAGE)
  - [x] POST /api/billing/reactivate - reactivate canceled (SUBSCRIPTION_MANAGE)
  - [x] POST /api/billing/downgrade-to-free - downgrade (SUBSCRIPTION_MANAGE)
  - [x] GET /api/billing/payments - payment history (SUBSCRIPTION_VIEW)
  - [x] GET /api/billing/payments/successful - successful payments only
  - [x] GET /api/billing/status - detailed subscription status
  - [x] GET /api/billing/stats - platform stats (PLATFORM_ACCESS - SUPERADMIN)
- [x] Repositories
  - [x] SubscriptionPlanRepository - plan queries
  - [x] ChurchSubscriptionRepository - subscription queries with stats
  - [x] PaymentRepository - payment queries with revenue calculations
- [x] Backend compilation verified ✅

**Phase 2: Subscription Management UI** ✅ **COMPLETE** (2025-12-29) - **INTEGRATION NEEDED**
- [x] Billing Page Component (~2,050 lines total)
  - [x] Current plan display with status badges
  - [x] Usage metrics (storage, users) with progress bars
  - [x] Plan comparison cards (3 plans)
  - [x] Upgrade/downgrade buttons with smart logic
  - [x] Trial countdown timer
  - [x] Responsive design (mobile-friendly)
- [x] Payment Flow
  - [x] Paystack payment modal integration
  - [x] Payment initialization & redirect
  - [x] Payment verification on callback
  - [x] Success/failure handling with messages
- [x] Payment History
  - [x] Table with all payments
  - [x] Payment status badges (color-coded)
  - [x] Card details display (last4, brand)
  - [x] Download invoices (PDF) - TODO: backend endpoint needed
- [x] Subscription Management
  - [x] Cancel subscription workflow with confirmation
  - [x] Reactivate subscription button
  - [x] Downgrade to free with confirmation
  - [x] Update payment method - TODO: future feature
- [x] TypeScript Interfaces (3 files)
  - [x] SubscriptionPlan, ChurchSubscription, Payment
  - [x] Helper functions for formatting & status badges
- [x] BillingService (Angular service)
  - [x] All 11 API endpoints wrapped
  - [x] Subscription caching with BehaviorSubject
  - [x] Helper methods for usage calculations
- [ ] **INTEGRATION REQUIRED** (15-30 min):
  - [ ] Add route to app routing configuration
  - [ ] Add "Billing" link to side navigation (SUBSCRIPTION_VIEW permission)
  - [ ] Move files from `/past-care-spring-frontend/src/app/` to actual app directory
  - [ ] See [BILLING_FRONTEND_INTEGRATION_GUIDE.md](BILLING_FRONTEND_INTEGRATION_GUIDE.md)

**Phase 3: Subscription Enforcement** (3-5 days)
- [ ] Backend Guards
  - [ ] Check subscription status before allowing operations
  - [ ] Enforce storage limits (reject uploads when exceeded)
  - [ ] Enforce user limits (prevent new user creation when exceeded)
  - [ ] Grace period handling (7 days past due before blocking)
- [ ] Frontend Guards
  - [ ] Show upgrade prompts when limits reached
  - [ ] Disable features for canceled/expired subscriptions
  - [ ] Trial countdown timer
- [ ] Email Notifications
  - [ ] Payment success email
  - [ ] Payment failure email
  - [ ] Subscription expiring soon (3 days before)
  - [ ] Trial ending soon (3 days before)
  - [ ] Subscription canceled confirmation

**Backend Needs**:
- Paystack API credentials (Secret Key, Public Key)
- Webhook signature verification
- SubscriptionPlan entity
- ChurchSubscription entity
- Payment entity
- PaystackService with API integration
- BillingService for subscription logic
- PaystackController with webhook endpoint
- Email templates for billing notifications

---

### 2. Admin Module - User Management ⭐⭐⭐⭐ CRITICAL
**Status**: 40% complete (basic CRUD exists)
**Priority**: 🔴 **HIGHEST** - Cannot manage users without this!
**Effort**: 2-3 weeks
**Dependencies**: RBAC (✅ Complete)
**Last Updated**: 2025-12-29

**Why Critical**: **Church admins cannot add/manage users!** This blocks user onboarding and team management.

**Phase 1: Enhanced User Management** (2 weeks)
- [ ] User Management UI Component
  - [ ] Users list page with grid/table view
  - [ ] User profile dialog (view/edit)
  - [ ] User creation dialog
  - [ ] User deactivation workflow
  - [ ] User activity log viewer
- [ ] User Roles & Permissions UI
  - [ ] Role assignment interface (dropdown with 8 roles: SUPERADMIN, ADMIN, PASTOR, TREASURER, FELLOWSHIP_LEADER, MEMBER_MANAGER, MEMBER, FELLOWSHIP_HEAD)
  - [ ] Permission viewer (show which permissions each role has)
  - [ ] Bulk role assignment
- [ ] User Invitation System
  - [ ] Send email invitations to new users
  - [ ] Invitation acceptance workflow
  - [ ] Invitation tracking (pending, accepted, expired)
- [ ] Password Management
  - [ ] Force password reset on first login
  - [ ] Password strength requirements UI
  - [ ] Password reset request workflow
- [ ] Backend Enhancements
  - [ ] User profile photo upload
  - [ ] User soft delete (isActive flag)
  - [ ] User last login tracking
  - [ ] User invitation entity and endpoints

**Phase 2: Church Settings** (1-2 weeks)
- [ ] Church Settings UI
  - [ ] Church profile section (logo, name, contact info)
  - [ ] Service times configuration
  - [ ] Fiscal year settings
  - [ ] Currency and locale settings
  - [ ] Timezone configuration
  - [ ] Branding settings (colors, fonts)
- [ ] Backend
  - [ ] ChurchSettings entity (key-value pairs)
  - [ ] Church logo upload
  - [ ] Settings CRUD endpoints

**Phase 3: Audit Logging UI** (1 week)
- [ ] Audit Log Viewer Component
  - [ ] Filterable audit log table (by user, action, entity, date)
  - [ ] Audit log detail view
  - [ ] Export to CSV
- [ ] System Health Dashboard
  - [ ] Uptime monitoring
  - [ ] Performance metrics
  - [ ] Error rate tracking

**Backend Already Has** (from RBAC implementation):
- ✅ Permission enum (79 permissions)
- ✅ Role definitions (8 roles with permission mappings)
- ✅ @RequirePermission annotation
- ✅ PermissionCheckAspect (AOP enforcement)
- ✅ User entity with church_id
- ✅ SecurityAuditLog entity
- ✅ TenantValidationService

**New Backend Needed**:
- UserInvitation entity
- ChurchSettings entity
- User photo upload endpoints
- Invitation email service

---

### 3. Platform Admin Dashboard ⭐⭐⭐ IMPORTANT
**Status**: ✅ **Phases 1 & 2 COMPLETE** (50% overall - 2/4 phases done)
**Priority**: 🟡 MEDIUM (After billing & user management)
**Effort**: 1-2 weeks (Phases 1 & 2 complete, Phases 3-4 remaining)
**Dependencies**: RBAC (✅ Complete), Storage Backend (✅ Complete)
**Last Updated**: 2025-12-29

**Why Critical**: Essential for platform developers to monitor all customers, troubleshoot issues, and manage the multi-tenant system effectively.

**Phase 1: Multi-Tenant Overview Dashboard** ✅ **COMPLETE** (2025-12-29)
- ✅ Platform Dashboard Page Component
  - ✅ `/platform-admin` route (SUPERADMIN only) - with `superAdminOnlyGuard`
  - ✅ Multi-church statistics cards (total churches, active users, total storage)
  - ✅ Reactive UI with Angular Signals
  - ✅ Modern control flow syntax (`@if`, `@for`)
- ✅ Church List Component
  - ✅ Searchable grid of all churches (real-time filtering)
  - ✅ Church status (active/inactive) with visual badges
  - ✅ Quick actions (view details, activate, deactivate)
  - ✅ Storage usage per church with color-coded progress bars
  - ✅ Sort functionality (name, storage, users, date)
  - ✅ Status filtering (all, active only, inactive only)
- ✅ Church Detail View Dialog
  - ✅ Complete church information display
  - ✅ Contact information section
  - ✅ Statistics (users, members)
  - ✅ Storage usage visualization
  - ✅ Quick actions (activate/deactivate from dialog)
  - ✅ Smooth animations and responsive design
- ✅ Backend Implementation
  - ✅ PlatformStatsController (aggregate stats across all churches)
  - ✅ Church activation/deactivation endpoints
  - ✅ PlatformService frontend integration
  - ✅ ChurchSummary model with all required fields

**Phase 1 Documentation**:
- ✅ SESSION_2025-12-29_CONTINUATION_COMPLETE.md - Route guards implementation
- ✅ PLATFORM_ADMIN_UI_FIXES_COMPLETE.md - Action buttons and filters fixes
- ✅ CHURCH_DETAIL_VIEW_IMPLEMENTATION_COMPLETE.md - Dialog implementation

**Phase 2: Security & Monitoring** ✅ **COMPLETE** (2025-12-29)
- ✅ Security Violations Dashboard
  - ✅ Real-time violation feed with pagination
  - ✅ Violation statistics by church/user with comprehensive stats
  - ✅ Detailed violation logs with enriched context (user names, church names)
  - ✅ Export violations to CSV
  - ⚠️ Alert configuration UI (Future enhancement - monitoring is complete)
- ✅ Backend endpoints (Enhanced with DTOs):
  - ✅ `GET /api/security/stats` - Returns SecurityStatsResponse with 8 metrics
  - ✅ `GET /api/security/violations/recent?limit=100` - Returns enriched SecurityViolationResponse[]
  - ✅ `GET /api/security/violations/user/{userId}` - Returns enriched violations
  - ✅ `GET /api/security/violations/church/{churchId}` - Returns enriched violations
- ✅ Frontend Components:
  - ✅ SecurityDashboardPage (security-dashboard-page/)
  - ✅ SecurityService for API integration
  - ✅ Security models and DTOs (SecurityStats, SecurityViolation, enums)
  - ✅ Navigation link in SUPERADMIN sidebar
  - ✅ Route with superAdminOnlyGuard
- ✅ Backend Enhancements:
  - ✅ SecurityViolationResponse DTO with user/church names
  - ✅ SecurityStatsResponse DTO with extended metrics
  - ✅ SecurityMonitoringService enriched methods
  - ✅ Batch loading optimization for users/churches

**Phase 3: Storage & Billing Management** (1 week)
- [ ] Storage Management Dashboard
  - [ ] Storage usage trends across all churches
  - [ ] Top storage consumers (churches)
  - [ ] Storage breakdown by type (files, database)
  - [ ] Storage alerts and warnings
- [ ] Billing Overview (Future)
  - [ ] Revenue metrics
  - [ ] Payment status per church
  - [ ] Subscription plans distribution
  - [ ] Overdue payments alerts
- [ ] Use existing backend endpoints:
  - ✅ `GET /api/storage/current/{churchId}`
  - ✅ `GET /api/storage/history/{churchId}`
  - ✅ `POST /api/storage/calculate/{churchId}`

**Phase 4: Troubleshooting Tools** (1 week) - ⚠️ **PARTIALLY COMPLETE**
- ✅ **Church Detail View** - COMPLETE (2025-12-29)
  - ✅ Complete church information
  - ✅ Storage breakdown with visual progress bar
  - ✅ Quick actions (activate/deactivate)
  - [ ] User list for church (Future enhancement)
  - [ ] Recent activity log (Future enhancement)
  - [ ] Security violations history (Future enhancement)
  - [ ] Advanced actions (reset user password, clear cache, etc.) (Future enhancement)
- [ ] System Logs Viewer (Not yet implemented)
  - [ ] Real-time log streaming (last 1000 lines)
  - [ ] Log level filtering
  - [ ] Search logs by keyword/church/user
  - [ ] Export logs
- [ ] Performance Metrics (Not yet implemented)
  - [ ] API response time trends
  - [ ] Slow query detection
  - [ ] Database connection pool status
  - [ ] Memory and CPU usage
- [ ] Backend Enhancements (Not yet implemented)
  - [ ] Real-time log streaming endpoint
  - [ ] Performance metrics collection
  - [ ] Quick troubleshooting actions API

**Backend Already Has**:
- ✅ PlatformStatsController - COMPLETE (2025-12-29)
  - ✅ GET /api/platform/stats (aggregate metrics)
  - ✅ GET /api/platform/churches/all (all churches summary)
  - ✅ POST /api/platform/churches/{id}/activate
  - ✅ POST /api/platform/churches/{id}/deactivate
- ✅ SecurityMonitoringController (4 endpoints)
- ✅ StorageUsageController (3 endpoints)
- ✅ SecurityAuditLog entity
- ✅ StorageUsage entity
- ✅ Multi-tenancy with TenantContext
- ✅ SUPERADMIN role definition
- ✅ PlatformService (frontend service)
- ✅ ChurchSummary model with storage calculations

**New Backend Still Needed**:
- SystemHealthService (uptime, performance monitoring)
- LogStreamingController (real-time logs)
- Church status fields (trial_end_date, subscription_status) - Optional for future billing
- Performance metrics collection endpoints

---

### 2. Admin Module - User Management ⭐⭐⭐ CRITICAL
**Status**: 40% complete (basic CRUD exists)
**Priority**: 🔴 HIGHEST
**Effort**: 2-3 weeks
**Dependencies**: RBAC (✅ Complete)

**Why Critical**: Foundation for all user administration features

**Phase 1: Enhanced User Management** (2 weeks)
- [ ] User Management UI Component
  - [ ] Users list page with grid/table view
  - [ ] User profile dialog (view/edit)
  - [ ] User creation dialog
  - [ ] User deactivation workflow
  - [ ] User activity log viewer
- [ ] User Roles & Permissions UI
  - [ ] Role assignment interface (dropdown with 8 roles: SUPERADMIN, ADMIN, PASTOR, TREASURER, FELLOWSHIP_LEADER, MEMBER_MANAGER, MEMBER, FELLOWSHIP_HEAD)
  - [ ] Permission viewer (show which permissions each role has)
  - [ ] Bulk role assignment
- [ ] User Invitation System
  - [ ] Send email invitations to new users
  - [ ] Invitation acceptance workflow
  - [ ] Invitation tracking (pending, accepted, expired)
- [ ] Password Management
  - [ ] Force password reset on first login
  - [ ] Password strength requirements UI
  - [ ] Password reset request workflow
- [ ] Backend Enhancements
  - [ ] User profile photo upload
  - [ ] User soft delete (isActive flag)
  - [ ] User last login tracking
  - [ ] User invitation entity and endpoints

**Phase 2: Church Settings** (1-2 weeks)
- [ ] Church Settings UI
  - [ ] Church profile section (logo, name, contact info)
  - [ ] Service times configuration
  - [ ] Fiscal year settings
  - [ ] Currency and locale settings
  - [ ] Timezone configuration
  - [ ] Branding settings (colors,fonts)
- [ ] Backend
  - [ ] ChurchSettings entity (key-value pairs)
  - [ ] Church logo upload
  - [ ] Settings CRUD endpoints

**Phase 3: Audit Logging UI** (1 week)
- [ ] Audit Log Viewer Component
  - [ ] Filterable audit log table (by user, action, entity, date)
  - [ ] Audit log detail view
  - [ ] Export to CSV
- [ ] System Health Dashboard
  - [ ] Uptime monitoring
  - [ ] Performance metrics
  - [ ] Error rate tracking

**Backend Already Has** (from RBAC implementation):
- ✅ Permission enum (79 permissions)
- ✅ Role definitions (8 roles with permission mappings)
- ✅ @RequirePermission annotation
- ✅ PermissionCheckAspect (AOP enforcement)
- ✅ User entity with church_id
- ✅ SecurityAuditLog entity
- ✅ TenantValidationService

**New Backend Needed**:
- UserInvitation entity
- ChurchSettings entity
- User photo upload endpoints
- Invitation email service

---

### 3. Subscription & Storage Module - Frontend ⭐⭐⭐ CRITICAL
**Status**: 50% complete (backend done, frontend pending)
**Priority**: 🔴 HIGHEST
**Effort**: 2-3 weeks
**Dependencies**: None (backend complete)

**Backend Already Complete** (2025-12-29):
- ✅ StorageUsage entity
- ✅ StorageCalculationService (daily scheduled job at 2 AM)
- ✅ File storage calculation (uploads directory scanning)
- ✅ Database storage estimation (16 entity types)
- ✅ StorageUsageController (3 endpoints)
- ✅ SecurityMonitoringController (4 endpoints)
- ✅ 30+ performance indexes

**Phase 1: Settings Page with Storage** (2-3 weeks)
- [ ] Create Settings Page Component
  - [ ] `settings-page.component.ts/html/css`
  - [ ] Add route `/settings` with PermissionGuard (CHURCH_SETTINGS_VIEW)
  - [ ] Add "Settings" link to side navigation (Admin section)
- [ ] Create Storage Service
  - [ ] `storage.service.ts`
  - [ ] Methods: `getCurrentUsage()`, `getUsageHistory()`, `calculateStorage()`
- [ ] Storage Models/Interfaces
  - [ ] `storage.model.ts`
  - [ ] Interfaces: `StorageUsage`, `StorageBreakdown`, `StorageHistory`
- [ ] Settings Page Sections
  - [ ] Church Information section
  - [ ] **Subscription & Billing section** ⭐
    - [ ] Storage usage card with progress bar
    - [ ] Breakdown: Files (50%) vs Database (50%)
    - [ ] Category breakdown (profile photos, event images, donations, members, etc.)
    - [ ] "Calculate Now" button (for manual recalculation)
    - [ ] Alert when >80% usage
  - [ ] User Management section (link to users page)
  - [ ] API Keys / Integrations section (future)

**Phase 2: Dashboard Widget** (1 week)
- [ ] Storage Usage Widget
  - [ ] Mini widget for dashboard showing `1.2 GB / 2.0 GB (60%)`
  - [ ] Click navigates to Settings page
  - [ ] Warning color when >80%
- [ ] Update DashboardService
  - [ ] Add `getStorageUsage()` method
  - [ ] Integrate with dashboard data loading

**Phase 3: Storage Management** (2-3 weeks - NICE TO HAVE)
- [ ] Usage History Chart
  - [ ] 90-day line chart (Chart.js)
  - [ ] Date range selector
  - [ ] Export history to CSV
- [ ] Storage Optimization Tips
  - [ ] Identify large files
  - [ ] Suggest cleanup actions
  - [ ] Archive old data
- [ ] Billing Integration (Future)
  - [ ] Paystack integration for payments
  - [ ] Plan selection (Starter, Professional, Enterprise)
  - [ ] Payment history
  - [ ] Invoice generation

**Phase 4: Security Monitoring Dashboard** (SUPERADMIN only)
- [ ] Security Dashboard Component
  - [ ] Violation statistics cards
  - [ ] Recent violations table
  - [ ] User/church violation details
  - [ ] Export to CSV
- [ ] Use existing backend endpoints:
  - `GET /api/security/stats`
  - `GET /api/security/violations/recent`
  - `GET /api/security/violations/user/{userId}`
  - `GET /api/security/violations/church/{churchId}`

---

## 🟡 MEDIUM PRIORITY (Important but not blocking)

### 4. RBAC Testing & Monitoring ⭐⭐
**Status**: Backend complete, testing pending
**Effort**: 1 week
**Priority**: 🟡 MEDIUM (but should be done soon after user management)

**Testing** (1-2 days):
- [ ] Manual cross-tenant access testing
  - [ ] Test with valid JWT trying to access another church's data
  - [ ] Verify 403 Forbidden response
  - [ ] Check `security_audit_logs` table for violation entry
- [ ] SUPERADMIN bypass testing
  - [ ] Login as SUPERADMIN
  - [ ] Access multiple churches' data
  - [ ] Verify no violations logged
- [ ] Hibernate filter verification
  - [ ] Enable SQL logging temporarily
  - [ ] Verify `WHERE church_id = ?` in queries

**Monitoring Setup** (2-3 days):
- [ ] Configure alerts for TenantViolationException
  - [ ] Email notification for >= 5 violations in 24h
  - [ ] Slack/Discord webhook
- [ ] Daily security log review process
- [ ] Adjust logging levels for production

---

### 5. Portal Improvements ⭐⭐
**Status**: Functional but needs enhancements
**Effort**: 1-2 weeks
**Priority**: 🟡 MEDIUM

**Church UUID/Invitation System** (from TODO.md Issue #10):
- [ ] Option A: Full UUID implementation (high effort, most secure)
- [ ] Option B: Church slug system (medium effort, balanced)
- [ ] Option C: Invitation code system (low effort, recommended) ⭐
  - [ ] ChurchInvitationCode entity
  - [ ] Admin generates invite codes
  - [ ] Portal registration requires valid code
  - [ ] Code tracks usage and expiry
- [ ] Option D: Enhanced current system (rate limiting, CAPTCHA)

**Recommendation**: Implement Option C (invite codes) as Phase 1

**Location Selector Integration** (from TODO.md Issue #11):
- [ ] Extract location selector into reusable component
- [ ] Use across members, portal registration, households
- [ ] Consistent address input UX

**Other Portal Enhancements**:
- [ ] Profile image upload feedback (TODO.md Issue #4)
- [ ] Better error messages on registration
- [ ] Email verification reminders

---

### 6. Missing Frontend Pages ⭐
**Status**: Backend complete, frontend missing
**Effort**: 1-2 days each
**Priority**: 🟡 MEDIUM

From PENDING_MODULES_SUMMARY.md:

**Counseling Sessions Page** (1-2 days):
- [ ] Create CounselingSessionsPage component
- [ ] Add/Edit/View dialogs
- [ ] Schedule session workflow
- [ ] Statistics cards
- [ ] Filters (type, status, counselor)
- [ ] Session outcome tracking

**Backend Already Has**:
- ✅ CounselingSession entity
- ✅ CounselingSessionService (full CRUD)
- ✅ CounselingSessionController (REST endpoints)
- ✅ Enums: CounselingType, CounselingStatus, SessionOutcome

---

## 🟢 LOW PRIORITY (Nice to have)

### 7. Giving Module - Phase 4 ⭐
**Status**: 75% complete (Phases 1-3 done)
**Effort**: 1-2 weeks
**Priority**: 🟢 LOW (user decided not priority)

**Phase 4: Tax Receipts** (deferred per user):
- [ ] Generate annual tax receipts
- [ ] PDF receipt templates
- [ ] Email receipts to donors
- [ ] Bulk receipt generation
- [ ] Receipt history tracking
- [ ] Export to accounting software

---

### 8. Reports Module - Phases 2-4 ⭐
**Status**: 33% complete (Phase 1 done: 13 pre-built reports)
**Effort**: 3-4 weeks
**Priority**: 🟢 LOW (user decided not priority)

**Phase 2: Custom Report Builder** (deferred per user):
- [ ] Report builder UI
- [ ] Drag-and-drop field selector
- [ ] Filter criteria builder
- [ ] Preview and test query
- [ ] Save custom reports

**Phase 3: Report Scheduling** (deferred per user):
- [ ] Schedule reports (daily, weekly, monthly)
- [ ] Email delivery
- [ ] Automated generation

**Phase 4: Report Sharing** (deferred per user):
- [ ] Share reports with other users
- [ ] Permission-based access
- [ ] Report templates library

---

### 9. Dashboard Module - Phase 2 ⭐
**Status**: 50% complete (Phase 1 done: 7 widgets)
**Effort**: 1 week
**Priority**: 🟢 LOW

**Phase 2: Customization** (from PENDING_MODULES_SUMMARY.md):
- [ ] Drag-and-drop widget layout (Angular CDK)
- [ ] User widget preferences (save layout)
- [ ] Hide/show widgets
- [ ] Export dashboard data
- [ ] Dashboard themes

---

### 10. Additional RBAC Enhancements ⭐
**Status**: Core complete, optional enhancements
**Effort**: 2-3 weeks
**Priority**: 🟢 LOW

From RBAC_PENDING_ITEMS.md:

**Additional Service Validations** (LOW PRIORITY):
- [ ] ReportService - add validation to report generation
- [ ] DashboardService - add validation to analytics queries
- [ ] AnalyticsService - add validation to aggregate data

**Related Entity Validation** (LOW PRIORITY):
- [ ] FellowshipService - validate member assignments (same church)
- [ ] HouseholdService - validate member additions (same church)
- [ ] EventService - validate member registrations (same church)
- [ ] CampaignService - validate pledge assignments (same church)

**Security Hardening** (FUTURE):
- [ ] Rate limiting for failed access attempts
- [ ] Auto-suspend accounts with >= 10 violations
- [ ] SIEM integration (Splunk, ELK Stack)

---

## 📋 TECHNICAL DEBT & CLEANUP

### 11. Testing ⭐
**Priority**: 🟡 MEDIUM
**Effort**: Ongoing

From TODO.md:

**E2E Tests Needed**:
- [ ] Phase 2.2: Bulk import E2E tests
- [ ] RBAC E2E tests (each role)
- [ ] Storage calculation E2E tests
- [ ] User management E2E tests
- [ ] Portal registration E2E tests

**Test Coverage Goals**:
- Backend unit tests: 80%+ (current: ~60%)
- Frontend unit tests: 70%+ (current: ~40%)
- E2E tests: All critical paths

---

### 12. Minor Issues ⭐
**Priority**: 🟢 LOW
**Effort**: 1-2 days total

From TODO.md:

- [ ] Issue #6: Members API gets called twice on page load (`/api/members/tags`)
- [ ] Issue #4: No feedback when uploading profile picture (add progress bar/spinner)
- [ ] Improve error messages across all forms
- [ ] Add loading skeletons for better UX
- [ ] Check if bithday automated smses module exists

---

## 🎯 RECOMMENDED IMPLEMENTATION ORDER

Based on user priority (Platform Admin Dashboard is highest priority):

### Week 1: Platform Admin Dashboard - Phase 1 (Multi-Tenant Overview)
1. ✅ Start: Create Platform Admin page with multi-church statistics
2. Backend: PlatformStatsController (aggregate stats)
3. Backend: Church status management endpoints
4. Frontend: Church list component with search
5. Frontend: System health indicators
6. Test end-to-end with SUPERADMIN role

### Week 2: Platform Admin Dashboard - Phase 2 (Security Monitoring)
1. Frontend: Security violations dashboard
2. Connect to existing SecurityMonitoringController endpoints
3. Real-time violation feed
4. Export violations to CSV
5. Test security monitoring workflow

### Week 3: Platform Admin Dashboard - Phase 3 (Storage & Billing)
1. Frontend: Storage management dashboard
2. Connect to existing StorageUsageController endpoints
3. Storage usage trends across all churches
4. Top storage consumers
5. Test storage monitoring

### Week 4: Platform Admin Dashboard - Phase 4 (Troubleshooting)
1. Frontend: Church detail view
2. Backend: System logs streaming endpoint
3. Frontend: Real-time log viewer
4. Frontend: Performance metrics display
5. Quick troubleshooting actions
6. Complete testing of all platform admin features

### Week 5-6: Admin Module Phase 1 (User Management UI)
1. ✅ Start: Create Users page with grid/list view
2. Create user management dialogs (add, edit, view, deactivate)
3. Implement role assignment interface
4. Add user invitation system
5. Implement password management UI
6. Test end-to-end

### Week 7-8: Subscription & Storage Frontend Phase 1
1. Create Settings page component
2. Create Storage service and models
3. Implement storage usage section with progress bar
4. Add breakdown chart (files vs database)
5. Add "Settings" link to side navigation
6. Test storage display and manual calculation

### Week 9: Admin Module Phase 2 (Church Settings)
1. Add Church Settings section to Settings page
2. Implement church profile editing
3. Add service times configuration
4. Add branding settings
5. Test church settings workflow

### Week 10: Storage Dashboard Widget & Testing
1. Create storage usage widget for dashboard
2. Integrate with dashboard data loading
3. RBAC manual testing (cross-tenant, SUPERADMIN)
4. Configure monitoring alerts
5. Security audit log review process

### Week 11-12: Portal Improvements & Polish
1. Implement church invitation code system
2. Extract location selector component
3. Add profile image upload feedback
4. Create counseling sessions page
5. Fix minor issues (#4, #6)

### Month 3+: Nice-to-Have Features
- Dashboard customization (Phase 2)
- Storage management features (Phase 3)
- Security monitoring dashboard (SUPERADMIN)
- Additional RBAC validations
- Report builder (if needed)
- Tax receipts (if needed)

---

## 📊 COMPLETION METRICS

### Current Status (2025-12-29) - Updated

**Modules Complete**: 9/12 (75%)
- ✅ Members Module (100%)
- ✅ Attendance Module (100%)
- ✅ Fellowship Module (100%)
- ✅ Dashboard Module (100% - Phase 1, Phase 2 optional)
- ✅ Pastoral Care Module (100%)
- ✅ Events Module (100%)
- ✅ Communications Module (100% - SMS only)
- ✅ RBAC System (100% - backend & frontend)
- ✅ **Platform Admin Dashboard - Phase 1** (100%) - ⭐ **NEW** (2025-12-29)

**Modules Partial**: 4/12 (33%)
- ⚠️ Platform Admin Dashboard (50% - Phases 1 & 2 ✅, Phases 3-4 pending)
- ⚠️ Admin Module (40% - basic CRUD exists, need user management UI)
- ⚠️ Subscription & Storage (50% - backend complete, need frontend)
- ⚠️ Giving Module (75% - Phases 1-3 done, Phase 4 optional)
- ⚠️ Reports Module (33% - 13 pre-built reports, custom builder optional)

**Recent Completions (2025-12-29)**:
- ✅ Platform Admin Dashboard - Phase 1 (Multi-Tenant Overview)
  - ✅ Church list grid with search, filter, sort
  - ✅ Platform statistics cards
  - ✅ Church detail view dialog
  - ✅ Activate/Deactivate functionality
  - ✅ Route guards (SUPERADMIN isolation)
  - ✅ UI segregation (SUPERADMIN nav)
  - ✅ Signal-based reactivity
  - ✅ Modern Angular syntax
- ✅ **Platform Admin Dashboard - Phase 2 (Security & Monitoring)** - ⭐ **NEW** (2025-12-29)
  - ✅ Security violations dashboard with 6 statistics cards
  - ✅ Real-time violation feed with filters (search, church, severity, type)
  - ✅ Pagination for large violation lists
  - ✅ Export violations to CSV
  - ✅ Backend DTOs (SecurityViolationResponse, SecurityStatsResponse)
  - ✅ Enriched service methods with batch loading
  - ✅ Frontend SecurityDashboardPage with modern Angular patterns

**Critical Path to 100%** (Updated):
1. ~~Platform Admin Dashboard Phases 1 & 2~~ ✅ COMPLETE
2. Platform Admin Dashboard Phases 3-4 (1-2 weeks)
3. Admin Module Phase 1 & 2 (3-4 weeks)
4. Subscription & Storage Frontend Phase 1 (2-3 weeks)
5. RBAC Testing & Monitoring (1 week)

**Total Effort to Complete Critical Path**: 7-9 weeks (reduced from 8-10)

---

## 📁 RELATED DOCUMENTATION

**Platform Admin Dashboard** (⭐ NEW):
- Complete: SESSION_2025-12-29_CONTINUATION_COMPLETE.md - Route guards & session summary
- Complete: PLATFORM_ADMIN_UI_FIXES_COMPLETE.md - Action buttons & filters
- Complete: CHURCH_DETAIL_VIEW_IMPLEMENTATION_COMPLETE.md - Detail dialog
- Complete: ROLE_BASED_ROUTE_GUARDS_COMPLETE.md - SUPERADMIN isolation
- Complete: MODERN_ANGULAR_CONTROL_FLOW_COMPLETE.md - Syntax migration
- Complete: SUPERADMIN_ROUTING_AND_SIGNALS_COMPLETE.md - Login redirect & Signals
- Pending: Phases 2-4 (Security monitoring, Storage management, Advanced tools)

**Admin Module**:
- Current: PLAN.md (Module 10, lines 1999-2106)
- Pending: User management UI, Church settings UI

**Subscription & Storage**:
- Complete: STORAGE_CALCULATION_IMPLEMENTATION_COMPLETE.md
- Complete: RBAC_CONTEXT_IMPLEMENTATION_COMPLETE.md
- Current: PLAN.md (Module 11, lines 2760-2909)
- Pending: Frontend UI (Phase 1-4)

**RBAC**:
- Complete: RBAC_IMPLEMENTATION_COMPLETE.md
- Complete: SESSION_2025-12-29_RBAC_COMPLETE.md
- Pending: RBAC_PENDING_ITEMS.md (testing, monitoring, optional enhancements)

**Portal**:
- Analysis: PORTAL_IMPROVEMENTS_ANALYSIS.md
- Pending: Church invitation system, Location selector reuse

**Testing**:
- Current: TODO.md (Issues #1-11)
- Pending: E2E tests for new features

---

**Document Status**: ✅ Complete
**Last Updated**: 2025-12-29
**Next Review**: After Admin Module Phase 1 completion
