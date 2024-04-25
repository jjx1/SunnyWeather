package com.example.sunnyweather.logic

import androidx.lifecycle.liveData
import com.example.filepersistencetest.R
import com.example.sunnyweather.logic.model.Place
import com.example.sunnyweather.logic.model.Weather
import com.example.sunnyweather.logic.network.SunnyWeatherNetwork
import com.google.gson.Gson
import com.sunnyweather.android.logic.dao.PlaceDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.Dispatcher
import retrofit2.http.Query
import java.lang.RuntimeException

object Respository {
    fun savePlace(place: Place) = PlaceDao.savePlace(place)
    fun getSavedPlace() = PlaceDao.getSavedPlace()
    fun isPlaceSaved() = PlaceDao.isPlaceSaved()
    //把搜索的请求操作进行一个切换，等于中转站
    fun searchPlaces(query: String) = liveData(Dispatchers.IO){
        val result = try {
            val placeResponse =SunnyWeatherNetwork.serchPlaces(query)
            if (placeResponse.status == "ok"){
                val places = placeResponse.places
                Result.success(places)
            }else{
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        }catch (e: Exception){
            Result.failure<List<Place>>(e)
        }
        emit(result)
    }
    fun refreshWeather(lng: String, lat:String) = liveData(Dispatchers.IO){
        val result = try {
            coroutineScope {
                val deferredRealtime = async {
                    SunnyWeatherNetwork.getRealtimeWeather(lng,lat)
                }
                val deferredDaily = async {
                    SunnyWeatherNetwork.getDailyWeather(lng,lat)
                }
                val realtimeResponse = deferredRealtime.await()
                val dailyResponse = deferredDaily.await()
                if (dailyResponse.status == "ok" && realtimeResponse.status == "ok"){
                    //把两个数据类返回的数据封装到weather
                    val weather = Weather(realtimeResponse.result.realtime,dailyResponse.result.daily)
                    Result.success(weather)
                }else{
                    Result.failure(
                        RuntimeException(
                          "realtimeresponse status is ${realtimeResponse.status}"+
                            "dailyresponse status is ${dailyResponse.status}"
                        )
                    )
                }
            }
        }catch (e: Exception){
            Result.failure<Weather>(e)
        }
        emit(result)
    }
}