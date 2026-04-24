package com.yei.dev.controlerinterrapidisimo.data.repositories

import com.yei.dev.controlerinterrapidisimo.data.remote.NetworkHandler
import com.yei.dev.controlerinterrapidisimo.data.remote.api.VersionApiService
import com.yei.dev.controlerinterrapidisimo.data.remote.dto.VersionResponseDto
import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.VersionStatus
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Property-based and unit tests for VersionRepositoryImpl.
 *
 * Tests the repository's responsibilities:
 * - Fetching version from API via NetworkHandler
 * - Delegating version comparison to compareVersions utility
 * - Returning VersionStatus with comparison result
 * - Error handling (network, API)
 *
 * Note: Version comparison logic is tested separately in VersionExtensionsTest (Task 11.2)
 */
class VersionRepositoryImplTest {

    // ========== CHECK VERSION TESTS ==========

    @Test
    fun `property - checkVersion with successful API call should return VersionStatus`() = runTest {
        checkAll(
            iterations = 100,
            providesVersionScenarios(),
        ) { scenario ->
            // Given
            val versionDto = VersionResponseDto(version = scenario.apiVersion)
            val sut = providesSut(
                networkHandler = providesNetworkHandler(Result.Success(versionDto)),
            )

            // When
            var result: Result<VersionStatus>? = null
            sut.checkVersion(scenario.localVersion).collect { result = it }

            // Then
            assert(result is Result.Success) {
                "Should return success for valid API response"
            }
            val versionStatus = (result as Result.Success).data
            assert(versionStatus.localVersion == scenario.localVersion) {
                "Local version should match input: expected '${scenario.localVersion}', got '${versionStatus.localVersion}'"
            }
            assert(versionStatus.apiVersion == scenario.apiVersion) {
                "API version should match response: expected '${scenario.apiVersion}', got '${versionStatus.apiVersion}'"
            }
            // Note: We don't assert the status value here because that's the responsibility
            // of compareVersions() which has its own tests (Task 11.2)
        }
    }

    @Test
    fun `checkVersion with network error should return network error`() = runTest {
        // Given
        val networkError = AppError.NetworkError("No internet connection")
        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Error(networkError)),
        )

        // When
        var result: Result<VersionStatus>? = null
        sut.checkVersion("1.0.0").collect { result = it }

        // Then
        assert(result is Result.Error) {
            "Should return error for network failure"
        }
        val error = (result as Result.Error).error
        assert(error is AppError.NetworkError) {
            "Should be NetworkError"
        }
    }

    @Test
    fun `checkVersion with API error should return API error`() = runTest {
        // Given
        val apiError = AppError.ApiError(statusCode = 500, message = "Internal Server Error")
        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Error(apiError)),
        )

        // When
        var result: Result<VersionStatus>? = null
        sut.checkVersion("1.0.0").collect { result = it }

        // Then
        assert(result is Result.Error) {
            "Should return error for API failure"
        }
        val error = (result as Result.Error).error
        assert(error is AppError.ApiError) {
            "Should be ApiError"
        }
    }

    // ========== PROVIDER METHODS ==========

    companion object {
        /**
         * Provides scenarios with various version combinations.
         * The actual comparison logic is tested in VersionExtensionsTest.
         */
        private fun providesVersionScenarios(): Arb<VersionScenario> = arbitrary {
            val major = Arb.int(1..10).bind()
            val minor = Arb.int(0..20).bind()
            val patch = Arb.int(0..50).bind()

            val localVersion = "$major.$minor.$patch"
            
            // Generate a random API version (could be same, newer, or older)
            val apiMajor = Arb.int(1..10).bind()
            val apiMinor = Arb.int(0..20).bind()
            val apiPatch = Arb.int(0..50).bind()
            val apiVersion = "$apiMajor.$apiMinor.$apiPatch"

            VersionScenario(
                localVersion = localVersion,
                apiVersion = apiVersion,
            )
        }

        /**
         * Provides the system under test (VersionRepositoryImpl).
         */
        private fun providesSut(
            networkHandler: NetworkHandler,
        ): VersionRepositoryImpl {
            return VersionRepositoryImpl(
                apiService = providesApiService(),
                networkHandler = networkHandler,
            )
        }

        /**
         * Provides a VersionApiService mock.
         */
        private fun providesApiService() = mockk<VersionApiService>()

        /**
         * Provides a NetworkHandler mock.
         */
        private fun providesNetworkHandler(result: Result<VersionResponseDto>) = mockk<NetworkHandler>().apply {
            coEvery { safeApiCall<VersionResponseDto>(any()) } returns result
        }
    }

    /**
     * Data class for version scenarios.
     */
    data class VersionScenario(
        val localVersion: String,
        val apiVersion: String,
    )
}
