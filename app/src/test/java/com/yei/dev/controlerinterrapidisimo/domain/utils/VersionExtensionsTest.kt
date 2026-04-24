package com.yei.dev.controlerinterrapidisimo.domain.utils

import com.yei.dev.controlerinterrapidisimo.domain.models.VersionComparisonStatus
import junit.framework.TestCase.assertEquals
import org.junit.Test

/**
 * Unit tests for VersionExtensions.
 *
 * Tests verify that the compareVersion extension function correctly compares
 * version strings using semantic versioning rules.
 */
class VersionExtensionsTest {

    @Test
    fun `compareVersion should return UP_TO_DATE when versions are equal`() {
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
    fun `compareVersion should handle versions with different number of parts`() {
        // Given
        val scenarios = providesDifferentLengthScenarios()

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

    @Test
    fun `compareVersion should handle versions with non-numeric parts`() {
        // Given
        val scenarios = providesNonNumericScenarios()

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
                description = "Equal versions with multiple parts (2.5.3 == 2.5.3)",
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
                description = "Equal two-part versions (3.2 == 3.2)",
                localVersion = "3.2",
                remoteVersion = "3.2",
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
                description = "Multiple versions behind (1.0.0 < 3.5.2)",
                localVersion = "1.0.0",
                remoteVersion = "3.5.2",
                expectedStatus = VersionComparisonStatus.UPDATE_NEEDED,
            ),
            VersionScenario(
                description = "Single digit behind (1 < 2)",
                localVersion = "1",
                remoteVersion = "2",
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
                description = "Multiple versions ahead (3.5.2 > 1.0.0)",
                localVersion = "3.5.2",
                remoteVersion = "1.0.0",
                expectedStatus = VersionComparisonStatus.AHEAD_OF_SERVER,
            ),
            VersionScenario(
                description = "Single digit ahead (2 > 1)",
                localVersion = "2",
                remoteVersion = "1",
                expectedStatus = VersionComparisonStatus.AHEAD_OF_SERVER,
            ),
        )

        fun providesDifferentLengthScenarios() = listOf(
            VersionScenario(
                description = "Local has more parts, equal (1.0.0 == 1.0)",
                localVersion = "1.0.0",
                remoteVersion = "1.0",
                expectedStatus = VersionComparisonStatus.UP_TO_DATE,
            ),
            VersionScenario(
                description = "Remote has more parts, equal (1.0 == 1.0.0)",
                localVersion = "1.0",
                remoteVersion = "1.0.0",
                expectedStatus = VersionComparisonStatus.UP_TO_DATE,
            ),
            VersionScenario(
                description = "Local has more parts, ahead (1.0.1 > 1.0)",
                localVersion = "1.0.1",
                remoteVersion = "1.0",
                expectedStatus = VersionComparisonStatus.AHEAD_OF_SERVER,
            ),
            VersionScenario(
                description = "Remote has more parts, behind (1.0 < 1.0.1)",
                localVersion = "1.0",
                remoteVersion = "1.0.1",
                expectedStatus = VersionComparisonStatus.UPDATE_NEEDED,
            ),
            VersionScenario(
                description = "Single vs triple part (2 > 1.9.9)",
                localVersion = "2",
                remoteVersion = "1.9.9",
                expectedStatus = VersionComparisonStatus.AHEAD_OF_SERVER,
            ),
        )

        fun providesNonNumericScenarios() = listOf(
            VersionScenario(
                description = "Version with text suffix ignored (1.0.0-beta treated as 1.0.0)",
                localVersion = "1.0.0-beta",
                remoteVersion = "1.0.0",
                expectedStatus = VersionComparisonStatus.UP_TO_DATE,
            ),
            VersionScenario(
                description = "Both versions with text suffix (1.0.0-alpha == 1.0.0-beta)",
                localVersion = "1.0.0-alpha",
                remoteVersion = "1.0.0-beta",
                expectedStatus = VersionComparisonStatus.UP_TO_DATE,
            ),
            VersionScenario(
                description = "Numeric comparison with text suffix (2.0.0-rc < 3.0.0-alpha)",
                localVersion = "2.0.0-rc",
                remoteVersion = "3.0.0-alpha",
                expectedStatus = VersionComparisonStatus.UPDATE_NEEDED,
            ),
            VersionScenario(
                description = "Empty string parts ignored (1..0 treated as 1.0)",
                localVersion = "1..0",
                remoteVersion = "1.0",
                expectedStatus = VersionComparisonStatus.UP_TO_DATE,
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
