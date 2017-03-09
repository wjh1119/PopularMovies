package app.com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mr.King on 2017/2/1 0001.
 */

public class VideoAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<HashMap> mData;

    public VideoAdapter(Context c, ArrayList<HashMap> videosData) {
        super();
        this.mContext = c;
        this.mData = videosData;
    }

    public ArrayList<HashMap> getData(){
        return mData;
    }

    public void clear(){
        mData = null;
    }

    //获取评论数量
    public int getCount() {
        return mData.size();

    }

    //获取图片位置
    public Object getItem(int position) {
        return mData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater mInflater = LayoutInflater.from(mContext);
            convertView = mInflater.inflate(R.layout.list_item_videos, null);

            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.list_item_videos_name);
            viewHolder.numberTextView = (TextView) convertView.findViewById(R.id.list_item_videos_number);
            viewHolder.playImageView = (ImageView) convertView.findViewById(R.id.list_item_videos_play);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final HashMap video = mData.get(position);

        viewHolder.numberTextView.setText(video.get("number").toString());
        viewHolder.nameTextView.setText(video.get("name").toString());
        viewHolder.playImageView.setImageResource(R.drawable.playvideo);
        viewHolder.playImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (video.get("key") != null && !video.get("key").equals("")) {

                    Uri youtubeUri = Uri.parse("http://www.youtube.com/watch?")
                            .buildUpon().appendQueryParameter("v",video.get("key").toString())
                            .build();
                    Logger.d("video",youtubeUri.toString());
                    Intent intent = new Intent(Intent.ACTION_VIEW, youtubeUri);
                    mContext.startActivity(intent);
                }
            }});
            return convertView;
    }

    private static class ViewHolder {
        TextView nameTextView;
        TextView numberTextView;
        ImageView playImageView;
    }
}
