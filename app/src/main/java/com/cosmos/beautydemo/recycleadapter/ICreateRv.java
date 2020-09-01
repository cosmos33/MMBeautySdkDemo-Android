package com.cosmos.beautydemo.recycleadapter;


import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by fanqiang on 2019/4/17.
 */
public interface ICreateRv {
    RecyclerView getRecycleView();

    RvBaseAdapter getAdapter();

    RecyclerView.LayoutManager getLayoutManager();

    RecyclerView.ItemDecoration getItemDecoration();
}
