package me.duckfollow.ozone.activity

import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import me.duckfollow.ozone.R
import java.util.HashMap

class ScanActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private lateinit var mScannerView: ZXingScannerView
    lateinit var myRefUser: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        val contentFrame = findViewById(R.id.content_frame) as ViewGroup
        mScannerView = ZXingScannerView(this)
        contentFrame.addView(mScannerView)
        val database = FirebaseDatabase.getInstance().reference
        myRefUser = database.child("user/")
    }

    override fun onResume() {
        super.onResume()
        mScannerView.setResultHandler(this)
        mScannerView.startCamera()
    }

    override fun onPause() {
        super.onPause()
        mScannerView.stopCamera()
    }

    override fun handleResult(p0: Result?) {

        try{
            val ck = p0.toString().split("me.duckfollow.ozone")
            if(ck.size > 1) {
                Toast.makeText(this,p0.toString(),Toast.LENGTH_LONG).show()
                val android_id = Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);

                val key = p0.toString().split("&")

                val map = HashMap<String, Any>()
                map.put("key",key[1])
                myRefUser.child(android_id+"/subscribe/"+key[1]+"/").updateChildren(map)

            }else {
                Toast.makeText(this,"No",Toast.LENGTH_LONG).show()
            }
        }catch (e:Exception){

        }

        Handler().postDelayed(Runnable {
            mScannerView.resumeCameraPreview(this@ScanActivity)
        },2000)
    }
}
