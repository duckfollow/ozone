package me.duckfollow.ozone.service

import me.duckfollow.ozone.model.WaqiInfo
import retrofit2.Call
import retrofit2.http.GET

interface WaqiGetInfoService {
    @GET
    fun getInfo():Call<WaqiInfo>
}