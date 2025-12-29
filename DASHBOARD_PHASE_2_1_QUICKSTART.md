# Dashboard Phase 2.1 - Quick Start Guide

**Last Updated:** 2025-12-28
**Purpose:** Get Phase 2.1 running and test all features

---

## Prerequisites ✅

- [x] Backend compiled successfully
- [x] Frontend built successfully (you mentioned this is done!)
- [x] MySQL database running
- [x] User account with credentials

---

## Step 1: Start the Backend (5 minutes)

### Option A: Using Maven (Development)
```bash
cd /home/reuben/Documents/workspace/pastcare-spring

# Start the backend
./mvnw spring-boot:run
```

**Expected Output:**
```
Started PastcareSpringApplication in X seconds
Flyway migration successful: V47, V48
Server running on port 8080
```

### Option B: Using JAR (Production-like)
```bash
cd /home/reuben/Documents/workspace/pastcare-spring

# Build JAR
./mvnw clean package -DskipTests

# Run JAR
java -jar target/pastcare-spring-0.0.1-SNAPSHOT.jar
```

### Verify Backend is Running
```bash
# In a new terminal
curl http://localhost:8080/actuator/health

# Expected: {"status":"UP"}
```

**Keep this terminal running** ✅

---

## Step 2: Start the Frontend (2 minutes)

```bash
# Open NEW terminal
cd /home/reuben/Documents/workspace/past-care-spring-frontend

# Start dev server
npm start
```

**Expected Output:**
```
Angular Live Development Server is listening on localhost:4200
✔ Compiled successfully
```

**Keep this terminal running** ✅

---

## Step 3: Test Backend APIs (5 minutes)

Run the automated testing script:

```bash
# Open NEW terminal
cd /home/reuben/Documents/workspace/pastcare-spring

# Run Phase 2.1 tests
./test-dashboard-phase-2-1.sh
```

The script will:
1. ✅ Check backend health
2. ✅ Login and get auth token
3. ✅ Test GET /api/dashboard/widgets/available (should return 17 widgets)
4. ✅ Test GET /api/dashboard/layout (creates default if not exists)
5. ✅ Test POST /api/dashboard/layout (save custom layout)
6. ✅ Test POST /api/dashboard/layout/reset (reset to default)
7. ✅ Verify role-based filtering works
8. ✅ Test concurrent updates

**Expected Result:** All tests should pass ✅

---

## Step 4: Test Frontend UI (10-15 minutes)

### 4.1 Open Application
1. Open browser: http://localhost:4200
2. Login with your credentials
3. Navigate to Dashboard

### 4.2 Verify Default Dashboard Loads
**What to Look For:**
- ✅ All widgets display (birthdays, anniversaries, stats, etc.)
- ✅ "Customize" button appears in top-right area
- ✅ No errors in browser console (F12)

### 4.3 Test Edit Mode
**Steps:**
1. Click "Customize" button
2. **Expected:** Button changes to "Exit Edit", edit mode badge appears
3. **Expected:** Three new buttons appear: "Widgets", "Save", "Reset"

### 4.4 Test Widget Configurator
**Steps:**
1. Click "Widgets" button
2. **Expected:** Panel slides down showing all available widgets
3. Toggle OFF "Birthdays This Week"
4. **Expected:** Birthday widget immediately disappears from dashboard
5. Toggle ON "Birthdays This Week"
6. **Expected:** Birthday widget immediately reappears
7. Close configurator panel (X button)

### 4.5 Test Drag-and-Drop (if cdkDrag added)
**Steps:**
1. In edit mode, hover over a widget
2. **Expected:** Cursor changes to move cursor, dashed border appears
3. **Expected:** Drag handle (bars icon) appears in widget header
4. Click and drag a widget to new position
5. **Expected:** Visual drag preview appears
6. Drop widget
7. **Expected:** Widgets reorder smoothly

**Note:** If drag handles don't appear, cdkDrag attributes need to be added to individual widgets (see DASHBOARD_PHASE_2_1_FINAL_STATUS.md)

### 4.6 Test Save Layout
**Steps:**
1. Make some changes (toggle widgets, drag to reorder)
2. Click "Save" button
3. **Expected:** Button shows "Saving..." briefly
4. **Expected:** Edit mode exits automatically
5. Refresh the page (F5)
6. **Expected:** Your custom layout loads with same widget order and visibility

### 4.7 Test Reset Layout
**Steps:**
1. Click "Customize" to enter edit mode
2. Click "Reset" button
3. **Expected:** Confirmation dialog appears
4. Click "OK" to confirm
5. **Expected:** All widgets restore to default positions
6. **Expected:** All widgets become visible again
7. **Expected:** Edit mode exits automatically

### 4.8 Test Mobile Responsive
**Steps:**
1. Open browser dev tools (F12)
2. Toggle device toolbar (Ctrl+Shift+M or Cmd+Shift+M)
3. Select mobile device (e.g., iPhone 12)
4. **Expected:** Layout controls stack vertically
5. **Expected:** Widgets adapt to mobile width
6. **Expected:** All buttons remain clickable

---

## Step 5: Verify Database Persistence (5 minutes)

### Check Widgets Table
```sql
-- Connect to MySQL
mysql -u root -p

USE pastcare_db;

-- Should return 17 rows
SELECT COUNT(*) FROM widgets;

-- View all widgets
SELECT widget_key, name, category, required_role
FROM widgets
ORDER BY category, name;

-- Expected output:
-- stats_overview, pastoral_care, upcoming_events, birthdays_week, etc.
```

### Check Dashboard Layouts Table
```sql
-- View user layouts
SELECT id, user_id, church_id, layout_name, is_default, created_at
FROM dashboard_layouts
ORDER BY created_at DESC
LIMIT 5;

-- Check your specific layout
SELECT layout_config
FROM dashboard_layouts
WHERE user_id = YOUR_USER_ID
LIMIT 1;

-- Expected: JSON string with widgets array
```

---

## Troubleshooting

### Backend Won't Start
**Issue:** `Port 8080 is already in use`
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or use different port
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

**Issue:** `Flyway migration failed`
```bash
# Check migration status
mysql -u root -p -D pastcare_db -e "SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;"

# If V47 or V48 failed, fix the SQL and re-run
```

### Frontend Won't Start
**Issue:** `Port 4200 is already in use`
```bash
# Kill process on 4200
lsof -i :4200
kill -9 <PID>

# Or start on different port
ng serve --port 4201
```

**Issue:** `Module not found` errors
```bash
# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install
npm start
```

### API Returns 401 Unauthorized
**Issue:** Token expired or invalid
```bash
# Re-login in the frontend
# Or get new token via API:
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"your@email.com","password":"yourpassword"}'
```

### Widgets Not Loading
**Issue:** Empty widgets array returned
```bash
# Check backend logs for errors
# Verify migrations ran: SELECT * FROM widgets;

# If empty, manually insert widgets:
# Run V47__create_widgets_table.sql manually
```

### Layout Not Saving
**Issue:** Click "Save" but layout doesn't persist
1. Check browser console for errors (F12 → Console)
2. Check Network tab (F12 → Network) for failed POST request
3. Verify backend logs show POST /api/dashboard/layout
4. Check database: `SELECT * FROM dashboard_layouts WHERE user_id = X;`

### Drag-and-Drop Not Working
**Issue:** Can't drag widgets
- **Most Likely:** cdkDrag attributes not added to individual widget-card elements
- **Solution:** See DASHBOARD_PHASE_2_1_FINAL_STATUS.md section "Remaining Polish Items"
- **Quick Test:** Check if drag handle (bars icon) appears in widget headers in edit mode

---

## Expected Results Summary

| Test | Expected Result |
|------|-----------------|
| Backend Health | ✅ Returns `{"status":"UP"}` |
| GET /widgets/available | ✅ Returns 17 widgets JSON array |
| GET /layout | ✅ Returns user layout or creates default |
| POST /layout | ✅ Saves and returns layout |
| POST /layout/reset | ✅ Resets to default layout |
| Frontend Loads | ✅ Dashboard displays all widgets |
| Click Customize | ✅ Edit mode activates |
| Click Widgets | ✅ Configurator panel opens |
| Toggle Visibility | ✅ Widget shows/hides immediately |
| Click Save | ✅ Layout persists to database |
| Refresh Page | ✅ Saved layout loads |
| Click Reset | ✅ Restores default layout |
| Mobile View | ✅ Responsive layout works |

---

## Testing Checklist

Copy this checklist and mark items as you test:

### Backend Tests
- [ ] Backend starts without errors
- [ ] Migrations V47 and V48 run successfully
- [ ] 17 widgets seeded in database
- [ ] GET /api/dashboard/widgets/available returns widgets
- [ ] GET /api/dashboard/layout returns or creates layout
- [ ] POST /api/dashboard/layout saves successfully
- [ ] POST /api/dashboard/layout/reset works
- [ ] Role-based filtering works (if applicable)

### Frontend Tests
- [ ] Frontend loads at http://localhost:4200
- [ ] Dashboard displays all widgets
- [ ] "Customize" button appears
- [ ] Clicking "Customize" enters edit mode
- [ ] Edit mode badge appears
- [ ] "Widgets", "Save", "Reset" buttons appear in edit mode
- [ ] Widget configurator panel opens
- [ ] Toggle switches show for all 17 widgets
- [ ] Toggling widget OFF hides it immediately
- [ ] Toggling widget ON shows it immediately
- [ ] Close configurator works
- [ ] Drag-and-drop works (if cdkDrag added)
- [ ] "Save" button persists layout
- [ ] Refresh page loads saved layout
- [ ] "Reset" button restores defaults
- [ ] Mobile responsive works

### Integration Tests
- [ ] Layout saved in frontend appears in database
- [ ] Changes in database reflect in frontend
- [ ] Multiple users have separate layouts
- [ ] Church isolation works (users only see their church's data)

---

## Performance Checks

### Backend Performance
```bash
# Time the API calls
time curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/dashboard/widgets/available

# Expected: < 50ms
```

### Frontend Performance
1. Open DevTools (F12) → Performance tab
2. Click "Record"
3. Interact with dashboard (toggle widgets, save layout)
4. Stop recording
5. **Expected:** No long tasks (>50ms), smooth 60fps

### Database Performance
```sql
-- Check query execution time
EXPLAIN SELECT * FROM dashboard_layouts WHERE user_id = 1;

-- Should use PRIMARY KEY or INDEX
```

---

## Next Steps After Testing

### If All Tests Pass ✅
1. **Create unit tests** for DashboardLayoutService (frontend & backend)
2. **Create E2E tests** with Playwright
3. **Add cdkDrag to individual widgets** (if not done yet)
4. **Update PLAN.md** to mark Phase 2.1 as complete
5. **Deploy to staging** environment
6. **User acceptance testing** with stakeholders
7. **Plan Phase 2.2:** Role-Based Templates

### If Tests Fail ❌
1. Document the failing test
2. Check browser console and network tab for errors
3. Check backend logs for errors
4. Check database for correct data
5. Review code for typos or logic errors
6. Fix issues and re-test
7. Update this document with findings

---

## Support and Documentation

| Document | Purpose |
|----------|---------|
| DASHBOARD_PHASE_2_1_FRONTEND_GUIDE.md | Implementation guide |
| DASHBOARD_PHASE_2_1_FINAL_STATUS.md | Current status and remaining work |
| DASHBOARD_PHASE_2_1_IMPLEMENTATION_COMPLETE.md | Complete feature documentation |
| test-dashboard-phase-2-1.sh | Automated API testing script |
| DASHBOARD_PHASE_2_1_QUICKSTART.md | This document |

---

## Success! 🎉

If all tests pass, congratulations! You have successfully implemented and tested Dashboard Phase 2.1: Custom Layouts MVP.

**Phase 2.1 Features:**
✅ Customizable dashboard layouts
✅ Drag-and-drop widget reordering
✅ Widget visibility controls
✅ Layout persistence
✅ Role-based filtering
✅ Mobile responsive
✅ Error handling

**Ready for:** Production deployment and Phase 2.2 planning

---

**Date:** 2025-12-28
**Status:** Ready for Testing
**Estimated Testing Time:** 30-40 minutes
