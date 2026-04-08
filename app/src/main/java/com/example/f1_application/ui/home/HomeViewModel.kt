package com.example.f1_application.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.example.f1_application.data.model.*
import com.example.f1_application.data.repository.F1Repository
import com.example.f1_application.data.repository.TeamHistory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = F1Repository(application)

    val favoriteDriver = MutableStateFlow<DriverStats?>(null)
    val favoriteTeamName = MutableStateFlow<String?>(null)
    val favoriteTeamHistory = MutableStateFlow<List<TeamHistory>>(emptyList())
    val favoriteCircuit = MutableStateFlow<CircuitStats?>(null)
    val favoriteTrackCountdown = MutableStateFlow("")

    private var globalCountdownJob: Job? = null
    private var favoriteCountdownJob: Job? = null


    private val _nextRace = MutableStateFlow<HypraceRace?>(null)
    val nextRace: StateFlow<HypraceRace?> = _nextRace
    private val _countdown = MutableStateFlow("Betöltés...")
    val countdown: StateFlow<String> = _countdown

    init { loadNextRace() }

    private fun loadNextRace() {
        viewModelScope.launch {
            val races = repository.getSeasonCalendar(2026)
            val next = races.find { race ->
                val date = race.schedule?.find { it.type == "MainRace" }?.eventStart ?: race.startDate
                date?.let { ZonedDateTime.parse(it).toLocalDateTime().isAfter(LocalDateTime.now()) } ?: false
            } ?: races.lastOrNull()
            _nextRace.value = next
            next?.let {
                val dateStr = it.schedule?.find { it.type == "MainRace" }?.eventStart ?: it.startDate!!
                globalCountdownJob?.cancel()
                globalCountdownJob = startCountdown(dateStr, _countdown)
            }
        }
    }

    fun loadFavoritesData(username: String) {
        viewModelScope.launch {
            val user = repository.getUser(username) ?: return@launch


            favoriteDriver.value = user.favoriteDriverId?.let { repository.getDriverStatsById(it) }
            favoriteTeamName.value = user.favoriteTeamName


            favoriteTeamHistory.value = user.favoriteTeamId?.let { repository.getTeamHistory(it, 2026) } ?: emptyList()


            val circuitStats = user.favoriteTrackId?.let { repository.getCircuitStatsById(it) }
            favoriteCircuit.value = circuitStats

            if (circuitStats == null) {
                favoriteCountdownJob?.cancel()
                favoriteTrackCountdown.value = ""
            } else {
                val races = repository.getSeasonCalendar(2026)
                val favRace = races.find { it.circuitId == user.favoriteTrackId }
                favRace?.startDate?.let {
                    favoriteCountdownJob?.cancel()
                    favoriteCountdownJob = startCountdown(it, favoriteTrackCountdown)
                }
            }
        }
    }

    private fun startCountdown(date: String, flow: MutableStateFlow<String>): Job {
        return viewModelScope.launch {
            try {
                val target = ZonedDateTime.parse(date).toLocalDateTime()
                while (isActive) {
                    val diff = Duration.between(LocalDateTime.now(), target)
                    if (diff.isNegative) {
                        flow.value = "A futam elkezdődött!"
                        break
                    }
                    flow.value = String.format("%d nap %02d:%02d:%02d", diff.toDays(), diff.toHours() % 24, diff.toMinutes() % 60, diff.seconds % 60)
                    delay(1000)
                }
            } catch (e: Exception) {
                flow.value = "Nincs időpont"
            }
        }
    }
}