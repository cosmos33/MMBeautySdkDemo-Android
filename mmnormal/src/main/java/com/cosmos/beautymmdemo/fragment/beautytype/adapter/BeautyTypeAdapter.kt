package com.cosmos.beautymmdemo.fragment.beautytype.adapter

import com.fanyiran.utils.recycleadapter.RvBaseAdapter


class BeautyTypeAdapter(data: List<BeautyTypeData>) : RvBaseAdapter<BeautyTypeData>(data) {
    init {
        addItemType(BeautyTypeItemType())
    }
}