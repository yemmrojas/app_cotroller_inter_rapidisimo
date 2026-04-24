---
inclusion: auto
---

# Testing Standards - Controller APP

## Mandatory Testing Rules

### 1. Provider Methods Pattern (CRITICAL)

**ALWAYS use provider methods for ALL dependencies and test data.**

#### ✅ CORRECT Pattern
```kotlin
companion object {
    // Provider for System Under Test
    private fun providesSut(
        dependency1: Dependency1,
        dependency2: Dependency2,
    ): SystemUnderTest {
        return SystemUnderTest(
            dependency1 = dependency1,
            dependency2 = dependency2,
        )
    }
    
    // Provider for mocks
    private fun providesMockDependency() = mockk<Dependency>(relaxed = true)
    
    // Provider for specific mock behavior
    private fun providesMockWithError() = mockk<Dependency>().apply {
        coEvery { method() } throws RuntimeException("Error")
    }
    
    // Provider for data generators (Property-Based Testing)
    private fun providesValidScenarios(): Arb<Scenario> = arbitrary {
        val field = Arb.string(minSize = 3, maxSize = 20)
            .filter { it.isNotBlank() }
            .bind()
        
        Scenario(field = field)
    }
}
```

#### ❌ FORBIDDEN Patterns
```kotlin
// ❌ NO usar @Before o @BeforeEach
@Before
fun setup() {
    sut = SystemUnderTest(...)
}

// ❌ NO crear mocks inline en tests
@Test
fun test() {
    val mock = mockk<Dependency>()  // ❌ Crear provider function
}

// ❌ NO usar variables globales
private lateinit var sut: SystemUnderTest
private lateinit var mockDependency: Dependency
```

### 2. Mock Creation with mockk

**Use `mockk<T>().apply { }` pattern for all converters and mocks with behavior.**

#### ✅ CORRECT Pattern
```kotlin
private fun providesConverter() = mockk<Converter<Input, Output>>().apply {
    every { convert(any()) } answers {
        val input = firstArg<Input>()
        Output(
            field1 = input.field1,
            field2 = input.field2,
        )
    }
}

private fun providesApiService() = mockk<ApiService>().apply {
    coEvery { fetchData() } returns mockData
}
```

#### ❌ FORBIDDEN Patterns
```kotlin
// ❌ NO usar object expressions
private fun providesConverter() = object : Converter<Input, Output> {
    override fun convert(input: Input): Output { ... }
}
```

### 3. Property-Based Testing vs Unit Testing

#### When to Use Property-Based Testing (checkAll)

**Use `checkAll` when testing universal properties with variable data.**

```kotlin
@Test
fun `property - method with valid input should satisfy property`() = runTest {
    checkAll(
        iterations = 50,  // or 100 for critical paths
        providesValidScenarios(),  // MUST have a generator
    ) { scenario ->
        // Given
        val sut = providesSut(...)
        
        // When
        val result = sut.method(scenario.input)
        
        // Then
        assert(result is Success)
        assert(result.data.field == scenario.expected)
    }
}
```

**Characteristics**:
- Tests a **universal property** (e.g., "for ANY valid input, output must be valid")
- Uses **generated data** with variations
- Runs **50-100 iterations**
- Has an **Arb generator** function

#### When to Use Simple Unit Testing

**Use simple tests when testing specific scenarios without data variation.**

```kotlin
@Test
fun `method with no data should return null`() = runTest {
    // Given
    val sut = providesSut(
        dependency = providesDependencyWithNoData(),
    )
    
    // When
    val result = sut.method()
    
    // Then
    assert(result is Success)
    assert(result.data == null)
}
```

**Characteristics**:
- Tests a **specific scenario** (e.g., "what happens when there's no data")
- **No data variation** needed
- Runs **once**
- **NO `checkAll`**, NO generator

#### Decision Tree

```
Does the test need to validate behavior with DIFFERENT data?
├─ YES → Use Property-Based Testing (checkAll + generator)
│         Example: "login with ANY blank username should fail"
│
└─ NO → Use Simple Unit Test (no checkAll)
          Example: "getUserSession with no session returns null"
```

### 4. Test Structure (Given-When-Then)

**ALWAYS use Given-When-Then comments for clarity.**

```kotlin
@Test
fun `descriptive test name`() = runTest {
    // Given - Setup
    val sut = providesSut(
        dependency = providesMockDependency(),
    )
    
    // When - Execute
    var result: Result<Data>? = null
    sut.method().collect { result = it }
    
    // Then - Assert
    assert(result is Result.Success) {
        "Should return success"
    }
    assert(result.data.field == expectedValue) {
        "Field should match expected value"
    }
}
```

### 5. Test Naming Convention

**Use descriptive names that explain the scenario and expected outcome.**

#### ✅ CORRECT Names
```kotlin
// Property-Based Tests
`property - login with blank username should return validation error`
`property - login with valid credentials should save session and return success`

// Unit Tests
`getUserSession with no session should return null`
`clearUserSession with database error should return database error`
```

#### ❌ FORBIDDEN Names
```kotlin
`test login`  // Too vague
`testLoginSuccess`  // Not descriptive
`test1`, `test2`  // Meaningless
```

### 6. Assertions with Messages

**ALWAYS include descriptive messages in assertions.**

```kotlin
// ✅ CORRECT
assert(result is Result.Success) {
    "Should return success for valid input"
}
assert(session.username == expected) {
    "Username should match: expected '$expected', got '${session.username}'"
}

// ❌ FORBIDDEN
assert(result is Result.Success)  // No message
assertTrue(result is Result.Success)  // No context
```

### 7. Provider Function Naming

**Use `provides` prefix for all provider functions.**

```kotlin
// ✅ CORRECT
private fun providesSut(...): SystemUnderTest
private fun providesApiService(): ApiService
private fun providesMockWithError(): Mock
private fun providesValidScenarios(): Arb<Scenario>

// ❌ FORBIDDEN
private fun createSut()  // Use "provides"
private fun getSut()  // Use "provides"
private fun makeMock()  // Use "provides"
```

### 8. Test Organization

**Group tests by method and scenario type.**

```kotlin
class RepositoryImplTest {
    
    // ========== METHOD_NAME TESTS ==========
    
    @Test
    fun `method with scenario1 should result1`() { ... }
    
    @Test
    fun `method with scenario2 should result2`() { ... }
    
    // ========== OTHER_METHOD TESTS ==========
    
    @Test
    fun `otherMethod with scenario1 should result1`() { ... }
    
    // ========== INTEGRATION TESTS ==========
    
    @Test
    fun `integration scenario should work end-to-end`() { ... }
    
    // ========== PROVIDER METHODS ==========
    
    companion object {
        private fun providesSut(...) { ... }
        private fun providesMock(...) { ... }
    }
}
```

### 9. Coroutines Testing

**Use `runTest` for all suspend functions and Flow tests.**

```kotlin
@Test
fun `test with coroutines`() = runTest {
    // Given
    val sut = providesSut(...)
    
    // When
    var result: Result<Data>? = null
    sut.flowMethod().collect { result = it }
    
    // Then
    assert(result is Result.Success)
}
```

### 10. Mock Verification

**Verify important interactions with `coVerify`.**

```kotlin
@Test
fun `method should call dependency`() = runTest {
    // Given
    val mockDependency = providesMockDependency()
    val sut = providesSut(dependency = mockDependency)
    
    // When
    sut.method()
    
    // Then
    coVerify { mockDependency.importantMethod(any()) }
}
```

## Repository Testing Checklist

For each repository method, test:

### Success Cases
- [ ] Valid input returns success
- [ ] Data is correctly transformed
- [ ] Data is correctly saved/retrieved

### Validation Cases
- [ ] Blank/empty required fields return validation error
- [ ] Invalid format returns validation error

### Error Cases
- [ ] Network error returns network error
- [ ] API error returns API error
- [ ] Database error returns database error

### Integration Cases
- [ ] Round-trip (save → retrieve) preserves data
- [ ] Clear operation removes data

## Example: Complete Repository Test

```kotlin
class ExampleRepositoryImplTest {

    // ========== METHOD TESTS ==========

    @Test
    fun `property - method with valid input should return success`() = runTest {
        checkAll(
            iterations = 100,
            providesValidScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                apiService = providesApiService(),
                dao = providesDao(),
            )

            // When
            var result: Result<Data>? = null
            sut.method(scenario.input).collect { result = it }

            // Then
            assert(result is Result.Success) {
                "Should return success for valid input"
            }
        }
    }

    @Test
    fun `method with blank input should return validation error`() = runTest {
        // Given
        val sut = providesSut(
            apiService = providesApiService(),
            dao = providesDao(),
        )

        // When
        var result: Result<Data>? = null
        sut.method("").collect { result = it }

        // Then
        assert(result is Result.Error) {
            "Should return error for blank input"
        }
        val error = (result as Result.Error).error
        assert(error is AppError.ValidationError)
    }

    // ========== PROVIDER METHODS ==========

    companion object {
        private fun providesSut(
            apiService: ApiService,
            dao: Dao,
        ): ExampleRepositoryImpl {
            return ExampleRepositoryImpl(
                apiService = apiService,
                dao = dao,
                converter = providesConverter(),
            )
        }

        private fun providesApiService() = mockk<ApiService>(relaxed = true)

        private fun providesDao() = mockk<Dao>(relaxed = true)

        private fun providesConverter() = mockk<Converter<Dto, Domain>>().apply {
            every { convert(any()) } answers {
                val input = firstArg<Dto>()
                Domain(field = input.field)
            }
        }

        private fun providesValidScenarios(): Arb<Scenario> = arbitrary {
            val input = Arb.string(minSize = 3, maxSize = 20)
                .filter { it.isNotBlank() }
                .bind()
            Scenario(input = input)
        }
    }

    data class Scenario(val input: String)
}
```

## Summary

1. ✅ **ALWAYS** use provider methods
2. ✅ **ALWAYS** use `mockk<T>().apply { }` for mocks with behavior
3. ✅ **Use `checkAll`** only when testing with variable data
4. ✅ **Use simple tests** for specific scenarios without variation
5. ✅ **ALWAYS** use Given-When-Then structure
6. ✅ **ALWAYS** include assertion messages
7. ✅ **Test only the class's responsibilities** - don't test delegated logic
8. ✅ **Use correct mockk functions** - `coEvery` for suspend, `every` for normal
9. ❌ **NEVER** use `@Before` or global variables
10. ❌ **NEVER** create mocks inline in tests
11. ❌ **NEVER** use `checkAll` without a generator
12. ❌ **NEVER** test logic that belongs to other classes/utilities
13. ❌ **NEVER** use `coEvery` for non-suspend functions

These rules ensure consistent, maintainable, and clear tests across the entire codebase.

## 11. Distinguish Between Suspend and Non-Suspend Functions

**CRITICAL**: Use the correct mockk functions based on whether the function is suspend or not.

| Function Type | Mock Setup | Verification |
|---------------|------------|--------------|
| `suspend fun` | `coEvery { }` | `coVerify { }` |
| `fun` (normal) | `every { }` | `verify { }` |

### ✅ CORRECT Examples

```kotlin
// Suspend function
interface ApiService {
    suspend fun fetchData(): Data
}

private fun providesApiService() = mockk<ApiService>().apply {
    coEvery { fetchData() } returns mockData  // ✅ coEvery for suspend
}

// Verification
coVerify { apiService.fetchData() }  // ✅ coVerify for suspend

// Normal function
interface Dao {
    fun getData(): Data
}

private fun providesDao() = mockk<Dao>().apply {
    every { getData() } returns mockData  // ✅ every for normal
}

// Verification
verify { dao.getData() }  // ✅ verify for normal
```

### ❌ WRONG Examples

```kotlin
// ❌ Using coEvery for normal function
private fun providesDao() = mockk<Dao>().apply {
    coEvery { getData() } returns mockData  // ❌ WRONG - getData is not suspend
}

// ❌ Using every for suspend function
private fun providesApiService() = mockk<ApiService>().apply {
    every { fetchData() } returns mockData  // ❌ WRONG - fetchData is suspend
}

// ❌ Using coVerify for normal function
coVerify { dao.getData() }  // ❌ WRONG - getData is not suspend

// ❌ Using verify for suspend function
verify { apiService.fetchData() }  // ❌ WRONG - fetchData is suspend
```

### How to Check

1. Look at the function signature in the interface/class
2. If it has `suspend` keyword → use `coEvery` / `coVerify`
3. If it doesn't have `suspend` → use `every` / `verify`

```kotlin
// Check the signature
suspend fun fetchData(): Data  // ← Has "suspend" → use coEvery
fun getData(): Data            // ← No "suspend" → use every
```

### Common Mistake: Room Database Functions

Room DAO functions are **NOT suspend** by default unless explicitly marked:

```kotlin
@Dao
interface UserDao {
    fun getUser(): User           // ❌ NOT suspend → use every
    suspend fun insertUser(user: User)  // ✅ IS suspend → use coEvery
}
```

## Testing Responsibilities - What to Test

### ✅ DO Test: Class's Direct Responsibilities

**Test what the class DOES, not what it DELEGATES.**

```kotlin
// ✅ CORRECT - Testing repository's responsibilities
@Test
fun `repository should fetch data from API and return result`() {
    // Test that:
    // 1. Repository calls the API service
    // 2. Repository transforms the response
    // 3. Repository returns the correct Result type
}

// ✅ CORRECT - Testing repository delegates to utility
@Test
fun `repository should call compareVersions and return VersionStatus`() {
    // Test that:
    // 1. Repository fetches version from API
    // 2. Repository calls compareVersions utility
    // 3. Repository returns VersionStatus with correct values
    // DON'T test the comparison logic itself - that's tested in VersionExtensionsTest
}
```

### ❌ DON'T Test: Delegated Logic

**Don't duplicate tests for logic that belongs to other classes.**

```kotlin
// ❌ WRONG - Testing compareVersions logic in RepositoryTest
@Test
fun `repository should return UPDATE_NEEDED when API version is newer`() {
    // This tests compareVersions() logic, not repository logic
    // compareVersions() should have its own tests
}

// ❌ WRONG - Testing converter logic in RepositoryTest
@Test
fun `repository should correctly map all DTO fields to domain model`() {
    // This tests converter logic, not repository logic
    // Converter should have its own tests
}
```

### Decision Tree: Should I Test This?

```
Is this logic implemented IN this class?
├─ YES → Test it
│   Example: Repository's error handling, Flow creation, data fetching
│
└─ NO → Don't test it (test it in the class where it's implemented)
    Example: Version comparison (in VersionExtensions)
             Data conversion (in Converter)
             Network calls (in NetworkHandler)
```

### Example: VersionRepositoryImpl

**What VersionRepositoryImpl DOES** (test these):
- ✅ Calls NetworkHandler.safeApiCall()
- ✅ Calls compareVersions() utility
- ✅ Creates VersionStatus with correct values
- ✅ Handles network errors
- ✅ Handles API errors

**What VersionRepositoryImpl DELEGATES** (don't test these):
- ❌ Version comparison logic (tested in VersionExtensionsTest)
- ❌ Network error handling (tested in NetworkHandlerTest)
- ❌ API call execution (tested in integration tests)

### Correct Test Structure

```kotlin
@Test
fun `property - checkVersion should return VersionStatus with API data`() = runTest {
    checkAll(iterations = 100, providesVersionScenarios()) { scenario ->
        // Given
        val versionDto = VersionResponseDto(version = scenario.apiVersion)
        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Success(versionDto)),
        )

        // When
        var result: Result<VersionStatus>? = null
        sut.checkVersion(scenario.localVersion).collect { result = it }

        // Then
        assert(result is Result.Success)
        val versionStatus = (result as Result.Success).data
        
        // ✅ Test repository's responsibility: returning correct data
        assert(versionStatus.localVersion == scenario.localVersion)
        assert(versionStatus.apiVersion == scenario.apiVersion)
        
        // ❌ DON'T test compareVersions logic here
        // assert(versionStatus.status == VersionComparisonStatus.UPDATE_NEEDED)
        // That's tested in VersionExtensionsTest
    }
}
```
