# Dashboard Phase 2.1 - Visual Testing Guide

**Purpose:** Visual reference for testing Phase 2.1 features

---

## What to Expect - Visual Walkthrough

### 1. Default Dashboard (Before Customization)

**What You Should See:**
```
┌─────────────────────────────────────────────────────────┐
│  Welcome back, John Doe 👋                              │
│  Here's what's happening with your community today      │
└─────────────────────────────────────────────────────────┘

┌────────────────────────┐  ┌────────────────────────────┐
│  [👤] 245              │  │  [❤️] 12                   │
│  Active Members        │  │  Need Prayer               │
└────────────────────────┘  └────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  🎁 Birthdays This Week                                  │
│  ├─ Sarah Johnson • Today • Age 45                      │
│  ├─ Mike Smith • 2 days • Age 32                        │
│  └─ Emily Davis • 5 days • Age 28                       │
└─────────────────────────────────────────────────────────┘

... (more widgets) ...
```

**Key Elements:**
- ✅ Welcome message with user name
- ✅ Quick stats cards (4 cards)
- ✅ Widget cards with data
- ✅ **"Customize" button in top-right** ← LOOK FOR THIS!

---

### 2. Edit Mode Activated

**After Clicking "Customize":**

```
┌─────────────────────────────────────────────────────────┐
│  [✏️ Edit Mode]          [Widgets] [Save] [Reset] [Exit]│
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  🎁 Birthdays This Week                     [☰] ← DRAG  │
│  ├─ Sarah Johnson • Today • Age 45                      │
│  └─ ...                                                  │
└─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
   ↑ Dashed border on hover
```

**Changes in Edit Mode:**
- ✅ Purple "Edit Mode" badge appears (top-left)
- ✅ "Customize" button changes to "Exit Edit"
- ✅ Three new buttons appear: "Widgets", "Save", "Reset"
- ✅ Widgets get dashed border on hover (if cdkDrag added)
- ✅ Drag handle (☰) appears in widget header (if cdkDrag added)
- ✅ Cursor changes to "move" when hovering widgets

---

### 3. Widget Configurator Panel

**After Clicking "Widgets" Button:**

```
╔═══════════════════════════════════════════════════════════╗
║  Configure Widgets                                    [X] ║
╠═══════════════════════════════════════════════════════════╣
║  Toggle widgets to show/hide on your dashboard           ║
║                                                           ║
║  ┌──────────────────────┐  ┌──────────────────────────┐ ║
║  │ [✓] 📊 Statistics    │  │ [✓] ❤️ Pastoral Care      │ ║
║  │     Overview         │  │     Needs                 │ ║
║  │     Key church...    │  │     Members needing...    │ ║
║  └──────────────────────┘  └──────────────────────────┘ ║
║                                                           ║
║  ┌──────────────────────┐  ┌──────────────────────────┐ ║
║  │ [✓] 🎁 Birthdays     │  │ [ ] 📅 Anniversaries      │ ║
║  │     This Week        │  │     This Month            │ ║
║  │     Upcoming...      │  │     Wedding...            │ ║
║  └──────────────────────┘  └──────────────────────────┘ ║
║                                                           ║
║  ... (more widgets) ...                                  ║
╚═══════════════════════════════════════════════════════════╝
```

**Key Features:**
- ✅ Purple gradient header
- ✅ Close button (X) in top-right
- ✅ Custom toggle switches (purple when ON, gray when OFF)
- ✅ Widget icons and names
- ✅ Description text below each widget
- ✅ Grid layout (2-3 columns on desktop, 1 on mobile)
- ✅ Smooth slide-down animation

**Toggle Switch States:**
- **ON (checked):** Purple gradient background, slider on right
- **OFF (unchecked):** Gray background, slider on left

---

### 4. Widget Visibility Toggle

**Before Toggle:**
```
Dashboard shows:
[Stats] [Pastoral Care] [Events] [Birthdays] [Anniversaries]
```

**Toggle OFF "Anniversaries":**
```
Dashboard shows:
[Stats] [Pastoral Care] [Events] [Birthdays] [          ]
                                              ↑ Space where it was
```

**Effect:**
- ✅ Widget disappears **immediately** (no page refresh)
- ✅ Space collapses or remains (depends on CSS grid)
- ✅ Toggle switch changes from purple to gray

**Toggle ON again:**
- ✅ Widget reappears **immediately**
- ✅ Toggle switch changes from gray to purple

---

### 5. Drag-and-Drop Preview (if cdkDrag added)

**Start Dragging:**
```
┌─────────────────────────────────────┐
│  🎁 Birthdays This Week  [☰]        │ ← Being dragged
│  (slightly rotated, shadow)         │
└─────────────────────────────────────┘
        ↓ Dragging down

┌─────────────────────────────────────┐
│  📅 Anniversaries This Month  [☰]   │
└─────────────────────────────────────┘

┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
   [  PLACEHOLDER - dashed border  ]   ← Where it will land
└ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘

┌─────────────────────────────────────┐
│  📈 Member Growth  [☰]              │
└─────────────────────────────────────┘
```

**Visual Effects:**
- ✅ **Dragging Widget:** Slightly rotated (2deg), elevated shadow, 90% opacity
- ✅ **Placeholder:** Dashed border, gray background, 30% opacity
- ✅ **Other Widgets:** Smooth transition as they move to make space
- ✅ **Cursor:** Changes to "grabbing" or "move"

**After Drop:**
- ✅ Widget snaps smoothly into new position
- ✅ All widgets reorder with smooth animation
- ✅ Layout updates in component state (ready to save)

---

### 6. Save Layout

**Click "Save" Button:**

```
[Saving...]  ← Button text changes briefly
     ↓
[Save]  ← Returns to normal
     ↓
Edit Mode Exits Automatically
     ↓
Dashboard shows customized layout
```

**What Happens:**
1. Button shows "Saving..." for ~500ms
2. POST request sent to `/api/dashboard/layout`
3. Backend saves JSON to database
4. Success response received
5. Edit mode exits (buttons disappear)
6. Dashboard shows final layout

**Browser Console:**
```
✅ Layout saved successfully
```

---

### 7. Page Refresh Test

**Before Refresh:**
```
Custom layout:
[Birthdays] [Stats] [Member Growth]
[Events]    [Pastoral Care]
```

**After Refresh (F5):**
```
Same layout loads:
[Birthdays] [Stats] [Member Growth]
[Events]    [Pastoral Care]

✅ Positions preserved
✅ Visibility preserved
```

**What This Tests:**
- ✅ Layout persists to database
- ✅ Layout loads on component init
- ✅ Widget visibility map initialized correctly
- ✅ No "flash" of default layout before custom loads

---

### 8. Reset to Default

**Click "Reset" Button:**

```
┌─────────────────────────────────────────────────────┐
│  Are you sure you want to reset to the default     │
│  layout? This will discard your current             │
│  customizations.                                     │
│                                                      │
│              [Cancel]  [OK]                          │
└─────────────────────────────────────────────────────┘
```

**After Clicking OK:**
- ✅ All widgets return to original positions
- ✅ All widgets become visible again
- ✅ Edit mode exits
- ✅ POST request to `/api/dashboard/layout/reset`

**Result:**
```
Default layout restored:
[Stats Overview]        [Pastoral Care]
[Upcoming Events]       [Recent Activities]
[Birthdays Week]        [Anniversaries Month] [Irregular Attenders]
[Member Growth]         [Location Stats]
... (all 17 widgets in default order)
```

---

### 9. Mobile Responsive View

**Desktop (1920x1080):**
```
┌──────────────────────────────────────────────────────────┐
│  [Edit Mode]          [Widgets] [Save] [Reset] [Exit]   │ ← Horizontal
└──────────────────────────────────────────────────────────┘

┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐ ← 4 columns
│  Widget 1  │ │  Widget 2  │ │  Widget 3  │ │  Widget 4  │
└────────────┘ └────────────┘ └────────────┘ └────────────┘
```

**Tablet (768x1024):**
```
┌──────────────────────────────────────┐
│  [Edit Mode]                         │
│  [Widgets] [Save] [Reset] [Exit]    │ ← Still horizontal
└──────────────────────────────────────┘

┌──────────────┐ ┌──────────────┐      ← 2 columns
│  Widget 1    │ │  Widget 2    │
└──────────────┘ └──────────────┘
```

**Mobile (375x667):**
```
┌───────────────────────────┐
│  [Edit Mode]              │
├───────────────────────────┤
│  [Widgets]                │
│  [Save]                   │ ← Vertical stack
│  [Reset]                  │
│  [Exit]                   │
└───────────────────────────┘

┌───────────────────────────┐ ← 1 column
│  Widget 1                 │
└───────────────────────────┘
┌───────────────────────────┐
│  Widget 2                 │
└───────────────────────────┘
```

**Breakpoints:**
- **Desktop:** > 768px - Horizontal controls, 4-column grid
- **Tablet:** 768px - 1024px - Horizontal controls, 2-column grid
- **Mobile:** < 768px - Vertical controls, 1-column grid

---

## Color Scheme & Styling

### Phase 2.1 Theme Colors:
```
Primary Purple: #667eea → #764ba2 (gradient)
Success Green:  #10b981
Danger Red:     #ef4444
Gray Light:     #e5e7eb
Gray Dark:      #374151
White:          #ffffff
```

### Button States:
- **Normal:** White background, gray border
- **Hover:** Purple border, purple text, slight lift
- **Active:** Purple gradient background, white text
- **Disabled:** 50% opacity, no hover effect

### Animations:
- **Slide Down:** 0.3s ease-out (configurator panel)
- **Pulse:** 2s infinite ease-in-out (edit mode badge)
- **Drag Preview:** 250ms cubic-bezier (smooth snap)

---

## Error States

### Backend Not Running:
```
┌─────────────────────────────────────┐
│  ⚠️ Failed to load dashboard data   │
│                                      │
│  [Retry]                             │
└─────────────────────────────────────┘
```

### Layout Load Failed:
- Fallback to default layout (all widgets visible)
- Console warning: `Error loading user layout: ...`
- No error shown to user (graceful degradation)

### Save Failed:
```
Browser Console:
❌ Error saving layout: Network error

Button:
[Save] ← Returns to normal (doesn't stay stuck on "Saving...")
```

### Invalid Layout JSON:
- parseLayoutConfig() returns default config
- Console error logged
- Dashboard shows default layout

---

## Browser Console Messages

### Successful Flow:
```javascript
// On page load
Loading user layout...
✅ User layout loaded: My Dashboard

// On widget toggle
Widget visibility changed: birthdays_week → false

// On save
Saving layout...
✅ Layout saved successfully

// On reset
Resetting layout...
✅ Layout reset successfully
```

### Error Flow:
```javascript
// Network error
❌ Error loading user layout: Failed to fetch
Using default layout

// Invalid JSON
❌ Error parsing layout config: Unexpected token
Returning default config

// Save failed
❌ Error saving layout: 500 Internal Server Error
```

---

## Network Tab Inspection

### Expected API Calls (in order):

1. **Page Load:**
   ```
   GET /api/dashboard/data              → 200 OK
   GET /api/dashboard/widgets/available → 200 OK
   GET /api/dashboard/layout            → 200 OK
   GET /api/dashboard/birthdays-week    → 200 OK
   GET /api/dashboard/anniversaries     → 200 OK
   ... (other widget data endpoints)
   ```

2. **Toggle Widget Visibility:**
   - No API call (client-side only until save)

3. **Save Layout:**
   ```
   POST /api/dashboard/layout
   Request Body: {
     "layoutName": "My Dashboard",
     "layoutConfig": "{\"version\":1,\"widgets\":[...]}"
   }
   Response: 200 OK
   ```

4. **Reset Layout:**
   ```
   POST /api/dashboard/layout/reset
   Request Body: {}
   Response: 200 OK with default layout
   ```

### Request Headers (all requests):
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6...
Content-Type: application/json
```

### Response Format:
```json
// GET /api/dashboard/layout
{
  "id": 123,
  "layoutName": "My Dashboard",
  "isDefault": true,
  "layoutConfig": "{\"version\":1,\"gridColumns\":4,\"widgets\":[{\"widgetKey\":\"stats_overview\",\"position\":{\"x\":0,\"y\":0},\"size\":{\"width\":2,\"height\":1},\"visible\":true},...]}",
  "updatedAt": "2025-12-28T13:45:00"
}
```

---

## Performance Metrics

### Target Performance:
- **API Response Time:** < 100ms
- **Layout Save:** < 200ms
- **Widget Toggle:** < 16ms (instant)
- **Drag-Drop Animation:** 60fps
- **Page Load (with layout):** < 2 seconds

### How to Measure:
1. Open DevTools → Network tab
2. Check "Disable cache"
3. Reload page
4. Look at timing for `/api/dashboard/layout` request:
   - **TTFB (Time to First Byte):** Should be < 50ms
   - **Content Download:** Should be < 10ms
   - **Total:** Should be < 100ms

---

## Testing Completion Criteria

### All Tests Pass When:
- [x] "Customize" button appears and works
- [x] Edit mode badge displays correctly
- [x] Widget configurator panel opens/closes smoothly
- [x] All 17 widgets appear in configurator
- [x] Toggle switches change widget visibility immediately
- [x] Drag-and-drop shows visual feedback (if cdkDrag added)
- [x] "Save" button persists layout to database
- [x] Refresh page loads saved layout
- [x] "Reset" button restores default with confirmation
- [x] Mobile responsive layout works on small screens
- [x] No console errors during normal operation
- [x] Network requests complete successfully (200 OK)
- [x] Performance metrics within targets

---

## Screenshots to Take (for Documentation)

Recommended screenshots for documentation:

1. **Default Dashboard** - Before customization
2. **Edit Mode Active** - Showing all edit controls
3. **Widget Configurator Open** - Panel with toggles
4. **Drag Preview** - Mid-drag with placeholder
5. **Custom Layout Saved** - After customization
6. **Reset Confirmation** - Dialog box
7. **Mobile View** - Responsive layout
8. **Network Tab** - Successful API calls
9. **Console Logs** - Success messages
10. **Database View** - Saved layout in MySQL

---

**Document Version:** 1.0
**Last Updated:** 2025-12-28
**Status:** Ready for Visual Testing
