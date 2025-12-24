# QR Code Check-In - Testing Guide

## Prerequisites

1. **Backend Running**: `./mvnw spring-boot:run`
2. **Frontend Running**: `npm start` (in frontend directory)
3. **Database**: MySQL running with past-care-spring database

---

## How to Test QR Code Check-In

### Step 1: Start the Application

**Terminal 1 - Backend:**
```bash
cd /home/reuben/Documents/workspace/pastcare-spring
./mvnw spring-boot:run
```

Wait for: `Started PastcareSpringApplication`

**Terminal 2 - Frontend:**
```bash
cd /home/reuben/Documents/workspace/past-care-spring-frontend
npm start
```

Wait for: `Application bundle generation complete.`

Open browser: `http://localhost:4200`

---

### Step 2: Create an Attendance Session

1. Login to the application
2. Navigate to **Attendance** page
3. Click **"New Session"** button
4. Fill in:
   - Session Name: "Sunday Service"
   - Date: Select today's date
   - Time: (optional) e.g., "09:00"
5. Click **"Create Session"**

---

### Step 3: Generate QR Code

1. Click on the session you just created
2. Click the **"QR Code"** button in the session header
3. A modal opens showing the QR code

**What the QR Code Should Contain:**

The QR code now encodes a **full URL** like:
```
http://localhost:4200/check-in?qr=AES_ENCRYPTED_SESSION_DATA
```

For example:
```
http://localhost:4200/check-in?qr=L3RzK0Nxc3dFUlpQVmR2WjBRPT0=
```

---

### Step 4: Test QR Code Scanning

**Option A - Use Your Phone:**

1. Open camera app on your phone (iPhone Camera or Android Camera app)
2. Point camera at the QR code on your computer screen
3. A notification should appear with the URL
4. Tap the notification → Opens check-in page on your phone

**Option B - Use Online QR Scanner:**

1. Right-click QR code → "Download QR Code" or take screenshot
2. Go to: https://webqr.com/
3. Click "Upload" and select the QR code image
4. Copy the decoded URL
5. Paste in your browser

**Option C - Manually Test (for Development):**

1. In the QR modal, look at the browser console (F12)
2. The QR code data is logged there (or check the message at bottom)
3. Copy the encrypted string from the message
4. Manually navigate to: `http://localhost:4200/check-in?qr=PASTE_ENCRYPTED_DATA`

---

### Step 5: Complete Check-In Flow

Once the check-in page opens:

**For Members:**
1. Toggle: **"I'm a Member"** (should be selected by default)
2. Enter phone number (use a phone number from your database)
3. Click **"Check In"**
4. Success screen should appear!

**For Visitors:**
1. Toggle: **"I'm a Visitor"**
2. Fill in:
   - First Name: "John"
   - Last Name: "Doe"
   - Phone: "1234567890"
   - Email: (optional) "john@example.com"
3. Click **"Check In as Visitor"**
4. Success screen should appear!

---

### Step 6: Verify Check-In

1. Go back to the attendance page (click "Back to Sessions" if still in session view)
2. Click on the session again
3. You should see the member/visitor marked as **Present**
4. Check the quick stats - "Present" count should be 1

---

## Troubleshooting

### Problem: QR Code Shows Encrypted Data Instead of URL

**Solution:** The QR code should contain a full URL starting with `http://localhost:4200/check-in?qr=`

Check the backend logs for the generated URL:
```bash
# In backend terminal, look for log message:
# "QR code generated successfully. Scan to check in at: http://..."
```

If you see just encrypted data, restart the backend:
```bash
# Ctrl+C to stop, then:
./mvnw spring-boot:run
```

### Problem: Check-In Page Says "Invalid QR Code"

**Causes:**
1. QR code expired (24 hour default)
2. Encrypted data corrupted
3. Wrong encryption key

**Solution:**
- Regenerate the QR code (click "Regenerate QR Code" button in modal)
- Check application.properties has `qrcode.secret.key=PastCareQRCode16`

### Problem: Phone Number Not Found

**Solution:**
1. Go to Members page
2. Check what phone numbers exist in your database
3. Use one of those numbers
4. Or create a new member first with that phone number

### Problem: Check-In Page Won't Load

**Causes:**
1. Frontend not running
2. Wrong URL
3. Route not configured

**Solution:**
```bash
# Check frontend is running:
curl http://localhost:4200

# Check route exists:
# Open browser and manually go to:
http://localhost:4200/check-in
# You should see error "Invalid check-in link. QR code data is missing."
```

---

## Expected Behavior

### ✅ Successful QR Code Scan:

1. QR scan → Opens URL in browser
2. URL format: `http://localhost:4200/check-in?qr=ENCRYPTED`
3. Page loads → Shows session info (name, date, time)
4. Enter details → Click "Check In"
5. Success animation → "Check-In Successful! Welcome, [Name]!"
6. Attendance marked in database

### ✅ Successful Member Check-In:

- Backend looks up member by phone number
- Creates attendance record with `checkInMethod: QR_CODE`
- Returns welcome message with member name
- Frontend shows success screen

### ✅ Successful Visitor Check-In:

- Backend creates/updates visitor record
- Increments visit count
- Marks `isFirstTime: true` for new visitors
- Creates visitor attendance record
- Different welcome message for first-time vs returning

---

## Testing Checklist

- [ ] QR code displays in modal
- [ ] QR code can be downloaded
- [ ] QR code encodes full URL (not just encrypted data)
- [ ] Scanning QR opens check-in page
- [ ] Session info displays correctly
- [ ] Member check-in works with valid phone
- [ ] Error shown for invalid phone number
- [ ] Visitor check-in creates new visitor
- [ ] Visitor check-in updates existing visitor
- [ ] Success screen displays with animation
- [ ] Attendance shows as "Present" in session view
- [ ] Late arrival tracking works (if checking in late)
- [ ] Can check in multiple people in one session

---

## Advanced Testing

### Test Late Arrivals:

1. Create session with time in the past (e.g., 1 hour ago)
2. Generate QR code
3. Check in now
4. Should be marked as late
5. Verify late arrivals section appears
6. Check minutes late is calculated correctly

### Test QR Expiry:

1. In application.properties, temporarily set:
   ```properties
   qrcode.default.expiry.hours=0
   ```
2. Restart backend
3. Generate QR code
4. Wait 1 minute
5. Scan QR code
6. Should show "This QR code has expired"

### Test Multiple Check-Ins:

1. Complete first check-in
2. Click "Check In Another Person"
3. Form resets
4. Can check in another member/visitor
5. Repeat for family members

---

## Production Deployment Notes

Before deploying to production, update **application.properties**:

```properties
# Change frontend URL to your production domain
app.frontend.url=https://yourchurch.com

# Use a secure random 16-character key
qrcode.secret.key=YOUR_SECURE_KEY_HERE

# Adjust expiry as needed (hours)
qrcode.default.expiry.hours=24
```

Generate secure key:
```bash
openssl rand -base64 12 | cut -c1-16
```

---

**Questions?** If QR code still doesn't work, check:
1. Backend logs for any errors
2. Frontend console (F12) for errors
3. Network tab to see API calls
4. Database to verify session was created

🤖 Generated with [Claude Code](https://claude.com/claude-code)
