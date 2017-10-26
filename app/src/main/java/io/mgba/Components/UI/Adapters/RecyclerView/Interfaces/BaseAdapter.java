package io.mgba.Components.UI.Adapters.RecyclerView.Interfaces;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.base.Function;

import java.util.List;

public abstract class BaseAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected final int layout;
    private final Function<View, RecyclerView.ViewHolder> generateViewHolder;
    protected List<T> items;
    protected Context ctx;

    public BaseAdapter(List<T> items, int layout, Function<View, RecyclerView.ViewHolder> generateViewHolder, Context ctx){
        this.items = items;
        this.layout = layout;
        this.generateViewHolder = generateViewHolder;
        this.ctx = ctx;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return generateViewHolder.apply(inflatedView);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}