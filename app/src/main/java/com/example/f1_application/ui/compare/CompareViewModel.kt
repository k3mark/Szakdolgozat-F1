package com.example.f1_application.ui.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.f1_application.data.local.SearchHistoryEntity
import com.example.f1_application.data.model.DriverStats
import com.example.f1_application.data.repository.F1Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CompareViewModel(private val repository: F1Repository) : ViewModel() {

    private val _driverA = MutableStateFlow<DriverStats?>(null)
    val driverA: StateFlow<DriverStats?> = _driverA

    private val _driverB = MutableStateFlow<DriverStats?>(null)
    val driverB: StateFlow<DriverStats?> = _driverB

    private val _queryA = MutableStateFlow("")
    val queryA: StateFlow<String> = _queryA

    private val _queryB = MutableStateFlow("")
    val queryB: StateFlow<String> = _queryB

    private val _isLoadingA = MutableStateFlow(false)
    val isLoadingA: StateFlow<Boolean> = _isLoadingA

    private val _isLoadingB = MutableStateFlow(false)
    val isLoadingB: StateFlow<Boolean> = _isLoadingB

    private val _errorA = MutableStateFlow<String?>(null)
    val errorA: StateFlow<String?> = _errorA

    private val _errorB = MutableStateFlow<String?>(null)
    val errorB: StateFlow<String?> = _errorB

    val searchHistory: StateFlow<List<SearchHistoryEntity>> =
        repository.getSearchHistory()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryAChange(q: String) { _queryA.value = q }
    fun onQueryBChange(q: String) { _queryB.value = q }

    fun searchDriverA(query: String = _queryA.value) {
        val q = query.trim()
        if (q.isEmpty()) return
        _queryA.value = q
        viewModelScope.launch {
            _isLoadingA.value = true
            _errorA.value = null
            val result = repository.getDriverStats(q)
            _driverA.value = result
            if (result == null) _errorA.value = "Nem található: \"$q\""
            _isLoadingA.value = false
        }
    }

    fun searchDriverB(query: String = _queryB.value) {
        val q = query.trim()
        if (q.isEmpty()) return
        _queryB.value = q
        viewModelScope.launch {
            _isLoadingB.value = true
            _errorB.value = null
            val result = repository.getDriverStats(q)
            _driverB.value = result
            if (result == null) _errorB.value = "Nem található: \"$q\""
            _isLoadingB.value = false
        }
    }

    fun clearDriverA() {
        _driverA.value = null
        _queryA.value = ""
        _errorA.value = null
    }

    fun clearDriverB() {
        _driverB.value = null
        _queryB.value = ""
        _errorB.value = null
    }
}

class CompareViewModelFactory(private val repository: F1Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompareViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CompareViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}