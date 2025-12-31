# Final Implementation Roadmap - Items 1-5
**Date**: 2025-12-30
**Status**: Planning Phase
**Goal**: Complete remaining 5 high/medium priority items for Version 1.0

---

## 📊 Overview

This roadmap covers the final 5 items needed to complete the PastCare platform to a production-ready state. After completing these items, the platform will be at **100% feature completeness** for Version 1.0.

**Current Status**: 87% complete (20/23 days from original roadmap)
**Target Status**: 100% complete (all core features implemented)

**Estimated Total Effort**: 6-9 weeks
**Target Completion**: Mid-February 2026

---

## 🎯 Implementation Items

### Item 1: Subscription & Storage Frontend ⭐⭐⭐
**Priority**: HIGH
**Status**: 50% complete (backend done, frontend pending)
**Effort**: 2-3 weeks
**Dependencies**: Storage Backend ✅ Complete

### Item 2: Complaints & Feedback Management Module ⭐⭐
**Priority**: MEDIUM-HIGH
**Status**: 0% complete (not started)
**Effort**: 2-3 weeks
**Dependencies**: RBAC ✅ Complete, Email Service ✅ Complete

### Item 3: RBAC Testing & Monitoring ⭐⭐
**Priority**: MEDIUM
**Status**: 0% complete (backend exists, needs testing)
**Effort**: 1 week
**Dependencies**: RBAC ✅ Complete

### Item 4: Portal Improvements ⭐⭐
**Priority**: MEDIUM
**Status**: 0% complete (portal exists, needs enhancements)
**Effort**: 1-2 weeks
**Dependencies**: None

### Item 5: Counseling Sessions Frontend Page ⭐
**Priority**: MEDIUM
**Status**: 50% complete (backend done, frontend pending)
**Effort**: 1-2 days
**Dependencies**: Backend ✅ Complete

---

# ITEM 1: Subscription & Storage Frontend 📊

## Overview
Complete the frontend UI for storage usage monitoring and subscription management in the Settings page.

**Backend Status**: ✅ 100% Complete
- StorageUsage entity
- StorageCalculationService (daily scheduled job)
- StorageUsageController (3 endpoints)
- File storage + database estimation working

**Frontend Status**: ⚠️ 50% Complete
- Basic storage display exists in settings page
- Need enhanced visualization, history, and management features

---

## Phase 1: Enhanced Storage Visualization (Week 1)

### Day 1: Storage Models Enhancement
**Goal**: Enhance existing storage models with additional interfaces

**Tasks**:
- [ ] Review existing `storage-usage.model.ts`
- [ ] Add missing interfaces if needed:
  - `StorageHistory` (if not exists)
  - `StorageAlert` (if not exists)
  - `StorageTrend` (for trend analysis)
- [ ] Add helper functions:
  - `formatStorageSize()` (bytes to GB/MB)
  - `getStorageTrendIcon()` (up/down/stable)
  - `calculateStoragePercentage()`
- [ ] Verify compilation

**Files to Modify**:
- `past-care-spring-frontend/src/app/models/storage-usage.model.ts`

**Deliverable**: Enhanced storage models with all required interfaces

---

### Day 2: Storage Service Enhancement
**Goal**: Add missing service methods for history and trends

**Tasks**:
- [ ] Review existing `storage-usage.service.ts`
- [ ] Add methods if missing:
  - `getStorageHistory(days: number)` (30/60/90 day history)
  - `getStorageTrend()` (usage trend calculation)
  - `getStorageAlerts()` (warnings when near limit)
- [ ] Add caching with BehaviorSubject for storage data
- [ ] Add error handling for all methods
- [ ] Verify compilation

**Files to Modify**:
- `past-care-spring-frontend/src/app/services/storage-usage.service.ts`

**Deliverable**: Complete storage service with history and trends

---

### Day 3-4: Settings Page Storage Tab Enhancement
**Goal**: Enhance existing storage tab with better visualization

**Tasks**:
- [ ] Review existing settings-page storage tab
- [ ] Add/enhance components:
  - Storage alert banner (when >80% usage)
  - Detailed breakdown by category (images, documents, videos, etc.)
  - Storage trend indicator (increasing/decreasing)
  - "Calculate Now" button functionality
  - Link to upgrade plan when near limit
- [ ] Add reactive signals for:
  - `storageHistory` (30/60/90 days)
  - `storageTrend` (up/down/stable)
  - `storageAlerts` (array of warnings)
- [ ] Add loading states and error handling
- [ ] Verify TypeScript compilation

**Files to Modify**:
- `past-care-spring-frontend/src/app/settings-page/settings-page.ts`
- `past-care-spring-frontend/src/app/settings-page/settings-page.html`
- `past-care-spring-frontend/src/app/settings-page/settings-page.css`

**Deliverable**: Enhanced storage tab with comprehensive visualization

---

### Day 5: Storage History Chart
**Goal**: Add 30/60/90-day storage usage history chart

**Tasks**:
- [ ] Choose charting library (Chart.js or similar)
- [ ] Install chart library: `npm install chart.js`
- [ ] Create storage history chart component:
  - Line chart showing storage usage over time
  - 30/60/90 day toggle buttons
  - Legend and axis labels
  - Responsive design
- [ ] Integrate into settings page
- [ ] Add loading skeleton while data loads
- [ ] Verify chart displays correctly

**Files to Create**:
- `past-care-spring-frontend/src/app/settings-page/storage-history-chart/` (optional separate component)

**Files to Modify**:
- `past-care-spring-frontend/src/app/settings-page/settings-page.html`
- `past-care-spring-frontend/src/app/settings-page/settings-page.ts`
- `past-care-spring-frontend/src/app/settings-page/settings-page.css`
- `package.json` (add chart.js dependency)

**Deliverable**: Working storage history chart with 30/60/90-day views

---

## Phase 2: Dashboard Storage Widget (Week 2 - Days 1-2)

### Day 6: Dashboard Storage Widget Component
**Goal**: Create mini storage widget for dashboard

**Tasks**:
- [ ] Create storage widget component:
  - Display format: "1.2 GB / 2.0 GB (60%)"
  - Progress bar with color coding (green <80%, orange 80-90%, red >90%)
  - Icon and title
  - Click navigates to Settings page
- [ ] Add to dashboard imports
- [ ] Position widget in dashboard grid
- [ ] Add responsive design
- [ ] Verify navigation works

**Files to Create**:
- `past-care-spring-frontend/src/app/dashboard/widgets/storage-widget/` (component)

**Files to Modify**:
- `past-care-spring-frontend/src/app/dashboard/dashboard.component.html`
- `past-care-spring-frontend/src/app/dashboard/dashboard.component.ts`

**Deliverable**: Working dashboard storage widget with navigation

---

### Day 7: Dashboard Integration & Testing
**Goal**: Complete dashboard integration and test all storage features

**Tasks**:
- [ ] Test storage widget on dashboard
- [ ] Test navigation from widget to settings
- [ ] Test storage calculation button
- [ ] Test history chart (30/60/90 days)
- [ ] Test alerts and warnings
- [ ] Fix any bugs found
- [ ] Verify responsive design (mobile/tablet/desktop)
- [ ] Run production build: `npm run build`

**Deliverable**: Fully integrated and tested storage features

---

## Phase 3: Storage Management Features (Week 2 - Days 3-5) - OPTIONAL

### Day 8-9: Storage Optimization Tips (Optional)
**Goal**: Add helpful tips for reducing storage usage

**Tasks**:
- [ ] Create storage optimization component
- [ ] Identify large files (top 10 largest)
- [ ] Suggest cleanup actions:
  - Delete old profile pictures
  - Remove duplicate uploads
  - Archive old event photos
- [ ] Add "View Large Files" section
- [ ] Add "Clean Up" action buttons
- [ ] Verify functionality

**Deliverable**: Storage optimization tips UI

---

### Day 10: Export & Reporting (Optional)
**Goal**: Add export functionality for storage data

**Tasks**:
- [ ] Add "Export to CSV" button
- [ ] Generate CSV with storage breakdown
- [ ] Add date range selector for export
- [ ] Implement download functionality
- [ ] Test export in different browsers

**Deliverable**: Storage data export feature

---

## ✅ Item 1 Completion Checklist

**Required (Core Features)**:
- [ ] Enhanced storage models with all interfaces
- [ ] Complete storage service with history methods
- [ ] Enhanced settings page storage tab
- [ ] Storage history chart (30/60/90 days)
- [ ] Dashboard storage widget
- [ ] All features tested and working
- [ ] Production build succeeds
- [ ] Responsive design verified

**Optional (Nice-to-Have)**:
- [ ] Storage optimization tips
- [ ] Large files viewer
- [ ] Storage data export to CSV

**Documentation**:
- [ ] Update PROGRESS_TRACKER.md
- [ ] Create STORAGE_FRONTEND_COMPLETE.md
- [ ] Update CONSOLIDATED_PENDING_TASKS.md

---

# ITEM 2: Complaints & Feedback Management Module 📝

## Overview
Build complete complaints and feedback management system for customer support and issue tracking.

**Why Important**: Critical for product improvement, customer satisfaction, and identifying system issues.

---

## Phase 1: Backend Implementation (Week 3)

### Day 1: Database Entities
**Goal**: Create database entities for complaints and comments

**Tasks**:
- [ ] Create `Complaint` entity:
  - Fields: id, churchId, title, description, category, priority, status
  - Fields: submittedBy, assignedTo, attachments (JSON)
  - Timestamps: createdAt, updatedAt, resolvedAt, closedAt
  - Relationships: ManyToOne with Church, User
- [ ] Create `ComplaintComment` entity:
  - Fields: id, complaintId, userId, comment, isInternal
  - Timestamps: createdAt
  - Relationships: ManyToOne with Complaint, User
- [ ] Create migrations:
  - V68__create_complaints_table.sql
  - V69__create_complaint_comments_table.sql
- [ ] Add enums:
  - ComplaintCategory (TECHNICAL_ISSUE, BILLING_ISSUE, FEATURE_REQUEST, etc.)
  - ComplaintPriority (LOW, MEDIUM, HIGH, CRITICAL)
  - ComplaintStatus (SUBMITTED, ACKNOWLEDGED, IN_PROGRESS, RESOLVED, CLOSED, REJECTED)
- [ ] Run migrations and verify database

**Files to Create**:
- `src/main/java/com/reuben/pastcare_spring/models/Complaint.java`
- `src/main/java/com/reuben/pastcare_spring/models/ComplaintComment.java`
- `src/main/java/com/reuben/pastcare_spring/enums/ComplaintCategory.java`
- `src/main/java/com/reuben/pastcare_spring/enums/ComplaintPriority.java`
- `src/main/java/com/reuben/pastcare_spring/enums/ComplaintStatus.java`
- `src/main/resources/db/migration/V68__create_complaints_table.sql`
- `src/main/resources/db/migration/V69__create_complaint_comments_table.sql`

**Deliverable**: Complete database schema for complaints

---

### Day 2: Repositories & DTOs
**Goal**: Create repositories and data transfer objects

**Tasks**:
- [ ] Create `ComplaintRepository`:
  - Query methods: findByChurchId, findByStatus, findBySubmittedBy
  - Stats queries: countByChurchId, countByStatus
- [ ] Create `ComplaintCommentRepository`:
  - Query methods: findByComplaintId, findByComplaintIdAndIsInternalFalse
- [ ] Create DTOs:
  - `ComplaintRequest` (for create/update)
  - `ComplaintResponse` (for API responses)
  - `ComplaintCommentRequest`
  - `ComplaintCommentResponse`
  - `ComplaintStatsResponse`
- [ ] Add validation annotations (@NotNull, @NotBlank, @Size)
- [ ] Verify compilation

**Files to Create**:
- `src/main/java/com/reuben/pastcare_spring/repositories/ComplaintRepository.java`
- `src/main/java/com/reuben/pastcare_spring/repositories/ComplaintCommentRepository.java`
- `src/main/java/com/reuben/pastcare_spring/dto/ComplaintRequest.java`
- `src/main/java/com/reuben/pastcare_spring/dto/ComplaintResponse.java`
- `src/main/java/com/reuben/pastcare_spring/dto/ComplaintCommentRequest.java`
- `src/main/java/com/reuben/pastcare_spring/dto/ComplaintCommentResponse.java`
- `src/main/java/com/reuben/pastcare_spring/dto/ComplaintStatsResponse.java`

**Deliverable**: Complete repository and DTO layer

---

### Day 3-4: ComplaintService
**Goal**: Implement complete business logic for complaints

**Tasks**:
- [ ] Create `ComplaintService` with methods:
  - `createComplaint(ComplaintRequest)` - Submit new complaint
  - `updateComplaintStatus(id, status, comment)` - Change status
  - `assignComplaint(id, userId)` - Assign to staff
  - `addComment(complaintId, CommentRequest)` - Add comment
  - `getComplaintById(id)` - Get single complaint with comments
  - `getMyComplaints(userId)` - User's own complaints
  - `getAllComplaints(churchId)` - All church complaints (ADMIN)
  - `getPlatformComplaints()` - All platform complaints (SUPERADMIN)
  - `getComplaintsByStatus(status)` - Filter by status
  - `getComplaintStats(churchId)` - Statistics
  - `resolveComplaint(id, resolution)` - Mark resolved
  - `closeComplaint(id)` - Close complaint
  - `reopenComplaint(id)` - Reopen if needed
- [ ] Add church-based access control
- [ ] Add permission checks
- [ ] Add validation logic
- [ ] Write unit tests
- [ ] Verify compilation

**Files to Create**:
- `src/main/java/com/reuben/pastcare_spring/services/ComplaintService.java`

**Deliverable**: Complete complaint service with business logic

---

### Day 5: ComplaintController & Permissions
**Goal**: Create REST API endpoints with RBAC

**Tasks**:
- [ ] Create `ComplaintController` with endpoints:
  - `POST /api/complaints` - Submit complaint (authenticated users)
  - `GET /api/complaints` - List my complaints
  - `GET /api/complaints/{id}` - Get complaint details
  - `PUT /api/complaints/{id}/status` - Update status (COMPLAINT_MANAGE)
  - `POST /api/complaints/{id}/comments` - Add comment
  - `PUT /api/complaints/{id}/assign` - Assign to staff (ADMIN)
  - `GET /api/complaints/church` - All church complaints (ADMIN)
  - `GET /api/complaints/platform` - All platform complaints (SUPERADMIN)
  - `GET /api/complaints/stats` - Statistics
  - `POST /api/complaints/{id}/resolve` - Mark resolved
  - `POST /api/complaints/{id}/close` - Close complaint
  - `POST /api/complaints/{id}/reopen` - Reopen complaint
- [ ] Add permissions to Permission enum:
  - `COMPLAINT_VIEW` - View own complaints
  - `COMPLAINT_CREATE` - Submit complaints
  - `COMPLAINT_MANAGE` - Manage complaints (ADMIN+)
  - `COMPLAINT_VIEW_ALL` - View all platform complaints (SUPERADMIN)
- [ ] Add permissions to role definitions
- [ ] Add @RequirePermission annotations
- [ ] Test all endpoints with Postman
- [ ] Verify compilation: `./mvnw clean compile`

**Files to Create**:
- `src/main/java/com/reuben/pastcare_spring/controllers/ComplaintController.java`

**Files to Modify**:
- `src/main/java/com/reuben/pastcare_spring/enums/Permission.java`
- `src/main/java/com/reuben/pastcare_spring/enums/Role.java`

**Deliverable**: Complete REST API with RBAC protection

---

## Phase 2: Frontend Implementation (Week 4-5)

### Day 6: Frontend Models & Service
**Goal**: Create TypeScript interfaces and Angular service

**Tasks**:
- [ ] Create `complaint.model.ts`:
  - Complaint interface
  - ComplaintComment interface
  - ComplaintStats interface
  - Enums: ComplaintCategory, ComplaintPriority, ComplaintStatus
  - Helper functions for badges and formatting
- [ ] Create `complaint.service.ts`:
  - All CRUD methods matching backend endpoints
  - File upload handling (for attachments)
  - Error handling
- [ ] Verify compilation: `npx tsc --noEmit`

**Files to Create**:
- `past-care-spring-frontend/src/app/models/complaint.model.ts`
- `past-care-spring-frontend/src/app/services/complaint.service.ts`

**Deliverable**: Complete frontend data layer

---

### Day 7-8: Complaints List Page
**Goal**: Create main complaints list component

**Tasks**:
- [ ] Create `complaints-page` component:
  - Card/table view of complaints
  - Status badges (color-coded)
  - Priority indicators
  - Filter by status, category, priority
  - Search by title/description
  - Sort by date, priority, status
  - Empty state
  - Loading state
  - Pagination (if needed)
- [ ] Add route to `app.routes.ts`
- [ ] Add navigation link to sidenav
- [ ] Add responsive design
- [ ] Verify compilation

**Files to Create**:
- `past-care-spring-frontend/src/app/complaints-page/` (component directory)

**Files to Modify**:
- `past-care-spring-frontend/src/app/app.routes.ts`
- `past-care-spring-frontend/src/app/side-nav-component/side-nav-component.html`

**Deliverable**: Working complaints list page

---

### Day 9: Submit Complaint Dialog
**Goal**: Create dialog for submitting new complaints

**Tasks**:
- [ ] Create submit complaint dialog:
  - Title input (required)
  - Description textarea (required)
  - Category dropdown
  - Priority selection (radio buttons)
  - File attachment support (images, documents)
  - Form validation
  - Loading state during submission
  - Success/error messages
- [ ] Integrate with complaints list page
- [ ] Test file upload functionality
- [ ] Verify compilation

**Files to Create**:
- `past-care-spring-frontend/src/app/complaints-page/submit-complaint-dialog/`

**Deliverable**: Working complaint submission dialog

---

### Day 10: Complaint Detail View
**Goal**: Create detailed view for individual complaints

**Tasks**:
- [ ] Create complaint detail component:
  - Full complaint information
  - Comment thread/timeline
  - Add comment functionality
  - Status history
  - Attached files viewer
  - Resolution details (if resolved)
  - Action buttons (resolve, close, reopen)
- [ ] Add routing for detail view
- [ ] Add navigation from list to detail
- [ ] Verify compilation

**Files to Create**:
- `past-care-spring-frontend/src/app/complaints-page/complaint-detail/`

**Deliverable**: Working complaint detail view

---

### Day 11-12: Admin Complaint Management
**Goal**: Create admin features for managing complaints

**Tasks**:
- [ ] Create admin management features:
  - Assign complaint dialog
  - Status update workflow
  - Internal notes (not visible to submitter)
  - Bulk actions (optional)
  - Complaint statistics dashboard
- [ ] Add permission guards (COMPLAINT_MANAGE)
- [ ] Test admin features
- [ ] Verify compilation
- [ ] Run production build: `npm run build`

**Deliverable**: Complete admin complaint management

---

## Phase 3: Email Notifications (Week 5 - Optional)

### Day 13: Email Templates & Integration
**Goal**: Add email notifications for complaint events

**Tasks**:
- [ ] Create email templates:
  - New complaint submitted → Notify admins
  - Complaint assigned → Notify assigned staff
  - Status changed → Notify submitter
  - Comment added → Notify submitter and staff
  - Complaint resolved → Notify submitter
- [ ] Integrate with EmailService
- [ ] Test email delivery
- [ ] Verify all notifications work

**Deliverable**: Complete email notification system

---

## ✅ Item 2 Completion Checklist

**Backend**:
- [ ] Complaint and ComplaintComment entities
- [ ] Database migrations (V68, V69)
- [ ] Repositories with query methods
- [ ] DTOs with validation
- [ ] ComplaintService with all methods
- [ ] ComplaintController with REST API
- [ ] Permissions added to RBAC
- [ ] Backend compiles successfully
- [ ] Unit tests passing

**Frontend**:
- [ ] TypeScript interfaces and enums
- [ ] ComplaintService (Angular)
- [ ] Complaints list page
- [ ] Submit complaint dialog
- [ ] Complaint detail view
- [ ] Admin management features
- [ ] Route and navigation integration
- [ ] Frontend compiles successfully
- [ ] Production build succeeds

**Optional**:
- [ ] Email notifications
- [ ] Bulk actions
- [ ] Advanced filtering
- [ ] Complaint analytics dashboard

**Documentation**:
- [ ] Update PROGRESS_TRACKER.md
- [ ] Create COMPLAINTS_MODULE_COMPLETE.md
- [ ] Update CONSOLIDATED_PENDING_TASKS.md

---

# ITEM 3: RBAC Testing & Monitoring 🔒

## Overview
Comprehensive testing and monitoring of the RBAC system to ensure security and compliance.

**Backend Status**: ✅ Complete (RBAC fully implemented)
**Testing Status**: ⚠️ Not Started

---

## Phase 1: Manual Testing (Week 6 - Days 1-3)

### Day 1: Cross-Tenant Access Testing
**Goal**: Verify tenant isolation works correctly

**Tasks**:
- [ ] Set up test environment:
  - Create 2 test churches (Church A, Church B)
  - Create test users for each church
  - Create test data (members, events, etc.)
- [ ] Test scenarios:
  - User from Church A tries to access Church B data
  - Verify 403 Forbidden response
  - Check security_audit_logs table for violation
  - Test with different endpoints (members, events, donations)
- [ ] Document test results
- [ ] Fix any security holes found

**Deliverable**: Cross-tenant access testing report

---

### Day 2: SUPERADMIN Bypass Testing
**Goal**: Verify SUPERADMIN can access all churches' data

**Tasks**:
- [ ] Login as SUPERADMIN (super@test.com)
- [ ] Access Church A data - verify success
- [ ] Access Church B data - verify success
- [ ] Verify no violations logged for SUPERADMIN
- [ ] Test platform admin endpoints:
  - GET /api/platform/stats
  - GET /api/platform/churches/all
  - GET /api/platform/billing/stats
- [ ] Document test results

**Deliverable**: SUPERADMIN access testing report

---

### Day 3: Hibernate Filter Verification
**Goal**: Verify SQL queries include church_id WHERE clause

**Tasks**:
- [ ] Enable SQL logging:
  - Add `spring.jpa.show-sql=true` to application.properties
  - Add `logging.level.org.hibernate.SQL=DEBUG`
- [ ] Run queries and check logs:
  - Verify `WHERE church_id = ?` in SELECT queries
  - Check INSERT/UPDATE queries include church_id
  - Verify SUPERADMIN queries don't have WHERE clause
- [ ] Document SQL patterns
- [ ] Disable logging after testing

**Deliverable**: Hibernate filter verification report

---

## Phase 2: Monitoring Setup (Week 6 - Days 4-5)

### Day 4: Alert Configuration
**Goal**: Set up alerts for security violations

**Tasks**:
- [ ] Configure email alerts:
  - Alert when >= 5 violations in 24 hours
  - Include violation details (user, church, entity)
  - Send to admin email
- [ ] Optional: Slack/Discord webhook
- [ ] Create alert template
- [ ] Test alert delivery
- [ ] Document alert configuration

**Files to Modify**:
- Application configuration for alerts
- Email templates

**Deliverable**: Working security alert system

---

### Day 5: Daily Security Log Review Process
**Goal**: Establish process for reviewing security logs

**Tasks**:
- [ ] Create log review checklist
- [ ] Set up daily review schedule
- [ ] Create dashboard for log visualization (use existing platform admin)
- [ ] Document review process
- [ ] Train team on log review

**Deliverable**: Security log review process documentation

---

## ✅ Item 3 Completion Checklist

**Testing**:
- [ ] Cross-tenant access tests completed
- [ ] SUPERADMIN bypass tests completed
- [ ] Hibernate filter verification completed
- [ ] All security holes fixed
- [ ] Test reports documented

**Monitoring**:
- [ ] Email alerts configured
- [ ] Alert templates created
- [ ] Daily log review process established
- [ ] Team trained on security monitoring

**Documentation**:
- [ ] Update PROGRESS_TRACKER.md
- [ ] Create RBAC_TESTING_COMPLETE.md
- [ ] Update CONSOLIDATED_PENDING_TASKS.md
- [ ] Create security monitoring runbook

---

# ITEM 4: Portal Improvements 🌐

## Overview
Enhance the portal registration system with invitation codes and better UX.

**Current Status**: Portal exists but needs security and UX enhancements

---

## Phase 1: Church Invitation Code System (Week 7)

### Day 1-2: Backend Implementation
**Goal**: Create invitation code system

**Tasks**:
- [ ] Create `ChurchInvitationCode` entity:
  - Fields: id, churchId, code (unique), createdBy
  - Fields: usageLimit, usageCount, expiresAt
  - Fields: isActive, createdAt
- [ ] Create migration: V70__create_church_invitation_codes_table.sql
- [ ] Create `ChurchInvitationCodeRepository`
- [ ] Create `InvitationCodeService`:
  - `generateCode(churchId, usageLimit, expiryDays)` - Admin creates code
  - `validateCode(code)` - Check if code is valid
  - `useCode(code)` - Increment usage count
  - `deactivateCode(codeId)` - Disable code
  - `getChurchCodes(churchId)` - List all codes for church
- [ ] Create `InvitationCodeController`:
  - POST /api/invitation-codes/generate (ADMIN)
  - GET /api/invitation-codes/validate/{code} (public)
  - POST /api/invitation-codes/{id}/deactivate (ADMIN)
  - GET /api/invitation-codes (ADMIN)
- [ ] Add permissions to RBAC
- [ ] Verify compilation

**Files to Create**:
- `src/main/java/com/reuben/pastcare_spring/models/ChurchInvitationCode.java`
- `src/main/resources/db/migration/V70__create_church_invitation_codes_table.sql`
- `src/main/java/com/reuben/pastcare_spring/repositories/ChurchInvitationCodeRepository.java`
- `src/main/java/com/reuben/pastcare_spring/services/InvitationCodeService.java`
- `src/main/java/com/reuben/pastcare_spring/controllers/InvitationCodeController.java`

**Deliverable**: Complete invitation code backend

---

### Day 3: Frontend - Admin Code Management
**Goal**: Create UI for admins to manage invitation codes

**Tasks**:
- [ ] Create invitation code management component:
  - List all invitation codes
  - Generate new code dialog
  - Code details (code, usage, expiry)
  - Deactivate code button
  - Copy code to clipboard
- [ ] Add to settings page or admin section
- [ ] Add route and navigation
- [ ] Verify compilation

**Files to Create**:
- `past-care-spring-frontend/src/app/invitation-codes-page/` (component)

**Deliverable**: Admin invitation code management UI

---

### Day 4: Frontend - Portal Registration with Code
**Goal**: Update portal registration to require invitation code

**Tasks**:
- [ ] Update portal registration component:
  - Add invitation code input field (first step)
  - Validate code before showing registration form
  - Show church name after code validation
  - Display error if code is invalid/expired
  - Link code to registration
- [ ] Update registration flow
- [ ] Test with valid and invalid codes
- [ ] Verify compilation

**Files to Modify**:
- `past-care-spring-frontend/src/app/portal-registration/` (component)

**Deliverable**: Portal registration with invitation code requirement

---

## Phase 2: Location Selector Component (Week 7 - Days 5-7)

### Day 5: Extract Location Selector
**Goal**: Create reusable location selector component

**Tasks**:
- [ ] Review existing location selector usage:
  - Members page
  - Portal registration
  - Households page
- [ ] Create `location-selector` shared component:
  - Country dropdown
  - State/Province dropdown
  - City input
  - Address input
  - Two-way data binding with @Input/@Output
  - Validation
- [ ] Export from shared module
- [ ] Verify compilation

**Files to Create**:
- `past-care-spring-frontend/src/app/shared/location-selector/` (component)

**Deliverable**: Reusable location selector component

---

### Day 6-7: Integrate Location Selector
**Goal**: Replace location inputs across the app

**Tasks**:
- [ ] Replace in members page
- [ ] Replace in portal registration
- [ ] Replace in households page
- [ ] Test all locations work correctly
- [ ] Verify consistent UX
- [ ] Fix any bugs
- [ ] Verify compilation

**Files to Modify**:
- `past-care-spring-frontend/src/app/members-page/`
- `past-care-spring-frontend/src/app/portal-registration/`
- `past-care-spring-frontend/src/app/households-page/`

**Deliverable**: Consistent location selector across app

---

## Phase 3: Portal UX Improvements (Week 8 - Days 1-2)

### Day 8: Profile Image Upload Feedback
**Goal**: Add better feedback for profile picture upload

**Tasks**:
- [ ] Add upload progress bar
- [ ] Add image preview before upload
- [ ] Add file size validation (client-side)
- [ ] Add success/error messages
- [ ] Add loading spinner during upload
- [ ] Test upload flow
- [ ] Verify compilation

**Files to Modify**:
- Portal registration component

**Deliverable**: Improved profile picture upload UX

---

### Day 9: Error Messages & Validation
**Goal**: Improve error messages across portal

**Tasks**:
- [ ] Review all error messages in portal
- [ ] Make errors more user-friendly
- [ ] Add field-level validation messages
- [ ] Add form-level validation
- [ ] Add email verification reminders
- [ ] Test all error scenarios
- [ ] Verify compilation

**Deliverable**: Better error handling in portal

---

## ✅ Item 4 Completion Checklist

**Backend**:
- [ ] ChurchInvitationCode entity
- [ ] Database migration (V70)
- [ ] InvitationCodeService
- [ ] InvitationCodeController
- [ ] RBAC permissions added
- [ ] Backend compiles successfully

**Frontend**:
- [ ] Admin code management UI
- [ ] Portal registration with code requirement
- [ ] Location selector component
- [ ] Location selector integrated everywhere
- [ ] Profile picture upload improvements
- [ ] Better error messages
- [ ] Frontend compiles successfully
- [ ] Production build succeeds

**Documentation**:
- [ ] Update PROGRESS_TRACKER.md
- [ ] Create PORTAL_IMPROVEMENTS_COMPLETE.md
- [ ] Update CONSOLIDATED_PENDING_TASKS.md

---

# ITEM 5: Counseling Sessions Frontend Page 🗣️

## Overview
Create frontend page for counseling sessions management (backend already exists).

**Backend Status**: ✅ 100% Complete
- CounselingSession entity
- CounselingSessionService (full CRUD)
- CounselingSessionController (REST endpoints)
- Enums: CounselingType, CounselingStatus, SessionOutcome

**Frontend Status**: ⚠️ 0% Complete

---

## Implementation (Week 8 - Days 3-4)

### Day 1: Frontend Models & Service
**Goal**: Create TypeScript interfaces and Angular service

**Tasks**:
- [ ] Create `counseling-session.model.ts`:
  - CounselingSession interface
  - Enums: CounselingType, CounselingStatus, SessionOutcome
  - Helper functions for badges and formatting
- [ ] Create `counseling-session.service.ts`:
  - All CRUD methods matching backend
  - Error handling
- [ ] Verify compilation: `npx tsc --noEmit`

**Files to Create**:
- `past-care-spring-frontend/src/app/models/counseling-session.model.ts`
- `past-care-spring-frontend/src/app/services/counseling-session.service.ts`

**Deliverable**: Complete frontend data layer

---

### Day 2: Counseling Sessions Page
**Goal**: Create main counseling sessions component

**Tasks**:
- [ ] Create `counseling-sessions-page` component:
  - Card grid view of sessions
  - Statistics cards (total, scheduled, completed, canceled)
  - Filters (type, status, counselor)
  - Search by member name
  - Add new session button
  - Empty state
  - Loading state
- [ ] Add dialogs:
  - Add/Edit session dialog (form with validation)
  - View session dialog (full details)
  - Delete confirmation dialog
- [ ] Add route to `app.routes.ts`
- [ ] Add navigation link to sidenav
- [ ] Add responsive design
- [ ] Verify compilation
- [ ] Run production build: `npm run build`

**Files to Create**:
- `past-care-spring-frontend/src/app/counseling-sessions-page/` (component directory)

**Files to Modify**:
- `past-care-spring-frontend/src/app/app.routes.ts`
- `past-care-spring-frontend/src/app/side-nav-component/side-nav-component.html`

**Deliverable**: Complete counseling sessions page

---

## ✅ Item 5 Completion Checklist

**Frontend**:
- [ ] TypeScript interfaces and enums
- [ ] CounselingSessionService (Angular)
- [ ] Counseling sessions page component
- [ ] Add/Edit/View/Delete dialogs
- [ ] Statistics cards
- [ ] Filters and search
- [ ] Route and navigation integration
- [ ] Frontend compiles successfully
- [ ] Production build succeeds
- [ ] Responsive design verified

**Documentation**:
- [ ] Update PROGRESS_TRACKER.md
- [ ] Create COUNSELING_SESSIONS_COMPLETE.md
- [ ] Update CONSOLIDATED_PENDING_TASKS.md

---

# 📊 Overall Timeline

## Week-by-Week Breakdown

**Week 1**: Subscription & Storage Frontend - Phase 1
- Days 1-5: Enhanced visualization, history chart

**Week 2**: Subscription & Storage Frontend - Phase 2-3
- Days 1-2: Dashboard widget
- Days 3-5: Storage management features (optional)

**Week 3**: Complaints Module - Backend
- Days 1-5: Entities, repositories, service, controller

**Week 4**: Complaints Module - Frontend (Part 1)
- Days 1-5: Models, service, list page, submit dialog

**Week 5**: Complaints Module - Frontend (Part 2)
- Days 1-5: Detail view, admin features, email notifications

**Week 6**: RBAC Testing & Monitoring
- Days 1-3: Manual testing
- Days 4-5: Monitoring setup

**Week 7**: Portal Improvements - Invitation Codes & Location Selector
- Days 1-4: Invitation code system
- Days 5-7: Location selector component

**Week 8**: Portal UX & Counseling Sessions
- Days 1-2: Portal UX improvements
- Days 3-4: Counseling sessions frontend

**Week 9**: Buffer for testing, bug fixes, documentation

---

## Success Criteria

After completing all 5 items:

✅ **Subscription & Storage**:
- [ ] Enhanced storage visualization in settings
- [ ] Storage history chart (30/60/90 days)
- [ ] Dashboard storage widget
- [ ] Storage management features

✅ **Complaints & Feedback**:
- [ ] Complete backend (entities, service, API)
- [ ] Frontend list and detail views
- [ ] Submit and manage complaints
- [ ] Email notifications
- [ ] Admin management features

✅ **RBAC Testing**:
- [ ] Cross-tenant access tested
- [ ] SUPERADMIN bypass verified
- [ ] Hibernate filters verified
- [ ] Security alerts configured
- [ ] Monitoring process established

✅ **Portal Improvements**:
- [ ] Invitation code system working
- [ ] Location selector reusable component
- [ ] Better profile upload UX
- [ ] Improved error messages

✅ **Counseling Sessions**:
- [ ] Complete frontend page
- [ ] CRUD operations working
- [ ] Filters and statistics
- [ ] Responsive design

---

## Next Steps

After completing this roadmap:

1. **Version 1.0 Release**:
   - Complete production deployment
   - User acceptance testing
   - Bug fixes and polish

2. **Optional Enhancements** (Version 1.1+):
   - Dashboard customization (drag-and-drop widgets)
   - Custom report builder
   - Tax receipts for Giving module
   - Performance metrics dashboard
   - Additional RBAC enhancements

3. **Maintenance & Support**:
   - Monitor production logs
   - Address user feedback
   - Performance optimization
   - Security updates

---

**Document Status**: ✅ Complete
**Last Updated**: 2025-12-30
**Author**: Claude Sonnet 4.5
**Review Status**: Ready for implementation
