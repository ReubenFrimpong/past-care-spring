# User Management System Improvements

## Overview
This document summarizes the improvements made to the user management system to address security, usability, and data integrity issues.

## Issues Addressed

### ✅ 1. User Access Restriction to Current Church (COMPLETED)

**Problem:** Users were not restricted to their own church - they could view, update, and delete users from other churches.

**Solution:** Implemented church-based access control across all user operations.

#### Changes Made:

**[UserPrincipal.java](src/main/java/com/reuben/pastcare_spring/security/UserPrincipal.java:64-70)**
- Added `getChurchId()` method to retrieve the current user's church ID
- Added `getUser()` method for full user object access

**[UserService.java](src/main/java/com/reuben/pastcare_spring/services/UserService.java:35-120)**

1. **getAllUsers() - Lines 35-55**
   - Now filters users by church
   - SUPERADMIN can see all users
   - Regular admins only see users from their church

2. **getUserById() - Lines 57-73**
   - Validates that the requested user belongs to the same church
   - Throws error if access is denied

3. **createUser() - Lines 123-191**
   - Automatically assigns new users to the current user's church
   - SUPERADMIN can specify any church
   - Regular admins can only create users for their own church

4. **updateUser() - Lines 193-237**
   - Validates church access before allowing updates
   - SUPERADMIN can change church association
   - Regular admins cannot change church assignment

5. **deleteUser() - Lines 239-254**
   - Validates church access before allowing deletion
   - Prevents cross-church user deletion

### ✅ 2. Automatic Password Generation (COMPLETED)

**Problem:** Users created via admin portal had no password mechanism defined.

**Solution:** Implemented secure automatic password generation with optional manual override.

#### Changes Made:

**[UserCreateResponse.java](src/main/java/com/reuben/pastcare_spring/dtos/UserCreateResponse.java)** - NEW FILE
- Created new response DTO for user creation
- Includes `temporaryPassword` field to return generated password
- Includes `message` field with instructions for admin

**[UserService.java](src/main/java/com/reuben/pastcare_spring/services/UserService.java:36-79)**
- Added `PasswordEncoder` dependency
- Implemented `generateSecurePassword()` method:
  - Generates 12-character passwords by default
  - Ensures at least one: lowercase, uppercase, digit, and special character
  - Uses `SecureRandom` for cryptographic security
  - Shuffles characters for additional randomness

**Password Generation Logic (Lines 136-147):**
```java
// Generate or use provided password
String temporaryPassword;
if (userRequest.password() != null && !userRequest.password().trim().isEmpty()) {
  // Use provided password
  temporaryPassword = userRequest.password();
} else {
  // Auto-generate a secure password
  temporaryPassword = generateSecurePassword(12);
}

// Encode and set the password
user.setPassword(passwordEncoder.encode(temporaryPassword));
```

**[UsersController.java](src/main/java/com/reuben/pastcare_spring/controllers/UsersController.java:54)**
- Updated return type to `UserCreateResponse`
- Response now includes the temporary password for the admin to share with the new user

#### Password Characteristics:
- **Length:** 12 characters (configurable)
- **Character Set:** Lowercase (a-z), Uppercase (A-Z), Digits (0-9), Special (!@#$%^&*)
- **Guaranteed Complexity:** At least one character from each category
- **Security:** Uses `SecureRandom` for cryptographically secure randomness

#### Example Generated Password:
```
T3k@mP9xL#wQ
```

### 🔄 3. Table Display Review (PENDING)

**Status:** Requires frontend investigation to determine best display format.

**Considerations:**
- Are tables the best UI pattern for user management?
- Should we use cards, list view, or keep tables?
- Mobile responsiveness concerns
- User experience consistency with other pages

### 🔄 4. User Management Page Styling (PENDING)

**Status:** Requires frontend component investigation.

**Requirements:**
- Match styling with members page
- Consistent dialog design
- Uniform color scheme and spacing
- Same UI components (buttons, inputs, etc.)

## Security Improvements Summary

### Church Isolation
- ✅ Users can only view users from their church
- ✅ Users can only create users for their church
- ✅ Users can only update users from their church
- ✅ Users can only delete users from their church
- ✅ SUPERADMIN has full access across all churches

### Password Security
- ✅ Automatic secure password generation
- ✅ BCrypt password encoding
- ✅ Temporary passwords returned only once during creation
- ✅ Passwords never exposed in regular GET requests

## API Response Changes

### POST /api/users (Create User)

**Old Response:**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "+233123456789",
  "title": "Pastor",
  "church": {...},
  "fellowships": [...],
  "primaryService": "Sunday Service",
  "role": "ADMIN"
}
```

**New Response:**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "+233123456789",
  "title": "Pastor",
  "church": {...},
  "fellowships": [...],
  "primaryService": "Sunday Service",
  "role": "ADMIN",
  "temporaryPassword": "T3k@mP9xL#wQ",
  "message": "User created successfully. Please share the temporary password with the user and ask them to change it on first login."
}
```

## Usage Instructions

### For Admins Creating Users

1. **Via API:**
   ```bash
   POST /api/users
   {
     "name": "Jane Smith",
     "email": "jane@church.com",
     "phoneNumber": "+233987654321",
     "title": "Deacon",
     "fellowshipIds": [1, 2],
     "primaryService": "Evening Service",
     "role": "USER"
     // password field is optional - will auto-generate if not provided
   }
   ```

2. **Response Will Include:**
   - All user details
   - `temporaryPassword` - Share this with the new user
   - `message` - Instructions for the admin

3. **Best Practice:**
   - Copy the `temporaryPassword` immediately
   - Share it securely with the new user (e.g., in person, encrypted message)
   - Instruct the user to change it on first login
   - The password will NOT be retrievable after this response

### For SUPERADMIN

- Can specify `churchId` in the request to create users for any church
- Can update `churchId` to transfer users between churches
- Can view, update, and delete users across all churches

### For Regular Admins

- Cannot specify `churchId` - automatically set to their church
- Cannot transfer users to other churches
- Can only manage users within their own church

## Testing

### Test Church Isolation

```bash
# As regular admin - should only see own church users
GET /api/users

# Should fail - user from different church
GET /api/users/{id-from-other-church}

# Should fail - cannot create for other church
POST /api/users
{
  "churchId": 999,  // Different church
  ...
}
```

### Test Password Generation

```bash
# Auto-generate password (omit password field)
POST /api/users
{
  "name": "Test User",
  "email": "test@church.com",
  ...
  // No password field
}

# Response should include temporaryPassword
```

## Future Enhancements

1. **Password Reset Flow**
   - Add "Reset Password" feature for admins
   - Generate new temporary password
   - Send via email/SMS

2. **Force Password Change**
   - Add `mustChangePassword` flag
   - Require change on first login

3. **Password Expiry**
   - Implement temporary password expiration (e.g., 24 hours)
   - Auto-disable account if not changed

4. **Audit Trail**
   - Log password generation events
   - Track who created which users
   - Monitor password changes

5. **Email Notifications**
   - Automatically email temporary password to new user
   - Include account activation link

## Files Modified

### Backend
1. `src/main/java/com/reuben/pastcare_spring/security/UserPrincipal.java`
2. `src/main/java/com/reuben/pastcare_spring/services/UserService.java`
3. `src/main/java/com/reuben/pastcare_spring/controllers/UsersController.java`
4. `src/main/java/com/reuben/pastcare_spring/dtos/UserCreateResponse.java` (NEW)

### Frontend
- **PENDING:** User management page styling updates
- **PENDING:** Table vs. alternative display evaluation

## Notes

- All password generation uses cryptographically secure random number generation
- Church-based access control is enforced at the service layer
- SUPERADMIN role bypasses church restrictions for multi-church management
- Temporary passwords are only returned once during user creation for security
