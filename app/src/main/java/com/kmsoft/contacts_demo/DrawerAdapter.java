package com.kmsoft.contacts_demo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class DrawerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_HEADER = 0;
    private final MainActivity mContext;
    private final List<DrawerItem> mDrawerItems;
    private final DrawerItemClickedListener mlistener;
    private View mHeaderView;

    public interface DrawerItemClickedListener {
        void onItemClicked(DrawerItem drawerItem);
    }

    public DrawerAdapter(MainActivity context, List<DrawerItem> drawerItems, DrawerItemClickedListener listener) {
        mContext = context;
        mDrawerItems = drawerItems;
        mlistener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(mHeaderView);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.drawer_item, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
        } else {
            DrawerItem drawerItem = mDrawerItems.get(position - 1);
            ((ItemViewHolder) holder).bind(drawerItem);
        }
    }

    @Override
    public int getItemCount() {
        return mDrawerItems.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        } else {
            return 1;
        }
    }

    public void addHeaderView(View headerView) {
        mHeaderView = headerView;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView item_text,item_text1;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            item_text1 = itemView.findViewById(R.id.item_text1);
            item_text = itemView.findViewById(R.id.item_text);
        }

        public void bind(DrawerItem drawerItem) {
            item_text.setText(drawerItem.getName());
            item_text1.setText(""+drawerItem.getNumber());
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition() - 1;
            DrawerItem drawerItem = mDrawerItems.get(position);
            mlistener.onItemClicked(drawerItem);
        }
    }
}
