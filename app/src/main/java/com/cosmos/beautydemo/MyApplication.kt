package com.cosmos.beautydemo

import android.app.Application
import android.content.Context
import com.mm.rifle.Rifle

class MyApplication : Application() {
    companion object {
        var context: Context? = null
    }
    override fun onCreate() {
        context = this
        super.onCreate()
        Rifle.init(this, "6b38bc8e6afdbd040b8f6386b65c0aac", false);
    }
}