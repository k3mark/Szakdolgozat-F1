package com.example.f1_application.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "constructor_standings")
data class ConstructorStandingEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val seasonYear: Int,
    val position: Int?,
    val points: Double?, // Double a félpontok miatt
    val teamId: String?,
    val teamName: String?
)