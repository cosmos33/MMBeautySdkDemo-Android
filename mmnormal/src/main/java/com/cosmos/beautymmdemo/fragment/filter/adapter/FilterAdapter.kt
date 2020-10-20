package com.cosmos.beautymmdemo.fragment.filter.adapter

import com.fanyiran.utils.recycleadapter.RvBaseAdapter

class FilterAdapter(data: List<FilterData>) : RvBaseAdapter<FilterData>(data) {
    init {
        addItemType(FilterItemType())
    }
}