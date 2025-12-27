# Pastoral Care Module - Verification & Completion Plan

**Created**: 2025-12-26
**Status**: Phase 1 Implementation Complete - Verification & Testing in Progress
**Module**: Pastoral Care Module (Phase 1: Care Needs & Visits Management)

---

## Overview

This document provides a step-by-step approach to verify and test all Pastoral Care Module Phase 1 features are functional.

---

## Phase 1 Features Checklist

### ✅ Backend Implementation

#### 1. Database Schema (Migrations)
- [x] **V25**: `care_needs` table with all required fields
- [x] **V26**: `visits` table with `visit_attendees` join table
- [ ] **Verify**: Run migrations and check tables exist
- [ ] **Verify**: Check all foreign key constraints are valid

**Files**:
- `/src/main/resources/db/migration/V25__create_care_needs_table.sql`
- `/src/main/resources/db/migration/V26__create_visits_table.sql`

#### 2. Domain Models (Entities)
- [x] **CareNeed** entity with TenantBaseEntity
- [x] **CareNeedType** enum (15 types)
- [x] **CareNeedPriority** enum (4 levels)
- [x] **CareNeedStatus** enum (6 statuses)
- [x] **Visit** entity with TenantBaseEntity
- [x] **VisitType** enum (7 types)
- [ ] **Verify**: All enums have correct values
- [ ] **Verify**: Entity relationships are properly mapped

**Files**:
- `/src/main/java/com/reuben/pastcare_spring/models/CareNeed.java`
- `/src/main/java/com/reuben/pastcare_spring/models/CareNeedType.java`
- `/src/main/java/com/reuben/pastcare_spring/models/CareNeedPriority.java`
- `/src/main/java/com/reuben/pastcare_spring/models/CareNeedStatus.java`
- `/src/main/java/com/reuben/pastcare_spring/models/Visit.java`
- `/src/main/java/com/reuben/pastcare_spring/models/VisitType.java`

#### 3. Repositories
- [x] **CareNeedRepository** with custom queries
- [x] **VisitRepository** with custom queries
- [ ] **Verify**: Tenant isolation is enforced
- [ ] **Verify**: Custom query methods work correctly

**Files**:
- `/src/main/java/com/reuben/pastcare_spring/repositories/CareNeedRepository.java`
- `/src/main/java/com/reuben/pastcare_spring/repositories/VisitRepository.java`

#### 4. Services
- [x] **CareNeedService** - CRUD operations
- [x] **VisitService** - CRUD operations
- [ ] **Verify**: Auto-detection logic exists
- [ ] **Verify**: Statistics calculation works
- [ ] **Verify**: Follow-up scheduling works

**Files**:
- `/src/main/java/com/reuben/pastcare_spring/services/CareNeedService.java`
- `/src/main/java/com/reuben/pastcare_spring/services/VisitService.java`

#### 5. Controllers (REST API)
- [x] **CareNeedController** - CRUD endpoints
- [x] **VisitController** - CRUD endpoints
- [ ] **Verify**: All endpoints are accessible
- [ ] **Verify**: Authentication/authorization works
- [ ] **Verify**: Error handling is proper

**Files**:
- `/src/main/java/com/reuben/pastcare_spring/controllers/CareNeedController.java`
- `/src/main/java/com/reuben/pastcare_spring/controllers/VisitController.java`

#### 6. DTOs (Data Transfer Objects)
- [x] **CareNeedRequest** - Create/Update DTO
- [x] **CareNeedResponse** - Read DTO
- [x] **CareNeedStatsResponse** - Statistics DTO
- [x] **VisitRequest** - Create/Update DTO
- [x] **VisitResponse** - Read DTO
- [ ] **Verify**: All fields map correctly

**Files**:
- `/src/main/java/com/reuben/pastcare_spring/dtos/CareNeedRequest.java`
- `/src/main/java/com/reuben/pastcare_spring/dtos/CareNeedResponse.java`
- `/src/main/java/com/reuben/pastcare_spring/dtos/CareNeedStatsResponse.java`
- `/src/main/java/com/reuben/pastcare_spring/dtos/VisitRequest.java`
- `/src/main/java/com/reuben/pastcare_spring/dtos/VisitResponse.java`

---

### ✅ Frontend Implementation

#### 1. Pages & Components
- [x] **PastoralCarePage** - Main care needs page
- [x] **VisitsPage** - Main visits page
- [x] **CareHistoryTimelineComponent** - Timeline visualization
- [x] **AutoDetectSuggestionsComponent** - Auto-detection UI
- [x] **MemberSearchComponent** - Member autocomplete (reusable)
- [ ] **Verify**: All components render correctly
- [ ] **Verify**: Navigation works properly

**Files**:
- `/src/app/pastoral-care-page/pastoral-care-page.ts`
- `/src/app/pastoral-care-page/pastoral-care-page.html`
- `/src/app/pastoral-care-page/pastoral-care-page.css`
- `/src/app/pastoral-care-page/care-history-timeline.component.ts`
- `/src/app/pastoral-care-page/auto-detect-suggestions.component.ts`
- `/src/app/visits-page/visits-page.ts`
- `/src/app/visits-page/visits-page.html`
- `/src/app/visits-page/visits-page.css`
- `/src/app/components/member-search/member-search.component.ts`

#### 2. Services
- [x] **CareNeedService** - API integration
- [x] **VisitService** - API integration
- [ ] **Verify**: API calls work correctly
- [ ] **Verify**: Error handling is implemented

**Files**:
- `/src/app/services/care-need.service.ts`
- `/src/app/services/visit.service.ts`

#### 3. Interfaces (TypeScript)
- [x] **CareNeed** interfaces and types
- [x] **Visit** interfaces and types
- [ ] **Verify**: Types match backend DTOs

**Files**:
- `/src/app/interfaces/care-need.ts`
- `/src/app/interfaces/visit.ts`

---

## Step-by-Step Verification Plan

### Step 1: Backend Verification (30 minutes)

#### 1.1 Database Verification
```bash
# Start the application
cd /home/reuben/Documents/workspace/pastcare-spring
./mvnw spring-boot:run

# Check logs for successful migration
# Verify tables were created:
# - care_needs
# - visits
# - visit_attendees
```

**Expected Result**: Application starts without errors, migrations complete successfully

#### 1.2 Enum Verification
```bash
# Check CareNeedType has 15 types
grep -A 20 "enum CareNeedType" src/main/java/com/reuben/pastcare_spring/models/CareNeedType.java

# Check CareNeedPriority has 4 levels
grep -A 10 "enum CareNeedPriority" src/main/java/com/reuben/pastcare_spring/models/CareNeedPriority.java

# Check CareNeedStatus has 6 statuses
grep -A 10 "enum CareNeedStatus" src/main/java/com/reuben/pastcare_spring/models/CareNeedStatus.java

# Check VisitType has correct types
grep -A 10 "enum VisitType" src/main/java/com/reuben/pastcare_spring/models/VisitType.java
```

**Expected Result**:
- CareNeedType: HOSPITAL_VISIT, BEREAVEMENT, COUNSELING, PRAYER_REQUEST, FINANCIAL_ASSISTANCE, HOUSING_ASSISTANCE, EMPLOYMENT_ASSISTANCE, FAMILY_CRISIS, SPIRITUAL_GUIDANCE, HEALTH_CONCERN, EMOTIONAL_SUPPORT, MARRIAGE_COUNSELING, ADDICTION_RECOVERY, LEGAL_ASSISTANCE, OTHER
- CareNeedPriority: URGENT, HIGH, MEDIUM, LOW
- CareNeedStatus: OPEN, PENDING, IN_PROGRESS, ON_HOLD, RESOLVED, CLOSED
- VisitType: HOME, HOSPITAL, OFFICE, PHONE, VIDEO_CALL, PASTORAL, COUNSELING

#### 1.3 API Endpoint Verification
```bash
# Test care needs endpoints
curl -X GET http://localhost:8080/api/care-needs/stats \
  -H "Authorization: Bearer YOUR_TOKEN"

# Test visits endpoints
curl -X GET http://localhost:8080/api/visits \
  -H "Authorization: Bearer YOUR_TOKEN"

# Test auto-detection
curl -X GET http://localhost:8080/api/care-needs/auto-detect \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Result**: All endpoints return 200 OK with valid JSON responses

---

### Step 2: Frontend Verification (30 minutes)

#### 2.1 Build Verification
```bash
cd /home/reuben/Documents/workspace/past-care-spring-frontend
npm run build
```

**Expected Result**: Build completes successfully without errors

#### 2.2 Component Verification
```bash
# Check all required files exist
ls -la src/app/pastoral-care-page/
ls -la src/app/visits-page/
ls -la src/app/components/member-search/
```

**Expected Result**: All TypeScript, HTML, and CSS files exist

#### 2.3 Visual Verification (Manual)
```bash
# Start frontend development server
npm start

# Navigate to:
# - http://localhost:4200/pastoral-care
# - http://localhost:4200/visits
```

**Expected Result**:
- Pages load without errors
- Stats cards display correctly
- Cards match fellowship page styling
- No animations present
- Member search autocomplete works

---

### Step 3: Functional Testing (45 minutes)

#### 3.1 Care Needs CRUD Testing

**Test Case 1: Create Care Need**
1. Navigate to Pastoral Care page
2. Click "Add Care Need" button
3. Use member search to select a member
4. Fill in:
   - Title: "Hospital Visit Required"
   - Type: HOSPITAL_VISIT
   - Priority: URGENT
   - Description: "Member hospitalized, needs visit"
   - Due Date: Tomorrow
   - Follow-up Required: Yes
4. Click "Create Care Need"

**Expected Result**:
- Care need created successfully
- Appears in card grid
- Stats updated
- Success message displayed

**Test Case 2: Edit Care Need**
1. Click "Edit" on a care need card
2. Change priority to HIGH
3. Update notes
4. Click "Update"

**Expected Result**:
- Care need updated successfully
- Changes reflected immediately
- Success message displayed

**Test Case 3: View Care Need Details**
1. Click "View Details" on a care need
2. Verify care history timeline shows

**Expected Result**:
- Details dialog opens
- Timeline component renders
- Shows creation event
- Shows any status changes

**Test Case 4: Resolve Care Need**
1. Click "Resolve" on an open care need
2. Add resolution notes
3. Click "Mark as Resolved"

**Expected Result**:
- Status changes to RESOLVED
- Resolved date is set
- Stats updated
- Card style changes

**Test Case 5: Delete Care Need**
1. Click "Delete" on a care need
2. Confirm deletion

**Expected Result**:
- Care need removed from list
- Stats updated
- Success message displayed

#### 3.2 Visit Management Testing

**Test Case 6: Schedule Visit**
1. Navigate to Visits page
2. Click "Schedule Visit"
3. Use member search to select a member
4. Fill in:
   - Type: HOME
   - Visit Date: Tomorrow
   - Start Time: 10:00 AM
   - Location: "123 Main St"
   - Purpose: "Pastoral visit"
5. Click "Schedule"

**Expected Result**:
- Visit created successfully
- Appears in visits grid
- Stats updated

**Test Case 7: Complete Visit**
1. Find a past visit
2. Click "Complete" button
3. Add outcomes

**Expected Result**:
- Visit marked as completed
- No longer shows in "Incomplete" filter

**Test Case 8: Filter Visits**
1. Click "Today" filter
2. Click "Upcoming" filter
3. Click "Past" filter
4. Click "Incomplete" filter

**Expected Result**:
- Correct visits show for each filter
- Stats remain consistent

#### 3.3 Auto-Detection Testing

**Test Case 9: Auto-Detection**
1. Navigate to Pastoral Care page
2. Check for auto-detected suggestions banner

**Expected Result**:
- If members have 3+ weeks absence, suggestions appear
- Can create care need from suggestion
- Can dismiss suggestion

#### 3.4 Member Search Testing

**Test Case 10: Member Search**
1. Open Add Care Need dialog
2. Click member search field
3. Type partial name
4. Select member from dropdown

**Expected Result**:
- Search shows matching members
- Shows member details (phone, email)
- Limits to 50 results
- Selection fills member ID

---

### Step 4: Integration Testing (30 minutes)

#### 4.1 Care Need → Visit Linking
**Test**: Create a visit linked to a care need

**Expected Result**: Visit shows related care need, care need shows visit in timeline

#### 4.2 Follow-up Scheduling
**Test**: Create care need with follow-up required

**Expected Result**: Follow-up date tracked, shows in UI prominently

#### 4.3 Multi-Tenant Isolation
**Test**: Create care needs in different churches

**Expected Result**: Care needs only visible within own church

---

### Step 5: E2E Test Creation (2-3 hours)

#### 5.1 Create Test File
```bash
# Create new E2E test file
cd /home/reuben/Documents/workspace/past-care-spring-frontend
touch e2e/pastoral-care.spec.ts
```

#### 5.2 Test Scenarios to Implement

```typescript
// Test scenarios:
// 1. Care Need Lifecycle (Create → In Progress → Resolved)
// 2. Visit Scheduling and Completion
// 3. Auto-Detection Flow
// 4. Member Search in Dialogs
// 5. Care History Timeline
// 6. Statistics Updates
// 7. Follow-up Tracking
// 8. Multi-tenant Isolation
// 9. Filter and Search
// 10. Confidential Care Needs
```

#### 5.3 Run E2E Tests
```bash
npx playwright test e2e/pastoral-care.spec.ts
```

**Expected Result**: All tests pass

---

### Step 6: Performance & UX Verification (20 minutes)

#### 6.1 Performance Checks
- [ ] Page loads in < 2 seconds
- [ ] Member search responds quickly
- [ ] No console errors
- [ ] No memory leaks

#### 6.2 Mobile Responsiveness
- [ ] Test on mobile viewport (375px width)
- [ ] Cards stack properly
- [ ] Dialogs are usable
- [ ] Member search works on mobile

#### 6.3 Accessibility
- [ ] Keyboard navigation works
- [ ] Screen reader compatible
- [ ] Proper ARIA labels
- [ ] Color contrast sufficient

---

## Verification Checklist

### Backend ✅
- [ ] Migrations run successfully
- [ ] All entities exist with proper relationships
- [ ] All enums have correct values
- [ ] Repositories work with tenant isolation
- [ ] Services implement all CRUD operations
- [ ] Auto-detection logic functional
- [ ] Controllers expose all endpoints
- [ ] DTOs map correctly
- [ ] Error handling is proper
- [ ] Authentication/authorization works

### Frontend ✅
- [ ] Build completes without errors
- [ ] All pages render correctly
- [ ] Navigation works
- [ ] Services call correct endpoints
- [ ] Forms validate properly
- [ ] Member search component works
- [ ] Care history timeline displays
- [ ] Auto-detection UI functions
- [ ] Styling matches fellowship cards
- [ ] No unwanted animations
- [ ] Mobile responsive

### Features ✅
- [ ] **15 Care Need Types**: All types selectable and functional
- [ ] **4 Priority Levels**: URGENT, HIGH, MEDIUM, LOW working
- [ ] **6 Statuses**: Full lifecycle (OPEN → PENDING → IN_PROGRESS → ON_HOLD → RESOLVED → CLOSED)
- [ ] **Assignment to Pastors**: Can assign to users
- [ ] **Follow-up Scheduling**: Follow-up dates tracked and displayed
- [ ] **Care Notes**: Description field for confidential notes
- [ ] **Care History Timeline**: Visual timeline showing events
- [ ] **Auto-Detection**: Identifies members with 3+ weeks absence
- [ ] **Visit Scheduling**: Full visit CRUD
- [ ] **Visit Types**: Multiple types (HOME, HOSPITAL, etc.)
- [ ] **Visit Completion**: Can mark visits as completed
- [ ] **Care Need → Visit Linking**: Visits can link to care needs

### Testing ✅
- [ ] Unit tests for services
- [ ] Integration tests for controllers
- [ ] E2E tests for user workflows
- [ ] All tests passing
- [ ] Code coverage > 80%

---

## Issues Found & Resolution

### Issue Tracking Template

| # | Issue | Severity | Status | Resolution |
|---|-------|----------|--------|------------|
| 1 | Duplicate migration V18 and V25 | Medium | Open | Need to verify which is active |
| 2 | | | | |

---

## Completion Criteria

Phase 1 is considered **COMPLETE** when:

1. ✅ All backend entities, services, and controllers exist
2. ✅ All frontend pages and components exist
3. ⏳ All 8 core features are functional (see Features checklist)
4. ⏳ Manual testing passes for all test cases
5. ⏳ E2E tests written and passing
6. ⏳ No critical or high-severity bugs
7. ⏳ Performance meets requirements
8. ⏳ Mobile responsive and accessible

---

## Next Steps After Verification

1. **If all tests pass**:
   - Mark Phase 1 as 100% complete in PLAN.md
   - Create summary document of what was built
   - Decide on Phase 2 vs Phase 3 priority

2. **If issues found**:
   - Document all issues
   - Prioritize by severity
   - Fix critical/high issues
   - Re-test

3. **Documentation**:
   - Update PLAN.md with accurate status
   - Document any deviations from original plan
   - Add screenshots to documentation
   - Update API documentation

---

## Timeline

- **Step 1**: Backend Verification - 30 mins
- **Step 2**: Frontend Verification - 30 mins
- **Step 3**: Functional Testing - 45 mins
- **Step 4**: Integration Testing - 30 mins
- **Step 5**: E2E Test Creation - 2-3 hours
- **Step 6**: Performance & UX - 20 mins

**Total Estimated Time**: 4-5 hours

---

**Last Updated**: 2025-12-26
**Next Review**: After completing verification steps
