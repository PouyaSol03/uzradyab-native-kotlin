package com.example.uzradyab.presentation.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.core.utils.DeviceSearch
import com.example.uzradyab.core.utils.ImmutableListWrapper
import com.example.uzradyab.core.utils.emptyImmutableList
import com.example.uzradyab.core.utils.toImmutable
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeviceSearchUiState(
    val query: String = "",
    val results: ImmutableListWrapper<Device> = emptyImmutableList(),
    val isSearching: Boolean = false,
)

/**
 * Server-side device search for lists that only hold the loaded pages of devices.
 * Results are debounced and only the latest query is kept.
 */
@HiltViewModel
class DeviceSearchViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
) : ViewModel() {

    private val queries = MutableStateFlow("")
    private val _uiState = MutableStateFlow(DeviceSearchUiState())
    val uiState: StateFlow<DeviceSearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            queries.collectLatest { query ->
                if (query.isEmpty()) {
                    _uiState.value = DeviceSearchUiState()
                    return@collectLatest
                }
                _uiState.update { it.copy(query = query, isSearching = true) }
                delay(SEARCH_DEBOUNCE_MS)
                val results = deviceRepository.searchDevices(query).getOrDefault(emptyList())
                _uiState.update { it.copy(results = results.toImmutable(), isSearching = false) }
            }
        }
    }

    fun onQueryChange(query: String) {
        queries.value = DeviceSearch.normalize(query)
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
    }
}
