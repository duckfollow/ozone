package me.duckfollow.ozone.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class WaqiInfoGeo(
    @Expose
    @SerializedName("status")
    val status:String,
    @Expose
    @SerializedName("data")
    val data:dataGeo
)

data class dataGeo(
    @Expose
    @SerializedName("aqi")
    val aqi:String,
    @Expose
    @SerializedName("idx")
    val idx:String,
    @Expose
    @SerializedName("attributions")
    val attributions:ArrayList<attributionsGeo>,
    @Expose
    @SerializedName("city")
    val city:cityGeo,
    @Expose
    @SerializedName("dominentpol")
    val dominentpol:String,
    @Expose
    @SerializedName("iaqi")
    val iaqi:iaqi,
    @Expose
    @SerializedName("time")
    val time:time,
    @Expose
    @SerializedName("debug")
    val debug:debug
)

data class attributionsGeo(
    @Expose
    @SerializedName("url")
    val url:String,
    @Expose
    @SerializedName("name")
    val name:String
)

data class cityGeo(
    @Expose
    @SerializedName("geo")
    val geo:Array<Double>,
    @Expose
    @SerializedName("name")
    val name:String,
    @Expose
    @SerializedName("url")
    val url:String
)