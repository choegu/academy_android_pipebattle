package com.choegu.indiegame.pipebattle.vo;

import java.io.Serializable;

/**
 * Created by student on 2018-01-15.
 */

public class GameCodeVO implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 6297861377560671876L;
    private String code;
    private String id;
    private int tileNum;

    public GameCodeVO() {
    }

    public GameCodeVO(String code, String id, int tileNum) {
        this.code = code;
        this.id = id;
        this.tileNum = tileNum;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
                ", id='" + id + '\'' +
                ", tileNum=" + tileNum +
                '}';
    }
}
