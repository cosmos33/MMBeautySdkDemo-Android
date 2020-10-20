package com.tencent.liteav.demo.livepusher.camerapush.readpixel

interface IReadPixels {
    fun getFrameData(fbo: Int, texWidth: Int, texHeight: Int): ByteArray?
}