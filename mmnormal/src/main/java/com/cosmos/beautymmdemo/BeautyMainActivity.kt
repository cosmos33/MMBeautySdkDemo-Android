package com.cosmos.beautymmdemo

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.core.glcore.cv.MMCVInfo
import com.cosmos.appbase.listener.OnFilterResourcePrepareListener
import com.cosmos.appbase.utils.FilterUtils
import com.cosmos.appbase.utils.LogUtil
import com.cosmos.appbase.utils.SaveByteToFile
import com.cosmos.beauty.CosmosBeautySDK
import com.cosmos.beauty.inter.OnAuthenticationStateListener
import com.cosmos.beauty.inter.OnBeautyResourcePreparedListener
import com.cosmos.beauty.model.AuthResult
import com.cosmos.beauty.model.BeautySDKInitConfig
import com.cosmos.beauty.model.MMRenderFrameParams
import com.cosmos.beauty.module.IMMRenderModuleManager
import com.cosmos.beauty.module.beauty.IBeautyModule
import com.cosmos.beauty.module.beauty.SimpleBeautyType
import com.cosmos.beauty.module.lookup.ILookupModule
import com.cosmos.beauty.module.sticker.DetectRect
import com.cosmos.beauty.module.sticker.IStickerModule
import com.cosmos.beautymmdemo.fragment.SurfaceFragment
import com.cosmos.beautymmdemo.fragment.beautytype.BeautyTypeFragment
import com.cosmos.beautymmdemo.fragment.filter.LookupFragment
import com.cosmos.beautymmdemo.fragment.sticker.StickerFragment
import com.mm.mmutil.toast.Toaster

class BeautyMainActivity : AppCompatActivity(),
    IMMRenderModuleManager.CVModelStatusListener, IMMRenderModuleManager.IDetectGestureCallback,
    IMMRenderModuleManager.IDetectFaceCallback {
    private val TAG = "MainActivity_Detect"

    private val SHOW_BEAUTY = 1
    private val SHOW_FILTER = 2
    private val SHOW_FACEMASK = 3

    private val tvBeauty: TextView by lazy { findViewById<TextView>(R.id.tvBeauty) }
    private val tvFilter: TextView by lazy { findViewById<TextView>(R.id.tvLookup) }
    private val tvFaceMask: TextView by lazy { findViewById<TextView>(R.id.tvFaceSticker) }
    private val flResourcePrepare: FrameLayout by lazy { findViewById<FrameLayout>(R.id.flResourcePrepare) }
    private val cbEnableBeauty: CheckBox by lazy { findViewById<CheckBox>(R.id.cbEnableBeauty) }
    private val cbEnableLookup: CheckBox by lazy { findViewById<CheckBox>(R.id.cbEnableLookup) }
    private val cbEnableSticker: CheckBox by lazy { findViewById<CheckBox>(R.id.cbEnableSticker) }


    private var beautyModule: IBeautyModule? = null
    private var lookupModule: ILookupModule? = null
    private lateinit var lookupModule2: ILookupModule// test multi lookupmodule
    private var stickerModule: IStickerModule? = null
    private var renderModuleManager: IMMRenderModuleManager? = null
    private var cameraId = 0
    private lateinit var fragmentLookup: LookupFragment
    private lateinit var fragmentBeauty: BeautyTypeFragment
    private lateinit var fragmentSticker: StickerFragment
    private lateinit var fragmentSurface: SurfaceFragment
    private var showFragment = 0
    private var isInit = false
    private val TRY_LOAD_CVMODEL = 3
    private var loadCVCount = 0
    private var authSuccess = false
    private var filterResouceSuccess = false
    private var cvModelSuccess = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSDK()
        setContentView(R.layout.activity_main_mm)
        tvBeauty.visibility = View.VISIBLE
        tvFilter.visibility = View.VISIBLE
        tvFaceMask.visibility = View.VISIBLE
        init()
    }

    private fun init() {
        initView()
    }

    private var saveByteToFile = SaveByteToFile()
    private fun initView() {
        fragmentSurface =
            supportFragmentManager.findFragmentById(R.id.fragmentSurface)!! as SurfaceFragment

        fragmentSurface.onSurfaceStatusChangeListener =
            object : SurfaceFragment.OnSurfaceStatusChangeListener {
                override fun onCameraPreviewData(
                    resultTexture: Int,
                    renderFrameParams: MMRenderFrameParams
                ): Int {
//                    saveByteToFile.saveByte(baseContext, data)
                    if (renderModuleManager != null) {
                        //美颜sdk处理
                        return renderModuleManager?.renderFrame(resultTexture, renderFrameParams)!!
                    }
                    return resultTexture
                }

                override fun onCameraRelease() {
                    renderModuleManager?.destroyModuleChain()
                    renderModuleManager?.release()
                }

                override fun surfaceDestroyed() {
                    cbEnableSticker.isChecked = false
                    cbEnableLookup.isChecked = false
                    cbEnableBeauty.isChecked = false
                }

            }
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

    private fun initSDK() {
        val beautySDKInitConfig = BeautySDKInitConfig.Builder(BeautyApplication.cosmosAppId)
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

        FilterUtils.prepareFilterResource(this, object : OnFilterResourcePrepareListener {
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

    override fun onDestroy() {
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
        fragmentSurface.onSwitchClick()
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

    fun onEnableBeautyClick(view: View) {
        if ((view as CheckBox).isChecked) {
            initBeautyModule()
        } else {
            renderModuleManager?.unRegisterModule(beautyModule!!)
        }
    }

    fun onEnableLookUpClick(view: View) {
        if ((view as CheckBox).isChecked) {
            initLookupModule()
        } else {
            renderModuleManager?.unRegisterModule(lookupModule!!)
            renderModuleManager?.unRegisterModule(lookupModule2)
        }
    }

    fun onEnableStickerClick(view: View) {
        if ((view as CheckBox).isChecked) {
            initStickerModule()
        } else {
            renderModuleManager?.unRegisterModule(stickerModule!!)
        }
    }

    private fun initStickerModule() {
        stickerModule = CosmosBeautySDK.createStickerModule()
        renderModuleManager?.registerModule(stickerModule!!)

        fragmentSticker.setStickerModule(stickerModule!!)
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
    }

    fun onResourcePrepareClick(view: View) {}
    fun onStickerClearClick(view: View) {
        stickerModule?.clear()
    }
}
