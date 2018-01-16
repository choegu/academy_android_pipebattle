package com.choegu.indiegame.pipebattle.vo;

import java.io.Serializable;

/**
 * Created by student on 2018-01-16.
 */

public class FinishCheckVO implements Serializable{
    private int tileNum;
    private int direction;

    public static final int ERROR = 0;
    public static final int EAST = 1;
    public static final int WEST = 2;
    public static final int SOUTH = 3;
    public static final int NORTH = 4;
    public static final int COMPLETE = 5;

    public FinishCheckVO() {
    }

    public FinishCheckVO(int tileNum, int Direction) {
        this.tileNum = tileNum;
        this.direction = Direction;
    }

    public int getTileNum() {
        return tileNum;
    }

    public void setTileNum(int tileNum) {
        this.tileNum = tileNum;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int Direction) {
        this.direction = Direction;
    }
    
    public void setTileNumPlus1() {
        tileNum++;
    }
    public void setTileNumPlus7() {
        tileNum = tileNum+7;
    }
    public void setTileNumMinus1() {
        tileNum--;
    }
    public void setTileNumMinus7() {
        tileNum = tileNum-7;
    }

    public void setDirectionError() {
        direction = ERROR;
    }
    public void setDirectionEast() {
        direction = EAST;
    }
    public void setDirectionWest() {
        direction = WEST;
    }
    public void setDirectionSouth() {
        direction = SOUTH;
    }
    public void setDirectionNorth() {
        direction = NORTH;
    }
    public void setDirectionComplete() {
        direction = COMPLETE;
    }

    @Override
    public String toString() {
        return "FinishCheckVO{" +
                "tileNum=" + tileNum +
                ", Direction='" + direction + '\'' +
                '}';
    }
}
