package com.example.sunnyweather.ui.place

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.sunnyweather.logic.Respository
import com.example.sunnyweather.logic.model.Place
import com.sunnyweather.android.logic.dao.PlaceDao
import retrofit2.http.Query

class PlaceViewModel : ViewModel() {
    fun savePlace(place: Place) = Respository.savePlace(place)
    fun getSavedPlace() = Respository.getSavedPlace()
    fun isPlaceSaved() = Respository.isPlaceSaved()

        private val searchLiveData = MutableLiveData<String>()
        val placeList = ArrayList<Place>()
        val placeLiveData = Transformations.switchMap(searchLiveData){query ->
            Respository.searchPlaces(query)
        }
    fun searchPlaces(query: String){
        searchLiveData.value = query
    }
}