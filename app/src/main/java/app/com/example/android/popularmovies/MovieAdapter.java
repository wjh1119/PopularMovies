package app.com.example.android.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.widget.CursorAdapter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * {@link MovieAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link android.widget.GridView}.
 */
public class MovieAdapter extends CursorAdapter {

    String LOG_TAG = MovieAdapter.class.getSimpleName();
    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }
    int mDefItem;

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToPosterPath(Cursor cursor) {
        String posterPath = cursor.getString(MovieFragment.COL_POSTER_PATH);
        return posterPath;
    }

    private BitmapDrawable convertCursorRowToPosterImage(Cursor cursor) {
        byte[] imageBlob = cursor.getBlob(MovieFragment.COL_POSTER_IMAGE);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);
        Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, 120, 180, true);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(resizeBitmap);
        return bitmapDrawable;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_movies, parent, false);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);

        ImageView imageView = (ImageView) view;

        imageView.setImageDrawable(convertCursorRowToPosterImage(cursor));
    }
}