package com.example.f1_application.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "circuits")
data class CircuitEntity(
    @PrimaryKey val id: String,
    val name: String?,
    val trackSize: Int?,
    val place: String?
)