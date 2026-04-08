package com.example.f1_application.data.model

import com.google.gson.annotations.SerializedName

data class RaceResponse(
    @SerializedName("MRData") val mrData: RaceMRData
)
data class RaceMRData(
    @SerializedName("RaceTable") val raceTable: RaceTable
)
data class RaceTable(
    @SerializedName("Races") val races: List<Race>
)


data class Race(
    @SerializedName("season") val season: String,
    @SerializedName("raceName") val raceName: String,
    @SerializedName("Circuit") val circuit: Circuit,
    @SerializedName("date") val date: String,
    @SerializedName("time") val time: String?
)

data class Circuit(
    @SerializedName("circuitName") val circuitName: String,
    @SerializedName("Location") val location: Location
)

data class Location(
    @SerializedName("locality") val locality: String,
    @SerializedName("country") val country: String
)