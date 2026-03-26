package com.example.f1_application.data.model

import com.google.gson.annotations.SerializedName

data class Driver(
    @SerializedName("driver_number") val driverNumber: Int,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("team_name") val teamName: String,
    @SerializedName("team_colour") val teamColour: String?,
    @SerializedName("headshot_url") val headshotUrl: String?,
    @SerializedName("country_code") val countryCode: String?,
    val points: Double = 0.0
)


data class Constructor(
    val name: String,
    val color: String?,
    val points: Double
)