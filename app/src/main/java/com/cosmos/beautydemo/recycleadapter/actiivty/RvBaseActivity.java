package com.cosmos.beautydemo.recycleadapter.actiivty;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cosmos.beautydemo.recycleadapter.CreateRvHelper;
import com.cosmos.beautydemo.recycleadapter.ICreateRv;


/**
 * Created by fanqiang on 2019/4/17.
 */
public abstract class RvBaseActivity extends AppCompatActivity implements ICreateRv {
    private CreateRvHelper createRvHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        createRvHelper = new CreateRvHelper.Builder(this).build();
    }

    @Override
    public RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL);
    }
}
