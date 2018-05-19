package com.example.android.bakingapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} with item click listeners.
 *
 * Created by john on 10/04/18.
 */

public abstract class AbstractAdapter<T, V extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<V> {

    final Context context;

    // Registered Click Listeners
    private final List<ItemClickListener> itemClickListeners = new ArrayList<>();

    AbstractAdapter(Context context) {
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
        void onClick(T item);
    }

    /**
     * Registers a ItemClickListener
     *
     * @param listener ItemClickListener
     */
    public void addItemClickListener(ItemClickListener listener) {
        itemClickListeners.add(listener);
    }

    /**
     * Unregisters a ItemClickListener
     *
     * @param listener ItemClickListener
     */
    public void removeItemClickListener(ItemClickListener listener) {
        itemClickListeners.remove(listener);
    }

    List<ItemClickListener> getItemClickListeners() {
        return itemClickListeners;
    }

    protected abstract T getItem(int position);

    public abstract void startLoading();

    public abstract void stopLoading();

    public abstract void clear();
}
