package com.example.myapplication

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("data/2.5/forecast")
    fun get7DayForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("cnt") count: Int = 40  // Maximum number of timestamps
    ): Call<WeatherResponse>

    @GET("geo/1.0/direct")
    fun getCoordinates(
        @Query("q") cityName: String,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): Call<List<GeocodingResponse>>
}

data class GeocodingResponse(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String
)
