package com.tencent.liteav.demo.livepusher.camerapush.readpixel

import android.opengl.GLES30
import android.os.Build
import androidx.annotation.RequiresApi
import java.nio.ByteBuffer

@RequiresApi(Build.VERSION_CODES.N)
class ReadPixelsN : IReadPixels {
    private val pboArray by lazy { IntArray(2) }
    private var lastTexWidth = -1
    private var lastTexHeight = -1
    private var lastSize = -1
    private var readIndex = 0
    private var mapIndex = 1


    override fun getFrameData(fbo: Int, texWidth: Int, texHeight: Int): ByteArray? {
        initPbo(texWidth, texHeight)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pboArray[readIndex])
        GLES30.glReadPixels(
            0,
            0,
            texWidth,
            texHeight,
            GLES30.GL_RGBA,
            GLES30.GL_UNSIGNED_BYTE,
            0
        )

        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pboArray[mapIndex])
        var resultBuffer = GLES30.glMapBufferRange(
            GLES30.GL_PIXEL_PACK_BUFFER,
            0,
            lastSize,
            GLES30.GL_MAP_READ_BIT
        )

        GLES30.glUnmapBuffer(GLES30.GL_PIXEL_PACK_BUFFER)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        readIndex = (readIndex + 1) % 2
        mapIndex = (mapIndex + 1) % 2
        if (resultBuffer == null) {
            return null
        }
        var result = ByteArray(resultBuffer.remaining())
        (resultBuffer as ByteBuffer).get(result)
        return result
    }

    private fun initPbo(texWidth: Int, texHeight: Int) {
        if (lastTexWidth == texWidth && lastTexHeight == texHeight) {
            return
        }
        lastTexHeight = texHeight
        lastTexWidth = texWidth
        lastSize = texHeight * texWidth * 4
        GLES30.glGenBuffers(2, pboArray, 0)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pboArray[0])
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, lastSize, null, GLES30.GL_STATIC_READ)

        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pboArray[1])
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, lastSize, null, GLES30.GL_STATIC_READ)

        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)
    }
}