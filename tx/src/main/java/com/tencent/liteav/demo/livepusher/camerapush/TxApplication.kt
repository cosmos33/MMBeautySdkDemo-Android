package com.tencent.liteav.demo.livepusher.camerapush

import android.content.Context
import com.tencent.rtmp.TXLiveBase

object TxApplication {
    var licenceUrl =
        "http://license.vod2.myqcloud.com/license/v1/ff6a0fcbf52804e5f9ef60679a606e71/TXLiveSDK.licence"
    var licenseKey = "52e64dcc93d73caf9dbe783a9c2981f4"

    var context: Context? = null
    var cosmosAppId: String = ""

    fun onCreate(context: Context, cosmosAppId: String) {
        TxApplication.context = context
        this.cosmosAppId = cosmosAppId
        TXLiveBase.setConsoleEnabled(true)
        TXLiveBase.getInstance()
            .setLicence(context, licenceUrl, licenseKey)
    }
}