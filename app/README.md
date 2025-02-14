# Bike Ride Forecast App - Technical Documentation

## Project Overview
The Bike Ride Forecast app is an Android application that provides a 7-day weather forecast with a specialized "bike score" to help users determine good days for cycling. The app uses the Open-Meteo API for weather data and displays it in a user-friendly interface.

## Project Structure

### Core Components

#### 1. Data Layer
**WeatherApi.kt**
- Defines the API interface using Retrofit
- Handles communication with Open-Meteo API
- Specifies query parameters for weather data

**WeatherResponse.kt**
- Data classes that model the API response
- `WeatherResponse`: Root response object
- `DailyData`: Contains lists of weather parameters

**WeatherDay.kt**
- Model class representing a single day's weather
- Contains weather description mapping logic
- Used to combine different weather parameters into a single object

#### 2. UI Layer

**MainActivity.kt**
- Main entry point of the application
- Sets up RecyclerView and API client
- Handles API calls and data processing
- Manages the main UI thread

**WeatherAdapter.kt**
- RecyclerView adapter for displaying weather data
- Handles the creation and binding of weather item views
- Implements bike score calculation and visualization
- Manages the pie chart display using MPAndroidChart

### Layout Files

**activity_main.xml**
- Main screen layout
- Contains RecyclerView and title
- Uses ConstraintLayout for flexible positioning
- Implements gradient background

**item_weather.xml**
- Individual weather card layout
- Contains weather information and pie chart
- Uses CardView for material design appearance
- Implements custom gradient background

### Resource Files

**colors.xml**
- Color definitions
- Contains theme colors
- Defines text and background colors

**themes.xml**
- App theme definitions
- Material Design theme customization
- Status bar and window background settings

**drawable/**
- `card_gradient_background.xml`: Gradient for weather cards
- `main_background_gradient.xml`: Main activity background
- `ic_launcher_foreground.xml`: App icon foreground
- `ic_launcher_background.xml`: App icon background

### Configuration Files

**app/build.gradle.kts**
- App-level build configuration
- Dependencies management
- SDK version settings
- Third-party library inclusion

**settings.gradle.kts**
- Project-level settings
- Repository configurations
- Project name and included modules

**AndroidManifest.xml**
- App permissions (Internet)
- Activity declarations
- Application metadata

## Key Features

### 1. Weather Data Retrieval
- Uses Retrofit for API communication
- Fetches 7-day forecast from Open-Meteo
- Includes temperature, weather conditions, and wind speed

### 2. Bike Score Calculation
kotlin
private fun calculateBikeScore(day: WeatherDay): Int {
val tempScore = when {
day.temperature in 15.0..25.0 -> 50
day.temperature in 10.0..30.0 -> 30
else -> 10
}
val windScore = if (day.windSpeed < 10) 30 else 10
val weatherScore = if (weatherCode in listOf(...)) 0 else 20
return tempScore + windScore + weatherScore
}

- Temperature evaluation (50 points max)
- Wind speed consideration (30 points max)
- Weather condition impact (20 points max)

### 3. Visual Representation
- MPAndroidChart for pie chart visualization
- Material Design cards with gradients
- Weather condition emojis
- Color-coded bike scores

## Data Flow
1. MainActivity initializes → Sets up RecyclerView
2. API call made to Open-Meteo
3. Response parsed into WeatherResponse
4. Data transformed into List<WeatherDay>
5. WeatherAdapter receives data
6. Adapter creates view holders and binds data
7. Each item displays weather info and bike score

## Dependencies
- Retrofit: API communication
- MPAndroidChart: Pie chart visualization
- RecyclerView: List display
- CardView: Material Design cards
- ConstraintLayout: Flexible layouts

## Build and Deployment
- Minimum SDK: Android 5.0 (API 21)
- Target SDK: Latest Android version
- Build using Android Studio or Gradle
- Debug and Release build variants available

## Future Enhancements
1. Location-based weather data
2. User preferences for bike score calculation
3. Weather notifications
4. Historical data tracking
5. Multiple location support