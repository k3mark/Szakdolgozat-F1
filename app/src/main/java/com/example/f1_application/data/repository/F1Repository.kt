package com.example.f1_application.data.repository

import android.content.Context
import com.example.f1_application.data.local.*
import com.example.f1_application.data.model.*
import com.example.f1_application.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import java.util.Locale

data class TeamHistory(val year: Int, val points: Double, val position: Int)

class F1Repository(context: Context) {
    private val api = RetrofitClient.apiService
    private val db = AppDatabase.getDatabase(context)
    private val dao = db.raceDao()
    private val circuitDao = db.circuitDao()
    private val userDao = db.userDao()
    private val searchHistoryDao = db.searchHistoryDao()

    fun toTitleCase(text: String?): String {
        if (text.isNullOrBlank()) return "Ismeretlen"
        return text.split(" ", "_", "-").filter { it.isNotEmpty() }.joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
        }
    }

    // --- AUTH ---
    suspend fun login(username: String, password: String): UserEntity? =
        userDao.getUser(username)?.takeIf { it.password == password }

    suspend fun registerUser(user: UserEntity): Boolean =
        userDao.registerUser(user) != -1L

    suspend fun getUser(username: String): UserEntity? =
        userDao.getUser(username)

    // --- KEDVENCEK ---
    suspend fun toggleFavoriteDriver(username: String, driverId: String, driverName: String) {
        val user = userDao.getUser(username) ?: return
        val isRemoving = user.favoriteDriverId == driverId
        userDao.updateUser(user.copy(
            favoriteDriverId = if (isRemoving) null else driverId,
            favoriteDriverName = if (isRemoving) null else toTitleCase(driverName)
        ))
    }

    suspend fun toggleFavoriteTeam(username: String, teamId: String, teamName: String) {
        val user = userDao.getUser(username) ?: return
        val isRemoving = user.favoriteTeamId == teamId
        userDao.updateUser(user.copy(
            favoriteTeamId = if (isRemoving) null else teamId,
            favoriteTeamName = if (isRemoving) null else toTitleCase(teamName)
        ))
    }

    suspend fun toggleFavoriteTrack(username: String, trackId: String, gpName: String, circuitName: String) {
        val user = userDao.getUser(username) ?: return
        val isRem = user.favoriteTrackId == trackId
        val combinedName = if (gpName.contains(circuitName, ignoreCase = true)) gpName
        else "$gpName ($circuitName)"
        userDao.updateUser(user.copy(
            favoriteTrackId = if (isRem) null else trackId,
            favoriteTrackName = if (isRem) null else combinedName
        ))
    }

    // --- ADAT LEKÉRÉS ---
    private suspend fun getSeasonId(year: Int): String? {
        return try {
            val response = api.getSeasons(pageSize = 25)
            response.items.find { it.year == year || it.name?.contains(year.toString()) == true }?.id
        } catch (e: Exception) { null }
    }

    suspend fun getSeasonCalendar(year: Int): List<HypraceRace> {
        val count = dao.getRaceCount(year)
        if (count > 0) return dao.getRacesByYear(year).map { it.toHypraceRace() }

        val seasonId = getSeasonId(year) ?: return emptyList()
        return try {
            val allCircuits = getOrFetchCircuits()
            val standings = getDriverStandings(year)
            val driverMap = standings.associate { (it.driverId ?: "") to (it.driverName ?: "Ismeretlen") }

            val response = api.getGrandsPrix(seasonId = seasonId, pageSize = 25)
            val apiRaces = response.races ?: emptyList()

            val entities = apiRaces.map { race ->
                val circuitInfo = allCircuits.find { it.id == race.circuitId }
                race.toEntity(year, driverMap, circuitInfo)
            }
            if (entities.isNotEmpty()) dao.insertRaces(entities)
            dao.getRacesByYear(year).map { it.toHypraceRace() }
        } catch (e: Exception) { emptyList() }
    }

    private suspend fun getOrFetchCircuits(): List<CircuitEntity> {
        if (circuitDao.getCircuitCount() > 0) return circuitDao.getAllCircuits()
        val all = mutableListOf<HypraceCircuit>()
        var page = 1
        while (true) {
            val resp = api.getCircuits(25, page)
            if (resp.items.isEmpty()) break
            all.addAll(resp.items)
            if (resp.items.size < 25) break
            page++
        }
        circuitDao.insertCircuits(all.map { CircuitEntity(it.id ?: "", it.name, it.trackSize, it.place) })
        return circuitDao.getAllCircuits()
    }

    suspend fun getDriverStandings(year: Int): List<HypraceDriverStanding> {
        val count = dao.getStandingsCount(year)
        if (count > 0) return dao.getStandingsByYear(year).map {
            HypraceDriverStanding(it.position, it.points, it.driverId, it.driverName, it.teamName)
        }

        val sId = getSeasonId(year) ?: return emptyList()
        val resp = api.getSeasonTeams(sId, 25)
        val toSave = mutableListOf<StandingEntity>()
        resp.items?.forEach { team ->
            team.drivers.filter { it.driverStatus != "Substitute" }.forEach { d ->
                val fullName = toTitleCase("${d.firstName ?: ""} ${d.lastName ?: ""}")
                toSave.add(StandingEntity(
                    seasonYear = year,
                    position = d.standing?.position,
                    points = d.standing?.points,
                    driverId = d.id,
                    driverName = fullName,
                    teamName = toTitleCase(team.name)
                ))
            }
        }
        dao.insertStandings(toSave)
        return toSave.map {
            HypraceDriverStanding(it.position, it.points, it.driverId, it.driverName, it.teamName)
        }.sortedBy { it.position ?: 99 }
    }

    suspend fun getConstructorStandings(year: Int): List<HypraceConstructorStanding> {
        val count = dao.getConstructorStandingsCount(year)
        if (count > 0) return dao.getConstructorStandingsByYear(year).map {
            HypraceConstructorStanding(it.position, it.points, it.teamId, it.teamName)
        }

        val sId = getSeasonId(year) ?: return emptyList()
        val resp = api.getSeasonTeams(sId, 25)
        val ranked = resp.items?.map { team ->
            ConstructorStandingEntity(
                seasonYear = year,
                position = 0,
                points = team.drivers.sumOf { it.standing?.points ?: 0.0 },
                teamId = team.id,
                teamName = toTitleCase(team.name)
            )
        }?.sortedByDescending { it.points ?: 0.0 }
            ?.mapIndexed { i, e -> e.copy(position = i + 1) } ?: emptyList()

        if (ranked.isNotEmpty()) dao.insertConstructorStandings(ranked)
        return ranked.map { HypraceConstructorStanding(it.position, it.points, it.teamId, it.teamName) }
    }

    // --- KERESÉSI ELŐZMÉNYEK ---
    fun getSearchHistory(): Flow<List<SearchHistoryEntity>> =
        searchHistoryDao.getRecentSearches()

    suspend fun saveSearch(query: String, resultType: String) {
        if (searchHistoryDao.exists(query) == 0) {
            searchHistoryDao.insertSearch(SearchHistoryEntity(query = query, resultType = resultType))
        }
    }

    suspend fun deleteSearch(id: Int) = searchHistoryDao.deleteSearch(id)

    suspend fun clearSearchHistory() = searchHistoryDao.clearAll()

    // --- MAPPING ---
    private fun HypraceRace.toEntity(
        year: Int,
        driverMap: Map<String, String>,
        circuitInfo: CircuitEntity?
    ): RaceEntity {
        val m = circuitInfo?.trackSize ?: 0
        return RaceEntity(
            id = id ?: "",
            name = raceName,
            officialName = circuitInfo?.name ?: "Ismeretlen pálya",
            circuitId = circuitId ?: circuit?.id,
            startDate = startDate,
            endDate = endDate,
            status = status,
            mainRaceDate = schedule?.find { it.type == "MainRace" }?.eventStart,
            year = year,
            winnerName = winner?.driverName ?: driverMap[winner?.driverId],
            polemanName = poleman?.driverName ?: driverMap[poleman?.driverId],
            p1Name = driverMap[podium?.getOrNull(0)?.driverId],
            p2Name = driverMap[podium?.getOrNull(1)?.driverId],
            p3Name = driverMap[podium?.getOrNull(2)?.driverId],
            trackLength = if (m > 0) "%.3f km".format(Locale.US, m / 1000.0) else null,
            poleTime = poleTime
        )
    }

    private fun RaceEntity.toHypraceRace() = HypraceRace(
        id = id,
        raceName = name,
        officialName = officialName,
        circuitId = circuitId,
        startDate = startDate,
        endDate = endDate,
        status = status,
        schedule = listOf(HypraceSchedule(type = "MainRace", eventStart = mainRaceDate)),
        winner = winnerName?.let { HypraceResultDriver(driverName = it) },
        podium = listOfNotNull(
            p1Name?.let { HypraceResultDriver(driverName = it) },
            p2Name?.let { HypraceResultDriver(driverName = it) },
            p3Name?.let { HypraceResultDriver(driverName = it) }
        ),
        poleman = polemanName?.let { HypraceResultDriver(driverName = it) },
        poleTime = poleTime,
        trackLength = trackLength
    )

    suspend fun getDriverStats(q: String): DriverStats? {
        val driverEntry = dao.getAllStandings().find {
            it.driverName?.contains(q, ignoreCase = true) == true
        }
        return driverEntry?.driverId?.let { id -> getDriverStatsById(id) }
    }

    suspend fun updateUserInfo(oldUsername: String, newUsername: String, newPassword: String): Boolean {
        val user = userDao.getUser(oldUsername) ?: return false
        val updatedUser = user.copy(username = newUsername, password = newPassword)
        return if (oldUsername == newUsername) {
            userDao.updateUser(updatedUser)
            true
        } else {
            if (userDao.getUser(newUsername) != null) return false
            userDao.registerUser(updatedUser)
            userDao.deleteUser(user)
            true
        }
    }

    suspend fun deleteAccount(username: String) {
        val user = userDao.getUser(username)
        if (user != null) userDao.deleteUser(user)
    }

    suspend fun resetFavorites(username: String) {
        val user = userDao.getUser(username) ?: return
        userDao.updateUser(user.copy(
            favoriteDriverId = null, favoriteDriverName = null,
            favoriteTeamId = null, favoriteTeamName = null,
            favoriteTrackId = null, favoriteTrackName = null
        ))
    }

    suspend fun getCircuitStats(query: String): CircuitStats? {
        val q = query.trim()
        val allRaces = dao.getLastRacesForStats(q)
        if (allRaces.isEmpty()) return null
        val latest = allRaces.first()
        val combinedName = if (latest.name?.contains(latest.officialName ?: "", ignoreCase = true) == true)
            latest.name ?: "Ismeretlen"
        else "${latest.name} (${latest.officialName ?: "Ismeretlen pálya"})"
        return CircuitStats(
            circuitName = combinedName,
            trackLength = latest.trackLength,
            lastFivePoles = allRaces
                .filter { it.polemanName != null }
                .distinctBy { it.year }
                .take(5)
                .map { PolemanInfo(it.year, it.polemanName!!, it.poleTime) }
        )
    }

    suspend fun getDriverStatsById(driverId: String): DriverStats? {
        val standings = dao.getAllStandings().filter { it.driverId == driverId }
        if (standings.isEmpty()) return null
        val races = dao.getAllRaces()
        val latest = standings.maxByOrNull { it.seasonYear }
        return DriverStats(
            fullName = latest?.driverName ?: "Ismeretlen",
            driverId = driverId,
            currentTeam = toTitleCase(latest?.teamName),
            wins = races.count { it.winnerName == latest?.driverName },
            podiums = races.count { it.p1Name == latest?.driverName || it.p2Name == latest?.driverName || it.p3Name == latest?.driverName },
            totalPoles = races.count { it.polemanName == latest?.driverName },
            totalPoints = standings.sumOf { it.points ?: 0.0 },
            bestPosition = standings.minOf { it.position ?: 99 },
            activeYears = standings.map { it.seasonYear }.distinct().sortedDescending().joinToString(", ")
        )
    }

    suspend fun getTeamPoints(teamId: String, year: Int): Double {
        return dao.getConstructorStandingsByYear(year).find { it.teamId == teamId }?.points ?: 0.0
    }

    suspend fun getTeamHistory(teamId: String, currentYear: Int): List<TeamHistory> {
        val history = mutableListOf<TeamHistory>()
        for (year in currentYear downTo (currentYear - 2)) {
            val standings = getConstructorStandings(year)
            val team = standings.find { it.teamId == teamId }
            if (team != null) {
                history.add(TeamHistory(year, team.points ?: 0.0, team.position ?: 0))
            }
        }
        return history
    }

    suspend fun getCircuitStatsById(circuitId: String): CircuitStats? {
        val allRaces = dao.getAllRaces().filter { it.circuitId == circuitId }.sortedByDescending { it.year }
        if (allRaces.isEmpty()) return null
        val latest = allRaces.first()
        return CircuitStats(
            circuitName = if (latest.name?.contains(latest.officialName ?: "", true) == true) latest.name!!
            else "${latest.name} (${latest.officialName})",
            trackLength = latest.trackLength,
            lastFivePoles = allRaces.filter { it.polemanName != null }.take(5).map {
                PolemanInfo(it.year, it.polemanName!!, it.poleTime)
            }
        )
    }
}