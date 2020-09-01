package com.cosmos.beautydemo.fragment.sticker

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cosmos.beauty.module.sticker.IStickerModule
import com.cosmos.beauty.module.sticker.MaskLoadCallback
import com.cosmos.beautydemo.R
import com.cosmos.beautydemo.fragment.sticker.adapter.StickerAdapter
import com.cosmos.beautydemo.fragment.sticker.adapter.StickerData
import com.cosmos.beautydemo.recycleadapter.RvBaseAdapter
import com.cosmos.beautydemo.recycleadapter.RvBaseFragment
import com.cosmos.beautydemo.recycleadapter.RvListenerImpl
import com.mm.mmutil.FileUtil
import com.mm.mmutil.task.ThreadUtils
import com.mm.mmutil.toast.Toaster
import java.io.File

class StickerFragment : RvBaseFragment() {
    private lateinit var recyclerView: RecyclerView
    private var facemaskModules: IStickerModule? = null
    private lateinit var rootView: View
    private var stickerSuccess = false;
    private var onStickerResouceCallback: OnStickerResouceCallback? = null

    private val faceMaskData by lazy { mutableListOf<StickerData>() }
    private val filterAdapter: StickerAdapter by lazy {
        StickerAdapter(
            faceMaskData
        )
    }

    override fun getAdapter(): RvBaseAdapter<*> {
        return filterAdapter
    }

    override fun getRecycleView(): RecyclerView {
        return recyclerView
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_sticker_type
    }

    override fun getLayoutManager(): RecyclerView.LayoutManager? {
        return LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    }

    override fun initView(view: View?) {
        rootView = view!!
        recyclerView = view.findViewById(R.id.beautyTypeRecyclerView)!!
        fakeDownloadFaceMask()
    }

    private fun initFilterData(path: String) {
        var temp = StickerData("彩虹1", "$path/rainbow")
        faceMaskData.add(temp)
        temp = StickerData("彩虹2", "$path/rainbow_vertical")
        faceMaskData.add(temp)
        temp = StickerData("凉凉", "$path/liangliang")
        faceMaskData.add(temp)
        temp = StickerData("悲伤心情", "$path/sad")
        faceMaskData.add(temp)
        temp = StickerData("愉快心情", "$path/happy")
        faceMaskData.add(temp)
        temp = StickerData("嘻哈", "$path/xiha")
        faceMaskData.add(temp)
        temp = StickerData("慌的一比", "$path/hurry")
        faceMaskData.add(temp)
        temp = StickerData("无言以对", "$path/nosay")
        faceMaskData.add(temp)
        temp = StickerData("pick me", "$path/pickme")
        faceMaskData.add(temp)
        temp = StickerData("可爱本人", "$path/cute")
        faceMaskData.add(temp)
        temp = StickerData("666", "$path/666")
        faceMaskData.add(temp)
        temp = StickerData("冷", "$path/cold")
        faceMaskData.add(temp)
        temp = StickerData("手控樱花", "$path/shoukongyinghua")
        faceMaskData.add(temp)
        temp = StickerData("打电话", "$path/dadianhua")
        faceMaskData.add(temp)
        temp = StickerData("点赞", "$path/dianzan")
        faceMaskData.add(temp)
        temp = StickerData("剪刀手", "$path/jiandaoshou")
        faceMaskData.add(temp)
        temp = StickerData("ok", "$path/ok")
        faceMaskData.add(temp)
        temp = StickerData("拳头", "$path/quantou")
        faceMaskData.add(temp)
        temp = StickerData("微笑", "$path/weixiao")
        faceMaskData.add(temp)
        temp = StickerData("一个手指", "$path/yigeshouzhi")
        faceMaskData.add(temp)
        temp = StickerData("biba", "$path/biba")
        faceMaskData.add(temp)
        temp = StickerData("一拜年", "$path/bainian")
        faceMaskData.add(temp)
        filterAdapter.notifyDataSetChanged()
        filterAdapter.setRvListener(object : RvListenerImpl() {
            override fun onClick(view: View?, data: Any?, position: Int) {
                facemaskModules?.addMaskModel(
                    File((data as StickerData).faceMaskPath),
                    MaskLoadCallback {
                        Toaster.show(
                            if (it == null) "切换失败" else {
                                "切换为${(data as StickerData).name}"
                            }
                        )
                    }
                )
            }
        })
    }

    private fun fakeDownloadFaceMask() {
        ThreadUtils.execute(
            ThreadUtils.TYPE_RIGHT_NOW
        ) {
            val filterDir = context!!.filesDir.absolutePath + "/facemasksource"
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
            recyclerView.post {
                stickerSuccess = true
                onStickerResouceCallback?.isStickerResourceReady()
                initFilterData(filterDir)
            }
        }

    }

    fun setVisible(visible: Boolean) {
        rootView.visibility = (if (visible) View.VISIBLE else View.GONE)
    }

    fun setFaceMaskModule(stickerModules: IStickerModule) {
        this.facemaskModules = stickerModules
    }

    fun isStickerResourceReady(): Boolean {
        return stickerSuccess
    }

    fun setResouceReadyCallBack(onStickerResouceCallback: OnStickerResouceCallback) {
        this.onStickerResouceCallback = onStickerResouceCallback
    }

    interface OnStickerResouceCallback {
        fun isStickerResourceReady()
    }
}
