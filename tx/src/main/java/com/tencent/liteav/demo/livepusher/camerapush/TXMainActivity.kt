package com.tencent.liteav.demo.livepusher.camerapush

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.tencent.liteav.demo.livepusher.R
import com.tencent.liteav.demo.livepusher.camerapush.ui.CameraPushImplFactory
import com.tencent.liteav.demo.livepusher.camerapush.ui.CameraPushMainActivity

class TXMainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_tx)
    }

    fun onCustomVideoCaptureClick(view: View) {
        CameraPushImplFactory.useTxVideoCapture = false
        startActivity(Intent(this, CameraPushMainActivity::class.java))
    }

    fun onTxVideoCaptureClick(view: View) {
        CameraPushImplFactory.useTxVideoCapture = true
        startActivity(Intent(this, CameraPushMainActivity::class.java))
    }
}