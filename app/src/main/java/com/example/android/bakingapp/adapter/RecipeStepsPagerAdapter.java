package com.example.android.bakingapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.data.Ingredient;
import com.example.android.bakingapp.data.Recipe;
import com.example.android.bakingapp.data.Step;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by john on 09/05/18.
 */

public class RecipeStepsPagerAdapter extends PagerAdapter {

    private final String TAG = RecipeStepsPagerAdapter.class.getSimpleName();

    // See:
    // https://www.bignerdranch.com/blog/viewpager-without-fragments/
    // https://www.journaldev.com/10096/android-viewpager-example-tutorial

    private Context context;
    private Recipe recipe;

    public RecipeStepsPagerAdapter(@NonNull Context context, @NonNull Recipe recipe) {
        this.context = context;
        this.recipe = recipe;
    }

    /**
     * Create the page for the given position.  The adapter is responsible
     * for adding the view to the container given here, although it only
     * must ensure this is done by the time it returns from
     * {@link #finishUpdate(ViewGroup)}.
     *
     * @param container The containing View in which the page will be shown.
     * @param position  The page position to be instantiated.
     * @return Returns an Object representing the new page.  This does not
     * need to be a View, but can be some other container of the page.
     */
    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        ViewGroup layout = null;
        int stepPosition = position;

        if (!hasIngredients()) {
            if (!hasSteps()) {
                Log.w(TAG, "Instantiating empty item");
                // TODO Handle empty recipe
                return null;
            }
        } else {
            stepPosition--;
        }

        if (hasIngredients() && position == 0) {
            // 1st Page is ingredients
            layout = getIngredientsLayout(container);
        } else {
            // Step page
            Step step = recipe.getSteps().get(stepPosition);
            layout = getStepLayout(container, step);
        }

        // TODO Select an ingredient page or step one based on position


        container.addView(layout);
        return layout;
    }

    private ViewGroup getIngredientsLayout(ViewGroup container) {
        int layoutResId = R.layout.fragment_recipe_viewer_ingredients;

        ViewGroup layout = (ViewGroup) inflater().inflate(layoutResId,
                container, false);

        // Setup ingredients layout
        LinearLayout ingredientsContainer = layout.findViewById(R.id.ingredients_container_ll);
        displayIngredients(ingredientsContainer, recipe.getIngredients());

        return layout;
    }

    /**
     * Sets up the layout for the given step and returns a view that represent it.
     *
     * @param container the layout container
     * @param step the given step to render
     * @return view represents the layout for the given step
     */
    private ViewGroup getStepLayout(ViewGroup container, Step step) {
        int layoutResId = R.layout.fragment_recipe_viewer_step;

        ViewGroup layout = (ViewGroup) inflater().inflate(layoutResId,
                container, false);

        // Setup step layout
        // TODO handle nulls
        TextView headerTv = layout.findViewById(R.id.step_header_tv);
        headerTv.setText(step.getShortDescription());

        ImageView thumbnailIv = layout.findViewById(R.id.step_thumbnail_iv);
        if (!TextUtils.isEmpty(step.getThumbnailURL())) {
            thumbnailIv.setVisibility(View.VISIBLE);
            Picasso.with(context).load(step.getThumbnailURL())
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.bg_movie_thumb)
                    .into(thumbnailIv);
        } else {
            thumbnailIv.setVisibility(View.GONE);
        }

        TextView contentTv = layout.findViewById(R.id.step_content_tv);
        contentTv.setText(step.getDescription());

        return layout;
    }

    private void displayIngredients(ViewGroup container, List<Ingredient> ingredients) {

        for (Ingredient ingredient : ingredients) {

            View ingredientItem = inflater()
                    .inflate(R.layout.fragment_recipe_viewer_ingredients_item, container,
                            false);

            TextView quantityTv = ingredientItem.findViewById(R.id.quantity_tv);

            quantityTv.setText(ingredient.getQuantity() + " " + ingredient.getMeasure());

            TextView ingredientTv = ingredientItem.findViewById(R.id.ingredient_tv);

            ingredientTv.setText(ingredient.getIngredient());

            container.addView(ingredientItem);
        }
    }

    private LayoutInflater inflater() {
        return LayoutInflater.from(context);
    }

    /**
     * Remove a page for the given position.  The adapter is responsible
     * for removing the view from its container, although it only must ensure
     * this is done by the time it returns from {@link #finishUpdate(ViewGroup)}.
     *
     * @param container The containing View from which the page will be removed.
     * @param position  The page position to be removed.
     * @param object    The same object that was returned by
     *                  {@link #instantiateItem(View, int)}.
     */
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return (hasIngredients() ? 1 : 0) + (hasSteps() ? recipe.getSteps().size() : 0);
    }

    private boolean hasIngredients() {
        return recipe.getIngredients() != null && recipe.getIngredients().size() > 0;
    }

    private boolean hasSteps() {
        return recipe.getSteps() != null && recipe.getSteps().size() > 0;
    }

    /**
     * Determines whether a page View is associated with a specific key object
     * as returned by {@link #instantiateItem(ViewGroup, int)}. This method is
     * required for a PagerAdapter to function properly.
     *
     * @param view   Page View to check for association with <code>object</code>
     * @param object Object to check for association with <code>view</code>
     * @return true if <code>view</code> is associated with the key object <code>object</code>
     */
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    /**
     * This method may be called by the ViewPager to obtain a title string
     * to describe the specified page. This method may return null
     * indicating no title for this page. The default implementation returns
     * null.
     *
     * @param position The position of the title requested
     * @return A title for the requested page
     */
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        // TODO return a title based on step (or ingredients title)
        return super.getPageTitle(position);
    }
}
