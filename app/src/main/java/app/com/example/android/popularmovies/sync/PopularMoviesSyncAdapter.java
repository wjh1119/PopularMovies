package app.com.example.android.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import app.com.example.android.popularmovies.BuildConfig;
import app.com.example.android.popularmovies.Logger;
import app.com.example.android.popularmovies.MainActivity;
import app.com.example.android.popularmovies.MessageEvent;
import app.com.example.android.popularmovies.NetworkUtil;
import app.com.example.android.popularmovies.R;
import app.com.example.android.popularmovies.ToastUtil;
import app.com.example.android.popularmovies.data.MovieContract;
import app.com.example.android.popularmovies.data.MovieDbHelper;

import static app.com.example.android.popularmovies.Utility.bitmapToByte;
import static app.com.example.android.popularmovies.Utility.getImageFromUrl;

public class PopularMoviesSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = PopularMoviesSyncAdapter.class.getSimpleName();
    // Interval at which to sync with the weather, in seconds.
    public static int SYNC_INTERVAL = 60*60*3;  // 3 hours
    public static int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int POPULAR_NOTIFICATION_ID = 3004;
    private static final int TOPRATED_NOTIFICATION_ID = 3005;


    private static final String[] NOTIFY_MOVIE_PROJECTION = new String[] {
            MovieContract.MovieEntry.COLUMN_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
    };

    // these indices must match the projection
    private static final int INDEX_ID = 0;
    private static final int INDEX_TITLE = 1;

    public PopularMoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Logger.d(LOG_TAG,"onPerformSync");
        if (!NetworkUtil.getConnectivityStatus(getContext())){
            ToastUtil.show(getContext(),"无网络连接，无法获取网络数据");
            return;
        }
        String movieJsonStrForPopular = getMovieJsonStr("popular");
        String movieJsonStrForToprated = getMovieJsonStr("toprated");
        try{
            int idOfFirstRankForPopularBefore = getFirstRankIdByMode("popular");
            int idOfFirstRankForTopratedBefore = getFirstRankIdByMode("toprated");
            
            setMovieRankToZero();
            Logger.d(LOG_TAG,"set rank of movies to 0");
            int idOfFirstRankForPopularAfter = getMovieDataFromJson(movieJsonStrForPopular,"popular");
            int idOfFirstRankForTopratedAfter = getMovieDataFromJson(movieJsonStrForToprated,"toprated");
            boolean isPopularFirstItemChanged
                    = idOfFirstRankForPopularBefore != idOfFirstRankForPopularAfter;
            boolean isTopratedFirstItemChanged
                    = idOfFirstRankForTopratedBefore != idOfFirstRankForTopratedAfter;
            //通知用户某一模式下的排名的变化，第三个参数为当无变化时是否通知用户。

            if (idOfFirstRankForPopularAfter != -1 && idOfFirstRankForTopratedAfter != -1) {
                EventBus.getDefault().post(new MessageEvent("onLoadingFinished"));
                Logger.d(LOG_TAG,"onLoadingFinished");
            }else{
                EventBus.getDefault().post(new MessageEvent("onDownloadFailed"));
                Logger.d(LOG_TAG,"onDownloadFailed");
            }
            notifyMovie(isPopularFirstItemChanged,"popular",false);
            notifyMovie(isTopratedFirstItemChanged,"toprated",false);
        }catch (JSONException e) {
            Logger.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private String getMovieJsonStr(String mode){
        // If there's no zip code, there's nothing to look up.  Verify size of params.

        final String LOG_TAG = "getMovieJsonStr";

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr;
        String BASE_URL;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast

            if(mode.equals("popular")) {
                BASE_URL = "http://api.themoviedb.org/3/movie/popular?";
            }else if(mode.equals("toprated")) {
                BASE_URL = "http://api.themoviedb.org/3/movie/top_rated?";
            }else{
                Logger.v(LOG_TAG,"mode is wrong");
                return null;
            }

            final String LANGUAGE_PARAM = "language";
            final String APPID_PARAM = "api_key";

            Uri builtPopularUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(LANGUAGE_PARAM, "zh")
                    .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_MOVIE_API_KEY)
                    .build();

            URL url = new URL(builtPopularUri.toString());
            Logger.v(LOG_TAG,"url is " + url);

            // Create the request to Movie, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(10000); //超时设置
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            Logger.v(LOG_TAG,"url connected");
            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            movieJsonStr = buffer.toString();
            Logger.v(LOG_TAG,"movieJsonStr is " + movieJsonStr);
            return movieJsonStr;
        } catch (IOException e) {
            Logger.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Logger.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    //从movieJsonStr中获取电影的详细数据，插入数据库中,并返回该排列模式下第一名的id，若无则返回-1。
    private int getMovieDataFromJson(String movieJsonStr,String mode)
            throws JSONException {

        int firstRankId  = -1;

        final String OWM_POSTER_PATH = "poster_path";

        final String OWM_ADULT = "adult";
        final String OWM_OVERVIEW = "overview";
        final String OWM_RELEASE_DATE = "release_date";
        final String OWM_ID = "id";
        final String OWM_ORIGINAL_TITLE = "original_title";
        final String OWM_ORIGINAL_LANGUAGE = "original_language";
        final String OWM_TITLE = "title";
        final String OWM_POPULARITY = "popularity";
        final String OWM_VOTE_COUNT = "vote_count";
        final String OWM_VIDEO = "video";
        final String OWM_VOTE_AVERAGE = "vote_average";


        try {
            final String OWM_LIST = "results";

            if (movieJsonStr != null){
                JSONObject rankJson = new JSONObject(movieJsonStr);
                JSONArray movieArray = rankJson.getJSONArray(OWM_LIST);

                int numberOfMovie = movieArray.length();

                if (numberOfMovie == 1){
                    firstRankId = -1;
                }

                MovieDbHelper dbHelper = MovieDbHelper.getDbHelper(getContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                ContentValues movieValues = new ContentValues();

                //MovieEntry所需要的数据
                String posterPath;
                Bitmap posterImage;
                String adult;
                String overview;
                String releaseDate;
                String reviews;
                String videos;
                String originalTitle;
                String originalLanguage;
                String title;
                float popularity;
                int voteCount;
                String video;
                float voteAverage;
                String collect;
                int runtime;
                int popularRank;
                int topratedRank;

                // Insert the new movie information into the database
                Vector<ContentValues> cVVector = new Vector<ContentValues>(movieArray.length());
                for(int i = 0; i < numberOfMovie; i++) {

                    // 根据位置获取电影信息
                    JSONObject movieInfo = movieArray.getJSONObject(i);

                    int id = Integer.parseInt(movieInfo.getString(OWM_ID));
                    //返回第一名的Id。
                    if (i == 0) {
                        firstRankId = id;
                    }

                    //根据电影id查询电影是否在数据表中
                    String idSelection = MovieContract.MovieEntry.TABLE_NAME+
                            "." + MovieContract.MovieEntry.COLUMN_ID + " = ? ";
                    String[] idSelectionArgs = new String[]{Integer.toString(id)};
                    Cursor getCursorById = db.query(
                            MovieContract.MovieEntry.TABLE_NAME,  // Table to Query
                            null, // leaving "columns" null just returns all the columns.
                            idSelection, // cols for "where" clause
                            idSelectionArgs, // values for "where" clause
                            null, // columns to group by
                            null, // columns to filter by row groups
                            null  // sort order
                    );

                    if (!getCursorById.moveToFirst())
                    {

                        //获取电影海报地址
                        posterPath = "https://image.tmdb.org/t/p/w185" + movieInfo.getString(OWM_POSTER_PATH);

                        posterImage = getImageFromUrl(posterPath);

                        adult = movieInfo.getString(OWM_ADULT);
                        overview = movieInfo.getString(OWM_OVERVIEW);
                        releaseDate = movieInfo.getString(OWM_RELEASE_DATE);

                        originalTitle = movieInfo.getString(OWM_ORIGINAL_TITLE);
                        originalLanguage = movieInfo.getString(OWM_ORIGINAL_LANGUAGE);
                        title =movieInfo.getString(OWM_TITLE);
                        popularity = Float.parseFloat(movieInfo.getString(OWM_POPULARITY));
                        voteCount = Integer.parseInt(movieInfo.getString(OWM_VOTE_COUNT));
                        video = movieInfo.getString(OWM_VIDEO);
                        voteAverage = Float.parseFloat(movieInfo.getString(OWM_VOTE_AVERAGE));
                        collect = "false";

                        //获取含runtime的Json，并提取出runtime
                        String runtimeJson = getJsonStrFromId(id,"runtime");
                        JSONObject movieJson = new JSONObject(runtimeJson);
                        runtime = movieJson.getInt("runtime");

                        //获取reviews，videos的JsonString
                        reviews = getJsonStrFromId(id,"reviews");
                        videos = getJsonStrFromId(id,"videos");

                        popularRank = mode.equals("popular")?i+1:0;
                        topratedRank = mode.equals("popular")?0:i+1;

                        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, posterPath);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_IMAGE,bitmapToByte(posterImage));
                        movieValues.put(MovieContract.MovieEntry.COLUMN_ADULT, adult);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, overview);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_ID, id);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, originalTitle);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, originalLanguage);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, title);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, popularity);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_VIDEO, video);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, voteCount);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, voteAverage);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_COLLECT, collect);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_RUNTIME, runtime);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_POPULAR_RANK, popularRank);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_TOPRATED_RANK, topratedRank);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_REVIEWS, reviews);
                        movieValues.put(MovieContract.MovieEntry.COLUMN_VIDEOS, videos);
                        Logger.v(LOG_TAG,"insert movieValues: " + movieValues);

                        cVVector.add(movieValues);
                    }else {

                        //更新对应排序模式的排名
                        ContentValues rankValue = new ContentValues();
                        if (mode.equals("popular")) {
                            rankValue.put(MovieContract.MovieEntry.COLUMN_POPULAR_RANK, i + 1);
                        } else if (mode.equals("toprated")) {
                            rankValue.put(MovieContract.MovieEntry.COLUMN_TOPRATED_RANK, i + 1);
                        } else {
                            Logger.d(LOG_TAG, "mode is wrong");
                        }

                        getContext().getContentResolver().update(MovieContract.MovieEntry.CONTENT_URI,
                                rankValue, idSelection, idSelectionArgs);
                        Logger.d(LOG_TAG,"movie is already in database,app only updates its rank");
                    }
                    getCursorById.close();
                }
                int inserted = 0;
                // add to database
                if ( cVVector.size() > 0 ) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    inserted = getContext().getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);
                }

                Logger.d(LOG_TAG, "FetchWeatherTask Complete. " + inserted + " Inserted");
                db.close();
                Logger.d(LOG_TAG,"fetch data from internet");
            }else{
                Logger.d(LOG_TAG,"fail to fetch data ,because Json is null");
            }
        } catch (JSONException e) {
            Logger.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }catch (NullPointerException e){
            Logger.e(LOG_TAG,e.getMessage(), e);
        }
        return firstRankId;
    }

    //根据获取的Id，获取runtime，reviews，videos的数据
    private String getJsonStrFromId(int id, String dataName){

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr;
        String BASE_URL;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast

            final String APPID_PARAM = "api_key";


            if (dataName.equals("reviews")) {
                BASE_URL = "http://api.themoviedb.org/3/movie/" + Integer.toString(id) + "/reviews?";
            }else if (dataName.equals("videos")) {
                BASE_URL = "http://api.themoviedb.org/3/movie/" + Integer.toString(id) + "/videos?";
            }else if (dataName.equals("runtime")) {
                BASE_URL = "http://api.themoviedb.org/3/movie/" + Integer.toString(id);
            }else{
                throw new UnsupportedOperationException("Unknown dataName: " + dataName);
            }

            Uri builtPopularUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_MOVIE_API_KEY)
                    .build();

            URL url = new URL(builtPopularUri.toString());
            Logger.d(LOG_TAG,"url is " + url);

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(10000);
            urlConnection.connect();

            Logger.v(LOG_TAG,"url connected");
            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            movieJsonStr = buffer.toString();
            Logger.d(LOG_TAG,dataName + "JsonStr is " + movieJsonStr);
            return movieJsonStr;
        } catch (IOException e) {
            Logger.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Logger.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    //将排名归零，即无排名
    private void setMovieRankToZero(){
        MovieDbHelper dbHelper = MovieDbHelper.getDbHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(
                MovieContract.MovieEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        if(cursor!=null&&cursor.moveToFirst()) {
            ContentValues rankValue = new ContentValues();
            rankValue.put(MovieContract.MovieEntry.COLUMN_POPULAR_RANK, 0);
            rankValue.put(MovieContract.MovieEntry.COLUMN_TOPRATED_RANK, 0);

            while (!cursor.isAfterLast()) {

                getContext().getContentResolver().update(MovieContract.MovieEntry.CONTENT_URI,
                        rankValue,null,null);
                cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();
    }

    //查询某一模式下排名第一的电影的ID的排名,没有则返回-1
    private int getFirstRankIdByMode(String mode){
        MovieDbHelper dbHelper = MovieDbHelper.getDbHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int idOfFirstRank = -1;

        String sPopularAndRankSelection =
                MovieContract.MovieEntry.TABLE_NAME+
                        "." + MovieContract.MovieEntry.COLUMN_POPULAR_RANK + " = ? ";

        String sTopratedAndRankSelection =
                MovieContract.MovieEntry.TABLE_NAME+
                        "." + MovieContract.MovieEntry.COLUMN_TOPRATED_RANK + " = ? ";

        String selection = mode.equals("popular")?sPopularAndRankSelection:sTopratedAndRankSelection;

        Cursor cursor = db.query(
                MovieContract.MovieEntry.TABLE_NAME,  // Table to Query
                new String[] {MovieContract.MovieEntry.COLUMN_ID}, // leaving "columns" null just returns all the columns.
                selection, // cols for "where" clause
                new String[] {Integer.toString(1)}, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        if(cursor!=null&&cursor.moveToFirst()) {
            idOfFirstRank = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_ID));
        }
        cursor.close();
        db.close();
        return idOfFirstRank;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        PopularMoviesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    public static void changeSyncInterval(Context context, String syncInterval) {
        SYNC_INTERVAL = Integer.parseInt(syncInterval) * 60 * 60;
        SYNC_FLEXTIME = SYNC_INTERVAL / 3;
        PopularMoviesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        Logger.d("changeSyncInterval","syncinterval is " + SYNC_INTERVAL);
    }

    private void notifyMovie(boolean isFirstItemChanged,String mode,boolean isNotifyIfNotChanged) {
        if (!isFirstItemChanged && !isNotifyIfNotChanged){
            Logger.d(LOG_TAG,"not notify");
            return;
        }
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
        Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if ( displayNotifications ) {
            String lastNotificationKey = mode.equals("popular")?
                    context.getString(R.string.pref_last_notification_popular):
                    context.getString(R.string.pref_last_notification_toprated);
            long lastSync = prefs.getLong(lastNotificationKey, 0);

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {

                String title = context.getString(R.string.app_name);
                Uri movieUri = MovieContract.MovieEntry.buildMovieWithModeAndRankUri(mode, 1);

                // we'll query our contentProvider, as always
                Cursor cursor = context.getContentResolver().query(movieUri, NOTIFY_MOVIE_PROJECTION, null, null, null);

                if (cursor.moveToFirst()) {
//                        int movieId = cursor.getInt(INDEX_ID);
                    String movieTitle = cursor.getString(INDEX_TITLE);
//                        String releaseDate = cursor.getString(INDEX_RELEASE_DATE);
//                        String voteAverage = cursor.getString(INDEX_VOTE_AVERAGE);

                    String contentText;

                    String modeName = mode.equals("popular")?
                            context.getString(R.string.pref_mode_label_popular):
                            context.getString(R.string.pref_mode_label_toprated);

                    int NOTIFICATION_ID = mode.equals("popular")?
                            POPULAR_NOTIFICATION_ID:TOPRATED_NOTIFICATION_ID;

                    if (isFirstItemChanged){
                        contentText = String.format(context.getString(R.string.notification_changed),
                                modeName,
                                movieTitle);
                    }else{
                        contentText = String.format(context.getString(R.string.notification_no_changed),
                                modeName);
                    }

                    // NotificationCompatBuilder is a very convenient way to build backward-compatible
                    // notifications.  Just throw in some data.
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getContext())
                                    .setSmallIcon(R.drawable.ic_movie_black_24dp)
                                    .setContentTitle(title)
                                    .setContentText(contentText);

                    // Make something interesting happen when the user clicks on the notification.
                    // In this case, opening the app is sufficient.
                    Intent resultIntent = new Intent(context, MainActivity.class);

                    // The stack builder object will contain an artificial back stack for the
                    // started Activity.
                    // This ensures that navigating backward from the Activity leads out of
                    // your application to the Home screen.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager =
                            (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    // NOTIFICATION_ID allows you to update the notification later on.
                    Notification notification = mBuilder.build();

                    //点击时取消
                    notification.flags |= Notification.FLAG_AUTO_CANCEL;
                    mNotificationManager.notify(NOTIFICATION_ID, notification);

                    //refreshing last sync
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(lastNotificationKey, System.currentTimeMillis());
                    editor.commit();
                }
                cursor.close();
            }
        }
    }
}