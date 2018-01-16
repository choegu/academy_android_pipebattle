package com.choegu.indiegame.pipebattle.vo;

import java.io.Serializable;

/**
 * Created by student on 2018-01-15.
 */

public class GameCodeVO implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 9144581995428760941L;
    private String code;
    private String player1;
    private String player2;
    private int tileType;
    private int tileNum;

    public GameCodeVO() {
    }

    public GameCodeVO(String code, String player1, String player2, int tileType, int tileNum) {
        this.code = code;
        this.player1 = player1;
        this.player2 = player2;
        this.tileType = tileType;
        this.tileNum = tileNum;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public int getTileType() {
        return tileType;
    }

    public void setTileType(int tileType) {
        this.tileType = tileType;
    }

    public int getTileNum() {
        return tileNum;
    }

    public void setTileNum(int tileNum) {
        this.tileNum = tileNum;
    }

    @Override
    public String toString() {
        return "GameCodeVO{" +
                "code='" + code + '\'' +
                ", player1='" + player1 + '\'' +
                ", player2='" + player2 + '\'' +
                ", tileType=" + tileType +
                ", tileNum=" + tileNum +
                '}';
    }
}
