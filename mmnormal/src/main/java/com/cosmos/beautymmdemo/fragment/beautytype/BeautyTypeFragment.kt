package com.cosmos.beautymmdemo.fragment.beautytype

import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cosmos.beauty.module.beauty.IBeautyModule
import com.cosmos.beauty.module.beauty.SimpleBeautyType
import com.cosmos.beautymmdemo.R
import com.cosmos.beautymmdemo.fragment.beautytype.adapter.BeautyTypeAdapter
import com.cosmos.beautymmdemo.fragment.beautytype.adapter.BeautyTypeData
import com.fanyiran.utils.base.RvBaseFragment
import com.fanyiran.utils.recycleadapter.ItemData
import com.fanyiran.utils.recycleadapter.RvBaseAdapter
import com.fanyiran.utils.recycleadapter.RvListenerImpl

class BeautyTypeFragment : RvBaseFragment() {
    private lateinit var beautyTypeSeekBar: SeekBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvValue: TextView
    private var beautyModule: IBeautyModule? = null
    private lateinit var rootView: View
    private var beautyType: SimpleBeautyType = SimpleBeautyType.BIG_EYE

    private val beautyTypeData by lazy { mutableListOf<BeautyTypeData>() }
    private val filterAdapter: BeautyTypeAdapter by lazy {
        BeautyTypeAdapter(
            beautyTypeData
        )
    }

    override fun getAdapter(): RvBaseAdapter<*> {
        return filterAdapter
    }

    override fun getRecycleView(): RecyclerView {
        return recyclerView
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_beauty_type
    }

    override fun getLayoutManager(): RecyclerView.LayoutManager? {
        return LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    }

    override fun initView(view: View?) {
        rootView = view!!
        beautyTypeSeekBar = view?.findViewById(R.id.beautyTypeSeekBar)!!
        recyclerView = view.findViewById(R.id.beautyTypeRecyclerView)!!
        tvValue = view.findViewById(R.id.tvValue)!!
        beautyTypeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                var beautyValue = getBeautyValue(beautyType, progress)
                tvValue.text = "$beautyValue"
                beautyModule?.setValue(beautyType, beautyValue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        initFilterData()
    }

    private fun getBeautyValue(beautyType: SimpleBeautyType, progress: Int): Float {
        when (beautyType) {
            SimpleBeautyType.BIG_EYE,
            SimpleBeautyType.SKIN_SMOOTH,
            SimpleBeautyType.SKIN_WHITENING,
            SimpleBeautyType.THIN_FACE,
            SimpleBeautyType.RUDDY,
            SimpleBeautyType.FACE_WIDTH,
            SimpleBeautyType.SHORTEN_FACE,
            SimpleBeautyType.EYE_HEIGHT -> return progress / 100f
            else -> return (progress / 100f - 0.5f) * 2
        }
    }

    private fun initFilterData() {
        var temp = BeautyTypeData("大眼", SimpleBeautyType.BIG_EYE)
        beautyTypeData.add(temp)
        temp = BeautyTypeData("美白", SimpleBeautyType.SKIN_WHITENING)
        beautyTypeData.add(temp)
        temp = BeautyTypeData("磨皮", SimpleBeautyType.SKIN_SMOOTH)
        beautyTypeData.add(temp)
        temp = BeautyTypeData("瘦脸", SimpleBeautyType.THIN_FACE)
        beautyTypeData.add(temp)
        temp = BeautyTypeData("红润", SimpleBeautyType.RUDDY)
        beautyTypeData.add(temp)
        temp = BeautyTypeData("下巴", SimpleBeautyType.CHIN_LENGTH)
        beautyTypeData.add(temp)
        temp = BeautyTypeData("削脸", SimpleBeautyType.JAW_SHAPE)
        beautyTypeData.add(temp)
        temp = BeautyTypeData("脸宽", SimpleBeautyType.FACE_WIDTH)
        beautyTypeData.add(temp)
        temp = BeautyTypeData("额头", SimpleBeautyType.FOREHEAD)
        beautyTypeData.add(temp)
        temp = BeautyTypeData("短脸", SimpleBeautyType.SHORTEN_FACE)
        beautyTypeData.add(temp)
        temp = BeautyTypeData("眼睛角度", SimpleBeautyType.EYE_TILT)
        beautyTypeData.add(temp)
        temp = BeautyTypeData("眼距", SimpleBeautyType.EYE_DISTANCE)
        beautyTypeData.add(temp)
        temp = BeautyTypeData("鼻高", SimpleBeautyType.NOSE_LIFT)
        beautyTypeData.add(temp)
        temp = BeautyTypeData("鼻子大小", SimpleBeautyType.NOSE_SIZE)
        beautyTypeData.add(temp)
        temp = BeautyTypeData("鼻子宽度", SimpleBeautyType.NOSE_WIDTH)
        beautyTypeData.add(temp)
        temp = BeautyTypeData("鼻梁", SimpleBeautyType.NOSE_RIDGE_WIDTH)
        beautyTypeData.add(temp)
        temp = BeautyTypeData("鼻尖", SimpleBeautyType.NOSE_TIP_SIZE)
        beautyTypeData.add(temp)
        temp = BeautyTypeData("嘴唇厚度", SimpleBeautyType.LIP_THICKNESS)
        beautyTypeData.add(temp)
        temp = BeautyTypeData("嘴唇大小", SimpleBeautyType.MOUTH_SIZE)
        beautyTypeData.add(temp)
//        temp = BeautyTypeData("祛法令纹", SimpleBeautyType.NASOLABIAL_FOLDS)
//        beautyTypeData.add(temp)
        temp = BeautyTypeData("眼高", SimpleBeautyType.EYE_HEIGHT)
        beautyTypeData.add(temp)
//        temp = BeautyTypeData("眼袋", SimpleBeautyType.SKIN_SMOOTHING_EYES)
//        beautyTypeData.add(temp)
        filterAdapter.notifyDataSetChanged()
        filterAdapter.setRvListener(object : RvListenerImpl() {
            override fun onClick(view: View?, data: ItemData?, position: Int) {
                beautyType = (data as BeautyTypeData).type
                beautyModule?.setValue(beautyType, beautyTypeSeekBar.progress / 100f)
            }
        })
    }

    fun setBeautyModule(filterModule: IBeautyModule) {
        this.beautyModule = filterModule
    }

    fun setVisible(visible: Boolean) {
        rootView.visibility = (if (visible) View.VISIBLE else View.GONE)
    }
}
