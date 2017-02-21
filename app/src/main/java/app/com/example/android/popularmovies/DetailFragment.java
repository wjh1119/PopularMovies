package app.com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import app.com.example.android.popularmovies.data.MovieContract;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Mr.King on 2017/2/17 0017.
 */

public class DetailFragment extends Fragment{
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    //使用Butter knife对id和view进行连接
    @BindView(R.id.movie_detail_name)
    TextView nameTextView;
    @BindView(R.id.movie_detail_date) TextView dateTextView;
    @BindView(R.id.movie_detail_vote_average) TextView voteAverageTextView;
    @BindView(R.id.movie_detail_overview_open) TextView overviewOpenTextView;
    @BindView(R.id.movie_detail_overview_close) TextView overviewCloseTextView;
    @BindView(R.id.movie_detail_overview_openOrClose) TextView overviewOpenOrCloseTextView;
    @BindView(R.id.movie_detail_runtime) TextView runtimeTextView;
    @BindView(R.id.movie_detail_videos) TextView videosTextView;
    @BindView(R.id.movie_detail_reviews_list)
    UnScrollListView reviewsListView;
    @BindView(R.id.movie_detail_videos_list)
    UnScrollListView videosListView;
    @BindView(R.id.movie_detail_image) ImageView imageView;

    private Unbinder unbinder;

    private String mMovieId;

    private String mIsCollect = null;

    private View mView;

    private static final int DETAIL_LOADER = 0;
    private static final int REVIEWS_LOADER = 1;
    private static final int VIDEOS_LOADER = 2;

    public static final String[] MOVIE_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.

            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_POSTER_IMAGE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_COLLECT,
            MovieContract.MovieEntry.COLUMN_RUNTIME,

            //排名
            MovieContract.MovieEntry.COLUMN_REVIEWS,
            MovieContract.MovieEntry.COLUMN_VIDEOS,
    };

    static final int _ID = 0;
    static final int COL_POSTER_IMAGE = 1;
    static final int COL_OVERVIEW = 2;
    static final int COL_RELEASE_DATE = 3;
    static final int COL_ID = 4;
    static final int COL_TITLE= 5;
    static final int COL_VOTE_AVERAGE = 6;

    //weather the movie is collected
    static final int COL_COLLECT = 7;

    //movie's runtime
    static final int COL_RUNTIME = 8;

    //
    static final int COL_REVIEWS = 9;
    static final int COL_VIDEOS = 10;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_detail, container, false);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //加载Loader
        getLoaderManager().initLoader(DETAIL_LOADER, null, new DetailLoaderCallbacks());
        getLoaderManager().initLoader(REVIEWS_LOADER, null, new ReviewsLoaderCallbacks());
        getLoaderManager().initLoader(VIDEOS_LOADER, null, new VideosLoaderCallbacks());
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_collect);

        //判断该电影是否被收藏，并以此显示对应的菜单“收藏”或“取消收藏”。
        if (mIsCollect != null) {
            if (mIsCollect.equals("true")){
                menuItem.setTitle("取消收藏");
            }else{
                menuItem.setTitle("收藏");
            }
        }else{
            Log.d(LOG_TAG,"mIsCollect is null");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //实现收藏功能
        int id = item.getItemId();
        if (id == R.id.action_collect) {
            Log.v(LOG_TAG,"click");
            if(item.getTitle().equals("收藏")){
                Toast.makeText(getContext(),"收藏成功", Toast.LENGTH_LONG).show();
                item.setTitle("取消收藏");
                updateCollect("true");
                mIsCollect = "true";
            }else{
                Toast.makeText(getContext(),"取消收藏", Toast.LENGTH_LONG).show();
                item.setTitle("收藏");
                updateCollect("false");
                mIsCollect = "false";
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateCollect(String isCollect){

        //更新数据库中影片的收藏值
        if (mMovieId != null && isCollect != null) {
            ContentValues values = new ContentValues();
            values.put(MovieContract.MovieEntry.COLUMN_COLLECT, isCollect);
            String selection = MovieContract.MovieEntry.TABLE_NAME+
                    "." + MovieContract.MovieEntry.COLUMN_ID + " = ? ";
            String[] selectionArgs = new String[]{mMovieId};
            getContext().getContentResolver().update(MovieContract.MovieEntry.CONTENT_URI,values,selection,selectionArgs);
            mIsCollect = isCollect;
        }else{
            Toast.makeText(getContext(),"数据正在加载，请稍后再试", Toast.LENGTH_LONG).show();
        }
    }

    //电影的详细信息（除评论及预告片）的回调
    private class DetailLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.v(LOG_TAG, "In onCreateLoader");
            Intent intent = getActivity().getIntent();
            if (intent == null) {
                return null;
            }
            Log.v(LOG_TAG,intent.getData().toString());

            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    intent.getData(),
                    MOVIE_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            if (!data.moveToFirst()) { return; }

            //ButterKnife连接
            unbinder = ButterKnife.bind(DetailFragment.this, mView);

            try {

                //载入电影名字
                String name = data.getString(COL_TITLE);
                nameTextView.setText(name);

                //载入电影图片
                byte[] imageBlob = data.getBlob(COL_POSTER_IMAGE);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
                imageView.setImageDrawable(bitmapDrawable);

                mMovieId = data.getString(COL_ID);
                mIsCollect = data.getString(COL_COLLECT);

                //载入电影上映日期
                String date = data.getString(COL_RELEASE_DATE);
                dateTextView.setText(date);

                //载入电影评分
                String voteAverage = data.getString(COL_VOTE_AVERAGE);
                voteAverageTextView.setText(voteAverage+getContext().getResources().getString(R.string.movie_detail_voteAverage_extraText));

                //movie's runtime
                int runtime = data.getInt(COL_RUNTIME);
                runtimeTextView.setText(runtime + getContext().getResources().
                        getString(R.string.movie_detail_runtime_extraText));

                //载入电影简介，并实现展开收起的功能
                String overview = data.getString(COL_OVERVIEW);
                overviewCloseTextView.setText(overview);
                overviewOpenTextView.setText(overview);
                overviewOpenTextView.setVisibility(View.GONE);
                overviewOpenOrCloseTextView.setText(R.string.movie_detail_overview_open);
                overviewOpenOrCloseTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(overviewCloseTextView.getVisibility() == View.VISIBLE){
                            overviewCloseTextView.setVisibility(View.GONE);
                            overviewOpenTextView.setVisibility(View.VISIBLE);
                            overviewOpenOrCloseTextView.setText(R.string.movie_detail_overview_close);
                        }else{
                            overviewCloseTextView.setVisibility(View.VISIBLE);
                            overviewOpenTextView.setVisibility(View.GONE);
                            overviewOpenOrCloseTextView.setText(R.string.movie_detail_overview_open);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) { }
    }

    //电影评论的回调
    private class ReviewsLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.v(LOG_TAG, "In onCreateLoader");
            Intent intent = getActivity().getIntent();
            if (intent == null) {
                return null;
            }
            Log.v(LOG_TAG,intent.getData().toString());

            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    intent.getData(),
                    MOVIE_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            if (!data.moveToFirst()) { return; }

            //ButterKnife连接
            unbinder = ButterKnife.bind(DetailFragment.this, mView);

            try {

                String reviewsJsonStr = data.getString(COL_REVIEWS);
                final String OWM_LIST = "results";

                if (reviewsJsonStr != null) {
                    JSONObject rankJson = new JSONObject(reviewsJsonStr);
                    JSONArray reviewsJsonArray = rankJson.getJSONArray(OWM_LIST);
                    ArrayList<HashMap> reviewsDataArray = new ArrayList<>();
                    int numberOfReviews = reviewsJsonArray.length();
                    //当无评论时显示无评论
                    if (numberOfReviews == 0){
                        HashMap reviewHashMap = new HashMap();
                        String number = getContext().getResources().
                                getString(R.string.movie_detail_reviews_none_text);
                        String content = "";
                        String author = "";

                        reviewHashMap.put("number",number);
                        reviewHashMap.put("content",content);
                        reviewHashMap.put("author",author);

                        reviewsDataArray.add(reviewHashMap);
                    }
                    for(int i = 0; i < numberOfReviews; i++) {
                        HashMap reviewHashMap = new HashMap();
                        String content;
                        String author;

                        final String OWM_CONTENT = "content";
                        final String OWM_AUTHOR = "author";

                        JSONObject reviewInfo = reviewsJsonArray.getJSONObject(i);

                        content = reviewInfo.getString(OWM_CONTENT);
                        author = reviewInfo.getString(OWM_AUTHOR);
                        if (content != null || author != null){

                            //打包影片的评论信息并传入适配器
                            reviewHashMap.put("number",getContext().getResources().
                                    getString(R.string.movie_detail_reviews_itemtitle_text) + (i+1));
                            reviewHashMap.put("content",content);
                            reviewHashMap.put("author","------" + author);

                            reviewsDataArray.add(reviewHashMap);
                        }
                    }
                    reviewsListView.setAdapter(new ReviewAdapter(getActivity(),reviewsDataArray));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) { }
    }

    //电影预告片的回调
    private class VideosLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.v(LOG_TAG, "In onCreateLoader");
            Intent intent = getActivity().getIntent();
            if (intent == null) {
                return null;
            }
            Log.v(LOG_TAG,intent.getData().toString());

            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    intent.getData(),
                    MOVIE_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            if (!data.moveToFirst()) { return; }

            //ButterKnife连接
            unbinder = ButterKnife.bind(DetailFragment.this, mView);

            try {

                String videosJsonStr = data.getString(COL_VIDEOS);
                final String OWM_LIST = "results";

                if (videosJsonStr != null) {
                    JSONObject videosJson = new JSONObject(videosJsonStr);
                    JSONArray videosJsonArray = videosJson.getJSONArray(OWM_LIST);
                    ArrayList<HashMap> videosDataArray = new ArrayList<>();
                    int numberOfvideos = videosJsonArray.length();

                    //无预告片时显示无预告
                    if (numberOfvideos == 0){
                        HashMap videoHashMap = new HashMap();
                        String number = getContext().getResources().
                                getString(R.string.movie_detail_videos_none_text);
                        String name = "";
                        String key = "";

                        //打包信息并传入适配器
                        videoHashMap.put("number",number);
                        videoHashMap.put("name",name);
                        videoHashMap.put("key",key);

                        videosDataArray.add(videoHashMap);
                    }
                    for(int i = 0; i < numberOfvideos; i++) {
                        HashMap videoHashMap = new HashMap();
                        String number;
                        String name;
                        String key;

                        final String OWM_NAME = "name";
                        final String OWM_KEY = "key";

                        JSONObject videoInfo = videosJsonArray.getJSONObject(i);

                        number = getContext().getResources().
                                getString(R.string.movie_detail_videos_itemtitle_text) + (i+1);
                        name = videoInfo.getString(OWM_NAME);
                        key = videoInfo.getString(OWM_KEY);
                        if (name != null ){

                            //打包信息并传入适配器
                            videoHashMap.put("number",number);
                            videoHashMap.put("name",name);
                            videoHashMap.put("key",key);

                            videosDataArray.add(videoHashMap);
                        }
                    }
                    videosListView.setAdapter(new VideoAdapter(getActivity(),videosDataArray));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) { }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
