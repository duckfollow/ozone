package me.duckfollow.ozone.service

import me.duckfollow.ozone.model.WaqiInfo
import retrofit2.Call
import retrofit2.http.GET

interface WaqiGetInfoService {
    @GET("/?token=fe5f8a6aa99f6bfb397762a0cade98a6d78795a6")
    fun getInfo():Call<WaqiInfo>
}