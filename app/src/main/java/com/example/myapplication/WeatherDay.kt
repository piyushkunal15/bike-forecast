package com.example.myapplication

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WeatherDay(
    val date: String,
    val temperature: Double,
    val weatherCode: Int,
    val windSpeed: Double,
    val description: String
) {
    fun getWeatherDescription(): String = description

    companion object {
        fun fromWeatherData(data: WeatherData, date: Date? = null): WeatherDay {
            val dateToUse = date ?: Date(data.dt * 1000)
            val formattedDate = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                .format(dateToUse)
            
            return WeatherDay(
                date = formattedDate,
                temperature = data.main.temp,
                weatherCode = data.weather.firstOrNull()?.id ?: 800,
                windSpeed = data.wind.speed,
                description = data.weather.firstOrNull()?.description?.capitalize() ?: "Clear"
            )
        }
    }
}