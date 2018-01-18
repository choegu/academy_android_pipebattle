package com.choegu.indiegame.pipebattle.vo;

/**
 * Created by student on 2018-01-18.
 */

public class MemberVO {
    private int memberNum;
    private String memberId;
    private String password;

    public MemberVO() {
    }

    public MemberVO(int memberNum, String memberId, String password) {
        this.memberNum = memberNum;
        this.memberId = memberId;
        this.password = password;
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

    @Override
    public String toString() {
        return "MemberVO{" +
                "memberNum=" + memberNum +
                ", memberId='" + memberId + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
