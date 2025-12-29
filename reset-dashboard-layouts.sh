#!/bin/bash
# Reset Dashboard Layouts - Delete old layouts to force regeneration
# This script deletes existing dashboard layouts so backend can create new ones with correct widget keys

set -e

echo "========================================="
echo "Dashboard Layouts Reset Script"
echo "========================================="
echo ""

# Prompt for MySQL password
read -sp "Enter MySQL root password: " MYSQL_PASSWORD
echo ""

# Check database connection
echo "Testing database connection..."
mysql -u root -p"$MYSQL_PASSWORD" pastcare_db -e "SELECT 1;" > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "❌ Error: Cannot connect to database"
    echo "Please check your MySQL credentials"
    exit 1
fi
echo "✅ Database connection successful"
echo ""

# Show current layouts
echo "Current dashboard layouts:"
mysql -u root -p"$MYSQL_PASSWORD" pastcare_db -e "SELECT id, user_id, layout_name, is_default, created_at FROM dashboard_layouts ORDER BY id;"
echo ""

# Count layouts
LAYOUT_COUNT=$(mysql -u root -p"$MYSQL_PASSWORD" pastcare_db -se "SELECT COUNT(*) FROM dashboard_layouts;")
echo "Total layouts: $LAYOUT_COUNT"
echo ""

if [ "$LAYOUT_COUNT" -eq 0 ]; then
    echo "ℹ️  No layouts to delete. Backend will create default layout on next access."
    exit 0
fi

# Confirm deletion
echo "⚠️  WARNING: This will delete ALL dashboard layouts!"
echo "Users will get new default layouts with correct widget keys on next login."
read -p "Do you want to continue? (yes/no): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    echo "❌ Operation cancelled"
    exit 0
fi

# Delete layouts
echo ""
echo "Deleting dashboard layouts..."
mysql -u root -p"$MYSQL_PASSWORD" pastcare_db -e "DELETE FROM dashboard_layouts;"

# Verify deletion
REMAINING=$(mysql -u root -p"$MYSQL_PASSWORD" pastcare_db -se "SELECT COUNT(*) FROM dashboard_layouts;")
if [ "$REMAINING" -eq 0 ]; then
    echo "✅ All dashboard layouts deleted successfully"
    echo ""
    echo "Next steps:"
    echo "1. Restart the backend: ./mvnw spring-boot:run"
    echo "2. Refresh the frontend (hard refresh: Ctrl+Shift+R)"
    echo "3. Open dashboard - new default layout will be created automatically"
    echo "4. Click 'Customize' → 'Widgets' to see 17 toggle switches"
else
    echo "❌ Error: $REMAINING layouts still remain"
    exit 1
fi

echo ""
echo "========================================="
echo "✅ Reset Complete!"
echo "========================================="
