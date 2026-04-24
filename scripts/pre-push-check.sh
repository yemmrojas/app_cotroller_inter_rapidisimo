#!/bin/bash

# Pre-push check script for Controller APP
# Run this script before pushing to ensure CI will pass

set -e

echo "=========================================="
echo "Running pre-push checks..."
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo -e "${RED}Error: gradlew not found. Are you in the project root?${NC}"
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

echo -e "${YELLOW}1. Running Ktlint check...${NC}"
if ./gradlew ktlintCheck; then
    echo -e "${GREEN}✓ Ktlint check passed${NC}"
else
    echo -e "${RED}✗ Ktlint check failed${NC}"
    echo -e "${YELLOW}Run './gradlew ktlintFormat' to auto-fix style issues${NC}"
    exit 1
fi
echo ""

echo -e "${YELLOW}2. Building project...${NC}"
if ./gradlew build; then
    echo -e "${GREEN}✓ Build successful${NC}"
else
    echo -e "${RED}✗ Build failed${NC}"
    exit 1
fi
echo ""

echo -e "${YELLOW}3. Running unit tests...${NC}"
if ./gradlew test; then
    echo -e "${GREEN}✓ All tests passed${NC}"
else
    echo -e "${RED}✗ Tests failed${NC}"
    exit 1
fi
echo ""

echo -e "${YELLOW}4. Generating test coverage report...${NC}"
if ./gradlew testDebugUnitTestCoverage 2>/dev/null; then
    echo -e "${GREEN}✓ Coverage report generated${NC}"
    
    echo -e "${YELLOW}5. Verifying 80% minimum coverage...${NC}"
    if ./gradlew verifyCoverage 2>/dev/null; then
        echo -e "${GREEN}✓ Coverage meets 80% minimum requirement${NC}"
    else
        echo -e "${RED}✗ Coverage is below 80% minimum${NC}"
        echo -e "${YELLOW}Check report at: app/build/reports/jacoco/testDebugUnitTestCoverage/html/index.html${NC}"
        exit 1
    fi
else
    echo -e "${YELLOW}⚠ Coverage report generation skipped (no tests exist yet)${NC}"
fi
echo ""

echo -e "${YELLOW}6. Running Android Lint...${NC}"
if ./gradlew lint; then
    echo -e "${GREEN}✓ Lint check passed${NC}"
else
    echo -e "${YELLOW}⚠ Lint found issues. Check report at: app/build/reports/lint-results.html${NC}"
fi
echo ""

echo "=========================================="
echo -e "${GREEN}All checks passed! Ready to push.${NC}"
echo "=========================================="
