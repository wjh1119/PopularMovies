package app.com.example.android.popularmovies.AsyncTask;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import app.com.example.android.popularmovies.R;

import static app.com.example.android.popularmovies.DetailFragment.COL_REVIEWS;

/**
 * Created by Mr.King on 2017/2/22 0022.
 */

public class FetchReviewsTask extends AsyncTask<Cursor, Void, ArrayList<HashMap>> {
    private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

    private final Context mContext;
    private Cursor mData;

    public FetchReviewsTask(Context context) {
        mContext = context;
    }

    //数据监听器
    FetchReviewsTask.OnDataFinishedListener onDataFinishedListener;

    public void setOnDataFinishedListener(
            FetchReviewsTask.OnDataFinishedListener onDataFinishedListener) {
        this.onDataFinishedListener = onDataFinishedListener;
    }

    @Override
    protected ArrayList<HashMap> doInBackground(Cursor... params) {

        ArrayList<HashMap> reviewsDataArray = new ArrayList<>();
        mData = params[0];
        try {

            String reviewsJsonStr = mData.getString(COL_REVIEWS);
            final String OWM_LIST = "results";

            if (reviewsJsonStr != null) {
                JSONObject rankJson = new JSONObject(reviewsJsonStr);
                JSONArray reviewsJsonArray = rankJson.getJSONArray(OWM_LIST);

                int numberOfReviews = reviewsJsonArray.length();
                //当无评论时显示无评论
                if (numberOfReviews == 0){
                    HashMap reviewHashMap = new HashMap();
                    String number = mContext.getString(R.string.movie_detail_reviews_none_text);
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
                        reviewHashMap.put("number",mContext.getString(R.string.movie_detail_reviews_itemtitle_text) + (i+1));
                        reviewHashMap.put("content",content);
                        reviewHashMap.put("author","------" + author);

                        reviewsDataArray.add(reviewHashMap);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reviewsDataArray;
    }

    @Override
    protected void onPostExecute(ArrayList<HashMap> hashMaps) {
        if(hashMaps!=null){
            onDataFinishedListener.onDataSuccessfully(hashMaps);
        }else{
            onDataFinishedListener.onDataFailed();
        }
    }

    public interface OnDataFinishedListener {

        void onDataSuccessfully(Object data);
        void onDataFailed();

    }
}
