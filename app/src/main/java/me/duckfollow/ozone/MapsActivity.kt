package me.duckfollow.ozone

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import me.duckfollow.ozone.activity.MainDetailsActivity
import me.duckfollow.ozone.model.WaqiLocation
import me.duckfollow.ozone.util.ApiConnection
import me.duckfollow.ozone.view.ViewLoading


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val url = "https://api.waqi.info/map/bounds/?latlng=39.379436,116.091230,40.235643,116.784382&token=demo"
        TaskDataLocation().execute(url)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        val clickCount = p0!!.tag.toString().toInt()
        val i = Intent(this,MainDetailsActivity::class.java)
        i.putExtra("lat",p0.position.latitude.toString())
        i.putExtra("lon",p0.position.longitude.toString())
        startActivity(i)
        return false
    }

    @SuppressLint("StaticFieldLeak")
    inner class TaskDataLocation:AsyncTask<String,String,String>(){
        val loading = ViewLoading(this@MapsActivity).create()
        override fun onPreExecute() {
            super.onPreExecute()
            loading.show()
        }
        override fun doInBackground(vararg params: String?): String? {
            return ApiConnection().getData(params[0].toString())
        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val gson = Gson()
            val dataLocation = gson.fromJson<WaqiLocation>(result,WaqiLocation::class.java)
            Log.d("data_res_location",dataLocation.status)

            for (i in 0..dataLocation.data.size-1){
               val marker = mMap.addMarker(
                                        MarkerOptions()
                                            .position(
                                                LatLng(
                                                    dataLocation.data[i].lat,
                                                    dataLocation.data[i].lon
                                                )
                                            )
                                            .icon(BitmapDescriptorFactory.fromBitmap(circleBitmap(dataLocation.data[i].aqi)))
                                        )
                marker.tag = i
            }

            mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(dataLocation.data[dataLocation.data.size-1].lat,dataLocation.data[dataLocation.data.size-1].lon)))
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        dataLocation.data[dataLocation.data.size-1].lat,
                        dataLocation.data[dataLocation.data.size-1].lon
                    ), 12.0f
                )
            )

            Handler().postDelayed(Runnable {
                loading.cancel()
            },4000)
        }

        @SuppressLint("ResourceAsColor")
        private fun circleBitmap(iaqi:String):Bitmap{
            // circle
            val air = iaqi.toInt()
            val diameter = 250
            val bm = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_4444)
            val canvas = Canvas(bm)
            val p = Paint()
            if(air > 200) {
                p.setColor(getResources().getColor(R.color.colorRed))
            }else {
                p.setColor(getResources().getColor(R.color.colorGreen))
            }
            canvas.drawCircle((diameter / 2).toFloat(), (diameter / 2).toFloat(),
                (diameter / 2).toFloat(), p)
            p.setColor(android.R.color.white);
            p.setTextSize(20.0F);
            canvas.drawText("text", (diameter/2).toFloat(), (diameter/2).toFloat(),p)

            return bm
        }
    }
}
