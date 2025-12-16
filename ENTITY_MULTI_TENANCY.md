# Entity Multi-Tenancy Architecture

## Overview

All entities in the PastCare Spring application have been reviewed and configured for multi-tenant SaaS architecture. Each entity is categorized based on its relationship to the tenant (church).

## Entity Categories

### 🏛️ Tenant Root (1 entity)

**Church** - The tenant entity itself
- **File:** `models/Church.java`
- **Extends:** `BaseEntity` (NOT TenantBaseEntity)
- **Rationale:** This IS the tenant, so it doesn't belong to a tenant
- **Fields Added:**
  - `name` (unique) - Church name
  - `address` - Physical address
  - `phoneNumber` - Contact number
  - `email` - Contact email
  - `website` - Church website
  - `active` - Whether church is active
- **Security:** Each church is completely isolated from others

### 👥 Tenant-Scoped Entities (3 entities)

These entities extend `TenantBaseEntity` and are automatically filtered by `church_id`:

#### 1. Member
- **File:** `models/Member.java`
- **Extends:** `TenantBaseEntity`
- **Purpose:** Church members/congregants
- **Key Fields:** firstName, lastName, phoneNumber, dob, etc.
- **Relationship:** `@ManyToOne` to Church (inherited from TenantBaseEntity)
- **Security:** Queries automatically filtered by church_id

#### 2. Fellowship
- **File:** `models/Fellowship.java`
- **Extends:** `TenantBaseEntity`
- **Purpose:** Church fellowship groups (youth, men's, women's, etc.)
- **Key Fields:** name
- **Relationship:** `@ManyToOne` to Church (inherited from TenantBaseEntity)
- **Security:** Queries automatically filtered by church_id

#### 3. AttendanceSession
- **File:** `models/AttendanceSession.java`
- **Extends:** `TenantBaseEntity`
- **Purpose:** Attendance tracking sessions
- **Key Fields:** sessionName, sessionDate, sessionTime, fellowship, notes
- **Relationship:** `@ManyToOne` to Church (inherited from TenantBaseEntity)
- **Security:** Queries automatically filtered by church_id

### 🔗 Indirectly Tenant-Scoped (1 entity)

**Attendance**
- **File:** `models/Attendance.java`
- **Extends:** `BaseEntity` (NOT TenantBaseEntity)
- **Purpose:** Individual attendance records
- **Tenant Scope:** Inherited through relationships
  - `@ManyToOne` Member (has church_id)
  - `@ManyToOne` AttendanceSession (has church_id)
- **Rationale:** Doesn't need explicit church_id because it's scoped through Member and AttendanceSession
- **Security:** Cannot create attendance for members/sessions from other churches

### 👤 User Entity (Special Case)

**User**
- **File:** `models/User.java`
- **Extends:** `BaseEntity` (NOT TenantBaseEntity)
- **Purpose:** Application users (pastors, admins, staff)
- **Tenant Relationship:** `@ManyToOne` Church (REQUIRED, not nullable)
- **Key Fields:**
  - name, email, phoneNumber, password
  - church (REQUIRED - users must belong to a church)
  - role (ADMIN, USER, etc.)
  - failedLoginAttempts, accountLockedUntil, accountLocked
- **Rationale:**
  - Users belong to a church but aren't filtered by TenantBaseEntity
  - Authentication logic needs direct church access
  - JWT includes churchId from user's church
- **Security:**
  - Church is REQUIRED (nullable=false)
  - User cannot be created without a church
  - Login validates user has associated church

### 🔒 Security/Audit Entities (2 entities)

These entities are **intentionally cross-tenant** for security purposes:

#### 1. LoginAttempt
- **File:** `models/LoginAttempt.java`
- **Extends:** `BaseEntity` (NOT TenantBaseEntity)
- **Purpose:** Track all login attempts for brute force protection
- **Rationale:**
  - IP-based rate limiting works across all churches
  - Prevents attacker from bypassing limits by targeting different churches
  - Provides platform-wide security monitoring
- **Fields:** email, ipAddress, success, attemptTime, userAgent
- **Security:** Cross-tenant by design for security

#### 2. RefreshToken
- **File:** `models/RefreshToken.java`
- **Extends:** `BaseEntity` (has explicit church_id)
- **Purpose:** Manage refresh tokens for persistent sessions
- **Tenant Relationship:** Explicit `@ManyToOne` Church
- **Rationale:**
  - Needs church_id for session management per tenant
  - Not filtered by TenantBaseEntity (authentication flow needs direct access)
  - Enables "logout from all devices" per church
- **Fields:** token, user, church, expiryDate, revoked, ipAddress, userAgent
- **Security:** Scoped to church but not auto-filtered

## Multi-Tenancy Mechanisms

### 1. TenantBaseEntity (Automatic Filtering)

```java
@FilterDef(name = "churchFilter", parameters = @ParamDef(name = "churchId", type = Long.class))
@Filter(name = "churchFilter", condition = "church_id = :churchId")
public abstract class TenantBaseEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "church_id", nullable = false)
    @Column(updatable = false)
    private Church church;
}
```

**How it works:**
1. Hibernate filter adds `WHERE church_id = :churchId` to ALL queries
2. TenantContext stores current church_id in thread-local storage
3. Filter parameter is set from TenantContext automatically
4. Queries are scoped to current tenant without developer intervention

**Entities using this:**
- Member
- Fellowship
- AttendanceSession

### 2. Explicit Church Relationship (Manual Scoping)

Some entities have church_id but don't use automatic filtering:

**User:**
```java
@ManyToOne
@JoinColumn(name = "church_id", nullable = false)
private Church church;
```

**RefreshToken:**
```java
@ManyToOne
@JoinColumn(name = "church_id", nullable = false)
private Church church;
```

**Why:** Authentication and session management need direct church access without automatic filtering.

### 3. Relationship-Based Scoping (Inherited)

**Attendance:**
- No direct church_id
- Scoped through Member (has church_id)
- Scoped through AttendanceSession (has church_id)
- Cannot create cross-tenant attendance records

## Security Guarantees

### ✅ What IS Protected

1. **Member Data** - Completely isolated per church
   - Cannot query members from other churches
   - Cannot create members in other churches
   - Hibernate filter prevents cross-tenant access

2. **Fellowship Data** - Completely isolated per church
   - Cannot see fellowships from other churches
   - Cannot add members to other churches' fellowships

3. **Attendance Data** - Completely isolated per church
   - Cannot record attendance for other churches' members
   - Cannot access other churches' attendance sessions

4. **User Data** - Scoped to church
   - Users must belong to a church (required)
   - JWT includes user's church_id
   - Login validates church association

5. **Session Data** - Scoped to church
   - Refresh tokens tied to church
   - Cannot use tokens across churches

### ⚠️ What is NOT Protected (By Design)

1. **Login Attempts** - Cross-tenant security data
   - Tracks all attempts across platform
   - Necessary for IP-based rate limiting
   - Prevents security bypass by targeting different churches

2. **Church Data** - Root tenant data
   - Churches can see their own data only
   - No automatic filtering (they ARE the tenant)

## Database Schema

### Tenant-Scoped Tables (with church_id)

```sql
-- Automatically filtered by TenantBaseEntity
members (id, church_id, first_name, last_name, ...)
fellowships (id, church_id, name, ...)
attendance_sessions (id, church_id, session_name, session_date, ...)

-- Explicit church_id (not auto-filtered)
users (id, church_id, name, email, password, ...)
refresh_tokens (id, church_id, user_id, token, ...)

-- Indirect scoping (through relationships)
attendances (id, member_id, attendance_session_id, status, ...)
```

### Cross-Tenant Tables (no church_id)

```sql
-- Root tenant entity
churches (id, name, address, active, ...)

-- Security/audit tables
login_attempts (id, email, ip_address, success, attempt_time, ...)
```

## Usage Examples

### Creating Tenant-Scoped Entities

```java
// Member (auto-scoped)
Member member = new Member();
member.setFirstName("John");
member.setLastName("Doe");
member.setChurch(currentChurch); // Required
memberRepository.save(member);

// Fellowship (auto-scoped)
Fellowship fellowship = new Fellowship();
fellowship.setName("Youth Group");
fellowship.setChurch(currentChurch); // Required
fellowshipRepository.save(fellowship);

// Attendance (indirectly scoped)
Attendance attendance = new Attendance();
attendance.setMember(member); // Member has church_id
attendance.setAttendanceSession(session); // Session has church_id
attendanceRepository.save(attendance); // Automatically scoped
```

### Querying Tenant-Scoped Entities

```java
// TenantContext set from JWT
TenantContext.setCurrentChurchId(123L);

// All queries automatically filtered
List<Member> members = memberRepository.findAll();
// SQL: SELECT * FROM members WHERE church_id = 123

List<Fellowship> fellowships = fellowshipRepository.findAll();
// SQL: SELECT * FROM fellowships WHERE church_id = 123

// Even custom queries are filtered
List<Member> activeMem bers = memberRepository.findByIsVerified(true);
// SQL: SELECT * FROM members WHERE is_verified = true AND church_id = 123
```

### Cross-Tenant Queries (Where Needed)

```java
// Login attempts (cross-tenant by design)
long failedAttempts = loginAttemptRepository
    .countFailedAttemptsByEmailSince(email, since);
// SQL: SELECT COUNT(*) FROM login_attempts
//      WHERE email = ? AND success = false AND attempt_time > ?
// (No church_id filter - intentionally cross-tenant)

// Refresh tokens (explicit church check)
Optional<RefreshToken> token = refreshTokenRepository
    .findByTokenAndChurch(tokenValue, church);
// SQL: SELECT * FROM refresh_tokens
//      WHERE token = ? AND church_id = ?
```

## Validation & Constraints

### Database Constraints

```sql
-- Foreign key constraints
ALTER TABLE members ADD CONSTRAINT fk_member_church
    FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE;

ALTER TABLE fellowships ADD CONSTRAINT fk_fellowship_church
    FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE;

ALTER TABLE users ADD CONSTRAINT fk_user_church
    FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE RESTRICT;

-- Not null constraints
ALTER TABLE members MODIFY church_id BIGINT NOT NULL;
ALTER TABLE fellowships MODIFY church_id BIGINT NOT NULL;
ALTER TABLE users MODIFY church_id BIGINT NOT NULL;

-- Unique constraints
ALTER TABLE churches ADD UNIQUE (name);
ALTER TABLE users ADD UNIQUE (email);
```

### Application Validations

```java
// User registration - validate church exists
public User register(AuthRegistrationRequest request) {
    Church church = churchRepository.findById(request.churchId())
        .orElseThrow(() -> new ResourceNotFoundException("Church not found"));

    User user = new User();
    user.setChurch(church); // Required
    // ... other fields
    return userRepository.save(user);
}

// Login - validate user has church
public AuthResponse login(AuthLoginRequest request) {
    User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new InvalidCredentialsException());

    if (user.getChurch() == null) {
        throw new RuntimeException("User must be associated with a church");
    }

    // Generate JWT with church_id
    String token = jwtUtil.generateToken(userDetails, user.getId(),
        user.getChurch().getId(), user.getRole().name(), rememberMe);

    return new AuthResponse(token, user);
}
```

## Testing Multi-Tenancy

### Unit Tests

```java
@Test
public void testMemberIsolation() {
    // Setup two churches
    Church church1 = createChurch("Church A");
    Church church2 = createChurch("Church B");

    // Create members in different churches
    Member member1 = createMember("John", church1);
    Member member2 = createMember("Jane", church2);

    // Set tenant context to church1
    TenantContext.setCurrentChurchId(church1.getId());

    // Query should only return church1's members
    List<Member> members = memberRepository.findAll();
    assertEquals(1, members.size());
    assertEquals("John", members.get(0).getFirstName());

    // Verify cannot access church2's members
    assertFalse(members.contains(member2));
}

@Test
public void testCrossTenantAccessPrevented() {
    Church church1 = createChurch("Church A");
    Church church2 = createChurch("Church B");
    Member member2 = createMember("Jane", church2);

    // Set context to church1
    TenantContext.setCurrentChurchId(church1.getId());

    // Try to access church2's member by ID
    Optional<Member> result = memberRepository.findById(member2.getId());

    // Should not find the member (filtered out)
    assertFalse(result.isPresent());
}
```

### Integration Tests

```java
@Test
@WithMockUser
public void testAttendanceCreationPrevented() {
    // Church A user trying to record attendance for Church B member
    TenantContext.setCurrentChurchId(churchA.getId());

    // memberB belongs to Church B
    Attendance attendance = new Attendance();
    attendance.setMember(memberB);
    attendance.setAttendanceSession(sessionA); // Church A session

    // Should fail - member and session from different churches
    assertThrows(ConstraintViolationException.class, () -> {
        attendanceRepository.save(attendance);
    });
}
```

## Migration Path

### Phase 1: Database Migration (Current)

```sql
-- Add church_id to existing tables
ALTER TABLE members ADD COLUMN church_id BIGINT;
ALTER TABLE fellowships ADD COLUMN church_id BIGINT;
ALTER TABLE attendance_sessions ADD COLUMN church_id BIGINT;

-- Add foreign keys
ALTER TABLE members ADD CONSTRAINT fk_member_church
    FOREIGN KEY (church_id) REFERENCES churches(id);

-- Make required after data migration
ALTER TABLE members MODIFY church_id BIGINT NOT NULL;
```

### Phase 2: Data Migration

```sql
-- If existing data needs church assignment
UPDATE members SET church_id = 1 WHERE church_id IS NULL;
UPDATE fellowships SET church_id = 1 WHERE church_id IS NULL;
UPDATE attendance_sessions SET church_id = 1 WHERE church_id IS NULL;
```

### Phase 3: Application Deployment

1. Deploy updated entities with TenantBaseEntity
2. Enable Hibernate filter in configuration
3. Update authentication to set TenantContext
4. Test thoroughly before production

## Future Enhancements

### 1. Additional Tenant-Scoped Entities

As the application grows, add these entities as TenantBaseEntity:
- Events
- Donations
- Communications
- Resources
- Volunteer Schedules

### 2. Tenant-Level Settings

```java
@Entity
public class ChurchSettings extends BaseEntity {
    @OneToOne
    private Church church;

    private String theme;
    private String timezone;
    private boolean enableSMS;
    private boolean enableEmail;
}
```

### 3. Audit Logs Per Tenant

```java
@Entity
public class AuditLog extends TenantBaseEntity {
    private String userId;
    private String action;
    private String entityType;
    private Long entityId;
    private String changes;
    private LocalDateTime timestamp;
}
```

### 4. Cross-Tenant Reporting (Admin Only)

For platform admins to see statistics across all churches:
```java
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class PlatformStatsService {
    public Stats getAllChurchesStats() {
        // Disable filter temporarily
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter("churchFilter");

        // Query across all churches
        Stats stats = calculateStats();

        // Re-enable filter
        session.enableFilter("churchFilter");
        return stats;
    }
}
```

## Best Practices

1. **Always set TenantContext** - Before any database operation
2. **Validate church association** - On entity creation
3. **Test cross-tenant access** - Verify isolation in tests
4. **Use TenantBaseEntity** - For new tenant-scoped entities
5. **Document exceptions** - If entity is intentionally cross-tenant
6. **Clear context after request** - Prevent thread-local leaks
7. **Log tenant context** - For debugging and auditing
8. **Never disable filters** - Unless explicitly needed for admin functions

## Troubleshooting

### Issue: Cross-tenant data visible

**Solution:**
- Verify TenantContext.setCurrentChurchId() is called
- Check Hibernate filter is enabled
- Ensure entity extends TenantBaseEntity

### Issue: Cannot create entity

**Solution:**
- Verify church is set on entity
- Check church exists in database
- Validate foreign key constraints

### Issue: User without church

**Solution:**
- Update User.church to non-nullable
- Migrate existing users to have church
- Add validation in registration flow

## Summary

| Entity | Base Class | Church Relationship | Auto-Filtered | Purpose |
|--------|-----------|---------------------|---------------|---------|
| Church | BaseEntity | - (IS tenant) | No | Tenant root |
| Member | TenantBaseEntity | Inherited | Yes | Church members |
| Fellowship | TenantBaseEntity | Inherited | Yes | Church groups |
| AttendanceSession | TenantBaseEntity | Inherited | Yes | Attendance sessions |
| Attendance | BaseEntity | Via relationships | No | Attendance records |
| User | BaseEntity | Explicit (required) | No | App users |
| RefreshToken | BaseEntity | Explicit | No | Auth sessions |
| LoginAttempt | BaseEntity | None (cross-tenant) | No | Security audit |

**Total Entities:** 8
**Tenant-Scoped:** 5 (Church, Member, Fellowship, AttendanceSession, RefreshToken)
**Cross-Tenant:** 1 (LoginAttempt)
**Indirectly Scoped:** 1 (Attendance)
**User Entity:** 1 (Special case)
