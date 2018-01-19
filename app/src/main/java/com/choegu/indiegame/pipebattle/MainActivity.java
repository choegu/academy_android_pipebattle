package com.choegu.indiegame.pipebattle;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.choegu.indiegame.pipebattle.vo.MemberCodeVO;
import com.choegu.indiegame.pipebattle.vo.OptionValue;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    // 네트워크 코드
    private final String MEMBER_LOGIN_CHECK = "memberLoginCheck";
    private final String MEMBER_JOIN = "memberJoin";
    private final String ID_PASSWORD_ERROR = "idPasswordError";
    private final String ID_EXIST_ERROR = "idExistError";
    private final String JOIN_FAILED_ETC = "joinFailedEtc";

    // 네트워크 연결
    private Socket socket;
    private ObjectInputStream sois;
    private ObjectOutputStream soos;

    // Layout
    private String loginId = "";
    private TextView textMainWelcome;
    private Button btnLoginLogout, btnJoin, btnEnterCustom, btnEnterNormal, btnEnterRank, btnSetting, btnRanking, btnExit;

    // 쓰레드
    private LoginCheckThread loginCheckThread;
    private JoinCallThread joinCallThread;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textMainWelcome = findViewById(R.id.text_main_welcome);
        btnLoginLogout = findViewById(R.id.btn_login_logout);
        btnJoin = findViewById(R.id.btn_join);
        btnEnterCustom = findViewById(R.id.btn_enter_custom);
        btnEnterNormal = findViewById(R.id.btn_enter_normal);
        btnEnterRank = findViewById(R.id.btn_enter_rank);
        btnSetting = findViewById(R.id.btn_setting);
        btnRanking = findViewById(R.id.btn_ranking);
        btnExit = findViewById(R.id.btn_exit);

        // 로그인 로그아웃
        btnLoginLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loginId.trim().equals("")) { // 로그아웃 상태
                    showLoginDialog();
                } else { // 로그인 상태
                    showLogoutCheckDialog();
                }
            }
        });

        // 회원가입
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showJoinDialog();
            }
        });

        // 커스텀 입장
        btnEnterCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loginId.trim().equals("")) { // 로그인 후 이용 가능 메세지
                    showMessageDialog("로그인 후 입장할 수 있습니다.");
                } else { // 커스텀 접속
                    Intent intent = new Intent(MainActivity.this, ListActivity.class);
                    intent.putExtra("loginId", loginId);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // 노멀 입장
        btnEnterNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // 랭크 입장
        btnEnterRank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // 설정
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // 랭킹 보기
        btnRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // 종료
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExitDialog();
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                int handleMsg = msg.what;
                MemberCodeVO receiveMsg;

                switch (handleMsg) {
                    case 41: // 회원가입 성공
                        showMessageDialog("회원가입에 성공했습니다.");
                        break;
                    case 42: // 중복된 ID 존재
                        showMessageDialog("중복 ID가 존재합니다.");
                        break;
                    case 43: // 기타 이유로 회원가입 실패
                        showMessageDialog("회원가입에 실패했습니다.");
                        break;
                    case 51: // 로그인 성공
                        receiveMsg = (MemberCodeVO) msg.obj;
                        loginId = receiveMsg.getMemberId();
                        showMessageDialog("로그인에 성공했습니다.");
                        btnLoginLogout.setText("로그아웃");
                        textMainWelcome.setText(loginId+"님 환영합니다.");
                        break;
                    case 52: // 로그인 실패
                        showMessageDialog("ID와 비밀번호를 확인하십시오.");
                        break;
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    // 로그인 확인 쓰레드
    class LoginCheckThread extends Thread {
        private String id;
        private String password;

        public LoginCheckThread(String id, String password) {
            this.id = id;
            this.password = password;
        }

        @Override
        public void run() {
            initNetwork();
            MemberCodeVO codeVO = new MemberCodeVO();
            codeVO.setCode(MEMBER_LOGIN_CHECK);
            codeVO.setMemberId(id);
            codeVO.setPassword(password);

            try {
                sleep(500);
                soos.writeObject(codeVO);

                codeVO = (MemberCodeVO) sois.readObject();

                if (codeVO.getCode().equals(ID_PASSWORD_ERROR)) {
                    Message msg = new Message();
                    msg.what = 52; // 로그인 실패
                    handler.sendMessage(msg);
                } else {
                    Message msg = new Message();
                    msg.what = 51; // 로그인 성공
                    msg.obj = codeVO;
                    handler.sendMessage(msg);
                }

                sois.close();
                soos.close();
                socket.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // 회원가입 요청 쓰레드
    class JoinCallThread extends Thread {
        private String id;
        private String password;

        public JoinCallThread(String id, String password) {
            this.id = id;
            this.password = password;
        }

        @Override
        public void run() {
            initNetwork();
            MemberCodeVO codeVO = new MemberCodeVO();
            codeVO.setCode(MEMBER_JOIN);
            codeVO.setMemberId(id);
            codeVO.setPassword(password);

            try {
                sleep(500);
                soos.writeObject(codeVO);

                codeVO = (MemberCodeVO) sois.readObject();

                if (codeVO.getCode().equals(MEMBER_JOIN)) {
                    Message msg = new Message();
                    msg.what = 41; // 회원가입 성공
                    handler.sendMessage(msg);
                } else if (codeVO.getCode().equals(ID_EXIST_ERROR)){
                    Message msg = new Message();
                    msg.what = 42; // 아이디 중복
                    handler.sendMessage(msg);
                } else if (codeVO.getCode().equals(JOIN_FAILED_ETC)){
                    Message msg = new Message();
                    msg.what = 43; // 기타 이유로 실패
                    handler.sendMessage(msg);
                }

                sois.close();
                soos.close();
                socket.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // DB 서버연결
    private void initNetwork() {
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

    // 종료 다이얼로그
    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("파이프배틀을 종료하시겠습니까?")
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.this.finish();
                    }
                })
                .show();
    }

    // 일반 메시지 전용 다이얼로그
    private void showMessageDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(message)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .show();
    }

    // 로그인 다이얼로그
    private void showLoginDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_login, null);
        final EditText loginEditId = dialogView.findViewById(R.id.login_edit_id);
        final EditText loginEditPassword = dialogView.findViewById(R.id.login_edit_password);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(dialogView)
                .setTitle("로그인")
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if ((loginEditId.getText()+"").trim().equals("") || (loginEditPassword.getText()+"").trim().equals("")) {
                            showMessageDialog("ID와 비밀번호를 모두 정확히 입력하십시오.");
                        } else {
                            loginCheckThread = new LoginCheckThread(loginEditId.getText()+"", loginEditPassword.getText()+"");
                            loginCheckThread.start();
                        }
                        dialogInterface.cancel();
                    }
                })
                .show();
    }

    // 회원가입 다이얼로그
    private void showJoinDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_join, null);
        final EditText joinEditId = dialogView.findViewById(R.id.join_edit_id);
        final EditText joinEditPassword = dialogView.findViewById(R.id.join_edit_password);
        final EditText joinEditRePassword = dialogView.findViewById(R.id.join_edit_re_password);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(dialogView)
                .setTitle("회원가입")
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if ((joinEditId.getText()+"").trim().equals("") || (joinEditPassword.getText()+"").trim().equals("") || (joinEditRePassword.getText()+"").trim().equals("")) {
                            Toast.makeText(MainActivity.this, "ID와 비밀번호를 정확히 입력하십시오.",Toast.LENGTH_SHORT).show();
                        } else if (!((joinEditPassword.getText()+"").equals(joinEditRePassword.getText()+""))) {
                            Toast.makeText(MainActivity.this, "같은 비밀번호를 입력하십시오.",Toast.LENGTH_SHORT).show();
                        } else {
                            joinCallThread = new JoinCallThread(joinEditId.getText()+"", joinEditPassword.getText()+"");
                            joinCallThread.start();
                        }
                        dialogInterface.cancel();
                    }
                })
                .show();
    }

    // 로그아웃 확인 다이얼로그
    private void showLogoutCheckDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("로그아웃하시겠습니까?")
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        loginId = "";
                        btnLoginLogout.setText("로그인");
                        textMainWelcome.setText("로그인 후 이용할 수 있습니다.");
                        dialogInterface.cancel();
                        showMessageDialog("로그아웃 되었습니다.");
                    }
                })
                .show();
    }

}
