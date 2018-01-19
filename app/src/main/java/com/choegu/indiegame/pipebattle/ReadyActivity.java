package com.choegu.indiegame.pipebattle;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.choegu.indiegame.pipebattle.vo.OptionValue;
import com.choegu.indiegame.pipebattle.vo.ReadyCodeVO;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by student on 2018-01-10.
 */

public class ReadyActivity extends AppCompatActivity {
    // 네크워크 코드
    private final String GAME_START_READY = "gameStartReady";
    private final String CHAT_READY = "chatReady";
    private final String REFRESH_READY = "refreshReady";
    private final String START_READY = "startReady";
    private final String FULL_ROOM_READY = "foolRoomReady";
    private final String ERROR_ROOM_READY = "errorRoomReady";
    private final String OUT_PLAYER1_READY = "outPlayer1Ready";
    private final String OUT_PLAYER2_READY = "outPlayer2Ready";
    private final String DROP_PLAYER2_READY = "dropPlayer2Ready";

    // 방 입장 task
    private final String CREATE = "create";
    private final String ENTER = "enter";

    // 네트워크 연결
    private int portNum;
    private ObjectInputStream sois;
    private ObjectOutputStream soos;
    private boolean readyNetwork = false, playerReady = false;
    private String loginId, task;
    private Socket socket;

    // Layout
    private Button btnReadyStart, btnReadyX, btnReadySend, btnPlayer1, btnPlayer2;
    private TextView textReadyChat;
    private EditText editReadymsg;
    private InputMethodManager imm;
    private final String EMPTY = "empty";

    // 쓰레드
    private ReadyRoomThread readyRoomThread;
    private ReadySendThread readySendThread;
    private ReadyStartThread readyStartThread;
    private ReadyCloseThread readyCloseThread;
    private OutPlayerThread outPlayerThread;
    private Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready);

        btnReadyStart = findViewById(R.id.btn_ready_start);
        btnReadyX = findViewById(R.id.btn_ready_x);
        btnReadySend = findViewById(R.id.btn_ready_send);
        btnPlayer1 = findViewById(R.id.btn_player1);
        btnPlayer2 = findViewById(R.id.btn_player2);
        textReadyChat = findViewById(R.id.text_ready_chat);
        editReadymsg = findViewById(R.id.edit_ready_msg);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Intent receiveIntent = getIntent();
        portNum = receiveIntent.getIntExtra("portNum", 0);
        loginId = receiveIntent.getStringExtra("loginId");
        task = receiveIntent.getStringExtra("task");

        if (task.equals(CREATE)) {
            btnReadyStart.setText("START");
            btnPlayer1.setText(loginId);
            btnPlayer1.setBackgroundColor(Color.YELLOW);
            btnPlayer2.setText(EMPTY);
            btnPlayer2.setBackgroundColor(Color.GRAY);
        } else if (task.equals(ENTER)) {
            btnPlayer2.setText(loginId);
            btnPlayer2.setBackgroundColor(Color.YELLOW);
        }
        btnReadyStart.setBackgroundColor(Color.GRAY);

        btnReadyStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readyStartThread = new ReadyStartThread();
                readyStartThread.start();
            }
        });

        btnReadyX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnReadySend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReadyCodeVO codeVO = new ReadyCodeVO();
                codeVO.setCode(CHAT_READY);
                codeVO.setMessage(loginId + " : " + editReadymsg.getText());

                readySendThread = new ReadySendThread(codeVO);
                readySendThread.start();

                hideKeyboard();
            }
        });

        btnPlayer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnPlayer2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                int handlerMsg = msg.what;
                ReadyCodeVO receiveMsg;

                switch (handlerMsg) {
                    case 131: // 채팅 전송 완료
                        editReadymsg.setText("");
                        break;
                    case 132: // 채팅 수신
                        receiveMsg = (ReadyCodeVO) msg.obj;
                        textReadyChat.append(receiveMsg.getMessage()+"\n");
                        textReadyChat.setVerticalScrollbarPosition(textReadyChat.getText().length());
                        break;
                    case 138: // 방 생성 직후 새로고침
                        receiveMsg = (ReadyCodeVO) msg.obj;

                        Log.d("chs", "code 138 : "+receiveMsg.toString());

                        if (task.equals(CREATE)) {
                            if (receiveMsg.getPlayer2()!=null && !receiveMsg.getPlayer2().isEmpty()) {
                                btnPlayer2.setText(receiveMsg.getPlayer2());
                                btnPlayer2.setBackgroundColor(Color.YELLOW);

                                textReadyChat.append(receiveMsg.getPlayer2()+"님이 입장하였습니다.\n");
                                textReadyChat.setVerticalScrollbarPosition(textReadyChat.getText().length());
                            }
                        } else if (task.equals(ENTER)) {
                            btnPlayer1.setText(receiveMsg.getPlayer1());
                            btnPlayer1.setBackgroundColor(Color.YELLOW);
                            btnPlayer2.setText(receiveMsg.getPlayer2());
                            btnPlayer2.setBackgroundColor(Color.YELLOW);
                        }
                        break;
                    case 139: // 정원초과 또는 error방으로 인한 ListActivity intent
                        showFullRoomExitDialog();
                        break;
                    case 141: // GAME START
                        Intent intent = new Intent(ReadyActivity.this, GameActivity.class);
                        intent.putExtra("portNum", portNum);
                        intent.putExtra("loginId", loginId);
                        if (task.equals(CREATE)) {
                            intent.putExtra("task", CREATE);
                        } else if (task.equals(ENTER)) {
                            intent.putExtra("task", ENTER);
                        }
                        startActivity(intent);
                        finish();

                        readyCloseThread = new ReadyCloseThread();
                        readyCloseThread.start();
                        break;
                    case 143: // ready 완료
                        btnReadyStart.setBackgroundColor(Color.GREEN);
                        playerReady = true;
                        break;
                    case 144: // 이미 ready 누름
                        Toast.makeText(ReadyActivity.this, "이미 눌렀습니다", Toast.LENGTH_SHORT).show();
                        break;
                    case 145: // 상대방 ready 안됨
                        Toast.makeText(ReadyActivity.this, "상대방이 준비 완료되지 않았습니다", Toast.LENGTH_SHORT).show();
                        break;
                    case 146: // 방장에 의한 방 종료
                        showRoomCreatorExitDialog();
                        break;
                    case 147: // 2P 퇴장
                        if (task.equals(CREATE)) {
                            receiveMsg = (ReadyCodeVO) msg.obj;
                            textReadyChat.append(receiveMsg.getPlayer2()+"님이 퇴장하였습니다.\n");
                            textReadyChat.setVerticalScrollbarPosition(textReadyChat.getText().length());

                            btnPlayer2.setText(EMPTY);
                            btnPlayer2.setBackgroundColor(Color.GRAY);
                            btnReadyStart.setBackgroundColor(Color.GRAY);
                            playerReady = false;
                        } else if (task.equals(ENTER)) {
                            readyRoomThread.interrupt();
                            readyCloseThread.start();

                            Intent intentOutPlayer2 = new Intent(ReadyActivity.this, ListActivity.class);
                            intentOutPlayer2.putExtra("loginId", loginId);
                            startActivity(intentOutPlayer2);
                            finish();
                        }
                        break;
                    case 149: // 2P 비정상 종료
                        if (task.equals(CREATE)) {
                            btnPlayer2.setText(EMPTY);
                            btnPlayer2.setBackgroundColor(Color.GRAY);
                            btnReadyStart.setBackgroundColor(Color.GRAY);
                            playerReady = false;
                        }
                        break;
                }
            }
        };

        readyRoomThread = new ReadyRoomThread();
        readyRoomThread.start();

        readyCloseThread = new ReadyCloseThread();
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    // 채팅 전송 쓰레드
    class ReadySendThread extends Thread {
        ReadyCodeVO codeVO;

        public ReadySendThread(ReadyCodeVO codeVO) {
            this.codeVO = codeVO;
        }

        @Override
        public void run() {
            try {
                soos.writeObject(codeVO);
                soos.flush();

                Message msg = new Message();
                msg.what = 131;
                handler.sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 준비 완료, 게임 시작 전송 쓰레드
    class ReadyStartThread extends Thread {
        ReadyCodeVO codeVO = new ReadyCodeVO();

        @Override
        public void run() {
            if (task.equals(CREATE)) {
                if (playerReady) {
                    // 게임 시작
                    codeVO.setCode(GAME_START_READY);
                    try {
                        soos.writeObject(codeVO);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    // 핸들러 옮겨야함
                    Message msg = new Message();
                    msg.what = 145;
                    handler.sendMessage(msg);
                }
            } else if (task.equals(ENTER)) {
                if (playerReady) {
                    // 이미 준비 눌렀습니다
                    Message msg = new Message();
                    msg.what = 144;
                    handler.sendMessage(msg);
                } else {
                    codeVO.setCode(START_READY);
                    try {
                        soos.writeObject(codeVO);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // 방 생성 직후 네트워크 연결 및 코드 받는 쓰레드
    class ReadyRoomThread extends Thread {
        @Override
        public void run() {
            try {
                if (!readyNetwork) {
                    readyNetwork = initNetwork();
                }

                ReadyCodeVO codeVO = new ReadyCodeVO();
                codeVO.setCode(REFRESH_READY);

                if (task.equals(CREATE)) {
                    codeVO.setPlayer1(loginId);
                } else if (task.equals(ENTER)) {
                    codeVO.setPlayer2(loginId);
                }

                soos.writeObject(codeVO);

                while (readyNetwork) {
                    ReadyCodeVO code = (ReadyCodeVO) sois.readObject();
                    Log.d("chs", "thread while : "+code.toString());

                    if (code.getCode().equals(CHAT_READY)) {
                        Message msg = new Message();
                        msg.what = 132;
                        msg.obj = code;
                        handler.sendMessage(msg);
                    } else if (code.getCode().equals(REFRESH_READY)) {
                        Message msg = new Message();
                        msg.what = 138;
                        msg.obj = code;
                        handler.sendMessage(msg);
                    } else if (code.getCode().equals(FULL_ROOM_READY)) {
                        Message msg = new Message();
                        msg.what = 139;
                        handler.sendMessage(msg);
                    } else if (code.getCode().equals(ERROR_ROOM_READY)) {
                        Message msg = new Message();
                        msg.what = 139;
                        handler.sendMessage(msg);
                    } else if (code.getCode().equals(START_READY)) { //player2 ready
                        Message msg = new Message();
                        msg.what = 143;
                        handler.sendMessage(msg);
                    } else if (code.getCode().equals(GAME_START_READY)) { //player1요청으로 둘다 시작
                        Message msg = new Message();
                        msg.what = 141;
                        handler.sendMessage(msg);

                        readyCloseThread.start();
                        throw new IOException();
                    } else if (code.getCode().equals(OUT_PLAYER1_READY)) { // 방장에 의한 나가기
                        Message msg = new Message();
                        msg.what = 146;
                        handler.sendMessage(msg);
                    } else if (code.getCode().equals(OUT_PLAYER2_READY)) { // 2P 퇴장
                        Message msg = new Message();
                        msg.what = 147;
                        msg.obj = code;
                        handler.sendMessage(msg);
                    } else if (code.getCode().equals(DROP_PLAYER2_READY)) { // 2P DROP
                        Message msg = new Message();
                        msg.what = 149;
                        handler.sendMessage(msg);
                    }

                }
            } catch (InterruptedIOException e){
                e.printStackTrace();
                Log.d("chs", "ready 쓰레드 인터럽트");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // 플레이어 퇴장 쓰레드
    class OutPlayerThread extends Thread {
        @Override
        public void run() {
            ReadyCodeVO codeVO = new ReadyCodeVO();

            if (task.equals(CREATE)) {
                codeVO.setPlayer1(loginId);
                codeVO.setCode(OUT_PLAYER1_READY);
            } else if (task.equals(ENTER)) {
                codeVO.setPlayer2(loginId);
                codeVO.setCode(OUT_PLAYER2_READY);
            }

            try {
                soos.writeObject(codeVO);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 소켓 종료 쓰레드
    class ReadyCloseThread extends Thread {
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

    // 방 나가기 다이얼로그
    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("방을 나가시겠습니까?")
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .setPositiveButton("나가기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 서버에 쓰레드 보내서 방 없애거나(P1) 인원수 줄이고(P2) 다시 명령 받아야함
                        outPlayerThread = new OutPlayerThread();
                        outPlayerThread.start();
                    }
                })
                .show();
    }

    // 풀방 또는 에러방 메시지 다이얼로그
    private void showFullRoomExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("정원 초과되었거나 유효하지 않은 방입니다")
                .setPositiveButton("나가기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        readyRoomThread.interrupt();
                        readyCloseThread.start();

                        Intent intent = new Intent(ReadyActivity.this, ListActivity.class);
                        intent.putExtra("loginId", loginId);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }

    // 방장에 의한 종료 다이얼로그
    private void showRoomCreatorExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("방장에 의해 종료됩니다")
                .setPositiveButton("나가기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        readyRoomThread.interrupt();
                        readyCloseThread.start();

                        Intent intent = new Intent(ReadyActivity.this, ListActivity.class);
                        intent.putExtra("loginId", loginId);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }

    // Ready Room 서버연결
    private boolean initNetwork() {

        try {
            socket = new Socket(InetAddress.getByName(OptionValue.serverIp), portNum);
            Log.d("chs", "소켓 생성 "+OptionValue.serverIp+" "+portNum);
            soos = new ObjectOutputStream(socket.getOutputStream());
            sois = new ObjectInputStream(socket.getInputStream());
            readyNetwork = true;

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "서버 문제 발생", Toast.LENGTH_SHORT).show();
        }

        return readyNetwork;
    }

    // 키보드 내리기
    private void hideKeyboard()
    {
        imm.hideSoftInputFromWindow(editReadymsg.getWindowToken(), 0);
    }
}
