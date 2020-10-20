package com.cosmos.beautymmdemo.fragment.filter

import android.view.View
import android.widget.CheckBox
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cosmos.appbase.utils.FilterUtils
import com.cosmos.beauty.module.lookup.ILookupModule
import com.cosmos.beautymmdemo.R
import com.cosmos.beautymmdemo.fragment.filter.adapter.FilterAdapter
import com.cosmos.beautymmdemo.fragment.filter.adapter.FilterData
import com.fanyiran.utils.base.RvBaseFragment
import com.fanyiran.utils.recycleadapter.ItemData
import com.fanyiran.utils.recycleadapter.RvBaseAdapter
import com.fanyiran.utils.recycleadapter.RvListenerImpl

class LookupFragment : RvBaseFragment() {
    private lateinit var filterSeekBar: SeekBar
    private lateinit var recyclerView: RecyclerView
    private var lookupModules: Array<ILookupModule>? = null
    private lateinit var rootView: View
    private var crrentProgress = 0f
    private lateinit var cbLookup: CheckBox

    private val filterData by lazy { mutableListOf<FilterData>() }
    private val filterAdapter: FilterAdapter by lazy {
        FilterAdapter(
            filterData
        )
    }

    override fun getAdapter(): RvBaseAdapter<*> {
        return filterAdapter
    }

    override fun getRecycleView(): RecyclerView {
        return recyclerView
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_beauty_lookup
    }

    override fun getLayoutManager(): RecyclerView.LayoutManager? {
        return LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    }

    override fun initView(view: View?) {
        rootView = view!!
        filterSeekBar = view?.findViewById(R.id.filterSeekBar)!!
        recyclerView = view.findViewById(R.id.filterRecyclerView)!!
        cbLookup = view.findViewById(R.id.cbLookup)!!
        filterSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                crrentProgress = (progress / 100f)
                if (cbLookup.isChecked) {
                    lookupModules?.get(1)?.setIntensity(crrentProgress)
                } else {
                    lookupModules?.get(0)?.setIntensity(crrentProgress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        initFilterData()
    }

    private fun initFilterData() {
        filterData.add(
            FilterData(
                "自然", getPath("Natural")
            )
        )
        filterData.add(
            FilterData(
                "清新",
                getPath("Fresh")
            )
        )
        filterData.add(
            FilterData(
                "红颜",
                getPath("Soulmate")
            )
        )
        filterData.add(
            FilterData(
                "日系",
                getPath("SunShine")
            )
        )
        filterData.add(
            FilterData(
                "少年时代",
                getPath("Boyhood")
            )
        )
        filterData.add(
            FilterData(
                "白鹭",
                getPath("Egret")
            )
        )
        filterData.add(
            FilterData(
                "复古",
                getPath("Retro")
            )
        )
        filterData.add(
            FilterData(
                "斯托克",
                getPath("Stoker")
            )
        )
        filterData.add(
            FilterData(
                "野餐",
                getPath("Picnic")
            )
        )
        filterData.add(
            FilterData(
                "弗里达",
                getPath("Frida")
            )
        )
        filterData.add(
            FilterData(
                "罗马",
                getPath("Rome")
            )
        )
        filterData.add(
            FilterData(
                "烧烤",
                getPath("Broil")
            )
        )
        filterData.add(
            FilterData(
                "烧烤2",
                getPath("BroilF2")
            )
        )
        filterData.add(
            FilterData(
                "粉调",
                getPath("PinkTone")
            )
        )
        filterData.add(
            FilterData(
                "红调",
                getPath("RedTone")
            )
        )
        filterData.add(
            FilterData(
                "灰调",
                getPath("GrayTone")
            )
        )
        filterData.add(
            FilterData(
                "旧时光",
                getPath("Old")
            )
        )
        filterData.add(
            FilterData(
                "叛逆",
                getPath("Rebellious")
            )
        )
        filterData.add(
            FilterData(
                "日系F2",
                getPath("Sun")
            )
        )
        filterData.add(
            FilterData(
                "雾感",
                getPath("Foggy")
            )
        )
        filterData.add(
            FilterData(
                "芝士",
                getPath("Cheese")
            )
        )
        filterData.add(
            FilterData(
                "鲜奶油",
                getPath("FreshCream")
            )
        )
        filterData.add(
            FilterData(
                "酥脆",
                getPath("Crispy")
            )
        )
        filterData.add(
            FilterData(
                "拿铁",
                getPath("Latte")
            )
        )
        filterData.add(
            FilterData(
                "凉白开",
                getPath("CoolWhite")
            )
        )
        filterData.add(
            FilterData(
                "藜麦",
                getPath("Quinoa")
            )
        )
        filterData.add(
            FilterData(
                "可口",
                getPath("Tasty")
            )
        )
        filterData.add(
            FilterData(
                "焦糖",
                getPath("Caramel")
            )
        )
        filterData.add(
            FilterData(
                "海苔",
                getPath("Seaweed")
            )
        )
        filterData.add(
            FilterData(
                "冰激凌",
                getPath("IceCream")
            )
        )
        filterData.add(
            FilterData(
                "白梨",
                getPath("SnowPear")
            )
        )
        filterAdapter.notifyDataSetChanged()
        filterAdapter.setRvListener(object : RvListenerImpl() {
            override fun onClick(view: View?, data: ItemData?, position: Int) {
                val size = lookupModules?.size
                if (cbLookup.isChecked) {
                    lookupModules?.get(1)?.setEffect((data as FilterData).path)
                    lookupModules?.get(1)?.setIntensity(crrentProgress)
                } else {
                    lookupModules?.get(0)?.setEffect((data as FilterData).path)
                    lookupModules?.get(0)?.setIntensity(crrentProgress)
                }
            }
        })
    }

    fun getPath(path: String): String {
        return FilterUtils.getFilterHomeDir().absolutePath + "/$path"
    }

    fun setFilterModule(lookupModules: Array<ILookupModule>) {
        this.lookupModules = lookupModules
    }

    fun setVisible(visible: Boolean) {
        rootView.visibility = (if (visible) View.VISIBLE else View.GONE)
    }
}
