package app.com.example.android.popularmovies.AsyncTask;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;

import app.com.example.android.popularmovies.data.MovieContract;

/**
 * Created by Mr.King on 2017/3/8 0008.
 */

public class UpdateCollectTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

    private final Context mContext;
    private Cursor mData;

    public UpdateCollectTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        String isCollect = params[0];
        String mMovieId = params[1];
        if (mMovieId != null && isCollect != null) {
            ContentValues values = new ContentValues();
            values.put(MovieContract.MovieEntry.COLUMN_COLLECT, isCollect);
            String selection = MovieContract.MovieEntry.TABLE_NAME+
                    "." + MovieContract.MovieEntry.COLUMN_ID + " = ? ";
            String[] selectionArgs = new String[]{mMovieId};
            mContext.getContentResolver().update(MovieContract.MovieEntry.CONTENT_URI,values,selection,selectionArgs);
        }else{
            Toast.makeText(mContext,"数据正在加载，请稍后再试", Toast.LENGTH_LONG).show();
        }
        return null;
    }
}
