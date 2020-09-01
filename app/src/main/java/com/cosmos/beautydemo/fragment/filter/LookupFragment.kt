package com.cosmos.beautydemo.fragment.filter

import android.view.View
import android.widget.CheckBox
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cosmos.beauty.module.lookup.ILookupModule
import com.cosmos.beautydemo.FilterUtils
import com.cosmos.beautydemo.R
import com.cosmos.beautydemo.fragment.filter.adapter.FilterAdapter
import com.cosmos.beautydemo.fragment.filter.adapter.FilterData
import com.cosmos.beautydemo.recycleadapter.RvBaseAdapter
import com.cosmos.beautydemo.recycleadapter.RvBaseFragment
import com.cosmos.beautydemo.recycleadapter.RvListenerImpl

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
        var temp = FilterData(
            "自然", getPath("Natural")
        )
        filterData.add(temp)
        temp = FilterData(
            "清新",
            getPath("Fresh")
        )
        filterData.add(temp)
        temp = FilterData(
            "红颜",
            getPath("Soulmate")
        )
        filterData.add(temp)
        temp = FilterData(
            "日系",
            getPath("SunShine")
        )
        filterData.add(temp)
        temp = FilterData(
            "少年时代",
            getPath("Boyhood")
        )
        filterData.add(temp)
        temp = FilterData(
            "白鹭",
            getPath("Egret")
        )
        filterData.add(temp)
        temp = FilterData(
            "复古",
            getPath("Retro")
        )
        filterData.add(temp)
        temp = FilterData(
            "斯托克",
            getPath("Stoker")
        )
        filterData.add(temp)
        temp = FilterData(
            "野餐",
            getPath("Picnic")
        )
        filterData.add(temp)
        temp = FilterData(
            "弗里达",
            getPath("Frida")
        )
        filterData.add(temp)
        temp = FilterData(
            "罗马",
            getPath("Rome")
        )
        filterData.add(temp)
        temp = FilterData(
            "烧烤",
            getPath("Broil")
        )
        filterData.add(temp)
        temp = FilterData(
            "烧烤2",
            getPath("BroilF2")
        )
        filterData.add(temp)
        filterAdapter.notifyDataSetChanged()
        filterAdapter.setRvListener(object : RvListenerImpl() {
            override fun onClick(view: View?, data: Any?, position: Int) {
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
