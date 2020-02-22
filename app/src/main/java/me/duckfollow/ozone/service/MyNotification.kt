package me.duckfollow.ozone.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.*
import android.provider.Settings
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.*
import me.duckfollow.ozone.MapsActivity
import me.duckfollow.ozone.R
import me.duckfollow.ozone.activity.LocationMangerActivity
import me.duckfollow.ozone.user.UserProfile
import me.duckfollow.ozone.util.ApiConnection
import me.duckfollow.ozone.utils.ConvertImagetoBase64
import org.json.JSONObject


class MyNotification : Service (), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {
    lateinit var myRefNotification: DatabaseReference
    private lateinit var googleApiClient: GoogleApiClient

    override fun onBind(intent: Intent?): IBinder? {
       return null
    }

    override fun onCreate() {
        super.onCreate()
        val database = FirebaseDatabase.getInstance().reference
        Log.d("noti_test",database.toString())
        val android_id = Settings.Secure.getString(this.getContentResolver(),
            Settings.Secure.ANDROID_ID);
        Log.d("noti_test",android_id)
        myRefNotification = database.child("user/"+android_id+"/notification/")

        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()

        googleApiClient.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (googleApiClient.isConnected) {
            googleApiClient.disconnect()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            noti()
        }catch (e:Exception){

        }
        return START_NOT_STICKY
    }

    fun noti(){
        val notification_listener = object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                val text =  p0.child("text").getValue().toString()
                val station = p0.child("station").getValue().toString()

                val collapsed = RemoteViews(packageName,R.layout.notification_collapsed)
                val expended = RemoteViews(packageName,R.layout.notification_expended)

                collapsed.setTextViewText(R.id.text_pm,text)
                collapsed.setTextViewText(R.id.text_station,station)

                expended.setTextViewText(R.id.text_pm,text)
                expended.setTextViewText(R.id.txt_station,station)
                try {
                    expended.setImageViewBitmap(R.id.img_profile,ConvertImagetoBase64().base64ToBitmap(UserProfile(this@MyNotification).getImageBase64()))
                }catch (e:Exception){

                }

                try {
                    val aqi = text.toInt()
                    if (aqi <= 50) {
                        collapsed.setTextColor(R.id.text_pm,resources.getColor(R.color.green))
                        collapsed.setTextViewText(R.id.text_pm_details,"ดี")
                        collapsed.setTextColor(R.id.text_pm_details,resources.getColor(R.color.green))
                        expended.setTextViewText(R.id.text_pm_details,"ดี")
                        expended.setTextColor(R.id.text_pm,resources.getColor(R.color.green))
                        expended.setTextColor(R.id.text_pm_details,resources.getColor(R.color.green))
                    }else if (aqi>50 && aqi <=100) {
                        collapsed.setTextColor(R.id.text_pm,resources.getColor(R.color.yellow))
                        collapsed.setTextViewText(R.id.text_pm_details,"ปานกลาง")
                        collapsed.setTextColor(R.id.text_pm_details,resources.getColor(R.color.yellow))
                        expended.setTextViewText(R.id.text_pm_details,"ปานกลาง")
                        expended.setTextColor(R.id.text_pm,resources.getColor(R.color.yellow))
                        expended.setTextColor(R.id.text_pm_details,resources.getColor(R.color.yellow))
                    }else if (aqi > 100 && aqi <= 150) {
                        collapsed.setTextColor(R.id.text_pm,resources.getColor(R.color.orange))
                        collapsed.setTextViewText(R.id.text_pm_details,"ไม่ดีต่อสุขภาพผู้ป่วยภูมิแพ้")
                        collapsed.setTextColor(R.id.text_pm_details,resources.getColor(R.color.orange))
                        expended.setTextViewText(R.id.text_pm_details,"ไม่ดีต่อสุขภาพผู้ป่วยภูมิแพ้")
                        expended.setTextColor(R.id.text_pm,resources.getColor(R.color.orange))
                        expended.setTextColor(R.id.text_pm_details,resources.getColor(R.color.orange))
                    }else if (aqi > 150 && aqi < 200) {
                        collapsed.setTextColor(R.id.text_pm,resources.getColor(R.color.red))
                        collapsed.setTextViewText(R.id.text_pm_details,"ไม่ดีต่อสุขภาพ")
                        collapsed.setTextColor(R.id.text_pm_details,resources.getColor(R.color.red))
                        expended.setTextViewText(R.id.text_pm_details,"ไม่ดีต่อสุขภาพ")
                        expended.setTextColor(R.id.text_pm,resources.getColor(R.color.red))
                        expended.setTextColor(R.id.text_pm_details,resources.getColor(R.color.red))
                    }else if (aqi < 200 && aqi <= 300) {
                        collapsed.setTextColor(R.id.text_pm,resources.getColor(R.color.violet))
                        collapsed.setTextViewText(R.id.text_pm_details,"ไม่ดีต่อสุขภาพมาก")
                        collapsed.setTextColor(R.id.text_pm_details,resources.getColor(R.color.violet))
                        expended.setTextViewText(R.id.text_pm_details,"ไม่ดีต่อสุขภาพมาก")
                        expended.setTextColor(R.id.text_pm,resources.getColor(R.color.violet))
                        expended.setTextColor(R.id.text_pm_details,resources.getColor(R.color.violet))
                    } else {
                        collapsed.setTextColor(R.id.text_pm,resources.getColor(R.color.super_red))
                        collapsed.setTextViewText(R.id.text_pm_details,"อันตราย")
                        collapsed.setTextColor(R.id.text_pm_details,resources.getColor(R.color.super_red))
                        expended.setTextViewText(R.id.text_pm_details,"อันตราย")
                        expended.setTextColor(R.id.text_pm,resources.getColor(R.color.super_red))
                        expended.setTextColor(R.id.text_pm_details,resources.getColor(R.color.super_red))
                    }
                }catch (e:Exception) {

                }
                createNotificationChannel()
                val notificationIntent = Intent(this@MyNotification, MapsActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    this@MyNotification,
                    0, notificationIntent, 0
                )
                val notification =
                    NotificationCompat.Builder(this@MyNotification, "ozone101")
                       .setContentTitle("Foreground Service")
//                        .setContentText(text)
                        .setSmallIcon(R.drawable.marker_icon)
                        .setCustomContentView(collapsed)
                        .setCustomBigContentView(expended)
                        .setContentIntent(pendingIntent)
                        .build()
                startForeground(1, notification)
            }
        }

        myRefNotification.addValueEventListener(notification_listener)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "ozone101",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onLocationChanged(p0: Location?) {
        Log.d("location_app_test",p0!!.latitude.toString()+"//"+p0.longitude)
        val mLat = p0.latitude
        val mLong = p0.longitude
        val url = "https://api.waqi.info/feed/geo:"+p0.latitude+";"+p0.longitude+"/?token=fe5f8a6aa99f6bfb397762a0cade98a6d78795a6"
        //Toast.makeText(this@MapsActivity,url,Toast.LENGTH_LONG).show()
        TaskDataRealTimeNoti().execute(url)
    }

    override fun onConnected(p0: Bundle?) {
        val locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient)
        try {
            if (locationAvailability.isLocationAvailable) {
                val locationRequest = LocationRequest.create().apply {
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    interval = 50000
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

        }
    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    inner class TaskDataRealTimeNoti: AsyncTask<String, String, String>(){
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

                try {
                    val iaqi = JSONObject(data.getString("iaqi"))
                    val pm25 = JSONObject(iaqi.getString("pm25"))
                    val v = pm25.getString("v")

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