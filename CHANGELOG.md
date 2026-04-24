# Changelog

All notable changes to the Controller APP project will be documented in this file.

The format follows: `[FEATURE]` or `[FIX]` - Description

## [Unreleased]

### Added
- [FEATURE] - Project governance and infrastructure setup
- [FEATURE] - README.md with project specifications, technology stack, and execution guide
- [FEATURE] - CHANGELOG.md following standard format
- [FEATURE] - Pull request template with mandatory checklist
- [FEATURE] - GitHub Actions CI/CD pipeline with Ktlint and test coverage validation
- [FEATURE] - Gradle wrapper and root build files for CI/CD execution
- [FEATURE] - Hilt dependency injection configuration in app module
- [FEATURE] - Retrofit, Room, Coroutines, Flow, Navigation Compose dependencies
- [FEATURE] - Kotlinx Serialization plugin and dependencies
- [FEATURE] - Kotest for property-based testing
- [FEATURE] - Clean Architecture package structure (domain, data, presentation layers)
- [FEATURE] - ControllerApplication class with @HiltAndroidApp annotation
- [FEATURE] - KSP (Kotlin Symbol Processing) for annotation processing

### Fixed
- [FIX] - Enforce 80% minimum code coverage requirement in CI/CD pipeline
- [FIX] - Coverage verification now fails build if threshold is not met
- [FIX] - Added strict coverage check task that parses XML report and validates percentage
- [FIX] - Use generated JaCoCo XML path (testDebugUnitTestCoverage.xml) in coverage check task

---

## [1.0.0] - TBD

### Planned Features
- [FEATURE] - Splash screen with version control validation
- [FEATURE] - User authentication with secure credential handling
- [FEATURE] - Local session persistence with Room database
- [FEATURE] - Database schema synchronization from remote service
- [FEATURE] - Dynamic table creation and data management
- [FEATURE] - Home screen with user information display
- [FEATURE] - Tables screen with synchronized data visualization
- [FEATURE] - Table detail screen with variable column structure support
- [FEATURE] - Localities screen with service coverage areas
- [FEATURE] - Navigation architecture with type-safe routes
- [FEATURE] - Network error handling with exponential backoff retry
- [FEATURE] - Comprehensive unit and property-based testing
- [FEATURE] - Clean Architecture implementation (Domain → Data → Presentation)
- [FEATURE] - Dependency injection with Hilt
- [FEATURE] - Reactive data streams with Coroutines and Flow

---

## Version History

### Version Format
- **Major.Minor.Patch** (e.g., 1.0.0)
- **Major**: Breaking changes or significant feature additions
- **Minor**: New features, backward compatible
- **Patch**: Bug fixes and minor improvements

### Change Categories
- **[FEATURE]**: New functionality or capability
- **[FIX]**: Bug fixes and corrections
- **[REFACTOR]**: Code improvements without functionality changes
- **[DOCS]**: Documentation updates
- **[TEST]**: Test additions or improvements
- **[PERF]**: Performance improvements
- **[SECURITY]**: Security-related changes

---

## Notes

- All changes must be documented before pushing to the repository
- Each entry should be concise and describe the change clearly
- Reference issue/ticket numbers when applicable
- Group changes by version and category
- Keep the most recent changes at the top
