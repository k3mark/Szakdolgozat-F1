package com.example.f1_application.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "races")
data class RaceEntity(
    @PrimaryKey val id: String,
    val name: String?,
    val officialName: String?,
    val circuitId: String?, // Ez kapcsolja össze a csillagot a pályával
    val startDate: String?,
    val endDate: String?,
    val status: String?,
    val mainRaceDate: String?,
    val year: Int,
    val winnerName: String? = null,
    val polemanName: String? = null,
    val p1Name: String? = null,
    val p2Name: String? = null,
    val p3Name: String? = null,
    val trackLength: String? = null,
    val lapCount: Int? = null,
    val totalDistance: String? = null,
    val poleTime: String? = null
)