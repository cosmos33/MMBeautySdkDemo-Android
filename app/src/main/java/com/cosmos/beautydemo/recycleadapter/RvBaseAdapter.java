package com.cosmos.beautydemo.recycleadapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cosmos.beautydemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanqiang on 2019/4/16.
 */
public abstract class RvBaseAdapter<T extends ItemData> extends RecyclerView.Adapter<RvViewHolder> {
    private List<T> baseDataList;
    private RvListener rvListener;
    private View.OnClickListener onClickListener;
    private View.OnLongClickListener onLongClickListener;
    private int[] onClickViewArray;
    private int[] onLongClickViewArray;

    public RvBaseAdapter(List<T> baseDataList) {
        this.baseDataList = baseDataList;
        if (this.baseDataList == null) {
            this.baseDataList = new ArrayList<>();
        }
    }

    public void addItemType(ItemType itemType) {
        ItemManager.getInstance().addItems(itemType);
    }

    @Override
    public int getItemViewType(int position) {
        return ItemManager.getInstance().getType(baseDataList.get(position), position);
    }

    @NonNull
    @Override
    public RvViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        int layout = ItemManager.getInstance().getLayout(viewType);
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(layout, viewGroup, false);
        return new RvViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull RvViewHolder rvViewHolder, int position) {
        int viewType = getItemViewType(position);
        ItemType item = ItemManager.getInstance().getItemType(viewType);
        rvViewHolder.getItemView().setTag(R.id.baseapdater_tag_item_position, position);
        rvViewHolder.getItemView().setTag(R.id.baseapdater_tag_item_data, baseDataList.get(position));
        if (item.openClick()) {
//            rvViewHolder.getItemView().setOnClickListener(getOnClickListener());
            onClickViewArray = item.getOnClickViews();
            if (onClickViewArray != null) {
                for (int i = 0; i < onClickViewArray.length; i++) {
                    View view = rvViewHolder.getView(onClickViewArray[i]);
                    if (view == null) {
                        continue;
                    }
                    view.setTag(R.id.baseapdater_tag_item_position, position);
                    view.setTag(R.id.baseapdater_tag_item_data, baseDataList.get(position));
                    view.setOnClickListener(getOnClickListener());
                }
            }
        } else {
            onClickViewArray = item.getOnClickViews();
            if (onClickViewArray != null) {
                for (int i = 0; i < onClickViewArray.length; i++) {
                    rvViewHolder.getView(onClickViewArray[i]).setOnClickListener(null);
                }
            }
//            rvViewHolder.getItemView().setOnClickListener(null);
        }

        if (item.openLongClick()) {
            onLongClickViewArray = item.getOnLongClickViews();
            if (onLongClickViewArray != null) {
                for (int i = 0; i < onLongClickViewArray.length; i++) {
                    View view = rvViewHolder.getView(onLongClickViewArray[i]);
                    view.setTag(R.id.baseapdater_tag_item_position, position);
                    view.setTag(R.id.baseapdater_tag_item_data, baseDataList.get(position));
                    view.setOnLongClickListener(getOnLongClickListener());
                }
            }
        } else {
            onLongClickViewArray = item.getOnLongClickViews();
            if (onLongClickViewArray != null) {
                for (int i = 0; i < onLongClickViewArray.length; i++) {
                    rvViewHolder.getView(onLongClickViewArray[i]).setOnLongClickListener(null);
                }
            }
        }

        item.fillContent(rvViewHolder, position, baseDataList.get(position));
    }

    private View.OnClickListener getOnClickListener() {
        if (onClickListener == null) {
            onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (int) v.getTag(R.id.baseapdater_tag_item_position);
                    T baseData = (T) v.getTag(R.id.baseapdater_tag_item_data);
                    if (rvListener != null) {
                        rvListener.onClick(v, baseData, position);
                    }
                }
            };
        }
        return onClickListener;
    }

    private View.OnLongClickListener getOnLongClickListener() {
        if (onLongClickListener == null) {
            onLongClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = (int) v.getTag(R.id.baseapdater_tag_item_position);
                    T baseData = (T) v.getTag(R.id.baseapdater_tag_item_data);
                    if (rvListener != null) {
                        rvListener.onLongClick(v, baseData, position);
                    }
                    return true;
                }
            };
        }
        return onLongClickListener;
    }

    @Override
    public int getItemCount() {
        return baseDataList == null ? 0 : baseDataList.size();
    }

    public void setRvListener(RvListener rvListener) {
        this.rvListener = rvListener;
    }

}
