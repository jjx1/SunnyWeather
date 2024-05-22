package com.example.sunnyweather.ui.weather

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.sunnyweather.logic.Respository
import com.example.sunnyweather.logic.model.Location
import com.example.sunnyweather.logic.model.Place
import com.sunnyweather.android.logic.model.RealtimeResponse


class WeatherViewModel : ViewModel() {
    private val locationLiveData = MutableLiveData<Location>()
    private val localLiveData = MutableLiveData<Location>()
    // 添加一个 MutableLiveData 来存储 searchLocalWeather 返回的 LiveData 数组
    private val localWeatherLiveDataArray = MutableLiveData<Array<MutableLiveData<RealtimeResponse.Realtime>>>()

    fun getSavedPlace(): List<Place> = Respository.getSavedPlaces()
    fun isPlaceSaved() = Respository.isPlaceSaved()

    var locationLng = ""
    var locationLat = ""
    var placeName = ""
    val weatherList = mutableListOf<RealtimeResponse.Realtime>()

    val weatherLiveData = Transformations.switchMap(locationLiveData){location ->
        Log.d("WeatherViewModel", "weatherLiveData1 is assigned with value: ${location.lng}, ${location.lat}")
        Respository.refreshWeather(location.lng,location.lat)
    }
    fun refreshWeather(lng: String, lat: String){
        locationLiveData.value = Location(lng,lat)
//        Log.d("WeatherViewModel", "Result is null: ${Location(lng,lat)}")
    }

    // 调用 searchLocalWeather 方法并获取返回的 LiveData 数组
    fun searchLocalWeather(place: List<Place> =getSavedPlace(), amount: Int=getSavedPlace().size) {
        // 调用 WeatherRepository 的 searchLocalWeather 方法
        localWeatherLiveDataArray.value = Respository.searchLocalWeather(place, amount)

        Log.d(" WeatherViewModel","本地天气获取=${localWeatherLiveDataArray.value}")
    }

    // 添加一个方法来获取 LiveData 数组
    fun getLocalWeatherLiveDataArray(): LiveData<Array<MutableLiveData<RealtimeResponse.Realtime>>> {
        return localWeatherLiveDataArray
    }



//    private suspend fun postLocation(location: Location) {
//
//        localLiveData.value = location
//    }
//
//    fun getlocalPlace(): List<Place> {
//        if (isPlaceSaved()) {
//            val savedPlaces = getSavedPlace()
//            var lat = ""
//            var lng = ""
//            for (place in savedPlaces) {
//                lat = place.location.lat
//                lng = place.location.lng
//                Log.d("WeatherActivity", "历史地点循环成功获取: savedPlaces.size=${place}")
//                // 手动触发 LiveData 的数据变化
//                CoroutineScope(Dispatchers.Main).launch(viewModelScope.coroutineContext) {
//                    delay(200L)
//                    postLocation(Location(lng, lat))
//                }
////                localLiveData.postValue(Location(lng, lat))
//            }
//            return savedPlaces
//        } else {
//            Log.d("WeatherActivity", "Result is 不存在: ${isPlaceSaved()}")
//            return emptyList()
//        }
//    }
//    fun getWeathersForPlace(place: Place): List<Weather> {
//        val weathers = mutableListOf<Weather>()
//        val lat = place.location.lat
//        val lng = place.location.lng
//
//        // 获取地点对应的天气信息
//        val weatherResponse = Respository.refreshWeather(lat, lng)
//
//        // 如果天气信息不为 null 且状态为 "ok"，则将实时天气添加到 weathers 列表中
//        weatherResponse?.let {
//            if (it.status == "ok") {
//                weathers.add(it.realtime)
//            }
//        }
//        return weathers
//    }
}