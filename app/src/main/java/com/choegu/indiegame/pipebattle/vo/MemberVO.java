package com.choegu.indiegame.pipebattle.vo;

import java.io.Serializable;

/**
 * Created by student on 2018-01-18.
 */

public class MemberVO implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 7886493010096239090L;
    private int memberNum;
    private String memberId;
    private String password;
    private int rating;
    private String tier;

    public MemberVO() {
    }

    public MemberVO(int memberNum, String memberId, String password, int rating, String tier) {
        this.memberNum = memberNum;
        this.memberId = memberId;
        this.password = password;
        this.rating = rating;
        this.tier = tier;
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    @Override
    public String toString() {
        return "MemberVO{" +
                "memberNum=" + memberNum +
                ", memberId='" + memberId + '\'' +
                ", password='" + password + '\'' +
                ", rating=" + rating +
                ", tier='" + tier + '\'' +
                '}';
    }
}
