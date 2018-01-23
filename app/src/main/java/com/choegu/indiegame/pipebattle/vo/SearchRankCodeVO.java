package com.choegu.indiegame.pipebattle.vo;

import java.io.Serializable;

/**
 * Created by student on 2018-01-22.
 */

public class SearchRankCodeVO implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 5720988873416325912L;
    private String code;
    private String loginId;
    private int rating;
    private String message;
    private int portNum;
    private String player1;
    private String player2;
    private int player1Rating;
    private int player2Rating;

    public SearchRankCodeVO() {
    }

    public SearchRankCodeVO(String code, String loginId, int rating, String message, int portNum, String player1, String player2, int player1Rating, int player2Rating) {
        this.code = code;
        this.loginId = loginId;
        this.rating = rating;
        this.message = message;
        this.portNum = portNum;
        this.player1 = player1;
        this.player2 = player2;
        this.player1Rating = player1Rating;
        this.player2Rating = player2Rating;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getPortNum() {
        return portNum;
    }

    public void setPortNum(int portNum) {
        this.portNum = portNum;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public int getPlayer1Rating() {
        return player1Rating;
    }

    public void setPlayer1Rating(int player1Rating) {
        this.player1Rating = player1Rating;
    }

    public int getPlayer2Rating() {
        return player2Rating;
    }

    public void setPlayer2Rating(int player2Rating) {
        this.player2Rating = player2Rating;
    }

    @Override
    public String toString() {
        return "SearchRankCodeVO{" +
                "code='" + code + '\'' +
                ", loginId='" + loginId + '\'' +
                ", rating=" + rating +
                ", message='" + message + '\'' +
                ", portNum=" + portNum +
                ", player1='" + player1 + '\'' +
                ", player2='" + player2 + '\'' +
                ", player1Rating=" + player1Rating +
                ", player2Rating=" + player2Rating +
                '}';
    }
}
