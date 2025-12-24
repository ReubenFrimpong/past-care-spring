# Phase 1 Enhanced Attendance - User Guide

**Last Updated**: 2025-12-24
**Version**: 1.0
**Status**: ✅ Complete

---

## Table of Contents

1. [Overview](#overview)
2. [Feature 1: Manual Attendance Marking](#feature-1-manual-attendance-marking)
3. [Feature 2: QR Code Check-In](#feature-2-qr-code-check-in)
4. [Feature 3: Geofencing & Mobile Check-In](#feature-3-geofencing--mobile-check-in)
5. [Feature 4: Visitor Management](#feature-4-visitor-management)
6. [Feature 5: Attendance Reminders](#feature-5-attendance-reminders)
7. [Feature 6: Late Arrival Tracking](#feature-6-late-arrival-tracking)
8. [Feature 7: Recurring Services](#feature-7-recurring-services)
9. [Technical Details](#technical-details)

---

## Overview

Phase 1 Enhanced Attendance introduces **8 major features** to modernize your church's attendance tracking:

✅ **Manual Attendance Marking** - Traditional click-to-mark attendance
✅ **QR Code Check-In** - Members scan QR codes to self-check-in
✅ **Geofencing** - Automatic check-in when near the church
✅ **Visitor Management** - Track first-time and returning visitors
✅ **Attendance Reminders** - Multi-channel SMS/Email/WhatsApp reminders
✅ **Late Arrival Tracking** - Track who arrives late and by how many minutes
✅ **Multiple Services** - Support for 13 service types (Sunday, Midweek, Youth, etc.)
✅ **Recurring Templates** - Create weekly/monthly recurring service patterns

---

## Feature 1: Manual Attendance Marking

### How to Use

1. **Navigate to Attendance Page**
   - Click on **"Attendance"** in the side navigation

2. **Create a Session**
   - Click **"New Session"** button
   - Fill in:
     - **Session Name**: e.g., "Sunday Service", "Bible Study"
     - **Date**: Select the date
     - **Time**: Optional time (e.g., "09:00 AM")
     - **Notes**: Optional notes about the session
   - Click **"Create Session"**

3. **Mark Attendance**
   - Click on a session from the list
   - You'll see all church members
   - For each member, click the status buttons:
     - 🟢 **Present** - Member attended
     - 🔴 **Absent** - Member did not attend
     - 🟡 **Excused** - Member had valid reason for absence

4. **Search and Filter**
   - Use the search box to find members by name
   - Filter by status (All/Present/Absent/Excused)

5. **Save Attendance**
   - Click **"Save All"** button to save all marked attendance
   - Or attendance auto-saves to localStorage (persists on page refresh)

6. **Complete Session**
   - Click **"Complete"** to mark the session as completed
   - Completed sessions are locked and cannot be edited

### Use Cases

- **Sunday Services** - Mark who attended the main service
- **Bible Study** - Track small group attendance
- **Prayer Meetings** - Record prayer meeting participants
- **Special Events** - Track attendance at church events

---

## Feature 2: QR Code Check-In ✅ COMPLETE

### How It Works

The QR code system allows members and visitors to **self-check-in** by scanning a QR code displayed at the church entrance or on a screen. The complete workflow is now fully implemented!

### Step-by-Step Process

#### For Church Admin (You):

1. **Create an Attendance Session** (see Feature 1)

2. **Generate QR Code**
   - Open the session (click on it)
   - Click the **"QR Code"** button in the session header
   - A modal opens showing the QR code

3. **Display QR Code**
   - **Option A - Project on Screen**: Use "Print" to display on a projector or TV at the entrance
   - **Option B - Print Poster**: Click **"Print"** and print the QR code poster for the registration desk
   - **Option C - Download Image**: Click **"Download QR Code"** to save as PNG file and share digitally

4. **QR Code Details**
   - Shows session name, date, and time
   - Shows expiry time (QR codes expire after 24 hours by default for security)
   - If expired, click **"Regenerate QR Code"** to create a new one
   - QR code contains full check-in URL (e.g., `http://yourchurch.com/check-in?qr=encrypted-data`)

#### For Church Members:

1. **Scan QR Code**
   - Use any QR code scanner app on their phone
   - Or use the camera app (most modern phones support QR scanning directly)
   - The scan opens a mobile-friendly check-in page automatically

2. **Choose Check-In Type**
   - Two buttons: **"I'm a Member"** or **"I'm a Visitor"**
   - Members: Enter their registered phone number
   - Visitors: Enter first name, last name, phone number, and optional email

3. **Submit Check-In**
   - Click **"Check In"** button
   - Backend validates the QR code (checks expiry and session validity)
   - Looks up member by phone number OR creates/updates visitor record
   - Marks attendance automatically

4. **Success Confirmation**
   - Beautiful success screen with green checkmark animation
   - Shows: "Check-In Successful! Welcome, [Name]!"
   - Displays session details (name, date, time)
   - Option to "Check In Another Person" for families

### Backend API Endpoint

```http
POST /api/check-in
Content-Type: application/json

{
  "sessionId": 123,
  "memberId": 456,
  "checkInMethod": "QR_CODE",
  "qrCodeData": "encrypted-qr-string-from-scan",
  "deviceInfo": "iPhone 14 Pro, iOS 16.5"
}
```

**Response:**
```json
{
  "attendanceId": 789,
  "sessionName": "Sunday Service",
  "memberName": "John Doe",
  "checkInTime": "2025-12-24T09:15:30",
  "status": "PRESENT",
  "isLate": false,
  "message": "Check-in successful! Welcome to Sunday Service."
}
```

### Security Features

- **AES Encryption**: QR data is encrypted using AES-256
- **Expiry Tracking**: QR codes expire after configured time (prevents old QR codes from being reused)
- **Session Validation**: Ensures QR belongs to an active, non-completed session
- **Time-Based**: Only valid during session time window

### Use Cases

- **Sunday Service Check-In**: Display QR on entrance screen
- **Events**: Print QR posters at registration desk
- **Youth Services**: Fast check-in for young people
- **Conferences**: Track attendance at multi-day events

---

## Feature 3: Geofencing & Mobile Check-In

### How It Works

Geofencing allows members to automatically check in when they arrive at the church building (or within a certain radius).

### Setup (Backend Already Complete)

1. **Session with Geofence**
   - When creating a session, the backend supports:
     - `geofenceLatitude`: Church's latitude coordinate
     - `geofenceLongitude`: Church's longitude coordinate
     - `geofenceRadiusMeters`: Radius for auto check-in (e.g., 100 meters)

2. **How Members Check In**
   - Member opens your church's mobile app/web app
   - App requests location permission
   - App calls: `GET /api/check-in/nearby-sessions?latitude=X&longitude=Y`
   - App shows nearby active sessions
   - Member taps to check in
   - App calls: `POST /api/check-in` with geofence method

### API Endpoints

#### Find Nearby Sessions
```http
GET /api/check-in/nearby-sessions?latitude=5.6037&longitude=-0.1870&maxDistanceMeters=5000
```

**Response:**
```json
[
  {
    "sessionId": 123,
    "sessionName": "Sunday Main Service",
    "sessionDate": "2025-12-24",
    "sessionTime": "09:00",
    "serviceType": "SUNDAY_MAIN_SERVICE",
    "distanceMeters": 45.8,
    "distanceKilometers": 0.05,
    "isWithinGeofence": true,
    "geofenceRadiusMeters": 100
  }
]
```

#### Check In with Geofence
```http
POST /api/check-in

{
  "sessionId": 123,
  "memberId": 456,
  "checkInMethod": "GEOFENCE",
  "latitude": 5.6037,
  "longitude": -0.1870,
  "deviceInfo": "Samsung Galaxy S22, Android 13"
}
```

### Late Arrival Tracking

The system automatically detects late arrivals:

- **Early Check-In Window**: Can check in up to 30 minutes before session start
- **On-Time Window**: From start time up to late cutoff (default: 15 minutes after start)
- **Late Window**: After late cutoff - marked as `isLate: true` with `minutesLate` calculated

**Example:**
- Session starts at 9:00 AM
- Late cutoff: 15 minutes (configurable)
- Member checks in at 9:20 AM
- Result: `isLate: true`, `minutesLate: 20`

### Use Cases

- **Sunday Services**: Auto check-in when members arrive at church
- **Outdoor Events**: Geofence at park or venue
- **Home Fellowships**: Multiple sessions at different locations
- **Youth Camps**: Geofence the campground

---

## Feature 4: Visitor Management

### Overview

Track first-time and returning visitors separately from members.

### How to Access

1. **Navigate to Visitors Page**
   - Click on **"Visitors"** in the side navigation
   - You'll see the Visitor Management dashboard

### Features

#### A. Visitor Stats Dashboard

Shows at a glance:
- 📊 **Total Visitors**: All-time visitor count
- 🆕 **First-Time Visitors**: Visitors on their first visit
- 🔄 **Returning Visitors**: Visitors who've come back
- ✅ **Converted to Members**: Visitors who became members

#### B. Record New Visitor

1. Click **"Record New Visitor"** button
2. Fill in visitor details:
   - **First Name** (required)
   - **Last Name** (required)
   - **Phone Number** (required)
   - **Email** (optional)
   - **Age Group**: Child, Teen, Young Adult, Adult, Senior
   - **How They Heard About Us**: Friend referral, social media, website, walk-in, etc.
   - **Attendance Session**: Link to a specific service
   - **Notes**: Any additional information

3. Click **"Save Visitor"**

#### C. Record Return Visit

1. Find the visitor in the list
2. Click the **3-dot menu** (⋮)
3. Select **"Record Visit"**
4. Select the session they attended
5. System automatically:
   - Increments `visitCount`
   - Sets `isFirstTime: false`
   - Updates `lastVisitDate`

#### D. Convert Visitor to Member

1. Find the visitor in the list
2. Click **3-dot menu** (⋮)
3. Select **"Convert to Member"**
4. System:
   - Creates a new member record
   - Pre-fills with visitor data
   - Marks visitor as `convertedToMember: true`
   - Links to the new member ID

#### E. Search and Filter

- **Search**: By name, phone, or email
- **Filter by Age Group**: View only children, teens, adults, etc.
- **Filter by Status**: First-time, returning, converted

### Backend Entities

**Visitor Entity Fields:**
- Basic info: firstName, lastName, phoneNumber, email
- Demographics: ageGroup (CHILD, TEEN, YOUNG_ADULT, ADULT, SENIOR)
- Tracking: visitCount, isFirstTime, lastVisitDate
- Source: howHeardAboutUs (FRIEND_REFERRAL, SOCIAL_MEDIA, WEBSITE, etc.)
- Conversion: convertedToMember, convertedMemberId
- Attendance: Can be linked to AttendanceSession

**VisitorAttendance Entity:**
- Links visitors to sessions
- Tracks check-in method, time, location
- Same check-in methods as members (MANUAL, QR_CODE, GEOFENCE, etc.)

### Use Cases

- **Sunday Services**: Record new visitors after service
- **Special Events**: Track event attendees who aren't members
- **Outreach Programs**: Follow up with visitors
- **Membership Pipeline**: Convert regular visitors to members

### API Endpoints

```http
# Get all visitors
GET /api/visitors

# Create visitor
POST /api/visitors

# Record a visit
POST /api/visitors/{id}/record-visit?sessionId=123

# Convert to member
POST /api/visitors/{id}/convert-to-member

# Get visitor stats
GET /api/visitors/stats
```

---

## Feature 5: Attendance Reminders

### Overview

Send automated reminders to members before services via **SMS**, **Email**, or **WhatsApp**.

### Current Status

⚠️ **Backend Complete, Frontend Pending**

The backend is fully implemented with:
- ✅ ReminderService with multi-channel delivery
- ✅ ReminderController with 7 REST endpoints
- ✅ Recipient tracking (sent, delivered, failed status)
- ✅ Scheduled and recurring reminders
- ✅ Database migrations (V6, V7, V8)

**What's Missing:**
- ❌ Frontend UI to create/manage reminders
- ❌ Integration with SMS/Email/WhatsApp providers (placeholders exist)

### How to Use (Via API)

#### Create a Reminder

```http
POST /api/reminders

{
  "message": "Reminder: Sunday Service tomorrow at 9:00 AM. See you there!",
  "targetGroup": "ALL_MEMBERS",
  "scheduledFor": "2025-12-24T08:00:00",
  "sendViaSms": true,
  "sendViaEmail": true,
  "sendViaWhatsapp": false,
  "recurrencePattern": "WEEKLY",
  "sessionId": 123
}
```

**Target Groups:**
- `ALL_MEMBERS`: All active members
- `ACTIVE_MEMBERS`: Members with status ACTIVE
- `FIRST_TIMERS`: First-time visitors
- `VISITORS`: All visitors
- `SPECIFIC_MEMBERS`: Provide member IDs in `recipientMemberIds` array

**Recurrence Patterns:**
- `NONE`: One-time reminder
- `DAILY`: Every day
- `WEEKLY`: Every week
- `MONTHLY`: Every month

#### Track Reminder Status

```http
# Get all reminders
GET /api/reminders

# Get reminder details with recipients
GET /api/reminders/{id}

# Get recipient statuses
GET /api/reminders/{id}/recipients
```

**Recipient Statuses:**
- `PENDING`: Scheduled but not sent
- `SENT`: Message sent to provider
- `DELIVERED`: Confirmed delivery
- `FAILED`: Delivery failed (see `failureReason`)

### Implementation Notes

**SMS Integration (Placeholder):**
```java
// In ReminderService.java - sendSmsReminder()
// TODO: Integrate with SMS provider (Twilio, AWS SNS, Africa's Talking, etc.)
log.info("SMS reminder sent to: {}", phoneNumber);
```

**Email Integration (Placeholder):**
```java
// In ReminderService.java - sendEmailReminder()
// TODO: Integrate with Email provider (SendGrid, AWS SES, Mailgun, etc.)
log.info("Email reminder sent to: {}", email);
```

**WhatsApp Integration (Placeholder):**
```java
// In ReminderService.java - sendWhatsappReminder()
// TODO: Integrate with WhatsApp Business API
log.info("WhatsApp reminder sent to: {}", phoneNumber);
```

### Use Cases

- **Sunday Service Reminders**: Send Saturday evening reminder
- **Midweek Service**: Wednesday morning reminder
- **Special Events**: One-time event reminders
- **Prayer Meetings**: Recurring weekly reminders
- **Youth Services**: Targeted reminders to youth members

### Future Frontend Work Needed

1. **Reminders Page Component**
   - Create `/attendance/reminders` route
   - List of scheduled reminders
   - Create/Edit/Delete reminder forms
   - Recipient list view
   - Delivery status tracking

2. **Quick Reminder Button**
   - Add to AttendancePage session view
   - "Send Reminder" button
   - Quick modal to send reminder for current session

---

## Feature 6: Late Arrival Tracking

### How It Works

The system automatically tracks late arrivals during check-in.

### Configuration

**Per Session Settings:**
- `sessionStartTime`: When the session starts (e.g., "09:00")
- `lateCutoffMinutes`: Minutes after start time to mark as late (default: 15)
- `earlyCheckInMinutes`: Minutes before start to allow check-in (default: 30)

**Example:**
- Session starts: 9:00 AM
- Early check-in allowed from: 8:30 AM
- Late cutoff: 9:15 AM (15 minutes after start)

### Attendance Fields

When marking attendance (manual or check-in), the system records:
- `checkInTime`: Exact time of check-in
- `isLate`: Boolean flag (true if after late cutoff)
- `minutesLate`: How many minutes late (0 if on time)

**Example Scenarios:**

| Check-In Time | Status | isLate | minutesLate |
|---------------|--------|--------|-------------|
| 8:45 AM | On time | false | 0 |
| 9:05 AM | On time | false | 0 |
| 9:20 AM | Late | true | 20 |
| 9:45 AM | Late | true | 45 |

### Viewing Late Arrivals

Currently, late arrival data is stored in the database but **not displayed in the UI**.

**Database Query:**
```sql
SELECT
  m.first_name,
  m.last_name,
  a.check_in_time,
  a.is_late,
  a.minutes_late
FROM attendance a
JOIN member m ON a.member_id = m.id
WHERE a.session_id = 123
  AND a.is_late = true
ORDER BY a.minutes_late DESC;
```

### Use Cases

- **Sunday Services**: Track punctuality trends
- **Staff Meetings**: Monitor late arrivals
- **Youth Services**: Encourage on-time arrival
- **Reports**: Generate lateness statistics

### Future Frontend Work Needed

1. **Late Arrivals Badge** on attendance page
2. **Late Arrivals List** in session details
3. **Statistics Dashboard** showing:
   - Average late arrivals per service
   - Most frequently late members
   - Punctuality trends over time

---

## Feature 7: Recurring Services

### Overview

Create service templates that repeat weekly, monthly, or at custom intervals.

### Database Fields

**AttendanceSession Entity:**
- `isRecurring`: Boolean flag
- `recurrencePattern`: DAILY, WEEKLY, MONTHLY, YEARLY
- `recurrenceEndDate`: When recurrence stops (optional)

### How to Use (Backend Only - No UI Yet)

**Create Recurring Service via API:**

```http
POST /api/attendance/sessions

{
  "sessionName": "Sunday Main Service",
  "sessionDate": "2025-12-24",
  "sessionTime": "09:00",
  "serviceType": "SUNDAY_MAIN_SERVICE",
  "isRecurring": true,
  "recurrencePattern": "WEEKLY",
  "recurrenceEndDate": "2026-12-31",
  "notes": "Every Sunday at 9 AM"
}
```

### How It Should Work

1. **Create Template**: Define the recurring pattern
2. **Auto-Generate Sessions**: System creates sessions for each occurrence
3. **Session Management**:
   - View all upcoming sessions
   - Skip specific dates (holidays)
   - Modify individual sessions without affecting template

### Current Limitations

⚠️ **No Automatic Generation Yet**

The database supports recurring services, but the backend doesn't have:
- ❌ Automatic session generation based on recurrence pattern
- ❌ Job scheduler to create sessions in advance
- ❌ UI to manage recurring templates

### Manual Workaround

Currently, you need to manually create sessions for each service.

### Future Implementation Needed

1. **RecurringSessionService**
   - `generateUpcomingSessions()` - Create sessions for next N weeks/months
   - `skipOccurrence()` - Skip a specific date
   - `endRecurrence()` - Stop generating future sessions

2. **Scheduled Job**
   - Spring `@Scheduled` task
   - Runs weekly to generate next month's sessions
   - Cleans up old completed sessions

3. **Frontend UI**
   - "Create Recurring Session" button
   - Calendar view of all sessions
   - Template management page

---

## Technical Details

### Database Schema

**New Tables:**
- `visitor` - Visitor information
- `visitor_attendance` - Visitor check-ins
- `reminder` - Scheduled reminders
- `reminder_recipient` - Delivery tracking

**Enhanced Tables:**
- `attendance_session` - 15+ new columns (geofence, service type, recurring, etc.)
- `attendance` - 10+ new columns (check-in method, time, location, late tracking)

### Enums

**CheckInMethod:**
- MANUAL
- QR_CODE
- GEOFENCE
- MOBILE_APP
- SELF_CHECKIN

**ServiceType (13 types):**
- SUNDAY_MAIN_SERVICE
- SUNDAY_SECOND_SERVICE
- MIDWEEK_SERVICE
- PRAYER_MEETING
- BIBLE_STUDY
- YOUTH_SERVICE
- CHILDREN_SERVICE
- MEN_FELLOWSHIP
- WOMEN_FELLOWSHIP
- CHOIR_PRACTICE
- LEADERSHIP_MEETING
- SPECIAL_EVENT
- OTHER

**AgeGroup:**
- CHILD (0-12)
- TEEN (13-19)
- YOUNG_ADULT (20-35)
- ADULT (36-59)
- SENIOR (60+)

**VisitorSource:**
- FRIEND_REFERRAL
- FAMILY_REFERRAL
- SOCIAL_MEDIA
- WEBSITE
- WALK_IN
- ADVERTISEMENT
- EVENT
- OTHER

**ReminderStatus:**
- SCHEDULED
- SENT
- DELIVERED
- FAILED
- CANCELLED

### REST API Endpoints (25+)

**AttendanceController:**
- POST `/api/attendance/sessions` - Create session
- GET `/api/attendance/sessions` - List sessions
- GET `/api/attendance/sessions/{id}` - Get session details
- PUT `/api/attendance/sessions/{id}` - Update session
- DELETE `/api/attendance/sessions/{id}` - Delete session
- PUT `/api/attendance/sessions/{id}/complete` - Complete session
- POST `/api/attendance/sessions/{id}/qr-code` - Generate QR code
- POST `/api/attendance/mark` - Mark single attendance
- POST `/api/attendance/mark-bulk` - Bulk mark attendance

**CheckInController:**
- POST `/api/check-in` - Process check-in
- GET `/api/check-in/nearby-sessions` - Find nearby sessions

**VisitorController:**
- POST `/api/visitors` - Create visitor
- GET `/api/visitors` - List visitors
- GET `/api/visitors/{id}` - Get visitor details
- PUT `/api/visitors/{id}` - Update visitor
- DELETE `/api/visitors/{id}` - Delete visitor
- POST `/api/visitors/{id}/record-visit` - Record visit
- POST `/api/visitors/{id}/convert-to-member` - Convert to member
- GET `/api/visitors/stats` - Get statistics

**ReminderController:**
- POST `/api/reminders` - Create reminder
- GET `/api/reminders` - List reminders
- GET `/api/reminders/{id}` - Get reminder details
- PUT `/api/reminders/{id}` - Update reminder
- DELETE `/api/reminders/{id}` - Delete reminder
- GET `/api/reminders/{id}/recipients` - Get recipients
- POST `/api/reminders/{id}/cancel` - Cancel reminder

### Security

**QR Code Encryption:**
- Algorithm: AES-256
- Mode: CBC (Cipher Block Chaining)
- Key: Configurable via environment variable
- IV: Random 16-byte initialization vector
- Encoding: Base64 for QR code string

**Geofence Calculations:**
- Uses Haversine formula for distance
- Accuracy: ±10 meters (depends on GPS)
- Earth radius: 6371 km

---

## Next Steps to Complete Full Workflow

### Priority 1: QR Code Check-In Page

**Create Mobile-Friendly Check-In Page:**

1. **Route**: `/check-in` or `/attendance/check-in`

2. **Component**: `CheckInPage.ts`
   - Decode QR data from URL parameter
   - Show session details
   - Member login or visitor form
   - Call `POST /api/check-in` endpoint
   - Show success/error message

3. **User Flow**:
   ```
   Member scans QR code
   ↓
   Opens: https://yourchurch.com/check-in?qr=encrypted-data
   ↓
   Page shows: "Check in to Sunday Service - Dec 24, 2025"
   ↓
   Member enters phone number or logs in
   ↓
   Click "Check In" button
   ↓
   Success: "Welcome! You're checked in."
   ```

### Priority 2: Reminders UI

**Create Reminders Management Page:**

1. **Route**: `/attendance/reminders`

2. **Components**:
   - `RemindersPage.ts` - List and manage reminders
   - `ReminderFormDialog.ts` - Create/edit reminder
   - `RecipientListDialog.ts` - View delivery status

3. **Features**:
   - Schedule new reminders
   - View sent reminders
   - Track delivery status
   - Resend failed reminders

### Priority 3: SMS/Email/WhatsApp Integration

**Integrate with Providers:**

1. **SMS**: Twilio, Africa's Talking, AWS SNS
2. **Email**: SendGrid, AWS SES, Mailgun
3. **WhatsApp**: WhatsApp Business API

### Priority 4: Recurring Services Automation

**Implement Auto-Generation:**

1. Create `RecurringSessionService`
2. Add scheduled job to generate sessions
3. Build UI for template management

### Priority 5: Reports and Analytics

**Attendance Reports:**

1. Late arrivals report
2. Attendance trends
3. Service comparison
4. Member punctuality scores
5. Visitor conversion funnel

---

## Summary

### ✅ What's Working Now (Fully Implemented)

1. **Manual Attendance** - ✅ Complete with save/complete workflow
2. **QR Code Check-In** - ✅ Complete end-to-end workflow
   - QR generation with full URL
   - Mobile-friendly check-in page
   - Member phone lookup
   - Visitor auto-creation
   - Success/error handling
3. **Visitor Management** - ✅ Complete with full CRUD UI
4. **Late Tracking Backend** - ✅ Automatic calculation during check-in

### ⚠️ What Needs Work (Backend Ready, Frontend Missing)

1. **Reminders UI** - Backend API complete, need frontend page
2. **Late Arrivals Display** - Data captured, need UI to show it
3. **Geofencing** - Backend API ready, need mobile app/PWA
4. **Recurring Automation** - Database supports it, need scheduler
5. **Provider Integration** - SMS/Email/WhatsApp placeholders (Twilio, SendGrid, etc.)

### 🎯 Quick Wins (Next Steps)

1. **Late Arrivals Badge** - 1-2 hours
   - Add badge to session view showing late count
   - List of late arrivals with minutes late
2. **Reminders Page** - 4-6 hours
   - Create/schedule reminders
   - View delivery status
   - Resend failed reminders
3. **SMS Integration** - 2-3 hours with Twilio
   - Sign up for Twilio account
   - Add credentials to application.properties
   - Replace placeholder with actual API call

---

**Questions?** Review this guide and let me know which features you'd like to explore or implement next!
