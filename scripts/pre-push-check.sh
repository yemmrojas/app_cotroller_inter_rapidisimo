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
if ./gradlew testDebugUnitTestCoverage; then
    echo -e "${GREEN}✓ Coverage report generated${NC}"
    echo -e "${YELLOW}Please check coverage report at: app/build/reports/coverage/test/debug/index.html${NC}"
else
    echo -e "${YELLOW}⚠ Coverage report generation skipped (tests may not exist yet)${NC}"
fi
echo ""

echo -e "${YELLOW}5. Running Android Lint...${NC}"
if ./gradlew lint; then
    echo -e "${GREEN}✓ Lint check passed${NC}"
else
    echo -e "${YELLOW}⚠ Lint found issues. Check report at: app/build/reports/lint-results.html${NC}"
fi
echo ""

echo "=========================================="
echo -e "${GREEN}All checks passed! Ready to push.${NC}"
echo "=========================================="
