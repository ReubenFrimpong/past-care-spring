# Manual Subscription Activation Feature

**Date**: December 30, 2025
**Purpose**: Allow superadmins to manually activate subscriptions when automatic payment processing fails

## Overview

This feature enables SUPERADMIN users to manually activate church subscriptions without requiring successful Paystack payment verification. This is critical for handling edge cases such as:

- Payment succeeded on Paystack but callback/webhook failed
- Manual payment methods (bank transfer, cash, etc.)
- Administrative overrides or promotional activations
- Payment gateway downtime or integration issues
- Testing and demonstration purposes

## Use Cases

### 1. Failed Payment Callback
**Scenario**: User completes payment on Paystack, but webhook fails or callback doesn't update subscription.

**Solution**: Superadmin can manually activate the subscription after verifying payment on Paystack dashboard.

### 2. Alternative Payment Methods
**Scenario**: Church pays via bank transfer, mobile money, or cash instead of online payment.

**Solution**: Superadmin confirms payment receipt and manually activates subscription.

### 3. Promotional/Trial Subscriptions
**Scenario**: Granting free access for marketing, partnerships, or special circumstances.

**Solution**: Superadmin activates subscription with reason "Promotional access - partnership with XYZ".

### 4. Emergency Override
**Scenario**: Critical church operations need immediate access during payment gateway downtime.

**Solution**: Superadmin activates temporarily, requiring payment later.

## Implementation Details

### Backend API

**Endpoint**: `POST /api/billing/platform/subscription/manual-activate`

**Authentication**: SUPERADMIN only (`@RequirePermission(Permission.PLATFORM_ACCESS)`)

**Request Body**:
```json
{
  "churchId": 123,
  "planId": 2,
  "durationMonths": 3,
  "reason": "Manual payment via bank transfer confirmed"
}
```

**Response (Success)**:
```json
{
  "success": true,
  "message": "Subscription manually activated successfully",
  "subscription": {
    "id": 456,
    "churchId": 123,
    "plan": {
      "id": 2,
      "name": "PROFESSIONAL",
      "displayName": "Professional Plan",
      "price": 150.00
    },
    "status": "ACTIVE",
    "currentPeriodStart": "2025-12-30",
    "currentPeriodEnd": "2026-03-30",
    "nextBillingDate": "2026-03-30",
    "paymentMethodType": "MANUAL",
    "autoRenew": false
  }
}
```

**Response (Error)**:
```json
{
  "success": false,
  "error": "Plan not found: 999"
}
```

### Service Layer

**File**: [BillingService.java](src/main/java/com/reuben/pastcare_spring/services/BillingService.java)

**Method**: `manuallyActivateSubscription()`

**Parameters**:
- `churchId` - ID of the church to activate
- `planId` - Subscription plan to activate
- `durationMonths` - Duration in months (defaults to 1)
- `reason` - Administrative reason (required for audit trail)
- `adminUserId` - ID of superadmin performing action

**Key Features**:

1. **Creates or Updates Subscription**:
   - Gets existing subscription or creates new one
   - Sets plan, status, and billing dates
   - Marks payment method as "MANUAL"
   - Disables auto-renewal by default

2. **Generates Audit Payment Record**:
   - Creates Payment entity with status "SUCCESSFUL"
   - Reference format: `MANUAL-{UUID}`
   - Payment type: `SUBSCRIPTION_MANUAL`
   - Stores reason in description field

3. **Audit Logging**:
   - Logs superadmin ID, church ID, plan, duration, and reason
   - Enables complete audit trail for compliance

### Controller

**File**: [BillingController.java](src/main/java/com/reuben/pastcare_spring/controllers/BillingController.java)

**Endpoint**: Lines 628-655

**Security**:
- `@RequirePermission(Permission.PLATFORM_ACCESS)` - SUPERADMIN only
- Captures admin user ID from `TenantContext`
- All actions are logged with admin identifier

## How It Works

### Flow Diagram

```
┌─────────────────┐
│   SUPERADMIN    │
│   Dashboard     │
└────────┬────────┘
         │
         │ POST /api/billing/platform/subscription/manual-activate
         │ { churchId, planId, durationMonths, reason }
         │
         ▼
┌─────────────────────────────────────────┐
│      BillingController                  │
│  - Validates PLATFORM_ACCESS permission │
│  - Extracts admin user ID               │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│      BillingService                     │
│  1. Get/create subscription             │
│  2. Validate plan exists                │
│  3. Calculate period dates              │
│  4. Update subscription to ACTIVE       │
│  5. Create manual payment record        │
│  6. Log admin action                    │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│         Database                        │
│  - church_subscriptions (updated)       │
│  - payments (new MANUAL record)         │
└─────────────────────────────────────────┘
```

### Subscription Updates

When manually activated:

1. **Status**: Set to `ACTIVE`
2. **Current Period**: Set to current date + duration months
3. **Next Billing Date**: Set to period end date
4. **Payment Method**: Set to `MANUAL`
5. **Auto Renew**: Disabled (manual subscriptions don't auto-renew)
6. **Failed Payment Attempts**: Reset to 0

### Payment Record

A tracking payment record is created with:

- **Reference**: `MANUAL-{UUID}` (e.g., `MANUAL-a1b2c3d4-e5f6...`)
- **Status**: `SUCCESSFUL`
- **Payment Type**: `SUBSCRIPTION_MANUAL`
- **Payment Method**: `MANUAL`
- **Amount**: Plan price × duration months
- **Description**: Includes the admin-provided reason
- **Payment Date**: Current timestamp

This creates an audit trail and shows in payment history.

## Testing

### Test Case 1: Manual Activation After Failed Callback

**Setup**:
1. Church completes payment on Paystack
2. Webhook/callback fails, subscription not activated
3. Verify payment success on Paystack dashboard

**Steps**:
```bash
# Verify church subscription status (should be inactive or pending)
curl -X GET http://localhost:8080/api/billing/subscription \
  -H "Authorization: Bearer {church_admin_token}"

# As SUPERADMIN, manually activate
curl -X POST http://localhost:8080/api/billing/platform/subscription/manual-activate \
  -H "Authorization: Bearer {superadmin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "churchId": 123,
    "planId": 2,
    "durationMonths": 1,
    "reason": "Payment verified on Paystack dashboard - callback failed"
  }'

# Verify subscription now active
curl -X GET http://localhost:8080/api/billing/subscription \
  -H "Authorization: Bearer {church_admin_token}"
```

**Expected**:
- Subscription status changes from inactive to ACTIVE
- Church gains access to paid features
- Manual payment record appears in payment history
- Audit log shows superadmin action

### Test Case 2: Bank Transfer Payment

**Setup**:
1. Church pays via bank transfer
2. Payment confirmed in bank account
3. Church requests activation

**Steps**:
```bash
curl -X POST http://localhost:8080/api/billing/platform/subscription/manual-activate \
  -H "Authorization: Bearer {superadmin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "churchId": 456,
    "planId": 3,
    "durationMonths": 12,
    "reason": "Bank transfer payment confirmed - Ref: BT20251230001"
  }'
```

**Expected**:
- 12-month subscription activated
- Payment record created with MANUAL type
- Auto-renewal disabled (church must renew manually)

### Test Case 3: Promotional Access

**Setup**:
1. Marketing partnership requires 3 months free access
2. No payment expected

**Steps**:
```bash
curl -X POST http://localhost:8080/api/billing/platform/subscription/manual-activate \
  -H "Authorization: Bearer {superadmin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "churchId": 789,
    "planId": 2,
    "durationMonths": 3,
    "reason": "Promotional access - Partnership with ABC Conference"
  }'
```

**Expected**:
- 3-month PROFESSIONAL plan activated
- Payment record shows GHS 0.00 or plan price (for tracking only)
- Reason clearly states promotional nature

### Test Case 4: Permission Denied

**Setup**: Non-superadmin user attempts manual activation

**Steps**:
```bash
curl -X POST http://localhost:8080/api/billing/platform/subscription/manual-activate \
  -H "Authorization: Bearer {church_admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "churchId": 123,
    "planId": 2,
    "durationMonths": 1,
    "reason": "Testing"
  }'
```

**Expected**:
- HTTP 403 Forbidden
- Error: "Insufficient permissions" or similar
- No subscription changes

## Security Considerations

### 1. SUPERADMIN-Only Access

**Implementation**: `@RequirePermission(Permission.PLATFORM_ACCESS)`

**Why**: Manual activation bypasses payment verification, so only trusted platform administrators should have access.

### 2. Audit Trail

**Required Fields**:
- Admin user ID (who performed the action)
- Reason (why it was done)
- Timestamp (when it happened)
- Church ID and plan details (what was affected)

**Logging**: All manual activations are logged with full details for compliance and review.

### 3. Payment Records

Even manual activations create payment records to:
- Maintain consistent payment history
- Enable financial reporting and reconciliation
- Support audits and compliance checks
- Track all subscription activations regardless of method

### 4. Auto-Renewal Disabled

Manual activations don't auto-renew because:
- Manual payment methods may not support recurring charges
- Prevents unexpected billing for promotional/special access
- Requires explicit renewal, giving control to both parties

## Frontend Integration

### Admin Dashboard UI (Recommended)

**Location**: Platform admin section (SUPERADMIN only)

**Interface**:

```typescript
interface ManualActivationForm {
  churchId: number;
  churchName: string; // For display only
  planId: number;
  durationMonths: number;
  reason: string;
}

async activateManually(form: ManualActivationForm) {
  const response = await this.http.post(
    '/api/billing/platform/subscription/manual-activate',
    {
      churchId: form.churchId,
      planId: form.planId,
      durationMonths: form.durationMonths,
      reason: form.reason
    }
  );

  if (response.success) {
    this.showSuccess('Subscription activated successfully');
    this.refreshSubscriptionList();
  } else {
    this.showError(response.error);
  }
}
```

**UI Components**:

1. **Church Selector**: Dropdown or search to select church
2. **Plan Selector**: Radio buttons or dropdown for available plans
3. **Duration Input**: Number input (default: 1 month)
4. **Reason Textarea**: Required field for admin notes
5. **Confirm Button**: With confirmation dialog
6. **Audit Log Viewer**: Show recent manual activations

**Validation**:
- Church ID required and must exist
- Plan ID required and must be active
- Duration must be positive integer (1-36 months suggested)
- Reason required and minimum 10 characters

## Edge Cases

### 1. Church Already Has Active Subscription

**Behavior**: Updates existing subscription to new plan/duration

**Example**: Church on PROFESSIONAL (monthly) upgraded manually to ENTERPRISE (quarterly)
- Current subscription updated
- New period dates calculated
- Manual payment record created

### 2. Duration Exceeds Expected Range

**Validation**: Backend accepts any positive integer

**Recommendation**: Frontend should validate:
- Minimum: 1 month
- Maximum: 36 months (suggest warning for > 12 months)
- Special handling for lifetime/indefinite access

### 3. Free Plan Manual Activation

**Allowed**: Yes, for administrative purposes

**Use Case**: Downgrade from paid plan without refund

**Note**: Creates manual payment record with GHS 0.00 amount

### 4. Concurrent Activation Requests

**Protection**: Database transaction isolation

**Behavior**: Last request wins; previous subscription state overwritten

**Mitigation**: Frontend should disable button after submission

## Comparison with Other Activation Methods

| Feature | Automatic (Paystack) | Manual (Superadmin) | Promotional Credits |
|---------|---------------------|---------------------|---------------------|
| Payment Required | ✅ Yes | ❌ No | ❌ No |
| Auto-Renewal | ✅ Yes | ❌ No | ✅ Yes (after credits) |
| Audit Trail | ✅ Yes | ✅ Yes (enhanced) | ✅ Yes |
| Use Case | Standard payments | Failed callbacks, manual payments | Free months, promotions |
| Permission | Church admin | SUPERADMIN only | SUPERADMIN only |
| Payment Method | CARD/Mobile Money | MANUAL | N/A |

## Monitoring and Reporting

### Metrics to Track

1. **Manual Activation Rate**: % of subscriptions activated manually vs automatically
2. **Reason Analysis**: Categorize reasons (failed callback, manual payment, promo, etc.)
3. **Admin Usage**: Which admins perform most manual activations
4. **Duration Distribution**: Average duration of manual activations

### Recommended Queries

**Count manual activations this month**:
```sql
SELECT COUNT(*) FROM payments
WHERE payment_type = 'SUBSCRIPTION_MANUAL'
  AND payment_date >= DATE_TRUNC('month', CURRENT_DATE);
```

**Manual activations by reason**:
```sql
SELECT description, COUNT(*) as count
FROM payments
WHERE payment_type = 'SUBSCRIPTION_MANUAL'
GROUP BY description
ORDER BY count DESC;
```

**Subscriptions with manual payment method**:
```sql
SELECT church_id, plan_id, current_period_end
FROM church_subscriptions
WHERE payment_method_type = 'MANUAL'
  AND status = 'ACTIVE';
```

## Best Practices

### For Superadmins

1. **Always Provide Clear Reasons**: Include reference numbers, ticket IDs, or specific details
   - ✅ Good: "Payment verified on Paystack - Ref PCS-abc123, ticket #456"
   - ❌ Bad: "Manual activation"

2. **Verify Before Activating**: Confirm payment receipt or authorization before activating
   - Check Paystack dashboard for online payments
   - Verify bank transfer receipt for manual payments
   - Get approval for promotional access

3. **Document Special Cases**: For unusual activations, create additional documentation
   - Partnership agreements
   - Management approval emails
   - Support ticket references

4. **Monitor Auto-Renewal**: Manual subscriptions don't auto-renew
   - Set calendar reminders for expiration
   - Notify church before expiration
   - Plan renewal process in advance

### For Implementation

1. **Rate Limiting**: Consider limiting manual activations per admin per day
2. **Notifications**: Send email to church when manually activated
3. **Approval Workflow**: For high-value or long-duration activations, require multi-step approval
4. **Reporting**: Generate monthly reports of all manual activations for review

## Related Documentation

- [PAYMENT_CALLBACK_FIX.md](PAYMENT_CALLBACK_FIX.md) - Payment verification and webhook handling
- [BILLING_SYSTEM_COMPLETE.md](BILLING_SYSTEM_COMPLETE.md) - Complete billing system overview
- [GRACE_PERIOD_IMPLEMENTATION_COMPLETE.md](GRACE_PERIOD_IMPLEMENTATION_COMPLETE.md) - Grace period management

## API Reference

### Endpoint Summary

| Endpoint | Method | Permission | Purpose |
|----------|--------|------------|---------|
| `/api/billing/platform/subscription/manual-activate` | POST | PLATFORM_ACCESS | Manually activate subscription |

### Request Schema

```json
{
  "type": "object",
  "properties": {
    "churchId": {
      "type": "integer",
      "required": true,
      "description": "ID of the church to activate"
    },
    "planId": {
      "type": "integer",
      "required": true,
      "description": "Subscription plan ID"
    },
    "durationMonths": {
      "type": "integer",
      "default": 1,
      "minimum": 1,
      "description": "Duration in months"
    },
    "reason": {
      "type": "string",
      "required": true,
      "minLength": 10,
      "description": "Administrative reason for manual activation"
    }
  }
}
```

### Response Schema

```json
{
  "type": "object",
  "properties": {
    "success": {
      "type": "boolean"
    },
    "message": {
      "type": "string"
    },
    "subscription": {
      "type": "object",
      "description": "Updated ChurchSubscription entity"
    }
  }
}
```

## Files Modified

1. **[BillingService.java](src/main/java/com/reuben/pastcare_spring/services/BillingService.java)** - Added `manuallyActivateSubscription()` method (lines 329-400)
2. **[BillingController.java](src/main/java/com/reuben/pastcare_spring/controllers/BillingController.java)** - Added endpoint and DTO (lines 619-667)

## Changelog

### Version 1.0 - December 30, 2025
- Initial implementation of manual subscription activation
- SUPERADMIN-only access with permission check
- Audit trail with admin ID, reason, and timestamp
- Manual payment record generation
- Auto-renewal disabled for manual activations

---

**Status**: ✅ **IMPLEMENTED AND TESTED**

**Security**: ✅ SUPERADMIN-only, full audit trail, payment records created

**Compliance**: ✅ All actions logged with admin ID and reason for audit purposes
