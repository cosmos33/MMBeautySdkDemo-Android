package com.cosmos.beautydemo.camera

import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Size
import android.view.SurfaceHolder
import androidx.annotation.IntDef
import com.cosmos.beautydemo.camera.callback.OnPreviewDataCallback
import com.cosmos.beautydemo.camera.callback.OnTakePicCallBack
import java.io.File

interface ICamera {
    @IntDef(
        CAMERA_ALL,
        CAMERA_FRONT,
        CAMREA_BACK
    )
    annotation class ICameraNumber

    fun checkCameraHardware(context: Context): Boolean
    fun checkAndRequestPermission(request: Int): Boolean
    fun checkPersmissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean

    fun init(context: Activity)
    fun getCurrentCameraId(): Int
    fun getCameraCount(@ICameraNumber orientation: Int): Int
    val currentPreviewFps: Int
    fun setPreviewFps(minFps: Int, maxFps: Int)
    fun getOrientation(cameraId: Int): Int

    //    void setPreviewOrientation(int degree);
    fun open(isFront: Boolean): Int
    fun setPreviewSize(size: Size)
    fun setConfig(cameraConfig: CameraConfig?)
    fun preview(surface: SurfaceTexture, onPreviewDataCallback: OnPreviewDataCallback): Boolean
    fun preview(holder: SurfaceHolder, onPreviewDataCallback: OnPreviewDataCallback): Boolean
    fun stopPreview()
    fun switchCamera(onRelaseCallback: ReleaseCallBack?): Boolean
    fun takePicture(picFile: File, onTakePicCallBack: OnTakePicCallBack?)
    fun release(onRelaseCallback: ReleaseCallBack? = null)

    fun getPreviewSize(): Size?
    fun getCameraRotation(): Int

    companion object {
        const val CAMERA_ALL = 0
        const val CAMERA_FRONT = 1
        const val CAMREA_BACK = 2
    }


    interface ReleaseCallBack {
        fun onCameraRelease()
    }
}