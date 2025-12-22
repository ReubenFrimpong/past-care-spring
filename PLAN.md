# PastCare Application - Master Implementation Plan

**Project Vision**: A comprehensive church management system helping pastors better connect with their members through intuitive UI, robust features, and comprehensive TDD with E2E testing.

**Last Updated**: 2025-12-20

---

## Application Architecture Overview

### Tech Stack
- **Backend**: Spring Boot 3.5.4 + MySQL + JWT Authentication
- **Frontend**: Angular 21 + PrimeNG + Tailwind CSS
- **Testing**: Playwright (E2E), Jasmine/Karma (Unit)
- **Multi-Tenancy**: Church-based isolation with Hibernate filters

### Core Modules
1. **Members Module** - Member management and profiles
2. **Attendance Module** - Service/event attendance tracking
3. **Fellowship Module** - Small groups management
4. **Dashboard Module** - Analytics and insights
5. **Pastoral Care Module** - Member care and follow-ups
6. **Giving Module** (NEW) - Donations and financial tracking
7. **Events Module** (NEW) - Church events and calendar
8. **Communications Module** (NEW) - SMS/Email/WhatsApp messaging
9. **Reports Module** (NEW) - Custom reports and analytics
10. **Admin Module** - Users, roles, and church settings

---

## Module 1: Members Module ✅ PLANNED

**Status**: Comprehensive specification completed
**Plan Document**: `/home/reuben/.claude/plans/snuggly-orbiting-gadget.md`
**Timeline**: 14-19 weeks (6 phases)

### Implementation Phases

#### Phase 1: Critical Fixes & International Support ⭐⭐⭐
- **Duration**: 2-3 weeks
- **Status**: ⏳ NOT STARTED
- **Tasks**:
  - [ ] Fix spouse validation bug
  - [ ] Fix profile image preservation bug
  - [ ] Add international phone validation
  - [ ] Add country selector and timezone support
  - [ ] Update Location entity for global addresses
  - [ ] Extract methods to appropriate services
  - [ ] Write comprehensive E2E tests

#### Phase 2: Quick Operations & Bulk Management ⭐⭐⭐
- **Duration**: 2-3 weeks
- **Status**: ⏳ NOT STARTED
- **Tasks**:
  - [ ] Quick add member workflow
  - [ ] CSV/Excel bulk import
  - [ ] Bulk update operations
  - [ ] Soft delete with archive
  - [ ] Advanced search builder
  - [ ] Saved searches
  - [ ] Tags system
  - [ ] Profile completeness indicator

#### Phase 3: Family & Household Management ⭐⭐
- **Duration**: 2-3 weeks
- **Status**: ⏳ NOT STARTED
- **Tasks**:
  - [ ] Create Household entity and CRUD
  - [ ] Spouse linking (bidirectional)
  - [ ] Parent-child relationships
  - [ ] Household head designation
  - [ ] Shared family addresses

#### Phase 4: Lifecycle & Communication Tracking ⭐⭐
- **Duration**: 3-4 weeks
- **Status**: ⏳ NOT STARTED
- **Tasks**:
  - [ ] Lifecycle events (baptism, confirmation, etc.)
  - [ ] Member status transitions (VISITOR → MEMBER → LEADER)
  - [ ] Communication log (calls, visits, emails)
  - [ ] Follow-up tracking system
  - [ ] Confidential notes (role-based access)

#### Phase 5: Skills & Ministry Involvement ⭐
- **Duration**: 2 weeks
- **Status**: ⏳ NOT STARTED
- **Tasks**:
  - [ ] Skills registry
  - [ ] Proficiency levels
  - [ ] Availability calendar
  - [ ] Skill-based search
  - [ ] Ministry assignment tracking

#### Phase 6: Member Self-Service Portal ⭐⭐⭐
- **Duration**: 3-4 weeks
- **Status**: ⏳ NOT STARTED
- **Tasks**:
  - [ ] Member self-registration
  - [ ] Email verification
  - [ ] Admin approval workflow
  - [ ] Profile self-management
  - [ ] Attendance viewing
  - [ ] Prayer request submission

### Key Deliverables
- 6 new entities (Household, LifecycleEvent, CommunicationLog, MemberSkill, SavedSearch, PortalUser)
- 30+ new API endpoints
- International support (phone, address, timezone)
- Comprehensive E2E test coverage
- Member portal with self-service

---

## Module 2: Attendance Module ⏳ PLANNED

**Status**: Specification in progress
**Current Implementation**: Basic session and attendance marking
**Timeline**: 6-8 weeks (4 phases)

### Current State
- AttendanceSession entity (session name, date, time, fellowship, notes, isCompleted)
- Attendance entity (member, session, status, remarks)
- Basic CRUD endpoints
- Bulk attendance marking

### Implementation Phases

#### Phase 1: Enhanced Attendance Tracking ⭐⭐⭐
- **Duration**: 2 weeks
- **Status**: ⏳ NOT STARTED
- **Priority Features**:
  - [ ] QR code check-in system
  - [ ] Mobile check-in app
  - [ ] Geofencing for automatic check-in
  - [ ] Late arrival tracking
  - [ ] Multiple services per day support
  - [ ] Recurring service templates
  - [ ] Attendance reminders (SMS/Email/WhatsApp)
  - [ ] First-time visitor flagging

#### Phase 2: Attendance Analytics ⭐⭐
- **Duration**: 2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Individual attendance rate calculation
  - [ ] Attendance trends over time
  - [ ] Irregular attendance alerts
  - [ ] Consecutive absence tracking (3+ weeks)
  - [ ] Fellowship attendance comparison
  - [ ] Service type attendance patterns
  - [ ] Age group attendance analysis
  - [ ] Attendance heatmaps (day/time analysis)

#### Phase 3: Visitor Management ⭐⭐
- **Duration**: 1-2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Guest/visitor registration form
  - [ ] Visitor follow-up workflow
  - [ ] Visitor-to-member conversion tracking
  - [ ] Visitor welcome messages
  - [ ] Visitor info capture (minimal fields)
  - [ ] How did you hear about us tracking

#### Phase 4: Integration & Reporting ⭐
- **Duration**: 1-2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Export attendance to Excel/PDF
  - [ ] Attendance certificates
  - [ ] Integration with member lifecycle
  - [ ] Auto-update member status based on attendance
  - [ ] Attendance-based segmentation

### New Entities Needed
1. **Visitor** - Guest information before becoming member
2. **CheckInMethod** - Enum (MANUAL, QR_CODE, GEOFENCE, MOBILE_APP)
3. **ServiceType** - Enum (SUNDAY_SERVICE, MIDWEEK_SERVICE, SPECIAL_EVENT, etc.)
4. **AttendanceReminder** - Scheduled reminders for irregular attenders

### Key Endpoints
- `POST /api/attendance/check-in/qr` - QR code check-in
- `POST /api/attendance/check-in/mobile` - Mobile app check-in
- `GET /api/attendance/analytics/member/{id}` - Individual analytics
- `GET /api/attendance/analytics/trends` - Church-wide trends
- `POST /api/attendance/visitors` - Register visitor
- `GET /api/attendance/irregular-attenders` - Members needing follow-up
- `POST /api/attendance/send-reminders` - Bulk reminder sending

### E2E Test Scenarios
- QR code generation and scanning
- Bulk check-in workflow
- Late arrival recording
- First-time visitor registration
- Attendance analytics accuracy
- Reminder sending (scheduled)
- Geofence check-in simulation
- Mobile app check-in flow

---

## Module 3: Fellowship Module ⏳ PLANNED

**Status**: Basic implementation exists, needs major enhancement
**Current Implementation**: Basic CRUD for fellowship groups
**Timeline**: 4-6 weeks (3 phases)

### Current State
- Fellowship entity (name, members, users)
- Basic CRUD endpoints
- Many-to-many with Members and Users

### Implementation Phases

#### Phase 1: Fellowship Management Enhancement ⭐⭐⭐
- **Duration**: 2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Fellowship leaders assignment
  - [ ] Meeting schedule (day, time, location)
  - [ ] Fellowship description and purpose
  - [ ] Fellowship image/logo
  - [ ] Maximum capacity settings
  - [ ] Fellowship type (age-based, interest-based, geographic)
  - [ ] Auto-assignment rules
  - [ ] Fellowship joining requests (member-initiated)

#### Phase 2: Fellowship Activities & Engagement ⭐⭐
- **Duration**: 2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Fellowship meetings (separate from church services)
  - [ ] Fellowship attendance tracking
  - [ ] Fellowship announcements
  - [ ] Fellowship prayer requests (group-specific)
  - [ ] Fellowship events calendar
  - [ ] Member engagement scoring per fellowship
  - [ ] Fellowship WhatsApp/Telegram integration
  - [ ] Fellowship reports (attendance, growth, activities)

#### Phase 3: Fellowship Analytics & Growth ⭐
- **Duration**: 1-2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Fellowship growth trends
  - [ ] Fellowship health metrics
  - [ ] Member retention per fellowship
  - [ ] Fellowship comparison dashboard
  - [ ] New fellowship formation workflow
  - [ ] Fellowship multiplication tracking
  - [ ] Fellowship balance recommendations (size, demographics)

### Enhanced Fellowship Entity
```java
@Entity
public class Fellowship extends TenantBaseEntity {
  private String name;
  private String description;
  private String imageUrl;

  @Enumerated(EnumType.STRING)
  private FellowshipType type; // AGE_BASED, INTEREST_BASED, GEOGRAPHIC, MINISTRY

  @ManyToOne
  private User leader; // Primary fellowship leader

  @ManyToMany
  private List<User> coleaders;

  private DayOfWeek meetingDay;
  private LocalTime meetingTime;

  @ManyToOne
  private Location meetingLocation;

  private Integer maxCapacity;
  private Boolean isActive;
  private Boolean acceptingMembers;

  @ManyToMany(mappedBy = "fellowships")
  private List<Member> members;
}
```

### New Entities
1. **FellowshipMeeting** - Meeting records (date, attendance, topics, notes)
2. **FellowshipAnnouncement** - Group-specific announcements
3. **FellowshipJoinRequest** - Member requests to join fellowship
4. **FellowshipType** - Enum (AGE_BASED, INTEREST_BASED, GEOGRAPHIC, MINISTRY)

### Key Endpoints
- `POST /api/fellowships/{id}/join-request` - Request to join
- `POST /api/fellowships/{id}/approve-member/{memberId}`
- `POST /api/fellowships/{id}/meetings` - Create fellowship meeting
- `GET /api/fellowships/{id}/analytics` - Fellowship metrics
- `POST /api/fellowships/{id}/announcements`
- `GET /api/fellowships/recommendations/{memberId}` - Suggest fellowships for member
- `POST /api/fellowships/{id}/auto-assign` - Auto-assign members based on rules

### E2E Test Scenarios
- Create fellowship with leader
- Member join request workflow
- Fellowship meeting creation and attendance
- Fellowship analytics accuracy
- Auto-assignment rules
- Fellowship announcements
- Fellowship capacity limits

---

## Module 4: Dashboard Module ⏳ PLANNED

**Status**: Basic implementation exists, needs major enhancement
**Current Implementation**: Stats, pastoral care needs, events, activities, location stats
**Timeline**: 3-4 weeks (2 phases)

### Current State
- Dashboard statistics endpoint
- Pastoral care needs
- Upcoming events
- Recent activities
- Location-based statistics

### Implementation Phases

#### Phase 1: Enhanced Dashboard Widgets ⭐⭐⭐
- **Duration**: 2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Customizable dashboard layout (drag-and-drop)
  - [ ] Widget library (30+ widgets)
  - [ ] Real-time data updates (WebSocket)
  - [ ] Role-based dashboard views
  - [ ] Dashboard templates (pastor, treasurer, fellowship leader)
  - [ ] Quick actions panel
  - [ ] Notification center
  - [ ] Today's agenda widget

**Widget Types**:
- Member stats (total, new, active, inactive)
- Attendance trends (graph)
- Financial summary (this month, YTD)
- Upcoming events (calendar)
- Birthday/anniversaries this week
- Pending tasks/follow-ups
- Recent activities feed
- Prayer requests
- Irregular attenders alert
- Fellowship growth comparison
- Giving trends
- Location map (member distribution)
- Service analytics
- Visitor conversion rate
- Member lifecycle distribution

#### Phase 2: Analytics & Insights ⭐⭐
- **Duration**: 1-2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Predictive analytics (attendance forecasting)
  - [ ] Anomaly detection (unusual patterns)
  - [ ] Member churn risk scoring
  - [ ] Growth projections
  - [ ] Engagement scoring
  - [ ] Health metrics (overall church health)
  - [ ] Comparison to previous periods
  - [ ] Goal tracking and progress
  - [ ] AI-powered insights (optional)

### New Entities
1. **DashboardLayout** - User's custom dashboard configuration
2. **Widget** - Widget definitions and settings
3. **Goal** - Church goals and tracking
4. **Insight** - System-generated insights

### Key Endpoints
- `GET /api/dashboard/widgets/available` - All available widgets
- `POST /api/dashboard/layout` - Save custom layout
- `GET /api/dashboard/real-time/{widget}` - Real-time widget data
- `GET /api/dashboard/insights` - AI-generated insights
- `GET /api/dashboard/goals` - Church goals and progress
- `POST /api/dashboard/widgets/{id}/refresh` - Refresh widget data

### E2E Test Scenarios
- Dashboard customization (drag-and-drop)
- Widget configuration
- Real-time updates
- Role-based views
- Dashboard templates
- Quick actions
- Insights generation

---

## Module 5: Pastoral Care Module ⏳ PLANNED

**Status**: Placeholder exists, needs full implementation
**Timeline**: 6-8 weeks (4 phases)

### Current State
- Route exists but minimal implementation
- Dashboard shows pastoral care needs

### Implementation Phases

#### Phase 1: Care Needs Management ⭐⭐⭐
- **Duration**: 2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Care need types (hospital visit, bereavement, counseling, prayer, financial, other)
  - [ ] Priority levels (urgent, high, medium, low)
  - [ ] Assignment to pastors/leaders
  - [ ] Status tracking (pending, in-progress, completed, closed)
  - [ ] Follow-up scheduling
  - [ ] Care notes (confidential)
  - [ ] Care history timeline
  - [ ] Automatic need detection (e.g., 3+ weeks absent)

#### Phase 2: Visit & Counseling Management ⭐⭐⭐
- **Duration**: 2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Visit scheduling calendar
  - [ ] Visit types (home, hospital, office, phone)
  - [ ] Visit reports and notes
  - [ ] Counseling sessions tracking
  - [ ] Referral system (to professional counselors)
  - [ ] Visit reminders
  - [ ] Travel route optimization (for multiple visits)
  - [ ] Visit duration tracking

#### Phase 3: Prayer Request Management ⭐⭐
- **Duration**: 1-2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Prayer request submission (member portal)
  - [ ] Prayer request categories
  - [ ] Urgent prayer flagging
  - [ ] Anonymous prayer requests
  - [ ] Prayer answered testimonies
  - [ ] Prayer chains (notify prayer team)
  - [ ] Prayer request expiration/archiving
  - [ ] Prayer statistics

#### Phase 4: Crisis & Emergency Management ⭐
- **Duration**: 1-2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Emergency contact protocols
  - [ ] Crisis categories (death, accident, natural disaster, etc.)
  - [ ] Emergency response team notifications
  - [ ] Crisis timeline tracking
  - [ ] Resource mobilization (food, shelter, funds)
  - [ ] Crisis communication templates
  - [ ] Post-crisis follow-up checklist

### New Entities
1. **CareNeed** - Pastoral care needs (type, priority, assignee, status, due date, notes)
2. **Visit** - Pastoral visits (type, date, duration, location, attendees, notes)
3. **CounselingSession** - Counseling sessions (confidential)
4. **PrayerRequest** - Prayer requests (requester, category, priority, answered, testimony)
5. **Crisis** - Crisis events (type, affected members, response team, timeline)
6. **CareNeedType** - Enum (HOSPITAL, BEREAVEMENT, COUNSELING, PRAYER, FINANCIAL, etc.)
7. **VisitType** - Enum (HOME, HOSPITAL, OFFICE, PHONE)

### Key Endpoints
- `POST /api/pastoral-care/needs` - Create care need
- `GET /api/pastoral-care/needs/assigned-to-me` - My assignments
- `POST /api/pastoral-care/visits` - Schedule visit
- `GET /api/pastoral-care/visits/calendar` - Visit calendar
- `POST /api/pastoral-care/prayer-requests` - Submit prayer request
- `GET /api/pastoral-care/prayer-requests/active` - Active requests
- `POST /api/pastoral-care/crisis` - Report crisis
- `GET /api/pastoral-care/analytics` - Care metrics

### E2E Test Scenarios
- Create care need and assign
- Schedule pastoral visit
- Submit prayer request (member portal)
- Anonymous prayer request
- Crisis reporting and response
- Visit calendar management
- Care need status transitions
- Confidential counseling notes (access control)
- Prayer answered testimony
- Care analytics

---

## Module 6: Giving Module 📦 NEW

**Status**: Not implemented
**Timeline**: 8-10 weeks (5 phases)
**Priority**: ⭐⭐⭐ (Essential for church sustainability)

### Implementation Phases

#### Phase 1: Donation Recording ⭐⭐⭐
- **Duration**: 2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Manual donation entry
  - [ ] Donation types (tithe, offering, special giving, pledge, missions, building fund)
  - [ ] Payment methods (cash, check, mobile money, bank transfer, card)
  - [ ] Anonymous donations
  - [ ] Recurring donations setup
  - [ ] Donation receipts (PDF generation)
  - [ ] Batch entry for Sunday offerings
  - [ ] Multi-currency support

#### Phase 2: Online Giving Integration ⭐⭐⭐
- **Duration**: 2-3 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Payment gateway integration (Stripe, PayPal, Flutterwave, Paystack)
  - [ ] Member portal giving page
  - [ ] One-time donation
  - [ ] Recurring donation setup
  - [ ] Pledge management
  - [ ] Mobile money integration (MTN, Vodafone, AirtelTigo)
  - [ ] Automated receipts
  - [ ] Failed payment retry

#### Phase 3: Pledge & Campaign Management ⭐⭐
- **Duration**: 2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Pledge creation and tracking
  - [ ] Campaign management (building fund, missions trip, etc.)
  - [ ] Campaign goals and progress
  - [ ] Pledge reminders
  - [ ] Pledge payment schedules
  - [ ] Campaign thermometer widget
  - [ ] Campaign donor recognition
  - [ ] Multi-year pledge support

#### Phase 4: Financial Reporting ⭐⭐⭐
- **Duration**: 2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Donor statements (monthly, quarterly, yearly)
  - [ ] Tax receipts (year-end)
  - [ ] Giving trends analysis
  - [ ] Top donors report
  - [ ] Giving by category
  - [ ] Comparison reports (YoY, MoM)
  - [ ] Budget vs. actual
  - [ ] Treasurer dashboard
  - [ ] Export to accounting software (QuickBooks, Excel)

#### Phase 5: Donor Engagement ⭐
- **Duration**: 1-2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Automated thank you messages
  - [ ] Giving milestones (first donation, $1000+, etc.)
  - [ ] Donor appreciation events
  - [ ] Giving consistency tracking
  - [ ] Lapsed donor recovery (not given in 3+ months)
  - [ ] Giving potential scoring
  - [ ] Stewardship resources

### New Entities
1. **Donation** - Donation records (member, amount, date, type, method, campaign, receipt)
2. **RecurringDonation** - Recurring donation setup (frequency, amount, start date, end date)
3. **Pledge** - Pledge commitments (member, amount, campaign, payment schedule)
4. **Campaign** - Fundraising campaigns (name, goal, start date, end date, description)
5. **DonationType** - Enum (TITHE, OFFERING, SPECIAL_GIVING, PLEDGE, MISSIONS, BUILDING_FUND)
6. **PaymentMethod** - Enum (CASH, CHECK, MOBILE_MONEY, BANK_TRANSFER, CREDIT_CARD, DEBIT_CARD)
7. **DonationReceipt** - Receipt records (donation, receipt number, PDF URL, sent date)

### Key Endpoints
- `POST /api/giving/donations` - Record donation
- `POST /api/giving/donations/batch` - Batch entry for Sunday offerings
- `GET /api/giving/member/{id}` - Member giving history
- `POST /api/giving/recurring` - Setup recurring donation
- `POST /api/giving/pledges` - Create pledge
- `POST /api/giving/campaigns` - Create campaign
- `GET /api/giving/campaigns/{id}/progress` - Campaign progress
- `GET /api/giving/reports/statements/{memberId}` - Donor statement
- `GET /api/giving/reports/tax-receipt/{memberId}/{year}` - Tax receipt
- `GET /api/giving/analytics` - Giving analytics
- `POST /api/portal/giving/donate` - Online donation (member portal)

### E2E Test Scenarios
- Record manual donation
- Batch entry for Sunday offerings
- Online donation workflow
- Recurring donation setup
- Pledge creation and tracking
- Campaign progress tracking
- Generate donor statement
- Generate tax receipt
- Anonymous donation
- Multi-currency donation
- Payment gateway integration
- Failed payment handling
- Giving analytics accuracy

### Security Considerations
- PCI DSS compliance for card payments
- Encrypted storage of payment methods
- Audit trail for all transactions
- Role-based access (TREASURER role)
- Separation of duties (entry vs. approval)

---

## Module 7: Events Module 📦 NEW

**Status**: Not implemented (referenced in Dashboard)
**Timeline**: 4-6 weeks (3 phases)
**Priority**: ⭐⭐

### Implementation Phases

#### Phase 1: Event Management ⭐⭐⭐
- **Duration**: 2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Event creation (name, date, time, location, description)
  - [ ] Event types (service, conference, outreach, social, training, other)
  - [ ] Event recurrence (weekly, monthly, yearly)
  - [ ] Event capacity and registration limits
  - [ ] Event image/flyer upload
  - [ ] Multi-day events
  - [ ] Event categories and tags
  - [ ] Event visibility (public, members-only, leadership-only)

#### Phase 2: Event Registration & Attendance ⭐⭐
- **Duration**: 2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Member registration for events
  - [ ] Guest registration (non-members)
  - [ ] Registration forms (custom fields)
  - [ ] Waitlist management
  - [ ] Registration confirmation emails
  - [ ] Registration fees (integration with Giving module)
  - [ ] QR code tickets
  - [ ] Event check-in system
  - [ ] Attendance tracking per event

#### Phase 3: Event Calendar & Communication ⭐
- **Duration**: 1-2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Church calendar view (month, week, day)
  - [ ] Event reminders (email, SMS)
  - [ ] iCal/Google Calendar export
  - [ ] Event invitations
  - [ ] Event updates/changes notification
  - [ ] Post-event feedback forms
  - [ ] Event photo gallery
  - [ ] Event analytics (registrations, attendance, feedback)

### New Entities
1. **Event** - Event details (name, date, time, location, type, capacity, description, imageUrl)
2. **EventRegistration** - Registration records (event, member/guest, status, registration date)
3. **EventAttendance** - Attendance records (event, member, check-in time)
4. **EventType** - Enum (SERVICE, CONFERENCE, OUTREACH, SOCIAL, TRAINING, MEETING, OTHER)
5. **RegistrationStatus** - Enum (REGISTERED, WAITLIST, CANCELLED, ATTENDED)

### Key Endpoints
- `POST /api/events` - Create event
- `GET /api/events` - List events (with filters)
- `GET /api/events/calendar` - Calendar view
- `POST /api/events/{id}/register` - Register for event
- `POST /api/events/{id}/check-in/{memberId}` - Check-in attendee
- `GET /api/events/{id}/attendees` - Event attendees
- `POST /api/events/{id}/send-reminder` - Send event reminder
- `GET /api/events/{id}/analytics` - Event analytics

### E2E Test Scenarios
- Create event with capacity limit
- Member registration workflow
- Waitlist management
- Event check-in via QR code
- Event calendar display
- Event reminders
- Registration fees payment
- Post-event feedback
- Event analytics

---

## Module 8: Communications Module 📦 NEW

**Status**: Not implemented (referenced in Member portal)
**Timeline**: 6-8 weeks (4 phases)
**Priority**: ⭐⭐⭐

### Implementation Phases

#### Phase 1: SMS Communication ⭐⭐⭐
- **Duration**: 2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] SMS gateway integration (Twilio, Africa's Talking, etc.)
  - [ ] Send individual SMS
  - [ ] Bulk SMS to groups
  - [ ] SMS templates
  - [ ] SMS scheduling
  - [ ] SMS delivery status tracking
  - [ ] SMS credits management
  - [ ] SMS opt-out handling
  - [ ] Character count and cost estimation

#### Phase 2: Email Communication ⭐⭐⭐
- **Duration**: 2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Email service integration (SendGrid, Mailgun, AWS SES)
  - [ ] Email templates (with HTML editor)
  - [ ] Bulk email sending
  - [ ] Email personalization (merge fields)
  - [ ] Email scheduling
  - [ ] Open and click tracking
  - [ ] Email bounce handling
  - [ ] Unsubscribe management
  - [ ] Email attachments

#### Phase 3: WhatsApp & Push Notifications ⭐⭐
- **Duration**: 2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] WhatsApp Business API integration
  - [ ] WhatsApp message templates
  - [ ] WhatsApp broadcast lists
  - [ ] Push notifications (member app)
  - [ ] In-app messaging
  - [ ] Notification preferences per member
  - [ ] Multi-channel messaging (SMS + Email + WhatsApp)

#### Phase 4: Communication Analytics & Campaigns ⭐
- **Duration**: 1-2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Communication campaigns
  - [ ] A/B testing for messages
  - [ ] Engagement analytics (open rate, click rate, response rate)
  - [ ] Communication history per member
  - [ ] Automated workflows (welcome series, follow-up sequences)
  - [ ] Segmentation for targeted messaging
  - [ ] Communication cost tracking

### New Entities
1. **Message** - Message records (type, recipient(s), subject, body, status, sent date)
2. **MessageTemplate** - Reusable templates (type, name, subject, body, variables)
3. **MessageCampaign** - Campaign tracking (name, type, audience, messages, analytics)
4. **MessageType** - Enum (SMS, EMAIL, WHATSAPP, PUSH_NOTIFICATION, IN_APP)
5. **MessageStatus** - Enum (DRAFT, SCHEDULED, SENDING, SENT, DELIVERED, FAILED, BOUNCED)
6. **CommunicationPreference** - Member preferences (SMS opt-in, email opt-in, WhatsApp opt-in)

### Key Endpoints
- `POST /api/communications/sms/send` - Send SMS
- `POST /api/communications/email/send` - Send email
- `POST /api/communications/whatsapp/send` - Send WhatsApp message
- `POST /api/communications/bulk` - Bulk send to segment
- `POST /api/communications/campaigns` - Create campaign
- `GET /api/communications/templates` - List templates
- `GET /api/communications/analytics` - Communication analytics
- `POST /api/communications/schedule` - Schedule message
- `PUT /api/members/{id}/communication-preferences` - Update preferences

### E2E Test Scenarios
- Send individual SMS
- Bulk SMS to fellowship
- Email with template
- WhatsApp broadcast
- Message scheduling
- Campaign creation and execution
- Communication analytics
- Opt-out handling
- Delivery status tracking
- Cost estimation

### Integration Points
- Attendance reminders (from Attendance module)
- Event invitations (from Events module)
- Prayer request notifications (from Pastoral Care module)
- Giving receipts (from Giving module)
- Birthday/anniversary wishes (from Members module)

---

## Module 9: Reports Module 📦 NEW

**Status**: Not implemented (basic exports exist)
**Timeline**: 4-5 weeks (3 phases)
**Priority**: ⭐⭐

### Implementation Phases

#### Phase 1: Pre-built Reports ⭐⭐⭐
- **Duration**: 2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Member directory
  - [ ] Attendance summary (by date range, service type, fellowship)
  - [ ] Giving summary (by date range, donor, campaign)
  - [ ] Fellowship roster
  - [ ] Birthday/anniversary list
  - [ ] Inactive members report
  - [ ] First-time visitors report
  - [ ] Pastoral care summary
  - [ ] Event attendance report
  - [ ] Growth trend report

#### Phase 2: Custom Report Builder ⭐⭐
- **Duration**: 2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Drag-and-drop report builder
  - [ ] Custom fields selection
  - [ ] Filters and grouping
  - [ ] Sorting options
  - [ ] Calculated fields
  - [ ] Report templates save/reuse
  - [ ] Scheduled report generation
  - [ ] Report sharing with users

#### Phase 3: Export & Visualization ⭐
- **Duration**: 1 week
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Export to PDF, Excel, CSV
  - [ ] Print-optimized layouts
  - [ ] Charts and graphs in reports
  - [ ] Report email distribution
  - [ ] Report archiving
  - [ ] Report versioning
  - [ ] Logo and branding on reports

### New Entities
1. **Report** - Report definitions (name, type, query, filters, fields)
2. **ReportSchedule** - Scheduled report generation (report, frequency, recipients)
3. **ReportExecution** - Report run history (report, execution date, parameters, output file)

### Key Endpoints
- `GET /api/reports/pre-built` - List pre-built reports
- `POST /api/reports/generate/{type}` - Generate pre-built report
- `POST /api/reports/custom` - Create custom report
- `POST /api/reports/{id}/execute` - Execute report
- `GET /api/reports/{id}/download` - Download report output
- `POST /api/reports/{id}/schedule` - Schedule report
- `GET /api/reports/executions` - Report history

### E2E Test Scenarios
- Generate member directory
- Attendance summary with filters
- Custom report builder workflow
- Report export to Excel/PDF
- Scheduled report generation
- Report email distribution
- Chart visualization in reports

---

## Module 10: Admin Module ⏳ ENHANCEMENT

**Status**: Basic user management exists
**Timeline**: 4-5 weeks (3 phases)
**Priority**: ⭐⭐

### Current State
- User CRUD (basic)
- Church registration
- JWT authentication
- Role-based access control (SUPERADMIN, ADMIN, TREASURER, FELLOWSHIP_HEAD)

### Implementation Phases

#### Phase 1: Enhanced User Management ⭐⭐⭐
- **Duration**: 2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] User profiles with photos
  - [ ] User activity log
  - [ ] User permissions (granular)
  - [ ] User groups/teams
  - [ ] User deactivation (soft delete)
  - [ ] Password policies (complexity, expiration)
  - [ ] Two-factor authentication (2FA)
  - [ ] User invitation system (email invites)
  - [ ] User roles customization

#### Phase 2: Church Settings ⭐⭐
- **Duration**: 1-2 weeks
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Church profile (logo, contact info, social media)
  - [ ] Service times configuration
  - [ ] Fiscal year settings
  - [ ] Currency and locale settings
  - [ ] Timezone configuration
  - [ ] Email templates customization
  - [ ] SMS sender ID configuration
  - [ ] Branding (colors, fonts)
  - [ ] Terms and privacy policy
  - [ ] Data retention policies

#### Phase 3: System Administration ⭐
- **Duration**: 1 week
- **Status**: ⏳ NOT STARTED
- **Features**:
  - [ ] Audit logs (all actions)
  - [ ] System health monitoring
  - [ ] Database backup/restore
  - [ ] Data export (full church data)
  - [ ] Data import (migration from other systems)
  - [ ] API keys management
  - [ ] Webhook configurations
  - [ ] System notifications
  - [ ] Performance metrics

### Enhanced User Entity
```java
@Entity
public class User extends BaseEntity {
  // Existing fields...

  private String profileImageUrl;
  private Boolean isActive;
  private Boolean twoFactorEnabled;
  private String twoFactorSecret;

  @ElementCollection
  private Set<String> permissions; // Granular permissions

  @ManyToMany
  private List<UserGroup> groups;

  private LocalDateTime lastLoginAt;
  private LocalDateTime passwordChangedAt;
  private LocalDateTime passwordExpiresAt;
}
```

### New Entities
1. **UserGroup** - User groups/teams (name, permissions, members)
2. **AuditLog** - System audit trail (user, action, entity, timestamp, details)
3. **ChurchSettings** - Church configuration (settings key-value pairs)
4. **ApiKey** - API key management (name, key, permissions, expires at)
5. **Webhook** - Webhook configurations (URL, events, secret)

### Key Endpoints
- `POST /api/admin/users/invite` - Invite new user
- `PUT /api/admin/users/{id}/deactivate` - Deactivate user
- `POST /api/admin/users/{id}/reset-password` - Reset password
- `GET /api/admin/audit-logs` - View audit logs
- `PUT /api/admin/church/settings` - Update church settings
- `POST /api/admin/backup` - Trigger database backup
- `GET /api/admin/system/health` - System health check
- `POST /api/admin/webhooks` - Configure webhook

### E2E Test Scenarios
- User invitation workflow
- User deactivation
- 2FA setup and login
- Granular permissions assignment
- Church settings update
- Audit log viewing
- System health monitoring
- API key generation

---

## Cross-Cutting Concerns

### 1. Notifications System
**Integration across modules**:
- Member portal notifications
- Admin dashboard alerts
- Mobile push notifications
- Email digests
- SMS alerts

### 2. Search & Filtering
**Global search across**:
- Members
- Events
- Giving records
- Pastoral care needs
- Communications

### 3. Mobile App (Future)
**Phases**:
- Member app (attendance, giving, events, profile)
- Pastor app (care needs, visits, communications)
- Admin app (dashboard, reports)

### 4. API Documentation
- OpenAPI/Swagger (already integrated)
- API versioning
- Rate limiting
- API authentication (separate from user auth)

### 5. Performance Optimization
- Database indexing strategy
- Query optimization
- Caching layer (Redis)
- CDN for static assets
- Lazy loading
- Pagination strategy

### 6. Security Hardening
- OWASP top 10 compliance
- Regular security audits
- Penetration testing
- Data encryption (at rest and in transit)
- GDPR compliance (for international churches)
- Role-based access control (RBAC) enforcement

---

## Testing Strategy

### E2E Testing Requirements
**For each module**:
- Happy path scenarios
- Error handling
- Edge cases
- Cross-browser testing
- Mobile responsive testing
- Performance testing
- Security testing

### Test Coverage Goals
- Backend unit tests: 80%+
- Frontend unit tests: 70%+
- E2E tests: All critical paths
- Integration tests: All API endpoints

### Test Automation
- CI/CD pipeline integration
- Automated test runs on PR
- Nightly regression tests
- Performance benchmarking

---

## Deployment Strategy

### Phases
1. **Development** (current): Local development
2. **Staging**: Cloud staging environment
3. **Production**: Multi-tenant cloud deployment

### Infrastructure
- Cloud provider (AWS, Azure, or Google Cloud)
- Database (MySQL RDS)
- File storage (S3 or equivalent)
- CDN for frontend assets
- Load balancing for scalability
- Auto-scaling based on load

### Monitoring
- Application monitoring (New Relic, Datadog)
- Error tracking (Sentry)
- Log aggregation (ELK stack)
- Uptime monitoring
- Performance monitoring

---

## Timeline Summary

| Module | Duration | Priority | Status |
|--------|----------|----------|--------|
| Members | 14-19 weeks | ⭐⭐⭐ | 📋 Planned |
| Attendance | 6-8 weeks | ⭐⭐⭐ | 📋 Planned |
| Fellowship | 4-6 weeks | ⭐⭐ | 📋 Planned |
| Dashboard | 3-4 weeks | ⭐⭐⭐ | 📋 Planned |
| Pastoral Care | 6-8 weeks | ⭐⭐⭐ | 📋 Planned |
| Giving | 8-10 weeks | ⭐⭐⭐ | 📋 Planned |
| Events | 4-6 weeks | ⭐⭐ | 📋 Planned |
| Communications | 6-8 weeks | ⭐⭐⭐ | 📋 Planned |
| Reports | 4-5 weeks | ⭐⭐ | 📋 Planned |
| Admin | 4-5 weeks | ⭐⭐ | 📋 Planned |

**Total Estimated Time**: 63-83 weeks (15-20 months)

### Recommended Execution Order
1. **Phase 1** (3-4 months): Members (Phase 1-2), Attendance (Phase 1), Dashboard (Phase 1)
2. **Phase 2** (3-4 months): Members (Phase 3-4), Giving (Phase 1-2), Events (Phase 1)
3. **Phase 3** (3-4 months): Pastoral Care (Phase 1-2), Communications (Phase 1-2), Fellowship (Phase 1-2)
4. **Phase 4** (3-4 months): Members (Phase 5-6), Giving (Phase 3-4), Attendance (Phase 2-3)
5. **Phase 5** (2-3 months): Reports, Admin enhancements, Polish & optimization

---

## Success Metrics

### Application-Wide KPIs
- User adoption rate: 80%+ of active church staff
- Member portal adoption: 50%+ of members
- System uptime: 99.9%
- Page load time: <2 seconds
- Mobile responsiveness: 100% of pages
- E2E test coverage: 90%+ of critical paths
- User satisfaction: 4.5/5 average rating

### Module-Specific Metrics
- **Members**: 90% profile completeness, <30s to add member
- **Attendance**: 80% check-in rate, <5min to record full service
- **Giving**: 30% online giving adoption, 100% receipt automation
- **Pastoral Care**: 90% follow-up completion, <24hr response time
- **Communications**: 95% delivery rate, 40% open rate (email)
- **Events**: 70% registration rate for major events

---

## Risk Mitigation

### Technical Risks
- **Multi-tenancy data leaks**: Comprehensive testing, automated checks
- **Payment gateway failures**: Fallback mechanisms, retry logic
- **Scale issues**: Performance testing, caching, optimization
- **Data loss**: Regular backups, disaster recovery plan

### Business Risks
- **Feature creep**: Strict scope management, phased rollout
- **User adoption**: Training, documentation, onboarding tours
- **Resource constraints**: Prioritization, MVP approach
- **Competition**: Focus on unique value (pastoral care focus)

---

## Documentation

### Required Documentation
- [ ] User manuals (by role)
- [ ] Admin guides
- [ ] API documentation
- [ ] Developer onboarding
- [ ] Database schema documentation
- [ ] Deployment guides
- [ ] Troubleshooting guides
- [ ] Video tutorials

---

## Notes
- All modules designed with TDD mindset
- E2E testing mandatory for all features
- International support baked into all modules
- Mobile-first responsive design
- Accessibility (WCAG 2.1 AA) compliance
- Regular security audits
- Performance budgets enforced

**Last Review**: 2025-12-20
**Next Review**: After Members Module Phase 1 completion
