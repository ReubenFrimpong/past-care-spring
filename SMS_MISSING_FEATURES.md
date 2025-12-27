# SMS Page - Missing Critical Features

## Date: 2025-12-27
## Status: INCOMPLETE - Critical features missing

---

## 🚨 CRITICAL MISSING FEATURES

### 1. Insufficient Credit Checks ⚠️ HIGH PRIORITY

**Current Issue**:
- No pre-send validation to check if user has sufficient credits
- Users can attempt to send SMS without enough balance
- Only shows error AFTER attempting to send (HTTP 402 error)

**Required Implementation**:

#### Frontend Changes:
1. **Pre-send Credit Validation** in `sms-page.ts`:
   ```typescript
   canSendSms(): boolean {
     const estimatedCost = this.estimatedCost();
     const currentBalance = this.stats().currentBalance;
     return currentBalance >= estimatedCost;
   }

   canSendBulkSms(): boolean {
     const estimatedCost = this.bulkEstimatedCost();
     const currentBalance = this.stats().currentBalance;
     return currentBalance >= estimatedCost;
   }
   ```

2. **Disable Send Button** when insufficient credits:
   ```html
   <!-- Single SMS -->
   <button [disabled]="smsForm.invalid || sending() || !canSendSms()">
     Send SMS
   </button>

   <!-- Bulk SMS -->
   <button [disabled]="bulkSmsForm.invalid || sending() || !canSendBulkSms()">
     Send Bulk SMS
   </button>
   ```

3. **Warning Message** when credits are low:
   ```html
   @if (estimatedCost() > stats().currentBalance) {
     <div class="alert alert-error">
       <i class="pi pi-exclamation-triangle"></i>
       Insufficient credits! You need GHS {{ estimatedCost() - stats().currentBalance | number:'1.2-2' }} more.
       <button class="btn-link" (click)="openPurchaseDialog()">Purchase Credits</button>
     </div>
   }
   ```

4. **Add validation in sendSms() method**:
   ```typescript
   sendSms() {
     if (this.smsForm.invalid) return;

     const member = this.selectedMember();
     if (!member) {
       this.showError('Please select a member');
       return;
     }

     // NEW: Check sufficient credits
     if (!this.canSendSms()) {
       this.showError('Insufficient SMS credits. Please purchase more credits.');
       this.openPurchaseDialog();
       return;
     }

     // ... rest of send logic
   }
   ```

5. **Add validation in sendBulkSms() method**:
   ```typescript
   sendBulkSms() {
     if (this.bulkSmsForm.invalid) return;

     const phoneNumbers = this.parsePhoneNumbers(formValue.phoneNumbers);
     if (phoneNumbers.length === 0) {
       this.showError('Please enter at least one valid phone number');
       return;
     }

     // NEW: Check sufficient credits
     if (!this.canSendBulkSms()) {
       this.showError(`Insufficient credits! Need GHS ${this.bulkEstimatedCost()} but only have GHS ${this.stats().currentBalance}`);
       this.openPurchaseDialog();
       return;
     }

     // ... rest of send logic
   }
   ```

**Files to Modify**:
- [x] `sms-page.ts` - Add canSendSms() and canSendBulkSms() methods
- [x] `sms-page.ts` - Add validation in sendSms() and sendBulkSms()
- [x] `sms-page.html` - Disable buttons when insufficient credits
- [x] `sms-page.html` - Show warning alert when credits are low
- [x] `sms-page.css` - Add btn-link styling for inline purchase link

---

### 2. Enhanced Bulk SMS Selection Options ⚠️ HIGH PRIORITY

**Current Issue**:
- Bulk SMS only allows manual phone number entry
- No option to select multiple members
- No option to send to fellowships
- No option to send to entire church
- Very limited compared to backend capabilities

**Required Implementation**:

#### Backend Endpoints Available:
```java
// Already implemented in backend:
POST /api/communications/sms/send-bulk              // Phone numbers
POST /api/communications/sms/send-to-members        // Member IDs
POST /api/communications/sms/send-to-fellowship     // Fellowship ID
POST /api/communications/sms/send-to-all-members    // All members
```

#### Frontend Changes Needed:

1. **Update Bulk SMS Form Model** in `sms-page.ts`:
   ```typescript
   bulkSmsForm = this.fb.group({
     recipientType: ['phones', Validators.required],  // NEW: phones | members | fellowship | all
     phoneNumbers: [''],                              // For manual entry
     memberIds: [[]],                                 // For member selection
     fellowshipId: [null],                            // For fellowship selection
     message: ['', [Validators.required, Validators.maxLength(1600)]],
     scheduledTime: [null]
   });
   ```

2. **Add Recipient Type Selection** in `sms-page.html`:
   ```html
   <div class="bulk-recipient-tabs">
     <button type="button" [class.active]="bulkRecipientType === 'phones'"
             (click)="setBulkRecipientType('phones')">
       <i class="pi pi-hashtag"></i> Phone Numbers
     </button>
     <button type="button" [class.active]="bulkRecipientType === 'members'"
             (click)="setBulkRecipientType('members')">
       <i class="pi pi-users"></i> Select Members
     </button>
     <button type="button" [class.active]="bulkRecipientType === 'fellowship'"
             (click)="setBulkRecipientType('fellowship')">
       <i class="pi pi-sitemap"></i> Fellowship
     </button>
     <button type="button" [class.active]="bulkRecipientType === 'all'"
             (click)="setBulkRecipientType('all')">
       <i class="pi pi-globe"></i> Entire Church
     </button>
   </div>
   ```

3. **Phone Numbers Option** (current implementation - keep as is):
   ```html
   @if (bulkRecipientType === 'phones') {
     <textarea formControlName="phoneNumbers" rows="5"
               placeholder="Enter phone numbers..."></textarea>
   }
   ```

4. **Select Members Option** (NEW):

   **Approach A: Enhance Existing MemberSearchComponent** (Recommended)

   Add multi-select support to `member-search.component.ts`:
   ```typescript
   @Input() multiple = false;
   @Output() membersSelected = new EventEmitter<Member[]>();
   selectedMembers = signal<Member[]>([]);

   selectMember(member: Member) {
     if (this.multiple) {
       // Add to selection if not already selected
       const current = this.selectedMembers();
       if (!current.find(m => m.id === member.id)) {
         this.selectedMembers.set([...current, member]);
         this.membersSelected.emit(this.selectedMembers());
       }
       this.searchQuery = ''; // Clear search for next selection
     } else {
       // Single selection (existing behavior)
       this.selectedMember.set(member);
       this.memberSelected.emit(member);
       this.searchQuery = `${member.firstName} ${member.lastName}`;
       this.showDropdown.set(false);
     }
   }

   removeMember(memberId: number) {
     const updated = this.selectedMembers().filter(m => m.id !== memberId);
     this.selectedMembers.set(updated);
     this.membersSelected.emit(updated);
   }
   ```

   Add chips display to template:
   ```html
   @if (multiple && selectedMembers().length > 0) {
     <div class="selected-chips">
       @for (member of selectedMembers(); track member.id) {
         <div class="member-chip">
           <span>{{ member.firstName }} {{ member.lastName }}</span>
           <button type="button" (click)="removeMember(member.id)">
             <i class="pi pi-times"></i>
           </button>
         </div>
       }
     </div>
   }
   ```

   **Usage in SMS Page**:
   ```html
   @if (bulkRecipientType === 'members') {
     <div class="form-group">
       <label>Select Members</label>
       <app-member-search
         [multiple]="true"
         [placeholder]="'Search and select members...'"
         (membersSelected)="onBulkMembersSelected($event)">
       </app-member-search>
       <small class="form-hint">Search and click to add members to bulk SMS list</small>

       @if (selectedBulkMembers().length > 0) {
         <div class="bulk-members-count">
           <i class="pi pi-users"></i>
           {{ selectedBulkMembers().length }} members selected
         </div>
       }
     </div>
   }
   ```

   **Approach B: Use Existing Component Multiple Times** (Simpler but less elegant)

   Use the existing single-select component and add members one by one:
   ```html
   @if (bulkRecipientType === 'members') {
     <div class="form-group">
       <label>Add Members</label>
       <app-member-search
         [placeholder]="'Search and add member...'"
         (memberSelected)="addBulkMember($event)">
       </app-member-search>
       <small class="form-hint">Search and select to add members to the list</small>
     </div>

     @if (selectedBulkMembers().length > 0) {
       <div class="selected-members-list">
         <div class="selected-count">
           <i class="pi pi-users"></i>
           {{ selectedBulkMembers().length }} members selected
         </div>
         <div class="member-chips">
           @for (member of selectedBulkMembers(); track member.id) {
             <div class="member-chip">
               {{ member.firstName }} {{ member.lastName }}
               <button type="button" (click)="removeBulkMember(member.id)">
                 <i class="pi pi-times"></i>
               </button>
             </div>
           }
         </div>
       </div>
     }
   }
   ```

   **Recommended**: Use Approach A (enhance existing component) for better UX and reusability.

5. **Fellowship Selection Option** (NEW):
   ```html
   @if (bulkRecipientType === 'fellowship') {
     <div class="fellowship-selector">
       <select formControlName="fellowshipId" class="form-select">
         <option value="">Select a fellowship...</option>
         @for (fellowship of fellowships(); track fellowship.id) {
           <option [value]="fellowship.id">
             {{ fellowship.name }} ({{ fellowship.memberCount }} members)
           </option>
         }
       </select>

       @if (selectedFellowship()) {
         <div class="fellowship-info">
           <i class="pi pi-info-circle"></i>
           Will send to {{ selectedFellowship().memberCount }} members in {{ selectedFellowship().name }}
         </div>
       }
     </div>
   }
   ```

6. **Entire Church Option** (NEW):
   ```html
   @if (bulkRecipientType === 'all') {
     <div class="church-wide-warning">
       <div class="alert alert-warning">
         <i class="pi pi-exclamation-triangle"></i>
         <strong>Church-wide SMS</strong>
         <p>This will send SMS to ALL members in the database ({{ totalMemberCount() }} members).</p>
         <p>Estimated Cost: <strong>GHS {{ churchWideEstimatedCost() | number:'1.2-2' }}</strong></p>
       </div>

       <label class="confirmation-checkbox">
         <input type="checkbox" [(ngModel)]="confirmChurchWide">
         I confirm I want to send to the entire church
       </label>
     </div>
   }
   ```

7. **Add FellowshipService** to `sms-page.ts`:
   ```typescript
   fellowships = signal<Fellowship[]>([]);
   selectedBulkMembers = signal<Member[]>([]);
   bulkRecipientType: 'phones' | 'members' | 'fellowship' | 'all' = 'phones';
   confirmChurchWide = false;

   loadFellowships() {
     this.fellowshipService.getFellowships().subscribe({
       next: (data) => this.fellowships.set(data),
       error: () => {}
     });
   }

   setBulkRecipientType(type: 'phones' | 'members' | 'fellowship' | 'all') {
     this.bulkRecipientType = type;
     this.resetBulkRecipients();
   }

   onBulkMembersSelected(members: Member[]) {
     this.selectedBulkMembers.set(members);
     const memberIds = members.map(m => m.id);
     this.bulkSmsForm.patchValue({ memberIds });
     this.calculateBulkCost();
   }

   removeBulkMember(memberId: number) {
     const updated = this.selectedBulkMembers().filter(m => m.id !== memberId);
     this.selectedBulkMembers.set(updated);
     this.onBulkMembersSelected(updated);
   }
   ```

8. **Update sendBulkSms() method**:
   ```typescript
   sendBulkSms() {
     if (this.bulkSmsForm.invalid) return;

     const formValue = this.bulkSmsForm.value;
     let request: any;
     let endpoint: string;

     switch (this.bulkRecipientType) {
       case 'phones':
         const phoneNumbers = this.parsePhoneNumbers(formValue.phoneNumbers);
         if (phoneNumbers.length === 0) {
           this.showError('Please enter at least one valid phone number');
           return;
         }
         request = { recipientPhones: phoneNumbers, message: formValue.message };
         endpoint = 'send-bulk';
         break;

       case 'members':
         if (this.selectedBulkMembers().length === 0) {
           this.showError('Please select at least one member');
           return;
         }
         request = { memberIds: formValue.memberIds, message: formValue.message };
         endpoint = 'send-to-members';
         break;

       case 'fellowship':
         if (!formValue.fellowshipId) {
           this.showError('Please select a fellowship');
           return;
         }
         request = { fellowshipId: formValue.fellowshipId, message: formValue.message };
         endpoint = 'send-to-fellowship';
         break;

       case 'all':
         if (!this.confirmChurchWide) {
           this.showError('Please confirm you want to send to the entire church');
           return;
         }
         request = { message: formValue.message };
         endpoint = 'send-to-all-members';
         break;
     }

     // Check sufficient credits
     if (!this.canSendBulkSms()) {
       this.showError('Insufficient SMS credits');
       this.openPurchaseDialog();
       return;
     }

     this.sending.set(true);
     this.smsService.sendBulkSms(endpoint, request).subscribe({
       next: (results) => {
         this.showSuccess(`SMS sent successfully to ${results.length} recipient(s)`);
         this.resetBulkForm();
         this.loadSmsHistory();
         this.loadStats();
         this.sending.set(false);
       },
       error: (err) => {
         this.sending.set(false);
         this.showError('Failed to send bulk SMS');
       }
     });
   }
   ```

**Files to Create/Modify**:
- [ ] `member-search.component.ts` - Add [multiple] input support (Approach A)
- [ ] `member-search.component.ts` - Add membersSelected output
- [ ] `member-search.component.ts` - Add selectedMembers signal
- [ ] `member-search.component.ts` - Update selectMember() for multi-select
- [ ] `member-search.component.ts` - Add removeMember() method
- [ ] `member-search.component.ts` - Add chips display to template
- [ ] `member-search.component.ts` - Add CSS for member chips
- [ ] `sms-page.ts` - Add bulkRecipientType property
- [ ] `sms-page.ts` - Add FellowshipService injection
- [ ] `sms-page.ts` - Add fellowships signal and loadFellowships()
- [ ] `sms-page.ts` - Add selectedBulkMembers signal
- [ ] `sms-page.ts` - Add setBulkRecipientType() method
- [ ] `sms-page.ts` - Add onBulkMembersSelected() method (or addBulkMember for Approach B)
- [ ] `sms-page.ts` - Add removeBulkMember() method
- [ ] `sms-page.ts` - Update sendBulkSms() to handle all recipient types
- [ ] `sms-page.html` - Add bulk recipient type tabs
- [ ] `sms-page.html` - Add member multi-select UI (using enhanced component)
- [ ] `sms-page.html` - Add fellowship selector
- [ ] `sms-page.html` - Add church-wide confirmation
- [ ] `sms-page.css` - Add bulk-recipient-tabs styling
- [ ] `sms-page.css` - Add member-chips styling (for SMS page display)
- [ ] `sms-page.css` - Add fellowship-info styling
- [ ] `sms.service.ts` - Update sendBulkSms() to accept dynamic endpoint

---

### 3. Character Count and Cost Estimation ⚠️ MEDIUM PRIORITY

**Current Status**:
- ✅ Character count is working (frontend computed signals)
- ✅ Message count calculation working (160 chars per SMS)
- ⚠️ Cost estimation PARTIALLY working but has issues

**Issues**:

1. **Cost estimation only triggers on input event**:
   - Doesn't calculate when member is first selected
   - Should auto-calculate when recipient changes

2. **No cost breakdown display**:
   - Doesn't show per-SMS rate
   - Doesn't show if message is local vs international
   - Doesn't show country-specific pricing

3. **Bulk cost calculation oversimplified**:
   - Uses first number's cost and multiplies
   - Doesn't account for different countries in bulk list
   - Should calculate per-recipient cost

**Required Improvements**:

#### 1. Auto-calculate on recipient change:
```typescript
// In onMemberSelected()
onMemberSelected(member: Member | null) {
  this.selectedMember.set(member);
  if (member) {
    this.smsForm.patchValue({ memberId: member.id });
    this.calculateCost();  // Already doing this ✅
  } else {
    this.smsForm.patchValue({ memberId: null });
    this.estimatedCost.set(0);
  }
}
```

#### 2. Show detailed cost breakdown:
```html
<!-- Enhanced cost display -->
<div class="cost-breakdown">
  <div class="cost-item">
    <span class="cost-label">Message Parts:</span>
    <span class="cost-value">{{ messageCount() }} SMS</span>
  </div>

  @if (costDetails()) {
    <div class="cost-item">
      <span class="cost-label">Rate per SMS:</span>
      <span class="cost-value">GHS {{ costDetails().ratePerSms | number:'1.4-4' }}</span>
    </div>

    <div class="cost-item">
      <span class="cost-label">Destination:</span>
      <span class="cost-value">
        {{ costDetails().destination }}
        <span class="badge" [class.badge-local]="costDetails().isLocal">
          {{ costDetails().isLocal ? 'Local' : 'International' }}
        </span>
      </span>
    </div>
  }

  <div class="cost-item total">
    <span class="cost-label">Total Cost:</span>
    <span class="cost-value">GHS {{ estimatedCost() | number:'1.2-2' }}</span>
  </div>
</div>
```

#### 3. Store cost details from API:
```typescript
costDetails = signal<CalculateSmsCostResponse | null>(null);

calculateCost() {
  const member = this.selectedMember();
  const phone = member?.phoneNumber || '';
  const message = this.smsForm.get('message')?.value;

  if (phone && message) {
    this.creditService.calculateCost({ phoneNumber: phone, message }).subscribe({
      next: (result) => {
        this.estimatedCost.set(result.cost);
        this.costDetails.set(result);  // NEW: Store full details
      },
      error: () => {
        this.estimatedCost.set(0);
        this.costDetails.set(null);
      }
    });
  } else {
    this.estimatedCost.set(0);
    this.costDetails.set(null);
  }
}
```

#### 4. Accurate bulk cost calculation:
```typescript
calculateBulkCost() {
  const phoneNumbers = this.bulkSmsForm.get('phoneNumbers')?.value || '';
  const message = this.bulkSmsForm.get('message')?.value;
  const phones = this.parsePhoneNumbers(phoneNumbers);

  if (phones.length > 0 && message) {
    // NEW: Calculate for each phone number, then sum
    const costRequests = phones.map(phone =>
      this.creditService.calculateCost({ phoneNumber: phone, message }).toPromise()
    );

    Promise.all(costRequests).then(results => {
      const totalCost = results.reduce((sum, r) => sum + r.cost, 0);
      this.bulkEstimatedCost.set(totalCost);
    }).catch(() => {
      this.bulkEstimatedCost.set(0);
    });
  } else {
    this.bulkEstimatedCost.set(0);
  }
}
```

**Files to Modify**:
- [x] `sms-page.ts` - Add costDetails signal
- [x] `sms-page.ts` - Update calculateCost() to store details
- [x] `sms-page.ts` - Improve calculateBulkCost() accuracy
- [x] `sms-page.html` - Add cost breakdown display
- [x] `sms-page.css` - Add cost-breakdown styling

---

### 4. Automatic Credit Deduction and Refund ⚠️ BACKEND FEATURE

**Current Status**: ✅ ALREADY IMPLEMENTED IN BACKEND

**Backend Implementation** (from `SmsService.java`):
```java
// Automatic deduction on send
private void deductCreditsForSms(User sender, BigDecimal cost, SmsMessage smsMessage) {
    smsCreditService.deductCredits(sender, cost,
        "SMS to " + smsMessage.getRecipientPhone(),
        smsMessage.getId());
}

// Automatic refund on failure
private void refundCredits(SmsMessage smsMessage) {
    smsCreditService.refundCredits(
        smsMessage.getSender(),
        smsMessage.getCost(),
        "Refund for failed SMS",
        smsMessage.getId());
}
```

**What Happens Automatically**:
1. ✅ Credits deducted immediately when SMS is sent
2. ✅ Credits refunded if SMS fails to deliver
3. ✅ Transaction history created for both deduction and refund
4. ✅ Balance updated in real-time

**Frontend Requirements**:
- [x] Show updated balance after SMS send (already implemented via `loadStats()`)
- [x] Show transaction history (DEFERRED - needs separate page)
- [ ] Show notification when refund occurs (NEW)

**Optional Enhancement - Refund Notifications**:
```typescript
// In SMS history, check for refunded messages
@if (sms.status === 'FAILED' && sms.refunded) {
  <span class="refund-badge">
    <i class="pi pi-undo"></i>
    Refunded
  </span>
}
```

---

## 📋 IMPLEMENTATION CHECKLIST

### Phase 1: Critical Credit Checks (HIGH PRIORITY)
- [ ] Add `canSendSms()` computed method
- [ ] Add `canSendBulkSms()` computed method
- [ ] Add pre-send validation in `sendSms()`
- [ ] Add pre-send validation in `sendBulkSms()`
- [ ] Disable send buttons when insufficient credits
- [ ] Show warning alert when credits are low
- [ ] Add btn-link styling for inline purchase link
- [ ] Test insufficient credit scenarios

**Estimated Time**: 2-3 hours

---

### Phase 2: Enhanced Bulk Selection (HIGH PRIORITY)

#### Step 1: Enhance MemberSearchComponent for Multi-Select
- [ ] Add `@Input() multiple = false` property
- [ ] Add `@Output() membersSelected` EventEmitter
- [ ] Add `selectedMembers` signal for tracking multiple selections
- [ ] Update `selectMember()` method to handle both single and multi-select
- [ ] Add `removeMember(memberId)` method
- [ ] Add chips display template for multi-select
- [ ] Add CSS styling for member chips
- [ ] Test multi-select functionality

#### Step 2: Add Bulk Recipient Type System
- [ ] Add `bulkRecipientType` property to sms-page
- [ ] Add bulk recipient type tabs UI (Phones, Members, Fellowship, All Church)
- [ ] Add CSS for bulk recipient tabs
- [ ] Add `setBulkRecipientType()` method

#### Step 3: Implement "Select Members" Option
- [ ] Use enhanced MemberSearchComponent with `[multiple]="true"`
- [ ] Add `selectedBulkMembers` signal
- [ ] Add `onBulkMembersSelected()` method
- [ ] Add `removeBulkMember()` method
- [ ] Add member count display
- [ ] Add CSS for bulk members display

#### Step 4: Implement "Fellowship" Option
- [ ] Add FellowshipService injection
- [ ] Add `fellowships` signal
- [ ] Add `loadFellowships()` method in ngOnInit
- [ ] Add fellowship selector dropdown UI
- [ ] Show fellowship member count in dropdown
- [ ] Add fellowship info display
- [ ] Add CSS for fellowship selector

#### Step 5: Implement "Entire Church" Option
- [ ] Add `confirmChurchWide` checkbox flag
- [ ] Add church-wide warning alert UI
- [ ] Add confirmation checkbox
- [ ] Load and display total member count
- [ ] Calculate church-wide estimated cost
- [ ] Add CSS for church-wide warning

#### Step 6: Update Send Logic
- [ ] Update `sendBulkSms()` to handle all 4 recipient types
- [ ] Add switch/case for recipient type routing
- [ ] Update `SmsService.sendBulkSms()` to accept dynamic endpoints
- [ ] Add validation for each recipient type
- [ ] Test all recipient type options

**Estimated Time**: 8-10 hours (includes MemberSearchComponent enhancement)

---

### Phase 3: Improved Cost Estimation (MEDIUM PRIORITY)
- [ ] Add `costDetails` signal
- [ ] Update `calculateCost()` to store full details
- [ ] Create cost breakdown UI component
- [ ] Add local/international badge
- [ ] Improve `calculateBulkCost()` to calculate per-number
- [ ] Add loading state during cost calculation
- [ ] Add CSS for cost breakdown display
- [ ] Test with local and international numbers

**Estimated Time**: 3-4 hours

---

### Phase 4: Optional Enhancements (LOW PRIORITY)
- [ ] Add refund notification in SMS history
- [ ] Add refund badge styling
- [ ] Create SMS transaction history page (separate feature)
- [ ] Add SMS template selector
- [ ] Add message preview feature

**Estimated Time**: 4-6 hours

---

## 📊 SUMMARY

### Total Items: 46
- **Critical**: 15 items (Phase 1 - Credit Checks)
- **High Priority**: 23 items (Phase 2 - Enhanced Bulk Selection)
  - Step 1: MemberSearchComponent enhancement (8 items)
  - Steps 2-6: Bulk selection features (15 items)
- **Medium**: 8 items (Phase 3 - Cost Estimation)
- **Optional**: 5 items (Phase 4 - Enhancements)
- **Already Complete**: 9 items (backend features)

### Estimated Total Time: 17-23 hours
- Phase 1: 2-3 hours
- Phase 2: 8-10 hours (includes component enhancement)
- Phase 3: 3-4 hours
- Phase 4: 4-6 hours

### Priority Order:
1. ⚠️ **Phase 1**: Insufficient credit checks (CRITICAL - prevents errors)
2. ⚠️ **Phase 2**: Enhanced bulk selection (HIGH - major UX improvement)
   - **Important**: Must enhance MemberSearchComponent first for multi-select support
   - Then add bulk recipient tabs (Phones, Members, Fellowship, All Church)
3. 📊 **Phase 3**: Cost estimation improvements (MEDIUM - better transparency)
4. ✨ **Phase 4**: Optional enhancements (LOW - nice to have)

### Key Decision Points:
- **MemberSearchComponent Enhancement**: Recommended to add multi-select support to existing component rather than create new one (better reusability)
- **Bulk Selection UI**: Tab-based interface with 4 options provides best UX
- **Fellowship Integration**: Requires FellowshipService - ensure it's available

---

## 🔗 Related Backend Endpoints

### Already Implemented:
```
POST /api/communications/sms/send                    // Single SMS
POST /api/communications/sms/send-bulk               // Phone numbers
POST /api/communications/sms/send-to-members         // Member IDs
POST /api/communications/sms/send-to-fellowship      // Fellowship ID
POST /api/communications/sms/send-to-all-members     // All members
POST /api/communications/sms/calculate-cost          // Cost estimation
GET  /api/communications/sms-credits/balance         // Current balance
POST /api/communications/sms-credits/purchase        // Purchase credits
```

All backend endpoints support automatic credit deduction and refund.

---

**Last Updated**: 2025-12-27
**Status**: Ready for implementation
