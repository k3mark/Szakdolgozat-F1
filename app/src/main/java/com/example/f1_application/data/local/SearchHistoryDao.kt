package com.example.f1_application.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistoryEntity)

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 20")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearch(id: Int)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM search_history WHERE query = :query")
    suspend fun exists(query: String): Int
}