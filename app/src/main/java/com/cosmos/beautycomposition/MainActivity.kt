package com.cosmos.beautycomposition

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cosmos.beautymmdemo.BeautyMainActivity
import com.qiniu.pili.droid.streaming.demo.QiNiuActivity
import com.tencent.liteav.demo.livepusher.camerapush.TXMainActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onBeautySdkDemoClick(view: View) {
        startActivity(Intent(this, BeautyMainActivity::class.java))
    }

    fun onQiNiuDemoClick(view: View) {
        startActivity(Intent(this, QiNiuActivity::class.java))
    }

    fun onTXDemoClick(view: View) {
        startActivity(Intent(this, TXMainActivity::class.java))
    }
}
