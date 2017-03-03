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

import android.app.ProgressDialog;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import app.com.example.android.popularmovies.data.MovieContract;
import app.com.example.android.popularmovies.sync.PopularMoviesSyncAdapter;

import static app.com.example.android.popularmovies.data.MovieContract.MovieEntry.COLUMN_POPULAR_RANK;

/**
 * Encapsulates fetching the forecast and displaying it as a {@link GridView} layout.
 */
public class MovieFragment extends Fragment {

    String LOG_TAG = MovieFragment.class.getSimpleName();
    public static ProgressDialog progressDialog;
    private static final int MOVIE_LOADER = 0;
    private static final int COLLECTION_LOADER = 1;

    private GridView mGridView;
    private int mPosition = GridView.INVALID_POSITION;

    private static final String SELECTED_KEY = "selected_position";

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
    };

    // These indices are tied to MOVIE_COLUMNS.  If MOVIE_COLUMNS changes, these
    // must change.
    static final int _ID = 0;
    static final int COL_POSTER_PATH = 1;
    static final int COL_POSTER_IMAGE = 2;
    static final int COL_TITLE= 3;
    static final int COL_POPULAR_RANK = 4;
    static final int COL_TOPRATED_RANK = 5;

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
    public void onStop() {
        //避免一些异常情况导致progressDialog没有dismiss
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        super.onStop();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.rankfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovie();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The CursorAdapter will take data from our cursor and populate the ListView.
        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mGridView = (GridView) rootView.findViewById(R.id.grid_fragment);
        mGridView.setAdapter(mMovieAdapter);

        // We'll call our MainActivity
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String mode = Utility.getPreferredMode(getActivity());
                    Log.v("intent","cursor" +" popular rank: " + cursor.getInt(COL_POPULAR_RANK) +
                            ", toprated rank: "+ cursor.getInt(COL_TOPRATED_RANK));
                    if (mode.equals("popular")){
                        ((Callback) getActivity())
                                .onItemSelected(MovieContract.MovieEntry.buildMovieWithModeAndRankUri(
                                        mode, cursor.getInt(COL_POPULAR_RANK)
                                ));
                        Log.v("intent","sent intent with mode: "+ mode +" and rank: "
                                + cursor.getInt(COL_POPULAR_RANK));
                    }else if (mode.equals("toprated")){
                        ((Callback) getActivity())
                                .onItemSelected(MovieContract.MovieEntry.buildMovieWithModeAndRankUri(
                                        mode, cursor.getInt(COL_TOPRATED_RANK)
                                ));
                        Log.v("intent","sent intent with mode: "+ mode +" and rank: "
                                + cursor.getInt(COL_TOPRATED_RANK));
                    }else{
                        Log.e("intent","mode is wrong");
                    }
                }
            }
        });


        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The gridview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        //当加载电影列表，通过MainActivity通知detailfragemnt显示第一项内容。
        mGridView.post(new Runnable() {
            @Override
            public void run() {
                if (mGridView.getCount() > 0){
                    ((Callback) getActivity())
                            .onListChanged((Cursor) mGridView.getItemAtPosition(0));
                    Log.d("onListChanged","R: cursor is " + mGridView.getItemAtPosition(0));
                }
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, new MoviesLoaderCallbacks());
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mMovieAdapter.getCount() == 0){
            Toast.makeText(getActivity(), "请点击菜单中的刷新获取电影排名", Toast.LENGTH_SHORT).show();
        }else{
        }
    }

    private void updateMovie() {
        PopularMoviesSyncAdapter.syncImmediately(getActivity());
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
        mGridView.post(new Runnable() {
            @Override
            public void run() {
                if (mGridView.getCount() > 0){
                    ((Callback) getActivity())
                            .onListChanged((Cursor) mGridView.getItemAtPosition(0));
                    Log.d("onListChanged","collectchanged: cursor is " + mGridView.getItemAtPosition(0));
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    private class MoviesLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor>{
        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            String mode = Utility.getPreferredMode(getActivity());
            Log.d(LOG_TAG,"onCreateLoader mode is " + mode);

            String sortOrder = MovieContract.MovieEntry.COLUMN_ID + " ASC";
            if (mode.equals("popular")){
                sortOrder = COLUMN_POPULAR_RANK + " ASC";
            }else if (mode.equals("toprated")) {
                sortOrder = MovieContract.MovieEntry.COLUMN_TOPRATED_RANK + " ASC";
            }else{
                Log.e("mode","error");
            }

            Uri movieForModeUri = MovieContract.MovieEntry.buildMovieWithModeUri(
                    mode);
            Log.v("onCreateLoader","create movieForModeUri: " + movieForModeUri.toString());

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
            Log.v("onLoadFinished", cursor.toString());
            if (!cursor.moveToFirst()){
                ((Callback) getActivity()).onNoItemInList();
            }else{
                ((Callback) getActivity()).onListChanged(cursor);
            }
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
                Log.e("mode","error");
            }

            Uri movieForModeAndCollectUri = MovieContract.MovieEntry.buildMovieWithModeAndCollectUri(
                    mode);
            Log.v("onCreateLoader","create movieForModeUri: " + movieForModeAndCollectUri.toString());

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
            Log.d("onLoadFinished","onLoadFinished" );
            if (!cursor.moveToFirst()){
                ((Callback) getActivity()).onNoItemInList();
            }else{
                ((Callback) getActivity()).onListChanged(cursor);
            }
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

    public interface Callback {
        void onItemSelected(Uri contentUri);
        void onListChanged(Cursor cursor);
        void onNoItemInList();
    }
}

