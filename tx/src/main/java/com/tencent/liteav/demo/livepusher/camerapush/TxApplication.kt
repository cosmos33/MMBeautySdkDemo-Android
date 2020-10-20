package com.tencent.liteav.demo.livepusher.camerapush

import android.content.Context
import com.tencent.rtmp.TXLiveBase

object TxApplication {
    var licenceUrl =
        "http://license.vod2.myqcloud.com/license/v1/01d65cf7f3dd34339328c7219f7a5517/TXLiveSDK.licence"
    var licenseKey = "ddbbe1446d9fb08dfe993081fda61760"

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