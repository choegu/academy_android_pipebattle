package com.choegu.indiegame.pipebattle;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

/**
 * Created by student on 2018-01-24.
 */

public class MyApp extends Application {
    private boolean isBackground;

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            isBackground = true;
            mService = currentActivity.getMusicService();
            mService.musicPause();
        }
    }

    private MusicService mService;
    private MusicActivity currentActivity;

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter screenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mService = currentActivity.getMusicService();
                if(mService!=null) {
                    mService.musicPause();
                }

            }
        }, screenOffFilter);


        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (activity instanceof MusicActivity) {
                    currentActivity = (MusicActivity) activity;
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
                if (isBackground) {
                    isBackground = false;
                    mService = currentActivity.getMusicService();
                    mService.musicPlay();
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {           }

            @Override
            public void onActivityStopped(Activity activity) {           }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {           }

            @Override
            public void onActivityDestroyed(Activity activity) {           }
        });
    }


}


