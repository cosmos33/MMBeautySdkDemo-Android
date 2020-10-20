package com.cosmos.appbase.camera

import android.content.Context
import android.util.Rational
import android.util.Size
import android.view.Surface
import java.lang.ref.WeakReference
import java.util.*

class CameraConfig private constructor(builder: Builder) {
    var cameraId: Int = ICamera.Companion.CAMREA_BACK
    val isUseFlash: Boolean
    val targetAspectRatio: Rational?
    val context: WeakReference<Context>?
    val surfaces: ArrayList<Surface>?
    val previewSize: Size?
    val previewMaxFps: Int
    val previewMinFps: Int

    class Builder {
        var cameraId = 0
        var useFlash = false
        var previewSize: Size? = null
        var previewMaxFps = 30
        var previewMinFps = 30
        var targetAspectRatio: Rational? = null
        var context: WeakReference<Context>? = null
        var surfaces: ArrayList<Surface>? = null
        fun cameraId(`val`: Int): Builder {
            cameraId = `val`
            return this
        }

        fun useFlash(`val`: Boolean): Builder {
            useFlash = `val`
            return this
        }

        fun targetAspectRatio(`val`: Rational?): Builder {
            targetAspectRatio = `val`
            return this
        }

        fun context(`val`: Context): Builder {
            context = WeakReference(`val`)
            return this
        }

        fun surfaces(`val`: ArrayList<Surface>?): Builder {
            surfaces = `val`
            return this
        }

        fun previewSize(`val`: Size?): Builder {
            previewSize = `val`
            return this
        }

        fun previewMaxFps(`val`: Int): Builder {
            previewMaxFps = `val`
            return this
        }

        fun previewMinFps(`val`: Int): Builder {
            previewMinFps = `val`
            return this
        }

        fun setPreviewSize(size: Size?): Builder {
            previewSize = size
            return this
        }

        fun build(): CameraConfig {
            checkNotNull(context) { "context is null" }
            return CameraConfig(this)
        }
    }

    init {
        cameraId = builder.cameraId
        isUseFlash = builder.useFlash
        targetAspectRatio = builder.targetAspectRatio
        context = builder.context
        surfaces = builder.surfaces
        previewSize = builder.previewSize
        previewMaxFps = builder.previewMaxFps
        previewMinFps = builder.previewMinFps
    }
}