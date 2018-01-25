package com.choegu.indiegame.pipebattle;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by student on 2018-01-25.
 */

public class MusicActivity extends AppCompatActivity {
    private MusicService mService;
    private boolean isBind= false;
    ServiceConnection sconn = new ServiceConnection() {
        @Override //서비스가 실행될 때 호출
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
            mService = myBinder.getService();
            isBind = true;

            mService.musicPlay();
        }

        @Override //서비스가 종료될 때 호출
        public void onServiceDisconnected(ComponentName name) {
            isBind = false;
            mService = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!isBind) {
            bindService(new Intent(this, MusicService.class), sconn, Context.BIND_AUTO_CREATE);
        }
    }

    public MusicService getMusicService(){
        return mService;
    }

    @Override
    protected void onStart() {
        if(isBind) {
            mService.musicPlay();
        }
        super.onStart();
    }
    boolean foreground;
    boolean running;

    @Override
    public void onPause()
    {
        super.onPause();

        foreground = MusicHelper.isAppInForeground(this);
        if(!foreground && isBind){
            mService.musicPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        running = MusicHelper.isAppRunning(this, "com.choegu.indiegame.pipebattle");
        foreground = MusicHelper.isAppInForeground(this);
        if(!foreground && isBind){
            mService.musicPause();
        }
        if(!running)
        {
            unbindService(sconn);
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(sconn);
        super.onDestroy();
    }


    public void stopMusic(){
        mService.musicPause();
    }
}
