package com.example.sunnyweather.logic.model

import com.sunnyweather.android.logic.model.RealtimeResponse

data class PlaceWeatherData(val places: List<Place>, val weathers: List<RealtimeResponse.Realtime>)
