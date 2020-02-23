package me.duckfollow.ozone.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import me.duckfollow.ozone.MapsActivity
import me.duckfollow.ozone.R
import me.duckfollow.ozone.adapter.AqiListAdapter
import me.duckfollow.ozone.model.AqiModel
import me.duckfollow.ozone.user.UserProfile
import me.duckfollow.ozone.util.ApiConnection
import me.duckfollow.ozone.utils.ConvertImagetoBase64
import me.duckfollow.ozone.view.ArcProgress
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainDetailsActivity : AppCompatActivity() {

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
    lateinit var root_view:RelativeLayout
    lateinit var text_name_shared:TextView
    lateinit var text_pm_shared:TextView
    lateinit var img_profile_share:ImageView
    lateinit var text_pm_details:TextView
    lateinit var progress:ArcProgress
    lateinit var text_details:TextView

    private val STORAGE_PERMISSION = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_details)
        initView()

        val location_data = intent.extras
        val url = "https://api.waqi.info/feed/geo:"+location_data.getString("lat")+";"+location_data.getString("lon")+"/?token=fe5f8a6aa99f6bfb397762a0cade98a6d78795a6"
        TaskData().execute(url)
        Log.d("data_res_url",url)

        btn_back.setOnClickListener {
            this.finish()
        }

        btn_shared.setOnClickListener {

            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION)

            }else {
                shared()
            }
        }

        val img_base64 = UserProfile(this).getImageBase64()
        if(img_base64 != "") {
            val b = ConvertImagetoBase64().base64ToBitmap(img_base64)
            img_profile_share.setImageBitmap(getCroppedBitmap(b))
        }
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

    private fun shared(){
        val b = getImageUri(getBitmapFromView(root_view))
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, b)
            type = "image/*"
        }

        val shareIntent = Intent.createChooser(sendIntent, "OzoneNotIncluded_Share")
        startActivity(shareIntent)
    }

    fun getImageUri(inImage:Bitmap): Uri {
          val bytes = ByteArrayOutputStream();
          inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
          val path = MediaStore.Images.Media.insertImage(this.getContentResolver(), inImage, "OzoneNotIncluded_Share", null);
          return Uri.parse(path);
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
        root_view = findViewById(R.id.root_view)
        text_name_shared = findViewById(R.id.text_name_shared)
        text_pm_shared = findViewById(R.id.text_pm_shared)
        img_profile_share = findViewById(R.id.img_profile_share)
        text_pm_details = findViewById(R.id.text_pm_details)
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
    }

    fun showShared(){
        TapTargetView.showFor(this,
            TapTarget.forView(btn_shared,"ปุ่มแชร์", "คุณสามารถแชร์ไปสตอรี่ของคุณได้").cancelable(false),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView) {
                    super.onTargetClick(view)
                    UserProfile(this@MainDetailsActivity).setMainDetailsTutorial(false.toString())
                    if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this@MainDetailsActivity,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION)
                    }else {
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
        paint2.color = getResources().getColor(R.color.colorGreen)
//        canvas.drawCircle(
//            width.toFloat()/2,
//            height.toFloat()/2,
//            10f,
//            paint2
//        )
        canvas.drawRect(100f, 100f, 200f, 200f, paint2);

        return bitmap
    }

    private fun getBitmapFromView(view:View):Bitmap {
        view.setLayoutParams(RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
        val dm = this.getResources().getDisplayMetrics();
            view.measure(View.MeasureSpec.makeMeasureSpec(dm.widthPixels,
                View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(dm.heightPixels,
                    View.MeasureSpec.EXACTLY));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        val bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);

        val canvas = Canvas(bitmap);
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        view.draw(canvas);
        return bitmap;
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
    inner class TaskData:AsyncTask<String,String,String>(){
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

                try {
                    val co = JSONObject(iaqi.getString("co"))
                    val v = co.getString("v")
                    data.add(AqiModel("co",v))
                } catch (e: Exception) {

                }
                try {
                    val no2 = JSONObject(iaqi.getString("no2"))
                    val v = no2.getString("v")
                    data.add(AqiModel("no2",v))
                } catch (e: Exception) {

                }
                try {
                    val o3 = JSONObject(iaqi.getString("o3"))
                    val v = o3.getString("v")
                    data.add(AqiModel("o3",v))
                } catch (e: Exception) {

                }
                try {
                    val pm10 = JSONObject(iaqi.getString("pm10"))
                    val v = pm10.getString("v")
                    data.add(AqiModel("pm10",v))
                } catch (e: Exception) {

                }

                try {
                    val pm25 = JSONObject(iaqi.getString("pm25"))
                    val v = pm25.getString("v")
                    data.add(AqiModel("pm25",v))
                    txtViewIaqi.text = v
                    text_pm_shared.text = v
                    progress.textShow = v
                    try {
                        val aqi = v.toInt()
                        progress.progress = if (aqi > 500) {
                            100
                        }else {
                            aqi / 5
                        }
                        if (aqi <= 50) {
                            txt_view_quality.text = getString(R.string.txt_good)
                            text_pm_shared.setTextColor(getResources().getColor(R.color.colorGreen))
                            text_pm_details.setTextColor(getResources().getColor(R.color.colorGreen))
                            text_pm_details.text = "ดี"

                            text_details.text = "ดี"
                            text_details.setTextColor(getResources().getColor(R.color.colorGreen))
                        } else if (aqi <= 100) {
                            txt_view_quality.text = getString(R.string.txt_moderate)
                            text_pm_shared.setTextColor(getResources().getColor(R.color.colorYellow))
                            text_pm_details.setTextColor(getResources().getColor(R.color.colorYellow))
                            text_pm_details.text = "ปานกลาง"

                            text_details.text = "ปานกลาง"
                            text_details.setTextColor(getResources().getColor(R.color.colorYellow))
                        } else if (aqi <= 150) {
                            txt_view_quality.text = getString(R.string.txt_unhealthy_for_sensitive_groups)
                            text_pm_shared.setTextColor(getResources().getColor(R.color.colorOrange))
                            text_pm_details.setTextColor(getResources().getColor(R.color.colorOrange))
                            text_pm_details.text = "ไม่ดีต่อสุขภาพผู้ป่วยภูมิแพ้"

                            text_details.text = "ไม่ดีต่อสุขภาพผู้ป่วยภูมิแพ้"
                            text_details.setTextColor(getResources().getColor(R.color.colorOrange))
                        } else if (aqi <= 200) {
                            txt_view_quality.text = getString(R.string.txt_unhealthy)
                            text_pm_shared.setTextColor(getResources().getColor(R.color.colorPink))
                            text_pm_details.setTextColor(getResources().getColor(R.color.colorPink))
                            text_pm_details.text = "ไม่ดีต่อสุขภาพ"

                            text_details.text = "ไม่ดีต่อสุขภาพ"
                            text_details.setTextColor(getResources().getColor(R.color.colorPink))
                        } else if (aqi <= 300) {
                            txt_view_quality.text = getString(R.string.txt_very_unhealthy)
                            text_pm_shared.setTextColor(getResources().getColor(R.color.colorViolet))
                            text_pm_details.setTextColor(getResources().getColor(R.color.colorViolet))
                            text_pm_details.text = "ไม่ดีต่อสุขภาพมาก"

                            text_details.text = "ไม่ดีต่อสุขภาพมาก"
                            text_details.setTextColor(getResources().getColor(R.color.colorViolet))
                        } else {
                            txt_view_quality.text = getString(R.string.txt_hazardous)
                            text_pm_shared.setTextColor(getResources().getColor(R.color.colorRed))
                            text_pm_details.setTextColor(getResources().getColor(R.color.colorRed))
                            text_pm_details.text = "อันตราย"

                            text_details.text = "อันตราย"
                            text_details.setTextColor(getResources().getColor(R.color.colorRed))
                        }
                    }catch (e:Exception){

                    }
                }catch (e:Exception){

                }

                try {
                    val so2 = JSONObject(iaqi.getString("so2"))
                    val v = so2.getString("v")
                    data.add(AqiModel("so2",v))
                } catch (e: Exception) {

                }

                adapter.notifyDataSetChanged()

                textViewCityName.text = name
                text_name_shared.text = name

                Handler().postDelayed(Runnable {
                    shimmer_view_container.stopShimmer()
                    shimmer_view_container.visibility = View.GONE
                    scroll_view.visibility = View.VISIBLE
                    if(UserProfile(this@MainDetailsActivity).getMainDetailsTutorial().toBoolean()){
                        showShared()
                    }
                },1000)
            }catch (e:Exception){

            }


//            try {
//                val gson = Gson()
//                val dataDetails = gson.fromJson<WaqiInfoGeo>(result, WaqiInfoGeo::class.java)
//                Log.d("data_res", result)
//
//                try {
//                    data.add(AqiModel("co", dataDetails.data.iaqi.co.v))
//                } catch (e: Exception) {
//
//                }
//                try {
//                    data.add(AqiModel("no2", dataDetails.data.iaqi.no2.v))
//                } catch (e: Exception) {
//
//                }
//                try {
//                    data.add(AqiModel("o3", dataDetails.data.iaqi.o3.v))
//                } catch (e: Exception) {
//
//                }
//                try {
//                    data.add(AqiModel("pm10", dataDetails.data.iaqi.pm10.v))
//                } catch (e: Exception) {
//
//                }
//                try {
//                    val pm25 = dataDetails.data.iaqi.pm25.v
//                    data.add(AqiModel("pm25", pm25))
//                    try {
//                        val aqi = pm25.toInt()
//                        if (aqi <= 50) {
//                            txt_view_quality.text = getString(R.string.txt_good)
//                        } else if (aqi <= 100) {
//                            txt_view_quality.text = getString(R.string.txt_moderate)
//                        } else if (aqi <= 150) {
//                            txt_view_quality.text = getString(R.string.txt_unhealthy_for_sensitive_groups)
//                        } else if (aqi <= 200) {
//                            txt_view_quality.text = getString(R.string.txt_unhealthy)
//                        } else if (aqi <= 300) {
//                            txt_view_quality.text = getString(R.string.txt_very_unhealthy)
//                        } else {
//                            txt_view_quality.text = getString(R.string.txt_hazardous)
//                        }
//                    }catch (e:Exception){
//
//                    }
//                } catch (e: Exception) {
//
//                }
//                try {
//                    data.add(AqiModel("so2", dataDetails.data.iaqi.so2.v))
//                } catch (e: Exception) {
//
//                }
//                try {
//                    data.add(AqiModel("w", dataDetails.data.iaqi.w.v))
//                } catch (e: Exception) {
//
//                }
//                try {
//                    data.add(AqiModel("wg", dataDetails.data.iaqi.wg.v))
//                } catch (e: Exception) {
//
//                }
//                adapter.notifyDataSetChanged()
//
//                textViewCityName.text = dataDetails.data.city.name
//                txtViewIaqi.text = dataDetails.data.aqi
//
//                Handler().postDelayed(Runnable {
//                    shimmer_view_container.stopShimmer()
//                    shimmer_view_container.visibility = View.GONE
//                    scroll_view.visibility = View.VISIBLE
//                },1000)
//            }catch (e:Exception){
//
//            }
        }
    }
}
