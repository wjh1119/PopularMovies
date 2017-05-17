/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.com.example.android.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.greenrobot.eventbus.EventBus;

import app.com.example.android.popularmovies.data.MovieContract;

import static app.com.example.android.popularmovies.data.MovieContract.MovieEntry.COLUMN_POPULAR_RANK;

/**
 * Encapsulates fetching the forecast and displaying it as a {@link GridView} layout.
 */
public class MovieFragment extends Fragment {

    String LOG_TAG = MovieFragment.class.getSimpleName();
    private static final int MOVIE_LOADER = 0;
    private static final int COLLECTION_LOADER = 1;
    private boolean mIsShowCollection = false;

    private GridView mGridView;
    private int mPosition = GridView.INVALID_POSITION;

    private static final String SELECTED_KEY = "selected_position";
    private static final String ISSHOWCOLLECTION_KEY = "isshwocollection";

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    public static final String[] MOVIE_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.

            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_POSTER_IMAGE,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POPULAR_RANK,
            MovieContract.MovieEntry.COLUMN_TOPRATED_RANK,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
    };

    // These indices are tied to MOVIE_COLUMNS.  If MOVIE_COLUMNS changes, these
    // must change.
    static final int _ID = 0;
    static final int COL_POSTER_PATH = 1;
    static final int COL_POSTER_IMAGE = 2;
    static final int COL_TITLE= 3;
    static final int COL_POPULAR_RANK = 4;
    static final int COL_TOPRATED_RANK = 5;
    static final int COL_VOTE_AVERAGE = 6;

    private MovieAdapter mMovieAdapter;

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.rankfragment, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The CursorAdapter will take data from our cursor and populate the ListView.
        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mGridView = (GridView) rootView.findViewById(R.id.grid_fragment);
        View emptyView = rootView.findViewById(R.id.gridview_movie_empty);
        mGridView.setAdapter(mMovieAdapter);
        mGridView.setEmptyView(emptyView);

        // We'll call our MainActivity
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String mode = Utility.getPreferredMode(getActivity());
                    Logger.v("intent","cursor" +" popular rank: " + cursor.getInt(COL_POPULAR_RANK) +
                            ", toprated rank: "+ cursor.getInt(COL_TOPRATED_RANK));
                    if (mode.equals("popular")){
                        EventBus.getDefault().post(new MessageEvent("onItemSelected",
                                MovieContract.MovieEntry.buildMovieWithModeAndRankUri(
                                        mode, cursor.getInt(COL_POPULAR_RANK)
                                )));
                        Logger.v("intent","sent intent with mode: "+ mode +" and rank: "
                                + cursor.getInt(COL_POPULAR_RANK));
                    }else if (mode.equals("toprated")){
                        EventBus.getDefault().post(new MessageEvent("onItemSelected",
                                MovieContract.MovieEntry.buildMovieWithModeAndRankUri(
                                        mode, cursor.getInt(COL_TOPRATED_RANK)
                                )));
                        Logger.v("intent","sent intent with mode: "+ mode +" and rank: "
                                + cursor.getInt(COL_TOPRATED_RANK));
                    }else{
                        Logger.d("intent","mode is wrong");
                    }
                }
            }
        });


        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null) {
            Logger.d(LOG_TAG, "savedInstanceState isn't null");
            if (savedInstanceState.containsKey(SELECTED_KEY)){
                // The gridview probably hasn't even been populated yet.  Actually perform the
                // swapout in onLoadFinished.
                mPosition = savedInstanceState.getInt(SELECTED_KEY);
            }
            if (savedInstanceState.containsKey(ISSHOWCOLLECTION_KEY)){
                mIsShowCollection = savedInstanceState.getBoolean(ISSHOWCOLLECTION_KEY);
                Logger.d("onCreateView","mIsShowCollection is " + mIsShowCollection);
            }
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Logger.d("onActivityCreated","mIsShowCollection is " + mIsShowCollection);
        if (mIsShowCollection){
            getLoaderManager().destroyLoader(MOVIE_LOADER);
            getLoaderManager().initLoader(COLLECTION_LOADER, null, new CollectionLoaderCallbacks());
        }else{
            getLoaderManager().destroyLoader(COLLECTION_LOADER);
            getLoaderManager().initLoader(MOVIE_LOADER, null, new MoviesLoaderCallbacks());
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    void onModeChanged(boolean isShowCollection ) {
        if (isShowCollection) {
            getLoaderManager().restartLoader(COLLECTION_LOADER, null, new CollectionLoaderCallbacks());
        }else{
            getLoaderManager().restartLoader(MOVIE_LOADER, null, new MoviesLoaderCallbacks());
        }
        mGridView.smoothScrollToPosition(0);
    }

    void onIsShowCollectionChanged(boolean isShowCollection){
        if(isShowCollection){//“我的收藏列表”
            getLoaderManager().destroyLoader(MOVIE_LOADER);
            getLoaderManager().restartLoader(COLLECTION_LOADER,null,new CollectionLoaderCallbacks());
        }else{
            getLoaderManager().destroyLoader(COLLECTION_LOADER);
            getLoaderManager().restartLoader(MOVIE_LOADER,null,new MoviesLoaderCallbacks());
        }
        mIsShowCollection = isShowCollection;

        Logger.d("onIsShowCoChanged","isShowcollection is " + isShowCollection);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        outState.putBoolean(ISSHOWCOLLECTION_KEY, mIsShowCollection);
        super.onSaveInstanceState(outState);
    }

    private class MoviesLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor>{
        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            String mode = Utility.getPreferredMode(getActivity());
            Logger.d(LOG_TAG,"onCreateLoader mode is " + mode);

            String sortOrder = MovieContract.MovieEntry.COLUMN_ID + " ASC";
            if (mode.equals("popular")){
                sortOrder = COLUMN_POPULAR_RANK + " ASC";
            }else if (mode.equals("toprated")) {
                sortOrder = MovieContract.MovieEntry.COLUMN_TOPRATED_RANK + " ASC";
            }else{
                Logger.d("mode","error");
            }

            Uri movieForModeUri = MovieContract.MovieEntry.buildMovieWithModeUri(
                    mode);
            Logger.v("onCreateLoader","create movieForModeUri: " + movieForModeUri.toString());

            CursorLoader cursorLoader = new CursorLoader(getActivity(),
                    movieForModeUri,
                    MOVIE_COLUMNS,
                    null,
                    null,
                    sortOrder);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            mMovieAdapter.swapCursor(cursor);
            if (mPosition != GridView.INVALID_POSITION) {
                // If we don't need to restart the loader, and there's a desired position to restore
                // to, do so now.
                mGridView.smoothScrollToPosition(mPosition);
            }

            //第一次加载数据库里没有数据，给予用户提示
            if (!cursor.moveToFirst()){
                EventBus.getDefault().post(new MessageEvent("onNoneItemInList"));
                Logger.d(LOG_TAG,"onNoneItemInList");
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            mMovieAdapter.swapCursor(null);
        }
    }
    private class CollectionLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor>{
        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            String mode = Utility.getPreferredMode(getActivity());

            String sortOrder = MovieContract.MovieEntry.COLUMN_ID + " ASC";
            if (mode.equals("popular")){
                sortOrder = COLUMN_POPULAR_RANK + " ASC";
            }else if (mode.equals("toprated")) {
                sortOrder = MovieContract.MovieEntry.COLUMN_TOPRATED_RANK + " ASC";
            }else{
                Logger.d("mode","error");
            }

            Uri movieForModeAndCollectUri = MovieContract.MovieEntry.buildMovieWithModeAndCollectUri(
                    mode);
            Logger.d("onCreateLoader","create movieForModeUri: " + movieForModeAndCollectUri.toString());

            CursorLoader cursorLoader = new CursorLoader(getActivity(),
                    movieForModeAndCollectUri,
                    MOVIE_COLUMNS,
                    null,
                    null,
                    sortOrder);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            mMovieAdapter.swapCursor(cursor);
            Logger.d("onLoadFinished","onLoadFinished" );
            if (mPosition != GridView.INVALID_POSITION) {
                // If we don't need to restart the loader, and there's a desired position to restore
                // to, do so now.
                mGridView.smoothScrollToPosition(mPosition);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            mMovieAdapter.swapCursor(null);
        }
    }
}

