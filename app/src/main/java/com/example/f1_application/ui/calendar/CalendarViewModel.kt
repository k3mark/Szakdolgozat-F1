package com.example.f1_application.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.f1_application.data.model.HypraceRace
import com.example.f1_application.data.repository.F1Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CalendarViewModel(private val repository: F1Repository) : ViewModel() {
    private val _races = MutableStateFlow<List<HypraceRace>>(emptyList())
    val races: StateFlow<List<HypraceRace>> = _races
    private val _selectedYear = MutableStateFlow(2026)
    val selectedYear: StateFlow<Int> = _selectedYear

    init { loadRaces() }

    fun updateYear(year: Int) {
        _selectedYear.value = year
        loadRaces()
    }

    private fun loadRaces() {
        viewModelScope.launch {
            _races.value = repository.getSeasonCalendar(_selectedYear.value)
        }
    }
}


class CalendarViewModelFactory(private val repository: F1Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}