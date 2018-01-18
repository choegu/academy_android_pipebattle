package com.choegu.indiegame.pipebattle.dao;

import android.util.Log;

import com.choegu.indiegame.pipebattle.vo.MemberVO;
import com.choegu.indiegame.pipebattle.vo.OptionValue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by student on 2018-01-18.
 */

public class MemberDao {
    private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://"+ OptionValue.serverIp+":"+OptionValue.dbPort+"/"+OptionValue.dbName;
    private static final String DB_ID = OptionValue.dbId;
    private static final String DB_PW = OptionValue.dbPassword;

    private Connection con;
    private PreparedStatement pstmt;
    private ResultSet rs;

    public MemberDao() {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            Log.e("chs", "jar 드라이버 로딩 오류");
            e.printStackTrace();
        }
    }

    // 회원가입
    public int insertMember(MemberVO member) {
        int result = 0;

        try {
            con = DriverManager.getConnection(DB_URL, DB_ID, DB_PW);
            String sql = "INSERT INTO MEMBERS(MEMBER_ID, PASSWORD) VALUES(?,?)";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setString(2, member.getPassword());

            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            Log.e("chs", "insertMember error");
            e.printStackTrace();
        }

        return result;
    }

    // 중복검사
    public int idExistCheck(String memberId) {
        int result = 0;

        try {
            con = DriverManager.getConnection(DB_URL, DB_ID, DB_PW);
            String sql = "SELECT COUNT(*) FROM MEMBERS WHERE MEMBER_ID=?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                result = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    // 로그인 체크
    public int idPasswordCheck(String memberId, String password) {
        int result = 0;

        try {
            con = DriverManager.getConnection(DB_URL, DB_ID, DB_PW);
            String sql = "SELECT COUNT(*) FROM MEMBERS WHERE MEMBER_ID=? AND PASSWORD=?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.setString(2,password);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                result = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }
}
