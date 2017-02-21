package app.com.example.android.popularmovies;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by Mr.King on 2017/2/21 0021.
 */
public class UnScrollListView extends ListView {
    public UnScrollListView(Context context) {
        super(context);
    }

    public UnScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UnScrollListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
