package com.example.f1_application.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.f1_application.data.model.CircuitStats
import com.example.f1_application.data.model.DriverStats
import com.example.f1_application.data.repository.F1Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun performSearch() {
        val query = _searchQuery.value.trim()
        if (query.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            // A repository-ból kérjük le az adatokat
            val driverMatch = repository.getDriverStats(query)
            val circuitMatch = repository.getCircuitStats(query)

            _driverResult.value = driverMatch
            _circuitResult.value = circuitMatch
            _isLoading.value = false
        }
    }
}

// --- ITT A FACTORY, AMIT KERES A RENDSZER ---
class SearchViewModelFactory(private val repository: F1Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}