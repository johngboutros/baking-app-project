package com.example.android.bakingapp.utilities;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.bakingapp.BuildConfig;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by john on 04/03/18.
 * <p>
 * Utilities to be used to communicate with the TMDb API.
 */

public final class TMDbUtils {

    private static final String TAG = TMDbUtils.class.getSimpleName();

    // API Key (v3 auth)
    private static final String API_KEY = BuildConfig.TMDB_API_KEY;

    // API base URL
    private static final String API_BASE_URL = "http://api.themoviedb.org/3";

    // API common params
    private static final String API_KEY_PARAM = "api_key";
    private static final String PAGE_PARAM = "page";

    // Popular movies path
    // Example API request:
    // http://api.themoviedb.org/3/movie/popular?api_key=[YOUR_API_KEY]
    private static final String POPULAR_PATH = "/movie/popular";

    // Top rated movies path
    private static final String TOP_RATED_PATH = "/movie/top_rated";

    // Movie path
    // Example API request:
    // https://api.themoviedb.org/3/movie/550?api_key=[YOUR_API_KEY]
    private static final String MOVIE_PATH = "/movie/%s";

    // Movie videos path
    // Example API request:
    // https://api.themoviedb.org/3/movie/550/videos?api_key=[YOUR_API_KEY]
    private static final String MOVIE_VIDEOS_PATH = MOVIE_PATH + "/videos";

    // Movie reviews path
    // Example API request:
    // https://api.themoviedb.org/3/movie/550/reviews?api_key=[YOUR_API_KEY]
    private static final String MOVIE_REVIEWS_PATH = MOVIE_PATH + "/reviews";

    // Youtube video path (https://www.youtube.com/watch?v=[VIDEO_KEY])
    // Example API request:
    // https://www.youtube.com/watch?v=SUXWAEX2jlg
    private static final String YOUTUBE_VIDEO_PATH = "https://www.youtube.com/watch";
    private static final String YOUTUBE_VIDEO_KEY_PARAM = "v";


    // Poster URL
    // Example:
    // http://image.tmdb.org/t/p/w185/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg
    // Poster base URL will look like: http://image.tmdb.org/t/p/.
    private static final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
    // Poster ‘size’, which will be one of the following: "w92", "w154", "w185", "w342", "w500",
    // "w780", or "original". For most phones we recommend using “w185”.
    private static String POSTER_SIZE_W92 = "w92";
    private static String POSTER_SIZE_W154 = "w154";
    private static String POSTER_SIZE_W185 = "w185";
    private static String POSTER_SIZE_W342 = "w342";
    private static String POSTER_SIZE_W500 = "w500";
    private static String POSTER_SIZE_W780 = "w780";
    private static String POSTER_SIZE_ORIGINAL = "original";

    public enum PosterSize {
        W92, W154, W185, W342, W500, W780, ORIGINAL
    }

    // Discover path
    // Example API requests:
    // https://api.themoviedb.org/3/discover/movie?api_key=<<api_key>>&language=en-US&sort_by=popularity.desc&include_adult=false&include_video=false&page=1
    // https://api.themoviedb.org/3/discover/movie?api_key=<<api_key>>&language=en-US&sort_by=popularity.desc&page=1
    private static final String DISCOVER_PATH = "/discover/movie";

    // Discover params
    private static final String LANGUAGE_PARAM = "language";
    private static final String SORT_BY_PARAM = "sort_by";
    private static final String RELEASE_DATE_LTE_PARAM = "release_date.lte";

    // Date Format
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    // Allowed SORT_BY Values:
    // popularity.asc, popularity.desc, release_date.asc, release_date.desc, revenue.asc,
    // revenue.desc, primary_release_date.asc, primary_release_date.desc, original_title.asc,
    // original_title.desc, vote_average.asc, vote_average.desc, vote_count.asc, vote_count.desc
    private static final String SORT_BY_ASC = ".asc";
    private static final String SORT_BY_DESC = ".desc";
    private static final String SORT_BY_POPULARITY = "popularity";
    private static final String SORT_BY_RELEASE_DATE = "release_date";
    private static final String SORT_BY_REVENUE = "revenue";
    private static final String SORT_BY_PRIMARY_RELEASE_DATE = "primary_release_date";
    private static final String SORT_BY_PRIMARY_ORIGINAL_TITLE = "original_title";
    private static final String SORT_BY_VOTE_AVERAGE = "vote_average";
    private static final String SORT_BY_VOTE_COUNT = "vote_count";

    /**
     * Movies list sort direction options
     */
    public enum SortDirection {
        DESC, ASC
    }

    /**
     * Movies list sort options
     */
    public enum SortBy {
        POPULARITY, RELEASE_DATE, REVENUE, PRIMARY_RELEASE_DATE, PRIMARY_ORIGINAL_TITLE,
        VOTE_AVERAGE, VOTE_COUNT
    }

    // This class should be used in a static way and never be initialized
    private TMDbUtils(){
        throw new AssertionError("No instances for you");
    }

    /**
     * Builds a value for SORT_BY param
     *
     * @param sortBy        @{@link SortBy} sort option
     * @param sortDirection @{@link SortDirection} sort direction
     * @return a string value for SORT_BY param
     */
    private static String buildSortOption(@NonNull SortBy sortBy, @NonNull SortDirection sortDirection) {

        String sortOption = "";

        switch (sortBy) {
            case POPULARITY:
                sortOption = SORT_BY_POPULARITY;
                break;
            case RELEASE_DATE:
                sortOption = SORT_BY_RELEASE_DATE;
                break;
            case REVENUE:
                sortOption = SORT_BY_REVENUE;
                break;
            case PRIMARY_RELEASE_DATE:
                sortOption = SORT_BY_PRIMARY_RELEASE_DATE;
                break;
            case PRIMARY_ORIGINAL_TITLE:
                sortOption = SORT_BY_PRIMARY_ORIGINAL_TITLE;
                break;
            case VOTE_AVERAGE:
                sortOption = SORT_BY_VOTE_AVERAGE;
                break;
            case VOTE_COUNT:
                sortOption = SORT_BY_VOTE_COUNT;
                break;
        }

        switch (sortDirection) {
            case DESC:
                sortOption += SORT_BY_DESC;
                break;
            case ASC:
                sortOption += SORT_BY_ASC;
                break;
        }

        return sortOption;
    }

    /**
     * Builds a @{@link URL} object for a discovery movies list request given a sort option as
     * a @{@link SortBy} parameter with a default sort direction based on the given sort option
     *
     * @param sortBy sort option
     * @return url for a discovery movies list request
     */
    public static URL buildDiscoveryUrl(@NonNull SortBy sortBy, Integer page) {

        // Default sort direction
        SortDirection sortDirection;
        if (sortBy == SortBy.PRIMARY_ORIGINAL_TITLE) {
            sortDirection = SortDirection.ASC;
        } else {
            sortDirection = SortDirection.DESC;
        }

        Uri.Builder builder = Uri.parse(API_BASE_URL + DISCOVER_PATH).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .appendQueryParameter(SORT_BY_PARAM, buildSortOption(sortBy, sortDirection));

        if (page != null && page > 0) {
            builder.appendQueryParameter(PAGE_PARAM, page.toString());
        }

        if (sortBy == SortBy.RELEASE_DATE) {
            builder.appendQueryParameter(RELEASE_DATE_LTE_PARAM, formatDate(new Date()));
        }

        Uri uri = builder.build();

        return buildUrl(uri);
    }

    /**
     * Returns a URL to a movie GET request.
     *
     * @param movieId the movie ID
     * @return URL to a movie GET request
     */
    public static URL buildMovieUrl(@NonNull Integer movieId) {

        Uri uri = Uri.parse(API_BASE_URL + String.format(MOVIE_PATH, movieId.toString()))
                .buildUpon()
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .build();

        return buildUrl(uri);
    }

    /**
     * Builds a string representation for a movie poster URL
     *
     * @param posterPath The path to a movie poster as returned by TMDb API
     * @param posterSize The preferred poster size
     * @return
     */
    public static String buildPosterURL(@NonNull String posterPath, @NonNull PosterSize posterSize) {
        String sizeSegment = null;

        switch (posterSize) {
            case W92:
                sizeSegment = POSTER_SIZE_W92;
                break;
            case W154:
                sizeSegment = POSTER_SIZE_W154;
                break;
            case W185:
                sizeSegment = POSTER_SIZE_W185;
                break;
            case W342:
                sizeSegment = POSTER_SIZE_W342;
                break;
            case W500:
                sizeSegment = POSTER_SIZE_W500;
                break;
            case W780:
                sizeSegment = POSTER_SIZE_W780;
                break;
            case ORIGINAL:
                sizeSegment = POSTER_SIZE_ORIGINAL;
                break;
            default:
                sizeSegment = POSTER_SIZE_W185;
        }

        String posterUrl = POSTER_BASE_URL + sizeSegment + posterPath;

        Log.d(TAG, "Poster URL built: " + posterUrl);

        return posterUrl;
    }

    /**
     * Builds popular movies GET URL
     *
     * @param page
     * @return
     */
    public static URL buildPopularMoviesURL(Integer page) {

        Uri.Builder builder = Uri.parse(API_BASE_URL + POPULAR_PATH)
                .buildUpon()
                .appendQueryParameter(API_KEY_PARAM, API_KEY);

        if (page != null && page > 0) {
            builder.appendQueryParameter(PAGE_PARAM, page.toString());
        }

        Uri uri = builder.build();

        return buildUrl(uri);
    }

    /**
     * Builds top rated movies GET URL
     *
     * @param page
     * @return
     */
    public static URL buildTopRatedMoviesURL(Integer page) {

        Uri.Builder builder = Uri.parse(API_BASE_URL + TOP_RATED_PATH)
                .buildUpon()
                .appendQueryParameter(API_KEY_PARAM, API_KEY);

        if (page != null && page > 0) {
            builder.appendQueryParameter(PAGE_PARAM, page.toString());
        }

        Uri uri = builder.build();

        return buildUrl(uri);
    }

    /**
     * Builds a URL for a GET Request that retrieves a list of videos for a given movie Id.
     *
     * @param movieId given movie Id
     * @return GET Request URL
     */
    public static URL buildMovieVideosUrl(@NonNull Integer movieId) {

        Uri uri = Uri.parse(API_BASE_URL + String.format(MOVIE_VIDEOS_PATH, movieId.toString()))
                .buildUpon()
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .build();

        return buildUrl(uri);
    }

    /**
     * Builds a URL for a GET Request that retrieves a list of reviews for a given movie Id.
     *
     * @param movieId given movie Id
     * @return GET Request URL
     */
    public static URL buildMovieReviewsUrl(@NonNull Integer movieId) {

        Uri uri = Uri.parse(API_BASE_URL + String.format(MOVIE_REVIEWS_PATH, movieId.toString()))
                .buildUpon()
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .build();

        return buildUrl(uri);
    }

    /**
     * Builds a Youtube video URI.
     *
     * @param key Video key
     * @return Youtube video URI
     */
    public static Uri buildYoutubeUri(@NonNull String key) {

        Uri uri = Uri.parse(YOUTUBE_VIDEO_PATH)
                .buildUpon()
                .appendQueryParameter(YOUTUBE_VIDEO_KEY_PARAM, key)
                .build();

        return uri;
    }

    /**
     * Builds a URL for a given URI.
     *
     * @param uri the URI to build a URL from
     * @return URL for the given URI
     */
    private static URL buildUrl(@NonNull Uri uri) {
        try {
            URL url = new URL(uri.toString());
            Log.d(TAG, "URL built: " + url);
            return url;
        } catch (MalformedURLException e) {
            Log.e(TAG, "ERROR building URL (" + uri.toString() + "): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Formats a date object to the format used by TMDb
     *
     * @param date
     * @return formatted date
     */
    public static String formatDate(Date date) {
        return dateFormat.format(date);
    }


    /**
     * Parses a formatted date string
     *
     * @param date
     * @return parsed date
     */
    public static Date parseDate(String date) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            Log.e(TAG, "parseDate Error: " + e.getMessage());
        }
        return null;
    }
}
