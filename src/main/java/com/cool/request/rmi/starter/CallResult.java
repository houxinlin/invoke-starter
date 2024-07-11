package com.cool.request.rmi.starter;

public class CallResult implements java.io.Serializable {
    public static final long serialVersionUID = 1L;
    private boolean isSuccess = false;
    private boolean isError = false;
    private String msg = "";

    public CallResult(boolean isSuccess, boolean isError, String msg) {
        this.isSuccess = isSuccess;
        this.isError = isError;
        this.msg = msg;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
