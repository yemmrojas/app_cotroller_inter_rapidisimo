package com.yei.dev.controlerinterrapidisimo.data.repositories

import com.yei.dev.controlerinterrapidisimo.data.local.dao.UserDao
import com.yei.dev.controlerinterrapidisimo.data.local.entity.UserEntity
import com.yei.dev.controlerinterrapidisimo.data.mappers.Converter
import com.yei.dev.controlerinterrapidisimo.data.remote.NetworkHandler
import com.yei.dev.controlerinterrapidisimo.data.remote.api.AuthApiService
import com.yei.dev.controlerinterrapidisimo.data.remote.dto.AuthResponseDto
import com.yei.dev.controlerinterrapidisimo.data.repositories.AuthRepositoryImpl.Companion.FIELD_CREDENTIALS
import com.yei.dev.controlerinterrapidisimo.data.repositories.AuthRepositoryImpl.Companion.FIELD_USER
import com.yei.dev.controlerinterrapidisimo.data.repositories.AuthRepositoryImpl.Companion.MESSAGE_EMPTY_CREDENTIALS
import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.AuthResponse
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.UserSession
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Property-based test for AuthRepositoryImpl.
 *
 * Property 3: Authentication and Session Round-Trip
 * For any valid authentication credentials, if authentication succeeds and user data is stored,
 * then retrieving the user session should return the same Usuario, Identificacion, and Nombre
 * values that were stored.
 *
 * Validates: Requirements 2.3, 2.5, 4.1, 4.3, 11.1
 *
 * Tests all methods of AuthRepositoryImpl:
 * - login() with various scenarios
 * - getUserSession() with various scenarios
 * - clearUserSession() with various scenarios
 */
class AuthRepositoryImplTest {

    // ========== LOGIN TESTS ==========

    @Test
    fun `property - login with blank username should return validation error`() = runTest {
        checkAll(
            iterations = 50,
            providesBlankUsernameScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                networkHandler = providesNetworkHandler(Result.Success(mockk())),
                userDao = providesUserDao(),
            )

            // When
            var result: Result<UserSession>? = null
            sut.login(
                username = scenario.username,
                password = scenario.password,
                mac = scenario.mac,
            ).collect { result = it }

            // Then
            assert(result is Result.Error) {
                "Should return error for blank username"
            }
            val error = (result as Result.Error).error
            assert(error is AppError.ValidationError) {
                "Should be ValidationError, got: ${error::class.simpleName}"
            }
            assert((error as AppError.ValidationError).field == "credentials") {
                "Field should be 'credentials'"
            }
            assertEquals(error.message,  MESSAGE_EMPTY_CREDENTIALS)
        }
    }

    @Test
    fun `property - login with blank password should return validation error`() = runTest {
        checkAll(
            iterations = 50,
            providesBlankPasswordScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                networkHandler = providesNetworkHandler(Result.Success(mockk())),
                userDao = providesUserDao(),
            )

            // When
            var result: Result<UserSession>? = null
            sut.login(
                username = scenario.username,
                password = scenario.password,
                mac = scenario.mac,
            ).collect { result = it }

            // Then
            assert(result is Result.Error) {
                "Should return error for blank password"
            }
            val error = (result as Result.Error).error
            assert(error is AppError.ValidationError) {
                "Should be ValidationError"
            }
            assert((error as AppError.ValidationError).field == FIELD_CREDENTIALS)
        }
    }

    @Test
    fun `property - login with valid credentials and successful API call should save session and return success`() =
        runTest {
            checkAll(
                iterations = 100,
                providesValidLoginScenarios(),
            ) { scenario ->
                // Given
                val (mockUserDao, capturedEntities) = providesUserDaoWithCapture()

                val authResponseDto = AuthResponseDto(
                    username = scenario.expectedSession.username,
                    name = scenario.expectedSession.name,
                    identification = "123456",
                )

                val sut = providesSut(
                    networkHandler = providesNetworkHandler(Result.Success(authResponseDto)),
                    userDao = mockUserDao,
                )

                // When
                var result: Result<UserSession>? = null
                sut.login(
                    username = scenario.username,
                    password = scenario.password,
                    mac = scenario.mac,
                ).collect { result = it }

                // Then
                assert(result is Result.Success) {
                    "Should return success for valid login"
                }
                val session = (result as Result.Success).data
                assert(session.username == scenario.expectedSession.username) {
                    "Username should match: expected '${scenario.expectedSession.username}', got '${session.username}'"
                }
                assert(session.name == scenario.expectedSession.name) {
                    "Name should match"
                }

                // Verify session was saved to database
                coVerify { mockUserDao.insertUser(any()) }
                assert(capturedEntities.size == 1) {
                    "Should have saved exactly one entity"
                }
            }
        }

    @Test
    fun `property - login with API returning blank username should return validation error`() =
        runTest {
            checkAll(
                iterations = 50,
                providesValidCredentialsScenarios(),
            ) { scenario ->
                // Given
                val authResponseDto = AuthResponseDto(
                    username = "",
                    identification = "123456",
                    name = "Test User",
                )

                val sut = providesSut(
                    networkHandler = providesNetworkHandler(Result.Success(authResponseDto)),
                    userDao = providesUserDao(),
                )

                // When
                var result: Result<UserSession>? = null
                sut.login(
                    username = scenario.username,
                    password = scenario.password,
                    mac = scenario.mac,
                ).collect { result = it }

                // Then
                assert(result is Result.Error) {
                    "Should return error for blank username in response"
                }
                val error = (result as Result.Error).error
                assert(error is AppError.ValidationError) {
                    "Should be ValidationError"
                }
                assert((error as AppError.ValidationError).field == FIELD_USER) {
                    "Field should be 'user'"
                }
            }
        }



    @Test
    fun `property - login with API returning blank name should return validation error`() =
        runTest {
            checkAll(
                iterations = 50,
                providesValidCredentialsScenarios(),
            ) { scenario ->
                // Given
                val authResponseDto = AuthResponseDto(
                    username = "testUser",
                    identification = "123456",
                    name = "",
                )

                val sut = providesSut(
                    networkHandler = providesNetworkHandler(Result.Success(authResponseDto)),
                    userDao = providesUserDao(),
                )

                // When
                var result: Result<UserSession>? = null
                sut.login(
                    username = scenario.username,
                    password = scenario.password,
                    mac = scenario.mac,
                ).collect { result = it }

                // Then
                assert(result is Result.Error) {
                    "Should return error for blank name in response"
                }
                val error = (result as Result.Error).error
                assert(error is AppError.ValidationError) {
                    "Should be ValidationError"
                }
            }
        }

    @Test
    fun `property - login with network error should return network error`() = runTest {
        checkAll(
            iterations = 50,
            providesValidCredentialsScenarios(),
        ) { scenario ->
            // Given
            val networkError = AppError.NetworkError("No internet connection")
            val sut = providesSut(
                networkHandler = providesNetworkHandler(Result.Error(networkError)),
                userDao = providesUserDao(),
            )

            // When
            var result: Result<UserSession>? = null
            sut.login(
                username = scenario.username,
                password = scenario.password,
                mac = scenario.mac,
            ).collect { result = it }

            // Then
            assert(result is Result.Error) {
                "Should return error for network failure"
            }
            val error = (result as Result.Error).error
            assert(error is AppError.NetworkError) {
                "Should be NetworkError, got: ${error::class.simpleName}"
            }
        }
    }

    @Test
    fun `property - login with API error should return API error`() = runTest {
        checkAll(
            iterations = 50,
            providesValidCredentialsScenarios(),
        ) { scenario ->
            // Given
            val apiError = AppError.ApiError(statusCode = 401, message = "Unauthorized")
            val sut = providesSut(
                networkHandler = providesNetworkHandler(Result.Error(apiError)),
                userDao = providesUserDao(),
            )

            // When
            var result: Result<UserSession>? = null
            sut.login(
                username = scenario.username,
                password = scenario.password,
                mac = scenario.mac,
            ).collect { result = it }

            // Then
            assert(result is Result.Error) {
                "Should return error for API failure"
            }
            val error = (result as Result.Error).error
            assert(error is AppError.ApiError) {
                "Should be ApiError"
            }
        }
    }

    @Test
    fun `property - login with database error should return database error`() = runTest {
        checkAll(
            iterations = 50,
            providesValidLoginScenarios(),
        ) { scenario ->
            // Given
            val authResponseDto = AuthResponseDto(
                username = scenario.expectedSession.username,
                identification = "123456",
                name = scenario.expectedSession.name,
            )

            val sut = providesSut(
                networkHandler = providesNetworkHandler(Result.Success(authResponseDto)),
                userDao = providesUserDaoWithInsertError(),
            )

            // When
            var result: Result<UserSession>? = null
            sut.login(
                username = scenario.username,
                password = scenario.password,
                mac = scenario.mac,
            ).collect { result = it }

            // Then
            assert(result is Result.Error) {
                "Should return error for database failure"
            }
            val error = (result as Result.Error).error
            assert(error is AppError.DatabaseError) {
                "Should be DatabaseError, got: ${error::class.simpleName}"
            }
            assert((error as AppError.DatabaseError).message == "Failed to save user session") {
                "Error message should match"
            }
        }
    }

    // ========== GET USER SESSION TESTS ==========

    @Test
    fun `property - getUserSession with existing session should return session data`() = runTest {
        checkAll(
            iterations = 100,
            providesValidUserSessionScenarios(),
        ) { scenario ->
            // Given
            val userEntity = UserEntity(
                id = 1,
                username = scenario.username,
                name = scenario.name
            )

            val sut = providesSut(
                networkHandler = providesNetworkHandler(Result.Success(mockk())),
                userDao = providesUserDaoWithUser(userEntity),
            )

            // When
            var result: Result<UserSession?>? = null
            sut.getUserSession().collect { result = it }

            // Then
            assert(result is Result.Success) {
                "Should return success"
            }
            val retrievedSession = (result as Result.Success).data
            assert(retrievedSession != null) {
                "Session should not be null"
            }
            assert(retrievedSession!!.username == scenario.username) {
                "Username should match"
            }
            assert(retrievedSession.name == scenario.name) {
                "Name should match"
            }
        }
    }

    @Test
    fun `property - getUserSession with no session should return null`() = runTest {
        // Given
        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Success(mockk())),
            userDao = providesUserDaoWithNoSession(),
        )

        // When
        var result: Result<UserSession?>? = null
        sut.getUserSession().collect { result = it }

        // Then
        assert(result is Result.Success) {
            "Should return success even with no session"
        }
        val retrievedSession = (result as Result.Success).data
        assert(retrievedSession == null) {
            "Session should be null when no session exists"
        }
    }

    @Test
    fun `property - getUserSession with database error should return database error`() = runTest {
        // Given
        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Success(mockk())),
            userDao = providesUserDaoWithGetError(),
        )

        // When
        var result: Result<UserSession?>? = null
        sut.getUserSession().collect { result = it }

        // Then
        assert(result is Result.Error) {
            "Should return error for database failure"
        }
        val error = (result as Result.Error).error
        assert(error is AppError.DatabaseError) {
            "Should be DatabaseError"
        }
        assert((error as AppError.DatabaseError).message == "Failed to retrieve user session") {
            "Error message should match"
        }
    }

    // ========== CLEAR USER SESSION TESTS ==========

    @Test
    fun `property - clearUserSession should successfully clear session`() = runTest {
        // Given
        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Success(mockk())),
            userDao = providesUserDao(),
        )

        // When
        var result: Result<Unit>? = null
        sut.clearUserSession().collect { result = it }

        // Then
        assert(result is Result.Success) {
            "Should return success"
        }
    }

    @Test
    fun `property - clearUserSession with database error should return database error`() = runTest {
        // Given
        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Success(mockk())),
            userDao = providesUserDaoWithClearError(),
        )

        // When
        var result: Result<Unit>? = null
        sut.clearUserSession().collect { result = it }

        // Then
        assert(result is Result.Error) {
            "Should return error for database failure"
        }
        val error = (result as Result.Error).error
        assert(error is AppError.DatabaseError) {
            "Should be DatabaseError"
        }
        assert((error as AppError.DatabaseError).message == "Failed to clear user session") {
            "Error message should match"
        }
    }

    // ========== INTEGRATION TESTS ==========

    @Test
    fun `property - authentication round-trip preserves user data`() = runTest {
        checkAll(
            iterations = 100,
            providesValidUserSessionScenarios(),
        ) { scenario ->
            // Given
            val (mockUserDao) = providesUserDaoWithSlot()

            val authResponseDto = AuthResponseDto(
                username = scenario.username,
                identification = "123456",
                name = scenario.name,
            )

            val sut = providesSut(
                networkHandler = providesNetworkHandler(Result.Success(authResponseDto)),
                userDao = mockUserDao,
            )

            // When - Login (store session)
            var loginResult: Result<UserSession>? = null
            sut.login(
                username = scenario.username,
                password = "validPassword123",
                mac = "00:11:22:33:44:55",
            ).collect { result ->
                loginResult = result
            }

            // Then - Login should succeed
            assert(loginResult is Result.Success) {
                "Login should succeed for valid credentials"
            }

            // When - Retrieve session
            var retrievedResult: Result<UserSession?>? = null
            sut.getUserSession().collect { result ->
                retrievedResult = result
            }

            // Then - Retrieved session should match stored session
            assert(retrievedResult is Result.Success) {
                "Session retrieval should succeed"
            }

            val retrievedSession = (retrievedResult as Result.Success).data
            assert(retrievedSession != null) {
                "Retrieved session should not be null"
            }

            // Property verification: Round-trip preserves all user data
            assert(retrievedSession!!.username == scenario.username) {
                "Username should be preserved"
            }
            assert(retrievedSession.name == scenario.name) {
                "Name should be preserved"
            }
        }
    }

    @Test
    fun `property - clear session removes all user data`() = runTest {
        checkAll(
            iterations = 100,
            providesValidUserSessionScenarios(),
        ) { scenario ->
            // Given
            val (mockUserDao) = providesUserDaoWithSlot()

            val authResponseDto = AuthResponseDto(
                username = scenario.username,
                identification = "123456",
                name = scenario.name,
            )

            val sut = providesSut(
                networkHandler = providesNetworkHandler(Result.Success(authResponseDto)),
                userDao = mockUserDao,
            )

            // When - Login, then clear session
            sut.login(
                username = scenario.username,
                password = "validPassword123",
                mac = "00:11:22:33:44:55",
            ).collect { /* consume */ }

            sut.clearUserSession().collect { /* consume */ }

            // When - Try to retrieve session after clearing
            var retrievedResult: Result<UserSession?>? = null
            sut.getUserSession().collect { result ->
                retrievedResult = result
            }

            // Then - Session should be null after clearing
            assert(retrievedResult is Result.Success) {
                "Retrieval should succeed even when no session exists"
            }

            val retrievedSession = (retrievedResult as Result.Success).data
            assert(retrievedSession == null) {
                "Session should be null after clearing"
            }

            // Verify clearUsers was called
            coVerify { mockUserDao.clearUsers() }
        }
    }

    // ========== PROVIDER METHODS ==========

    companion object {
        /**
         * Provides scenarios with blank username.
         */
        private fun providesBlankUsernameScenarios(): Arb<LoginScenario> = arbitrary {
            val username = Arb.string(minSize = 0, maxSize = 5)
                .filter { it.isBlank() }
                .bind()

            val password = Arb.string(minSize = 5, maxSize = 20)
                .filter { it.isNotBlank() }
                .bind()

            LoginScenario(
                username = username,
                password = password,
                mac = "00:11:22:33:44:55",
            )
        }

        /**
         * Provides scenarios with blank password.
         */
        private fun providesBlankPasswordScenarios(): Arb<LoginScenario> = arbitrary {
            val username = Arb.string(minSize = 3, maxSize = 20)
                .filter { it.isNotBlank() }
                .bind()

            val password = Arb.string(minSize = 0, maxSize = 5)
                .filter { it.isBlank() }
                .bind()

            LoginScenario(
                username = username,
                password = password,
                mac = "00:11:22:33:44:55",
            )
        }

        /**
         * Provides scenarios with valid credentials.
         */
        private fun providesValidCredentialsScenarios(): Arb<LoginScenario> = arbitrary {
            val username = Arb.string(minSize = 3, maxSize = 20)
                .filter { it.isNotBlank() && it.trim() == it }
                .bind()

            val password = Arb.string(minSize = 5, maxSize = 20)
                .filter { it.isNotBlank() && it.trim() == it }
                .bind()

            LoginScenario(
                username = username,
                password = password,
                mac = "00:11:22:33:44:55",
            )
        }

        /**
         * Provides scenarios with valid login data and expected session.
         */
        private fun providesValidLoginScenarios(): Arb<ValidLoginScenario> = arbitrary {
            val username = Arb.string(minSize = 3, maxSize = 20)
                .filter { it.isNotBlank() && it.trim() == it }
                .bind()

            val password = Arb.string(minSize = 5, maxSize = 20)
                .filter { it.isNotBlank() && it.trim() == it }
                .bind()

            val name = Arb.string(minSize = 3, maxSize = 50)
                .filter { it.isNotBlank() && it.trim() == it }
                .bind()

            ValidLoginScenario(
                username = username,
                password = password,
                mac = "00:11:22:33:44:55",
                expectedSession = UserSession(
                    username = username,
                    name = name
                )
            )
        }

        /**
         * Provides scenarios with valid user sessions.
         */
        private fun providesValidUserSessionScenarios(): Arb<UserSession> = arbitrary {
            val username = Arb.string(minSize = 3, maxSize = 20)
                .filter { it.isNotBlank() && it.trim() == it }
                .bind()

            val name = Arb.string(minSize = 3, maxSize = 50)
                .filter { it.isNotBlank() && it.trim() == it }
                .bind()

            UserSession(
                username = username,
                name = name
            )
        }

        /**
         * Provides the system under test (AuthRepositoryImpl).
         */
        private fun providesSut(
            networkHandler: NetworkHandler,
            userDao: UserDao,
        ): AuthRepositoryImpl {
            return AuthRepositoryImpl(
                apiService = providesApiService(),
                userDao = userDao,
                networkHandler = networkHandler,
                authResponseConverter = providesAuthResponseConverter(),
                userEntityToDomainConverter = providesUserEntityToDomainConverter(),
                userSessionToEntityConverter = providesUserSessionToEntityConverter(),
            )
        }

        /**
         * Provides an AuthApiService mock.
         */
        private fun providesApiService() = mockk<AuthApiService>()

        /**
         * Provides a NetworkHandler mock.
         */
        private fun providesNetworkHandler(result: Result<AuthResponseDto>) = mockk<NetworkHandler>().apply {
            coEvery { safeApiCall<AuthResponseDto>(any()) } returns result
        }

        /**
         * Provides a UserDao mock.
         */
        private fun providesUserDao() = mockk<UserDao>(relaxed = true)

        /**
         * Provides a UserDao mock that throws an exception on insertUser.
         */
        private fun providesUserDaoWithInsertError() = mockk<UserDao>().apply {
            coEvery { insertUser(any()) } throws RuntimeException("Database error")
        }

        /**
         * Provides a UserDao mock that returns null on getUser.
         */
        private fun providesUserDaoWithNoSession() = mockk<UserDao>().apply {
            coEvery { getUser() } returns null
        }

        /**
         * Provides a UserDao mock that throws an exception on getUser.
         */
        private fun providesUserDaoWithGetError() = mockk<UserDao>().apply {
            coEvery { getUser() } throws RuntimeException("Database error")
        }

        /**
         * Provides a UserDao mock that throws an exception on clearUsers.
         */
        private fun providesUserDaoWithClearError() = mockk<UserDao>().apply {
            coEvery { clearUsers() } throws RuntimeException("Database error")
        }

        /**
         * Provides a UserDao mock configured for a specific user entity.
         */
        private fun providesUserDaoWithUser(userEntity: UserEntity) = mockk<UserDao>().apply {
            coEvery { getUser() } returns userEntity
        }

        /**
         * Provides a UserDao mock that captures inserted entities.
         * Returns a Pair of (UserDao, MutableList<UserEntity>) where the list contains captured entities.
         */
        private fun providesUserDaoWithCapture(): Pair<UserDao, MutableList<UserEntity>> {
            val capturedEntities = mutableListOf<UserEntity>()
            val mockUserDao = mockk<UserDao>(relaxed = true).apply {
                coEvery { insertUser(any()) } answers {
                    capturedEntities.add(firstArg())
                }
            }
            return Pair(mockUserDao, capturedEntities)
        }

        /**
         * Provides a UserDao mock that simulates persistent storage using a slot.
         * Useful for testing round-trip scenarios (insert -> retrieve -> clear).
         * Returns a Pair of (UserDao, CapturingSlot<UserEntity>).
         */
        private fun providesUserDaoWithSlot(): Pair<UserDao, CapturingSlot<UserEntity>> {
            val storedEntity = slot<UserEntity>()
            val mockUserDao = mockk<UserDao>(relaxed = true).apply {
                coEvery { insertUser(capture(storedEntity)) } returns Unit
                coEvery { getUser() } answers {
                    if (storedEntity.isCaptured) storedEntity.captured else null
                }
                coEvery { clearUsers() } answers {
                    storedEntity.clear()
                }
            }
            return Pair(mockUserDao, storedEntity)
        }

        /**
         * Provides an AuthResponseConverter that converts AuthResponseDto to AuthResponse.
         */

        private fun providesAuthResponseConverter() = mockk<Converter<AuthResponseDto, AuthResponse>>().apply {
            every { convert(any()) } answers {
                val input = firstArg<AuthResponseDto>()
                AuthResponse(
                    username = input.username,
                    identification = input.identification,
                    name = input.name,
                    token = null,
                )
            }
        }


        /**
         * Provides a UserEntityToDomainConverter that converts UserEntity to UserSession.
         */
        private fun providesUserEntityToDomainConverter() = mockk<Converter<UserEntity, UserSession>>().apply {
            every { convert(any()) } answers {
                val input = firstArg<UserEntity>()
                UserSession(
                    username = input.username,
                    name = input.name
                )
            }
        }

        /**
         * Provides a UserSessionToEntityConverter that converts UserSession to UserEntity.
         */
        private fun providesUserSessionToEntityConverter() = mockk<Converter<UserSession, UserEntity>>().apply {
            every { convert(any()) } answers {
                val input = firstArg<UserSession>()
                UserEntity(
                    id = 1,
                    username = input.username,
                    name = input.name
                )
            }
        }
    }

    /**
     * Data class for login scenarios.
     */
    data class LoginScenario(
        val username: String,
        val password: String,
        val mac: String,
    )

    /**
     * Data class for valid login scenarios with expected session.
     */
    data class ValidLoginScenario(
        val username: String,
        val password: String,
        val mac: String,
        val expectedSession: UserSession,
    )
}
