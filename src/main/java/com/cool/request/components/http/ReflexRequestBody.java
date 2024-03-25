package com.cool.request.components.http;

import java.io.Serializable;

public class ReflexRequestBody implements Serializable {
    private static final long serialVersionUID = 1000000;
    private String type;
    private String id;


    public String getId() {
        return id;
    }

    public ReflexRequestBody() {
        this.type = getType();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return "";
    }

    public void setType(String type) {
        this.type = type;
    }
}
