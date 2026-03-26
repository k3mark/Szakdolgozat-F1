package com.example.f1_application.data.model

data class CircuitStats(
    val circuitName: String,
    val trackLength: String?,
    val lapCount: Int?,
    val totalDistance: String?, // trackSize * laps
    val lastFivePoles: List<PolemanInfo>
)

data class PolemanInfo(
    val year: Int,
    val driverName: String,
    val poleTime: String?
)