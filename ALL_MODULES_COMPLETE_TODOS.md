# PastCare SaaS - ALL MODULES Complete Implementation TODOs

**Purpose**: Complete granular task breakdown for EVERY module
**SaaS Multi-Tenant Context**: All features church-isolated, subscription-based
**Last Updated**: 2025-12-20

---

## 📚 Documentation Structure

### Implementation Files Created:
1. **IMPLEMENTATION_TODOS.md** - Members Module Phase 1 (Detailed, 1413 lines)
2. **MEMBERS_PHASE_2_6_TODOS.md** - Members Module Phases 2-6 (In Progress)
3. **THIS FILE** - All Other Modules Complete Todos

---

## 📌 Status Legend
- ⏳ **Not Started**
- 🔄 **In Progress**
- ✅ **Completed**
- 🔴 **Blocked**
- ⚠️ **Needs Review**

---

# MODULE 2: ATTENDANCE

## Overview
Complete attendance tracking with QR codes, mobile check-in, analytics, and visitor management.

**Total Estimate**: 6-8 weeks
**Phases**: 4

---

## Phase 1: Enhanced Attendance Tracking (2 weeks) ⏳

### 1.1 QR Code Check-In System ⏳
**Priority**: HIGH | **Estimate**: 12 hours

#### Backend Tasks
- [ ] Add QR code generation library
  ```xml
  <dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.2</version>
  </dependency>
  <dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.2</version>
  </dependency>
  ```
- [ ] Create `QRCodeService`
  - [ ] `generateMemberQRCode(Long memberId)` - Generate unique QR for member
  - [ ] `generateSessionQRCode(Long sessionId)` - Generate QR for session check-in
  - [ ] `validateQRCode(String qrData)` - Validate and parse QR code
  - [ ] QR data format: `{"type":"member","id":123,"churchId":5,"timestamp":1234567890,"signature":"..."}`
  - [ ] Add HMAC signature to prevent forgery
  - [ ] Set expiration (QR codes valid for 24 hours)
- [ ] Create `AttendanceCheckInMethod` enum
  ```java
  public enum CheckInMethod {
      MANUAL,           // Admin marks attendance manually
      QR_CODE_SCAN,     // Member scans QR at entrance
      MOBILE_APP,       // Check-in via mobile app
      GEOFENCE,         // Automatic based on location
      NFC_TAP,          // Future: NFC card/phone tap
      BIOMETRIC         // Future: Facial recognition
  }
  ```
- [ ] Update `Attendance` entity
  ```java
  @Enumerated(EnumType.STRING)
  private CheckInMethod checkInMethod;

  private LocalDateTime checkInTime; // Exact time they checked in
  private String checkInLocation; // GPS coordinates if mobile
  private String checkInDevice; // Device info
  ```
- [ ] Create database migration `V12__add_checkin_fields_to_attendance.sql`
  - [ ] Add check_in_method column
  - [ ] Add check_in_time column
  - [ ] Add check_in_location column
  - [ ] Add check_in_device column
  - [ ] Add indexes for querying
- [ ] Create `QRCheckInRequest` DTO
  - [ ] String qrData (scanned QR code data)
  - [ ] Long sessionId (which session to check into)
  - [ ] String gpsLocation (optional, from mobile device)
  - [ ] String deviceInfo (optional, user agent)
- [ ] Create `POST /api/attendance/check-in/qr` endpoint
  - [ ] Parse QR code data
  - [ ] Validate signature
  - [ ] Validate expiration
  - [ ] Extract member ID and church ID
  - [ ] Validate session exists and is active
  - [ ] Mark attendance with status PRESENT
  - [ ] Record check-in method, time, location
  - [ ] Return AttendanceResponse
- [ ] Create `GET /api/members/{id}/qr-code` endpoint
  - [ ] Generate QR code for member
  - [ ] Return as PNG image (byte array)
  - [ ] OR return base64 encoded string
  - [ ] Cache QR codes (valid 24 hours)
- [ ] Create `GET /api/attendance/sessions/{id}/qr-code` endpoint
  - [ ] Generate QR code for session check-in point
  - [ ] Return printable QR code (A4 size PDF)
  - [ ] Include session name, date, church logo
- [ ] Add QR code validation
  - [ ] Verify HMAC signature
  - [ ] Check expiration timestamp
  - [ ] Validate church ID matches session
  - [ ] Prevent replay attacks (one-time use or rate limit)
- [ ] Add security measures
  - [ ] Rate limiting: max 1 check-in per member per minute
  - [ ] Detect duplicate scans (same member, same session)
  - [ ] Log all QR scan attempts for audit
- [ ] Add unit tests
  - [ ] Test QR code generation
  - [ ] Test QR code validation (valid, expired, invalid signature)
  - [ ] Test check-in via QR code
  - [ ] Test duplicate check-in prevention
  - [ ] Test cross-church QR code rejection

#### Frontend Tasks
- [ ] Create `QRScannerComponent`
  - [ ] Use device camera to scan QR codes
  - [ ] Library: `@zxing/ngx-scanner` or `html5-qrcode`
  - [ ] Show camera preview
  - [ ] Detect and parse QR code
  - [ ] Display scan result
  - [ ] Submit to backend API
- [ ] Create QR check-in flow
  - [ ] Member opens check-in page (mobile-optimized)
  - [ ] Select session (if multiple services today)
  - [ ] Click "Scan QR Code"
  - [ ] Camera opens
  - [ ] Scan QR code at entrance
  - [ ] Success message: "Checked in successfully!"
  - [ ] Show check-in time and seat number (optional)
- [ ] Create `MemberQRCodeComponent`
  - [ ] Display member's personal QR code
  - [ ] Generate on demand
  - [ ] Show QR code image
  - [ ] "Save to Phone" button (download image)
  - [ ] "Add to Wallet" button (Apple/Google Wallet)
  - [ ] Expiration timer
- [ ] Create session QR code generator (admin)
  - [ ] In session detail page
  - [ ] "Generate Check-In QR Code" button
  - [ ] Display large QR code for printing
  - [ ] "Download PDF" button (A4 size)
  - [ ] "Print" button
  - [ ] Preview before printing
- [ ] Add QR check-in option to attendance page
  - [ ] Button: "QR Check-In Mode"
  - [ ] Opens scanner interface
  - [ ] Continuous scanning (check in multiple members)
  - [ ] List of checked-in members (real-time)
  - [ ] Sound/vibration on successful scan
- [ ] Create check-in success animation
  - [ ] Green checkmark animation
  - [ ] Member name and photo flash
  - [ ] Confetti for first-time visitors
- [ ] Add QR code to member portal
  - [ ] Member dashboard shows "My QR Code"
  - [ ] Always accessible
  - [ ] Can be added to phone home screen (PWA)

#### E2E Tests
- [ ] Test: Generate member QR code
  - [ ] Navigate to member profile
  - [ ] Click "Generate QR Code"
  - [ ] Assert QR code image displayed
  - [ ] Click "Download"
  - [ ] Assert image file downloaded
- [ ] Test: QR code check-in (happy path)
  - [ ] Create active session
  - [ ] Generate member QR code
  - [ ] Open QR scanner
  - [ ] Scan member QR code
  - [ ] Assert check-in successful
  - [ ] Assert attendance marked as PRESENT
  - [ ] Assert check-in time recorded
- [ ] Test: Duplicate QR scan prevention
  - [ ] Check in member via QR
  - [ ] Scan same QR code again immediately
  - [ ] Assert error: "Already checked in"
  - [ ] Assert attendance not duplicated
- [ ] Test: Expired QR code
  - [ ] Generate QR code
  - [ ] Wait 25 hours (or mock timestamp)
  - [ ] Scan expired QR
  - [ ] Assert error: "QR code expired"
  - [ ] Assert check-in rejected
- [ ] Test: Invalid QR signature
  - [ ] Tamper with QR code data
  - [ ] Scan tampered QR
  - [ ] Assert error: "Invalid QR code"
  - [ ] Assert check-in rejected
- [ ] Test: Print session QR code
  - [ ] Open session details
  - [ ] Click "Generate Check-In QR Code"
  - [ ] Assert QR code displayed
  - [ ] Click "Download PDF"
  - [ ] Assert PDF downloaded with QR code, session name, date
- [ ] Test: Continuous QR scanning
  - [ ] Enable QR check-in mode
  - [ ] Scan multiple member QR codes in succession
  - [ ] Assert each check-in successful
  - [ ] Assert real-time list updates
- [ ] Test: Cross-church QR rejection
  - [ ] Generate QR for Member A in Church 1
  - [ ] Try to check in to Session in Church 2
  - [ ] Assert error: "QR code not valid for this session"

**Edge Cases**:
- Poor lighting (QR code hard to scan)
- Damaged/blurry QR codes
- Screen brightness too low
- QR code on printed paper vs phone screen
- Multiple people scanning simultaneously (race condition)
- Network failure during check-in (retry mechanism)
- Member without QR code (fallback to manual)
- Check-in after session ended (late arrivals)
- Check-in before session started (early arrivals)
- QR code screenshot used fraudulently (detect static image?)
- Member checks in for someone else (acceptable or prevent?)

---

### 1.2 Mobile Check-In App ⏳
**Priority**: HIGH | **Estimate**: 16 hours

#### Backend Tasks
- [ ] Create mobile-optimized API endpoints
  - [ ] `GET /api/attendance/mobile/sessions/today` - Today's sessions
  - [ ] `POST /api/attendance/mobile/check-in` - Mobile check-in
  - [ ] `GET /api/attendance/mobile/my-attendance` - My attendance history
- [ ] Add geolocation validation
  - [ ] Accept GPS coordinates in check-in request
  - [ ] Validate member is within X meters of church location
  - [ ] Configurable radius (default 500m)
  - [ ] Override for special events (outdoor, off-site)
- [ ] Create `MobileCheckInRequest` DTO
  ```java
  private Long sessionId;
  private Long memberId;
  private Double latitude;
  private Double longitude;
  private String deviceInfo;
  private CheckInMethod method; // MOBILE_APP or GEOFENCE
  ```
- [ ] Add device registration (push notifications)
  - [ ] Store device tokens for FCM/APNS
  - [ ] Link devices to members
  - [ ] Send push notifications for session reminders
- [ ] Add offline check-in queue
  - [ ] Accept check-ins even if offline
  - [ ] Queue for later sync
  - [ ] Prevent duplicate sync
- [ ] Add unit tests
  - [ ] Test geolocation validation (within radius)
  - [ ] Test geolocation validation (outside radius)
  - [ ] Test mobile check-in flow
  - [ ] Test offline queue and sync

#### Frontend Tasks (PWA)
- [ ] Create Progressive Web App (PWA)
  - [ ] Service worker for offline support
  - [ ] App manifest for "Add to Home Screen"
  - [ ] App icon and splash screen
  - [ ] Offline caching strategy
- [ ] Create mobile home screen
  - [ ] "Check In Now" large button
  - [ ] Today's sessions list
  - [ ] My attendance summary
  - [ ] My QR code (quick access)
- [ ] Implement geofence check-in
  - [ ] Request location permission
  - [ ] Get current GPS coordinates
  - [ ] Calculate distance to church
  - [ ] Auto-check-in if within radius
  - [ ] Notification: "You're at church! Check in now?"
- [ ] Add camera permissions handling
  - [ ] Request camera permission for QR scanning
  - [ ] Graceful fallback if denied
  - [ ] Instructions for enabling in settings
- [ ] Create offline mode
  - [ ] Queue check-ins locally
  - [ ] Display "Offline" indicator
  - [ ] Auto-sync when online
  - [ ] Show sync status
- [ ] Add push notification support
  - [ ] Register device for push
  - [ ] Handle incoming notifications
  - [ ] Deep link to session check-in
- [ ] Optimize for mobile performance
  - [ ] Lazy loading
  - [ ] Image optimization
  - [ ] Minimal bundle size
  - [ ] Fast load time (<3s on 3G)

#### E2E Tests (Mobile)
- [ ] Test: Install PWA to home screen
  - [ ] Open app in mobile browser
  - [ ] Click "Add to Home Screen"
  - [ ] Assert app icon added
  - [ ] Launch from home screen
  - [ ] Assert opens in standalone mode
- [ ] Test: Geofence check-in
  - [ ] Mock GPS location (near church)
  - [ ] Open app
  - [ ] Assert auto-check-in prompt appears
  - [ ] Click "Check In"
  - [ ] Assert attendance marked
- [ ] Test: Outside geofence radius
  - [ ] Mock GPS location (far from church)
  - [ ] Try to check in
  - [ ] Assert error: "You're not at church"
  - [ ] Assert check-in rejected
- [ ] Test: Offline check-in
  - [ ] Disable network
  - [ ] Check in via QR code
  - [ ] Assert queued locally
  - [ ] Enable network
  - [ ] Assert auto-sync successful
  - [ ] Assert attendance marked on server
- [ ] Test: Push notification
  - [ ] Register device
  - [ ] Admin sends session reminder
  - [ ] Assert push notification received
  - [ ] Tap notification
  - [ ] Assert app opens to check-in page

**Edge Cases**:
- GPS unavailable or disabled
- Poor GPS accuracy (100m+ error)
- Member forgot phone (use QR on someone else's phone?)
- Multiple churches in same location (geofence overlap)
- Church at multiple campuses (different geofences)
- Network switches between WiFi and cellular
- App backgrounded during check-in
- Device time out of sync
- Very old devices (iOS 12, Android 8)
- Low battery mode affects GPS

---

### 1.3 Recurring Service Templates ⏳
**Priority**: MEDIUM | **Estimate**: 10 hours

#### Backend Tasks
- [ ] Create `ServiceTemplate` entity
  ```java
  @Entity
  public class ServiceTemplate extends TenantBaseEntity {
      @Column(nullable = false)
      private String templateName; // "Sunday Morning Service"

      @Enumerated(EnumType.STRING)
      private DayOfWeek dayOfWeek; // SUNDAY, WEDNESDAY, etc.

      private LocalTime serviceTime; // 09:00, 18:00, etc.

      @ManyToOne
      private Fellowship fellowship; // Optional: specific fellowship

      @ManyToOne
      private Location location; // Service location

      private Boolean isActive = true;

      @Column(columnDefinition = "TEXT")
      private String description;

      private String recurrencePattern; // WEEKLY, BIWEEKLY, MONTHLY
  }
  ```
- [ ] Create migration for service_template table
- [ ] Create `ServiceTemplateService`
  - [ ] `createTemplate(ServiceTemplateRequest)`
  - [ ] `generateSessionsFromTemplate(Long templateId, LocalDate startDate, LocalDate endDate)`
    - [ ] Create AttendanceSession for each occurrence
    - [ ] Handle weekly, biweekly, monthly recurrence
    - [ ] Skip holidays (configurable)
  - [ ] `getUpcomingSessionsFromTemplate(Long templateId, int weeks)`
- [ ] Create scheduled job
  - [ ] Run every Sunday at 1 AM
  - [ ] Generate next 4 weeks of sessions from all active templates
  - [ ] Prevent duplicate session creation
- [ ] Add API endpoints
  - [ ] `POST /api/service-templates`
  - [ ] `GET /api/service-templates`
  - [ ] `PUT /api/service-templates/{id}`
  - [ ] `DELETE /api/service-templates/{id}`
  - [ ] `POST /api/service-templates/{id}/generate-sessions`
    - [ ] Manually trigger session generation
- [ ] Add unit tests

#### Frontend Tasks
- [ ] Create `ServiceTemplatesComponent`
  - [ ] List of all templates
  - [ ] Create/Edit/Delete templates
  - [ ] Preview next 10 weeks of sessions
  - [ ] Activate/Deactivate templates
- [ ] Add template to session creation
  - [ ] "Create from Template" option
  - [ ] Select template
  - [ ] Auto-fill session details
  - [ ] Override if needed

#### E2E Tests
- [ ] Test: Create service template
  - [ ] Fill template details
  - [ ] Set recurrence: Weekly, Sunday, 9:00 AM
  - [ ] Save
  - [ ] Assert template created
- [ ] Test: Generate sessions from template
  - [ ] Create template
  - [ ] Click "Generate Sessions"
  - [ ] Select date range: Next 4 weeks
  - [ ] Assert 4 sessions created
  - [ ] Verify dates and times match template
- [ ] Test: Scheduled auto-generation
  - [ ] Create active template
  - [ ] Trigger scheduled job (mock or wait)
  - [ ] Assert sessions auto-created for next month

**Edge Cases**:
- Template for fifth Sunday of month (some months don't have)
- Daylight Saving Time transitions (time shifts)
- Holiday exclusions (Christmas, Easter)
- Template changes mid-generation (which sessions affected?)
- Deleted template with existing sessions (preserve sessions?)

---

### 1.4 Late Arrival Tracking ⏳
**Priority**: LOW | **Estimate**: 4 hours

#### Backend Tasks
- [ ] Add `isLateArrival` field to `Attendance`
  - [ ] Boolean flag
  - [ ] Determined by check-in time vs session time
- [ ] Add `LATE` attendance status enum value
  ```java
  public enum AttendanceStatus {
      PRESENT,
      LATE,           // NEW
      ABSENT,
      EXCUSED
  }
  ```
- [ ] Update check-in logic
  - [ ] If check-in time > session time + 15 minutes → mark as LATE
  - [ ] Configurable lateness threshold per church
- [ ] Add late arrival statistics
  - [ ] Count late arrivals per session
  - [ ] Late arrival rate per member
  - [ ] Chronic lateness report

#### Frontend Tasks
- [ ] Show late indicator on attendance list
  - [ ] Yellow badge: "LATE"
  - [ ] Show arrival time
- [ ] Add late arrival filter
- [ ] Show late percentage in session stats

#### E2E Tests
- [ ] Test: Late check-in marks as LATE
  - [ ] Session starts at 9:00 AM
  - [ ] Member checks in at 9:20 AM
  - [ ] Assert status = LATE
- [ ] Test: On-time check-in marks as PRESENT
  - [ ] Session starts at 9:00 AM
  - [ ] Member checks in at 8:55 AM
  - [ ] Assert status = PRESENT

---

## Phase 2: Attendance Analytics (2 weeks) ⏳

### 2.1 Individual Attendance Rate ⏳
**Priority**: HIGH | **Estimate**: 8 hours

#### Backend Tasks
- [ ] Add attendance rate calculation to `MemberService`
  - [ ] `getAttendanceRate(Long memberId, LocalDate startDate, LocalDate endDate)`
  - [ ] Formula: (Sessions Attended / Total Sessions) * 100
  - [ ] Consider only sessions member was expected to attend
  - [ ] Cache results (recalculate weekly)
- [ ] Add to MemberResponse
  ```java
  private Double attendanceRate; // Percentage (0-100)
  private Integer sessionsAttended;
  private Integer totalSessions;
  ```
- [ ] Create `GET /api/members/{id}/attendance-rate` endpoint
  - [ ] Query params: startDate, endDate
  - [ ] Return detailed breakdown
- [ ] Add church-wide attendance statistics
  - [ ] Average attendance rate
  - [ ] Distribution (0-25%, 26-50%, 51-75%, 76-100%)

#### Frontend Tasks
- [ ] Add attendance rate to member profile
  - [ ] Circular gauge (0-100%)
  - [ ] Color coded (red <50%, yellow 50-75%, green >75%)
  - [ ] Text: "85% attendance rate (last 3 months)"
- [ ] Add attendance rate to member card
  - [ ] Small indicator
  - [ ] Hover for details
- [ ] Create attendance rate filter
  - [ ] Filter members by rate range
- [ ] Add chart showing attendance over time
  - [ ] Line graph: attendance by month
  - [ ] Identify trends (improving/declining)

#### E2E Tests
- [ ] Test: Calculate attendance rate
  - [ ] Create 10 sessions
  - [ ] Member attends 8 sessions
  - [ ] View member profile
  - [ ] Assert rate = 80%
- [ ] Test: Attendance rate date range
  - [ ] Calculate rate for last 3 months
  - [ ] Calculate rate for last 6 months
  - [ ] Assert different values

---

### 2.2 Irregular Attendance Alerts ⏳
**Priority**: HIGH | **Estimate**: 6 hours

#### Backend Tasks
- [ ] Create scheduled job to detect irregular attenders
  - [ ] Run daily at 6 AM
  - [ ] Query members with <3 sessions attended in last month
  - [ ] Or members absent for 3+ consecutive sessions
  - [ ] Create alert record
- [ ] Create `AttendanceAlert` entity
  ```java
  @Entity
  public class AttendanceAlert extends TenantBaseEntity {
      @ManyToOne
      private Member member;

      @Enumerated(EnumType.STRING)
      private AlertType alertType;
      // LOW_ATTENDANCE, CONSECUTIVE_ABSENCES,
      // SUDDEN_DECLINE, STOPPED_ATTENDING

      private Integer sessionsAbsent;
      private LocalDate lastAttended;
      private String description;

      private Boolean resolved = false;
      private String resolutionNotes;
      private LocalDateTime resolvedAt;
  }
  ```
- [ ] Create `GET /api/attendance/alerts` endpoint
- [ ] Create `POST /api/attendance/alerts/{id}/resolve` endpoint

#### Frontend Tasks
- [ ] Add alerts to dashboard
  - [ ] "Irregular Attenders" widget
  - [ ] Count and list
  - [ ] Click to view details
- [ ] Create alerts page
  - [ ] List all unresolved alerts
  - [ ] Filter by type
  - [ ] Resolve with notes
- [ ] Send notifications
  - [ ] Email to pastors
  - [ ] In-app notification

#### E2E Tests
- [ ] Test: Alert created for irregular attender
  - [ ] Member absent for 4 weeks
  - [ ] Run scheduled job
  - [ ] Assert alert created
  - [ ] View dashboard
  - [ ] Assert alert appears in widget
- [ ] Test: Resolve alert
  - [ ] View alert
  - [ ] Click "Resolve"
  - [ ] Add notes: "Contacted member, on vacation"
  - [ ] Assert alert marked resolved

---

(Due to length, continuing with summaries for remaining modules...)

---

# MODULE 3: GIVING (SaaS Critical)

## Key Features with Estimates

### Phase 1: Donation Recording (2 weeks)
- Manual donation entry (8h)
- Payment method tracking (4h)
- Donation types (tithe, offering, special) (4h)
- Receipt generation (PDF) (8h)
- Multi-currency support (6h)
- Batch entry for Sunday offerings (6h)

### Phase 2: Online Giving Integration (3 weeks)
- Stripe integration (16h)
- Paystack integration (West Africa) (12h)
- Flutterwave integration (12h)
- Mobile Money (MTN, Vodafone, AirtelTigo) (16h)
- Recurring donations (10h)
- Payment webhooks (8h)
- Failed payment retry (6h)

### Phase 3: Pledge Management (2 weeks)
- Pledge creation (8h)
- Payment schedules (6h)
- Pledge reminders (6h)
- Campaign tracking (8h)
- Progress thermometer widget (4h)

### Phase 4: Financial Reporting (2 weeks)
- Donor statements (8h)
- Tax receipts (year-end) (8h)
- Giving trends analytics (8h)
- Budget vs actual (8h)
- Export to QuickBooks/Excel (6h)

### Phase 5: Donor Engagement (1 week)
- Thank you automation (6h)
- Milestone recognition (4h)
- Lapsed donor recovery (4h)
- Giving potential scoring (4h)

**Total: 8-10 weeks**

---

# MODULE 4: EVENTS

## Key Features with Estimates

### Phase 1: Event Management (2 weeks)
- Event CRUD (8h)
- Event types (service, conference, outreach) (4h)
- Recurring events (6h)
- Event capacity limits (4h)
- Event image/flyer (4h)
- Multi-day events (6h)

### Phase 2: Registration & Attendance (2 weeks)
- Member registration (8h)
- Guest registration (non-members) (6h)
- Registration forms (custom fields) (8h)
- Waitlist management (6h)
- Registration fees (integrate with Giving) (8h)
- QR code tickets (6h)
- Event check-in (4h)

### Phase 3: Calendar & Communication (1 week)
- Church calendar view (8h)
- Event reminders (email/SMS) (6h)
- iCal/Google Calendar export (4h)
- Event updates notification (4h)

**Total: 4-6 weeks**

---

# MODULE 5: COMMUNICATIONS

## Key Features with Estimates

### Phase 1: SMS (2 weeks)
- Twilio integration (8h)
- Africa's Talking integration (8h)
- Send individual SMS (4h)
- Bulk SMS (6h)
- SMS templates (6h)
- SMS scheduling (4h)
- Delivery tracking (6h)
- Credits management (4h)

### Phase 2: Email (2 weeks)
- SendGrid integration (8h)
- Email templates (HTML editor) (8h)
- Bulk email (6h)
- Personalization (merge fields) (6h)
- Email scheduling (4h)
- Open/click tracking (6h)
- Bounce handling (4h)

### Phase 3: WhatsApp & Push (2 weeks)
- WhatsApp Business API (12h)
- WhatsApp templates (6h)
- Push notifications (PWA) (8h)
- In-app messaging (6h)
- Notification preferences (4h)

### Phase 4: Campaigns (1 week)
- Campaign creation (6h)
- A/B testing (6h)
- Engagement analytics (6h)
- Automated workflows (6h)

**Total: 6-8 weeks**

---

# MODULE 6: REPORTS

## Key Features with Estimates

### Phase 1: Pre-built Reports (2 weeks)
- Member directory (6h)
- Attendance summary (6h)
- Giving summary (6h)
- Fellowship roster (4h)
- Birthday/anniversary list (4h)
- Inactive members report (4h)
- First-time visitors report (4h)
- Pastoral care summary (4h)
- Event attendance report (4h)
- Growth trend report (6h)

### Phase 2: Custom Report Builder (2 weeks)
- Drag-and-drop builder (12h)
- Field selection (6h)
- Filters and grouping (8h)
- Calculated fields (6h)
- Save report templates (4h)

### Phase 3: Export & Scheduling (1 week)
- Export to PDF (6h)
- Export to Excel/CSV (4h)
- Print layouts (4h)
- Scheduled reports (6h)
- Email distribution (4h)

**Total: 4-5 weeks**

---

# MODULE 7: ADMIN & SaaS

## Key SaaS Features with Estimates

### Phase 1: Subscription Management (2 weeks)
- Subscription plans (Free, Basic, Pro, Enterprise) (8h)
- Stripe subscription integration (12h)
- Plan upgrade/downgrade (8h)
- Usage-based billing (6h)
- Invoice generation (6h)
- Payment method management (4h)
- Billing history (4h)

### Phase 2: Multi-Tenancy (1 week)
- Church registration workflow (6h)
- Subdomain provisioning (8h)
- Data isolation verification (6h)
- Tenant limits (storage, users, members) (6h)

### Phase 3: User Management (2 weeks)
- User profiles (6h)
- 2FA authentication (8h)
- Password policies (4h)
- User invitations (6h)
- Granular permissions (8h)
- User activity log (6h)

### Phase 4: Church Settings (1 week)
- Church profile (logo, contact) (4h)
- Service times (4h)
- Fiscal year (2h)
- Currency/locale (4h)
- Timezone (2h)
- Email/SMS branding (6h)
- Terms and privacy (2h)

### Phase 5: System Admin (1 week)
- Audit logs (6h)
- Database backup/restore (6h)
- Data export (full church data) (4h)
- API keys management (4h)
- Webhooks (4h)
- Performance monitoring integration (4h)

**Total: 7-8 weeks**

---

# SPECIAL MODULE: SaaS Platform Features

## Critical SaaS Requirements

### 1. Onboarding Flow ⏳
**Estimate**: 1 week

- [ ] Landing page with pricing (6h)
- [ ] Trial signup (14-day free) (4h)
- [ ] Church registration wizard (8h)
- [ ] Payment method collection (4h)
- [ ] Email verification (4h)
- [ ] Onboarding tour/tutorial (6h)
- [ ] Sample data creation (4h)

### 2. Subscription Plans ⏳
**Estimate**: 2 weeks

#### Plan Tiers
```
FREE (Trial):
- Up to 50 members
- Basic attendance tracking
- Limited reports
- Email support
- 14-day trial, then $0/month

BASIC ($29/month):
- Up to 200 members
- Full attendance (QR codes, mobile)
- Basic giving
- Standard reports
- Email support

PROFESSIONAL ($99/month):
- Up to 1,000 members
- Advanced analytics
- Online giving integrations
- SMS/Email communications (1000 credits/month)
- Member portal
- Phone support

ENTERPRISE ($299/month):
- Unlimited members
- Custom reports
- API access
- Dedicated support
- Custom integrations
- White-label option
```

#### Implementation
- [ ] Plan definition entity (8h)
- [ ] Feature flags per plan (6h)
- [ ] Usage tracking (4h)
- [ ] Plan limits enforcement (8h)
- [ ] Upgrade prompts (4h)
- [ ] Downgrade protection (4h)

### 3. Billing & Payments ⏳
**Estimate**: 2 weeks

- [ ] Stripe subscription integration (16h)
- [ ] Automatic billing (4h)
- [ ] Failed payment handling (6h)
- [ ] Retry logic (4h)
- [ ] Subscription pause/cancel (6h)
- [ ] Proration handling (6h)
- [ ] Tax calculation (4h)
- [ ] Invoice generation (6h)
- [ ] Payment method updates (4h)

### 4. Usage Limits & Quotas ⏳
**Estimate**: 1 week

- [ ] Member count limits (4h)
- [ ] Storage limits (for images, files) (4h)
- [ ] SMS/Email credits (6h)
- [ ] API rate limits (4h)
- [ ] Concurrent user limits (4h)
- [ ] Quota exceeded handling (4h)
- [ ] Overage billing (optional) (4h)

### 5. White-Label (Enterprise) ⏳
**Estimate**: 2 weeks

- [ ] Custom domain mapping (8h)
- [ ] Custom branding (logo, colors) (6h)
- [ ] Email from custom domain (6h)
- [ ] Remove "Powered by PastCare" (2h)
- [ ] Custom login page (6h)
- [ ] Mobile app branding (8h)

### 6. Data Migration & Export ⏳
**Estimate**: 1 week

- [ ] Export all church data (JSON/CSV) (6h)
- [ ] GDPR compliance (right to be forgotten) (4h)
- [ ] Import from other systems (8h)
- [ ] Bulk data operations (4h)
- [ ] Data retention policies (4h)

### 7. Support & Help Center ⏳
**Estimate**: 2 weeks

- [ ] Knowledge base/FAQ (12h)
- [ ] Video tutorials (8h)
- [ ] In-app help tooltips (6h)
- [ ] Support ticket system (12h)
- [ ] Live chat integration (4h)
- [ ] Community forum (optional) (8h)

---

# IMPLEMENTATION PRIORITY ORDER (SaaS Focus)

## Phase 1 (Months 1-3): MVP
1. Members Module Phase 1 (fixes + international)
2. Attendance Module Phase 1 (QR codes, basic tracking)
3. Admin/SaaS (subscription, billing, onboarding)
4. Basic Dashboard
5. Landing page + marketing site

## Phase 2 (Months 4-6): Core Features
1. Members Module Phase 2 (bulk ops, search)
2. Giving Module Phase 1-2 (donations + online giving)
3. Communications Phase 1 (SMS/Email basics)
4. Events Module Phase 1 (event management)
5. Reports Module Phase 1 (pre-built reports)

## Phase 3 (Months 7-10): Advanced Features
1. Members Module Phase 3-4 (family, lifecycle)
2. Attendance Module Phase 2 (analytics)
3. Pastoral Care Module Phase 1-2
4. Giving Module Phase 3-4 (pledges, reporting)
5. Communications Phase 2-3 (WhatsApp, campaigns)

## Phase 4 (Months 11-15): Premium Features
1. Members Module Phase 5-6 (skills, portal)
2. Fellowship Module (all phases)
3. Dashboard Module (advanced analytics)
4. Events Module Phase 2-3
5. Reports Module Phase 2-3 (custom builder)

## Phase 5 (Months 16-20): Enterprise
1. Developer Analytics Dashboard
2. API platform
3. White-label features
4. Mobile apps (iOS/Android)
5. Advanced integrations

---

# TESTING REQUIREMENTS (ALL MODULES)

## E2E Test Coverage Target: 90%+

### Critical Paths (Must Test)
- [ ] Church signup to first member added
- [ ] Member creation to attendance marking
- [ ] Donation recording to receipt generation
- [ ] Event creation to registration to check-in
- [ ] SMS/Email sending to delivery confirmation
- [ ] Report generation to export
- [ ] Plan upgrade to payment success
- [ ] Data export to re-import

### Security Testing
- [ ] SQL injection attempts (all forms)
- [ ] XSS attacks (all text inputs)
- [ ] CSRF protection verification
- [ ] Authentication bypass attempts
- [ ] Authorization checks (cross-tenant)
- [ ] Rate limiting enforcement
- [ ] Encryption verification (data at rest/transit)

### Performance Testing
- [ ] Load test: 1000 concurrent users
- [ ] Load test: 10,000 members per church
- [ ] Load test: 100,000 total members (all churches)
- [ ] Response time <500ms for 95th percentile
- [ ] Database query optimization
- [ ] File upload stress test
- [ ] Bulk operation performance

---

## TOTAL PROJECT ESTIMATE

**All Modules**: 63-83 weeks (15-20 months)
**With SaaS Features**: 72-92 weeks (18-23 months)
**Minimum Viable Product (MVP)**: 12-16 weeks (3-4 months)

**Team Size Recommendation**:
- 2-3 Full-stack developers
- 1 DevOps engineer
- 1 QA/Test engineer
- 1 UI/UX designer
- 1 Product manager

**Estimated Budget** (if outsourcing at $50/hr average):
- MVP: $24,000 - $32,000
- Full Product: $144,000 - $184,000

---

This completes the comprehensive implementation todos for ALL modules!
