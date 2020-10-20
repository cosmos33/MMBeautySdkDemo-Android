package com.cosmos.appbase.camera

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import android.view.SurfaceHolder
import com.cosmos.appbase.camera.ICamera.ICameraNumber
import com.cosmos.appbase.camera.algorithm.IRecorderSize
import com.cosmos.appbase.camera.algorithm.RecorderSizeImpl
import com.cosmos.appbase.camera.callback.OnPreviewDataCallback
import com.cosmos.appbase.camera.callback.OnTakePicCallBack
import com.cosmos.appbase.utils.LogUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference

@Suppress("DEPRECATION")
class CameraImpl : CameraBase() {
    private var cameraNum = 0
    private var cameraFrontCount = 0
    private var cameraBackCount = 0
    private var surfaceReference: WeakReference<Any>? = null
    var currentCamera: Camera? = null
        private set
    private var currentCameraId = -1
    override var currentPreviewFps = 0
        private set
    private var isTakePic = false
    private var isPreviewing = false
    private var orientationEventListener: OrientationEventListener? = null
    private var iRecorderSize: IRecorderSize? = null
    private lateinit var onPreviewDataCallback: OnPreviewDataCallback
    private var byteArray: ByteArray? = null
    private var previewSize: Size? = null
    private var cameraRotationDegree = 0

    override fun init(activity: Context) {
        super.init(activity)
        LogUtil.v(TAG, "init")
        cameraNum = Camera.getNumberOfCameras()
        var cameraInfo: Camera.CameraInfo
        for (i in 0 until cameraNum) {
            cameraInfo = Camera.CameraInfo()
            try {
                Camera.getCameraInfo(i, cameraInfo)
            } catch (e: RuntimeException) {
                e.printStackTrace()
                continue
            }
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraFrontCount++
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraBackCount++
            }
        }
        LogUtil.v(TAG, "cameraNum:$cameraNum")
        iRecorderSize = RecorderSizeImpl()
    }

    override fun getCameraCount(@ICameraNumber orientation: Int): Int {
        when (orientation) {
            ICamera.CAMERA_ALL -> return cameraNum
            ICamera.CAMERA_FRONT -> return cameraFrontCount
            ICamera.CAMREA_BACK -> return cameraBackCount
        }
        return 0
    }

    override fun setPreviewFps(minFps: Int, maxFps: Int) {
        checkCurrentCamera()
        val properPreviewFps = getProperPreviewFps(minFps, maxFps)
        LogUtil.v(
            TAG,
            String.format(
                "preview fps:%d,%d",
                properPreviewFps[0],
                properPreviewFps[1]
            )
        )
        val parameters = currentCamera!!.parameters
        parameters.setPreviewFpsRange(properPreviewFps[0], properPreviewFps[1])
        currentCamera!!.parameters = parameters
    }

    private fun getProperPreviewFps(minFps: Int, maxFps: Int): IntArray {
        // TODO: 2020/5/12  获取最佳预览fps
        val supportedPreviewFpsRange =
            currentCamera!!.parameters.supportedPreviewFpsRange
        for (fps in supportedPreviewFpsRange) {
            if (fps[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] == minFps * 1000) {
                return fps
            }
        }
        return supportedPreviewFpsRange[0]
    }

    override fun getOrientation(cameraId: Int): Int {
        if (cameraId < 0 || cameraId > cameraNum - 1) {
            return -1
        }
        val cameraInfo = Camera.CameraInfo()
        try {
            Camera.getCameraInfo(cameraId, cameraInfo)
        } catch (e: RuntimeException) {
            e.printStackTrace()
            return -1
        }
        return cameraInfo.orientation
    }

    private fun setCameraDisplayOrientation(screenRotation: Int) {
        val context = activity ?: return
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(currentCameraId, info)
        val rotation = screenRotation;
        // context.windowManager.defaultDisplay.rotation

        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360 // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360
        }
        cameraRotationDegree = result
        currentCamera!!.setDisplayOrientation(result)
    }

    override fun open(isFront: Boolean): Int {
        release()
        var cameraInfo: Camera.CameraInfo
        currentCameraId = -1
        for (i in 0 until cameraNum) {
            cameraInfo = Camera.CameraInfo()
            try {
                Camera.getCameraInfo(i, cameraInfo)
            } catch (e: RuntimeException) {
                e.printStackTrace()
                continue
            }
            if (isFront && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                currentCameraId = i
                break
            }
            if (!isFront && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                currentCameraId = i
                break
            }
        }
        if (currentCameraId != -1) {
            openInner(currentCameraId)
        }
        LogUtil.v(TAG, "currentCamera:$currentCamera")
        return currentCameraId
    }

    override fun setPreviewSize(size: Size) {
        checkCurrentCamera()
        //        Size previewSize = getPreviewSize(size);
        val parameters = currentCamera!!.parameters
        val previewSize = iRecorderSize!!.getOptimalVideoSize(
            parameters.supportedVideoSizes,
            parameters.supportedPreviewSizes, size.width, size.height
        )
        LogUtil.v(
            TAG,
            String.format("previewSize:%d*%d", previewSize!!.width, previewSize.height)
        )
        this.previewSize = Size(previewSize.width, previewSize.height)
        parameters.setPreviewSize(previewSize.width, previewSize.height)
        try {
            currentCamera!!.parameters = parameters
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

    private fun openInner(tempCameraId: Int): Boolean {
        currentCamera = try {
            //On some devices, this method may take a long time to complete
            currentCameraId = tempCameraId
            Camera.open(tempCameraId)
        } catch (e: RuntimeException) {
            e.printStackTrace()
            return false
        }
        currentCameraId = tempCameraId
        currentCamera?.setErrorCallback(Camera.ErrorCallback { error, camera -> })
        return true
    }

    override fun setConfig(cameraConfig: CameraConfig?) {}

    var screenRotation: Int = 0

    override fun preview(
        surface: SurfaceTexture,
        onPreviewDataCallback: OnPreviewDataCallback,
        screenRotation: Int
    ): Boolean {
        this.screenRotation = screenRotation
        checkCurrentCamera()
        this.onPreviewDataCallback = onPreviewDataCallback
        surfaceReference = WeakReference(surface)
        try {
            currentCamera!!.setPreviewTexture(surface)
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        pveviewInner()
        return true
    }

    private fun pveviewInner() {
        for (i in 1..3) {
            var byteArray = ByteArray(
                currentCamera!!.parameters.previewSize.width
                        * currentCamera!!.parameters.previewSize.height
                        * ImageFormat.getBitsPerPixel(currentCamera!!.parameters.previewFormat)
                        / 8
            )
            currentCamera!!.addCallbackBuffer(byteArray)
        }
        currentCamera!!.setPreviewCallbackWithBuffer() { data, _ ->
            onPreviewDataCallback.onPreviewData(data)
            currentCamera?.addCallbackBuffer(data)
        }
        if (activity != null && orientationEventListener == null) {
            orientationEventListener =
                object : OrientationEventListener(activity) {
                    override fun onOrientationChanged(orientation: Int) {
                        this@CameraImpl.onOrientationChanged(orientation)
                    }
                }
        }
        orientationEventListener!!.enable()
        isPreviewing = true
        currentCamera!!.startPreview()
        setCameraDisplayOrientation(screenRotation)
    }

    private fun checkCurrentCamera() {
        checkNotNull(currentCamera) { "camera is null" }
    }

    override fun preview(
        holder: SurfaceHolder,
        onPreviewDataCallback: OnPreviewDataCallback,
        screenRotation: Int
    ): Boolean {
        this.screenRotation = screenRotation

        this.onPreviewDataCallback = onPreviewDataCallback
        checkTakingPic()
        checkCurrentCamera()
        surfaceReference = WeakReference(holder)
        try {
            currentCamera!!.setPreviewDisplay(holder)
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        pveviewInner()
        return true
    }

    override fun stopPreview() {
        if (currentCamera != null) {
            try {
                currentCamera!!.stopPreview()
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
        }
    }

    private fun checkTakingPic() {
        check(!isTakePic) { "camera is takingPic" }
    }

    override fun switchCamera(onRelaseCallback: ICamera.ReleaseCallBack?): Boolean {
        var tempId = currentCameraId
        release(onRelaseCallback)
        return if (tempId == -1) {
            if (open(true) != -1) {
                false
            } else {
                previewSize?.let { setPreviewSize(it) }
                previewInner()
            }
        } else {
            tempId = (tempId + 1) % cameraNum
            LogUtil.v(
                TAG,
                String.format("switchCamera cameraId:%d", tempId)
            )
            if (!openInner(tempId)) {
                false
            } else {
                previewSize?.let { setPreviewSize(it) }
                previewInner()
            }
        }
    }

    override fun takePicture(
        picFile: File,
        onTakePicCallBack: OnTakePicCallBack?
    ) {
        checkPreviewing()
        checkCurrentCamera()
        isTakePic = true
        val supportedPictureSizes =
            currentCamera!!.parameters.supportedPictureSizes
        for (supportedPictureSize in supportedPictureSizes) {
            LogUtil.v(
                TAG,
                String.format(
                    "supportPicSize:%d;%d",
                    supportedPictureSize.width,
                    supportedPictureSize.height
                )
            )
        }
        val parameters = currentCamera!!.parameters
        parameters.setPictureSize(
            supportedPictureSizes[0].width,
            supportedPictureSizes[0].height
        )
        currentCamera!!.parameters = parameters
        try {
            currentCamera!!.takePicture(
                null,
                null,
                null,
                Camera.PictureCallback { data, camera -> // TODO: 2020/5/12 左右反转
                    savePicFiles(picFile, data)
                    isTakePic = false
                    onTakePicCallBack?.onTakePicCallBack(picFile)
                })
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

    private fun savePicFiles(picFile: File, data: ByteArray) {
        if (!picFile.exists()) {
            try {
                picFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
                // TODO: 2020/5/12 创建文件失败
            }
        }
        val off = 0
        val remainSize = data.size
        try {
            val fileOutputStream = FileOutputStream(picFile)
            fileOutputStream.write(data, off, remainSize)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun checkPreviewing() {
        check(isPreviewing) { "take picture must" }
    }

    private fun previewInner(): Boolean {
        if (surfaceReference == null) {
            return false
        }
        val surface = surfaceReference!!.get() ?: return false
        if (surface is SurfaceTexture) {
            return preview(surface, onPreviewDataCallback, screenRotation)
        } else if (surface is SurfaceHolder) {
            return preview(surface, onPreviewDataCallback, screenRotation)
        }
        return false
    }

    fun onOrientationChanged(orientation: Int) {
        var orientation = orientation
        if (currentCameraId == -1) {
            return
        }
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(currentCameraId, info)
        orientation = (orientation + 45) / 90 * 90
        var rotation = 0
        rotation = if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            (info.orientation - orientation + 360) % 360
        } else {  // back-facing camera
            (info.orientation + orientation) % 360
        }
//        val parameters = currentCamera!!.parameters
        try {
            if (currentCamera!!.parameters != null) {
                currentCamera!!.parameters.setRotation(rotation)

            }
        } catch (e: Exception) {
            LogUtil.v(TAG, "Set parameter failed !!")
        }
//        currentCamera!!.parameters = parameters
    }

    override fun release(onRelaseCallback: ICamera.ReleaseCallBack?) {
        LogUtil.v(TAG, "release")
        if (orientationEventListener != null) {
            orientationEventListener!!.disable()
        }
        if (currentCamera != null) {
            currentCamera!!.setPreviewCallbackWithBuffer(null)
            currentCamera!!.release()
            currentCamera = null
        }
        onRelaseCallback?.let {
            onRelaseCallback.onCameraRelease()
        }
        currentCameraId = -1
        isPreviewing = false
        isTakePic = false
    }

    override fun getCameraRotation(): Int {
        return cameraRotationDegree
    }

    override fun autoFocus() {
        try {
            currentCamera?.autoFocus(null)
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }


    override fun getCurrentCameraId(): Int {
        return currentCameraId
    }

    override fun getPreviewSize(): Size? {
        return previewSize
    }

    companion object {
        private const val TAG = "CameraImpl"
    }
}