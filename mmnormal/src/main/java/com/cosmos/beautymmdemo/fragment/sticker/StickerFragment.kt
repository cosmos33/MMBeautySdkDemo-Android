package com.cosmos.beautymmdemo.fragment.sticker

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cosmos.appbase.listener.OnStickerResourcePrepareListener
import com.cosmos.appbase.utils.FilterUtils
import com.cosmos.beauty.module.sticker.IStickerModule
import com.cosmos.beauty.module.sticker.MaskLoadCallback
import com.cosmos.beautymmdemo.R
import com.cosmos.beautymmdemo.fragment.sticker.adapter.StickerAdapter
import com.cosmos.beautymmdemo.fragment.sticker.adapter.StickerData
import com.fanyiran.utils.base.RvBaseFragment
import com.fanyiran.utils.recycleadapter.ItemData
import com.fanyiran.utils.recycleadapter.RvBaseAdapter
import com.fanyiran.utils.recycleadapter.RvListenerImpl
import com.mm.mmutil.toast.Toaster
import java.io.File

class StickerFragment : RvBaseFragment() {
    private lateinit var recyclerView: RecyclerView
    private var stickerModules: IStickerModule? = null
    private lateinit var rootView: View
    private var stickerSuccess = false;
    private var onStickerResouceCallback: OnStickerResouceCallback? = null

    private val stickerData by lazy { mutableListOf<StickerData>() }
    private val filterAdapter: StickerAdapter by lazy {
        StickerAdapter(
            stickerData
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
        fakeDownloadSticker()
    }

    private fun initFilterData(path: String) {
        stickerData.add(StickerData("彩虹1", "$path/rainbow"))
        stickerData.add(StickerData("漫画", "$path/manhua"))
        stickerData.add(StickerData("彩虹2", "$path/rainbow_vertical"))
        stickerData.add(StickerData("彩虹_engine", "$path/rainbow_engine"))
        stickerData.add(StickerData("凉凉", "$path/liangliang"))
        stickerData.add(StickerData("悲伤心情", "$path/sad"))
        stickerData.add(StickerData("愉快心情", "$path/happy"))
        stickerData.add(StickerData("嘻哈女", "$path/xiha"))
        stickerData.add(StickerData("慌的一比", "$path/hurry"))
        stickerData.add(StickerData("无言以对", "$path/nosay"))
        stickerData.add(StickerData("pick me", "$path/pickme"))
        stickerData.add(StickerData("可爱本人", "$path/cute"))
        stickerData.add(StickerData("666", "$path/666"))
        stickerData.add(StickerData("冷", "$path/cold"))
        stickerData.add(StickerData("比心", "$path/bixin"))
        stickerData.add(StickerData("双手比心", "$path/shuangshoubixin"))
        stickerData.add(StickerData("手控樱花", "$path/shoukongyinghua"))
        stickerData.add(StickerData("打电话", "$path/dadianhua"))
        stickerData.add(StickerData("点赞", "$path/dianzan"))
        stickerData.add(StickerData("剪刀手", "$path/jiandaoshou"))
        stickerData.add(StickerData("ok", "$path/ok"))
        stickerData.add(StickerData("拳头", "$path/quantou"))
        stickerData.add(StickerData("微笑", "$path/weixiao"))
        stickerData.add(StickerData("一个手指", "$path/yigeshouzhi"))
        stickerData.add(StickerData("biba", "$path/biba"))
        stickerData.add(StickerData("baolian", "$path/baolian"))
        stickerData.add(StickerData("一拜年", "$path/bainian"))
        filterAdapter.notifyDataSetChanged()
        filterAdapter.setRvListener(object : RvListenerImpl() {
            override fun onClick(view: View?, data: ItemData?, position: Int) {
                stickerModules?.addMaskModel(
                    File((data as StickerData).stickerPath),
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

    private fun fakeDownloadSticker() {
        FilterUtils.prepareStikcerResource(context, object : OnStickerResourcePrepareListener {
            override fun onStickerReady(rootPath: String) {
                recyclerView.post {
                    stickerSuccess = true
                    onStickerResouceCallback?.isStickerResourceReady()
                    initFilterData(rootPath)
                }
            }
        })

    }

    fun setVisible(visible: Boolean) {
        rootView.visibility = (if (visible) View.VISIBLE else View.GONE)
    }

    fun setStickerModule(stickerModules: IStickerModule) {
        this.stickerModules = stickerModules
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
