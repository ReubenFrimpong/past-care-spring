# Church Registration & Multi-Tenant Architecture

## Problem Statement

Current issues:
1. `User.church` is `nullable = false` - prevents superadmins from existing
2. No church creation during registration flow
3. Need seamless UX for first-time church signup
4. Need to support SUPERADMIN role without church association

## Solution Architecture

### 1. Database Schema Changes

#### User Model Update
```java
@ManyToOne
@JoinColumn(name = "church_id", nullable = true)  // ← Change to nullable
private Church church;
```

**Validation Rule:** 
- SUPERADMIN: `church` can be null
- All other roles: `church` must NOT be null

### 2. Role Hierarchy

```
SUPERADMIN (Platform Admin)
  ↓
ADMIN (Church Admin - Full access to their church)
  ↓
TREASURER (Financial management)
  ↓
FELLOWSHIP_HEAD (Fellowship-specific access)
```

**Permissions:**
- **SUPERADMIN**: 
  - No church association
  - Can view/manage all churches
  - Platform-wide analytics
  - Create churches manually
  
- **ADMIN**: 
  - Church owner/primary admin
  - Full access to their church data
  - Can invite other users to their church
  - Can manage church settings
  
- **TREASURER**: Limited to financial features
- **FELLOWSHIP_HEAD**: Limited to assigned fellowships

### 3. Registration Flow Design

#### Scenario A: New Church Registration (First User)

**Frontend Flow:**
```
Step 1: Personal Info
  ├─ Name
  ├─ Email
  ├─ Password
  └─ Phone (optional)

Step 2: Church Info
  ├─ Church Name *
  ├─ Church Address
  ├─ Church Phone
  ├─ Church Email
  └─ Church Website

Step 3: Confirmation & Submit
```

**Backend Payload:**
```json
{
  "user": {
    "name": "John Pastor",
    "email": "john@example.com",
    "password": "***",
    "phoneNumber": "+1234567890",
    "role": "ADMIN"  // Auto-assigned for new church
  },
  "church": {
    "name": "Grace Community Church",
    "address": "123 Faith St",
    "phoneNumber": "+1234567890",
    "email": "info@gracechurch.org",
    "website": "gracechurch.org"
  }
}
```

**Backend Process:**
1. Validate unique church name
2. Validate unique user email
3. Create church (transaction)
4. Create user with `role=ADMIN` and `church_id=new_church_id`
5. Return success

#### Scenario B: Join Existing Church (Invited User)

**Frontend Flow:**
```
Step 1: Personal Info
  ├─ Name
  ├─ Email
  ├─ Password
  ├─ Phone (optional)
  └─ Invitation Code/Link

Step 2: Confirmation & Submit
```

**Backend Payload:**
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "password": "***",
  "phoneNumber": "+1234567890",
  "invitationToken": "abc123xyz",
  "role": "TREASURER"  // From invitation
}
```

**Backend Process:**
1. Validate invitation token
2. Extract `church_id` and `role` from invitation
3. Create user with assigned church and role
4. Mark invitation as used
5. Return success

#### Scenario C: Superadmin Creation (Manual/Seed)

**Backend Only:**
```java
User superadmin = new User();
superadmin.setName("Platform Admin");
superadmin.setEmail("admin@pastcare.com");
superadmin.setPassword(encoded);
superadmin.setRole(Role.SUPERADMIN);
superadmin.setChurch(null);  // No church!
```

### 4. Implementation Plan

#### Phase 1: Backend Changes

**1.1 Update User Model**
```java
// User.java
@ManyToOne
@JoinColumn(name = "church_id", nullable = true)
private Church church;

// Add validation method
public void validate() {
    if (role != Role.SUPERADMIN && church == null) {
        throw new IllegalStateException("Non-superadmin users must be associated with a church");
    }
}
```

**1.2 Create New DTOs**
```java
// ChurchRegistrationRequest.java
public record ChurchRegistrationRequest(
    @NotBlank String name,
    String address,
    String phoneNumber,
    String email,
    String website
) {}

// UserChurchRegistrationRequest.java
public record UserChurchRegistrationRequest(
    @Valid UserRegistrationData user,
    @Valid ChurchRegistrationRequest church
) {}

// UserRegistrationData.java
public record UserRegistrationData(
    @NotBlank String name,
    @NotBlank @Email String email,
    @NotBlank String password,
    String phoneNumber
) {}

// InvitationRegistrationRequest.java
public record InvitationRegistrationRequest(
    @NotBlank String name,
    @NotBlank @Email String email,
    @NotBlank String password,
    String phoneNumber,
    @NotBlank String invitationToken
) {}
```

**1.3 Create Invitation System**
```java
// Invitation.java
@Entity
public class Invitation extends BaseEntity {
    @ManyToOne
    private Church church;
    
    @Column(unique = true, nullable = false)
    private String token;  // UUID
    
    @Column(nullable = false)
    private String email;  // Who this invitation is for
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    @ManyToOne
    private User invitedBy;
    
    private LocalDateTime expiresAt;
    
    private boolean used = false;
    
    private LocalDateTime usedAt;
}
```

**1.4 Update AuthService**
```java
@Service
public class AuthService {
    
    @Transactional
    public User registerNewChurch(UserChurchRegistrationRequest request) {
        // 1. Validate church name is unique
        if (churchRepository.existsByName(request.church().name())) {
            throw new DuplicateChurchException("Church name already exists");
        }
        
        // 2. Validate user email is unique
        if (userRepository.existsByEmail(request.user().email())) {
            throw new DuplicateUserException("Email already registered");
        }
        
        // 3. Create church
        Church church = new Church();
        church.setName(request.church().name());
        church.setAddress(request.church().address());
        church.setPhoneNumber(request.church().phoneNumber());
        church.setEmail(request.church().email());
        church.setWebsite(request.church().website());
        church.setActive(true);
        church = churchRepository.save(church);
        
        // 4. Create user as ADMIN of new church
        User user = new User();
        user.setName(request.user().name());
        user.setEmail(request.user().email());
        user.setPassword(passwordEncoder.encode(request.user().password()));
        user.setPhoneNumber(request.user().phoneNumber());
        user.setRole(Role.ADMIN);  // First user is always ADMIN
        user.setChurch(church);
        
        return userRepository.save(user);
    }
    
    @Transactional
    public User registerWithInvitation(InvitationRegistrationRequest request) {
        // 1. Find and validate invitation
        Invitation invitation = invitationRepository.findByToken(request.invitationToken())
            .orElseThrow(() -> new InvalidInvitationException("Invalid invitation token"));
        
        if (invitation.isUsed()) {
            throw new InvalidInvitationException("Invitation already used");
        }
        
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidInvitationException("Invitation expired");
        }
        
        if (!invitation.getEmail().equalsIgnoreCase(request.email())) {
            throw new InvalidInvitationException("Invitation email mismatch");
        }
        
        // 2. Validate user email is unique
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("Email already registered");
        }
        
        // 3. Create user with church from invitation
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhoneNumber(request.phoneNumber());
        user.setRole(invitation.getRole());
        user.setChurch(invitation.getChurch());
        user = userRepository.save(user);
        
        // 4. Mark invitation as used
        invitation.setUsed(true);
        invitation.setUsedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
        
        return user;
    }
}
```

**1.5 Update AuthController**
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @PostMapping("/register/church")
    public ResponseEntity<User> registerNewChurch(
        @Valid @RequestBody UserChurchRegistrationRequest request
    ) {
        User user = authService.registerNewChurch(request);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/register/invitation")
    public ResponseEntity<User> registerWithInvitation(
        @Valid @RequestBody InvitationRegistrationRequest request
    ) {
        User user = authService.registerWithInvitation(request);
        return ResponseEntity.ok(user);
    }
    
    // Keep old /register for backward compatibility or remove it
    @PostMapping("/register")
    @Deprecated
    public ResponseEntity<User> register(...) {
        // Old endpoint - can be removed or kept for superadmin creation
    }
}
```

**1.6 Update Login Validation**
```java
// AuthService.login()
public AuthTokenData login(AuthLoginRequest request, HttpServletRequest httpRequest) {
    // ... existing auth code ...
    
    // Validate user has a church (unless SUPERADMIN)
    if (user.getRole() != Role.SUPERADMIN && user.getChurch() == null) {
        throw new RuntimeException("User must be associated with a church");
    }
    
    // For SUPERADMIN, don't include church in JWT
    Long churchId = user.getChurch() != null ? user.getChurch().getId() : null;
    
    String accessToken = jwtUtil.generateToken(
        userDetails,
        user.getId(),
        churchId,  // Can be null for SUPERADMIN
        user.getRole().name(),
        request.rememberMe()
    );
    
    // ... rest of code ...
}
```

#### Phase 2: Frontend Changes

**2.1 Registration Page Options**
```typescript
enum RegistrationType {
  NEW_CHURCH = 'new_church',
  INVITATION = 'invitation'
}
```

**2.2 New Church Registration Form**
```html
<!-- Step 1: User Info -->
<form>
  <input name="name" required />
  <input name="email" type="email" required />
  <input name="password" type="password" required />
  <input name="phoneNumber" />
  <button (click)="nextStep()">Continue</button>
</form>

<!-- Step 2: Church Info -->
<form>
  <h3>Create Your Church</h3>
  <input name="churchName" required />
  <input name="churchAddress" />
  <input name="churchPhone" />
  <input name="churchEmail" type="email" />
  <input name="churchWebsite" />
  <button (click)="submit()">Create Account</button>
</form>
```

**2.3 Invitation Registration Form**
```html
<form>
  <h3>Join Your Church</h3>
  <input name="invitationCode" readonly value="{{ tokenFromUrl }}" />
  <div class="church-info">
    <p>You're joining: {{ churchName }}</p>
    <p>Role: {{ roleName }}</p>
  </div>
  <input name="name" required />
  <input name="email" type="email" required />
  <input name="password" type="password" required />
  <input name="phoneNumber" />
  <button (click)="submit()">Complete Registration</button>
</form>
```

**2.4 Registration Service**
```typescript
registerNewChurch(userData: UserData, churchData: ChurchData): Observable<User> {
  return this.http.post(`${this.apiUrl}/auth/register/church`, {
    user: userData,
    church: churchData
  });
}

registerWithInvitation(userData: UserData, invitationToken: string): Observable<User> {
  return this.http.post(`${this.apiUrl}/auth/register/invitation`, {
    ...userData,
    invitationToken
  });
}
```

### 5. Migration Plan

**Database Migration:**
```sql
-- 1. Make church_id nullable
ALTER TABLE user MODIFY COLUMN church_id BIGINT NULL;

-- 2. Create invitation table
CREATE TABLE invitation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    church_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    invited_by_id BIGINT,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (church_id) REFERENCES church(id),
    FOREIGN KEY (invited_by_id) REFERENCES user(id)
);

-- 3. Create superadmin user (optional - can be done via code)
INSERT INTO user (name, email, password, role, church_id, created_at, updated_at)
VALUES ('Platform Admin', 'admin@pastcare.com', '$2a$...', 'SUPERADMIN', NULL, NOW(), NOW());
```

### 6. User Flows Summary

**Flow 1: First Church Setup**
```
User visits /register
  → Selects "Create New Church"
  → Fills personal info
  → Fills church info
  → Submits
  → Backend creates church + user (ADMIN)
  → Auto-login
  → Redirects to dashboard
```

**Flow 2: Team Member Joins**
```
Admin invites user via email
  → Backend creates invitation with token
  → Email sent with link: /register/invitation?token=abc123
  → User clicks link
  → Pre-filled church info shown
  → User fills personal info
  → Submits
  → Backend validates token + creates user
  → Auto-login
  → Redirects to dashboard
```

**Flow 3: Superadmin Access**
```
Superadmin logs in
  → No church context in JWT
  → Special dashboard showing all churches
  → Can switch between churches for debugging
  → Platform-wide analytics
```

### 7. Security Considerations

1. **Church Name Uniqueness**: Prevent duplicate church names
2. **Invitation Security**:
   - Tokens are UUID (hard to guess)
   - Expire after 7 days
   - Single-use only
   - Email verification
3. **Role Validation**: Backend always validates role permissions
4. **Tenant Isolation**: Non-superadmin users can only see their church data
5. **SUPERADMIN Protection**: 
   - Can only be created via database/seed
   - Cannot be created through registration endpoints

### 8. Testing Checklist

- [ ] SUPERADMIN can log in without church
- [ ] New church registration creates both church and user
- [ ] Duplicate church name is rejected
- [ ] Duplicate email is rejected
- [ ] Invitation-based registration works
- [ ] Expired invitations are rejected
- [ ] Used invitations are rejected
- [ ] Email mismatch on invitation is rejected
- [ ] Non-SUPERADMIN users require church association
- [ ] JWT generation handles null churchId for SUPERADMIN
- [ ] Tenant isolation works (users can't see other churches)
- [ ] First user of new church becomes ADMIN

### 9. Future Enhancements

1. **Church Switching**: Allow users to be part of multiple churches
2. **Subdomain Routing**: `gracechurch.pastcare.com`
3. **Church Transfer**: Transfer church ownership
4. **Bulk Invitations**: Invite multiple users at once
5. **Self-Service Church Settings**: Church admins can update church info
6. **Church Deactivation**: Soft-delete churches
7. **User Approval Workflow**: Admin approval for new registrations

---

## Quick Implementation Checklist

### Backend
- [ ] Update `User.church` to nullable
- [ ] Create `Invitation` entity
- [ ] Create new DTOs (ChurchRegistrationRequest, etc.)
- [ ] Update AuthService with new registration methods
- [ ] Update AuthController with new endpoints
- [ ] Add validation for church requirement (except SUPERADMIN)
- [ ] Update JWT generation for nullable churchId
- [ ] Create database migration
- [ ] Add exception classes
- [ ] Create InvitationService

### Frontend
- [ ] Create multi-step registration form
- [ ] Add church creation step
- [ ] Create invitation registration page
- [ ] Update AuthService with new methods
- [ ] Handle invitation token from URL
- [ ] Add proper error messages
- [ ] Update registration routes

### Testing
- [ ] Unit tests for AuthService methods
- [ ] Integration tests for registration flows
- [ ] Test SUPERADMIN login
- [ ] Test invitation lifecycle
- [ ] Test validation errors
