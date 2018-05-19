package com.example.android.bakingapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.data.Recipe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by john on 03/03/18.
 */

public class RecipesListAdapter extends AbstractAdapter<Recipe, RecipesListAdapter.ViewHolder> {

    private static final String TAG = RecipesListAdapter.class.getSimpleName();

    private static int ITEM_BACKGROUND_RES_ID = R.drawable.bg_recipe_thumb;

    // Discovered recipes list
    private List<Recipe> recipes = new ArrayList<Recipe>();

    /**
     * ItemType could be used to view some items in a different way such as taking a whole row span
     * for the loading item in a grid view.
     */
    private enum ItemType {
        ITEM, LOADING_ITEM
    }


    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(ViewHolder, int, List)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(ViewHolder, int)
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);

        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override {@link #onBindViewHolder(ViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Recipe recipe = getItem(position);

        String recipeImageUrl = recipe.getImage();


        ItemType itemType = getItemType(position);
        switch (itemType) {
            case ITEM:

                String title = recipe.getName();

                if (TextUtils.isEmpty(title)) {
                    title = context.getString(R.string.untitled_recipe);
                }

                holder.title.setText(title);
                holder.summary.setText(getSummary(recipe));

                if (TextUtils.isEmpty(recipe.getImage())) {
                    holder.image.setImageDrawable(context.getResources()
                            .getDrawable(ITEM_BACKGROUND_RES_ID));
                } else {
                    holder.image.setContentDescription(title);
                    Picasso.with(context).load(recipeImageUrl)
                            .fit()
                            .centerCrop()
                            .placeholder(ITEM_BACKGROUND_RES_ID)
                            .into(holder.image);
                    // DEBUG: using Picasso.Listener to detect load failure
                    //
                    //        Picasso.Builder builder = new Picasso.Builder(context);
                    //        builder.listener(new Picasso.Listener() {
                    //            @Override
                    //            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    //                exception.printStackTrace();
                    //            }
                    //        });
                    //
                    //        builder.build().load(movies.get(position).getPosterPath())
                    //                .fit()
                    //                .centerCrop()
                    //                .placeholder(ITEM_BACKGROUND_RES_ID)
                    //                .into(holder.image);
                }

                holder.loading.setVisibility(View.GONE);
                holder.image.setVisibility(View.VISIBLE);
                holder.title.setVisibility(View.VISIBLE);
                holder.summary.setVisibility(View.VISIBLE);

                holder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (ItemClickListener listener : getItemClickListeners()) {
                            listener.onClick(recipe);
                        }
                    }
                });

                break;

            case LOADING_ITEM:
                holder.image.setVisibility(View.GONE);
                holder.title.setVisibility(View.GONE);
                holder.summary.setVisibility(View.GONE);
                holder.loading.setVisibility(View.VISIBLE);
                break;
        }
    }

    private String getSummary(Recipe recipe) {
        StringBuilder builder = new StringBuilder();

        if (recipe.getIngredients() != null && recipe.getIngredients().size() > 0) {
            for (int i = 0; i<recipe.getIngredients().size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(recipe.getIngredients().get(i).getIngredient());
            }
        }

        return String.format(context.getString(R.string.recipe_summary_template), builder.toString());
    }


    /**
     * ViewHolder to display a discovery movie item
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.recipe_card_cv)
        CardView card;

        @BindView(R.id.recipe_image_iv)
        ImageView image;

        @BindView(R.id.recipe_loading_pb)
        ProgressBar loading;

        @BindView(R.id.recipe_title_tv)
        TextView title;

        @BindView(R.id.recipe_summary_tv)
        TextView summary;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    /**
     * Called by RecyclerView when it starts observing this Adapter.
     * <p>
     * Keep in mind that same adapter may be observed by multiple RecyclerViews.
     *
     * @param recyclerView The RecyclerView instance which started observing this adapter.
     * @see #onDetachedFromRecyclerView(RecyclerView)
     */
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            ((GridLayoutManager) recyclerView.getLayoutManager())
                    .setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                        /**
                         * Returns the number of span occupied by the item at <code>position</code>.
                         *
                         * @param position The adapter position of the item
                         * @return The number of spans occupied by the item at the provided position
                         */
                        @Override
                        public int getSpanSize(int position) {
                            ItemType itemType = getItemType(position);
                            switch (itemType) {
                                case ITEM:
                                    return 1;
                                case LOADING_ITEM:
                                    return context.getResources()
                                            .getInteger(R.integer.discovery_grid_columns);
                            }
                            return 0;
                        }
                    });
        }
    }

    /**
     * Called by RecyclerView when it stops observing this Adapter.
     *
     * @param recyclerView The RecyclerView instance which stopped observing this adapter.
     * @see #onAttachedToRecyclerView(RecyclerView)
     */
    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    /**
     * Return the view type of the item at <code>position</code> for the purposes
     * of view recycling.
     * <p>
     * <p>The default implementation of this method returns 0, making the assumption of
     * a single view type for the adapter. Unlike ListView adapters, types need not
     * be contiguous. Consider using id resources to uniquely identify item view types.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at
     * <code>position</code>. Type codes need not be contiguous.
     */
    @Override
    public int getItemViewType(int position) {

        Recipe item = getItem(position);

        if (item != null) {
            boolean noId = item.getId() == null;
            if (noId) {
                return ItemType.LOADING_ITEM.ordinal();
            } else {
                return ItemType.ITEM.ordinal();
            }
        }

        return super.getItemViewType(position);
    }

    private ItemType getItemType(int position) {
        int itemViewType = getItemViewType(position);
        return ItemType.values()[itemViewType];
    }

    /**
     * Initializes the adapter with a {@link Context} and discovery recipes data
     *
     * @param context typically the container activity
     */
    public RecipesListAdapter(Context context) {
        super(context);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return recipes.size();
    }

    /**
     * Adds a recipe to the adapter and notifies any registered observers that the item reflected at
     * position has been newly inserted. The item previously at position is now at position
     * position + 1.
     *
     * @param recipe
     */
    public void add(Recipe recipe) {
        recipes.add(recipe);
        notifyItemInserted(recipes.size() - 1);
    }

    /**
     * Adds recipes to the adapter.
     *
     * @param recipes
     */
    public void addAll(List<Recipe> recipes) {
        for (Recipe recipe : recipes) {
            add(recipe);
        }
    }

    public void remove(Recipe recipe) {
        int position = recipes.indexOf(recipe);
        if (position > -1) {
            recipes.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    /**
     * Returns the currently displayed recipes List.
     *
     * @return the currently displayed recipes List.
     */
    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void startLoading() {
        // An empty recipe should be viewed as a loading view
        add(new Recipe());
    }

    public void stopLoading() {
        int position = getItemCount() - 1;
        Recipe item = getItem(position);

        if (item != null) {
            remove(item);
        }
    }

    /**
     * Returns the item at that position.
     *
     * @param position of returned item
     * @return the item at that position
     */
    @Override
    public Recipe getItem(int position) {
        return recipes.get(position);
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }
}
