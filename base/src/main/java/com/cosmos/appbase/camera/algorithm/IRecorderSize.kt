package com.cosmos.appbase.camera.algorithm

import android.hardware.Camera

@Suppress("DEPRECATION")
interface IRecorderSize {
    fun getOptimalVideoSize(
        supportedVideoSizes: List<Camera.Size>?,
        previewSizes: List<Camera.Size>, w: Int, h: Int
    ): Camera.Size?
}