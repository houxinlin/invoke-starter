package com.cool.request.components.http;

import java.io.Serializable;

public class RequestParameterDescription implements Serializable {
    private static final long serialVersionUID = 1000000;
    private String key;
    private String type;
    private String description;

    public RequestParameterDescription(String key, String type, String description) {
        this.key = key;
        this.type = type;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
