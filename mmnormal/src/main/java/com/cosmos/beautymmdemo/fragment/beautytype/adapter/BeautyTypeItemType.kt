package com.cosmos.beautymmdemo.fragment.beautytype.adapter

import android.widget.TextView
import com.cosmos.beautymmdemo.R
import com.fanyiran.utils.recycleadapter.ItemData
import com.fanyiran.utils.recycleadapter.ItemTypeAbstract
import com.fanyiran.utils.recycleadapter.RvViewHolder

class BeautyTypeItemType : ItemTypeAbstract() {
    override fun getLayout(): Int {
        return R.layout.item_layout_filter
    }

    override fun fillContent(rvViewHolder: RvViewHolder?, position: Int, data: ItemData?) {
        var tvName = rvViewHolder?.getView(R.id.tvName) as TextView
        tvName.text = (data as BeautyTypeData).name
    }

    override fun openClick(): Boolean {
        return true
    }

    override fun getOnClickViews(): IntArray {
        return intArrayOf(R.id.tvName)
    }

    override fun getType(): Int {
        return com.cosmos.apputils.adapter.TypeConstant.TYPE_BEAUTYTYPE
    }
}