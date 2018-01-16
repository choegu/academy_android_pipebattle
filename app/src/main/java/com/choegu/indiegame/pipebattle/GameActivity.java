package com.choegu.indiegame.pipebattle;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.choegu.indiegame.pipebattle.vo.GameCodeVO;
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
    // 네크워크 코드 : 승패 등 추가해야함
    private final String LOADING_COMPLETE_GAME = "loadingCompleteGame";
    private final String CLICK_MAIN_GAME = "clickMainGame";
    private final String CLICK_ATTACK_GAME = "clickAttackGame";

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
    private EnemyGameAdapter enemyGameAdapter, attackDialogAdapter;
    private AttackGameAdapter attackGameAdapter;
    private NextGameAdapter nextGameAdapter;

    // 쓰레드
    private GameStartThread gameStartThread;
    private ClickMainGameThread clickMainGameThread;
    private Handler handler;

    // 파이프게임 로직
    private Random random;
    private TileVO tileSample;
    private int clickNewCountMain = 0;
    private final int EXPLOSION = 8;
    private final int MISSILE = 11;
    private final int BLANK_TILE = -1;

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
            tileSample.setType(BLANK_TILE);
            tileVOListMain.add(tileSample);
        }

        tileVOListEnemy = new ArrayList<>();
        for (int i=0; i<49; i++) {
            tileSample = new TileVO();
            tileSample.setType(BLANK_TILE);
            tileVOListEnemy.add(tileSample);
        }

        tileVOListAttack = new ArrayList<>();
        for (int i=0; i<4; i++) {
            tileSample = new TileVO();
            tileSample.setType(BLANK_TILE);
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
                if (i!=0 && i!=48 && tileVOListMain.get(i).getType()!=EXPLOSION) {
                    int tileTypeNow = tileVOListNext.get(0).getType();

                    // 새로운 곳 클릭할때마다 클릭뉴카운트 1 증가
                    if(tileVOListMain.get(i).getType() == BLANK_TILE) {
                        clickNewCountMain++;
                    }

                    // 메인 게임 판 적용
                    tileVOListMain.get(i).setType(tileTypeNow);
                    tileVOListNext.remove(0);
                    tileSample = new TileVO();
                    tileSample.setType(random.nextInt(6));
                    tileVOListNext.add(tileSample);
                    mainGameAdapter.notifyDataSetChanged();
                    nextGameAdapter.notifyDataSetChanged();

                    // 상대 화면 적용
                    clickMainGameThread = new ClickMainGameThread(tileTypeNow, i);
                    clickMainGameThread.start();

                    // 클릭뉴카운트 5 될때마다 공격아이템 생성
                    if ((clickNewCountMain%5) == 0) {
                        tileVOListAttack.remove(3);
                        tileSample = new TileVO();
                        tileSample.setType(MISSILE);
                        tileVOListAttack.add(0, tileSample);
                        attackGameAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        gridViewAttack.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (tileVOListAttack.get(i).getType() == MISSILE) {
                    makeAttackDialog(i).show();
                }
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                int handlerMsg = msg.what;
                GameCodeVO receiveMsg;

                switch (handlerMsg) {
                    case 151: // 게임 로딩 서로 완료 : 나중에 카운트로 수정
                        Toast.makeText(GameActivity.this, "서로 로딩완료 되었습니다", Toast.LENGTH_SHORT).show();
                        break;
                    case 161: // Player 메인 게임 클릭
                        receiveMsg = (GameCodeVO) msg.obj;
                        tileVOListEnemy.get(receiveMsg.getTileNum()).setType(receiveMsg.getTileType());
                        enemyGameAdapter.notifyDataSetChanged();
                        break;
                    case 162: // 공격 받음
                        receiveMsg = (GameCodeVO) msg.obj;
                        tileVOListMain.get(receiveMsg.getTileNum()).setType(EXPLOSION);
                        mainGameAdapter.notifyDataSetChanged();
                        break;
                }
            }
        };

        gameStartThread = new GameStartThread();
        gameStartThread.start();
    }

    // 방 생성 직후 네트워크 연결 및 코드 받는 쓰레드
    class GameStartThread extends Thread {
        @Override
        public void run() {
            try {
                if (!gameNetwork) {
                    gameNetwork = initNetwork();
                }

                GameCodeVO codeVO = new GameCodeVO();
                codeVO.setCode(LOADING_COMPLETE_GAME);

                if (task.equals(CREATE)) {
                    codeVO.setPlayer1(loginId);
                } else if (task.equals(ENTER)) {
                    codeVO.setPlayer2(loginId);
                }

                soos.writeObject(codeVO);

                while (gameNetwork) {
                    GameCodeVO code = (GameCodeVO) sois.readObject();

                    if (code.getCode().equals(LOADING_COMPLETE_GAME)) {
                        Message msg = new Message();
                        msg.what = 151;
                        handler.sendMessage(msg);
                    } else if (code.getCode().equals(CLICK_MAIN_GAME)) {
                        if (code.getPlayer1()!=null && !code.getPlayer1().isEmpty() && task.equals(ENTER)) {
                            Message msg = new Message();
                            msg.what = 161;
                            msg.obj = code;
                            handler.sendMessage(msg);
                        } else if (code.getPlayer2()!=null && !code.getPlayer2().isEmpty() && task.equals(CREATE)) {
                            Message msg = new Message();
                            msg.what = 161;
                            msg.obj = code;
                            handler.sendMessage(msg);
                        }
                    } else if (code.getCode().equals(CLICK_ATTACK_GAME)) {
                        if (code.getPlayer1()!=null && !code.getPlayer1().isEmpty() && task.equals(ENTER)) {
                            Message msg = new Message();
                            msg.what = 162;
                            msg.obj = code;
                            handler.sendMessage(msg);
                        } else if (code.getPlayer2()!=null && !code.getPlayer2().isEmpty() && task.equals(CREATE)) {
                            Message msg = new Message();
                            msg.what = 162;
                            msg.obj = code;
                            handler.sendMessage(msg);
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // 메인게임 클릭 쓰레드
    class ClickMainGameThread extends Thread {
        int tileType, tileNum;
        GameCodeVO codeVO;

        public ClickMainGameThread(int tileType, int tileNum) {
            this.tileType = tileType;
            this.tileNum = tileNum;
        }

        @Override
        public void run() {
            codeVO = new GameCodeVO();

            codeVO.setCode(CLICK_MAIN_GAME);
            if (task.equals(CREATE)) {
                codeVO.setPlayer1(loginId);
            } else if (task.equals(ENTER)) {
                codeVO.setPlayer2(loginId);
            }
            codeVO.setTileType(tileType);
            codeVO.setTileNum(tileNum);

            try {
                soos.writeObject(codeVO);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 공격 클릭 쓰레드
    class ClickAttackEnemyThread extends Thread {
        int tileNum;
        GameCodeVO codeVO;

        public ClickAttackEnemyThread(int tileNum) {
            this.tileNum = tileNum;
        }

        @Override
        public void run() {
            codeVO = new GameCodeVO();

            codeVO.setCode(CLICK_ATTACK_GAME);
            if (task.equals(CREATE)) {
                codeVO.setPlayer1(loginId);
            } else if (task.equals(ENTER)) {
                codeVO.setPlayer2(loginId);
            }
            codeVO.setTileNum(tileNum);

            try {
                soos.writeObject(codeVO);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    // 공격 다이얼로그
    private Dialog makeAttackDialog(final int attackNum) {
        final Dialog attackDialog = new Dialog(this);
        attackDialog.setContentView(R.layout.dialog_attack);

        GridView gridViewAttackDialog = attackDialog.findViewById(R.id.gridView_attack_dialog);
        attackDialogAdapter = new EnemyGameAdapter(this, R.layout.item_dialog_enemy, tileVOListEnemy);
        gridViewAttackDialog.setAdapter(attackDialogAdapter);

        gridViewAttackDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClickAttackEnemyThread clickAttackEnemyThread = new ClickAttackEnemyThread(i);
                clickAttackEnemyThread.start();
                tileVOListEnemy.get(i).setType(EXPLOSION);
                enemyGameAdapter.notifyDataSetChanged();
                tileVOListAttack.remove(attackNum);
                tileSample = new TileVO();
                tileSample.setType(BLANK_TILE);
                tileVOListAttack.add(tileSample);
                attackGameAdapter.notifyDataSetChanged();
                attackDialog.cancel();
            }
        });

        return attackDialog;
    }

}
