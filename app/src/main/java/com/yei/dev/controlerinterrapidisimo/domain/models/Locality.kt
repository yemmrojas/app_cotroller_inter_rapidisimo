package com.yei.dev.controlerinterrapidisimo.domain.models

/**
 * Represents a locality (city/location) in the system.
 *
 * @param localityId The unique identifier for the locality
 * @param cityAbbreviation The abbreviated name of the city
 * @param fullName The complete name of the locality
 */
data class Locality(
    val localityId: String,
    val cityAbbreviation: String,
    val fullName: String
)
