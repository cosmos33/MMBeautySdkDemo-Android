package com.cosmos.appbase.filter

import project.android.imageprocessing.filter.colour.EmptyFilter

class Empty2Filter : EmptyFilter() {
    public override fun setWidth(width: Int) {
        super.setWidth(width)
    }

    public override fun setHeight(height: Int) {
        super.setHeight(height)
    }
}