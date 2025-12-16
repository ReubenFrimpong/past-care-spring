# Security Features - Brute Force Protection

## Overview

The PastCare Spring application implements comprehensive brute force protection to prevent unauthorized access through password guessing attacks while maintaining user-friendly password policies.

## Features

### 1. **Failed Login Attempt Tracking**
- All login attempts (successful and failed) are tracked in the database
- Tracks email, IP address, timestamp, user agent, and success status
- Automatically cleaned up after 30 days

### 2. **Account Lockout Mechanism**
- **Threshold**: 5 failed login attempts
- **Lockout Duration**: 15 minutes
- **Auto-unlock**: Accounts automatically unlock after the lockout period expires
- **Reset on Success**: Failed attempt counter resets to 0 on successful login

### 3. **IP-Based Rate Limiting**
- **Threshold**: 10 failed attempts from the same IP
- **Time Window**: 15 minutes
- **Protection**: Blocks the IP address temporarily even if using different email addresses

### 4. **Security Logging**
- All authentication events are logged with SLF4J
- Failed attempts log email and IP address
- Account lockouts are logged with lockout expiry time
- Successful logins are logged for audit trail

### 5. **User-Friendly Error Messages**
- Generic error messages for security (don't reveal account existence)
- Specific lockout messages with estimated unlock time
- Clear feedback for too many requests from IP

## Configuration

### Constants (BruteForceProtectionService.java)

```java
MAX_FAILED_ATTEMPTS = 5           // Failed attempts before account lockout
LOCKOUT_DURATION_MINUTES = 15     // Account lockout duration
ATTEMPT_WINDOW_MINUTES = 15       // Time window for counting attempts
MAX_IP_ATTEMPTS = 10              // Failed attempts from IP before blocking
```

You can adjust these values based on your security requirements.

## Database Schema

### LoginAttempt Table
```sql
CREATE TABLE login_attempts (
    id BIGINT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    ip_address VARCHAR(255) NOT NULL,
    success BOOLEAN NOT NULL,
    attempt_time TIMESTAMP NOT NULL,
    user_agent VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### User Table (Additional Fields)
```sql
ALTER TABLE users ADD COLUMN failed_login_attempts INT DEFAULT 0;
ALTER TABLE users ADD COLUMN account_locked_until TIMESTAMP;
ALTER TABLE users ADD COLUMN account_locked BOOLEAN DEFAULT FALSE;
```

## API Error Codes

### 401 UNAUTHORIZED
- Invalid credentials
- Wrong email or password

### 423 LOCKED
- Account locked due to too many failed attempts
- Returns estimated unlock time in error message

### 429 TOO_MANY_REQUESTS
- Too many failed attempts from the same IP address
- Temporary IP block

## Frontend Integration

The frontend handles these error codes:

```typescript
if (error.status === 423) {
  // Account locked - show lockout message
} else if (error.status === 429) {
  // IP blocked - show rate limit message
} else if (error.status === 401) {
  // Invalid credentials
}
```

## Security Best Practices

### 1. **No Password Complexity Requirements**
- Users can create passwords of any strength
- No forced mixing of cases, numbers, or special characters
- Minimum length enforced through validation

### 2. **Progressive Security**
- First few attempts: Normal error messages
- After threshold: Account lockout
- Persistent abuse: IP blocking

### 3. **Transparent Protection**
- Users are informed when their account is locked
- Clear indication of when they can try again
- No surprise lockouts without explanation

### 4. **Privacy Protection**
- Generic error messages don't reveal if account exists
- Logging includes only necessary security information
- IP addresses handled according to privacy regulations

## Monitoring and Maintenance

### Scheduled Cleanup
- **Frequency**: Daily at 2:00 AM
- **Retention**: 30 days of login attempts
- **Purpose**: Keep database size manageable while maintaining audit trail

### Manual Unlock (Admin Feature - To Be Implemented)
Administrators can manually unlock accounts if needed:
```java
bruteForceProtectionService.resetFailedAttempts(email);
```

## Testing the Protection

### Test Failed Attempts
1. Try logging in with wrong password 5 times
2. 6th attempt should return 423 LOCKED
3. Wait 15 minutes or manually unlock
4. Login should work again

### Test IP Blocking
1. Try logging in with multiple different emails from same IP
2. After 10 failed attempts, should return 429 TOO_MANY_REQUESTS
3. Wait 15 minutes for automatic unblock

### Test Successful Login Reset
1. Fail login 3 times
2. Login successfully with correct password
3. Counter should reset to 0
4. Can fail 5 more times before lockout

## Future Enhancements

1. **CAPTCHA Integration**: Add CAPTCHA after N failed attempts
2. **Email Notifications**: Notify users of suspicious login attempts
3. **2FA/MFA**: Two-factor authentication for additional security
4. **Geolocation**: Track login locations and flag unusual activity
5. **Device Fingerprinting**: Recognize trusted devices
6. **Admin Dashboard**: View and manage locked accounts
7. **Configurable Settings**: Allow configuration via application.properties

## Compliance Considerations

- **GDPR**: Login attempts include IP addresses (personal data)
- **Retention**: 30-day retention policy for login attempts
- **Right to be Forgotten**: Cleanup mechanism supports data deletion
- **Audit Trail**: Comprehensive logging for security audits

## Troubleshooting

### Account Stays Locked After Timeout
- Check server time is correct
- Verify `accountLockedUntil` field in database
- Check if cleanup task is running

### Too Many False Positives
- Increase `MAX_FAILED_ATTEMPTS` threshold
- Increase `LOCKOUT_DURATION_MINUTES` to reduce frustration
- Consider implementing "forgot password" flow

### IP Blocking Issues
- Ensure X-Forwarded-For header is properly set (behind proxy)
- Whitelist internal IP addresses if needed
- Adjust `MAX_IP_ATTEMPTS` for shared IP scenarios
