package com.cosmos.beautycomposition

import android.app.Application
import android.content.Context
import com.cosmos.beautymmdemo.BeautyApplication
import com.mm.rifle.Rifle
import com.qiniu.pili.droid.streaming.demo.StreamingApplication
import com.tencent.liteav.demo.livepusher.camerapush.TxApplication

class MyApplication : Application() {
    val cosmosAppId = "6b38bc8e6afdbd040b8f6386b65c0aac"

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
    }
}