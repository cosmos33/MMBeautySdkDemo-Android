package com.cosmos.beautymmdemo.fragment.beautytype.adapter

import com.cosmos.beauty.module.beauty.SimpleBeautyType
import com.fanyiran.utils.recycleadapter.ItemData

data class BeautyTypeData(val name: String, val type: SimpleBeautyType) : ItemData {
    override fun getItemType(): Int {
        return com.cosmos.apputils.adapter.TypeConstant.TYPE_BEAUTYTYPE
    }
}