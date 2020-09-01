package com.cosmos.beautydemo.recycleadapter;


import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by fanqiang on 2019/4/17.
 */
public class CreateRvHelper {
    private Builder builder;
    private RecyclerView recyclerView;
    private RvBaseAdapter adapter;

    private CreateRvHelper(Builder builder) {
        this.builder = builder;
        recyclerView = builder.iCreateRv.getRecycleView();
        adapter = builder.iCreateRv.getAdapter();
        initRecycleView();
    }

    private void initRecycleView() {
        RecyclerView.ItemDecoration itemDecoration = builder.iCreateRv.getItemDecoration();
        RecyclerView.LayoutManager layoutManager = builder.iCreateRv.getLayoutManager();
        if (recyclerView == null || adapter == null || layoutManager == null) {
            throw new IllegalArgumentException("初始化失败！");
        }
        recyclerView.setLayoutManager(layoutManager);
        if (itemDecoration != null) {
            recyclerView.addItemDecoration(itemDecoration);
        }
        recyclerView.setAdapter(adapter);
    }

    public static class Builder {
        private ICreateRv iCreateRv;

        public Builder(ICreateRv iCreateRv) {
            this.iCreateRv = iCreateRv;
        }

        public CreateRvHelper build() {
            if (iCreateRv == null) {
                throw new IllegalArgumentException("iCreateRv is null");
            }
            return new CreateRvHelper(this);
        }
    }

}
