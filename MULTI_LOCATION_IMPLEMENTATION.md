# Multi-Location Crisis Management - Implementation Summary

## Overview
Added multi-location support to the existing Crisis Management system (`crises-page`), allowing a single crisis to affect multiple geographic locations simultaneously.

## What Was Added

### Backend
The backend was already implemented in the previous conversation with:
- `CrisisAffectedLocation` entity
- Database migration V33
- DTOs: `AffectedLocationRequest` and `AffectedLocationResponse`
- Repository and service methods
- Auto-detect functionality across multiple locations

### Frontend - New Features Added to `crises-page`

#### 1. Location Management in Dialogs
Added location management section to both Report and Edit Crisis dialogs:
- **Location List Display**: Shows all added locations with edit/remove buttons
- **Add Location Button**: Opens dialog to add new locations
- **Preview Members Button**: Shows preview of affected members across all locations
- **Location Form Fields**:
  - Suburb (e.g., "Tema")
  - City (e.g., "Accra")
  - District (e.g., "Tema Metropolitan")
  - Region (e.g., "Greater Accra")
  - Country Code (e.g., "GH")

#### 2. Location Display on Crisis Card
- Shows "Affected Locations" field when locations are present
- Displays each location as a blue tag/chip
- Clean, compact display with hover effects

#### 3. Auto-Detect Button
- Added "Auto-Detect" button in crisis card footer
- Only appears when crisis has affected locations
- Automatically finds and adds members in those locations
- Shows success message with count of detected members

#### 4. Preview Functionality
- Preview dialog shows all members that would be affected
- Displays member name and their location (city)
- Deduplicates members across multiple locations
- Shows count in dialog header

## Files Modified

### Frontend Files
1. **[crises-page.ts](src/app/crises-page/crises-page.ts)**
   - Added location-related imports
   - Added dialog state signals (showLocationDialog, showPreviewDialog)
   - Added location form and affected locations signal
   - Added methods:
     - `openAddLocationDialog()`
     - `openEditLocationDialog(index)`
     - `saveLocation()`
     - `removeLocation(index)`
     - `getLocationDisplay(location)`
     - `previewAffectedMembers()`
     - `autoDetectMembers(crisis)`
   - Updated `openReportDialog()` and `openEditDialog()` to handle locations
   - Updated `reportCrisis()` and `updateCrisis()` to include locations in request

2. **[crises-page.html](src/app/crises-page/crises-page.html)**
   - Added "Affected Locations (Geographic)" section in Report dialog (lines 343-377)
   - Added "Affected Locations (Geographic)" section in Edit dialog (lines 482-516)
   - Added location display on crisis card (lines 188-198)
   - Added Auto-Detect button in card footer (lines 271-276)
   - Added Location Dialog (lines 1011-1058)
   - Added Preview Members Dialog (lines 1060-1106)

3. **[crises-page.css](src/app/crises-page/crises-page.css)**
   - Added styles for location manager (lines 1160-1199)
   - Added styles for location tags on cards (lines 1201-1224)
   - Added styles for preview members dialog (lines 1226-1268)

## User Workflow

### Adding Locations to a Crisis

1. **Open Report/Edit Dialog**
2. **Scroll to "Affected Locations (Geographic)" section**
3. **Click "Add Location" button**
4. **Fill in location fields** (at least one field required):
   - Enter suburb, city, district, region, or country code
5. **Click "Save Location"**
6. **Repeat** for additional locations
7. **Optional: Click "Preview Members"** to see who will be affected
8. **Save the crisis**

### Auto-Detecting Affected Members

1. **View a crisis card** that has affected locations
2. **Click the "Auto-Detect" button** in the footer
3. **Confirm the action**
4. **System automatically**:
   - Queries all affected locations
   - Finds members in those areas
   - Deduplicates members
   - Adds them to the crisis
5. **Success message** shows number of members detected

## Key Features

### Multi-Location Support
- Add unlimited locations per crisis
- Edit or remove locations individually
- Each location independently tracked in database

### Member Detection
- Preview members before saving
- Auto-detect across all locations
- Automatic deduplication
- Shows member's city in preview

### Data Integrity
- Cascade deletion (deleting crisis removes locations)
- At least one location field required
- Proper tenant isolation maintained
- Transaction safety

### UX Improvements
- Location tags with hover effects
- Clean, intuitive UI
- Loading states for preview
- Confirmation dialogs for auto-detect
- Success/error messages

## Technical Details

### Location Data Structure
```typescript
interface AffectedLocationRequest {
  suburb?: string;
  city?: string;
  district?: string;
  region?: string;
  countryCode?: string;
}
```

### Auto-Detect Algorithm
1. Iterate through each location
2. Query members matching location fields
3. Collect all members in a Set (auto-dedup)
4. Return unique list
5. Create crisis affected member records

### Preview Functionality
1. Parallel API calls for each location
2. Collect results with `Promise.all()`
3. Flatten arrays
4. Deduplicate using Map by member ID
5. Display in dialog

## Build Status
✅ **Backend**: Compiles successfully
✅ **Frontend**: Compiles successfully (only warnings about bundle size and papaparse, unrelated to this feature)

## Testing Checklist
- [ ] Create crisis with single location
- [ ] Create crisis with multiple locations
- [ ] Edit crisis to add/remove locations
- [ ] Preview affected members
- [ ] Auto-detect members across locations
- [ ] Verify deduplication works
- [ ] Delete crisis (verify cascade deletion)
- [ ] Verify backward compatibility with legacy location field

## Notes
- The existing single "location" text field is preserved for backward compatibility
- New multi-location feature works alongside the legacy field
- UI remains consistent with existing crises-page design
- No changes were made to the crisis card layout - only additions
