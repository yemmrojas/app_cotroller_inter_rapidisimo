package com.yei.dev.controlerinterrapidisimo.domain.utils

/**
 * Cleans a credential string by removing newlines and trimming whitespace.
 *
 * This function handles both actual newline characters and escaped newline strings
 * that may come from API responses or user input.
 *
 * Examples:
 * - "user\n" -> "user"
 * - "pass\\n" -> "pass"
 * - "  data  " -> "data"
 *
 * @return The cleaned credential string
 */
fun String.cleanCredential(): String {
    return this.trim()
        .replace("\n", "")      // Remove actual newline characters
        .replace("\\n", "")     // Remove escaped newline strings
}
