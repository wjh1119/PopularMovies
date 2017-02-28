package app.com.example.android.popularmovies.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import app.com.example.android.popularmovies.BuildConfig;
import app.com.example.android.popularmovies.NetworkUtil;
import app.com.example.android.popularmovies.data.MovieContract;
import app.com.example.android.popularmovies.data.MovieDbHelper;

import static app.com.example.android.popularmovies.Utility.bitmapToByte;
import static app.com.example.android.popularmovies.Utility.getImageFromUrl;

/**
 * Created by Mr.King on 2017/2/24 0024.
 */

public class PopularMoviesService extends IntentService {
    public static final String MODE_QUERY_EXTRA = "mqe";
    private final String LOG_TAG = PopularMoviesService.class.getSimpleName();
    public PopularMoviesService() {
        super("PopularMovies");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (!NetworkUtil.getConnectivityStatus(this)){
            Toast.makeText(this,"无网络连接，无法获取网络数据", Toast.LENGTH_LONG).show();
            return;
        }
        String movieJsonStrForPopular = getMovieJsonStr("popular");
        String movieJsonStrForToprated = getMovieJsonStr("toprated");
        try{
            getMovieDataFromJson(movieJsonStrForToprated,"toprated");
            Log.v(LOG_TAG,"getMovieData toprated from " + movieJsonStrForToprated);
            getMovieDataFromJson(movieJsonStrForPopular,"popular");
            Log.v(LOG_TAG,"getMovieData popular from " + movieJsonStrForPopular);
        }catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
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
                Log.v(LOG_TAG,"mode is wrong");
                return null;
            }

            final String LANGUAGE_PARAM = "language";
            final String APPID_PARAM = "api_key";

            Uri builtPopularUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(LANGUAGE_PARAM, "zh")
                    .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_MOVIE_API_KEY)
                    .build();

            URL url = new URL(builtPopularUri.toString());
            Log.v(LOG_TAG,"url is " + url);

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            Log.v(LOG_TAG,"url connected");
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
            Log.v(LOG_TAG,"movieJsonStr is " + movieJsonStr);
            return movieJsonStr;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
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
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    //从movieJsonStr中获取电影的详细数据，并插入值MovieEntry中
    private void getMovieDataFromJson(String movieJsonStr,String mode)
            throws JSONException {

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

//                int numberOfMovie = movieArray.length();
                int numberOfMovie = 4;

                MovieDbHelper dbHelper = new MovieDbHelper(this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                // Insert the new movie information into the database
                for(int i = 0; i < numberOfMovie; i++) {

                    ContentValues movieValues = new ContentValues();

                    //MovieEntry所需要的数据
                    String posterPath;
                    Bitmap posterImage;
                    String adult;
                    String overview;
                    String releaseDate;
                    int id;
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
                    // 根据位置获取电影信息
                    JSONObject movieInfo = movieArray.getJSONObject(i);

                    //获取电影海报地址
                    posterPath = "https://image.tmdb.org/t/p/w185" + movieInfo.getString(OWM_POSTER_PATH);

                    posterImage = getImageFromUrl(posterPath);

                    adult = movieInfo.getString(OWM_ADULT);
                    overview = movieInfo.getString(OWM_OVERVIEW);
                    releaseDate = movieInfo.getString(OWM_RELEASE_DATE);

                    //根据电影id查询电影是否在数据表中
                    id = Integer.parseInt(movieInfo.getString(OWM_ID));

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

                    popularRank = 0;
                    topratedRank = 0;
                    if (mode.equals("popular")){
                        popularRank = i+1;
                        topratedRank = 0;
                    }else if (mode.equals("toprated")){
                        popularRank = 0;
                        topratedRank = i+1;
                    }else{
                        Log.e(LOG_TAG,"mode is wrong");
                    }


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

                    if (getCursorById.moveToFirst() == false)
                    {
                        this.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,movieValues);
                    }else{

                        //如果id已存在，更新对应排序模式的排名
                        ContentValues modeValue = new ContentValues();
                        if (mode.equals("popular")){
                            modeValue.put(MovieContract.MovieEntry.COLUMN_POPULAR_RANK, popularRank);
                        }else if (mode.equals("toprated")){
                            modeValue.put(MovieContract.MovieEntry.COLUMN_TOPRATED_RANK, topratedRank);
                        }else{
                            Log.d(LOG_TAG,"mode is wrong");
                        }

                        this.getContentResolver().update(MovieContract.MovieEntry.CONTENT_URI,
                                modeValue,idSelection,idSelectionArgs);
                    }

                    Log.v(LOG_TAG,"insert movieValues: " + movieValues);
                }
                Log.v(LOG_TAG,"fetch data from internet");
//            int inserted = 0;
//            // add to database
//            if ( cVVector.size() > 0 ) {
//                ContentValues[] cvArray = new ContentValues[cVVector.size()];
//                cVVector.toArray(cvArray);
//                inserted = this.getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);
//            }

            }else{
                Log.v(LOG_TAG,"Json is null");
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }catch (NullPointerException e){
            Log.e(LOG_TAG,e.getMessage(), e);
        }
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
            Log.v(LOG_TAG,"url is " + url);

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            Log.v(LOG_TAG,"url connected");
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
            Log.v(LOG_TAG,dataName + "JsonStr is " + movieJsonStr);
            return movieJsonStr;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
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
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    public static class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent sendIntent = new Intent(context, PopularMoviesService.class);
            sendIntent.putExtra(PopularMoviesService.MODE_QUERY_EXTRA,
                    intent.getStringExtra(PopularMoviesService.MODE_QUERY_EXTRA));
            context.startService(sendIntent);

        }
    }
}
