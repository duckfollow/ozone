package me.duckfollow.ozone.activity

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.gson.Gson
import me.duckfollow.ozone.R
import me.duckfollow.ozone.model.WaqiInfo
import me.duckfollow.ozone.model.WaqiInfoGeo
import me.duckfollow.ozone.util.ApiConnection

class MainDetailsActivity : AppCompatActivity() {

    lateinit var textViewCityName:TextView
    lateinit var txtViewIaqi:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_details)
        initView()

        val location_data = intent.extras
        val url = "https://api.waqi.info/feed/geo:"+location_data.getString("lat")+";"+location_data.getString("lon")+"/?token=fe5f8a6aa99f6bfb397762a0cade98a6d78795a6"
        TaskData().execute(url)
        Log.d("data_res_url",url)
    }

    private fun initView(){
        textViewCityName = findViewById(R.id.textViewCityName)
        txtViewIaqi = findViewById(R.id.txtViewIaqi)
    }

    inner class TaskData:AsyncTask<String,String,String>(){
        override fun doInBackground(vararg params: String?): String? {
            return ApiConnection().getData(params[0].toString())
        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val gson = Gson()
            val dataDetails = gson.fromJson<WaqiInfoGeo>(result,WaqiInfoGeo::class.java)
            Log.d("data_res",result)

            textViewCityName.text = dataDetails.data.city.name
            txtViewIaqi.text = dataDetails.data.aqi
        }
    }
}
