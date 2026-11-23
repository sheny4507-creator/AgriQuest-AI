package com.example.data.remote

import com.example.data.model.AlertLevel
import com.example.data.model.DailyWeatherDay
import com.example.data.model.PhilippineRegion
import com.example.data.model.WeatherForecast
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CancellationException
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    @Json(name = "current") val current: CurrentWeatherDto? = null,
    @Json(name = "daily") val daily: DailyWeatherDto? = null
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    @Json(name = "temperature_2m") val temperature2m: Double = 29.0,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Int = 75,
    @Json(name = "rain") val rain: Double = 0.0,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double = 10.0,
    @Json(name = "weather_code") val weatherCode: Int = 2
)

@JsonClass(generateAdapter = true)
data class DailyWeatherDto(
    @Json(name = "time") val time: List<String> = emptyList(),
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double> = emptyList(),
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double> = emptyList(),
    @Json(name = "precipitation_sum") val precipitationSum: List<Double> = emptyList(),
    @Json(name = "weather_code") val weatherCode: List<Int> = emptyList()
)

interface OpenMeteoService {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,rain,weather_code,wind_speed_10m",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum",
        @Query("timezone") timezone: String = "Asia/Manila",
        @Query("forecast_days") forecastDays: Int = 5
    ): OpenMeteoResponse
}

class WeatherRepository {
    private val service: OpenMeteoService by lazy {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenMeteoService::class.java)
    }

    suspend fun fetchWeatherForRegion(region: PhilippineRegion): WeatherForecast {
        return try {
            val response = service.getForecast(
                latitude = region.latitude,
                longitude = region.longitude
            )
            val current = response.current ?: return demoFallback(region)
            val daily = response.daily

            val days = if (daily != null && daily.time.isNotEmpty()) {
                daily.time.indices.map { idx ->
                    val rawDate = daily.time[idx]
                    val shortLabel = if (idx == 0) "Today" else rawDate.takeLast(5)
                    DailyWeatherDay(
                        dateLabel = shortLabel,
                        maxTempC = daily.temperature2mMax.getOrElse(idx) { 31.0 },
                        minTempC = daily.temperature2mMin.getOrElse(idx) { 24.0 },
                        rainMm = daily.precipitationSum.getOrElse(idx) { 0.0 },
                        weatherCode = daily.weatherCode.getOrElse(idx) { 2 }
                    )
                }
            } else {
                demoDays()
            }

            buildAdvisoryForecast(
                region = region,
                tempC = current.temperature2m,
                humidity = current.relativeHumidity2m,
                windKmh = current.windSpeed10m,
                rainMm = current.rain,
                code = current.weatherCode,
                days = days,
                isDemo = false
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            demoFallback(region)
        }
    }

    private fun buildAdvisoryForecast(
        region: PhilippineRegion,
        tempC: Double,
        humidity: Int,
        windKmh: Double,
        rainMm: Double,
        code: Int,
        days: List<DailyWeatherDay>,
        isDemo: Boolean
    ): WeatherForecast {
        val upcomingHeavyRain = days.take(3).any { it.rainMm >= 25.0 }
        val (alertTitle, alertMsg, level, tips) = when {
            rainMm >= 10.0 || upcomingHeavyRain || code in listOf(65, 82, 95, 96, 99) -> {
                Quadruple(
                    "Heavy Rain & Drainage Alert",
                    "Expect heavy rainfall in ${region.name}. Protect low-lying plots and young vegetable seedlings from waterlogging.",
                    AlertLevel.WARNING,
                    listOf(
                        "Clear drainage canals and bund spillways before peak downpour.",
                        "Delay fertilizer topdressing so nutrients don't wash away.",
                        "Stake tall kamatis & talong plants and harvest ripe crops early."
                    )
                )
            }
            windKmh >= 30.0 -> {
                Quadruple(
                    "Strong Wind Crop Alert",
                    "Gusty winds (${windKmh.toInt()} km/h) detected in ${region.name}. Secure trellises and shade nets.",
                    AlertLevel.CAUTION,
                    listOf(
                        "Reinforce bamboo stakes on talong, sitaw, and kamatis beds.",
                        "Avoid spraying foliar nutrients or neem oil until winds calm.",
                        "Check banana plants for leaning props."
                    )
                )
            }
            tempC >= 33.5 -> {
                Quadruple(
                    "High Heat & Dry Stress Watch",
                    "Hot field conditions (${tempC.toInt()}°C) in ${region.name}. Minimize midday evaporation and leaf scorch.",
                    AlertLevel.CAUTION,
                    listOf(
                        "Irrigate early morning (5–8 AM) or late afternoon to cut water loss.",
                        "Add rice straw or dried grass mulch around vegetable roots.",
                        "Watch for spider mites and whiteflies which multiply faster in hot weather."
                    )
                )
            }
            humidity >= 85 -> {
                Quadruple(
                    "High Humidity Fungal Watch",
                    "Humid air ($humidity%) favors leaf blast, downy mildew, and early blight.",
                    AlertLevel.CAUTION,
                    listOf(
                        "Water at the root zone only—keep foliage as dry as possible.",
                        "Prune crowded bottom leaves for better morning airflow.",
                        "Scout leaves early for gray/brown spots."
                    )
                )
            }
            else -> {
                Quadruple(
                    "Favorable Farming Window",
                    "Balanced weather in ${region.name}—great conditions for transplanting, scouting, and scheduled care.",
                    AlertLevel.GOOD,
                    listOf(
                        "Ideal morning window for weeding, composting, or foliar care.",
                        "Inspect 10 sample plants across your field for early pest signs.",
                        "Log today's input costs in Farm Budget Challenge to track profit."
                    )
                )
            }
        }

        return WeatherForecast(
            regionName = region.name,
            province = region.province,
            currentTempC = tempC,
            humidityPct = humidity,
            windKmh = windKmh,
            rainMm = rainMm,
            weatherCode = code,
            conditionLabel = weatherCodeToLabel(code),
            farmingAlertTitle = alertTitle,
            farmingAlertMessage = alertMsg,
            alertLevel = level,
            cropProtectionTips = tips,
            dailyDays = days,
            isDemoFallback = isDemo
        )
    }

    private fun demoFallback(region: PhilippineRegion): WeatherForecast {
        return buildAdvisoryForecast(
            region = region,
            tempC = 30.5,
            humidity = 78,
            windKmh = 14.0,
            rainMm = 2.4,
            code = 2,
            days = demoDays(),
            isDemo = true
        )
    }

    private fun demoDays() = listOf(
        DailyWeatherDay("Today", 32.0, 24.5, 2.4, 2),
        DailyWeatherDay("Day 2", 31.5, 24.0, 8.0, 61),
        DailyWeatherDay("Day 3", 30.0, 23.8, 18.5, 80),
        DailyWeatherDay("Day 4", 32.2, 24.4, 1.0, 1),
        DailyWeatherDay("Day 5", 33.0, 25.0, 0.0, 0)
    )

    private fun weatherCodeToLabel(code: Int): String = when (code) {
        0 -> "Clear Sunny Sky"
        1, 2 -> "Partly Cloudy"
        3 -> "Overcast Clouds"
        45, 48 -> "Morning Mist / Fog"
        51, 53, 55 -> "Light Drizzle"
        61, 63, 65 -> "Steady Rain"
        80, 81, 82 -> "Passing Rain Showers"
        95, 96, 99 -> "Thunderstorm Watch"
        else -> "Tropical Fair Weather"
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
