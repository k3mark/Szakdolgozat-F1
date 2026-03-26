package com.example.f1_application.data.model

import com.google.gson.annotations.SerializedName

data class Meeting(
    @SerializedName("meeting_key") val meetingKey: Int,
    @SerializedName("meeting_name") val meetingName: String,
    @SerializedName("location") val location: String,
    @SerializedName("country_name") val countryName: String,
    @SerializedName("date_start") val dateStart: String
)