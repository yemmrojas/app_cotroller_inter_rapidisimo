package com.yei.dev.controlerinterrapidisimo.domain.utils

import android.util.Log
import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests for RetryExtensions.
 *
 * Tests verify that the retryWithExponentialBackoff function:
 * - Returns success immediately without retrying
 * - Retries on network errors
 * - Retries on 5xx server errors
 * - Does not retry on validation errors
 * - Does not retry on 4xx client errors
 * - Implements exponential backoff with correct delays
 * - Respects max retries limit
 */
class RetryExtensionsTest {

    private fun setupLogMock() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
    }

    private fun teardownLogMock() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `retryWithExponentialBackoff should return success immediately without retrying`() =
        runTest {
            setupLogMock()
            try {
                // Given
                var callCount = 0
                val block: suspend () -> Result<String> = {
                    callCount++
                    Result.Success("success")
                }

                // When
                val result = retryWithExponentialBackoff(block = block)

                // Then
                assertEquals(Result.Success("success"), result)
                assertEquals(1, callCount)
            } finally {
                teardownLogMock()
            }
        }

    @Test
    fun `retryWithExponentialBackoff should retry on network error and eventually succeed`() =
        runTest {
            setupLogMock()
            try {
                // Given
                var callCount = 0
                val block: suspend () -> Result<String> = {
                    callCount++
                    if (callCount < 3) {
                        Result.Error(AppError.NetworkError("Connection timeout"))
                    } else {
                        Result.Success("success after retries")
                    }
                }

                // When
                val result = retryWithExponentialBackoff(
                    maxRetries = 2,
                    initialDelay = 10L,
                    block = block,
                )

                // Then
                assertEquals(Result.Success("success after retries"), result)
                assertEquals(3, callCount)
            } finally {
                teardownLogMock()
            }
        }

    @Test
    fun `retryWithExponentialBackoff should retry on 5xx server error and eventually succeed`() =
        runTest {
            setupLogMock()
            try {
                // Given
                var callCount = 0
                val block: suspend () -> Result<String> = {
                    callCount++
                    if (callCount < 2) {
                        Result.Error(AppError.ApiError(503, "Service Unavailable"))
                    } else {
                        Result.Success("success after server error")
                    }
                }

                // When
                val result = retryWithExponentialBackoff(
                    maxRetries = 3,
                    initialDelay = 10L,
                    block = block,
                )

                // Then
                assertEquals(Result.Success("success after server error"), result)
                assertEquals(2, callCount)
            } finally {
                teardownLogMock()
            }
        }

    @Test
    fun `retryWithExponentialBackoff should not retry on validation error`() =
        runTest {
            setupLogMock()
            try {
                // Given
                var callCount = 0
                val block: suspend () -> Result<String> = {
                    callCount++
                    Result.Error(AppError.ValidationError("username", "Username is required"))
                }

                // When
                val result = retryWithExponentialBackoff(
                    maxRetries = 3,
                    initialDelay = 10L,
                    block = block,
                )

                // Then
                assertTrue(result is Result.Error)
                assertEquals(1, callCount)
            } finally {
                teardownLogMock()
            }
        }

    @Test
    fun `retryWithExponentialBackoff should not retry on 4xx client error`() =
        runTest {
            setupLogMock()
            try {
                // Given
                var callCount = 0
                val block: suspend () -> Result<String> = {
                    callCount++
                    Result.Error(AppError.ApiError(400, "Bad Request"))
                }

                // When
                val result = retryWithExponentialBackoff(
                    maxRetries = 3,
                    initialDelay = 10L,
                    block = block,
                )

                // Then
                assertTrue(result is Result.Error)
                assertEquals(1, callCount)
            } finally {
                teardownLogMock()
            }
        }

    @Test
    fun `retryWithExponentialBackoff should not retry on database error`() =
        runTest {
            setupLogMock()
            try {
                // Given
                var callCount = 0
                val block: suspend () -> Result<String> = {
                    callCount++
                    Result.Error(AppError.DatabaseError("Failed to insert user"))
                }

                // When
                val result = retryWithExponentialBackoff(
                    maxRetries = 3,
                    initialDelay = 10L,
                    block = block,
                )

                // Then
                assertTrue(result is Result.Error)
                assertEquals(1, callCount)
            } finally {
                teardownLogMock()
            }
        }

    @Test
    fun `retryWithExponentialBackoff should exhaust retries and return final error`() =
        runTest {
            setupLogMock()
            try {
                // Given
                var callCount = 0
                val block: suspend () -> Result<String> = {
                    callCount++
                    Result.Error(AppError.NetworkError("Connection timeout"))
                }

                // When
                val result = retryWithExponentialBackoff(
                    maxRetries = 3,
                    initialDelay = 10L,
                    block = block,
                )

                // Then
                assertTrue(result is Result.Error)
                assertEquals(4, callCount) // maxRetries + 1 = 3 + 1
            } finally {
                teardownLogMock()
            }
        }

    @Test
    fun `retryWithExponentialBackoff should respect max retries configuration`() =
        runTest {
            setupLogMock()
            try {
                // Given
                val scenarios = providesMaxRetriesScenarios()

                scenarios.forEach { scenario ->
                    var callCount = 0
                    val block: suspend () -> Result<String> = {
                        callCount++
                        Result.Error(AppError.NetworkError("Connection timeout"))
                    }

                    // When
                    val result = retryWithExponentialBackoff(
                        maxRetries = scenario.maxRetries,
                        initialDelay = 10L,
                        block = block,
                    )

                    // Then
                    assertTrue("Failed for: ${scenario.description}", result is Result.Error)
                    assertEquals(
                        "Failed for: ${scenario.description}",
                        scenario.expectedCallCount,
                        callCount,
                    )
                }
            } finally {
                teardownLogMock()
            }
        }

    @Test
    fun `retryWithExponentialBackoff should implement exponential backoff delays`() =
        runTest {
            setupLogMock()
            try {
                // Given
                val scenarios = providesExponentialBackoffScenarios()

                scenarios.forEach { scenario ->
                    var callCount = 0
                    val timestamps = mutableListOf<Long>()
                    val block: suspend () -> Result<String> = {
                        callCount++
                        timestamps.add(System.currentTimeMillis())
                        if (callCount <= scenario.failureCount) {
                            Result.Error(AppError.NetworkError("Connection timeout"))
                        } else {
                            Result.Success("success")
                        }
                    }

                    // When
                    val result = retryWithExponentialBackoff(
                        maxRetries = scenario.maxRetries,
                        initialDelay = scenario.initialDelay,
                        maxDelay = scenario.maxDelay,
                        factor = scenario.factor,
                        block = block,
                    )

                    // Then
                    assertEquals("Failed for: ${scenario.description}", Result.Success("success"), result)

                    // Verify delays increase exponentially
                    if (timestamps.size > 1) {
                        for (i in 1 until timestamps.size - 1) {
                            val delay1 = timestamps[i] - timestamps[i - 1]
                            val delay2 = timestamps[i + 1] - timestamps[i]
                            // Second delay should be greater than first (exponential backoff)
                            assertTrue(
                                "Failed for: ${scenario.description} - delays not exponential",
                                delay2 >= delay1,
                            )
                        }
                    }
                }
            } finally {
                teardownLogMock()
            }
        }

    @Test
    fun `retryWithExponentialBackoff should cap delay at maxDelay`() =
        runTest {
            setupLogMock()
            try {
                // Given
                var callCount = 0
                val maxDelay = 100L
                val block: suspend () -> Result<String> = {
                    callCount++
                    if (callCount <= 5) {
                        Result.Error(AppError.NetworkError("Connection timeout"))
                    } else {
                        Result.Success("success")
                    }
                }

                // When
                val startTime = System.currentTimeMillis()
                val result = retryWithExponentialBackoff(
                    maxRetries = 5,
                    initialDelay = 50L,
                    maxDelay = maxDelay,
                    factor = 2.0,
                    block = block,
                )
                val totalTime = System.currentTimeMillis() - startTime

                // Then
                assertEquals(Result.Success("success"), result)
                // With maxDelay capping, total time should be reasonable
                // 5 retries with max 100ms delay each = ~500ms max
                assertTrue("Total time exceeded expected bounds", totalTime < 1000L)
            } finally {
                teardownLogMock()
            }
        }

    @Test
    fun `retryWithExponentialBackoff should handle mixed error types correctly`() =
        runTest {
            setupLogMock()
            try {
                // Given
                val scenarios = providesErrorTypeScenarios()

                scenarios.forEach { scenario ->
                    var callCount = 0
                    val block: suspend () -> Result<String> = {
                        callCount++
                        scenario.errors[callCount - 1]
                    }

                    // When
                    val result = retryWithExponentialBackoff(
                        maxRetries = 5,
                        initialDelay = 10L,
                        block = block,
                    )

                    // Then
                    assertEquals(
                        "Failed for: ${scenario.description}",
                        scenario.expectedCallCount,
                        callCount,
                    )
                }
            } finally {
                teardownLogMock()
            }
        }

    // Provider methods
    companion object {
        fun providesMaxRetriesScenarios() = listOf(
            MaxRetriesScenario(
                description = "maxRetries = 1",
                maxRetries = 1,
                expectedCallCount = 2, // maxRetries + 1
            ),
            MaxRetriesScenario(
                description = "maxRetries = 3",
                maxRetries = 3,
                expectedCallCount = 4, // maxRetries + 1
            ),
            MaxRetriesScenario(
                description = "maxRetries = 5",
                maxRetries = 5,
                expectedCallCount = 6, // maxRetries + 1
            ),
        )

        fun providesExponentialBackoffScenarios() = listOf(
            ExponentialBackoffScenario(
                description = "Standard exponential backoff (factor=2.0)",
                maxRetries = 3,
                initialDelay = 50L,
                maxDelay = 1000L,
                factor = 2.0,
                failureCount = 2,
            ),
            ExponentialBackoffScenario(
                description = "Slower backoff (factor=1.5)",
                maxRetries = 3,
                initialDelay = 50L,
                maxDelay = 1000L,
                factor = 1.5,
                failureCount = 2,
            ),
            ExponentialBackoffScenario(
                description = "Single retry with backoff",
                maxRetries = 2,
                initialDelay = 50L,
                maxDelay = 1000L,
                factor = 2.0,
                failureCount = 1,
            ),
        )

        fun providesErrorTypeScenarios() = listOf(
            ErrorTypeScenario(
                description = "Network error then success",
                errors = listOf(
                    Result.Error(AppError.NetworkError("Timeout")),
                    Result.Success("success"),
                ),
                expectedCallCount = 2,
            ),
            ErrorTypeScenario(
                description = "5xx error then success",
                errors = listOf(
                    Result.Error(AppError.ApiError(500, "Internal Server Error")),
                    Result.Success("success"),
                ),
                expectedCallCount = 2,
            ),
            ErrorTypeScenario(
                description = "Validation error stops immediately",
                errors = listOf(
                    Result.Error(AppError.ValidationError("field", "Invalid")),
                    Result.Success("success"),
                ),
                expectedCallCount = 1,
            ),
            ErrorTypeScenario(
                description = "4xx error stops immediately",
                errors = listOf(
                    Result.Error(AppError.ApiError(404, "Not Found")),
                    Result.Success("success"),
                ),
                expectedCallCount = 1,
            ),
            ErrorTypeScenario(
                description = "Network error then 4xx error",
                errors = listOf(
                    Result.Error(AppError.NetworkError("Timeout")),
                    Result.Error(AppError.ApiError(400, "Bad Request")),
                ),
                expectedCallCount = 2,
            ),
        )
    }

    data class MaxRetriesScenario(
        val description: String,
        val maxRetries: Int,
        val expectedCallCount: Int,
    ) {
        override fun toString(): String = description
    }

    data class ExponentialBackoffScenario(
        val description: String,
        val maxRetries: Int,
        val initialDelay: Long,
        val maxDelay: Long,
        val factor: Double,
        val failureCount: Int,
    ) {
        override fun toString(): String = description
    }

    data class ErrorTypeScenario(
        val description: String,
        val errors: List<Result<String>>,
        val expectedCallCount: Int,
    ) {
        override fun toString(): String = description
    }
}
