# Implementation Plan: Controller APP

## Overview

This implementation plan breaks down the Controller APP feature into discrete, incremental coding tasks following Clean Architecture principles. The implementation will proceed layer by layer (Domain → Data → Presentation), with testing integrated throughout to validate functionality early.

## Tasks

- [x] 0. Project Governance and Infrastructure Setup
  - [x] 0.1 Configure Git & Documentation
    - Create README.md with project specifications, technology stack, and execution guide
    - Create CHANGELOG.md following the standard [FEATURE/FIX] - Description format
    - Create .github/pull_request_template.md with mandatory checklist (Labels, Owner, Changelog, Tests, Standard Titles)
    - Include sections: Business & Technical Description, Unit Tests Explanation, Mandatory Checklist
    - _Technical Requirement: Git Configuration and Documentation_
  
  - [x] 0.2 Configure CI/CD Pipeline (GitHub Actions)
    - Create .github/workflows/android_ci.yml workflow
    - Configure Ktlint for code style validation
    - Configure unit test execution with minimum 80% coverage requirement
    - Configure project build verification
    - Add status badges to README.md
    - _Technical Requirement: CI/CD Configuration_

- [x] 1. Set up project structure and dependencies
  - Configure Hilt dependency injection in the app module
  - Add required dependencies: Retrofit, Room, Coroutines, Flow, Navigation Compose, Kotlinx Serialization, Kotest
  - Create package structure: domain, data, presentation layers
  - Configure Kotlinx Serialization plugin
  - Add Ktlint for code style enforcement
  - _Requirements: 12.1, 13.1, 13.2, 14.1_

- [x] 2. Implement Domain Layer - Core Models and Interfaces
  - [x] 2.1 Create domain models
    - Define Result sealed class (Success, Error)
    - Define AppError sealed class hierarchy
    - Define VersionStatus, UserSession, AuthResponse, TableSchema, Locality, and other domain models
    - _Requirements: 1.3, 2.2, 3.2, 4.1, 8.2_
  
  - [x] 2.2 Define repository interfaces
    - Create VersionRepository interface with Flow return types
    - Create AuthRepository interface with Flow return types
    - Create DataSyncRepository interface with Flow return types
    - Create LocalitiesRepository interface with Flow return types
    - _Requirements: 12.1, 14.2_
  
  - [x] 2.2.1 Document interfaces with KDoc
    - Document all repository interfaces explaining their business purpose
    - Document all use case interfaces with input/output specifications
    - Document API service interfaces with endpoint details
    - Note: Implementation documentation only required for complex logic
    - _Technical Requirement: Interface Documentation_
  
  - [x] 2.3 Implement use cases
    - Create CheckVersionUseCase with Flow
    - Create LoginUserUseCase with Flow
    - Create GetUserSessionUseCase with Flow
    - Create SyncDatabaseUseCase with Flow
    - Create GetTablesUseCase with Flow
    - Create GetTableDataUseCase with Flow
    - Create GetLocalitiesUseCase with Flow
    - Create LogoutUseCase with Flow
    - _Requirements: 12.2, 14.2_

- [x] 3. Implement Data Layer - Network and Database
  - [x] 3.1 Create API service interfaces
    - Define VersionApiService with Retrofit annotations
    - Define AuthApiService with required headers and body
    - Define DataSyncApiService
    - Define LocalitiesApiService
    - Create data models with @Serializable annotation
    - _Requirements: 2.1, 3.1, 8.1, 10.1_
  
  - [x] 3.2 Implement NetworkHandler
    - Create NetworkHandler class with safeApiCall method
    - Implement network connectivity check
    - Implement error mapping (timeout, no connection, API errors)
    - Add logging for all errors
    - Configure Kotlinx Serialization to be lenient for escaped characters (e.g., \n in Password field from Interrapidisimo API)
    - Add OkHttp interceptor to handle JSON parsing edge cases
    - _Requirements: 9.1, 9.2, 9.3, 9.4_
    - _Technical Note: API returns escaped characters in Password field that require lenient parsing_
  
  - [x] 3.3 Create Room database
    - Define UserEntity with Room annotations
    - Create UserDao with insert, query, and delete operations
    - Create AppDatabase abstract class
    - Implement DynamicTableDao for schema synchronization
    - _Requirements: 3.2, 4.1, 4.3_
  
  - [x] 3.4 Implement repository implementations
    - Create VersionRepositoryImpl with Flow
    - Create AuthRepositoryImpl with Flow and validation
    - Create DataSyncRepositoryImpl with Flow
    - Create LocalitiesRepositoryImpl with Flow
    - Implement data mappers (Entity ↔ Domain)
    - _Requirements: 12.3, 14.2_

- [x] 3.5 Write property test for authentication round-trip
  - **Property 3: Authentication and Session Round-Trip**
  - **Validates: Requirements 2.3, 2.5, 4.1, 4.3, 11.1**

- [ ]* 3.6 Write property test for session persistence
  - **Property 4: Session Persistence Across Restarts**
  - **Validates: Requirements 4.5, 11.2**

- [ ]* 3.7 Write property test for user data validation
  - **Property 5: User Data Validation Rejects Empty Fields**
  - **Validates: Requirements 4.2, 10.4**

- [ ]* 3.8 Write property test for network error handling
  - **Property 9: Network Error Handling Consistency**
  - **Validates: Requirements 1.9, 2.4, 2.6, 8.4, 9.1, 9.2, 9.3**

- [x] 4. Implement Dependency Injection with Hilt
  - [x] 4.1 Create Hilt modules
    - Create NetworkModule (Retrofit, OkHttp, API services)
    - Create DatabaseModule (Room database, DAOs)
    - Create RepositoryModule (Repository implementations)
    - Create UseCaseModule (Use case instances)
    - _Requirements: 13.2, 13.3, 13.4, 13.5_
  
  - [x] 4.2 Configure Application class
    - Annotate Application class with @HiltAndroidApp
    - _Requirements: 13.1_

- [x] 5. Checkpoint - Verify domain and data layers
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Implement Presentation Layer - Navigation
  - [x] 6.1 Define navigation routes
    - Create serializable route objects (SplashRoute, LoginRoute, HomeRoute, TablesRoute, TableDetailRoute, LocalitiesRoute)
    - _Requirements: 5.2, 5.3_
  
  - [x] 6.2 Create navigation graph
    - Implement AppNavigation composable with NavHost
    - Configure navigation destinations and transitions
    - _Requirements: 5.1, 5.4_

- [ ]* 6.3 Write property test for navigation type safety
  - **Property 15: Navigation Type Safety with Serializable Routes**
  - **Validates: Requirements 5.2, 5.3**

- [ ] 7. Implement Presentation Layer - ViewModels
  - [ ] 7.1 Create SplashViewModel
    - Implement version check and session restoration logic
    - Manage SplashState (Loading, VersionMismatch, NavigateToLogin, NavigateToHome, Error)
    - Use StateFlow for state management
    - _Requirements: 1.1, 1.2, 1.6, 1.7, 1.8, 14.3_
  
  - [ ] 7.2 Create LoginViewModel
    - Implement login logic with LoginUserUseCase
    - Manage LoginState (Idle, Loading, Success, Error)
    - Handle authentication errors
    - _Requirements: 2.1, 2.4, 2.6, 14.3_
  
  - [ ] 7.3 Create HomeViewModel
    - Implement user data loading
    - Implement database sync trigger
    - Implement logout functionality
    - Manage HomeState (Loading, Success, Error)
    - _Requirements: 6.1, 11.5, 14.3_
  
  - [ ] 7.4 Create TablesViewModel
    - Implement tables listing logic
    - Implement table data loading
    - Manage TablesState (Loading, TablesList, TableData, Error)
    - _Requirements: 7.1, 7.2, 7.5, 14.3_
  
  - [ ] 7.5 Create LocalitiesViewModel
    - Implement localities loading logic
    - Manage LocalitiesState (Loading, Success, Error)
    - _Requirements: 8.1, 8.4, 14.3_

- [ ]* 7.6 Write unit tests for ViewModels
  - Test SplashViewModel state transitions
  - Test LoginViewModel authentication flow
  - Test HomeViewModel user data display
  - Test TablesViewModel table operations
  - Test LocalitiesViewModel localities loading
  - **CRITICAL: Use provider methods pattern (e.g., providesSut(), providesRepositoryMock())**
  - **DO NOT use @Before or global initialization in tests**
  - **All dependencies must be injected via provider methods**
  - _Requirements: 15.3_
  - _Technical Requirement: Provider Methods Testing Pattern_

- [ ] 8. Implement Presentation Layer - UI Screens
  - [ ] 8.1 Create SplashScreen composable
    - Display app logo and loading indicator
    - Show version check status messages
    - Handle navigation based on state
    - _Requirements: 1.1, 1.4, 1.5, 1.9_
  
  - [ ] 8.2 Create LoginScreen composable
    - Create input fields for usuario and password
    - Create login button
    - Display error messages
    - Handle navigation on success
    - _Requirements: 2.1, 2.4_
  
  - [ ] 8.3 Create HomeScreen composable
    - Display user information (Usuario, Identificacion, Nombre)
    - Create "Tablas" and "Localidades" navigation buttons
    - Add logout button
    - _Requirements: 6.1, 6.2, 6.3, 6.4_
  
  - [ ] 8.4 Create TablesScreen composable
    - Display list of tables with names and record counts
    - Handle table selection
    - Show empty state when no tables available
    - _Requirements: 7.1, 7.2, 7.4_
  
  - [ ] 8.5 Create TableDetailScreen composable
    - Display table data in scrollable format
    - Handle variable column structures
    - Show loading and error states
    - _Requirements: 7.3, 7.5_
  
  - [ ] 8.6 Create LocalitiesScreen composable
    - Display localities in scrollable list
    - Show AbreviacionCiudad and NombreCompleto
    - Handle loading and error states
    - _Requirements: 8.2, 8.3, 8.4_

- [ ] 9. Implement Retry Logic with Exponential Backoff
  - [ ] 9.1 Create retry utility function
    - Implement retryWithExponentialBackoff function
    - Configure max retries, initial delay, and backoff factor
    - Determine retryable errors
    - _Requirements: 9.5_

- [ ]* 9.2 Write property test for exponential backoff
  - **Property 13: Exponential Backoff Retry Logic**
  - **Validates: Requirements 9.5**

- [ ] 10. Implement Database Schema Synchronization
  - [ ] 10.1 Implement schema fetch and sync logic
    - Fetch schema from API
    - Create or update tables dynamically
    - Preserve existing data during updates
    - Implement transaction rollback on failure
    - _Requirements: 3.1, 3.2, 3.3, 3.6_

- [ ]* 10.2 Write property test for schema synchronization
  - **Property 7: Database Schema Synchronization Idempotence**
  - **Validates: Requirements 3.2, 3.5**

- [ ]* 10.3 Write property test for data preservation
  - **Property 8: Table Data Preservation During Schema Updates**
  - **Validates: Requirements 3.3**

- [ ]* 10.4 Write property test for transaction rollback
  - **Property 14: Database Transaction Rollback on Failure**
  - **Validates: Requirements 3.6**

- [ ] 11. Implement Version Comparison Logic
  - [ ] 11.1 Create version comparison utility
    - Implement version string parsing
    - Implement comparison logic (semantic versioning)
    - Return appropriate VersionComparisonStatus
    - _Requirements: 1.3, 1.4, 1.5, 1.6_

- [ ]* 11.2 Write property test for version comparison
  - **Property 1: Version Comparison Correctness**
  - **Validates: Requirements 1.3, 1.4, 1.5, 1.6**

- [ ]* 11.3 Write property test for splash navigation logic
  - **Property 2: Splash Screen Navigation Logic**
  - **Validates: Requirements 1.7, 1.8**

- [ ] 12. Integration and Final Wiring
  - [ ] 12.1 Wire MainActivity with navigation
    - Set up NavHost in MainActivity
    - Configure Hilt injection
    - Handle system back button
    - _Requirements: 5.1, 5.4_
  
  - [ ] 12.2 Configure API endpoints and headers
    - Set base URLs for all API services
    - Configure default headers
    - Set timeout configurations
    - _Requirements: 2.1, 3.1, 8.1_
  
  - [ ] 12.3 Add error logging
    - Implement logging for all error types
    - Add debug logging for API requests/responses
    - _Requirements: 9.4_

- [ ]* 12.4 Write integration tests
  - Test end-to-end authentication flow
  - Test navigation between screens
  - Test data synchronization flow
  - **Use provider methods pattern for all test dependencies**
  - **Avoid @Before setup methods**
  - _Requirements: 15.2_

- [ ] 12.5 Update CHANGELOG.md for final version
  - Ensure current project version matches CHANGELOG.md entry
  - Verify all features and fixes are documented
  - Add release date and version number
  - Review format compliance: [FEATURE/FIX] - Description
  - _Technical Requirement: Version Control and Changelog Maintenance_

- [ ] 13. Final Checkpoint - Comprehensive Testing
  - Ensure all tests pass, ask the user if questions arise.
  - Verify all requirements are implemented
  - Test app on device/emulator

## Notes

- Tasks marked with `*` are optional property-based and unit tests that can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties with 100+ iterations
- Unit tests validate specific examples and edge cases
- Implementation follows Clean Architecture: Domain → Data → Presentation
- Flow is used throughout for reactive data streams
- All repository methods return Flow for consistency

## Mandatory Team Standards

### Testing Pattern
- **ALWAYS use provider methods** (e.g., `providesSut()`, `providesRepositoryMock()`)
- **NEVER use @Before or @Setup** annotations for test initialization
- All test dependencies must be injected through provider methods
- Example pattern from team standard:
  ```kotlin
  companion object {
      @JvmStatic
      fun providesTestScenarios() = listOf(/* scenarios */)
  }
  ```

### Documentation Requirements
- All interfaces MUST be documented with KDoc
- Implementations only need documentation if logic is complex
- Include business purpose in interface documentation

### Version Control
- CHANGELOG.md MUST be updated before every push
- Format: `[FEATURE]` or `[FIX]` - Description
- Pull requests MUST use the template with mandatory checklist

### CI/CD Requirements
- All code MUST pass Ktlint validation
- Unit test coverage MUST be minimum 80%
- Build MUST succeed before merge

### JSON Parsing
- Kotlinx Serialization configured as lenient for API edge cases
- Interrapidisimo API returns escaped characters (e.g., `\n` in Password)
- OkHttp interceptor handles special characters in responses
