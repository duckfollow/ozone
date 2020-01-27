package me.duckfollow.ozone.activity

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import me.duckfollow.ozone.R
import me.duckfollow.ozone.adapter.AqiListAdapter
import me.duckfollow.ozone.model.AqiModel
import me.duckfollow.ozone.model.WaqiInfo
import me.duckfollow.ozone.model.WaqiInfoGeo
import me.duckfollow.ozone.util.ApiConnection
import java.lang.Exception

class MainDetailsActivity : AppCompatActivity() {

    lateinit var textViewCityName:TextView
    lateinit var txtViewIaqi:TextView
    lateinit var list_iaqi:RecyclerView
    lateinit var adapter:AqiListAdapter
    lateinit var data:ArrayList<AqiModel>
    lateinit var btn_back:ImageButton
    lateinit var shimmer_view_container:ShimmerFrameLayout
    lateinit var scroll_view:NestedScrollView


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
    }

    private fun initView(){
        textViewCityName = findViewById(R.id.textViewCityName)
        txtViewIaqi = findViewById(R.id.txtViewIaqi)
        list_iaqi = findViewById(R.id.list_iaqi)
        btn_back = findViewById(R.id.btn_back)
        shimmer_view_container = findViewById(R.id.shimmer_view_container)
        scroll_view = findViewById(R.id.scroll_view)
        scroll_view.visibility = View.GONE

        list_iaqi.layoutManager = LinearLayoutManager(this)
        list_iaqi.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        data = ArrayList()
        adapter = AqiListAdapter(data)
        list_iaqi.adapter = adapter
        list_iaqi.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING)
        val snapHelper = LinearSnapHelper() // Or PagerSnapHelper
        snapHelper.attachToRecyclerView(list_iaqi)
    }

    inner class TaskData:AsyncTask<String,String,String>(){
        override fun onPreExecute() {
            super.onPreExecute()
            data.clear()
            shimmer_view_container.startShimmer()
        }
        override fun doInBackground(vararg params: String?): String? {
            return ApiConnection().getData(params[0].toString())
        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            try {
                val gson = Gson()
                val dataDetails = gson.fromJson<WaqiInfoGeo>(result, WaqiInfoGeo::class.java)
                Log.d("data_res", result)

                try {
                    data.add(AqiModel("co", dataDetails.data.iaqi.co.v))
                } catch (e: Exception) {

                }
                try {
                    data.add(AqiModel("no2", dataDetails.data.iaqi.no2.v))
                } catch (e: Exception) {

                }
                try {
                    data.add(AqiModel("o3", dataDetails.data.iaqi.o3.v))
                } catch (e: Exception) {

                }
                try {
                    data.add(AqiModel("pm10", dataDetails.data.iaqi.pm10.v))
                } catch (e: Exception) {

                }
                try {
                    data.add(AqiModel("pm25", dataDetails.data.iaqi.pm25.v))
                } catch (e: Exception) {

                }
                try {
                    data.add(AqiModel("so2", dataDetails.data.iaqi.so2.v))
                } catch (e: Exception) {

                }
                try {
                    data.add(AqiModel("w", dataDetails.data.iaqi.w.v))
                } catch (e: Exception) {

                }
                try {
                    data.add(AqiModel("wg", dataDetails.data.iaqi.wg.v))
                } catch (e: Exception) {

                }
                adapter.notifyDataSetChanged()

                textViewCityName.text = dataDetails.data.city.name
                txtViewIaqi.text = dataDetails.data.aqi

                Handler().postDelayed(Runnable {
                    shimmer_view_container.stopShimmer()
                    shimmer_view_container.visibility = View.GONE
                    scroll_view.visibility = View.VISIBLE
                },1000)
            }catch (e:Exception){

            }
        }
    }
}
