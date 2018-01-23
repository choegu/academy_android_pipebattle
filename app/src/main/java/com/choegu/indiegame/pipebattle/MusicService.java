package com.choegu.indiegame.pipebattle;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

/**
 * Created by student on 2018-01-22.
 */

public class MusicService extends Service {
    private MediaPlayer player;

    private IBinder mBinder = new MyBinder();

    class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        player = MediaPlayer.create(this, R.raw.hos_main_theme);
        player.setLooping(true); // Set looping
        player.setVolume(100, 100);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return super.onUnbind(intent);
    }

    public void musicPlay(){
        player.start();
    }

    public void musicPause(){
        player.pause();
    }

    public void musicStop(){
        player.stop();
        player = null;
    }

//    @Override
//    public void onDestroy() {
//        player.stop();
//        player.release();
//    }

    @Override
    public void onLowMemory() {

    }
}
