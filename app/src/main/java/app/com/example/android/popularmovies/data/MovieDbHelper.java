package app.com.example.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import app.com.example.android.popularmovies.data.MovieContract.MovieEntry;

/**
 * Created by Mr.King on 2017/2/13 0013.
 */

public class MovieDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    public static final String DATABASE_NAME = "movie.db";

    private static MovieDbHelper instance;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * 为应用程序提供一个单一的入口，保证应用程序使用同一个对象操作数据库，不会因为对象不同而使同步方法失效
     * @param context 上下文
     * @return  instance
     */
    public static MovieDbHelper getDbHelper(Context context){
        if(instance==null)
            instance=new MovieDbHelper(context);
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY," +
                MovieEntry.COLUMN_ID + " INTEGER UNIQUE NOT NUlL," +
                MovieEntry.COLUMN_ADULT + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER_IMAGE + " BLOB NOT NULL, " +
                MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_ORIGINAL_LANGUAGE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, "+
                MovieEntry.COLUMN_POPULARITY + " REAL NOT NULL, " +
                MovieEntry.COLUMN_VOTE_COUNT + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                MovieEntry.COLUMN_VIDEO + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_COLLECT + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RUNTIME + " INTEGER NOT NULL, " +

                //排名
                MovieEntry.COLUMN_POPULAR_RANK + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_TOPRATED_RANK + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_REVIEWS + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_VIDEOS + " TEXT NOT NULL, " +

                " UNIQUE (" + MovieEntry.COLUMN_ID + ") ON CONFLICT REPLACE);";


        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
