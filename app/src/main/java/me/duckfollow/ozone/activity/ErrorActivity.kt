package me.duckfollow.ozone.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_error.*
import me.duckfollow.ozone.MapsActivity
import me.duckfollow.ozone.R

class ErrorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)
        root_view.setOnClickListener {
            val i = Intent(this,MapsActivity::class.java)
            startActivity(i)
            this.finish()
        }
    }
}
