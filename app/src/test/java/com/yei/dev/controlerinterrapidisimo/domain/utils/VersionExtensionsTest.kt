package com.yei.dev.controlerinterrapidisimo.domain.utils

import com.yei.dev.controlerinterrapidisimo.domain.models.VersionComparisonStatus
import junit.framework.TestCase.assertEquals
import org.junit.Test

/**
 * Unit tests for VersionExtensions.
 *
 * Tests verify that the compareVersion function correctly compares
 * version strings by normalizing them (removing dots) and comparing as integers.
 *
 * This approach works for both:
 * - Semantic versioning: "1.0.0" -> 100
 * - Simple format: "100" -> 100
 */
class VersionExtensionsTest {

    @Test
    fun `compareVersion should return UP_TO_DATE when normalized versions are equal`() {
        // Given
        val scenarios = providesEqualVersionScenarios()

        scenarios.forEach { scenario ->
            // When
            val result = scenario.localVersion.compareVersion(scenario.remoteVersion)

            // Then
            assertEquals(
                "Failed for: ${scenario.description}",
                VersionComparisonStatus.UP_TO_DATE,
                result,
            )
        }
    }

    @Test
    fun `compareVersion should return UPDATE_NEEDED when local version is older`() {
        // Given
        val scenarios = providesUpdateNeededScenarios()

        scenarios.forEach { scenario ->
            // When
            val result = scenario.localVersion.compareVersion(scenario.remoteVersion)

            // Then
            assertEquals(
                "Failed for: ${scenario.description}",
                VersionComparisonStatus.UPDATE_NEEDED,
                result,
            )
        }
    }

    @Test
    fun `compareVersion should return AHEAD_OF_SERVER when local version is newer`() {
        // Given
        val scenarios = providesAheadOfServerScenarios()

        scenarios.forEach { scenario ->
            // When
            val result = scenario.localVersion.compareVersion(scenario.remoteVersion)

            // Then
            assertEquals(
                "Failed for: ${scenario.description}",
                VersionComparisonStatus.AHEAD_OF_SERVER,
                result,
            )
        }
    }

    @Test
    fun `compareVersion should handle API format (semantic vs simple)`() {
        // Given
        val scenarios = providesApiFormatScenarios()

        scenarios.forEach { scenario ->
            // When
            val result = scenario.localVersion.compareVersion(scenario.remoteVersion)

            // Then
            assertEquals(
                "Failed for: ${scenario.description}",
                scenario.expectedStatus,
                result,
            )
        }
    }

    // Provider methods
    companion object {
        fun providesEqualVersionScenarios() = listOf(
            VersionScenario(
                description = "Simple equal versions (1.0.0 == 1.0.0)",
                localVersion = "1.0.0",
                remoteVersion = "1.0.0",
                expectedStatus = VersionComparisonStatus.UP_TO_DATE,
            ),
            VersionScenario(
                description = "Equal versions normalize to same value (2.5.3 == 2.5.3)",
                localVersion = "2.5.3",
                remoteVersion = "2.5.3",
                expectedStatus = VersionComparisonStatus.UP_TO_DATE,
            ),
            VersionScenario(
                description = "Equal single digit versions (5 == 5)",
                localVersion = "5",
                remoteVersion = "5",
                expectedStatus = VersionComparisonStatus.UP_TO_DATE,
            ),
            VersionScenario(
                description = "Semantic vs simple format equal (1.0.0 == 100)",
                localVersion = "1.0.0",
                remoteVersion = "100",
                expectedStatus = VersionComparisonStatus.UP_TO_DATE,
            ),
        )

        fun providesUpdateNeededScenarios() = listOf(
            VersionScenario(
                description = "Major version behind (1.0.0 < 2.0.0)",
                localVersion = "1.0.0",
                remoteVersion = "2.0.0",
                expectedStatus = VersionComparisonStatus.UPDATE_NEEDED,
            ),
            VersionScenario(
                description = "Minor version behind (1.2.0 < 1.3.0)",
                localVersion = "1.2.0",
                remoteVersion = "1.3.0",
                expectedStatus = VersionComparisonStatus.UPDATE_NEEDED,
            ),
            VersionScenario(
                description = "Patch version behind (1.2.3 < 1.2.4)",
                localVersion = "1.2.3",
                remoteVersion = "1.2.4",
                expectedStatus = VersionComparisonStatus.UPDATE_NEEDED,
            ),
            VersionScenario(
                description = "Single digit behind (1 < 2)",
                localVersion = "1",
                remoteVersion = "2",
                expectedStatus = VersionComparisonStatus.UPDATE_NEEDED,
            ),
            VersionScenario(
                description = "Semantic behind simple format (1.0.0 < 101)",
                localVersion = "1.0.0",
                remoteVersion = "101",
                expectedStatus = VersionComparisonStatus.UPDATE_NEEDED,
            ),
        )

        fun providesAheadOfServerScenarios() = listOf(
            VersionScenario(
                description = "Major version ahead (2.0.0 > 1.0.0)",
                localVersion = "2.0.0",
                remoteVersion = "1.0.0",
                expectedStatus = VersionComparisonStatus.AHEAD_OF_SERVER,
            ),
            VersionScenario(
                description = "Minor version ahead (1.3.0 > 1.2.0)",
                localVersion = "1.3.0",
                remoteVersion = "1.2.0",
                expectedStatus = VersionComparisonStatus.AHEAD_OF_SERVER,
            ),
            VersionScenario(
                description = "Patch version ahead (1.2.4 > 1.2.3)",
                localVersion = "1.2.4",
                remoteVersion = "1.2.3",
                expectedStatus = VersionComparisonStatus.AHEAD_OF_SERVER,
            ),
            VersionScenario(
                description = "Single digit ahead (2 > 1)",
                localVersion = "2",
                remoteVersion = "1",
                expectedStatus = VersionComparisonStatus.AHEAD_OF_SERVER,
            ),
            VersionScenario(
                description = "Semantic ahead simple format (2.0.0 > 100)",
                localVersion = "2.0.0",
                remoteVersion = "100",
                expectedStatus = VersionComparisonStatus.AHEAD_OF_SERVER,
            ),
        )

        fun providesApiFormatScenarios() = listOf(
            VersionScenario(
                description = "API format: semantic local vs simple remote (1.0.0 == 100)",
                localVersion = "1.0.0",
                remoteVersion = "100",
                expectedStatus = VersionComparisonStatus.UP_TO_DATE,
            ),
            VersionScenario(
                description = "API format: semantic local vs simple remote (1.0.1 > 100)",
                localVersion = "1.0.1",
                remoteVersion = "100",
                expectedStatus = VersionComparisonStatus.AHEAD_OF_SERVER,
            ),
            VersionScenario(
                description = "API format: semantic local vs simple remote (1.0.0 < 101)",
                localVersion = "1.0.0",
                remoteVersion = "101",
                expectedStatus = VersionComparisonStatus.UPDATE_NEEDED,
            ),
        )
    }

    data class VersionScenario(
        val description: String,
        val localVersion: String,
        val remoteVersion: String,
        val expectedStatus: VersionComparisonStatus,
    ) {
        override fun toString(): String = description
    }
}
