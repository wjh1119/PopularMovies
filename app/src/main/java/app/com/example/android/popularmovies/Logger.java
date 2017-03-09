package app.com.example.android.popularmovies;

import android.util.Log;

/**
 * Created by Mr.King on 2017/3/9 0009.
 */

public final class Logger {

    private Logger() {

    }

    //控制是否显示LOG，你可以在Logger里面配置，也可以像API_KEY那样，在gradle里面配置
    //建议在gradle里面配置，方便管理等。这里我为了能完整演示而使用了一个常量来控制
    private static final boolean showLog = BuildConfig.SHOW_LOG;

    public static void v(final String tag, final String msg) {
        if (showLog) Log.v(tag, msg);
    }

    public static void d(final String tag, final String msg) {
        if (showLog) Log.d(tag, msg);
    }

    public static void i(final String tag, final String msg) {
        if (showLog) Log.i(tag, msg);
    }

    public static void w(final String tag, final String msg) {
        if (showLog) Log.w(tag, msg);
    }

    public static void e(final String tag, final String msg, final Throwable tr) {
        if (showLog) Log.e(tag, msg, tr);
    }
}
