package com.example.f1_application.data.model

data class DriverStats(
    val fullName: String,
    val driverId: String,
    val currentTeam: String,
    val wins: Int,
    val podiums: Int,
    val totalPoles: Int, // Kiszámolt érték a futamok alapján
    val totalPoints: Double,
    val bestPosition: Int,
    val activeYears: String
)