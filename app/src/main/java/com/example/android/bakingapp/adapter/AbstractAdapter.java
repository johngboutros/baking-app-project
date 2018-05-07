package com.example.android.bakingapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by john on 10/04/18.
 */

public abstract class AbstractAdapter<T, V extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<V> {

    protected final Context context;

    // Registered Click Listeners
    private List<ItemClickListener> itemClickListeners = new ArrayList<ItemClickListener>();

    protected AbstractAdapter(Context context) {
        this.context = context;
    }

    /**
     * Item Click Listener.
     */
    public interface ItemClickListener<T> {
        /**
         * To be implemented for desired behavior when a item clicked.
         *
         * @param item clicked item
         */
        public void onClick(T item);
    }

    /**
     * Registers a ItemClickListener
     *
     * @param listener
     */
    public void addItemClickListener(ItemClickListener listener) {
        itemClickListeners.add(listener);
    }

    /**
     * Unregisters a ItemClickListener
     *
     * @param listener
     */
    public void removeItemClickListener(ItemClickListener listener) {
        itemClickListeners.remove(listener);
    }

    protected List<ItemClickListener> getItemClickListeners() {
        return itemClickListeners;
    }

    protected abstract T getItem(int position);

    public abstract void startLoading();

    public abstract void stopLoading();

    public abstract void clear();
}
