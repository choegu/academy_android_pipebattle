package com.choegu.indiegame.pipebattle.vo;

import java.io.Serializable;

/**
 * Created by student on 2018-01-19.
 */

public class MemberCodeVO implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 4724459025658943067L;
    private String code;
    private int memberNum;
    private String memberId;
    private String password;
    private String message;

    public MemberCodeVO() {
    }

    public MemberCodeVO(String code, int memberNum, String memberId, String password, String message) {
        this.code = code;
        this.memberNum = memberNum;
        this.memberId = memberId;
        this.password = password;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getMemberNum() {
        return memberNum;
    }

    public void setMemberNum(int memberNum) {
        this.memberNum = memberNum;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "MemberCodeVO{" +
                "code='" + code + '\'' +
                ", memberNum=" + memberNum +
                ", memberId='" + memberId + '\'' +
                ", password='" + password + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
