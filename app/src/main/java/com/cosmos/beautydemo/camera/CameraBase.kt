package com.cosmos.beautydemo.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.lang.ref.WeakReference

abstract class CameraBase : ICamera {
    private var cameraRequestCode: Int = 0
    protected var activityWeakReference: WeakReference<Activity>? = null
    override fun init(context: Activity) {
        activityWeakReference = WeakReference(context)
    }

    override fun checkCameraHardware(context: Context): Boolean {
        return context.packageManager
            .hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    override fun checkAndRequestPermission(requestCode: Int): Boolean {
        cameraRequestCode = requestCode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity?.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                activity?.requestPermissions(arrayOf(Manifest.permission.CAMERA), requestCode)
                return false
            }
        }
        return true
    }

    override fun checkPersmissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        if (requestCode == cameraRequestCode && grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }

    protected val activity: Activity?
        protected get() = if (activityWeakReference == null) {
            null
        } else activityWeakReference!!.get()
}