package com.example.sunnyweather.ui.place

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.sunnyweather.logic.Respository
import com.example.sunnyweather.logic.model.Place
import retrofit2.http.Query

class PlaceViewModel : ViewModel() {
        private val searchLiveData = MutableLiveData<String>()
        val placeList = ArrayList<Place>()
        val placeLiveData = Transformations.switchMap(searchLiveData){query ->
            Respository.searchPlaces(query)
        }
    fun searchPlaces(query: String){
        searchLiveData.value = query
    }
}