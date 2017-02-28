package app.com.example.android.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import app.com.example.android.popularmovies.data.MovieContract;

import static app.com.example.android.popularmovies.MovieFragment.COL_POPULAR_RANK;
import static app.com.example.android.popularmovies.MovieFragment.COL_TOPRATED_RANK;

public class MainActivity extends ActionBarActivity implements MovieFragment.Callback{

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    // final String MOVIEFRAGMENT_TAG = "MFTAG";
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

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
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
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
            }else{
                item.setTitle(getString(R.string.action_showCollection));//“我的收藏列表”
                mIsShowCollection = false;
                MovieFragment mf = (MovieFragment)getSupportFragmentManager().findFragmentById(R.id.main_container);
                if ( null != mf ) {
                    mf.onIsShowCollectionChanged(mIsShowCollection);

                }
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
            DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.onModeChanged(mode,1);
            }
            mMode = mode;
        }

    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
            Log.d("Click","mTwoPane is true ");
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
            Log.d("Click","mTwoPane is false ");
        }
    }

    @Override
    public void onShowDefalutItem(Cursor cursor) {
        Log.d("onShowDefalutItem","R2: cursor is " + cursor);
        if (cursor != null){
            if (mMode.equals("popular")){
                this.onItemSelected(MovieContract.MovieEntry.buildMovieWithModeAndRankUri(
                        mMode, cursor.getInt(COL_POPULAR_RANK)));
                Log.d("onShowDefalutItem","R3: mMode is " + mMode + " rank is : " + cursor.getInt(COL_POPULAR_RANK));
            }else if (mMode.equals("toprated")){
                this.onItemSelected(MovieContract.MovieEntry.buildMovieWithModeAndRankUri(
                        mMode, cursor.getInt(COL_TOPRATED_RANK)));
                Log.d("onShowDefalutItem","R3: mMode is " + mMode + " rank is : " + cursor.getInt(COL_TOPRATED_RANK));
            }else{
                Log.e("onShowDefalutItem","mode is wrong");
            }
        }else{
            Log.d("onShowDefalutItem","error: cursor is " + cursor);
        }

    }
}
