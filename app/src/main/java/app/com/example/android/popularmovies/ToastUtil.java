package app.com.example.android.popularmovies;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Mr.King on 2017/3/9 0009.
 */

public final class ToastUtil {

    private ToastUtil() {

    }

    private static Toast mToast = null;

    public static void show(final Context context, final String msg) {
        toast(context, msg);
    }

    public static void show(final Context context, final int resId) {
        toast(context, context.getString(resId));
    }

    private static void toast(final Context context, final String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(msg);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }
}
