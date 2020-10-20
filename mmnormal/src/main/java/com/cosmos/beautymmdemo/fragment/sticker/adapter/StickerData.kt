package com.cosmos.beautymmdemo.fragment.sticker.adapter

import com.fanyiran.utils.recycleadapter.ItemData


data class StickerData(val name: String, val stickerPath: String) : ItemData {
    override fun getItemType(): Int {
        return com.cosmos.apputils.adapter.TypeConstant.TYPE_STICKERTYPE
    }
}