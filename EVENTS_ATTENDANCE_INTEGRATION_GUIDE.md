# Events-Attendance Integration User Guide

## Overview

The PastCare system seamlessly integrates the **Events Module** and **Attendance Module** to provide comprehensive tracking for church services and activities. This integration allows you to:

- Automatically generate attendance tracking for recurring services (e.g., Sunday services)
- Link events to attendance sessions for unified reporting
- Track attendance for special events, conferences, and regular services
- Manage both event registration and attendance in one place

---

## Table of Contents

1. [Understanding the Integration](#understanding-the-integration)
2. [Setting Up a Recurring Sunday Service](#setting-up-a-recurring-sunday-service)
3. [Manual Attendance Session Creation](#manual-attendance-session-creation)
4. [Viewing Event Attendance](#viewing-event-attendance)
5. [API Reference](#api-reference)
6. [Common Use Cases](#common-use-cases)
7. [Troubleshooting](#troubleshooting)

---

## Understanding the Integration

### How It Works

The Events-Attendance integration creates a bidirectional link between events and attendance tracking:

```
Event (e.g., Sunday Service)
    ↓
Recurring Event Instances
    ↓
Auto-generated Attendance Sessions
    ↓
Attendance Records (Member check-ins)
```

### Database Relationship

- **AttendanceSession** table has an optional `event_id` foreign key
- When an event is linked to an attendance session, the session inherits:
  - Event name
  - Event date and time
  - Event capacity (if set)
  - Check-in windows (1 hour before event to event end time)

### Auto-Generation Rules

Attendance sessions are **automatically generated** for recurring events of these types:
- `SERVICE` - Sunday services, midweek services
- `PRAYER` - Prayer meetings
- `YOUTH` - Youth services

Other event types (conferences, social events, outreach) can manually create attendance sessions if needed.

---

## Setting Up a Recurring Sunday Service

### Step 1: Create the Parent Recurring Event

**API Endpoint:** `POST /api/events`

**Request Body:**
```json
{
  "name": "Sunday Main Service",
  "description": "Weekly Sunday worship service",
  "eventType": "SERVICE",
  "startDate": "2025-01-05T10:00:00",
  "endDate": "2025-01-05T12:00:00",
  "timezone": "Africa/Nairobi",
  "locationType": "PHYSICAL",
  "physicalLocation": "Main Sanctuary",
  "requiresRegistration": false,
  "visibility": "PUBLIC",
  "isRecurring": true,
  "recurrencePattern": "WEEKLY",
  "recurrenceEndDate": "2025-12-31",
  "reminderDaysBefore": 0
}
```

**Response:**
```json
{
  "id": 1,
  "name": "Sunday Main Service",
  "isRecurring": true,
  "recurrencePattern": "WEEKLY",
  "eventStatus": "UPCOMING"
}
```

### Step 2: Generate Recurring Instances

**API Endpoint:** `POST /api/events/{id}/generate-instances?maxInstances=52`

This will:
1. Generate 52 event instances (one per week for a year)
2. **Automatically create 52 attendance sessions** (one for each instance)
3. Link each attendance session to its corresponding event

**Response:**
```json
{
  "message": "Successfully generated 52 recurring instances",
  "count": 52,
  "instances": [...]
}
```

**Backend Log Output:**
```
INFO: Generated 52 recurring instances for event 1
INFO: Auto-generated 52 attendance sessions for recurring event 1
```

### Step 3: Verify Attendance Sessions Were Created

**API Endpoint:** `GET /api/events/1/attendance-sessions`

**Response:**
```json
[
  {
    "id": 1,
    "sessionName": "Sunday Main Service",
    "sessionDate": "2025-01-05",
    "sessionTime": "10:00:00",
    "eventId": 2,
    "eventName": "Sunday Main Service",
    "churchId": 1,
    "isCompleted": false
  },
  {
    "id": 2,
    "sessionName": "Sunday Main Service",
    "sessionDate": "2025-01-12",
    "sessionTime": "10:00:00",
    "eventId": 3,
    "eventName": "Sunday Main Service",
    "churchId": 1,
    "isCompleted": false
  }
  // ... 50 more sessions
]
```

### Step 4: Members Check In to Services

**API Endpoint:** `POST /api/attendance/mark`

**Request Body:**
```json
{
  "memberId": 123,
  "attendanceSessionId": 1,
  "status": "PRESENT",
  "checkInMethod": "QR_CODE",
  "checkInTime": "2025-01-05T09:55:00"
}
```

---

## Manual Attendance Session Creation

For events that don't auto-generate attendance sessions (conferences, social events), you can manually create them.

### Option 1: Create from Event

**API Endpoint:** `POST /api/events/{eventId}/create-attendance-session`

**Response:**
```json
{
  "id": 100,
  "sessionName": "Annual Church Conference",
  "sessionDate": "2025-06-15",
  "eventId": 45,
  "eventName": "Annual Church Conference"
}
```

### Option 2: Create Directly with Event Link

**API Endpoint:** `POST /api/attendance/sessions`

**Request Body:**
```json
{
  "sessionName": "Youth Camp - Day 1",
  "sessionDate": "2025-07-20",
  "sessionTime": "09:00:00",
  "churchId": 1,
  "eventId": 78,
  "notes": "First day of youth camp"
}
```

---

## Viewing Event Attendance

### Get Attendance for Specific Event

**API Endpoint:** `GET /api/events/{eventId}/attendance-sessions`

Returns all attendance sessions linked to that event.

### Get All Events with Attendance Tracking

**API Endpoint:** `GET /api/events/with-attendance-tracking`

Returns all events in your church that have attendance sessions linked.

### View Session Details with Attendance Records

**API Endpoint:** `GET /api/attendance/sessions/{sessionId}`

**Response:**
```json
{
  "id": 1,
  "sessionName": "Sunday Main Service",
  "sessionDate": "2025-01-05",
  "eventId": 2,
  "eventName": "Sunday Main Service",
  "isCompleted": true,
  "totalMembers": 150,
  "presentCount": 142,
  "absentCount": 5,
  "excusedCount": 3,
  "attendances": [
    {
      "id": 1,
      "memberName": "John Doe",
      "status": "PRESENT",
      "checkInTime": "2025-01-05T09:55:00",
      "checkInMethod": "QR_CODE"
    }
    // ... more records
  ]
}
```

---

## API Reference

### Event Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/events` | Create event (recurring or one-time) |
| POST | `/api/events/{id}/generate-instances` | Generate recurring instances |
| GET | `/api/events/{id}/attendance-sessions` | Get attendance sessions for event |
| POST | `/api/events/{id}/create-attendance-session` | Manually create attendance session |
| GET | `/api/events/with-attendance-tracking` | List events with attendance |

### Attendance Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/attendance/sessions` | Create attendance session (can link to event) |
| GET | `/api/attendance/sessions/{id}` | Get session with attendance records |
| POST | `/api/attendance/mark` | Mark member attendance |
| POST | `/api/attendance/mark-bulk` | Mark multiple members at once |

---

## Common Use Cases

### Use Case 1: Weekly Sunday Service

**Scenario:** Set up automatic attendance tracking for every Sunday at 10 AM.

**Steps:**
1. Create recurring event with `eventType: "SERVICE"`, `recurrencePattern: "WEEKLY"`
2. Generate instances: `POST /api/events/{id}/generate-instances?maxInstances=52`
3. Attendance sessions are **automatically created**
4. Each week, members check in via QR code or manual entry
5. View reports: `GET /api/attendance/sessions/{sessionId}`

### Use Case 2: Special Conference with Multiple Sessions

**Scenario:** 3-day conference with morning and evening sessions.

**Steps:**
1. Create parent event: "Annual Conference 2025"
2. Create 6 individual event instances (3 days × 2 sessions)
3. For each instance, manually create attendance session:
   ```
   POST /api/events/{instanceId}/create-attendance-session
   ```
4. Track attendance for each session separately

### Use Case 3: Fellowship-Specific Service

**Scenario:** Youth fellowship has separate Sunday service.

**Steps:**
1. Create recurring event with `eventType: "YOUTH"`
2. Generate instances
3. Auto-generated attendance sessions will be created
4. When creating attendance session, optionally link to fellowship:
   ```json
   {
     "fellowshipId": 5,
     "eventId": 123
   }
   ```

### Use Case 4: Midweek Bible Study

**Scenario:** Every Wednesday evening Bible study.

**Steps:**
1. Create recurring event:
   ```json
   {
     "name": "Wednesday Bible Study",
     "eventType": "SERVICE",
     "recurrencePattern": "WEEKLY",
     "startDate": "2025-01-08T18:30:00"
   }
   ```
2. Generate instances
3. Attendance tracking is automatically enabled

---

## Frequently Asked Questions (FAQ)

### Question 1: Do manual attendance and event attendance combine in analytics?

**Answer: YES** - All attendance sessions (whether manually created or auto-generated from events) contribute to the same analytics.

**How it works:**
- Manual attendance sessions and event-linked attendance sessions are stored in the **same `AttendanceSession` table**
- The only difference is the `event_id` field (NULL for manual, populated for event-linked)
- All analytics queries aggregate across ALL attendance sessions regardless of source

**Example:**
```sql
-- This query counts ALL attendance records for analytics
SELECT COUNT(a) FROM Attendance a
JOIN a.attendanceSession s
WHERE s.church.id = :churchId
AND s.sessionDate BETWEEN :startDate AND :endDate
-- No filter on event_id - includes both manual and event-linked sessions
```

**Analytics that combine both:**
- Total attendance count
- Attendance rate percentage
- Member engagement scores
- Check-in method distribution
- Late arrival statistics
- Unique members attended count

**When you view analytics (GET /api/attendance/analytics), you get:**
- Combined data from ALL sessions (manual + event-linked)
- No separation unless you specifically filter by event

**Example Scenario:**
- Church has 4 Sunday services in January (event-linked attendance)
- Church also has 2 manual attendance sessions for special prayer meetings
- Analytics for January will show: **6 total sessions**, combined attendance from all

---

### Question 2: How does the system handle multiple services on the same day?

**Answer:** The system supports multiple services on the same day through **separate attendance sessions** with **unique member check-ins per session**.

#### Design Solution: One Attendance Record Per Session

**Database Design:**
```java
// A member can have MULTIPLE attendance records for the SAME day
// as long as they are for DIFFERENT sessions
findByMemberIdAndAttendanceSessionId(Long memberId, Long attendanceSessionId)
```

**This means:**
- Member can check into "9:00 AM Service" → PRESENT
- Same member can check into "11:30 AM Service" → PRESENT
- Both count as separate attendance records

#### Example: Church with Two Sunday Services

**Setup:**
1. **Create First Service Event:**
```json
{
  "name": "Sunday First Service",
  "eventType": "SERVICE",
  "startDate": "2025-01-05T09:00:00",
  "endDate": "2025-01-05T10:30:00",
  "recurrencePattern": "WEEKLY"
}
```

2. **Create Second Service Event:**
```json
{
  "name": "Sunday Second Service",
  "eventType": "SERVICE",
  "startDate": "2025-01-05T11:30:00",
  "endDate": "2025-01-05T13:00:00",
  "recurrencePattern": "WEEKLY"
}
```

3. **Generate instances for both** → Creates 2 separate attendance sessions per Sunday

**Result for January 5, 2025:**
- AttendanceSession #1: "Sunday First Service" (9:00 AM)
- AttendanceSession #2: "Sunday Second Service" (11:30 AM)

#### Member Check-In Flow

**Scenario:** John attends both services on Sunday

1. **9:00 AM - Check into First Service:**
```json
POST /api/attendance/mark
{
  "memberId": 123,
  "attendanceSessionId": 1,  // First Service session
  "status": "PRESENT"
}
```

2. **11:30 AM - Check into Second Service:**
```json
POST /api/attendance/mark
{
  "memberId": 123,
  "attendanceSessionId": 2,  // Second Service session
  "status": "PRESENT"
}
```

**Database Result:**
```
Attendance Table:
| id | member_id | attendance_session_id | status  | check_in_time    |
|----|-----------|----------------------|---------|------------------|
| 1  | 123       | 1                    | PRESENT | 2025-01-05 09:05 |
| 2  | 123       | 2                    | PRESENT | 2025-01-05 11:35 |
```

#### Analytics with Multiple Services

**For Attendance Reports:**

**Option 1: View Each Service Separately**
```
GET /api/attendance/sessions/1
→ Shows John as PRESENT in First Service

GET /api/attendance/sessions/2
→ Shows John as PRESENT in Second Service
```

**Option 2: Combined Daily View**
```
GET /api/attendance/analytics?date=2025-01-05
→ Shows:
  - Total Sessions: 2
  - John attended: 2 sessions (100% for the day)
  - Mary attended: 1 session (50% for the day)
```

**Option 3: Monthly Aggregate**
```
GET /api/attendance/analytics?startDate=2025-01-01&endDate=2025-01-31
→ Shows:
  - Total Sessions: 8 (4 Sundays × 2 services)
  - John attended: 8/8 sessions (100% attendance rate)
  - Mary attended: 4/8 sessions (50% attendance rate - only attended first service)
```

#### Handling "Absent" Status for Multiple Services

**Scenario:** Mary only attends the first service

**What happens:**
1. Mary checks into First Service → PRESENT
2. Mary does NOT check into Second Service → System marks ABSENT (or leaves unmarked)

**Final Attendance Report for January 5:**
```
Mary:
- First Service (9:00 AM): PRESENT ✓
- Second Service (11:30 AM): ABSENT ✗
- Overall for day: 50% attendance (1 of 2 services)
```

**System Behavior:**
- The system does NOT automatically mark members absent in the second service if they attended the first
- Church admins must manually mark attendance for EACH session
- This prevents false "absent" marks when members intentionally only attend one service

#### Best Practices for Multiple Services

**1. Clear Service Names:**
- Use "Sunday First Service (9 AM)" and "Sunday Second Service (11:30 AM)"
- Helps members know which QR code to scan

**2. Separate QR Codes:**
- Generate different QR codes for each service
- Prevents accidental check-in to wrong service

**3. Session Time Windows:**
```json
{
  "checkInStartTime": "08:00:00",  // 1 hour before first service
  "checkInEndTime": "10:30:00"     // End of first service
}
```
```json
{
  "checkInStartTime": "10:30:00",  // After first service ends
  "checkInEndTime": "13:00:00"     // End of second service
}
```

**4. Attendance Marking Strategy:**
- **Option A:** Mark attendance separately for each service (recommended)
- **Option B:** Mark members as PRESENT if they attend ANY service on that day
- **Option C:** Require attendance at MAIN service (11:30 AM) for "present" status

**5. Analytics Configuration:**
- Decide whether analytics should show:
  - Per-service attendance (more granular)
  - Daily attendance (attended at least one service)
  - Combined totals (total session attendance)

#### API Query for Same-Day Sessions

**Get all sessions for a specific date:**
```
GET /api/attendance/sessions?startDate=2025-01-05&endDate=2025-01-05
```

**Response:**
```json
[
  {
    "id": 1,
    "sessionName": "Sunday First Service",
    "sessionDate": "2025-01-05",
    "sessionTime": "09:00:00",
    "presentCount": 120,
    "totalMembers": 150
  },
  {
    "id": 2,
    "sessionName": "Sunday Second Service",
    "sessionDate": "2025-01-05",
    "sessionTime": "11:30:00",
    "presentCount": 95,
    "totalMembers": 150
  }
]
```

**Combined analytics across both:**
```
Total unique members who attended ANY service: 135
Total attendance records: 215 (some members attended both)
Overall engagement rate: 90% (135 of 150 members attended at least one service)
```

---

## Troubleshooting

### Problem: Attendance Sessions Not Auto-Generated

**Symptoms:** Created recurring event but no attendance sessions were created.

**Solutions:**
1. Check event type - only `SERVICE`, `PRAYER`, and `YOUTH` types auto-generate
2. Verify you called `POST /api/events/{id}/generate-instances`
3. Check server logs for errors during generation
4. Manual workaround: Create attendance sessions manually for each instance

### Problem: Duplicate Attendance Sessions

**Symptoms:** Multiple attendance sessions for the same event.

**Solutions:**
1. System prevents duplicates - check `existsByEvent_Id()` before creating
2. If duplicates exist, delete extras via `DELETE /api/attendance/sessions/{id}`
3. Check if sessions were created both automatically and manually

### Problem: Can't Link Event to Existing Attendance Session

**Symptoms:** Already have attendance session, want to link to event.

**Solution:**
Use update endpoint to add event link:
```
PUT /api/attendance/sessions/{sessionId}
{
  "sessionName": "Sunday Service",
  "sessionDate": "2025-01-05",
  "eventId": 123,  // Add this
  "churchId": 1
}
```

### Problem: Event Deleted but Attendance Session Remains

**Behavior:** This is by design - attendance sessions use `ON DELETE SET NULL`

**Explanation:** When an event is deleted, the attendance session's `event_id` is set to NULL, but the session and its attendance records are **preserved** for historical tracking.

**To view orphaned sessions:**
```sql
SELECT * FROM attendance_session WHERE event_id IS NULL
```

---

## Best Practices

### 1. Use Recurring Events for Regular Services

Don't create individual events for each Sunday - use one recurring event and generate instances.

### 2. Generate Instances in Batches

Generate 3-6 months of instances at a time, not a whole year, to allow for schedule changes.

### 3. Complete Sessions After Service

Mark session as complete after service ends:
```
PUT /api/attendance/sessions/{id}/complete
```

### 4. Leverage Auto-Generation

For service-type events, let the system auto-generate attendance sessions. Only create manually for special cases.

### 5. Use Event Notes for Context

Add context in event notes that will be copied to attendance sessions:
```json
{
  "notes": "Easter Sunday Service - Expect high attendance"
}
```

---

## Frontend Integration Guide

### Display Event with Attendance Link

```typescript
// Fetch event and its attendance sessions
const event = await eventService.getEvent(eventId);
const attendanceSessions = await eventService.getAttendanceSessionsForEvent(eventId);

// Display in UI
<div>
  <h2>{event.name}</h2>
  <p>Event Date: {event.startDate}</p>

  {attendanceSessions.length > 0 && (
    <div>
      <h3>Attendance Tracking</h3>
      <ul>
        {attendanceSessions.map(session => (
          <li key={session.id}>
            {session.sessionDate}: {session.presentCount}/{session.totalMembers} present
            <button onclick={() => viewAttendance(session.id)}>View Details</button>
          </li>
        ))}
      </ul>
    </div>
  )}
</div>
```

### Create Recurring Service with Attendance

```typescript
async function setupSundayService() {
  // Step 1: Create recurring event
  const event = await eventService.createEvent({
    name: 'Sunday Main Service',
    eventType: 'SERVICE',
    startDate: '2025-01-05T10:00:00',
    endDate: '2025-01-05T12:00:00',
    isRecurring: true,
    recurrencePattern: 'WEEKLY',
    recurrenceEndDate: '2025-12-31',
    locationType: 'PHYSICAL',
    physicalLocation: 'Main Sanctuary',
    requiresRegistration: false,
    visibility: 'PUBLIC'
  });

  // Step 2: Generate instances (attendance sessions auto-created)
  const result = await eventService.generateRecurringInstances(event.id, 52);

  console.log(`Created ${result.count} events with automatic attendance tracking`);

  return event;
}
```

---

## Database Schema Reference

### attendance_session Table

```sql
CREATE TABLE attendance_session (
  id BIGINT PRIMARY KEY,
  church_id BIGINT NOT NULL,
  event_id BIGINT,  -- NEW: Links to events table
  session_name VARCHAR(255) NOT NULL,
  session_date DATE NOT NULL,
  session_time TIME,
  fellowship_id BIGINT,
  is_completed BOOLEAN DEFAULT FALSE,
  notes TEXT,
  service_type VARCHAR(50),
  check_in_opens_at TIMESTAMP,
  check_in_closes_at TIMESTAMP,
  max_capacity INTEGER,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,

  FOREIGN KEY (church_id) REFERENCES church(id),
  FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE SET NULL,
  FOREIGN KEY (fellowship_id) REFERENCES fellowship(id),

  INDEX idx_attendance_session_event (event_id)
);
```

### Key Fields

- **event_id**: Optional link to Events module. NULL if standalone attendance session.
- **service_type**: Mapped from event type (SERVICE → SUNDAY_MAIN_SERVICE, etc.)
- **check_in_opens_at/closes_at**: Auto-set from event start/end times

---

## Migration History

**V86_add_event_id_to_attendance_session.sql**
- Added `event_id BIGINT` column
- Added foreign key constraint to `events` table
- Added index for performance
- Uses `ON DELETE SET NULL` to preserve attendance history

---

## Support and Further Reading

- **Events Module Documentation**: See `EventController.java` for all event endpoints
- **Attendance Module Documentation**: See `AttendanceController.java` for attendance endpoints
- **Recurring Events Guide**: See `RecurringEventService.java` for recurrence pattern details
- **Service Types Reference**: See `ServiceType.java` enum for all available service types

---

**Last Updated:** 2025-12-31
**Version:** 1.0
**Author:** Claude Sonnet 4.5
