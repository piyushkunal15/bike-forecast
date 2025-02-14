package com.example.myapplication

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlin.math.roundToInt

class WeatherAdapter(private val weatherList: List<WeatherDay>) :
    RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>() {

    class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
        val textViewTemp: TextView = itemView.findViewById(R.id.textViewTemp)
        val textViewWeatherDesc: TextView = itemView.findViewById(R.id.textViewWeatherDesc)
        val textViewBikeScore: TextView = itemView.findViewById(R.id.textViewBikeScore)
        val pieChart: PieChart = itemView.findViewById(R.id.pieChart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weather, parent, false)
        return WeatherViewHolder(view)
    }

    override fun getItemCount(): Int = weatherList.size

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val weatherDay = weatherList[position]
        
        holder.textViewDate.text = weatherDay.date
        holder.textViewTemp.text = "🌡️ ${weatherDay.temperature.roundToInt()}°C"
        holder.textViewWeatherDesc.text = "${getWeatherEmoji(weatherDay.weatherCode)} ${weatherDay.getWeatherDescription()}"

        val bikeScore = calculateBikeScore(weatherDay)
        setupPieChart(holder.pieChart, bikeScore)
        
        val bikeScoreColor = when {
            bikeScore >= 80 -> Color.parseColor("#4CAF50")
            bikeScore >= 60 -> Color.parseColor("#FFC107")
            else -> Color.parseColor("#F44336")
        }
        holder.textViewBikeScore.setTextColor(bikeScoreColor)
        holder.textViewBikeScore.text = "\uD83D\uDEB2 Bike Score: $bikeScore%"
    }

    private fun calculateBikeScore(day: WeatherDay): Int {
        val tempScore = when {
            day.temperature in 15.0..25.0 -> 50
            day.temperature in 10.0..30.0 -> 30
            else -> 10
        }
        
        val windScore = when {
            day.windSpeed < 5.0 -> 30
            day.windSpeed < 10.0 -> 20
            day.windSpeed < 15.0 -> 10
            else -> 0
        }
        
        val weatherScore = when (day.weatherCode) {
            in 800..801 -> 20  // Clear or few clouds
            in 802..804 -> 15  // Cloudy conditions
            in 701..781 -> 10  // Atmospheric conditions (mist, fog, etc)
            in 600..622 -> 0   // Snow
            in 500..531 -> 5   // Rain
            in 200..232 -> 0   // Thunderstorm
            else -> 10
        }
        
        return tempScore + windScore + weatherScore
    }

    private fun getWeatherEmoji(weatherCode: Int): String {
        return when (weatherCode) {
            800 -> "☀️"     // Clear sky
            in 801..802 -> "⛅" // Few clouds
            in 803..804 -> "☁️" // Cloudy
            in 701..781 -> "🌫️" // Mist, fog, etc
            in 600..622 -> "🌨️" // Snow
            in 500..531 -> "🌧️" // Rain
            in 300..321 -> "🌦️" // Drizzle
            in 200..232 -> "⛈️" // Thunderstorm
            else -> "🌤️"
        }
    }

    private fun setupPieChart(pieChart: PieChart, score: Int) {
        pieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 85f
            setDrawEntryLabels(false)
            legend.isEnabled = false
            setTouchEnabled(false)
        }

        val entries = listOf(
            PieEntry(score.toFloat(), "Score"),
            PieEntry((100 - score).toFloat(), "Remaining")
        )

        val dataSet = PieDataSet(entries, "Bike Score").apply {
            colors = listOf(
                when {
                    score >= 80 -> Color.parseColor("#4CAF50")
                    score >= 60 -> Color.parseColor("#FFC107")
                    else -> Color.parseColor("#F44336")
                },
                Color.parseColor("#EEEEEE")
            )
            setDrawValues(false)
        }

        pieChart.data = PieData(dataSet)
        pieChart.invalidate()
    }
}
