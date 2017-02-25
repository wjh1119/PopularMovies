package app.com.example.android.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by Mr.King on 2017/2/13 0013.
 */

public class MovieContract {
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "app.com.example.android.popularmovies";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_MOVIE= "movie";

    /* Inner class that defines the table contents of the location table */
    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        // Table name
        public static final String TABLE_NAME = "movie";

        // Human readable location string, provided by the API.  Because for styling,
        // "Mountain View" is more recognizable than 94043.
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_POSTER_IMAGE = "poster_image";

        public static final String COLUMN_ADULT = "adult";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_rate";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_ORIGINAL_LANGUAGE = "original_language";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_VOTE_COUNT = "vote_count";
        public static final String COLUMN_VIDEO = "video";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";

        //weather the movie is collected
        public static final String COLUMN_COLLECT = "collect";

        //movie's runtime
        public static final String COLUMN_RUNTIME = "runtime";

        //
        public static final String COLUMN_POPULAR_RANK = "popular_rank";
        public static final String COLUMN_TOPRATED_RANK = "toprated_rank";
        public static final String COLUMN_REVIEWS = "reviews";
        public static final String COLUMN_VIDEOS = "videos";

        public static Uri buildMovieUri(Long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieWithModeUri(
                String mode) {
            Log.v("buildUri","mode is " + mode + "uri is " + CONTENT_URI.buildUpon().appendPath(mode)
                    .build().toString());
            return CONTENT_URI.buildUpon().appendPath(mode).build();
        }

        public static Uri buildMovieWithModeAndRankUri(
                String mode, int rank) {
            Log.v("intent build","rank is " + rank + "uri is " + CONTENT_URI.buildUpon().appendPath(mode)
                    .appendPath(Integer.toString(rank)).build().toString());
            return CONTENT_URI.buildUpon().appendPath(mode)
                    .appendPath(Integer.toString(rank)).build();
        }

        public static Uri buildMovieWithModeAndCollectUri(
                String mode) {
            Log.v("intent build","uri is " + CONTENT_URI.buildUpon().appendPath(mode)
                    .appendPath("c").build().toString());
            return CONTENT_URI.buildUpon().appendPath(mode)
                    .appendPath("c").build();
        }

        public static String getModeFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static int getRankFromUri(Uri uri){
            try {
                return Integer.parseInt(uri.getPathSegments().get(2));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return -1;
        }
    }
}
