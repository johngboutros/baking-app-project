package com.example.android.bakingapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.android.bakingapp.R;
import com.example.android.bakingapp.activity.StepListActivity;
import com.example.android.bakingapp.data.Recipe;
import com.example.android.bakingapp.utilities.GsonRequest;
import com.example.android.bakingapp.utilities.NetworkUtils;
import com.example.android.bakingapp.utilities.RecipesUtils;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link RecipeWidgetConfigureActivity RecipeWidgetConfigureActivity}
 */
public class RecipeWidget extends AppWidgetProvider {

    static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId) {

        final Long recipeId = RecipeWidgetConfigureActivity.loadWidgetPref(context, appWidgetId);
        if (recipeId == null) {
            return;
        }

        // Load recipes
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
                        Recipe widgetRecipe = null;
                        for (Recipe recipe : recipes) {
                            if (recipe.getId().equals(recipeId)) {
                                widgetRecipe = recipe;
                                break;
                            }
                        }
                        if (widgetRecipe != null) {
                            // Construct the RemoteViews object
                            RemoteViews views = new RemoteViews(context.getPackageName(),
                                    R.layout.recipe_widget);

                            // Setup views
                            setupViews(context, widgetRecipe, views, appWidgetId);

                            // Instruct the widget manager to update the widget
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context,
                                context.getString(R.string.recipes_load_error), Toast.LENGTH_LONG).show();
                    }
                });

        NetworkUtils.get(context).addToRequestQueue(context, recipesRequest);
    }

    private static void setupViews(final Context context, Recipe recipe, RemoteViews views,
                                   int appWidgetId) {

        String title = recipe.getName();

        if (TextUtils.isEmpty(title)) {
            title = context.getString(R.string.untitled_recipe);
        }

        views.setTextViewText(R.id.recipe_title_tv, title);
        views.setTextViewText(R.id.recipe_summary_tv, getSummary(context, recipe));

        if (TextUtils.isEmpty(recipe.getImage())) {
            views.setImageViewResource(R.id.recipe_image_iv, R.drawable.bg_recipe_thumb);
        } else {
            // TODO Test load image into RemoteViews
            Picasso.with(context).load(recipe.getImage())
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.bg_recipe_thumb)
                    .into(views, R.id.recipe_image_iv, new int[]{appWidgetId});
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

        // Create an Intent to launch ExampleActivity
        Intent intent = new Intent(context, StepListActivity.class);
        intent.putExtra(StepListActivity.RECIPE_EXTRA_PARAM, Parcels.wrap(recipe));

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
    }

    private static String getSummary(Context context, Recipe recipe) {
        String summary = context.getString(R.string.no_summary);

        if (recipe.getSteps() != null && recipe.getSteps().size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < recipe.getSteps().size(); i++) {
                if (i > 0) {
                    builder.append("\n");
                }
                builder.append(String.format(context.getString(R.string.step_template), i+1,
                        recipe.getSteps().get(i).getShortDescription()));
            }
            summary = builder.toString();
        } else if (recipe.getIngredients() != null && recipe.getIngredients().size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < recipe.getIngredients().size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(recipe.getIngredients().get(i).getIngredient());
            }
            summary = String.format(context.getString(R.string.recipe_summary_template),
                    builder.toString());
        }

        return summary;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            RecipeWidgetConfigureActivity.deleteWidgetPref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

