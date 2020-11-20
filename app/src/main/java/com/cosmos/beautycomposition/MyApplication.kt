package com.cosmos.beautycomposition

import android.app.Application
import android.content.Context
import com.cosmos.beautymmdemo.BeautyApplication
import com.mm.rifle.Rifle
import com.qiniu.pili.droid.streaming.demo.StreamingApplication
import com.tencent.liteav.demo.livepusher.camerapush.TxApplication
import io.agora.api.example.AgoraApplication

class MyApplication : Application() {
    val cosmosAppId = "cosmos后台的appid"

    companion object {
        var context: Context? = null
    }

    override fun onCreate() {
        context = this
        super.onCreate()
        Rifle.init(this, cosmosAppId, false)

        BeautyApplication.cosmosAppId = cosmosAppId
        TxApplication.onCreate(this, cosmosAppId)
        StreamingApplication.getInstance().onCreate(this, cosmosAppId)
        AgoraApplication.onCreate(this, cosmosAppId)
    }
}