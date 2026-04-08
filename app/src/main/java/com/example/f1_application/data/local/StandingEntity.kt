package com.example.f1_application.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "driver_standings")
data class StandingEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val seasonYear: Int,
    val position: Int?,
    val points: Double?,
    val driverId: String?,
    val driverName: String?,
    val teamName: String?
)