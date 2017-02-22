package app.com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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

import java.util.ArrayList;
import java.util.HashMap;

import app.com.example.android.popularmovies.data.MovieContract;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Mr.King on 2017/2/17 0017.
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
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

    private Cursor mData;

    private MenuItem mCollectMenuItem;

    private static final int DETAIL_LOADER = 0;

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
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        mCollectMenuItem = menu.findItem(R.id.action_collect);

        //判断该电影是否被收藏，并以此显示对应的菜单“收藏”或“取消收藏”。
        if (mIsCollect != null) {
            if (mIsCollect.equals("true")){
                //menuItem.setTitle(R.string.action_collect_cancel); //"取消收藏“
                mCollectMenuItem.setIcon(R.drawable.ic_favorite_white_24dp);
            }else{
                //menuItem.setTitle(R.string.action_collect);//”收藏
                mCollectMenuItem.setIcon(R.drawable.ic_favorite_border_white_24dp);
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
            if(mIsCollect.equals("false")){//“收藏”
                Toast.makeText(getContext(),"收藏成功", Toast.LENGTH_LONG).show();
                //item.setTitle(getString(R.string.action_collect_cancel));//“取消收藏”
                item.setIcon(R.drawable.ic_favorite_white_24dp);
                updateCollect("true");
                mIsCollect = "true";
            }else{
                Toast.makeText(getContext(),"取消收藏", Toast.LENGTH_LONG).show();
                //item.setTitle(getString(R.string.action_collect));//“收藏”
                item.setIcon(R.drawable.ic_favorite_border_white_24dp);
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

        Log.d(LOG_TAG,Thread.currentThread().getName());
        mData = data;
        //ButterKnife连接
        unbinder = ButterKnife.bind(DetailFragment.this, mView);

        FetchDetailTask fetchDetailTask = new FetchDetailTask(getContext());
        fetchDetailTask.setOnDataFinishedListener(new FetchDetailTask.OnDataFinishedListener(){
            @Override
            public void onDataSuccessfully(Object data) {
                HashMap detailDataArray = (HashMap) data;

                //载入电影名字
                String name = (String) detailDataArray.get("name");
                nameTextView.setText(name);
                nameTextView.setFocusable(true);
                nameTextView.setFocusableInTouchMode(true);
                nameTextView.requestFocus();

                //载入电影图片
                BitmapDrawable bitmapDrawable = (BitmapDrawable) detailDataArray
                        .get("bitmapDrawable");
                imageView.setImageDrawable(bitmapDrawable);

                mMovieId = (String) detailDataArray.get("movieId");
                mIsCollect = (String) detailDataArray.get("isCollect");
                if (mCollectMenuItem != null) {
                    if (mIsCollect.equals("true")){
                        //menuItem.setTitle(R.string.action_collect_cancel); //"取消收藏“
                        mCollectMenuItem.setIcon(R.drawable.ic_favorite_white_24dp);
                    }else{
                        //menuItem.setTitle(R.string.action_collect);//”收藏
                        mCollectMenuItem.setIcon(R.drawable.ic_favorite_border_white_24dp);
                    }
                }else{
                    Log.d(LOG_TAG,"mIsCollect is null");
                }

                //载入电影上映日期
                String date = (String) detailDataArray.get("date");
                dateTextView.setText(date);

                //载入电影评分
                String voteAverage = (String) detailDataArray.get("voteAverage");
                voteAverageTextView.setText(voteAverage+getString(R.string.movie_detail_voteAverage_extraText));

                //movie's runtime
                int runtime = (int) detailDataArray.get("runtime");
                runtimeTextView.setText(runtime + getString(R.string.movie_detail_runtime_extraText));

                //载入电影简介，并实现展开收起的功能
                String overview = (String) detailDataArray.get("overview");
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
            }

            @Override
            public void onDataFailed() {
                Toast.makeText(getContext(),"获取影片详细信息失败",Toast.LENGTH_SHORT).show();
            }
        });
        fetchDetailTask.execute(mData);

        FetchReviewsTask fetchReviewsTask = new FetchReviewsTask(getContext());
        fetchReviewsTask.setOnDataFinishedListener(new FetchReviewsTask.OnDataFinishedListener(){
            @Override
            public void onDataSuccessfully(Object data) {
                ArrayList<HashMap> reviewsDataArray = (ArrayList<HashMap>) data;
                reviewsListView.setAdapter(new ReviewAdapter(getActivity(),reviewsDataArray));
            }

            @Override
            public void onDataFailed() {
                Toast.makeText(getContext(),"获取评论数据失败",Toast.LENGTH_SHORT).show();
            }
        });
        fetchReviewsTask.execute(mData);

        FetchVideosTask fetchVideosTask = new FetchVideosTask(getContext());
        fetchVideosTask.setOnDataFinishedListener(new FetchVideosTask.OnDataFinishedListener(){
            @Override
            public void onDataSuccessfully(Object data) {
                ArrayList<HashMap> videosDataArray = (ArrayList<HashMap>) data;
                videosListView.setAdapter(new VideoAdapter(getActivity(),videosDataArray));
            }

            @Override
            public void onDataFailed() {
                Toast.makeText(getContext(),"获取预告片数据失败",Toast.LENGTH_SHORT).show();
            }
        });
        fetchVideosTask.execute(mData);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
