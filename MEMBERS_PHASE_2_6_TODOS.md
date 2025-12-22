# PastCare - Members Module Phase 2-6 Complete Implementation TODOs

**SaaS Context**: Multi-tenant church management platform
**Last Updated**: 2025-12-20

---

## 📌 Legend
- ⏳ **Not Started**
- 🔄 **In Progress**
- ✅ **Completed**
- 🔴 **Blocked**
- ⚠️ **Needs Review**

---

# MEMBERS MODULE - PHASE 2-6

## Phase 2: Quick Operations & Bulk Management (Continued)

### 2.6 Saved Searches ⏳
**Priority**: MEDIUM | **Estimate**: 10 hours

#### Backend Tasks
- [ ] Create `SavedSearch` entity
  ```java
  @Entity
  public class SavedSearch extends TenantBaseEntity {
      @ManyToOne
      @JoinColumn(name = "created_by_user_id", nullable = false)
      private User createdBy;

      @Column(nullable = false, length = 200)
      private String searchName;

      @Column(columnDefinition = "TEXT", nullable = false)
      private String searchCriteria; // JSON string of search parameters

      private Boolean isPublic = false; // Visible to all church users
      private Boolean isDynamic = false; // Auto-updates as members change

      @Column(columnDefinition = "TEXT")
      private String description;

      private LocalDateTime lastExecuted;
      private Long lastResultCount;
  }
  ```
- [ ] Create database migration `V7__create_saved_search_table.sql`
  - [ ] Create saved_search table
  - [ ] Add foreign key to users table
  - [ ] Add foreign key to church table
  - [ ] Add index on (church_id, created_by_user_id)
  - [ ] Add index on search_name for quick lookup
- [ ] Create `SavedSearchRepository`
  - [ ] `findByChurch(Church, Pageable)` - All church saved searches
  - [ ] `findByChurchAndCreatedBy(Church, User, Pageable)` - User's searches
  - [ ] `findByChurchAndIsPublicTrue(Church, Pageable)` - Public searches
- [ ] Create `SavedSearchService`
  - [ ] `createSavedSearch(SavedSearchRequest)` - Create and validate
  - [ ] `updateSavedSearch(Long id, SavedSearchRequest)` - Update existing
  - [ ] `deleteSavedSearch(Long id)` - Delete (check ownership)
  - [ ] `executeSavedSearch(Long id, Pageable)` - Run search and update lastExecuted
  - [ ] Validate search criteria JSON format
  - [ ] Check user has permission to access search
- [ ] Create `SavedSearchRequest` DTO
  - [ ] String searchName (NotBlank, max 200 chars)
  - [ ] String searchCriteria (NotBlank, valid JSON)
  - [ ] Boolean isPublic (default false)
  - [ ] Boolean isDynamic (default false)
  - [ ] String description (max 500 chars)
- [ ] Create `SavedSearchResponse` DTO
  - [ ] Include all SavedSearch fields
  - [ ] Add createdByUser (id, name)
  - [ ] Add lastExecutedAgo (human-readable: "2 days ago")
  - [ ] Add canEdit (based on ownership and role)
  - [ ] Add canDelete (based on ownership and role)
- [ ] Create API endpoints
  - [ ] `POST /api/saved-searches` - Create saved search
  - [ ] `GET /api/saved-searches` - List all accessible searches
    - [ ] Query param: includePublic (default true)
    - [ ] Query param: includePrivate (default true, filtered by user)
  - [ ] `GET /api/saved-searches/{id}` - Get single search
  - [ ] `PUT /api/saved-searches/{id}` - Update search (owner only)
  - [ ] `DELETE /api/saved-searches/{id}` - Delete search (owner only)
  - [ ] `POST /api/saved-searches/{id}/execute` - Execute search
    - [ ] Return Page<MemberResponse>
    - [ ] Update lastExecuted and lastResultCount
    - [ ] Support pagination
  - [ ] `POST /api/saved-searches/{id}/duplicate` - Clone search for editing
- [ ] Add permission checks
  - [ ] Only owner can update/delete private searches
  - [ ] ADMIN can delete any search
  - [ ] All church users can view public searches
  - [ ] Only creator can change isPublic flag
- [ ] Add validation
  - [ ] Validate JSON structure of searchCriteria
  - [ ] Prevent SQL injection via criteria
  - [ ] Check field names in criteria against whitelist
  - [ ] Validate operator types
- [ ] Add unit tests
  - [ ] Test create saved search
  - [ ] Test execute saved search
  - [ ] Test update lastExecuted timestamp
  - [ ] Test permission checks (owner vs non-owner)
  - [ ] Test public vs private visibility
  - [ ] Test dynamic search (re-execute gives updated results)
  - [ ] Test invalid JSON criteria (should fail)

#### Frontend Tasks
- [ ] Create `SavedSearchesComponent`
  - [ ] List view of all accessible searches
  - [ ] Filter: My Searches | Public Searches | All
  - [ ] Sort by: Name, Last Executed, Result Count
  - [ ] Search bar to filter saved searches
- [ ] Add saved search card/row design
  - [ ] Search name (bold, clickable)
  - [ ] Description (truncated)
  - [ ] Created by (user name)
  - [ ] Last executed (relative time)
  - [ ] Result count badge
  - [ ] Public/Private badge
  - [ ] Dynamic/Static badge
  - [ ] Actions: Execute, Edit, Duplicate, Delete
- [ ] Create `SaveSearchDialogComponent`
  - [ ] Triggered when saving from advanced search
  - [ ] Fields: Name, Description, Public checkbox, Dynamic checkbox
  - [ ] Preview of search criteria (human-readable)
  - [ ] Save button
- [ ] Add "Save Search" button to advanced search builder
  - [ ] Appears after search executed
  - [ ] Opens save dialog
  - [ ] Pre-fills criteria from current search
- [ ] Add saved searches dropdown to members page
  - [ ] Dropdown button: "Saved Searches"
  - [ ] List of user's searches + public searches
  - [ ] Click to execute immediately
  - [ ] "Manage Saved Searches" option at bottom
- [ ] Implement execute saved search flow
  - [ ] Click saved search from dropdown or list
  - [ ] Show loading indicator
  - [ ] Execute search API call
  - [ ] Display results in members list
  - [ ] Show active search indicator (chip/badge)
  - [ ] "Clear Search" button to reset
- [ ] Add edit saved search functionality
  - [ ] Open advanced search builder
  - [ ] Pre-populate with saved criteria
  - [ ] Edit criteria
  - [ ] Save updates (or save as new)
- [ ] Add duplicate saved search
  - [ ] Copy existing search
  - [ ] Open in advanced search builder
  - [ ] User can modify and save as new search
- [ ] Add delete confirmation
  - [ ] "Are you sure you want to delete this saved search?"
  - [ ] Show search name in confirmation
  - [ ] Delete button (red, destructive)
- [ ] Add visual indicators
  - [ ] Public searches: globe icon
  - [ ] Private searches: lock icon
  - [ ] Dynamic searches: refresh icon
  - [ ] Owner badge: "Created by you"

#### E2E Tests
- [ ] Test: Save search after advanced search
  - [ ] Build advanced search (Age > 18 AND Fellowship = Youth)
  - [ ] Click "Search"
  - [ ] Click "Save Search"
  - [ ] Enter name: "Youth Members 18+"
  - [ ] Check "Public" checkbox
  - [ ] Click "Save"
  - [ ] Assert success toast
  - [ ] Navigate to saved searches
  - [ ] Assert search appears in list
- [ ] Test: Execute saved search from dropdown
  - [ ] Create saved search "Active Members"
  - [ ] Go to members page
  - [ ] Click "Saved Searches" dropdown
  - [ ] Click "Active Members"
  - [ ] Assert search executed
  - [ ] Assert results match search criteria
  - [ ] Assert active search indicator shown
- [ ] Test: Edit saved search
  - [ ] Create saved search
  - [ ] Click "Edit"
  - [ ] Assert advanced search builder opens with criteria
  - [ ] Modify criteria (change age from 18 to 21)
  - [ ] Save updates
  - [ ] Re-execute search
  - [ ] Assert updated criteria applied
- [ ] Test: Duplicate saved search
  - [ ] Create saved search "Youth 18-25"
  - [ ] Click "Duplicate"
  - [ ] Modify name to "Young Adults 26-30"
  - [ ] Modify age criteria
  - [ ] Save as new search
  - [ ] Assert both searches exist
  - [ ] Execute each and verify different results
- [ ] Test: Delete saved search
  - [ ] Create saved search
  - [ ] Click "Delete"
  - [ ] Assert confirmation dialog
  - [ ] Confirm deletion
  - [ ] Assert search removed from list
  - [ ] Assert no longer in dropdown
- [ ] Test: Public vs Private searches
  - [ ] Login as User A
  - [ ] Create public search "Public Test"
  - [ ] Create private search "Private Test"
  - [ ] Logout and login as User B (same church)
  - [ ] View saved searches
  - [ ] Assert "Public Test" visible
  - [ ] Assert "Private Test" NOT visible
- [ ] Test: Dynamic search updates
  - [ ] Create dynamic saved search "Birth Month = March"
  - [ ] Execute in March (assert X members)
  - [ ] Add new member with March birthday
  - [ ] Re-execute same saved search
  - [ ] Assert count increased by 1
- [ ] Test: Permission checks
  - [ ] Login as User A
  - [ ] Create private search
  - [ ] Logout and login as User B
  - [ ] Attempt to edit User A's search via API
  - [ ] Assert 403 Forbidden
- [ ] Test: Invalid search criteria JSON
  - [ ] Create saved search with malformed JSON (via API)
  - [ ] Assert 400 Bad Request
  - [ ] Assert validation error message

**Edge Cases**:
- Very long search names (>200 chars, should truncate)
- Search criteria JSON size limit (>10KB)
- Search with 0 results (still valid)
- Search that times out (>30s execution)
- Circular references in nested criteria (should prevent)
- Search using deleted/archived members (dynamic search excludes)
- Saved search with fellowship that was deleted (handle gracefully)
- User deletes account but saved searches remain (orphaned, deletable by admin)
- Concurrent execution of same search (race condition)
- Search criteria includes fields that no longer exist (schema changes)
- Special characters in search name (escape properly)
- Duplicate search names (allow, distinguish by creator)

---

### 2.7 Tags System ⏳
**Priority**: MEDIUM | **Estimate**: 8 hours

#### Backend Tasks
- [ ] Add `tags` field to `Member` entity
  ```java
  @ElementCollection
  @CollectionTable(name = "member_tags",
                   joinColumns = @JoinColumn(name = "member_id"))
  @Column(name = "tag")
  private Set<String> tags = new HashSet<>();
  ```
- [ ] Create database migration `V8__add_member_tags.sql`
  - [ ] Create member_tags table (member_id, tag)
  - [ ] Add foreign key to members table
  - [ ] Add index on tag column for search
  - [ ] Add composite index on (member_id, tag) for uniqueness
- [ ] Update `MemberRequest` DTO
  - [ ] Add `Set<String> tags` field
  - [ ] Each tag: max 50 chars, lowercase, alphanumeric + hyphen only
  - [ ] Validation pattern: `^[a-z0-9-]+$`
- [ ] Update `MemberService`
  - [ ] Handle tags in `createMember()` - normalize to lowercase
  - [ ] Handle tags in `updateMember()` - merge or replace?
    - [ ] Option A: Replace all tags
    - [ ] Option B: Add new tags, keep existing
  - [ ] Add `addTagsToMember(Long memberId, Set<String> tags)` method
  - [ ] Add `removeTagsFromMember(Long memberId, Set<String> tags)` method
  - [ ] Add `getAllTags(Long churchId)` - Get all unique tags in church
  - [ ] Add `getMembersByTag(Long churchId, String tag, Pageable)` - Search by tag
- [ ] Create `TagStatistics` class
  ```java
  public class TagStatistics {
      private String tag;
      private Long count; // Number of members with this tag
  }
  ```
- [ ] Add tag statistics method
  - [ ] `getTagStatistics(Long churchId)` - Returns List<TagStatistics>
  - [ ] Query: SELECT tag, COUNT(*) FROM member_tags GROUP BY tag
  - [ ] Order by count DESC (most used tags first)
- [ ] Create API endpoints
  - [ ] `GET /api/members/tags` - Get all tags for church (with counts)
  - [ ] `GET /api/members/tags/{tag}/members` - Get members with specific tag
  - [ ] `POST /api/members/{id}/tags` - Add tags to member
    - [ ] Request body: Set<String> tags
  - [ ] `DELETE /api/members/{id}/tags` - Remove tags from member
    - [ ] Request body: Set<String> tags
  - [ ] `POST /api/members/bulk/tags/add` - Bulk add tags
    - [ ] Request: memberIds, tags
  - [ ] `POST /api/members/bulk/tags/remove` - Bulk remove tags
    - [ ] Request: memberIds, tags
- [ ] Add tag normalization
  - [ ] Convert to lowercase
  - [ ] Trim whitespace
  - [ ] Replace spaces with hyphens
  - [ ] Remove special characters
  - [ ] Example: "Youth Leader" → "youth-leader"
- [ ] Add validation
  - [ ] Tag length: 1-50 chars
  - [ ] No duplicate tags per member (Set handles this)
  - [ ] Prevent empty tags
  - [ ] Max tags per member: 20
- [ ] Add unit tests
  - [ ] Test tag normalization
  - [ ] Test add tags to member
  - [ ] Test remove tags from member
  - [ ] Test search members by tag
  - [ ] Test tag statistics calculation
  - [ ] Test bulk add/remove tags
  - [ ] Test tag validation (invalid characters, too long)

#### Frontend Tasks
- [ ] Create `TagInputComponent`
  - [ ] PrimeNG Chips component for multiple tags
  - [ ] Autocomplete from existing church tags
  - [ ] Add new tags on Enter or comma
  - [ ] Remove tags with X button
  - [ ] Show tag count (e.g., "5 tags")
  - [ ] Normalize tags on blur (lowercase, hyphenated)
- [ ] Add tags field to member form
  - [ ] In "Church" or "Basic Info" tab
  - [ ] Label: "Tags (categories, labels)"
  - [ ] Placeholder: "Type and press Enter to add tags..."
  - [ ] Show popular tags as suggestions
  - [ ] Inline validation (no special chars, max length)
- [ ] Create `TagsManagementComponent`
  - [ ] Table of all tags in church
  - [ ] Columns: Tag name, Member count, Actions
  - [ ] Sort by count (most used first)
  - [ ] Actions: Rename, Merge, Delete
- [ ] Add tag filter to members page
  - [ ] Multi-select dropdown "Filter by Tags"
  - [ ] List all church tags with counts
  - [ ] Select multiple tags (AND or OR logic?)
  - [ ] Clear filters button
- [ ] Implement tag-based search
  - [ ] Select tags from dropdown
  - [ ] Apply filter
  - [ ] Members list shows only members with selected tags
  - [ ] Show active tag filters as chips
  - [ ] Click chip to remove filter
- [ ] Add tag display on member cards
  - [ ] Show up to 3 tags as colored badges
  - [ ] "+2 more" if more than 3 tags
  - [ ] Hover to see all tags
  - [ ] Click tag to filter members by that tag
- [ ] Add bulk tag operations in toolbar
  - [ ] Select members
  - [ ] Click "Add Tags"
  - [ ] Tag input dialog
  - [ ] Apply to all selected
  - [ ] Similarly for "Remove Tags"
- [ ] Create tag color coding
  - [ ] Auto-assign colors to tags (hash-based)
  - [ ] Consistent color per tag
  - [ ] High contrast for readability
- [ ] Add tag management actions
  - [ ] Rename tag: rename "youth" to "young-adults" (updates all members)
  - [ ] Merge tags: merge "leader" and "leaders" into "leader"
  - [ ] Delete tag: remove from all members (with confirmation)

#### E2E Tests
- [ ] Test: Add tags to member
  - [ ] Open member form
  - [ ] Click tags input
  - [ ] Type "youth"
  - [ ] Press Enter
  - [ ] Assert "youth" tag added
  - [ ] Type "leader" and press comma
  - [ ] Assert "leader" tag added
  - [ ] Submit form
  - [ ] Open member profile
  - [ ] Assert both tags displayed
- [ ] Test: Tag autocomplete
  - [ ] Create member with tag "youth-fellowship"
  - [ ] Create new member
  - [ ] Start typing "youth" in tags
  - [ ] Assert autocomplete suggests "youth-fellowship"
  - [ ] Click suggestion
  - [ ] Assert tag added
- [ ] Test: Tag normalization
  - [ ] Add tag "Youth Leader"
  - [ ] Blur tags input
  - [ ] Assert normalized to "youth-leader"
  - [ ] Submit form
  - [ ] Verify stored as "youth-leader"
- [ ] Test: Filter members by tag
  - [ ] Create 5 members with tag "youth"
  - [ ] Create 3 members with tag "leader"
  - [ ] Create 2 members with both tags
  - [ ] Go to members page
  - [ ] Select tag filter "youth"
  - [ ] Assert 7 members shown (5 + 2 with both)
  - [ ] Add tag filter "leader" (AND logic)
  - [ ] Assert 2 members shown (only those with both)
- [ ] Test: Bulk add tags
  - [ ] Select 10 members
  - [ ] Click "Add Tags"
  - [ ] Enter tags: "active", "verified"
  - [ ] Click "Apply"
  - [ ] Assert success toast
  - [ ] Filter by "active"
  - [ ] Assert all 10 members appear
- [ ] Test: Bulk remove tags
  - [ ] Create members with tag "inactive"
  - [ ] Select those members
  - [ ] Click "Remove Tags"
  - [ ] Select "inactive"
  - [ ] Confirm
  - [ ] Assert tag removed from all
- [ ] Test: Tag management - Rename
  - [ ] Create members with tag "old-tag"
  - [ ] Go to tag management
  - [ ] Click "Rename" on "old-tag"
  - [ ] Enter new name: "new-tag"
  - [ ] Confirm
  - [ ] Assert all members now have "new-tag"
  - [ ] Assert "old-tag" no longer exists
- [ ] Test: Tag management - Merge
  - [ ] Create members with tags "leader" and "leaders"
  - [ ] Go to tag management
  - [ ] Select "leader" and "leaders"
  - [ ] Click "Merge"
  - [ ] Choose target: "leader"
  - [ ] Confirm
  - [ ] Assert all members now have "leader"
  - [ ] Assert "leaders" deleted
- [ ] Test: Tag management - Delete
  - [ ] Create tag "temp-tag"
  - [ ] Delete tag
  - [ ] Confirm deletion
  - [ ] Assert removed from all members
  - [ ] Assert tag not in tag list
- [ ] Test: Click tag on member card to filter
  - [ ] View member card with tag "youth"
  - [ ] Click "youth" tag badge
  - [ ] Assert members list filters to show only "youth" tagged members
- [ ] Test: Tag validation
  - [ ] Try to add tag with special chars: "youth@leader"
  - [ ] Assert validation error
  - [ ] Try to add tag >50 chars
  - [ ] Assert validation error
  - [ ] Try to add empty tag
  - [ ] Assert rejected

**Edge Cases**:
- Tag with only numbers: "2024" (should be valid)
- Tag with unicode characters: "café" (normalize to "cafe" or reject?)
- Case sensitivity: "Youth" vs "youth" (normalize to lowercase)
- Duplicate tags in input: "youth, youth" (deduplicate)
- Max tags limit: 21st tag (should reject)
- Tag with leading/trailing spaces: " youth " (trim)
- Tag with multiple spaces: "youth  leader" (normalize to "youth-leader")
- Tag deletion with thousands of members (performance)
- Tag rename conflict: rename to existing tag (merge or error?)
- Concurrent tag operations (race conditions)
- Tags in search criteria (saved searches)
- Export members with tags (CSV should include tags column)
- Import members with tags (CSV parsing)

---

### 2.8 Profile Completeness Indicator ✅
**Priority**: MEDIUM | **Estimate**: 6 hours | **Completed**: 2025-12-22

#### Backend Tasks
- [ ] Add `profileCompleteness` field to `Member` entity
  - [ ] `private Integer profileCompleteness = 0;` (percentage 0-100)
- [ ] Create database migration `V9__add_profile_completeness.sql`
  - [ ] Add column profile_completeness INT DEFAULT 0
  - [ ] Update existing members (calculate and backfill)
- [ ] Create `ProfileCompletenessService`
  - [ ] Define required fields for 100% completeness:
    ```java
    // Core fields (required for creation)
    - firstName, lastName, phoneNumber, sex (25% total)

    // Important fields (50% total)
    - email (10%)
    - dateOfBirth (10%)
    - maritalStatus (5%)
    - spouseName (if married) (5%)
    - location (10%)
    - profileImageUrl (10%)

    // Additional fields (25% total)
    - occupation (5%)
    - memberSince (5%)
    - fellowships (not empty) (5%)
    - emergencyContactName (5%)
    - emergencyContactNumber (5%)
    ```
  - [ ] `calculateCompleteness(Member member)` method
    - [ ] Check each field
    - [ ] Sum percentages
    - [ ] Return integer 0-100
  - [ ] `getMissingFields(Member member)` method
    - [ ] Return List<String> of missing field names
    - [ ] Human-readable names: "Email Address", "Date of Birth", etc.
- [ ] Update `MemberService`
  - [ ] Call `calculateCompleteness()` after create/update
  - [ ] Store in member.profileCompleteness
  - [ ] Include in MemberResponse
- [ ] Add endpoint for completeness details
  - [ ] `GET /api/members/{id}/profile-completeness`
  - [ ] Response:
    ```json
    {
      "completeness": 75,
      "missingFields": ["occupation", "memberSince", "location"],
      "suggestions": ["Add occupation", "Set member since date", "Add location"]
    }
    ```
- [ ] Add church-wide completeness statistics
  - [ ] `GET /api/members/stats/completeness`
  - [ ] Average completeness across all members
  - [ ] Distribution: <25%, 25-50%, 50-75%, 75-100%
  - [ ] Members with 100% completeness count
- [ ] Add unit tests
  - [ ] Test completeness calculation for various scenarios
  - [ ] Test with all fields filled (100%)
  - [ ] Test with minimal fields (25%)
  - [ ] Test with partial fields (55%, 70%, etc.)
  - [ ] Test married member without spouse (should affect completeness)
  - [ ] Test missing fields identification

#### Frontend Tasks
- [ ] Create `ProfileCompletenessComponent`
  - [ ] Circular progress indicator (0-100%)
  - [ ] Color coding:
    - [ ] 0-25%: Red
    - [ ] 26-50%: Orange
    - [ ] 51-75%: Yellow
    - [ ] 76-100%: Green
  - [ ] Percentage text in center
  - [ ] Label: "Profile Completeness"
- [ ] Add completeness indicator to member card
  - [ ] Small circular progress in corner
  - [ ] Hover tooltip: "75% complete"
  - [ ] Click to view missing fields
- [ ] Add completeness to member profile view
  - [ ] Prominent display at top
  - [ ] "Complete your profile" banner if <100%
  - [ ] List of missing fields with icons
  - [ ] "Add [Field]" button for each missing field
  - [ ] Progress bar visualization
- [ ] Create missing fields panel
  - [ ] Accordion or expandable section
  - [ ] Title: "Incomplete Fields (3)"
  - [ ] List items:
    - [ ] Field name
    - [ ] Importance indicator (required vs optional)
    - [ ] Quick add button
  - [ ] Click quick add opens inline form
- [ ] Add completeness filter to members page
  - [ ] Dropdown: "Profile Completeness"
  - [ ] Options:
    - [ ] Complete (100%)
    - [ ] Nearly Complete (75-99%)
    - [ ] Incomplete (0-74%)
    - [ ] Custom range (slider)
  - [ ] Apply filter to show matching members
- [ ] Add completeness to member form
  - [ ] Show live completeness percentage as user fills form
  - [ ] Update in real-time
  - [ ] Encouragement: "Almost there! Add 2 more fields for 100%"
- [ ] Add church-wide completeness dashboard widget
  - [ ] Average completeness gauge
  - [ ] Pie chart: distribution
  - [ ] Goal: "80% of members at 100% completeness"
  - [ ] Progress toward goal
- [ ] Add gamification (optional)
  - [ ] "Complete Profile" achievement badge
  - [ ] Leaderboard: churches with highest avg completeness

#### E2E Tests
- [ ] Test: Completeness calculation on create
  - [ ] Create member with minimal fields (name, phone, sex)
  - [ ] Assert completeness = 25%
  - [ ] View member profile
  - [ ] Assert progress indicator shows 25%
  - [ ] Assert red color
- [ ] Test: Completeness updates on edit
  - [ ] Create member at 25%
  - [ ] Edit and add email, DOB, location
  - [ ] Save
  - [ ] Assert completeness increased to 55%
  - [ ] Assert color changed to orange
- [ ] Test: 100% completeness
  - [ ] Create member
  - [ ] Fill all fields
  - [ ] Submit
  - [ ] Assert completeness = 100%
  - [ ] Assert green color
  - [ ] Assert "Profile Complete" badge shown
- [ ] Test: Missing fields identification
  - [ ] Create incomplete member (60%)
  - [ ] View profile
  - [ ] Click "View Missing Fields"
  - [ ] Assert list shows: "occupation", "memberSince", "fellowships"
  - [ ] Assert count matches
- [ ] Test: Quick add missing field
  - [ ] View incomplete profile
  - [ ] Click "Add" button next to "Occupation"
  - [ ] Inline form appears
  - [ ] Enter occupation: "Teacher"
  - [ ] Save
  - [ ] Assert occupation added
  - [ ] Assert completeness increased
  - [ ] Assert "occupation" removed from missing list
- [ ] Test: Filter by completeness
  - [ ] Create 10 members with varying completeness
  - [ ] Filter: "Complete (100%)"
  - [ ] Assert only 100% members shown
  - [ ] Filter: "Incomplete (0-74%)"
  - [ ] Assert members <75% shown
- [ ] Test: Church-wide completeness stats
  - [ ] Navigate to dashboard
  - [ ] View completeness widget
  - [ ] Assert average completeness shown
  - [ ] Assert distribution pie chart displays
  - [ ] Assert goal progress shown
- [ ] Test: Real-time completeness in form
  - [ ] Open member form
  - [ ] Assert completeness starts at 25% (required fields)
  - [ ] Add email
  - [ ] Assert completeness updates to 35%
  - [ ] Add DOB
  - [ ] Assert completeness updates to 45%
  - [ ] Continue until 100%

**Edge Cases**:
- Married member without spouse name (should count as incomplete)
- Spouse name filled but marital status not "married" (doesn't count)
- Empty string vs null (both should count as missing)
- Whitespace-only values (should count as missing)
- Default values (memberSince defaulting to today - counts as filled)
- Archived members (exclude from church stats?)
- Completeness calculation performance (1000+ members)
- Completeness recalculation on schema changes (new required fields)
- Backwards compatibility (members created before completeness feature)
- Member with 100% completeness, then field removed (recalculate)

---

## Phase 3: Family & Household Management (2-3 weeks)

### 3.1 Household Entity & CRUD ✅
**Priority**: HIGH | **Estimate**: 12 hours | **Completed**: 2025-12-22

#### Backend Tasks
- [ ] Create `Household` entity
  ```java
  @Entity
  @Data
  @EqualsAndHashCode(callSuper = true)
  public class Household extends TenantBaseEntity {
      @Column(nullable = false, length = 200)
      private String householdName; // e.g., "The Doe Family"

      @ManyToOne
      @JoinColumn(name = "household_head_id")
      private Member householdHead; // Primary contact

      @ManyToOne
      @JoinColumn(name = "location_id")
      private Location sharedLocation; // Family address

      @OneToMany(mappedBy = "household")
      private List<Member> members = new ArrayList<>();

      @Column(columnDefinition = "TEXT")
      private String notes;

      private LocalDate establishedDate;

      @Column(length = 500)
      private String householdImageUrl; // Family photo

      private String householdEmail; // Shared family email
      private String householdPhone; // Shared family phone
  }
  ```
- [ ] Create database migration `V10__create_household_table.sql`
  - [ ] Create household table with all columns
  - [ ] Add foreign key to church (via TenantBaseEntity)
  - [ ] Add foreign key to members (household_head_id)
  - [ ] Add foreign key to locations (location_id)
  - [ ] Add index on church_id
  - [ ] Add index on household_name
- [ ] Add `household` field to `Member` entity
  ```java
  @ManyToOne
  @JoinColumn(name = "household_id")
  private Household household;
  ```
- [ ] Create migration `V11__add_household_id_to_member.sql`
  - [ ] ALTER TABLE member ADD COLUMN household_id BIGINT
  - [ ] Add foreign key constraint
  - [ ] Add index on household_id
- [ ] Create `HouseholdRepository`
  - [ ] `findByChurch(Church, Pageable)` - All church households
  - [ ] `findByChurchAndHouseholdNameContaining(Church, String, Pageable)` - Search
  - [ ] `countByChurch(Church)` - Count households
  - [ ] `findByChurchAndMembersContaining(Church, Member)` - Find by member
- [ ] Create `HouseholdRequest` DTO
  - [ ] String householdName (NotBlank, max 200)
  - [ ] Long householdHeadId (required)
  - [ ] List<Long> memberIds (optional, can add later)
  - [ ] Long locationId (optional)
  - [ ] String notes (max 1000)
  - [ ] LocalDate establishedDate (optional)
  - [ ] String householdEmail (Email validation)
  - [ ] String householdPhone (InternationalPhoneNumber)
- [ ] Create `HouseholdResponse` DTO
  - [ ] All Household fields
  - [ ] MemberResponse householdHead (full object)
  - [ ] List<MemberResponse> members (full objects)
  - [ ] LocationResponse sharedLocation
  - [ ] Integer memberCount
- [ ] Create `HouseholdService`
  - [ ] `createHousehold(HouseholdRequest)` - Create and validate
    - [ ] Validate household head exists
    - [ ] Validate household head in same church
    - [ ] Validate member IDs exist
    - [ ] Set household for all members
    - [ ] Set household head if not in members list
  - [ ] `updateHousehold(Long id, HouseholdRequest)` - Update
    - [ ] Validate ownership/church
    - [ ] Update all fields
    - [ ] Handle member additions/removals
  - [ ] `deleteHousehold(Long id)` - Delete
    - [ ] Remove household reference from all members
    - [ ] Soft delete or hard delete?
  - [ ] `addMembersToHousehold(Long householdId, List<Long> memberIds)`
  - [ ] `removeMembersFromHousehold(Long householdId, List<Long> memberIds)`
  - [ ] `getHouseholdById(Long id)` - Get with all members
  - [ ] `getAllHouseholds(Long churchId, Pageable)` - List with pagination
  - [ ] `searchHouseholds(Long churchId, String search, Pageable)`
- [ ] Add validation
  - [ ] Household name unique per church
  - [ ] Household head must be a member
  - [ ] All members must be in same church
  - [ ] Cannot have circular household references
  - [ ] Household head must be in members list
- [ ] Create API endpoints
  - [ ] `POST /api/households` - Create household
  - [ ] `GET /api/households` - List households (paginated)
    - [ ] Query: search, page, size, sort
  - [ ] `GET /api/households/{id}` - Get single household
  - [ ] `PUT /api/households/{id}` - Update household
  - [ ] `DELETE /api/households/{id}` - Delete household
  - [ ] `POST /api/households/{id}/members` - Add members to household
    - [ ] Request: List<Long> memberIds
  - [ ] `DELETE /api/households/{id}/members/{memberId}` - Remove member
  - [ ] `POST /api/households/{id}/household-image` - Upload family photo
  - [ ] `GET /api/households/stats` - Household statistics
    - [ ] Total households
    - [ ] Average household size
    - [ ] Largest household
- [ ] Add unit tests
  - [ ] Test create household
  - [ ] Test add members to household
  - [ ] Test remove members from household
  - [ ] Test delete household (members unlinked)
  - [ ] Test household head validation
  - [ ] Test church validation (cross-tenant)
  - [ ] Test unique household name per church

#### Frontend Tasks
- [ ] Create `HouseholdsPageComponent`
  - [ ] Route: /households
  - [ ] List view of all households
  - [ ] Card or table layout
  - [ ] Search bar
  - [ ] "Create Household" button
  - [ ] Pagination
- [ ] Create household card design
  - [ ] Household name (prominent)
  - [ ] Family photo (or placeholder)
  - [ ] Household head name and photo
  - [ ] Member count badge (e.g., "4 members")
  - [ ] Established date
  - [ ] Location (if set)
  - [ ] Actions: View, Edit, Delete
- [ ] Create `HouseholdFormDialogComponent`
  - [ ] Modal dialog for create/edit
  - [ ] Fields:
    - [ ] Household name (required)
    - [ ] Household head selector (member search)
    - [ ] Members multi-select (search members)
    - [ ] Location selector (reuse location component)
    - [ ] Established date (date picker)
    - [ ] Household email
    - [ ] Household phone
    - [ ] Notes (textarea)
    - [ ] Family photo upload
  - [ ] Validation
  - [ ] Submit button
- [ ] Create household head selector
  - [ ] Autocomplete search for members
  - [ ] Show member photo and name
  - [ ] Mark as household head (star icon)
  - [ ] Required field
- [ ] Create members multi-select
  - [ ] Searchable member list
  - [ ] Checkboxes for selection
  - [ ] Show selected count
  - [ ] Automatically include household head
  - [ ] Prevent removing household head
- [ ] Create `HouseholdDetailComponent`
  - [ ] Full household view
  - [ ] Family photo (large)
  - [ ] Household info panel
  - [ ] Members list with photos
  - [ ] Household head badge
  - [ ] Edit/Delete buttons
  - [ ] "Add Member" button
  - [ ] Notes section
- [ ] Add household to member profile
  - [ ] "Household" section
  - [ ] If member in household:
    - [ ] Household name (clickable to household detail)
    - [ ] Household head indicator (if applicable)
    - [ ] Family members list
  - [ ] If not in household:
    - [ ] "Add to Household" button
    - [ ] Or "Create Household" button
- [ ] Add household field to member form
  - [ ] In "Family" tab
  - [ ] Household selector (search existing)
  - [ ] Or "Create New Household" option
  - [ ] Auto-fill location from household
- [ ] Add household statistics to dashboard
  - [ ] Total households card
  - [ ] Average household size card
  - [ ] Largest household card
  - [ ] Households created this month
- [ ] Add household actions
  - [ ] Add member to household (from member profile or household page)
  - [ ] Remove member from household
  - [ ] Change household head
  - [ ] Delete household (with confirmation)

#### E2E Tests
- [ ] Test: Create household
  - [ ] Click "Create Household"
  - [ ] Fill household name: "The Smith Family"
  - [ ] Search and select household head: "John Smith"
  - [ ] Add members: "Jane Smith", "Johnny Smith"
  - [ ] Set established date: "2020-01-15"
  - [ ] Add location
  - [ ] Upload family photo
  - [ ] Click "Create"
  - [ ] Assert household created
  - [ ] Assert appears in households list
- [ ] Test: View household details
  - [ ] Click on household card
  - [ ] Assert household detail page opens
  - [ ] Assert household name displayed
  - [ ] Assert family photo shown
  - [ ] Assert household head marked with badge
  - [ ] Assert all members listed
  - [ ] Assert location shown on map
- [ ] Test: Add member to existing household
  - [ ] View household
  - [ ] Click "Add Member"
  - [ ] Search for member "Mary Smith"
  - [ ] Select and add
  - [ ] Assert member added to household
  - [ ] Open Mary's profile
  - [ ] Assert household section shows "The Smith Family"
- [ ] Test: Remove member from household
  - [ ] View household with 4 members
  - [ ] Click remove icon on member "Mary Smith"
  - [ ] Confirm removal
  - [ ] Assert member removed
  - [ ] Assert count updated to 3 members
  - [ ] Open Mary's profile
  - [ ] Assert household section empty
- [ ] Test: Change household head
  - [ ] View household (head: John)
  - [ ] Click "Edit"
  - [ ] Change household head to "Jane"
  - [ ] Save
  - [ ] Assert Jane now marked as head
  - [ ] Assert John still in household but not head
- [ ] Test: Delete household
  - [ ] Select household
  - [ ] Click "Delete"
  - [ ] Assert confirmation: "This will remove all members from the household"
  - [ ] Confirm
  - [ ] Assert household deleted
  - [ ] Open member profiles
  - [ ] Assert household reference removed
- [ ] Test: Create household from member profile
  - [ ] Open member profile (no household)
  - [ ] Click "Create Household"
  - [ ] Auto-fill household head with current member
  - [ ] Fill household name
  - [ ] Add other family members
  - [ ] Save
  - [ ] Assert household created
  - [ ] Assert member now in household
- [ ] Test: Household statistics
  - [ ] Create 5 households (sizes: 2, 3, 4, 5, 6)
  - [ ] Navigate to dashboard
  - [ ] Assert total households: 5
  - [ ] Assert average size: 4
  - [ ] Assert largest household: 6 members
- [ ] Test: Search households
  - [ ] Create households: "The Smith Family", "The Doe Family", "The Johnson Family"
  - [ ] Search: "Smith"
  - [ ] Assert only Smith family shown
  - [ ] Clear search
  - [ ] Assert all households shown
- [ ] Test: Household validation
  - [ ] Try to create household without name
  - [ ] Assert validation error
  - [ ] Try to create without household head
  - [ ] Assert validation error
  - [ ] Try to add non-existent member ID via API
  - [ ] Assert 404 Not Found

**Edge Cases**:
- Single-member household (valid or require 2+?)
- Household head leaves household (require new head)
- Household head deleted (auto-assign new head or delete household?)
- Duplicate household names (allow but distinguish by location/head)
- Very large household (20+ members - performance)
- Member in multiple households (should prevent)
- Creating household with deleted members (validate)
- Household without location (valid, many families don't share address)
- Concurrent household edits (optimistic locking)
- Circular references (prevented by design)
- Family photo upload failures
- Migrating existing members to households (bulk operation)

---

(Continuing in next file due to length limit...)
