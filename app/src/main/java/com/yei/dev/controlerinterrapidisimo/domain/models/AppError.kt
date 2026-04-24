package com.yei.dev.controlerinterrapidisimo.domain.models

/**
 * Sealed class hierarchy representing all possible errors in the application.
 */
sealed class AppError {
    /**
     * Represents network-related errors such as connectivity issues or timeouts.
     *
     * @param message Human-readable error message
     * @param cause The underlying exception that caused this error, if any
     */
    data class NetworkError(
        val message: String,
        val cause: Throwable? = null
    ) : AppError()

    /**
     * Represents API-related errors with HTTP status codes.
     *
     * @param statusCode The HTTP status code returned by the API
     * @param message Human-readable error message
     */
    data class ApiError(
        val statusCode: Int,
        val message: String
    ) : AppError()

    /**
     * Represents database-related errors such as query failures or constraint violations.
     *
     * @param message Human-readable error message
     * @param cause The underlying exception that caused this error, if any
     */
    data class DatabaseError(
        val message: String,
        val cause: Throwable? = null
    ) : AppError()

    /**
     * Represents validation errors for user input or data constraints.
     *
     * @param field The field that failed validation
     * @param message Human-readable error message describing the validation failure
     */
    data class ValidationError(
        val field: String,
        val message: String
    ) : AppError()

    /**
     * Represents unexpected errors that don't fit into other categories.
     *
     * @param message Human-readable error message
     * @param cause The underlying exception that caused this error, if any
     */
    data class UnknownError(
        val message: String,
        val cause: Throwable? = null
    ) : AppError()
}
