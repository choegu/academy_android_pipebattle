package com.choegu.indiegame.pipebattle;

import android.app.Dialog;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.choegu.indiegame.pipebattle.vo.MemberCodeVO;
import com.choegu.indiegame.pipebattle.vo.MemberVO;
import com.choegu.indiegame.pipebattle.vo.OptionValue;
import com.choegu.indiegame.pipebattle.vo.SearchNormalCodeVO;
import com.choegu.indiegame.pipebattle.vo.SearchRankCodeVO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // 네트워크 코드
    private final String MEMBER_LOGIN_CHECK = "memberLoginCheck";
    private final String MEMBER_JOIN = "memberJoin";
    private final String ID_PASSWORD_ERROR = "idPasswordError";
    private final String ID_EXIST_ERROR = "idExistError";
    private final String JOIN_FAILED_ETC = "joinFailedEtc";
    private final String SEARCH_NORMAL = "searchNormal";
    private final String SEARCH_RANK = "searchRank";
    private final String MEMBER_SELECT_RATING = "memberSelectRating";
    private final String MEMBER_SELECT_RATING_ALL = "memberSelectRatingAll";
    private final String MEMBER_UPDATE_RATING = "memberUpdateRating";
    private final String WAITING_SEARCH = "waitingSearch";

    // 네트워크 연결
    private Socket socket;
    private ObjectInputStream sois;
    private ObjectOutputStream soos;

    // Layout
    private String loginId = "";
    private TextView textMainWelcome, textStartTier, textStartRating;
    private Button btnLoginLogout, btnJoin, btnEnterStart, btnRanking;
    private Dialog searchNormalDialog, searchRankDialog, searchCompleteDialog;
    private RankingAdapter rankingAdapter;
    private List<MemberVO> rankingList;

    // 쓰레드
    private LoginCheckThread loginCheckThread;
    private JoinCallThread joinCallThread;
    private SearchNormalThread searchNormalThread;
    private SearchRankThread searchRankThread;
    private SelectRatingThread selectRatingThread;
    private SelectRatingAllThread selectRatingAllThread;
    private CloseMainSocketThread closeMainSocketThread;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textMainWelcome = findViewById(R.id.text_main_welcome);
        btnLoginLogout = findViewById(R.id.btn_login_logout);
        btnJoin = findViewById(R.id.btn_join);
        btnEnterStart = findViewById(R.id.btn_enter_start);
        btnRanking = findViewById(R.id.btn_ranking);

        Intent receiveIntent = getIntent();
        if (receiveIntent.getStringExtra("loginId")!=null) {
            loginId = receiveIntent.getStringExtra("loginId");
        }

        if (loginId.trim().equals("")) { // 로그아웃 상태
            textMainWelcome.setText("로그인 후 이용할 수 있습니다.");
            btnLoginLogout.setBackgroundResource(R.drawable.login_basic);
        } else { // 로그인 상태
            textMainWelcome.setText(loginId+"님 환영합니다.");
            btnLoginLogout.setBackgroundResource(R.drawable.logout_basic);
        }

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

        // 게임 스타트
        btnEnterStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loginId.trim().equals("")) { // 로그아웃 상태
                    showLoginDialog();
                } else { // 로그인 상태
                    makeEnterStartDialog().show();
                }
            }
        });

        // 랭킹 보기
        btnRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeRankingDialog().show();
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
                        btnLoginLogout.setBackgroundResource(R.drawable.logout_basic);
                        textMainWelcome.setText(loginId+"님 환영합니다.");
                        break;
                    case 52: // 로그인 실패
                        showMessageDialog("ID와 비밀번호를 확인하십시오.");
                        break;
                    case 61: // 방으로 인텐트
                        SearchNormalCodeVO codeVO = (SearchNormalCodeVO) msg.obj;
                        Intent intent = new Intent(MainActivity.this, ReadyActivity.class);
                        intent.putExtra("portNum", codeVO.getPortNum());
                        intent.putExtra("loginId", loginId);
                        if (codeVO.getPlayer1().equals(loginId)) {
                            intent.putExtra("task", "create");
                        } else if (codeVO.getPlayer2().equals(loginId)) {
                            intent.putExtra("task", "enter");
                        }
                        intent.putExtra("mode", "normal");
                        intent.putExtra("player1", codeVO.getPlayer1());
                        intent.putExtra("player2", codeVO.getPlayer2());
                        if (searchCompleteDialog!=null && searchCompleteDialog.isShowing()) {
                            searchCompleteDialog.cancel();
                        }
                        startActivity(intent);
                        finish();

                        break;
                    case 62: // 서치 완료
                        if (searchNormalDialog!=null && searchNormalDialog.isShowing()) {
                            searchNormalDialog.cancel();
                        }
                        searchCompleteDialog = makeSearchCompleteDialog();
                        searchCompleteDialog.show();
                        break;
                    case 71: // 레이팅 확인
                        receiveMsg = (MemberCodeVO) msg.obj;
                        int selectRating = receiveMsg.getRating();
                        textStartRating.setText(selectRating+"");

                        if (selectRating < 1000) {
                            textStartTier.setText("BRONZE");
                        } else if (selectRating < 1100) {
                            textStartTier.setText("SILVER");
                        } else if (selectRating < 1200) {
                            textStartTier.setText("GOLD");
                        } else if (selectRating < 1300) {
                            textStartTier.setText("PLATINUM");
                        } else if (selectRating < 1400) {
                            textStartTier.setText("DIAMOND");
                        } else if (selectRating < 1500) {
                            textStartTier.setText("MASTER");
                        } else {
                            textStartTier.setText("GRAND MASTER");
                        }

                        break;
                    case 73: // 랭킹 리스트 로드
                        rankingAdapter.notifyDataSetChanged();
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
            initNetworkMember();
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
            initNetworkMember();
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

    // 티어 레이팅 확인 쓰레드
    class SelectRatingThread extends Thread {
        private String id;

        public SelectRatingThread(String id) {
            this.id = id;
        }

        @Override
        public void run() {
            initNetworkMember();
            MemberCodeVO codeVO = new MemberCodeVO();
            codeVO.setCode(MEMBER_SELECT_RATING);
            codeVO.setMemberId(id);

            try {
                sleep(500);
                soos.writeObject(codeVO);

                codeVO = (MemberCodeVO) sois.readObject();

                Message msg = new Message();
                msg.what = 71; // 레이팅 확인
                msg.obj = codeVO;
                handler.sendMessage(msg);

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

    // 티어 레이팅 랭킹 불러오기 쓰레드
    class SelectRatingAllThread extends Thread {
        @Override
        public void run() {
            initNetworkMember();
            MemberCodeVO codeVO = new MemberCodeVO();
            codeVO.setCode(MEMBER_SELECT_RATING_ALL);

            try {
                sleep(500);
                soos.writeObject(codeVO);

                rankingList = (List<MemberVO>) sois.readObject();

                int rankNum = 0;
                for (MemberVO list: rankingList) {
                    rankNum++;
                    list.setMemberNum(rankNum);
                    if (list.getRating() < 1000) {
                        list.setTier("BRONZE");
                    } else if (list.getRating() < 1100) {
                        list.setTier("SILVER");
                    } else if (list.getRating() < 1200) {
                        list.setTier("GOLD");
                    } else if (list.getRating() < 1300) {
                        list.setTier("PLATINUM");
                    } else if (list.getRating() < 1400) {
                        list.setTier("DIAMOND");
                    } else if (list.getRating() < 1500) {
                        list.setTier("MASTER");
                    } else {
                        list.setTier("GRAND MASTER");
                    }
                }

                rankingAdapter.notifyDataSetChanged();

                Message msg = new Message();
                msg.what = 73; // 레이팅 확인
                msg.obj = rankingList;
                handler.sendMessage(msg);

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

    // 노멀 서치 쓰레드
    class SearchNormalThread extends Thread {
        @Override
        public void run() {
            initNetworkNormal();
            Log.d("chsn", "노멀 서버 연결");

            SearchNormalCodeVO codeVO = new SearchNormalCodeVO();
            codeVO.setCode(SEARCH_NORMAL);
            codeVO.setLoginId(loginId);

            try {
                sleep(500);
                soos.writeObject(codeVO);
                soos.flush();

                codeVO = (SearchNormalCodeVO) sois.readObject();
                Log.d("chsn", codeVO.toString());

                Message msg = new Message();
                msg.what = 62; // 서치완료 메시지 1~2초
                handler.sendMessage(msg);

                if (codeVO.getPlayer1().equals(loginId)) {
                    sleep(1000);
                } else {
                    sleep(2500); // P2는 방 만들어진 후에 들어오게끔
                }

                msg = new Message();
                msg.what = 61; // 방으로 인텐트
                msg.obj = codeVO;
                handler.sendMessage(msg);

                sois.close();
                soos.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    // 랭크 서치 쓰레드
    class SearchRankThread extends Thread {
        @Override
        public void run() {
            initNetworkNormal();
            Log.d("chsn", "Rank 서버 연결");

            SearchRankCodeVO codeVO = new SearchRankCodeVO();
            codeVO.setCode(SEARCH_RANK);
            codeVO.setLoginId(loginId);

            try {
                sleep(500);
                soos.writeObject(codeVO);
                soos.flush();

                codeVO = (SearchRankCodeVO) sois.readObject();
                Log.d("chsn", codeVO.toString());

                Message msg = new Message();
                msg.what = 62; // 서치완료 메시지 1~2초
                handler.sendMessage(msg);

                if (codeVO.getPlayer1().equals(loginId)) {
                    sleep(1000);
                } else {
                    sleep(2500); // P2는 방 만들어진 후에 들어오게끔
                }

                msg = new Message();
                msg.what = 61; // 방으로 인텐트
                msg.obj = codeVO;
                handler.sendMessage(msg);

                sois.close();
                soos.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    // close socket thread
    class CloseMainSocketThread extends Thread {
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

    // Normal 서버연결
    private void initNetworkNormal() {
        try {
            socket = new Socket(InetAddress.getByName(OptionValue.serverIp), OptionValue.normalServerPort); // Normal port
            soos = new ObjectOutputStream(socket.getOutputStream());
            sois = new ObjectInputStream(socket.getInputStream());
            // 입장 직후 방목록 정보 서버로부터 receive
            // thread이므로 handler 통해 main thread로 보냄
            // handler 쪽에서는 방 화면을 갱신.
            Log.d("yyj", "client socket connected to normal server");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "서버 문제 발생", Toast.LENGTH_SHORT).show();
        }
    }

    // Rank 서버연결
    private void initNetworkRank() {
        try {
            socket = new Socket(InetAddress.getByName(OptionValue.serverIp), OptionValue.rankServerPort); // Normal port
            soos = new ObjectOutputStream(socket.getOutputStream());
            sois = new ObjectInputStream(socket.getInputStream());
            // 입장 직후 방목록 정보 서버로부터 receive
            // thread이므로 handler 통해 main thread로 보냄
            // handler 쪽에서는 방 화면을 갱신.
            Log.d("yyj", "client socket connected to rank server");
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
                        btnLoginLogout.setBackgroundResource(R.drawable.login_basic);
                        textMainWelcome.setText("로그인 후 이용할 수 있습니다.");
                        dialogInterface.cancel();
                        showMessageDialog("로그아웃 되었습니다.");
                    }
                })
                .show();
    }

    // 게임 스타트 다이얼로그 (상단에 티어 및 레이팅 띄우기)
    private Dialog makeEnterStartDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_start);

        textStartTier = dialog.findViewById(R.id.text_start_tier);
        textStartRating = dialog.findViewById(R.id.text_start_rating);
        Button btnEnterCustom = dialog.findViewById(R.id.btn_enter_custom);
        Button btnEnterNormal = dialog.findViewById(R.id.btn_enter_normal);
        Button btnEnterRank = dialog.findViewById(R.id.btn_enter_rank);

        selectRatingThread = new SelectRatingThread(loginId);
        selectRatingThread.start();

        btnEnterCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();

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

        btnEnterNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();

                if (loginId.trim().equals("")) { // 로그인 후 이용 가능 메세지
                    showMessageDialog("로그인 후 입장할 수 있습니다.");
                } else { // 노멀 접속
                    searchNormalDialog = makeSearchNormalDialog();
                    searchNormalDialog.show();
                }
            }
        });

        btnEnterRank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();

                if (loginId.trim().equals("")) { // 로그인 후 이용 가능 메세지
                    showMessageDialog("로그인 후 입장할 수 있습니다.");
                } else { // Rank 접속
                    searchRankDialog = makeSearchRankDialog();
                    searchRankDialog.show();
                }
            }
        });

        return dialog;
    }

    // 노멀 서치 다이얼로그
    private Dialog makeSearchNormalDialog() {
        // 노말 찾는 쓰레드
        searchNormalThread = new SearchNormalThread();
        searchNormalThread.start();

        Dialog searchNormalDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        searchNormalDialog = builder.setTitle("노멀게임 : 상대를 검색하는 중입니다.")
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 클로즈 쓰레드
                        closeMainSocketThread = new CloseMainSocketThread();
                        closeMainSocketThread.start();
                        dialogInterface.cancel();
                    }
                })
                .create();

        searchNormalDialog.setCancelable(false);
        searchNormalDialog.setCanceledOnTouchOutside(false);

        return searchNormalDialog;
    }

    // Rank 서치 다이얼로그
    private Dialog makeSearchRankDialog() {
        // Rank 찾는 쓰레드
        searchRankThread = new SearchRankThread();
        searchRankThread.start();

        Dialog searchNormalDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        searchNormalDialog = builder.setTitle("Rank게임 : 상대를 검색하는 중입니다.")
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 클로즈 쓰레드
                        closeMainSocketThread = new CloseMainSocketThread();
                        closeMainSocketThread.start();
                        dialogInterface.cancel();
                    }
                })
                .create();

        searchNormalDialog.setCancelable(false);
        searchNormalDialog.setCanceledOnTouchOutside(false);

        return searchNormalDialog;
    }

    // 서치 완료 다이얼로그
    private Dialog makeSearchCompleteDialog() {
        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        dialog = builder.setTitle("상대를 찾았습니다.")
                .create();

        searchNormalDialog.setCancelable(false);
        searchNormalDialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    // Ranking 다이얼로그 (쓰레드로 불러오는 작업 해야함)
    private Dialog makeRankingDialog() {
        rankingList = new ArrayList<>();
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_ranking);

        ListView listViewRanking = dialog.findViewById(R.id.list_view_ranking);
        rankingAdapter = new RankingAdapter(this, R.layout.item_ranking, rankingList);
        listViewRanking.setAdapter(rankingAdapter);

        selectRatingAllThread = new SelectRatingAllThread();
        selectRatingAllThread.start();

        return dialog;
    }
}
