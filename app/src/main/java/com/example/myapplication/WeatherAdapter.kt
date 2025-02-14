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

class WeatherAdapter : RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>() {
    private val weatherList = mutableListOf<WeatherDay>()
    private var isLoading = false
    private var hasReachedMax = false
    private var onLoadMoreListener: (() -> Unit)? = null

    class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val weekIndicator: TextView = itemView.findViewById(R.id.weekIndicator)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val temperatureTextView: TextView = itemView.findViewById(R.id.temperatureTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val windSpeedTextView: TextView = itemView.findViewById(R.id.windSpeedTextView)
        val bikeScoreChart: PieChart = itemView.findViewById(R.id.bikeScoreChart)
        val textViewBikeScore: TextView = itemView.findViewById(R.id.textViewBikeScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weather, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val weatherDay = weatherList[position]
        
        // Add week indicator
        val weekNumber = (position / 7) + 1
        holder.weekIndicator.text = "Week $weekNumber"

        holder.dateTextView.text = weatherDay.date
        holder.temperatureTextView.text = String.format("%.1f°C", weatherDay.temperature)
        holder.descriptionTextView.text = weatherDay.description
        
        // Format wind speed with units
        val windSpeedKmh = weatherDay.windSpeed * 3.6 // Convert m/s to km/h
        holder.windSpeedTextView.text = String.format("%.1f km/h", windSpeedKmh)

        // Calculate bike score (0-100)
        val bikeScore = calculateBikeScore(weatherDay)
        
        // Update pie chart with color coding
        setupPieChart(holder.bikeScoreChart, bikeScore)
        
        // Set bike score text with color
        holder.textViewBikeScore.text = bikeScore.toString()
        holder.textViewBikeScore.setTextColor(getBikeScoreColor(bikeScore))

        // Check if we need to load more data
        if (position == weatherList.size - 2 && !isLoading && !hasReachedMax) {
            isLoading = true
            onLoadMoreListener?.invoke()
        }
    }

    private fun calculateBikeScore(weatherDay: WeatherDay): Int {
        var score = 100

        // Temperature penalty (ideal range: 18-23°C)
        when {
            weatherDay.temperature < 0 -> score -= 50
            weatherDay.temperature < 5 -> score -= 40
            weatherDay.temperature < 10 -> score -= 30
            weatherDay.temperature < 15 -> score -= 20
            weatherDay.temperature < 18 -> score -= 10
            weatherDay.temperature > 30 -> score -= 50
            weatherDay.temperature > 27 -> score -= 40
            weatherDay.temperature > 25 -> score -= 30
            weatherDay.temperature > 23 -> score -= 10
        }

        // Wind speed penalty (ideal: < 10 km/h)
        val windSpeedKmh = weatherDay.windSpeed * 3.6
        when {
            windSpeedKmh > 40 -> score -= 50
            windSpeedKmh > 30 -> score -= 40
            windSpeedKmh > 20 -> score -= 30
            windSpeedKmh > 15 -> score -= 20
            windSpeedKmh > 10 -> score -= 10
        }

        // Weather condition penalty
        when (weatherDay.weatherCode) {
            in 200..232 -> score -= 100  // Thunderstorm
            in 300..321 -> score -= 50   // Drizzle
            in 500..504 -> score -= 70   // Rain
            in 511..511 -> score -= 100  // Freezing rain
            in 520..531 -> score -= 80   // Shower rain
            in 600..622 -> score -= 100  // Snow
            in 701..701 -> score -= 30   // Mist
            in 711..711 -> score -= 60   // Smoke
            in 721..721 -> score -= 40   // Haze
            in 731..731 -> score -= 70   // Dust/sand whirls
            in 741..741 -> score -= 50   // Fog
            in 751..762 -> score -= 80   // Sand/dust
            in 771..771 -> score -= 90   // Squalls
            in 781..781 -> score -= 100  // Tornado
            800 -> score -= 0            // Clear sky
            801 -> score -= 5            // Few clouds
            802 -> score -= 10           // Scattered clouds
            803 -> score -= 15           // Broken clouds
            804 -> score -= 20           // Overcast clouds
        }

        return score.coerceIn(0, 100)
    }

    private fun getBikeScoreColor(score: Int): Int {
        return when {
            score >= 90 -> Color.parseColor("#2E7D32")  // Dark Green - Excellent
            score >= 80 -> Color.parseColor("#4CAF50")  // Green - Very Good
            score >= 70 -> Color.parseColor("#8BC34A")  // Light Green - Good
            score >= 60 -> Color.parseColor("#FDD835")  // Yellow - Moderate
            score >= 50 -> Color.parseColor("#FFB300")  // Orange - Fair
            score >= 40 -> Color.parseColor("#FB8C00")  // Dark Orange - Below Average
            score >= 30 -> Color.parseColor("#F4511E")  // Light Red - Poor
            else -> Color.parseColor("#C62828")         // Dark Red - Very Poor
        }
    }

    private fun setupPieChart(pieChart: PieChart, score: Int) {
        val entries = listOf(
            PieEntry(score.toFloat()),
            PieEntry((100 - score).toFloat())
        )

        val dataSet = PieDataSet(entries, "Bike Score")
        dataSet.colors = listOf(
            getBikeScoreColor(score),     // Dynamic color based on score
            Color.parseColor("#EEEEEE")   // Light gray for remaining
        )
        dataSet.setDrawValues(false)

        val pieData = PieData(dataSet)
        
        with(pieChart) {
            data = pieData
            description.isEnabled = false
            legend.isEnabled = false
            setDrawEntryLabels(false)
            isRotationEnabled = false
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleColor(Color.TRANSPARENT)
            setTransparentCircleAlpha(0)
            holeRadius = 75f
            setTouchEnabled(false)
            invalidate()
        }
    }

    override fun getItemCount(): Int = weatherList.size

    fun setOnLoadMoreListener(listener: () -> Unit) {
        onLoadMoreListener = listener
    }

    fun addItems(newItems: List<WeatherDay>) {
        if (weatherList.size >= 28) { // 4 weeks * 7 days
            hasReachedMax = true
            return
        }
        val startPosition = weatherList.size
        val itemsToAdd = newItems.take(28 - weatherList.size) // Only add up to 28 days total
        weatherList.addAll(itemsToAdd)
        notifyItemRangeInserted(startPosition, itemsToAdd.size)
        isLoading = false
    }

    fun clearItems() {
        weatherList.clear()
        hasReachedMax = false
        isLoading = false
        notifyDataSetChanged()
    }

    fun getItems(): List<WeatherDay> = weatherList.toList()
}
