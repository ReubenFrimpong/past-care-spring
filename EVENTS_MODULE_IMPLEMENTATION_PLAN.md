# Events Module - Step-by-Step Implementation Plan

**Module**: Events Management System
**Priority**: ⭐⭐ Medium Priority
**Estimated Duration**: 4-6 weeks (3 phases)
**Status**: Not Started

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Phase Breakdown](#phase-breakdown)
3. [Database Schema](#database-schema)
4. [Implementation Steps](#implementation-steps)
5. [Context Boundaries](#context-boundaries)
6. [Consistency Checklist](#consistency-checklist)

---

## Overview

### Module Purpose
Enable churches to create, manage, and track events (services, conferences, outreach, social gatherings) with registration, attendance tracking, and analytics.

### Key Features
- Event creation and management (CRUD)
- Event types and categories
- Recurring events (weekly, monthly, yearly)
- Registration and capacity management
- QR code check-in integration
- Event images/flyers
- Visibility control (public, members-only, leadership-only)
- Event attendance tracking
- Event analytics and reporting
- Email/SMS notifications (integration with Communications Module)

### Dependencies
- ✅ Members Module (complete)
- ✅ Attendance Module (complete)
- ✅ Communications Module (SMS complete, Email deferred)

---

## Phase Breakdown

### Phase 1: Core Event Management (2 weeks)
**Goal**: Create, read, update, delete events with basic properties

**Features**:
- Event entity with all fields
- Event types (service, conference, outreach, social, training, other)
- CRUD operations
- Event listing and filtering
- Event search
- Basic validation

**Deliverables**:
- Backend: Entity, Repository, Service, Controller, DTOs
- Database: Migration files
- Frontend: Events page, create/edit dialog, list view
- Tests: Unit tests for service layer

---

### Phase 2: Registration & Attendance (2 weeks)
**Goal**: Handle event registration and attendance tracking

**Features**:
- Event registration (members and visitors)
- Capacity management and waitlist
- Registration approval workflow
- QR code generation for events
- Check-in integration with Attendance Module
- Registration status tracking
- Attendance statistics per event

**Deliverables**:
- Backend: Registration entity, service, controller
- Frontend: Registration dialog, check-in interface
- Integration: Link with Attendance Module
- Tests: Registration flow tests

---

### Phase 3: Advanced Features (1-2 weeks)
**Goal**: Recurring events, notifications, and analytics

**Features**:
- Recurring event patterns
- Event templates
- Automated notifications (reminders, confirmations)
- Event analytics dashboard
- Event calendar view
- Event export (PDF, Excel)
- Post-event feedback

**Deliverables**:
- Backend: Recurrence logic, notification scheduler, analytics
- Frontend: Calendar view, analytics widgets
- Integration: Communications Module
- Tests: E2E event workflow tests

---

## Database Schema

### Core Tables

#### 1. `events` Table
```sql
CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL,

    -- Basic Info
    name VARCHAR(200) NOT NULL,
    description TEXT,
    event_type VARCHAR(50) NOT NULL,  -- ENUM: SERVICE, CONFERENCE, OUTREACH, SOCIAL, TRAINING, OTHER
    category VARCHAR(100),

    -- Date & Time
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    timezone VARCHAR(50) DEFAULT 'UTC',
    is_all_day BOOLEAN DEFAULT FALSE,

    -- Location
    location_type VARCHAR(50) NOT NULL,  -- ENUM: PHYSICAL, VIRTUAL, HYBRID
    location_id BIGINT,  -- FK to locations table
    venue_name VARCHAR(200),
    venue_address TEXT,
    virtual_meeting_url VARCHAR(500),
    virtual_meeting_password VARCHAR(100),

    -- Registration
    requires_registration BOOLEAN DEFAULT FALSE,
    registration_opens_at DATETIME,
    registration_closes_at DATETIME,
    max_capacity INT,
    current_registrations INT DEFAULT 0,
    allow_waitlist BOOLEAN DEFAULT FALSE,
    waitlist_count INT DEFAULT 0,
    requires_approval BOOLEAN DEFAULT FALSE,

    -- Visibility & Access
    visibility VARCHAR(50) NOT NULL DEFAULT 'PUBLIC',  -- ENUM: PUBLIC, MEMBERS_ONLY, LEADERSHIP_ONLY, PRIVATE
    is_featured BOOLEAN DEFAULT FALSE,
    is_published BOOLEAN DEFAULT FALSE,

    -- Media
    banner_image_url VARCHAR(500),
    thumbnail_image_url VARCHAR(500),

    -- Recurrence
    is_recurring BOOLEAN DEFAULT FALSE,
    recurrence_pattern VARCHAR(50),  -- ENUM: DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM
    recurrence_interval INT DEFAULT 1,
    recurrence_end_date DATETIME,
    parent_event_id BIGINT,  -- For recurring event instances

    -- Metadata
    created_by_id BIGINT NOT NULL,
    updated_by_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    FOREIGN KEY (church_id) REFERENCES churches(id),
    FOREIGN KEY (location_id) REFERENCES locations(id),
    FOREIGN KEY (created_by_id) REFERENCES users(id),
    FOREIGN KEY (updated_by_id) REFERENCES users(id),
    FOREIGN KEY (parent_event_id) REFERENCES events(id),

    INDEX idx_church_dates (church_id, start_date, end_date),
    INDEX idx_event_type (event_type),
    INDEX idx_visibility (visibility, is_published),
    INDEX idx_recurrence (is_recurring, parent_event_id)
);
```

#### 2. `event_registrations` Table
```sql
CREATE TABLE event_registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    church_id BIGINT NOT NULL,

    -- Registrant Info
    member_id BIGINT,  -- Nullable for visitor registrations
    visitor_name VARCHAR(200),
    visitor_email VARCHAR(200),
    visitor_phone VARCHAR(50),

    -- Registration Details
    registration_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',  -- ENUM: PENDING, APPROVED, REJECTED, CANCELLED, WAITLISTED
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approval_date TIMESTAMP NULL,
    approved_by_id BIGINT,

    -- Attendance
    checked_in BOOLEAN DEFAULT FALSE,
    check_in_time TIMESTAMP NULL,
    checked_in_by_id BIGINT,
    attendance_record_id BIGINT,  -- FK to attendance table

    -- Additional Info
    number_of_guests INT DEFAULT 0,
    special_requirements TEXT,
    notes TEXT,

    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (event_id) REFERENCES events(id),
    FOREIGN KEY (church_id) REFERENCES churches(id),
    FOREIGN KEY (member_id) REFERENCES members(id),
    FOREIGN KEY (approved_by_id) REFERENCES users(id),
    FOREIGN KEY (checked_in_by_id) REFERENCES users(id),
    FOREIGN KEY (attendance_record_id) REFERENCES attendance(id),

    INDEX idx_event (event_id),
    INDEX idx_member (member_id),
    INDEX idx_status (registration_status),
    INDEX idx_check_in (event_id, checked_in),

    UNIQUE KEY unique_member_event (event_id, member_id)
);
```

#### 3. `event_organizers` Table
```sql
CREATE TABLE event_organizers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    church_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) DEFAULT 'ORGANIZER',  -- ENUM: ORGANIZER, COORDINATOR, VOLUNTEER

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (church_id) REFERENCES churches(id),
    FOREIGN KEY (user_id) REFERENCES users(id),

    UNIQUE KEY unique_event_user (event_id, user_id)
);
```

#### 4. `event_tags` Table (optional, for flexible categorization)
```sql
CREATE TABLE event_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    church_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,

    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (church_id) REFERENCES churches(id),

    INDEX idx_event (event_id),
    INDEX idx_tag (tag)
);
```

---

## Implementation Steps

### **CONTEXT 1: Database Setup**

#### Step 1.1: Create Migration Files (V41-V44)

**File**: `V41__create_events_table.sql`
```sql
-- Copy the events table schema from above
-- Add all necessary indexes
-- Include comments for clarity
```

**File**: `V42__create_event_registrations_table.sql`
```sql
-- Copy the event_registrations table schema
-- Add foreign key constraints
-- Add indexes for performance
```

**File**: `V43__create_event_organizers_table.sql`
```sql
-- Copy the event_organizers table schema
```

**File**: `V44__create_event_tags_table.sql`
```sql
-- Copy the event_tags table schema
```

**Verification**:
```bash
# Run migrations
./mvnw flyway:migrate

# Verify tables created
./mvnw flyway:info
```

---

### **CONTEXT 2: Backend Enums**

#### Step 2.1: Create Event Enums

**File**: `src/main/java/com/reuben/pastcare_spring/models/EventType.java`
```java
package com.reuben.pastcare_spring.models;

public enum EventType {
    SERVICE,        // Sunday service, midweek service
    CONFERENCE,     // Conferences, seminars
    OUTREACH,       // Evangelism, community outreach
    SOCIAL,         // Fellowship, socials
    TRAINING,       // Training sessions, workshops
    MEETING,        // Committee meetings, board meetings
    OTHER           // Other events
}
```

**File**: `src/main/java/com/reuben/pastcare_spring/models/EventLocationType.java`
```java
package com.reuben.pastcare_spring.models;

public enum EventLocationType {
    PHYSICAL,   // In-person at physical location
    VIRTUAL,    // Online only
    HYBRID      // Both physical and virtual
}
```

**File**: `src/main/java/com/reuben/pastcare_spring/models/EventVisibility.java`
```java
package com.reuben.pastcare_spring.models;

public enum EventVisibility {
    PUBLIC,             // Visible to everyone including non-members
    MEMBERS_ONLY,       // Only visible to church members
    LEADERSHIP_ONLY,    // Only visible to leadership
    PRIVATE             // Only visible to organizers
}
```

**File**: `src/main/java/com/reuben/pastcare_spring/models/RegistrationStatus.java`
```java
package com.reuben.pastcare_spring.models;

public enum RegistrationStatus {
    PENDING,        // Awaiting approval
    APPROVED,       // Approved and confirmed
    REJECTED,       // Rejected by organizer
    CANCELLED,      // Cancelled by registrant
    WAITLISTED      // On waitlist
}
```

**File**: `src/main/java/com/reuben/pastcare_spring/models/RecurrencePattern.java`
```java
package com.reuben.pastcare_spring.models;

public enum RecurrencePattern {
    NONE,       // Not recurring
    DAILY,      // Every day
    WEEKLY,     // Every week
    MONTHLY,    // Every month
    YEARLY,     // Every year
    CUSTOM      // Custom pattern
}
```

**Verification**:
```bash
./mvnw compile
```

---

### **CONTEXT 3: Backend Entities**

#### Step 3.1: Create Event Entity

**File**: `src/main/java/com/reuben/pastcare_spring/models/Event.java`

**Template Structure**:
```java
@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event extends TenantBaseEntity {

    // Basic Info
    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Column(length = 100)
    private String category;

    // Date & Time (continue with all fields...)
    // Location (continue with all fields...)
    // Registration (continue with all fields...)
    // Visibility (continue with all fields...)
    // Media (continue with all fields...)
    // Recurrence (continue with all fields...)

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_event_id")
    private Event parentEvent;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<EventRegistration> registrations = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<EventOrganizer> organizers = new ArrayList<>();

    // Business logic methods
    public boolean isRegistrationOpen() { /* ... */ }
    public boolean hasCapacity() { /* ... */ }
    public boolean canRegister(Member member) { /* ... */ }
    public int getAvailableSlots() { /* ... */ }
}
```

#### Step 3.2: Create EventRegistration Entity

**File**: `src/main/java/com/reuben/pastcare_spring/models/EventRegistration.java`

#### Step 3.3: Create EventOrganizer Entity

**File**: `src/main/java/com/reuben/pastcare_spring/models/EventOrganizer.java`

**Verification**:
```bash
./mvnw compile
```

---

### **CONTEXT 4: Backend Repositories**

#### Step 4.1: Create EventRepository

**File**: `src/main/java/com/reuben/pastcare_spring/repositories/EventRepository.java`

```java
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Find by church
    Page<Event> findByChurchId(Long churchId, Pageable pageable);

    // Find by date range
    @Query("SELECT e FROM Event e WHERE e.church.id = :churchId " +
           "AND e.startDate >= :startDate AND e.endDate <= :endDate " +
           "AND e.deletedAt IS NULL")
    List<Event> findByChurchIdAndDateRange(
        @Param("churchId") Long churchId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // Find upcoming events
    @Query("SELECT e FROM Event e WHERE e.church.id = :churchId " +
           "AND e.startDate > :now AND e.isPublished = true " +
           "AND e.deletedAt IS NULL ORDER BY e.startDate ASC")
    List<Event> findUpcomingEvents(
        @Param("churchId") Long churchId,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

    // Find by type
    List<Event> findByChurchIdAndEventType(Long churchId, EventType eventType);

    // Find by visibility
    List<Event> findByChurchIdAndVisibility(Long churchId, EventVisibility visibility);

    // Search by name
    @Query("SELECT e FROM Event e WHERE e.church.id = :churchId " +
           "AND LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "AND e.deletedAt IS NULL")
    List<Event> searchByName(
        @Param("churchId") Long churchId,
        @Param("query") String query
    );

    // Count by status
    long countByChurchIdAndIsPublished(Long churchId, boolean isPublished);
}
```

#### Step 4.2: Create EventRegistrationRepository

**File**: `src/main/java/com/reuben/pastcare_spring/repositories/EventRegistrationRepository.java`

```java
@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    // Find by event
    List<EventRegistration> findByEventId(Long eventId);

    // Find by member
    List<EventRegistration> findByMemberId(Long memberId);

    // Count registrations
    long countByEventIdAndRegistrationStatus(Long eventId, RegistrationStatus status);

    // Check if member registered
    boolean existsByEventIdAndMemberId(Long eventId, Long memberId);

    // Find pending approvals
    @Query("SELECT r FROM EventRegistration r WHERE r.event.id = :eventId " +
           "AND r.registrationStatus = 'PENDING'")
    List<EventRegistration> findPendingRegistrations(@Param("eventId") Long eventId);
}
```

---

### **CONTEXT 5: Backend DTOs**

#### Step 5.1: Create Request DTOs

**File**: `src/main/java/com/reuben/pastcare_spring/dtos/EventRequest.java`
**File**: `src/main/java/com/reuben/pastcare_spring/dtos/EventRegistrationRequest.java`

#### Step 5.2: Create Response DTOs

**File**: `src/main/java/com/reuben/pastcare_spring/dtos/EventResponse.java`
**File**: `src/main/java/com/reuben/pastcare_spring/dtos/EventRegistrationResponse.java`
**File**: `src/main/java/com/reuben/pastcare_spring/dtos/EventStatsResponse.java`

---

### **CONTEXT 6: Backend Service Layer**

#### Step 6.1: Create EventService

**File**: `src/main/java/com/reuben/pastcare_spring/services/EventService.java`

**Key Methods**:
```java
@Service
@Transactional
public class EventService {

    // CRUD Operations
    public Event createEvent(EventRequest request, User creator);
    public Event updateEvent(Long id, EventRequest request, User updater);
    public void deleteEvent(Long id);
    public Event getEventById(Long id);
    public Page<Event> getAllEvents(Long churchId, Pageable pageable);

    // Search & Filter
    public List<Event> searchEvents(Long churchId, String query);
    public List<Event> getUpcomingEvents(Long churchId, int limit);
    public List<Event> getEventsByDateRange(Long churchId, LocalDateTime start, LocalDateTime end);
    public List<Event> getEventsByType(Long churchId, EventType type);

    // Registration
    public boolean canRegister(Long eventId, Long memberId);
    public int getAvailableSlots(Long eventId);

    // Recurrence
    public List<Event> generateRecurringInstances(Event parentEvent, LocalDateTime until);

    // Statistics
    public EventStatsResponse getEventStats(Long churchId);
}
```

#### Step 6.2: Create EventRegistrationService

**File**: `src/main/java/com/reuben/pastcare_spring/services/EventRegistrationService.java`

**Key Methods**:
```java
@Service
@Transactional
public class EventRegistrationService {

    public EventRegistration registerForEvent(EventRegistrationRequest request);
    public void cancelRegistration(Long registrationId);
    public void approveRegistration(Long registrationId, User approver);
    public void rejectRegistration(Long registrationId, User approver);
    public void checkIn(Long registrationId, User checkedInBy);

    public List<EventRegistration> getEventRegistrations(Long eventId);
    public List<EventRegistration> getMemberRegistrations(Long memberId);
    public EventRegistration getRegistrationById(Long id);
}
```

---

### **CONTEXT 7: Backend Controllers**

#### Step 7.1: Create EventController

**File**: `src/main/java/com/reuben/pastcare_spring/controllers/EventController.java`

**Endpoints**:
```java
@RestController
@RequestMapping("/api/events")
public class EventController {

    // CRUD
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventRequest request);

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long id);

    @GetMapping
    public ResponseEntity<Page<EventResponse>> getAllEvents(Pageable pageable);

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long id, @RequestBody EventRequest request);

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id);

    // Search & Filter
    @GetMapping("/search")
    public ResponseEntity<List<EventResponse>> searchEvents(@RequestParam String query);

    @GetMapping("/upcoming")
    public ResponseEntity<List<EventResponse>> getUpcomingEvents(@RequestParam(defaultValue = "10") int limit);

    @GetMapping("/by-type")
    public ResponseEntity<List<EventResponse>> getEventsByType(@RequestParam EventType type);

    // Statistics
    @GetMapping("/stats")
    public ResponseEntity<EventStatsResponse> getStats();
}
```

#### Step 7.2: Create EventRegistrationController

**File**: `src/main/java/com/reuben/pastcare_spring/controllers/EventRegistrationController.java`

---

### **CONTEXT 8: Frontend Models**

**File**: `src/app/models/event.model.ts`

```typescript
export interface Event {
  id: number;
  name: string;
  description?: string;
  eventType: EventType;
  category?: string;
  startDate: string;
  endDate: string;
  locationType: LocationType;
  locationName?: string;
  virtualMeetingUrl?: string;
  requiresRegistration: boolean;
  maxCapacity?: number;
  currentRegistrations: number;
  visibility: EventVisibility;
  isPublished: boolean;
  bannerImageUrl?: string;
  isRecurring: boolean;
  createdAt: string;
}

export enum EventType {
  SERVICE = 'SERVICE',
  CONFERENCE = 'CONFERENCE',
  OUTREACH = 'OUTREACH',
  SOCIAL = 'SOCIAL',
  TRAINING = 'TRAINING',
  OTHER = 'OTHER'
}

// ... continue with other interfaces
```

---

### **CONTEXT 9: Frontend Services**

**File**: `src/app/services/event.service.ts`

```typescript
@Injectable({
  providedIn: 'root'
})
export class EventService {
  private apiUrl = `${environment.apiUrl}/events`;

  constructor(private http: HttpClient) {}

  createEvent(request: EventRequest): Observable<Event> {
    return this.http.post<Event>(this.apiUrl, request);
  }

  getEvents(page: number = 0, size: number = 10): Observable<Page<Event>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<Page<Event>>(this.apiUrl, { params });
  }

  // ... continue with other methods
}
```

---

### **CONTEXT 10: Frontend Components**

#### Components to Create:
1. **EventsPageComponent** - Main events page
2. **EventListComponent** - Display events grid/list
3. **EventCardComponent** - Individual event card
4. **EventDetailComponent** - Event details view
5. **EventFormComponent** - Create/edit event form
6. **EventCalendarComponent** - Calendar view
7. **EventRegistrationComponent** - Registration form
8. **EventCheckInComponent** - Check-in interface

---

## Context Boundaries

### Context 1: Database Layer
- **Scope**: Migrations only
- **Files**: `V41__*.sql` to `V44__*.sql`
- **Dependencies**: None
- **Verification**: `./mvnw flyway:migrate`

### Context 2-3: Backend Models
- **Scope**: Enums + Entities
- **Files**: All enum and entity classes
- **Dependencies**: Context 1 (database)
- **Verification**: `./mvnw compile`

### Context 4-5: Backend Data Layer
- **Scope**: Repositories + DTOs
- **Files**: Repository interfaces and DTO classes
- **Dependencies**: Context 2-3
- **Verification**: `./mvnw compile`

### Context 6-7: Backend Service/Controller
- **Scope**: Business logic + REST API
- **Files**: Service and Controller classes
- **Dependencies**: Context 4-5
- **Verification**: `./mvnw test` + Postman/curl

### Context 8-9: Frontend Data Layer
- **Scope**: Models + Services
- **Files**: TypeScript interfaces and services
- **Dependencies**: None (can run parallel to backend)
- **Verification**: `npm run build`

### Context 10: Frontend UI Layer
- **Scope**: Components + Templates
- **Files**: Angular components
- **Dependencies**: Context 8-9
- **Verification**: `ng serve`

---

## Consistency Checklist

Use this checklist when implementing across multiple contexts:

### ✅ Before Starting Any Context

- [ ] Read this entire plan document
- [ ] Verify all dependencies are complete
- [ ] Have example code from similar modules (Members, Attendance)
- [ ] Understand the naming conventions

### ✅ Naming Conventions

**Database**:
- Tables: `snake_case` (e.g., `events`, `event_registrations`)
- Columns: `snake_case` (e.g., `event_type`, `created_at`)
- Indexes: `idx_table_column` (e.g., `idx_events_church_id`)

**Backend Java**:
- Classes: `PascalCase` (e.g., `Event`, `EventService`)
- Methods: `camelCase` (e.g., `createEvent`, `getUpcomingEvents`)
- Enums: `UPPER_CASE` (e.g., `EVENT_TYPE.SERVICE`)
- Packages: `lowercase` (e.g., `models`, `services`)

**Frontend TypeScript**:
- Interfaces: `PascalCase` (e.g., `Event`, `EventRequest`)
- Properties: `camelCase` (e.g., `eventType`, `startDate`)
- Enums: `PascalCase` values `UPPER_CASE` (e.g., `EventType.SERVICE`)
- Components: `kebab-case` files, `PascalCase` classes

### ✅ Field Naming Consistency

**Event Type**:
- DB: `event_type` (VARCHAR)
- Java: `eventType` (EventType enum)
- TS: `eventType` (EventType enum)

**Start Date**:
- DB: `start_date` (DATETIME)
- Java: `startDate` (LocalDateTime)
- TS: `startDate` (string - ISO 8601)

**Church Reference**:
- DB: `church_id` (BIGINT FK)
- Java: `church` (Church object via `@ManyToOne`)
- TS: `churchId` (number in requests), omitted in responses (from context)

### ✅ After Completing Each Context

- [ ] Code compiles without errors
- [ ] All imports resolved
- [ ] No unused variables or methods
- [ ] Consistent with existing modules (Members, Attendance)
- [ ] Documentation added (JavaDoc, TSDoc)
- [ ] Tests pass (if applicable)
- [ ] Git commit with clear message

### ✅ Cross-Context Validation

**Backend-Frontend Alignment**:
- [ ] DTOs match TypeScript interfaces
- [ ] Enum values identical
- [ ] Date formats consistent (ISO 8601)
- [ ] API endpoint paths match service URLs
- [ ] HTTP methods match (GET, POST, PUT, DELETE)

**Database-Backend Alignment**:
- [ ] Table names match `@Table` annotations
- [ ] Column names match `@Column` annotations
- [ ] Foreign keys match `@ManyToOne`/`@OneToMany`
- [ ] Enum values match database constraints

---

## Quick Reference

### File Locations

**Backend**:
```
src/main/java/com/reuben/pastcare_spring/
├── models/
│   ├── Event.java
│   ├── EventRegistration.java
│   ├── EventOrganizer.java
│   ├── EventType.java (enum)
│   ├── EventVisibility.java (enum)
│   └── RegistrationStatus.java (enum)
├── repositories/
│   ├── EventRepository.java
│   └── EventRegistrationRepository.java
├── services/
│   ├── EventService.java
│   └── EventRegistrationService.java
├── controllers/
│   ├── EventController.java
│   └── EventRegistrationController.java
└── dtos/
    ├── EventRequest.java
    ├── EventResponse.java
    ├── EventRegistrationRequest.java
    └── EventRegistrationResponse.java

src/main/resources/db/migration/
├── V41__create_events_table.sql
├── V42__create_event_registrations_table.sql
├── V43__create_event_organizers_table.sql
└── V44__create_event_tags_table.sql
```

**Frontend**:
```
src/app/
├── models/
│   └── event.model.ts
├── services/
│   ├── event.service.ts
│   └── event-registration.service.ts
├── events-page/
│   ├── events-page.component.ts
│   ├── events-page.component.html
│   └── events-page.component.css
└── components/
    ├── event-card/
    ├── event-form/
    ├── event-calendar/
    └── event-registration/
```

---

## Next Steps

1. **Review this plan** with all stakeholders
2. **Start with Context 1** (Database migrations)
3. **Work sequentially** through contexts 2-7 for backend
4. **Parallelize contexts 8-10** for frontend (can start while backend is in progress)
5. **Integration testing** after both backend and frontend complete
6. **E2E testing** with Playwright
7. **Documentation** and deployment

---

**Document Version**: 1.0
**Created**: December 27, 2025
**Status**: Ready for Implementation
**Estimated Completion**: 4-6 weeks
