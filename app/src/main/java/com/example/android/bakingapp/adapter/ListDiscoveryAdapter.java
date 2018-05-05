package com.example.android.bakingapp.adapter;

import android.content.Context;

import com.example.android.bakingapp.data.Movie;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by john on 03/03/18.
 */

public class ListDiscoveryAdapter extends AbstractDiscoveryAdapter {

    private static final String TAG = ListDiscoveryAdapter.class.getSimpleName();

    // Discovered movies list
    private List<Movie> movies = new ArrayList<Movie>();

    /**
     * Initializes the adapter with a {@link Context} and discovery movies data
     *
     * @param context typically the container activity
     */
    public ListDiscoveryAdapter(Context context) {
        super(context);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return movies.size();
    }

    /**
     * Adds a movie to the adapter and notifies any registered observers that the item reflected at
     * position has been newly inserted. The item previously at position is now at position
     * position + 1.
     *
     * @param movie
     */
    public void add(Movie movie) {
        movies.add(movie);
        notifyItemInserted(movies.size() - 1);
    }

    /**
     * Adds movies to the adapter.
     *
     * @param movies
     */
    public void addAll(List<Movie> movies) {
        for (Movie movie : movies) {
            add(movie);
        }
    }

    public void remove(Movie movie) {
        int position = movies.indexOf(movie);
        if (position > -1) {
            movies.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    /**
     * Returns the currently displayed movies List.
     *
     * @return the currently displayed movies List.
     */
    public List<Movie> getMovies() {
        return movies;
    }

    public void startLoading() {
        // An empty movie should be viewed as a loading view
        add(new Movie());
    }

    public void stopLoading() {
        int position = getItemCount() - 1;
        Movie item = getItem(position);

        if (item != null) {
            remove(item);
        }
    }

    /**
     * Returns the item at that position.
     *
     * @param position of returned item
     * @return the item at that position
     */
    @Override
    public Movie getItem(int position) {
        return movies.get(position);
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }
}
