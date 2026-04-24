package com.yei.dev.controlerinterrapidisimo.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Handles network operations and provides safe API call execution with error mapping.
 *
 * This class wraps API calls with comprehensive error handling, network connectivity checks,
 * and logging. It converts various exception types and HTTP error responses into domain-level
 * AppError instances.
 *
 * @param context Android context for accessing system services
 */
class NetworkHandler(
    private val context: Context
) {
    companion object {
        private const val TAG = "NetworkHandler"
    }

    /**
     * Checks if network connectivity is available.
     *
     * @return true if network is available, false otherwise
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Executes an API call with comprehensive error handling.
     *
     * This method wraps the API call with:
     * - Network connectivity check
     * - HTTP response validation
     * - Exception handling and mapping to domain errors
     * - Logging for all errors
     *
     * @param T The type of data expected in the response
     * @param apiCall Suspend function that executes the API call
     * @return Result.Success with data if successful, Result.Error with AppError otherwise
     */
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>
    ): Result<T> {
        return try {
            // Check network connectivity first
            if (!isNetworkAvailable()) {
                val error = AppError.NetworkError("No internet connection")
                logError("Network connectivity check failed", error)
                return Result.Error(error)
            }

            // Execute the API call
            val response = apiCall()

            // Handle response
            when {
                response.isSuccessful && response.body() != null -> {
                    Log.d(TAG, "API call successful: ${response.code()}")
                    Result.Success(response.body()!!)
                }
                response.isSuccessful && response.body() == null -> {
                    val error = AppError.ApiError(
                        statusCode = response.code(),
                        message = "Empty response body"
                    )
                    logError("API returned empty body", error)
                    Result.Error(error)
                }
                else -> {
                    val errorMessage = response.errorBody()?.string() ?: response.message()
                    val error = AppError.ApiError(
                        statusCode = response.code(),
                        message = errorMessage
                    )
                    logError("API returned error response", error)
                    Result.Error(error)
                }
            }
        } catch (e: SocketTimeoutException) {
            val error = AppError.NetworkError("Connection timed out", e)
            logError("API call timed out", error)
            Result.Error(error)
        } catch (e: IOException) {
            val error = AppError.NetworkError("Network error: ${e.message}", e)
            logError("Network I/O error", error)
            Result.Error(error)
        } catch (e: Exception) {
            val error = AppError.UnknownError("Unexpected error: ${e.message}", e)
            logError("Unexpected error during API call", error)
            Result.Error(error)
        }
    }

    /**
     * Logs error details with appropriate log level.
     *
     * @param message Context message describing where the error occurred
     * @param error The AppError to log
     */
    private fun logError(message: String, error: AppError) {
        when (error) {
            is AppError.NetworkError -> {
                Log.e(TAG, "$message: ${error.message}", error.cause)
            }
            is AppError.ApiError -> {
                Log.e(TAG, "$message: [${error.statusCode}] ${error.message}")
            }
            is AppError.DatabaseError -> {
                Log.e(TAG, "$message: ${error.message}", error.cause)
            }
            is AppError.ValidationError -> {
                Log.w(TAG, "$message: ${error.field} - ${error.message}")
            }
            is AppError.UnknownError -> {
                Log.e(TAG, "$message: ${error.message}", error.cause)
            }
        }
    }
}
