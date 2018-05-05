package com.example.android.bakingapp.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import java.util.Date;

/**
 * Created by john on 01/04/18.
 */

@Database(entities = {Movie.class}, version = 1)
@TypeConverters(FavoritesDatabase.Converters.class)
public abstract class FavoritesDatabase extends RoomDatabase {

    private final static String DATABASE_NAME = "PopularMovies.db";

    private static FavoritesDatabase instance;

    public static FavoritesDatabase get(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    FavoritesDatabase.class,
                    DATABASE_NAME).build();
        }
        return instance;
    }

    public abstract FavoritesDao favoritesDao();

    public static class Converters {
        @TypeConverter
        public static Date fromTimestamp(Long value) {
            return value == null ? null : new Date(value);
        }

        @TypeConverter
        public static Long toTimestamp(Date date) {
            return date == null ? null : date.getTime();
        }
    }

}
