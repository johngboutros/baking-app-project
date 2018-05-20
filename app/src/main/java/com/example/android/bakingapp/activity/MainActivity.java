package com.example.android.bakingapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
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
import com.example.android.bakingapp.utilities.RecipesUtils;
import com.example.android.bakingapp.utilities.SimpleIdlingResource;
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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // SimpleIdlingResource variable that will be null in production
    @Nullable
    private SimpleIdlingResource mIdlingResource;

    @BindView(R.id.recipe_list_rv)
    RecyclerView recyclerView;

    @BindInt(R.integer.discovery_grid_columns)
    int gridColumns;

    // Loading flag
    private boolean isLoading;

    // Saved instance state Bundle keys
    private final static String SAVED_STATE_BUNDLE_KEY = "saved_state";

    // Recipes Adapter
    private RecipesListAdapter recipesAdapter;

    // ItemClickListener
    private AbstractAdapter.ItemClickListener itemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this, gridColumns);
        recyclerView.setLayoutManager(layoutManager);

        setupRecipesAdapter();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        } else {
            // Initialize the IdlingResource
            getIdlingResource();
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

//                    setIdlingResource(false);

                    Intent intent = new Intent(MainActivity.this,
                            StepListActivity.class);
                    intent.putExtra(StepListActivity.RECIPE_EXTRA_PARAM, Parcels.wrap(recipe));

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

        Parcelable savedState = saveInstanceState();

        outState.putParcelable(SAVED_STATE_BUNDLE_KEY, savedState);
    }

    private void loadBakingRecipes() {

        if (!isLoading) {
            isLoading = true;
            setIdlingResource(false);
            recipesAdapter.startLoading();
        }

        String url = RecipesUtils.RECIPES_URL;

        Request recipesRequest
                = new GsonRequest<>(Request.Method.GET,
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
                        Log.d(TAG, "Recipes: " + Arrays.toString(recipes));

                        recipesAdapter.clear();
                        recipesAdapter.addAll(Arrays.asList(recipes));
                        setIdlingResource(!isLoading);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (isLoading) {
                            isLoading = false;
                            setIdlingResource(true);
                            recipesAdapter.stopLoading();
                        }

                        Toast.makeText(MainActivity.this,
                                getString(R.string.recipes_load_error), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "VolleyError: " + error.getMessage());
                    }
                });

        NetworkUtils.get(MainActivity.this).addToRequestQueue(this, recipesRequest);
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
    private Parcelable saveInstanceState() {

        SavedInstanceState state = new SavedInstanceState();

        if (recipesAdapter != null) {
            state.recipes = recipesAdapter.getRecipes();
        }

        state.layoutState = recyclerView.getLayoutManager().onSaveInstanceState();
        state.title = String.valueOf(getTitle());

        return Parcels.wrap(state);
    }

    /**
     * Restores the adapter's state using a {@link Parcelable} generated by
     * saveInstanceState()
     *
     * @param savedInstanceState Bundle with a {@link Parcelable} generated by saveInstanceState()
     */
    private void restoreInstanceState(Bundle savedInstanceState) {

        SavedInstanceState state = Parcels.unwrap(savedInstanceState
                .getParcelable(SAVED_STATE_BUNDLE_KEY));

        setTitle(state.title);

        if (recipesAdapter != null) {
            recipesAdapter.setRecipes(state.recipes);
            recyclerView.getLayoutManager().onRestoreInstanceState(state.layoutState);
        }
    }

    /**
     * A class to save the adapter's state
     */
    @Parcel
    static class SavedInstanceState {
        // Discovered recipes list
        List<Recipe> recipes = new ArrayList<>();
        Parcelable layoutState;
        String title;
    }

    /**
     * A method that returns the IdlingResource variable. It will
     * instantiate a new instance of SimpleIdlingResource if the IdlingResource is null.
     * This method will only be called from test.
     */
    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new SimpleIdlingResource();
        }
        return mIdlingResource;
    }

    private void setIdlingResource(boolean idle) {
        if (mIdlingResource == null) return;
        mIdlingResource.setIdleState(idle);
    }
}
