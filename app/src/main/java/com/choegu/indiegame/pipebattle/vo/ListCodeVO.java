package com.choegu.indiegame.pipebattle.vo;

import java.io.Serializable;

public class ListCodeVO implements Serializable {
    private static final long serialVersionUID = 8451002734895044846L;
    private String code;
    private String id;
    private String title;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

	@Override
	public String toString() {
		return "ListCodeVO [code=" + code + ", id=" + id + ", title=" + title + "]";
	}
    
    
}