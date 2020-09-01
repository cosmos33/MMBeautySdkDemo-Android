package com.cosmos.beautydemo.fragment.beautytype.adapter

import com.cosmos.beautydemo.recycleadapter.RvBaseAdapter

class BeautyTypeAdapter(data: List<BeautyTypeData>) : RvBaseAdapter<BeautyTypeData>(data) {
    init {
        addItemType(BeautyTypeItemType())
    }
}