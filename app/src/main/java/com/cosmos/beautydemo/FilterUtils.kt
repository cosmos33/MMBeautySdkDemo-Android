package com.cosmos.beautydemo

import android.content.Context
import com.mm.mmutil.FileUtil
import com.mm.mmutil.task.ThreadUtils
import java.io.File

object FilterUtils {
    private val FILENAME = "filterData.zip"
    val MOMENT_FILTER_FILE = "filterData"

    fun prepareResource(
        context: Context?,
        onFilterResourcePrepareListener: OnFilterResourcePrepareListener?
    ) {
        ThreadUtils.execute(
            ThreadUtils.TYPE_RIGHT_NOW
        ) {
            val filterDir = getFilterHomeDir()
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
            MyApplication.context?.filesDir?.absolutePath,
            "/beauty"
        )
    }

    fun getFilterHomeDir(): File {
        var dir = File(getBeautyDirectory(), MOMENT_FILTER_FILE);
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir;
    }
}