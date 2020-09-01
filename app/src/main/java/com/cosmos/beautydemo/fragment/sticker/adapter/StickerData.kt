package com.cosmos.beautydemo.fragment.sticker.adapter

import com.cosmos.beautydemo.adapter.TypeConstant
import com.cosmos.beautydemo.recycleadapter.ItemData

data class StickerData(val name: String, val faceMaskPath: String) : ItemData {
    override fun getItemType(): Int {
        return TypeConstant.TYPE_FACEMASKTYPE
    }
}