package com.sunnyweather.android.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.example.sunnyweather.SunnyWeatherApplication
import com.example.sunnyweather.logic.model.Place
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

//object PlaceDao {
//
//    //存储
//    fun savePlace(place: Place) {
//        sharedPreferences().edit {
//            putString("place", Gson().toJson(place))
//        }
//    }
//    //读取
//    fun getSavedPlace(): Place {
//        val placeJson = sharedPreferences().getString("place", "")
//        return Gson().fromJson(placeJson, Place::class.java)
//    }
//
//    fun isPlaceSaved() = sharedPreferences().contains("place")
//
//    //全局的存储
//    private fun sharedPreferences() =
//        SunnyWeatherApplication.context.getSharedPreferences("sunny_weather", Context.MODE_PRIVATE)
//
//}

object PlaceDao {

    // 存储地点列表
    fun savePlaces(places: Place) {

        // 首先获取已保存的地点列表
        val savedPlaces = getSavedPlaces().toMutableList()
        // 将新的地点添加到列表中
        savedPlaces.add(places)
        // 将更新后的地点列表转换为 JSON 字符串并保存
        val json = Gson().toJson(savedPlaces)
        sharedPreferences().edit {
            putString("places", json)
        }
    }

    // 读取地点列表
    fun getSavedPlaces(): List<Place> {
        val json = sharedPreferences().getString("places", "")
        return Gson().fromJson(json, object : TypeToken<List<Place>>() {}.type)
            ?: emptyList()
    }

    // 判断是否有存储的地点
    fun isPlaceSaved() = sharedPreferences().contains("places")

    private fun sharedPreferences() =
        SunnyWeatherApplication.context.getSharedPreferences("sunny_weather", Context.MODE_PRIVATE)
}
