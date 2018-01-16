package com.choegu.indiegame.pipebattle;

import android.app.Dialog;
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

import com.choegu.indiegame.pipebattle.vo.FinishCheckVO;
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
            tileVOListMain.add(tileSample);
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
                } else if(i==0 || i==48) {
                    if (gameFinishCheck()) { // 성공

                    } else { // 실패

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
                }
            }
        });

        return attackDialog;
    }

    // 피니쉬 체크 메소드
    private boolean gameFinishCheck() {
        boolean gameFinishResult = false;

        FinishCheckVO checkVO = new FinishCheckVO();
        checkVO.setTileNum(1);
        checkVO.setDirectionEast();

        while(true) {
            switch (tileVOListMain.get(checkVO.getTileNum()).getType()) {
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
            }

            if (checkVO.getDirection()==FinishCheckVO.ERROR) {
                gameFinishResult = false;
                break;
            } else if (checkVO.getDirection()==FinishCheckVO.COMPLETE) {
                gameFinishResult = true;
                break;
            }
        }

        return gameFinishResult;
    }

    // 타일별 game finish check logic
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
}
