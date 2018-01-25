package com.choegu.indiegame.pipebattle;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.choegu.indiegame.pipebattle.vo.ListCodeVO;
import com.choegu.indiegame.pipebattle.vo.OptionValue;
import com.choegu.indiegame.pipebattle.vo.RoomVO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by student on 2018-01-10.
 */

public class ListActivity extends MusicActivity {
    // 네트워크 task
    private final String CREATE_ROOM = "createRoom";
    private final String REFRESH_ROOM = "refreshRoom";

    // 네트워크 연결
    private Socket socket;
    private ObjectInputStream sois;
    private ObjectOutputStream soos;
    private boolean rootNetwork;

    // Layout
    private Button btnCreate, btnRefresh;
    private ListView listViewRoom;
    private List<RoomVO> roomVOList;
    private RoomAdapter adapter;
    private String loginId;

    // 쓰레드
    private RefreshRoomThread refresh;
    private CreateRoomThread create;
    private CloseListThread closeNetworkThread;
    private Handler handler;

    // BGM
    private SoundPool sound;
    private int clickSoundId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        btnCreate = findViewById(R.id.btn_create);
        btnRefresh = findViewById(R.id.btn_refresh);
        listViewRoom = findViewById(R.id.listview_room);

        sound = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);// maxStreams, streamType, srcQuality
        clickSoundId = sound.load(this, R.raw.click_button,1);

        Intent receiveIntent = getIntent();
        loginId = receiveIntent.getStringExtra("loginId");

        roomVOList = new ArrayList<>();
        refresh = new RefreshRoomThread();
        Log.d("yyj", "refresh before"+rootNetwork);
        refresh.start(); // initNetwork;

        Log.d("yyj", "refresh start"+rootNetwork);
        adapter = new RoomAdapter(this, R.layout.item_room, roomVOList);
        listViewRoom.setAdapter(adapter);

        // 방 생성
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.play(clickSoundId, 1.0F, 1.0F,  1,  0,  1.0F);
                showCreateDialog();
            }
        });

        // 방 입장
        listViewRoom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                sound.play(clickSoundId, 1.0F, 1.0F,  1,  0,  1.0F);
                if (roomVOList.get(i).getPlayerNum()<2) {
                    Intent intent = new Intent(ListActivity.this, ReadyActivity.class);
                    intent.putExtra("portNum", roomVOList.get(i).getPortNum());
                    intent.putExtra("loginId", loginId);
                    intent.putExtra("task", "enter");
                    intent.putExtra("mode", "custom");
                    startActivity(intent);
                    finish();
                    closeNetworkThread = new CloseListThread();
                    closeNetworkThread.start();
                } else {
                    showFullRoomMessageDialog();
                }
            }
        });

        // 새로고침
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sound.play(clickSoundId, 1.0F, 1.0F,  1,  0,  1.0F);
                refresh = new RefreshRoomThread();
                refresh.start();
            }
        });

        closeNetworkThread = new CloseListThread();

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                int handlerMsg = msg.what;

                switch (handlerMsg) {
                    case 101: // 방 생성
                        Intent intent = new Intent(ListActivity.this, ReadyActivity.class);
                        intent.putExtra("portNum", msg.arg1);
                        intent.putExtra("loginId", loginId);
                        intent.putExtra("task", "create");
                        intent.putExtra("mode", "custom");

                        try {
                            sois.close();
                            soos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        closeNetworkThread.start();

                        startActivity(intent);
                        finish();

                        break;
                    case 108: // 새로고침
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(), "현재 방 : "+roomVOList.size()+"개", Toast.LENGTH_SHORT).show();
                        break;
                    case 109: // 방 없음 toast
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(), "생성된 방 없음", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
    }


    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    // 새로고침 쓰레드
    class RefreshRoomThread extends Thread {
        @Override
        public void run() {
            try {

                if (!rootNetwork) {
                    Log.d("yyj", "refreshRoomThread start");
                    initNetwork();
                }

                if (rootNetwork) {
                    ListCodeVO codeVO = new ListCodeVO();
                    codeVO.setCode(REFRESH_ROOM);
                    soos.writeObject(codeVO);
                    soos.flush();
                    Log.d("chs", "refresh code");
                    roomVOList.clear();
                    Log.d("chs", "room list clear");
                    List<RoomVO> rList = (List<RoomVO>) sois.readObject();
                    Log.d("chs", "Receive list"+rList.toString());
                    roomVOList.addAll(rList);
                    Log.d("chs", rList.size()+"개");
                }

                Log.d("chs", roomVOList.size()+"");

                if (roomVOList.size()==0) {
                    // 생성된 방 없음
                    Message msg = new Message();
                    msg.what = 109;
                    handler.sendMessage(msg);
                } else {
                    Message msg = new Message();
                    msg.what = 108;
                    handler.sendMessage(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // 방 생성 쓰레드
    class CreateRoomThread extends Thread {
        private ListCodeVO codeVO;

        public CreateRoomThread(ListCodeVO codeVO) {
            this.codeVO = codeVO;
        }

        @Override
        public void run() {
            try {
                Log.d("yyj", "soos:"+soos);
                soos.writeObject(codeVO);
                soos.flush();

                int createPortNum = (int)sois.readObject();

                // 나중에 로딩중으로 대체
                sleep(500);

                Log.d("yyj", "portNum"+createPortNum);

                // 방 생성
                Message msg = new Message();
                msg.what = 101;
                msg.arg1 = createPortNum;
                handler.sendMessage(msg);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // 소켓 종료 쓰레드
    class CloseListThread extends Thread {
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

    // 루트 서버연결
    private boolean initNetwork() {
        rootNetwork = false;

        Log.d("yyj","1");
        try {
            socket = new Socket(InetAddress.getByName(OptionValue.serverIp), 10000);
            Log.d("yyj","2");
            soos = new ObjectOutputStream(socket.getOutputStream());
            Log.d("yyj","3");
            sois = new ObjectInputStream(socket.getInputStream());
            Log.d("yyj","4");
            rootNetwork = true;
            // 입장 직후 방목록 정보 서버로부터 receive
            // thread이므로 handler 통해 main thread로 보냄
            // handler 쪽에서는 방 화면을 갱신.
            Log.d("yyj", "client socket connected");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "서버 문제 발생", Toast.LENGTH_SHORT).show();
        }

        return rootNetwork;
    }

    // 방생성 다이얼로그
    private void showCreateDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create, null);
        final EditText editRoomName = dialogView.findViewById(R.id.create_edit_room_name);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(dialogView)
                .setTitle("생성할 방 이름 입력")
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ListCodeVO codeVO = new ListCodeVO();
                        codeVO.setCode(CREATE_ROOM);
                        codeVO.setId(loginId);
                        codeVO.setTitle(editRoomName.getText()+"");

                        create = new CreateRoomThread(codeVO);
                        create.start();
                    }
                })
                .show();
    }

    // 풀방 다이얼로그
    private void showFullRoomMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("방 인원이 가득 찼습니다")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .show();
    }

    // 목록 나가기 다이얼로그
    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("메인화면으로 나가시겠습니까?")
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .setPositiveButton("나가기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        closeNetworkThread.start();
                        dialogInterface.cancel();
                        Intent intent = new Intent(ListActivity.this, MainActivity.class);
                        intent.putExtra("loginId", loginId);
                        startActivity(intent);
                        ListActivity.this.finish();
                    }
                })
                .show();
    }
}
