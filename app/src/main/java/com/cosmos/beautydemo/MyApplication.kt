package com.cosmos.beautydemo

import android.app.Application
import android.content.Context

class MyApplication : Application() {
    companion object {
        var context: Context? = null
    }
    override fun onCreate() {
        context = this
        super.onCreate()
    }
}