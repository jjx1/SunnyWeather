package com.example.sunnyweather.ui.weather

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.sunnyweather.logic.Respository
import com.example.sunnyweather.logic.model.Location


class WeatherViewModel : ViewModel() {
    private val locationLiveData = MutableLiveData<Location>()
    var locationLng = ""
    var locationLat = ""
    var placeName = ""
    val weatherLiveData = Transformations.switchMap(locationLiveData){location ->
        Log.d("WeatherViewModel", "weatherLiveData is assigned with value: ${location.lng}, ${location.lat}")
        Respository.refreshWeather(location.lng,location.lat)
    }
    fun refreshWeather(lng: String, lat: String){
        locationLiveData.value = Location(lng,lat)
//        Log.d("WeatherViewModel", "Result is null: ${Location(lng,lat)}")
    }
}