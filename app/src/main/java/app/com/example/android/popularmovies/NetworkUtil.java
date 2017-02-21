package app.com.example.android.popularmovies;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {

    private NetworkUtil() {
    }

    //判断网络是否可用
    public static boolean getConnectivityStatus(final Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        //合理的运用运算符能节省一些代码
        //这条语句的意思是，只有当info不为null，并且网络可用的情况下才返回true，其余情况返回false
        return info != null && info.isConnected();
    }
}