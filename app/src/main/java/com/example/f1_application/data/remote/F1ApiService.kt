package com.example.f1_application.data.remote

import com.example.f1_application.data.model.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface F1ApiService {
    @GET("v2/seasons")
    suspend fun getSeasons(@Query("pageSize") pageSize: Int): SeasonResponse

    @GET("v2/seasons/{seasonId}/teams")
    suspend fun getSeasonTeams(
        @Path("seasonId") seasonId: String,
        @Query("pageSize") pageSize: Int
    ): SeasonTeamsResponse

    // JAVÍTVA: Az új végpont használata seasonId-vel
    @GET("v2/grands-prix")
    suspend fun getGrandsPrix(
        @Query("seasonId") seasonId: String,
        @Query("pageSize") pageSize: Int
    ): SeasonRacesResponse

    // JAVÍTVA: pageNumber hozzáadva a 79 pálya lekéréséhez
    @GET("v2/circuits")
    suspend fun getCircuits(
        @Query("pageSize") pageSize: Int,
        @Query("pageNumber") pageNumber: Int
    ): CircuitsResponse
}