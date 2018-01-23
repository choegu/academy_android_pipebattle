package com.choegu.indiegame.pipebattle;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by student on 2018-01-22.
 */

public class MusicService extends Service {
    private MediaPlayer playerMainTheme ,playerBattleFieldOfEternity;
    private MyBinder myBinder = new MyBinder();

    class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public void onCreate() {
        playerMainTheme = MediaPlayer.create(this, R.raw.hos_main_theme );
        playerBattleFieldOfEternity = MediaPlayer.create(this, R.raw.hos_battlefield_of_eternity);

        playerMainTheme.setLooping(true);
        playerBattleFieldOfEternity.setLooping(true);
    }

    public void startMainTheme() {
        playerMainTheme.start();
    }
    public void stopMainTheme() {
        playerMainTheme.stop();
    }

    public void startBattleFieldOfEternity() {
        playerBattleFieldOfEternity.start();
    }
    public void stopBattleFieldOfEternity() {
        playerBattleFieldOfEternity.stop();
    }

}
