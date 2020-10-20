package com.tencent.liteav.demo.livepusher.camerapush.readpixel

import android.opengl.GLES30
import java.nio.ByteBuffer

class ReadPixel : IReadPixels {
    private var byteBuffer: ByteBuffer? = null
    private var lastTexWidth = -1
    private var lastTexHeight = -1

    override fun getFrameData(fbo: Int, texWidth: Int, texHeight: Int): ByteArray {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo)
        if (byteBuffer == null || lastTexWidth != texWidth || lastTexHeight != texHeight) {
            lastTexWidth = texWidth
            lastTexHeight = texHeight
            byteBuffer = ByteBuffer.allocate(texWidth * texHeight * 4)
        }
        byteBuffer?.clear()
        GLES30.glReadPixels(
            0,
            0,
            texWidth,
            texHeight,
            GLES30.GL_RGBA,
            GLES30.GL_UNSIGNED_BYTE,
            byteBuffer
        )
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        return byteBuffer!!.array()
    }
}