package com.yei.dev.controlerinterrapidisimo.data.mappers

import com.yei.dev.controlerinterrapidisimo.data.remote.dto.LocalityDto
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Property-based tests for LocalityConverter.
 *
 * Tests the conversion from LocalityDto to Locality domain model.
 */
class LocalityConverterTest {

    @Test
    fun `property - convert should map all fields correctly`() = runTest {
        checkAll(
            iterations = 100,
            providesLocalityDtoScenarios(),
        ) { dto ->
            // Given
            val sut = providesSut()

            // When
            val result = sut.convert(dto)

            // Then
            assert(result.cityAbbreviation == dto.cityAbbreviation) {
                "City abbreviation should match: expected '${dto.cityAbbreviation}', got '${result.cityAbbreviation}'"
            }
            assert(result.fullName == dto.fullName) {
                "Full name should match: expected '${dto.fullName}', got '${result.fullName}'"
            }
        }
    }

    @Test
    fun `property - convertList should convert all items`() = runTest {
        checkAll(
            iterations = 50,
            providesLocalityDtoListScenarios(),
        ) { dtoList ->
            // Given
            val sut = providesSut()

            // When
            val result = sut.convertList(dtoList)

            // Then
            assert(result.size == dtoList.size) {
                "Result list size should match input: expected ${dtoList.size}, got ${result.size}"
            }
            result.forEachIndexed { index, locality ->
                assert(locality.cityAbbreviation == dtoList[index].cityAbbreviation) {
                    "City abbreviation should match at index $index"
                }
                assert(locality.fullName == dtoList[index].fullName) {
                    "Full name should match at index $index"
                }
            }
        }
    }

    @Test
    fun `convertList with empty list should return empty list`() {
        // Given
        val sut = providesSut()

        // When
        val result = sut.convertList(emptyList())

        // Then
        assert(result.isEmpty()) {
            "Result should be empty list"
        }
    }

    // ========== PROVIDER METHODS ==========

    companion object {
        /**
         * Provides LocalityDto scenarios.
         */
        private fun providesLocalityDtoScenarios(): Arb<LocalityDto> = arbitrary {
            val abbreviation = Arb.string(minSize = 2, maxSize = 5)
                .filter { it.isNotBlank() }
                .bind()

            val fullName = Arb.string(minSize = 5, maxSize = 50)
                .filter { it.isNotBlank() }
                .bind()

            LocalityDto(
                cityAbbreviation = abbreviation,
                fullName = fullName,
                localityId = Arb.string(minSize = 1, maxSize = 10).bind()
            )
        }

        /**
         * Provides LocalityDto list scenarios.
         */
        private fun providesLocalityDtoListScenarios(): Arb<List<LocalityDto>> = arbitrary {
            val count = Arb.int(1..10).bind()
            List(count) {
                providesLocalityDtoScenarios().bind()
            }
        }

        /**
         * Provides the system under test (LocalityConverter).
         */
        private fun providesSut(): LocalityConverter {
            return LocalityConverter()
        }
    }
}
