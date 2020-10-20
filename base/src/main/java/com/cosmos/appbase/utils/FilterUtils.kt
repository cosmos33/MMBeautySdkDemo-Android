package com.cosmos.appbase.utils

import android.content.Context
import com.cosmos.appbase.listener.OnFilterResourcePrepareListener
import com.cosmos.appbase.listener.OnStickerResourcePrepareListener
import com.immomo.doki.DokiContextHolder
import com.mm.mmutil.FileUtil
import com.mm.mmutil.task.ThreadUtils
import java.io.File

object FilterUtils {
    private val FILENAME = "filterData.zip"
    val MOMENT_FILTER_FILE = "filterData"

    fun prepareFilterResource(
        context: Context?,
        onFilterResourcePrepareListener: OnFilterResourcePrepareListener?
    ) {
        ThreadUtils.execute(
            ThreadUtils.TYPE_RIGHT_NOW
        ) {
            val filterDir =
                getFilterHomeDir()
            if (!filterDir.exists() || filterDir.list().size <= 0) {
                if (filterDir.exists()) {
                    FileUtil.deleteDir(filterDir)
                }
                FileUtil.copyAssets(
                    context,
                    FILENAME,
                    File(
                        getBeautyDirectory(),
                        FILENAME
                    )
                )
                FileUtil.unzip(
                    File(
                        getBeautyDirectory(),
                        FILENAME
                    ).absolutePath,
                    getBeautyDirectory()?.absolutePath,
                    false
                )
            }
            onFilterResourcePrepareListener?.onFilterReady()
        }
    }

    fun getBeautyDirectory(): File? {
        return File(
            DokiContextHolder.getAppContext().filesDir?.absolutePath,
            "/beauty"
        )
    }

    fun getFilterHomeDir(): File {
        var dir = File(
            getBeautyDirectory(),
            MOMENT_FILTER_FILE
        );
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir;
    }

    fun prepareStikcerResource(
        context: Context?,
        onStickerResourcePrepareListener: OnStickerResourcePrepareListener
    ) {
        ThreadUtils.execute(
            ThreadUtils.TYPE_RIGHT_NOW
        ) {
            val filterDir = context?.filesDir?.absolutePath + "/facemasksource"
            if (!File(filterDir).exists()) {
                File(filterDir).mkdirs()
                var file = File(filterDir, "facemask.zip")
                file.createNewFile()
                FileUtil.copyAssets(context, "facemask.zip", file)
                FileUtil.unzip(
                    File(filterDir, "facemask.zip").absolutePath,
                    filterDir,
                    false
                )
            }
            onStickerResourcePrepareListener.onStickerReady(filterDir)
        }

    }
}