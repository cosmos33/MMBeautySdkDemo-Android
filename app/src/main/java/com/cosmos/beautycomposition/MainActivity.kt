package com.cosmos.beautycomposition

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cosmos.beautymmdemo.BeautyMainActivity
import com.qiniu.pili.droid.streaming.demo.QiNiuActivity
import com.tencent.liteav.demo.livepusher.camerapush.TXMainActivity
import io.agora.api.example.AgoraMainActivity

class MainActivity : AppCompatActivity() {
    val ZEGO_OLDSDK_ENTRY_CLASS = "com.zego.videofilter.ui.VideoFilterMainUI" //即构旧版sdk
    val ZEGO_NEWSDK_ENTRY_CLASS = "im.zego.customrender.ui.ZGVideoRenderTypeUI" //即构新版sdk
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkAndRequestPermission(1000)
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

    fun onAgoraDemoClick(view: View) {
        startActivity(Intent(this, AgoraMainActivity::class.java))
    }

    fun onZegoDemoClick(view: View) {
        var videoFilterMainUIClass: Class<Any>? = ClassHelper.getClass(ZEGO_OLDSDK_ENTRY_CLASS)
        if (videoFilterMainUIClass == null) {
            videoFilterMainUIClass = ClassHelper.getClass(ZEGO_NEWSDK_ENTRY_CLASS)
        }
        startActivity(Intent(this, videoFilterMainUIClass))
    }

    fun checkAndRequestPermission(requestCode: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), requestCode)
                return false
            }
        }
        return true
    }
}
