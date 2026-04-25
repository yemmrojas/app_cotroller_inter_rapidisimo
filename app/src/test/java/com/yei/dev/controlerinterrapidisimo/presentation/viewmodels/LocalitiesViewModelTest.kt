package com.yei.dev.controlerinterrapidisimo.presentation.viewmodels

import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.LocalitiesState
import com.yei.dev.controlerinterrapidisimo.domain.models.Locality
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.usecases.GetLocalitiesUseCase
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for LocalitiesViewModel.
 *
 * Tests LocalitiesViewModel localities loading functionality:
 * - Loading localities successfully
 * - Error handling for localities loading
 * - Empty state handling
 * - State transitions
 *
 * Requirements: 8.1, 8.4, 14.3
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocalitiesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== LOAD LOCALITIES TESTS ==========

    @Test
    fun `property - loadLocalities with localities available should show success state`() = runTest {
        checkAll(
            iterations = 50,
            providesLocalitiesScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                getLocalitiesUseCase = providesGetLocalitiesUseCase(Result.Success(scenario.localities)),
            )

            // When
            sut.loadLocalities()
            advanceUntilIdle() // Wait for all coroutines to complete

            // Then
            val finalState = sut.state.value

            // Verify final state
            assert(finalState is LocalitiesState.Success) {
                "Final state should be Success"
            }
            val successState = finalState as LocalitiesState.Success

            // Verify locality data
            successState.localities.forEachIndexed { index, locality ->
                val expectedLocality = scenario.localities[index]
                assertEquals(expectedLocality.cityAbbreviation, locality.cityAbbreviation)
                assertEquals(expectedLocality.fullName, locality.fullName)
            }

        }
    }

    @Test
    fun `property - loadLocalities with empty localities list should show error state`() = runTest {
        // Given
        val sut = providesSut(
            getLocalitiesUseCase = providesGetLocalitiesUseCase(Result.Success(emptyList())),
        )

        // When
        sut.loadLocalities()
        advanceUntilIdle() // Wait for all coroutines to complete

        // Then
        val finalState = sut.state.value

        // Verify final state
        assert(finalState is LocalitiesState.Error) {
            "Final state should be Error when localities list is empty"
        }

        val errorState = finalState as LocalitiesState.Error
        assert(errorState.message.contains("No localities available")) {
            "Error message should indicate no localities available"
        }
    }

    @Test
    fun `property - loadLocalities with network error should show error state`() = runTest {
        checkAll(
            iterations = 50,
            providesErrorScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                getLocalitiesUseCase = providesGetLocalitiesUseCase(Result.Error(scenario.error)),
            )

            // When
            sut.loadLocalities()
            advanceUntilIdle() // Wait for all coroutines to complete

            // Then
            val finalState = sut.state.value

            // Verify final state
            assert(finalState is LocalitiesState.Error) {
                "Final state should be Error when localities loading fails"
            }

            val errorState = finalState as LocalitiesState.Error
            assert(errorState.message.contains("Failed to load localities")) {
                "Error message should indicate localities load failure"
            }

            // Verify error type is preserved in message
            when (scenario.error) {
                is AppError.NetworkError -> {
                    assert(errorState.message.contains("Network error")) {
                        "Should mention network error"
                    }
                }
                is AppError.ApiError -> {
                    assert(errorState.message.contains("API error")) {
                        "Should mention API error"
                    }
                }
                is AppError.DatabaseError -> {
                    assert(errorState.message.contains("Database error")) {
                        "Should mention database error"
                    }
                }
                is AppError.ValidationError -> {
                    assert(errorState.message.contains("Validation error")) {
                        "Should mention validation error"
                    }
                }
                is AppError.UnknownError -> {
                    assert(errorState.message.contains("Unexpected error")) {
                        "Should mention unexpected error"
                    }
                }
            }
        }
    }

    @Test
    fun `property - loadLocalities with API error should show error state`() = runTest {
        // Given
        val sut = providesSut(
            getLocalitiesUseCase = providesGetLocalitiesUseCase(
                Result.Error(AppError.ApiError(statusCode = 500, message = "Internal Server Error"))
            ),
        )

        // When
        sut.loadLocalities()
        advanceUntilIdle() // Wait for all coroutines to complete

        // Then
        val finalState = sut.state.value

        // Verify final state
        assert(finalState is LocalitiesState.Error) {
            "Final state should be Error when API fails"
        }

        val errorState = finalState as LocalitiesState.Error
        assert(errorState.message.contains("Failed to load localities")) {
            "Error message should indicate localities load failure"
        }
    }

    @Test
    fun `property - loadLocalities preserves locality data structure`() = runTest {
        checkAll(
            iterations = 50,
            providesLocalitiesScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                getLocalitiesUseCase = providesGetLocalitiesUseCase(Result.Success(scenario.localities)),
            )

            // When
            sut.loadLocalities()
            advanceUntilIdle() // Wait for all coroutines to complete

            // Then
            val finalState = sut.state.value

            // Verify final state
            assert(finalState is LocalitiesState.Success) {
                "Final state should be Success"
            }

            val successState = finalState as LocalitiesState.Success

            // Verify all localities have both required fields
            successState.localities.forEachIndexed { index, locality ->
                assert(locality.cityAbbreviation.isNotBlank()) {
                    "Locality at index $index should have non-blank abreviacionCiudad"
                }
                assert(locality.fullName.isNotBlank()) {
                    "Locality at index $index should have non-blank nombreCompleto"
                }
            }
        }
    }

    // ========== PROVIDER METHODS ==========

    companion object {
        /**
         * Provides scenarios with localities data.
         */
        private fun providesLocalitiesScenarios(): Arb<LocalitiesScenario> = arbitrary {
            val localityCount = Arb.int(1..20).bind()
            val localities = Arb.list(Arb.locality(), localityCount..localityCount).bind()

            LocalitiesScenario(localities = localities)
        }

        /**
         * Provides scenarios with various error types.
         */
        private fun providesErrorScenarios(): Arb<ErrorScenario> = arbitrary {
            val errorType = Arb.int(0..4).bind()

            val error = when (errorType) {
                0 -> AppError.NetworkError("Network error: No internet connection")
                1 -> AppError.ApiError(statusCode = 500, message = "Internal Server Error")
                2 -> AppError.DatabaseError("Database error: Failed to retrieve data")
                3 -> AppError.ValidationError(field = "localities", message = "Invalid data format")
                else -> AppError.UnknownError("Unexpected error: Something went wrong")
            }

            ErrorScenario(error = error)
        }

        /**
         * Provides a Locality generator.
         */
        private fun Arb.Companion.locality(): Arb<Locality> = arbitrary {
            val cityAbbreviation = Arb.string(minSize = 2, maxSize = 10)
                .filter { it.isNotBlank() && it.trim() == it }
                .bind()

            val fullName = Arb.string(minSize = 5, maxSize = 50)
                .filter { it.isNotBlank() && it.trim() == it }
                .bind()

            Locality(
                cityAbbreviation = cityAbbreviation,
                fullName = fullName
            )
        }

        /**
         * Provides the system under test (LocalitiesViewModel).
         */
        private fun providesSut(
            getLocalitiesUseCase: GetLocalitiesUseCase,
        ): LocalitiesViewModel {
            return LocalitiesViewModel(
                getLocalitiesUseCase = getLocalitiesUseCase,
            )
        }

        /**
         * Provides a GetLocalitiesUseCase mock.
         */
        private fun providesGetLocalitiesUseCase(result: Result<List<Locality>>) = mockk<GetLocalitiesUseCase>().apply {
            coEvery { this@apply.invoke() } returns flowOf(result)
        }
    }

    /**
     * Data class for localities scenarios.
     */
    data class LocalitiesScenario(
        val localities: List<Locality>,
    )

    /**
     * Data class for error scenarios.
     */
    data class ErrorScenario(
        val error: AppError,
    )
}
