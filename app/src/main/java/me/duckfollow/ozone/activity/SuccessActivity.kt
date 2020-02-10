package me.duckfollow.ozone.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_success.*
import me.duckfollow.ozone.R

class SuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)
        view_root.setOnClickListener {
            this.finish()
        }
    }
}
