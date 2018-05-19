package com.example.android.bakingapp.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.example.android.bakingapp.utilities.RecipesUtils;

import java.util.Arrays;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * The configuration screen for the {@link RecipeWidget RecipeWidget} AppWidget.
 */
public class RecipeWidgetConfigureActivity extends Activity {

    private static final String TAG = RecipeWidgetConfigureActivity.class.getSimpleName();

    @BindView(R.id.recipe_list_rv)
    RecyclerView recyclerView;

    @BindInt(R.integer.discovery_grid_columns)
    int gridColumns;

    // Recipes Adapter
    private RecipesListAdapter recipesAdapter;

    // ItemClickListener
    private AbstractAdapter.ItemClickListener itemClickListener;

    // Loading flag
    private boolean isLoading;

    private static final String PREFS_NAME = "com.example.android.bakingapp.widget.RecipeWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    RecipesListAdapter.ItemClickListener<Recipe> mOnClickListener = new RecipesListAdapter.ItemClickListener<Recipe>() {
        public void onClick(Recipe recipe) {
            final Context context = RecipeWidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            saveWidgetPref(context, mAppWidgetId, recipe.getId());

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RecipeWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public RecipeWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveWidgetPref(Context context, int appWidgetId, Long id) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putLong(PREF_PREFIX_KEY + appWidgetId, id);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static Long loadWidgetPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Long value = prefs.getLong(PREF_PREFIX_KEY + appWidgetId, -1);
        if (value > -1) {
            return value;
        } else {
            return null;
        }
    }

    static void deleteWidgetPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.recipe_widget_configure);
        ButterKnife.bind(this);

        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this, gridColumns);
        recyclerView.setLayoutManager(layoutManager);

        setupRecipesAdapter();
        loadBakingRecipes();

        recipesAdapter.addItemClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

    }

    private void setupRecipesAdapter() {
        if (recipesAdapter != null)
            return;

        recipesAdapter = new RecipesListAdapter(this);
        recyclerView.setAdapter(recipesAdapter);
    }

    private void loadBakingRecipes() {

        if (!isLoading) {
            isLoading = true;
            recipesAdapter.startLoading();
        }

        String url = RecipesUtils.RECIPES_URL;

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

                        recipesAdapter.clear();
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

                        Toast.makeText(RecipeWidgetConfigureActivity.this,
                                getString(R.string.recipes_load_error), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "VolleyError: " + error.getMessage());
                    }
                });

        NetworkUtils.get(RecipeWidgetConfigureActivity.this).addToRequestQueue(recipesRequest);
    }

}

