package com.cosmos.beautydemo.fragment.sticker.adapter


import com.cosmos.beautydemo.recycleadapter.RvBaseAdapter

class StickerAdapter(data: List<StickerData>) : RvBaseAdapter<StickerData>(data) {
    init {
        addItemType(StickerItemType())
    }
}