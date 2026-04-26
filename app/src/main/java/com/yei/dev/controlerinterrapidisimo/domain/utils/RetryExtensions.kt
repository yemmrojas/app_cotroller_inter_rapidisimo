package com.yei.dev.controlerinterrapidisimo.domain.utils

import android.util.Log
import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import kotlinx.coroutines.delay

private const val TAG = "RetryExtensions"

/**
 * Executes a suspend function with exponential backoff retry logic.
 *
 * This function automatically retries operations that fail with retryable errors
 * (network errors and 5xx server errors). Non-retryable errors (validation errors,
 * 4xx client errors) are returned immediately without retry.
 *
 * The retry delay follows an exponential backoff pattern:
 * delay = initialDelay * (factor ^ attemptNumber), capped at maxDelay
 *
 * @param T The type of data returned on success
 * @param maxRetries Maximum number of retry attempts (default: 3)
 * @param initialDelay Initial delay in milliseconds before first retry (default: 1000ms)
 * @param maxDelay Maximum delay in milliseconds between retries (default: 10000ms)
 * @param factor Multiplier for exponential backoff (default: 2.0)
 * @param block The suspend function to execute, returning a Result<T>
 * @return The result of the operation after all retries or immediate success/failure
 *
 * Example:
 * ```kotlin
 * val result = retryWithExponentialBackoff(maxRetries = 3) {
 *     apiService.fetchData()
 * }
 * ```
 */
suspend fun <T> retryWithExponentialBackoff(
    maxRetries: Int = 3,
    initialDelay: Long = 1000L,
    maxDelay: Long = 10000L,
    factor: Double = 2.0,
    block: suspend () -> Result<T>,
): Result<T> {
    var currentDelay = initialDelay

    repeat(maxRetries + 1) { attempt ->
        val result = block()

        // Return immediately on success
        if (result is Result.Success) {
            return result
        }

        // Check if error is retryable
        if (!isRetryable(result)) {
            return result
        }

        // If this is not the last attempt, wait before retrying
        if (attempt < maxRetries) {
            Log.d(TAG, "Retry attempt ${attempt + 1}/${maxRetries + 1} failed, waiting ${currentDelay}ms before retry")
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
    }

    // Should not reach here, but return error as fallback
    return Result.Error(AppError.UnknownError("All retry attempts exhausted"))
}

/**
 * Determines whether an error is retryable.
 *
 * Retryable errors include:
 * - Network errors (timeouts, connection failures)
 * - Server errors (5xx HTTP status codes)
 *
 * Non-retryable errors include:
 * - Validation errors
 * - Client errors (4xx HTTP status codes)
 * - Database errors
 * - Unknown errors
 *
 * @param T The type of data in the Result
 * @param result The Result to check
 * @return true if the error is retryable, false otherwise
 */
private fun <T> isRetryable(result: Result<T>): Boolean {
    return when (result) {
        is Result.Error -> when (result.error) {
            // Network errors are always retryable
            is AppError.NetworkError -> {
                Log.d(TAG, "Network error is retryable: ${result.error.message}")
                true
            }
            // Server errors (5xx) are retryable
            is AppError.ApiError -> {
                val isServerError = result.error.statusCode in 500..599
                if (isServerError) {
                    Log.d(TAG, "Server error ${result.error.statusCode} is retryable")
                }
                isServerError
            }
            // Validation, database, and unknown errors are not retryable
            is AppError.ValidationError -> {
                Log.d(TAG, "Validation error is not retryable")
                false
            }

            is AppError.DatabaseError -> {
                Log.d(TAG, "Database error is not retryable")
                false
            }

            is AppError.UnknownError -> {
                Log.d(TAG, "Unknown error is not retryable")
                false
            }
        }
        // Success is not an error, so not retryable
        else -> false
    }
}
