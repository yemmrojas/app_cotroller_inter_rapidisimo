package com.yei.dev.controlerinterrapidisimo.domain.models

/**
 * Represents the result of an operation that can either succeed or fail.
 *
 * @param T The type of data returned on success
 */
sealed class Result<out T> {
    /**
     * Represents a successful operation with data.
     *
     * @param data The data returned by the operation
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Represents a failed operation with an error.
     *
     * @param error The error that occurred during the operation
     */
    data class Error(val error: AppError) : Result<Nothing>()
}
