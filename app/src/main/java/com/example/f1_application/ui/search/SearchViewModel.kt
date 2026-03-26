package com.example.f1_application.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.f1_application.data.local.SearchHistoryEntity
import com.example.f1_application.data.model.CircuitStats
import com.example.f1_application.data.model.DriverStats
import com.example.f1_application.data.repository.F1Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: F1Repository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _driverResult = MutableStateFlow<DriverStats?>(null)
    val driverResult: StateFlow<DriverStats?> = _driverResult

    private val _circuitResult = MutableStateFlow<CircuitStats?>(null)
    val circuitResult: StateFlow<CircuitStats?> = _circuitResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Előzmények Flow-ból StateFlow-vá alakítva
    val searchHistory: StateFlow<List<SearchHistoryEntity>> =
        repository.getSearchHistory()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun performSearch(query: String = _searchQuery.value) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        // Ha előzményből jött, töltse be a query mezőbe is
        _searchQuery.value = trimmed

        viewModelScope.launch {
            _isLoading.value = true

            val driverMatch = repository.getDriverStats(trimmed)
            val circuitMatch = repository.getCircuitStats(trimmed)

            _driverResult.value = driverMatch
            _circuitResult.value = circuitMatch

            // Mentés az előzményekbe
            val resultType = when {
                driverMatch != null -> "DRIVER"
                circuitMatch != null -> "CIRCUIT"
                else -> "NONE"
            }
            repository.saveSearch(trimmed, resultType)

            _isLoading.value = false
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch { repository.deleteSearch(id) }
    }

    fun clearHistory() {
        viewModelScope.launch { repository.clearSearchHistory() }
    }
}

class SearchViewModelFactory(private val repository: F1Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}