package com.example.android.bakingapp.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by john on 08/03/18.
 * <p>
 * Utilities singleton to handle network requests.
 */

public final class NetworkUtils {

    /**
     * Static singleton instance
     */
    private static NetworkUtils instance;

    /**
     * Volley {@link RequestQueue} as a singleton to be kept running for the lifetime of the app.
     *
     * @see <a href="https://developer.android.com/training/volley/requestqueue.html>source</a>
     */
    private RequestQueue requestQueue;

    /**
     * Volley {@link ImageLoader}
     */
    private ImageLoader imageLoader;

    /**
     * For caches that do not override sizeOf, this is the maximum number of entries in the cache.
     * For all other caches, this is the maximum sum of the sizes of the entries in this cache.
     */
    private final static int IMAGES_CACHE_MAX_SIZE = 20;

    // For internal use only
    private NetworkUtils(Context context) {

        if (context == null)
            throw new IllegalArgumentException("Context must be provided");

        getRequestQueue(context);
        getImageLoader(context);
    }

    // NetworkUtils can't be initialized without args
    private NetworkUtils() {
        throw new AssertionError("NetworkUtils can't be initialized without args");
    }

    /**
     * Returns the static singleton instance of {@link NetworkUtils}. Initializes a new one if it
     * doesn't exist.
     *
     * @return the static singleton {@link NetworkUtils} instance.
     */
    public static synchronized NetworkUtils get(Context context) {
        if (instance == null) {
            instance = new NetworkUtils(context);
        }
        return instance;
    }

    /**
     * Returns the singleton instance of {@link RequestQueue}. Initializes a new one if it doesn't
     * exist.
     *
     * @return the singleton {@link RequestQueue} instance.
     */
    public RequestQueue getRequestQueue(Context context) {
        if (requestQueue == null) {

            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());

            // DiskBasedCache provides a one-file-per-response cache with an in-memory index
            //
            // Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

            // Network provides a network transport based on your preferred HTTP client. BasicNetwork is
            // Volley's default network implementation. A BasicNetwork must be initialized with the HTTP
            // client your app is using to connect to the network. Typically this is an HttpURLConnection.
            //
            // Network network = new BasicNetwork(new HurlStack());

            // requestQueue = new RequestQueue(cache, network);
        }

        return requestQueue;
    }

    /**
     * Adds a request to the @{@link RequestQueue}
     *
     * @param req Request to be added to the queue
     * @param <T> The type of parsed response this request expects.
     */
    public <T> void addToRequestQueue(Context context, Request<T> req) {
        getRequestQueue(context).add(req);
    }

    /**
     * Returns the singleton instance of {@link ImageLoader}. Initializes a new one if not exists.
     *
     * @return the singleton {@link ImageLoader} instance.
     */
    public ImageLoader getImageLoader(Context context) {
        if (imageLoader == null) {
            imageLoader = new ImageLoader(getRequestQueue(context),
                    new ImageLoader.ImageCache() {
                        private final LruCache<String, Bitmap>
                                cache = new LruCache<>(IMAGES_CACHE_MAX_SIZE);

                        @Override
                        public Bitmap getBitmap(String url) {
                            return cache.get(url);
                        }

                        @Override
                        public void putBitmap(String url, Bitmap bitmap) {
                            cache.put(url, bitmap);
                        }
                    });
        }
        return imageLoader;
    }

}
