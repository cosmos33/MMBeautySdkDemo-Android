package com.tencent.liteav.demo.livepusher.camerapush.ui

import android.content.Context
import com.tencent.liteav.demo.livepusher.camerapush.CameraPushCustomVideoCaptureImpl
import com.tencent.liteav.demo.livepusher.camerapush.CameraPushTxVideoCaptureImpl
import com.tencent.liteav.demo.livepusher.camerapush.model.CameraPushImpl
import com.tencent.rtmp.ui.TXCloudVideoView

object CameraPushImplFactory {
    var useTxVideoCapture = false
    fun createPushImpl(
        context: Context,
        pusherView: TXCloudVideoView,
        screenRotation: Int
    ): CameraPushImpl {
        return if (useTxVideoCapture) {
            CameraPushTxVideoCaptureImpl(
                context,
                pusherView,
                screenRotation
            )
        } else {
            CameraPushCustomVideoCaptureImpl(
                context,
                pusherView,
                screenRotation
            )
        }
    }
}