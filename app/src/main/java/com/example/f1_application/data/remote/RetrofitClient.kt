package com.example.f1_application.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://hyprace-api.p.rapidapi.com/"

    // A képernyőmentésed alapján a kulcsod
    private const val API_KEY = "21cc7f1fc0msh22c85e0f920241dp13f2c1jsn85fc537debf5" //km10
    //private const val API_KEY = "c38944651fmsh8847a25e81bec2ap1c49d2jsn90eebffb02dc" //km25
    //private const val API_KEY = "9a6d7aa3fdmsh0cf6b2943cafc0fp10d0f8jsnd4afc62d51a5" //pi
    //private const val API_KEY = "74bc56a2d1mshdbf6f9bcc47683fp1dde56jsnab8ff75f9163" //pu
    //private const val API_KEY = "862ebdfdf4msh58930c6e8ecb7e8p1c5646jsn691cd41f207e" //ga
    //private const val API_KEY = "5e252e267fmsh5fd6699825f927dp103e62jsn8738c4527347" //keme2.1.1
    //private const val API_KEY = "62825d6271msh98fce679e447cb5p1963c2jsn225f2eca671b" //keme2.1.2
    //private const val API_KEY = "90890141b8mshcc5b61f37a7b2d7p176738jsnf0eba064f2e0" //madi
    //private const val API_KEY = "d675682818msh47b79e79773e8e9p1a3330jsn03709af21e1b" //talian
    //private const val API_KEY = "9ca5b4a22fmshe79780dc5922051p1b2510jsnf54b32176287" // lau
    //private const val API_KEY = "2099949572msh2da155e905dddd9p1c3f0djsn19549b68ef30" //fazek
    //private const val API_KEY = "8f20c228ffmshb17d0fcfe9f670dp113963jsn4006487994fa" //simi08
    //private const val API_KEY = "e2f81e717fmsh4825fc28a23dc83p13e7a1jsneeea6ad7dc7f" //simi0108



    // Az okHttpClient létrehozása a .build() hívással fejeződik be
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("x-rapidapi-key", API_KEY)
                .addHeader("x-rapidapi-host", "hyprace-api.p.rapidapi.com")
                .build()
            chain.proceed(request)
        }
        .build() // Ez állítja elő a tényleges OkHttpClient-et

    val apiService: F1ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Most már a típus megegyezik a várttal
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(F1ApiService::class.java)
    }
}