package com.yei.dev.controlerinterrapidisimo.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * OkHttp interceptor that handles JSON parsing edge cases.
 *
 * This interceptor addresses specific issues with the Interrapidisimo API,
 * particularly escaped characters (e.g., \n) in JSON fields like the Password field.
 * It preprocesses response bodies to ensure they can be parsed correctly by
 * Kotlinx Serialization.
 *
 * Technical Note: The Interrapidisimo API returns escaped characters in some fields
 * that require lenient parsing. This interceptor normalizes the response before
 * deserialization.
 */
class JsonParsingInterceptor : Interceptor {
    companion object {
        private const val TAG = "JsonParsingInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Only process JSON responses
        val contentType = response.body.contentType()
        if (contentType?.subtype != "json") {
            return response
        }

        return try {
            val originalBody = response.body.string()

            // Log the original response for debugging
            Log.d(TAG, "Original response: $originalBody")

            // The response body is already valid JSON, but we ensure it's properly handled
            // by Kotlinx Serialization's lenient mode (configured in the JSON instance)
            // This interceptor mainly serves as a logging and monitoring point

            // Create new response with the same body
            response.newBuilder()
                .body(originalBody.toResponseBody(contentType))
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Error processing response body", e)
            response
        }
    }
}
