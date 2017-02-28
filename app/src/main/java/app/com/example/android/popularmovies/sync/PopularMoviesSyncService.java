package app.com.example.android.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class PopularMoviesSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static PopularMoviesSyncAdapter sSunshineSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("PopularMoviesSyncService", "onCreate - PopularMoviesSyncService");
        synchronized (sSyncAdapterLock) {
            if (sSunshineSyncAdapter == null) {
                sSunshineSyncAdapter = new PopularMoviesSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSunshineSyncAdapter.getSyncAdapterBinder();
    }
}