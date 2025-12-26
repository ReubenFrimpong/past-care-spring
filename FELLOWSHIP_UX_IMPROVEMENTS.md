# Fellowship "Manage Members" Dialog UX Improvements

## Problem
The "Add Members to Fellowship" dialog displayed all church members without indicating which ones were already in the fellowship, causing confusion and potential duplicate additions. Additionally, there was no easy way to remove members from fellowships.

## Solution
Converted the "Add Members" dialog into a unified "Manage Members" dialog that allows both adding and removing members:
- Members currently in the fellowship are **pre-checked**
- **Check** new members to add them
- **Uncheck** existing members to remove them
- All changes are saved with a single "Save Changes" action

## Changes Made

### Backend Changes

#### 1. New Endpoint: Get Fellowship Member IDs
**File:** `FellowshipController.java`
- Added `GET /api/fellowships/{id}/members/ids` endpoint
- Returns list of member IDs currently in the fellowship
- Used for frontend to identify existing members

**File:** `FellowshipService.java`
- Added `getFellowshipMemberIds(Long fellowshipId)` method
- Extracts and returns member IDs from fellowship entity

### Frontend Changes

#### 1. Fellowship Service
**File:** `fellowship.service.ts`
- Added `getFellowshipMemberIds(fellowshipId: number): Observable<number[]>` method
- Calls new backend endpoint to fetch existing member IDs

#### 2. Fellowships Page Component
**File:** `fellowships-page.ts`

Added/Updated:
- `existingFellowshipMemberIds` signal to store existing member IDs
- `isMemberInFellowship(memberId: number)` helper method to check membership
- Updated `openBulkAddMembersDialog()` to:
  - Fetch existing member IDs when dialog opens
  - Pre-select existing members (so they're checked by default)
- Updated `toggleMemberSelection()` to allow toggling any member (removed restriction)
- **Completely rewrote `submitBulkAddMembers()`** to:
  - Calculate which members to add (checked but not in fellowship)
  - Calculate which members to remove (in fellowship but unchecked)
  - Execute both add and remove operations in parallel
  - Display appropriate success message ("Added X, Removed Y members")

#### 3. Template Updates
**File:** `fellowships-page.html`

Updated dialog to:
- Changed title from "Add Members to..." to "Manage Members - ..."
- Added info message: "Check members to add, uncheck to remove. Currently X member(s) in fellowship."
- Removed "selected count" display
- Updated member selection items:
  - Add `[class.in-fellowship]` class for current members (blue highlight)
  - Removed disabled state on checkboxes (all members can be toggled)
  - Display "Current member" badge with blue background and users icon
  - Show checkmark for all selected members
- Changed button from "Add X Member(s)" to "Save Changes"
- Changed button icon from plus to save icon

#### 4. Styling
**File:** `fellowships-page.css`

Updated styles:
- `.member-selection-item.in-fellowship` - Blue highlight (instead of grayed out)
- `.current-member-badge` - Blue badge with users icon (instead of green "already in fellowship")
- `.info-message` - New blue info box with instructions
- Removed `.already-member` grayed-out styling
- Removed disabled checkbox styles
- Updated `.member-name` to support badge layout with flexbox

## Visual Improvements

### Before
- Dialog titled "Add Members to [Fellowship]"
- All members shown with unchecked checkboxes
- No indication of existing membership
- Could accidentally add duplicate members
- No way to remove members from UI

### After
- Dialog titled "Manage Members - [Fellowship]"
- Info message explains the functionality
- Members currently in fellowship are:
  - **Pre-checked** (can be unchecked to remove)
  - Highlighted with blue background
  - Display "Current member" badge with blue users icon
  - Fully interactive and can be toggled
- New members shown with:
  - Unchecked checkboxes (can be checked to add)
  - Normal white background
  - No badge
- "Save Changes" button executes both adds and removes

## User Experience Benefits

1. **Unified Interface** - Single dialog for both adding and removing members
2. **Clarity** - Clear visual distinction between current members and available members
3. **Efficiency** - Make all membership changes in one action
4. **Intuitive** - Simple check/uncheck paradigm everyone understands
5. **Informative** - Info message and badges provide clear guidance
6. **Flexible** - Can add multiple, remove multiple, or do both simultaneously

## Testing

Run the test script:
```bash
./test-member-fellowship-ux.sh
```

This will verify:
- New endpoint returns correct member IDs
- Endpoint is accessible with authentication
- Data structure is correct

## API Documentation

### Get Fellowship Member IDs
```
GET /api/fellowships/{id}/members/ids
```

**Authentication:** Required

**Response:** `200 OK`
```json
[1, 5, 12, 23]
```

**Description:** Returns array of member IDs currently in the specified fellowship.
