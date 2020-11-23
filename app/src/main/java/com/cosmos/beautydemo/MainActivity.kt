package com.cosmos.beautydemo

import android.graphics.SurfaceTexture
import android.opengl.EGLSurface
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.core.glcore.cv.MMCVInfo
import com.core.glcore.util.ImageFrame
import com.cosmos.beauty.CosmosBeautySDK
import com.cosmos.beauty.inter.OnAuthenticationStateListener
import com.cosmos.beauty.inter.OnBeautyResourcePreparedListener
import com.cosmos.beauty.model.AuthResult
import com.cosmos.beauty.model.BeautySDKInitConfig
import com.cosmos.beauty.model.MMRenderFrameParams
import com.cosmos.beauty.model.datamode.CameraDataMode
import com.cosmos.beauty.module.IMMRenderModuleManager
import com.cosmos.beauty.module.beauty.IBeautyModule
import com.cosmos.beauty.module.beauty.SimpleBeautyType
import com.cosmos.beauty.module.lookup.ILookupModule
import com.cosmos.beauty.module.sticker.DetectRect
import com.cosmos.beauty.module.sticker.IStickerModule
import com.cosmos.beautydemo.camera.CameraImpl
import com.cosmos.beautydemo.camera.CameraManager
import com.cosmos.beautydemo.camera.ICamera
import com.cosmos.beautydemo.camera.callback.OnPreviewDataCallback
import com.cosmos.beautydemo.filter.DirectDrawer
import com.cosmos.beautydemo.filter.FBOHelper
import com.cosmos.beautydemo.fragment.AspectFrameLayout
import com.cosmos.beautydemo.fragment.beautytype.BeautyTypeFragment
import com.cosmos.beautydemo.fragment.filter.LookupFragment
import com.cosmos.beautydemo.fragment.sticker.StickerFragment
import com.cosmos.beautydemo.gl.EGLHelper
import com.cosmos.beautydemo.gl.GLUtils
import com.mm.mmutil.toast.Toaster

class MainActivity : AppCompatActivity(), OnPreviewDataCallback,
    IMMRenderModuleManager.CVModelStatusListener, IMMRenderModuleManager.IDetectGestureCallback,
    IMMRenderModuleManager.IDetectFaceCallback {
    private val TAG = "MainActivity_Detect"
    val cosmosAppId = "cosmos后台的appid"

    private val SHOW_BEAUTY = 1
    private val SHOW_FILTER = 2
    private val SHOW_FACEMASK = 3

    private var textureId: Int = -1
    private val cameraRequestCode = 12300
    private val surfaceView by lazy { findViewById<SurfaceView>(R.id.surfaceView) }
    private val ivCover: ImageView by lazy { findViewById<ImageView>(R.id.ivCover) }
    private val tvBeauty: TextView by lazy { findViewById<TextView>(R.id.tvBeauty) }
    private val tvFilter: TextView by lazy { findViewById<TextView>(R.id.tvLookup) }
    private val tvFaceMask: TextView by lazy { findViewById<TextView>(R.id.tvFaceSticker) }
    private val cameraPreview: AspectFrameLayout by lazy { findViewById<AspectFrameLayout>(R.id.cameraPreview_afl) }
    private val flResourcePrepare: FrameLayout by lazy { findViewById<FrameLayout>(R.id.flResourcePrepare) }


    private var beautyModule: IBeautyModule? = null
    private var lookupModule: ILookupModule? = null
    private lateinit var lookupModule2: ILookupModule// test multi lookupmodule
    private var stickerModule: IStickerModule? = null
    private var renderModuleManager: IMMRenderModuleManager? = null
    private lateinit var cameraImpl: CameraManager

    private lateinit var surfaceTexture: SurfaceTexture
    private var eglSurface: EGLSurface? = null
    private lateinit var fboHelper: FBOHelper
    private var width = 0
    private var height = 0
    private var directDrawer: DirectDrawer? = null
    private var mtx = FloatArray(16)
    private var cameraId = 0
    private lateinit var fragmentLookup: LookupFragment
    private lateinit var fragmentBeauty: BeautyTypeFragment
    private lateinit var fragmentSticker: StickerFragment
    private var showFragment = 0
    private var isFrontCamera = true
    private var isInit = false
    private var cameraHander: Handler? = null
    private val holderWidth = 720
    private val holderHeight = 1280
    private val coastUtil by lazy { CostUtil("RenderTime") }
    private val TRY_LOAD_CVMODEL = 3
    private var loadCVCount = 0
    private var authSuccess = false
    private var filterResouceSuccess = false
    private var cvModelSuccess = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val handlerThread = HandlerThread("camera thread")
        handlerThread.start()
        cameraHander = Handler(handlerThread.looper)
        cameraImpl = CameraManager(cameraHander!!, CameraImpl())
        cameraImpl.init(this)
        tvBeauty.visibility = View.VISIBLE
        tvFilter.visibility = View.VISIBLE
        tvFaceMask.visibility = View.VISIBLE
        init()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (cameraImpl.checkPersmissionResult(requestCode, permissions, grantResults)) {
            initCamera()
        }
    }

    private fun init() {
        initView()
        initSDK()
    }

    private fun initView() {
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {
                this@MainActivity.width = width
                this@MainActivity.height = height
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                cameraImpl.stopPreview()
                cameraImpl.release(object : ICamera.ReleaseCallBack {
                    override fun onCameraRelease() {
                        releaseRender()
                        cameraHander?.post {
                            renderModuleManager?.destroyModuleChain()
                            renderModuleManager?.release()
                        }
                    }
                })
                surfaceTexture.release()
                directDrawer = null
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                holder?.setFixedSize(holderWidth, holderHeight)
                if (cameraImpl.checkAndRequestPermission(cameraRequestCode)) {
                    initCamera()
                }
            }
        })
        fragmentBeauty =
            supportFragmentManager.findFragmentById(R.id.fragmentBeautyType)!! as BeautyTypeFragment
        fragmentLookup =
            supportFragmentManager.findFragmentById(R.id.fragmentFilter)!! as LookupFragment
        fragmentSticker =
            supportFragmentManager.findFragmentById(R.id.fragmentFaceMask)!! as StickerFragment

        fragmentSticker.setResouceReadyCallBack(object : StickerFragment.OnStickerResouceCallback {
            override fun isStickerResourceReady() {
                checkResouceReady()
            }

        })
        fragmentLookup.setVisible(false)
        fragmentSticker.setVisible(false)
    }

    private fun initCamera() {
        cameraPreview.setAspectRatio(720 * 1.0 / 1280)
        textureId = GLUtils.generateTexure()
        surfaceTexture = SurfaceTexture(textureId)
        cameraImpl.open(true)
        cameraImpl.setPreviewSize(Size(1280, 720))
        cameraImpl.setPreviewFps(30, 30)
        cameraImpl.preview(surfaceTexture, this)
        isFrontCamera = true
    }

    private fun initSDK() {
        val beautySDKInitConfig = BeautySDKInitConfig.Builder(cosmosAppId)
            .setUserVersionCode(BuildConfig.VERSION_CODE)
            .setUserVersionName(BuildConfig.VERSION_NAME)
            .build()
        CosmosBeautySDK.init(this, beautySDKInitConfig, object : OnAuthenticationStateListener {
            override fun onResult(result: AuthResult) {
                if (!result.isSucceed) {
                    Toaster.show("授权失败")
                } else {
                    runOnUiThread {
                        authSuccess = true
                        checkResouceReady()
                        initRender()
                        isInit = true
                    }
                }
            }
        }, object : OnBeautyResourcePreparedListener {
            override fun onResult(isSucceed: Boolean) {
                if (!isSucceed) {
                    Toaster.show("resource false")
                }
            }
        })

        FilterUtils.prepareResource(this, object : OnFilterResourcePrepareListener {
            override fun onFilterReady() {
                filterResouceSuccess = true
                checkResouceReady()
            }
        })
    }

    override fun onResume() {
        if (isInit) {
            initRender()
        }
        super.onResume()
    }

    override fun onCvModelStatus(loadCvModelSuccess: Boolean) {
        if (!loadCvModelSuccess) {
            if (loadCVCount >= TRY_LOAD_CVMODEL) {
                Toaster.show("load cv model failed!")
                return
            }
            loadCVCount++
            renderModuleManager?.prepare(true, this, this, this)
            return
        }
        Toaster.show("load cv model success!")
        cvModelSuccess = true
        checkResouceReady()
    }

    override fun onDetectGesture(type: String, detect: DetectRect) {
        LogUtil.v(
            TAG, "onDetectGesture :$type rect{x = ${detect.x},y = ${detect.y}" +
                    ",width = ${detect.width},height = {${detect.height}} }"
        )
    }

    override fun onGestureMiss() {
        LogUtil.v(
            TAG, "onDetectGesture :miss"
        )
    }

    override fun onDetectHead(info: MMCVInfo?) {
        LogUtil.v(TAG, "onDetectHead: count = $info")
    }
    private fun initRender() {
        renderModuleManager = CosmosBeautySDK.createRenderModuleManager()
        tvBeauty.visibility = View.VISIBLE
        tvFilter.visibility = View.VISIBLE
        tvFaceMask.visibility = View.VISIBLE

        renderModuleManager?.prepare(true, this, this, this)
    }

    override fun onPreviewData(data: ByteArray) {
        if (cameraImpl.getPreviewSize() == null) {
            return
        }
        if (directDrawer == null) {
            EGLHelper.instance.init()
            eglSurface = EGLHelper.instance.genEglSurface(surfaceView.holder)
            EGLHelper.instance.makeCurrent(eglSurface)
            fboHelper = FBOHelper(width, height)
            fboHelper.setNeedFlip(
                isFrontCamera,
                cameraImpl.getOrientation(cameraImpl.getCurrentCameraId())
            )
            directDrawer = DirectDrawer()
        }
        val orientation = cameraImpl.getOrientation(cameraImpl.getCurrentCameraId())
        var resultTexture = fboHelper.update(data, orientation, cameraImpl.getPreviewSize())
        if (renderModuleManager != null) {
            coastUtil.start()
            val renderFrameParams = MMRenderFrameParams(
                CameraDataMode(
                    isFrontCamera,
                    cameraImpl.getCameraRotation()
                ),
                data,
                cameraImpl.getPreviewSize()!!.width,
                cameraImpl.getPreviewSize()!!.height,
                holderWidth,
                holderHeight,

                ImageFrame.MMFormat.FMT_NV21
            )
            resultTexture = renderModuleManager?.renderFrame(resultTexture, renderFrameParams)!!
            coastUtil.end()
        }
        surfaceTexture.getTransformMatrix(mtx)
        directDrawer?.draw(resultTexture, mtx, orientation)
        surfaceTexture.updateTexImage()
        EGLHelper.instance.swapBuffers(eglSurface)
        if (ivCover.visibility != View.GONE) {
            runOnUiThread {
                ivCover.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        cameraImpl.release(null)
        releaseRender()
        super.onDestroy()
    }

    private fun releaseRender() {
        if (beautyModule != null) {
            renderModuleManager?.unRegisterModule(beautyModule!!)
        }
        if (lookupModule != null) {
            renderModuleManager?.unRegisterModule(lookupModule!!)
        }
        if (stickerModule != null) {
            renderModuleManager?.unRegisterModule(stickerModule!!)
        }
    }

    fun onSwitchClick(view: View) {
        cameraImpl.switchCamera(object : ICamera.ReleaseCallBack {
            override fun onCameraRelease() {
                runOnUiThread {
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

    fun onLookupClick(view: View) {
        showFragment = SHOW_FILTER
        changeFragment()
    }

    fun onBeautyClick(view: View) {
        showFragment = SHOW_BEAUTY
        changeFragment()
    }

    fun onStickerClick(view: View) {
        showFragment = SHOW_FACEMASK
        changeFragment()
    }

    fun changeFragment() {
        when (showFragment) {
            SHOW_BEAUTY -> {
                fragmentBeauty.setVisible(true)
                fragmentLookup.setVisible(false)
                fragmentSticker.setVisible(false)
            }
            SHOW_FILTER -> {
                fragmentLookup.setVisible(true)
                fragmentBeauty.setVisible(false)
                fragmentSticker.setVisible(false)
            }
            SHOW_FACEMASK -> {
                fragmentSticker.setVisible(true)
                fragmentBeauty.setVisible(false)
                fragmentLookup.setVisible(false)
            }
        }
    }

    private fun initStickerModule() {
        stickerModule = CosmosBeautySDK.createStickerModule()
        renderModuleManager?.registerModule(stickerModule!!)

        fragmentSticker.setFaceMaskModule(stickerModule!!)
    }

    private fun initLookupModule() {
        lookupModule = CosmosBeautySDK.createLoopupModule()
        renderModuleManager?.registerModule(lookupModule!!)

        lookupModule2 = CosmosBeautySDK.createLoopupModule()
        renderModuleManager?.registerModule(lookupModule2)
        fragmentLookup.setFilterModule(arrayOf(lookupModule!!, lookupModule2))
    }

    private fun initBeautyModule() {
        beautyModule = CosmosBeautySDK.createBeautyModule()
        renderModuleManager?.registerModule(beautyModule!!)

        beautyModule!!.setValue(SimpleBeautyType.BIG_EYE, 0.0f)
        beautyModule!!.setValue(SimpleBeautyType.SKIN_WHITENING, 0.0f)
        beautyModule!!.setValue(SimpleBeautyType.SKIN_SMOOTH, 0.0f)
        beautyModule!!.setValue(SimpleBeautyType.THIN_FACE, 0.0f)
        beautyModule!!.setValue(SimpleBeautyType.RUDDY, 0.0f)

        fragmentBeauty.setBeautyModule(beautyModule!!)
    }

    private fun checkResouceReady() {
        if (authSuccess && filterResouceSuccess && cvModelSuccess && fragmentSticker.isStickerResourceReady()) {
            flResourcePrepare.visibility = View.GONE;
        }
        initBeautyModule()
        initLookupModule()
        initStickerModule()
    }

    fun onResourcePrepareClick(view: View) {}
    fun onStickerClearClick(view: View) {
        stickerModule?.clear()
    }
}
