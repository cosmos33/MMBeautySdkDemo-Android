package com.cosmos.beautydemo.recycleadapter;

import android.view.View;

/**
 * Created by fanqiang on 2019/4/16.
 */
public interface RvListener<T> {
    void onClick(View view, T data, int position);

    void onLongClick(View view, T data, int position);
}
