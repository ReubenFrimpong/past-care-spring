# Backend Restart Required

**Date:** 2025-12-28
**Issue:** Dashboard endpoints returning "No static resource" errors

---

## Problem

The backend is throwing errors for new dashboard endpoints:
```
No static resource api/dashboard/templates
No static resource api/dashboard/top-members
```

## Root Cause

The backend was started **before** the new Dashboard Phase 2.2-2.4 services were created:
- DashboardTemplateService
- GoalService  
- InsightService

The currently running backend doesn't have these services or their 19 new endpoints.

## Solution

**Restart the backend** to load the new services:

```bash
# 1. Stop current backend (Ctrl+C or kill process)
pkill -f "spring-boot:run"

# 2. Clean and start fresh
./mvnw clean spring-boot:run
```

## What Will Be Available After Restart

### Templates Endpoints (8)
- `GET /api/dashboard/templates` - List all templates
- `GET /api/dashboard/templates/{id}` - Get template by ID
- `GET /api/dashboard/templates/role/{role}` - Get templates by role
- `POST /api/dashboard/templates` - Create template
- `PUT /api/dashboard/templates/{id}` - Update template
- `DELETE /api/dashboard/templates/{id}` - Delete template
- `POST /api/dashboard/templates/{id}/apply` - Apply template to user
- `GET /api/dashboard/templates/default/{role}` - Get default template for role

### Goals Endpoints (9)
- `GET /api/dashboard/goals` - List all goals
- `GET /api/dashboard/goals/{id}` - Get goal by ID
- `GET /api/dashboard/goals/active` - Get active goals
- `GET /api/dashboard/goals/type/{type}` - Get goals by type
- `POST /api/dashboard/goals` - Create goal
- `PUT /api/dashboard/goals/{id}` - Update goal
- `DELETE /api/dashboard/goals/{id}` - Delete goal
- `POST /api/dashboard/goals/{id}/recalculate` - Recalculate goal progress
- `POST /api/dashboard/goals/recalculate-all` - Recalculate all goals

### Insights Endpoints (8)
- `GET /api/dashboard/insights` - List all insights
- `GET /api/dashboard/insights/active` - Get active (non-dismissed) insights
- `GET /api/dashboard/insights/{id}` - Get insight by ID
- `GET /api/dashboard/insights/category/{category}` - Get insights by category
- `GET /api/dashboard/insights/severity/{severity}` - Get insights by severity
- `POST /api/dashboard/insights/generate` - Generate new insights
- `POST /api/dashboard/insights/{id}/dismiss` - Dismiss insight
- `DELETE /api/dashboard/insights/{id}` - Delete insight

## Verification

After restart, check that endpoints respond:
```bash
# Should return 200 OK (or 401 if not authenticated)
curl http://localhost:8080/api/dashboard/templates
curl http://localhost:8080/api/dashboard/goals
curl http://localhost:8080/api/dashboard/insights
```

---

**Current Status:** Backend needs restart to load new dashboard features
