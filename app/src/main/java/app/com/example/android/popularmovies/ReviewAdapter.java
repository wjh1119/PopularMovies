package app.com.example.android.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mr.King on 2017/2/1 0001.
 */

public class ReviewAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<HashMap> mData;

    public ReviewAdapter(Context c, ArrayList<HashMap> reviewsData) {
        super();
        this.mContext = c;
        this.mData = reviewsData;
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
            convertView = mInflater.inflate(R.layout.list_item_reviews, null);

            viewHolder.numberTextView = (TextView) convertView.findViewById(R.id.list_item_reviews_number);
            viewHolder.contentTextView = (TextView) convertView.findViewById(R.id.list_item_reviews_content);
            viewHolder.authorTextView = (TextView) convertView.findViewById(R.id.list_item_reviews_author);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        HashMap review = mData.get(position);

        //viewHolder.numberTextView.setText("评论" + (position+1));
        viewHolder.numberTextView.setText(review.get("number").toString());
        viewHolder.contentTextView.setText(review.get("content").toString());
        viewHolder.authorTextView.setText(review.get("author").toString());

        return convertView;
    }

    private static class ViewHolder {
        TextView numberTextView;
        TextView contentTextView;
        TextView authorTextView;
    }
}
