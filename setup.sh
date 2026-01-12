#!/bin/bash

# E-Learning Platform - Quick Setup Script
# This script automates the setup process for macOS/Linux

echo "========================================"
echo "E-Learning Platform - Setup Script"
echo "========================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Java is installed
echo "Checking Java installation..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo -e "${GREEN}✓${NC} Java is installed: $JAVA_VERSION"
else
    echo -e "${RED}✗${NC} Java is not installed"
    echo "Please install Java 17 or higher"
    exit 1
fi

# Check if Maven is installed
echo "Checking Maven installation..."
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1)
    echo -e "${GREEN}✓${NC} Maven is installed: $MVN_VERSION"
else
    echo -e "${RED}✗${NC} Maven is not installed"
    echo "Please install Maven 3.6 or higher"
    exit 1
fi

# Check if MySQL is installed
echo "Checking MySQL installation..."
if command -v mysql &> /dev/null; then
    echo -e "${GREEN}✓${NC} MySQL is installed"
else
    echo -e "${YELLOW}!${NC} MySQL is not installed"
    echo "Please install MySQL 8.0 or higher"
    exit 1
fi

echo ""
echo "========================================"
echo "Building Project"
echo "========================================"
echo ""

# Clean and build the project
mvn clean install

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✓${NC} Build successful!"
    echo ""
    echo "========================================"
    echo "Setup Complete!"
    echo "========================================"
    echo ""
    echo "Next steps:"
    echo "1. Set up the database:"
    echo "   mysql -u root -p < database/setup.sql"
    echo ""
    echo "2. Configure database credentials:"
    echo "   Edit src/main/resources/config.properties"
    echo ""
    echo "3. Run the application:"
    echo "   mvn javafx:run"
    echo ""
    echo "Default login credentials:"
    echo "  Admin:   admin / admin123"
    echo "  Teacher: teacher / teacher123"
    echo "  Student: student / student123"
    echo ""
else
    echo ""
    echo -e "${RED}✗${NC} Build failed!"
    echo "Please check the error messages above"
    exit 1
fi
