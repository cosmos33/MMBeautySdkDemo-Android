package com.cosmos.beautymmdemo.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.opengl.EGLSurface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import com.core.glcore.util.ImageFrame
import com.cosmos.appbase.AspectFrameLayout
import com.cosmos.appbase.camera.CameraImpl
import com.cosmos.appbase.camera.CameraManager
import com.cosmos.appbase.camera.ICamera
import com.cosmos.appbase.camera.callback.OnPreviewDataCallback
import com.cosmos.appbase.filter.DirectDrawer
import com.cosmos.appbase.filter.FBOHelper
import com.cosmos.appbase.gl.EGLHelper
import com.cosmos.appbase.gl.GLUtils
import com.cosmos.appbase.utils.CostUtil
import com.cosmos.beauty.model.MMRenderFrameParams
import com.cosmos.beauty.model.datamode.CameraDataMode
import com.cosmos.beautymmdemo.R
import com.fanyiran.utils.base.BaseFragment

class SurfaceFragment : BaseFragment(),
    OnPreviewDataCallback {
    private var textureId: Int = -1
    private val cameraRequestCode = 12300
    private var isFrontCamera = true

    private lateinit var surfaceView: SurfaceView
    private lateinit var ivCover: ImageView
    private lateinit var cameraPreview: AspectFrameLayout

    private lateinit var cameraImpl: CameraManager
    private lateinit var surfaceTexture: SurfaceTexture

    private var eglSurface: EGLSurface? = null
    private lateinit var fboHelper: FBOHelper
    private var width = 0
    private var height = 0
    private var directDrawer: DirectDrawer? = null
    private var cameraHander: Handler? = null
    private val holderWidth = 720
    private val holderHeight = 1280
    private val coastUtil by lazy { CostUtil("RenderTime") }
    private var mtx = FloatArray(16)
    var onSurfaceStatusChangeListener: OnSurfaceStatusChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val handlerThread = HandlerThread("camera thread")
        handlerThread.start()
        cameraHander = Handler(handlerThread.looper)
        cameraImpl = CameraManager(
            cameraHander!!,
            CameraImpl()
        )
        cameraImpl.init(activity!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_surface
    }

    override fun initView(view: View?) {
        surfaceView = view?.findViewById<SurfaceView>(R.id.surfaceView)!!
        ivCover = view.findViewById<ImageView>(R.id.ivCover)
        cameraPreview = view.findViewById<AspectFrameLayout>(R.id.cameraPreview_afl)
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {
                this@SurfaceFragment.width = width
                this@SurfaceFragment.height = height
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                cameraImpl.stopPreview()
                cameraImpl.release(object : ICamera.ReleaseCallBack {
                    override fun onCameraRelease() {
//                        releaseRender()
                        cameraHander?.post {
                            onSurfaceStatusChangeListener?.onCameraRelease()
                        }
                    }
                })
                surfaceTexture.release()
                onSurfaceStatusChangeListener?.surfaceDestroyed()
                directDrawer = null
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                holder?.setFixedSize(holderWidth, holderHeight)
                if (checkAndRequestPermission(cameraRequestCode)) {
                    initCamera()
                }
            }
        })
    }

    fun checkAndRequestPermission(requestCode: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity?.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                activity?.requestPermissions(arrayOf(Manifest.permission.CAMERA), requestCode)
                return false
            }
        }
        return true
    }

    fun checkPersmissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }

    private fun initCamera() {
        cameraPreview.setAspectRatio(720 * 1.0 / 1280)
        textureId = GLUtils.generateTexure()
        surfaceTexture = SurfaceTexture(textureId)
        cameraImpl.open(true)
        cameraImpl.setPreviewSize(Size(1280, 720))
        cameraImpl.setPreviewFps(30, 30)
        cameraImpl.preview(surfaceTexture, this, activity!!.windowManager.defaultDisplay.rotation)
        cameraImpl.autoFocus()
        isFrontCamera = true
    }

    override fun onPreviewData(data: ByteArray) {
        if (cameraImpl.getPreviewSize() == null) {
            return
        }
        if (directDrawer == null) {
            EGLHelper.instance.init()
            eglSurface = EGLHelper.instance.genEglSurface(surfaceView.holder)
            EGLHelper.instance.makeCurrent(eglSurface)
            fboHelper =
                FBOHelper(cameraImpl.getPreviewSize()!!.width, cameraImpl.getPreviewSize()!!.height)
            fboHelper.setNeedFlip(
                isFrontCamera,
                cameraImpl.getOrientation(cameraImpl.getCurrentCameraId())
            )
            directDrawer = DirectDrawer()
        }
        val orientation = cameraImpl.getOrientation(cameraImpl.getCurrentCameraId())
        var resultTexture = fboHelper.update(data, orientation, cameraImpl.getPreviewSize())
        coastUtil.start()

        onSurfaceStatusChangeListener?.let {
            val renderFrameParams = MMRenderFrameParams(
                CameraDataMode(isFrontCamera, cameraImpl.getCameraRotation()),
                data,
                cameraImpl.getPreviewSize()!!.width,
                cameraImpl.getPreviewSize()!!.height,
                holderWidth,
                holderHeight,
                ImageFrame.MMFormat.FMT_NV21
            )
            resultTexture = onSurfaceStatusChangeListener?.onCameraPreviewData(
                resultTexture, renderFrameParams
            )!!
        }

        coastUtil.end()
        surfaceTexture.getTransformMatrix(mtx)
        directDrawer?.draw(resultTexture, mtx, orientation)
        surfaceTexture.updateTexImage()
        EGLHelper.instance.swapBuffers(eglSurface)
        if (ivCover.visibility != View.GONE) {
            activity?.runOnUiThread {
                ivCover.visibility = View.GONE
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (checkPersmissionResult(requestCode, permissions, grantResults)) {
            initCamera()
        }
    }

    fun onSwitchClick() {
        cameraImpl.switchCamera(object : ICamera.ReleaseCallBack {
            override fun onCameraRelease() {
                activity?.runOnUiThread {
                    ivCover.visibility = View.VISIBLE
                }
            }
        })

        isFrontCamera = !isFrontCamera
        fboHelper.setNeedFlip(
            isFrontCamera,
            cameraImpl.getOrientation(cameraImpl.getCurrentCameraId())
        )

    }

    override fun onDestroy() {
        cameraImpl.release(null)
        super.onDestroy()
    }

    public interface OnSurfaceStatusChangeListener {
        fun onCameraPreviewData(textureId: Int, renderFrameParams: MMRenderFrameParams): Int

        fun surfaceDestroyed()
        fun onCameraRelease()
    }
}