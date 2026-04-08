package com.example.f1_application.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://hyprace-api.p.rapidapi.com/"
    private const val API_KEY = "21cc7f1fc0msh22c85e0f920241dp13f2c1jsn85fc537debf5"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("x-rapidapi-key", API_KEY)
                .addHeader("x-rapidapi-host", "hyprace-api.p.rapidapi.com")
                .build()
            chain.proceed(request)
        }
        .build()

    val apiService: F1ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(F1ApiService::class.java)
    }
}