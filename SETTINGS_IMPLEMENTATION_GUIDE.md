# Church Settings Implementation Guide

## Overview
This document tracks the implementation status of church settings and their impact on application functionality.

## Settings Database Migration
- **Migration File**: `V68__create_church_settings_table.sql`
- **Status**: ✅ Applied successfully
- **Table**: `church_settings` (church_id, setting_key, setting_value, setting_type)

## Settings API
All settings are accessible via REST API:
- `GET /api/settings` - Get all settings (requires `CHURCH_SETTINGS_VIEW`)
- `PUT /api/settings` - Save multiple settings (requires `CHURCH_SETTINGS_EDIT`)
- `GET /api/settings/{key}` - Get specific setting
- `PUT /api/settings/{key}` - Save single setting
- `DELETE /api/settings/{key}` - Delete setting

## Settings Implementation Status

### ✅ IMPLEMENTED Settings

#### 1. autoApprovePortalRegistrations (BOOLEAN)
- **Default**: `false`
- **File**: `PortalUserService.java:72`
- **Impact**:
  - When `true`: New portal registrations are automatically approved with `APPROVED` status and `isActive=true`
  - When `false`: Registrations require email verification (`PENDING_VERIFICATION` status)
- **Email Behavior**:
  - Auto-approved users receive welcome email immediately
  - Non-auto-approved users receive verification email
- **Member Verification**: Auto-approved users have `member.isVerified=true`

---

---

### ✅ Church Logo Upload Feature

**Status**: ✅ **COMPLETED**

#### Backend Implementation:
- **File**: `ChurchController.java`
- **Endpoints**:
  - `POST /api/churches/{id}/logo` - Upload church logo (max 2MB)
  - `DELETE /api/churches/{id}/logo` - Delete church logo
  - `GET /api/churches/{id}` - Get church with logo URL
  - `PUT /api/churches/{id}` - Update church profile

- **File**: `ChurchService.java` (NEW)
  - `uploadLogo()` - Handles logo upload with validation and compression
  - `deleteLogo()` - Removes logo and updates church
  - `getChurchById()`, `updateChurch()` - Church CRUD operations

- **File**: `Church.java:35`
  - Added `logoUrl` field to store church logo path

- **Storage**: Uses `ImageService` for compression (500KB max, reuses fellowship-dir)

#### Frontend Implementation:
- **File**: `settings-page.html:168-212`
  - Logo upload section in Church Profile tab
  - Preview with delete button when logo exists
  - Upload button with file validation

- **File**: `settings-page.ts:31,51,382-486`
  - `isUploadingLogo` signal for upload state
  - `churchForm.logoUrl` field
  - `onLogoFileSelected()` - Validates file type/size (2MB max)
  - `uploadLogo()` - Uploads via FormData
  - `deleteLogo()` - Deletes with confirmation
  - `getLogoUrl()` - Returns full URL for display

- **File**: `settings-page.css:685-779`
  - Logo section styling with preview/placeholder
  - Upload controls and danger button styles

- **File**: `side-nav-component.ts:48,66,76,240-265`
  - `churchLogoUrl` property
  - `loadChurchLogo()` - Fetches church data and extracts logo
  - Loads on init and route changes

- **File**: `side-nav-component.html:10-18`
  - Displays church logo in sidebar header if available
  - Falls back to default heart icon

- **File**: `side-nav-component.css:57-73`
  - `.logo-image` styling (40x40px, white background, rounded)

#### Features:
- ✅ Upload logo from settings page
- ✅ Image validation (type, size)
- ✅ Automatic compression via ImageService
- ✅ Logo preview in settings
- ✅ Delete logo functionality
- ✅ Display logo in app sidebar/header
- ✅ Fallback to default icon when no logo

---

### ⏳ NOT YET IMPLEMENTED Settings

#### 2. requirePhotoForMembers (BOOLEAN)
- **Default**: `false`
- **Proposed Impact**:
  - Should validate member creation/update forms require photo upload
  - Frontend should show photo field as required/optional based on setting
  - Backend validation in `MemberService`
- **Files to Modify**:
  - `MemberService.java` - Add validation
  - `members-page.ts` - Make photo field required conditionally

#### 3. enablePublicPortal (BOOLEAN)
- **Default**: `true`
- **Proposed Impact**:
  - Control whether public portal routes are accessible
  - Could be implemented as security filter/interceptor
  - Public pages: registration, login, password reset
- **Files to Modify**:
  - `SecurityConfig.java` - Add conditional public access
  - Portal frontend routes

#### 4. allowSelfCheckIn (BOOLEAN)
- **Default**: `true`
- **Proposed Impact**:
  - Control whether members can check themselves in via QR codes
  - Affects check-in endpoints and QR code functionality
- **Files to Modify**:
  - `CheckInController.java`
  - `AttendanceService.java`

#### 5. defaultEventDuration (NUMBER)
- **Default**: `120` (minutes)
- **Proposed Impact**:
  - Pre-fill event duration when creating new events
  - Frontend: Use this value as default in event form
- **Files to Modify**:
  - `events-page.ts` - Load and use as default duration

#### 6. attendanceGracePeriod (NUMBER)
- **Default**: `15` (minutes)
- **Proposed Impact**:
  - Allow attendance check-in X minutes before/after event start
  - Validate check-in times against this grace period
- **Files to Modify**:
  - `AttendanceService.java`
  - `CheckInController.java`

### 📧 Notification Settings (Not Direct App Impact)

These settings control notification behavior but don't affect core functionality:

#### 7. emailNotifications (BOOLEAN) - Default: `true`
#### 8. smsNotifications (BOOLEAN) - Default: `true`
#### 9. eventReminders (BOOLEAN) - Default: `true`
#### 10. birthdayReminders (BOOLEAN) - Default: `true`
#### 11. anniversaryReminders (BOOLEAN) - Default: `true`
#### 12. donationReceipts (BOOLEAN) - Default: `true`
#### 13. attendanceReports (BOOLEAN) - Default: `false`

**Proposed Impact**: These should be checked before sending respective notifications/emails

**Files to Modify**:
- `EventReminderService.java` - Check `eventReminders` before sending
- `EmailService.java` - Check settings before each notification type
- `SmsService.java` - Check `smsNotifications` before sending
- Scheduled tasks that send birthday/anniversary reminders

---

## Implementation Priorities

### High Priority (Core Functionality)
1. ✅ `autoApprovePortalRegistrations` - **COMPLETED**
2. ⏳ `allowSelfCheckIn` - Affects attendance feature
3. ⏳ `attendanceGracePeriod` - Affects attendance validation

### Medium Priority (User Experience)
4. ⏳ `defaultEventDuration` - Improves event creation UX
5. ⏳ `requirePhotoForMembers` - Data quality control
6. ⏳ `enablePublicPortal` - Security/privacy feature

### Low Priority (Nice to Have)
7. ⏳ Notification settings - Currently notifications may not be fully implemented

---

## How to Implement Remaining Settings

### Example: Implementing `allowSelfCheckIn`

1. **Inject ChurchSettingsService** in relevant service:
```java
private final ChurchSettingsService churchSettingsService;
```

2. **Check setting before operation**:
```java
public void checkIn(Long churchId, CheckInRequest request) {
    boolean allowSelfCheckIn = churchSettingsService.getBooleanSetting(
        churchId,
        "allowSelfCheckIn",
        true  // default value
    );

    if (!allowSelfCheckIn && request.isSelfCheckIn()) {
        throw new IllegalStateException("Self check-in is disabled for this church");
    }

    // Continue with check-in logic...
}
```

3. **Frontend**: Load setting and disable/hide self check-in UI
```typescript
ngOnInit() {
  this.settingsService.getSetting('allowSelfCheckIn').subscribe(enabled => {
    this.allowSelfCheckIn = enabled === 'true';
  });
}
```

---

## Testing Checklist

### ✅ Completed Tests
- [x] Migration V68 applied successfully
- [x] Settings API compiles
- [x] `autoApprovePortalRegistrations` setting works

### ⏳ Pending Tests
- [ ] Test settings API endpoints with actual data
- [ ] E2E test: Portal registration with auto-approve ON
- [ ] E2E test: Portal registration with auto-approve OFF
- [ ] E2E test: Settings CRUD operations
- [ ] Test other settings when implemented

---

## Notes
- All settings use string storage in database with type metadata
- Type conversion happens in `ChurchSettingsService` via type-safe getters
- Settings are church-scoped (multi-tenant)
- Frontend loads settings via `/api/settings` on page init
- Settings changes take effect immediately (no cache/restart required)
