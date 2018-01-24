package com.choegu.indiegame.pipebattle;

import android.app.Application;

/**
 * Created by student on 2018-01-24.
 */

public class MyApp extends Application {
    public boolean isBackground;

//    @Override
//    public void onTrimMemory(int level) {
//        super.onTrimMemory(level);
//        if (level == TRIM_MEMORY_UI_HIDDEN) {
//            isBackground = true;
//            notifyBackground();
//        }
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate(); // ...
//        // IntentFilter screenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF); registerReceiver(new BroadcastReceiver() { @Override public void onReceive(Context context, Intent intent) { if (isBackground) { isBackground = false; notifyForeground(); } } }, screenOffFilter); } }
//
//
//    }

}
