package me.duckfollow.ozone.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class WaqiLocation(
    @Expose
    @SerializedName("status")
    val status:String,
    @Expose
    @SerializedName("data")
    val data:ArrayList<Location>
)

data class Location(
    @Expose
    @SerializedName("lat")
    val lat:Double,
    @Expose
    @SerializedName("lon")
    val lon:Double,
    @Expose
    @SerializedName("uid")
    val uid:String,
    @Expose
    @SerializedName("aqi")
    val aqi:String,
    @Expose
    @SerializedName("station")
    val station:station
)

data class station(
    @Expose
    @SerializedName("name")
    val name:String,
    @Expose
    @SerializedName("time")
    val time:String
)