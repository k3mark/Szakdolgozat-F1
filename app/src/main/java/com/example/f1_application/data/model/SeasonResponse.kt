package com.example.f1_application.data.model
import com.google.gson.annotations.SerializedName

data class HypraceRace(
    val id: String?,
    @SerializedName("name") val raceName: String?,
    val officialName: String? = null,
    val circuitId: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val status: String? = null,
    val schedule: List<HypraceSchedule>? = emptyList(),
    val winner: HypraceResultDriver? = null,
    val podium: List<HypraceResultDriver>? = emptyList(),
    val poleman: HypraceResultDriver? = null,
    val poleTime: String? = null,
    val trackLength: String? = null,
    val lapCount: Int? = null,
    val totalDistance: String? = null,
    val circuit: HypraceCircuit? = null,
    val laps: Int? = null
)

data class HypraceCircuit(
    val id: String?,
    val name: String?,
    val place: String? = null,
    @SerializedName("trackSize") val trackSize: Int? = null
)

data class HypraceDriverStanding(val position: Int?, val points: Double?, val driverId: String?, val driverName: String?)
data class HypraceConstructorStanding(val position: Int?, val points: Double?, val teamId: String?, val teamName: String?)
data class HypraceTeam(val id: String?, val name: String?, val drivers: List<HypraceDriver> = emptyList())
data class HypraceDriver(val id: String?, val firstName: String?, val lastName: String?, val driverStatus: String?, val standing: HypraceStanding?)
data class HypraceStanding(val position: Int?, val points: Double?)
data class HypraceResultDriver(val driverId: String? = null, val driverName: String? = null)
data class HypraceSchedule(val type: String?, @SerializedName("startDate") val eventStart: String?)
data class SeasonResponse(@SerializedName("items") val items: List<SeasonItem>)
data class SeasonItem(val id: String, val year: Int, val name: String?)
data class SeasonTeamsResponse(@SerializedName("items") val items: List<HypraceTeam>?)
data class CircuitsResponse(@SerializedName("items") val items: List<HypraceCircuit>)
data class SeasonRacesResponse(@SerializedName("items") val races: List<HypraceRace>?)