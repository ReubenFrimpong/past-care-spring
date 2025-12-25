# Fellowship Module Phase 1 - Manual Testing Guide

## Prerequisites
- ✅ Backend server running on http://localhost:8080
- ✅ Frontend server running on http://localhost:4200
- ✅ MySQL database with V16 migration applied
- ✅ Valid authentication credentials

## Test Scenarios

### 1. Page Access & Navigation
**Steps:**
1. Open browser to http://localhost:4200
2. Login with valid credentials
3. Click "Fellowships" link in the side navigation (Community section)
4. Verify the Fellowships page loads without errors

**Expected:**
- Page displays with header "Fellowships Management"
- Stats cards show: Total Fellowships, Active Fellowships, Total Members, Avg Members/Fellowship
- Search and filter controls are visible
- "Add Fellowship" button is present

---

### 2. Create Fellowship (Basic)
**Steps:**
1. Click "Add Fellowship" button
2. Fill in the form:
   - Name: "Youth Fellowship" (required)
   - Type: Select "Age-Based" (required)
   - Description: "Fellowship for young adults aged 18-35"
   - Active: ✓ checked
   - Accepting New Members: ✓ checked
3. Click "Create" button

**Expected:**
- Success message: "Fellowship created successfully"
- Dialog closes
- New fellowship appears in the grid
- Stats update to reflect new fellowship

---

### 3. Create Fellowship (Full Details)
**Steps:**
1. Click "Add Fellowship" button
2. Fill in the form:
   - Name: "Men's Prayer Group"
   - Type: "Gender-Based"
   - Description: "Weekly men's prayer and fellowship"
   - Meeting Day: "Wednesday"
   - Meeting Time: "19:00"
   - Maximum Capacity: 30
   - Active: ✓ checked
   - Accepting New Members: ✓ checked
3. Click "Create"

**Expected:**
- Fellowship created with all details saved
- Meeting time displays as "Wednesday at 19:00" in the card
- Capacity shows "Members: 0 / 30"

---

### 4. View Fellowship Details
**Steps:**
1. Find any fellowship card
2. Click the eye icon (👁️) to view details

**Expected:**
- Details dialog opens
- Shows all fellowship information:
  - Name, Type, Status
  - Description (if provided)
  - Leader and Co-leaders (if assigned)
  - Meeting schedule and location (if set)
  - Member count / capacity
- "View Join Requests" button shows count

---

### 5. Edit Fellowship
**Steps:**
1. Click the pencil icon (✏️) on a fellowship card
2. Modify some fields:
   - Change description
   - Change meeting time
   - Toggle "Accepting New Members"
3. Click "Update"

**Expected:**
- Success message: "Fellowship updated successfully"
- Changes are visible in the fellowship card
- Stats update if status/accepting members changed

---

### 6. Search Functionality
**Steps:**
1. Type "youth" in the search box
2. Observe filtered results
3. Clear search
4. Type a leader's name
5. Observe results

**Expected:**
- Results filter in real-time as you type
- Matches on fellowship name, description, and leader name
- Empty state shows if no matches
- Clearing search shows all fellowships again

---

### 7. Filter by Type
**Steps:**
1. Select "Age-Based" from the Type filter
2. Verify only age-based fellowships show
3. Select "Gender-Based"
4. Select "All Types"

**Expected:**
- Grid updates to show only matching types
- Stats cards recalculate based on filtered results
- "All Types" shows everything

---

### 8. Filter by Status
**Steps:**
1. Select "Active" from Status filter
2. Select "Inactive"
3. Create a new fellowship and uncheck "Active"
4. Verify it appears in "Inactive" filter

**Expected:**
- Active filter shows only active fellowships
- Inactive filter shows only inactive ones
- Stats reflect filtered data

---

### 9. Filter by Accepting Members
**Steps:**
1. Select "Accepting Members" filter
2. Select "Not Accepting"
3. Toggle a fellowship's "Accepting New Members" in edit
4. Verify it moves between filters

**Expected:**
- Filters work correctly
- Fellowships with acceptingMembers=true appear in "Accepting"
- Others appear in "Not Accepting"

---

### 10. Combine Multiple Filters
**Steps:**
1. Enter search term: "fellowship"
2. Select Type: "Interest-Based"
3. Select Status: "Active"
4. Select Accepting: "Accepting Members"
5. Click "Clear Filters"

**Expected:**
- Results match ALL active filters (AND operation)
- Stats update based on filtered set
- "Clear Filters" resets everything

---

### 11. Submit Join Request
**Steps:**
1. Click the user-plus icon (➕👤) on a fellowship card
2. Select a member from the dropdown
3. Enter optional message: "I would like to join this group"
4. Click "Submit Request"

**Expected:**
- Success message: "Join request submitted successfully"
- Request is created with PENDING status

---

### 12. View Join Requests List
**Steps:**
1. Open fellowship details (eye icon)
2. Click "View Join Requests" button
3. Observe the list

**Expected:**
- Dialog shows all join requests for that fellowship
- Each request shows:
  - Member name
  - Status badge (PENDING, APPROVED, REJECTED)
  - Request message (if provided)
  - Request timestamp
  - Review details (if reviewed)
- Pending requests show Approve/Reject buttons

---

### 13. Approve Join Request
**Steps:**
1. Open Join Requests list for a fellowship
2. Find a PENDING request
3. Click "Approve" button

**Expected:**
- Success message: "Join request approved"
- Request status changes to APPROVED
- Review timestamp and reviewer name populate
- Member count increases by 1
- Approve/Reject buttons disappear

---

### 14. Reject Join Request
**Steps:**
1. Open Join Requests list
2. Find a PENDING request
3. Click "Reject" button

**Expected:**
- Success message: "Join request rejected"
- Status changes to REJECTED
- Review details populate
- Member count does NOT increase

---

### 15. Delete Fellowship
**Steps:**
1. Click trash icon (🗑️) on a fellowship card
2. Confirm deletion in dialog
3. Click "Delete"

**Expected:**
- Warning dialog appears with fellowship name
- Success message: "Fellowship deleted successfully"
- Fellowship removed from grid
- Stats update

---

### 16. Stats Calculations
**Verify the following calculations are correct:**

**Total Fellowships:**
- Count of all fellowships (active + inactive)

**Active Fellowships:**
- Count where isActive = true

**Total Members:**
- Sum of memberCount across ALL fellowships

**Avg Members/Fellowship:**
- Average memberCount for ACTIVE fellowships only
- Rounded to nearest integer
- Shows 0 if no active fellowships

**Test by:**
- Creating fellowships
- Approving join requests to add members
- Toggling active/inactive status
- Deleting fellowships

---

### 17. Validation Testing
**Test the following validations:**

**Create/Edit:**
- Name is required (try submitting empty)
- Type is required (default should be selected)

**Join Request:**
- Member selection is required (try submitting with "Select a member")

**Expected:**
- Error message appears: "Fellowship name is required"
- Error message: "Please select a member"
- Forms don't submit until valid

---

### 18. Edge Cases

**Empty State:**
1. Delete all fellowships (or filter to show none)
2. Verify empty state shows:
   - Inbox icon
   - "No fellowships found" message
   - "Clear Filters" button (if filters active)

**Capacity Limits:**
1. Create fellowship with maxCapacity=2
2. Approve 2 join requests
3. Observe card shows "Members: 2 / 2"

**No Leader/Coleaders:**
- Fellowships without leaders should still display properly
- Leader/Coleader sections should be hidden if not assigned

**Long Text:**
- Create fellowship with very long name/description
- Verify text doesn't break layout

---

### 19. Responsive Design
**Test on different screen sizes:**

**Desktop (>1024px):**
- Grid shows 3-4 cards per row
- All filters on one line
- Full side navigation visible

**Tablet (768px-1024px):**
- Grid shows 2 cards per row
- Filters wrap appropriately

**Mobile (<768px):**
- Grid shows 1 card per row
- Bottom navigation appears
- Dialogs are full-screen or nearly full

---

### 20. Error Handling

**Test error scenarios:**

**Network Error:**
- Stop backend server
- Try to load fellowships
- Expected: Error message appears

**Duplicate Join Request:**
- Submit join request for member already in fellowship
- Expected: Error message from backend

**Permission Issues:**
- Log out
- Try to access /fellowships
- Expected: Redirected to login (authGuard)

---

## API Endpoints to Verify

### Fellowship CRUD
- `GET /api/fellowships` - Get all fellowships
- `GET /api/fellowships/active` - Get active only
- `GET /api/fellowships/accepting-members` - Get accepting members
- `GET /api/fellowships/type/{type}` - Get by type
- `GET /api/fellowships/{id}` - Get by ID
- `POST /api/fellowships` - Create fellowship
- `PUT /api/fellowships/{id}` - Update fellowship
- `DELETE /api/fellowships/{id}` - Delete fellowship

### Leader Management
- `POST /api/fellowships/{id}/leader/{userId}` - Assign leader
- `POST /api/fellowships/{id}/coleaders/{userId}` - Add coleader
- `DELETE /api/fellowships/{id}/coleaders/{userId}` - Remove coleader

### Join Requests
- `POST /api/fellowships/join-requests` - Create join request
- `GET /api/fellowships/{id}/join-requests` - Get all requests
- `GET /api/fellowships/{id}/join-requests/pending` - Get pending
- `POST /api/fellowships/join-requests/{id}/approve` - Approve request
- `POST /api/fellowships/join-requests/{id}/reject` - Reject request

---

## Backend Logs to Monitor

While testing, watch `/tmp/spring-boot.log` for:
- SQL queries being executed
- Validation errors
- Exception stack traces
- Performance issues

---

## Known Limitations (Phase 1)

1. **Leader/Coleader Assignment:**
   - UI only displays existing leaders from fellowship responses
   - No UI to assign/change leaders (backend API exists)
   - Will be added in Phase 2

2. **Location Assignment:**
   - Meeting location can't be set via UI
   - Field exists but no location picker implemented
   - Backend support is complete

3. **Image Upload:**
   - imageUrl field exists but is text input only
   - No file upload widget implemented
   - Can be added in enhancement phase

4. **Pagination:**
   - All fellowships load at once
   - May need pagination for large datasets

5. **Current User Context:**
   - currentUserId not extracted from AuthService
   - Join request approval/rejection may need this implemented

---

## Success Criteria

✅ All CRUD operations work without errors
✅ Filtering and search return correct results
✅ Stats calculations are accurate
✅ Join request workflow (create, approve, reject) works
✅ Validation prevents invalid data
✅ UI is responsive on mobile/tablet/desktop
✅ Error messages display appropriately
✅ No console errors in browser
✅ Backend logs show no exceptions

---

## Next Steps After Testing

If all tests pass:
1. Mark Phase 1 as complete in PLAN.md
2. Document any bugs found
3. Plan Phase 2 enhancements:
   - Leader/Coleader management UI
   - Location picker
   - Image upload
   - Member management (view members in fellowship)
   - Bulk operations
   - Reports and analytics
