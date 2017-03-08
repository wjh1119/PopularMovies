package app.com.example.android.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Mr.King on 2017/3/3 0003.
 */

public class HintFragment extends Fragment {

    private String mHint;

    static final String HINT = "hint";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //获取Uri
        Bundle arguments = getArguments();
        if (arguments != null) {
            mHint = arguments.getString(HintFragment.HINT);
        }
        View rootView = inflater.inflate(R.layout.fragment_hint, container, false);
        TextView hintText = (TextView) rootView.findViewById(R.id.hint_textview);
        hintText.setText(mHint);
        return rootView;
    }
}
