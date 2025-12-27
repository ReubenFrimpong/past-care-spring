# Crisis Management - Multi-Location Support Implementation

## Overview
Extended the Crisis Management system to support multiple geographic locations per crisis event, allowing a single crisis to affect multiple areas simultaneously.

## Backend Implementation

### New Entity
**CrisisAffectedLocation.java**
- Represents a geographic location affected by a crisis
- One-to-Many relationship with Crisis entity
- Fields: suburb, city, district, region, countryCode
- Cascade deletion when crisis is deleted

### Database Migration
**V33__create_crisis_affected_location_table.sql**
- Created `crisis_affected_location` table
- Foreign key to crisis table with CASCADE delete
- Indexes on all geographic fields for performance

### DTOs
**AffectedLocationRequest.java**
- DTO for submitting location data from frontend
- All fields optional (suburb, city, district, region, countryCode)

**AffectedLocationResponse.java**
- DTO for returning location data to frontend
- Includes id and all location fields
- `fromEntity()` mapper method

### Repository
**CrisisAffectedLocationRepository.java**
- `findByCrisis(Crisis crisis)` - Get all locations for a crisis
- `deleteByCrisis(Crisis crisis)` - Delete all locations for a crisis
- `existsByLocation()` - Check if location already exists

### Service Layer Updates
**CrisisService.java - autoDetectAffectedMembers()**
- Updated to support multi-location detection
- Iterates through all affected locations
- Uses Set<Member> for automatic deduplication
- Fallback to legacy single location fields for backward compatibility

**Code snippet:**
```java
Set<Member> allAffectedMembers = new HashSet<>();
List<CrisisAffectedLocation> locations = crisis.getAffectedLocationsList();

if (locations != null && !locations.isEmpty()) {
    // Multi-location: find members for each location
    for (CrisisAffectedLocation location : locations) {
        List<Member> membersInLocation = memberRepository.findByGeographicLocation(
            church, location.getSuburb(), location.getCity(),
            location.getDistrict(), location.getRegion(), location.getCountryCode()
        );
        allAffectedMembers.addAll(membersInLocation);
    }
}
```

### Controller Updates
**CrisisController.java**
- Added Member import for auto-detect endpoints
- No other changes needed (uses existing endpoints)

### Modified Files
- **Crisis.java**: Added `affectedLocationsList` relationship with helper methods
- **CrisisRequest.java**: Added `affectedLocations` array field
- **CrisisResponse.java**: Added `affectedLocations` array field and mapping
- **MemberRepository.java**: Already had `findByGeographicLocation()` method

## Frontend Implementation

### Interfaces
**crisis.interface.ts**
- Added `AffectedLocationRequest` interface
- Added `AffectedLocationResponse` interface
- Updated `CrisisRequest` and `CrisisResponse` with `affectedLocations` arrays

### Service
**crisis.service.ts**
- `autoDetectAffectedMembers(crisisId)` - POST to auto-detect endpoint
- `previewAffectedMembers(params)` - GET to preview endpoint with query params
- Both methods support multi-location scenarios

### Component
**crisis-page.ts**
- Added `affectedLocations` signal to manage location array
- Location management methods:
  - `openAddLocationDialog()` - Add new location
  - `openEditLocationDialog(index)` - Edit existing location
  - `saveLocation()` - Save location to array
  - `removeLocation(index)` - Remove location from array
  - `getLocationDisplay()` - Format location for display
- Preview functionality:
  - `previewAffectedMembers()` - Query all locations in parallel
  - Uses `Promise.all()` for parallel API calls
  - Deduplicates members using Map
- Auto-detect functionality:
  - `autoDetectMembers(crisis)` - Trigger server-side detection

### Template
**crisis-page.html**
- Crisis cards display all affected locations as tags
- Add/Edit dialog includes location manager section
- Location dialog for adding/editing individual locations
- Preview dialog shows all affected members before save
- Displays unique member count across all locations

### Styling
**crisis-page.css**
- Location tags styling with hover effects
- Location manager section with scrollable list
- Preview dialog with member grid
- Consistent with existing design patterns

### Routes
**app.routes.ts**
- Updated to use CrisisPage component at `/crises` route

## Key Features

### 1. Multiple Locations Per Crisis
- A crisis can affect unlimited geographic areas
- Each location independently specified
- Easy to add/edit/remove locations

### 2. Member Detection Across Locations
- Auto-detect finds members across ALL affected locations
- Automatic deduplication (members counted once)
- Preview before adding to see affected members

### 3. Backward Compatibility
- Legacy single location fields still supported
- Service checks for multi-location first, falls back to legacy
- Existing crises continue to work

### 4. Performance Optimizations
- Database indexes on all location fields
- Parallel API calls in frontend (Promise.all)
- Set-based deduplication in backend
- Efficient geographic queries

## Usage Flow

### Creating a Crisis with Multiple Locations

1. Click "Add Crisis" button
2. Fill in crisis details (title, type, severity, etc.)
3. Click "Add Affected Location" in the Location Manager
4. Fill in location fields (suburb, city, district, region, country)
5. Save the location
6. Repeat steps 3-5 for additional locations
7. Optional: Click "Preview Affected Members" to see who will be detected
8. Save the crisis

### Auto-Detecting Affected Members

1. View an existing crisis
2. Click the "Auto-Detect" button
3. System finds all members matching ANY of the affected locations
4. Members are deduplicated (counted once even if in multiple locations)
5. Members are automatically added to the crisis

## Testing Checklist

- [x] Backend compiles successfully
- [x] Frontend compiles successfully
- [ ] Create crisis with single location
- [ ] Create crisis with multiple locations
- [ ] Preview affected members across locations
- [ ] Auto-detect members across multiple locations
- [ ] Verify deduplication (member in multiple locations counted once)
- [ ] Edit crisis to add/remove locations
- [ ] Delete crisis (verify cascade deletion of locations)
- [ ] Verify backward compatibility with legacy single-location crises

## Files Created

### Backend
1. `src/main/java/com/reuben/pastcare_spring/models/CrisisAffectedLocation.java`
2. `src/main/java/com/reuben/pastcare_spring/repositories/CrisisAffectedLocationRepository.java`
3. `src/main/java/com/reuben/pastcare_spring/dtos/AffectedLocationRequest.java`
4. `src/main/java/com/reuben/pastcare_spring/dtos/AffectedLocationResponse.java`
5. `src/main/resources/db/migration/V33__create_crisis_affected_location_table.sql`

### Frontend
1. `/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/crisis-page/crisis-page.ts`
2. `/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/crisis-page/crisis-page.html`
3. `/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/crisis-page/crisis-page.css`

## Files Modified

### Backend
1. `src/main/java/com/reuben/pastcare_spring/models/Crisis.java`
2. `src/main/java/com/reuben/pastcare_spring/services/CrisisService.java`
3. `src/main/java/com/reuben/pastcare_spring/controllers/CrisisController.java`
4. `src/main/java/com/reuben/pastcare_spring/dtos/CrisisRequest.java`
5. `src/main/java/com/reuben/pastcare_spring/dtos/CrisisResponse.java`

### Frontend
1. `/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/interfaces/crisis.interface.ts`
2. `/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/services/crisis.service.ts`
3. `/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/app.routes.ts`

## Build Status
✅ Backend: Compiles successfully
✅ Frontend: Compiles successfully (all TypeScript errors resolved)

## Next Steps
1. Test the multi-location functionality end-to-end
2. Verify member detection across multiple locations
3. Test preview functionality
4. Verify cascade deletion of locations
5. Test backward compatibility with existing crises
