package com.example.android.bakingapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.data.Recipe;
import com.example.android.bakingapp.fragment.RecipeViewerFragment;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipeDetailsActivity extends AppCompatActivity {

    private static final String TAG = RecipeDetailsActivity.class.getSimpleName();

    // Movie Intent Extra param
    public static final String RECIPE_EXTRA_PARAM = "recipe";

    // Share text (first trailer link if exists)
    private String shareText;

    // Saved instance state Bundle keys
    private final static String SCROLL_STATE_BUNDLE_KEY = "scroll_state";
    private Float pendingScrollPercent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);
        ButterKnife.bind(this);

        final Recipe recipe = Parcels.unwrap(getIntent().getParcelableExtra(RECIPE_EXTRA_PARAM));

        setTitle(recipe.getName());

        RecipeViewerFragment recipeViewer = RecipeViewerFragment.newInstance(recipe);

        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction().add(R.id.recipe_viewer_container, recipeViewer)
                .commit();

//        if (TextUtils.isEmpty(recipe.getImage())) {
//
//            imageDisplay.setImageDrawable(getResources().getDrawable(R.drawable.bg_movie_thumb));
//
//        } else {
//
//            String posterURL = TMDbUtils.buildPosterURL(recipe.getImage(), TMDbUtils.PosterSize.W185);
//
//            Picasso.with(this).load(posterURL)
//                    .fit()
//                    .centerCrop()
//                    .placeholder(R.drawable.bg_movie_thumb)
//                    .into(imageDisplay);
//        }

    }


    /**
     * Starting a chooser to share the input text.
     *
     * @param text text to share
     */
    private void shareText(String text) {

        // mimeType
        String mimeType = "text/plain";

        // title for the chooser window that will pop up
        String title = getString(R.string.movie_detail_share_chooser_title);

        // Use ShareCompat.IntentBuilder to build the Intent and start the chooser
        Intent intent = ShareCompat.IntentBuilder.from(this)
                .setChooserTitle(title)
                .setType(mimeType)
                .setText(text)
                .getIntent();

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.activity_recipe_details, menu);

        // Return true to display menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_item_share:
                if (shareText != null) {
                    shareText(shareText);
                }
                break;

            default:
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        int scrollY = scrollView.getScrollY();
//        int maxScrollY = scrollView.getChildAt(0).getHeight();
//        outState.putFloat(SCROLL_STATE_BUNDLE_KEY, getScrollPercent(scrollY, maxScrollY));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        pendingScrollPercent = savedInstanceState.getFloat(SCROLL_STATE_BUNDLE_KEY);
    }

    private static float getScrollPercent(int scrollY, int maxScrollY) {
        return scrollY * 100 / maxScrollY;
    }

    private static int getScrollY(float scrollYPercent, int maxScrollY) {
        return (int) scrollYPercent * maxScrollY / 100;
    }

//    private void notifyLoaded() {
//        if (activity_done_loading) {
//            if (pendingScrollPercent != null) {
//                scrollView.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        int maxScrollY = scrollView.getChildAt(0).getHeight();
//                        scrollView.setScrollY(getScrollY(pendingScrollPercent, maxScrollY));
//                        pendingScrollPercent = null;
//                    }
//                });
//            }
//        }
//    }
}
