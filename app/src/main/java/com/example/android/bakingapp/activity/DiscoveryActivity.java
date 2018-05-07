package com.example.android.bakingapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.android.bakingapp.R;
import com.example.android.bakingapp.adapter.AbstractAdapter;
import com.example.android.bakingapp.adapter.RecipesListAdapter;
import com.example.android.bakingapp.data.Recipe;
import com.example.android.bakingapp.utilities.GsonRequest;
import com.example.android.bakingapp.utilities.NetworkUtils;

import org.parceler.Parcel;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DiscoveryActivity extends AppCompatActivity {

    private static final String TAG = DiscoveryActivity.class.getSimpleName();

    /**
     * Available sort options provided by the adapter
     */
    public enum SortOption {
        POPULARITY, TOP_RATED, RELEASE_DATE, REVENUE, FAVORITES
    }

    @BindView(R.id.discovery_list_rv)
    RecyclerView recyclerView;

    // TODO Chnage columns number
    @BindInt(R.integer.discovery_grid_columns)
    int gridColumns;

    // Loading flag
    private boolean isLoading;

    // Saved instance state Bundle keys
    private final static String LAYOUT_STATE_BUNDLE_KEY = "layout_state";
    private final static String ADAPTER_STATE_BUNDLE_KEY = "adapter_state";
    private final static String TITLE_BUNDLE_KEY = "title";

    // Recipes Adapter
    private RecipesListAdapter recipesAdapter;

    // ItemClickListener
    private AbstractAdapter.ItemClickListener itemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO Change layout
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this, gridColumns);
        recyclerView.setLayoutManager(layoutManager);

        setupRecipesAdapter();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        } else {
            loadBakingRecipes();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Listeners should also be registered once an Adapter is re-initialized
        if (recipesAdapter != null) {
            registerItemClickListener(recipesAdapter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterItemClickListener(recipesAdapter);
    }

    private void registerItemClickListener(RecipesListAdapter adapter) {
        if (itemClickListener == null) {
            itemClickListener = new AbstractAdapter.ItemClickListener<Recipe>() {
                @Override
                public void onClick(Recipe recipe) {
                    // TODO launch RecipeDetail Activity
                    Intent intent = new Intent(DiscoveryActivity.this,
                            RecipeDetailsActivity.class);
                    intent.putExtra(RecipeDetailsActivity.RECIPE_EXTRA_PARAM, Parcels.wrap(recipe));

                    startActivity(intent);
                }
            };
            adapter.addItemClickListener(itemClickListener);
        }
    }

    private void unregisterItemClickListener(RecipesListAdapter adapter) {
        if (itemClickListener != null) {
            adapter.removeItemClickListener(itemClickListener);
            itemClickListener = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Parcelable adapterState = saveInstanceState();
        Parcelable layoutState = recyclerView.getLayoutManager().onSaveInstanceState();

        outState.putParcelable(ADAPTER_STATE_BUNDLE_KEY, adapterState);
        outState.putParcelable(LAYOUT_STATE_BUNDLE_KEY, layoutState);
        outState.putString(TITLE_BUNDLE_KEY, String.valueOf(getTitle()));
    }

    private void loadBakingRecipes() {

        if (!isLoading) {
            isLoading = true;
            recipesAdapter.startLoading();
        }

        // TODO Replace hardcoded url
        String url = "https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/baking.json";

        Request recipesRequest
                = new GsonRequest<Recipe[]>(Request.Method.GET,
                url,
                null,
                Recipe[].class,
                null,
                new Response.Listener<Recipe[]>() {
                    @Override
                    public void onResponse(Recipe[] recipes) {
                        if (isLoading) {
                            isLoading = false;
                            recipesAdapter.stopLoading();
                        }
                        Log.d(TAG, "Recipes: " + recipes);

                        recipesAdapter.addAll(Arrays.asList(recipes));

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (isLoading) {
                            isLoading = false;
                            recipesAdapter.stopLoading();
                        }

                        Toast.makeText(DiscoveryActivity.this,
                                getString(R.string.recipes_load_error), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "VolleyError: " + error.getMessage());
                    }
                });

        NetworkUtils.get(DiscoveryActivity.this).addToRequestQueue(recipesRequest);
    }

    private void setupRecipesAdapter() {
        if (recipesAdapter != null)
            return;

        recipesAdapter = new RecipesListAdapter(this);
        recyclerView.setAdapter(recipesAdapter);
    }

    /**
     * Generates the adapter's state as a {@link Parcelable}
     *
     * @return the adapter's instance state
     */
    public Parcelable saveInstanceState() {

        SavedInstanceState state = new SavedInstanceState();

        if (recipesAdapter != null) {
            state.recipes = recipesAdapter.getRecipes();
        }

        return Parcels.wrap(state);
    }

    /**
     * Restores the adapter's state using a {@link Parcelable} generated by
     * saveInstanceState()
     *
     * @param savedInstanceState Bundle with a {@link Parcelable} generated by saveInstanceState()
     */
    public void restoreInstanceState(Bundle savedInstanceState) {

        SavedInstanceState state = Parcels.unwrap(savedInstanceState
                .getParcelable(ADAPTER_STATE_BUNDLE_KEY));

        Parcelable layoutState = savedInstanceState.getParcelable(LAYOUT_STATE_BUNDLE_KEY);

        String title = savedInstanceState.getString(TITLE_BUNDLE_KEY);
        setTitle(title);

        if (recipesAdapter != null) {
                recipesAdapter.setRecipes(state.recipes);
                recyclerView.getLayoutManager().onRestoreInstanceState(layoutState);
        }
    }

    /**
     * A class to save the adapter's state
     */
    @Parcel
    static class SavedInstanceState {
        // Discovered recipes list
        List<Recipe> recipes = new ArrayList<Recipe>();
    }

}
