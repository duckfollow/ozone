package me.duckfollow.ozone.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main_details.*
import me.duckfollow.ozone.R
import me.duckfollow.ozone.`interface`.GraphViewInterface
import me.duckfollow.ozone.adapter.AqiListAdapter
import me.duckfollow.ozone.adapter.GraphViewAdapter
import me.duckfollow.ozone.model.AqiModel
import me.duckfollow.ozone.model.GraphViewModel
import me.duckfollow.ozone.user.UserProfile
import me.duckfollow.ozone.util.ApiConnection
import me.duckfollow.ozone.utils.ConvertImagetoBase64
import me.duckfollow.ozone.view.ArcProgress
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainDetailsActivity : AppCompatActivity(),GraphViewInterface {

    lateinit var textViewCityName:TextView
    lateinit var txtViewIaqi:TextView
    lateinit var list_iaqi:RecyclerView
    lateinit var adapter:AqiListAdapter
    lateinit var data:ArrayList<AqiModel>
    lateinit var btn_back:ImageButton
    lateinit var shimmer_view_container:ShimmerFrameLayout
    lateinit var scroll_view:NestedScrollView
    lateinit var txt_view_quality:TextView
    lateinit var btn_shared:Button
    private lateinit var myRootView:View
//    lateinit var text_pm_details:TextView
    lateinit var progress:ArcProgress
    lateinit var text_details:TextView
    lateinit var text_country:TextView

    private val STORAGE_PERMISSION = 3

    var ADMOB_AD_UNIT_ID = ""
    var currentNativeAd: UnifiedNativeAd? = null

    lateinit var myRefGraph: DatabaseReference

    var key_data = ""

    var dataGraph = ArrayList<GraphViewModel>()
    lateinit var adapterGraph:GraphViewAdapter

    lateinit var txt_aqi_graph:TextView
    lateinit var txt_date_graph:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_details)
        initView()
        ADMOB_AD_UNIT_ID = resources.getString(R.string.ad_mob_key)

        val location_data = intent.extras
        val url = "https://api.waqi.info/feed/geo:"+ location_data!!.getString("lat")+";"+location_data.getString(
            "lon"
        )+"/?token=fe5f8a6aa99f6bfb397762a0cade98a6d78795a6"
        TaskData().execute(url)
        Log.d("data_res_url", url)

        val database = FirebaseDatabase.getInstance().reference
        myRefGraph = database.child("graph/")

        btn_back.setOnClickListener {
            this.finish()
        }

        btn_shared.setOnClickListener {

            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION
                )

            }else {
                shared()
            }
        }

        val img_base64 = UserProfile(this).getImageBase64()
        if(img_base64 != "") {
            val b = ConvertImagetoBase64().base64ToBitmap(img_base64)
//            img_profile_share.setImageBitmap(getCroppedBitmap(b))
        }

        MobileAds.initialize(application, OnInitializationCompleteListener {
        })

        refreshAd()

        val graphView = findViewById<RecyclerView>(R.id.graph_view)


        adapterGraph = GraphViewAdapter(dataGraph,this)
        graphView.adapter = adapterGraph

        graphView.layoutManager = LinearLayoutManager(this@MainDetailsActivity)
        graphView.layoutManager = LinearLayoutManager(
            this@MainDetailsActivity,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        graphView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING)
        val snapHelper = LinearSnapHelper() // Or PagerSnapHelper
        snapHelper.attachToRecyclerView(graphView)

    }

    fun getCroppedBitmap(bitmap: Bitmap):Bitmap {

        val output = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)

        val color = resources.getColor(R.color.colorPrimary)
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(
            (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
            (bitmap.width / 2).toFloat(), paint
        )
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output
    }

    private fun shared(){
//        val b = getImageUri(getBitmapFromView(root_view))
//        val sendIntent: Intent = Intent().apply {
//            action = Intent.ACTION_SEND
//            putExtra(Intent.EXTRA_STREAM, b)
//            type = "image/*"
//        }
//
//        val shareIntent = Intent.createChooser(sendIntent, "OzoneNotIncluded_Share")
//        startActivity(shareIntent)
        val i_shared = Intent(this, SharedActivity::class.java)
        i_shared.putExtra("aqi", txtViewIaqi.text)
        i_shared.putExtra("city", textViewCityName.text)
        startActivity(i_shared)
    }

    fun getImageUri(inImage: Bitmap): Uri {
          val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
              this.contentResolver,
              inImage,
              "OzoneNotIncluded_Share",
              null
          )
        return Uri.parse(path)
    }

    private fun initView(){
        textViewCityName = findViewById(R.id.textViewCityName)
        txtViewIaqi = findViewById(R.id.txtViewIaqi)
        list_iaqi = findViewById(R.id.list_iaqi)
        btn_back = findViewById(R.id.btn_back)
        shimmer_view_container = findViewById(R.id.shimmer_view_container)
        shimmer_view_container.visibility = View.VISIBLE
        scroll_view = findViewById(R.id.scroll_view)
        scroll_view.visibility = View.GONE
        txt_view_quality = findViewById(R.id.txt_view_quality)
        btn_shared = findViewById(R.id.btn_shared)
//        img_profile_share = findViewById(R.id.img_profile_share)
//        text_pm_details = findViewById(R.id.text_pm_details)
        progress = findViewById(R.id.progress)
        text_details = findViewById(R.id.text_details)

        list_iaqi.layoutManager = LinearLayoutManager(this)
        list_iaqi.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        data = ArrayList()
        adapter = AqiListAdapter(data)
        list_iaqi.adapter = adapter
        list_iaqi.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING)
        val snapHelper = LinearSnapHelper() // Or PagerSnapHelper
        snapHelper.attachToRecyclerView(list_iaqi)

        txt_aqi_graph = findViewById<TextView>(R.id.txt_aqi_graph)
        txt_date_graph = findViewById<TextView>(R.id.txt_date_graph)
    }

    fun showShared(){
        TapTargetView.showFor(this,
            TapTarget.forView(btn_shared, "ปุ่มแชร์", "คุณสามารถแชร์ไปสตอรี่ของคุณได้").cancelable(
                false
            ),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView) {
                    super.onTargetClick(view)
                    UserProfile(this@MainDetailsActivity).setMainDetailsTutorial(false.toString())
                    if (ContextCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this@MainDetailsActivity,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION
                        )
                    } else {
                        shared()
                    }
                }
            })
    }

    fun img_shared(): Bitmap {
        val height = 680
        val width = 480
//        val bitmapdraw = resources.getDrawable(R.drawable.marker_icon) as BitmapDrawable
//        val b = bitmapdraw.getBitmap()
//        val smallMarker = Bitmap.createScaledBitmap(b, width, height, false)
//        return smallMarker
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint2 = Paint()
        paint2.color = resources.getColor(R.color.colorGreen)
//        canvas.drawCircle(
//            width.toFloat()/2,
//            height.toFloat()/2,
//            10f,
//            paint2
//        )
        canvas.drawRect(100f, 100f, 200f, 200f, paint2)

        return bitmap
    }

    private fun getBitmapFromView(view: View):Bitmap {
        view.layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        val dm = this.resources.displayMetrics
        view.measure(
                View.MeasureSpec.makeMeasureSpec(
                    dm.widthPixels,
                    View.MeasureSpec.EXACTLY
                ),
                View.MeasureSpec.makeMeasureSpec(
                    dm.heightPixels,
                    View.MeasureSpec.EXACTLY
                )
            )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        view.layout(view.left, view.top, view.right, view.bottom)
        view.draw(canvas)
        return bitmap
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == STORAGE_PERMISSION){
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                shared()
            } else {

            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    inner class TaskData:AsyncTask<String, String, String>(){
        override fun onPreExecute() {
            super.onPreExecute()
            data.clear()
            shimmer_view_container.startShimmer()
        }
        override fun doInBackground(vararg params: String?): String? {
            return ApiConnection().getData(params[0].toString())
        }
        @SuppressLint("ResourceAsColor")
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)


            try {
                val json = JSONObject(result)
                val status = json.getString("status")
                val dataJSON = JSONObject(json.getString("data"))
                val city = JSONObject(dataJSON.getString("city"))
                val name = city.getString("name")
                val iaqi = JSONObject(dataJSON.getString("iaqi"))

                var pm2Text = "0"

                try {
                    val co = JSONObject(iaqi.getString("co"))
                    val v = co.getString("v")
                    data.add(AqiModel("co", v))
                } catch (e: Exception) {

                }
                try {
                    val no2 = JSONObject(iaqi.getString("no2"))
                    val v = no2.getString("v")
                    data.add(AqiModel("no2", v))
                } catch (e: Exception) {

                }
                try {
                    val o3 = JSONObject(iaqi.getString("o3"))
                    val v = o3.getString("v")
                    data.add(AqiModel("o3", v))
                } catch (e: Exception) {

                }
                try {
                    val pm10 = JSONObject(iaqi.getString("pm10"))
                    val v = pm10.getString("v")
                    data.add(AqiModel("pm10", v))
                } catch (e: Exception) {

                }

                try {
                    val pm25 = JSONObject(iaqi.getString("pm25"))
                    val v = pm25.getString("v")
                    pm2Text = v
                    data.add(AqiModel("pm25", v))
                    txtViewIaqi.text = v
//                    text_pm_shared.text = v
                    progress.textShow = v
                    try {
                        val aqi = v.toInt()
                        progress.progress = if (aqi > 500) {
                            100
                        }else {
                            aqi / 5
                        }

                        val dataAQI = MainDetailsActivity().getDataAQI(aqi)

                        txt_view_quality.text = dataAQI["txt_quality"].toString()
                        text_details.text = dataAQI["txt_detail"].toString()
                        text_details.setTextColor(dataAQI["color_aqi"].toString().toInt())

                    }catch (e: Exception){

                    }
                }catch (e: Exception){

                }


                try {
                    val so2 = JSONObject(iaqi.getString("so2"))
                    val v = so2.getString("v")
                    data.add(AqiModel("so2", v))
                } catch (e: Exception) {

                }

                adapter.notifyDataSetChanged()

                textViewCityName.text = name

                Log.d("testName", name.replace(",", "").replace(" ", ""))

                key_data = name.replace(",", "").replace(" ", "")


                val current = LocalDateTime.now()

                val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                val formatted = current.format(formatter)

                val formatter2 = DateTimeFormatter.ofPattern("MM/yyyy")
                val formatted2 = current.format(formatter2)

                val now = Date()
                val timestamp = now.time

                val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                val dateFormatted = current.format(dateFormat)

                val data_graph = HashMap<String, Any>()
                data_graph["date"] = formatted2
                data_graph["text"] = pm2Text
                data_graph["timestamp"] = timestamp
                data_graph["datestamp"] = dateFormatted

                myRefGraph.child("$key_data/$formatted$pm2Text").updateChildren(data_graph)


                val GraphListener = object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        dataGraph.clear()
                        val i = p0.children.iterator()
                        while (i.hasNext()) {
                            val i_data = (i.next() as DataSnapshot)
                            val date = i_data.child("date").value.toString()
                            val timestamp = i_data.child("timestamp").value.toString()
                            val datestamp = i_data.child("datestamp").value.toString()
                            val aqi = i_data.child("text").value.toString().toInt()
                            val color = when {
                                aqi <= 50 -> {
                                    "#4caf50"
                                }
                                aqi <= 100 -> {
                                    "#ffeb3b"
                                }
                                aqi <= 150 -> {
                                    "#ffc107"
                                }
                                aqi <= 200 -> {
                                    "#f44336"
                                }
                                aqi <= 300 -> {
                                    "#9c27b0"
                                }
                                else -> {
                                    "#bb1950"
                                }
                            }
                            dataGraph.add(GraphViewModel(date,timestamp,datestamp, aqi, color))

                        }
//                        txt_aqi_graph.text = dataGraph[dataGraph.size-1].aqi.toString()
//                        txt_date_graph.text = dataGraph[dataGraph.size-1].datestamp
                        adapterGraph.notifyDataSetChanged()
                    }

                }

                myRefGraph.child(key_data).orderByChild("timestamp").addValueEventListener(GraphListener)

                Handler().postDelayed(Runnable {
                    shimmer_view_container.stopShimmer()
                    shimmer_view_container.visibility = View.GONE
                    scroll_view.visibility = View.VISIBLE
                    if (UserProfile(this@MainDetailsActivity).getMainDetailsTutorial()
                            .toBoolean()
                    ) {
                        showShared()
                    }
                }, 1000)
            }catch (e: Exception){
                Toast.makeText(this@MainDetailsActivity,"ไม่พบข้อมูล",Toast.LENGTH_LONG).show()
                this@MainDetailsActivity.finish()
            }
        }
    }

    private fun getDataAQI(aqi:Int):HashMap<String,Any> {
        val data = HashMap<String,Any>()
        if (aqi <= 50) {
            data["txt_quality"] = getString(R.string.txt_good)
            data["txt_detail"] = "ดี"
            data["color_aqi"] = resources.getColor(R.color.colorGreen)
        } else if (aqi <= 100) {
            data["txt_quality"] = getString(R.string.txt_moderate)
            data["txt_detail"] = "ปานกลาง"
            data["color_aqi"] = resources.getColor(R.color.colorYellow)
        } else if (aqi <= 150) {
            data["txt_quality"] = getString(R.string.txt_unhealthy_for_sensitive_groups)
            data["txt_detail"] = "ไม่ดีต่อสุขภาพผู้ป่วยภูมิแพ้"
            data["color_aqi"] = resources.getColor(R.color.colorOrange)
        } else if (aqi <= 200) {
            data["txt_quality"] = getString(R.string.txt_unhealthy)
            data["txt_detail"] = "ไม่ดีต่อสุขภาพ"
            data["color_aqi"] = resources.getColor(R.color.colorPink)
        } else if (aqi <= 300) {
            data["txt_quality"] = getString(R.string.txt_very_unhealthy)
            data["txt_detail"] = "ไม่ดีต่อสุขภาพมาก"
            data["color_aqi"] = resources.getColor(R.color.colorViolet)
        } else {
            data["txt_quality"] = getString(R.string.txt_hazardous)
            data["txt_detail"] = "อันตราย"
            data["color_aqi"] = resources.getColor(R.color.colorRed)
        }

        return data
    }

    /**
     * Populates a [UnifiedNativeAdView] object with data from a given
     * [UnifiedNativeAd].
     *
     * @param nativeAd the object containing the ad's assets
     * @param adView the view to be populated
     */
    private fun populateUnifiedNativeAdView(nativeAd: UnifiedNativeAd, adView: UnifiedNativeAdView) {
        // Set the media view.
        adView.mediaView = adView.findViewById<MediaView>(R.id.ad_media)

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        adView.mediaView.setMediaContent(nativeAd.mediaContent)

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView.visibility = View.INVISIBLE
        } else {
            adView.bodyView.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView.visibility = View.INVISIBLE
        } else {
            adView.callToActionView.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon.drawable
            )
            adView.iconView.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView.visibility = View.INVISIBLE
        } else {
            adView.priceView.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView.visibility = View.INVISIBLE
        } else {
            adView.storeView.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)

    }

    /**
     * Creates a request for a new native ad based on the boolean parameters and calls the
     * corresponding "populate" method when one is successfully returned.
     *
     */
    private fun refreshAd() {

        val builder = AdLoader.Builder(this, ADMOB_AD_UNIT_ID)

        builder.forUnifiedNativeAd { unifiedNativeAd ->
            // OnUnifiedNativeAdLoadedListener implementation.
            // If this callback occurs after the activity is destroyed, you must call
            // destroy and return or you may get a memory leak.
            var activityDestroyed = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                activityDestroyed = isDestroyed
            }
            if (activityDestroyed || isFinishing || isChangingConfigurations) {
                unifiedNativeAd.destroy()
                return@forUnifiedNativeAd
            }
            // You must call destroy on old ads when you are done with them,
            // otherwise you will have a memory leak.
            currentNativeAd?.destroy()
            currentNativeAd = unifiedNativeAd
            val adView = layoutInflater
                .inflate(R.layout.ad_unified_small, null) as UnifiedNativeAdView
            populateUnifiedNativeAdView(unifiedNativeAd, adView)
            ad_frame.removeAllViews()
            ad_frame.addView(adView)
        }

        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()

        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()

        builder.withNativeAdOptions(adOptions)

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                val error =
                    """
           domain: ${loadAdError.domain}, code: ${loadAdError.code}, message: ${loadAdError.message}
          """"
                Toast.makeText(
                    this@MainDetailsActivity, "Failed to load native ad with error $error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }
    override fun onDestroy() {
        currentNativeAd?.destroy()
        super.onDestroy()
    }

    override fun GraphViewClick(position: Int) {
        //Graph Click
//        Toast.makeText(this,position.toString(),Toast.LENGTH_LONG).show()
        val data = getDataAQI(dataGraph[position].aqi)
        txt_aqi_graph.text = data["txt_detail"].toString() + " AQI " + dataGraph[position].aqi.toString()
        txt_date_graph.text = dataGraph[position].datestamp
    }

}
