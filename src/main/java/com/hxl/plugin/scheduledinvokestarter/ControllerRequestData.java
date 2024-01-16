package com.hxl.plugin.scheduledinvokestarter;

import java.util.ArrayList;
import java.util.List;

public class ControllerRequestData {
    private  String type = "controller";
    private String url;
    private String contentType;
    private List<FormDataInfo> formData =new ArrayList<>();
    private String body; //json xml raw bin urlencoded
    private String id;
    private boolean useProxyObject;
    private boolean useInterceptor;
    private boolean userFilter;
    private List<KeyValue> headers =new ArrayList<>();
    private String method;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public List<FormDataInfo> getFormData() {
        return formData;
    }

    public void setFormData(List<FormDataInfo> formData) {
        this.formData = formData;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isUseProxyObject() {
        return useProxyObject;
    }

    public void setUseProxyObject(boolean useProxyObject) {
        this.useProxyObject = useProxyObject;
    }

    public boolean isUseInterceptor() {
        return useInterceptor;
    }

    public void setUseInterceptor(boolean useInterceptor) {
        this.useInterceptor = useInterceptor;
    }

    public boolean isUserFilter() {
        return userFilter;
    }

    public void setUserFilter(boolean userFilter) {
        this.userFilter = userFilter;
    }

    public List<KeyValue> getHeaders() {
        return headers;
    }

    public void setHeaders(List<KeyValue> headers) {
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
