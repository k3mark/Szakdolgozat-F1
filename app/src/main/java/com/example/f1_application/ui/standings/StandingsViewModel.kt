package com.example.f1_application.ui.standings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.f1_application.data.model.HypraceConstructorStanding
import com.example.f1_application.data.model.HypraceDriverStanding
import com.example.f1_application.data.repository.F1Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// 1. Nézet típusok meghatározása
enum class StandingViewType { DRIVER, CONSTRUCTOR }

// 2. A ViewModel osztály
class StandingsViewModel(private val repository: F1Repository) : ViewModel() {

    // Alapértelmezett év beállítása 2026-ra
    private val _selectedYear = MutableStateFlow(2026)
    val selectedYear: StateFlow<Int> = _selectedYear

    private val _viewType = MutableStateFlow(StandingViewType.DRIVER)
    val viewType: StateFlow<StandingViewType> = _viewType

    private val _driverStandings = MutableStateFlow<List<HypraceDriverStanding>>(emptyList())
    val driverStandings: StateFlow<List<HypraceDriverStanding>> = _driverStandings

    private val _constructorStandings = MutableStateFlow<List<HypraceConstructorStanding>>(emptyList())
    val constructorStandings: StateFlow<List<HypraceConstructorStanding>> = _constructorStandings

    init {
        loadData()
    }

    fun setYear(year: Int) {
        _selectedYear.value = year
        loadData()
    }

    fun setViewType(type: StandingViewType) {
        _viewType.value = type
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val year = _selectedYear.value
            // Adatok lekérése a repository-n keresztül
            _driverStandings.value = repository.getDriverStandings(year)
            _constructorStandings.value = repository.getConstructorStandings(year)
        }
    }
}

// 3. A Factory osztály, ami megoldja az Unresolved reference hibát
class StandingsViewModelFactory(private val repository: F1Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StandingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StandingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}