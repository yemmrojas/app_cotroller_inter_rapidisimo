# Controller APP - Interrapidisimo

![Android CI](https://github.com/[username]/[repo]/workflows/Android%20CI/badge.svg)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-green.svg)](https://developer.android.com/about/versions/nougat)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-green.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Controller APP is an Android application for Interrapidisimo that provides version control validation, secure authentication, local data synchronization, and locality management capabilities.

## Project Specifications

### Overview
Controller APP is built following Clean Architecture principles with a three-layer architecture (Domain, Data, Presentation) and unidirectional data flow using MVI/MVVM pattern. The application ensures operators have the correct app version, secure access, and synchronized local data for offline operations.

### Key Features
- **Version Control Validation**: Automatic version checking on startup with update notifications
- **Secure Authentication**: User authentication with local session persistence
- **Database Schema Synchronization**: Dynamic table creation and data synchronization from remote services
- **Locality Management**: Display and management of service coverage areas
- **Offline Support**: Local data persistence with Room database
- **Clean Architecture**: Clear separation of concerns across Domain, Data, and Presentation layers

## Technology Stack

### Core Technologies
- **Language**: Kotlin
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Build System**: Gradle with Kotlin DSL

### Architecture & Patterns
- **Architecture**: Clean Architecture (Domain → Data → Presentation)
- **Design Pattern**: MVI/MVVM with unidirectional data flow
- **Dependency Injection**: Hilt
- **Reactive Programming**: Coroutines + Flow

### UI Layer
- **UI Framework**: Jetpack Compose
- **Navigation**: Navigation Compose (Jetpack Navigation 3) with Serializable routes
- **State Management**: StateFlow / SharedFlow

### Data Layer
- **Networking**: Retrofit + OkHttp
- **Serialization**: Kotlinx Serialization
- **Local Database**: Room
- **Data Persistence**: SQLite

### Testing
- **Unit Testing**: Kotest
- **Property-Based Testing**: Kotest Property Testing
- **Test Pattern**: Provider methods (no @Before/@Setup annotations)
- **Coverage Requirement**: Minimum 80%

### Code Quality
- **Linter**: Ktlint
- **CI/CD**: GitHub Actions

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/yei/dev/controlerinterrapidisimo/
│   │   │   ├── domain/              # Business logic layer
│   │   │   │   ├── models/          # Domain models
│   │   │   │   ├── repositories/    # Repository interfaces
│   │   │   │   └── usecases/        # Use cases
│   │   │   ├── data/                # Data layer
│   │   │   │   ├── remote/          # API services
│   │   │   │   ├── local/           # Room database
│   │   │   │   ├── repositories/    # Repository implementations
│   │   │   │   └── mappers/         # Data mappers
│   │   │   ├── presentation/        # UI layer
│   │   │   │   ├── navigation/      # Navigation graph
│   │   │   │   ├── screens/         # Composable screens
│   │   │   │   └── viewmodels/      # ViewModels
│   │   │   └── di/                  # Dependency injection modules
│   │   └── AndroidManifest.xml
│   └── test/                        # Unit tests
└── build.gradle.kts
```

## Execution Guide

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK 34
- Gradle 8.0 or later

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/[username]/[repo].git
   cd [repo]
   ```

2. **Configure local properties**
   Create or update `local.properties` with your Android SDK path:
   ```properties
   sdk.dir=/path/to/your/Android/sdk
   ```

3. **Sync Gradle dependencies**
   ```bash
   ./gradlew build
   ```

4. **Run the application**
   - Open the project in Android Studio
   - Select a device or emulator
   - Click Run or use: `./gradlew installDebug`

### Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run unit tests with coverage
./gradlew testDebugUnitTestCoverage

# Run Ktlint checks
./gradlew ktlintCheck

# Format code with Ktlint
./gradlew ktlintFormat

# Run all checks (lint + tests)
./gradlew check
```

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run tests for specific module
./gradlew :app:testDebugUnitTest

# Run tests with coverage report
./gradlew testDebugUnitTestCoverage
# Coverage report: app/build/reports/jacoco/testDebugUnitTestCoverage/html/index.html

# Verify coverage meets 80% minimum (will fail if below threshold)
./gradlew verifyCoverage

# Run specific test class
./gradlew test --tests "com.yei.dev.controlerinterrapidisimo.domain.usecases.CheckVersionUseCaseTest"
```

### Code Quality Checks

```bash
# Run Ktlint style checks
./gradlew ktlintCheck

# Auto-format code with Ktlint
./gradlew ktlintFormat

# Run Android Lint
./gradlew lint

# Run all pre-push checks (recommended before pushing)
./scripts/pre-push-check.sh
```

## Development Guidelines

### Testing Standards
- **Always use provider methods** for test initialization (e.g., `providesSut()`, `providesRepositoryMock()`)
- **Never use @Before or @Setup** annotations for test initialization
- All test dependencies must be injected through provider methods
- Minimum 80% code coverage required
- Property-based tests should run 100+ iterations

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
- Unit test coverage MUST be minimum 80% (enforced by CI)
- Build MUST succeed before merge
- Coverage verification will fail the build if threshold is not met

## API Configuration

The application connects to Interrapidisimo's backend services. API endpoints are configured in the NetworkModule.

### Base URLs
- Version Control: `/api/ParametrosFramework/ConsultarParametrosFramework/VPStoreAppControl`
- Authentication: `/api/Seguridad/AuthenticaUsuarioApp`
- Data Sync: `/api/SincronizadorDatos/ObtenerEsquema/true`
- Localities: `/api/ParametrosFramework/ObtenerLocalidadesRecogidas`

### Special Considerations
- Kotlinx Serialization configured as lenient for API edge cases
- Interrapidisimo API returns escaped characters (e.g., `\n` in Password field)
- OkHttp interceptor handles special characters in responses

## Contributing

1. Create a feature branch from `main`
2. Implement your changes following the coding standards
3. Update CHANGELOG.md with your changes
4. Ensure all tests pass and coverage meets requirements
5. Run Ktlint formatting
6. Submit a pull request using the PR template

## License

[Specify License]

## Contact

For questions or support, contact the development team at [contact information].

---

**Version**: 1.0.0  
**Last Updated**: April 2026
