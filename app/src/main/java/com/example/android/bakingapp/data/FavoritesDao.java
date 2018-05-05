package com.example.android.bakingapp.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import java.util.List;

import com.example.android.bakingapp.data.MoviesContract.Columns;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by john on 05/04/18.
 */
@Dao
public interface FavoritesDao {

    String TABLE_NAME = "favorites";

    @Query("SELECT * FROM " + TABLE_NAME
            + " ORDER BY " + Columns.CREATION_DATE + " DESC")
    List<Movie> getAll();

    @Query("SELECT * FROM " + TABLE_NAME
            + " ORDER BY " + Columns.CREATION_DATE + " DESC")
    LiveData<List<Movie>> getAllAsync();

    @Query("SELECT * FROM " + TABLE_NAME
            + " ORDER BY " + Columns.CREATION_DATE + " DESC")
    Cursor getAllCursor();

    @Query("SELECT * FROM " + TABLE_NAME
            + " ORDER BY " + Columns.CREATION_DATE + " DESC"
            + " LIMIT :limit OFFSET :offset")
    List<Movie> getAll(int limit, int offset);

    @Query("SELECT * FROM " + TABLE_NAME
            + " WHERE " + Columns.UID + " = :movieId")
    Movie getMovie(long movieId);

    @Query("SELECT * FROM " + TABLE_NAME
            + " WHERE " + Columns.UID + " = :movieId")
    Cursor getMovieCursor(long movieId);

    @Insert(onConflict = REPLACE)
    Long insert(Movie movie);

    @Insert(onConflict = REPLACE)
    List<Long> insertAll(Movie... movies);

    @Delete
    int delete(Movie movie);
}