# Contributing to Controller APP

Thank you for your interest in contributing to Controller APP! This document provides guidelines and instructions for contributing to the project.

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [Getting Started](#getting-started)
3. [Development Workflow](#development-workflow)
4. [Coding Standards](#coding-standards)
5. [Testing Requirements](#testing-requirements)
6. [Pull Request Process](#pull-request-process)
7. [Commit Message Guidelines](#commit-message-guidelines)

## Code of Conduct

- Be respectful and inclusive
- Focus on constructive feedback
- Help others learn and grow
- Maintain professional communication

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Git
- Basic knowledge of Kotlin and Android development

### Setup

1. Fork the repository
2. Clone your fork:
   ```bash
   git clone https://github.com/[your-username]/[repo].git
   cd [repo]
   ```
3. Add upstream remote:
   ```bash
   git remote add upstream https://github.com/[original-username]/[repo].git
   ```
4. Create a feature branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```

## Development Workflow

### 1. Before Starting Work

- Sync with upstream:
  ```bash
  git fetch upstream
  git rebase upstream/main
  ```
- Create a feature branch from `main`
- Ensure your local environment is set up correctly

### 2. During Development

- Write code following Clean Architecture principles
- Add tests for new functionality
- Update documentation as needed
- Run pre-push checks frequently:
  ```bash
  ./scripts/pre-push-check.sh
  ```

### 3. Before Submitting

- Ensure all tests pass
- Run Ktlint formatting:
  ```bash
  ./gradlew ktlintFormat
  ```
- Update CHANGELOG.md
- Verify coverage meets 80% minimum
- Test on a real device or emulator

## Coding Standards

### Architecture

Follow Clean Architecture with three layers:

1. **Domain Layer** (Business Logic)
   - Use cases
   - Repository interfaces
   - Domain models
   - No dependencies on other layers

2. **Data Layer** (Data Management)
   - Repository implementations
   - API services
   - Database DAOs
   - Data models and mappers

3. **Presentation Layer** (UI)
   - Composable screens
   - ViewModels
   - UI state classes
   - Navigation

### Kotlin Style Guide

- Follow official [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use Ktlint for automatic formatting
- Maximum line length: 120 characters
- Use meaningful variable and function names
- Prefer immutability (val over var)
- Use data classes for models
- Leverage Kotlin features (extension functions, sealed classes, etc.)

### Documentation

- **All interfaces MUST be documented** with KDoc
- Document public functions with complex logic
- Include parameter descriptions and return value explanations
- Add examples for non-obvious usage

Example:
```kotlin
/**
 * Authenticates a user with the provided credentials.
 *
 * @param usuario The username for authentication
 * @param password The user's password
 * @param mac The device MAC address for identification
 * @return Flow emitting Result with AuthResponse on success or AppError on failure
 */
fun authenticateUser(
    usuario: String,
    password: String,
    mac: String
): Flow<Result<AuthResponse>>
```

### Naming Conventions

- Classes: PascalCase (e.g., `UserRepository`)
- Functions: camelCase (e.g., `getUserSession`)
- Constants: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_ATTEMPTS`)
- Private properties: camelCase with underscore prefix (e.g., `_state`)
- Composables: PascalCase (e.g., `LoginScreen`)

## Testing Requirements

### Test Coverage

- Minimum 80% code coverage required
- Focus on business logic and critical paths
- Test edge cases and error scenarios

### Testing Pattern

**ALWAYS use provider methods** - NEVER use @Before or @Setup:

```kotlin
class MyViewModelTest : FunSpec({
    
    companion object {
        @JvmStatic
        fun providesSut(
            useCase: MyUseCase = mockk()
        ): MyViewModel {
            return MyViewModel(useCase)
        }
        
        @JvmStatic
        fun providesUseCaseMock(): MyUseCase = mockk()
    }
    
    test("should update state when action is triggered") {
        val useCase = providesUseCaseMock()
        val sut = providesSut(useCase)
        
        // Test implementation
    }
})
```

### Test Types

1. **Unit Tests**
   - Test individual components in isolation
   - Mock dependencies
   - Fast execution

2. **Property-Based Tests**
   - Test universal properties across many inputs
   - Use Kotest property testing
   - Validate correctness properties

3. **Integration Tests**
   - Test component interactions
   - Verify end-to-end flows
   - Use real implementations where possible

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "MyViewModelTest"

# Generate coverage report
./gradlew testDebugUnitTestCoverage
```

## Pull Request Process

### 1. Prepare Your PR

- Ensure all tests pass
- Run Ktlint formatting
- Update CHANGELOG.md
- Write clear commit messages
- Rebase on latest main

### 2. Create Pull Request

- Use the PR template
- Fill out all required sections
- Add descriptive title: `[TYPE] - Brief description`
- Link related issues
- Add screenshots for UI changes

### 3. PR Review

- Address reviewer feedback promptly
- Keep discussions focused and professional
- Make requested changes in new commits
- Squash commits before merge (if requested)

### 4. After Merge

- Delete your feature branch
- Update your local main branch
- Close related issues

## Commit Message Guidelines

### Format

```
[TYPE] - Brief description (max 72 characters)

Detailed explanation of the change (if needed).
Include motivation and context.

Closes #123
```

### Types

- `[FEATURE]` - New functionality
- `[FIX]` - Bug fixes
- `[REFACTOR]` - Code improvements without functionality changes
- `[DOCS]` - Documentation updates
- `[TEST]` - Test additions or improvements
- `[PERF]` - Performance improvements
- `[SECURITY]` - Security-related changes

### Examples

```
[FEATURE] - Add user authentication with session persistence

Implements login flow with secure credential handling and local
session storage using Room database.

Closes #42
```

```
[FIX] - Resolve crash on network timeout

Added proper error handling for timeout scenarios in NetworkHandler.
Now displays user-friendly error message instead of crashing.

Fixes #87
```

## Questions?

If you have questions or need help:

1. Check existing documentation
2. Search closed issues
3. Open a new issue with the `question` label
4. Reach out to maintainers

---

Thank you for contributing to Controller APP! 🚀
