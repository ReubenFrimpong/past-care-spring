
# PastCare - Detailed Implementation TODOs

**Purpose**: Granular task breakdown for each module with edge cases and practical implementation steps
**Last Updated**: 2025-12-20

---

## 📌 Legend
- ⏳ **Not Started**
- 🔄 **In Progress**
- ✅ **Completed**
- 🔴 **Blocked**
- ⚠️ **Needs Review**

---

# MODULE 1: MEMBERS

## Phase 1: Critical Fixes & International Support (2-3 weeks)

### 1.1 Fix Spouse Validation Bug ✅ COMPLETED
**Priority**: CRITICAL | **Estimate**: 4 hours | **Actual**: 0.5 hours

#### Backend Tasks
- [x] Update `MemberService.createMember()` validation logic
  - [x] Check if `maritalStatus == "married"` OR `maritalStatus == "MARRIED"`
  - [x] If married and `spouseName` is null/empty/blank, throw `ValidationException`
  - [x] Error message: "Spouse name is required for married members"
  - [x] Handle case-insensitive marital status check
- [x] Update `MemberService.updateMember()` with same validation
  - [x] Apply validation only if marital status changed to married
  - [x] Allow updates if already married and spouse exists
- [x] Created helper method `validateSpouseRequirement()` for reusability
- [ ] Add unit test: `testCreateMarriedMemberWithoutSpouse_ShouldThrowException()`
- [ ] Add unit test: `testCreateMarriedMemberWithSpouse_ShouldSucceed()`
- [ ] Add unit test: `testUpdateToMarriedWithoutSpouse_ShouldThrowException()`
- [ ] Add unit test: `testUpdateMarriedMemberKeepingSpouse_ShouldSucceed()`

#### Frontend Tasks
- [ ] Add reactive validation to member form
  - [ ] Watch `maritalStatus` field changes
  - [ ] If "married" selected, mark `spouseName` as required
  - [ ] Show red asterisk on spouse name label
  - [ ] Disable submit if married and spouse empty
- [ ] Add validation error display
  - [ ] Show error message below spouse name field
  - [ ] "Spouse name is required when marital status is married"
  - [ ] Style with red text and error icon
- [ ] Handle API validation errors
  - [ ] Catch 400 Bad Request with validation error
  - [ ] Display error in toast notification
  - [ ] Highlight spouse name field in red

#### E2E Tests
- [ ] Test: Create married member without spouse name
  - [ ] Fill form with marital status "Married"
  - [ ] Leave spouse name empty
  - [ ] Click submit
  - [ ] Assert validation error displayed
  - [ ] Assert form not submitted
- [ ] Test: Create married member with spouse name
  - [ ] Fill form with marital status "Married"
  - [ ] Enter spouse name
  - [ ] Submit form
  - [ ] Assert member created successfully
  - [ ] Assert spouse name saved
- [ ] Test: Update single to married without spouse
  - [ ] Edit existing single member
  - [ ] Change marital status to "Married"
  - [ ] Leave spouse name empty
  - [ ] Assert validation error
- [ ] Test: Update married member keeping spouse
  - [ ] Edit married member with spouse
  - [ ] Change only phone number
  - [ ] Assert update succeeds
  - [ ] Assert spouse name preserved

**Edge Cases**:
- Case-insensitive marital status ("Married", "MARRIED", "married")
- Empty string vs null spouse name
- Whitespace-only spouse name
- Special characters in spouse name
- Very long spouse name (>255 chars)
- Changing from married to single (should spouse name be cleared?)

---

### 1.2 Fix Profile Image Preservation Bug ✅ COMPLETED
**Priority**: CRITICAL | **Estimate**: 6 hours | **Actual**: 0.3 hours

#### Backend Tasks
- [x] Update `MemberService.updateMember()` method
  - [x] Check if `memberRequest.getProfileImageUrl()` is null
  - [x] If null, preserve existing `member.getProfileImageUrl()`
  - [x] Only update image if new URL provided
  - [x] Add explicit null check before setting
- [ ] Add unit test: `testUpdateMemberWithoutImage_ShouldPreserveExistingImage()`
  - [ ] Create member with image URL
  - [ ] Update with request having null imageUrl
  - [ ] Assert existing image preserved
- [ ] Add unit test: `testUpdateMemberWithNewImage_ShouldUpdateImage()`
  - [ ] Create member with image URL
  - [ ] Update with new image URL
  - [ ] Assert new image saved
- [ ] Add logging for image updates
  - [ ] Log when image preserved
  - [ ] Log when image updated
  - [ ] Log when image removed

#### Frontend Tasks
- [ ] Update member form submission logic
  - [ ] Check if image file selected
  - [ ] If no new image, don't include `profileImageUrl` in request
  - [ ] OR send existing URL from loaded member
  - [ ] Track image change state separately
- [ ] Add image preview preservation
  - [ ] Show existing image when editing
  - [ ] Mark as "unchanged" if no new upload
  - [ ] Show "new image selected" indicator
- [ ] Handle image removal scenario
  - [ ] Add "Remove Image" button
  - [ ] Send explicit null or empty string
  - [ ] Confirm before removing
- [ ] Add image upload state tracking
  - [ ] States: UNCHANGED, NEW_FILE, REMOVED
  - [ ] Send appropriate value based on state

#### E2E Tests
- [ ] Test: Update member without changing image
  - [ ] Create member with profile image
  - [ ] Edit member, change name only
  - [ ] Don't upload new image
  - [ ] Submit form
  - [ ] Assert member updated
  - [ ] Assert profile image URL unchanged
  - [ ] Verify image displays correctly
- [ ] Test: Update member with new image
  - [ ] Create member with profile image
  - [ ] Edit member
  - [ ] Upload new image file
  - [ ] Submit form
  - [ ] Assert new image saved
  - [ ] Assert old image replaced
- [ ] Test: Remove member image
  - [ ] Create member with profile image
  - [ ] Edit member
  - [ ] Click "Remove Image"
  - [ ] Submit form
  - [ ] Assert image removed
  - [ ] Assert placeholder shown
- [ ] Test: Upload image then cancel edit
  - [ ] Edit member
  - [ ] Upload new image
  - [ ] Cancel form
  - [ ] Re-open member profile
  - [ ] Assert original image still present

**Edge Cases**:
- Member without existing image (null)
- Empty string vs null image URL
- Invalid image URL format
- Image upload failure mid-update
- Concurrent edits (user A uploads, user B edits)
- Large image file (compression timing)
- Corrupted image file upload
- S3/storage service unavailable

---

### 1.3 Add International Phone Validation ✅ COMPLETED
**Priority**: HIGH | **Estimate**: 12 hours | **Actual**: 3 hours

#### Backend Tasks
- [x] Add Google libphonenumber dependency to `pom.xml`
  - [x] Added version 8.13.26
- [x] Create `PhoneNumberValidator` class
  - [x] Updated `InternationalPhoneNumberValidator` to use Google libphonenumber
  - [x] Implement validation using PhoneNumberUtil
  - [x] Parse phone number with country code detection
  - [x] Return validation result with detailed error messages
  - [x] Handle parsing exceptions with specific error types
- [x] Update `@InternationalPhoneNumber` annotation
  - [x] No changes needed - annotation works with updated validator
- [x] Update `InternationalPhoneNumberValidator` class
  - [x] Integrated Google libphonenumber library
  - [x] Support all 200+ countries automatically
  - [x] Validate format and length for each country
  - [x] Use isValidNumber for comprehensive validation
- [x] Add `countryCode` field to `Member` entity
  - [x] Added VARCHAR(10) column
  - [x] Store ISO country code (e.g., "GH", "US", "NG")
  - [x] Default to "GH" for existing records
- [x] Update `MemberRequest` DTO
  - [x] Added `countryCode` field with @Pattern validation
  - [x] Pattern: `^[A-Z]{2}$` (2-letter ISO code)
  - [x] Optional field (nullable)
- [x] Create database migration
  - [x] Created `V3__add_country_code_to_member.sql`
  - [x] Add column with default 'GH'
  - [x] Update existing records to 'GH'
- [x] Update `MemberService` validation
  - [x] Added countryCode handling in createMember
  - [x] Added countryCode handling in updateMember
  - [x] Default to "GH" if not provided
  - [x] Phone validation automatically uses country code from phone number prefix
- [x] Add unit tests for each country
  - [x] Created InternationalPhoneNumberValidatorTest with 32 tests
  - [x] Test Ghana: +233 24 123 4567 ✓
  - [x] Test USA: +1 (202) 456-1111 ✓
  - [x] Test UK: +44 20 7946 0958 ✓
  - [x] Test Nigeria: +234 802 123 4567 ✓
  - [x] Test India: +91 98765 43210 ✓
  - [x] Test invalid: 123 (too short) ✓
  - [x] Test invalid: 12345678901234567 (too long) ✓
  - [x] Additional countries: Canada, Germany, France, South Africa, Kenya, Brazil, Japan, Australia ✓
  - [x] All 32 tests passing

#### Frontend Tasks
- [ ] Install `libphonenumber-js` package
  ```bash
  npm install libphonenumber-js
  ```
- [ ] Create `PhoneInputComponent`
  - [ ] Country selector dropdown with flags
  - [ ] Auto-format as user types
  - [ ] Show validation errors in real-time
  - [ ] Support copy/paste of international numbers
  - [ ] Parse and format automatically
- [ ] Add country flag icons
  - [ ] Install `flag-icons` package
  - [ ] Create flag icon component
  - [ ] Map country code to flag
- [ ] Create country selector service
  - [ ] List of all countries with codes
  - [ ] Search/filter countries
  - [ ] Popular countries at top (GH, US, UK, NG)
  - [ ] Country dial codes
- [ ] Update member form
  - [ ] Replace plain phone input with PhoneInputComponent
  - [ ] Add country selector
  - [ ] Show formatted preview
  - [ ] Validate on blur
- [ ] Add phone number formatting utility
  - [ ] Format for display (e.g., +233 24 123 4567)
  - [ ] Format for storage (E.164: +233241234567)
  - [ ] Parse pasted numbers

#### E2E Tests
- [x] ✅ Test: Country selector with flags - `e2e/members-form.spec.ts:495`
  - [x] ✅ Shows country autocomplete field
  - [x] ✅ Displays flags (🇺🇸, 🇬🇭, 🇬🇧, etc.)
  - [x] ✅ Allows selecting country from dropdown
  - [x] ✅ Updates input with selected country
- [x] ✅ Test: Ghana phone number validation - `e2e/members-form.spec.ts:516`
  - [x] ✅ Fill phone: +233241234567
  - [x] ✅ No validation error shown
  - [x] ✅ Form accepts valid Ghana format
- [x] ✅ Test: USA phone number validation - `e2e/members-form.spec.ts:529`
  - [x] ✅ Select country: United States (🇺🇸)
  - [x] ✅ Enter: +1 202-456-1111
  - [x] ✅ No validation error shown
  - [x] ✅ Form accepts valid USA format
- [x] ✅ Test: UK phone number validation - `e2e/members-form.spec.ts:548`
  - [x] ✅ Select country: United Kingdom (🇬🇧)
  - [x] ✅ Enter: +44 20 7946 0958
  - [x] ✅ No validation error shown
  - [x] ✅ Form accepts valid UK format
- [x] ✅ Test: Invalid phone number - `e2e/members-form.spec.ts:567`
  - [x] ✅ Enter: +233 12 (too short)
  - [x] ✅ Assert validation error shown
  - [x] ✅ Form cannot be submitted
- [x] ✅ Test: Default country code - `e2e/members-form.spec.ts:580`
  - [x] ✅ Form defaults to Ghana (GH)
  - [x] ✅ Shows Ghana flag 🇬🇭
- [ ] Test: Paste international number (Future enhancement)
  - [ ] Paste: +234 802 123 4567
  - [ ] Assert country auto-detected (Nigeria)
  - [ ] Assert formatted correctly
- [ ] Test: Change country with existing number (Future enhancement)
  - [ ] Enter Ghana number: 024 123 4567
  - [ ] Change country to USA
  - [ ] Assert number cleared or re-validated
- [ ] Test: Multiple phone fields (WhatsApp, emergency) (Future enhancement)
  - [ ] Each has own country selector
  - [ ] Can have different countries
  - [ ] All validate independently

**Edge Cases**:
- Leading zeros handling (some countries require, others don't)
- Extension numbers (e.g., +1-555-123-4567 ext. 123)
- Special characters in phone number
- Phone number with spaces, dashes, parentheses
- Country code change with existing number
- Duplicate phone across different countries
- Toll-free numbers (800, 888 in US)
- Short codes (SMS services)
- VoIP numbers validation
- Mobile vs landline detection
- Premium rate numbers

---

### 1.4 Add Country and Timezone Support ✅ COMPLETED
**Priority**: HIGH | **Estimate**: 10 hours | **Actual**: 1.5 hours

#### Backend Tasks
- [x] Add `timezone` field to `Member` entity
  - [x] VARCHAR(50) column for IANA timezone
  - [x] Examples: "Africa/Accra", "America/New_York", "Europe/London"
  - [x] Nullable (falls back to church timezone)
- [x] Create database migration
  - [x] Created `V4__add_timezone_to_member.sql`
  - [x] Add timezone column with default 'Africa/Accra'
  - [x] Update existing records to "Africa/Accra"
- [x] Update `MemberRequest` DTO
  - [x] Added `timezone` field (optional)
  - [x] Pattern validation for IANA format: `^[A-Za-z_]+/[A-Za-z_]+(/[A-Za-z_]+)?$`
  - [x] Supports timezones with sublocation (e.g., America/Indiana/Indianapolis)
- [x] Update `MemberService`
  - [x] Added timezone handling in createMember
  - [x] Added timezone handling in updateMember
  - [x] Default to "Africa/Accra" if not provided
- [ ] Create `TimezoneService` - **DEFERRED TO PHASE 2**
  - Note: Basic timezone storage implemented. Full service with timezone operations deferred
- [ ] Add timezone to `MemberResponse` DTO - **DEFERRED TO PHASE 2**
  - Note: Timezone field available in Member entity, DTO mapping deferred
- [ ] Update birthday/anniversary calculations - **DEFERRED TO PHASE 4**
  - Note: This will be part of Lifecycle & Communication Tracking phase
- [x] Add unit tests
  - [x] Created MemberTimezoneTest with 12 tests
  - [x] Test timezone storage and retrieval ✓
  - [x] Test IANA format validation ✓
  - [x] Test null timezone (optional field) ✓
  - [x] Test various timezone formats (Africa/Accra, America/New_York, etc.) ✓
  - [x] Test invalid formats (UTC+3, spaces, just location) ✓
  - [x] All 12 tests passing

#### Frontend Tasks
- [ ] Create `TimezoneSelectComponent`
  - [ ] Dropdown with searchable timezones
  - [ ] Group by region
  - [ ] Show current offset (UTC±X)
  - [ ] Popular timezones at top
  - [ ] Auto-detect from browser (optional)
- [ ] Add timezone to member form
  - [ ] Timezone selector in "Basic Info" or "Contact" tab
  - [ ] Optional field with default
  - [ ] Show current time in selected timezone (preview)
- [ ] Create timezone utility service
  - [ ] Parse IANA timezone names
  - [ ] Format timezone for display
  - [ ] Calculate offset
  - [ ] Detect browser timezone
- [ ] Update date/time displays
  - [ ] Show member DOB in their timezone
  - [ ] Show "local time" indicator
  - [ ] Tooltip with admin's time equivalent
- [ ] Add timezone to member profile view
  - [ ] Display timezone name and offset
  - [ ] Show current time in member's location
  - [ ] Clock icon with time

#### E2E Tests
- [x] ✅ Test: Timezone selector visible - `e2e/members-form.spec.ts:597`
  - [x] ✅ Shows timezone autocomplete field
  - [x] ✅ Displays options with UTC offsets
  - [x] ✅ Shows Accra (UTC+0) option
- [x] ✅ Test: Select timezone - `e2e/members-form.spec.ts:615`
  - [x] ✅ Open timezone dropdown
  - [x] ✅ Select: New York (EST)
  - [x] ✅ Verify selection in input
- [x] ✅ Test: Default timezone - `e2e/members-form.spec.ts:629`
  - [x] ✅ Form defaults to Africa/Accra
  - [x] ✅ Shows UTC offset
- [x] ✅ Test: Filter timezones by typing - `e2e/members-form.spec.ts:638`
  - [x] ✅ Type "London" in timezone field
  - [x] ✅ Shows London timezone option
  - [x] ✅ Can select filtered result
- [x] ✅ Test: Multiple timezone regions - `e2e/members-form.spec.ts:657`
  - [x] ✅ Dropdown shows 10+ timezone options
  - [x] ✅ Timezones from Africa, Americas, Europe, Asia regions
- [x] ✅ Test: Country and timezone persistence - `e2e/members-form.spec.ts:742`
  - [x] ✅ Create member with USA country + New York timezone
  - [x] ✅ Save and verify member created
  - [x] ✅ Re-open member edit form
  - [x] ✅ Verify country shows "United States"
  - [x] ✅ Verify timezone shows "New York"
- [ ] Test: Auto-detect timezone from browser (Future enhancement)
  - [ ] Click "Auto-detect" button
  - [ ] Assert browser timezone selected
  - [ ] Verify correct IANA name
- [ ] Test: Birthday display in different timezones (Future enhancement)
  - [ ] Create member with DOB and timezone
  - [ ] View member profile
  - [ ] Assert birthday shows in member's timezone
  - [ ] Assert tooltip shows admin's timezone equivalent

**Edge Cases**:
- Daylight Saving Time boundaries (spring forward, fall back)
- Timezones without DST (e.g., Arizona, Hawaii)
- Historical timezone changes
- Countries with multiple timezones (USA, Russia, etc.)
- Offset timezones (e.g., India UTC+5:30, Nepal UTC+5:45)
- Member traveling (temporary timezone change)
- Timezone renamed or deprecated
- UTC vs GMT disambiguation
- Birthday at midnight (which day in different zones?)
- Event scheduling across timezones

---

### 1.5 Update Location Entity for International Addresses ✅ COMPLETED
**Priority**: HIGH | **Estimate**: 8 hours | **Actual**: 2 hours

#### Backend Tasks
- [x] Update `Location` entity
  - [x] Added `countryCode` VARCHAR(2) NOT NULL DEFAULT 'GH'
  - [x] Added `countryName` VARCHAR(100) NOT NULL DEFAULT 'Ghana'
  - [x] Added `state` VARCHAR(100) - for USA, Australia, etc.
  - [x] Added `province` VARCHAR(100) - for Canada
  - [x] Added `postalCode` VARCHAR(20)
  - [x] Added `addressLine1` VARCHAR(200)
  - [x] Added `addressLine2` VARCHAR(200)
  - [x] Kept existing Ghana fields (region, district, city, suburb) for backward compatibility
- [x] Create database migration
  - [x] Created `V5__add_international_fields_to_location.sql`
  - [x] Added new columns with proper defaults
  - [x] Set defaults for existing records (GH/Ghana)
  - [x] Added indexes on countryCode and postalCode
- [ ] Update `LocationRequest` DTO - **DEFERRED TO PHASE 2**
  - Note: Location entity updated, DTO updates deferred
- [ ] Update `LocationService` - **DEFERRED TO PHASE 2**
  - Note: Basic entity structure in place, service enhancements deferred
- [x] Create address formatting utility
  - [x] Implemented getDisplayName() with country-specific logic
  - [x] Implemented getShortName() with country-specific logic
  - [x] Implemented getFullName() with formatting for:
    - [x] Ghana format: Suburb, City, District, Region ✓
    - [x] USA format: Address Line 1, Address Line 2, City, State ZIP ✓
    - [x] UK format: Address Line 1, Address Line 2, Town, County, Postcode ✓
    - [x] Canada format: Address Line 1, Address Line 2, City, Province Postal Code ✓
    - [x] Generic format for other countries ✓
- [ ] Update geocoding service - **DEFERRED TO PHASE 2**
  - Note: Geocoding service enhancements deferred
- [x] Add unit tests
  - [x] Created LocationInternationalTest with 13 tests
  - [x] Test Ghana address format (2 tests) ✓
  - [x] Test USA address format (2 tests) ✓
  - [x] Test UK address format (2 tests) ✓
  - [x] Test Canada address format (2 tests) ✓
  - [x] Test generic international addresses (Nigeria, France) ✓
  - [x] Test edge cases (default values, minimal address) ✓
  - [x] All 13 tests passing

#### Frontend Tasks
- [ ] Create `AddressFormComponent`
  - [ ] Dynamic form based on selected country
  - [ ] Show/hide fields based on country
  - [ ] Different labels per country (State vs Province)
  - [ ] Country-specific validation
- [ ] Add country selector to address form
  - [ ] Dropdown with all countries
  - [ ] Flag icons
  - [ ] Popular countries at top
  - [ ] Search functionality
- [ ] Create address templates
  - [ ] Ghana template (Suburb, City, Region, GPS)
  - [ ] USA template (Address, City, State, ZIP)
  - [ ] UK template (Address, Town, County, Postcode)
  - [ ] Canada template (Address, City, Province, Postal Code)
  - [ ] Generic template (Address, City, Country)
- [ ] Update location autocomplete
  - [ ] Pass country context to Nominatim
  - [ ] Filter results by country
  - [ ] Parse address components by country
  - [ ] Map to appropriate fields
- [ ] Add address display formatting
  - [ ] Format for display based on country
  - [ ] Multi-line address rendering
  - [ ] Include country flag
- [ ] Update map integration
  - [ ] Support international coordinates
  - [ ] Zoom to country on selection
  - [ ] Show country-appropriate map style

#### E2E Tests
- [x] ✅ Test: Location search field visible - `e2e/members-form.spec.ts:683`
  - [x] ✅ locationDisplay input field is visible
  - [x] ✅ Field is readonly (triggers search dialog)
  - [x] ✅ Search button with pi-search icon visible
- [x] ✅ Test: Location search dialog opens - `e2e/members-form.spec.ts:693`
  - [x] ✅ Click search button
  - [x] ✅ Location search dialog/map interface opens
- [x] ✅ Test: Form supports international location format - `e2e/members-form.spec.ts:710`
  - [x] ✅ Create member with international phone (+1)
  - [x] ✅ Form structure accepts international addresses
  - [x] ✅ Backend Location entity ready for int'l data
- [ ] Test: Ghana address format (Future - dynamic form needed)
  - [ ] Select country: Ghana
  - [ ] Assert fields: Suburb, City, Region, GPS
  - [ ] Fill address
  - [ ] Assert validation rules for Ghana
  - [ ] Submit and verify
- [ ] Test: USA address format
  - [ ] Select country: United States
  - [ ] Assert fields: Address Line 1, Address Line 2, City, State, ZIP
  - [ ] Fill address
  - [ ] Assert ZIP code validation (5 or 9 digits)
  - [ ] Submit and verify
- [ ] Test: UK address format
  - [ ] Select country: United Kingdom
  - [ ] Assert fields: Address Line 1, Address Line 2, Town, County, Postcode
  - [ ] Fill address
  - [ ] Assert postcode validation (UK format)
  - [ ] Submit and verify
- [ ] Test: Address autocomplete for different countries
  - [ ] Select country: United States
  - [ ] Search address: "1600 Pennsylvania"
  - [ ] Assert results filtered to USA
  - [ ] Select result
  - [ ] Assert fields populated correctly
- [ ] Test: Change country with existing address
  - [ ] Fill Ghana address
  - [ ] Change country to USA
  - [ ] Assert form clears or asks to confirm
  - [ ] Assert new country fields shown
- [ ] Test: GPS coordinates for international addresses
  - [ ] Enter international address
  - [ ] Click "Get GPS"
  - [ ] Assert coordinates retrieved
  - [ ] Assert map centered correctly

**Edge Cases**:
- Countries without states/provinces
- Countries with long postal codes (e.g., UK: SW1A 1AA)
- Countries without postal codes
- Address Line 1 required, Line 2 optional
- P.O. Box addresses
- Military addresses (APO, FPO)
- Unicode characters in addresses (Chinese, Arabic, etc.)
- Very long street names (>200 chars)
- Apartment/Unit numbers in different countries
- Rural addresses without street names
- Geocoding failure or rate limits
- Multiple locations with same address (apartments)

---

### 1.6 Extract Methods to Services (Refactoring) ✅ COMPLETED
**Priority**: MEDIUM | **Estimate**: 4 hours | **Actual**: 2 hours

#### Backend Tasks
- [x] Extract `uploadProfileImage` from `MembersController`
  - [x] ✅ **Already Extracted**: ImageService already exists with all upload logic
  - [x] ✅ Logic in `uploadMemberProfileImage(Long memberId, MultipartFile image)` in MemberService
  - [x] ✅ Returns uploaded image URL via ProfileImageUploadResponse
  - [x] ✅ Handles image validation in ImageService
  - [x] ✅ Handles image compression in ImageService (Thumbnailator)
  - [x] ✅ Controller calls MemberService which delegates to ImageService
  - [x] ✅ ImageService unit tests already exist
- [x] Extract `extractChurchIdFromRequest` from `MembersController`
  - [x] ✅ Created `RequestContextUtil` utility class
  - [x] ✅ Moved JWT extraction logic to `extractChurchId(HttpServletRequest request)`
  - [x] ✅ Added `extractUserId(HttpServletRequest request)` method
  - [x] ✅ Handles both cookie and Authorization header token extraction
  - [x] ✅ Throws IllegalStateException if no token found
  - [x] ✅ Updated MembersController to use utility
  - [x] ✅ Added 10 comprehensive unit tests for RequestContextUtil
- [x] Review other controllers for similar extractions
  - [x] ✅ Checked all controllers - only MembersController had this pattern
  - [x] ✅ No other controllers needed extraction
- [x] Update service layer documentation
  - [x] ✅ Added JavaDoc comments to RequestContextUtil
  - [x] ✅ Documented all parameters and return types
  - [x] ✅ Documented exceptions thrown (IllegalStateException)
  - [x] ✅ Added usage examples in comments

#### Testing Tasks
- [x] Unit test: `ImageService.uploadMemberProfileImage()`
  - [x] ✅ Tests already exist from previous implementation
- [x] Unit test: `RequestContextUtil.extractChurchId()`
  - [x] ✅ Test extraction from cookie - testExtractChurchIdFromCookie()
  - [x] ✅ Test extraction from header - testExtractChurchIdFromAuthorizationHeader()
  - [x] ✅ Test missing token - testExtractChurchIdNoToken()
  - [x] ✅ Test invalid header format - testExtractChurchIdInvalidAuthorizationHeaderFormat()
  - [x] ✅ Test empty cookies - testExtractChurchIdEmptyCookies()
  - [x] ✅ Test wrong cookie name - testExtractChurchIdWrongCookieName()
  - [x] ✅ Test cookie precedence - testCookieTakesPrecedenceOverHeader()
  - [x] ✅ Test extractUserId from cookie - testExtractUserIdFromCookie()
  - [x] ✅ Test extractUserId from header - testExtractUserIdFromAuthorizationHeader()
  - [x] ✅ Test extractUserId no token - testExtractUserIdNoToken()
- [x] Integration test: Verify controllers use new services
  - [x] ✅ All tests pass (68 tests total)
  - [x] ✅ Backend compiles successfully
  - [x] ✅ Behavior unchanged from refactoring

**Edge Cases**:
- Multiple file uploads (batch processing)
- Concurrent uploads from same user
- Service method called with null parameters
- Transaction rollback scenarios
- Cache invalidation after upload

---

## Phase 2: Quick Operations & Bulk Management (2-3 weeks)

### 2.1 Quick Add Member Workflow ⏳
**Priority**: HIGH | **Estimate**: 8 hours

#### Backend Tasks
- [ ] Create `MemberQuickAddRequest` DTO
  - [ ] Required: firstName (NotBlank, max 100 chars)
  - [ ] Required: lastName (NotBlank, max 100 chars)
  - [ ] Required: phoneNumber (InternationalPhoneNumber)
  - [ ] Required: sex (NotBlank, enum: MALE, FEMALE)
  - [ ] Optional: countryCode (default from church)
  - [ ] Optional: tags (e.g., "visitor", "first-timer")
- [ ] Create `POST /api/members/quick-add` endpoint
  - [ ] Validate request
  - [ ] Create member with minimal fields
  - [ ] Set default status: VISITOR
  - [ ] Set profileCompleteness: 25%
  - [ ] Generate unique member ID
  - [ ] Return MemberResponse
- [ ] Update `MemberService`
  - [ ] Add `quickAddMember(MemberQuickAddRequest)` method
  - [ ] Set default values for optional fields
  - [ ] Mark as "quick add" for later completion
  - [ ] Send welcome SMS if phone provided
- [ ] Add validation
  - [ ] Check for duplicate phone number
  - [ ] Suggest existing member if match found
  - [ ] Allow override with confirmation
- [ ] Add unit tests
  - [ ] Test quick add with valid data
  - [ ] Test duplicate phone detection
  - [ ] Test missing required fields
  - [ ] Test default values set correctly

#### Frontend Tasks
- [ ] Create `QuickAddDialogComponent`
  - [ ] Lightweight form with 4 fields only
  - [ ] Large, easy-to-tap inputs (mobile-friendly)
  - [ ] Auto-focus on first name field
  - [ ] Tab navigation between fields
  - [ ] Enter key to submit
- [ ] Add "Quick Add" button to members page
  - [ ] Prominent placement (top right)
  - [ ] Keyboard shortcut: Ctrl+Q or Cmd+Q
  - [ ] Icon: lightning bolt or plus-circle
- [ ] Implement quick add flow
  - [ ] Open dialog
  - [ ] Fill minimal fields
  - [ ] Submit via Enter or button
  - [ ] Show success message
  - [ ] Option: "Add Another" or "Close"
  - [ ] Option: "Complete Profile" (redirect to full form)
- [ ] Add duplicate detection UI
  - [ ] On phone number blur, check for duplicates
  - [ ] Show warning banner if match found
  - [ ] "This number exists: John Doe (Member)"
  - [ ] Buttons: "View Existing" | "Add Anyway"
- [ ] Add session persistence
  - [ ] Save form data in localStorage
  - [ ] Restore on page refresh
  - [ ] Clear after successful submit

#### E2E Tests
- [ ] Test: Quick add member successfully
  - [ ] Click "Quick Add" button
  - [ ] Fill: First Name, Last Name, Phone, Sex
  - [ ] Click "Add & Close"
  - [ ] Assert success message shown
  - [ ] Assert member appears in list
  - [ ] Assert status is VISITOR
  - [ ] Assert profile completeness ~25%
- [ ] Test: Quick add with "Add Another"
  - [ ] Click "Quick Add"
  - [ ] Fill form
  - [ ] Click "Add & Add Another"
  - [ ] Assert success message
  - [ ] Assert form clears
  - [ ] Assert dialog remains open
  - [ ] Add second member
  - [ ] Assert both members in list
- [ ] Test: Quick add with duplicate phone
  - [ ] Create member with phone: +233 24 123 4567
  - [ ] Click "Quick Add"
  - [ ] Enter same phone number
  - [ ] Blur phone field
  - [ ] Assert duplicate warning shown
  - [ ] Assert "View Existing" button appears
  - [ ] Click "View Existing"
  - [ ] Assert navigates to existing member profile
- [ ] Test: Quick add keyboard shortcuts
  - [ ] Press Ctrl+Q
  - [ ] Assert quick add dialog opens
  - [ ] Fill fields using Tab navigation
  - [ ] Press Enter to submit
  - [ ] Assert member added
- [ ] Test: Quick add validation errors
  - [ ] Open quick add dialog
  - [ ] Leave first name empty
  - [ ] Try to submit
  - [ ] Assert validation error shown
  - [ ] Assert submit disabled
  - [ ] Fill all required fields
  - [ ] Assert submit enabled
- [ ] Test: Complete profile from quick add
  - [ ] Quick add member
  - [ ] Click "Complete Profile" in success message
  - [ ] Assert redirects to full member form
  - [ ] Assert pre-filled with quick add data
  - [ ] Fill remaining fields
  - [ ] Submit
  - [ ] Assert profile completeness increased

**Edge Cases**:
- Extremely long names (>100 chars)
- Names with special characters (é, ñ, ü, etc.)
- Single-letter names
- Same phone number but different country code
- Phone number with extension
- Adding during server downtime (offline queue)
- Rapid successive quick adds (rate limiting)
- Browser auto-fill interference
- Mobile keyboard issues (autocorrect, autocomplete)
- Accidental double-click on submit

---

### 2.2 CSV/Excel Bulk Import ⏳
**Priority**: HIGH | **Estimate**: 16 hours

#### Backend Tasks
- [ ] Add Apache POI dependency for Excel parsing
  ```xml
  <dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
  </dependency>
  ```
- [ ] Add OpenCSV dependency for CSV parsing
  ```xml
  <dependency>
    <groupId>com.opencsv</groupId>
    <artifactId>opencsv</artifactId>
    <version>5.9</version>
  </dependency>
  ```
- [ ] Create `BulkImportRequest` DTO
  - [ ] MultipartFile file
  - [ ] boolean updateExisting (default: false)
  - [ ] boolean skipDuplicates (default: true)
  - [ ] boolean sendWelcomeMessage (default: false)
  - [ ] Map<String, String> columnMapping (CSV header to field mapping)
- [ ] Create `BulkImportResponse` DTO
  - [ ] int totalRows
  - [ ] int imported
  - [ ] int updated
  - [ ] int failed
  - [ ] List<ImportError> errors
    - [ ] int rowNumber
    - [ ] String fieldName
    - [ ] String errorMessage
    - [ ] String rowData
- [ ] Create `ImportError` class
  - [ ] Track row number, field, error message
  - [ ] Include raw row data for debugging
- [ ] Create `BulkImportService`
  - [ ] `parseCsv(MultipartFile)` - parse CSV file
  - [ ] `parseExcel(MultipartFile)` - parse XLSX file
  - [ ] `validateRow(Map<String, String> row)` - validate single row
  - [ ] `importMembers(List<Map<String, String>> rows, BulkImportOptions)` - import validated rows
  - [ ] Handle duplicates based on phone number
  - [ ] Batch insert for performance (100 rows at a time)
  - [ ] Transaction management (rollback on failure)
- [ ] Create `POST /api/members/bulk-import` endpoint
  - [ ] Accept multipart/form-data
  - [ ] Validate file type (.csv or .xlsx)
  - [ ] Validate file size (<10MB)
  - [ ] Parse file
  - [ ] Validate all rows
  - [ ] Import valid rows
  - [ ] Return detailed response
- [ ] Add column mapping support
  - [ ] Auto-detect standard headers (First Name, Last Name, Phone, etc.)
  - [ ] Allow custom mapping via request
  - [ ] Case-insensitive header matching
  - [ ] Trimming whitespace in headers
- [ ] Add data validation per row
  - [ ] Required fields (firstName, lastName, phoneNumber, sex)
  - [ ] Phone number format (with country code)
  - [ ] Email format (if provided)
  - [ ] Date format for DOB (if provided)
  - [ ] Enum values (sex, maritalStatus)
  - [ ] String length limits
- [ ] Add duplicate handling
  - [ ] Check by phone number
  - [ ] If updateExisting=true, update member
  - [ ] If skipDuplicates=true, skip and log
  - [ ] If both false, fail with error
- [ ] Add background processing for large files
  - [ ] Queue import job
  - [ ] Process asynchronously
  - [ ] Send email when complete
  - [ ] Provide job status endpoint
- [ ] Add unit tests
  - [ ] Test CSV parsing
  - [ ] Test Excel parsing
  - [ ] Test validation logic
  - [ ] Test duplicate handling
  - [ ] Test batch insert
  - [ ] Test error collection

#### Frontend Tasks
- [ ] Create `BulkImportWizardComponent` (multi-step)
  - [ ] Step 1: Upload File
  - [ ] Step 2: Map Columns
  - [ ] Step 3: Preview & Validate
  - [ ] Step 4: Import & Results
- [ ] Step 1: Upload File
  - [ ] Drag-and-drop file upload
  - [ ] File type restriction (.csv, .xlsx)
  - [ ] File size validation (max 10MB)
  - [ ] Show file name, size, row count
  - [ ] Download sample template button
  - [ ] "Next" button to proceed
- [ ] Step 2: Map Columns
  - [ ] Show CSV/Excel headers
  - [ ] Dropdown for each header to map to member field
  - [ ] Auto-suggest mapping (fuzzy match)
  - [ ] Required field indicators
  - [ ] Preview first 3 rows with mapping
  - [ ] "Back" and "Next" buttons
- [ ] Step 3: Preview & Validate
  - [ ] Show first 10 rows with mapped data
  - [ ] Run validation on all rows
  - [ ] Show error count and warnings
  - [ ] Expandable error list
  - [ ] Option to download errors as CSV
  - [ ] Import options checkboxes:
    - [ ] Update existing members
    - [ ] Skip duplicates
    - [ ] Send welcome messages
  - [ ] "Import" button
- [ ] Step 4: Import & Results
  - [ ] Show progress bar during import
  - [ ] Show results summary
  - [ ] Total rows, imported, updated, failed
  - [ ] Error details table
  - [ ] Download error report button
  - [ ] "Import Another File" or "Close" buttons
- [ ] Create sample CSV template
  - [ ] Headers: First Name, Last Name, Phone, Sex, Email, DOB, Marital Status, Occupation
  - [ ] 2-3 sample rows with valid data
  - [ ] Download as CSV
- [ ] Add bulk import button to members page
  - [ ] Icon: upload or file-arrow-up
  - [ ] Opens wizard in dialog

#### E2E Tests
- [ ] Test: Import valid CSV file
  - [ ] Create CSV with 10 members
  - [ ] Click "Bulk Import"
  - [ ] Upload CSV file
  - [ ] Assert Step 2: columns auto-mapped
  - [ ] Click "Next"
  - [ ] Assert Step 3: preview shows 10 rows, 0 errors
  - [ ] Click "Import"
  - [ ] Assert Step 4: 10 imported, 0 failed
  - [ ] Assert all 10 members in members list
- [ ] Test: Import Excel file
  - [ ] Create XLSX with 5 members
  - [ ] Upload file
  - [ ] Follow wizard
  - [ ] Assert 5 members imported
- [ ] Test: Import with validation errors
  - [ ] Create CSV with invalid data:
    - [ ] Row 2: Missing last name
    - [ ] Row 5: Invalid phone number
    - [ ] Row 8: Invalid sex value
  - [ ] Upload and proceed to Step 3
  - [ ] Assert 3 errors shown
  - [ ] Assert error details display row number and issue
  - [ ] Click "Download Errors"
  - [ ] Assert CSV downloaded with error rows
  - [ ] Abort import
- [ ] Test: Import with duplicates (skip mode)
  - [ ] Create 3 existing members
  - [ ] Create CSV with 5 members (3 duplicates, 2 new)
  - [ ] Upload and map columns
  - [ ] Check "Skip duplicates"
  - [ ] Import
  - [ ] Assert 2 imported, 3 skipped, 0 failed
  - [ ] Assert skipped members not created
- [ ] Test: Import with duplicates (update mode)
  - [ ] Create 3 existing members
  - [ ] Create CSV with same 3 members (updated data)
  - [ ] Check "Update existing members"
  - [ ] Import
  - [ ] Assert 0 imported, 3 updated, 0 failed
  - [ ] Assert existing members data updated
- [ ] Test: Column mapping customization
  - [ ] Create CSV with non-standard headers
    - [ ] "Given Name" instead of "First Name"
    - [ ] "Family Name" instead of "Last Name"
  - [ ] Upload file
  - [ ] In Step 2, manually map columns
  - [ ] Proceed and import
  - [ ] Assert members imported with correct field mapping
- [ ] Test: Large file import (1000 rows)
  - [ ] Create CSV with 1000 members
  - [ ] Upload and import
  - [ ] Assert progress bar shows during import
  - [ ] Assert all 1000 imported successfully
  - [ ] Assert performance acceptable (<2 min)
- [ ] Test: Download sample template
  - [ ] Click "Bulk Import"
  - [ ] Click "Download Sample Template"
  - [ ] Assert CSV file downloaded
  - [ ] Open file and verify headers and sample data

**Edge Cases**:
- Empty CSV file (0 rows)
- CSV with only headers (no data rows)
- CSV with extra columns (unmapped)
- CSV with missing columns (required fields)
- CSV with non-UTF-8 encoding
- CSV with different delimiters (comma, semicolon, tab)
- Excel with multiple sheets (use first sheet only)
- Excel with formulas (resolve to values)
- Excel with merged cells
- Excel with formatting (colors, borders - ignore)
- Phone numbers in different formats (with/without +, spaces, dashes)
- Date formats (MM/DD/YYYY vs DD/MM/YYYY ambiguity)
- Boolean values (true/false vs yes/no vs 1/0)
- Empty cells vs cells with whitespace
- Very long strings (truncate or error)
- Special characters in CSV (quotes, commas in data)
- Duplicate phone numbers within same import file
- Import timeout for very large files
- Memory issues with huge files (stream processing)
- Concurrent imports from multiple users
- Network interruption during import
- Database deadlock during batch insert

---

### 2.3 Bulk Update Operations ⏳
**Priority**: HIGH | **Estimate**: 10 hours

#### Backend Tasks
- [ ] Create `BulkUpdateRequest` DTO
  - [ ] List<Long> memberIds (required, max 1000 IDs)
  - [ ] Map<String, Object> updates (field -> new value)
  - [ ] Supported fields:
    - [ ] fellowshipIds (add/remove fellowships)
    - [ ] tags (add/remove tags)
    - [ ] status (change lifecycle status)
    - [ ] isVerified (bulk verify)
    - [ ] Any other updateable field
- [ ] Create `BulkUpdateResponse` DTO
  - [ ] int updated (successful updates)
  - [ ] int failed (failed updates)
  - [ ] List<BulkUpdateError> errors
    - [ ] Long memberId
    - [ ] String errorMessage
- [ ] Create `PATCH /api/members/bulk-update` endpoint
  - [ ] Validate request
  - [ ] Max 1000 members per request
  - [ ] Validate field names (whitelist)
  - [ ] Validate field values
  - [ ] Update members in transaction
  - [ ] Return response with results
- [ ] Update `MemberService`
  - [ ] Add `bulkUpdate(BulkUpdateRequest)` method
  - [ ] Fetch all members by IDs
  - [ ] Validate each member exists
  - [ ] Apply updates to each member
  - [ ] Handle errors gracefully (skip failing members)
  - [ ] Use batch update for performance
  - [ ] Log all bulk update operations
- [ ] Add validation
  - [ ] Check user has permission to update all members
  - [ ] Validate field names against whitelist
  - [ ] Validate field values (type, format, constraints)
  - [ ] Check member exists before update
  - [ ] Prevent updating immutable fields (createdAt, id, etc.)
- [ ] Add audit logging
  - [ ] Log user who performed bulk update
  - [ ] Log timestamp
  - [ ] Log number of members affected
  - [ ] Log fields updated and new values
  - [ ] Log any errors
- [ ] Add unit tests
  - [ ] Test bulk update fellowships
  - [ ] Test bulk update tags
  - [ ] Test bulk update status
  - [ ] Test with some invalid member IDs
  - [ ] Test field validation
  - [ ] Test permission check
  - [ ] Test max limit enforcement (1000 members)

#### Frontend Tasks
- [ ] Add multi-select to members list
  - [ ] Checkbox in each member card/row
  - [ ] "Select All" checkbox in header
  - [ ] "Select Filtered" (select current page/filter results)
  - [ ] "Clear Selection" button
  - [ ] Selected count badge
- [ ] Create `BulkActionsToolbar` component
  - [ ] Appears when members selected
  - [ ] Shows selected count
  - [ ] Buttons:
    - [ ] "Add to Fellowship"
    - [ ] "Remove from Fellowship"
    - [ ] "Add Tags"
    - [ ] "Remove Tags"
    - [ ] "Change Status"
    - [ ] "Mark as Verified"
    - [ ] "Delete" (soft delete)
  - [ ] Sticky position (always visible)
- [ ] Create bulk action dialogs
  - [ ] `BulkAddFellowshipDialog`
    - [ ] Fellowship multi-select
    - [ ] Confirm button
  - [ ] `BulkAddTagsDialog`
    - [ ] Tag input (create or select existing)
    - [ ] Confirm button
  - [ ] `BulkChangeStatusDialog`
    - [ ] Status dropdown
    - [ ] Reason textarea (optional)
    - [ ] Confirm button
- [ ] Implement bulk update flow
  - [ ] Select members
  - [ ] Click bulk action button
  - [ ] Open appropriate dialog
  - [ ] Fill form
  - [ ] Click confirm
  - [ ] Show loading indicator
  - [ ] Send bulk update request
  - [ ] Show results in toast
  - [ ] Refresh members list
  - [ ] Clear selection
- [ ] Add progress indicator
  - [ ] Show "Updating X members..."
  - [ ] Progress bar if large selection
  - [ ] Success/error count in real-time
- [ ] Add undo functionality (optional)
  - [ ] Show "Undo" button in success toast
  - [ ] Revert bulk update within 10 seconds
  - [ ] Disable after timeout

#### E2E Tests
- [ ] Test: Bulk add to fellowship
  - [ ] Select 5 members
  - [ ] Click "Add to Fellowship"
  - [ ] Select "Youth Fellowship"
  - [ ] Click "Confirm"
  - [ ] Assert success toast: "5 members added to Youth Fellowship"
  - [ ] Open each member profile
  - [ ] Assert "Youth Fellowship" appears in fellowships list
- [ ] Test: Bulk add tags
  - [ ] Select 10 members
  - [ ] Click "Add Tags"
  - [ ] Enter tags: "active", "leader"
  - [ ] Confirm
  - [ ] Assert success toast
  - [ ] Filter members by tag "active"
  - [ ] Assert 10 members shown
- [ ] Test: Bulk change status
  - [ ] Select 8 members with status VISITOR
  - [ ] Click "Change Status"
  - [ ] Select status: MEMBER
  - [ ] Confirm
  - [ ] Assert success toast
  - [ ] Filter by status MEMBER
  - [ ] Assert 8 members appear
- [ ] Test: Bulk update with errors
  - [ ] Select 5 members (3 valid, 2 deleted/invalid IDs)
  - [ ] Perform bulk update
  - [ ] Assert partial success toast: "3 updated, 2 failed"
  - [ ] Click "View Errors"
  - [ ] Assert error details shown for 2 failed members
- [ ] Test: Select all filtered members
  - [ ] Filter members by fellowship: "Youth"
  - [ ] Assert 15 members shown
  - [ ] Click "Select All"
  - [ ] Assert all 15 selected
  - [ ] Perform bulk update
  - [ ] Assert 15 members updated
- [ ] Test: Bulk update permission check
  - [ ] Login as FELLOWSHIP_HEAD
  - [ ] Select members from different fellowships
  - [ ] Attempt bulk update
  - [ ] Assert error: "No permission to update some members"
  - [ ] Assert only permitted members updated
- [ ] Test: Bulk update max limit
  - [ ] Select 1001 members (if database has enough)
  - [ ] Attempt bulk update
  - [ ] Assert error: "Maximum 1000 members per bulk update"
  - [ ] OR process in batches automatically

**Edge Cases**:
- Selecting members across multiple pages
- Refreshing page with active selection (lost)
- Member deleted during selection
- Member updated by another user during bulk update
- Network timeout during bulk update
- Conflicting updates (add fellowship A, remove fellowship A)
- Invalid field values in bulk update
- Bulk update with no members selected
- Concurrent bulk updates from multiple users
- Very large selection (10,000+ members)
- Bulk update with validation errors (some members invalid)
- Undo after page refresh (lost context)
- Bulk update rollback on error (all or nothing vs partial)

---

### 2.4 Soft Delete with Archive ⏳
**Priority**: MEDIUM | **Estimate**: 8 hours

#### Backend Tasks
- [ ] Add archive fields to `Member` entity
  - [ ] `archived` Boolean DEFAULT false
  - [ ] `archivedAt` LocalDateTime (nullable)
  - [ ] `archivedBy` User (ManyToOne, nullable)
  - [ ] `archiveReason` String (TEXT, nullable)
- [ ] Create database migration
  - [ ] `V6__add_archive_fields_to_member.sql`
  - [ ] Add archive columns
  - [ ] Create index on archived column
- [ ] Update `MemberRepository`
  - [ ] Add default filter to exclude archived members
  - [ ] `findByChurchAndArchivedFalse(Church, Pageable)`
  - [ ] Add query to get archived members: `findByChurchAndArchivedTrue(Church, Pageable)`
- [ ] Create `MemberArchiveRequest` DTO
  - [ ] List<Long> memberIds
  - [ ] String archiveReason (required, max 500 chars)
- [ ] Create `DELETE /api/members/bulk-delete` endpoint
  - [ ] Soft delete (set archived=true)
  - [ ] Set archivedAt to current timestamp
  - [ ] Set archivedBy to current user
  - [ ] Set archiveReason from request
  - [ ] Return count of archived members
- [ ] Create `POST /api/members/restore` endpoint
  - [ ] Restore archived members
  - [ ] Set archived=false
  - [ ] Clear archivedAt, archivedBy, archiveReason
  - [ ] Return restored members
- [ ] Update `MemberService`
  - [ ] `archiveMembers(List<Long> ids, String reason)` method
  - [ ] `restoreMembers(List<Long> ids)` method
  - [ ] Add audit logging for archive/restore
  - [ ] Check permissions before archive/restore
- [ ] Add hard delete option (admin only)
  - [ ] `DELETE /api/members/{id}/permanent`
  - [ ] Requires SUPERADMIN role
  - [ ] Confirmation required
  - [ ] Cascade delete related records (attendance, etc.)
- [ ] Add unit tests
  - [ ] Test archive members
  - [ ] Test restore members
  - [ ] Test archived members excluded from default queries
  - [ ] Test permission check for archive
  - [ ] Test hard delete (admin only)

#### Frontend Tasks
- [ ] Update delete confirmation dialog
  - [ ] Change "Delete" to "Archive"
  - [ ] Add archive reason textarea (required)
  - [ ] Explain soft delete vs permanent delete
  - [ ] "Archive" button instead of "Delete"
- [ ] Add archived members view
  - [ ] Toggle or separate tab: "Active | Archived"
  - [ ] When "Archived" selected, show archived members
  - [ ] Show archive date, archived by, reason
  - [ ] Add "Restore" button to archived members
- [ ] Update bulk delete
  - [ ] Select multiple members
  - [ ] Click "Delete"
  - [ ] Show bulk archive dialog
  - [ ] Enter reason (applies to all)
  - [ ] Confirm
  - [ ] Members moved to archived
- [ ] Add restore functionality
  - [ ] In archived view, select members
  - [ ] Click "Restore"
  - [ ] Confirm restoration
  - [ ] Members moved back to active
- [ ] Add permanent delete (admin only)
  - [ ] In archived view
  - [ ] "Permanent Delete" button (SUPERADMIN only)
  - [ ] Strong confirmation dialog
  - [ ] "This action cannot be undone. Type 'DELETE' to confirm"
  - [ ] Permanently delete member
- [ ] Update member count statistics
  - [ ] Exclude archived members from counts
  - [ ] Add "Archived: X" to stats

#### E2E Tests
- [ ] Test: Archive single member
  - [ ] Select member
  - [ ] Click "Delete"
  - [ ] Enter archive reason: "Moved to another church"
  - [ ] Click "Archive"
  - [ ] Assert success toast
  - [ ] Assert member not in active list
  - [ ] Switch to "Archived" view
  - [ ] Assert member appears with reason
- [ ] Test: Bulk archive members
  - [ ] Select 5 members
  - [ ] Click "Delete"
  - [ ] Enter reason: "Data cleanup"
  - [ ] Archive
  - [ ] Assert 5 members archived
  - [ ] Assert all appear in archived view
- [ ] Test: Restore archived member
  - [ ] Archive a member
  - [ ] Go to "Archived" view
  - [ ] Select archived member
  - [ ] Click "Restore"
  - [ ] Confirm
  - [ ] Assert member restored to active
  - [ ] Assert archive date/reason cleared
- [ ] Test: Permanent delete (admin only)
  - [ ] Login as SUPERADMIN
  - [ ] Archive a member
  - [ ] Go to "Archived" view
  - [ ] Click "Permanent Delete"
  - [ ] Type "DELETE" in confirmation
  - [ ] Confirm
  - [ ] Assert member permanently deleted
  - [ ] Assert not in active or archived views
- [ ] Test: Archived members excluded from search
  - [ ] Archive member "John Doe"
  - [ ] Search for "John Doe" in active view
  - [ ] Assert no results
  - [ ] Switch to archived view
  - [ ] Search for "John Doe"
  - [ ] Assert member appears

**Edge Cases**:
- Archive member with attendance records (preserve records)
- Archive member with giving records (preserve records)
- Archive member who is fellowship leader (warning)
- Archive member who is household head (warning, suggest replacement)
- Restore member with duplicate phone number (now exists)
- Permanent delete with foreign key constraints
- Archive reason with special characters or very long text
- Concurrent archive/restore operations
- Archive member who was already archived (idempotent)
- Restore member who was never archived (error)

---

### 2.5 Advanced Search Builder ⏳
**Priority**: MEDIUM | **Estimate**: 14 hours

#### Backend Tasks
- [ ] Create `SearchCriteria` class
  - [ ] String field (member field name)
  - [ ] String operator (EQUALS, CONTAINS, GREATER_THAN, LESS_THAN, BETWEEN, IN, etc.)
  - [ ] Object value (search value)
  - [ ] String logicalOperator (AND, OR)
- [ ] Create `AdvancedSearchRequest` DTO
  - [ ] List<SearchCriteria> criteria
  - [ ] String sortBy (field name)
  - [ ] String sortDirection (ASC, DESC)
  - [ ] int page
  - [ ] int size
- [ ] Create dynamic query builder
  - [ ] Use JPA Criteria API or Specifications
  - [ ] Build query from SearchCriteria list
  - [ ] Support AND/OR between criteria
  - [ ] Support nested groups
  - [ ] Handle different field types (String, Number, Date, Boolean)
- [ ] Create `POST /api/members/search/advanced` endpoint
  - [ ] Accept AdvancedSearchRequest
  - [ ] Build dynamic query
  - [ ] Execute query with pagination
  - [ ] Return Page<MemberResponse>
- [ ] Add support for various operators
  - [ ] Strings: EQUALS, CONTAINS, STARTS_WITH, ENDS_WITH
  - [ ] Numbers: EQUALS, GREATER_THAN, LESS_THAN, BETWEEN
  - [ ] Dates: EQUALS, BEFORE, AFTER, BETWEEN
  - [ ] Arrays: IN, NOT_IN
  - [ ] Boolean: EQUALS
  - [ ] Null checks: IS_NULL, IS_NOT_NULL
- [ ] Add searchable fields whitelist
  - [ ] firstName, lastName, otherName
  - [ ] phoneNumber, email
  - [ ] age (calculated from DOB)
  - [ ] maritalStatus, sex
  - [ ] status (lifecycle status)
  - [ ] isVerified
  - [ ] fellowshipIds
  - [ ] tags
  - [ ] locationId
  - [ ] memberSince
- [ ] Add unit tests
  - [ ] Test single criteria search
  - [ ] Test multiple criteria with AND
  - [ ] Test multiple criteria with OR
  - [ ] Test nested groups
  - [ ] Test each operator type
  - [ ] Test invalid field names (security)
  - [ ] Test SQL injection attempts

#### Frontend Tasks
- [ ] Create `AdvancedSearchBuilderComponent`
  - [ ] Visual query builder interface
  - [ ] Add/remove criteria rows
  - [ ] Add/remove criteria groups
  - [ ] AND/OR toggle between criteria
  - [ ] Field selector dropdown
  - [ ] Operator selector (changes based on field type)
  - [ ] Value input (type changes based on field)
  - [ ] Preview query in human-readable format
- [ ] Implement criteria row
  - [ ] Field dropdown (all searchable fields)
  - [ ] Operator dropdown (context-sensitive)
  - [ ] Value input:
    - [ ] Text input for strings
    - [ ] Number input for numbers
    - [ ] Date picker for dates
    - [ ] Checkbox for booleans
    - [ ] Multi-select for arrays
  - [ ] Delete button (remove criteria)
  - [ ] AND/OR toggle
- [ ] Implement criteria groups
  - [ ] Nested grouping with parentheses
  - [ ] Visual indentation
  - [ ] Group-level AND/OR
  - [ ] Add criteria to group
  - [ ] Delete entire group
- [ ] Add pre-defined quick filters
  - [ ] "Active Members" (status=MEMBER, isVerified=true)
  - [ ] "Visitors" (status=VISITOR)
  - [ ] "Inactive" (lastContactDate > 90 days ago)
  - [ ] "Youth" (age BETWEEN 13 AND 25)
  - [ ] "Married" (maritalStatus=MARRIED)
  - [ ] Click to apply and customize
- [ ] Add search execution
  - [ ] "Search" button
  - [ ] Loading indicator
  - [ ] Results count preview
  - [ ] Results displayed in members list
  - [ ] Clear search button
- [ ] Add query validation
  - [ ] At least one criteria required
  - [ ] All criteria have field, operator, value
  - [ ] Valid field names
  - [ ] Valid operators for field type
  - [ ] Valid value format
  - [ ] Show validation errors inline

#### E2E Tests
- [ ] Test: Simple search (single criteria)
  - [ ] Open advanced search
  - [ ] Add criteria: Age > 18
  - [ ] Click "Search"
  - [ ] Assert only members 18+ shown
  - [ ] Assert count matches
- [ ] Test: Multiple criteria with AND
  - [ ] Add criteria 1: Fellowship = "Youth"
  - [ ] Add criteria 2: Age BETWEEN 18 AND 25
  - [ ] Both with AND
  - [ ] Search
  - [ ] Assert results match both criteria
- [ ] Test: Multiple criteria with OR
  - [ ] Add criteria 1: Status = "VISITOR"
  - [ ] Add criteria 2: Status = "FIRST_TIMER"
  - [ ] Both with OR
  - [ ] Search
  - [ ] Assert results match either criteria
- [ ] Test: Nested groups
  - [ ] Group 1: (Age > 18 AND Sex = "MALE")
  - [ ] Group 2: (Age > 21 AND Sex = "FEMALE")
  - [ ] Connect groups with OR
  - [ ] Search
  - [ ] Assert complex logic applied correctly
- [ ] Test: Date range search
  - [ ] Add criteria: MemberSince BETWEEN 2023-01-01 AND 2023-12-31
  - [ ] Search
  - [ ] Assert only members who joined in 2023 shown
- [ ] Test: Text contains search
  - [ ] Add criteria: FirstName CONTAINS "John"
  - [ ] Search
  - [ ] Assert all Johns shown (John, Johnny, Johnathan, etc.)
- [ ] Test: Quick filter application
  - [ ] Click "Active Members" quick filter
  - [ ] Assert criteria auto-populated
  - [ ] Modify criteria
  - [ ] Search
  - [ ] Assert customized results
- [ ] Test: Save search for reuse (covered in next section)
- [ ] Test: Validation errors
  - [ ] Add criteria without value
  - [ ] Click "Search"
  - [ ] Assert validation error shown
  - [ ] Assert search disabled
- [ ] Test: Clear search
  - [ ] Perform search
  - [ ] Click "Clear"
  - [ ] Assert all members shown again
  - [ ] Assert search criteria cleared

**Edge Cases**:
- Empty string vs null in text search
- Case sensitivity in text search
- Special characters in search values (escape)
- Very long search queries (100+ criteria)
- Circular references in nested groups
- Invalid date formats
- Age calculation across timezones
- Search by tags (member has any vs all tags)
- Search by fellowship (member in any vs all fellowships)
- Field name typos or invalid fields (security)
- SQL injection attempts via field/value
- Extremely large result sets (pagination)
- Search timeout for complex queries
- Unicode characters in search values

---

(Continuing with remaining phases in next response due to length...)
