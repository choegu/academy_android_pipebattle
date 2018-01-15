package com.choegu.indiegame.pipebattle.vo;

import java.io.Serializable;

public class ReadyCodeVO implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = -7062838983670898374L;
    private String code;
    private String id;
    private String player1;
    private String player2;
    private String observer1;
    private String observer2;
    private String observer3;
    private String observer4;
    private String message;

    public ReadyCodeVO() {
        // TODO Auto-generated constructor stub
    }

    public ReadyCodeVO(String code, String id, String player1, String player2, String observer1, String observer2,
                       String observer3, String observer4, String message) {
        super();
        this.code = code;
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
        this.observer1 = observer1;
        this.observer2 = observer2;
        this.observer3 = observer3;
        this.observer4 = observer4;
        this.message = message;
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

    public String getObserver1() {
        return observer1;
    }

    public void setObserver1(String observer1) {
        this.observer1 = observer1;
    }

    public String getObserver2() {
        return observer2;
    }

    public void setObserver2(String observer2) {
        this.observer2 = observer2;
    }

    public String getObserver3() {
        return observer3;
    }

    public void setObserver3(String observer3) {
        this.observer3 = observer3;
    }

    public String getObserver4() {
        return observer4;
    }

    public void setObserver4(String observer4) {
        this.observer4 = observer4;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ReadyCodeVO [code=" + code + ", id=" + id + ", player1=" + player1 + ", player2=" + player2
                + ", observer1=" + observer1 + ", observer2=" + observer2 + ", observer3=" + observer3 + ", observer4="
                + observer4 + ", message=" + message + "]";
    }


}
