package com.cosmos.beautydemo.fragment.sticker.adapter

import android.widget.TextView
import com.cosmos.beautydemo.R
import com.cosmos.beautydemo.adapter.TypeConstant
import com.cosmos.beautydemo.recycleadapter.ItemData
import com.cosmos.beautydemo.recycleadapter.ItemTypeAbstract
import com.cosmos.beautydemo.recycleadapter.RvViewHolder

class StickerItemType : ItemTypeAbstract() {
    override fun getLayout(): Int {
        return R.layout.item_layout_filter
    }

    override fun fillContent(rvViewHolder: RvViewHolder?, position: Int, data: ItemData?) {
        var tvName = rvViewHolder?.getView(R.id.tvName) as TextView
        tvName.text = (data as StickerData).name
    }

    override fun openClick(): Boolean {
        return true
    }

    override fun getOnClickViews(): IntArray {
        return intArrayOf(R.id.tvName)
    }

    override fun getType(): Int {
        return TypeConstant.TYPE_FACEMASKTYPE
    }
}