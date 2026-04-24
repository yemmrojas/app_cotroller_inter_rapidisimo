package com.yei.dev.controlerinterrapidisimo.domain.usecases

import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.Locality
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.repositories.LocalitiesRepository
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests for GetLocalitiesUseCase.
 *
 * Tests verify that the use case correctly delegates to the LocalitiesRepository
 * and returns the expected results for various scenarios.
 */
class GetLocalitiesUseCaseTest {

    @Test
    fun `invoke should return success with localities when retrieval succeeds`() = runTest {
        // Given
        val scenario = providesSuccessScenarios().first()
        val mockRepository = providesLocalitiesRepository(
            result = Result.Success(scenario.expectedLocalities),
        )
        val sut = providesSut(localitiesRepository = mockRepository)

        // When
        sut().collect { result ->
            // Then
            assert(result is Result.Success)
            assertEquals((result as Result.Success).data, scenario.expectedLocalities)
        }
    }

    @Test
    fun `invoke should return error when retrieval fails`() = runTest {
        // Given
        val scenario = providesErrorScenarios().first()
        val mockRepository = providesLocalitiesRepository(
            result = Result.Error(scenario.error),
        )
        val sut = providesSut(localitiesRepository = mockRepository)

        // When
        sut().collect { result ->
            // Then
            assert(result is Result.Error)
            assertEquals((result as Result.Error).error, scenario.error)
        }
    }

    // Provider methods
    companion object {
        fun providesSuccessScenarios() = listOf(
            SuccessScenario(
                description = "Multiple localities",
                expectedLocalities = listOf(
                    Locality(cityAbbreviation = "BOG", fullName = "Bogotá D.C."),
                    Locality(cityAbbreviation = "MED", fullName = "Medellín"),
                    Locality(cityAbbreviation = "CAL", fullName = "Cali"),
                ),
            ),
            SuccessScenario(
                description = "Single locality",
                expectedLocalities = listOf(
                    Locality(cityAbbreviation = "BAQ", fullName = "Barranquilla"),
                ),
            ),
            SuccessScenario(
                description = "Empty localities list",
                expectedLocalities = emptyList(),
            ),
        )

        fun providesErrorScenarios() = listOf(
            ErrorScenario(
                description = "Network error fetching localities",
                error = AppError.NetworkError("No internet connection"),
            ),
            ErrorScenario(
                description = "API error",
                error = AppError.ApiError(500, "Internal server error"),
            ),
            ErrorScenario(
                description = "Unknown error",
                error = AppError.UnknownError("Unexpected error occurred"),
            ),
        )

        private fun providesSut(
            localitiesRepository: LocalitiesRepository,
        ): GetLocalitiesUseCase =
            GetLocalitiesUseCase(localitiesRepository = localitiesRepository)

        private fun providesLocalitiesRepository(
            result: Result<List<Locality>>,
        ): LocalitiesRepository {
            return mockk<LocalitiesRepository> {
                every { fetchLocalities() } returns flowOf(result)
            }
        }
    }

    data class SuccessScenario(
        val description: String,
        val expectedLocalities: List<Locality>,
    ) {
        override fun toString(): String = description
    }

    data class ErrorScenario(
        val description: String,
        val error: AppError,
    ) {
        override fun toString(): String = description
    }
}
