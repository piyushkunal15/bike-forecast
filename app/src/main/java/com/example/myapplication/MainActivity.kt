package com.example.myapplication

import android.os.Bundle
import android.util.Log
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

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var cityTextView: TextView
    private lateinit var searchEditText: TextInputEditText
    private lateinit var searchButton: Button
    private lateinit var weatherApi: WeatherApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewWeather)
        cityTextView = findViewById(R.id.cityTextView)
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

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
            searchCity(searchEditText.text.toString())
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchCity(searchEditText.text.toString())
                true
            } else {
                false
            }
        }

        // Initial search for Bangalore
        searchCity("Bangalore")
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

        weatherApi.getCoordinates(cityName, 1, ApiKeys.WEATHER_API_KEY)
            .enqueue(object : Callback<List<GeocodingResponse>> {
                override fun onResponse(
                    call: Call<List<GeocodingResponse>>,
                    response: Response<List<GeocodingResponse>>
                ) {
                    if (response.isSuccessful) {
                        val locations = response.body()
                        if (locations.isNullOrEmpty()) {
                            Toast.makeText(
                                this@MainActivity,
                                "City not found",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                        
                        val location = locations[0]
                        fetchWeatherData(location.lat, location.lon)
                    } else {
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

    private fun fetchWeatherData(lat: Double, lon: Double) {
        weatherApi.get7DayForecast(lat, lon, ApiKeys.WEATHER_API_KEY)
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        val weatherResponse = response.body()
                        if (weatherResponse != null) {
                            // Update city name
                            val cityName = "${weatherResponse.city.name}, ${weatherResponse.city.country}"
                            cityTextView.text = cityName

                            // Process weather data
                            val dailyWeather = weatherResponse.list
                                .groupBy {
                                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        .format(Date(it.dt * 1000))
                                }
                                .map { it.value.first() }
                                .take(7)
                                .map { WeatherDay.fromWeatherData(it) }

                            val adapter = WeatherAdapter(dailyWeather)
                            recyclerView.adapter = adapter
                        }
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Error fetching weather data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Toast.makeText(
                        this@MainActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
