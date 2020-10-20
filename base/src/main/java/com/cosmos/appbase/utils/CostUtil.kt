package com.cosmos.appbase.utils

import android.os.Handler
import android.os.SystemClock

class CostUtil(val TAG: String) {
    private var costTime = 0

    @Volatile
    private var costAllTime = 0L

    @Volatile
    private var costCount = 0
    private var handler: Handler? = null

    private var startTime = 0L

    init {
        handler = Handler()
        handler?.postDelayed(object : Runnable {
            override fun run() {
                LogUtil.v(TAG, costTime.toString())
                costTime = 0
                handler?.postDelayed(this, 1000)
            }
        }, 1000)
    }

    fun start() {
        startTime = SystemClock.currentThreadTimeMillis()
    }

    fun end() {
        costCount++
        costAllTime += SystemClock.currentThreadTimeMillis() - startTime
        costTime = (costAllTime / costCount).toInt()
        if (costAllTime > 1000) {
            costAllTime = 0
            costCount = 0
        }
    }
}