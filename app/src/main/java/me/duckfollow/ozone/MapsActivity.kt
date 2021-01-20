package me.duckfollow.ozone

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
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
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.android.synthetic.main.activity_maps.*
import me.duckfollow.ozone.activity.ErrorActivity
import me.duckfollow.ozone.activity.LocationMangerActivity
import me.duckfollow.ozone.activity.MainDetailsActivity
import me.duckfollow.ozone.activity.ProfileActivity
import me.duckfollow.ozone.adapter.LocationListAdapter
import me.duckfollow.ozone.model.AqiModel
import me.duckfollow.ozone.model.ListModel
import me.duckfollow.ozone.model.WaqiInfoGeo
import me.duckfollow.ozone.model.WaqiLocation
import me.duckfollow.ozone.service.MyNotification
import me.duckfollow.ozone.user.UserProfile
import me.duckfollow.ozone.util.ApiConnection
import me.duckfollow.ozone.utils.ConvertImagetoBase64
import me.duckfollow.ozone.view.ViewLoading
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.random.Random


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    LocationListener, GoogleMap.OnMarkerDragListener,GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var mMapLocation: GoogleMap
    private lateinit var googleApiClient: GoogleApiClient
    var mLat:Double = 13.773227
    var mLong:Double = 100.5689558
    lateinit var dataLocation:WaqiLocation
    lateinit var text_pm:TextView
    lateinit var txt_station:TextView
    lateinit var card_view:CardView
    lateinit var btn_show_location:Button
    lateinit var viewMap:View
    lateinit var btn_profile: ImageButton
    lateinit var text_pm_details:TextView
    lateinit var btn_menu:Button
    var dataList: ArrayList<ListModel> = ArrayList()

    lateinit var myRefUser: DatabaseReference
    lateinit var myRefLocation: DatabaseReference
    lateinit var myRefaddLocation: DatabaseReference
    lateinit var myRefNotification: DatabaseReference

    lateinit var manager: ReviewManager
    var reviewInfo: ReviewInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        initView()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        viewMap = mapFragment.view!!

        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()

        val url = "https://api.waqi.info/map/bounds/?latlng="/*+mLat+","+mLong+","*/+(mLat+1)+","+(mLong+1)+","+(mLat-1)+","+(mLong-1)+"&token=fe5f8a6aa99f6bfb397762a0cade98a6d78795a6"
        Log.d("app_url",url)
        TaskDataLocation(mLat,mLong).execute(url)

        btn_menu.setOnClickListener {
            Menu()
        }

        btn_show_location.setOnClickListener {
            myLocation()
        }

        btn_profile.setOnClickListener {
            val i_profile = Intent(this,ProfileActivity::class.java)
            val activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, btn_profile, "view_profile")
            startActivity(i_profile,activityOptionsCompat.toBundle())
        }
        val android_id = Settings.Secure.getString(this.getContentResolver(),
            Settings.Secure.ANDROID_ID);
        val database = FirebaseDatabase.getInstance().reference
        myRefUser = database.child("location/"+android_id+"/")
        myRefLocation = database.child("user/"+android_id+"/subscribe/")
        myRefNotification = database.child("user/"+android_id+"/notification/")
        myRefaddLocation = database.child("location/")

        try {
            val serviceIntent = Intent(this, MyNotification::class.java);
            serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
            ContextCompat.startForegroundService(this, serviceIntent);
        }catch (e:Exception){

        }

//        val manager = ReviewManagerFactory.create(applicationContext)
//        val manager = FakeReviewManager(applicationContext)

        initReviews()

    }


    // Call this method asap, for example in onCreate()
    private fun initReviews() {
//        manager = FakeReviewManager(applicationContext)
        manager = ReviewManagerFactory.create(this)
        manager.requestReviewFlow().addOnCompleteListener { request ->
            if (request.isSuccessful) {
                reviewInfo = request.result
            } else {
                // Log error
            }
        }
    }

    // Call this when you want to show the dialog
    private fun askForReview() {
        if (reviewInfo != null) {
            manager.launchReviewFlow(this, reviewInfo!!).addOnFailureListener {
                // Log error and continue with the flow
            }.addOnCompleteListener { _ ->
                // Log success and continue with the flow
            }
        }
    }


    fun showProfile() {
        TapTargetView.showFor(this,
            TapTarget.forView(btn_profile,"โปรไฟล์ของคุณ", "คุณสามารถแก้ไขรูป และแสกนเพิ่มเพื่อนได้").cancelable(false),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView) {
                    super.onTargetClick(view)
                    showMenuLocation()
                }
            })
    }

    fun showMenuLocation () {
        TapTargetView.showFor(this,
            TapTarget.forView(btn_show_location,"ตำแหน่งปัจจุบัน", "คุณสามารถดูตำแหน่งปัจจุบันของคุณได้ และสามารถลาก เพื่อย้ายตำแหน่ง").cancelable(false),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView) {
                    super.onTargetClick(view)
//                    showList()
                    showViewMap()
                }
            })
    }

    fun showList() {
        TapTargetView.showFor(this,
            TapTarget.forView(btn_menu,"ข้อมูลเพิ่มเติม", "คุณสามารถดูรายการทั้งหมดที่แสดงอยู่บนแผนที่ได้").cancelable(false),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView) {
                    super.onTargetClick(view)
                    showViewMap()
                }
            })
    }

    fun showViewMap(){
        TapTargetView.showFor(this,
            TapTarget.forView(viewMap,"คลิกที่หมุด", "คลิกเพื่อแสดงรายละเอียดต่างๆ").cancelable(false),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView) {
                    super.onTargetClick(view)
                    UserProfile(this@MapsActivity).setMapTutorial(false.toString())
                    val i = Intent(this@MapsActivity,MainDetailsActivity::class.java)
                    i.putExtra("lat","13.773227")
                    i.putExtra("lon","100.5689558")
                    startActivity(i)
                }
            })
    }

    private  fun initView(){
        text_pm = findViewById(R.id.text_pm)
        txt_station = findViewById(R.id.txt_station)
        card_view = findViewById(R.id.card_view)
        btn_show_location = findViewById(R.id.btn_show_location)
        btn_profile = findViewById(R.id.btn_profile)
        text_pm_details = findViewById(R.id.text_pm_details)
        btn_menu = findViewById(R.id.btn_menu)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMapLocation = googleMap
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMarkerDragListener(this)
        mMap.setOnMapClickListener(this)

        try {
            val view_compass = (viewMap.findViewById<View>(Integer.parseInt("1")).getParent() as View).findViewById<View>(Integer.parseInt("5"))
            val layoutParams = view_compass.layoutParams as RelativeLayout.LayoutParams
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 660, 30, 0); // 160 la truc y , 30 la  truc x
        }catch (e:java.lang.Exception){
            Log.e("error_view",e.toString())
        }

        val hashMapMarker = HashMap<String,Marker>();
        val addLocation = object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {

                val lat = p0.child("latitude").getValue().toString()
                val long = p0.child("longitude").getValue().toString()
                val img = p0.child("img").getValue().toString()
                UserProfile(this@MapsActivity).setImageBase64(img)

                try {
                    val markerRemove = hashMapMarker.get(p0.key.toString());
                    markerRemove!!.remove()
                    hashMapMarker.remove(p0.key.toString());
                }catch (e:java.lang.Exception){

                }

                try {
                    val b = ConvertImagetoBase64().base64ToBitmap(img)
                    val imgBitmap = getCroppedBitmap(ConvertImagetoBase64().getResizedBitmap(b,150,150))
                    val marker = mMapLocation.addMarker(
                        MarkerOptions()
                            .position(
                                LatLng(
                                    lat.toDouble(),
                                    long.toDouble()
                                )
                            )
                            .icon(BitmapDescriptorFactory.fromBitmap(imgBitmap))
                    )
                    marker.zIndex = 1F
                    hashMapMarker.put(p0.key.toString(),marker);
                    Log.d("key_event2",p0.key.toString())
                }catch (e:Exception){

                }

                Log.d("key_event",lat+"//"+long)
            }
        }

        val locationListener = object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val i = p0.children.iterator()
                while (i.hasNext()) {
                    val key = (i.next() as DataSnapshot).key
                    Log.d("key_event" ,key)

                    myRefaddLocation.child(key!!).addValueEventListener(addLocation)
                }
            }

        }

        myRefLocation.addValueEventListener(locationListener)
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        val i = Intent(this,MainDetailsActivity::class.java)
        i.putExtra("lat",p0!!.position.latitude.toString())
        i.putExtra("lon",p0.position.longitude.toString())
        i.putExtra("corona",p0.tag.toString())
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
            myLocation()
        }
        val adapter = LocationListAdapter(dataList)
        list_location.layoutManager = LinearLayoutManager(this)
        list_location.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        list_location.adapter = adapter

        bottomSheetDialogLoading.show()
    }

    private fun myLocation(){
        var img = user_marker()
        if(UserProfile(this).getImageBase64() != ""){
            val b = ConvertImagetoBase64().base64ToBitmap(UserProfile(this).getImageBase64())
            img = getCroppedBitmap(ConvertImagetoBase64().getResizedBitmap(b,150,150))
        }
        mMap.clear()
        val marker_user = mMap.addMarker(
            MarkerOptions()
                .position(
                    LatLng(
                        mLat,
                        mLong
                    )
                )
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView()))
        )
        marker_user.isDraggable = true
        marker_user.tag = "user_location"
        marker_user.zIndex = 1F
        val url = "https://api.waqi.info/map/bounds/?latlng="/*+mLat+","+mLong+","*/+(mLat+1)+","+(mLong+1)+","+(mLat-1)+","+(mLong-1)+"&token=fe5f8a6aa99f6bfb397762a0cade98a6d78795a6"
        TaskDataLocation(mLat,mLong).execute(url)
    }

fun getCroppedBitmap(bitmap:Bitmap):Bitmap {

    val output = Bitmap.createBitmap(bitmap.getWidth(),
            bitmap.getHeight(), Bitmap.Config.ARGB_8888);
    val canvas = Canvas(output);

    val color = getResources().getColor(R.color.colorPrimary)
    val paint = Paint();
    val rect = Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

    paint.setAntiAlias(true);
    canvas.drawARGB(0, 0, 0, 0);
    paint.setColor(color);
    // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
    canvas.drawCircle(
        (bitmap.getWidth() / 2).toFloat(), (bitmap.getHeight() / 2).toFloat(),
        (bitmap.getWidth() / 2).toFloat(), paint);
    paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    canvas.drawBitmap(bitmap, rect, rect, paint);
    //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
    //return _bmp;
    return output;
}

    private fun getMarkerBitmapFromView ():Bitmap {
        val customMarkerView = (getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.marker, null)
        val markerImageView = customMarkerView.findViewById<ImageView>(R.id.profile_image)
        var img = user_marker()
        if(UserProfile(this).getImageBase64() != ""){
            val b = ConvertImagetoBase64().base64ToBitmap(UserProfile(this).getImageBase64())
            img = getCroppedBitmap(ConvertImagetoBase64().getResizedBitmap(b,150,150))
        }
        markerImageView.setImageBitmap(img)
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        val returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
            Bitmap.Config.ARGB_8888);
        val canvas = Canvas(returnedBitmap);
        val drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap
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

    @SuppressLint("MissingPermission")
    override fun onConnected(p0: Bundle?) {
            val locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient)
            try {
                if (locationAvailability.isLocationAvailable) {
                    val locationRequest = LocationRequest.create().apply {
                        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        interval = 5000
                    }
                    LocationServices.FusedLocationApi.requestLocationUpdates(
                        googleApiClient,
                        locationRequest,
                        this
                    )
                } else {
                    // Do something when location provider not available
                }
            }catch (e:Exception){
                Handler().postDelayed(Runnable {
                    try {
                        val i = Intent(this,LocationMangerActivity::class.java)
                        startActivity(i)
                        this.finish()
                    }catch (e:java.lang.Exception){

                    }
                }, 4000)
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
        val url = "https://api.waqi.info/feed/geo:"+p0.latitude+";"+p0.longitude+"/?token=fe5f8a6aa99f6bfb397762a0cade98a6d78795a6"
        //Toast.makeText(this@MapsActivity,url,Toast.LENGTH_LONG).show()
        TaskDataRealTime().execute(url)

        var img_base64 = "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"
        if(UserProfile(this).getImageBase64() != ""){
            img_base64 = UserProfile(this).getImageBase64()
        }
        val map = HashMap<String, Any>()
        map.put("img",img_base64)
        map.put("latitude",p0.latitude)
        map.put("longitude",p0.longitude)
        myRefUser.updateChildren(map)

    }

    fun showReview(){
        if (UserProfile(this).getCountReview() == "5") {
            Handler().postDelayed(Runnable {
//                review()
                askForReview()
            },1000)
        }else {
            val count = UserProfile(this).getCountReview().toInt()+1
            UserProfile(this).setCountReview((count.toString()))
        }
    }

    fun review() {
        val mView = layoutInflater.inflate(R.layout.layout_review, null)
        val bottomSheetDialogLoading = BottomSheetDialog(this, R.style.BottomSheetDialog)
        bottomSheetDialogLoading.setContentView(mView)
        bottomSheetDialogLoading.setCancelable(false)

        val bottomSheet = bottomSheetDialogLoading.findViewById<View>(R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)
        behavior.peekHeight = Resources.getSystem().getDisplayMetrics().heightPixels* Resources.getSystem().displayMetrics.density.toInt()

        val btn_cancel = mView.findViewById<Button>(R.id.btn_cancel)
        btn_cancel.setOnClickListener {
            bottomSheetDialogLoading.cancel()
            UserProfile(this).setCountReview("0")
        }

        val btn_review = mView.findViewById<Button>(R.id.btn_review)
        btn_review.setOnClickListener {
            val uri = Uri.parse("https://play.google.com/store/apps/details?id=me.duckfollow.ozone"); // missing 'http://' will cause crashed
            val intent = Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            UserProfile(this).setCountReview("99")
            bottomSheetDialogLoading.cancel()
        }
        bottomSheetDialogLoading.show()
    }

    @SuppressLint("StaticFieldLeak")
    inner class TaskDataLocation(val lat:Double,val lon:Double):AsyncTask<String,String,String>(){
        val loading = ViewLoading(this@MapsActivity).create()
        override fun onPreExecute() {
            super.onPreExecute()
            loading.show()
            dataList.clear()
        }
        override fun doInBackground(vararg params: String?): String? {
            return ApiConnection().getData(params[0].toString())
        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val json = JSONObject(result)
                val dataLocation = JSONArray(json.getString("data"))

                for (i in 0..dataLocation.length() - 1) {
                    val dataJSON = JSONObject(dataLocation[i].toString())
                    val lat = dataJSON.getDouble("lat")
                    val lon = dataJSON.getDouble("lon")
                    val aqi = dataJSON.getString("aqi")
                    val station = JSONObject(dataJSON.getString("station"))
                    val name = station.getString("name")
                    try {
                        dataList.add(
                            ListModel(
                                name,
                                aqi
                            )
                        )
                    }catch (e:Exception){

                    }
                    var width = 250 + (0..150).random()
                    val marker = mMap.addMarker(
                        MarkerOptions()
                            .position(
                                LatLng(
                                    lat,
                                    lon
                                )
                            )
                            .icon(
                                BitmapDescriptorFactory.fromBitmap(
                                    createImage(
                                        width,
                                        width,
                                        aqi
                                    )
                                )
                            )
                    )
                    marker.tag = i

                }

                mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(lat, lon)))
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            lat,
                            lon
                        ), 12.0f
                    )
                )

                Handler().postDelayed(Runnable {
                    try {
                        loading.cancel()
                    }catch (e:java.lang.Exception){

                    }
                    if(UserProfile(this@MapsActivity).getMapTutorial().toBoolean()){
                        showProfile()
                    }
                    showReview()
                }, 3500)
            }catch (e:Exception){
                loading.cancel()
                errorCodeCheck("dataError")
            }
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
                canvas.drawText(name!!, xPos.toFloat(), yPos.toFloat(), paint)
                return bitmap
        }
    }

    override fun onMarkerDragEnd(p0: Marker?) {
        var img = user_marker()
        if(UserProfile(this).getImageBase64() != ""){
            val b = ConvertImagetoBase64().base64ToBitmap(UserProfile(this).getImageBase64())
            img = getCroppedBitmap(ConvertImagetoBase64().getResizedBitmap(b,150,150))
        }
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
                    .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView()))
            )
            marker_user.isDraggable = true
            marker_user.tag = "user_location"
            marker_user.zIndex = 1F
            val url =
                "https://api.waqi.info/map/bounds/?latlng=" /*+ p0.position.latitude + "," + p0.position.longitude + "," */+ (p0.position.latitude + 1) + "," + (p0.position.longitude + 1)+ "," + (p0.position.latitude - 1) + "," + (p0.position.longitude - 1) + "&token=fe5f8a6aa99f6bfb397762a0cade98a6d78795a6"
            TaskDataLocation(mLat,mLong).execute(url)
    }

    override fun onMarkerDragStart(p0: Marker?) {

    }

    override fun onMarkerDrag(p0: Marker?) {

    }

    override fun onMapClick(p0: LatLng?) {
        var img = user_marker()
        if(UserProfile(this).getImageBase64() != ""){
            val b = ConvertImagetoBase64().base64ToBitmap(UserProfile(this).getImageBase64())
            img = getCroppedBitmap(ConvertImagetoBase64().getResizedBitmap(b,150,150))
        }

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
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView()))
        )
        marker_user.isDraggable = true
        marker_user.tag = "user_location"
        marker_user.zIndex = 1F
        val url = "https://api.waqi.info/map/bounds/?latlng="/*+p0.latitude+","+p0.longitude+","*/+(p0.latitude+1)+","+(p0.longitude+1)+","+(p0.latitude-1)+","+(p0.longitude-1)+"&token=fe5f8a6aa99f6bfb397762a0cade98a6d78795a6"
        TaskDataLocation(mLat,mLong).execute(url)
    }

    fun user_marker():Bitmap{
        val height = 250
        val width = 250
        val bitmapdraw = resources.getDrawable(R.drawable.marker_icon) as BitmapDrawable
        val b = bitmapdraw.getBitmap()
        val smallMarker = Bitmap.createScaledBitmap(b, width, height, false)
        return smallMarker
    }

    private fun errorCodeCheck(type:String){
        val i = Intent(this,ErrorActivity::class.java)
        startActivity(i)
        this.finish()
    }

    inner class TaskDataRealTime:AsyncTask<String,String,String>(){
        override fun doInBackground(vararg params: String?): String? {
            return ApiConnection().getData(params[0].toString())
        }

        @SuppressLint("ResourceAsColor")
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val json = JSONObject(result)
                val status = json.getString("status")
                val data = JSONObject(json.getString("data"))
                val city = JSONObject(data.getString("city"))
                val name = city.getString("name")
                txt_station.text = name

                try {
                    val iaqi = JSONObject(data.getString("iaqi"))
                    val pm25 = JSONObject(iaqi.getString("pm25"))
                    val v = pm25.getString("v")
                    text_pm.text = v

                    try {
                        val aqi = v.toInt()
                        if (aqi <= 50) {
                            text_pm.setTextColor(resources.getColor(R.color.green))
                            text_pm_details.text = "ดี"
                            text_pm_details.setTextColor(resources.getColor(R.color.green))
                        }else if (aqi>50 && aqi <=100) {
                            text_pm.setTextColor(resources.getColor(R.color.yellow))
                            text_pm_details.text = "ปานกลาง"
                            text_pm_details.setTextColor(resources.getColor(R.color.yellow))
                        }else if (aqi > 100 && aqi <= 150) {
                            text_pm.setTextColor(resources.getColor(R.color.orange))
                            text_pm_details.text = "ไม่ดีต่อสุขภาพผู้ป่วยภูมิแพ้"
                            text_pm_details.setTextColor(resources.getColor(R.color.orange))
                        }else if (aqi > 150 && aqi < 200) {
                            text_pm.setTextColor(resources.getColor(R.color.red))
                            text_pm_details.text = "ไม่ดีต่อสุขภาพ"
                            text_pm_details.setTextColor(resources.getColor(R.color.red))
                        }else if (aqi < 200 && aqi <= 300) {
                            text_pm.setTextColor(resources.getColor(R.color.violet))
                            text_pm_details.text = "ไม่ดีต่อสุขภาพมาก"
                            text_pm_details.setTextColor(resources.getColor(R.color.violet))
                        } else {
                            text_pm.setTextColor(resources.getColor(R.color.super_red))
                            text_pm_details.text = "อันตราย"
                            text_pm_details.setTextColor(resources.getColor(R.color.super_red))
                        }
                    }catch (e:Exception) {

                    }

                    val data_notification = HashMap<String,Any>()
                    data_notification.put("station",name)
                    data_notification.put("text",v)

                    myRefNotification.updateChildren(data_notification)

                }catch (e:Exception){

                }

            }catch (e:Exception){
                //Toast.makeText(this@MapsActivity,e.toString(),Toast.LENGTH_LONG).show()
            }
        }
    }
}
