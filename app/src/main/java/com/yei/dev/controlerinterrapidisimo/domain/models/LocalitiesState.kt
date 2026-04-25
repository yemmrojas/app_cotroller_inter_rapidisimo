package com.yei.dev.controlerinterrapidisimo.domain.models

/**
 * Represents the state of the localities screen.
 */
sealed class LocalitiesState {
    /** Localities screen is loading data */
    object Loading : LocalitiesState()

    /** Localities successfully loaded */
    data class Success(val localities: List<Locality>) : LocalitiesState()

    /** Error occurred while loading localities */
    data class Error(val message: String) : LocalitiesState()
}