package com.yei.dev.controlerinterrapidisimo.data.mappers

import com.yei.dev.controlerinterrapidisimo.domain.models.UserSession
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Property-based tests for UserSessionToEntityConverter.
 *
 * Tests the conversion from UserSession domain model to UserEntity.
 */
class UserSessionToEntityConverterTest {

    @Test
    fun `property - convert should map all fields correctly`() = runTest {
        checkAll(
            iterations = 100,
            providesUserSessionScenarios(),
        ) { session ->
            // Given
            val sut = providesSut()

            // When
            val result = sut.convert(session)

            // Then
            assert(result.id == 1) {
                "ID should always be 1 (single user session)"
            }
            assert(result.username == session.username) {
                "Username should match: expected '${session.username}', got '${result.username}'"
            }
            assert(result.name == session.name) {
                "Name should match: expected '${session.name}', got '${result.name}'"
            }
        }
    }

    @Test
    fun `property - convertList should convert all items with id 1`() = runTest {
        checkAll(
            iterations = 50,
            providesUserSessionListScenarios(),
        ) { sessionList ->
            // Given
            val sut = providesSut()

            // When
            val result = sut.convertList(sessionList)

            // Then
            assert(result.size == sessionList.size) {
                "Result list size should match input: expected ${sessionList.size}, got ${result.size}"
            }
            result.forEachIndexed { index, userEntity ->
                assert(userEntity.id == 1) {
                    "ID should always be 1 at index $index"
                }
                assert(userEntity.username == sessionList[index].username) {
                    "Username should match at index $index"
                }
                assert(userEntity.name == sessionList[index].name) {
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
         * Provides UserSession scenarios.
         */
        private fun providesUserSessionScenarios(): Arb<UserSession> = arbitrary {
            val username = Arb.string(minSize = 3, maxSize = 20)
                .filter { it.isNotBlank() }
                .bind()

            val name = Arb.string(minSize = 3, maxSize = 50)
                .filter { it.isNotBlank() }
                .bind()

            UserSession(
                username = username,
                name = name,
            )
        }

        /**
         * Provides UserSession list scenarios.
         */
        private fun providesUserSessionListScenarios(): Arb<List<UserSession>> = arbitrary {
            val count = Arb.int(1..10).bind()
            List(count) {
                providesUserSessionScenarios().bind()
            }
        }

        /**
         * Provides the system under test (UserSessionToEntityConverter).
         */
        private fun providesSut(): UserSessionToEntityConverter {
            return UserSessionToEntityConverter()
        }
    }
}
