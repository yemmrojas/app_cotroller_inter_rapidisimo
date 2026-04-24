# Pull Request

## Business Description

<!-- Describe the business value and user impact of this change -->
<!-- What problem does this solve? What feature does this add? -->



## Technical Description

<!-- Describe the technical implementation details -->
<!-- What components were modified? What architectural decisions were made? -->



## Unit Tests Explanation

<!-- Describe the testing strategy and coverage -->
<!-- What scenarios are tested? What edge cases are covered? -->
<!-- Include property-based tests if applicable -->



## Changes Made

<!-- List the specific changes in this PR -->

- [ ] Code changes
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] CHANGELOG.md updated



## Testing Checklist

- [ ] All unit tests pass locally
- [ ] Test coverage meets minimum 80% requirement
- [ ] Property-based tests included (if applicable)
- [ ] Manual testing completed on device/emulator
- [ ] No new warnings or errors introduced



## Code Quality Checklist

- [ ] Code follows Ktlint style guidelines
- [ ] `./gradlew ktlintCheck` passes
- [ ] All interfaces documented with KDoc
- [ ] Complex logic includes explanatory comments
- [ ] No hardcoded values (use constants/config)



## Mandatory Checklist

### Labels
- [ ] PR has appropriate labels (feature/bugfix/refactor/docs)
- [ ] Priority label assigned (high/medium/low)

### Ownership
- [ ] PR has assigned owner/reviewer
- [ ] Owner has reviewed and approved

### Changelog
- [ ] CHANGELOG.md updated with changes
- [ ] Format follows standard: `[FEATURE]` or `[FIX]` - Description
- [ ] Version number updated if applicable

### Tests
- [ ] Unit tests written for new functionality
- [ ] Tests use provider methods pattern (no @Before/@Setup)
- [ ] All tests pass: `./gradlew test`
- [ ] Coverage report generated and reviewed

### Standard Titles
- [ ] PR title follows format: `[TYPE] - Brief description`
  - Types: FEATURE, FIX, REFACTOR, DOCS, TEST, PERF, SECURITY
  - Example: `[FEATURE] - Add user authentication with session persistence`

### Build Verification
- [ ] Project builds successfully: `./gradlew build`
- [ ] No build warnings introduced
- [ ] APK installs and runs on target device



## Breaking Changes

<!-- List any breaking changes and migration steps required -->

- [ ] No breaking changes
- [ ] Breaking changes documented below

<!-- If breaking changes exist, describe them here -->



## Screenshots/Videos

<!-- Add screenshots or videos demonstrating the changes (if UI-related) -->



## Related Issues

<!-- Link related issues, tickets, or PRs -->

Closes #
Related to #



## Deployment Notes

<!-- Any special deployment considerations or steps -->



## Rollback Plan

<!-- Describe how to rollback this change if issues arise -->



---

## Reviewer Notes

<!-- For reviewers: Add your review comments and approval here -->

### Review Checklist
- [ ] Code follows Clean Architecture principles
- [ ] Domain layer has no dependencies on data/presentation
- [ ] Repository interfaces properly defined
- [ ] Use cases encapsulate business logic correctly
- [ ] ViewModels depend only on domain use cases
- [ ] Error handling is comprehensive
- [ ] Network calls use Flow and proper error mapping
- [ ] Database operations are transactional where needed
- [ ] UI state management uses StateFlow appropriately
- [ ] Navigation uses type-safe serializable routes
- [ ] Tests are comprehensive and follow provider methods pattern
- [ ] No code smells or anti-patterns introduced



---

**PR Author**: @[username]  
**Reviewers**: @[reviewer1], @[reviewer2]  
**Target Branch**: `main`  
**Created**: [Date]
