#!/bin/bash

# Script to add @RequirePermission annotations to all controllers
# This script adds the necessary imports and replaces @PreAuthorize with @RequirePermission

set -e

CONTROLLERS_DIR="src/main/java/com/reuben/pastcare_spring/controllers"

echo "🔒 RBAC Annotation Script"
echo "========================="
echo ""
echo "This script will add @RequirePermission annotations to controllers"
echo "based on the permission mapping defined in COMPREHENSIVE_IMPLEMENTATION_PLANS.md"
echo ""

# Function to add imports to a file if not already present
add_imports() {
    local file=$1

    # Check if RequirePermission import exists
    if ! grep -q "import com.reuben.pastcare_spring.annotations.RequirePermission;" "$file"; then
        # Find the line after package declaration
        sed -i '/^package /a\
\
import com.reuben.pastcare_spring.annotations.RequirePermission;\
import com.reuben.pastcare_spring.enums.Permission;' "$file"
        echo "  ✓ Added imports"
    else
        echo "  ℹ Imports already exist"
    fi
}

# Function to replace @PreAuthorize with @RequirePermission
replace_annotation() {
    local file=$1
    local view_permission=$2
    local create_permission=$3
    local edit_permission=$4
    local delete_permission=$5

    echo "Processing: $(basename $file)"

    # Add imports first
    add_imports "$file"

    # Replace @PreAuthorize for GET endpoints (view permission)
    if [ -n "$view_permission" ]; then
        sed -i "s/@PreAuthorize(\"isAuthenticated()\")/@RequirePermission(Permission.$view_permission)/g" "$file"
    fi

    echo "  ✓ Replaced annotations"
}

# Pledge Controller
echo ""
echo "📋 PledgeController"
echo "-------------------"
cat > /tmp/pledge-annotations.txt << 'EOF'
# PledgeController permission mapping:
# GET endpoints -> PLEDGE_VIEW_ALL (or PLEDGE_VIEW_OWN for member's own pledges)
# POST/PUT endpoints -> PLEDGE_MANAGE
# DELETE endpoints -> PLEDGE_MANAGE
EOF

if [ -f "$CONTROLLERS_DIR/PledgeController.java" ]; then
    file="$CONTROLLERS_DIR/PledgeController.java"

    # Add imports
    add_imports "$file"

    # Replace annotations with specific logic for Pledge endpoints
    # View all pledges
    sed -i 's|@GetMapping\s*$|@GetMapping\n  @RequirePermission(Permission.PLEDGE_VIEW_ALL)|g' "$file"
    sed -i 's|@GetMapping("/{id}")|@GetMapping("/{id}")\n  @RequirePermission(Permission.PLEDGE_VIEW_ALL)|g' "$file"

    # Create/Update/Delete - require PLEDGE_MANAGE
    sed -i 's|@PostMapping\s*$|@PostMapping\n  @RequirePermission(Permission.PLEDGE_MANAGE)|g' "$file"
    sed -i 's|@PutMapping("/{id}")|@PutMapping("/{id}")\n  @RequirePermission(Permission.PLEDGE_MANAGE)|g' "$file"
    sed -i 's|@DeleteMapping("/{id}")|@DeleteMapping("/{id}")\n  @RequirePermission(Permission.PLEDGE_MANAGE)|g' "$file"

    # Remove old @PreAuthorize
    sed -i '/@PreAuthorize("isAuthenticated()")/d' "$file"

    echo "  ✓ Added RBAC annotations"
else
    echo "  ⚠ File not found"
fi

echo ""
echo "✅ RBAC Annotation Script Complete"
echo ""
echo "Next steps:"
echo "1. Review the changes in each controller"
echo "2. Compile the project: ./mvnw compile"
echo "3. Test with different roles"
echo ""
