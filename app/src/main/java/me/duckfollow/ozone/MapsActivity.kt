package me.duckfollow.ozone

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import me.duckfollow.ozone.activity.MainDetailsActivity
import me.duckfollow.ozone.adapter.LocationListAdapter
import me.duckfollow.ozone.model.WaqiLocation
import me.duckfollow.ozone.util.ApiConnection
import me.duckfollow.ozone.view.ViewLoading


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    LocationListener, GoogleMap.OnMarkerDragListener,GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var googleApiClient: GoogleApiClient
    var mLat:Double = 13.773227
    var mLong:Double = 100.5689558
    lateinit var dataLocation:WaqiLocation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()

        val url = "https://api.waqi.info/map/bounds/?latlng=13.773227,100.5689558,14.773227,101.5689558&token=fe5f8a6aa99f6bfb397762a0cade98a6d78795a6"
        TaskDataLocation().execute(url)

        val btn_menu = findViewById<Button>(R.id.btn_menu)
        btn_menu.setOnClickListener {
            Menu()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMarkerDragListener(this)
        mMap.setOnMapClickListener(this)
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        val i = Intent(this,MainDetailsActivity::class.java)
        i.putExtra("lat",p0!!.position.latitude.toString())
        i.putExtra("lon",p0.position.longitude.toString())
        startActivity(i)
        return false
    }

    private fun Menu(){
        val mView = layoutInflater.inflate(R.layout.layout_menu, null)
        val bottomSheetDialogLoading = BottomSheetDialog(this, R.style.BottomSheetDialog)
        bottomSheetDialogLoading.setContentView(mView)
//        val bottomSheet = bottomSheetDialogLoading.findViewById<View>(R.id.design_bottom_sheet)
//        val behavior = BottomSheetBehavior.from(bottomSheet)
//        behavior.peekHeight = Resources.getSystem().getDisplayMetrics().heightPixels* Resources.getSystem().displayMetrics.density.toInt()

        val list_location = mView.findViewById<RecyclerView>(R.id.list_location)
        val btn_location = mView.findViewById<Button>(R.id.btn_location)
        btn_location.setOnClickListener {
            bottomSheetDialogLoading.cancel()
            mMap.clear()
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(mLat,mLong)))
//            mMap.animateCamera(
//                CameraUpdateFactory.newLatLngZoom(
//                    LatLng(
//                        mLat,
//                        mLong
//                    ), 12.0f
//                )
//            )
            val marker_user = mMap.addMarker(
                                                MarkerOptions()
                                                    .position(
                                                        LatLng(
                                                            mLat,
                                                            mLong
                                                        )
                                                    )
                                                    .icon(BitmapDescriptorFactory.fromBitmap(user_marker()))
                                            )
            marker_user.isDraggable = true
            marker_user.tag = "user_location"
            val url = "https://api.waqi.info/map/bounds/?latlng="+mLat+","+mLong+","+(mLat+1)+","+(mLong+1)+"&token=fe5f8a6aa99f6bfb397762a0cade98a6d78795a6"
            TaskDataLocation().execute(url)
        }
        val adapter = LocationListAdapter(dataLocation.data)
        list_location.layoutManager = LinearLayoutManager(this)
        list_location.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        list_location.adapter = adapter

        bottomSheetDialogLoading.show()
    }

    override fun onStart() {
        super.onStart()
        googleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()
        if (googleApiClient.isConnected) {
            googleApiClient.disconnect()
        }
    }

    override fun onConnected(p0: Bundle?) {
        val locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient)
        if(locationAvailability.isLocationAvailable) {
            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 5000
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this)
        } else {
            // Do something when location provider not available
        }
    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    override fun onLocationChanged(p0: Location?) {
        Log.d("location_app",p0!!.latitude.toString()+"//"+p0.longitude)
        mLat = p0.latitude
        mLong = p0.longitude
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
            dataLocation = gson.fromJson<WaqiLocation>(result,WaqiLocation::class.java)
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
                                            .icon(BitmapDescriptorFactory.fromBitmap(createImage(250,250,dataLocation.data[i].aqi)))
                                        )
                marker.tag = i

//                val marker2 = mMap.addGroundOverlay(GroundOverlayOptions()
//                    .image(BitmapDescriptorFactory.fromBitmap(createImage(2000,2000,dataLocation.data[i].aqi)))
//                    .position(LatLng(
//                        dataLocation.data[i].lat,
//                        dataLocation.data[i].lon
//                    ),2000f))
//                marker2.tag = i

            }

            mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(mLat,mLong)))
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        mLat,
                        mLong
                    ), 12.0f
                )
            )

            Handler().postDelayed(Runnable {
                loading.cancel()
            },3500)
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

        fun createImage(width: Int, height: Int, name: String?): Bitmap? {
            var aqi = 0
            try {
                aqi = name!!.toInt()
            }catch (e:Exception){

            }
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                val paint2 = Paint()

                if (aqi <= 50) {
                    paint2.color = getResources().getColor(R.color.colorGreen)
                } else if (aqi <= 100) {
                    paint2.color = getResources().getColor(R.color.colorYellow)
                } else if (aqi <= 150) {
                    paint2.color = getResources().getColor(R.color.colorOrange)
                } else if (aqi <= 200) {
                    paint2.color = getResources().getColor(R.color.colorPink)
                } else if (aqi <= 300) {
                    paint2.color = getResources().getColor(R.color.colorViolet)
                } else {
                    paint2.color = getResources().getColor(R.color.colorRed)
                }

                canvas.drawCircle(
                    width.toFloat() / 2,
                    height.toFloat() / 2,
                    height.toFloat() / 2,
                    paint2
                )
                val paint = Paint()
                paint.color = Color.WHITE
                paint.textSize = 72f
                paint.textScaleX = 1f
                val xPos = (canvas.width / 4)
                val yPos = (canvas.height / 2) + 30
                canvas.drawText(name, xPos.toFloat(), yPos.toFloat(), paint)
                return bitmap
        }
    }

    override fun onMarkerDragEnd(p0: Marker?) {
        try {
            mMap.clear()
            mLat = p0!!.position.latitude
            mLong = p0.position.longitude
            val marker_user = mMap.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            p0.position.latitude,
                            p0.position.longitude
                        )
                    )
                    .icon(BitmapDescriptorFactory.fromBitmap(user_marker()))
            )
            marker_user.isDraggable = true
            marker_user.tag = "user_location"
            val url =
                "https://api.waqi.info/map/bounds/?latlng=" + p0.position.latitude + "," + p0.position.longitude + "," + (p0.position.latitude + 1) + "," + (p0.position.longitude + 1) + "&token=fe5f8a6aa99f6bfb397762a0cade98a6d78795a6"
            TaskDataLocation().execute(url)
        }catch (e:Exception){

        }
    }

    override fun onMarkerDragStart(p0: Marker?) {

    }

    override fun onMarkerDrag(p0: Marker?) {

    }

    override fun onMapClick(p0: LatLng?) {
        mMap.clear()
        mLat = p0!!.latitude
        mLong = p0.longitude
        val marker_user = mMap.addMarker(
            MarkerOptions()
                .position(
                    LatLng(
                        p0.latitude,
                        p0.longitude
                    )
                )
                .icon(BitmapDescriptorFactory.fromBitmap(user_marker()))
        )
        marker_user.isDraggable = true
        marker_user.tag = "user_location"
        val url = "https://api.waqi.info/map/bounds/?latlng="+p0.latitude+","+p0.longitude+","+(p0.latitude+1)+","+(p0.longitude+1)+"&token=fe5f8a6aa99f6bfb397762a0cade98a6d78795a6"
        TaskDataLocation().execute(url)
    }

    fun user_marker():Bitmap{
        val height = 250
        val width = 250
        val bitmapdraw = resources.getDrawable(R.drawable.marker_icon) as BitmapDrawable
        val b = bitmapdraw.getBitmap()
        val smallMarker = Bitmap.createScaledBitmap(b, width, height, false)
        return smallMarker
    }
}
