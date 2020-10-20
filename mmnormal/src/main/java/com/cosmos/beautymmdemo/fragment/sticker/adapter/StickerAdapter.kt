package com.cosmos.beautymmdemo.fragment.sticker.adapter


import com.fanyiran.utils.recycleadapter.RvBaseAdapter

class StickerAdapter(data: List<StickerData>) : RvBaseAdapter<StickerData>(data) {
    init {
        addItemType(StickerItemType())
    }
}