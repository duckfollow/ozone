package me.duckfollow.ozone.service

import me.duckfollow.ozone.model.WaqiLocation
import retrofit2.Call
import retrofit2.http.GET

interface WaqiGetLocationService {
    @GET("map/bounds/?latlng=39.379436,116.091230,40.235643,116.784382&token=fe5f8a6aa99f6bfb397762a0cade98a6d78795a6")
    fun getLocation(): Call<WaqiLocation>
}