package app.com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import app.com.example.android.popularmovies.sync.PopularMoviesSyncAdapter;

public class MainActivity extends ActionBarActivity
        implements MovieFragment.Callback, DetailFragment.Callback{

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
                showHintInDetailContainerOrToast("请点击左侧电影海报查看电影详细信息");
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
                showHintInDetailContainerOrToast("电影列表已刷新");
            }else{
                item.setTitle(getString(R.string.action_showCollection));//“我的收藏列表”
                mIsShowCollection = false;
                MovieFragment mf = (MovieFragment)getSupportFragmentManager().findFragmentById(R.id.main_container);
                if ( null != mf ) {
                    mf.onIsShowCollectionChanged(mIsShowCollection);
                }
                showHintInDetailContainerOrToast("电影列表已刷新");
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
            showHintInDetailContainerOrToast("电影列表更改排序方式");

            mMode = mode;
        }

    }

    private void showHintInDetailContainerOrToast(String hint){
        if (mTwoPane){
            Bundle args = new Bundle();
            args.putString(HintFragment.HINT, hint);

            HintFragment fragment = new HintFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, fragment, HINTFRAGMENT_TAG)
                    .commit();
        }else{
            Toast.makeText(this,hint,Toast.LENGTH_SHORT).show();
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
    public void onCancelCollection() {
        if(mTwoPane && mIsShowCollection){
            showHintInDetailContainerOrToast("该电影不在收藏列表，请在左侧重新选择");
        }
    }
}
