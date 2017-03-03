package app.com.example.android.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import app.com.example.android.popularmovies.sync.PopularMoviesSyncAdapter;

public class MainActivity extends ActionBarActivity implements MovieFragment.Callback{

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static final String HINTFRAGMENT_TAG = "HFTAG";

    private boolean mTwoPane;

    private String mMode;

    private boolean mIsShowCollection;

    private static MenuItem mShowCollectionItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mMode = Utility.getPreferredMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                FragmentManager manager = getSupportFragmentManager();
//                manager.beginTransaction()
//                        .replace(R.id.detail_container, new HintFragment(), HINTFRAGMENT_TAG)
//                        .add(R.id.detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
//                        .hide(manager.findFragmentByTag(DETAILFRAGMENT_TAG))
//                        .commit();
                manager.beginTransaction()
                        .replace(R.id.detail_container, new HintFragment(), HINTFRAGMENT_TAG)
                        .commit();

            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        PopularMoviesSyncAdapter.initializeSyncAdapter(this);
        Log.d(LOG_TAG,"initializeSyncAdapter");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);


        mShowCollectionItem = menu.findItem(R.id.action_showCollection);

        //判断该电影是否被收藏，并以此显示对应的菜单“收藏”或“取消收藏”。
        if (mIsShowCollection) {
            mShowCollectionItem.setTitle(getString(R.string.action_showCollection_showAllMovies));//“全部电影列表”
        }else{
            mShowCollectionItem.setTitle(getString(R.string.action_showCollection));//“我的收藏列表”
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_showCollection) {
            if(!mIsShowCollection){//
                item.setTitle(getString(R.string.action_showCollection_showAllMovies));//“全部电影列表”
                mIsShowCollection = true;
                MovieFragment mf = (MovieFragment)getSupportFragmentManager().findFragmentById(R.id.main_container);
                if ( null != mf ) {
                    mf.onIsShowCollectionChanged(mIsShowCollection);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, new HintFragment(), HINTFRAGMENT_TAG)
                        .commit();

            }else{
                item.setTitle(getString(R.string.action_showCollection));//“我的收藏列表”
                mIsShowCollection = false;
                MovieFragment mf = (MovieFragment)getSupportFragmentManager().findFragmentById(R.id.main_container);
                if ( null != mf ) {
                    mf.onIsShowCollectionChanged(mIsShowCollection);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, new HintFragment(), HINTFRAGMENT_TAG)
                        .commit();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String mode = Utility.getPreferredMode( this );
        Log.d(LOG_TAG,"onResume");
        // update the mode in our second pane using the fragment manager
        if (mode != null && !mode.equals(mMode)) {
            MovieFragment mf = (MovieFragment)getSupportFragmentManager().findFragmentById(R.id.main_container);
            if ( null != mf ) {
                mf.onModeChanged(mIsShowCollection);
            }
            getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, new HintFragment(), HINTFRAGMENT_TAG)
                        .commit();

            mMode = mode;
        }

    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            showMovieDetailInMainActivity(contentUri);
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
            Log.d("Click","mTwoPane is false ");
        }
    }

    @Override
    public void onListChanged(Cursor cursor) {
//        Log.d("onListChanged","onListChanged");
//        if(!mTwoPane){
//            Log.d("onListChanged","mTwoPane is false ");
//            return;
//        }
//        Log.d("onListChanged","R2: cursor is " + cursor);
//        if (cursor != null && mMode != null){
//            int rank = mMode.equals("popular")?cursor.getInt(COL_POPULAR_RANK)
//                    :cursor.getInt(COL_TOPRATED_RANK);
//
//            showMovieDetailInMainActivity(MovieContract.MovieEntry.buildMovieWithModeAndRankUri(
//                    mMode, rank));
//            Log.d("onListChanged","R3: mMode is " + mMode + " rank is : " + rank);
//        }else{
//            Log.d("onListChanged","error: cursor is " + cursor);
//        }
    }

    //当电影列表里没有item，给予用户提示。
    @Override
    public void onNoItemInList() {
        Log.d("onNoItemInList","onNoItemInList");
    }

    // In two-pane mode, show the detail view in this activity by
    // adding or replacing the detail fragment using a
    // fragment transaction.
    private void showMovieDetailInMainActivity(Uri contentUri){
        Bundle args = new Bundle();
        args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.detail_container, fragment, DETAILFRAGMENT_TAG)
                .commit();
        Log.d("showMovieDetail","cursor is " + contentUri);
    }

}
