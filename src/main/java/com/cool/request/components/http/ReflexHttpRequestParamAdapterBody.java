package com.cool.request.components.http;


import java.util.ArrayList;
import java.util.List;

/**
 * 发生调用时候发起的数据包，将来把这里优化掉
 */
public class ReflexHttpRequestParamAdapterBody extends ReflexRequestBody {
    private static final long serialVersionUID = 1000000;
    private String url;
    private String contentType;
    private List<FormDataInfo> formData = new ArrayList<>();
    private String body; //json xml raw bin urlencoded
    private boolean useProxyObject;
    private boolean useInterceptor;
    private boolean userFilter;
    private List<KeyValue> headers = new ArrayList<>();
    private String method;
    private Object attachData; //附加数据

    public ReflexHttpRequestParamAdapterBody() {
    }

    public Object getAttachData() {
        return attachData;
    }

    public void setAttachData(Object attachData) {
        this.attachData = attachData;
    }

    @Override
    public String getType() {
        return "controller";
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

    @Override
    public String toString() {
        return "ReflexHttpRequestParamAdapterBody{" +
                "url='" + url + '\'' +
                ", contentType='" + contentType + '\'' +
                ", formData=" + formData +
                ", body='" + body + '\'' +
                ", useProxyObject=" + useProxyObject +
                ", useInterceptor=" + useInterceptor +
                ", userFilter=" + userFilter +
                ", headers=" + headers +
                ", method='" + method + '\'' +
                ", attachData=" + attachData +
                ", type=" + getType() +
                ", id=" + getId() +
                '}';
    }
}
