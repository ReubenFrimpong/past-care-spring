# Phase 1: Enhanced Attendance Tracking - User Guide

## Overview
Phase 1 adds powerful attendance tracking features to PastCare, making it easy to track who attends services through multiple check-in methods including QR codes, mobile apps, and geofencing.

---

## Table of Contents
1. [Manual Attendance Marking](#1-manual-attendance-marking)
2. [QR Code Check-In](#2-qr-code-check-in)
3. [Mobile Self Check-In](#3-mobile-self-check-in)
4. [Geofencing Auto Check-In](#4-geofencing-auto-check-in)
5. [Visitor Management](#5-visitor-management)
6. [Attendance Reminders](#6-attendance-reminders)
7. [Late Arrival Tracking](#7-late-arrival-tracking)
8. [API Endpoints Reference](#8-api-endpoints-reference)

---

## 1. Manual Attendance Marking

**What it is**: Traditional method where church staff manually marks attendance for members.

### How to Use:
1. Navigate to **Attendance** page from the side menu
2. Click **"New Session"** button to create an attendance session
3. Fill in:
   - **Session Name**: e.g., "Sunday Main Service"
   - **Date**: Select the service date
   - **Time**: Optional service time
   - **Notes**: Optional notes about the session
4. Click **"Create Session"**
5. Select the session from the list
6. For each member, click the status button:
   - **Green checkmark** = Present
   - **Red X** = Absent
   - **Yellow clock** = Excused
7. Optionally add remarks for specific members
8. Click **"Save All"** to save attendance records
9. Click **"Complete"** to finalize the session (locks it from further edits)

**Check-In Method**: `MANUAL`

---

## 2. QR Code Check-In

**What it is**: Members scan a QR code displayed on a screen/projector to automatically mark themselves present.

### How it Works:
1. **Admin generates QR code** for the session
2. **QR code is displayed** on screen/projector at church entrance
3. **Members scan** the QR code with their phones
4. **System automatically marks** them present with timestamp and location

### Setup Instructions:

#### Step 1: Create a Session
1. Go to **Attendance** page
2. Create a new session (or select existing one)

#### Step 2: Generate QR Code
1. Select the session
2. Click the **"QR Code"** button in the session header
3. A modal opens showing:
   - **QR Code image** (encrypted, secure)
   - **Expiry time** (default: 24 hours)
   - **Session details**

#### Step 3: Display QR Code
Choose one of these options:
- **Display on screen**: Click **"Fullscreen"** to show QR on projector
- **Print**: Click **"Print"** to print QR code posters
- **Download**: Click **"Download"** to save as image file

#### Step 4: Member Scans QR Code
The QR code contains an encrypted link like:
```
http://yourchurch.com/check-in?code=ABC123ENCRYPTED...
```

When a member scans it:
1. Their phone camera opens the link
2. They're taken to a check-in page
3. They enter their **phone number** to identify themselves
4. System marks them **PRESENT** automatically
5. Records:
   - Check-in time
   - Location (latitude/longitude)
   - Device info (browser/phone type)
   - Check-in method: `QR_CODE`

### Security Features:
- **AES-128 encryption**: QR data is encrypted, can't be tampered with
- **Expiry tracking**: QR codes expire after 24 hours (configurable)
- **Session-specific**: Each session has unique QR code
- **One-time scan**: Can't scan same QR from multiple devices simultaneously

### Best Practices:
✅ Generate fresh QR code for each service
✅ Display QR at church entrance/lobby
✅ Have backup tablets for members without phones
✅ Test QR code before service starts
✅ Regenerate if QR expires during long services

**Check-In Method**: `QR_CODE`

---

## 3. Mobile Self Check-In

**What it is**: Members use their phones to check themselves in from anywhere (if geofencing allows).

### How it Works:
Members access: `http://yourchurch.com/check-in`

1. **Enter phone number** to identify themselves
2. System shows **nearby sessions** based on GPS location
3. Member **selects the correct session**
4. Clicks **"Check In"**
5. System marks them present with:
   - GPS coordinates
   - Check-in time
   - Device info
   - Distance from church (if geofencing enabled)

### API Endpoint:
```http
POST /api/check-in
Content-Type: application/json

{
  "sessionId": 1,
  "memberId": 42,
  "latitude": 5.6037,
  "longitude": -0.1870,
  "deviceInfo": "iPhone 14, Safari 17.0"
}
```

### Response:
```json
{
  "id": 123,
  "memberId": 42,
  "memberName": "John Doe",
  "attendanceSessionId": 1,
  "status": "PRESENT",
  "checkInMethod": "MOBILE_APP",
  "checkInTime": "2025-12-24T10:15:30",
  "checkInLocationLat": 5.6037,
  "checkInLocationLong": -0.1870,
  "isLate": false,
  "minutesLate": null,
  "deviceInfo": "iPhone 14, Safari 17.0"
}
```

**Check-In Method**: `MOBILE_APP`

---

## 4. Geofencing Auto Check-In

**What it is**: System automatically detects when members are near the church and prompts them to check in.

### How it Works:
1. **Session has geofence configured**:
   - Latitude: Church location (e.g., 5.6037)
   - Longitude: Church location (e.g., -0.1870)
   - Radius: Detection zone (e.g., 100 meters)
2. **Member's phone detects location** via GPS
3. **App finds nearby sessions** within geofence radius
4. **Notification prompts** member to check in
5. **One-tap check-in** marks them present

### API: Find Nearby Sessions
```http
GET /api/check-in/nearby-sessions?latitude=5.6037&longitude=-0.1870&radiusKm=0.5
```

### Response:
```json
[
  {
    "sessionId": 1,
    "sessionName": "Sunday Main Service",
    "sessionDate": "2025-12-24",
    "sessionTime": "09:00",
    "distance": 45.3,
    "distanceUnit": "meters",
    "withinGeofence": true,
    "geofenceRadiusMeters": 100
  }
]
```

### Configuration (in database):
Each `AttendanceSession` can have:
- `geofenceLatitude`: Church latitude
- `geofenceLongitude`: Church longitude
- `geofenceRadiusMeters`: Detection radius (default: 100m)

**Check-In Method**: `GEOFENCE`

---

## 5. Visitor Management

**What it is**: Track first-time visitors and repeat visitors separately from regular members.

### Frontend Access:
Navigate to **Visitors** page from the side menu (Community section).

### Features:

#### A. Record New Visitor
1. Click **"Add Visitor"** button
2. Fill in visitor details:
   - First Name *
   - Last Name *
   - Phone Number *
   - Email (optional)
   - Age Group: Child, Teen, Young Adult, Adult, Senior
   - How they heard about church: Friend, Family, Social Media, Website, Walk-in, Event, etc.
3. Click **"Save"**
4. System automatically sets:
   - `isFirstTime = true`
   - `visitCount = 1`

#### B. Record Repeat Visit
When a visitor returns:
1. Find the visitor in the list
2. Click **"Record Visit"** button
3. System increments `visitCount`
4. Sets `isFirstTime = false` if it was their first visit

#### C. Convert Visitor to Member
When a visitor joins as a member:
1. Click **"Convert to Member"** on visitor card
2. System:
   - Creates full `Member` record
   - Copies visitor data (name, phone, email)
   - Sets `convertedToMemberId`
   - Marks `convertedToMember = true`

#### D. View Statistics
Dashboard shows:
- Total visitors
- First-time visitors this month
- Repeat visitors
- Conversion rate (visitors → members)

### API Endpoints:
```http
# Create visitor
POST /api/visitors
{
  "firstName": "Jane",
  "lastName": "Smith",
  "phoneNumber": "0241234567",
  "email": "jane@example.com",
  "ageGroup": "YOUNG_ADULT",
  "howHeardAboutUs": "SOCIAL_MEDIA"
}

# Record a visit
POST /api/visitors/{id}/record-visit

# Get first-time visitors
GET /api/visitors/first-time

# Convert to member
POST /api/visitors/convert
{
  "visitorId": 5,
  "memberData": {
    "firstName": "Jane",
    "lastName": "Smith",
    "phoneNumber": "0241234567",
    "email": "jane@example.com",
    "sex": "FEMALE",
    "maritalStatus": "SINGLE"
  }
}
```

**Use Cases**:
- Track evangelism effectiveness
- Follow up with first-timers
- Identify regular visitors for membership
- Analyze visitor sources (which marketing works)

---

## 6. Attendance Reminders

**What it is**: Automated reminders sent to members before services via SMS, Email, or WhatsApp.

### Backend Implementation:
The system has a `Reminder` entity and `ReminderRecipient` tracking individual delivery status.

### How it Works:
1. **Admin creates reminder**:
   - Message text
   - Target group (All, Fellowship X, Age Group, etc.)
   - Schedule time (e.g., "Saturday 6 PM before Sunday service")
   - Channels: SMS ✓, Email ✓, WhatsApp ✓
2. **System schedules reminder**
3. **At scheduled time**, system:
   - Finds all members matching target group
   - Creates `ReminderRecipient` for each member
   - Sends via selected channels
   - Tracks delivery status: SENT → DELIVERED / FAILED

### Reminder Status Flow:
```
SCHEDULED → SENT → DELIVERED
                ↓
              FAILED
```

### Database Schema:
```sql
-- Reminder table
CREATE TABLE reminder (
  id BIGINT PRIMARY KEY,
  church_id BIGINT,
  message TEXT,
  target_group VARCHAR(100),  -- "ALL", "FELLOWSHIP_5", "AGE_YOUNG_ADULT"
  scheduled_for TIMESTAMP,
  status VARCHAR(20),  -- SCHEDULED, SENT, DELIVERED, FAILED, CANCELLED
  send_via_sms BOOLEAN,
  send_via_email BOOLEAN,
  send_via_whatsapp BOOLEAN,
  recipient_count INT,
  recurrence_pattern VARCHAR(100)  -- "WEEKLY_SUNDAY_6PM"
);

-- Recipient tracking
CREATE TABLE reminder_recipient (
  id BIGINT PRIMARY KEY,
  reminder_id BIGINT,
  member_id BIGINT,
  status VARCHAR(20),  -- SCHEDULED, SENT, DELIVERED, FAILED
  sent_at TIMESTAMP,
  delivered_at TIMESTAMP,
  failed_at TIMESTAMP,
  failure_reason VARCHAR(500)
);
```

### Recurring Reminders:
Set `recurrencePattern` to auto-send weekly:
- `WEEKLY_SUNDAY_6PM` - Every Saturday at 6 PM
- `WEEKLY_WEDNESDAY_5PM` - Every Tuesday at 5 PM for midweek service

**Note**: Frontend UI for reminders is **NOT YET IMPLEMENTED**. Backend API is ready but needs a Reminders page to be built.

---

## 7. Late Arrival Tracking

**What it is**: System automatically detects when members check in after service starts and marks them as late.

### How it Works:
Each `AttendanceSession` has:
- `sessionTime`: When service starts (e.g., "09:00")
- `lateCutoffMinutes`: Grace period (default: 30 minutes)
- `allowLateCheckin`: Whether to allow late check-ins (default: true)

When a member checks in:
1. **Compare check-in time** to session start time
2. **If after session start**:
   - Set `isLate = true`
   - Calculate `minutesLate = checkInTime - sessionTime`
3. **If within grace period** (< 30 min late):
   - Still mark as PRESENT
4. **If beyond grace period** and `allowLateCheckin = false`:
   - Reject check-in

### Example:
```
Session Time: 09:00 AM
Late Cutoff: 30 minutes
Allow Late Check-in: true

Check-in at 09:15 AM → isLate=true, minutesLate=15 ✅ Allowed
Check-in at 09:45 AM → isLate=true, minutesLate=45 ✅ Allowed
Check-in at 10:30 AM → isLate=true, minutesLate=90 ✅ Allowed

If allowLateCheckin = false:
Check-in at 09:35 AM → ❌ Rejected (beyond 30-min grace period)
```

### Database Fields:
```java
@Entity
public class Attendance {
  private Boolean isLate = false;
  private Integer minutesLate;
  private LocalDateTime checkInTime;
}

@Entity
public class AttendanceSession {
  private LocalTime sessionTime;
  private Integer lateCutoffMinutes = 30;
  private Boolean allowLateCheckin = true;
}
```

**Use Cases**:
- Identify habitually late members for pastoral care
- Analytics: "What % of members arrive late?"
- Encourage punctuality by showing late statistics

---

## 8. API Endpoints Reference

### Attendance Sessions

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/attendance/sessions` | Create new session |
| GET | `/api/attendance/sessions` | Get all sessions |
| GET | `/api/attendance/sessions/{id}` | Get session details |
| PUT | `/api/attendance/sessions/{id}` | Update session |
| PUT | `/api/attendance/sessions/{id}/complete` | Complete session (lock) |
| POST | `/api/attendance/sessions/{id}/qr-code` | Generate QR code |

### Attendance Marking

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/attendance/mark` | Mark single attendance |
| POST | `/api/attendance/mark-bulk` | Mark multiple attendances |
| GET | `/api/attendance/session/{sessionId}` | Get session attendances |
| GET | `/api/attendance/member/{memberId}` | Get member's attendance history |

### Mobile Check-In

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/check-in` | Check in via mobile/QR |
| GET | `/api/check-in/nearby-sessions` | Find nearby sessions (geofencing) |

**Parameters for nearby-sessions**:
- `latitude` (required): Member's GPS latitude
- `longitude` (required): Member's GPS longitude
- `radiusKm` (optional): Search radius in km (default: 0.5)

### Visitor Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/visitors` | Create visitor |
| GET | `/api/visitors` | Get all visitors |
| GET | `/api/visitors/{id}` | Get visitor details |
| POST | `/api/visitors/{id}/record-visit` | Record repeat visit |
| GET | `/api/visitors/first-time` | Get first-time visitors |
| GET | `/api/visitors/non-converted` | Get visitors not yet members |
| POST | `/api/visitors/convert` | Convert visitor to member |
| GET | `/api/visitors/by-phone?phone=...` | Find visitor by phone |

---

## Summary: What's Accessible Now

### ✅ Fully Accessible (Frontend + Backend)
1. **Manual Attendance Marking** - Attendance page
2. **QR Code Generation** - "QR Code" button in session view
3. **Visitor Management** - Visitors page (add, view, record visits, convert)

### ⚙️ Backend Ready, No Frontend Yet
4. **Mobile Check-In** - API works, needs a `/check-in` public page
5. **Geofencing** - API works, needs mobile app integration
6. **Attendance Reminders** - API works, needs Reminders management page

### 📝 What You Need to Build Next
If you want to use the advanced features:

1. **Check-In Public Page** (`/check-in` route):
   - Show nearby sessions
   - Let members enter phone number
   - One-click check-in
   - Show success/error messages

2. **Reminders Management Page** (`/reminders` route):
   - Create reminders
   - Select target groups
   - Schedule delivery
   - View delivery status
   - Set recurring patterns

3. **Mobile App** (optional):
   - Native iOS/Android app
   - Background geofence monitoring
   - Push notifications for nearby sessions
   - One-tap check-in

---

## Testing the QR Code Feature

### Quick Test:
1. Go to Attendance page
2. Create a session for today
3. Select the session
4. Click "QR Code" button
5. Modal shows QR code with encrypted data
6. Try these actions:
   - **Download**: Saves QR as PNG
   - **Print**: Opens print dialog
   - **Fullscreen**: Shows QR in fullscreen (press ESC to exit)

### What the QR Contains:
The QR encodes an encrypted string like:
```
ABC123XYZ789...
```

When decrypted (using your secret key), it contains:
```
{sessionId}:{expiryTimestamp}
```

Example decrypted:
```
42:2025-12-25T10:00:00
```

### To Complete the Check-In Flow:
You need to build a public page that:
1. Accepts the QR data as URL parameter: `/check-in?code=ABC123...`
2. Validates the QR code (not expired, valid session)
3. Asks member to identify themselves (phone number)
4. Calls `POST /api/check-in` with:
   - sessionId (from decrypted QR)
   - memberId (from phone lookup)
   - GPS location
   - Device info

---

## Next Steps

To complete Phase 1, you should:

1. **Build Check-In Public Page** - So QR codes actually work end-to-end
2. **Build Reminders Page** - To manage attendance reminders
3. **Test Each Feature** - Create test scenarios for all check-in methods
4. **Add Analytics** (Phase 2) - Track attendance trends, late arrivals, etc.

Would you like me to help you build the Check-In public page or the Reminders management page?
