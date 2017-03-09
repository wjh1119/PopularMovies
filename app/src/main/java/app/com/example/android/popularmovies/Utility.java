package app.com.example.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Mr.King on 2017/2/15 0015.
 */

public class Utility {


    public static Bitmap getImageFromUrl(String imageUrl){

        final String LOG_TAG = "getImageFromUrl";

        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(imageUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(6000); //超时设置
            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false); //设置不使用缓存
            InputStream inputStream=urlConnection.getInputStream();

            if (inputStream == null) {
                // Nothing to do.
                return null;
            }

            Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            Logger.v("getImageFromUrl","url is: " + imageUrl);
            return bitmap;
        } catch (IOException e) {
            Logger.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    public static byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        return os.toByteArray();
    }

    public static String getPreferredMode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_mode_key),
                context.getString(R.string.pref_mode_popular));
    }

    public static String getPreferredSyncInterval(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sync_interval_key), context.getString(R.string.pref_sync_interval_default));
    }
}
