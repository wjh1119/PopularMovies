package app.com.example.android.popularmovies.AsyncTask;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import app.com.example.android.popularmovies.DetailFragment;
import app.com.example.android.popularmovies.R;

/**
 * Created by Mr.King on 2017/2/22 0022.
 */

public class FetchVideosTask extends AsyncTask<Cursor, Void, ArrayList<HashMap>> {
    private final String LOG_TAG = FetchVideosTask.class.getSimpleName();

    private final Context mContext;
    private Cursor mData;

    public FetchVideosTask(Context context) {
        mContext = context;
    }

    //数据监听器
    OnDataFinishedListener onDataFinishedListener;

    public void setOnDataFinishedListener(
            OnDataFinishedListener onDataFinishedListener) {
        this.onDataFinishedListener = onDataFinishedListener;
    }

    @Override
    protected ArrayList<HashMap> doInBackground(Cursor... params) {

        mData = params[0];

        ArrayList<HashMap> videosDataArray = new ArrayList<>();

        try {

            String videosJsonStr = mData.getString(DetailFragment.COL_VIDEOS);
            final String OWM_LIST = "results";

            if (videosJsonStr != null) {
                JSONObject videosJson = new JSONObject(videosJsonStr);
                JSONArray videosJsonArray = videosJson.getJSONArray(OWM_LIST);

                int numberOfvideos = videosJsonArray.length();

                //无预告片时显示无预告
                if (numberOfvideos == 0){
                    HashMap videoHashMap = new HashMap();
                    String number = mContext.getString(R.string.movie_detail_videos_none_text);
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

                    number = mContext.getString(R.string.movie_detail_videos_itemtitle_text) + (i+1);
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videosDataArray;
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
