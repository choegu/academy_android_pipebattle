package com.choegu.indiegame.pipebattle;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.choegu.indiegame.pipebattle.vo.FinishCheckVO;
import com.choegu.indiegame.pipebattle.vo.GameCodeVO;
import com.choegu.indiegame.pipebattle.vo.MemberCodeVO;
import com.choegu.indiegame.pipebattle.vo.OptionValue;
import com.choegu.indiegame.pipebattle.vo.TileVO;

import java.io.IOException;
import java.io.InterruptedIOException;
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
    private final String LOADING_COMPLETE_GAME = "loadingCompleteGame";
    private final String CLICK_MAIN_GAME = "clickMainGame";
    private final String CLICK_ATTACK_GAME = "clickAttackGame";
    private final String OUT_PLAYER1_GAME = "outPlayer1Game";
    private final String OUT_PLAYER2_GAME = "outPlayer2Game";
    private final String GAME_FINISH_RESULT = "gameFinishResult";
    private final String GAME_ALREADY_END = "gameAlreadyEnd";
    private final String MEMBER_UPDATE_RATING = "memberUpdateRating";

    // 방 입장 task
    private final String CREATE = "create";
    private final String ENTER = "enter";
    private final String RANK = "rank";
    private String player1, player2;
    private int ratingP1, ratingP2;

    // 네트워크 연결
    private int portNum;
    private Socket socket;
    private ObjectInputStream sois;
    private ObjectOutputStream soos;
    private boolean gameNetwork = false;
    private String loginId, task, mode;

    // Layout
    private ConstraintLayout rootActivity;
    private GridView gridViewMain, gridViewEnemy, gridViewAttack, gridViewNext;
    private List<TileVO> tileVOListMain, tileVOListEnemy, tileVOListAttack, tileVOListNext;
    private MainGameAdapter mainGameAdapter;
    private EnemyGameAdapter enemyGameAdapter, attackDialogAdapter;
    private AttackGameAdapter attackGameAdapter;
    private NextGameAdapter nextGameAdapter;
    private Dialog currentDialog, loadingDialog;
    private TextView textLoading, textGamePlayer1, textGamePlayer2, textGameVs;

    // 쓰레드
    private GameStartThread gameStartThread;
    private ClickMainGameThread clickMainGameThread;
    private GameCloseThread gameCloseThread;
    private OutPlayerFromGameThread outPlayerFromGameThread;
    private GameFinishNoticeThread gameFinishNoticeThread;
    private GameLoadingThread gameLoadingThread;
    private UpdateRatingThread updateRatingThread;
    private Handler handler;

    // 파이프게임 로직
    private Random random;
    private TileVO tileSample;
    private int clickNewCountMain = 0;
    private final int TOTAL_COLUMN = 7;
    private final int EXPLOSION = 8;
    private final int MISSILE = 11;
    private final int BLANK_TILE = -1;
    private final int FIRST_VALVE = 6;
    private final int END_VALVE = 7;
    private List<Integer> missileImpossibleList;
    private List<Integer> moveToEastImpossibleList;
    private List<Integer> moveToWestImpossibleList;
    private List<Integer> moveToSouthImpossibleList;
    private List<Integer> moveToNorthImpossibleList;

    // BGM
    private SoundPool sound;
    private int clickSoundAttack, clickSoundTile;
    private MediaPlayer bgmPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent receiveIntent = getIntent();
        portNum = receiveIntent.getIntExtra("portNum", 0);
        loginId = receiveIntent.getStringExtra("loginId");
        task = receiveIntent.getStringExtra("task");
        mode = receiveIntent.getStringExtra("mode");
        player1 = receiveIntent.getStringExtra("player1");
        player2 = receiveIntent.getStringExtra("player2");

        OptionValue.task = task;

        if (receiveIntent.getStringExtra("mode")==null || receiveIntent.getStringExtra("mode").equals(RANK)) {
            mode = RANK;
            ratingP1 = receiveIntent.getIntExtra("ratingP1", 0);
            ratingP2 = receiveIntent.getIntExtra("ratingP2", 0);
        }

        rootActivity = findViewById(R.id.activity_game_root);
        textGamePlayer1 = findViewById(R.id.text_game_player1);
        textGamePlayer2 = findViewById(R.id.text_game_player2);
        textGameVs = findViewById(R.id.text_game_vs);
        gridViewMain = findViewById(R.id.gridView_main_game);
        gridViewEnemy = findViewById(R.id.gridView_enemy_game);
        gridViewAttack = findViewById(R.id.gridView_attack_item);
        gridViewNext = findViewById(R.id.gridView_next_tile);

        sound = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);// maxStreams, streamType, srcQuality
        clickSoundAttack = sound.load(this, R.raw.click_attack,1);
        clickSoundTile = sound.load(this, R.raw.click_tile,2);

        // 배경이미지 및 글씨 색
        if (OptionValue.task.equals(CREATE)) { // devil back (player:angel)
            Glide.with(this).load(R.drawable.devil_back).into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        rootActivity.setBackground(resource);
                    }
                }
            });

            Glide.with(this).load(R.drawable.devil_small).into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        gridViewEnemy.setBackground(resource);
                    }
                }
            });
            textGamePlayer1.setTextColor(Color.WHITE);
            textGamePlayer2.setTextColor(Color.WHITE);
            textGameVs.setTextColor(Color.WHITE);
//            rootActivity.setBackgroundResource(R.drawable.devil_back);
//            gridViewEnemy.setBackgroundResource(R.drawable.devil_small);
        } else if (OptionValue.task.equals(ENTER)) { // angel
            Glide.with(this).load(R.drawable.angel_back).into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        rootActivity.setBackground(resource);
                    }
                }
            });
            Glide.with(this).load(R.drawable.angel_small).into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        gridViewEnemy.setBackground(resource);
                    }
                }
            });

//            rootActivity.setBackgroundResource(R.drawable.angel_back);
//            gridViewEnemy.setBackgroundResource(R.drawable.angel_small);
        }

        textGamePlayer1.setText(player1);
        textGamePlayer2.setText(player2);

        loadingDialog = makeLoadingDialog();
        loadingDialog.show(); // 로딩 다이얼로그

        random = new Random();

        // GridView 들 생성
        tileVOListMain = new ArrayList<>();
        for (int i=0; i<49; i++) {
            tileSample = new TileVO();
            if (i == 0) {
                tileSample.setType(FIRST_VALVE);
            } else if (i==48) {
                tileSample.setType(END_VALVE);
            } else {
                tileSample.setType(BLANK_TILE);
            }
            tileVOListMain.add(tileSample);
        }

        tileVOListEnemy = new ArrayList<>();
        for (int i=0; i<49; i++) {
            tileSample = new TileVO();
            if (i == 0) {
                tileSample.setType(FIRST_VALVE);
            } else if (i==48) {
                tileSample.setType(END_VALVE);
            } else {
                tileSample.setType(BLANK_TILE);
            }
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

        bgmPlayer = MediaPlayer.create(this, R.raw.hos_battlefield_of_eternity);
        bgmPlayer.setLooping(true);
        bgmPlayer.start();

        gridViewMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                sound.play(clickSoundTile, 1.0F, 1.0F,  2,  0,  1.0F);

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
                    if (clickNewCountMain == 5) {
                        tileVOListAttack.remove(3);
                        tileSample = new TileVO();
                        tileSample.setType(MISSILE);
                        tileVOListAttack.add(0, tileSample);
                        attackGameAdapter.notifyDataSetChanged();
                        clickNewCountMain = 0;
                    }
                } else if(i==0 || i==48) {
                    if (gameFinishCheck()) { // 성공
                        gameFinishNoticeThread = new GameFinishNoticeThread();
                        gameFinishNoticeThread.start();
                    } else { // 실패
                        currentDialog = showFailedGameDialog();
                        currentDialog.show();
                    }
                }
            }
        });

        gridViewAttack.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                sound.play(clickSoundTile, 1.0F, 1.0F,  2,  0,  1.0F);
                if (tileVOListAttack.get(i).getType() == MISSILE) {
                    currentDialog = makeAttackDialog(i);
                    currentDialog.show();
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
                        switch (msg.arg1) {
                            case 3:
                                textLoading.setText("3");
                                break;
                            case 2:
                                textLoading.setText("2");
                                break;
                            case 1:
                                textLoading.setText("1");
                                break;
                            case 0:
                                textLoading.setText("GO");
                                break;
                            case -1:
                                loadingDialog.cancel();
                                break;
                        }
                        break;
                    case 161: // Player 메인 게임 클릭
                        receiveMsg = (GameCodeVO) msg.obj;
                        if (tileVOListEnemy.get(receiveMsg.getTileNum()).getType()!=EXPLOSION){
                            tileVOListEnemy.get(receiveMsg.getTileNum()).setType(receiveMsg.getTileType());
                            enemyGameAdapter.notifyDataSetChanged();
                        }
                        break;
                    case 162: // 공격 받음
                        receiveMsg = (GameCodeVO) msg.obj;
                        tileVOListMain.get(receiveMsg.getTileNum()).setType(EXPLOSION);
                        mainGameAdapter.notifyDataSetChanged();
                        sound.play(clickSoundAttack, 1.0F, 1.0F,  1,  0,  1.0F);
                        break;
                    case 166: // P1 나감
                        if (task.equals(CREATE)) {
                            gameStartThread.interrupt();
                            gameCloseThread.start();

                            Intent intent = new Intent(GameActivity.this, MainActivity.class);
                            intent.putExtra("loginId", loginId);
                            startActivity(intent);
                            finish();
                        } else if (task.equals(ENTER)) {
                            makeFinishDialog(true).show();
                        }
                        break;
                    case 167: // P2 나감
                        if (task.equals(CREATE)) {
                            makeFinishDialog(true).show();
                        } else if (task.equals(ENTER)) {
                            gameStartThread.interrupt();
                            gameCloseThread.start();

                            Intent intent = new Intent(GameActivity.this, MainActivity.class);
                            intent.putExtra("loginId", loginId);
                            startActivity(intent);
                            finish();
                        }
                        break;
                    case 169: // 게임 성공
                        receiveMsg = (GameCodeVO) msg.obj;
                        if (task.equals(CREATE)) {
                            if (receiveMsg.getPlayer1()!=null && !receiveMsg.getPlayer1().isEmpty()) {
                                makeFinishDialog(true).show();
                            } else if (receiveMsg.getPlayer2()!=null && !receiveMsg.getPlayer2().isEmpty()) {
                                if (currentDialog!=null && currentDialog.isShowing()) {
                                    Log.d("chs", "다이얼로그 열림");
                                    currentDialog.cancel();
                                }
                                makeFinishDialog(false).show();
                            }
                        } else if (task.equals(ENTER)) {
                            Log.d("yyj","test:"+receiveMsg);
                            if (receiveMsg.getPlayer1()!=null && !receiveMsg.getPlayer1().isEmpty()) {
                                if (currentDialog!=null && currentDialog.isShowing()) {
                                    Log.d("chs", "다이얼로그 열림");
                                    currentDialog.cancel();
                                }
                                makeFinishDialog(false).show();
                            } else if (receiveMsg.getPlayer2()!=null && !receiveMsg.getPlayer2().isEmpty()) {
                                makeFinishDialog(true).show();
                            }
                        }
                        break;
                    case 170: // 미리 누가 게임 성공
                        Toast.makeText(GameActivity.this, "상대가 먼저 성공하였습니다.",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        // 미사일 발사 불가 타일 로딩
        missileImpossibleList = new ArrayList<>();
        missileImpossibleList.add(0);
        missileImpossibleList.add(1);
        missileImpossibleList.add(7);
        missileImpossibleList.add(8);
        missileImpossibleList.add(40);
        missileImpossibleList.add(41);
        missileImpossibleList.add(47);
        missileImpossibleList.add(48);

        // 게임 피니쉬 체크 리스트 로딩
        moveToEastImpossibleList = new ArrayList<>();
        for (int i=1; i<=6; i++) {
            moveToEastImpossibleList.add(TOTAL_COLUMN*i-1);
        }
        moveToWestImpossibleList = new ArrayList<>();
        for (int i=1; i<=6; i++) {
            moveToWestImpossibleList.add(TOTAL_COLUMN*i);
        }
        moveToSouthImpossibleList = new ArrayList<>();
        for (int i=1; i<=6; i++) {
            moveToSouthImpossibleList.add(i+41);
        }
        moveToNorthImpossibleList = new ArrayList<>();
        for (int i=1; i<=6; i++) {
            moveToNorthImpossibleList.add(i);
        }

        // 서버 연결 및 준비완료 쓰레드 시작
        gameStartThread = new GameStartThread();
        gameStartThread.start();

        gameCloseThread = new GameCloseThread();
        gameLoadingThread = new GameLoadingThread();
    }

    @Override
    public void onBackPressed() {
        currentDialog = showExitGameDialog();
        currentDialog.show();
    }

    @Override
    protected void onDestroy() {
        bgmPlayer.stop();
        bgmPlayer.release();
        super.onDestroy();
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
                        gameLoadingThread.start();
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
                    } else if (code.getCode().equals(OUT_PLAYER1_GAME)) { // P1 나감
                        Message msg = new Message();
                        msg.what = 166;
                        handler.sendMessage(msg);
                    } else if (code.getCode().equals(OUT_PLAYER2_GAME)) { // P2 나감
                        Message msg = new Message();
                        msg.what = 167;
                        handler.sendMessage(msg);
                    } else if (code.getCode().equals(GAME_FINISH_RESULT)) { // 게임 성공
                        Message msg = new Message();
                        msg.what = 169;
                        msg.obj = code;
                        handler.sendMessage(msg);
                    } else if (code.getCode().equals(GAME_ALREADY_END)) { // 누군가 미리 게임 성공
                        Message msg = new Message();
                        msg.what = 170;
                        handler.sendMessage(msg);
                    }

                }
            } catch (InterruptedIOException e) {
                e.printStackTrace();
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

            missileImpossibleList.add(tileNum-8);
            missileImpossibleList.add(tileNum-7);
            missileImpossibleList.add(tileNum-6);
            missileImpossibleList.add(tileNum-1);
            missileImpossibleList.add(tileNum);
            missileImpossibleList.add(tileNum+1);
            missileImpossibleList.add(tileNum+6);
            missileImpossibleList.add(tileNum+7);
            missileImpossibleList.add(tileNum+8);
        }
    }

    // 플레이어 퇴장 쓰레드
    class OutPlayerFromGameThread extends Thread {
        @Override
        public void run() {
            GameCodeVO codeVO = new GameCodeVO();

            if (task.equals(CREATE)) {
                codeVO.setPlayer1(loginId);
                codeVO.setCode(OUT_PLAYER1_GAME);
            } else if (task.equals(ENTER)) {
                codeVO.setPlayer2(loginId);
                codeVO.setCode(OUT_PLAYER2_GAME);
            }

            try {
                soos.writeObject(codeVO);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 게임 성공 쓰레드 (상대에게 성공결과 전송)
    class GameFinishNoticeThread extends Thread {
        @Override
        public void run() {
            GameCodeVO codeVO = new GameCodeVO();
            codeVO.setCode(GAME_FINISH_RESULT);

            if (task.equals(CREATE)) {
                codeVO.setPlayer1(loginId);
            } else if (task.equals(ENTER)) {
                codeVO.setPlayer2(loginId);
            }

            try {
                soos.writeObject(codeVO);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 소켓 종료 쓰레드
    class GameCloseThread extends Thread {
        @Override
        public void run() {
            try {
                sois.close();
                soos.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 로딩 쓰레드
    class GameLoadingThread extends Thread {
        @Override
        public void run() {
            try {
                Message msg;

                msg = new Message();
                msg.what = 151;
                msg.arg1 = 3;
                handler.sendMessage(msg);
                sleep(1000);

                msg = new Message();
                msg.what = 151;
                msg.arg1 = 2;
                handler.sendMessage(msg);
                sleep(1000);

                msg = new Message();
                msg.what = 151;
                msg.arg1 = 1;
                handler.sendMessage(msg);
                sleep(1000);

                msg = new Message();
                msg.what = 151;
                msg.arg1 = 0;
                handler.sendMessage(msg);
                sleep(1000);

                msg = new Message();
                msg.what = 151;
                msg.arg1 = -1;
                handler.sendMessage(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // 랭크 종료 후 레이팅 업데이트 쓰레드
    class UpdateRatingThread extends Thread {
        private String updatePlayer1, updatePlayer2;
        private int updateRatingP1, updateRatingP2;

        public UpdateRatingThread(String updatePlayer1, String updatePlayer2, int updateRatingP1, int updateRatingP2) {
            this.updatePlayer1 = updatePlayer1;
            this.updatePlayer2 = updatePlayer2;
            this.updateRatingP1 = updateRatingP1;
            this.updateRatingP2 = updateRatingP2;
        }

        @Override
        public void run() {
            try {
                initNetworkMember();
                sleep(1000);
                MemberCodeVO codeVO = new MemberCodeVO();
                codeVO.setCode(MEMBER_UPDATE_RATING);
                codeVO.setMemberId(updatePlayer1);
                codeVO.setRating(updateRatingP1);
                soos.writeObject(codeVO);
                sois.close();
                soos.close();
                socket.close();

                sleep(1000);

                initNetworkMember();
                sleep(1000);
                codeVO = new MemberCodeVO();
                codeVO.setCode(MEMBER_UPDATE_RATING);
                codeVO.setMemberId(updatePlayer2);
                codeVO.setRating(updateRatingP2);
                soos.writeObject(codeVO);
                sois.close();
                soos.close();
                socket.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Game Room 서버연결
    private boolean initNetwork() {

        try {
            socket = new Socket(InetAddress.getByName(OptionValue.serverIp), portNum);
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

    // DB 서버연결
    private void initNetworkMember() {
        try {
            socket = new Socket(InetAddress.getByName(OptionValue.serverIp), OptionValue.dbServerPort); // DB port
            soos = new ObjectOutputStream(socket.getOutputStream());
            sois = new ObjectInputStream(socket.getInputStream());
            // 입장 직후 방목록 정보 서버로부터 receive
            // thread이므로 handler 통해 main thread로 보냄
            // handler 쪽에서는 방 화면을 갱신.
            Log.d("yyj", "client socket connected to db server");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "서버 문제 발생", Toast.LENGTH_SHORT).show();
        }
    }

    // 공격 다이얼로그
    private Dialog makeAttackDialog(final int attackNum) {
        final Dialog attackDialog = new Dialog(this);
        attackDialog.setContentView(R.layout.dialog_attack);

        GridView gridViewAttackDialog = attackDialog.findViewById(R.id.gridView_attack_dialog);
        attackDialogAdapter = new EnemyGameAdapter(this, R.layout.item_dialog_enemy, tileVOListEnemy);
        gridViewAttackDialog.setAdapter(attackDialogAdapter);

        if (OptionValue.task.equals(CREATE)) { // devil
            gridViewAttackDialog.setBackgroundResource(R.drawable.devil_small);
        } else if (OptionValue.task.equals(ENTER)) { // angel
            gridViewAttackDialog.setBackgroundResource(R.drawable.angel_small);
        }

        gridViewAttackDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                sound.play(clickSoundAttack, 1.0F, 1.0F,  1,  0,  1.0F);
                if (!missileImpossibleList.contains(i)) {
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
                } else {
                    Toast.makeText(GameActivity.this, "설치할 수 없는 위치입니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return attackDialog;
    }

    // finish game 다이얼로그, 누군가 포기해서 종료되는 것 포함
    private Dialog makeFinishDialog(boolean winLose) {
        final Dialog finishDialog = new Dialog(this);
        finishDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        finishDialog.setContentView(R.layout.dialog_finish);

        ImageView imgFinishResult = finishDialog.findViewById(R.id.finish_img_result);
        TextView textFinishResult = finishDialog.findViewById(R.id.finish_text_result);
        Button btnFinishOk = finishDialog.findViewById(R.id.finish_btn_ok);

        if (winLose) {
            if (mode.equals(RANK)) {
                if (loginId.equals(player1)) { // 클라이언트가 P1이면
                    ratingP1 = ratingP1 + 50;
                    ratingP2 = ratingP2 - 50;
                    textFinishResult.setText("승리하였습니다. Rating : "+ratingP1);
                } else { // 클라이언트가 P2이면
                    ratingP1 = ratingP1 - 50;
                    ratingP2 = ratingP2 + 50;
                    textFinishResult.setText("승리하였습니다. Rating : "+ratingP2);
                }
            updateRatingThread = new UpdateRatingThread(player1, player2, ratingP1, ratingP2);
            updateRatingThread.start();

            } else {
                textFinishResult.setText("승리하였습니다.");
            }
            imgFinishResult.setImageResource(R.drawable.victory);
        } else {
            if (mode.equals(RANK)) {
                if (loginId.equals(player1)) { // 클라이언트가 P1이면
                    ratingP1 = ratingP1 - 50;
                    textFinishResult.setText("패배하였습니다. Rating : "+ratingP1);
                } else { // 클라이언트가 P2이면
                    ratingP2 = ratingP2 - 50;
                    textFinishResult.setText("패배하였습니다. Rating : "+ratingP2);
                }
            } else {
                textFinishResult.setText("패배하였습니다.");
            }
            imgFinishResult.setImageResource(R.drawable.defeat);
        }
        btnFinishOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishDialog.cancel();

                gameStartThread.interrupt();
                gameCloseThread.start();

                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                intent.putExtra("loginId", loginId);
                startActivity(intent);
                finish();
            }
        });

        finishDialog.setCanceledOnTouchOutside(false);
        finishDialog.setCancelable(false);

        return finishDialog;
    }

    // 게임 실패 다이얼로그
    private Dialog showFailedGameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        return builder.setTitle("실패했습니다. 다시 시도하세요.")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .create();
    }

    // 게임 나가기 다이얼로그
    private Dialog showExitGameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        return builder.setTitle("게임을 나가시겠습니까?")
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .setPositiveButton("나가기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 서버에 쓰레드 보내서 소켓 없애고 상대 플레이어에게도 종료명령 보내야함
                        outPlayerFromGameThread = new OutPlayerFromGameThread();
                        outPlayerFromGameThread.start();
                    }
                }).create();

    }

    // 로딩 다이얼로그
    private Dialog makeLoadingDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_loading, null);
        TextView textLoading = dialogView.findViewById(R.id.text_loading);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Dialog dialog = builder.setView(dialogView).create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        this.textLoading = textLoading;

        return dialog;
    }

    // 피니쉬 체크 메소드
    private boolean gameFinishCheck() {
        boolean gameFinishResult = false;
        boolean gameFinishWhile = true;

        FinishCheckVO checkVO = new FinishCheckVO();
        checkVO.setTileNum(1);
        checkVO.setDirectionEast();

        while(gameFinishWhile) {
            switch (tileVOListMain.get(checkVO.getTileNum()).getType()) {
                case -1:
                    finishCheckBlank(checkVO);
                    break;
                case 0:
                    finishCheck0(checkVO);
                    break;
                case 1:
                    finishCheck1(checkVO);
                    break;
                case 2:
                    finishCheck2(checkVO);
                    break;
                case 3:
                    finishCheck3(checkVO);
                    break;
                case 4:
                    finishCheck4(checkVO);
                    break;
                case 5:
                    finishCheck5(checkVO);
                    break;
                case FIRST_VALVE:
                    finishCheck6(checkVO);
                    break;
                case END_VALVE:
                    finishCheck7(checkVO);
                    break;
                case EXPLOSION:
                    finishCheck8(checkVO);
                    break;
            }

            if (checkVO.getDirection()==FinishCheckVO.ERROR) {
                gameFinishResult = false;
                gameFinishWhile = false;
            } else if (checkVO.getDirection()==FinishCheckVO.COMPLETE) {
                gameFinishResult = true;
                gameFinishWhile = false;
            }
        }

        return gameFinishResult;
    }

    // 타일별 game finish check logic
    private FinishCheckVO finishCheckBlank(FinishCheckVO finishCheck) {
        finishCheck.setDirectionError();
        return finishCheck;
    }
    private FinishCheckVO finishCheck0(FinishCheckVO finishCheck) {
        if (finishCheck.getDirection()==FinishCheckVO.NORTH) {
            if (moveToEastImpossibleList.contains(finishCheck.getTileNum())) {
                finishCheck.setDirectionError();
            } else {
                finishCheck.setTileNumPlus1();
                finishCheck.setDirectionEast();
            }
        } else if (finishCheck.getDirection()==FinishCheckVO.WEST) {
            if (moveToSouthImpossibleList.contains(finishCheck.getTileNum())) {
                finishCheck.setDirectionError();
            } else {
                finishCheck.setTileNumPlus7();
                finishCheck.setDirectionSouth();
            }
        } else {
            finishCheck.setDirectionError();
        }
        return finishCheck;
    }
    private FinishCheckVO finishCheck1(FinishCheckVO finishCheck) {
        if (finishCheck.getDirection()==FinishCheckVO.EAST) {
            if (moveToEastImpossibleList.contains(finishCheck.getTileNum())) {
                finishCheck.setDirectionError();
            } else {
                finishCheck.setTileNumPlus1();
                finishCheck.setDirectionEast();
            }
        } else if (finishCheck.getDirection()==FinishCheckVO.WEST) {
            if (moveToWestImpossibleList.contains(finishCheck.getTileNum())) {
                finishCheck.setDirectionError();
            } else {
                finishCheck.setTileNumMinus1();
                finishCheck.setDirectionWest();
            }
        } else {
            finishCheck.setDirectionError();
        }
        return finishCheck;
    }
    private FinishCheckVO finishCheck2(FinishCheckVO finishCheck) {
        if (finishCheck.getDirection()==FinishCheckVO.EAST) {
            if (moveToSouthImpossibleList.contains(finishCheck.getTileNum())) {
                finishCheck.setDirectionError();
            } else {
                finishCheck.setTileNumPlus7();
                finishCheck.setDirectionSouth();
            }
        } else if (finishCheck.getDirection()==FinishCheckVO.NORTH) {
            if (moveToWestImpossibleList.contains(finishCheck.getTileNum())) {
                finishCheck.setDirectionError();
            } else {
                finishCheck.setTileNumMinus1();
                finishCheck.setDirectionWest();
            }
        } else {
            finishCheck.setDirectionError();
        }
        return finishCheck;
    }
    private FinishCheckVO finishCheck3(FinishCheckVO finishCheck) {
        if (finishCheck.getDirection()==FinishCheckVO.EAST) {
            if (moveToNorthImpossibleList.contains(finishCheck.getTileNum())) {
                finishCheck.setDirectionError();
            } else {
                finishCheck.setTileNumMinus7();
                finishCheck.setDirectionNorth();
            }
        } else if (finishCheck.getDirection()==FinishCheckVO.SOUTH) {
            if (moveToWestImpossibleList.contains(finishCheck.getTileNum())) {
                finishCheck.setDirectionError();
            } else {
                finishCheck.setTileNumMinus1();
                finishCheck.setDirectionWest();
            }
        } else {
            finishCheck.setDirectionError();
        }
        return finishCheck;
    }
    private FinishCheckVO finishCheck4(FinishCheckVO finishCheck) {
        if (finishCheck.getDirection()==FinishCheckVO.SOUTH) {
            if (moveToEastImpossibleList.contains(finishCheck.getTileNum())) {
                finishCheck.setDirectionError();
            } else {
                finishCheck.setTileNumPlus1();
                finishCheck.setDirectionEast();
            }
        } else if (finishCheck.getDirection()==FinishCheckVO.WEST) {
            if (moveToNorthImpossibleList.contains(finishCheck.getTileNum())) {
                finishCheck.setDirectionError();
            } else {
                finishCheck.setTileNumMinus7();
                finishCheck.setDirectionNorth();
            }
        } else {
            finishCheck.setDirectionError();
        }
        return finishCheck;
    }
    private FinishCheckVO finishCheck5(FinishCheckVO finishCheck) {
        if (finishCheck.getDirection()==FinishCheckVO.NORTH) {
            if (moveToNorthImpossibleList.contains(finishCheck.getTileNum())) {
                finishCheck.setDirectionError();
            } else {
                finishCheck.setTileNumMinus7();
                finishCheck.setDirectionNorth();
            }
        } else if (finishCheck.getDirection()==FinishCheckVO.SOUTH) {
            if (moveToSouthImpossibleList.contains(finishCheck.getTileNum())) {
                finishCheck.setDirectionError();
            } else {
                finishCheck.setTileNumPlus7();
                finishCheck.setDirectionSouth();
            }
        } else {
            finishCheck.setDirectionError();
        }
        return finishCheck;
    }
    private FinishCheckVO finishCheck6(FinishCheckVO finishCheck) {
        if (moveToEastImpossibleList.contains(finishCheck.getTileNum())) {
            finishCheck.setDirectionError();
        } else {
            finishCheck.setTileNumPlus1();
            finishCheck.setDirectionEast();
        }
        return finishCheck;
    }
    private FinishCheckVO finishCheck7(FinishCheckVO finishCheck) {
        if (finishCheck.getDirection()==FinishCheckVO.EAST) {
            finishCheck.setDirectionComplete();
        } else {
            finishCheck.setDirectionError();
        }
        return finishCheck;
    }
    private FinishCheckVO finishCheck8(FinishCheckVO finishCheck) {
        finishCheck.setDirectionError();
        return finishCheck;
    }

}
