# Frontend Project Location

## Important: Separate Frontend Repository

The Angular frontend for PastCare is maintained in a **separate directory** and should **NEVER** be added to the backend project.

### Frontend Location

**Standalone Frontend Directory:**
```
/home/reuben/Documents/workspace/past-care-spring-frontend/
```

### Why Separate?

1. **Clean Separation of Concerns**: Backend (Spring Boot) and Frontend (Angular) are independent projects
2. **Easier Deployment**: Frontend can be deployed to CDN/static hosting, backend to application server
3. **Faster Development**: No need to rebuild backend when working on frontend
4. **Better Git Workflow**: Separate commits, branches, and version control for frontend and backend

### Running the Frontend

```bash
# Navigate to the standalone frontend directory
cd /home/reuben/Documents/workspace/past-care-spring-frontend

# Install dependencies (first time only)
npm install

# Start development server
ng serve

# Build for production
ng build --configuration=production
```

### Backend Project Rules

⚠️ **DO NOT**:
- Add `past-care-spring-frontend/` directory to the backend project
- Copy frontend files into the backend repository
- Create Angular files inside `/home/reuben/Documents/workspace/pastcare-spring/`

✅ **DO**:
- Keep frontend at `/home/reuben/Documents/workspace/past-care-spring-frontend/`
- Make all frontend changes in the standalone frontend directory
- Deploy frontend and backend separately

### Git Configuration

The backend `.gitignore` has been configured to prevent accidental commits of frontend files:

```
past-care-spring-frontend/
node_modules/
package.json
angular.json
tsconfig.json
```

If you see `past-care-spring-frontend/` directory in the backend project, it was created by mistake and should be deleted immediately.

---

**Last Updated**: 2025-12-31
**Enforced By**: .gitignore rules
