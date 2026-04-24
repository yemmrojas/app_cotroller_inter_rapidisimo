package com.yei.dev.controlerinterrapidisimo.domain.models

/**
 * Represents a locality (city/location) in the system.
 *
 * @param cityAbbreviation The abbreviated name of the city
 * @param fullName The complete name of the locality
 */
data class Locality(
    val cityAbbreviation: String,
    val fullName: String
)
