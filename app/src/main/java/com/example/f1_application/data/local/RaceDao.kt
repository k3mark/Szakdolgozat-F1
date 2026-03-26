package com.example.f1_application.data.local

import androidx.room.*

@Dao
interface RaceDao {
    @Query("SELECT * FROM races WHERE year = :year ORDER BY startDate ASC")
    suspend fun getRacesByYear(year: Int): List<RaceEntity>

    @Query("SELECT * FROM races")
    suspend fun getAllRaces(): List<RaceEntity>

    @Query("SELECT * FROM driver_standings")
    suspend fun getAllStandings(): List<StandingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRaces(races: List<RaceEntity>)

    @Query("SELECT COUNT(*) FROM races WHERE year = :year")
    suspend fun getRaceCount(year: Int): Int

    @Query("SELECT * FROM driver_standings WHERE seasonYear = :year ORDER BY position ASC")
    suspend fun getStandingsByYear(year: Int): List<StandingEntity>

    @Query("SELECT COUNT(*) FROM driver_standings WHERE seasonYear = :year")
    suspend fun getStandingsCount(year: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStandings(standings: List<StandingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConstructorStandings(standings: List<ConstructorStandingEntity>)

    @Query("SELECT * FROM constructor_standings WHERE seasonYear = :year ORDER BY position ASC")
    suspend fun getConstructorStandingsByYear(year: Int): List<ConstructorStandingEntity>

    @Query("SELECT COUNT(*) FROM constructor_standings WHERE seasonYear = :year")
    suspend fun getConstructorStandingsCount(year: Int): Int

    @Query("SELECT * FROM races WHERE name LIKE '%' || :circuitName || '%' ORDER BY year DESC LIMIT 5")
    suspend fun getLastFiveRacesAtCircuit(circuitName: String): List<RaceEntity>

    @Query("SELECT * FROM driver_standings WHERE driverName LIKE '%' || :query || '%' LIMIT 1")
    suspend fun searchDriver(query: String): StandingEntity?

    @Query("SELECT * FROM circuits WHERE name LIKE '%' || :query || '%' LIMIT 1")
    suspend fun searchCircuit(query: String): CircuitEntity?

    @Query("SELECT * FROM races WHERE winnerName = :driverName OR polemanName = :driverName")
    suspend fun getRacesForDriver(driverName: String): List<RaceEntity>

    @Query("SELECT * FROM races WHERE name LIKE '%' || :query || '%' OR officialName LIKE '%' || :query || '%' ORDER BY year DESC LIMIT 20")
    suspend fun getLastRacesForStats(query: String): List<RaceEntity>
}
