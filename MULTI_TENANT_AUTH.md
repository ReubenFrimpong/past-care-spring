# Multi-Tenant JWT Authentication with HttpOnly Cookies

## Overview

PastCare Spring now implements a comprehensive multi-tenant authentication system with:
- ✅ Tenant (church) isolation in JWT claims
- ✅ HttpOnly cookies for XSS protection
- ✅ Refresh token mechanism for long-lived sessions
- ✅ Automatic token rotation
- ✅ Session management (max 5 devices per user)
- ✅ Brute force protection
- ✅ Tenant-scoped data access

## Architecture

### JWT Structure

**Access Token** (15 minutes - 1 hour):
```json
{
  "sub": "user@example.com",
  "userId": 123,
  "churchId": 456,
  "role": "ADMIN",
  "tokenType": "access",
  "iat": 1234567890,
  "exp": 1234571490
}
```

**Refresh Token** (30 days):
- UUID-based token stored in database
- Linked to user and church (tenant)
- Tracks IP address and user agent
- Can be revoked individually or all at once

### Cookie Configuration

**Access Token Cookie:**
- Name: `access_token`
- HttpOnly: `true`
- Secure: `true` (in production)
- SameSite: `Lax`
- Path: `/`
- Max-Age: 3600 seconds (1 hour)

**Refresh Token Cookie:**
- Name: `refresh_token`
- HttpOnly: `true`
- Secure: `true` (in production)
- SameSite: `Lax`
- Path: `/api/auth/refresh`
- Max-Age: 2592000 seconds (30 days)

## API Endpoints

### POST /api/auth/login
Login with email and password.

**Request:**
```json
{
  "email": "pastor@church.com",
  "password": "securePassword",
  "rememberMe": true
}
```

**Response:**
```json
{
  "token": null,  // Tokens are in HttpOnly cookies
  "refreshToken": null,
  "user": {
    "id": 123,
    "name": "John Doe",
    "email": "pastor@church.com",
    "church": {
      "id": 456,
      "name": "Grace Community Church"
    },
    "role": "ADMIN"
  }
}
```

**Cookies Set:**
- `access_token` - JWT with tenant claims
- `refresh_token` - UUID refresh token

### POST /api/auth/refresh
Refresh the access token using refresh token cookie.

**Request:** No body required (uses cookie)

**Response:**
```json
{
  "token": null,  // New access token in cookie
  "refreshToken": null,
  "user": { ... }
}
```

**Cookies Updated:**
- `access_token` - New JWT

### POST /api/auth/logout
Logout and revoke refresh token.

**Request:** No body required (uses cookie)

**Response:** 200 OK

**Cookies Cleared:**
- `access_token`
- `refresh_token`

### POST /api/auth/register
Register a new user (must be associated with a church).

**Request:**
```json
{
  "name": "Jane Smith",
  "email": "jane@church.com",
  "phoneNumber": "+1234567890",
  "password": "securePassword",
  "role": "USER"
}
```

## Frontend Integration

### Login Flow

```typescript
async login(email: string, password: string, rememberMe: boolean) {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include', // IMPORTANT: Include cookies
    body: JSON.stringify({ email, password, rememberMe })
  });

  const data = await response.json();
  // Tokens are automatically stored in HttpOnly cookies
  // Store user data in localStorage or state management
  localStorage.setItem('user', JSON.stringify(data.user));

  return data.user;
}
```

### Making Authenticated Requests

```typescript
async fetchData() {
  const response = await fetch('/api/members', {
    credentials: 'include' // Automatically sends cookies
  });

  if (response.status === 401) {
    // Token expired, try to refresh
    await this.refreshToken();
    // Retry original request
    return fetch('/api/members', { credentials: 'include' });
  }

  return response.json();
}
```

### Token Refresh

```typescript
async refreshToken() {
  const response = await fetch('/api/auth/refresh', {
    method: 'POST',
    credentials: 'include'
  });

  if (!response.ok) {
    // Refresh token expired or invalid
    this.logout();
    throw new Error('Session expired');
  }

  const data = await response.json();
  localStorage.setItem('user', JSON.stringify(data.user));
}
```

### Logout

```typescript
async logout() {
  await fetch('/api/auth/logout', {
    method: 'POST',
    credentials: 'include'
  });

  localStorage.removeItem('user');
  // Redirect to login page
}
```

### Angular HTTP Interceptor

```typescript
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpServletRequest, next: HttpHandler): Observable<HttpEvent<any>> {
    // Ensure credentials (cookies) are included
    const authReq = req.clone({
      withCredentials: true
    });

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          // Try to refresh token
          return this.authService.refreshToken().pipe(
            switchMap(() => next.handle(authReq)),
            catchError(() => {
              this.authService.logout();
              return throwError(() => error);
            })
          );
        }
        return throwError(() => error);
      })
    );
  }
}
```

## Database Schema

### refresh_tokens Table

```sql
CREATE TABLE refresh_tokens (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  token VARCHAR(500) UNIQUE NOT NULL,
  user_id BIGINT NOT NULL,
  church_id BIGINT NOT NULL,
  expiry_date DATETIME NOT NULL,
  revoked BOOLEAN DEFAULT FALSE,
  ip_address VARCHAR(255) NOT NULL,
  user_agent VARCHAR(255),
  last_used_at DATETIME NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (church_id) REFERENCES churches(id) ON DELETE CASCADE,

  INDEX idx_token (token),
  INDEX idx_user_id (user_id),
  INDEX idx_expiry_date (expiry_date)
);
```

### Updated users Table

```sql
ALTER TABLE users
  ADD COLUMN failed_login_attempts INT DEFAULT 0,
  ADD COLUMN account_locked_until DATETIME,
  ADD COLUMN account_locked BOOLEAN DEFAULT FALSE;
```

## Multi-Tenancy Implementation

### TenantBaseEntity

All tenant-scoped entities extend `TenantBaseEntity`:

```java
@MappedSuperclass
@FilterDef(name = "churchFilter", parameters = @ParamDef(name = "churchId", type = Long.class))
@Filter(name = "churchFilter", condition = "church_id = :churchId")
public abstract class TenantBaseEntity extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "church_id", nullable = false)
  private Church church;
}
```

### Tenant Context

```java
// Set current tenant from JWT
TenantContext.setCurrentChurchId(jwtUtil.extractChurchId(token));

// All queries automatically filtered by church_id
List<Member> members = memberRepository.findAll(); // Only current church's members
```

### Entities Using Multi-Tenancy

- ✅ `Member` extends `TenantBaseEntity`
- ✅ `Fellowship` extends `TenantBaseEntity`
- ✅ `AttendanceSession` extends `TenantBaseEntity`
- ✅ `Attendance` (through Member relationship)
- ✅ `RefreshToken` has church_id

## Security Features

### XSS Protection
- HttpOnly cookies prevent JavaScript access
- Tokens cannot be stolen via XSS attacks

### CSRF Protection
- SameSite=Lax prevents cross-site attacks
- POST requests protected automatically

### Brute Force Protection
- 5 failed attempts → account lockout (15 min)
- 10 failed attempts from IP → IP block (15 min)
- All attempts logged with IP and user agent

### Session Management
- Max 5 active sessions per user
- Oldest sessions auto-revoked
- Individual session revocation
- "Logout everywhere" functionality

### Tenant Isolation
- Church ID embedded in JWT
- All queries automatically scoped to tenant
- Cross-tenant access prevented at data layer

## Configuration

### application.properties

```properties
# JWT Configuration
jwt.expiration=3600000                    # 1 hour
jwt.expiration.remember-me=2592000000     # 30 days
jwt.secret=YOUR_BASE64_SECRET_KEY

# Refresh Token Configuration
jwt.refresh-token.expiration=2592000000   # 30 days
jwt.refresh-token.max-active=5            # Max sessions per user

# Cookie Configuration (Production)
jwt.cookie.domain=yourdomain.com
jwt.cookie.secure=true                    # HTTPS only
jwt.cookie.same-site=Lax
```

### Development vs Production

**Development (localhost):**
```properties
jwt.cookie.domain=localhost
jwt.cookie.secure=false
```

**Production:**
```properties
jwt.cookie.domain=.yourapp.com
jwt.cookie.secure=true
```

## Migration from localStorage

### Phase 1: Dual Mode (Current)
- Backend sends tokens in both cookies AND response body
- Frontend can use either method
- Allows gradual migration

### Phase 2: Cookie-Only (Future)
```java
// Remove tokens from response body
return ResponseEntity.ok(new AuthResponse(null, null, authResponse.user()));
```

### Frontend Changes Required

1. **Remove localStorage token storage**
```typescript
// OLD - DON'T DO THIS
localStorage.setItem('authToken', response.token);

// NEW - Cookies handled automatically
// Just store user data
localStorage.setItem('user', JSON.stringify(response.user));
```

2. **Add withCredentials to all HTTP requests**
```typescript
// Angular
this.http.get('/api/members', { withCredentials: true })

// Fetch API
fetch('/api/members', { credentials: 'include' })
```

3. **Update auth guard**
```typescript
canActivate(): boolean {
  const user = localStorage.getItem('user');
  if (!user) {
    // Try to refresh token silently
    this.authService.refreshToken().subscribe({
      next: () => true,
      error: () => {
        this.router.navigate(['/login']);
        return false;
      }
    });
  }
  return true;
}
```

## Scheduled Maintenance

### Cleanup Expired Tokens

```java
@Scheduled(cron = "0 0 3 * * *") // Daily at 3 AM
public void cleanupExpiredTokens() {
  refreshTokenService.cleanupExpiredTokens();
}
```

### Cleanup Old Login Attempts

```java
@Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
public void cleanupOldLoginAttempts() {
  bruteForceProtectionService.cleanupOldAttempts();
}
```

## Monitoring & Auditing

### Metrics to Track
- Active sessions per user
- Failed login attempts per hour
- Token refresh rate
- Average session duration
- Expired tokens cleaned up

### Security Logs
```
INFO: Creating new refresh token for user: pastor@church.com from IP: 192.168.1.1
WARN: Failed login attempt for email: user@test.com from IP: 10.0.0.1
WARN: Account locked for email: user@test.com due to 5 failed attempts
INFO: Revoked refresh token for user: pastor@church.com
```

## Testing

### Test Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@church.com","password":"password","rememberMe":true}' \
  -c cookies.txt
```

### Test Authenticated Request
```bash
curl -X GET http://localhost:8080/api/members \
  -b cookies.txt
```

### Test Token Refresh
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -b cookies.txt \
  -c cookies.txt
```

### Test Logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -b cookies.txt
```

## Troubleshooting

### Cookies Not Being Set
- Check `withCredentials: true` in frontend requests
- Verify CORS configuration allows credentials
- Check browser console for cookie warnings

### 401 Errors After Login
- Verify JWT filter extracts token from cookies
- Check TenantContext is set from JWT
- Ensure user has associated church

### Tokens Expiring Too Quickly
- Check `jwt.expiration` configuration
- Verify refresh token mechanism is working
- Monitor token refresh frequency

### Cross-Tenant Data Leaks
- Verify `@Filter` annotation on entities
- Check TenantContext is set correctly
- Test with multiple churches

## Best Practices

1. **Always use HTTPS in production** (`jwt.cookie.secure=true`)
2. **Rotate JWT secret regularly**
3. **Monitor failed login attempts**
4. **Implement rate limiting on auth endpoints**
5. **Log all security events**
6. **Test cross-tenant isolation thoroughly**
7. **Keep refresh tokens short-lived (30 days max)**
8. **Revoke tokens on password change**
9. **Implement device management UI**
10. **Regular security audits**

## Next Steps

1. **Implement Tenant Filter** - Auto-apply church filter to all queries
2. **Add Device Management** - Let users view/revoke active sessions
3. **Implement Password Reset** - Secure flow with email verification
4. **Add 2FA** - Optional two-factor authentication
5. **Church Admin Dashboard** - Manage users and permissions
6. **Audit Logs** - Track all data access per tenant
7. **Performance Monitoring** - Track query performance by tenant
8. **Backup/Export** - Per-tenant data export
