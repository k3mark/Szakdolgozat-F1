package com.example.f1_application.data.local
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val password: String,
    val favoriteDriverId: String? = null,
    val favoriteDriverName: String? = null,
    val favoriteTeamId: String? = null,
    val favoriteTeamName: String? = null,
    val favoriteTrackId: String? = null,
    val favoriteTrackName: String? = null
)