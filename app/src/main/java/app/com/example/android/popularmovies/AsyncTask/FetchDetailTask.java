package app.com.example.android.popularmovies.AsyncTask;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;

import java.util.HashMap;

import app.com.example.android.popularmovies.DetailFragment;

import static app.com.example.android.popularmovies.DetailFragment.COL_OVERVIEW;
import static app.com.example.android.popularmovies.DetailFragment.COL_RELEASE_DATE;
import static app.com.example.android.popularmovies.DetailFragment.COL_RUNTIME;
import static app.com.example.android.popularmovies.DetailFragment.COL_TITLE;
import static app.com.example.android.popularmovies.DetailFragment.COL_VOTE_AVERAGE;

/**
 * Created by Mr.King on 2017/2/22 0022.
 */

public class FetchDetailTask extends AsyncTask<Cursor, Void, HashMap> {
    private final String LOG_TAG = FetchDetailTask.class.getSimpleName();

    private Cursor mData;

    public FetchDetailTask() {

    }

    //数据监听器
    FetchDetailTask.OnDataFinishedListener onDataFinishedListener;

    public void setOnDataFinishedListener(
            FetchDetailTask.OnDataFinishedListener onDataFinishedListener) {
        this.onDataFinishedListener = onDataFinishedListener;
    }

    @Override
    protected HashMap doInBackground(Cursor... params) {

        HashMap detailHashMap = new HashMap();
        mData = params[0];
        try {

            //载入电影名字
            String name = mData.getString(COL_TITLE);

            //载入电影图片
            byte[] imageBlob = mData.getBlob(DetailFragment.COL_POSTER_IMAGE);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);
            BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);

            String movieId = mData.getString(DetailFragment.COL_ID);
            String isCollect = mData.getString(DetailFragment.COL_COLLECT);

            //载入电影上映日期
            String date = mData.getString(COL_RELEASE_DATE);

            //载入电影评分
            String voteAverage = mData.getString(COL_VOTE_AVERAGE);

            //movie's runtime
            int runtime = mData.getInt(COL_RUNTIME);

            //载入电影简介，并实现展开收起的功能
            String overview = mData.getString(COL_OVERVIEW);

            detailHashMap.put("name",name);
            detailHashMap.put("bitmapDrawable",bitmapDrawable);
            detailHashMap.put("movieId",movieId);
            detailHashMap.put("isCollect",isCollect);
            detailHashMap.put("date",date);
            detailHashMap.put("voteAverage",voteAverage);
            detailHashMap.put("runtime",runtime);
            detailHashMap.put("overview",overview);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return detailHashMap;
    }

    @Override
    protected void onPostExecute(HashMap hashMap) {
        if(hashMap!=null){
            onDataFinishedListener.onDataSuccessfully(hashMap);
        }else{
            onDataFinishedListener.onDataFailed();
        }
    }

    public interface OnDataFinishedListener {

        void onDataSuccessfully(Object mData);
        void onDataFailed();

    }
}

