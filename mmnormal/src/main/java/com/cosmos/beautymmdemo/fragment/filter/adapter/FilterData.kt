package com.cosmos.beautymmdemo.fragment.filter.adapter

import com.fanyiran.utils.recycleadapter.ItemData


data class FilterData(val name: String, val path: String) : ItemData {
    override fun getItemType(): Int {
        return com.cosmos.apputils.adapter.TypeConstant.TYPE_FILTER
    }
}