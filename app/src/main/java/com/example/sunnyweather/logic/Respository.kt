package com.example.sunnyweather.logic

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.example.sunnyweather.logic.model.Place
import com.example.sunnyweather.logic.model.Weather
import com.example.sunnyweather.logic.network.SunnyWeatherNetwork
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.RealtimeResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.lang.RuntimeException

object Respository {
    // 存储地点列表
    fun savePlaces(places: Place) = PlaceDao.savePlaces(places)
    // 获取地点列表
    fun getSavedPlaces(): List<Place> = PlaceDao.getSavedPlaces()
    // 判断是否有存储的地点列表
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
    fun searchLocalWeather(places: List<Place>, amount: Int): Array<MutableLiveData<RealtimeResponse.Realtime>> {
        val liveDataArray = Array(amount) { MutableLiveData<RealtimeResponse.Realtime>() }
        for (i in 0 until amount) {
            val liveData = liveDataArray[i]
            CoroutineScope(Dispatchers.IO).launch {
                val result = try {
                    val localWeather = SunnyWeatherNetwork.getLocalWeather(places[i].location.lng,places[i].location.lat)
                    Log.d("Respository","本地天气获取${localWeather.result.realtime}")
                    if (localWeather.status == "ok") {
                        Result.success(localWeather.result.realtime)

                    } else {
                        Result.failure<RealtimeResponse.Realtime>(RuntimeException("response status is ${localWeather.status}"))
                    }
                } catch (e: Exception) {
                    Result.failure<RealtimeResponse.Realtime>(e)
                }
                liveData.postValue(result.getOrDefault(null))
            }
        }
        return liveDataArray
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
//     fun refreshRealtimeWeather(lng: String, lat: String): Result<RealtimeResponse> {
//        return try {
//            val realtimeResponse = SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
//            if (realtimeResponse.status == "ok") {
//                Result.success(realtimeResponse)
//            } else {
//                Result.failure(
//                    RuntimeException(
//                        "Realtime response status is ${realtimeResponse.status}"
//                    )
//                )
//            }
//        } catch (e: Exception) {
//            Result.failure<RealtimeResponse>(e)
//        }
//    }
//    emit(result)
}