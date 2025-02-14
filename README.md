# Bike Ride Weather Forecast App

An Android application that provides 4-week weather forecasts optimized for cyclists, featuring bike-ride suitability scores and city search functionality.

## Features

- 🌤️ 4-week weather forecast with infinite scroll
- 🔍 Global city search
- 🚲 Bike ride suitability score
- 📊 Visual weather indicators
- 🌡️ Temperature and wind speed information

## Setup

1. Clone the repository
2. Create `ApiKeys.kt` and add your OpenWeatherMap API key:

kotlin
object ApiKeys {
const val WEATHER_API_KEY = "your_api_key_here"
}
3. Build and run in Android Studio

## Project Structure
app/
├── MainActivity.kt # Main UI and business logic
├── WeatherAdapter.kt # RecyclerView adapter for weather cards
├── WeatherApi.kt # OpenWeatherMap API interface
├── WeatherDay.kt # Weather data model
├── WeatherResponse.kt # API response models
└── ApiKeys.kt # API key configuration
## Dependencies

- Retrofit2 for API calls
- MPAndroidChart for visualizations
- Material Design components
- ConstraintLayout for UI

## Implementation Details

- Uses OpenWeatherMap API for weather data
- Implements infinite scroll with 4-week limit
- Calculates bike ride suitability based on weather conditions
- Handles date progression for multi-week forecast
- Manages loading states and error handling

## Future Enhancements

- [ ] Save last searched city
- [ ] Add weather icons
- [ ] Implement temperature graphs
- [ ] Add unit settings (C°/F°)
- [ ] Add city favorites

## License

This project is licensed under the MIT License.
