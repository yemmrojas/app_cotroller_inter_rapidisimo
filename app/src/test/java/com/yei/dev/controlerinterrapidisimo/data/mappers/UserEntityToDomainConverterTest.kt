package com.yei.dev.controlerinterrapidisimo.data.mappers

import com.yei.dev.controlerinterrapidisimo.data.local.entity.UserEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Property-based tests for UserEntityToDomainConverter.
 *
 * Tests the conversion from UserEntity to UserSession domain model.
 */
class UserEntityToDomainConverterTest {

    @Test
    fun `property - convert should map all fields correctly`() = runTest {
        checkAll(
            iterations = 100,
            providesUserEntityScenarios(),
        ) { entity ->
            // Given
            val sut = providesSut()

            // When
            val result = sut.convert(entity)

            // Then
            assert(result.username == entity.username) {
                "Username should match: expected '${entity.username}', got '${result.username}'"
            }
            assert(result.name == entity.name) {
                "Name should match: expected '${entity.name}', got '${result.name}'"
            }
        }
    }

    @Test
    fun `property - convertList should convert all items`() = runTest {
        checkAll(
            iterations = 50,
            providesUserEntityListScenarios(),
        ) { entityList ->
            // Given
            val sut = providesSut()

            // When
            val result = sut.convertList(entityList)

            // Then
            assert(result.size == entityList.size) {
                "Result list size should match input: expected ${entityList.size}, got ${result.size}"
            }
            result.forEachIndexed { index, userSession ->
                assert(userSession.username == entityList[index].username) {
                    "Username should match at index $index"
                }
                assert(userSession.name == entityList[index].name) {
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
         * Provides UserEntity scenarios.
         */
        private fun providesUserEntityScenarios(): Arb<UserEntity> = arbitrary {
            val id = Arb.int(1..1000).bind()

            val username = Arb.string(minSize = 3, maxSize = 20)
                .filter { it.isNotBlank() }
                .bind()

            val name = Arb.string(minSize = 3, maxSize = 50)
                .filter { it.isNotBlank() }
                .bind()

            UserEntity(
                id = id,
                username = username,
                name = name,
            )
        }

        /**
         * Provides UserEntity list scenarios.
         */
        private fun providesUserEntityListScenarios(): Arb<List<UserEntity>> = arbitrary {
            val count = Arb.int(1..10).bind()
            List(count) {
                providesUserEntityScenarios().bind()
            }
        }

        /**
         * Provides the system under test (UserEntityToDomainConverter).
         */
        private fun providesSut(): UserEntityToDomainConverter {
            return UserEntityToDomainConverter()
        }
    }
}
