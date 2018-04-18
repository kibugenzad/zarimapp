package rw.limitless.limitlessapps.ussd;

import android.app.Application;
import android.os.SystemClock;

/**
 * Created by limitlessapps on 30/12/2017.
 */

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SystemClock.sleep(3000);
    }
}
