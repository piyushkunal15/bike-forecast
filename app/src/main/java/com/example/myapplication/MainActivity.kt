package com.example.myapplication

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.view.inputmethod.InputMethodManager
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var cityTextView: TextView
    private lateinit var searchEditText: TextInputEditText
    private lateinit var searchButton: Button
    private lateinit var weatherApi: WeatherApi
    private var currentCall: Call<*>? = null
    private var currentLat: Double = 0.0
    private var currentLon: Double = 0.0
    private var currentDayOffset = 0
    private val MAX_FORECAST_DAYS = 28 // 4 weeks
    private lateinit var weatherAdapter: WeatherAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewWeather)
        cityTextView = findViewById(R.id.cityTextView)
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        
        // Initialize adapter
        weatherAdapter = WeatherAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = weatherAdapter
        recyclerView.setHasFixedSize(true)

        // Set up infinite scroll
        weatherAdapter.setOnLoadMoreListener {
            loadMoreWeatherData()
        }

        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherApi = retrofit.create(WeatherApi::class.java)

        // Set up touch listener for hiding keyboard
        val rootLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.root_layout)
        rootLayout.setOnClickListener { hideKeyboard() }

        // Set up search functionality
        searchButton.setOnClickListener {
            searchCity(searchEditText.text.toString().trim())
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchCity(searchEditText.text.toString().trim())
                true
            } else {
                false
            }
        }

        // Initial search for Bangalore
        recyclerView.post {
            searchCity("Bangalore")
        }
    }

    private fun hideKeyboard() {
        currentFocus?.let { view ->
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }
    }

    private fun searchCity(cityName: String) {
        if (cityName.isBlank()) {
            Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
            return
        }

        hideKeyboard()
        weatherAdapter.clearItems()
        currentDayOffset = 0 // Reset offset for new city

        // Cancel any ongoing network calls
        currentCall?.cancel()

        // Show loading state
        cityTextView.text = "Searching..."

        val call = weatherApi.getCoordinates(cityName, 1, ApiKeys.WEATHER_API_KEY)
        currentCall = call

        call.enqueue(object : Callback<List<GeocodingResponse>> {
            override fun onResponse(
                call: Call<List<GeocodingResponse>>,
                response: Response<List<GeocodingResponse>>
            ) {
                if (call.isCanceled) return
                
                if (response.isSuccessful) {
                    val locations = response.body()
                    if (locations.isNullOrEmpty()) {
                        cityTextView.text = "City not found"
                        Toast.makeText(
                            this@MainActivity,
                            "City not found",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    
                    val location = locations[0]
                    currentLat = location.lat
                    currentLon = location.lon
                    fetchWeatherData(location.lat, location.lon, true)
                } else {
                    cityTextView.text = "Error finding city"
                    Toast.makeText(
                        this@MainActivity,
                        "Error finding city",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<GeocodingResponse>>, t: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun fetchWeatherData(lat: Double, lon: Double, isNewSearch: Boolean = false) {
        // Don't fetch if we've reached the maximum days
        if (weatherAdapter.getItems().size >= MAX_FORECAST_DAYS) {
            return
        }

        val call = weatherApi.get7DayForecast(lat, lon, ApiKeys.WEATHER_API_KEY)
        currentCall = call

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (call.isCanceled) return
                
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    if (weatherResponse != null) {
                        // Update city name only on new search
                        if (isNewSearch) {
                            val cityName = "${weatherResponse.city.name}, ${weatherResponse.city.country}"
                            cityTextView.text = cityName
                            currentDayOffset = 0 // Reset offset for new city
                        }

                        // Get the first weather data point
                        val firstDataPoint = weatherResponse.list.firstOrNull()
                        if (firstDataPoint != null) {
                            // Create weather data for next 7 days
                            val dailyWeather = (0..6).map { dayOffset ->
                                val calendar = Calendar.getInstance()
                                calendar.add(Calendar.DAY_OF_YEAR, currentDayOffset + dayOffset)
                                
                                // Use the temperature and weather from API for first day,
                                // or estimate for future days
                                val weatherData = if (dayOffset == 0) {
                                    firstDataPoint
                                } else {
                                    // Find closest time point for this day if available
                                    weatherResponse.list.find { data ->
                                        val dataDate = Calendar.getInstance().apply {
                                            timeInMillis = data.dt * 1000
                                        }
                                        dataDate.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
                                    } ?: firstDataPoint // Fallback to first data point if no match
                                }

                                WeatherDay.fromWeatherData(weatherData, calendar.time)
                            }

                            weatherAdapter.addItems(dailyWeather)
                            currentDayOffset += 7
                            
                            // Update UI if we've reached the maximum
                            if (weatherAdapter.getItems().size >= MAX_FORECAST_DAYS) {
                                showMaxForecastReachedMessage()
                            }
                        }
                    }
                } else {
                    handleError("Error fetching weather data")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                if (call.isCanceled) return
                handleError("Network error: ${t.message}")
            }
        })
    }

    private fun loadMoreWeatherData() {
        fetchWeatherData(currentLat, currentLon, false)
    }

    private fun handleError(message: String) {
        cityTextView.text = message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showMaxForecastReachedMessage() {
        Toast.makeText(
            this,
            "Maximum forecast period (4 weeks) reached",
            Toast.LENGTH_SHORT
        ).show()
    }
}
