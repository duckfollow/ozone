package me.duckfollow.ozone.service

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.duckfollow.ozone.util.UnsafeOkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory;

class WaqiService {
    private var iGetService: WaqiGetInfoService? = null

    fun createService(): WaqiGetInfoService? {
        val gson: Gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
       // val okHttpClient = UnsafeOkHttpClient.unsafeOkHttpClient
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            //.client(okHttpClient)
            .baseUrl("https://api.waqi.info/feed/bangkok/")
            .build()
        iGetService = retrofit.create<WaqiGetInfoService>(WaqiGetInfoService::class.java)
        return iGetService
    }
}