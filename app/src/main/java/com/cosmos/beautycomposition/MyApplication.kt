package com.cosmos.beautycomposition

import android.app.Application
import android.content.Context
import com.cosmos.beautymmdemo.BeautyApplication
import com.mm.rifle.Rifle
import com.qiniu.pili.droid.streaming.demo.StreamingApplication
import com.tencent.liteav.demo.livepusher.camerapush.TxApplication
import io.agora.api.example.AgoraApplication

class MyApplication : Application() {
    val cosmosAppId = "cosmos后台的appid"//TODO 这里需要修改为cosmos后台注册的appid

    val ZEGO_OLDSDK_APPLICATION = "com.zego.common.application.ZegoApplication" //即构旧版sdk
    val ZEGO_NEWSDK_APPLICATION = "im.zego.common.application.ZegoApplication" //即构新版sdk

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
        callZegoApplicationOnCreate()
    }

    private fun callZegoApplicationOnCreate() {
        var zegoApplication = ClassHelper.getClass(ZEGO_OLDSDK_APPLICATION)
        if (zegoApplication == null) {
            zegoApplication = ClassHelper.getClass(ZEGO_NEWSDK_APPLICATION)
        }
        var onCreateMethod =
            zegoApplication?.getMethod("onCreate", Context::class.java, String::class.java)
        onCreateMethod?.invoke(null, this, cosmosAppId)
    }
}