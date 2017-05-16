package app.com.example.android.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by Mr.King on 2017/2/13 0013.
 */

public class MovieProvider extends ContentProvider {
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    public static final int MOVIE = 100;
    public static final int MOVIE_WITH_MODE = 101;
    public static final int MOVIE_WITH_MODE_AND_RANK = 102;
    public static final int MOVIE_WITH_MODE_AND_COLLECT = 103;

    private static final SQLiteQueryBuilder sRankByModeQueryBuilder;

    static{
        sRankByModeQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        sRankByModeQueryBuilder.setTables(MovieContract.MovieEntry.TABLE_NAME);
    }

    //popularRank = ?
    private static final String sPopularAndRankSelection =
            MovieContract.MovieEntry.TABLE_NAME+
                    "." + MovieContract.MovieEntry.COLUMN_POPULAR_RANK + " = ? ";

    //topratedRank = ?
    private static final String sTopratedAndRankSelection =
            MovieContract.MovieEntry.TABLE_NAME+
                    "." + MovieContract.MovieEntry.COLUMN_TOPRATED_RANK + " = ? ";

    //popularRank > ? AND collect = ?
    private static final String sPopularAndCollectSelection =
            MovieContract.MovieEntry.TABLE_NAME+
                    "." + MovieContract.MovieEntry.COLUMN_POPULAR_RANK + " > ? AND "
                    + MovieContract.MovieEntry.COLUMN_COLLECT+ " = ? ";

    //topratedRank > ? AND collect = ?
    private static final String sTopratedAndCollectSelection =
            MovieContract.MovieEntry.TABLE_NAME+
                    "." + MovieContract.MovieEntry.COLUMN_TOPRATED_RANK + " > ? AND "
                    + MovieContract.MovieEntry.COLUMN_COLLECT+ " = ? ";

    //popularRank > 0 ,
    private static final String sPopularSelection =
            MovieContract.MovieEntry.TABLE_NAME+
                    "." + MovieContract.MovieEntry.COLUMN_POPULAR_RANK + " > ? ";

    //topratedRank > 0
    private static final String sTopratedSelection =
            MovieContract.MovieEntry.TABLE_NAME+
                    "." + MovieContract.MovieEntry.COLUMN_TOPRATED_RANK + " > ? ";

    //获取某排名模式下的排名
    private Cursor getRankByMode(Uri uri, String[] projection, String sortOrder) {
        String mode = MovieContract.MovieEntry.getModeFromUri(uri);

        String[] selectionArgs;
        String selection;
        if (mode.equals("popular")) {
            selectionArgs = new String[]{Integer.toString(0)};
            selection = sPopularSelection;
        }else if (mode.equals("toprated")) {
            selectionArgs = new String[]{Integer.toString(0)};
            selection = sTopratedSelection;
        }else{
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return sRankByModeQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    //通过排名模式及排名获取电影信息
    private Cursor getMovieByModeAndRank(
            Uri uri, String[] projection, String sortOrder) {
        String mode = MovieContract.MovieEntry.getModeFromUri(uri);
        int rank = MovieContract.MovieEntry.getRankFromUri(uri);

        String[] selectionArgs;
        String selection;
        if (mode.equals("popular")) {
            selectionArgs = new String[]{Integer.toString(rank)};
            selection = sPopularAndRankSelection;
        }else if (mode.equals("toprated")) {
            selectionArgs = new String[]{Integer.toString(rank)};
            selection = sTopratedAndRankSelection;
        }else{
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return sRankByModeQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    //获取某排名模式下被收藏的电影
    private Cursor getMovieByModeAndCollect(
            Uri uri, String[] projection, String sortOrder) {
        String mode = MovieContract.MovieEntry.getModeFromUri(uri);

        String[] selectionArgs;
        String selection;
        if (mode.equals("popular")) {
            selectionArgs = new String[]{Integer.toString(0),"true"};
            selection = sPopularAndCollectSelection;
        }else if (mode.equals("toprated")) {
            selectionArgs = new String[]{Integer.toString(0),"true"};
            selection = sTopratedAndCollectSelection;
        }else{
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return sRankByModeQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    public static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/*", MOVIE_WITH_MODE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/*/#", MOVIE_WITH_MODE_AND_RANK);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/*/*", MOVIE_WITH_MODE_AND_COLLECT);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = MovieDbHelper.getDbHelper(getContext());
        return true;
    }

    /*
        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.

     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case MOVIE_WITH_MODE_AND_COLLECT:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_WITH_MODE_AND_RANK:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE_WITH_MODE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "movie/*/#"
            case MOVIE_WITH_MODE_AND_RANK:
            {
                retCursor = getMovieByModeAndRank(uri, projection, sortOrder);
                break;
            }
            // "movie/*/*"
            case MOVIE_WITH_MODE_AND_COLLECT:
            {
                retCursor = getMovieByModeAndCollect(uri, projection, sortOrder);
                break;
            }
            // "movie/*"
            case MOVIE_WITH_MODE: {
                retCursor = getRankByMode(uri, projection, sortOrder);
                break;
            }
            // "movie"
            case MOVIE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case MOVIE:
                rowsDeleted = db.delete(
                        MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE:
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}



