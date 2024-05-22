package com.example.sunnyweather.logic.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PlaceResponse(val status: String, val places: List<Place>)
data class Place(val name: String, val location: Location,@SerializedName("formatted_address") val address: String): Serializable
data class Location(val lng: String,val lat: String)