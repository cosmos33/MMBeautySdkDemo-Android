package com.cosmos.beautydemo.fragment.filter.adapter

import com.cosmos.beautydemo.recycleadapter.RvBaseAdapter

class FilterAdapter(data: List<FilterData>) : RvBaseAdapter<FilterData>(data) {
    init {
        addItemType(FilterItemType())
    }
}