package com.yei.dev.controlerinterrapidisimo.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Data transfer object for version API response.
 */
@Serializable
data class VersionResponseDto(
    val version: String
)
