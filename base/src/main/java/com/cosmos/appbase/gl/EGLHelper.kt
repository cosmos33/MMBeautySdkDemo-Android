package com.cosmos.appbase.gl

import android.opengl.*
import com.cosmos.appbase.utils.LogUtil
import javax.microedition.khronos.egl.EGL10

class EGLHelper private constructor() {
    var eGLDislplay: EGLDisplay? = null
        private set
    private lateinit var configs: Array<EGLConfig?>
    var eglContext: EGLContext? = null
        private set

    fun makeCurrent(eglSurface: EGLSurface?) {
        if (eGLDislplay == null || eglContext == null) {
            LogUtil.v(TAG, "eglMakeCurrent: display or eglContext may be null")
            return
        }
        val result =
            EGL14.eglMakeCurrent(eGLDislplay, eglSurface, eglSurface, eglContext)
        if (!result) {
            LogUtil.v(
                TAG,
                String.format("eglMakeCurrent: %d!", EGL14.eglGetError())
            )
        }
    }

    fun swapBuffers(eglSurface: EGLSurface?): Boolean {
        if (eGLDislplay == null) {
            LogUtil.v(TAG, "swapBuffers: display is null")
            return false
        }
        return EGL14.eglSwapBuffers(eGLDislplay, eglSurface)
    }

    fun setPresentationTime(
        eglSurface: EGLSurface?,
        time: Long
    ) {
        EGLExt.eglPresentationTimeANDROID(eGLDislplay, eglSurface, time)
    }

    internal object EGLHelperWrapper {
        var eglHelper = EGLHelper()
    }

    public fun init(): Int {
        eGLDislplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (eGLDislplay === EGL14.EGL_NO_DISPLAY) {
            LogUtil.v(TAG, "eglGetDisplay failed!")
            return -1
        }
        val version = IntArray(2)
        var result =
            EGL14.eglInitialize(eGLDislplay, version, 0, version, 1)
        if (!result) {
            LogUtil.v(TAG, "initialize failed!")
            return -1
        }
        val num_config = IntArray(1)
        val attrib_list = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL_RECORDABLE_ANDROID, 1,  // placeholder for recordable [@-3]
            EGL14.EGL_NONE
        )
        configs = arrayOfNulls(1)
        result = EGL14.eglChooseConfig(
            eGLDislplay,
            attrib_list,
            0,
            configs,
            0,
            configs.size,
            num_config,
            0
        )
        if (!result) {
            LogUtil.v(TAG, "eglGetConfigs failed!")
            return -1
        }
        val attrList = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        eglContext = EGL14.eglCreateContext(
            eGLDislplay,
            configs[0],
            EGL14.EGL_NO_CONTEXT,
            attrList,
            0
        )
        if (eglContext === EGL14.EGL_NO_CONTEXT) {
            LogUtil.v(TAG, "eglCreateContext failed!")
            return -1
        }
        return 0
    }

    fun genEglSurface(`object`: Any?): EGLSurface? {
        val surfaceAttr = intArrayOf(
            EGL14.EGL_NONE
        )
        var eglSurface: EGLSurface? = null

        if (`object` == null) {
            val surfaceAttribs = intArrayOf(
                EGL10.EGL_WIDTH, 1,
                EGL10.EGL_HEIGHT, 1,
                EGL14.EGL_NONE
            )

            eglSurface = EGL14.eglCreatePbufferSurface(
                eGLDislplay,
                configs[0],
                surfaceAttr,
                0
            )
        } else {
            eglSurface = EGL14.eglCreateWindowSurface(
                eGLDislplay,
                configs[0],
                `object`,
                surfaceAttr,
                0
            )
        }
        if (eglSurface == null || !check()) {
            LogUtil.v(TAG, "eglCreateWindowSurface failed!")
            return null
        }
        return eglSurface
    }

    private fun check(): Boolean {
        val error = EGL14.eglGetError()
        if (error != EGL14.EGL_SUCCESS) {
            LogUtil.v(TAG, String.format("eglGetError: 0x%x!", error))
            return false
        }
        return true
    }

    companion object {
        private const val TAG = "EGLHelper"

        // Android-specific extension.
        private const val EGL_RECORDABLE_ANDROID = 0x3142
        val instance: EGLHelper
            get() = EGLHelperWrapper.eglHelper
    }

    init {
//        init()
    }
}