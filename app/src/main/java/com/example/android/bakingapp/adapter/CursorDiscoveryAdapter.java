package com.example.android.bakingapp.adapter;

import android.content.Context;
import android.database.Cursor;

import com.example.android.bakingapp.data.Movie;

/**
 * Created by john on 09/04/18.
 */

public class CursorDiscoveryAdapter extends AbstractDiscoveryAdapter {

    // Class variable for the Cursor that holds task data
    private Cursor mCursor;

    /**
     * Initializes the adapter with a {@link Context}
     *
     * @param context       typically the container activity
     */
    public CursorDiscoveryAdapter(Context context) {
        super(context);
    }

    /**
     * Returns the item at that position.
     */
    @Override
    public Movie getItem(int position) {
        if (mCursor.moveToPosition(position)) {
            return new Movie(mCursor);
        }
        return null;
    }

    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    @Override
    public void startLoading() {
        // TODO implement startLoading()
    }

    @Override
    public void stopLoading() {
        // TODO implement stopLoading()
    }

    @Override
    public void clear() {
        // TODO implement clear()
    }

    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

    public Cursor getCursor() {
        return mCursor;
    }
}
