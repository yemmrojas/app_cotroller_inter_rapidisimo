package com.yei.dev.controlerinterrapidisimo.data.mappers

import com.yei.dev.controlerinterrapidisimo.data.remote.dto.AuthResponseDto
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Property-based tests for AuthResponseConverter.
 *
 * Tests the conversion from AuthResponseDto to AuthResponse domain model.
 */
class AuthResponseConverterTest {

    @Test
    fun `property - convert should map all fields correctly`() = runTest {
        checkAll(
            iterations = 100,
            providesAuthResponseDtoScenarios(),
        ) { dto ->
            // Given
            val sut = providesSut()

            // When
            val result = sut.convert(dto)

            // Then
            assert(result.username == dto.username) {
                "Username should match: expected '${dto.username}', got '${result.username}'"
            }
            assert(result.identification == dto.identification) {
                "Identification should match: expected '${dto.identification}', got '${result.identification}'"
            }
            assert(result.name == dto.name) {
                "Name should match: expected '${dto.name}', got '${result.name}'"
            }
            assert(result.token == null) {
                "Token should always be null"
            }
        }
    }

    @Test
    fun `property - convertList should convert all items`() = runTest {
        checkAll(
            iterations = 50,
            providesAuthResponseDtoListScenarios(),
        ) { dtoList ->
            // Given
            val sut = providesSut()

            // When
            val result = sut.convertList(dtoList)

            // Then
            assert(result.size == dtoList.size) {
                "Result list size should match input: expected ${dtoList.size}, got ${result.size}"
            }
            result.forEachIndexed { index, authResponse ->
                assert(authResponse.username == dtoList[index].username) {
                    "Username should match at index $index"
                }
                assert(authResponse.identification == dtoList[index].identification) {
                    "Identification should match at index $index"
                }
                assert(authResponse.name == dtoList[index].name) {
                    "Name should match at index $index"
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
         * Provides AuthResponseDto scenarios.
         */
        private fun providesAuthResponseDtoScenarios(): Arb<AuthResponseDto> = arbitrary {
            val username = Arb.string(minSize = 3, maxSize = 20)
                .filter { it.isNotBlank() }
                .bind()

            val identification = Arb.string(minSize = 5, maxSize = 15)
                .filter { it.isNotBlank() }
                .bind()

            val name = Arb.string(minSize = 3, maxSize = 50)
                .filter { it.isNotBlank() }
                .bind()

            AuthResponseDto(
                username = username,
                identification = identification,
                name = name,
            )
        }

        /**
         * Provides AuthResponseDto list scenarios.
         */
        private fun providesAuthResponseDtoListScenarios(): Arb<List<AuthResponseDto>> = arbitrary {
            val count = Arb.int(1..10).bind()
            List(count) {
                providesAuthResponseDtoScenarios().bind()
            }
        }

        /**
         * Provides the system under test (AuthResponseConverter).
         */
        private fun providesSut(): AuthResponseConverter {
            return AuthResponseConverter()
        }
    }
}
