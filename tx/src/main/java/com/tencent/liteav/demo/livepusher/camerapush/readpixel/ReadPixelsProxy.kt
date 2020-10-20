package com.tencent.liteav.demo.livepusher.camerapush.readpixel

import android.os.Build

object ReadPixelsProxy : IReadPixels {
    private var readPixelsImpl: IReadPixels? = null

    override fun getFrameData(fbo: Int, texWidth: Int, texHeight: Int): ByteArray? {
        if (readPixelsImpl == null) {
            readPixelsImpl = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ReadPixelsN()
            } else {
                ReadPixel()
            }
        }
        return readPixelsImpl!!.getFrameData(fbo, texWidth, texHeight)
    }
}