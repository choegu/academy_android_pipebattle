package com.choegu.indiegame.pipebattle.vo;

import java.io.Serializable;

/**
 * Created by student on 2018-01-10.
 */

public class RoomVO implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -2588909321158535316L;
	private int roomNum;
    private String title;
    private String createId;
    private int playerNum;
    private int portNum;

    public RoomVO() {
    }

    public RoomVO(int roomNum, String title, String createId, int playerNum, int portNum) {
        this.roomNum = roomNum;
        this.title = title;
        this.createId = createId;
        this.playerNum = playerNum;
        this.portNum = portNum;
    }

    public int getRoomNum() {
        return roomNum;
    }

    public void setRoomNum(int roomNum) {
        this.roomNum = roomNum;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreateId() {
        return createId;
    }

    public void setCreateId(String createId) {
        this.createId = createId;
    }

    public int getPlayerNum() {
        return playerNum;
    }

    public void setPlayerNum(int playerNum) {
        this.playerNum = playerNum;
    }

    public int getPortNum() {
        return portNum;
    }

    public void setPortNum(int portNum) {
        this.portNum = portNum;
    }

    @Override
    public String toString() {
        return "RoomVO{" +
                "roomNum=" + roomNum +
                ", title='" + title + '\'' +
                ", createId='" + createId + '\'' +
                ", playerNum=" + playerNum +
                ", portNum=" + portNum +
                '}';
    }
}
