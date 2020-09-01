package com.cosmos.beautydemo.camera

import android.graphics.SurfaceTexture
import android.os.Handler
import android.util.Size
import com.cosmos.beautydemo.camera.callback.OnPreviewDataCallback

class CameraManager constructor(handler: Handler, cameraImpl: CameraImpl) : ICamera by cameraImpl {
    private val cameraImpl: CameraImpl = cameraImpl
    private val handler: Handler = handler

    override fun open(isFront: Boolean): Int {
        handler.post {
            this@CameraManager.cameraImpl.open(isFront)
        }
        return 0
    }

    override fun setPreviewFps(minFps: Int, maxFps: Int) {
        handler.post {
            this@CameraManager.cameraImpl.setPreviewFps(minFps, maxFps)
        }
    }

    override fun setPreviewSize(size: Size) {
        handler.post {
            this@CameraManager.cameraImpl.setPreviewSize(size)
        }
    }

    override fun preview(
        surface: SurfaceTexture, onPreviewDataCallback: OnPreviewDataCallback
    ): Boolean {
        handler.post {
            this@CameraManager.cameraImpl.preview(surface, onPreviewDataCallback)
        }
        return true
    }

    override fun switchCamera(onRelaseCallback: ICamera.ReleaseCallBack?): Boolean {
        handler.post {
            this@CameraManager.cameraImpl.switchCamera(onRelaseCallback)
        }
        return true
    }
}