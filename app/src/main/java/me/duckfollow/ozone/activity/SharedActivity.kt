package me.duckfollow.ozone.activity

import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import me.duckfollow.ozone.R
import me.duckfollow.ozone.user.UserProfile
import me.duckfollow.ozone.utils.ConvertImagetoBase64
import java.lang.Exception

class SharedActivity : AppCompatActivity() {

    lateinit var img_profile_share:ImageView
    lateinit var text_pm_shared:TextView
    lateinit var text_pm_details:TextView
    lateinit var text_name_shared:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared)
        initView()

        val img_base64 = UserProfile(this).getImageBase64()
        if(img_base64 != "") {
            try {
                val b = ConvertImagetoBase64().base64ToBitmap(img_base64)
                img_profile_share.setImageBitmap(getCroppedBitmap(b))
            }catch (e:Exception) {

            }
        }

        val i = intent.extras
        val aqi_data = i!!.get("aqi").toString()
        val city = i.get("city").toString()
        try {
            text_pm_shared.text = aqi_data
            text_name_shared.text = city
            val aqi = aqi_data.toInt()

            if (aqi <= 50) {
                text_pm_shared.setTextColor(getResources().getColor(R.color.colorGreen))
                text_pm_details.setTextColor(getResources().getColor(R.color.colorGreen))
                text_pm_details.text = "ดี"

            } else if (aqi <= 100) {
                text_pm_shared.setTextColor(getResources().getColor(R.color.colorYellow))
                text_pm_details.setTextColor(getResources().getColor(R.color.colorYellow))
                text_pm_details.text = "ปานกลาง"

            } else if (aqi <= 150) {
                text_pm_shared.setTextColor(getResources().getColor(R.color.colorOrange))
                text_pm_details.setTextColor(getResources().getColor(R.color.colorOrange))
                text_pm_details.text = "ไม่ดีต่อสุขภาพผู้ป่วยภูมิแพ้"

            } else if (aqi <= 200) {
                text_pm_shared.setTextColor(getResources().getColor(R.color.colorPink))
                text_pm_details.setTextColor(getResources().getColor(R.color.colorPink))
                text_pm_details.text = "ไม่ดีต่อสุขภาพ"

            } else if (aqi <= 300) {
                text_pm_shared.setTextColor(getResources().getColor(R.color.colorViolet))
                text_pm_details.setTextColor(getResources().getColor(R.color.colorViolet))
                text_pm_details.text = "ไม่ดีต่อสุขภาพมาก"

            } else {
                text_pm_shared.setTextColor(getResources().getColor(R.color.colorRed))
                text_pm_details.setTextColor(getResources().getColor(R.color.colorRed))
                text_pm_details.text = "อันตราย"
            }
        }catch (e:Exception){

        }
    }

    private fun initView() {
        img_profile_share = findViewById(R.id.img_profile_share)
        text_pm_shared = findViewById(R.id.text_pm_shared)
        text_pm_details = findViewById(R.id.text_pm_details)
        text_name_shared = findViewById(R.id.text_name_shared)
    }

    private fun getCroppedBitmap(bitmap: Bitmap): Bitmap {

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
}