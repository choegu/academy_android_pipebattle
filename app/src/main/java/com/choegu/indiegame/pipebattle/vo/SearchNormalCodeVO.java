package com.choegu.indiegame.pipebattle.vo;

import java.io.Serializable;

/**
 * Created by student on 2018-01-19.
 */

public class SearchNormalCodeVO implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 6581743526924840543L;
    private String code;
    private String loginId;
    private String message;
    private int portNum;

    public SearchNormalCodeVO() {
    }

    public SearchNormalCodeVO(String code, String loginId, String message, int portNum) {
        this.code = code;
        this.loginId = loginId;
        this.message = message;
        this.portNum = portNum;
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

    @Override
    public String toString() {
        return "SearchNormalCodeVO{" +
                "code='" + code + '\'' +
                ", loginId='" + loginId + '\'' +
                ", message='" + message + '\'' +
                ", portNum=" + portNum +
                '}';
    }
}
