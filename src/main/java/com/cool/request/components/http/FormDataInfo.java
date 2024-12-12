package com.cool.request.components.http;


import java.io.Serializable;

public class FormDataInfo extends RequestParameterDescription implements Cloneable, Serializable {
    private static final long serialVersionUID = 1000000;

    public FormDataInfo(String name, String value, String type) {
        super(name, type, "");
    }

    @Override
    public FormDataInfo clone() {
        return new FormDataInfo(getKey(), getValue(), getType());
    }
}
