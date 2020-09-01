package com.cosmos.beautydemo.fragment.filter.adapter

import com.cosmos.beautydemo.adapter.TypeConstant
import com.cosmos.beautydemo.recycleadapter.ItemData

data class FilterData(val name: String, val path: String) : ItemData {
    override fun getItemType(): Int {
        return TypeConstant.TYPE_FILTER
    }
}