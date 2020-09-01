package com.cosmos.beautydemo.fragment.beautytype.adapter

import com.cosmos.beauty.module.beauty.SimpleBeautyType
import com.cosmos.beautydemo.adapter.TypeConstant
import com.cosmos.beautydemo.recycleadapter.ItemData

data class BeautyTypeData(val name: String, val type: SimpleBeautyType) : ItemData {
    override fun getItemType(): Int {
        return TypeConstant.TYPE_BEAUTYTYPE
    }
}