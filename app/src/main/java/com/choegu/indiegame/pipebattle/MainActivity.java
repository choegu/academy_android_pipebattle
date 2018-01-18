package com.choegu.indiegame.pipebattle;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.choegu.indiegame.pipebattle.dao.MemberDao;
import com.choegu.indiegame.pipebattle.vo.ListCodeVO;
import com.choegu.indiegame.pipebattle.vo.MemberVO;

public class MainActivity extends AppCompatActivity {
    // 로그인
    private String loginId;
    private MemberDao memberDao;
    private MemberVO memberVO;

    // Layout
    private TextView textMainTitle, textMainLoginId;
    private Button btnLogin, btnJoin, btnEnter, btnSetting, btnRanking, btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textMainTitle = findViewById(R.id.text_main_title);
        textMainLoginId = findViewById(R.id.text_main_loginId);
        btnLogin = findViewById(R.id.btn_login);
        btnJoin = findViewById(R.id.btn_join);
        btnEnter = findViewById(R.id.btn_enter);
        btnSetting = findViewById(R.id.btn_setting);
        btnRanking = findViewById(R.id.btn_ranking);
        btnExit = findViewById(R.id.btn_exit);

        if (loginId==null || loginId.isEmpty()) {
            textMainLoginId.setText("로그인 후 진행할 수 있습니다.");
        } else {
            textMainLoginId.setText(loginId+"님 환영합니다.");
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loginId==null || loginId.isEmpty()) {
                    showLoginDialog();
                } else {
                    showLogoutCheckDialog();
                }
            }
        });

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showJoinDialog();
            }
        });

        // 방 입장
        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loginId==null || loginId.isEmpty()) {
                    Toast.makeText(MainActivity.this, "로그인 후 진행할 수 있습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, ListActivity.class);
                    intent.putExtra("loginId", loginId);
                    startActivity(intent);
                    finish();
                }
            }
        });

        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExitDialog();
            }
        });

        memberDao = new MemberDao();
        memberVO = new MemberVO();
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
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
                        if (memberDao.idPasswordCheck(loginEditId.getText()+"", loginEditPassword.getText()+"")==1) {
                            loginId = loginEditId.getText()+"";
                            textMainLoginId.setText(loginId+"님 환영합니다.");
                            btnLogin.setText("로그아웃");
                            Toast.makeText(MainActivity.this, "로그인되었습니다.",Toast.LENGTH_SHORT).show();
                            dialogInterface.cancel();
                        } else {
                            Toast.makeText(MainActivity.this, "ID 또는 비밀번호를 잘못 입력하였습니다.",Toast.LENGTH_SHORT).show();
                        }
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
                            Toast.makeText(MainActivity.this, "정확한 값을 입력하십시오.",Toast.LENGTH_SHORT).show();
                        } else if (memberDao.idExistCheck(joinEditId.getText()+"")>=1) {
                            Toast.makeText(MainActivity.this, "중복된 아이디입니다.",Toast.LENGTH_SHORT).show();
                        } else if (!((joinEditPassword.getText()+"").equals(joinEditRePassword.getText()+""))) {
                            Toast.makeText(MainActivity.this, "같은 비밀번호를 입력하십시오.",Toast.LENGTH_SHORT).show();
                        } else {
                            dialogInterface.cancel();
                            memberVO.setMemberId(joinEditId.getText()+"");
                            memberVO.setPassword(joinEditPassword.getText()+"");
                            if (memberDao.insertMember(memberVO)==0) {
                                Toast.makeText(MainActivity.this, "서버 오류",Toast.LENGTH_SHORT).show();
                            } else {
                                showJoinCompleteDialog();
                            }
                        }
                    }
                })
                .show();
    }

    // 회원가입 완료 다이얼로그
    private void showJoinCompleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("회원가입되었습니다.")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
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
                        textMainLoginId.setText("로그인 후 진행할 수 있습니다.");
                        btnLogin.setText("로그인");
                        Toast.makeText(MainActivity.this, "로그아웃되었습니다.",Toast.LENGTH_SHORT).show();
                        dialogInterface.cancel();
                    }
                })
                .show();
    }
}
