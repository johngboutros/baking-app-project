package com.example.android.bakingapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.android.bakingapp.adapter.AbstractDiscoveryAdapter;
import com.example.android.bakingapp.adapter.AbstractDiscoveryAdapter.MovieClickListener;
import com.example.android.bakingapp.adapter.CursorDiscoveryAdapter;
import com.example.android.bakingapp.adapter.ListDiscoveryAdapter;
import com.example.android.bakingapp.R;
import com.example.android.bakingapp.components.PaginationScrollListener;
import com.example.android.bakingapp.data.FavoritesDao;
import com.example.android.bakingapp.data.FavoritesDatabase;
import com.example.android.bakingapp.data.Movie;
import com.example.android.bakingapp.data.MoviesContract;
import com.example.android.bakingapp.utilities.GsonRequest;
import com.example.android.bakingapp.utilities.NetworkUtils;
import com.example.android.bakingapp.utilities.TMDbUtils;

import org.parceler.Parcel;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindInt;
import butterknife.BindString;
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
    RecyclerView discoveryRecyclerView;

    @BindInt(R.integer.discovery_grid_columns)
    int gridColumns;

    @BindString(R.string.pref_discovery_sort_key)
    String sortPrefKey;

    @BindString(R.string.pref_discovery_sort_popularity)
    String sortPrefPopularity;

    @BindString(R.string.pref_discovery_sort_top_rated)
    String sortPrefTopRated;

    @BindString(R.string.pref_discovery_sort_release_date)
    String sortPrefReleaseDate;

    @BindString(R.string.pref_discovery_sort_revenue)
    String sortPrefRevenue;

    @BindString(R.string.pref_discovery_sort_favorites)
    String sortPrefFavorites;

    @BindString(R.string.pref_discovery_sort_default)
    String sortPrefDefault;

    // Discovered pages count
    private int pageCount;

    // Total result pages count
    private int totalPageCount;

    // Loading flag
    private boolean isLoading;

    // Current SortOption
    private final SortOption defaultSortOption = SortOption.POPULARITY;

    // Current SortOption
    private SortOption currentSortOption = defaultSortOption;


    // Saved instance state Bundle keys
    private final static String LAYOUT_STATE_BUNDLE_KEY = "layout_state";
    private final static String ADAPTER_STATE_BUNDLE_KEY = "adapter_state";
    private final static String TITLE_BUNDLE_KEY = "title";

    // TMDb API / Favorites Discovery Adapter
    private AbstractDiscoveryAdapter discoveryAdapter;

    // TMDb API Discovery Adapter
    // private ListDiscoveryAdapter discoveryAdapter;

    // Favorites Adapter
    // private CursorDiscoveryAdapter favoritesAdapter;

    // MovieClickListener
    private MovieClickListener movieClickListener;

    // Scroll listener
    private ScrollListener scrollListener;

    // Favorites Observer
    private FavoritesObserver favoritesObserver;

    // Favorites DAO
    private FavoritesDao favoritesDao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_discovery);
        ButterKnife.bind(this);

        favoritesDao = FavoritesDatabase.get(this).favoritesDao();

        discoveryRecyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this, gridColumns);
        discoveryRecyclerView.setLayoutManager(layoutManager);

        String preferenceValue = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(sortPrefKey, sortPrefDefault);


        if (sortPrefFavorites.equals(preferenceValue)) {
            setupFavoritesAdapter();
        } else {
            setupDiscoveryAdapter();
        }

        if (savedInstanceState == null) {
            loadSortPreferences(preferenceValue);
        } else {
            restoreInstanceState(savedInstanceState);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Listeners should also be registered once an Adapter is re-initialized
        if (discoveryAdapter != null) {
            registerMovieClickListener(discoveryAdapter);
            if (discoveryAdapter instanceof ListDiscoveryAdapter)
                registerScrollListener();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterMovieClickListener(discoveryAdapter);
        unregisterScrollListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterFavoritesObserver();
    }

    private void registerMovieClickListener(AbstractDiscoveryAdapter discoveryAdapter) {
        if (movieClickListener == null) {
            movieClickListener = new MovieClickListener() {
                @Override
                public void onMovieClicked(Movie movie) {
                    // launch MovieDetails Activity
                    Intent intent = new Intent(DiscoveryActivity.this,
                            MovieDetailsActivity.class);
                    intent.putExtra(MovieDetailsActivity.MOVIE_EXTRA_PARAM, Parcels.wrap(movie));

                    startActivity(intent);
                }
            };
            discoveryAdapter.addMovieClickListener(movieClickListener);
        }
    }

    private void unregisterMovieClickListener(AbstractDiscoveryAdapter discoveryAdapter) {
        if (movieClickListener != null) {
            discoveryAdapter.removeMovieClickListener(movieClickListener);
            movieClickListener = null;
        }
    }

    private void registerScrollListener() {
        if (scrollListener == null) {
            scrollListener = new ScrollListener(
                    (LinearLayoutManager) discoveryRecyclerView.getLayoutManager());
            discoveryRecyclerView.addOnScrollListener(scrollListener);
        }
    }

    private void unregisterScrollListener() {
        if (scrollListener != null) {
            discoveryRecyclerView.removeOnScrollListener(scrollListener);
            scrollListener = null;
        }
    }

    private void registerFavoritesObserver() {
        if (favoritesObserver == null) {
            favoritesObserver = new FavoritesObserver(new Handler());
            getContentResolver().registerContentObserver(MoviesContract.CONTENT_URI,
                    true, favoritesObserver);
        }
    }

    private void unregisterFavoritesObserver() {
        if (favoritesObserver != null) {
            getContentResolver().unregisterContentObserver(favoritesObserver);
            favoritesObserver = null;
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     * <p>
     * <p>This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * {@link #onPrepareOptionsMenu}.
     * <p>
     * <p>The default implementation populates the menu with standard system
     * menu items.  These are placed in the {@link Menu#CATEGORY_SYSTEM} group so that
     * they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     * <p>
     * <p>You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateOptionsMenu() is called.
     * <p>
     * <p>When you add items to the menu, you can implement the Activity's
     * {@link #onOptionsItemSelected} method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_discovery, menu);
        return true;
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.</p>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        String preferenceValue = null;
        switch (item.getItemId()) {
            case R.id.menu_sort_by_popularity:
                preferenceValue = getString(R.string.pref_discovery_sort_popularity);
                break;

            case R.id.menu_sort_by_top_rated:
                preferenceValue = getString(R.string.pref_discovery_sort_top_rated);
                break;

            case R.id.menu_sort_by_release_date:
                preferenceValue = getString(R.string.pref_discovery_sort_release_date);
                break;

            case R.id.menu_sort_by_revenue:
                preferenceValue = getString(R.string.pref_discovery_sort_revenue);
                break;

            case R.id.menu_favorites:
                preferenceValue = getString(R.string.pref_discovery_sort_favorites);
                break;

        }
        saveSortPreferences(preferenceValue);
        loadSortPreferences(preferenceValue);

        return true;
    }

    private void saveSortPreferences(String preferenceValue) {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(sortPrefKey, preferenceValue);
        editor.apply();

    }

    private void loadSortPreferences(String preferenceValue) {

        if (sortPrefPopularity.equals(preferenceValue)) {
            setTitle(R.string.popular_movies);
            discoverMore(SortOption.POPULARITY);
        } else if (sortPrefTopRated.equals(preferenceValue)) {
            setTitle(R.string.top_rated_movies);
            discoverMore(SortOption.TOP_RATED);
        } else if (sortPrefReleaseDate.equals(preferenceValue)) {
            setTitle(R.string.recently_released_movies);
            discoverMore(SortOption.RELEASE_DATE);
        } else if (sortPrefRevenue.equals(preferenceValue)) {
            setTitle(R.string.highest_grossing_movies);
            discoverMore(SortOption.REVENUE);
        } else if (sortPrefFavorites.equals(preferenceValue)) {
            setTitle(R.string.menu_favorites);
            discoverMore(SortOption.FAVORITES);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Parcelable adapterState = saveInstanceState();
        Parcelable layoutState = discoveryRecyclerView.getLayoutManager().onSaveInstanceState();

        outState.putParcelable(ADAPTER_STATE_BUNDLE_KEY, adapterState);
        outState.putParcelable(LAYOUT_STATE_BUNDLE_KEY, layoutState);
        outState.putString(TITLE_BUNDLE_KEY, String.valueOf(getTitle()));
    }

    /**
     * Loads more movies using the current sort option.
     */
    public void discoverMore() {
        discoverMore(null);
    }

    /**
     * Loads more movies using the provided sort option.
     *
     * @param sortOption
     */
    public void discoverMore(SortOption sortOption) {

        if (SortOption.FAVORITES.equals(sortOption)) {
            setupFavoritesAdapter();
        } else {
            setupDiscoveryAdapter();
        }

        if (sortOption != null && !currentSortOption.equals(sortOption)) {
            this.currentSortOption = sortOption;
            if (isLoading) {
                isLoading = false;
                discoveryAdapter.stopLoading();
            }
            discoveryAdapter.clear();
            pageCount = 0;
            totalPageCount = 0;
        }

        Integer page = pageCount > 0 ? pageCount + 1 : null;

        String url = null;

        switch (this.currentSortOption) {
            case FAVORITES:
                loadFavorites(null);
                break;
            case POPULARITY:
                url = TMDbUtils.buildPopularMoviesURL(page).toString();
                loadUrl(url);
                break;
            case TOP_RATED:
                url = TMDbUtils.buildTopRatedMoviesURL(page).toString();
                loadUrl(url);
                break;
            case RELEASE_DATE:
                url = TMDbUtils.buildDiscoveryUrl(TMDbUtils.SortBy.RELEASE_DATE, page).toString();
                loadUrl(url);
                break;
            case REVENUE:
                url = TMDbUtils.buildDiscoveryUrl(TMDbUtils.SortBy.REVENUE, page).toString();
                loadUrl(url);
                break;
            default:
                url = TMDbUtils.buildDiscoveryUrl(TMDbUtils.SortBy.POPULARITY, page).toString();
                loadUrl(url);
        }
    }

    private void loadUrl(String url) {

        if (!isLoading) {
            isLoading = true;
            discoveryAdapter.startLoading();
        }

        Request movieRequest
                = new GsonRequest<Movie.Page>(Request.Method.GET,
                url,
                null,
                Movie.Page.class,
                null,
                new Response.Listener<Movie.Page>() {
                    @Override
                    public void onResponse(Movie.Page moviePage) {
                        if (isLoading) {
                            isLoading = false;
                            discoveryAdapter.stopLoading();
                        }
                        Log.d(TAG, "Movie Page: " + moviePage);

                        pageCount = moviePage.getPage();
                        totalPageCount = moviePage.getTotalPages();
                        ((ListDiscoveryAdapter) discoveryAdapter).addAll(moviePage.getResults());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (isLoading) {
                            isLoading = false;
                            discoveryAdapter.stopLoading();
                        }

                        Toast.makeText(DiscoveryActivity.this,
                                getString(R.string.discovery_load_error), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "VolleyError: " + error.getMessage());
                    }
                });

        NetworkUtils.get(DiscoveryActivity.this).addToRequestQueue(movieRequest);
    }

    private void loadFavorites(Parcelable layoutState) {

        /*
          Using ContentObserver
         */
        favoritesObserver.observe(layoutState);

        // Start loading
//        if (!isLoading) {
//            isLoading = true;
//            discoveryAdapter.startLoading();
//        }

//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//
//                /*
//                  Using Cursor by ContentResolver
//                 */
////                final Cursor cursor = getContentResolver().query(MoviesContract.CONTENT_URI,
////                        null, null, null, null);
//
//                /*
//                   Using Cursor by Room
//                  */
////                final Cursor cursor = favoritesDao.getAllCursor();
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        if (!currentSortOption.equals(SortOption.FAVORITES))
//                            return;
//
//                        // Stop loading
//                        if (isLoading) {
//                            isLoading = false;
//                            discoveryAdapter.stopLoading();
//                        }
//
//                        ((CursorDiscoveryAdapter) discoveryAdapter).swapCursor(cursor);
//
//                        Log.d(TAG, "Favorites loaded, size: " + cursor.getCount());
//                    }
//                });
//            }
//        });

        /*
          Using Room's LiveData (requires ListDiscoveryAdapter)

          (Recommended as besides leveraging Room's sync implementation
           and saving data access boilerplate code, it allows using the same
           Adapter as discovery which based on a List; eliminating the code
           to maintain both types of adapters e.g. listeners registration
           and instance restoring)
         */
//        LiveData<List<Movie>> favorites = favoritesDao.getAllAsync();
//
//        favorites.observe(this, new Observer<List<Movie>>() {
//            @Override
//            public void onChanged(@Nullable List<Movie> movies) {
//
//                if (!currentSortOption.equals(SortOption.FAVORITES))
//                    return;
//
//                // Stop loading
//                if (isLoading) {
//                    isLoading = false;
//                    discoveryAdapter.stopLoading();
//                }
//
//                Log.d(TAG, "Favorites loaded, size: " + movies.size());
//
//                discoveryAdapter.setMovies(movies);
//
//            }
//        });

    }

    private void setupDiscoveryAdapter() {
        if (discoveryAdapter != null && discoveryAdapter instanceof ListDiscoveryAdapter)
            return;

        if (discoveryAdapter != null)
            unregisterMovieClickListener(discoveryAdapter);

        discoveryAdapter = new ListDiscoveryAdapter(this);
        discoveryRecyclerView.setAdapter(discoveryAdapter);
        registerMovieClickListener(discoveryAdapter);
        registerScrollListener();
        unregisterFavoritesObserver();
    }

    private void setupFavoritesAdapter() {
        if (discoveryAdapter != null && discoveryAdapter instanceof CursorDiscoveryAdapter)
            return;

        if (discoveryAdapter != null)
            unregisterMovieClickListener(discoveryAdapter);

        discoveryAdapter = new CursorDiscoveryAdapter(this);
        discoveryRecyclerView.setAdapter(discoveryAdapter);
        registerMovieClickListener(discoveryAdapter);
        unregisterScrollListener();
        registerFavoritesObserver();
    }

    /**
     * Generates the adapter's state as a {@link Parcelable}
     *
     * @return the adapter's instance state
     */
    public Parcelable saveInstanceState() {

        SavedInstanceState state = new SavedInstanceState();

        if (discoveryAdapter != null) {
            if (discoveryAdapter instanceof ListDiscoveryAdapter) {
                state.movies = ((ListDiscoveryAdapter) discoveryAdapter).getMovies();
            }
        }

        state.pageCount = pageCount;
        state.totalPageCount = totalPageCount;
        state.isLoading = this.isLoading;
        state.currentSortOption = currentSortOption;

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

        // NOTE: setting the List directly inside the adapter setMovies() works as well as addAll()
        if (discoveryAdapter != null) {
            if (discoveryAdapter instanceof ListDiscoveryAdapter) {
                ((ListDiscoveryAdapter) discoveryAdapter).setMovies(state.movies);
                discoveryRecyclerView.getLayoutManager().onRestoreInstanceState(layoutState); // useless?
            } else if (discoveryAdapter instanceof CursorDiscoveryAdapter) {
                loadFavorites(layoutState);
            }
        }

        this.pageCount = state.pageCount;
        this.totalPageCount = state.totalPageCount;
        this.isLoading = state.isLoading;
        this.currentSortOption = state.currentSortOption;
    }

    /**
     * A class to save the adapter's state
     */
    @Parcel
    static class SavedInstanceState {
        // Discovered movies list
        List<Movie> movies = new ArrayList<Movie>();
        // Discovered pages count
        int pageCount;
        // Total result pages count
        int totalPageCount;
        // Loading flag
        boolean isLoading;
        // Current SortOption
        SortOption currentSortOption;
    }

    /**
     * Custom Pagination {@link RecyclerView.OnScrollListener} instance to be set to the {@link RecyclerView}.
     */
    private class ScrollListener extends PaginationScrollListener {

        /**
         * Initializes a new ScrollListener with an Adapter and a LayoutManager
         *
         * @param layoutManager {@link RecyclerView}'s LayoutManager
         */
        public ScrollListener(LinearLayoutManager layoutManager) {
            super(layoutManager);
        }

        @Override
        protected void loadMoreItems() {
            discoverMore();
        }

        @Override
        public int getTotalPageCount() {
            return totalPageCount;
        }

        @Override
        public boolean isLastPage() {
            return pageCount >= getTotalPageCount();
        }

        @Override
        public boolean isLoading() {
            return isLoading;
        }
    }

    class FavoritesObserver extends ContentObserver {

        private Cursor mCursor;

        public FavoritesObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            observe(null);
        }

        private void cursorLoaded(Cursor cursor, Parcelable layoutState) {
            ((CursorDiscoveryAdapter) discoveryAdapter).swapCursor(cursor);

            if (layoutState != null) {
                discoveryRecyclerView.getLayoutManager()
                        .onRestoreInstanceState(layoutState);
            }
        }

        private void observe(final Parcelable layoutState) {

            if (mCursor != null) {
                cursorLoaded(mCursor, layoutState);
            }

            if (!isLoading) {
                isLoading = true;
                discoveryAdapter.startLoading();
            }
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {

                    mCursor = getContentResolver().query(MoviesContract.CONTENT_URI,
                            null, null, null, null);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (!currentSortOption.equals(SortOption.FAVORITES))
                                return;

                            if (isLoading) {
                                isLoading = false;
                                discoveryAdapter.stopLoading();
                            }

                            Log.d(TAG, "Favorites loaded, size: " + mCursor.getCount());
                            cursorLoaded(mCursor, layoutState);
                        }
                    });
                }
            });
        }
    }
}
