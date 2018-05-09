package com.example.android.bakingapp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.android.bakingapp.R;
import com.example.android.bakingapp.data.FavoritesDao;
import com.example.android.bakingapp.data.FavoritesDatabase;
import com.example.android.bakingapp.data.Movie;
import com.example.android.bakingapp.data.MoviesContract;
import com.example.android.bakingapp.data.Review;
import com.example.android.bakingapp.data.Video;
import com.example.android.bakingapp.utilities.GsonRequest;
import com.example.android.bakingapp.utilities.NetworkUtils;
import com.example.android.bakingapp.utilities.TMDbUtils;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MovieDetailsActivity extends AppCompatActivity {

    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    // Movie Intent Extra param
    public static final String MOVIE_EXTRA_PARAM = "movie";

    // Max displayed reviews count
    private static final int MAX_REVIEWS_COUNT = 5;

    @BindView(R.id.movie_detail_image_iv)
    ImageView imageDisplay;

    @BindView(R.id.movie_detail_year_tv)
    TextView yearDisplay;

    private static final String YEAR_FORMAT = "yyyy";

    @BindView(R.id.movie_detail_rate_tv)
    TextView rateDisplay;

    @BindString(R.string.movie_detail_rate_format)
    String rateFormat;

    @BindView(R.id.movie_detail_overview_tv)
    TextView overviewDisplay;

    @BindView(R.id.movie_detail_trailers_separator_v)
    View trailersSeparator;

    @BindView(R.id.movie_detail_trailers_label_tv)
    View trailersLabel;

    @BindView(R.id.movie_detail_trailers_loading_pb)
    View trailersLoading;

    @BindView(R.id.movie_detail_trailers_container_ll)
    LinearLayout trailersContainer;

    @BindView(R.id.movie_detail_reviews_separator_v)
    View reviewsSeparator;

    @BindView(R.id.movie_detail_reviews_label_tv)
    View reviewsLabel;

    @BindView(R.id.movie_detail_reviews_loading_pb)
    View reviewsLoading;

    @BindView(R.id.movie_detail_reviews_container_ll)
    LinearLayout reviewsContainer;

    @BindView(R.id.movie_detail_favorite_btn)
    Button favoriteButton;

    @BindView(R.id.movie_detail_scroll_sv)
    ScrollView scrollView;

    private FavoritesDao favoritesDao;

    // Not null if this movie was saved as favorite
    private Movie favorite;

    // Share text (first trailer link if exists)
    private String shareText;

    // Year format
    private final SimpleDateFormat yearFormat = new SimpleDateFormat(YEAR_FORMAT);

    // Saved instance state Bundle keys
    private final static String SCROLL_STATE_BUNDLE_KEY = "scroll_state";
    private Float pendingScrollPercent;

    // Tracking loaded sections
    private boolean videosLoaded;
    private boolean reviewsLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);

        favoritesDao = FavoritesDatabase.get(this).favoritesDao();

        final Movie movie = Parcels.unwrap(getIntent().getParcelableExtra(MOVIE_EXTRA_PARAM));

        // Load videos
        loadVideos(movie);

        // Load reviews
        loadReviews(movie);

        setTitle(movie.getTitle());

        if (TextUtils.isEmpty(movie.getPosterPath())) {

            imageDisplay.setImageDrawable(getResources().getDrawable(R.drawable.bg_movie_thumb));

        } else {

            String posterURL = TMDbUtils.buildPosterURL(movie.getPosterPath(), TMDbUtils.PosterSize.W185);

            Picasso.with(this).load(posterURL)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.bg_movie_thumb)
                    .into(imageDisplay);
        }

        if (!TextUtils.isEmpty(movie.getReleaseDate())) {
            yearDisplay.setText(yearFormat.format(TMDbUtils.parseDate(movie.getReleaseDate())));
        }

        if (movie.getVoteAverage() > 0) {
            rateDisplay.setText(String.format(rateFormat, movie.getVoteAverage()));
        } else {
            rateDisplay.setText(R.string.movie_detail_unrated);
        }

        if (!TextUtils.isEmpty(movie.getOverview())) {
            overviewDisplay.setText(movie.getOverview());
        }

        // Load favorite movie if exists
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                favorite = favoritesDao.getMovie(movie.getId());

                // Setup favorite view if exists
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupFavorite(favorite != null);
                    }
                });

            }
        });
    }

    private void loadVideos(@NonNull final Movie movie) {

        videosLoaded = false;

        String url = TMDbUtils.buildMovieVideosUrl(movie.getId()).toString();

        // Start loading
        trailersLoading.setVisibility(View.VISIBLE);

        // Display trailers
        Request videosRequest
                = new GsonRequest<>(Request.Method.GET,
                url,
                null,
                Video.Page.class,
                null,
                new Response.Listener<Video.Page>() {
                    @Override
                    public void onResponse(Video.Page page) {

                        // Stop loading
                        trailersLoading.setVisibility(View.GONE);

                        Log.d(TAG, "Page: " + page);

                        if (page.getResults() != null && !page.getResults().isEmpty()) {

                            Iterator<Video> videoIterator = page.getResults().iterator();

                            while (videoIterator.hasNext()) {
                                final Video video = videoIterator.next();

                                final Uri webpage = TMDbUtils.buildYoutubeUri(video.getKey());

                                if (shareText == null && webpage != null) {
                                    // Share text (first trailer link if exists)
                                    shareText = webpage.toString();
                                }

                                View videoItem = getLayoutInflater()
                                        .inflate(R.layout.item_movie_details_video, trailersContainer,
                                                false);

                                TextView title = videoItem
                                        .findViewById(R.id.movie_detail_video_item_name_tv);
                                title.setText(video.getName());

                                if (!videoIterator.hasNext()) {
                                    View separator = videoItem
                                            .findViewById(R.id.movie_detail_video_item_separator_v);
                                    separator.setVisibility(View.INVISIBLE);
                                }

                                // Handle video click
                                videoItem.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        if (video.getSite() != null
                                                && video.getSite().toLowerCase().equals("youtube")) {

                                            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);

                                            if (intent.resolveActivity(getPackageManager()) != null) {
                                                startActivity(intent);
                                            }

                                        } else {
                                            Toast.makeText(MovieDetailsActivity.this,
                                                    getString(R.string.movie_detail_video_not_supported),
                                                    Toast.LENGTH_LONG).show();

                                            Log.e(TAG, "Video site not supported: " + video.getSite());
                                        }

                                    }
                                });

                                trailersContainer.addView(videoItem);
                            }
                        } else {
                            // Hide trailers view
                            hideTrailersView();
                        }

                        if (shareText == null) {
                            shareText = movie.getTitle()
                                    + " (" + yearFormat.format(TMDbUtils
                                    .parseDate(movie.getReleaseDate())) + ")";
                        }

                        videosLoaded = true;
                        notifyLoaded();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        // Stop loading
                        trailersLoading.setVisibility(View.GONE);

                        hideTrailersView();

                        videosLoaded = true;
                        notifyLoaded();

                        Toast.makeText(MovieDetailsActivity.this,
                                getString(R.string.movie_detail_load_trailers_error),
                                Toast.LENGTH_LONG).show();

                        Log.e(TAG, "VolleyError: " + error.getMessage());
                    }
                });

        NetworkUtils.get(MovieDetailsActivity.this).addToRequestQueue(videosRequest);
    }

    private void loadReviews(@NonNull Movie movie) {

        reviewsLoaded = false;

        String url = TMDbUtils.buildMovieReviewsUrl(movie.getId()).toString();

        // Start loading
        reviewsLoading.setVisibility(View.VISIBLE);

        // Display reviews
        Request reviewsRequest
                = new GsonRequest<>(Request.Method.GET,
                url,
                null,
                Review.Page.class,
                null,
                new Response.Listener<Review.Page>() {
                    @Override
                    public void onResponse(Review.Page page) {

                        // Stop loading
                        reviewsLoading.setVisibility(View.GONE);

                        Log.d(TAG, "Page: " + page);

                        if (page.getResults() != null && !page.getResults().isEmpty()) {

                            Iterator<Review> reviewIterator = page.getResults().iterator();

                            int displayedCount = 0;
                            while (reviewIterator.hasNext()) {

                                final Review review = reviewIterator.next();

                                // Skip empty reviews
                                if (TextUtils.isEmpty(review.getContent())) continue;

                                View reviewItem = getLayoutInflater()
                                        .inflate(R.layout.item_movie_details_review, reviewsContainer,
                                                false);

                                String author = TextUtils.isEmpty(review.getAuthor()) ?
                                        getString(R.string.movie_detail_reviews_unknown_author) :
                                        review.getAuthor();

                                TextView authorTv = reviewItem
                                        .findViewById(R.id.movie_detail_review_item_author_tv);
                                TextView contentTv = reviewItem
                                        .findViewById(R.id.movie_detail_review_item_content_tv);

                                authorTv.setText(author);
                                contentTv.setText(review.getContent());

                                reviewsContainer.addView(reviewItem);

                                displayedCount++;

                                if (!reviewIterator.hasNext() || displayedCount == MAX_REVIEWS_COUNT) {
                                    // Setup last review & exit loop
                                    View separator = reviewItem
                                            .findViewById(R.id.separator_v);
                                    separator.setVisibility(View.INVISIBLE);

                                    break;
                                }
                            }

                        } else {
                            // Hide reviews view
                            hideReviewsView();
                        }

                        reviewsLoaded = true;
                        notifyLoaded();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        // Stop loading
                        reviewsLoading.setVisibility(View.GONE);

                        hideReviewsView();

                        reviewsLoaded = true;
                        notifyLoaded();

                        Toast.makeText(MovieDetailsActivity.this,
                                getString(R.string.movie_detail_load_reviews_error),
                                Toast.LENGTH_LONG).show();

                        Log.e(TAG, "VolleyError: " + error.getMessage());
                    }
                });

        NetworkUtils.get(MovieDetailsActivity.this).addToRequestQueue(reviewsRequest);
    }

    private void hideTrailersView() {
        trailersSeparator.setVisibility(View.GONE);
        trailersLabel.setVisibility(View.GONE);
        trailersContainer.setVisibility(View.GONE);
    }

    private void hideReviewsView() {
        reviewsSeparator.setVisibility(View.GONE);
        reviewsLabel.setVisibility(View.GONE);
        reviewsContainer.setVisibility(View.GONE);
    }

    @OnClick(R.id.movie_detail_favorite_btn)
    void favoriteClicked() {

        if (favorite != null) {
            removeFromFavorites(favorite);
        } else {
            Movie movie = Parcels.unwrap(getIntent().getParcelableExtra(MOVIE_EXTRA_PARAM));
            addToFavorites(movie);
        }
    }

    private void addToFavorites(@NonNull final Movie movie) {

        // Disable button
        favoriteButton.setEnabled(false);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                // Set creation date at the time it were added
                movie.setCreationDate(new Date());

                /*
                   Using ContentResolver
                  */
                getContentResolver().insert(MoviesContract.CONTENT_URI, movie.toContentValues());

                /*
                   Using Room DAO
                  */
//                 favoritesDao.insert(movie);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        favorite = movie;
                        setupFavorite(true);
                        // Enable button
                        favoriteButton.setEnabled(true);
                        Toast.makeText(MovieDetailsActivity.this,
                                String.format(getString(R.string.movie_detail_favorite_added), movie.getTitle()),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void removeFromFavorites(@NonNull final Movie movie) {

        // Disable button
        favoriteButton.setEnabled(false);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                /*
                   Using ContentResolver
                  */
                getContentResolver().delete(MoviesContract.CONTENT_URI.buildUpon()
                                .appendPath("" + movie.getId()).build(), null,
                        null);

                /*
                   Using Room DAO
                  */
//                 favoritesDao.delete(movie);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        favorite = null;
                        setupFavorite(false);
                        // Enable button
                        favoriteButton.setEnabled(true);
                        Toast.makeText(MovieDetailsActivity.this,
                                String.format(getString(R.string.movie_detail_favorite_removed),
                                        movie.getTitle()),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setupFavorite(boolean isFavorite) {
        if (isFavorite) {
            favoriteButton.setText(getString(R.string.movie_detail_favorite_button_added));
            favoriteButton.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            favoriteButton.setTextColor(Color.WHITE);
        } else {
            favoriteButton.setText(getString(R.string.movie_detail_favorite_button));
            favoriteButton.setBackgroundColor(Color.LTGRAY);
            favoriteButton.setTextColor(Color.BLACK);
        }
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
        int scrollY = scrollView.getScrollY();
        int maxScrollY = scrollView.getChildAt(0).getHeight();
        outState.putFloat(SCROLL_STATE_BUNDLE_KEY, getScrollPercent(scrollY, maxScrollY));
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

    private void notifyLoaded() {
        if (videosLoaded && reviewsLoaded) {
            if (pendingScrollPercent != null) {
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        int maxScrollY = scrollView.getChildAt(0).getHeight();
                        scrollView.setScrollY(getScrollY(pendingScrollPercent, maxScrollY));
                        pendingScrollPercent = null;
                    }
                });
            }
        }
    }
}
