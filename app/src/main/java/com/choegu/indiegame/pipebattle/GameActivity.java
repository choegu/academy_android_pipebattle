package com.choegu.indiegame.pipebattle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.choegu.indiegame.pipebattle.vo.TileVO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by student on 2018-01-12.
 */

public class GameActivity extends AppCompatActivity {
    // 네크워크 코드

    // 방 입장 task
    private final String CREATE = "create";
    private final String ENTER = "enter";

    // 네트워크 연결
    private int portNum;
    private ObjectInputStream sois;
    private ObjectOutputStream soos;
    private boolean gameNetwork = false;
    private String loginId, task;

    // Layout
    private GridView gridViewMain, gridViewEnemy, gridViewAttack, gridViewNext;
    private List<TileVO> tileVOListMain, tileVOListEnemy, tileVOListAttack, tileVOListNext;
    private MainGameAdapter mainGameAdapter;
    private EnemyGameAdapter enemyGameAdapter;
    private AttackGameAdapter attackGameAdapter;
    private NextGameAdapter nextGameAdapter;

    // 쓰레드
    private GameStartThread gameStartThread;
    private Handler handler;

    // 파이프게임 로직
    private Random random;
    private TileVO tileSample;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent receiveIntent = getIntent();
        portNum = receiveIntent.getIntExtra("portNum", 0);
        loginId = receiveIntent.getStringExtra("loginId");
        task = receiveIntent.getStringExtra("task");

        gridViewMain = findViewById(R.id.gridView_main_game);
        gridViewEnemy = findViewById(R.id.gridView_enemy_game);
        gridViewAttack = findViewById(R.id.gridView_attack_item);
        gridViewNext = findViewById(R.id.gridView_next_tile);

        random = new Random();

        // GridView 들 생성
        tileVOListMain = new ArrayList<>();
        for (int i=0; i<49; i++) {
            tileSample = new TileVO();
            tileSample.setType(-1);
            tileVOListMain.add(tileSample);
        }

        tileVOListEnemy = new ArrayList<>();
        for (int i=0; i<49; i++) {
            tileSample = new TileVO();
            tileSample.setType(-1);
            tileVOListEnemy.add(tileSample);
        }

        tileVOListAttack = new ArrayList<>();
        for (int i=0; i<4; i++) {
            tileSample = new TileVO();
            tileSample.setType(-1);
            tileVOListAttack.add(tileSample);
        }

        tileVOListNext = new ArrayList<>();
        for (int i=0; i<6; i++) {
            tileSample = new TileVO();
            tileSample.setType(random.nextInt(6)); // 0~5까지의 난수
            tileVOListNext.add(tileSample);
        }

        mainGameAdapter = new MainGameAdapter(this, R.layout.item_game_main, tileVOListMain);
        enemyGameAdapter = new EnemyGameAdapter(this, R.layout.item_game_enemy, tileVOListEnemy);
        attackGameAdapter = new AttackGameAdapter(this, R.layout.item_game_attack, tileVOListAttack);
        nextGameAdapter = new NextGameAdapter(this, R.layout.item_game_next, tileVOListNext);

        gridViewMain.setAdapter(mainGameAdapter);
        gridViewEnemy.setAdapter(enemyGameAdapter);
        gridViewAttack.setAdapter(attackGameAdapter);
        gridViewNext.setAdapter(nextGameAdapter);

        gridViewMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i!=0 && i!=48) {
                    tileVOListMain.get(i).setType(tileVOListNext.get(0).getType());
                    tileVOListNext.remove(0);
                    tileSample = new TileVO();
                    tileSample.setType(random.nextInt(6));
                    tileVOListNext.add(tileSample);
                    refreshGameAdapter();
                } else {
                    tileVOListMain.get(i).setType(9);
                    mainGameAdapter.notifyDataSetChanged();
                }
            }
        });

        gridViewAttack.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {

            }
        };

        gameStartThread = new GameStartThread();
        gameStartThread.start();
    }

    // 방 생성 직후 네트워크 연결 및 코드 받는 쓰레드
    class GameStartThread extends Thread {
        @Override
        public void run() {
//            try {
//                if (!gameNetwork) {
//                    gameNetwork = initNetwork();
//                }
//
//                ReadyCodeVO codeVO = new ReadyCodeVO();
//                codeVO.setCode(REFRESH_READY);
//
//                if (task.equals(CREATE)) {
//                    codeVO.setPlayer1(loginId);
//                } else if (task.equals(ENTER)) {
//                    codeVO.setPlayer2(loginId);
//                }
//
//                soos.writeObject(codeVO);
//
//                while (gameNetwork) {
//                    ReadyCodeVO code = (ReadyCodeVO) sois.readObject();
//                    Log.d("chs", "thread while : "+code.toString());

//                    if (code.getCode().equals(CHAT_READY)) {
//                        Message msg = new Message();
//                        msg.what = 132;
//                        msg.obj = code;
//                        handler.sendMessage(msg);
//                    } else if (code.getCode().equals(REFRESH_READY)) {
//                        Message msg = new Message();
//                        msg.what = 138;
//                        msg.obj = code;
//                        handler.sendMessage(msg);
//                    } else if (code.getCode().equals(START_READY)) {
//                        Message msg = new Message();
//                        msg.what = 143;
//                        handler.sendMessage(msg);
//                    } else if (code.getCode().equals(GAME_START_READY)) {
//                        Message msg = new Message();
//                        msg.what = 141;
//                        handler.sendMessage(msg);
//                    }
//
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
        }
    }

    // Game Room 서버연결
    private boolean initNetwork() {

        try {
            Socket socket = new Socket(InetAddress.getByName("70.12.115.57"), portNum);
            soos = new ObjectOutputStream(socket.getOutputStream());
            sois = new ObjectInputStream(socket.getInputStream());
            gameNetwork = true;

            Log.d("chs", "client socket connected");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "서버 문제 발생", Toast.LENGTH_SHORT).show();
        }

        return gameNetwork;
    }

    // 모든 adapter 새로고침
    private void refreshGameAdapter(){
        attackGameAdapter.notifyDataSetChanged();
        enemyGameAdapter.notifyDataSetChanged();
        mainGameAdapter.notifyDataSetChanged();
        nextGameAdapter.notifyDataSetChanged();
    }
}
