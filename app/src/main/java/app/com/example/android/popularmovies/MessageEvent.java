package app.com.example.android.popularmovies;

/**
 * Created by Mr.King on 2017/5/17 0017.
 */

public class MessageEvent {

    public String msg;
    public Object object;
    public MessageEvent(String msg) {
        this.msg = msg;
        this.object = null;
    }
    public MessageEvent(String msg, Object object) {
        this.msg = msg;
        this.object = object;
    }
}
