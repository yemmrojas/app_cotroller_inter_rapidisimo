package com.yei.dev.controlerinterrapidisimo.data.repositories

import com.yei.dev.controlerinterrapidisimo.data.mappers.Converter
import com.yei.dev.controlerinterrapidisimo.data.remote.NetworkHandler
import com.yei.dev.controlerinterrapidisimo.data.remote.api.LocalitiesApiService
import com.yei.dev.controlerinterrapidisimo.data.remote.dto.LocalitiesResponseDto
import com.yei.dev.controlerinterrapidisimo.data.remote.dto.LocalityDto
import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.Locality
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Property-based and unit tests for LocalitiesRepositoryImpl.
 *
 * Tests all methods of LocalitiesRepositoryImpl:
 * - fetchLocalities() with various scenarios
 */
class LocalitiesRepositoryImplTest {

    // ========== FETCH LOCALITIES TESTS ==========

    @Test
    fun `property - fetchLocalities with successful API call should return localities list`() = runTest {
        checkAll(
            iterations = 100,
            providesLocalitiesScenarios(),
        ) { scenario ->
            // Given
            // LocalitiesResponseDto is now a List<LocalityDto> directly
            val localitiesDto = scenario.dtoList

            val sut = providesSut(
                networkHandler = providesNetworkHandler(Result.Success(localitiesDto)),
            )

            // When
            var result: Result<List<Locality>>? = null
            sut.fetchLocalities().collect { result = it }

            // Then
            assert(result is Result.Success) {
                "Should return success for valid API response"
            }
            val localities = (result as Result.Success).data
            assert(localities.size == scenario.dtoList.size) {
                "Should return same number of localities: expected ${scenario.dtoList.size}, got ${localities.size}"
            }
            localities.forEachIndexed { index, locality ->
                assert(locality.cityAbbreviation == scenario.dtoList[index].cityAbbreviation) {
                    "City abbreviation should match at index $index"
                }
                assert(locality.fullName == scenario.dtoList[index].fullName) {
                    "Full name should match at index $index"
                }
            }
        }
    }

    @Test
    fun `fetchLocalities with empty list should return empty list`() = runTest {
        // Given
        // LocalitiesResponseDto is now a List<LocalityDto> directly
        val localitiesDto = emptyList<LocalityDto>()
        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Success(localitiesDto)),
        )

        // When
        var result: Result<List<Locality>>? = null
        sut.fetchLocalities().collect { result = it }

        // Then
        assert(result is Result.Success) {
            "Should return success even with empty list"
        }
        val localities = (result as Result.Success).data
        assert(localities.isEmpty()) {
            "Should return empty list"
        }
    }

    @Test
    fun `fetchLocalities with network error should return network error`() = runTest {
        // Given
        val networkError = AppError.NetworkError("No internet connection")
        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Error(networkError)),
        )

        // When
        var result: Result<List<Locality>>? = null
        sut.fetchLocalities().collect { result = it }

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
    fun `fetchLocalities with API error should return API error`() = runTest {
        // Given
        val apiError = AppError.ApiError(statusCode = 500, message = "Internal Server Error")
        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Error(apiError)),
        )

        // When
        var result: Result<List<Locality>>? = null
        sut.fetchLocalities().collect { result = it }

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
         * Provides scenarios with localities data.
         */
        private fun providesLocalitiesScenarios(): Arb<LocalitiesScenario> = arbitrary {
            val count = Arb.int(1..10).bind()
            val localities = List(count) {
                val localityId = Arb.string(minSize = 8, maxSize = 8)
                    .filter { it.isNotBlank() }
                    .bind()
                val abbreviation = Arb.string(minSize = 2, maxSize = 5)
                    .filter { it.isNotBlank() }
                    .bind()
                val fullName = Arb.string(minSize = 5, maxSize = 30)
                    .filter { it.isNotBlank() }
                    .bind()

                LocalityDto(
                    localityId = localityId,
                    cityAbbreviation = abbreviation,
                    fullName = fullName,
                )
            }

            LocalitiesScenario(dtoList = localities)
        }

        /**
         * Provides the system under test (LocalitiesRepositoryImpl).
         */
        private fun providesSut(
            networkHandler: NetworkHandler,
        ): LocalitiesRepositoryImpl {
            return LocalitiesRepositoryImpl(
                apiService = providesApiService(),
                networkHandler = networkHandler,
                localityConverter = providesLocalityConverter(),
            )
        }

        /**
         * Provides a LocalitiesApiService mock.
         */
        private fun providesApiService() = mockk<LocalitiesApiService>()

        /**
         * Provides a NetworkHandler mock.
         */
        private fun providesNetworkHandler(result: Result<LocalitiesResponseDto>) = mockk<NetworkHandler>().apply {
            coEvery { safeApiCall<LocalitiesResponseDto>(any()) } returns result
        }

        /**
         * Provides a LocalityConverter that converts LocalityDto to Locality.
         */
        private fun providesLocalityConverter() = mockk<Converter<LocalityDto, Locality>>().apply {
            every { convert(any()) } answers {
                val input = firstArg<LocalityDto>()
                Locality(
                    localityId = input.localityId,
                    cityAbbreviation = input.cityAbbreviation,
                    fullName = input.fullName,
                )
            }
            every { convertList(any()) } answers {
                val inputList = firstArg<List<LocalityDto>>()
                inputList.map { dto ->
                    Locality(
                        localityId = dto.localityId,
                        cityAbbreviation = dto.cityAbbreviation,
                        fullName = dto.fullName,
                    )
                }
            }
        }
    }

    /**
     * Data class for localities scenarios.
     */
    data class LocalitiesScenario(
        val dtoList: List<LocalityDto>,
    )
}
