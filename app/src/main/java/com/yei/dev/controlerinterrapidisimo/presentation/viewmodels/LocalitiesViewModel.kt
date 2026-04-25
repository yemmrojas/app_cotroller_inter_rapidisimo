package com.yei.dev.controlerinterrapidisimo.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.LocalitiesState
import com.yei.dev.controlerinterrapidisimo.domain.models.Locality
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.usecases.GetLocalitiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the localities screen.
 *
 * Responsible for loading and managing localities data.
 *
 * @param getLocalitiesUseCase Use case for retrieving localities
 */
@HiltViewModel
class LocalitiesViewModel @Inject constructor(
    private val getLocalitiesUseCase: GetLocalitiesUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<LocalitiesState>(LocalitiesState.Loading)
    val state: StateFlow<LocalitiesState> = _state.asStateFlow()

    /**
     * Loads localities data from the remote service.
     *
     * Requirements: 8.1, 8.4, 14.3
     */
    fun loadLocalities() {
        viewModelScope.launch {
            getLocalitiesUseCase().collect { localitiesResult ->
                when (localitiesResult) {
                    is Result.Success -> {
                        val localities = localitiesResult.data
                        if (localities.isNotEmpty()) {
                            _state.value = LocalitiesState.Success(localities)
                        } else {
                            _state.value = LocalitiesState.Error("No localities available")
                        }
                    }
                    is Result.Error -> {
                        _state.value = LocalitiesState.Error(
                            "Failed to load localities: ${localitiesResult.error}"
                        )
                    }
                }
            }
        }
    }
}
