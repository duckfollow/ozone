package me.duckfollow.ozone.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlin.collections.ArrayList

data class WaqiInfo(
    @Expose
    @SerializedName("status")
    val status:String,
    @Expose
    @SerializedName("data")
    val data:data
)

data class data(
    @Expose
    @SerializedName("aqi")
    val aqi:String,
    @Expose
    @SerializedName("idx")
    val idx:String,
    @Expose
    @SerializedName("attributions")
    val attributions:ArrayList<attributions>,
    @Expose
    @SerializedName("city")
    val city:city,
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

data class attributions(
    @Expose
    @SerializedName("url")
    val url:String,
    @Expose
    @SerializedName("name")
    val name:String,
    @Expose
    @SerializedName("logo")
    val logo:String
)

data class city(
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

data class iaqi(
    @Expose
    @SerializedName("co")
    val co:iaqi_air,
    @Expose
    @SerializedName("no2")
    val no2:iaqi_air,
    @Expose
    @SerializedName("o3")
    val o3:iaqi_air,
    @Expose
    @SerializedName("p")
    val p:iaqi_air,
    @Expose
    @SerializedName("pm10")
    val pm10:iaqi_air,
    @Expose
    @SerializedName("pm25")
    val pm25:iaqi_air,
    @Expose
    @SerializedName("r")
    val r:iaqi_air,
    @Expose
    @SerializedName("so2")
    val so2:iaqi_air,
    @Expose
    @SerializedName("t")
    val t:iaqi_air,
    @Expose
    @SerializedName("w")
    val w:iaqi_air,
    @Expose
    @SerializedName("wg")
    val wg:iaqi_air
)

data class iaqi_air(
    @Expose
    @SerializedName("v")
    val v:String
)

data class time(
    @Expose
    @SerializedName("s")
    val s:String,
    @Expose
    @SerializedName("tz")
    val tz:String,
    @Expose
    @SerializedName("v")
    val v:String
)

data class debug(
    @Expose
    @SerializedName("sync")
    val sync:String
)