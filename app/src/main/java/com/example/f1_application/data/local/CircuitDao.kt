package com.example.f1_application.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CircuitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCircuits(circuits: List<CircuitEntity>)

    @Query("SELECT * FROM circuits")
    suspend fun getAllCircuits(): List<CircuitEntity>

    @Query("SELECT COUNT(*) FROM circuits")
    suspend fun getCircuitCount(): Int
}